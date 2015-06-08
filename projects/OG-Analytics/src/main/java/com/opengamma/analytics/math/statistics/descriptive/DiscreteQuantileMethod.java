/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import com.opengamma.util.ArgumentChecker;

/**
 * Implementation of a quantile estimator.
 * The estimation is one of the sorted sample data.
 * <p> Reference: Value-At-Risk, OpenGamma Documentation 31, Version 0.1, April 2015.
 */
public abstract class DiscreteQuantileMethod extends QuantileCalculationMethod {

  @Override
  public double quantileFromSorted(double level, double[] sortedSample) {
    ArgumentChecker.isTrue(level > 0, "Quantile should be above 0.");
    ArgumentChecker.isTrue(level < 1, "Quantile should be below 1.");
    int sampleSize = sortedSample.length;
    int index = index(level * sampleSize);
    return sortedSample[index - 1];
  }
  
  /**
   * Internal method computing the index for a give quantile multiply by sample size.
   * @param quantileSize The quantile * sample size.
   * @return The index in the sample.
   */
  abstract int index(double quantileSize);

}
