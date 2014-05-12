/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.slf4j.helpers.MessageFormatter;

/**
 * Contains utility methods for checking inputs to methods.
 * <p>
 * This utility is used throughout the system to validate inputs to methods.
 * Most of the methods return their validated input, allowing patterns like this:
 * <pre>
 *  // constructor
 *  public Person(String name, int age) {
 *    _name = ArgumentChecker.notBlank(name, "name");
 *    _age = ArgumentChecker.notNegative(age, "age");
 *  }
 * </pre>
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
   * <p>
   * Given the input parameter, this returns only if it is true.
   * This will normally be the result of a caller-specific check.
   * For example:
   * <pre>
   *  ArgumentChecker.isTrue(collection.contains("value"), "Collection must contain 'value'");
   * </pre>
   * 
   * @param trueIfValid  a boolean resulting from testing an argument, may be null
   * @param message  the error message, not null
   * @return true always
   * @throws IllegalArgumentException if the test value is false
   */
  public static boolean isTrue(boolean trueIfValid, String message) {
    if (!trueIfValid) {
      throw new IllegalArgumentException(message);
    }
    return true;
  }
  
  /**
   * Checks that the specified boolean is true.
   * <p>
   * Given the input parameter, this returns only if it is true.
   * This will normally be the result of a caller-specific check.
   * For example:
   * <pre>
   *  ArgumentChecker.isTrue(collection.contains("value"), "Collection must contain 'value': {}", collection);
   * </pre>
   * <p>
   * Formatting of the error message uses placeholders as per SLF4J.
   * Each {} in the message is replaced by the next message argument.
   * 
   * @param trueIfValid  a boolean resulting from testing an argument, may be null
   * @param message  the error message with {} placeholders, not null
   * @param arg  the message arguments
   * @return true always
   * @throws IllegalArgumentException if the test value is false
   */
  public static boolean isTrue(boolean trueIfValid, String message, Object... arg) {
    if (!trueIfValid) {
      throw new IllegalArgumentException(MessageFormatter.arrayFormat(message, arg).getMessage());
    }
    return true;
  }  

  /**
   * Checks that the specified boolean is false.
   * <p>
   * Given the input parameter, this returns only if it is false.
   * This will normally be the result of a caller-specific check.
   * For example:
   * <pre>
   *  ArgumentChecker.isFalse(collection.contains("value"), "Collection must not contain 'value'");
   * </pre>
   * 
   * @param falseIfValid  a boolean resulting from testing an argument, may be null
   * @param message  the error message, not null
   * @return false always
   * @throws IllegalArgumentException if the test value is true
   */
  public static boolean isFalse(boolean falseIfValid, String message) {
    if (falseIfValid) {
      throw new IllegalArgumentException(message);
    }
    return false;
  }

  /**
   * Checks that the specified boolean is false.
   * <p>
   * Given the input parameter, this returns only if it is false.
   * This will normally be the result of a caller-specific check.
   * For example:
   * <pre>
   *  ArgumentChecker.isFalse(collection.contains("value"), "Collection must not contain 'value': {}", collection);
   * </pre>
   * <p>
   * Formatting of the error message uses placeholders as per SLF4J.
   * Each {} in the message is replaced by the next message argument.
   * 
   * @param falseIfValid  a boolean resulting from testing an argument, may be null
   * @param message  the error message with {} placeholders, not null
   * @param arg  the message arguments
   * @return false always
   * @throws IllegalArgumentException if the test value is true
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
   * <p>
   * Given the input parameter, this returns only if it is non-null.
   * For example, in a constructor:
   * <pre>
   *  _name = ArgumentChecker.notNull(name, "name");
   * </pre>
   * 
   * @param <T>  the type of the input parameter reflected in the result
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @return the input {@code parameter}, not null
   * @throws IllegalArgumentException if the input is null
   */
  public static <T> T notNull(T parameter, String name) {
    if (parameter == null) {
      throw new IllegalArgumentException("Input parameter '" + name + "' must not be null");
    }
    return parameter;
  }

  /**
   * Checks that the specified injected parameter is non-null.
   * <p>
   * Given the input parameter, this returns only if it is non-null.
   * This is intended for parameters injected from configuration or similar.
   * As a convention, the name of the parameter should be the exact name that
   * you would provide in the configuration file.
   * 
   * @param <T>  the type of the input parameter reflected in the result
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @return the input {@code parameter}, not null
   * @throws IllegalArgumentException if the input is null
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
   * Given the input parameter, this returns the trimmed input only if it is
   * non-null and contains at least one non whitespace character.
   * For example, in a constructor:
   * <pre>
   *  _name = ArgumentChecker.notBlank(name, "name");
   * </pre>
   * <p>
   * The parameter is trimmed using {@link StringUtils#strip()} before testing for length zero.
   * This matches the definition of {@link StringUtils#isBlank(String)}.
   * The trimmed parameter is returned.
   * 
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @return the trimmed input {@code parameter}, not null
   * @throws IllegalArgumentException if the input is null or blank
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
   * <p>
   * Given the input parameter, this returns only if it is non-null and contains
   * at least one character, which may be a whitespace character.
   * See also {@link #notBlank(String, String)}.
   * For example, in a constructor:
   * <pre>
   *  _name = ArgumentChecker.notEmpty(name, "name");
   * </pre>
   * 
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @return the input {@code parameter}, not null
   * @throws IllegalArgumentException if the input is null or empty
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
   * <p>
   * Given the input parameter, this returns only if it is non-null and contains
   * at least one element. The element is not validated and may be null.
   * For example, in a constructor:
   * <pre>
   *  _names = ArgumentChecker.notEmpty(names, "names");
   * </pre>
   * 
   * @param <T>  the type of the input array reflected in the result
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @return the input {@code parameter}, not null
   * @throws IllegalArgumentException if the input is null or empty
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
   * <p>
   * Given the input parameter, this returns only if it is non-null and contains
   * at least one element.
   * For example, in a constructor:
   * <pre>
   *  _values = ArgumentChecker.notEmpty(values, "values");
   * </pre>
   * 
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @return the input {@code parameter}, not null
   * @throws IllegalArgumentException if the input is null or empty
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
   * <p>
   * Given the input parameter, this returns only if it is non-null and contains
   * at least one element.
   * For example, in a constructor:
   * <pre>
   *  _values = ArgumentChecker.notEmpty(values, "values");
   * </pre>
   * 
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @return the input {@code parameter}, not null
   * @throws IllegalArgumentException if the input is null or empty
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
   * <p>
   * Given the input parameter, this returns only if it is non-null and contains
   * at least one element.
   * For example, in a constructor:
   * <pre>
   *  _values = ArgumentChecker.notEmpty(values, "values");
   * </pre>
   * 
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @return the input {@code parameter}, not null
   * @throws IllegalArgumentException if the input is null or empty
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
   * <p>
   * Given the input parameter, this returns only if it is non-null and contains
   * at least one element. The element is not validated and may be null.
   * For example, in a constructor:
   * <pre>
   *  _values = ArgumentChecker.notEmpty(values, "values");
   * </pre>
   * 
   * @param <T>  the element type of the input iterable reflected in the result
   * @param <I>  the type of the input iterable, reflected in the result
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @return the input {@code parameter}, not null
   * @throws IllegalArgumentException if the input is null or empty
   */
  public static <T, I extends Iterable<T>> I notEmpty(I parameter, String name) {
    notNull(parameter, name);
    if (!parameter.iterator().hasNext()) {
      throw new IllegalArgumentException("Input parameter iterable '" + name + "' must not be empty");
    }
    return parameter;
  }

  /**
   * Checks that the specified parameter collection is non-null and not empty.
   * <p>
   * Given the input parameter, this returns only if it is non-null and contains
   * at least one element. The element is not validated and may be null.
   * For example, in a constructor:
   * <pre>
   *  _values = ArgumentChecker.notEmpty(values, "values");
   * </pre>
   *
   * @param <T>  the element type of the input collection reflected in the result
   * @param <C>  the type of the input collection, reflected in the result
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @return the input {@code parameter}, not null
   * @throws IllegalArgumentException if the input is null or empty
   */
  public static <T, C extends Collection<T>> C notEmpty(C parameter, String name) {
    notNull(parameter, name);
    if (parameter.isEmpty()) {
      throw new IllegalArgumentException("Input parameter collection '" + name + "' must not be empty");
    }
    return parameter;
  }

  /**
   * Checks that the specified parameter map is non-null and not empty.
   * <p>
   * Given the input parameter, this returns only if it is non-null and contains
   * at least one mapping. The element is not validated and may contain nulls.
   * For example, in a constructor:
   * <pre>
   *  _keyValues = ArgumentChecker.notEmpty(keyValues, "keyValues");
   * </pre>
   * 
   * @param <K>  the key type of the input map key, reflected in the result
   * @param <V>  the value type of the input map value, reflected in the result
   * @param <M>  the type of the input map, reflected in the result
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @return the input {@code parameter}, not null
   * @throws IllegalArgumentException if the input is null or empty
   */
  public static <K, V, M extends Map<K, V>> M notEmpty(M parameter, String name) {
    notNull(parameter, name);
    if (parameter.isEmpty()) {
      throw new IllegalArgumentException("Input parameter map '" + name + "' must not be empty");
    }
    return parameter;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks that the specified parameter array is non-null and contains no nulls.
   * <p>
   * Given the input parameter, this returns only if it is non-null and contains no nulls.
   * For example, in a constructor:
   * <pre>
   *  _values = ArgumentChecker.noNulls(values, "values");
   * </pre>
   * 
   * @param <T>  the type of the input array reflected in the result
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @return the input {@code parameter}, not null
   * @throws IllegalArgumentException if the input is null or contains nulls
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
   * <p>
   * Given the input parameter, this returns only if it is non-null and contains no nulls.
   * For example, in a constructor:
   * <pre>
   *  _values = ArgumentChecker.noNulls(values, "values");
   * </pre>
   * 
   * @param <T>  the element type of the input iterable reflected in the result
   * @param <I>  the type of the input iterable, reflected in the result
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @return the input {@code parameter}, not null
   * @throws IllegalArgumentException if the input is null or contains nulls
   */
  public static <T, I extends Iterable<T>> I noNulls(I parameter, String name) {
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
   * <p>
   * Given the input parameter, this returns only if it is non-null and contains no nulls.
   * For example, in a constructor:
   * <pre>
   *  _keyValues = ArgumentChecker.noNulls(keyValues, "keyValues");
   * </pre>
   * 
   * @param <K>  the key type of the input map key, reflected in the result
   * @param <V>  the value type of the input map value, reflected in the result
   * @param <M>  the type of the input map, reflected in the result
   * @param parameter  the parameter to check, may be null
   * @param name  the name of the parameter to use in the error message, not null
   * @return the input {@code parameter}, not null
   * @throws IllegalArgumentException if the input is null or contains nulls
   */
  public static <K, V, M extends Map<K, V>> M noNulls(M parameter, String name) {
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
   * <p>
   * Given the input parameter, this returns only if it is zero or greater.
   * For example, in a constructor:
   * <pre>
   *  _amount = ArgumentChecker.notNegative(amount, "amount");
   * </pre>
   * 
   * @param parameter  the parameter to check
   * @param name  the name of the parameter to use in the error message, not null
   * @return the input {@code parameter}
   * @throws IllegalArgumentException if the input is negative
   */
  public static int notNegative(int parameter, String name) {
    if (parameter < 0) {
      throw new IllegalArgumentException("Input parameter '" + name + "' must not be negative");
    }
    return parameter;
  }

  /**
   * Checks that the argument is not negative.
   * <p>
   * Given the input parameter, this returns only if it is zero or greater.
   * For example, in a constructor:
   * <pre>
   *  _amount = ArgumentChecker.notNegative(amount, "amount");
   * </pre>
   * 
   * @param parameter  the parameter to check
   * @param name  the name of the parameter to use in the error message, not null
   * @return the input {@code parameter}
   * @throws IllegalArgumentException if the input is negative
   */
  public static long notNegative(long parameter, String name) {
    if (parameter < 0) {
      throw new IllegalArgumentException("Input parameter '" + name + "' must not be negative");
    }
    return parameter;
  }

  /**
   * Checks that the argument is not negative.
   * <p>
   * Given the input parameter, this returns only if it is zero or greater.
   * For example, in a constructor:
   * <pre>
   *  _amount = ArgumentChecker.notNegative(amount, "amount");
   * </pre>
   * 
   * @param parameter  the parameter to check
   * @param name  the name of the parameter to use in the error message, not null
   * @return the input {@code parameter}
   * @throws IllegalArgumentException if the input is negative
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
   * <p>
   * Given the input parameter, this returns only if it is greater than zero.
   * For example, in a constructor:
   * <pre>
   *  _amount = ArgumentChecker.notNegativeOrZero(amount, "amount");
   * </pre>
   * 
   * @param parameter  the parameter to check
   * @param name  the name of the parameter to use in the error message, not null
   * @return the input {@code parameter}
   * @throws IllegalArgumentException if the input is negative or zero
   */
  public static int notNegativeOrZero(int parameter, String name) {
    if (parameter <= 0) {
      throw new IllegalArgumentException("Input parameter '" + name + "' must not be negative or zero");
    }
    return parameter;
  }

  /**
   * Checks that the argument is not negative or zero.
   * <p>
   * Given the input parameter, this returns only if it is greater than zero.
   * For example, in a constructor:
   * <pre>
   *  _amount = ArgumentChecker.notNegativeOrZero(amount, "amount");
   * </pre>
   * 
   * @param parameter  the parameter to check
   * @param name  the name of the parameter to use in the error message, not null
   * @return the input {@code parameter}
   * @throws IllegalArgumentException if the input is negative or zero
   */
  public static long notNegativeOrZero(long parameter, String name) {
    if (parameter <= 0) {
      throw new IllegalArgumentException("Input parameter '" + name + "' must not be negative or zero");
    }
    return parameter;
  }

  /**
   * Checks that the argument is not negative or zero.
   * <p>
   * Given the input parameter, this returns only if it is greater than zero.
   * For example, in a constructor:
   * <pre>
   *  _amount = ArgumentChecker.notNegativeOrZero(amount, "amount");
   * </pre>
   * 
   * @param parameter  the parameter to check
   * @param name  the name of the parameter to use in the error message, not null
   * @return the input {@code parameter}
   * @throws IllegalArgumentException if the input is negative or zero
   */
  public static double notNegativeOrZero(double parameter, String name) {
    if (parameter <= 0) {
      throw new IllegalArgumentException("Input parameter '" + name + "' must not be negative or zero");
    }
    return parameter;
  }

  /**
   * Checks that the argument is greater than zero to within a given accuracy.
   * <p>
   * Given the input parameter, this returns only if it is greater than zero
   * using the {@code eps} accuracy for zero.
   * For example, in a constructor:
   * <pre>
   *  _amount = ArgumentChecker.notNegativeOrZero(amount, 0.0001d, "amount");
   * </pre>
   * 
   * @param parameter  the value to check
   * @param eps  the accuracy to use for zero
   * @param name  the name of the parameter to use in the error message, not null
   * @return the input {@code parameter}
   * @throws IllegalArgumentException if the absolute value of the argument is less than eps
   */
  public static double notNegativeOrZero(double parameter, double eps, String name) {
    if (CompareUtils.closeEquals(parameter, 0, eps)) {
      throw new IllegalArgumentException("Input parameter '" + name + "' must not be zero");
    }
    if (parameter < 0) {
      throw new IllegalArgumentException("Input parameter '" + name + "' must be greater than zero");
    }
    return parameter;
  }

  /**
   * Checks that the argument is greater than zero to within a given accuracy.
   * <p>
   * Given the input parameter, this returns only if it is greater than zero
   * using the {@code eps} accuracy for zero.
   * For example, in a constructor:
   * <pre>
   *  _amount = ArgumentChecker.notNegativeOrZero(amount, 0.0001d, "Invalid amount {} for date {}", amount, date);
   * </pre>
   * <p>
   * Formatting of the error message uses placeholders as per SLF4J.
   * Each {} in the message is replaced by the next message argument.
   * 
   * @param parameter  the value to check
   * @param eps  the accuracy to use for zero
   * @param message  the error message with {} placeholders, not null
   * @param args  the message arguments
   * @return the input {@code parameter}
   * @throws IllegalArgumentException if the absolute value of the argument is less than eps
   * @deprecated no need for message arguments here
   */
  @Deprecated
  public static double notNegativeOrZero(double parameter, double eps, String message, Object... args) {
    if (CompareUtils.closeEquals(parameter, 0, eps)) {
      throw new IllegalArgumentException(MessageFormatter.arrayFormat(message, args).getMessage());
    }
    if (parameter < 0) {
      throw new IllegalArgumentException(MessageFormatter.arrayFormat(message, args).getMessage());
    }
    return parameter;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks that the argument is not equal to zero to within a given accuracy.
   * <p>
   * Given the input parameter, this returns only if it is not zero comparing
   * using the {@code eps} accuracy.
   * For example, in a constructor:
   * <pre>
   *  _amount = ArgumentChecker.notZero(amount, 0.0001d, "amount");
   * </pre>
   * 
   * @param parameter  the value to check
   * @param eps  the accuracy to use for zero
   * @param name  the name of the parameter to use in the error message, not null
   * @return the input {@code parameter}
   * @throws IllegalArgumentException if the absolute value of the argument is less than eps
   */
  public static double notZero(double parameter, double eps, String name) {
    if (CompareUtils.closeEquals(parameter, 0d, eps)) {
      throw new IllegalArgumentException("Input parameter '" + name + "' must not be zero");
    }
    return parameter;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks a collection for null elements.
   * <p>
   * Given a collection, this returns true if any element is null.
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
   * <p>
   * Given a collection, this returns true if any element is negative.
   * 
   * @param iterable  the collection to test, not null
   * @return true if the collection contains a negative element
   * @throws IllegalArgumentException if the collection is null or any element is null
   */
  public static boolean hasNegativeElement(Iterable<Double> iterable) {
    notNull(iterable, "collection");
    for (Double d : iterable) {
      notNull(d, "collection element");
      if (d < 0) {
        return true;
      }
    }
    return false;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks that a value is within the range low &lt; x &lt; high.
   * <p>
   * Given a value, this returns true if it is within the specified range
   * excluding the boundaries.
   * 
   * @param low  the low value of the range
   * @param high  the high value of the range
   * @param value  the value
   * @return true if low &lt; x &lt; high
   */
  public static boolean isInRangeExclusive(double low, double high, double value) {
    return (value > low && value < high);
  }

  /**
   * Checks that a value is within the range low &lt;= x &lt;= high.
   * <p>
   * Given a value, this returns true if it is within the specified range
   * including both boundaries.
   * 
   * @param low  the low value of the range
   * @param high  the high value of the range
   * @param value  the value
   * @return true if low &lt;= x &lt;= high
   */
  public static boolean isInRangeInclusive(double low, double high, double value) {
    return (value >= low && value <= high);
  }

  /**
   * Checks that a value is within the range low &lt; x &lt;= high.
   * <p>
   * Given a value, this returns true if it is within the specified range
   * excluding the lower boundary but including the upper boundary.
   * 
   * @param low  the low value of the range
   * @param high  the high value of the range
   * @param value  the value
   * @return true if low &lt; x &lt;= high
   */

  public static boolean isInRangeExcludingLow(double low, double high, double value) {
    return (value > low && value <= high);
  }

  /**
   * Checks that a value is within the range low &lt;= x &lt; high.
   * <p>
   * Given a value, this returns true if it is within the specified range
   * excluding the upper boundary but including the lower boundary.
   * 
   * @param low  the low value of the range
   * @param high  the high value of the range
   * @param value  the value
   * @return true if low &lt;= x &lt; high
   */
  public static boolean isInRangeExcludingHigh(double low, double high, double value) {
    return (value >= low && value < high);
  }

  //-------------------------------------------------------------------------
  /**
   * Checks that the two values are in order or equal.
   * <p>
   * Given two comparable instances, this checks that the first is "lower than"
   * or "equal to" the second.
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
