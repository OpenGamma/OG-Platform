/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.surface;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.math.interpolation.Interpolator2D;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/**
 * 
 */
public class InterpolatedDoublesSurface extends DoublesSurface {

  public static InterpolatedDoublesSurface from(final double[] xData, final double[] yData, final double[] zData, final Interpolator2D interpolator) {
    return new InterpolatedDoublesSurface(xData, yData, zData, interpolator);
  }

  public static InterpolatedDoublesSurface from(final Double[] xData, final Double[] yData, final Double[] zData, final Interpolator2D interpolator) {
    return new InterpolatedDoublesSurface(xData, yData, zData, interpolator);
  }

  public static InterpolatedDoublesSurface from(final List<Double> xData, final List<Double> yData, final List<Double> zData, final Interpolator2D interpolator) {
    return new InterpolatedDoublesSurface(xData, yData, zData, interpolator);
  }

  public static InterpolatedDoublesSurface from(final DoublesPair[] xyData, final Double[] zData, final Interpolator2D interpolator) {
    return new InterpolatedDoublesSurface(xyData, zData, interpolator);
  }

  public static InterpolatedDoublesSurface from(final DoublesPair[] xyData, final double[] zData, final Interpolator2D interpolator) {
    return new InterpolatedDoublesSurface(xyData, zData, interpolator);
  }

  public static InterpolatedDoublesSurface from(final List<DoublesPair> xyData, final List<Double> zData, final Interpolator2D interpolator) {
    return new InterpolatedDoublesSurface(xyData, zData, interpolator);
  }

  public static InterpolatedDoublesSurface from(final Map<DoublesPair, Double> xyzData, final Interpolator2D interpolator) {
    return new InterpolatedDoublesSurface(xyzData, interpolator);
  }

  public static InterpolatedDoublesSurface from(final List<Triple<Double, Double, Double>> xyzData, final Interpolator2D interpolator) {
    return new InterpolatedDoublesSurface(xyzData, interpolator);
  }

  public static InterpolatedDoublesSurface from(final double[] xData, final double[] yData, final double[] zData, final Interpolator2D interpolator, final String name) {
    return new InterpolatedDoublesSurface(xData, yData, zData, interpolator, name);
  }

  public static InterpolatedDoublesSurface from(final Double[] xData, final Double[] yData, final Double[] zData, final Interpolator2D interpolator, final String name) {
    return new InterpolatedDoublesSurface(xData, yData, zData, interpolator, name);
  }

  public static InterpolatedDoublesSurface from(final List<Double> xData, final List<Double> yData, final List<Double> zData, final Interpolator2D interpolator, final String name) {
    return new InterpolatedDoublesSurface(xData, yData, zData, interpolator, name);
  }

  public static InterpolatedDoublesSurface from(final DoublesPair[] xyData, final Double[] zData, final Interpolator2D interpolator, final String name) {
    return new InterpolatedDoublesSurface(xyData, zData, interpolator, name);
  }

  public static InterpolatedDoublesSurface from(final DoublesPair[] xyData, final double[] zData, final Interpolator2D interpolator, final String name) {
    return new InterpolatedDoublesSurface(xyData, zData, interpolator, name);
  }

  public static InterpolatedDoublesSurface from(final List<DoublesPair> xyData, final List<Double> zData, final Interpolator2D interpolator, final String name) {
    return new InterpolatedDoublesSurface(xyData, zData, interpolator, name);
  }

  public static InterpolatedDoublesSurface from(final Map<DoublesPair, Double> xyzData, final Interpolator2D interpolator, final String name) {
    return new InterpolatedDoublesSurface(xyzData, interpolator, name);
  }

  public static InterpolatedDoublesSurface from(final List<Triple<Double, Double, Double>> xyzData, final Interpolator2D interpolator, final String name) {
    return new InterpolatedDoublesSurface(xyzData, interpolator, name);
  }

  private final Interpolator2D _interpolator;
  private Map<DoublesPair, Double> _data;

  public InterpolatedDoublesSurface(final double[] xData, final double[] yData, final double[] zData, final Interpolator2D interpolator) {
    super(xData, yData, zData);
    Validate.notNull(interpolator, "interpolator");
    _interpolator = interpolator;
    init();
  }

  public InterpolatedDoublesSurface(final Double[] xData, final Double[] yData, final Double[] zData, final Interpolator2D interpolator) {
    super(xData, yData, zData);
    Validate.notNull(interpolator, "interpolator");
    _interpolator = interpolator;
    init();
  }

  public InterpolatedDoublesSurface(final List<Double> xData, final List<Double> yData, final List<Double> zData, final Interpolator2D interpolator) {
    super(xData, yData, zData);
    Validate.notNull(interpolator, "interpolator");
    _interpolator = interpolator;
    init();
  }

