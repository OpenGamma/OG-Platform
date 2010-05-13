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
  public U getRoot(Function1D<S, T> function, T x);

}
