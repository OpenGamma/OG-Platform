/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;
/**
 * Contains utility methods for checking fudge message fields.
 */
public final class FudgeFieldChecker {

  /**
   * Restrictive constructor.
   */
  private FudgeFieldChecker() {
  }

  /**
   * Checks that the specified fudge field is non-null.
   * @param object  the fudge field or object to check, may be null
   * @param message the error message, not null
   * @throws IllegalArgumentException if the input is null
   */
  public static void notNull(Object object, String message) {
    if (object == null) {
      throw new IllegalArgumentException(message);
    }
  }
}
