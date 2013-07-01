/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import java.util.Arrays;

import com.opengamma.util.ArgumentChecker;

/**
 * A yield or hazard curve values between nodes are linearly interpolated from t*r points (where t is time and r is the zero rate)
 */
public class ISDACompliantCurve {

  private final int _n;
  private final double[] _t;
  private final double[] _r;
  private final double[] _rt;
  private final double[] _df;

  /**
   * 
   * @param t Set of times that form the knots of the curve. Must be ascending with the first value >= 0.
   * @param r Set of zero rates 
   */
  public ISDACompliantCurve(final double[] t, final double[] r) {
    ArgumentChecker.notEmpty(t, "t empty");
    ArgumentChecker.notEmpty(r, "r empty");
    _n = t.length;
    ArgumentChecker.isTrue(_n == r.length, "r and t different lengths");
    ArgumentChecker.isTrue(t[0] >= 0, "first t must be >= 0.0");
    for (int i = 1; i < _n; i++) {
      ArgumentChecker.isTrue(t[i] > t[i - 1], "Times must be ascending");
    }

    _t = new double[_n];
    _r = new double[_n];
    _rt = new double[_n];
    _df = new double[_n];
    System.arraycopy(t, 0, _t, 0, _n);
    System.arraycopy(r, 0, _r, 0, _n);
    for (int i = 0; i < _n; i++) {
      _rt[i] = _r[i] * _t[i]; // We make no check that rt is ascending (i.e. we allow negative forward rates)
      _df[i] = Math.exp(-_rt[i]);
    }
  }

  protected ISDACompliantCurve(final ISDACompliantCurve from) {
    ArgumentChecker.notNull(from, "null from");
    // Shallow copy
    _n = from._n;
    _t = from._t;
    _r = from._r;
    _rt = from._rt;
    _df = from._df;
  }

  private ISDACompliantCurve(final double[] t, final double[] r, final double[] rt, final double[] df) {
    _n = t.length;
    _t = t;
    _r = r;
    _rt = rt;
    _df = df;
  }

  protected double[] getKnotTimes() {
    double[] res = new double[_n];
    System.arraycopy(_t, 0, res, 0, _n);
    return res;
  }

  /**
   * The discount factor or survival probability 
   * @param t Time 
   * @return value 
   */
  public double getDiscountFactor(final double t) {
    ArgumentChecker.isTrue(t >= 0, "require t >= 0.0");
    if (t == 0.0) {
      return 1.0;
    }
    final int index = Arrays.binarySearch(_t, t);
    if (index >= 0) {
      return _df[index];
    }

    int insertionPoint = -(1 + index);
    final double rt = getRT(t, insertionPoint);
    return Math.exp(-rt);
  }

  public double getTimeAtIndex(final int index) {
    return _t[index];
  }

  public double getZeroRateAtIndex(final int index) {
    return _r[index];
  }

  /**
   * The zero rate or zero hazard rate 
   * @param t Time 
   * @return value 
   */
  public double getZeroRate(final double t) {
    ArgumentChecker.isTrue(t >= 0, "require t >= 0.0");

    // short-cut doing binary search
    if (t <= _t[0]) {
      return _r[0];
    }
    if (t >= _t[_n - 1]) {
      return _r[_n - 1];
    }

    final int index = Arrays.binarySearch(_t, t);
    if (index >= 0) {
      return _r[index];
    }

    final int insertionPoint = -(1 + index);
    final double rt = getRT(t, insertionPoint);
    return rt / t;
  }

  /**
   * Get the zero rate multiplied by time - this is the same as the negative log of the discount factor
   * @param t  Time
   * @return value
   */
  public double getRT(final double t) {
    ArgumentChecker.isTrue(t >= 0, "require t >= 0.0");

    // short-cut doing binary search
    if (t <= _t[0]) {
      return _r[0] * t;
    }
    if (t >= _t[_n - 1]) {
      return _r[_n - 1] * t;
    }

    final int index = Arrays.binarySearch(_t, t);
    if (index >= 0) {
      return _rt[index];
    }

    final int insertionPoint = -(1 + index);
    return getRT(t, insertionPoint);
  }

  private double getRT(final double t, final int insertionPoint) {
    if (insertionPoint == 0) {
      return t * _r[0];
    }
    if (insertionPoint == _n) {
      return t * _r[_n - 1];
    }

    final double t1 = _t[insertionPoint - 1];
    final double t2 = _t[insertionPoint];
    final double dt = t2 - t1;
    return ((t2 - t) * _rt[insertionPoint - 1] + (t - t1) * _rt[insertionPoint]) / dt;
  }

  /**
   * 
   * @return number of knots in curve
   */
  public int getNumberOfKnots() {
    return _n;
  }

  public double[] getNodeSensitivity(final double t) {

    double[] res = new double[_n];

    // short-cut doing binary search
    if (t <= _t[0]) {
      res[0] = 1.0;
      return res;
    }
    if (t >= _t[_n - 1]) {
      res[_n - 1] = 1.0;
      return res;
    }

    final int index = Arrays.binarySearch(_t, t);
    if (index >= 0) {
      res[index] = 1.0;
      return res;
    }

    final int insertionPoint = -(1 + index);
    final double t1 = _t[insertionPoint - 1];
    final double t2 = _t[insertionPoint];
    final double dt = t2 - t1;
    res[insertionPoint - 1] = t1 * (t2 - t) / dt / t;
    res[insertionPoint] = t2 * (t - t1) / dt / t;
    return res;
  }

