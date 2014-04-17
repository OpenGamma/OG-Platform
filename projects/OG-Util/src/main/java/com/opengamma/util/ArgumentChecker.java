/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.helpers.MessageFormatter;

/**
 * Contains utility methods for checking inputs to methods.
 */
public final class ArgumentChecker {

  /**
   * Restricted constructor.
   */
  private ArgumentChecker() {
  }

  //-------------------------------------------------------------------------
  /**
   * Checks that the specified boolean is true.
   * This will normally be the result of a caller-specific check.
   * 
   * @param trueIfValid  a boolean resulting from testing an argument, may be null
   * @param message  the error message, not null
   * @throws IllegalArgumentException if the test value is false
   * @return true
   */
  public static boolean isTrue(boolean trueIfValid, String message) {
    if (!trueIfValid) {
      throw new IllegalArgumentException(message);
    }
    return true;
  }
  
  /**
   * Checks that the specified boolean is true.
   * This will normally be the result of a caller-specific check.
   * 
   * @param trueIfValid  a boolean resulting from testing an argument, may be null
   * @param message  the error message, not null
   * @param arg  the message arguments
   * @throws IllegalArgumentException if the test value is false
   * @return true
   */
  public static boolean isTrue(boolean trueIfValid, String message, Object... arg) {
    if (!trueIfValid) {
      throw new IllegalArgumentException(MessageFormatter.arrayFormat(message, arg).getMessage());
    }
    return true;
  }  

  /**
   * Checks that the specified boolean is false.
   * This will normally be the result of a caller-specific check.
   * 
   * @param falseIfValid  a boolean resulting from testing an argument, may be null
   * @param message  the error message, not null
   * @throws IllegalArgumentException if the test value is false
   * @return false
   */
  public static boolean isFalse(boolean falseIfValid, String message) {
    if (falseIfValid) {
      throw new IllegalArgumentException(message);
    }
    return false;
  }

