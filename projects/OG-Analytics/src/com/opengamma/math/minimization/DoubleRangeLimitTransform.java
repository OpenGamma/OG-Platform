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
 * If a model parameter {@latex.inline $x$} is constrained to be between two values {@latex.inline $a \\geq x \\geq b$}, the function to transform it to an unconstrained
 * variable is {@latex.inline $y$} is given by
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{align*}
 * y &= \\tanh^{-1}\\left(\\frac{x - m}{s}\\right)\\\\
 * m &= \\frac{a + b}{2}\\\\
 * s &= \\frac{b - a}{2}
 * \\end{align*}
 * }
 * with the inverse transform
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{align*}
 * x &= s\\tanh(y) + m\\\\
 * \\end{align*}
 * }
 */
public class DoubleRangeLimitTransform implements ParameterLimitsTransform {
  private static final double TANH_MAX = 25.0;
  private final double _lower;
  private final double _upper;
  private final double _scale;
  private final double _mid;

  /**
   * @param lower Lower limit
   * @param upper Upper limit
   * @throws IllegalArgumentException If the upper limit is not greater than the lower limit
   */
  public DoubleRangeLimitTransform(final double lower, final double upper) {
    Validate.isTrue(upper > lower, "upper limit must be greater than lower");
    _lower = lower;
    _upper = upper;
    _mid = (lower + upper) / 2;
    _scale = (upper - lower) / 2;
  }

  /**
   * If {@latex.inline $y > 25$}, this returns {@latex.inline $b$}. If {@latex.inline $y < -25$} returns {@latex.inline $a$}.
   * {@inheritDoc}
   */
  @Override
  public double inverseTransform(final double y) {
    if (y > TANH_MAX) {
      return _upper;
    } else if (y < -TANH_MAX) {
      return _lower;
    }
    return _mid + _scale * TrigonometricFunctionUtils.tanh(y);
  }

  /**
   * {@inheritDoc}
   * @throws IllegalArgumentException If {@latex.inline $x > b$} or {@latex.inline $x < a$}
   */
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

  /**
   * If {@latex.inline $|y| > 25$}, this returns 0.
   * {@inheritDoc}
   */
  @Override
  public double inverseTransformGradient(final double y) {
    if (y > TANH_MAX || y < -TANH_MAX) {
      return 0.0;
    }
    return _scale * (1 - FunctionUtils.square(TrigonometricFunctionUtils.tanh(y)));
  }

  /**
   * {@inheritDoc}
   * @throws IllegalArgumentException If {@latex.inline $x > b$} or {@latex.inline $x < a$}
   */
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
