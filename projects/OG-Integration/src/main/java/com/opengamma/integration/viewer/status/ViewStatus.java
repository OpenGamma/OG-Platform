/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer.status;

/**
 * Status of a specific view calculation result.
 */
public enum ViewStatus {
  /**
   * Value
   */
  VALUE("V"),
  /**
   * No Value
   */
  NO_VALUE("N"),
  /**
   * Graph Fail
   */
  GRAPH_FAIL("F");
  
  private final String _value;
  
  private ViewStatus(String value) {
    _value = value;
  }
  
  public String getValue() {
    return _value;
  }
  
  public String toString() {
    return getValue();
  }
}
