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
 * 
 */
public class NewtonRaphsonSingleRootFinder extends RealSingleRootFinder {
  private static final int MAX_ITER = 1000;
  private final double _accuracy;

  public NewtonRaphsonSingleRootFinder() {
    this(1e-12);
  }

  public NewtonRaphsonSingleRootFinder(final double accuracy) {
    _accuracy = Math.abs(accuracy);
  }

  @Override
  public Double getRoot(final Function1D<Double, Double> function, final Double x1, final Double x2) {
    Validate.notNull(function, "function");
    Validate.notNull(x1, "x1");
    Validate.notNull(x2, "x2");
    final DoubleFunction1D f = DoubleFunction1D.from(function);
    return getRoot(f, f.derivative(), x1, x2);
  }

  public Double getRoot(final Function1D<Double, Double> function, final Function1D<Double, Double> derivative, final Double x1, final Double x2) {
    Validate.notNull(function, "function");
    Validate.notNull(derivative, "derivative");
    Validate.notNull(x1, "x1");
    Validate.notNull(x2, "x2");
    return getRoot(DoubleFunction1D.from(function), DoubleFunction1D.from(derivative), x1, x2);
  }

  public Double getRoot(final DoubleFunction1D function, final DoubleFunction1D derivative, final Double x1, final Double x2) {
    Validate.notNull(function);
    Validate.notNull(derivative, "derivative function");
    Validate.notNull(x1);
    Validate.notNull(x2);
    double x = (x1 + x2) / 2;
    final double y1 = function.evaluate(x);
    if (Math.abs(y1) < _accuracy) {
      return x;
    }
    for (int i = 0; i < MAX_ITER; i++) {
      final double newX = x - (function.evaluate(x) / derivative.evaluate(x));
      if (newX < x1 || newX > x2) {
        throw new MathException("Step has taken x outside original bounds");
      }
      if (Math.abs(newX - x) <= _accuracy) {
        return newX;
      }
      x = newX;
    }
    throw new MathException("Could not find root in " + MAX_ITER + " attempts");
  }

  public Double getRoot(final DoubleFunction1D function, final Double x1, final Double x2) {
    return getRoot(function, function.derivative(), x1, x2);
  }
}
