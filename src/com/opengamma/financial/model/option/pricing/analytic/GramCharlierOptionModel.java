/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.SkewKurtosisOptionDataBundle;
import com.opengamma.financial.model.option.pricing.OptionPricingException;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.InterpolationException;
import com.opengamma.math.statistics.distribution.NormalProbabilityDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 * @author emcleod
 * 
 */
public class GramCharlierOptionModel extends AnalyticOptionModel<EuropeanVanillaOptionDefinition, SkewKurtosisOptionDataBundle> {
  ProbabilityDistribution<Double> _normalProbabilityDistribution = new NormalProbabilityDistribution(0, 1);

  @Override
  public Function1D<SkewKurtosisOptionDataBundle, Double> getPricingFunction(final EuropeanVanillaOptionDefinition definition) {
    final Function1D<SkewKurtosisOptionDataBundle, Double> pricingFunction = new Function1D<SkewKurtosisOptionDataBundle, Double>() {

      @Override
      public Double evaluate(final SkewKurtosisOptionDataBundle data) {
        try {
          final double s = data.getSpot();
          final double k = definition.getStrike();
          final double t = definition.getTimeToExpiry(data.getDate());
          final double sigma = data.getVolatility(t, k);
          final double r = data.getInterestRate(t);
          final double b = data.getCostOfCarry();
          final double sqrtT = Math.sqrt(t);
          final double skew = data.getSkew() / sqrtT;
          final double kurtosis = data.getKurtosis() / t;
          final double d1 = getD1(s, k, t, sigma, b);
          final double sigmaT = sigma * sqrtT;
          final double df1 = Math.exp(-r * t);
          final double df2 = getDF(r, b, t);
          final double callPrice = s * df2 * _normalProbabilityDistribution.getCDF(d1) - k * df1 * _normalProbabilityDistribution.getCDF(d1 - sigmaT) + s
              * _normalProbabilityDistribution.getPDF(d1) * sigmaT * (skew * (2 * sigmaT - d1) / 6. - kurtosis * (1 - d1 * d1 + 3 * d1 * sigmaT - 3 * sigmaT * sigmaT) / 24.);
          if (!definition.isCall())
            return callPrice + k * df1 - s * df2;
          return callPrice;
        } catch (final InterpolationException e) {
          throw new OptionPricingException(e);
        }
      }
    };
    return pricingFunction;
  }
}
