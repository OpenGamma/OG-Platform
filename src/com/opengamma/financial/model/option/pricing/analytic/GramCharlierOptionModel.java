/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.SkewKurtosisOptionDataBundle;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalProbabilityDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 * @author emcleod
 * 
 */
public class GramCharlierOptionModel extends AnalyticOptionModel<OptionDefinition, SkewKurtosisOptionDataBundle> {
  ProbabilityDistribution<Double> _normal = new NormalProbabilityDistribution(0, 1);

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
        final double periodsPerYear = data.getPeriodsPerYear();
        final double sqrtN = Math.sqrt(periodsPerYear);
        final double periodsToExpiry = definition.getTimeToExpiry(data.getDate()) * periodsPerYear;
        final double sqrtT = Math.sqrt(periodsToExpiry);
        final double sigmaT = data.getVolatility(t, k) * sqrtT / sqrtN;
        final double r = data.getInterestRate(t) / periodsPerYear;
        final double b = data.getCostOfCarry() / periodsPerYear;
        final double d = (Math.log(s / k) + periodsToExpiry * b + sigmaT * sigmaT / 2) / sigmaT;
        final double gamma1T = data.getOnePeriodSkew() / sqrtT;
        final double gamma2T = data.getOnePeriodKurtosis() / periodsToExpiry;
        final double df1 = Math.exp(-r * periodsToExpiry);
        final double df2 = getDF(r, b, periodsToExpiry);
        final double correction = sigmaT * (gamma1T * (2 * sigmaT - d) / 6. - gamma2T * (1 - d * d + 3 * d * sigmaT - 3 * sigmaT * sigmaT) / 24.);
        final double callPrice = s * df2 * (_normal.getCDF(d) + _normal.getPDF(d) * correction) - k * df1 * _normal.getCDF(d - sigmaT);
        if (!definition.isCall())
          return callPrice + k * df1 - s * df2;
        return callPrice;
      }
    };
    return pricingFunction;
  }
}
