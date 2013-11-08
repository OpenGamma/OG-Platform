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
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.util.ArgumentChecker;

/**
 * A yield or hazard curve values between nodes are linearly interpolated from t*r points (where t is time and r is the zero rate)
 */

public class ISDACompliantCurve extends DoublesCurve {

  // these are simply cached values (they can be recalculated from _t & _r)
  // number of knots in curve
  private final int _n;
  //inverse of time steps 
  private final double[] _invDt;
  // r*t (-ln(p)) 
  private final double[] _rt;

  // the knot positions and values
  @PropertyDefinition(get = "private", set = "private")
  private final double[] _t;

  @PropertyDefinition(get = "private", set = "private")
  private final double[] _r;

  /**
   * Flat curve at level r
   * @param t (arbitrary) single knot point (t > 0)
   * @param r the level
   */
  public ISDACompliantCurve(final double t, final double r) {
    this(new double[] {t }, new double[] {r });
  }

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
    _invDt = new double[_n - 1];
    //  _df = new double[_n];
    System.arraycopy(t, 0, _t, 0, _n);
    System.arraycopy(r, 0, _r, 0, _n);
    for (int i = 0; i < _n; i++) {
      _rt[i] = _r[i] * _t[i]; // We make no check that rt is ascending (i.e. we allow negative forward rates)
    }
    for (int i = 1; i < _n; i++) {
      _invDt[i - 1] = 1. / (_t[i] - _t[i - 1]);
    }
  }

  protected ISDACompliantCurve(final ISDACompliantCurve from) {
    ArgumentChecker.notNull(from, "null from");
    // Shallow copy
    _n = from._n;
    _t = from._t;
    _r = from._r;
    _rt = from._rt;
    _invDt = from._invDt;
    //  _df = from._df;
  }

  /**
   * A curve in which the knots are measured (in fractions of a year) from a particular base-date but the curve is 'observed'
   * from a different base-date. As an example<br>
   * Today (the observation point) is 11-Jul-13, but the yield curve is snapped (bootstrapped from money market and swap rates)
   * on 10-Jul-13 - seen from today there is an offset of -1/365 (assuming a day count of ACT/365) that must be applied to use
   * the yield curve today.  <br>
   * In general, a discount curve observed at time $t_1$ can be written as $P(t_1,T)$. Observed from time $t_2$ this is
   * $P(t_2,T) = \frac{P(t_1,T)}{P(t_1,t_2)}$
   * @param timesFromBaseDate times measured from the base date of the curve
   * @param r zero rates
   * @param newBaseFromOriginalBase if this curve is to be used from a new base-date, what is the offset from the original curve base
   */
  protected ISDACompliantCurve(final double[] timesFromBaseDate, final double[] r, final double newBaseFromOriginalBase) {
    ArgumentChecker.notEmpty(timesFromBaseDate, "t empty");
    ArgumentChecker.notEmpty(r, "r empty");
    _n = timesFromBaseDate.length;
    ArgumentChecker.isTrue(_n == r.length, "r and t different lengths");
    ArgumentChecker.isTrue(timesFromBaseDate[0] >= 0.0, "timesFromBaseDate must be >= 0");

    // TODO allow larger offsets
    ArgumentChecker.isTrue(timesFromBaseDate[0] - newBaseFromOriginalBase > 0, "offsetFromNewBaseDate too negative");
    for (int i = 1; i < _n; i++) {
      ArgumentChecker.isTrue(timesFromBaseDate[i] > timesFromBaseDate[i - 1], "Times must be ascending");
    }

    _t = new double[_n];
    _r = new double[_n];
    _rt = new double[_n];
    final double r0 = r[0];
    if (newBaseFromOriginalBase == 0.0) {
      System.arraycopy(timesFromBaseDate, 0, _t, 0, _n);
      System.arraycopy(r, 0, _r, 0, _n);
      for (int i = 0; i < _n; i++) {
        _rt[i] = _r[i] * _t[i]; // We make no check that rt is ascending (i.e. we allow negative forward rates)
      }
    } else {
      _r[0] = r0;
      for (int i = 0; i < _n; i++) {
        _t[i] = timesFromBaseDate[i] - newBaseFromOriginalBase;
        _rt[i] = r[i] * timesFromBaseDate[i] - r0 * newBaseFromOriginalBase;
      }
      for (int i = 1; i < _n; i++) {
        _r[i] = _rt[i] / _t[i];
      }
    }
    _invDt = new double[_n - 1];
    for (int i = 1; i < _n; i++) {
      _invDt[i - 1] = 1. / (_t[i] - _t[i - 1]);
    }

  }

  /**
   * Constructor mainly used for serialization. This takes all the intermediate calculation results to ensure
   * a strict copy of the original. This should not be the main constructor used in the general case.
   */
  @Deprecated
  public ISDACompliantCurve(final double[] t, final double[] r, final double[] rt, final double[] invDT) {
    _n = t.length;
    _t = t;
    _r = r;
    _rt = rt;
    _invDt = invDT;

  }

  public double[] getKnotTimes() {
    final double[] res = new double[_n];
    System.arraycopy(_t, 0, res, 0, _n);
    return res;
  }

  public double[] getKnotZeroRates() {
    final double[] res = new double[_n];
    System.arraycopy(_r, 0, res, 0, _n);
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
    return Math.exp(-getRT(t));
  }

  public double getTimeAtIndex(final int index) {
    return _t[index];
  }

  public double getZeroRateAtIndex(final int index) {
    return _r[index];
  }

  public double getRTAtIndex(final int index) {
    return _rt[index];
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
    if (t > _t[_n - 1]) {
      final double rt = getRT(t, _n - 1);
      return rt / t;
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
    ArgumentChecker.isTrue(t >= 0, "require t >= 0.0, was, {}", t);

    // short-cut doing binary search
    if (t <= _t[0]) {
      return _r[0] * t;
    }
    if (t > _t[_n - 1]) {
      return getRT(t, _n - 1); //linear extrapolation
    }

    final int index = Arrays.binarySearch(_t, t);
    if (index >= 0) {
      return _rt[index];
    }

    final int insertionPoint = -(1 + index);
    return getRT(t, insertionPoint);

  }

  double getRT(final double t, final int insertionPoint) {
    if (insertionPoint == 0) {
      return t * _r[0];
    }
    if (insertionPoint == _n) {
      return getRT(t, insertionPoint - 1); //linear extrapolation
    }
    final int ipm1 = insertionPoint - 1;
    return ((_t[insertionPoint] - t) * _rt[ipm1] + (t - _t[ipm1]) * _rt[insertionPoint]) * _invDt[ipm1];
  }

  public double getForwardRate(final double t) {
    // short-cut doing binary search
    if (t <= _t[0]) {
      return _r[0];
    }
    if (t > _t[_n - 1]) {
      return getForwardRate(_n - 1); //linear extrapolation
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
      return _r[0];
    }
    if (insertionPoint == _n) {
      return getForwardRate(insertionPoint - 1);
    }
    final int ipm1 = insertionPoint - 1;
    return (_rt[insertionPoint] - _rt[insertionPoint - 1]) * _invDt[ipm1];
  }

  /**
   *
   * @return number of knots in curve
   */
  public int getNumberOfKnots() {
    return _n;
  }

  /**
   * get the sensitivity of the interpolated rate at time t to the curve node. Note, since the interpolator is highly local, most
   * of the returned values will be zero, so it maybe more efficient to call getSingleNodeSensitivity
   * @param t The time
   * @return sensitivity to the nodes
   */
  public double[] getNodeSensitivity(final double t) {

    final double[] res = new double[_n];

    // short-cut doing binary search
    if (t <= _t[0]) {
      res[0] = 1.0;
      return res;
    }
    if (t >= _t[_n - 1]) {
      final int insertionPoint = _n - 1;
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
   * get the sensitivity of the interpolated zero rate at time t to the value of the zero rate at a given node (knot).  For a
   * given index, i, this is zero unless $$t_{i-1} < t < t_{i+1}$$ since the interpolation is highly local.
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
    //    if (t >= _t[_n - 1]) {
    //
    //      return nodeIndex == _n - 1 ? 1.0 : 0.0;
    //    }

    final int index = Arrays.binarySearch(_t, t);
    if (index >= 0) {
      return nodeIndex == index ? 1.0 : 0.0;
    }

    final int insertionPoint = Math.min(_n - 1, -(1 + index));
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

  /**
   * The sensitivity of the discount factor at some time, t, to the value of the zero rate at a given node (knot). For a
   * given index, i, this is zero unless $$t_{i-1} < t < t_{i+1}$$ since the interpolation is highly local.
   * @param t time value of the discount factor
   * @param nodeIndex node index
   * @return sensitivity of a discount factor to a single node
   */
  public double getSingleNodeDiscountFactorSensitivity(final double t, final int nodeIndex) {
    ArgumentChecker.isTrue(t >= 0, "require t >= 0.0");
    ArgumentChecker.isTrue(nodeIndex >= 0 && nodeIndex < _n, "index out of range");

    if (t == 0.0) {
      return 0.0;
    }
    if (t <= _t[0]) {
      return nodeIndex == 0 ? -t * Math.exp(-t * _r[0]) : 0.0;
    }
    //    if (t >= _t[_n - 1]) {
    //      return nodeIndex == _n - 1 ? -t * Math.exp(-t * _r[_n - 1]) : 0.0;
    //    }

    final int index = Arrays.binarySearch(_t, t);
    if (index >= 0) {
      return nodeIndex == index ? -t * Math.exp(-_rt[nodeIndex]) : 0.0;
    }

    final int insertionPoint = Math.min(_n - 1, -(1 + index));
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
   * A curve in which the knots are measured (in fractions of a year) from a particular base-date but the curve is 'observed'
   * from a different base-date. As an example<br>
   * Today (the observation point) is 11-Jul-13, but the yield curve is snapped (bootstrapped from money market and swap rates)
   * on 10-Jul-13 - seen from today there is an offset of -1/365 (assuming a day count of ACT/365) that must be applied to use
   * the yield curve today.  <br>
   * In general, a discount curve observed at time $t_1$ can be written as $P(t_1,T)$. Observed from time $t_2$ this is
   * $P(t_2,T) = \frac{P(t_1,T)}{P(t_1,t_2)}$
   * @param newBaseFromOriginalBase if this curve is to be used from a new base-date, what is the offset from the curve base
   * @return a new curve with the offset
   */
  public ISDACompliantCurve withOffset(final double newBaseFromOriginalBase) {
    return new ISDACompliantCurve(_t, _r, newBaseFromOriginalBase);
  }

  /**
   * Update are rates in curve.
   * @param r Set of rates
   * @return a new curve
   */
  public ISDACompliantCurve withRates(final double[] r) {
    return new ISDACompliantCurve(_t, r);
  }

  /**
   * Adjust a rate at a particular index.
   * @param rate The new rate
   * @param index The index of the knot
   * @return a new curve
   */
  public ISDACompliantCurve withRate(final double rate, final int index) {
    ArgumentChecker.isTrue(index >= 0 && index < _n, "index out of range");
    final double[] t = new double[_n];
    final double[] r = new double[_n];
    final double[] rt = new double[_n];
    final double[] invDt = new double[_n - 1];
    //    final double[] df = new double[_n];
    System.arraycopy(_t, 0, t, 0, _n);
    System.arraycopy(_r, 0, r, 0, _n);
    System.arraycopy(_rt, 0, rt, 0, _n);
    System.arraycopy(_invDt, 0, invDt, 0, _n - 1);
    //   System.arraycopy(_df, 0, df, 0, _n);

    r[index] = rate;
    rt[index] = rate * t[index];
    //  df[index] = Math.exp(-rt[index]);
    return new ISDACompliantCurve(t, r, rt, invDt);
  }

  public void setRate(final double rate, final int index) {
    ArgumentChecker.isTrue(index >= 0 && index < _n, "index out of range");
    _r[index] = rate;
    _rt[index] = rate * _t[index];
    //  _df[index] = Math.exp(-_rt[index]);
  }

  /**
   * Adjust a discount factor at a particular index.
   * @param discountFactor The new discount factor
   * @param index The index of the knot
   * @return a new curve
   */
  public ISDACompliantCurve withDiscountFactor(final double discountFactor, final int index) {

    ArgumentChecker.isTrue(index >= 0 && index < _n, "index out of range");
    final double[] t = new double[_n];
    final double[] r = new double[_n];
    final double[] rt = new double[_n];
    final double[] invDt = new double[_n - 1];
    //  final double[] df = new double[_n];
    System.arraycopy(_t, 0, t, 0, _n);
    System.arraycopy(_r, 0, r, 0, _n);
    System.arraycopy(_rt, 0, rt, 0, _n);
    System.arraycopy(_invDt, 0, invDt, 0, _n - 1);
    //  System.arraycopy(_df, 0, df, 0, _n);

    //   df[index] = discountFactor;
    rt[index] = -Math.log(discountFactor);
    r[index] = rt[index] / t[index];
    return new ISDACompliantCurve(t, r, rt, invDt);
  }

  //  public double[] getT() {
  //    return _t;
  //  }
  //
  //  public double[] getR() {
  //    return _r;
  //  }
  //
  //  public double[] getRt() {
  //    return _rt;
  //  }

  //  public double[] getDf() {
  //    return _df;
  //  }

  @Override
  public Double[] getYValueParameterSensitivity(final Double x) {
    return ArrayUtils.toObject(getNodeSensitivity(x));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_r);
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
    if (!Arrays.equals(_r, other._r)) {
      return false;
    }
    if (!Arrays.equals(_t, other._t)) {
      return false;
    }
    return true;
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
    return ArrayUtils.toObject(_r);
  }

  @Override
  public int size() {
    return _n;
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
   * Gets the t.
   * @return the value of the property
   */
  private double[] getT() {
    return (_t != null ? _t.clone() : null);
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
   * Gets the r.
   * @return the value of the property
   */
  private double[] getR() {
    return (_r != null ? _r.clone() : null);
  }

  /**
   * Gets the the {@code r} property.
   * @return the property, not null
   */
  public final Property<double[]> r() {
    return metaBean().r().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public ISDACompliantCurve clone() {
    return (ISDACompliantCurve) super.clone();
  }

  @Override
  public String toString() {
    final StringBuilder buf = new StringBuilder(96);
    buf.append("ISDACompliantCurve{");
    final int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  @Override
  protected void toString(final StringBuilder buf) {
    super.toString(buf);
    buf.append("t").append('=').append(JodaBeanUtils.toString(getT())).append(',').append(' ');
    buf.append("r").append('=').append(JodaBeanUtils.toString(getR())).append(',').append(' ');
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
    private final MetaProperty<double[]> _t = DirectMetaProperty.ofReadOnly(this, "t", ISDACompliantCurve.class, double[].class);
    /**
     * The meta-property for the {@code r} property.
     */
    private final MetaProperty<double[]> _r = DirectMetaProperty.ofReadOnly(this, "r", ISDACompliantCurve.class, double[].class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(this, (DirectMetaPropertyMap) super.metaPropertyMap(), "t", "r");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(final String propertyName) {
      switch (propertyName.hashCode()) {
        case 116:  // t
          return _t;
        case 114:  // r
          return _r;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ISDACompliantCurve> builder() {
      throw new UnsupportedOperationException();
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
     * The meta-property for the {@code r} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<double[]> r() {
      return _r;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(final Bean bean, final String propertyName, final boolean quiet) {
      switch (propertyName.hashCode()) {
        case 116:  // t
          return ((ISDACompliantCurve) bean).getT();
        case 114:  // r
          return ((ISDACompliantCurve) bean).getR();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(final Bean bean, final String propertyName, final Object newValue, final boolean quiet) {
      switch (propertyName.hashCode()) {
        case 116:  // t
          if (quiet) {
            return;
          }
          throw new UnsupportedOperationException("Property cannot be written: t");
        case 114:  // r
          if (quiet) {
            return;
          }
          throw new UnsupportedOperationException("Property cannot be written: r");
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
