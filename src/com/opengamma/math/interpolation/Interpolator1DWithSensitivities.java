/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

/**
 * 
 */
abstract public class Interpolator1DWithSensitivities<T extends Interpolator1DModel> implements
    Interpolator<T, Double, InterpolationResultWithSensitivities1> {

  @Override
  abstract public InterpolationResultWithSensitivities1 interpolate(T model, Double value);

}
