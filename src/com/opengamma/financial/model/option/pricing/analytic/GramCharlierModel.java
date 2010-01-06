/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.SkewKurtosisOptionDataBundle;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 * @author emcleod
 * 
 */
public class GramCharlierModel extends AnalyticOptionModel<OptionDefinition, SkewKurtosisOptionDataBundle> {
  final ProbabilityDistribution<Double> _normal = new NormalDistribution(0, 1);

  @Override
  public Function1D<SkewKurtosisOptionDataBundle, Double> getPricingFunction(final OptionDefinition definition) {
    if (definition == null)
      throw new IllegalArgumentException("Option definition was null");
    final Function1D<SkewKurtosisOptionDataBundle, Double> pricingFunction = new Function1D<SkewKurtosisOptionDataBundle, Double>() {

      @Override
      public Double evaluate(final SkewKurtosisOptionDataBundle data) {
        if (data == null)
          throw new IllegalArgumentException("Data bundle was null");
        final double s = data.getSpot();
        final double k = definition.getStrike();
        final double t = definition.getTimeToExpiry(data.getDate());
        final double b = data.getCostOfCarry();
        final double r = data.getInterestRate(t);
        final double sigma = data.getVolatility(t, k);
        final double sigmaT = sigma * Math.sqrt(t);
        final double d1 = getD1(s, k, t, sigma, b);
        final double d2 = getD2(d1, sigma, t);
        final double skew = data.getAnnualizedSkew();
        final double kurtosis = data.getAnnualizedPearsonKurtosis();
        final double correction = sigmaT * (skew * (2 * sigmaT - d1) / (6. * Math.sqrt(t)) - kurtosis * (1 - d1 * d1 + 3 * sigmaT * (d1 - sigmaT)) / (24 * t));
        final double df1 = Math.exp(-r * t);
        final double df2 = getDF(r, b, t);
        final double callPrice = s * df2 * (_normal.getCDF(d1) + _normal.getPDF(d1) * correction) - k * df1 * _normal.getCDF(d2);
        if (!definition.isCall())
          return callPrice + k * df1 - s * df2;
        return callPrice;
      }
    };
    return pricingFunction;
  }
}
