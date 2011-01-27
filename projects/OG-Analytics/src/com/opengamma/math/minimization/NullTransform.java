/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

/**
 * 
 */
public class NullTransform implements ParameterLimitsTransform {

  @Override
  public double inverseTrasfrom(double y) {
    return y;
  }

  @Override
  public double inverseTrasfromGradient(double y) {
    return 1;
  }

  @Override
  public double transform(double x) {
    return x;
  }

  @Override
  public double transformGrdient(double x) {
    return 1;
  }

}
