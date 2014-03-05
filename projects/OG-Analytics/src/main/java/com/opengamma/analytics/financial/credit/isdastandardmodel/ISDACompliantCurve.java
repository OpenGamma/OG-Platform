/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.util.ArgumentChecker;

/**
 * A yield or hazard curve values between nodes are linearly interpolated from t*r points,
 * where t is time and r is the zero rate.
 */
@BeanDefinition
public class ISDACompliantCurve extends DoublesCurve {

  // the knot positions and values
  @PropertyDefinition(get = "manual", set = "private")
  private double[] _t;

  @PropertyDefinition(get = "manual", set = "private")
  private double[] _rt;

  public static ISDACompliantCurve makeFromForwardRates(final double[] t, final double[] fwd) {
    ArgumentChecker.notEmpty(t, "t");
    ArgumentChecker.notEmpty(fwd, "fwd");
    final int n = t.length;
    ArgumentChecker.isTrue(n == fwd.length, "length of t not equal to length of fwd");
    final double[] rt = new double[n];
    rt[0] = t[0] * fwd[0];
    for (int i = 1; i < n; i++) {
      rt[i] = rt[i - 1] + fwd[i] * (t[i] - t[i - 1]);
    }
    return new ISDACompliantCurve(new double[][] {t, rt });
  }

  public static ISDACompliantCurve makeFromRT(final double[] t, final double[] rt) {
    ArgumentChecker.notEmpty(t, "t");
    ArgumentChecker.notEmpty(rt, "rt");
    ArgumentChecker.isTrue(t.length == rt.length, "length of t not equal to length of rt");
    return new ISDACompliantCurve(new double[][] {t, rt });
  }

  /**
   * Constructor for Joda-Beans.
   */
  protected ISDACompliantCurve() {
  }

  /**
   * Creates a flat curve at level r.
   * 
   * @param t  (arbitrary) single knot point (t > 0)
   * @param r  the level
   */
  public ISDACompliantCurve(final double t, final double r) {
    this(new double[] {t }, new double[] {r });
  }

  /**
   * Creates an instance from a set of times and zero rates.
   *
   * @param t  the set of times that form the knots of the curve, must be ascending with the first value >= 0, not null
   * @param r  the set of zero rates, not null
   */
  public ISDACompliantCurve(final double[] t, final double[] r) {
    super("");
    ArgumentChecker.notEmpty(t, "t");
    ArgumentChecker.notEmpty(r, "r");
    final int n = t.length;
    ArgumentChecker.isTrue(n == r.length, "times and rates different lengths");
    ArgumentChecker.isTrue(t[0] >= 0, "first t must be >= 0.0");
    for (int i = 1; i < n; i++) {
      ArgumentChecker.isTrue(t[i] > t[i - 1], "Times must be ascending");
    }

    _t = new double[n];
    _rt = new double[n];
    System.arraycopy(t, 0, _t, 0, n);
    for (int i = 0; i < n; i++) {
      _rt[i] = r[i] * t[i]; // We make no check that rt is ascending (i.e. we allow negative forward rates) 
    }
  }

  protected ISDACompliantCurve(final double[][] tAndRt) {
    super("");
    ArgumentChecker.notNull(tAndRt, "tAndRt");
    _t = tAndRt[0].clone();
    _rt = tAndRt[1].clone();
  }

  /**
   * Creates a shallow clone of the specified curve.
   * 
   * @param from  the curve to clone from, not null
   */
  protected ISDACompliantCurve(final ISDACompliantCurve from) {
    super(from.getName());
    ArgumentChecker.notNull(from, "null from");
    // Shallow copy
    _t = from._t;
    _rt = from._rt;
  }

