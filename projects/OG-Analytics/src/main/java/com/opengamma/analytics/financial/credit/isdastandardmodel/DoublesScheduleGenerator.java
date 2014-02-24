/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import java.util.Arrays;

/**
 * Utility for combining and truncating ascending arrays of doubles. This is used for CDS pricing.
 */
public abstract class DoublesScheduleGenerator {

  private static final double TOL = 1. / 730;

  /**
   * Combines the knot points on the yield and credit curves into a single (ordered) list of times strictly between the specified 
   * start and end. The start and end values are added at the beginning and end of the list. If two times are very close (defined as 
   * less than half a day - 1/730 years different) only the smaller value is kept (with the exception of the end value which takes 
   * precedence). <p>
   * Since ISDACompliantCurve is piecewise constant in the forward rate, this makes the integrals that appear in CDS pricing (i.e. 
   * $$\int_0^T P(t) \frac{dQ(t)}{dt} dt$$ on the protection leg and $$\sum_{i=0}^{N-1}\int_{T_i}^{T_{i+1}} (t-T_i) P(t) \frac{dQ(t)}{dt} dt$$
   * on the premium leg) analytic between the points in the list. 
   * @param start First time in the list
   * @param end last time in the list
   * @param yieldCurve The yield curve 
   * @param creditCurve The credit curve
   * @return A list of times used to split CDS pricing integrals into analytic pieces.
   */
  public static double[] getIntegrationsPoints(final double start, final double end, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve) {
    return getIntegrationsPoints(start, end, yieldCurve.getKnotTimes(), creditCurve.getKnotTimes());
  }

  /**
   * Combines two sets of numbers and return only the values  strictly between the specified 
   * start and end. The start and end values are added at the beginning and end of the list. If two times are very close (defined as 
   * less than half a day - 1/730 years different) only the smaller value is kept (with the exception of the end value which takes 
   * precedence). 
   * @param start First time in the list
   * @param end last time in the list
   * @param setA the first set
   * @param setB the second
   * @return Combined list between first and last value
   */
  public static double[] getIntegrationsPoints(final double start, final double end, final double[] setA, final double[] setB) {
    final double[] set1 = truncateSetExclusive(start, end, setA);
    final double[] set2 = truncateSetExclusive(start, end, setB);
    final int n1 = set1.length;
    final int n2 = set2.length;
    final int n = n1 + n2;
    final double[] set = new double[n];
    System.arraycopy(set1, 0, set, 0, n1);
    System.arraycopy(set2, 0, set, n1, n2);
    Arrays.sort(set);

    final double[] temp = new double[n + 2];
    temp[0] = start;
    int pos = 0;
    for (int i = 0; i < n; i++) {
      if (different(temp[pos], set[i])) {
        temp[++pos] = set[i];
      }
    }
    if (different(temp[pos], end)) {
      pos++;
    }
    temp[pos] = end; // add the end point (this may replace the last entry in temp if that is not significantly different)

    final int resLength = pos + 1;
    if (resLength == n + 2) {
      return temp; // everything was unique
    }

    final double[] res = new double[resLength];
    System.arraycopy(temp, 0, res, 0, resLength);
    return res;
  }

  /**
   * Combines two sets of numbers (times) and return the unique sorted set. 
   *  If two times are very close (defined as  less than half a day - 1/730 years different) only the smaller value is kept. 
   * @param set1 The first set 
   * @param set2 The second set 
   * @return The unique sorted set, set1 U set2  
   */
  public static double[] combineSets(final double[] set1, final double[] set2) {
    final int n1 = set1.length;
    final int n2 = set2.length;
    final int n = n1 + n2;
    final double[] set = new double[n];
    System.arraycopy(set1, 0, set, 0, n1);
    System.arraycopy(set2, 0, set, n1, n2);
    Arrays.sort(set);

    final double[] temp = new double[n];
    temp[0] = set[0];
    int pos = 0;
    for (int i = 1; i < n; i++) {
      if (different(temp[pos], set[i])) {
        temp[++pos] = set[i];
      }
    }

    final int resLength = pos + 1;
    if (resLength == n) {
      return temp; // everything was unique
    }

    final double[] res = new double[resLength];
    System.arraycopy(temp, 0, res, 0, resLength);
    return res;
  }

  private static boolean different(final double a, final double b) {
    return Math.abs(a - b) > TOL;
  }

  /**
   * Truncates an array of doubles so it contains only the values between lower and upper, plus the values of lower and higher (as the first
   * and last entry respectively). If no values met this criteria an array just containing lower and upper is returned. If the first (last) 
   * entry of set is too close to lower (upper) - defined by TOL - the first (last) entry of set is replaced by lower (upper).
   * @param lower The lower value
   * @param upper The upper value
   * @param set The numbers must be sorted in ascending order
   * @return the truncated array 
   */
  public static double[] truncateSetInclusive(final double lower, final double upper, final double[] set) {
    // this is private, so assume inputs are fine
    final double[] temp = truncateSetExclusive(lower, upper, set);
    final int n = temp.length;
    if (n == 0) {
      return new double[] {lower, upper };
    }
    final boolean addLower = different(lower, temp[0]);
    final boolean addUpper = different(upper, temp[n - 1]);
    if (!addLower && !addUpper) { // replace first and last entries of set
      temp[0] = lower;
      temp[n - 1] = upper;
      return temp;
    }

    final int m = n + (addLower ? 1 : 0) + (addUpper ? 1 : 0);
    final double[] res = new double[m];
    System.arraycopy(temp, 0, res, (addLower ? 1 : 0), n);
    res[0] = lower;
    res[m - 1] = upper;

    return res;
  }

  /**
   * Truncates an array of doubles so it contains only the values between lower and upper exclusive. If no values met this criteria an 
   * empty array is returned 
   * @param lower The lower value
   * @param upper The upper value
   * @param set The numbers must be sorted in ascending order
   * @return the truncated array 
   */
  public static double[] truncateSetExclusive(final double lower, final double upper, final double[] set) {
    // this is private, so assume inputs are fine

    final int n = set.length;
    if (upper < set[0] || lower > set[n - 1]) {
      return new double[0];
    }

    int lIndex;
    if (lower < set[0]) {
      lIndex = 0;
    } else {
      final int temp = Arrays.binarySearch(set, lower);
      lIndex = temp >= 0 ? temp + 1 : -(temp + 1);
    }

    int uIndex;
    if (upper > set[n - 1]) {
      uIndex = n;
    } else {
      final int temp = Arrays.binarySearch(set, lIndex, n, upper);
      uIndex = temp >= 0 ? temp : -(temp + 1);
    }

    final int m = uIndex - lIndex;
    if (m == n) {
      return set;
    }

    final double[] trunc = new double[m];
    System.arraycopy(set, lIndex, trunc, 0, m);
    return trunc;
  }

  public static double[] leftTruncate(final double lower, final double[] set) {

    final int n = set.length;
    if (n == 0) {
      return set;
    }
    if (lower < set[0]) {
      return set;
    }
    if (lower >= set[n - 1]) {
      return new double[0];
    }

    final int index = Arrays.binarySearch(set, lower);
    final int chop = index >= 0 ? index + 1 : -(index + 1);
    double[] res;
    if (chop == 0) {
      res = set;
    } else {
      res = new double[n - chop];
      System.arraycopy(set, chop, res, 0, n - chop);
    }
    return res;
  }

}
