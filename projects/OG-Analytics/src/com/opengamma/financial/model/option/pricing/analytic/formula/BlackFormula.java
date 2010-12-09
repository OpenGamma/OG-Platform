/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.CompareUtils;

/**
 * 
 */
public class BlackFormula {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  public static double optionPrice(final double f, final double k, final double discountFactor, final double sigma, final double t, boolean isCall) {

    final double simgaRootT = sigma * Math.sqrt(t);
    if (simgaRootT < 1e-16) {
      return (f > k ? discountFactor * (f - k) : 0.0);
    }
    final double d1 = getD1(f, k, simgaRootT);
    final double d2 = d1 - simgaRootT;
    final int sign = isCall ? 1 : -1;
    return sign * discountFactor * (f * NORMAL.getCDF(sign * d1) - k * NORMAL.getCDF(sign * d2));
  }

  private static double getD1(final double f, final double k, final double simgaRootT) {
    final double numerator = (Math.log(f / k) + simgaRootT * simgaRootT / 2);
    if (CompareUtils.closeEquals(numerator, 0, 1e-16)) {
      return 0;
    }
    return numerator / simgaRootT;
  }

}
