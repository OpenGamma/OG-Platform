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
 * @param <R> the result type
 */
public abstract class Function1<A, R> {

  /**
   * Executes the behaviour of the function.
   * 
   * @param a  the first parameter
   * @return the result
   */
  public abstract R execute(A a);

}
