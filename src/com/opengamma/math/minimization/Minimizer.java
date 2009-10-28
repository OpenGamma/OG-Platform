/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import com.opengamma.math.function.Function;

/**
 * 
 * @author emcleod
 * 
 */

public interface Minimizer<S extends Function<Double, Double>, T> {
  public double TOLERANCE = 1e-12;

  public Double[] minimize(S f, T[] initialPoints);

}
