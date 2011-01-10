/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

/**
 * @param <S> The type of the data
 * @param <T> The type of the value to interpolate
 */

public interface Interpolator<S, T> {

  Double interpolate(S data, T value);
}
