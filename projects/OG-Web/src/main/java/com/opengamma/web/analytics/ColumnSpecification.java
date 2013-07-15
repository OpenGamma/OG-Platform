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
  /** Column header. */
  private final String _header;

  /* package */ ColumnSpecification(String calcConfigName, String valueName, ValueProperties valueProperties, String header) {
    ArgumentChecker.notNull(calcConfigName, "calcConfigName");
    ArgumentChecker.notNull(valueName, "valueName");
    ArgumentChecker.notNull(valueProperties, "valueProperties");
    ArgumentChecker.notEmpty(header, "header");
    _calcConfigName = calcConfigName;
    _valueName = valueName;
    _valueProperties = valueProperties;
    _header = header;
  }

  /* package */ ColumnSpecification(String calcConfigName, String valueName, ValueProperties valueProperties) {
    this(calcConfigName, valueName, valueProperties, valueName);
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

  /* package */ String getHeader() {
    return _header;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ColumnSpecification that = (ColumnSpecification) o;

    if (!_calcConfigName.equals(that._calcConfigName)) {
      return false;
    }
    if (!_header.equals(that._header)) {
      return false;
    }
    if (!_valueName.equals(that._valueName)) {
      return false;
    }
    if (!_valueProperties.equals(that._valueProperties)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = _calcConfigName.hashCode();
    result = 31 * result + _valueName.hashCode();
    result = 31 * result + _valueProperties.hashCode();
    result = 31 * result + _header.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "ColumnSpecification [" +
        "_calcConfigName='" + _calcConfigName + "'" +
        ", _valueName='" + _valueName + "'" +
        ", _valueProperties=" + _valueProperties +
        ", _header='" + _header + "'" +
        "]";
  }
}
