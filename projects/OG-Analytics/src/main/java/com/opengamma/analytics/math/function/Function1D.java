/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.function;

import java.io.Serializable;

import com.opengamma.util.ArgumentChecker;

/**
 * 1-D function implementation.
 * @param <S> Type of the arguments
 * @param <T> Return type of the function
 */
public abstract class Function1D<S, T> implements Function<S, T>, Serializable {

  /**
   * Implementation of the interface. This method only uses the first argument.
   * @param x The list of inputs into the function, not null and no null elements
   * @return The value of the function
   */
  @SuppressWarnings("unchecked")
  @Override
  public T evaluate(final S... x) {
    ArgumentChecker.noNulls(x, "parameter list");
    ArgumentChecker.isTrue(x.length == 1, "parameter list must have one element");
    return evaluate(x[0]);
  }

  /**
   * 1-D function method
   * @param x The argument of the function, not null
   * @return The value of the function
   */
  public abstract T evaluate(S x);
}
