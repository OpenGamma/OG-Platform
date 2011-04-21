/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server;

/**
 * 
 */
public enum OutputType {
  /**
   * Rendered as a floating point number
   */
  SCALAR,
  
  /**
   * Rendered as a matrix of floating point numbers
   */
  MATRIX;
  
  public static OutputType getOutputType(Class<?> type) {
    if (type.equals(double[][].class)) {
      return OutputType.MATRIX;
    }
    return OutputType.SCALAR;
  }
}