  public InterpolatedDoublesSurface(final DoublesPair[] xyData, final double[] zData, final Interpolator2D interpolator) {
    super(xyData, zData);
    Validate.notNull(interpolator, "interpolator");
    _interpolator = interpolator;
    init();
  }

  public InterpolatedDoublesSurface(final DoublesPair[] xyData, final Double[] zData, final Interpolator2D interpolator) {
    super(xyData, zData);
    Validate.notNull(interpolator, "interpolator");
    _interpolator = interpolator;
    init();
  }

  public InterpolatedDoublesSurface(final List<DoublesPair> xyData, final List<Double> zData, final Interpolator2D interpolator) {
    super(xyData, zData);
    Validate.notNull(interpolator, "interpolator");
    _interpolator = interpolator;
    init();
  }

  public InterpolatedDoublesSurface(final Map<DoublesPair, Double> xyzData, final Interpolator2D interpolator) {
    super(xyzData);
    Validate.notNull(interpolator, "interpolator");
    _interpolator = interpolator;
    init();
  }

  public InterpolatedDoublesSurface(final List<Triple<Double, Double, Double>> xyzData, final Interpolator2D interpolator) {
    super(xyzData);
    Validate.notNull(interpolator, "interpolator");
    _interpolator = interpolator;
    init();
  }

  public InterpolatedDoublesSurface(final double[] xData, final double[] yData, final double[] zData, final Interpolator2D interpolator, final String name) {
    super(xData, yData, zData, name);
    Validate.notNull(interpolator, "interpolator");
    _interpolator = interpolator;
    init();
  }

  public InterpolatedDoublesSurface(final Double[] xData, final Double[] yData, final Double[] zData, final Interpolator2D interpolator, final String name) {
    super(xData, yData, zData, name);
    Validate.notNull(interpolator, "interpolator");
    _interpolator = interpolator;
    init();
  }

  public InterpolatedDoublesSurface(final List<Double> xData, final List<Double> yData, final List<Double> zData, final Interpolator2D interpolator, final String name) {
    super(xData, yData, zData, name);
    Validate.notNull(interpolator, "interpolator");
    _interpolator = interpolator;
    init();
  }

  public InterpolatedDoublesSurface(final DoublesPair[] xyData, final double[] zData, final Interpolator2D interpolator, final String name) {
    super(xyData, zData, name);
    Validate.notNull(interpolator, "interpolator");
    _interpolator = interpolator;
    init();
  }

  public InterpolatedDoublesSurface(final DoublesPair[] xyData, final Double[] zData, final Interpolator2D interpolator, final String name) {
    super(xyData, zData, name);
    Validate.notNull(interpolator, "interpolator");
    _interpolator = interpolator;
    init();
  }

  public InterpolatedDoublesSurface(final List<DoublesPair> xyData, final List<Double> zData, final Interpolator2D interpolator, final String name) {
    super(xyData, zData, name);
    Validate.notNull(interpolator, "interpolator");
    _interpolator = interpolator;
    init();
  }

  public InterpolatedDoublesSurface(final Map<DoublesPair, Double> xyzData, final Interpolator2D interpolator, final String name) {
    super(xyzData, name);
    Validate.notNull(interpolator, "interpolator");
    _interpolator = interpolator;
    init();
  }

  public InterpolatedDoublesSurface(final List<Triple<Double, Double, Double>> xyzData, final Interpolator2D interpolator, final String name) {
    super(xyzData, name);
    Validate.notNull(interpolator, "interpolator");
    _interpolator = interpolator;
    init();
  }

  // TODO this logic should be in the interpolator
  private void init() {
    _data = new TreeMap<DoublesPair, Double>();
    final double[] x = getXDataAsPrimitive();
    final double[] y = getYDataAsPrimitive();
    final double[] z = getZDataAsPrimitive();
    for (int i = 0; i < size(); i++) {
      _data.put(DoublesPair.of(x[i], y[i]), z[i]);
    }
  }

  @Override
  public Double getZValue(final Double x, final Double y) {
    Validate.notNull(x, "x");
    Validate.notNull(y, "y");
    return _interpolator.interpolate(_data, new DoublesPair(x, y));
  }

  @Override
  public Double getZValue(final Pair<Double, Double> xy) {
    Validate.notNull(xy, "xy");
    return _interpolator.interpolate(_data, DoublesPair.of(xy));
  }

  public Interpolator2D getInterpolator() {
    return _interpolator;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _interpolator.hashCode();
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
    final InterpolatedDoublesSurface other = (InterpolatedDoublesSurface) obj;
    return ObjectUtils.equals(_interpolator, other._interpolator);
  }

}
