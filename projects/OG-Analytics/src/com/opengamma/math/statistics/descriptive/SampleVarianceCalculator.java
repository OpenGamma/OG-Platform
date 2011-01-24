/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;

/**
 * Calculates the sample variance of a series of data. 
 * <p> 
 * The unbiased sample variance {@latex.inline $VAR$} of a series {@latex.inline $x_1, x_2, \\dots, x_n$} is given by:
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{eqnarray*}
 * VAR = \\frac{1}{n-1}\\sum\\limits_{i=1}^{n}(x_i - \\overline{x})^2
 * \\end{eqnarray*}}
 * where {@latex.inline $\\overline{x}$} is the sample mean. For the population variance, see {@link PopulationVarianceCalculator}.
 */
public class SampleVarianceCalculator extends Function1D<double[], Double> {
  private final Function1D<double[], Double> _meanCalculator = new MeanCalculator();

  /**
   * @param x The array of data
   * @return The sample variance
   * @throws IllegalArgumentException If the array is null or contains fewer than two elements
   */
  @Override
  public Double evaluate(final double[] x) {
    Validate.notNull(x, "x");
    if (x.length < 2) {
      throw new IllegalArgumentException("Need at least two points to calculate the sample variance");
    }
    final Double mean = _meanCalculator.evaluate(x);
    double diff, sum = 0;
    for (final Double value : x) {
      diff = value - mean;
      sum += diff * diff;
    }
    final int n = x.length;
    return sum / (n - 1);
  }

}
