/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import com.opengamma.math.function.Function1D;

/**
 * 
 * @param <S> The type of the function input 
 * @param <T> The type of the function output 
 */
public interface MultiDimensionalRootFinder<S, T> {

  S getRoot(Function1D<S, T> function, S x);

}
