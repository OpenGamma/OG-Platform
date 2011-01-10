/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.function;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <S> Type of variable
 * @param <T> Return type of function 
 */
public abstract class Function2D<S, T> implements Function<S, T> {
  private static final Logger s_logger = LoggerFactory.getLogger(Function2D.class);

  @Override
  public T evaluate(final S... x) {
    Validate.notNull(x);
    if (x.length < 2) {
      throw new IllegalArgumentException("Need two arguments");
    }
    if (x.length > 2) {
      s_logger.info("Array had more than two elements; only using the first two.");
    }
    Validate.notNull(x[0], "first argument");
    Validate.notNull(x[1], "second argument");
    return evaluate(x[0], x[1]);
  }

  public abstract T evaluate(S x1, S x2);
}
