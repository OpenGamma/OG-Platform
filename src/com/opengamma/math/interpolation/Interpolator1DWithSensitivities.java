/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

/**
 * 
 */
abstract public class Interpolator1DWithSensitivities<T extends Interpolator1DModel> extends Interpolator1D<T> {

  @Override
  abstract public InterpolationResultWithSensitivities interpolate(T model, Double value);

}
