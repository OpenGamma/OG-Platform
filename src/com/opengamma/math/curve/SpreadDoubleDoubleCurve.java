/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function;

/**
 * 
 */
public class SpreadDoubleDoubleCurve extends Curve<Double, Double> {
  private final CurveSpreadFunction _spreadFunction;
  private final Function<Double, Double> _f;
  private final Curve<Double, Double>[] _curves;

  public static SpreadDoubleDoubleCurve from(final Curve<Double, Double>[] curves, final CurveSpreadFunction spreadFunction) {
    return new SpreadDoubleDoubleCurve(curves, spreadFunction);
  }

  public static SpreadDoubleDoubleCurve from(final Curve<Double, Double>[] curves, final CurveSpreadFunction spreadFunction, final String name) {
    return new SpreadDoubleDoubleCurve(curves, spreadFunction, name);
  }

  public SpreadDoubleDoubleCurve(final Curve<Double, Double>[] curves, final CurveSpreadFunction spreadFunction) {
    super();
    Validate.notNull(curves, "curves");
    Validate.isTrue(curves.length > 1, "curves");
    Validate.notNull(spreadFunction, "spread operator");
    _curves = curves;
    _spreadFunction = spreadFunction;
    _f = spreadFunction.evaluate(curves);
  }

  public SpreadDoubleDoubleCurve(final Curve<Double, Double>[] curves, final CurveSpreadFunction spreadFunction, final String name) {
    super(name);
    Validate.notNull(curves, "curves");
    Validate.isTrue(curves.length > 1, "curves");
    Validate.notNull(spreadFunction, "spread operator");
    _curves = curves;
    _spreadFunction = spreadFunction;
    _f = spreadFunction.evaluate(curves);
  }

  public Set<String> getUnderlyingNames() {
    final Set<String> result = new HashSet<String>();
    for (final Curve<Double, Double> curve : _curves) {
      if (curve instanceof SpreadDoubleDoubleCurve) {
        result.addAll(((SpreadDoubleDoubleCurve) curve).getUnderlyingNames());
      } else {
        result.add(curve.getName());
      }
    }
    return result;
  }

  public String getLongName() {
    final StringBuffer sb = new StringBuffer(getName());
    sb.append("=");
    int i = 0;
    sb.append("(");
    for (final Curve<Double, Double> curve : _curves) {
      if (curve instanceof SpreadDoubleDoubleCurve) {
        sb.append(((SpreadDoubleDoubleCurve) curve).getLongName().substring(2));
      } else {
        sb.append(curve.getName());
      }
      if (i != _curves.length - 1) {
        sb.append(_spreadFunction.getOperationName());
      }
      i++;
    }
    sb.append(")");
    return sb.toString();
  }

  public Curve<Double, Double>[] getUnderlyingCurves() {
    return _curves;
  }

  @Override
  public Double[] getXData() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Double[] getYData() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Double getYValue(final Double x) {
    Validate.notNull(x, "x");
    return _f.evaluate(x);
  }

  @Override
  public int size() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_curves);
    result = prime * result + ((_spreadFunction == null) ? 0 : _spreadFunction.hashCode());
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
    final SpreadDoubleDoubleCurve other = (SpreadDoubleDoubleCurve) obj;
    if (!Arrays.equals(_curves, other._curves)) {
      return false;
    }
    return ObjectUtils.equals(_spreadFunction, other._spreadFunction);
  }

}
