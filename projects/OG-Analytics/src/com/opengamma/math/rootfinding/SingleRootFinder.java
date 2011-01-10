/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import com.opengamma.math.function.Function1D;

/**
 * 
 * @param <S>
 * @param <T>
 */
public interface SingleRootFinder<S, T> {

  S getRoot(Function1D<S, T> function, S x1, S x2);
}