  /**
   * get the sensitivity of the interpolated rate at time t to a specified node 
   * @param t The time
   * @param nodeIndex The node index 
   * @return sensitivity to a single node 
   */
  public double getSingleNodeSensitivity(final double t, final int nodeIndex) {
    ArgumentChecker.isTrue(t >= 0, "require t >= 0.0");
    ArgumentChecker.isTrue(nodeIndex >= 0 && nodeIndex < _n, "index out of range");

    if (t <= _t[0]) {
      return nodeIndex == 0 ? 1.0 : 0.0;
    }
    if (t >= _t[_n - 1]) {
      return nodeIndex == _n - 1 ? 1.0 : 0.0;
    }

    final int index = Arrays.binarySearch(_t, t);
    if (index >= 0) {
      return nodeIndex == index ? 1.0 : 0.0;
    }

    final int insertionPoint = -(1 + index);
    if (nodeIndex != insertionPoint && nodeIndex != insertionPoint - 1) {
      return 0.0;
    }

    final double t1 = _t[insertionPoint - 1];
    final double t2 = _t[insertionPoint];
    final double dt = t2 - t1;
    if (nodeIndex == insertionPoint) {
      return t2 * (t - t1) / dt / t;
    }

    return t1 * (t2 - t) / dt / t;
  }

  public double getSingleNodeDiscountFactorSensitivity(final double t, final int nodeIndex) {
    ArgumentChecker.isTrue(t >= 0, "require t >= 0.0");
    ArgumentChecker.isTrue(nodeIndex >= 0 && nodeIndex < _n, "index out of range");

    if (t == 0.0) {
      return 0.0;
    }
    if (t <= _t[0]) {
      return nodeIndex == 0 ? -t * Math.exp(-t * _r[0]) : 0.0;
    }
    if (t >= _t[_n - 1]) {
      return nodeIndex == _n - 1 ? -t * Math.exp(-t * _r[_n - 1]) : 0.0;
    }

    final int index = Arrays.binarySearch(_t, t);
    if (index >= 0) {
      return nodeIndex == index ? -t * _df[nodeIndex] : 0.0;
    }

    final int insertionPoint = -(1 + index);
    if (nodeIndex != insertionPoint && nodeIndex != insertionPoint - 1) {
      return 0.0;
    }

    final double t1 = _t[insertionPoint - 1];
    final double t2 = _t[insertionPoint];
    final double dt = t2 - t1;
    final double rt = ((t2 - t) * _rt[insertionPoint - 1] + (t - t1) * _rt[insertionPoint]) / dt;
    final double p = Math.exp(-rt);
    if (nodeIndex == insertionPoint) {
      return -t2 * (t - t1) * p / dt;
    }

    return -t1 * (t2 - t) * p / dt;
  }

  /**
   * Update are rates in curve 
   * @param r Set of rates
   * @return a new curve 
   */
  public ISDACompliantCurve withRates(final double[] r) {
    return new ISDACompliantCurve(_t, r);
  }

  /**
   * Adjust a rate at a particular index 
   * @param rate The new rate
   * @param index The index of the knot
   * @return a new curve 
   */
  public ISDACompliantCurve withRate(final double rate, final int index) {
    ArgumentChecker.isTrue(index >= 0 && index < _n, "index out of range");
    double[] t = new double[_n];
    double[] r = new double[_n];
    double[] rt = new double[_n];
    double[] df = new double[_n];
    System.arraycopy(_t, 0, t, 0, _n);
    System.arraycopy(_r, 0, r, 0, _n);
    System.arraycopy(_rt, 0, rt, 0, _n);
    System.arraycopy(_df, 0, df, 0, _n);
    r[index] = rate;
    rt[index] = rate * t[index];
    df[index] = Math.exp(-rt[index]);
    return new ISDACompliantCurve(t, r, rt, df);
  }

  /**
   * Adjust a discount factor at a particular index 
   * @param discountFactor The new discount factor
   * @param index The index of the knot
   * @return a new curve 
   */
  public ISDACompliantCurve withDiscountFactor(final double discountFactor, final int index) {
    ArgumentChecker.isTrue(index >= 0 && index < _n, "index out of range");
    double[] t = new double[_n];
    double[] r = new double[_n];
    double[] rt = new double[_n];
    double[] df = new double[_n];
    System.arraycopy(_t, 0, t, 0, _n);
    System.arraycopy(_r, 0, r, 0, _n);
    System.arraycopy(_rt, 0, rt, 0, _n);
    System.arraycopy(_df, 0, df, 0, _n);

    df[index] = discountFactor;
    rt[index] = -Math.log(discountFactor);
    r[index] = rt[index] / t[index];
    return new ISDACompliantCurve(t, r, rt, df);
  }

}
