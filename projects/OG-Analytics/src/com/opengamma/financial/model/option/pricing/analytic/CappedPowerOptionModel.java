/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.definition.CappedPowerOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 * 
 */
public class CappedPowerOptionModel extends AnalyticOptionModel<CappedPowerOptionDefinition, StandardOptionDataBundle> {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final CappedPowerOptionDefinition definition) {
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
        final double inv = 1. / power;
        final double cap = definition.getCap();
        final boolean isCall = definition.isCall();
        final double sigmaT = sigma * Math.sqrt(t);
        final double x = t * (b + sigma * sigma * (power - 0.5));
        final double d1 = getD(s / Math.pow(k, 1. / power), x, sigmaT);
        final double d2 = d1 - power * sigmaT;
        final double d3 = getD(isCall ? s / Math.pow(k + cap, inv) : s / Math.pow(k - cap, inv), x, sigmaT);
        final double d4 = d3 - power * sigmaT;
        final int sign = isCall ? 1 : -1;
        final double df1 = Math.exp(-r * t);
        final double df2 = Math.exp(t * ((power - 1) * (r + power * sigma * sigma * 0.5) - power * (r - b)));
        final double term1 = Math.pow(s, power) * df2 * (NORMAL.getCDF(sign * d1) - NORMAL.getCDF(sign * d3));
        final double term2 = df1 * (k * NORMAL.getCDF(sign * d2) - (k + sign * cap) * NORMAL.getCDF(sign * d4));
        return sign * (term1 - term2);
      }

    };
    return pricingFunction;
  }

  double getD(final double x, final double y, final double z) {
    return (Math.log(x) + y) / z;
  }
}
