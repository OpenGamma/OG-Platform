/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import com.opengamma.financial.model.interestrate.definition.G2ppPiecewiseConstantParameters;

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

}
