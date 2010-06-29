/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import com.opengamma.math.function.Function1D;

/**
 * 
 * @param <S>
 * @param <T>
 * @param <U>
 * @param <V>
 */
public interface SingleRootFinder<S, T, U, V> {

  V getRoot(Function1D<S, T> function, U x1, U x2);
}
