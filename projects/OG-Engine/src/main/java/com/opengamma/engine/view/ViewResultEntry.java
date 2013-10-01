/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ViewResultEntry {
  
  private final String _calculationConfiguration;
  private final ComputedValueResult _computedValue;
  
  public ViewResultEntry(String calculationConfiguration,
      ComputedValueResult computedValue) {
    ArgumentChecker.notNull(calculationConfiguration, "calculationConfiguration");
    ArgumentChecker.notNull(computedValue, "computedValue");
    _calculationConfiguration = calculationConfiguration;
    _computedValue = computedValue;
  }
  
  public String getCalculationConfiguration() {
    return _calculationConfiguration;
  }
  
  public ComputedValueResult getComputedValue() {
    return _computedValue;
  }

  @Override
  public String toString() {
    return "ViewResultEntry [_calculationConfiguration=" + _calculationConfiguration + ", _computedValue=" + _computedValue + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_calculationConfiguration == null) ? 0 : _calculationConfiguration.hashCode());
    result = prime * result + ((_computedValue == null) ? 0 : _computedValue.hashCode());
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
    ViewResultEntry other = (ViewResultEntry) obj;
    if (_calculationConfiguration == null) {
      if (other._calculationConfiguration != null) {
        return false;
      }
    } else if (!_calculationConfiguration.equals(other._calculationConfiguration)) {
      return false;
    }
    if (_computedValue == null) {
      if (other._computedValue != null) {
        return false;
      }
    } else if (!_computedValue.equals(other._computedValue)) {
      return false;
    }
    return true;
  }
  
}
