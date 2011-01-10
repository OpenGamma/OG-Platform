/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.definition.AsymmetricPowerOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * Pricing model for asymmetric power options.
 * 
 */
public class AsymmetricPowerOptionModel extends AnalyticOptionModel<AsymmetricPowerOptionDefinition, StandardOptionDataBundle> {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final AsymmetricPowerOptionDefinition definition) {
    Validate.notNull(definition);
    final Function1D<StandardOptionDataBundle, Double> pricingFunction = new Function1D<StandardOptionDataBundle, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final StandardOptionDataBundle data) {
        Validate.notNull(data);
        final double s = data.getSpot();
        final double k = definition.getStrike();
        final double t = definition.getTimeToExpiry(data.getDate());
        final double sigma = data.getVolatility(t, k);
        final double r = data.getInterestRate(t);
        final double b = data.getCostOfCarry();
        final double power = definition.getPower();
        final double sigmaT = sigma * Math.sqrt(t);
        final double d1 = (Math.log(s / Math.pow(k, 1. / power)) + t * (b + sigma * sigma * (power - 0.5))) / sigmaT;
        final double d2 = d1 - power * sigmaT;
        final int sign = definition.isCall() ? 1 : -1;
        final double df1 = Math.exp(((power - 1) * (r + power * sigma * sigma * 0.5) - power * (r - b)) * t);
        final double df2 = Math.exp(-r * t);
        return sign * (Math.pow(s, power) * df1 * NORMAL.getCDF(sign * d1) - df2 * k * NORMAL.getCDF(sign * d2));
      }
    };
    return pricingFunction;
  }

}
