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
 */
public final class ArgumentChecker {

  /**
   * Restrictive constructor.
   */
  private ArgumentChecker() {
  }

  //-------------------------------------------------------------------------
  /**
   * Checks that the specified boolean is true.
   * This will normally be the result of a caller-specific check.
   * @param trueIfValid  a boolean resulting from testing an argument, may be null
   * @param message  the error message, not null
   * @throws NullPointerException if the input is null
   */
  public static void isTrue(boolean trueIfValid, String message) throws NullPointerException {
    if (trueIfValid == false) {
      throw new IllegalArgumentException(message);
    }
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
   * As a convention, the name of the parameter should be the exact name that you would
   * provide in a Spring configuration file.
   * 
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

  //-------------------------------------------------------------------------
  /**
   * Checks that the argument is not negative.
   * @param parameter  the parameter to check
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is negative
   */
  public static void notNegative(int parameter, String name) throws IllegalArgumentException {
    if (parameter < 0) {
      throw new IllegalArgumentException("Input parameter '" + name + "' must not be negative");
    }
  }

  /**
   * Checks that the argument is not negative.
   * @param parameter  the parameter to check
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is negative
   */
  public static void notNegative(double parameter, String name) throws IllegalArgumentException {
    if (parameter < 0) {
      throw new IllegalArgumentException("Input parameter '" + name + "' must not be negative");
    }
  }

  /**
   * Checks that the argument is not negative or zero.
   * @param parameter  the parameter to check
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is negative or zero
   */
  public static void notNegativeOrZero(int parameter, String name) throws IllegalArgumentException {
    if (parameter <= 0) {
      throw new IllegalArgumentException("Input parameter '" + name + "' must not be negative or zero");
    }
  }

  /**
   * Checks that the argument is not negative or zero.
   * @param parameter  the parameter to check
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is negative or zero
   */
  public static void notNegativeOrZero(double parameter, String name) throws IllegalArgumentException {
    if (parameter <= 0) {
      throw new IllegalArgumentException("Input parameter '" + name + "' must not be negative or zero");
    }
  }
  
  /**
   * Checks that the argument is not equal to zero to within an accuracy eps
   * @param x The value to check
   * @param eps The accuracy
   * @param name The name to use in the error message
   * @throws IllegalArgumentException If the absolute value of the argument is less than eps
   */
  public static void notZero(double x, double eps, String name) throws IllegalArgumentException {
    if (CompareUtils.closeEquals(x, 0, eps)) {
      throw new IllegalArgumentException("Input parameter '" + name + "' must not be zero");
    }
  }

  /**
   * Checks a collection for null elements
   * @param collection The collection to test
   * @return true if the collection contains a null element
   * @throws IllegalArgumentException If the collection is null
   */
  public static boolean hasNullElement(Collection<?> collection) {    
    notNull(collection, "collection");
    for (Object o : collection) {
      if (o == null) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Checks a collection of doubles for negative elements
   * @param collection The collection to test
   * @return true if the collection contains a negative element
   * @throws IllegalArgumentException If the collection is null
   */
  
  public static boolean hasNegativeElement(Collection<Double> collection) {
    notNull(collection, "collection");
    for (Double d : collection) {
      if (d < 0) {
        return true;
      }
    }
    return false;
  }
    
  /**
   * Checks that a value is within the range low < x < high
   * @param low Low value of the range
   * @param high High value of the range
   * @param x The value
   * @return true if low < x < high
   */
  public static boolean isInRangeExclusive(double low, double high, double x) {
    if (x > low && x < high) {
      return true;
    }
    return false;
  }

  /**
   * Checks that a value is within the range low <= x <= high
   * @param low Low value of the range
   * @param high High value of the range
   * @param x The value
   * @return true if low <= x <= high
   */
  public static boolean isInRangeInclusive(double low, double high, double x) {
    if (x >= low && x <= high) {
      return true;
    }
    return false;
  }

  /**
   * Checks that a value is within the range low < x <= high
   * @param low Low value of the range
   * @param high High value of the range
   * @param x The value
   * @return true if low < x <= high
   */

  public static boolean isInRangeExcludingLow(double low, double high, double x) {
    if (x > low && x <= high) {
      return true;
    }
    return false;
  }
  
  /**
   * Checks that a value is within the range low <= x < high
   * @param low Low value of the range
   * @param high High value of the range
   * @param x The value
   * @return true if low <= x < high
   */

  public static boolean isInRangeExcludingHigh(double low, double high, double x) {
    if (x >= low && x < high) {
      return true;
    }
    return false;
  }

}
