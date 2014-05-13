/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.result;

/**
 * A function taking two arguments.
 * 
 * @param <T>  the type of the first argument
 * @param <U>  the type of the second argument
 * @param <R>  the return type
 */
public interface Function2<T, U, R> {

  /**
   * Applies the function to the arguments
   *
   * @param t  the first argument
   * @param u  the second argument
   * @return the value calculated by the function
   */
  R apply(T t, U u);

}
