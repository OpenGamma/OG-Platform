/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculates the geometric mean of a series of data. 
 * <p>
 * The geometric mean {@latex.inline $\\mu$} of a series of elements {@latex.inline $x_1, x_2, \\dots, x_n$} is given by:
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{eqnarray*}
 * \\mu = \\left({\\prod\\limits_{i=1}^n x_i}\\right)^{\\frac{1}{n}}
 * \\end{eqnarray*}}
 * 
 */
public class GeometricMeanCalculator extends Function1D<double[], Double> {

  /**
   * @param x The array of data
   * @return The geometric mean
   * @throws IllegalArgumentException If the array is null or empty
   */
  @Override
  public Double evaluate(final double[] x) {
    Validate.notNull(x, "x");
    ArgumentChecker.notEmpty(x, "x");
    final int n = x.length;
    double mult = x[0];
    for (int i = 1; i < n; i++) {
      mult *= x[i];
    }
    return Math.pow(mult, 1. / n);
  }

}
