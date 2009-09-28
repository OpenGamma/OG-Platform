/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import com.opengamma.financial.model.option.definition.SimpleChooserOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalProbabilityDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.time.DateUtil;

/**
 * 
 * Pricing model for simple chooser options.
 * 
 * @author emcleod
 */
public class SimpleChooserOptionModel extends AnalyticOptionModel<SimpleChooserOptionDefinition, StandardOptionDataBundle> {
  final ProbabilityDistribution<Double> _normalProbabilityDistribution = new NormalProbabilityDistribution(0, 1);

  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final SimpleChooserOptionDefinition definition) {
    Function1D<StandardOptionDataBundle, Double> pricingFunction = new Function1D<StandardOptionDataBundle, Double>() {

      @Override
      public Double evaluate(StandardOptionDataBundle data) {
        double s = data.getSpot();
        double k = definition.getStrike();
        double t1 = DateUtil.getDifferenceInYears(data.getDate(), definition.getChooseDate());
        double t2 = definition.getTimeToExpiry(data.getDate());
        double b = data.getCostOfCarry();
        double r = data.getInterestRate(t1);
        double sigma = data.getVolatility(t1, k);
        double sigmaT1 = sigma * Math.sqrt(t1);
        double sigmaT2 = sigma * Math.sqrt(t2);
        double sigmaSq = sigma * sigma / 2.;
        double logSK = Math.log(s / k);
        double bT2 = b * t2;
        double d = getD(logSK, bT2, sigmaSq * t2, sigmaT2);
        double y = getD(logSK, bT2, sigmaSq * t1, sigmaT1);
        double df1 = getDF(r, b, t2);
        double df2 = getDF(r, 0, t2);
        return s * df1 * (_normalProbabilityDistribution.getCDF(d) - _normalProbabilityDistribution.getCDF(-y)) - k * df2
            * (_normalProbabilityDistribution.getCDF(d - sigmaT2) - _normalProbabilityDistribution.getCDF(-y + sigmaT1));
      }

    };
    return pricingFunction;
  }

  double getD(double x, double y, double z, double sigmaT) {
    return (x + y + z) / sigmaT;
  }
}
