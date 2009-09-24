/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import com.opengamma.financial.model.option.definition.LogOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalProbabilityDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 * Pricing model for log options.
 * 
 * @author emcleod
 */
public class LogOptionModel extends AnalyticOptionModel<LogOptionDefinition, StandardOptionDataBundle> {
  final ProbabilityDistribution<Double> _normalProbabilityDistribution = new NormalProbabilityDistribution(0, 1);

  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final LogOptionDefinition definition) {
    Function1D<StandardOptionDataBundle, Double> pricingFunction = new Function1D<StandardOptionDataBundle, Double>() {

      @Override
      public Double evaluate(StandardOptionDataBundle data) {
        double s = data.getSpot();
        double k = definition.getStrike();
        double t = definition.getTimeToExpiry(data.getDate());
        double b = data.getCostOfCarry();
        double r = data.getInterestRate(t);
        double sigma = data.getVolatility(t, k);
        double df = Math.exp(-r * t);
        double sigmaT = sigma * Math.sqrt(t);
        double x = (Math.log(s / k) + t * (b - sigma * sigma * 0.5)) / sigmaT;
        return df * sigmaT * (_normalProbabilityDistribution.getPDF(x) + x * _normalProbabilityDistribution.getCDF(x));
      }

    };
    return pricingFunction;
  }

}
