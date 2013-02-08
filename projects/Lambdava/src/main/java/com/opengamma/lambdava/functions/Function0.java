/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.lambdava.functions;

/**
 * Simple functional interface taking zero parameter.
 * 
 * @param <R> the result type
 */
public abstract class Function0<R> {

  /**
   * Executes the behaviour of the function.
   * 
   * @return the result
   */
  public abstract R execute();

}
