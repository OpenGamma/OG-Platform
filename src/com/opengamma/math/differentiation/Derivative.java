/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.differentiation;

import com.opengamma.math.function.Function;

/**
 * 
 * @param <S>
 * @param <T>
 */
public interface Derivative<S, T> {

  Function<S, T> derivative(Function<S, T> function);
}
