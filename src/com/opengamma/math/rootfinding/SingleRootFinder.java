/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public interface SingleRootFinder<S, T> {

  public T getRoot(Function1D<S, T> function, S x1, S x2);
}