  /**
   * Checks that the specified boolean is false.
   * This will normally be the result of a caller-specific check.
   * 
   * @param falseIfValid  a boolean resulting from testing an argument, may be null
   * @param message  the error message, not null
   * @param arg  the message arguments
   * @throws IllegalArgumentException if the test value is false
   * @return false
   */
  public static boolean isFalse(boolean falseIfValid, String message, Object... arg) {
    if (falseIfValid) {
      throw new IllegalArgumentException(MessageFormatter.arrayFormat(message, arg).getMessage());
    }
    return false;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Checks that the specified parameter is non-null.
   * 
   * @param <T>  the type of the input parameter reflected in the result
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is null
   * @return the input {@code parameter}, not null
   */
  public static <T> T notNull(T parameter, String name) {
    if (parameter == null) {
      throw new IllegalArgumentException("Input parameter '" + name + "' must not be null");
    }
    return parameter;
  }

  /**
   * Checks that the specified injected parameter is non-null.
   * As a convention, the name of the parameter should be the exact name that you would
   * provide in a Spring configuration file.
   * 
   * @param <T>  the type of the input parameter reflected in the result
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is null
   * @return the input {@code parameter}, not null
   */
  public static <T> T notNullInjected(T parameter, String name) {
    if (parameter == null) {
      throw new IllegalArgumentException("Injected input parameter '" + name + "' must not be null");
    }
    return parameter;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks that the specified parameter is non-null and not blank.
   * <p>
   * The parameter is trimmed using {@link StringUtil#strip()} before testing for length zero.
   * This matches the definition of {@link StringUtils#isBlank(String)}.
   * the trimmed parameter is returned.
   * 
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is null or blank
   * @return the trimmed input {@code parameter}, not null
   */
  public static String notBlank(String parameter, String name) {
    notNull(parameter, name);
    parameter = StringUtils.strip(parameter);
    if (parameter.length() == 0) {
      throw new IllegalArgumentException("Input parameter '" + name + "' must not be empty");
    }
    return parameter;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks that the specified parameter is non-null and not empty.
   * 
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is null or empty
   * @return the input {@code parameter}, not null
   */
  public static String notEmpty(String parameter, String name) {
    notNull(parameter, name);
    if (parameter.length() == 0) {
      throw new IllegalArgumentException("Input parameter '" + name + "' must not be empty");
    }
    return parameter;
  }

  /**
   * Checks that the specified parameter array is non-null and not empty.
   * 
   * @param <T>  the type of the input array reflected in the result
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is null or empty
   * @return the input {@code parameter}, not null
   */
  public static <T> T[] notEmpty(T[] parameter, String name) {
    notNull(parameter, name);
    if (parameter.length == 0) {
      throw new IllegalArgumentException("Input parameter array '" + name + "' must not be empty");
    }
    return parameter;
  }

  /**
   * Checks that the specified parameter array is non-null and not empty.
   * 
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is null or empty
   * @return the input {@code parameter}, not null
   */
  public static int[] notEmpty(int[] parameter, String name) {
    notNull(parameter, name);
    if (parameter.length == 0) {
      throw new IllegalArgumentException("Input parameter array '" + name + "' must not be empty");
    }
    return parameter;
  }

  /**
   * Checks that the specified parameter array is non-null and not empty.
   * 
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is null or empty
   * @return the input {@code parameter}, not null
   */
  public static long[] notEmpty(long[] parameter, String name) {
    notNull(parameter, name);
    if (parameter.length == 0) {
      throw new IllegalArgumentException("Input parameter array '" + name + "' must not be empty");
    }
    return parameter;
  }

  /**
   * Checks that the specified parameter array is non-null and not empty.
   * 
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is null or empty
   * @return the input {@code parameter}, not null
   */
  public static double[] notEmpty(double[] parameter, String name) {
    notNull(parameter, name);
    if (parameter.length == 0) {
      throw new IllegalArgumentException("Input parameter array '" + name + "' must not be empty");
    }
    return parameter;
  }

  /**
   * Checks that the specified parameter iterable is non-null and not empty.
   * 
   * @param <T>  the type of the input iterable reflected in the result
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is null or empty
   * @return the input {@code parameter}, not null
   */
  public static <T> Iterable<T> notEmpty(Iterable<T> parameter, String name) {
    notNull(parameter, name);
    if (!parameter.iterator().hasNext()) {
      throw new IllegalArgumentException("Input parameter iterable '" + name + "' must not be empty");
    }
    return parameter;
  }

  /**
   * Checks that the specified parameter list is non-null and not empty.
   *
   * @param <T>  the type of the input iterable reflected in the result
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is null or empty
   * @return the input {@code parameter}, not null
   */
  public static <T> List<T> notEmpty(List<T> parameter, String name) {
    notNull(parameter, name);
    if (parameter.isEmpty()) {
      throw new IllegalArgumentException("Input parameter list '" + name + "' must not be empty");
    }
    return parameter;
  }

  /**
   * Checks that the specified parameter list is non-null and not empty.
   *
   * @param <T>  the type of the input iterable reflected in the result
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is null or empty
   * @return the input {@code parameter}, not null
   */
  public static <T> Set<T> notEmpty(Set<T> parameter, String name) {
    notNull(parameter, name);
    if (parameter.isEmpty()) {
      throw new IllegalArgumentException("Input parameter list '" + name + "' must not be empty");
    }
    return parameter;
  }

  /**
   * Checks that the specified parameter map is non-null and not empty.
   * 
   * @param <K>  the type of the input map key reflected in the result
   * @param <V>  the type of the input map value reflected in the result
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is null or empty
   * @return the input {@code parameter}, not null
   */
  public static <K, V> Map<K, V> notEmpty(Map<K, V> parameter, String name) {
    notNull(parameter, name);
    if (parameter.isEmpty()) {
      throw new IllegalArgumentException("Input parameter map '" + name + "' must not be empty");
    }
    return parameter;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks that the specified parameter array is non-null and contains no nulls.
   * 
   * @param <T>  the type of the input array reflected in the result
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is null or contains nulls
   * @return the input {@code parameter}, not null
   */
  public static <T> T[] noNulls(T[] parameter, String name) {
    notNull(parameter, name);
    for (int i = 0; i < parameter.length; i++) {
      if (parameter[i] == null) {
        throw new IllegalArgumentException("Input parameter array '" + name + "' must not contain null at index " + i);
      }
    }
    return parameter;
  }

  /**
   * Checks that the specified parameter collection is non-null and contains no nulls.
   * 
   * @param <T>  the type of the input iterable reflected in the result
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is null or contains nulls
   * @return the input {@code parameter}, not null
   */
  public static <T> Iterable<T> noNulls(Iterable<T> parameter, String name) {
    notNull(parameter, name);
    for (Object obj : parameter) {
      if (obj == null) {
        throw new IllegalArgumentException("Input parameter iterable '" + name + "' must not contain null");
      }
    }
    return parameter;
  }

  /**
   * Checks that the specified parameter map is non-null and contains no nulls.
   * 
   * @param <K>  the type of the input map key reflected in the result
   * @param <V>  the type of the input map value reflected in the result
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is null or contains nulls
   * @return the input {@code parameter}, not null
   */
  public static <K, V> Map<K, V> noNulls(Map<K, V> parameter, String name) {
    notNull(parameter, name);
    for (Entry<K, V> entry : parameter.entrySet()) {
      if (entry.getKey() == null) {
        throw new IllegalArgumentException("Input parameter map '" + name + "' must not contain a null key");
      }
      if (entry.getValue() == null) {
        throw new IllegalArgumentException("Input parameter map '" + name + "' must not contain a null value");
      }
    }
    return parameter;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks that the argument is not negative.
   * 
   * @param parameter  the parameter to check
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is negative
   * @return parameter
   */
  public static int notNegative(int parameter, String name) {
    if (parameter < 0) {
      throw new IllegalArgumentException("Input parameter '" + name + "' must not be negative");
    }
    return parameter;
  }

  /**
   * Checks that the argument is not negative.
   * 
   * @param parameter  the parameter to check
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is negative
   * @return parameter
   */
  public static long notNegative(long parameter, String name) {
    if (parameter < 0) {
      throw new IllegalArgumentException("Input parameter '" + name + "' must not be negative");
    }
    return parameter;
  }

  /**
   * Checks that the argument is not negative.
   * 
   * @param parameter  the parameter to check
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is negative
   * @return parameter
   */
  public static double notNegative(double parameter, String name) {
    if (parameter < 0) {
      throw new IllegalArgumentException("Input parameter '" + name + "' must not be negative");
    }
    return parameter;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks that the argument is not negative or zero.
   * 
   * @param parameter  the parameter to check
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is negative or zero
   * @return parameter
   */
  public static int notNegativeOrZero(int parameter, String name) {
    if (parameter <= 0) {
      throw new IllegalArgumentException("Input parameter '" + name + "' must not be negative or zero");
    }
    return parameter;
  }

  /**
   * Checks that the argument is greater than zero to within a given accuracy.
   * 
   * @param parameter  the value to check
   * @param eps  the accuracy
   * @param name  the name to use in the error message
   * @param args  the message arguments
   * @throws IllegalArgumentException If the absolute value of the argument is less than eps
   * @return parameter
   */
  public static double notNegativeOrZero(double parameter, double eps, String name, Object... args) {
    if (CompareUtils.closeEquals(parameter, 0, eps)) {
      throw new IllegalArgumentException(MessageFormatter.arrayFormat("Input parameter '" + name + "' must not be zero", args).getMessage());
    }
    if (parameter < 0) {
      throw new IllegalArgumentException(MessageFormatter.arrayFormat("Input parameter '" + name + "' must be greater than zero", args).getMessage());
    }
    return parameter;
  }
  
  /**
   * Checks that the argument is not negative or zero.
   * 
   * @param parameter  the parameter to check
   * @param name  the name of the parameter to use in the error message, not null
   * @throws IllegalArgumentException if the input is negative or zero
   * @return parameter
   */
  public static double notNegativeOrZero(double parameter, String name) {
    if (parameter <= 0) {
      throw new IllegalArgumentException("Input parameter '" + name + "' must not be negative or zero");
    }
    return parameter;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks that the argument is not equal to zero to within a given accuracy.
   * 
   * @param parameter  the value to check
   * @param eps  the accuracy
   * @param name  the name to use in the error message
   * @throws IllegalArgumentException If the absolute value of the argument is less than eps
   * @return parameter
   */
  public static double notZero(double parameter, double eps, String name) {
    if (CompareUtils.closeEquals(parameter, 0, eps)) {
      throw new IllegalArgumentException("Input parameter '" + name + "' must not be zero");
    }
    return parameter;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks a collection for null elements.
   * 
   * @param iterable  the collection to test, not null
   * @return true if the collection contains a null element
   * @throws IllegalArgumentException if the collection is null
   */
  public static boolean hasNullElement(Iterable<?> iterable) {
    notNull(iterable, "iterable");
    for (Object o : iterable) {
      if (o == null) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks a collection of doubles for negative elements.
   * 
   * @param iterable  the collection to test, not null
   * @return true if the collection contains a negative element
   * @throws IllegalArgumentException if the collection is null
   */

  public static boolean hasNegativeElement(Iterable<Double> iterable) {
    notNull(iterable, "collection");
    for (Double d : iterable) {
      if (d < 0) {
        return true;
      }
    }
    return false;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks that a value is within the range low < x < high.
   * 
   * @param low Low value of the range
   * @param high High value of the range
   * @param x  the value
   * @return true if low < x < high
   */
  public static boolean isInRangeExclusive(double low, double high, double x) {
    if (x > low && x < high) {
      return true;
    }
    return false;
  }

  /**
   * Checks that a value is within the range low <= x <= high.
   * 
   * @param low  the low value of the range
   * @param high  the high value of the range
   * @param x  the value
   * @return true if low <= x <= high
   */
  public static boolean isInRangeInclusive(double low, double high, double x) {
    if (x >= low && x <= high) {
      return true;
    }
    return false;
  }

  /**
   * Checks that a value is within the range low < x <= high.
   * 
   * @param low  the low value of the range
   * @param high  the high value of the range
   * @param x  the value
   * @return true if low < x <= high
   */

  public static boolean isInRangeExcludingLow(double low, double high, double x) {
    if (x > low && x <= high) {
      return true;
    }
    return false;
  }

  /**
   * Checks that a value is within the range low <= x < high.
   * 
   * @param low  the low value of the range
   * @param high  the high value of the range
   * @param x  the value
   * @return true if low <= x < high
   */
  public static boolean isInRangeExcludingHigh(double low, double high, double x) {
    if (x >= low && x < high) {
      return true;
    }
    return false;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks that the two values are in order or equal.
   * 
   * @param <T>  the type
   * @param obj1  the first object, will be checked for not null
   * @param obj2  the second object, will be checked for not null
   * @param param1  the first parameter name, not null
   * @param param2  the second parameter name, not null
   * @throws IllegalArgumentException if either input is null or they are not in order
   */
  public static <T> void inOrderOrEqual(Comparable<? super T> obj1, T obj2, String param1, String param2) {
    notNull(obj1, param1);
    notNull(obj2, param2);
    if (obj1.compareTo(obj2) > 0) {
      throw new IllegalArgumentException("Input parameter '" + param1 + "' must be before '" + param2 + "'");
    }
  }

}
