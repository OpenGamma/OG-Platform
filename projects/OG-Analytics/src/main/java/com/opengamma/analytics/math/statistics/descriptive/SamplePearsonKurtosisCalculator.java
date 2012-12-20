/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;

/**
 * The sample Pearson kurtosis gives a measure of how heavy the tails of a
 * distribution are with respect to the normal distribution (which has a
 * Pearson kurtosis of three). It is calculated using
 * $$
 * \begin{align*}
 * \text{Pearson kurtosis} = \text{Fisher kurtosis} + 3
 * \end{align*}
 * $$
 * where the Fisher kurtosis is calculated using {@link SampleFisherKurtosisCalculator}.
 */
public class SamplePearsonKurtosisCalculator extends Function1D<double[], Double> {
  private static final Function1D<double[], Double> KURTOSIS = new SampleFisherKurtosisCalculator();

  /**
   * @param x The array of data, not null. Must contain at least four data points.
   * @return The sample Pearson kurtosis
   */
  @Override
  public Double evaluate(final double[] x) {
    Validate.notNull(x, "x");
    Validate.isTrue(x.length >= 4, "Need at least four points to calculate kurtosis");
    return KURTOSIS.evaluate(x) + 3;
  }
}
