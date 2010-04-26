/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.util.Collection;
import java.util.Map;

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

  //-------------------------------------------------------------------------
  /**
   * Checks that the specified parameter is non-null.
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @throws NullPointerException if the input is null
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
   * @throws NullPointerException if the input is null
   */
  public static void notNullInjected(Object parameter, String name) throws NullPointerException {
    if (parameter == null) {
      throw new NullPointerException("Injected input parameter '" + name + "' must not be null");
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Checks that the specified parameter is non-null and not empty.
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @throws NullPointerException if the input is null
   * @throws IllegalArgumentException if the input is empty
   */
  public static void notEmpty(String parameter, String name) throws IllegalArgumentException {
    notNull(parameter, name);
    if (parameter.length() == 0) {
      throw new IllegalArgumentException("Input parameter '" + name + "' must not be zero length");
    }
  }

  /**
   * Checks that the specified parameter array is non-null and not empty.
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @throws NullPointerException if the input is null
   * @throws IllegalArgumentException if the input is empty
   */
  public static void notEmpty(Object[] parameter, String name) throws IllegalArgumentException {
    notNull(parameter, name);
    if (parameter.length == 0) {
      throw new IllegalArgumentException("Input parameter array '" + name + "' must not be zero length");
    }
  }

  /**
   * Checks that the specified parameter collection is non-null and not empty.
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @throws NullPointerException if the input is null
   * @throws IllegalArgumentException if the input is empty
   */
  public static void notEmpty(Collection<?> parameter, String name) throws IllegalArgumentException {
    notNull(parameter, name);
    if (parameter.size() == 0) {
      throw new IllegalArgumentException("Input parameter collection '" + name + "' must not be zero length");
    }
  }

  /**
   * Checks that the specified parameter map is non-null and not empty.
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @throws NullPointerException if the input is null
   * @throws IllegalArgumentException if the input is empty
   */
  public static void notEmpty(Map<?, ?> parameter, String name) throws IllegalArgumentException {
    notNull(parameter, name);
    if (parameter.size() == 0) {
      throw new IllegalArgumentException("Input parameter map '" + name + "' must not be zero length");
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Checks that the specified parameter array is non-null and contains no nulls.
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @throws NullPointerException if the input is null or contains nulls
   */
  public static void noNulls(Object[] parameter, String name) throws IllegalArgumentException {
    notNull(parameter, name);
    for (int i = 0; i < parameter.length; i++) {
      if (parameter[i] == null) {
        throw new NullPointerException("Input parameter array '" + name + "' must not contain null at index " + i);
      }
    }
  }

  /**
   * Checks that the specified parameter collection is non-null and contains no nulls.
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @throws NullPointerException if the input is null or contains nulls
   */
  public static void noNulls(Collection<?> parameter, String name) throws IllegalArgumentException {
    notNull(parameter, name);
    for (Object obj : parameter) {
      if (obj == null) {
        throw new NullPointerException("Input parameter collection '" + name + "' must not contain null");
      }
    }
  }

}
