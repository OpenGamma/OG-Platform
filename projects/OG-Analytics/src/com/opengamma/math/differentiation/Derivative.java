/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.differentiation;

import com.opengamma.math.function.Function1D;

/**
 * finds the gradient (in a generic sense) of a function 
 * @param <S> The domain type of the function
 * @param <T> The range type of the function
 * @param <U> The range type of the gradient 
 */
public interface Derivative<S, T, U> {

  Function1D<S, U> derivative(Function1D<S, T> function);
}
