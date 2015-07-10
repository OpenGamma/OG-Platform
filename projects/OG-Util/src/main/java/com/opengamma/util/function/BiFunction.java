/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.function;

/**
 * A function taking two arguments.
 * <p>
 * This takes two object arguments and returns an object result.
 *
 * @param <T> the type of the first argument
 * @param <U> the type of the second argument
 * @param <R> the type of the result
 */
public interface BiFunction<T, U, R> {

  /**
   * Applies the function.
   *
   * @param obj1  the first argument
   * @param obj2  the second argument
   * @return the result of the function
   */
  R apply(T obj1, U obj2);

}
