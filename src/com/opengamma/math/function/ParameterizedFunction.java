/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.function;

/**
 * 
 */
public interface ParameterizedFunction<S, T, U> {

  U evaluate(S x, T parameters);
}
