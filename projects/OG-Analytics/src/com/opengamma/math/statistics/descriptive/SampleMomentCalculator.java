/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;

/**
 * Calculates the n<sup>th</sup> sample raw moment of a series of data.
 * <p>
 * The sample raw moment {@latex.inline $m_n$} of a series of data {@latex.inline $x_1, x_2, \\dots, x_s$} is given by:
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{align*}
 * m_n = \\frac{1}{s}\\sum_{i=1}^s x_i^n
 * \\end{align*}
 * }
 */
public class SampleMomentCalculator extends Function1D<double[], Double> {
  private final int _n;

  /**
   * @param n The degree of the moment to calculate, cannot be negative
   */
  public SampleMomentCalculator(final int n) {
    Validate.isTrue(n >= 0, "n must be >= 0");
    _n = n;
  }

  /**
   * @param x The array of data, not null or empty
   * @return The sample raw moment
   */
  @Override
  public Double evaluate(final double[] x) {
    Validate.notNull(x, "x was null");
    Validate.isTrue(x.length > 0, "x was empty");
    if (_n == 0) {
      return 1.;
    }
    double sum = 0;
    for (final Double d : x) {
      sum += Math.pow(d, _n);
    }
    return sum / (x.length - 1);
  }

}
