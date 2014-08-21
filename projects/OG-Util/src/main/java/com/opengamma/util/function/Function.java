/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.function;

/**
 * A function taking a single argument.
 * <p>
 * This takes one object argument and returns an object result.
 *
 * @param <T> the type of the argument
 * @param <R> the type of the result
 */
public interface Function<T, R> {

  /**
   * Applies the function.
   *
   * @param obj  the argument
   * @return the result of the function
   */
  R apply(T obj);

}
