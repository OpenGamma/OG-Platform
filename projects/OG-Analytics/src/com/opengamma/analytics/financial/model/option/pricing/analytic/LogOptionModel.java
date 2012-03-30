/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.definition.LogOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 * Pricing model for log options.
 * 
 */
public class LogOptionModel extends AnalyticOptionModel<LogOptionDefinition, StandardOptionDataBundle> {
  private final ProbabilityDistribution<Double> _normalProbabilityDistribution = new NormalDistribution(0, 1);

  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final LogOptionDefinition definition) {
    Validate.notNull(definition);
    final Function1D<StandardOptionDataBundle, Double> pricingFunction = new Function1D<StandardOptionDataBundle, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final StandardOptionDataBundle data) {
        Validate.notNull(data);
        final double s = data.getSpot();
        final double k = definition.getStrike();
        final double t = definition.getTimeToExpiry(data.getDate());
        final double b = data.getCostOfCarry();
        final double r = data.getInterestRate(t);
        final double sigma = data.getVolatility(t, k);
        final double df = Math.exp(-r * t);
        final double sigmaT = sigma * Math.sqrt(t);
        final double x = (Math.log(s / k) + t * (b - sigma * sigma * 0.5)) / sigmaT;
        return df * sigmaT * (_normalProbabilityDistribution.getPDF(x) + x * _normalProbabilityDistribution.getCDF(x));
      }

    };
    return pricingFunction;
  }

}
