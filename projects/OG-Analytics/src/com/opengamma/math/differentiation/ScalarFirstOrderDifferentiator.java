/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.differentiation;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;

/**
* Differentiates a scalar function with respect to its argument using finite difference. For a function y =f(x) 
* where x and y are scalars this produces the gradient function, g(x), i.e. a function that returns the gradient for each point x,
* where g is the scalar dy/dx
*/
public class ScalarFirstOrderDifferentiator implements Derivative<Double, Double, Double> {

  private static final double DEFAULT_EPS = 1e-5;
  private static final double MIN_EPS = Math.sqrt(Double.MIN_NORMAL);

  private static final FiniteDifferenceType DIFF_TYPE = FiniteDifferenceType.CENTRAL;

  private final double _eps;
  private final double _twoEps;
  private final FiniteDifferenceType _differenceType;

  public ScalarFirstOrderDifferentiator() {
    this(DIFF_TYPE, DEFAULT_EPS);
  }

  public ScalarFirstOrderDifferentiator(final FiniteDifferenceType differenceType) {
    this(differenceType, DEFAULT_EPS);
  }

  public ScalarFirstOrderDifferentiator(final FiniteDifferenceType differenceType, final double eps) {
    Validate.notNull(differenceType);
    if (eps < MIN_EPS) {
      throw new IllegalArgumentException("eps is too small. A good value is 1e-5*size of domain. The minimum value is "
          + MIN_EPS);
    }
    _differenceType = differenceType;
    _eps = eps;
    _twoEps = 2 * _eps;
  }

  @Override
  public Function1D<Double, Double> derivative(final Function1D<Double, Double> function) {
    Validate.notNull(function);
    switch (_differenceType) {
      case FORWARD:
        return new Function1D<Double, Double>() {

          @SuppressWarnings("synthetic-access")
          @Override
          public Double evaluate(final Double x) {
            return (function.evaluate(x + _eps) - function.evaluate(x)) / _eps;
          }
        };
      case CENTRAL:
        return new Function1D<Double, Double>() {

          @SuppressWarnings("synthetic-access")
          @Override
          public Double evaluate(final Double x) {
            return (function.evaluate(x + _eps) - function.evaluate(x - _eps)) / _twoEps;
          }
        };
      case BACKWARD:
        return new Function1D<Double, Double>() {

          @SuppressWarnings("synthetic-access")
          @Override
          public Double evaluate(final Double x) {
            return (function.evaluate(x) - function.evaluate(x - _eps)) / _eps;
          }
        };
    }
    throw new IllegalArgumentException("Can only handle forward, backward and central differencing");
  }
}
