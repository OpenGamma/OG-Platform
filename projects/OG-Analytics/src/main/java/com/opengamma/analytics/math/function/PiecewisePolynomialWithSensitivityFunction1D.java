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
 * Give a class {@link PiecewisePolynomialResultsWithSensitivity}, Compute node sensitivity of function value, first derivative value and second derivative value
 */
public class PiecewisePolynomialWithSensitivityFunction1D extends PiecewisePolynomialFunction1D {

  private static final MatrixAlgebra MA = new OGMatrixAlgebra();

  /** 
   * @param pp {@link PiecewisePolynomialResultsWithSensitivity}
   * @param xKey 
   * @return Node sensitivity value at x=xKey
   */
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
    return getSensitivity(a, s);
  }

  /** 
   * @param pp {@link PiecewisePolynomialResultsWithSensitivity}
   * @param xKeys 
   * @return Node sensitivity value at x=xKeys
   */
  public DoubleMatrix1D[] nodeSensitivity(final PiecewisePolynomialResultsWithSensitivity pp, final double[] xKeys) {
    ArgumentChecker.notNull(pp, "null pp");
    ArgumentChecker.notNull(xKeys, "null xKeys");
    final int nKeys = xKeys.length;
    final DoubleMatrix1D[] res = new DoubleMatrix1D[nKeys];

    for (int i = 0; i < nKeys; ++i) {
      ArgumentChecker.isFalse(Double.isNaN(xKeys[i]), "xKey containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(xKeys[i]), "xKey containing Infinity");
    }
    if (pp.getDimensions() > 1) {
      throw new NotImplementedException();
    }

    final double[] knots = pp.getKnots().getData();
    final int nKnots = knots.length;

    for (int j = 0; j < nKeys; ++j) {
      final double xKey = xKeys[j];
      int interval = FunctionUtils.getLowerBoundIndex(knots, xKey);
      if (interval == nKnots - 1) {
        interval--; // there is 1 less interval that knots
      }

      final double s = xKey - knots[interval];
      final DoubleMatrix2D a = pp.getCoefficientSensitivity(interval);
      res[j] = getSensitivity(a, s);
    }

    return res;
  }

  /** 
   * @param pp {@link PiecewisePolynomialResultsWithSensitivity}
   * @param xKey 
   * @return Node sensitivity of derivative value at x=xKey
   */
  public DoubleMatrix1D differentiateNodeSensitivity(final PiecewisePolynomialResultsWithSensitivity pp, final double xKey) {
    ArgumentChecker.notNull(pp, "null pp");
    ArgumentChecker.isFalse(Double.isNaN(xKey), "xKey containing NaN");
    ArgumentChecker.isFalse(Double.isInfinite(xKey), "xKey containing Infinity");

    if (pp.getDimensions() > 1) {
      throw new NotImplementedException();
    }
    final int nCoefs = pp.getOrder();
    ArgumentChecker.isFalse(nCoefs < 2, "Polynomial degree is too low");
    final int nIntervals = pp.getNumberOfIntervals();

    final DoubleMatrix2D[] diffSense = new DoubleMatrix2D[nIntervals];
    final DoubleMatrix2D[] senseMat = pp.getCoefficientSensitivityAll();
    final int nData = senseMat[0].getNumberOfColumns();
    for (int i = 0; i < nIntervals; ++i) {
      final double[][] tmp = new double[nCoefs - 1][nData];
      for (int j = 0; j < nCoefs - 1; ++j) {
        for (int k = 0; k < nData; ++k) {
          tmp[j][k] = (nCoefs - 1 - j) * senseMat[i].getData()[j][k];
        }
      }
      diffSense[i] = new DoubleMatrix2D(tmp);
    }

    PiecewisePolynomialResultsWithSensitivity ppDiff = new PiecewisePolynomialResultsWithSensitivity(pp.getKnots(),
        pp.getCoefMatrix(), nCoefs - 1, pp.getDimensions(), diffSense);
    return nodeSensitivity(ppDiff, xKey);
  }

  /** 
   * @param pp {@link PiecewisePolynomialResultsWithSensitivity}
   * @param xKeys 
   * @return Node sensitivity of derivative value at x=xKeys
   */
  public DoubleMatrix1D[] differentiateNodeSensitivity(final PiecewisePolynomialResultsWithSensitivity pp, final double[] xKeys) {
    ArgumentChecker.notNull(pp, "null pp");

    if (pp.getDimensions() > 1) {
      throw new NotImplementedException();
    }
    final int nCoefs = pp.getOrder();
    ArgumentChecker.isFalse(nCoefs < 2, "Polynomial degree is too low");
    final int nIntervals = pp.getNumberOfIntervals();

    final DoubleMatrix2D[] diffSense = new DoubleMatrix2D[nIntervals];
    final DoubleMatrix2D[] senseMat = pp.getCoefficientSensitivityAll();
    final int nData = senseMat[0].getNumberOfColumns();
    for (int i = 0; i < nIntervals; ++i) {
      final double[][] tmp = new double[nCoefs - 1][nData];
      for (int j = 0; j < nCoefs - 1; ++j) {
        for (int k = 0; k < nData; ++k) {
          tmp[j][k] = (nCoefs - 1 - j) * senseMat[i].getData()[j][k];
        }
      }
      diffSense[i] = new DoubleMatrix2D(tmp);
    }

    PiecewisePolynomialResultsWithSensitivity ppDiff = new PiecewisePolynomialResultsWithSensitivity(pp.getKnots(), pp.getCoefMatrix(), nCoefs - 1, pp.getDimensions(), diffSense);
    return nodeSensitivity(ppDiff, xKeys);
  }

  /** 
   * @param pp {@link PiecewisePolynomialResultsWithSensitivity}
   * @param xKey 
   * @return Node sensitivity of second derivative value at x=xKey
   */
  public DoubleMatrix1D differentiateTwiceNodeSensitivity(final PiecewisePolynomialResultsWithSensitivity pp, final double xKey) {
    ArgumentChecker.notNull(pp, "null pp");
    ArgumentChecker.isFalse(Double.isNaN(xKey), "xKey containing NaN");
    ArgumentChecker.isFalse(Double.isInfinite(xKey), "xKey containing Infinity");

    if (pp.getDimensions() > 1) {
      throw new NotImplementedException();
    }
    final int nCoefs = pp.getOrder();
    ArgumentChecker.isFalse(nCoefs < 3, "Polynomial degree is too low");
    final int nIntervals = pp.getNumberOfIntervals();

    final DoubleMatrix2D[] diffSense = new DoubleMatrix2D[nIntervals];
    final DoubleMatrix2D[] senseMat = pp.getCoefficientSensitivityAll();
    final int nData = senseMat[0].getNumberOfColumns();
    for (int i = 0; i < nIntervals; ++i) {
      final double[][] tmp = new double[nCoefs - 2][nData];
      for (int j = 0; j < nCoefs - 2; ++j) {
        for (int k = 0; k < nData; ++k) {
          tmp[j][k] = (nCoefs - 1 - j) * (nCoefs - 2 - j) * senseMat[i].getData()[j][k];
        }
      }
      diffSense[i] = new DoubleMatrix2D(tmp);
    }

    PiecewisePolynomialResultsWithSensitivity ppDiff = new PiecewisePolynomialResultsWithSensitivity(pp.getKnots(),
        pp.getCoefMatrix(), nCoefs - 2, pp.getDimensions(), diffSense);
    return nodeSensitivity(ppDiff, xKey);
  }

  /** 
   * @param pp {@link PiecewisePolynomialResultsWithSensitivity}
   * @param xKeys 
   * @return Node sensitivity of second derivative value at x=xKeys
   */
  public DoubleMatrix1D[] differentiateTwiceNodeSensitivity(final PiecewisePolynomialResultsWithSensitivity pp, final double[] xKeys) {
    ArgumentChecker.notNull(pp, "null pp");

    if (pp.getDimensions() > 1) {
      throw new NotImplementedException();
    }
    final int nCoefs = pp.getOrder();
    ArgumentChecker.isFalse(nCoefs < 3, "Polynomial degree is too low");
    final int nIntervals = pp.getNumberOfIntervals();

    final DoubleMatrix2D[] diffSense = new DoubleMatrix2D[nIntervals];
    final DoubleMatrix2D[] senseMat = pp.getCoefficientSensitivityAll();
    final int nData = senseMat[0].getNumberOfColumns();
    for (int i = 0; i < nIntervals; ++i) {
      final double[][] tmp = new double[nCoefs - 2][nData];
      for (int j = 0; j < nCoefs - 2; ++j) {
        for (int k = 0; k < nData; ++k) {
          tmp[j][k] = (nCoefs - 1 - j) * (nCoefs - 2 - j) * senseMat[i].getData()[j][k];
        }
      }
      diffSense[i] = new DoubleMatrix2D(tmp);
    }

    PiecewisePolynomialResultsWithSensitivity ppDiff = new PiecewisePolynomialResultsWithSensitivity(pp.getKnots(), pp.getCoefMatrix(), nCoefs - 2, pp.getDimensions(), diffSense);
    return nodeSensitivity(ppDiff, xKeys);
  }

  /**
   * Compute sensitivity from sensitivity coefficients for the interval
   * @param coefficientSensitivity Coefficients for sensitivity function
   * @param sValue (key value) - (lower bound knot)
   * @return Sensitivity
   */
  protected DoubleMatrix1D getSensitivity(DoubleMatrix2D coefficientSensitivity, double sValue) {
    int nCoefs = coefficientSensitivity.getNumberOfRows();
    DoubleMatrix1D res = coefficientSensitivity.getRowVector(0);
    for (int i = 1; i < nCoefs; i++) {
      res = (DoubleMatrix1D) MA.scale(res, sValue);
      res = (DoubleMatrix1D) MA.add(res, coefficientSensitivity.getRowVector(i));
    }
    return res;
  }
}
