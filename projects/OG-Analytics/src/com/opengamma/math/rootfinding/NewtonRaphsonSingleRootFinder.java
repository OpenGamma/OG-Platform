/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import org.apache.commons.lang.Validate;

import com.opengamma.math.MathException;
import com.opengamma.math.function.DoubleFunction1D;
import com.opengamma.math.function.Function1D;

/**
 * Class for finding the real root of a function within a range of <it>x</it>-values using the one-dimensional version of Newton's method.
 * <p>
 * For a function {@latex.inline $f(x)$}, the Taylor series expansion is given by:
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{align*}
 * f(x + \\delta) \\approx f(x) + f'(x)\\delta + \\frac{f''(x)}{2}\\delta^2 + \\cdots
 * \\end{align*}
 * }
 * As delta approaches zero (and if the function is well-behaved), this gives 
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{align*}
 * \\delta = -\\frac{f(x)}{f'(x)}
 * \\end{align*}
 * }
 * when {@latex.inline $f(x + \\delta) = 0$.
 * <p>
 * There are several well-known problems with Newton's method, in particular when the range of values given includes a local
 * maximum or minimum. In this situation, the next iterative step can shoot off to {@latex.inline $\\pm\\infty$}. This implementation
 * currently does not attempt to correct for this: if the value of <it>x</it> goes beyond the initial range of values <it>x<sub>low</sub></it>
 * and <it>x<sub>high</sub></it>, an exception is thrown.
 * <p>
 * If the function that is provided does not override the {@link com.opengamma.math.function.DoubleFunction1D#derivative()} method, then 
 * the derivative is approximated using finite difference. This is undesirable for several reasons: (i) the extra function evaluations will lead
 * to slower convergence; and (ii) the choice of shift size is very important (too small and the result will be dominated by rounding errors, too large
 * and convergence will be even slower). Use of another root-finder is recommended in this case.
 */
/**
 * 
 */
/**
 * 
 */
public class NewtonRaphsonSingleRootFinder extends RealSingleRootFinder {
  private static final int MAX_ITER = 1000;
  private final double _accuracy;

  /**
   * Default constructor. Sets accuracy to 1e-12.
   */
  public NewtonRaphsonSingleRootFinder() {
    this(1e-12);
  }

  /**
   * Takes the accuracy of the root as a parameter - this is the maximum difference between the true root and the returned value that is allowed. 
   * If this is negative, then the absolute value is used.
   * @param accuracy The accuracy
   */
  public NewtonRaphsonSingleRootFinder(final double accuracy) {
    _accuracy = Math.abs(accuracy);
  }

  /**
   * {@inheritDoc}
   * @throws MathException If the root is not found in 1000 attempts; if the Newton step takes the estimate for the root outside the original bounds.
   */
  @Override
  public Double getRoot(final Function1D<Double, Double> function, final Double x1, final Double x2) {
    Validate.notNull(function, "function");
    Validate.notNull(x1, "x1");
    Validate.notNull(x2, "x2");
    final DoubleFunction1D f = DoubleFunction1D.from(function);
    return getRoot(f, f.derivative(), x1, x2);
  }

  /**
   * Uses the {@link com.opengamma.math.function.DoubleFunction1D#derivative()} method. <it>x<sub>1</sub></it> and <it>x<sub>2</sub></it> do not have to be increasing.
   * @param function The function, not null
   * @param x1 The first bound of the root, not null
   * @param x2 The second bound of the root, not null
   * @return The root
   * @throws MathException If the root is not found in 1000 attempts; if the Newton step takes the estimate for the root outside the original bounds.
   */
  public Double getRoot(final DoubleFunction1D function, final Double x1, final Double x2) {
    Validate.notNull(function, "function");
    Validate.notNull(x1, "x1");
    Validate.notNull(x2, "x2");
    return getRoot(function, function.derivative(), x1, x2);
  }

  /**
   * Uses the function and its derivative. 
   * @param function The function, not null
   * @param derivative The derivative, not null
   * @param x1 The first bound of the root, not null
   * @param x2 The second bound of the root, not null
   * @return The root
   * @throws MathException If the root is not found in 1000 attempts; if the Newton step takes the estimate for the root outside the original bounds.
   */
  public Double getRoot(final Function1D<Double, Double> function, final Function1D<Double, Double> derivative, final Double x1, final Double x2) {
    Validate.notNull(function, "function");
    Validate.notNull(derivative, "derivative");
    Validate.notNull(x1, "x1");
    Validate.notNull(x2, "x2");
    return getRoot(DoubleFunction1D.from(function), DoubleFunction1D.from(derivative), x1, x2);
  }

  /**
   * Uses the function and its derivative.
   * @param function The function, not null
   * @param derivative The derivative, not null
   * @param x1 The first bound of the root, not null
   * @param x2 The second bound of the root, not null
   * @return The root
   * @throws MathException If the root is not found in 1000 attempts; if the Newton step takes the estimate for the root outside the original bounds.
   */
  public Double getRoot(final DoubleFunction1D function, final DoubleFunction1D derivative, final Double x1, final Double x2) {
    Validate.notNull(function);
    Validate.notNull(derivative, "derivative function");
    Validate.notNull(x1);
    Validate.notNull(x2);
    double y = function.evaluate(x1);
    if (Math.abs(y) < _accuracy) {
      return x1;
    }
    y = function.evaluate(x2);
    if (Math.abs(y) < _accuracy) {
      return x2;
    }

    double x = (x1 + x2) / 2;
    final double xLower = x1 > x2 ? x2 : x1;
    final double xUpper = x1 > x2 ? x1 : x2;
    for (int i = 0; i < MAX_ITER; i++) {
      final double newX = x - (function.evaluate(x) / derivative.evaluate(x));
      if (newX < xLower || newX > xUpper) {
        throw new MathException("Step has taken x outside original bounds");
      }
      if (Math.abs(newX - x) <= _accuracy) {
        return newX;
      }
      x = newX;
    }
    throw new MathException("Could not find root in " + MAX_ITER + " attempts");
  }
}
