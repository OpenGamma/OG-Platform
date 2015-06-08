/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import java.util.Arrays;

/**
 * Abstract method to estimate quantiles from sample observations.
 */
public abstract class QuantileCalculationMethod {
  
  /**
   * Compute the quantile estimation.
   * @param level The quantile level. The number is in decimal, i.e. 99% = 0.99. 
   * The quantile should be 0 < quantile < 1.
   * @param sortedSample The sample observations. Sorted from the smallest to the largest.
   * @return The quantile estimation.
   */
  abstract double quantileFromSorted(double level, double[] sortedSample);
  
  /**
   * Compute the quantile estimation.
   * @param level The quantile level. The number is in decimal, i.e. 99% = 0.99. 
   * The quantile should be 0 < quantile < 1.
   * @param sample The sample observations. The sample is supposed to be unsorted, the first step is to sort the data.
   * @return The quantile estimation.
   */
  public double quantileFromUnsorted(double level, double[] sample) {
    double[] sorted = sample.clone();
    Arrays.sort(sorted);
    return quantileFromSorted(level, sorted);
  }

}
