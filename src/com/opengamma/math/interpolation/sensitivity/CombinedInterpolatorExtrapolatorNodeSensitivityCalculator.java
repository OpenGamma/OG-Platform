/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation.sensitivity;

import org.apache.commons.lang.Validate;

import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 * @param <T>
 */
public class CombinedInterpolatorExtrapolatorNodeSensitivityCalculator<T extends Interpolator1DDataBundle> implements Interpolator1DNodeSensitivityCalculator<T> {
  private final Interpolator1DNodeSensitivityCalculator<T> _interpolator;
  private final Interpolator1DNodeSensitivityCalculator<T> _leftExtrapolator;
  private final Interpolator1DNodeSensitivityCalculator<T> _rightExtrapolator;

  public CombinedInterpolatorExtrapolatorNodeSensitivityCalculator(final Interpolator1DNodeSensitivityCalculator<T> interpolator, final Interpolator1DNodeSensitivityCalculator<T> leftExtrapolator,
      final Interpolator1DNodeSensitivityCalculator<T> rightExtrapolator) {
    _interpolator = interpolator;
    _leftExtrapolator = leftExtrapolator;
    _rightExtrapolator = rightExtrapolator;
  }

  @Override
  public double[] calculate(final T data, final double value) {
    Validate.notNull(data, "data");
    Validate.notNull(value, "value");
    if (value < data.firstKey()) {
      if (_leftExtrapolator != null) {
        return _leftExtrapolator.calculate(data, value);
      }
    } else if (value > data.lastKey()) {
      if (_rightExtrapolator != null) {
        return _rightExtrapolator.calculate(data, value);
      }
    }
    return _interpolator.calculate(data, value);
  }

}
