/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.function;

import org.apache.commons.lang.Validate;

/**
 * 2-D function implementation.
 * @param <S> Type of the arguments
 * @param <T> Return type of function 
 */
public abstract class Function2D<S, T> implements Function<S, T> {

  /**
   * Implementation of the interface. This method only uses the first and second arguments.
   * @param x The list of inputs into the function, not null and no null elements
   * @return The value of the function
   */
  @Override
  public T evaluate(final S... x) {
    Validate.notNull(x);
    if (x.length != 2) {
      throw new IllegalArgumentException("Need two arguments");
    }
    Validate.notNull(x[0], "first argument");
    Validate.notNull(x[1], "second argument");
    return evaluate(x[0], x[1]);
  }

  /**
   * 2-D function method
   * @param x1 The first argument of the function, not null
   * @param x2 The second argument of the function, not null
   * @return The value of the function
   */
  public abstract T evaluate(S x1, S x2);
}
