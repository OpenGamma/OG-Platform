/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.summary;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

/**
 * Encapsulates summary details about a security.
 */
public class Summary {

  private final Map<SummaryField, Object> _fieldMap;
  private final String _error;
  
  public Summary(Map<SummaryField, Object> fieldMap) {
    _fieldMap = ImmutableMap.copyOf(fieldMap);
    _error = null;
  }
  
  public Summary(String error) {
    _fieldMap = null;
    _error = error;
  }
  
  public Object getFieldValue(SummaryField summaryField) {
    if (_fieldMap == null) {
      return null;
    }
    return _fieldMap.get(summaryField);
  }
  
  public String getError() {
    return _error;
  }
  
}
