/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import com.opengamma.math.function.Function1D;

/**
 * 
 * @param <F>
 * @param <G>
 * @param <S>
 */
public interface MinimizerWithGradient<F extends Function1D<S, ?>, G extends Function1D<S, ?>, S> extends Minimizer<F, S> {

  S minimize(final F function, final G gradient, final S startPosition);
}
