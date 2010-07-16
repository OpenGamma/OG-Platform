/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

/**
 * 
 */
public interface ExtrapolatorMethod<T extends Interpolator1DDataBundle, U extends InterpolationResult> {

  public abstract U leftExtrapolate(T model, Double value, Interpolator1D<T, U> interpolator);

  public abstract U rightExtrapolate(T model, Double value, Interpolator1D<T, U> interpolator);

}
