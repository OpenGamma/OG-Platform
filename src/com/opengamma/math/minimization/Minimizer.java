/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import com.opengamma.math.function.Function;

/**
 * 
 * @param <S>
 * @param <T>
 */
public interface Minimizer<S extends Function<Double, Double>, T> {
  /**
   * 
   */
  double TOLERANCE = 1e-12;

  T minimize(S f, T point1, T point2);

}
