/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import com.opengamma.analytics.financial.model.interestrate.definition.G2ppPiecewiseConstantParameters;

/**
 * Methods related to to the G2++ model (equivalent to Hull-White two factors) with piecewise constant volatility.
 */
public class G2ppPiecewiseConstantModel {

  /**
   * The maturity dependent part of the volatility (function called H in the implementation note).
   * @param g2parameters The model parameters.
   * @param u The start time.
   * @param v The end times.
   * @return The volatility.
   */
  public double[][] volatilityMaturityPart(final G2ppPiecewiseConstantParameters g2parameters, double u, double[] v) {
    double[] a = g2parameters.getMeanReversion();
    double[][] result = new double[2][v.length];
    double expa0u = Math.exp(-a[0] * u);
    double expa1u = Math.exp(-a[1] * u);
    for (int loopcf = 0; loopcf < v.length; loopcf++) {
      result[0][loopcf] = (expa0u - Math.exp(-a[0] * v[loopcf])) / a[0];
      result[1][loopcf] = (expa1u - Math.exp(-a[1] * v[loopcf])) / a[1];
    }
    return result;
  }

  /**
   * The maturity dependent part of the volatility (function called H in the implementation note).
   * @param g2parameters The model parameters.
   * @param u The start time.
   * @param v The end times.
   * @return The volatility.
   */
  public double[] volatilityMaturityPart(final G2ppPiecewiseConstantParameters g2parameters, double u, double v) {
    double[] a = g2parameters.getMeanReversion();
    double[] result = new double[2];
    double expa0u = Math.exp(-a[0] * u);
    double expa1u = Math.exp(-a[1] * u);
    result[0] = (expa0u - Math.exp(-a[0] * v)) / a[0];
    result[1] = (expa1u - Math.exp(-a[1] * v)) / a[1];
    return result;
  }

  /**
   * The expiry time dependent part of the volatility.
   * @param g2parameters The model parameters.
   * @param theta0 The start expiry time.
   * @param theta1 The end expiry time.
   * @return The volatility.
   */
  public double[][] gamma(final G2ppPiecewiseConstantParameters g2parameters, double theta0, double theta1) {
    double[] a = g2parameters.getMeanReversion();
    DoubleArrayList[] sigma = g2parameters.getVolatility();
    int indexStart = 1; // Period in which the time startExpiry is; _volatilityTime[i-1] <= startExpiry < _volatilityTime[i];
    while (theta0 > g2parameters.getVolatilityTime()[indexStart]) {
      indexStart++;
    }
    int indexEnd = indexStart; // Period in which the time endExpiry is; _volatilityTime[i-1] <= endExpiry < _volatilityTime[i];
    while (theta1 > g2parameters.getVolatilityTime()[indexEnd]) {
      indexEnd++;
    }
    int sLen = indexEnd - indexStart + 2;
    double[] s = new double[sLen];
    s[0] = theta0;
    System.arraycopy(g2parameters.getVolatilityTime(), indexStart, s, 1, sLen - 2);
    s[sLen - 1] = theta1;

    double[] gammaii = new double[2];
    double gamma12 = 0.0;
    double[][] exp2as = new double[2][sLen];
    double[] expa0a1s = new double[sLen];
    for (int loopindex = 0; loopindex < sLen; loopindex++) {
      for (int loop = 0; loop < 2; loop++) {
        exp2as[loop][loopindex] = Math.exp(2 * a[loop] * s[loopindex]);
      }
      expa0a1s[loopindex] = Math.exp((a[0] + a[1]) * s[loopindex]);
    }
    for (int loopindex = 0; loopindex < sLen - 1; loopindex++) {
      for (int loop = 0; loop < 2; loop++) {
        gammaii[loop] += sigma[loop].get(indexStart - 1 + loopindex) * sigma[loop].get(indexStart - 1 + loopindex) * (exp2as[loop][loopindex + 1] - exp2as[loop][loopindex]);
      }
      gamma12 += sigma[0].get(indexStart - 1 + loopindex) * sigma[1].get(indexStart - 1 + loopindex) * (expa0a1s[loopindex + 1] - expa0a1s[loopindex]);
    }
    double[][] result = new double[2][2];
    result[0][0] = gammaii[0] / (2 * a[0]);
    result[1][1] = gammaii[1] / (2 * a[1]);
    result[1][0] = gamma12 / (a[0] + a[1]);
    result[0][1] = result[1][0];
    return result;
  }

