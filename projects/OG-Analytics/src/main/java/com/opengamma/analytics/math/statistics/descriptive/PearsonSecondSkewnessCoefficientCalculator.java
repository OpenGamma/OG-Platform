/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;

/**
 * Given a series of data $x_1, x_2, \dots, x_n$ with mean $\overline{x}$, median $m$
 * and standard deviation $\sigma$, the Pearson second skewness coefficient is given by
 * $$
 * \begin{align*}
 * \text{skewness} = \frac{3(\overline{x} - m)}{\sigma}
 * \end{align*}
 * $$
 * @see MeanCalculator
 * @see MedianCalculator
 * @see SampleStandardDeviationCalculator
 */
public class PearsonSecondSkewnessCoefficientCalculator extends Function1D<double[], Double> {
  private final Function1D<double[], Double> _mean = new MeanCalculator();
  private final Function1D<double[], Double> _median = new MedianCalculator();
  private final Function1D<double[], Double> _stdDev = new SampleStandardDeviationCalculator();

  /**
   * @param x The array of data, not null. Must contain at least two data points
   * @return The Pearson second skewness coefficient 
   */
  @Override
  public Double evaluate(final double[] x) {
    Validate.notNull(x);
    Validate.isTrue(x.length > 1, "Need at least two data points to calculate Pearson first skewness coefficient");
    return 3 * (_mean.evaluate(x) - _median.evaluate(x)) / _stdDev.evaluate(x);
  }

}
