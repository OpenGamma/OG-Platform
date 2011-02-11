/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.cube;

import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.math.interpolation.InterpolatorND;
import com.opengamma.math.interpolation.data.InterpolatorNDDataBundle;
import com.opengamma.util.tuple.Triple;

/**
 * 
 */
public class InterpolatedDoublesCube extends DoublesCube {

  public static InterpolatedDoublesCube from(double[] xData, double[] yData, double[] zData, double[] values, InterpolatorND<? extends InterpolatorNDDataBundle> interpolator) {
    return new InterpolatedDoublesCube(xData, yData, zData, values, interpolator);
  }

  public static InterpolatedDoublesCube from(Double[] xData, Double[] yData, Double[] zData, Double[] values, InterpolatorND<? extends InterpolatorNDDataBundle> interpolator) {
    return new InterpolatedDoublesCube(xData, yData, zData, values, interpolator);
  }

  public static InterpolatedDoublesCube from(List<Double> xData, List<Double> yData, List<Double> zData, List<Double> values, InterpolatorND<? extends InterpolatorNDDataBundle> interpolator) {
    return new InterpolatedDoublesCube(xData, yData, zData, values, interpolator);
  }

  public static InterpolatedDoublesCube from(double[] xData, double[] yData, double[] zData, double[] values, InterpolatorND<? extends InterpolatorNDDataBundle> interpolator, String name) {
    return new InterpolatedDoublesCube(xData, yData, zData, values, interpolator, name);
  }

  public static InterpolatedDoublesCube from(Double[] xData, Double[] yData, Double[] zData, Double[] values, InterpolatorND<? extends InterpolatorNDDataBundle> interpolator, String name) {
    return new InterpolatedDoublesCube(xData, yData, zData, values, interpolator, name);
  }

  public static InterpolatedDoublesCube from(List<Double> xData, List<Double> yData, List<Double> zData, List<Double> values, InterpolatorND<? extends InterpolatorNDDataBundle> interpolator,
      String name) {
    return new InterpolatedDoublesCube(xData, yData, zData, values, interpolator, name);
  }

  @SuppressWarnings("rawtypes")
  private final InterpolatorND _interpolator;
  private InterpolatorNDDataBundle _dataBundle;

  public InterpolatedDoublesCube(double[] xData, double[] yData, double[] zData, double[] values, InterpolatorND<? extends InterpolatorNDDataBundle> interpolator) {
    super(xData, yData, zData, values);
    Validate.notNull(interpolator, "interpolator");
    _interpolator = interpolator;
    init();
  }

  public InterpolatedDoublesCube(Double[] xData, Double[] yData, Double[] zData, Double[] values, InterpolatorND<? extends InterpolatorNDDataBundle> interpolator) {
    super(xData, yData, zData, values);
    Validate.notNull(interpolator, "interpolator");
    _interpolator = interpolator;
    init();
  }

  public InterpolatedDoublesCube(List<Double> xData, List<Double> yData, List<Double> zData, List<Double> values, InterpolatorND<? extends InterpolatorNDDataBundle> interpolator) {
    super(xData, yData, zData, values);
    Validate.notNull(interpolator, "interpolator");
    _interpolator = interpolator;
    init();
  }

  public InterpolatedDoublesCube(double[] xData, double[] yData, double[] zData, double[] values, InterpolatorND<? extends InterpolatorNDDataBundle> interpolator, String name) {
    super(xData, yData, zData, values, name);
    Validate.notNull(interpolator, "interpolator");
    _interpolator = interpolator;
    init();
  }

  public InterpolatedDoublesCube(Double[] xData, Double[] yData, Double[] zData, Double[] values, InterpolatorND<? extends InterpolatorNDDataBundle> interpolator, String name) {
    super(xData, yData, zData, values, name);
    Validate.notNull(interpolator, "interpolator");
    _interpolator = interpolator;
    init();
  }

  public InterpolatedDoublesCube(List<Double> xData, List<Double> yData, List<Double> zData, List<Double> values, InterpolatorND<? extends InterpolatorNDDataBundle> interpolator, String name) {
    super(xData, yData, zData, values, name);
    Validate.notNull(interpolator, "interpolator");
    _interpolator = interpolator;
    init();
  }

  private void init() {
    _dataBundle = _interpolator.getDataBundle(getXDataAsPrimitive(), getYDataAsPrimitive(), getZDataAsPrimitive(), getValuesAsPrimitive());
  }

  @SuppressWarnings("unchecked")
  @Override
  public Double getValue(Double x, Double y, Double z) {
    Validate.notNull(x, "x");
    Validate.notNull(y, "y");
    Validate.notNull(z, "z");
    return _interpolator.interpolate(_dataBundle, new double[] {x, y, z});
  }

  @SuppressWarnings("unchecked")
  @Override
  public Double getValue(Triple<Double, Double, Double> xyz) {
    Validate.notNull(xyz, "xyz");
    Double x = xyz.getFirst();
    Validate.notNull(x, "x");
    Double y = xyz.getSecond();
    Validate.notNull(y, "y");
    Double z = xyz.getThird();
    Validate.notNull(z, "z");
    return _interpolator.interpolate(_dataBundle, new double[] {x, y, z});
  }

  @SuppressWarnings("unchecked")
  public InterpolatorND<? extends InterpolatorNDDataBundle> getInterpolator() {
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
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    InterpolatedDoublesCube other = (InterpolatedDoublesCube) obj;
    return ObjectUtils.equals(_interpolator, other._interpolator);
  }

}
