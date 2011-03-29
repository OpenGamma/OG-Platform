/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;

/**
 * Given a series of data {@latex.inline $x_1, x_2, \\dots, x_n$} with mean {@latex.inline $\\overline{x}$}, mode {@latex.inline $m$}
 * and standard deviation {@latex.inline $\\sigma$}, the Pearson first skewness coefficient is given by
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{align*}
 * \\text{skewness} = \\frac{3(\\overline{x} - m)}{\\sigma}
 * \\end{align*}
 * }
 * @see MeanCalculator
 * @see ModeCalculator
 * @see SampleStandardDeviationCalculator
 */
public class PearsonFirstSkewnessCoefficientCalculator extends Function1D<double[], Double> {
  private static final Function1D<double[], Double> MEAN = new MeanCalculator();
  private static final Function1D<double[], Double> MODE = new ModeCalculator();
  private static final Function1D<double[], Double> STD_DEV = new SampleStandardDeviationCalculator();

  /**
   * @param x The array of data, not null. Must contain at least two data points
   * @return The Pearson first skewness coefficient 
   */
  @Override
  public Double evaluate(final double[] x) {
    Validate.notNull(x);
    Validate.isTrue(x.length > 1, "Need at least two data points to calculate Pearson first skewness coefficient");
    return 3 * (MEAN.evaluate(x) - MODE.evaluate(x)) / STD_DEV.evaluate(x);
  }

}
