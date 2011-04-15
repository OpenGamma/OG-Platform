/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 */
public class NormalPriceFunction implements OptionPriceFunction<BlackFunctionData> {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  @Override
  public Function1D<BlackFunctionData, Double> getPriceFunction(final EuropeanVanillaOption option) {
    Validate.notNull(option, "option");
    final double k = option.getStrike();
    final double t = option.getTimeToExpiry();
    return new Function1D<BlackFunctionData, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public final Double evaluate(final BlackFunctionData data) {
        Validate.notNull(data, "data");
        final double f = data.getForward();
        final double discountFactor = data.getDiscountFactor();
        final double sigma = data.getBlackVolatility();
        final double sigmaRootT = sigma * Math.sqrt(t);
        final int sign = option.isCall() ? 1 : -1;
        if (sigmaRootT < 1e-16) {
          final double x = sign * (f - k);
          return (x > 0 ? discountFactor * x : 0.0);
        }
        final double arg = sign * (f - k) / sigmaRootT;
        return discountFactor * (sign * (f - k) * NORMAL.getCDF(arg) + sigmaRootT * NORMAL.getPDF(arg));
      }
    };
  }
}
