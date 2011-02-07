/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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

  public static double optionPrice(final double f, final double k, final double discountFactor, final double sigma, final double t, final boolean isCall) {

    // if ((isCall && f > k) || (!isCall && k > f)) {
    // // price in-money options as out-the-money and use put-call parity
    // double price = optionPriceImp(f, k, discountFactor, sigma, t, !isCall);
    // return price + discountFactor * Math.abs(f - k);
    // }
    return optionPriceImp(f, k, discountFactor, sigma, t, isCall);
  }

  private static double optionPriceImp(final double f, final double k, final double discountFactor, final double sigma, final double t, final boolean isCall) {

    final int sign = isCall ? 1 : -1;
    final double simgaRootT = sigma * Math.sqrt(t);
    if (simgaRootT < 1e-16) {
      final double x = sign * (f - k);
      return (x > 0 ? discountFactor * x : 0.0);
    }
    final double d1 = getD1(f, k, simgaRootT);
    final double d2 = d1 - simgaRootT;

    return sign * discountFactor * (f * NORMAL.getCDF(sign * d1) - k * NORMAL.getCDF(sign * d2));
  }

  private static double getD1(final double f, final double k, final double sigmaRootT) {
    final double numerator = (Math.log(f / k) + sigmaRootT * sigmaRootT / 2);
    if (CompareUtils.closeEquals(numerator, 0, 1e-16)) {
      return 0;
    }
    return numerator / sigmaRootT;
  }

}
