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
  public double inverseTransform(final double y) {
    return y;
  }

  @Override
  public double inverseTransformGradient(final double y) {
    return 1;
  }

  @Override
  public double transform(final double x) {
    return x;
  }

  @Override
  public double transformGradient(final double x) {
    return 1;
  }

  @Override
  public int hashCode() {
    return 37;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    return getClass() == obj.getClass();
  }

}
