/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.function;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.analytics.math.interpolation.PiecewisePolynomialResultsWithSensitivity;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class PiecewisePolynomialWithSensitivityFunction1D extends PiecewisePolynomialFunction1D {

  private static final MatrixAlgebra MA = new OGMatrixAlgebra();

  public DoubleMatrix1D nodeSensitivity(final PiecewisePolynomialResultsWithSensitivity pp, final double xKey) {
    ArgumentChecker.notNull(pp, "null pp");
    ArgumentChecker.isFalse(Double.isNaN(xKey), "xKey containing NaN");
    ArgumentChecker.isFalse(Double.isInfinite(xKey), "xKey containing Infinity");

    if (pp.getDimensions() > 1) {
      throw new NotImplementedException();
    }

    final double[] knots = pp.getKnots().getData();
    final int nKnots = knots.length;
    int interval = FunctionUtils.getLowerBoundIndex(knots, xKey);
    if (interval == nKnots - 1) {
      interval--; // there is 1 less interval that knots
    }

    final double s = xKey - knots[interval];
    final DoubleMatrix2D a = pp.getCoefficientSensitivity(interval);
    final int nCoefs = a.getNumberOfRows();

    DoubleMatrix1D res = a.getRowVector(0);
    for (int i = 1; i < nCoefs; i++) {
      res = (DoubleMatrix1D) MA.scale(res, s);
      res = (DoubleMatrix1D) MA.add(res, a.getRowVector(i));
    }

    return res;
  }

}
