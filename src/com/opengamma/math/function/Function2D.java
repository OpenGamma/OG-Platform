/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author emcleod
 */
public abstract class Function2D<S, T> implements Function<S, T> {
  private static final Logger s_Log = LoggerFactory.getLogger(Function2D.class);

  public T evaluate(final S... x) {
    if (x == null)
      throw new IllegalArgumentException("Null argument");
    if (x.length < 2)
      throw new IllegalArgumentException("Need two arguments");
    if (x.length > 2) {
      s_Log.info("Array had more than two elements; only using the first two.");
    }
    if (x[0] == null)
      throw new IllegalArgumentException("First argument was null");
    if (x[1] == null)
      throw new IllegalArgumentException("Second argument was null");
    return evaluate(x[0], x[1]);
  }

  public abstract T evaluate(S x1, S x2);
}
