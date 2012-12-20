/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.util.ArgumentChecker;

/**
 * Key for analytics columns based on the calculation configuration, value name and properties.
 */
public class ColumnKey {

  private final String _calcConfigName;
  private final String _valueName;
  private final ValueProperties _valueProperties;

  /* package */ ColumnKey(String calcConfigName, String valueName, ValueProperties valueProperties) {
    ArgumentChecker.notNull(calcConfigName, "calcConfigName");
    ArgumentChecker.notNull(valueName, "valueName");
    ArgumentChecker.notNull(valueProperties, "valueProperties");
    _calcConfigName = calcConfigName;
    _valueName = valueName;
    _valueProperties = valueProperties;
  }

  /* package */ String getCalcConfigName() {
    return _calcConfigName;
  }

  /* package */ String getValueName() {
    return _valueName;
  }

  /* package */ ValueProperties getValueProperties() {
    return _valueProperties;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _calcConfigName.hashCode();
    result = prime * result + _valueName.hashCode();
    result = prime * result + _valueProperties.hashCode();
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
    if (!(obj instanceof ColumnKey)) {
      return false;
    }
    ColumnKey other = (ColumnKey) obj;
    if (!_calcConfigName.equals(other._calcConfigName)) {
      return false;
    }
    if (!_valueName.equals(other._valueName)) {
      return false;
    }
    if (!_valueProperties.equals(other._valueProperties)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "ColumnKey [calcConfigName=" + _calcConfigName + ", valueName=" + _valueName + ", valueProperties=" + _valueProperties + "]";
  }
}
