/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public interface MultiDRootFinder<S, T, U> {
  public S getRoot(Function1D<S, T> function, S x);

  public S getRoot(Function1D<S, T> function, Function1D<S, U> jacobian, S x);
}
