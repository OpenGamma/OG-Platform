/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class NodalDoubleDoubleCurve extends DoubleDoubleCurve {

  public static NodalDoubleDoubleCurve from(final double[] xData, final double[] yData) {
    return new NodalDoubleDoubleCurve(xData, yData, false);
  }

  public static NodalDoubleDoubleCurve from(final Double[] xData, final Double[] yData) {
    return new NodalDoubleDoubleCurve(xData, yData, false);
  }

  public static NodalDoubleDoubleCurve from(final Map<Double, Double> data) {
    return new NodalDoubleDoubleCurve(data, false);
  }

  public static NodalDoubleDoubleCurve from(final DoublesPair[] data) {
    return new NodalDoubleDoubleCurve(data, false);
  }

  public static NodalDoubleDoubleCurve from(final Set<DoublesPair> data) {
    return new NodalDoubleDoubleCurve(data, false);
  }

  public static NodalDoubleDoubleCurve from(final double[] xData, final double[] yData, final String name) {
    return new NodalDoubleDoubleCurve(xData, yData, false, name);
  }

  public static NodalDoubleDoubleCurve from(final Double[] xData, final Double[] yData, final String name) {
    return new NodalDoubleDoubleCurve(xData, yData, false, name);
  }

  public static NodalDoubleDoubleCurve from(final Map<Double, Double> data, final String name) {
    return new NodalDoubleDoubleCurve(data, false, name);
  }

  public static NodalDoubleDoubleCurve from(final DoublesPair[] data, final String name) {
    return new NodalDoubleDoubleCurve(data, false, name);
  }

  public static NodalDoubleDoubleCurve from(final Set<DoublesPair> data, final String name) {
    return new NodalDoubleDoubleCurve(data, false, name);
  }

  public static NodalDoubleDoubleCurve fromSorted(final double[] xData, final double[] yData) {
    return new NodalDoubleDoubleCurve(xData, yData, true);
  }

  public static NodalDoubleDoubleCurve fromSorted(final Double[] xData, final Double[] yData) {
    return new NodalDoubleDoubleCurve(xData, yData, true);
  }

  public static NodalDoubleDoubleCurve fromSorted(final Map<Double, Double> data) {
    return new NodalDoubleDoubleCurve(data, true);
  }

  public static NodalDoubleDoubleCurve fromSorted(final DoublesPair[] data) {
    return new NodalDoubleDoubleCurve(data, true);
  }

  public static NodalDoubleDoubleCurve fromSorted(final Set<DoublesPair> data) {
    return new NodalDoubleDoubleCurve(data, true);
  }

  public static NodalDoubleDoubleCurve fromSorted(final double[] xData, final double[] yData, final String name) {
    return new NodalDoubleDoubleCurve(xData, yData, true, name);
  }

  public static NodalDoubleDoubleCurve fromSorted(final Double[] xData, final Double[] yData, final String name) {
    return new NodalDoubleDoubleCurve(xData, yData, true, name);
  }

  public static NodalDoubleDoubleCurve fromSorted(final Map<Double, Double> data, final String name) {
    return new NodalDoubleDoubleCurve(data, true, name);
  }

  public static NodalDoubleDoubleCurve fromSorted(final DoublesPair[] data, final String name) {
    return new NodalDoubleDoubleCurve(data, true, name);
  }

  public static NodalDoubleDoubleCurve fromSorted(final Set<DoublesPair> data, final String name) {
    return new NodalDoubleDoubleCurve(data, true, name);
  }

  public NodalDoubleDoubleCurve(final double[] xData, final double[] yData, final boolean isSorted) {
    super(xData, yData, isSorted);
  }

  public NodalDoubleDoubleCurve(final Double[] xData, final Double[] yData, final boolean isSorted) {
    super(xData, yData, isSorted);
  }

  public NodalDoubleDoubleCurve(final Map<Double, Double> data, final boolean isSorted) {
    super(data, isSorted);
  }

  public NodalDoubleDoubleCurve(final DoublesPair[] data, final boolean isSorted) {
    super(data, isSorted);
  }

  public NodalDoubleDoubleCurve(final Set<DoublesPair> data, final boolean isSorted) {
    super(data, isSorted);
  }

  public NodalDoubleDoubleCurve(final double[] xData, final double[] yData, final boolean isSorted, final String name) {
    super(xData, yData, isSorted, name);
  }

  public NodalDoubleDoubleCurve(final Double[] xData, final Double[] yData, final boolean isSorted, final String name) {
    super(xData, yData, isSorted, name);
  }

  public NodalDoubleDoubleCurve(final Map<Double, Double> data, final boolean isSorted, final String name) {
    super(data, isSorted, name);
  }

  public NodalDoubleDoubleCurve(final DoublesPair[] data, final boolean isSorted, final String name) {
    super(data, isSorted, name);
  }

  public NodalDoubleDoubleCurve(final Set<DoublesPair> data, final boolean isSorted, final String name) {
    super(data, isSorted, name);
  }

  @Override
  public Double getYValue(final Double x) {
    Validate.notNull(x, "x");
    final int index = Arrays.binarySearch(getXDataAsPrimitive(), x);
    if (index < 0) {
      throw new IllegalArgumentException("Curve does not contain data for x point " + x);
    }
    return getYDataAsPrimitive()[index];
  }
}
