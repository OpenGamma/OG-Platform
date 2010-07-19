/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

/**
 * 
 * @param <T> Type of data bundle
 * @param <U> Type of interpolation result
 */
public interface ExtrapolatorMethod<T extends Interpolator1DDataBundle, U extends InterpolationResult> {

  U leftExtrapolate(T model, Double value, Interpolator1D<T, U> interpolator);

  U rightExtrapolate(T model, Double value, Interpolator1D<T, U> interpolator);

}
