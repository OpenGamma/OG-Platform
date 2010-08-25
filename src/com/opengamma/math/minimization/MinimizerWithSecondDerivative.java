/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import com.opengamma.math.function.Function1D;

/**
 * 
 * @param <F>
 * @param <G>
 * @param <H>
 * @param <S>
 */

public interface MinimizerWithSecondDerivative<F extends Function1D<S, ?>, G extends Function1D<S, ?>, H extends Function1D<S, ?>, S> {
  S minimize(F function, G grad, H hessian, S startPosition);
}
