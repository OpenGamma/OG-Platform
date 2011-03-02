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
import com.opengamma.util.CompareUtils;

/**
 * 
 */
public class BlackPriceFunction implements OptionPriceFunction<BlackFunctionData> {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  @Override
  public Function1D<BlackFunctionData, Double> getPriceFunction(final EuropeanVanillaOption option) {
    Validate.notNull(option, "option");
    final double k = option.getK();
    final double t = option.getT();
    return new Function1D<BlackFunctionData, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final BlackFunctionData data) {
        Validate.notNull(data, "data");
        final double sigma = data.getSigma();
        final double f = data.getF();
        final double discountFactor = data.getDf();
        final int sign = option.isCall() ? 1 : -1;
        final double sigmaRootT = sigma * Math.sqrt(t);
        if (f == k) {
          return discountFactor * f * (2 * NORMAL.getCDF(sigmaRootT / 2) - 1);
        }
        if (sigmaRootT < 1e-16) {
          final double x = sign * (f - k);
          return (x > 0 ? discountFactor * x : 0.0);
        }
        final double d1 = getD1(f, k, sigmaRootT);
        final double d2 = d1 - sigmaRootT;

        return sign * discountFactor * (f * NORMAL.getCDF(sign * d1) - k * NORMAL.getCDF(sign * d2));
      }
    };
  }

  public Function1D<BlackFunctionData, Double> getVegaFunction(final EuropeanVanillaOption option) {
    Validate.notNull(option, "option");
    final double k = option.getK();
    final double t = option.getT();
    return new Function1D<BlackFunctionData, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final BlackFunctionData data) {
        Validate.notNull(data, "data");
        final double sigma = data.getSigma();
        final double f = data.getF();
        final double discountFactor = data.getDf();
        final double rootT = Math.sqrt(t);
        final double d1 = getD1(f, k, sigma * rootT);
        return f * rootT * discountFactor * NORMAL.getPDF(d1);
      }

    };
  }

  private static double getD1(final double f, final double k, final double sigmaRootT) {
    final double numerator = (Math.log(f / k) + sigmaRootT * sigmaRootT / 2);
    if (CompareUtils.closeEquals(numerator, 0, 1e-16)) {
      return 0;
    }
    return numerator / sigmaRootT;
  }

}
