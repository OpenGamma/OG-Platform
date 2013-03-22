/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.pnl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.greeks.Underlying;
import com.opengamma.analytics.financial.sensitivity.Sensitivity;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class SensitivityAndReturnDataBundle {
  private final Sensitivity<?> _sensitivity;
  private final double _value;
  private final Map<UnderlyingType, DoubleTimeSeries<?>> _underlyingReturnTS;
  private final List<UnderlyingType> _underlyings;

  public SensitivityAndReturnDataBundle(final Sensitivity<?> sensitivity, final double value, final Map<UnderlyingType, DoubleTimeSeries<?>> underlyingReturnTS) {
    Validate.notNull(sensitivity, "sensitivity");
    Validate.notNull(underlyingReturnTS, "underlying returns");
    Validate.notEmpty(underlyingReturnTS, "underlying returns");
    Validate.noNullElements(underlyingReturnTS.keySet(), "underlying return key set");
    Validate.noNullElements(underlyingReturnTS.values(), "underlying return values");
    _underlyings = sensitivity.getUnderlyingTypes();
    Validate.isTrue(_underlyings.size() == underlyingReturnTS.size());
    Validate.isTrue(_underlyings.containsAll(underlyingReturnTS.keySet()));
    _sensitivity = sensitivity;
    _value = value;
    _underlyingReturnTS = underlyingReturnTS;
  }

  public Sensitivity<?> getSensitivity() {
    return _sensitivity;
  }

  public double getValue() {
    return _value;
  }

  public Map<UnderlyingType, DoubleTimeSeries<?>> getUnderlyingReturnTS() {
    return _underlyingReturnTS;
  }

  public List<UnderlyingType> getUnderlyingTypes() {
    return _underlyings;
  }

  public Underlying getUnderlying() {
    return _sensitivity.getUnderlying();
  }

  public DoubleTimeSeries<?> getReturnTimeSeriesForUnderlying(final UnderlyingType type) {
    Validate.notNull(type, "underlying");
    final DoubleTimeSeries<?> result = _underlyingReturnTS.get(type);
    Validate.notNull(result, "underlying return time series for " + type);
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_sensitivity == null) ? 0 : _sensitivity.hashCode());
    result = prime * result + ((_underlyingReturnTS == null) ? 0 : _underlyingReturnTS.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_value);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    final SensitivityAndReturnDataBundle other = (SensitivityAndReturnDataBundle) obj;
    return ObjectUtils.equals(_sensitivity, other._sensitivity) && ObjectUtils.equals(_underlyingReturnTS, other._underlyingReturnTS) && ObjectUtils.equals(_value, other._value);
  }

}
