/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import com.opengamma.util.ArgumentChecker;

/**
 * A function to allow a smooth weighing between two functions. If two functions f(x) and g(x) fit the data set (x_i,y_i) at the points x_a and x_b
 * (i.e. f(x_a) = g(x_a) = y_a and  f(x_b) = g(x_b) = y_b), then a weighted function h(x) = w(x)f(x) + (1-w(x))*g(x) with 0 <= w(x) <= 1 will also
 * fit the points a and b
 * TODO this belongs with interpolator
 */
public abstract class WeightingFunction {

  /**
   * Get the function weight for point x
   * @param xs  All the independent data points
   * @param x An arbitrary point
   * @return The weight
   */
  public double getWeight(final double[] xs, final double x) {
    ArgumentChecker.notNull(xs, "strikes");
    final int index = SurfaceArrayUtils.getLowerBoundIndex(xs, x);
    final double y = getY(xs, index, x);
    return getWeight(y);
  }

  /**
   * Get the function weight for point x  - use this if index in known
   * @param xs  All the independent data points
   * @param index The index of the data point below x
   * @param x An arbitrary point
   * @return The weight
   */
  public double getWeight(final double[] xs, final int index, final double x) {
    ArgumentChecker.notNull(xs, "strikes");
    final double y = getY(xs, index, x);
    return getWeight(y);
  }

  /**
   * For an arbitrary point x, let the two data points immediately below and above x be, x_a and x_b, then define y = (x_b - x)/(x_b - x_a).
   * @param xs All the independent data points
   * @param lowerBoundIndex The index of x_a
   * @param x An arbitrary point
   * @return y
   */
  private double getY(double[] xs, int lowerBoundIndex, double x) {
    ArgumentChecker.notNull(xs, "strikes");
    ArgumentChecker.notNegative(lowerBoundIndex, "index");
    ArgumentChecker.isTrue(lowerBoundIndex <= xs.length - 2, "index cannot be larger than {}, have {}", xs.length - 2, lowerBoundIndex);
    return (xs[lowerBoundIndex + 1] - x) / (xs[lowerBoundIndex + 1] - xs[lowerBoundIndex]);
  }

  /**
   * The condition that must be satisfied by all weight functions is that w(1) = 1, w(0) = 0 and dw(y)/dy <= 0 - i.e. w(y) is monotonically decreasing
   * @param y a value between 0 and 1
   * @return The weight
   */
  public abstract double getWeight(double y);

}
