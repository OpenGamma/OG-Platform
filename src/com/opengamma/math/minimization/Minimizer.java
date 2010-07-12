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
 */
public interface Minimizer<S extends Function<Double, Double>> {
  /**
   * 
   */
  double TOLERANCE = 1e-12;

  double[] minimize(S f, double[] initialPoints);

}
