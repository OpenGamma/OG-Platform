/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
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
    final double k = option.getK();
    final double t = option.getT();
    return new Function1D<BlackFunctionData, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public final Double evaluate(final BlackFunctionData data) {
        Validate.notNull(data, "data");
        final double f = data.getF();
        final double discountFactor = data.getDf();
        final double sigma = data.getSigma();
        final double sigmaRootT = sigma * Math.sqrt(t);
        final int sign = option.isCall() ? 1 : -1;
        final double arg = sign * (f - k) / sigmaRootT;
        return discountFactor * (sign * (f - k) * NORMAL.getCDF(arg) + sigmaRootT * NORMAL.getPDF(arg));
      }
    };
  }
}
