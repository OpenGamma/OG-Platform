/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
  private final Interpolator1DNodeSensitivityCalculator<T> _sensitivityCalculator;
  private final Interpolator1DNodeSensitivityCalculator<T> _leftSensitivityCalculator;
  private final Interpolator1DNodeSensitivityCalculator<T> _rightSensitivityCalculator;

  public CombinedInterpolatorExtrapolatorNodeSensitivityCalculator(final Interpolator1DNodeSensitivityCalculator<T> sensitivityCalculator) {
    Validate.notNull(sensitivityCalculator, "sensitivity calculator");
    _sensitivityCalculator = sensitivityCalculator;
    _leftSensitivityCalculator = null;
    _rightSensitivityCalculator = null;
  }

  public CombinedInterpolatorExtrapolatorNodeSensitivityCalculator(final Interpolator1DNodeSensitivityCalculator<T> sensitivityCalculator,
      final Interpolator1DNodeSensitivityCalculator<T> leftAndRightSensitivityCalculator) {
    Validate.notNull(sensitivityCalculator, "sensitivity calculator");
    Validate.notNull(leftAndRightSensitivityCalculator, "left and right sensitivity calculators");
    _sensitivityCalculator = sensitivityCalculator;
    _leftSensitivityCalculator = leftAndRightSensitivityCalculator;
    _rightSensitivityCalculator = leftAndRightSensitivityCalculator;
  }

  public CombinedInterpolatorExtrapolatorNodeSensitivityCalculator(final Interpolator1DNodeSensitivityCalculator<T> sensitivityCalculator,
      final Interpolator1DNodeSensitivityCalculator<T> leftSensitivityCalculator, final Interpolator1DNodeSensitivityCalculator<T> rightSensitivityCalculator) {
    Validate.notNull(sensitivityCalculator, "sensitivity calculator");
    Validate.notNull(leftSensitivityCalculator, "left sensitivity calculator");
    Validate.notNull(rightSensitivityCalculator, "right sensitivity calculator");
    _sensitivityCalculator = sensitivityCalculator;
    _leftSensitivityCalculator = leftSensitivityCalculator;
    _rightSensitivityCalculator = rightSensitivityCalculator;
  }

  @Override
  public double[] calculate(final T data, final double value) {
    Validate.notNull(data, "data");
    Validate.notNull(value, "value");
    if (value < data.firstKey()) {
      if (_leftSensitivityCalculator != null) {
        return _leftSensitivityCalculator.calculate(data, value);
      }
    } else if (value > data.lastKey()) {
      if (_rightSensitivityCalculator != null) {
        return _rightSensitivityCalculator.calculate(data, value);
      }
    }
    return _sensitivityCalculator.calculate(data, value);
  }

  public Interpolator1DNodeSensitivityCalculator<T> getSensitivityCalculator() {
    return _sensitivityCalculator;
  }

  public Interpolator1DNodeSensitivityCalculator<T> getLeftSensitivityCalculator() {
    return _leftSensitivityCalculator;
  }

  public Interpolator1DNodeSensitivityCalculator<T> getRightSensitivityCalculator() {
    return _rightSensitivityCalculator;
  }

}
