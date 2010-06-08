/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * 
 * A base class for interpolation in one dimension.
 * 
 * @author emcleod
 */

public abstract class Interpolator1D implements Interpolator<Map<Double, Double>, Double, Double>, Serializable {
  protected static final double EPS = 1e-12;

  /**
   * @param data
   *          A map containing the (x, y) data set.
   * @param value
   *          The value of x for which an interpolated value for y is to be
   *          found.
   * @return An InterpolationResult containing the interpolated value of y and
   *         (if appropriate) the estimated error of this value.
   */
  @Override
  public abstract InterpolationResult<Double> interpolate(Map<Double, Double> data, Double value);

  /**
   * 
   * @param data
   *          A map containing the (x, y) data set.
   * @return A map containing the data set sorted by x values.
   * @throws IllegalArgumentException
   *           Thrown if the data set is null or if its size is less than two.
   */
  protected Interpolator1DModel initData(final Map<Double, Double> data) {
    if (data == null) {
      throw new IllegalArgumentException("Data map was null");
    }
    if (data.size() < 2) {
      throw new IllegalArgumentException("Need at least two points to perform interpolation");
    }
    return new NavigableMapInterpolator1DModel(new TreeMap<Double, Double>(data));
  }

  /**
   * Given x for which the interpolated value of y is to be found, this method
   * returns the nearest low value of x in the data set.
   * 
   * @param data
   *          A map whose entries are (x, y) data points.
   * @param value
   *          The value of x for which the interpolated point y is to be found.
   * @return The nearest low value of x in the data set.
   * @throws InterpolationException
   *           If either of the arguments is null, if there is no nearest lower
   *           value of x in the data set, or if x is larger than the largest
   *           value of x in the data set.
   */
  protected Double getLowerBoundKey(final NavigableMap<Double, Double> data, final Double value) {
    if (data == null) {
      throw new IllegalArgumentException("Data set was null");
    }
    if (value == null) {
      throw new IllegalArgumentException("x value was null");
    }
    final Double lower = data.floorKey(value);
    if (lower == null) {
      throw new InterpolationException("Value " + value + " was less than the lowest data point for x " + data.firstKey());
    }
    if (!value.equals(data.lastKey()) && lower.equals(data.lastKey())) {
      throw new InterpolationException("Value " + value + " was greater than the largest data point for x " + data.lastKey());
    }
    return lower;
  }

  /**
   * Given x for which the interpolated value of y is to be found, this method
   * returns the index of the nearest low value of x in the data set. Note that
   * the index of the first element is zero.
   * 
   * @param data
   *          A map whose entries are (x, y) data points.
   * @param value
   *          The value of x for which the interpolated point y is to be found.
   * @return The index of the nearest low value of x in the data set.
   */
  protected int getLowerBoundIndex(final NavigableMap<Double, Double> data, final Double value) {
    final Double lower = getLowerBoundKey(data, value);
    int i = 0;
    final Iterator<Double> iter = data.keySet().iterator();
    Double key = iter.next();
    while (!key.equals(lower)) {
      key = iter.next();
      i++;
    }
    return i;
  }

  protected boolean classEquals(final Object o) {
    if (o == null) {
      return false;
    }
    return getClass().equals(o.getClass());
  }
}
