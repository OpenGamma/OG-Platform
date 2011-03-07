/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import org.apache.commons.lang.Validate;

import com.opengamma.math.FunctionUtils;
import com.opengamma.math.TrigonometricFunctionUtils;

/**
 * 
 */
public class DoubleRangeLimitTransform implements ParameterLimitsTransform {

  private static final double TANH_MAX = 25.0;
  private final double _lower;
  private final double _upper;
  private final double _scale;
  private final double _mid;

  /**
   * * If a model parameter,<i>x</i>, is constrained to be between limited <i>lower</i> and <i>upper</i> then this will transform it to an unconstrained variable <i>y</i> using a
   * tanh function
   * @param lower Limit
   * @param upper Limit
   */
  public DoubleRangeLimitTransform(final double lower, final double upper) {
    Validate.isTrue(upper > lower, "upper limit must be greater than lower");
    _lower = lower;
    _upper = upper;
    _mid = (lower + upper) / 2;
    _scale = (upper - lower) / 2;
  }

  @Override
  public double inverseTransform(final double y) {
    if (y > TANH_MAX) {
      return _upper;
    } else if (y < -TANH_MAX) {
      return _lower;
    }
    return _mid + _scale * TrigonometricFunctionUtils.tanh(y);
  }

  @Override
  public double transform(final double x) {
    Validate.isTrue(x <= _upper && x >= _lower, "parameter out of range");
    if (x == _upper) {
      return TANH_MAX;
    } else if (x == _lower) {
      return -TANH_MAX;
    }
    return TrigonometricFunctionUtils.atanh((x - _mid) / _scale);
  }

  @Override
  public double inverseTransformGradient(final double y) {
    if (y > TANH_MAX || y < -TANH_MAX) {
      return 0.0;
    }
    return _scale * (1 - FunctionUtils.square(TrigonometricFunctionUtils.tanh(y)));
  }

  @Override
  public double transformGradient(final double x) {
    Validate.isTrue(x <= _upper && x >= _lower, "parameter out of range");
    final double t = (x - _mid) / _scale;
    return 1 / (_scale * (1 - t * t));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_lower);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_upper);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final DoubleRangeLimitTransform other = (DoubleRangeLimitTransform) obj;
    if (Double.doubleToLongBits(_lower) != Double.doubleToLongBits(other._lower)) {
      return false;
    }
    return Double.doubleToLongBits(_upper) == Double.doubleToLongBits(other._upper);
  }

}
