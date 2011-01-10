/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import com.opengamma.math.function.Function1D;

/**
 * 
 * @param <F>
 * @param <S>
 */
public interface Minimizer<F extends Function1D<S, ?>, S> {

  S minimize(final F function, final S startPosition);

}
