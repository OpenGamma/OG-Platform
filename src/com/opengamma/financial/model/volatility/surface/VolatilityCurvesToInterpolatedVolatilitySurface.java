/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.volatility.curve.VolatilityCurve;
import com.opengamma.math.interpolation.InterpolationResult;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DDataBundle;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class VolatilityCurvesToInterpolatedVolatilitySurface extends VolatilitySurface {
  private final TreeMap<Double, VolatilityCurve> _curves;
  private final Interpolator1D<Interpolator1DDataBundle, ? extends InterpolationResult> _interpolator;
  private final double[] _xValues;
  private final int _n;

  public VolatilityCurvesToInterpolatedVolatilitySurface(final Map<Double, VolatilityCurve> curves, final Interpolator1D<Interpolator1DDataBundle, ? extends InterpolationResult> interpolator) {
    Validate.notNull(curves, "curves");
    Validate.notEmpty(curves, "curves");
    Validate.noNullElements(curves.values(), "curves");
    Validate.notNull(interpolator);
    _curves = new TreeMap<Double, VolatilityCurve>(curves);
    _interpolator = interpolator;
    _n = _curves.size();
    _xValues = new double[_n];
    int i = 0;
    for (final Map.Entry<Double, VolatilityCurve> entry : _curves.entrySet()) {
      _xValues[i++] = entry.getKey();
    }
  }

  public Map<Double, VolatilityCurve> getCurves() {
    return _curves;
  }

  @Override
  public Double getVolatility(final DoublesPair xy) {
    Validate.notNull(xy, "xy");
    final Double x = xy.first;
    final double y = xy.second;
    if (_curves.containsKey(x)) {
      return _curves.get(x).getVolatility(y);
    }
    if (x < _curves.firstKey()) {
      return _curves.get(_curves.firstKey()).getVolatility(y);
    }
    if (x > _curves.lastKey()) {
      return _curves.get(_curves.lastKey()).getVolatility(y);
    }
    final double[] yValues = new double[_n];
    int i = 0;
    for (final Map.Entry<Double, VolatilityCurve> entry : _curves.entrySet()) {
      yValues[i++] = entry.getValue().getVolatility(y);
    }
    return _interpolator.interpolate(_interpolator.getDataBundle(_xValues, yValues), x).getResult();
  }

  @Override
  public VolatilitySurface withMultipleShifts(final Map<DoublesPair, Double> shifts) {
    throw new NotImplementedException();
  }

  @Override
  public VolatilitySurface withParallelShift(final double shift) {
    throw new NotImplementedException();
  }

  @Override
  public VolatilitySurface withSingleShift(final DoublesPair xy, final double shift) {
    throw new NotImplementedException();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_curves == null) ? 0 : _curves.hashCode());
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
    final VolatilityCurvesToInterpolatedVolatilitySurface other = (VolatilityCurvesToInterpolatedVolatilitySurface) obj;
    return ObjectUtils.equals(_curves, other._curves) && ObjectUtils.equals(_interpolator, other._interpolator);
  }

}
