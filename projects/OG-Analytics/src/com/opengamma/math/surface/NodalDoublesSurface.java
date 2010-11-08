/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.surface;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/**
 * 
 */
public class NodalDoublesSurface extends DoublesSurface {

  public static NodalDoublesSurface from(final double[] xData, final double[] yData, final double[] zData) {
    return new NodalDoublesSurface(xData, yData, zData);
  }

  public static NodalDoublesSurface from(final Double[] xData, final Double[] yData, final Double[] zData) {
    return new NodalDoublesSurface(xData, yData, zData);
  }

  public static NodalDoublesSurface from(final List<Double> xData, final List<Double> yData, final List<Double> zData) {
    return new NodalDoublesSurface(xData, yData, zData);
  }

  public static NodalDoublesSurface from(final DoublesPair[] xyData, final Double[] zData) {
    return new NodalDoublesSurface(xyData, zData);
  }

  public static NodalDoublesSurface from(final DoublesPair[] xyData, final double[] zData) {
    return new NodalDoublesSurface(xyData, zData);
  }

  public static NodalDoublesSurface from(final List<DoublesPair> xyData, final List<Double> zData) {
    return new NodalDoublesSurface(xyData, zData);
  }

  public static NodalDoublesSurface from(final Map<DoublesPair, Double> data) {
    return new NodalDoublesSurface(data);
  }

  public static NodalDoublesSurface from(final List<Triple<Double, Double, Double>> xyzData) {
    return new NodalDoublesSurface(xyzData);
  }

  public static NodalDoublesSurface from(final double[] xData, final double[] yData, final double[] zData, final String name) {
    return new NodalDoublesSurface(xData, yData, zData, name);
  }

  public static NodalDoublesSurface from(final Double[] xData, final Double[] yData, final Double[] zData, final String name) {
    return new NodalDoublesSurface(xData, yData, zData, name);
  }

  public static NodalDoublesSurface from(final List<Double> xData, final List<Double> yData, final List<Double> zData, final String name) {
    return new NodalDoublesSurface(xData, yData, zData, name);
  }

  public static NodalDoublesSurface from(final DoublesPair[] xyData, final double[] zData, final String name) {
    return new NodalDoublesSurface(xyData, zData, name);
  }

  public static NodalDoublesSurface from(final DoublesPair[] xyData, final Double[] zData, final String name) {
    return new NodalDoublesSurface(xyData, zData, name);
  }

  public static NodalDoublesSurface from(final List<DoublesPair> xyData, final List<Double> zData, final String name) {
    return new NodalDoublesSurface(xyData, zData, name);
  }

  public static NodalDoublesSurface from(final Map<DoublesPair, Double> xyzData, final String name) {
    return new NodalDoublesSurface(xyzData, name);
  }

  public static NodalDoublesSurface from(final List<Triple<Double, Double, Double>> xyzData, final String name) {
    return new NodalDoublesSurface(xyzData, name);
  }

  public NodalDoublesSurface(final double[] xData, final double[] yData, final double[] zData) {
    super(xData, yData, zData);
  }

  public NodalDoublesSurface(final Double[] xData, final Double[] yData, final Double[] zData) {
    super(xData, yData, zData);
  }

  public NodalDoublesSurface(final List<Double> xData, final List<Double> yData, final List<Double> zData) {
    super(xData, yData, zData);
  }

  public NodalDoublesSurface(final DoublesPair[] xyData, final double[] zData) {
    super(xyData, zData);
  }

  public NodalDoublesSurface(final DoublesPair[] xyData, final Double[] zData) {
    super(xyData, zData);
  }

  public NodalDoublesSurface(final List<DoublesPair> xyData, final List<Double> zData) {
    super(xyData, zData);
  }

  public NodalDoublesSurface(final Map<DoublesPair, Double> xyzData) {
    super(xyzData);
  }

  public NodalDoublesSurface(final List<Triple<Double, Double, Double>> xyzData) {
    super(xyzData);
  }

  public NodalDoublesSurface(final double[] xData, final double[] yData, final double[] zData, final String name) {
    super(xData, yData, zData, name);
  }

  public NodalDoublesSurface(final Double[] xData, final Double[] yData, final Double[] zData, final String name) {
    super(xData, yData, zData, name);
  }

  public NodalDoublesSurface(final List<Double> xData, final List<Double> yData, final List<Double> zData, final String name) {
    super(xData, yData, zData, name);
  }

  public NodalDoublesSurface(final DoublesPair[] xyData, final double[] zData, final String name) {
    super(xyData, zData, name);
  }

  public NodalDoublesSurface(final DoublesPair[] xyData, final Double[] zData, final String name) {
    super(xyData, zData, name);
  }

  public NodalDoublesSurface(final List<DoublesPair> xyData, final List<Double> zData, final String name) {
    super(xyData, zData, name);
  }

  public NodalDoublesSurface(final Map<DoublesPair, Double> xyzData, final String name) {
    super(xyzData, name);
  }

  public NodalDoublesSurface(final List<Triple<Double, Double, Double>> xyzData, final String name) {
    super(xyzData, name);
  }

  @Override
  public Double getZValue(final Double x, final Double y) {
    Validate.notNull(x, "x");
    Validate.notNull(y, "y");
    final double[] xArray = getXDataAsPrimitive();
    final double[] yArray = getYDataAsPrimitive();
    final int n = size();
    for (int i = 0; i < n; i++) {
      if (Double.doubleToLongBits(xArray[i]) == Double.doubleToLongBits(x)) {
        if (Double.doubleToLongBits(yArray[i]) == Double.doubleToLongBits(y)) {
          return getZDataAsPrimitive()[i];
        }
      }
    }
    throw new IllegalArgumentException("No x-y data in surface for (" + x + ", " + y + ")");
  }

  @Override
  public Double getZValue(final Pair<Double, Double> xy) {
    Validate.notNull(xy, "x-y pair");
    return getZValue(xy.getFirst(), xy.getSecond());
  }

}
