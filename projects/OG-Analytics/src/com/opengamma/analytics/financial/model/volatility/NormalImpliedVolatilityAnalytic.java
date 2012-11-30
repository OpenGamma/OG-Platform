/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.NormalFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.NormalPriceFunction;

/**
 *
 * Computes the implied normal volatility, using the analytic approximation of
 * J. Choi, K Kim and M. Kwak (2009), "Numerical Approximation of the Implied Volatility
 * Under Arithmetic Brownian Motion", Applied Math. Finance, 16(3), pp. 261-268
 *
 */
public final class NormalImpliedVolatilityAnalytic implements NormalImpliedVolatility {

  private static final double A0 = 3.994961687345134e-1;
  private static final double A1 = 2.100960795068497e+1;
  private static final double A2 = 4.980340217855084e+1;
  private static final double A3 = 5.988761102690991e+2;
  private static final double A4 = 1.848489695437094e+3;
  private static final double A5 = 6.106322407867059e+3;
  private static final double A6 = 2.493415285349361e+4;
  private static final double A7 = 1.266458051348246e+4;

  private static final double B0 = 1.000000000000000e+0;
  private static final double B1 = 4.990534153589422e+1;
  private static final double B2 = 3.093573936743112e+1;
  private static final double B3 = 1.495105008310999e+3;
  private static final double B4 = 1.323614537899738e+3;
  private static final double B5 = 1.598919697679745e+4;
  private static final double B6 = 2.392008891720782e+4;
  private static final double B7 = 3.608817108375034e+3;
  private static final double B8 = -2.067719486400926e+2;
  private static final double B9 = 1.174240599306013e+1;

  /** approx 2.22e-16**/
  private static final double MACHINE_EPSILON = Math.ulp(1.0);

  /** approx 1.49e-8 **/
  private static final double SQRT_MACHINE_EPSILON = Math.sqrt(MACHINE_EPSILON);

  private static final boolean POLISH_ROOT = false;

  private static final NormalPriceFunction NORMAL_PRICE_FUNCTION = new NormalPriceFunction();

  private static double arctanh(double x) {
    final double absx = Math.abs(x);

    if (absx < SQRT_MACHINE_EPSILON) {
      //arctanh = x + x*x*x/3 +... for x near 0
      return x;
    } else if (absx < 1) {
      return 0.5 * (Math.log1p(x) - Math.log1p(-x));
    } else {
      return Double.NaN;
    }
  }

  private double calcStraddlePremium(final NormalFunctionData normalModelData, final EuropeanVanillaOption option, final double optionPrice) {
    final double discountFactor = normalModelData.getNumeraire();
    final double forwardPremium = optionPrice / discountFactor;

    final boolean isCall = option.isCall();
    final double f = normalModelData.getForward();
    final double k = option.getStrike();

    //Max(F-K,0) - Max(K-F,0) = F - K
    if (isCall) {
      return 2 * forwardPremium - (f - k);
    } else {
      return 2 * forwardPremium + (f - k);
    }

  }

  private static double h(double eta) {

    if (eta < 0) {
      throw new IllegalArgumentException("Require non-negative eta " + eta);
    }

    final double num = A0 + eta * (A1 + eta * (A2 + eta * (A3 + eta * (A4 + eta * (A5 + eta * (A6 + eta * A7))))));

    final double den = B0 + eta * (B1 + eta * (B2 + eta * (B3 +
        eta * (B4 + eta * (B5 + eta * (B6 + eta * (B7 + eta * (B8 + eta * B9))))))));

    return Math.sqrt(eta) * (num / den);

  }

  public double getImpliedVolatility(final NormalFunctionData normalModelData, final EuropeanVanillaOption option, final double optionPrice) {

    final double straddlePrem = calcStraddlePremium(normalModelData, option, optionPrice);
    final double f = normalModelData.getForward();
    final double k = option.getStrike();
    final double t = Math.max(option.getTimeToExpiry(), MACHINE_EPSILON);

    //FIXME review numerics why nu>=1
    final double nu = Math.max(-1.0 + MACHINE_EPSILON, Math.min((f - k) / straddlePrem, 1.0 - MACHINE_EPSILON));
    //nu / arctanh(nu) -> 1 as nu -> 0
    final double eta = (nu < SQRT_MACHINE_EPSILON) ? 1.0 : nu / arctanh(nu);
    final double heta = h(eta);
    double impliedBpvol = Math.sqrt(Math.PI / (2 * t)) * straddlePrem * heta;

    if (POLISH_ROOT) {
      NormalFunctionData newData = new NormalFunctionData(f, normalModelData.getNumeraire(), impliedBpvol);
      final double[] derivative = new double[3];
      final double newOptionPrem = NORMAL_PRICE_FUNCTION.getPriceAdjoint(option, newData, derivative);
      final double newStraddlePrem = calcStraddlePremium(newData, option, newOptionPrem);
      final double vega = (2 * derivative[1]) / newData.getNumeraire();
      impliedBpvol -= (newStraddlePrem - straddlePrem) / vega;
    }

    return impliedBpvol;

  }

}
