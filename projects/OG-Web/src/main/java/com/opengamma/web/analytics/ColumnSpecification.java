/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.util.ArgumentChecker;

/**
 * Contains the details of the calculated analytics data displayed in a column in the analytics grid.
 */
public class ColumnSpecification {

  /** Name of the calculation configuration that produces the column data. */
  private final String _calcConfigName;
  /** Value name of the column's data. */
  private final String _valueName;
  /** Value properties used when calculating the column's data. */
  private final ValueProperties _valueProperties;

  /* package */ ColumnSpecification(String calcConfigName, String valueName, ValueProperties valueProperties) {
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
    if (!(obj instanceof ColumnSpecification)) {
      return false;
    }
    ColumnSpecification other = (ColumnSpecification) obj;
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