  /**
   * Computes the swap rate for a given value of the standard normal random
   * variables in the $P(.,\theta)$ numeraire.
   * @param x The random variable values.
   * @param discountedCashFlowFixed The discounted cash flows equivalent of the swap fixed leg. Dimensions: cash flow - factor
   * @param alphaFixed The zero-coupon bond volatilities for each random variable for the swap fixed leg.
   * @param tau2Fixed The total zero-coupon bond volatilities for the swap fixed leg.
   * @param discountedCashFlowIbor The discounted cash flows equivalent of the swap Ibor leg.
   * @param alphaIbor The zero-coupon bond volatilities for each random variable for the swap Ibor leg.
   * @param tau2Ibor The total zero-coupon bond volatilities for the swap Ibor leg.
   * @return The swap rate.
   */
  public double swapRate(final double[] x, final double[] discountedCashFlowFixed, final double[][] alphaFixed, final double[] tau2Fixed, final double[] discountedCashFlowIbor,
      final double[][] alphaIbor, final double[] tau2Ibor) {
    double resultFixed = 0.0;
    double resultIbor = 0.0;
    for (int loopcf = 0; loopcf < discountedCashFlowFixed.length; loopcf++) {
      resultFixed += discountedCashFlowFixed[loopcf] * Math.exp(-alphaFixed[loopcf][0] * x[0] - alphaFixed[loopcf][1] * x[1] - tau2Fixed[loopcf] / 2.0);
    }
    for (int loopcf = 0; loopcf < discountedCashFlowIbor.length; loopcf++) {
      resultIbor += discountedCashFlowIbor[loopcf] * Math.exp(-alphaIbor[loopcf][0] * x[0] - alphaIbor[loopcf][1] * x[1] - tau2Ibor[loopcf] / 2.0);
    }
    return -resultIbor / resultFixed;
  }

  /**
   * Computes the swap rate and its first order derivatives with respect to the
   * value of the standard normal random variables in the $P(.,\theta)$
   * numeraire.
   * @param x The random variable values.
   * @param discountedCashFlowFixed The discounted cash flows equivalent of the swap fixed leg. Dimensions: cash flow - factor
   * @param alphaFixed The zero-coupon bond volatilities for each random variable for the swap fixed leg.
   * @param tau2Fixed The total zero-coupon bond volatilities for the swap fixed leg.
   * @param discountedCashFlowIbor The discounted cash flows equivalent of the swap Ibor leg.
   * @param alphaIbor The zero-coupon bond volatilities for each random variable for the swap Ibor leg.
   * @param tau2Ibor The total zero-coupon bond volatilities for the swap Ibor leg.
   * @param d1 The array with the first order derivative. Will be changed by the method.
   * @return The swap rate first order derivatives.
   */
  public double swapRate(final double[] x, final double[] discountedCashFlowFixed, final double[][] alphaFixed, final double[] tau2Fixed, final double[] discountedCashFlowIbor,
      final double[][] alphaIbor, final double[] tau2Ibor, double[] d1) {
    double f = 0.0;
    double g = 0.0;
    double[] df = new double[2];
    double[] dg = new double[2];
    double term;
    for (int loopcf = 0; loopcf < discountedCashFlowIbor.length; loopcf++) {
      term = discountedCashFlowIbor[loopcf] * Math.exp(-alphaIbor[loopcf][0] * x[0] - alphaIbor[loopcf][1] * x[1] - tau2Ibor[loopcf] / 2.0);
      f += term;
      for (int loopd = 0; loopd < 2; loopd++) {
        df[loopd] += -alphaIbor[loopcf][loopd] * term;
      }
    }
    for (int loopcf = 0; loopcf < discountedCashFlowFixed.length; loopcf++) {
      term = discountedCashFlowFixed[loopcf] * Math.exp(-alphaFixed[loopcf][0] * x[0] - alphaFixed[loopcf][1] * x[1] - tau2Fixed[loopcf] / 2.0);
      g += term;
      for (int loopd = 0; loopd < 2; loopd++) {
        dg[loopd] += -alphaFixed[loopcf][loopd] * term;
      }
    }
    for (int loopd = 0; loopd < 2; loopd++) {
      d1[loopd] = -(df[loopd] * g - dg[loopd] * f) / (g * g);
    }
    return -f / g;
  }

