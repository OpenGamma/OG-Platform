/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive.robust;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.descriptive.MeanCalculator;

/**
 * 
 */
public class WinsorizedMeanCalculator extends Function1D<double[], Double> {
  private static final Function1D<double[], Double> MEAN_CALCULATOR = new MeanCalculator();
  private final double _gamma;

  public WinsorizedMeanCalculator(final double gamma) {
    Validate.isTrue(gamma > 0 && gamma < 1, "Gamma must be between 0 and 1, have {}", gamma);
    _gamma = gamma > 0.5 ? 1 - gamma : gamma;
  }

  @Override
  public Double evaluate(final double[] x) {
    Validate.notNull(x, "x was null");
    final int length = x.length;
    Validate.isTrue(length > 0, "x was empty");
    final double[] winsorized = Arrays.copyOf(x, length);
    Arrays.sort(winsorized);
    final int value = (int) Math.round(length * _gamma);
    final double x1 = winsorized[value];
    final double x2 = winsorized[length - value - 1];
    for (int i = 0; i < value; i++) {
      winsorized[i] = x1;
      winsorized[length - 1 - i] = x2;
    }
    return MEAN_CALCULATOR.evaluate(winsorized);
  }
}
