/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.function;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 1-D function implementation.
 * @param <S> Type of the arguments
 * @param <T> Return type of the function
 */
public abstract class Function1D<S, T> implements Function<S, T> {
  private static final Logger s_logger = LoggerFactory.getLogger(Function1D.class);

  /**
   * Implementation of the interface. This method only uses the first argument.
   * @param x The list of inputs into the function, not null and no null elements
   * @return The value of the function
   */
  @Override
  public T evaluate(final S... x) {
    Validate.noNullElements(x, "Parameter list");
    Validate.notEmpty(x, "parameter list");
    if (x.length > 1) {
      throw new IllegalArgumentException("Array had more than one element");
      // s_logger.info("Array had more than one element; only using the first");
    }
    return evaluate(x[0]);
  }

  /**
   * 1-D function method
   * @param x The argument of the function, not null
   * @return The value of the function
   */
  public abstract T evaluate(S x);
}