  /**
   * A curve in which the knots are measured (in fractions of a year) from a particular base-date but the curve is 'observed'
   * from a different base-date. As an example<br>
   * Today (the observation point) is 11-Jul-13, but the yield curve is snapped (bootstrapped from money market and swap rates)
   * on 10-Jul-13 - seen from today there is an offset of -1/365 (assuming a day count of ACT/365) that must be applied to use
   * the yield curve today.  <br>
   * In general, a discount curve observed at time $t_1$ can be written as $P(t_1,T)$. Observed from time $t_2$ this is
   * $P(t_2,T) = \frac{P(t_1,T)}{P(t_1,t_2)}$
   * 
   * @param timesFromBaseDate  the times measured from the base date of the curve, not null
   * @param r  zero rates, not null
   * @param newBaseFromOriginalBase  if this curve is to be used from a new base-date, this is the offset from the original curve base
   */
  protected ISDACompliantCurve(final double[] timesFromBaseDate, final double[] r, final double newBaseFromOriginalBase) {
    final int n = timesFromBaseDate.length;
    ArgumentChecker.isTrue(n == r.length, "times and rates different lengths");
    ArgumentChecker.isTrue(timesFromBaseDate[0] >= 0.0, "timesFromBaseDate must be >= 0");

    for (int i = 1; i < n; i++) {
      ArgumentChecker.isTrue(timesFromBaseDate[i] > timesFromBaseDate[i - 1], "Times must be ascending");
    }

    if (newBaseFromOriginalBase == 0) { //no offset 
      _t = new double[n];
      _rt = new double[n];
      System.arraycopy(timesFromBaseDate, 0, _t, 0, n);
      for (int i = 0; i < n; i++) {
        _rt[i] = r[i] * _t[i]; // We make no check that rt is ascending (i.e. we allow negative forward rates)
      }
    } else if (newBaseFromOriginalBase < timesFromBaseDate[0]) {
      //offset less than t value of 1st knot, so no knots are not removed 
      _t = new double[n];
      _rt = new double[n];
      final double eta = r[0] * newBaseFromOriginalBase;
      for (int i = 0; i < n; i++) {
        _t[i] = timesFromBaseDate[i] - newBaseFromOriginalBase;
        _rt[i] = r[i] * timesFromBaseDate[i] - eta;
      }
    } else if (newBaseFromOriginalBase >= timesFromBaseDate[n - 1]) {
      //new base after last knot. The new 'curve' has a constant zero rate which we represent with a nominal knot at 1.0
      _t = new double[1];
      _rt = new double[1];
      _t[0] = 1.0;
      _rt[0] = (r[n - 1] * timesFromBaseDate[n - 1] - r[n - 2] * timesFromBaseDate[n - 2]) / (timesFromBaseDate[n - 1] - timesFromBaseDate[n - 2]);
    } else {
      //offset greater than (or equal to) t value of 1st knot, so at least one knot must be removed  
      int index = Arrays.binarySearch(timesFromBaseDate, newBaseFromOriginalBase);
      if (index < 0) {
        index = -(index + 1);
      } else {
        index++;
      }
      final double eta = (r[index - 1] * timesFromBaseDate[index - 1] * (timesFromBaseDate[index] - newBaseFromOriginalBase) + r[index] * timesFromBaseDate[index] *
          (newBaseFromOriginalBase - timesFromBaseDate[index - 1])) /
          (timesFromBaseDate[index] - timesFromBaseDate[index - 1]);
      final int m = n - index;
      _t = new double[m];
      _rt = new double[m];
      for (int i = 0; i < m; i++) {
        _t[i] = timesFromBaseDate[i + index] - newBaseFromOriginalBase;
        _rt[i] = r[i + index] * timesFromBaseDate[i + index] - eta;
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the knot times.
   * 
   * @return the knot times, not null
   */
  public double[] getKnotTimes() {
    return _t.clone();
  }

  /**
   * Gets the knot zero rates.
   * 
   * @return the knot zero rates, not null
   */
  public double[] getKnotZeroRates() {
    final int n = _t.length;
    final double[] r = new double[n];
    for (int i = 0; i < n; i++) {
      r[i] = _rt[i] / _t[i];
    }
    return r;
  }

  /**
   * The discount factor or survival probability.
   * 
   * @param t  the time
   * @return the discount factor value
   */
  public double getDiscountFactor(final double t) {
    return Math.exp(-getRT(t));
  }

  /**
   * Gets the time at the specified index.
   * 
   * @param index  the zero-based index
   * @return the time
   */
  public double getTimeAtIndex(final int index) {
    return _t[index];
  }

  /**
   * Gets the zero rate at the specified index.
   * 
   * @param index  the zero-based index
   * @return the zero rate
   */
  public double getZeroRateAtIndex(final int index) {
    return _rt[index] / _t[index];
  }

  /**
   * Gets the RT value at the specified index.
   * <p>
   * RT is zero rate multiplied by time, which is the same as the negative log of the discount factor.
   * 
   * @param index  the zero-based index
   * @return the RT value
   */
  public double getRTAtIndex(final int index) {
    return _rt[index];
  }

  /**
   * Gets the zero rate or zero hazard rate at the specified time.
   * 
   * @param t  time
   * @return zero rate value
   */
  public double getZeroRate(final double t) {
    ArgumentChecker.isTrue(t >= 0, "require t >= 0.0");
    // short-cut doing binary search
    if (t <= _t[0]) {
      return _rt[0] / _t[0];
    }
    final int n = _t.length;
    if (t > _t[n - 1]) {
      final double rt = getRT(t, n - 1);
      return rt / t;
    }
    final int index = Arrays.binarySearch(_t, t);
    if (index >= 0) {
      return _rt[index] / _t[index];
    }
    final int insertionPoint = -(1 + index);
    final double rt = getRT(t, insertionPoint);
    return rt / t;
  }

  /**
   * Gets the RT value at the specified time.
   * <p>
   * RT is zero rate multiplied by time, which is the same as the negative log of the discount factor.
   * 
   * @param t  time
   * @return the RT value
   */
  public double getRT(final double t) {
    //  ArgumentChecker.isTrue(t >= 0, "require t >= 0.0, was, {}", t);
    // short-cut doing binary search
    if (t <= _t[0]) {
      return _rt[0] * t / _t[0];
    }
    final int n = _t.length;
    if (t > _t[n - 1]) {
      return getRT(t, n - 1); //linear extrapolation
    }
    final int index = Arrays.binarySearch(_t, t);
    if (index >= 0) {
      return _rt[index];
    }
    final int insertionPoint = -(1 + index);
    return getRT(t, insertionPoint);
  }

  public double[] getRTandSensitivity(final double t, final int nodeIndex) {
    ArgumentChecker.isTrue(t >= 0, "require t >= 0.0, was, {}", t);
    final int n = _t.length;
    ArgumentChecker.isTrue(nodeIndex >= 0 && nodeIndex < n, "node index of {} out of range", nodeIndex);
    // short-cut doing binary search
    if (n == 1 || t <= _t[0]) {
      return new double[] {_rt[0] * t / _t[0], nodeIndex == 0 ? t : 0.0 };
    }

    int index;
    if (t > _t[n - 1]) {
      index = n - 1;
    } else if (t == _t[nodeIndex]) {
      return new double[] {_rt[nodeIndex], t };
    } else if (nodeIndex > 0 && t > _t[nodeIndex - 1] && t < _t[nodeIndex]) {
      index = nodeIndex;
    } else {
      index = Arrays.binarySearch(_t, t);
      if (index >= 0) {
        return new double[] {_rt[index], 0.0 }; //if nodeIndex == index, would have matched earlier 
      }
      index = -(index + 1);
      if (index == n) {
        index--;
      }
    }

    final double t1 = _t[index - 1];
    final double t2 = _t[index];
    final double dt = t2 - t1;
    final double w1 = (t2 - t) / dt;
    final double w2 = (t - t1) / dt;
    final double rt = w1 * _rt[index - 1] + w2 * _rt[index];
    double sense = 0.0;
    if (nodeIndex == index) {
      sense = t2 * w2;
    } else if (nodeIndex == index - 1) {
      sense = t1 * w1;
    }

    return new double[] {rt, sense };
  }

  private double getRT(final double t, final int insertionPoint) {
    if (insertionPoint == 0) {
      return t * _rt[0] / _t[0];
    }
    final int n = _t.length;
    if (insertionPoint == n) {
      return getRT(t, insertionPoint - 1); //linear extrapolation
    }

    final double t1 = _t[insertionPoint - 1];
    final double t2 = _t[insertionPoint];
    final double dt = t2 - t1;

    return ((t2 - t) * _rt[insertionPoint - 1] + (t - t1) * _rt[insertionPoint]) / dt;
  }

  public double getForwardRate(final double t) {
    // short-cut doing binary search
    if (t <= _t[0]) {
      return _rt[0] / _t[0];
    }
    final int n = _t.length;
    if (t > _t[n - 1]) {
      return getForwardRate(n - 1); //linear extrapolation
    }

    final int index = Arrays.binarySearch(_t, t);
    if (index >= 0) {
      //Strictly, the forward rate is undefined at the nodes - this defined the value at the node to be that infinitesimally before
      return getForwardRate(index);
    }
    final int insertionPoint = -(1 + index);
    return getForwardRate(insertionPoint);
  }

  private double getForwardRate(final int insertionPoint) {
    if (insertionPoint == 0) {
      return _rt[0] / _t[0];
    }
    final int n = _t.length;
    if (insertionPoint == n) {
      return getForwardRate(insertionPoint - 1);
    }
    final double dt = _t[insertionPoint] - _t[insertionPoint - 1];
    return (_rt[insertionPoint] - _rt[insertionPoint - 1]) / dt;
  }

  /**
   * Gets the number of knots in the curve.
   *
   * @return number of knots in curve
   */
  public int getNumberOfKnots() {
    return _t.length;
  }

  /**
   * Get the sensitivity of the interpolated rate at time t to the curve node.
   * Note, since the interpolator is highly local, most of the returned values will be zero,
   * so it maybe more efficient to call getSingleNodeSensitivity.
   * 
   * @param t  the time
   * @return the sensitivity to the nodes, not null
   */
  public double[] getNodeSensitivity(final double t) {

    final int n = _t.length;
    final double[] res = new double[n];

    // short-cut doing binary search
    if (t <= _t[0]) {
      res[0] = 1.0;
      return res;
    }
    if (t >= _t[n - 1]) {
      final int insertionPoint = n - 1;
      final double t1 = _t[insertionPoint - 1];
      final double t2 = _t[insertionPoint];
      final double dt = t2 - t1;
      res[insertionPoint - 1] = t1 * (t2 - t) / dt / t;
      res[insertionPoint] = t2 * (t - t1) / dt / t;
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
   * Gets the sensitivity of the interpolated zero rate at time t to the value of the zero rate at a given node (knot).
   * For a given index, i, this is zero unless $$t_{i-1} < t < t_{i+1}$$ since the interpolation is highly local.
   * 
   * @param t  the time
   * @param nodeIndex  the node index
   * @return the sensitivity to a single node
   */
  public double getSingleNodeSensitivity(final double t, final int nodeIndex) {
    ArgumentChecker.isTrue(t >= 0, "require t >= 0.0");
    ArgumentChecker.isTrue(nodeIndex >= 0 && nodeIndex < _t.length, "index out of range");

    if (t <= _t[0]) {
      return nodeIndex == 0 ? 1.0 : 0.0;
    }

    return getSingleNodeRTSensitivity(t, nodeIndex) / t;
  }

  /**
   * Gets the sensitivity of the interpolated zero rate times time (RT or -ln(discount factor))  at time t to the value of the zero rate at a given node (knot).
   * For a given index, i, this is zero unless $$t_{i-1} < t < t_{i+1}$$ since the interpolation is highly local.
   * @param t  the time
   * @param nodeIndex  the node index
   * @return the sensitivity to a single node
   */
  public double getSingleNodeRTSensitivity(final double t, final int nodeIndex) {
    ArgumentChecker.isTrue(t >= 0, "require t >= 0.0");
    final int n = _t.length;
    ArgumentChecker.isTrue(nodeIndex >= 0 && nodeIndex < n, "index out of range");

    if (t <= _t[0]) {
      return nodeIndex == 0 ? t : 0.0;
    }

    final int index = Arrays.binarySearch(_t, t);
    if (index >= 0) {
      return nodeIndex == index ? t : 0.0;
    }

    final int insertionPoint = Math.min(n - 1, -(1 + index));
    if (nodeIndex != insertionPoint && nodeIndex != insertionPoint - 1) {
      return 0.0;
    }

    final double t1 = _t[insertionPoint - 1];
    final double t2 = _t[insertionPoint];
    final double dt = t2 - t1;
    if (nodeIndex == insertionPoint) {
      return t2 * (t - t1) / dt;
    }

    return t1 * (t2 - t) / dt;
  }

  /**
   * The sensitivity of the discount factor at some time, t, to the value of the zero rate at a given node (knot).
   * For a given index, i, this is zero unless $$t_{i-1} < t < t_{i+1}$$ since the interpolation is highly local.
   * 
   * @param t  the time value of the discount factor
   * @param nodeIndex  the node index
   * @return the  sensitivity of a discount factor to a single node
   */
  public double getSingleNodeDiscountFactorSensitivity(final double t, final int nodeIndex) {

    final double[] temp = getRTandSensitivity(t, nodeIndex);
    return -temp[1] * Math.exp(-temp[0]);

  }

  //-------------------------------------------------------------------------
  /**
   * A curve in which the knots are measured (in fractions of a year) from a particular base-date but the curve is 'observed'
   * from a different base-date. As an example<br>
   * Today (the observation point) is 11-Jul-13, but the yield curve is snapped (bootstrapped from money market and swap rates)
   * on 10-Jul-13 - seen from the original base date (10-Jul-13) there is an offset of 1/365(assuming a day count of ACT/365) that must be applied to use
   * the yield curve today.  <br>
   * In general, a discount curve observed at time $t_1$ can be written as $P(t_1,T)$. Observed from time $t_2$ this is
   * $P(t_2,T) = \frac{P(t_1,T)}{P(t_1,t_2)}$
   * 
   * @param newBaseFromOriginalBase  if this curve is to be used from a new base-date, what is the offset from the original curve base
   * @return a new curve with the offset
   */
  public ISDACompliantCurve withOffset(final double newBaseFromOriginalBase) {
    return new ISDACompliantCurve(_t, getKnotZeroRates(), newBaseFromOriginalBase);
  }

  /**
   * Update are rates in curve.
   * 
   * @param r  the set of rates, not null
   * @return a new curve, not null
   */
  public ISDACompliantCurve withRates(final double[] r) {
    return new ISDACompliantCurve(_t, r);
  }

  /**
   * Adjust a rate at a particular index.
   * 
   * @param rate  the new rate
   * @param index  the index of the knot
   * @return a new curve, not null
   */
  public ISDACompliantCurve withRate(final double rate, final int index) {
    final int n = _t.length;
    ArgumentChecker.isTrue(index >= 0 && index < n, "index out of range");
    final double[] t = _t.clone();
    final double[] rt = _rt.clone();

    rt[index] = rate * t[index];
    return new ISDACompliantCurve(new double[][] {t, rt });
  }

  public void setRate(final double rate, final int index) {
    final int n = _t.length;
    ArgumentChecker.isTrue(index >= 0 && index < n, "index out of range");
    _rt[index] = rate * _t[index];
  }

  /**
   * Adjust a discount factor at a particular index.
   * 
   * @param discountFactor  the new discount factor
   * @param index  the index of the knot
   * @return a new curve, not null
   */
  public ISDACompliantCurve withDiscountFactor(final double discountFactor, final int index) {
    final int n = _t.length;
    ArgumentChecker.isTrue(index >= 0 && index < n, "index out of range");
    final double[] t = _t.clone();
    final double[] rt = _rt.clone();
    rt[index] = -Math.log(discountFactor);
    return new ISDACompliantCurve(new double[][] {t, rt });
  }

  /**
   * Gets the times, which must not be altered.
   * 
   * @return the times, not null
   */
  public double[] getT() {
    return _t;
  }

  public double[] getRt() {
    return _rt;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_rt);
    result = prime * result + Arrays.hashCode(_t);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ISDACompliantCurve other = (ISDACompliantCurve) obj;
    if (!Arrays.equals(_rt, other._rt)) {
      return false;
    }
    if (!Arrays.equals(_t, other._t)) {
      return false;
    }
    return true;
  }

  @Override
  public Double[] getYValueParameterSensitivity(final Double x) {
    return ArrayUtils.toObject(getNodeSensitivity(x));
  }

  @Override
  public double getDyDx(final double x) {
    if (x <= _t[0]) {
      return 0.0;
    }
    return (getForwardRate(x) - getZeroRate(x)) / x;
  }

  @Override
  public Double[] getXData() {
    return ArrayUtils.toObject(_t);
  }

  @Override
  public Double[] getYData() {
    return ArrayUtils.toObject(getKnotZeroRates());
  }

  @Override
  public int size() {
    return getNumberOfKnots();
  }

  @Override
  public Double getYValue(final Double x) {
    return getZeroRate(x);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ISDACompliantCurve}.
   * @return the meta-bean, not null
   */
  public static ISDACompliantCurve.Meta meta() {
    return ISDACompliantCurve.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ISDACompliantCurve.Meta.INSTANCE);
  }

  @Override
  public ISDACompliantCurve.Meta metaBean() {
    return ISDACompliantCurve.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Sets the t.
   * @param t  the new value of the property
   */
  private void setT(double[] t) {
    this._t = t;
  }

  /**
   * Gets the the {@code t} property.
   * @return the property, not null
   */
  public final Property<double[]> t() {
    return metaBean().t().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Sets the rt.
   * @param rt  the new value of the property
   */
  private void setRt(double[] rt) {
    this._rt = rt;
  }

  /**
   * Gets the the {@code rt} property.
   * @return the property, not null
   */
  public final Property<double[]> rt() {
    return metaBean().rt().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public ISDACompliantCurve clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("ISDACompliantCurve{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  @Override
  protected void toString(StringBuilder buf) {
    super.toString(buf);
    buf.append("t").append('=').append(JodaBeanUtils.toString(getT())).append(',').append(' ');
    buf.append("rt").append('=').append(JodaBeanUtils.toString(getRt())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ISDACompliantCurve}.
   */
  public static class Meta extends DoublesCurve.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code t} property.
     */
    private final MetaProperty<double[]> _t = DirectMetaProperty.ofReadWrite(
        this, "t", ISDACompliantCurve.class, double[].class);
    /**
     * The meta-property for the {@code rt} property.
     */
    private final MetaProperty<double[]> _rt = DirectMetaProperty.ofReadWrite(
        this, "rt", ISDACompliantCurve.class, double[].class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "t",
        "rt");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 116:  // t
          return _t;
        case 3650:  // rt
          return _rt;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ISDACompliantCurve> builder() {
      return new DirectBeanBuilder<ISDACompliantCurve>(new ISDACompliantCurve());
    }

    @Override
    public Class<? extends ISDACompliantCurve> beanType() {
      return ISDACompliantCurve.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code t} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<double[]> t() {
      return _t;
    }

    /**
     * The meta-property for the {@code rt} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<double[]> rt() {
      return _rt;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 116:  // t
          return ((ISDACompliantCurve) bean).getT();
        case 3650:  // rt
          return ((ISDACompliantCurve) bean).getRt();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 116:  // t
          ((ISDACompliantCurve) bean).setT((double[]) newValue);
          return;
        case 3650:  // rt
          ((ISDACompliantCurve) bean).setRt((double[]) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
