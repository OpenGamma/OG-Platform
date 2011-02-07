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
  public double inverseTransform(double y) {
    return y;
  }

  @Override
  public double inverseTransformGradient(double y) {
    return 1;
  }

  @Override
  public double transform(double x) {
    return x;
  }

  @Override
  public double transformGradient(double x) {
    return 1;
  }

}
