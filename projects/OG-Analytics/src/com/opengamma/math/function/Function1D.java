/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;

/**
 *         Many functions only need one argument: extending this function
 *         eliminates the need to create an array.
 * @param <S>
 *          Type of the arguments
 * @param <T>
 *          Return type of function
 */
public abstract class Function1D<S, T> implements Function<S, T> {
  private static final Logger s_logger = LoggerFactory.getLogger(Function1D.class);

  @Override
  public T evaluate(final S... x) {
    ArgumentChecker.notNull(x, "Null parameter list");
    ArgumentChecker.notEmpty(x, "Parameter list");
    ArgumentChecker.noNulls(x, "Parameter list");
    if (x.length > 1) {
      s_logger.info("Array had more than one element; only using the first");
    }
    return evaluate(x[0]);
  }

  public abstract T evaluate(S x);
}
