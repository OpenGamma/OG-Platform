/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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
  private final Minimizer1D _minimizer;

  public LineSearch(final Minimizer1D minimizer) {
    ArgumentChecker.notNull(minimizer, "null minimizer");
    _minimizer = minimizer;
  }

  public double minimise(final Function1D<DoubleMatrix1D, Double> function, final DoubleMatrix1D direction, final DoubleMatrix1D x) {

    LineSearchHelper f = new LineSearchHelper(function, direction, x);
    return _minimizer.minimize(f, 1.0, 0.0, 10.0);
  }

  private class LineSearchHelper extends Function1D<Double, Double> {
    private final Function1D<DoubleMatrix1D, Double> _f;
    private final DoubleMatrix1D _p;
    private final DoubleMatrix1D _x0;

    public LineSearchHelper(final Function1D<DoubleMatrix1D, Double> function, DoubleMatrix1D direction, DoubleMatrix1D x) {
      _f = function;
      _p = direction;
      _x0 = x;
    }

    @Override
    public Double evaluate(Double lambda) {
      DoubleMatrix1D x = (DoubleMatrix1D) OG_ALGEBRA.add(_x0, OG_ALGEBRA.scale(_p, lambda));
      return _f.evaluate(x);
    }

  }
}
