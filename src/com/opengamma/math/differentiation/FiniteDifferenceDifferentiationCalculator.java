/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.differentiation;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;

/**
 * 
 */

public class FiniteDifferenceDifferentiationCalculator extends Function1D<Function1D<Double, Double>, Function1D<Double, Double>> {
  /**
   * Differencing type:
   * <p>
   * Forward: [f(x + eps) - f(x)] / eps
   * <p>
   * Central: [f(x + eps) - f(x - eps)] / (2 * eps)
   * <p>
   * Backward: [f(x) - f(x - eps)] / eps
   */
  public enum DifferenceType {
    /**
     * Forward differencing
     */
    FORWARD,
    /**
     * Central differencing
     */
    CENTRAL,
    /**
     * Backward differencing
     */
    BACKWARD
  }

  private final double _eps;
  private final double _twoEps;
  private final DifferenceType _differenceType;

  public FiniteDifferenceDifferentiationCalculator(final DifferenceType differenceType) {
    Validate.notNull(differenceType);
    _differenceType = differenceType;
    _eps = 1e-9;
    _twoEps = 2e-9;
  }

  public FiniteDifferenceDifferentiationCalculator(final DifferenceType differenceType, final double eps) {
    Validate.notNull(differenceType);
    _differenceType = differenceType;
    _eps = eps;
    _twoEps = 2 * _eps;
  }

  @Override
  public Function1D<Double, Double> evaluate(final Function1D<Double, Double> x) {
    Validate.notNull(x);
    switch (_differenceType) {
      case FORWARD:
        return new Function1D<Double, Double>() {

          @SuppressWarnings("synthetic-access")
          @Override
          public Double evaluate(final Double d) {
            return (x.evaluate(d + _eps) - x.evaluate(d)) / _eps;
          }
        };
      case CENTRAL:
        return new Function1D<Double, Double>() {

          @SuppressWarnings("synthetic-access")
          @Override
          public Double evaluate(final Double d) {
            return (x.evaluate(d + _eps) - x.evaluate(d - _eps)) / _twoEps;
          }
        };
      case BACKWARD:
        return new Function1D<Double, Double>() {

          @SuppressWarnings("synthetic-access")
          @Override
          public Double evaluate(final Double d) {
            return (x.evaluate(d) - x.evaluate(d - _eps)) / _eps;
          }
        };
    }
    throw new IllegalArgumentException("Can only handle forward, backward and central differencing");
  }
}
