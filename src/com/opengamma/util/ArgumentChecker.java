/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

/**
 * Contains utility methods for checking inputs to methods.
 *
 * @author kirk
 */
public final class ArgumentChecker {

  /**
   * Restrictive constructor.
   */
  private ArgumentChecker() {
  }

  /**
   * Checks that the specified parameter is non-null.
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @throws NullPointerException
   */
  public static void notNull(Object parameter, String name) throws NullPointerException {
    if (parameter == null) {
      throw new NullPointerException("Input parameter '" + name + "' must not be null");
    }
  }

  /**
   * Checks that the specified injected parameter is non-null.
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @throws NullPointerException
   */
  public static void notNullInjected(Object parameter, String name) throws NullPointerException {
    if (parameter == null) {
      throw new NullPointerException("Injected input parameter '" + name + "' must not be null");
    }
  }

}
