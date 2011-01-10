/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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
public class LinearExtrapolator1DNodeSensitivityCalculator<T extends Interpolator1DDataBundle> implements Interpolator1DNodeSensitivityCalculator<T> {
  private static final double EPS = 1e-6;
  private final Interpolator1DNodeSensitivityCalculator<T> _calculator;

  public LinearExtrapolator1DNodeSensitivityCalculator(final Interpolator1DNodeSensitivityCalculator<T> calculator) {
    Validate.notNull(calculator, "calculator");
    _calculator = calculator;
  }

  @Override
  public double[] calculate(final T data, final double value) {
    Validate.notNull(data, "data");
    if (value < data.firstKey()) {
      return getLeftSensitivities(data, value);
    } else if (value > data.lastKey()) {
      return getRightSensitivities(data, value);
    }
    throw new IllegalArgumentException("Value " + value + " was within data range");
  }

  private double[] getLeftSensitivities(final T data, final double value) {
    final double eps = EPS * (data.lastKey() - data.firstKey());
    final double x = data.firstKey();
    final double[] result = _calculator.calculate(data, x + eps);
    final int n = result.length;
    for (int i = 1; i < n; i++) {
      result[i] = result[i] * (value - x) / eps;
    }
    result[0] = 1 + (result[0] - 1) * (value - x) / eps;
    return result;
  }

  private double[] getRightSensitivities(final T data, final Double value) {
    final double eps = EPS * (data.lastKey() - data.firstKey());
    final double x = data.lastKey();
    final double[] result = _calculator.calculate(data, x - eps);
    final int n = result.length;
    for (int i = 0; i < n - 1; i++) {
      result[i] = -result[i] * (value - x) / eps;
    }
    result[n - 1] = 1 + (1 - result[n - 1]) * (value - x) / eps;
    return result;
  }
}
