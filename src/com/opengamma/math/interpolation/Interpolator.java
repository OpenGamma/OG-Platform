/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

/**
 * 
 * @author emcleod
 * 
 */

public interface Interpolator<S, T, U> {

  public InterpolationResult<U> interpolate(S data, T value);
}
