/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import com.opengamma.financial.model.option.definition.SimpleChooserOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.time.DateUtil;

/**
 * 
 * Pricing model for simple chooser options.
 * 
 */
public class SimpleChooserOptionModel extends AnalyticOptionModel<SimpleChooserOptionDefinition, StandardOptionDataBundle> {
  private final ProbabilityDistribution<Double> _normalProbabilityDistribution = new NormalDistribution(0, 1);

  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final SimpleChooserOptionDefinition definition) {
    final Function1D<StandardOptionDataBundle, Double> pricingFunction = new Function1D<StandardOptionDataBundle, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final StandardOptionDataBundle data) {
        final double s = data.getSpot();
        final double k = definition.getStrike();
        final double t1 = DateUtil.getDifferenceInYears(data.getDate(), definition.getChooseDate());
        final double t2 = definition.getTimeToExpiry(data.getDate());
        final double b = data.getCostOfCarry();
        final double r = data.getInterestRate(t1);
        final double sigma = data.getVolatility(t1, k);
        final double sigmaT1 = sigma * Math.sqrt(t1);
        final double sigmaT2 = sigma * Math.sqrt(t2);
        final double sigmaSq = sigma * sigma / 2.;
        final double logSK = Math.log(s / k);
        final double bT2 = b * t2;
        final double d = getD(logSK, bT2, sigmaSq * t2, sigmaT2);
        final double y = getD(logSK, bT2, sigmaSq * t1, sigmaT1);
        final double df1 = getDF(r, b, t2);
        final double df2 = getDF(r, 0, t2);
        return s * df1 * (_normalProbabilityDistribution.getCDF(d) - _normalProbabilityDistribution.getCDF(-y)) - k * df2
            * (_normalProbabilityDistribution.getCDF(d - sigmaT2) - _normalProbabilityDistribution.getCDF(-y + sigmaT1));
      }

    };
    return pricingFunction;
  }

  double getD(final double x, final double y, final double z, final double sigmaT) {
    return (x + y + z) / sigmaT;
  }
}
