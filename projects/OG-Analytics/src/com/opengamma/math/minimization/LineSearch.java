/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import static com.opengamma.math.matrix.MatrixAlgebraFactory.OG_ALGEBRA;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class LineSearch {
  private final ScalarMinimizer _minimizer;
  private final MinimumBracketer _bracketer;

  public LineSearch(final ScalarMinimizer minimizer) {
    ArgumentChecker.notNull(minimizer, "null minimizer");
    _minimizer = minimizer;
    _bracketer = new ParabolicMinimumBracketer();
  }

  public double minimise(final Function1D<DoubleMatrix1D, Double> function, final DoubleMatrix1D direction, final DoubleMatrix1D x) {

    final LineSearchHelper f = new LineSearchHelper(function, direction, x);

    final double[] bracketPoints = _bracketer.getBracketedPoints(f, 0, 1.0);
    if (bracketPoints[2] < bracketPoints[0]) {
      final double temp = bracketPoints[0];
      bracketPoints[0] = bracketPoints[2];
      bracketPoints[2] = temp;
    }
    return _minimizer.minimize(f, bracketPoints[1], bracketPoints[0], bracketPoints[2]);
  }

  private static class LineSearchHelper extends Function1D<Double, Double> {
    private final Function1D<DoubleMatrix1D, Double> _f;
    private final DoubleMatrix1D _p;
    private final DoubleMatrix1D _x0;

    public LineSearchHelper(final Function1D<DoubleMatrix1D, Double> function, final DoubleMatrix1D direction, final DoubleMatrix1D x) {
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
