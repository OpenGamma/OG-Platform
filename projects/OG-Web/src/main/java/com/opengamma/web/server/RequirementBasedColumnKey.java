/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.server;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class RequirementBasedColumnKey {

  private final String _calcConfigName;
  private final String _valueName;
  private final ValueProperties _valueProperties;
  
  public RequirementBasedColumnKey(String calcConfigName, String valueName, ValueProperties valueProperties) {
    ArgumentChecker.notNull(calcConfigName, "calcConfigName");
    ArgumentChecker.notNull(valueName, "valueName");
    ArgumentChecker.notNull(valueProperties, "valueProperties");
    _calcConfigName = calcConfigName;
    _valueName = valueName;
    _valueProperties = valueProperties;
  }

  public String getCalcConfigName() {
    return _calcConfigName;
  }

  public String getValueName() {
    return _valueName;
  }

  public ValueProperties getValueProperties() {
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
    if (!(obj instanceof RequirementBasedColumnKey)) {
      return false;
    }
    RequirementBasedColumnKey other = (RequirementBasedColumnKey) obj;
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
    return "WebViewGridColumnKey [calcConfigName=" + _calcConfigName + ", valueName=" + _valueName + ", valueProperties=" + _valueProperties + "]";
  }
  
  
  
}
