/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import java.util.Arrays;

import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class AnalyticCDSPricer {

  private static final double TOL = 1. / 730;

  public double rpv01(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final PriceType cleanOrDirty) {
    ArgumentChecker.notNull(cds, "null cds");
    ArgumentChecker.notNull(yieldCurve, "null yieldCurve");
    ArgumentChecker.notNull(creditCurve, "null creditCurve");

    final double offset = cds.isProtectionFromStartOfDay() ? -cds.getCurveOneDay() : 0.0;

    final int n = cds.getNumPayments();
    double pv = 0.0;
    for (int i = 0; i < n; i++) {
      final double q = creditCurve.getDiscountFactor(cds.getCreditObservationTime(i));
      final double p = yieldCurve.getDiscountFactor(cds.getPaymentTime(i));
      pv += cds.getAccrualFraction(i) * p * q;

    }

    if (cds.isPayAccOnDefault()) {
      final double[] integrationSchedule = getIntegrationsPoints(cds.getAccStart(0), cds.getAccEnd(n - 1), yieldCurve, creditCurve);
      final double offsetStepin = cds.getStepin() + offset;

      double accPV = 0.0;
      for (int i = 0; i < n; i++) {
        double offsetAccStart = cds.getAccStart(i) + offset;
        double offsetAccEnd = cds.getAccEnd(i) + offset;
        final double accRate = cds.getAccrualFraction(i) / (offsetAccEnd - offsetAccStart);
        accPV += calculateSinglePeriodAccrualOnDefault(accRate, offsetStepin, offsetAccStart, offsetAccEnd, integrationSchedule, yieldCurve, creditCurve);
      }
      pv += accPV;
    }

    final double df = yieldCurve.getDiscountFactor(cds.getValuationTime());
    pv /= df;

    if (cleanOrDirty == PriceType.CLEAN) {
      pv -= cds.getAccrued();
    }
    return pv;
  }

  private double calculateSinglePeriodAccrualOnDefault(final double accRate, final double stepin, final double accStart, final double accEnd, final double[] integrationPoints,
      final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve) {

    double start = Math.max(accStart, stepin);
    if (start >= accEnd) {
      return 0.0;
    }
    double[] knots = truncateSetInclusive(start, accEnd, integrationPoints);

    double t = knots[0];
    double s0 = creditCurve.getDiscountFactor(t);
    double df0 = yieldCurve.getDiscountFactor(t);
    double t0 = t - accStart + 1 / 730.0; // TODO not entirely clear why ISDA adds half a day
    double pv = 0.0;
    final int nItems = knots.length;
    for (int j = 1; j < nItems; ++j) {
      t = knots[j];
      final double s1 = creditCurve.getDiscountFactor(t);
      final double df1 = yieldCurve.getDiscountFactor(t);
      final double t1 = t - accStart + 1 / 730.0;
      final double dt = knots[j] - knots[j - 1];

      // TODO this is a know bug that is fixed in ISDA v.1.8.2
      final double lambda = Math.log(s0 / s1) / dt;
      final double fwdRate = Math.log(df0 / df1) / dt;
      final double lambdafwdRate = lambda + fwdRate + 1.0e-50;
      final double tPV = lambda * accRate * s0 * df0 * ((t0 + 1.0 / (lambdafwdRate)) / (lambdafwdRate) - (t1 + 1.0 / (lambdafwdRate)) / (lambdafwdRate) * s1 / s0 * df1 / df0);
      pv += tPV;
      s0 = s1;
      df0 = df1;
      t0 = t1;
    }
    return pv;
  }

  public double protectionLeg(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve) {
    ArgumentChecker.notNull(cds, "null cds");
    ArgumentChecker.notNull(yieldCurve, "null yieldCurve");
    ArgumentChecker.notNull(creditCurve, "null creditCurve");

    final double[] integrationSchedule = getIntegrationsPoints(cds.getProtectionStart(), cds.getProtectionEnd(), yieldCurve, creditCurve);

    double ht1 = creditCurve.getRT(integrationSchedule[0]);
    double rt1 = yieldCurve.getRT(integrationSchedule[0]);
    double s1 = Math.exp(-ht1);
    double p1 = Math.exp(-rt1);
    double pv = 0.0;
    final int n = integrationSchedule.length;
    for (int i = 1; i < n; ++i) {

      final double ht0 = ht1;
      final double rt0 = rt1;
      final double p0 = p1;
      final double s0 = s1;

      ht1 = creditCurve.getRT(integrationSchedule[i]);
      rt1 = yieldCurve.getRT(integrationSchedule[i]);
      s1 = Math.exp(-ht1);
      p1 = Math.exp(-rt1);
      final double dht = ht1 - ht0;
      final double drt = rt1 - rt0;
      final double dhrt = dht + drt;

      // this is equivalent to the ISDA code without explicitly calculating the time step - it also handles the limit
      double dPV;
      if (Math.abs(dhrt) < 1e-5) {
        dPV = dht * (1 - dhrt * (0.5 - dhrt / 6)) * p0 * s0;
      } else {
        dPV = dht / dhrt * (p0 * s0 - p1 * s1);
      }

      pv += dPV;

    }
    pv *= cds.getLGD();

    // Compute the discount factor discounting the upfront payment made on the cash settlement date back to the valuation date
    final double df = yieldCurve.getDiscountFactor(cds.getValuationTime());
    pv /= df;

    return pv;
  }

  private double[] getIntegrationsPoints(final double start, final double end, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve) {

    double[] set1 = truncateSetExclusive(start, end, yieldCurve.getKnotTimes());
    double[] set2 = truncateSetExclusive(start, end, creditCurve.getKnotTimes());
    final int n1 = set1.length;
    final int n2 = set2.length;
    final int n = n1 + n2;
    double[] set = new double[n];
    System.arraycopy(set1, 0, set, 0, n1);
    System.arraycopy(set2, 0, set, n1, n2);
    Arrays.sort(set);

    double[] temp = new double[n + 2];
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

    double[] res = new double[resLength];
    System.arraycopy(temp, 0, res, 0, resLength);
    return res;
  }

  private boolean different(final double a, final double b) {
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
  private double[] truncateSetInclusive(final double lower, final double upper, final double[] set) {
    // this is private, so assume inputs are fine
    double[] temp = truncateSetExclusive(lower, upper, set);
    final int n = temp.length;
    if (n == 0) {
      return new double[] {lower, upper};
    }
    boolean addLower = different(lower, temp[0]);
    boolean addUpper = different(upper, temp[n - 1]);
    if (!addLower && !addUpper) { // replace first and last entries of set
      temp[0] = lower;
      temp[n - 1] = upper;
      return temp;
    }

    int m = n + (addLower ? 1 : 0) + (addUpper ? 1 : 0);
    double[] res = new double[m];
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
  private double[] truncateSetExclusive(final double lower, final double upper, final double[] set) {
    // this is private, so assume inputs are fine

    final int n = set.length;
    if (upper < set[0] || lower > set[n - 1]) {
      return new double[0];
    }

    int lIndex;
    if (lower < set[0]) {
      lIndex = 0;
    } else {
      int temp = Arrays.binarySearch(set, lower);
      lIndex = temp >= 0 ? temp + 1 : -(temp + 1);
    }

    int uIndex;
    if (upper > set[n - 1]) {
      uIndex = n;
    } else {
      int temp = Arrays.binarySearch(set, lIndex, n, upper);
      uIndex = temp >= 0 ? temp : -(temp + 1);
    }

    final int m = uIndex - lIndex;
    if (m == n) {
      return set;
    }

    double[] trunc = new double[m];
    System.arraycopy(set, lIndex, trunc, 0, m);
    return trunc;
  }

  private double[] leftTruncate(final double lower, final double[] set) {
    // this is private, so assume inputs are fine
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

    int index = Arrays.binarySearch(set, lower);
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
