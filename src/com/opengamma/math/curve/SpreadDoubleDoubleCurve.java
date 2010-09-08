/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function;

/**
 * 
 */
public class SpreadDoubleDoubleCurve extends Curve<Double, Double> {
  private final DoubleDoubleCurveSpreadFunction _spreadFunction;
  private final Function<Double, Double> _f;
  private final Curve<Double, Double>[] _curves;

  public SpreadDoubleDoubleCurve(Curve<Double, Double>[] curves, DoubleDoubleCurveSpreadFunction spreadFunction) {
    super();
    Validate.notNull(curves, "curves");
    Validate.isTrue(curves.length > 1, "curves");
    Validate.notNull(spreadFunction, "spread operator");
    _curves = curves;
    _spreadFunction = spreadFunction;
    _f = spreadFunction.evaluate(curves);
  }

  public SpreadDoubleDoubleCurve(Curve<Double, Double>[] curves, DoubleDoubleCurveSpreadFunction spreadFunction, String name) {
    super(name);
    Validate.notNull(curves, "curves");
    Validate.isTrue(curves.length > 1, "curves");
    Validate.notNull(spreadFunction, "spread operator");
    _curves = curves;
    _spreadFunction = spreadFunction;
    _f = spreadFunction.evaluate(curves);
  }

  public List<String> getUnderlyingNames() {
    List<String> result = new ArrayList<String>();
    for (Curve<Double, Double> curve : _curves) {
      if (curve instanceof SpreadDoubleDoubleCurve) {
        result.addAll(((SpreadDoubleDoubleCurve) curve).getUnderlyingNames());
      } else {
        result.add(curve.getName());
      }
    }
    return result;
  }

  public String getLongName() {
    StringBuffer sb = new StringBuffer(getName());
    sb.append("=");
    int i = 0;
    for (Curve<Double, Double> curve : _curves) {
      sb.append("(");
      if (curve instanceof SpreadDoubleDoubleCurve) {
        sb.append(((SpreadDoubleDoubleCurve) curve).getLongName());
      } else {
        sb.append(curve.getName());
      }
      if (i != _curves.length - 1) {
        sb.append(_spreadFunction.getOperationName());
      }
      sb.append(")");
      i++;
    }
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
  public Double getYValue(Double x) {
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
    SpreadDoubleDoubleCurve other = (SpreadDoubleDoubleCurve) obj;
    if (!Arrays.equals(_curves, other._curves)) {
      return false;
    }
    return ObjectUtils.equals(_spreadFunction, other._spreadFunction);
  }

}
