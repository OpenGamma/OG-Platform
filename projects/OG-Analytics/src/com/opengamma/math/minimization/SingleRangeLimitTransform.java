/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class SingleRangeLimitTransform implements ParameterLimitsTransform {

  private static final double EXP_MAX = 50.;
  private final double _limit;
  private final int _sign;

  /**
   * If a model parameter,<i>x</i>, is constrained to be either above or below some level <i>a</i> (but not both), then this will transform it to an unconstrained variable <i>y</i>.
   * The transformation is <i>x = a + ln(e^y + 1)</i> for <i>x > a</i> and <i>x = a - ln(e^y + 1)</i> for <i>x < a</i>. For large y (>50) this becomes <i>x = a +/- y</i>, so any value of
   *  <i>y</i> will give a value of <i>x</i>
   * @param a The limit level 
   * @param limitType Type of the limit for the parameter
   */
  public SingleRangeLimitTransform(final double a, final LimitType limitType) {
    _limit = a;
    _sign = limitType == LimitType.GREATER_THAN ? 1 : -1;
  }

  @Override
  public double inverseTransform(final double y) {
    if (y > EXP_MAX) {
      return _limit + _sign * y;
    } else if (y < -EXP_MAX) {
      return _limit;
    }
    return _limit + _sign * Math.log(Math.exp(y) + 1);
  }

  @Override
  public double transform(final double x) {
    Validate.isTrue(_sign * x >= _sign * _limit, "x not in limit");
    if (x == _limit) {
      return -EXP_MAX;
    }
    final double r = _sign * (x - _limit);
    if (r > EXP_MAX) {
      return r;
    }
    return Math.log(Math.exp(r) - 1);
  }

  @Override
  public double inverseTransformGradient(final double y) {
    if (y > EXP_MAX) {
      return _sign;
    }
    final double temp = Math.exp(y);
    return _sign * temp / (temp + 1);
  }

  @Override
  public double transformGradient(final double x) {
    Validate.isTrue(_sign * x >= _sign * _limit, "x not in limit");
    final double r = _sign * (x - _limit);
    if (r > EXP_MAX) {
      return 1.0;
    }
    final double temp = Math.exp(r);
    return _sign * temp / (temp - 1);
  }

}
