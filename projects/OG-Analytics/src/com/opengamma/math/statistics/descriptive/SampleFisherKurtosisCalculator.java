/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;

/**
 * The sample Fisher kurtosis gives a measure of how heavy the tails of a distribution are with respect to the normal distribution (which
 * has a Fisher kurtosis of zero). An estimator of the kurtosis is
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{align*}
 * \\mu_4 = \\frac{(n+1)n}{(n-1)(n-2)(n-3)}\\frac{\\sum_{i=1}^n (x_i - \\overline{x})^4}{\\mu_2^2} - 3\\frac{(n-1)^2}{(n-2)(n-3)}
 * \\end{align*}
 * }
 * where {@latex.inline $\\overline{x}$} is the sample mean and {@latex.inline $\\mu_2$} is the unbiased estimator of the population variance.
 */
public class SampleFisherKurtosisCalculator extends Function1D<double[], Double> {
  private static final Function1D<double[], Double> MEAN = new MeanCalculator();

  @Override
  public Double evaluate(final double[] x) {
    Validate.notNull(x, "x");
    Validate.isTrue(x.length >= 4, "Need at least four points to calculate kurtosis");
    double sum = 0;
    final double mean = MEAN.evaluate(x);
    double variance = 0;
    for (final Double d : x) {
      final double diff = d - mean;
      final double diffSq = diff * diff;
      variance += diffSq;
      sum += diffSq * diffSq;
    }
    final int n = x.length;
    variance /= n - 1;
    return n * (n + 1.) * sum / ((n - 1.) * (n - 2.) * (n - 3.) * variance * variance) - 3 * (n - 1.) * (n - 1.) / ((n - 2.) * (n - 3.));
  }
}
