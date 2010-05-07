/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *         Many functions only need one argument: extending this function
 *         eliminates the need to create an array.
 * @param <S>
 *          Type of the arguments
 * @param <T>
 *          Return type of function
 */
public abstract class Function1D<S, T> implements Function<S, T> {
  private static final Logger s_Log = LoggerFactory.getLogger(Function1D.class);

  public T evaluate(final S... x) {
    if (x == null)
      throw new IllegalArgumentException("Null argument");
    if (x.length == 0)
      throw new IllegalArgumentException("Argument array was empty");
    if (x[0] == null)
      throw new IllegalArgumentException("Argument was null");
    if (x.length > 1) {
      s_Log.info("Array had more than one element; only using the first");
    }
    return evaluate(x[0]);
  }

  public abstract T evaluate(S x);
}
