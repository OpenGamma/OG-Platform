/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import com.opengamma.math.function.Function1D;

/**
 * 
 * @param <S>
 * @param <T>
 */
public interface Minimizer<F extends Function1D<S, ?>, S> {
  /**
   * 
   */
  double TOLERANCE = 1e-12;

  S minimize(F function, S startPosition);

}
