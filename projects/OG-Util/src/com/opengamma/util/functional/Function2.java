/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.functional;

/**
 * Simple functional interface taking one parameter.
 * 
 * @param <A> the input parameter type
 * @param <B> the input parameter type
 * @param <R> the result type
 */
public abstract class Function2<A, B, R> {

  /**
   * Executes the behaviour of the function.
   * 
   * @param a  the first parameter
   * @param b  the second parameter
   * @return the result
   */
  public abstract R execute(A a, B b);

}