  /**
   * Computes the swap rate and its first and second order derivatives with
   * respect to the value of the standard normal random variables in the
   * $P(.,\theta)$ numeraire.
   * @param x The random variable values.
   * @param discountedCashFlowFixed The discounted cash flows equivalent of the swap fixed leg. Dimensions: cash flow - factor
   * @param alphaFixed The zero-coupon bond volatilities for each random variable for the swap fixed leg.
   * @param tau2Fixed The total zero-coupon bond volatilities for the swap fixed leg.
   * @param discountedCashFlowIbor The discounted cash flows equivalent of the swap Ibor leg.
   * @param alphaIbor The zero-coupon bond volatilities for each random variable for the swap Ibor leg.
   * @param tau2Ibor The total zero-coupon bond volatilities for the swap Ibor leg.
   * @param d1 The array with the first order derivative. Will be changed by the method.
   * @param d2 The array with the second order derivative. Will be changed by the method.
   * @return The swap rate second order derivatives.
   */
  public double swapRate(final double[] x, final double[] discountedCashFlowFixed, final double[][] alphaFixed, final double[] tau2Fixed, final double[] discountedCashFlowIbor,
      final double[][] alphaIbor, final double[] tau2Ibor, double[] d1, double[][] d2) {
    double f = 0.0;
    double g = 0.0;
    double[] df = new double[2];
    double[] dg = new double[2];
    double[][] d2f = new double[2][2];
    double[][] d2g = new double[2][2];
    double term;
    for (int loopcf = 0; loopcf < discountedCashFlowIbor.length; loopcf++) {
      term = discountedCashFlowIbor[loopcf] * Math.exp(-alphaIbor[loopcf][0] * x[0] - alphaIbor[loopcf][1] * x[1] - tau2Ibor[loopcf] / 2.0);
      f += term;
      for (int loopd = 0; loopd < 2; loopd++) {
        df[loopd] += -alphaIbor[loopcf][loopd] * term;
      }
      for (int loopd1 = 0; loopd1 < 2; loopd1++) {
        for (int loopd2 = 0; loopd2 < 2; loopd2++) {
          d2f[loopd1][loopd2] += alphaIbor[loopcf][loopd1] * alphaIbor[loopcf][loopd2] * term;
        }
      }
    }
    for (int loopcf = 0; loopcf < discountedCashFlowFixed.length; loopcf++) {
      term = discountedCashFlowFixed[loopcf] * Math.exp(-alphaFixed[loopcf][0] * x[0] - alphaFixed[loopcf][1] * x[1] - tau2Fixed[loopcf] / 2.0);
      g += term;
      for (int loopd = 0; loopd < 2; loopd++) {
        dg[loopd] += -alphaFixed[loopcf][loopd] * term;
      }
      for (int loopd1 = 0; loopd1 < 2; loopd1++) {
        for (int loopd2 = 0; loopd2 < 2; loopd2++) {
          d2g[loopd1][loopd2] += alphaFixed[loopcf][loopd1] * alphaFixed[loopcf][loopd2] * term;
        }
      }
    }
    for (int loopd = 0; loopd < 2; loopd++) {
      d1[loopd] = -(df[loopd] * g - dg[loopd] * f) / (g * g);
    }
    for (int loopd1 = 0; loopd1 < 2; loopd1++) {
      for (int loopd2 = loopd1; loopd2 < 2; loopd2++) {
        d2[loopd1][loopd2] = -(d2f[loopd1][loopd2] * g + df[loopd2] * dg[loopd1] - df[loopd1] * dg[loopd2] - f * d2g[loopd1][loopd2]) / (g * g) + 2 * dg[loopd1] * (df[loopd2] * g - f * dg[loopd2])
            / (g * g * g);
      }
    }
    d2[1][0] = d2[0][1];
    return -f / g;
  }

}
