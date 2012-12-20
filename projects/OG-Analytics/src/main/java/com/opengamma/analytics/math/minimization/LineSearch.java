/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.minimization;

import static com.opengamma.analytics.math.matrix.MatrixAlgebraFactory.OG_ALGEBRA;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;

/**
 * 
 */
public class LineSearch {
  private final ScalarMinimizer _minimizer;
  private final MinimumBracketer _bracketer;

  public LineSearch(final ScalarMinimizer minimizer) {
    Validate.notNull(minimizer, "null minimizer");
    _minimizer = minimizer;
    _bracketer = new ParabolicMinimumBracketer();
  }

  public double minimise(final Function1D<DoubleMatrix1D, Double> function, final DoubleMatrix1D direction, final DoubleMatrix1D x) {
    Validate.notNull(function, "function");
    Validate.notNull(direction, "direction");
    Validate.notNull(x, "x");
    final LineSearchFunction f = new LineSearchFunction(function, direction, x);

    final double[] bracketPoints = _bracketer.getBracketedPoints(f, 0, 1);
    if (bracketPoints[2] < bracketPoints[0]) {
      final double temp = bracketPoints[0];
      bracketPoints[0] = bracketPoints[2];
      bracketPoints[2] = temp;
    }
    return _minimizer.minimize(f, bracketPoints[1], bracketPoints[0], bracketPoints[2]);
  }

  private static class LineSearchFunction extends Function1D<Double, Double> {
    private final Function1D<DoubleMatrix1D, Double> _f;
    private final DoubleMatrix1D _p;
    private final DoubleMatrix1D _x0;

    public LineSearchFunction(final Function1D<DoubleMatrix1D, Double> function, final DoubleMatrix1D direction, final DoubleMatrix1D x) {
      _f = function;
      _p = direction;
      _x0 = x;
    }

    @Override
    public Double evaluate(final Double lambda) {
      final DoubleMatrix1D x = (DoubleMatrix1D) OG_ALGEBRA.add(_x0, OG_ALGEBRA.scale(_p, lambda));
      return _f.evaluate(x);
    }

  }
}
