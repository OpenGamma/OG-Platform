/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

/**
 * Simple argument checker throwing {@code IllegalArgumentException}.
 */
/* package */ class ArgumentChecker {

  //-------------------------------------------------------------------------
  /**
   * Checks that the specified boolean is true.
   * This will normally be the result of a caller-specific check.
   * 
   * @param trueIfValid  a boolean resulting from testing an argument, may be null
   * @param message  the error message, not null
   * @throws IllegalArgumentException if the test value is false
   */
  public static void isTrue(boolean trueIfValid, String message) {
    if (trueIfValid == false) {
      throw new IllegalArgumentException(message);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Checks that the specified parameter is non-null.
   * 
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is null
   */
  public static void notNull(Object parameter, String name) {
    if (parameter == null) {
      throw new IllegalArgumentException("Input parameter '" + name + "' must not be null");
    }
  }

}
