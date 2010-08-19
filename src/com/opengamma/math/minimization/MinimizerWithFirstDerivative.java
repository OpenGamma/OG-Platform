/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public interface MinimizerWithFirstDerivative<F extends Function1D<S, ?>, G extends Function1D<S, ?>, S> {

  S minimize(F function, G grad, S startPosition);
}
