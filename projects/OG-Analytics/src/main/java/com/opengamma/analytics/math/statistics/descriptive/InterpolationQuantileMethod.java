/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import com.opengamma.util.ArgumentChecker;

/**
 * Implementation of a quantile estimator.
 * The quantile is linearly interpolated between two sample values. The probability dimension 
 * <i>p<subscript>i</subscript> on which the interpolation take place (X axis) varies between actual
 * implementation of the abstract class. For each probability <i>p<subscript>i</subscript></i>, the cumulative 
 * distribution value is the sample value with same index. 
 * The index used above are the Java index plus 1.
 * <p> Reference: Value-At-Risk, OpenGamma Documentation 31, Version 0.1, April 2015.
 */
public abstract class InterpolationQuantileMethod extends QuantileCalculationMethod {

  @Override
  public double quantileFromSorted(double level, double[] sortedSample) {
    ArgumentChecker.isTrue(level > 0, "Quantile should be above 0.");
    ArgumentChecker.isTrue(level < 1, "Quantile should be below 1.");
    int sampleSize = sortedSample.length;
    double adjustedLevel = level * sampleCorrection(sampleSize) + indexCorrection();
    int lowerIndex = (int) Math.floor(adjustedLevel);
    int upperIndex = (int) Math.ceil(adjustedLevel);
    double lowerWeight = upperIndex - adjustedLevel;
    double upperWeight = 1.0d - lowerWeight;
    return lowerWeight * sortedSample[lowerIndex - 1] + upperWeight * sortedSample[upperIndex - 1];
  }
  
  /**
   * Internal method returning the index correction for the specific implementation.
   * @return The correction.
   */
  abstract double indexCorrection();
  
  abstract int sampleCorrection(int sampleSize);

}
