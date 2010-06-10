/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

/**
 * @param <S> The type of the data
 * @param <T> The type of the value to interpolate
 * @param <U> The type of the result
 */

public interface Interpolator<S, T, U> {

  InterpolationResult<U> interpolate(S data, T value);
}
