/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.curve;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.Validate;

import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public abstract class InterpolatedYieldAndDiscountCurve extends YieldAndDiscountCurve {
  private final Map<Double, Double> _data;
  private final SortedMap<Double, Interpolator1D<? extends Interpolator1DDataBundle>> _interpolators;
  private final SortedMap<Double, Interpolator1DDataBundle> _models;

  /**
   * 
   * @param t
   *          An array containing maturities in years
   * @param y
   *          An array containing either interest rates
   *          in percent (e.g. 3% = 0.03) or discount factors
   * @param interpolator
   *          An interpolator to get interest rates / discount factors for
   *          maturities that fall in between nodes. This cannot be null.
   * @throws IllegalArgumentException
   *           Thrown if the arrays are null or empty, or if it contains a
   *           negative time to maturity.
   */
  public InterpolatedYieldAndDiscountCurve(final double[] t, final double[] y, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    this(t, y, Collections.<Double, Interpolator1D<? extends Interpolator1DDataBundle>> singletonMap(Double.POSITIVE_INFINITY, interpolator));
  }

  /**
   * 
   * @param t
   *          An array containing maturities in years
   * @param y
   *          An array containing either interest rates
   *          in percent (e.g. 3% = 0.03) or discount factors
   * @param interpolators
   *          A map of times and interpolators. This allows different
   *          interpolators to be used for different regions of the curve. The time value is
   *          the maximum time in years for which an interpolator is valid.
   * @throws IllegalArgumentException
   *           Thrown if the arrays are null or empty, or if it contains a
   *           negative time to maturity.
   */
  public InterpolatedYieldAndDiscountCurve(final double[] t, final double[] y, final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators) {
    Validate.notNull(t);
    Validate.notNull(y);
    Validate.notNull(interpolators);
    Validate.notEmpty(interpolators);
    final int n = t.length;
    if (n < 2) {
      throw new IllegalArgumentException("Need to have at least two data points for an interpolated curve");
    }
    if (n != y.length) {
      throw new IllegalArgumentException("Need to have same number of values in x and y data");
    }
    assert ArgumentChecker.hasNegativeElement(interpolators.keySet()) == false;
    assert ArgumentChecker.hasNullElement(interpolators.values()) == false;
    final Map<Double, Double> data = new HashMap<Double, Double>();
    for (int i = 0; i < n; i++) {
      assert (t[i] >= 0);
      data.put(t[i], y[i]);
    }
    if (data.size() != n) {
      throw new IllegalArgumentException("Some nodes are not none distinct");
    }
    _data = Collections.<Double, Double> unmodifiableMap(data);
    _interpolators = Collections.<Double, Interpolator1D<? extends Interpolator1DDataBundle>> unmodifiableSortedMap(new TreeMap<Double, Interpolator1D<? extends Interpolator1DDataBundle>>(
        interpolators));
    final SortedMap<Double, Interpolator1DDataBundle> models = new TreeMap<Double, Interpolator1DDataBundle>();
    for (final Map.Entry<Double, Interpolator1D<? extends Interpolator1DDataBundle>> entry : _interpolators.entrySet()) {
      models.put(entry.getKey(), entry.getValue().getDataBundle(t, y));
    }
    _models = Collections.unmodifiableSortedMap(models);
  }

  /**
   * 
   * @param data
   *          A map containing pairs of maturities in years and and either interest rates
   *          in percent (e.g. 3% = 0.03) or discount factors
   * @param interpolator
   *          An interpolator to get interest rates / discount factors for
   *          maturities that fall in between nodes. This cannot be null.
   * @throws IllegalArgumentException
   *           Thrown if the data map is null or empty, or if it contains a
   *           negative time to maturity.
   */
  public InterpolatedYieldAndDiscountCurve(final Map<Double, Double> data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    this(data, Collections.<Double, Interpolator1D<? extends Interpolator1DDataBundle>> singletonMap(Double.POSITIVE_INFINITY, interpolator));
  }

  /**
   * 
   * @param data
   *          A map containing pairs of maturities in years and and either interest rates
   *          in percent (e.g. 3% = 0.03) or discount factors
   * @param interpolators
   *          A map of times and interpolators. This allows different
   *          interpolators
   *          to be used for different regions of the curve. The time value is
   *          the
   *          maximum time in years for which an interpolator is valid.
   * @throws IllegalArgumentException
   *           Thrown if the data map is null or empty, or if it contains a
   *           negative time to maturity.
   */
  public InterpolatedYieldAndDiscountCurve(final Map<Double, Double> data, final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators) {
    Validate.notNull(data);
    Validate.notNull(interpolators);
    Validate.notEmpty(interpolators);
    if (data.size() < 2) {
      throw new IllegalArgumentException("Need to have at least two data points for an interpolated curve");
    }
    assert ArgumentChecker.hasNullElement(data.keySet()) == false;
    assert ArgumentChecker.hasNullElement(data.values()) == false;
    assert ArgumentChecker.hasNegativeElement(data.keySet()) == false;
    assert ArgumentChecker.hasNegativeElement(interpolators.keySet()) == false;
    assert ArgumentChecker.hasNullElement(interpolators.values()) == false;
    _data = Collections.<Double, Double> unmodifiableMap(data);
    _interpolators = Collections.<Double, Interpolator1D<? extends Interpolator1DDataBundle>> unmodifiableSortedMap(new TreeMap<Double, Interpolator1D<? extends Interpolator1DDataBundle>>(
        interpolators));
    final SortedMap<Double, Interpolator1DDataBundle> models = new TreeMap<Double, Interpolator1DDataBundle>();
    for (final Map.Entry<Double, Interpolator1D<? extends Interpolator1DDataBundle>> entry : _interpolators.entrySet()) {
      models.put(entry.getKey(), entry.getValue().getDataBundle(data));
    }
    _models = Collections.unmodifiableSortedMap(models);
  }

  /**
   * 
   * @return The data.
   */
  public Map<Double, Double> getData() {
    return _data;
  }

  /**
   * 
   * @return The interpolator for this curve.
   */
  public SortedMap<Double, Interpolator1D<? extends Interpolator1DDataBundle>> getInterpolators() {
    return _interpolators;
  }

  public SortedMap<Double, Interpolator1DDataBundle> getDataBundles() {
    return _models;
  }

  /**
   * 
   * @param t Time in years
   * @return The interest rate for time to maturity <i>t</i>.
   * @throws IllegalArgumentException
   *           If the time to maturity is negative.
   */
  @Override
  public abstract double getInterestRate(final Double t);

  /**
   * 
   * @param t The time in years
   * @return The discount factor for time to maturity <i>t</i>.
   * @throws IllegalArgumentException
   *           If the time to maturity is negative.
   */
  @Override
  public abstract double getDiscountFactor(final Double t);

  @Override
  public Set<Double> getMaturities() {
    return getData().keySet();
  }

  @Override
  public abstract YieldAndDiscountCurve withParallelShift(final Double shift);

  @Override
  public abstract YieldAndDiscountCurve withSingleShift(final Double t, final Double shift);

  @Override
  public abstract YieldAndDiscountCurve withMultipleShifts(final Map<Double, Double> shifts);

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_data == null) ? 0 : _data.hashCode());
    result = prime * result + ((_interpolators == null) ? 0 : _interpolators.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final InterpolatedYieldAndDiscountCurve other = (InterpolatedYieldAndDiscountCurve) obj;
    if (_data == null) {
      if (other._data != null) {
        return false;
      }
    } else if (!_data.equals(other._data)) {
      return false;
    }
    if (_interpolators == null) {
      if (other._interpolators != null) {
        return false;
      }
    } else if (!_interpolators.equals(other._interpolators)) {
      return false;
    }
    return true;
  }
}
