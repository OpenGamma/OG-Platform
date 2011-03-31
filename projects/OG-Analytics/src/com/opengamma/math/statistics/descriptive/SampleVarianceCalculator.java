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
 * The unbiased sample variance {@latex.inline $\\mathrm{var}$} of a series {@latex.inline $x_1, x_2, \\dots, x_n$} is given by:
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{align*}
 * \\text{var} = \\frac{1}{n-1}\\sum_{i=1}^{n}(x_i - \\overline{x})^2
 * \\end{align*}}
 * where {@latex.inline $\\overline{x}$} is the sample mean. For the population variance, see {@link PopulationVarianceCalculator}.
 */
public class SampleVarianceCalculator extends Function1D<double[], Double> {
  private static final Function1D<double[], Double> MEAN = new MeanCalculator();

  /**
   * @param x The array of data, not null, must contain at least two elements
   * @return The sample variance
   */
  @Override
  public Double evaluate(final double[] x) {
    Validate.notNull(x, "x");
    Validate.isTrue(x.length >= 2, "Need at least two points to calculate the sample variance");
    final Double mean = MEAN.evaluate(x);
    double sum = 0;
    for (final Double value : x) {
      final double diff = value - mean;
      sum += diff * diff;
    }
    final int n = x.length;
    return sum / (n - 1);
  }

}
