/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.result;

/**
 * A function taking two arguments.
 */
public interface Function2<T, U, R> {

  /**
   * Applies the function to the arguments
   *
   * @param t an argument
   * @param u an argument
   * @return the function's return value
   */
  R apply(T t, U u);
}
