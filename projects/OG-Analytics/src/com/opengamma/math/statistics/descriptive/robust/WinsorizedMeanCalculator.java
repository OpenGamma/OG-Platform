/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive.robust;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.descriptive.MeanCalculator;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class WinsorizedMeanCalculator extends Function1D<double[], Double> {
  private final double _gamma;
  private final Function1D<double[], Double> _meanCalculator = new MeanCalculator();

  public WinsorizedMeanCalculator(final double gamma) {
    if (!ArgumentChecker.isInRangeExclusive(0, 1, gamma)) {
      throw new IllegalArgumentException("Gamma must be between 0 and 1, have " + gamma);
    }
    _gamma = gamma > 0.5 ? 1 - gamma : gamma;
  }

  @Override
  public Double evaluate(final double[] x) {
    Validate.notNull(x, "x");
    ArgumentChecker.notEmpty(x, "x");
    final int length = x.length;
    final double[] winsorized = Arrays.copyOf(x, length);
    Arrays.sort(winsorized);
    final int value = (int) Math.round(length * _gamma);
    final double x1 = winsorized[value];
    final double x2 = winsorized[length - value - 1];
    for (int i = 0; i < value; i++) {
      winsorized[i] = x1;
      winsorized[length - 1 - i] = x2;
    }
    return _meanCalculator.evaluate(winsorized);
  }
}
