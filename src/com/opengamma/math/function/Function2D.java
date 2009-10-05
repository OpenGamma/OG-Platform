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
public abstract class Function2D<S, T, U> implements Function<Object, U> {
  private static final Logger s_Log = LoggerFactory.getLogger(Function2D.class);

  public U evaluate(final Object... x) {
    if (x == null)
      throw new IllegalArgumentException("Null argument");
    if (x.length > 2) {
      s_Log.info("Array had more than two elements; only using the first two.");
    }
    return evaluate(x[0], x[1]);
  }

  public abstract U evaluate(S x1, T x2);
}
