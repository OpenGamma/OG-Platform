package com.opengamma.math.interpolation;

/**
 * 
 * @author emcleod
 * 
 */

public interface Interpolator<S, T, U> {

  public InterpolationResult<U> interpolate(S data, T value) throws InterpolationException;
}
