/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.differentiation;

import com.opengamma.analytics.math.function.Function1D;

/**
 * Given a one-dimensional function (see {@link Function1D}), returns a function that calculates the gradient.
 * @param <S> The domain type of the function
 * @param <T> The range type of the function
 * @param <U> The range type of the differential
 */
public interface Differentiator<S, T, U> {

  /**
   * @param function A function for which to get the differential function, not null
   * @return A function that calculates the differential
   */
  Function1D<S, U> differentiate(Function1D<S, T> function);

  /**
   * 
   * @param function A function for which to get the differential function, not null
   * @param domain A function that returns false if the requested value is not in  the domain, true otherwise. Not null
   * @return A function that calculates the differential
   */
  Function1D<S, U> differentiate(Function1D<S, T> function, Function1D<S, Boolean> domain);
}
