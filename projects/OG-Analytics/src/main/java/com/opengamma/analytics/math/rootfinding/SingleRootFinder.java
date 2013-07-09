/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.rootfinding;

import com.opengamma.analytics.math.function.Function1D;

/**
 * Interface for classes that attempt to find a root for a one-dimensional function (see {@link com.opengamma.analytics.math.function.Function1D}) $f(x)$ bounded 
 * by user-supplied values, $x_1$ and $x_2$. If there is not a single root between these 
 * bounds, an exception is thrown.
 * @param <S> The input type of the function
 * @param <T> The output type of the function
 */
public interface SingleRootFinder<S, T> {

  /**
   * @param function The function, not null
   * @param x The bounds, not null
   * @return A root lying between x1 and x2
   */
  @SuppressWarnings("unchecked")
  S getRoot(Function1D<S, T> function, S... x);

}
