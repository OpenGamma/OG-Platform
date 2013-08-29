/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.curve;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.volatility.VolatilityTermStructure;
import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.curve.CurveShiftFunctionFactory;

/**
 * VolatilityTermStructure backed by a Curve<Double, Double>
 */
public class VolatilityCurve implements VolatilityTermStructure {
  private final Curve<Double, Double> _curve;

  public VolatilityCurve(final Curve<Double, Double> curve) {
    Validate.notNull(curve, "curve");
    _curve = curve;
  }

  @Override
  public Double getVolatility(final Double x) {
    return _curve.getYValue(x);
  }

  public Curve<Double, Double> getCurve() {
    return _curve;
  }

  public VolatilityCurve withParallelShift(final double shift) {
    return new VolatilityCurve(CurveShiftFunctionFactory.getShiftedCurve(_curve, shift));
  }

  public VolatilityCurve withSingleShift(final double x, final double shift) {
    return new VolatilityCurve(CurveShiftFunctionFactory.getShiftedCurve(_curve, x, shift));
  }

  public VolatilityCurve withMultipleShifts(final double[] xShifts, final double[] yShifts) {
    return new VolatilityCurve(CurveShiftFunctionFactory.getShiftedCurve(_curve, xShifts, yShifts));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _curve.hashCode();
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
    final VolatilityCurve other = (VolatilityCurve) obj;
    return ObjectUtils.equals(_curve, other._curve);
  }

}
