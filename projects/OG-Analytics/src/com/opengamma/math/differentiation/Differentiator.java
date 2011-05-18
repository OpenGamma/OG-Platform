/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.differentiation;

import com.opengamma.math.function.Function1D;

/**
 * Given a one-dimensional function (see {@link Function1D}), returns a function that calculates the gradient. 
 * @param <S> The domain type of the function
 * @param <T> The range type of the function
 * @param <U> The range type of the gradient 
 */
public interface Differentiator<S, T, U> {

  /**
   * @param function A function for which to get the gradient function, not null
   * @return A function that calculates the gradient 
   */
  Function1D<S, U> differentiate(Function1D<S, T> function);
}
