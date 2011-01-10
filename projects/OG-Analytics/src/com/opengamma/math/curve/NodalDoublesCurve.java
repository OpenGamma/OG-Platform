/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class NodalDoublesCurve extends DoublesCurve {

  public static NodalDoublesCurve from(final double[] xData, final double[] yData) {
    return new NodalDoublesCurve(xData, yData, false);
  }

  public static NodalDoublesCurve from(final Double[] xData, final Double[] yData) {
    return new NodalDoublesCurve(xData, yData, false);
  }

  public static NodalDoublesCurve from(final Map<Double, Double> data) {
    return new NodalDoublesCurve(data, false);
  }

  public static NodalDoublesCurve from(final DoublesPair[] data) {
    return new NodalDoublesCurve(data, false);
  }

  public static NodalDoublesCurve from(final Set<DoublesPair> data) {
    return new NodalDoublesCurve(data, false);
  }

  public static NodalDoublesCurve from(final List<Double> xData, final List<Double> yData) {
    return new NodalDoublesCurve(xData, yData, false);
  }

  public static NodalDoublesCurve from(final List<DoublesPair> data) {
    return new NodalDoublesCurve(data, false);
  }

  public static NodalDoublesCurve from(final double[] xData, final double[] yData, final String name) {
    return new NodalDoublesCurve(xData, yData, false, name);
  }

  public static NodalDoublesCurve from(final Double[] xData, final Double[] yData, final String name) {
    return new NodalDoublesCurve(xData, yData, false, name);
  }

  public static NodalDoublesCurve from(final Map<Double, Double> data, final String name) {
    return new NodalDoublesCurve(data, false, name);
  }

  public static NodalDoublesCurve from(final DoublesPair[] data, final String name) {
    return new NodalDoublesCurve(data, false, name);
  }

  public static NodalDoublesCurve from(final Set<DoublesPair> data, final String name) {
    return new NodalDoublesCurve(data, false, name);
  }

  public static NodalDoublesCurve from(final List<Double> xData, final List<Double> yData, final String name) {
    return new NodalDoublesCurve(xData, yData, false, name);
  }

  public static NodalDoublesCurve from(final List<DoublesPair> data, final String name) {
    return new NodalDoublesCurve(data, false, name);
  }

  public static NodalDoublesCurve fromSorted(final double[] xData, final double[] yData) {
    return new NodalDoublesCurve(xData, yData, true);
  }

  public static NodalDoublesCurve fromSorted(final Double[] xData, final Double[] yData) {
    return new NodalDoublesCurve(xData, yData, true);
  }

  public static NodalDoublesCurve fromSorted(final Map<Double, Double> data) {
    return new NodalDoublesCurve(data, true);
  }

  public static NodalDoublesCurve fromSorted(final DoublesPair[] data) {
    return new NodalDoublesCurve(data, true);
  }

  public static NodalDoublesCurve fromSorted(final Set<DoublesPair> data) {
    return new NodalDoublesCurve(data, true);
  }

  public static NodalDoublesCurve fromSorted(final List<DoublesPair> data) {
    return new NodalDoublesCurve(data, true);
  }

  public static NodalDoublesCurve fromSorted(final List<Double> xData, final List<Double> yData) {
    return new NodalDoublesCurve(xData, yData, true);
  }

  public static NodalDoublesCurve fromSorted(final double[] xData, final double[] yData, final String name) {
    return new NodalDoublesCurve(xData, yData, true, name);
  }

  public static NodalDoublesCurve fromSorted(final Double[] xData, final Double[] yData, final String name) {
    return new NodalDoublesCurve(xData, yData, true, name);
  }

  public static NodalDoublesCurve fromSorted(final Map<Double, Double> data, final String name) {
    return new NodalDoublesCurve(data, true, name);
  }

  public static NodalDoublesCurve fromSorted(final DoublesPair[] data, final String name) {
    return new NodalDoublesCurve(data, true, name);
  }

  public static NodalDoublesCurve fromSorted(final Set<DoublesPair> data, final String name) {
    return new NodalDoublesCurve(data, true, name);
  }

  public static NodalDoublesCurve fromSorted(final List<Double> xData, final List<Double> yData, final String name) {
    return new NodalDoublesCurve(xData, yData, true, name);
  }

  public static NodalDoublesCurve fromSorted(final List<DoublesPair> data, final String name) {
    return new NodalDoublesCurve(data, true, name);
  }

  public NodalDoublesCurve(final double[] xData, final double[] yData, final boolean isSorted) {
    super(xData, yData, isSorted);
  }

  public NodalDoublesCurve(final Double[] xData, final Double[] yData, final boolean isSorted) {
    super(xData, yData, isSorted);
  }

  public NodalDoublesCurve(final Map<Double, Double> data, final boolean isSorted) {
    super(data, isSorted);
  }

  public NodalDoublesCurve(final DoublesPair[] data, final boolean isSorted) {
    super(data, isSorted);
  }

  public NodalDoublesCurve(final Set<DoublesPair> data, final boolean isSorted) {
    super(data, isSorted);
  }

  public NodalDoublesCurve(final List<Double> xData, final List<Double> yData, final boolean isSorted) {
    super(xData, yData, isSorted);
  }

  public NodalDoublesCurve(final List<DoublesPair> data, final boolean isSorted) {
    super(data, isSorted);
  }

  public NodalDoublesCurve(final double[] xData, final double[] yData, final boolean isSorted, final String name) {
    super(xData, yData, isSorted, name);
  }

  public NodalDoublesCurve(final Double[] xData, final Double[] yData, final boolean isSorted, final String name) {
    super(xData, yData, isSorted, name);
  }

  public NodalDoublesCurve(final Map<Double, Double> data, final boolean isSorted, final String name) {
    super(data, isSorted, name);
  }

  public NodalDoublesCurve(final DoublesPair[] data, final boolean isSorted, final String name) {
    super(data, isSorted, name);
  }

  public NodalDoublesCurve(final Set<DoublesPair> data, final boolean isSorted, final String name) {
    super(data, isSorted, name);
  }

  public NodalDoublesCurve(final List<Double> xData, final List<Double> yData, final boolean isSorted, final String name) {
    super(xData, yData, isSorted, name);
  }

  public NodalDoublesCurve(final List<DoublesPair> data, final boolean isSorted, final String name) {
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
