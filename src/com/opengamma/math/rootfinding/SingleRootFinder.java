/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 */
public interface SingleRootFinder<S, T, U> {

  public U getRoot(Function1D<S, T> function, T x1, T x2);
}