/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface.fitting;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

/**
 * 
 */
public class SurfaceFittedSmileDataPoints {
  private final Map<Double, List<Double>> _data;
  
  public SurfaceFittedSmileDataPoints(final Map<Double, List<Double>> data) {
    Validate.notNull(data, "data");
    _data = data;
  }
  
  public Map<Double, List<Double>> getData() {
    return _data;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _data.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final SurfaceFittedSmileDataPoints other = (SurfaceFittedSmileDataPoints) obj;
    return ObjectUtils.equals(_data, other._data);
  }
  
  
}
