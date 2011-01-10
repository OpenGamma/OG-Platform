/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculates the arithmetic mean of a series of data.
 * <p>
 * The arithmetic mean {@latex.inline $\\mu$} of a series of elements {@latex.inline $x_1, x_2, \\dots, x_n$} is given by:
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{eqnarray*}
 * \\mu = \\frac{1}{n}\\left({\\sum\\limits_{i=1}^n x_i}\\right)
 * \\end{eqnarray*}} 
 */
public class MeanCalculator extends Function1D<double[], Double> {

  /**
   * @param x The array of data
   * @return The arithmetic mean
   * @throws IllegalArgumentException If the array is null or empty
   */
  @Override
  public Double evaluate(final double[] x) {
    Validate.notNull(x);
    ArgumentChecker.notEmpty(x, "x");
    if (x.length == 1) {
      return x[0];
    }
    double sum = 0;
    for (final Double d : x) {
      sum += d;
    }
    return sum / x.length;
  }

}
