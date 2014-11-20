/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import java.util.Arrays;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.math.function.PiecewisePolynomialWithSensitivityFunction1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ParallelArrayBinarySort;

/**
 * Given a data set {xValues[i], yValues[i]}, interpolate {xValues[i], xValues[i] * yValues[i]} by a piecewise polynomial function. 
 * The interpolation can be clamped at {xValuesClamped[j], xValuesClamped[j] * yValuesClamped[j]}, i.e., {xValuesClamped[j], yValuesClamped[j]}, 
 * where the extra points can be inside or outside the data range. 
 */
public class ProductPiecewisePolynomialInterpolator extends PiecewisePolynomialInterpolator {
  private final PiecewisePolynomialInterpolator _baseMethod;
  private final double[] _xValuesClamped;
  private final double[] _yValuesClamped;
  private static final PiecewisePolynomialWithSensitivityFunction1D FUNC = new PiecewisePolynomialWithSensitivityFunction1D();

  private static final double EPS = 1.0e-15;

  public ProductPiecewisePolynomialInterpolator(PiecewisePolynomialInterpolator baseMethod) {
    ArgumentChecker.notNull(baseMethod, "baseMethod");
    ArgumentChecker.isFalse(baseMethod instanceof ProductPiecewisePolynomialInterpolator,
        "baseMethod should not be ProductPiecewisePolynomialInterpolator");
    _baseMethod = baseMethod;
    _xValuesClamped = null;
    _yValuesClamped = null;
  }
  
  public ProductPiecewisePolynomialInterpolator(PiecewisePolynomialInterpolator baseMethod, double[] xValuesClamped,
      double[] yValuesClamped) {
    ArgumentChecker.notNull(baseMethod, "method");
    ArgumentChecker.notNull(xValuesClamped, "xValuesClamped");
    ArgumentChecker.notNull(yValuesClamped, "yValuesClamped");
    ArgumentChecker.isFalse(baseMethod instanceof ProductPiecewisePolynomialInterpolator,
        "baseMethod should not be ProductPiecewisePolynomialInterpolator");
    int nExtraPoints = xValuesClamped.length;
    ArgumentChecker.isTrue(yValuesClamped.length == nExtraPoints,
        "xValuesClamped and yValuesClamped should be the same length");
    _baseMethod = baseMethod;
    _xValuesClamped = Arrays.copyOf(xValuesClamped, nExtraPoints);
    _yValuesClamped = Arrays.copyOf(yValuesClamped, nExtraPoints);
  }

  @Override
  public PiecewisePolynomialResult interpolate(double[] xValues, double[] yValues) {
    PiecewisePolynomialResult result;
    if (isClamped()) {
      double[][] xyValuesAll = getDataTotal(xValues, yValues);
      result = _baseMethod.interpolate(xyValuesAll[0], xyValuesAll[1]);
    } else {
      double[] xyValues = getProduct(xValues, yValues);
      result = _baseMethod.interpolate(xValues, xyValues);
    }
    int nIntervalsAll = result.getNumberOfIntervals();
    if (Math.abs(xValues[xValues.length - 1] - result.getKnots().getData()[nIntervalsAll]) < EPS) {
      double extraDerivative = FUNC.differentiate(result, xValues[xValues.length - 1]).getEntry(0);
      double[] newKnots = new double[nIntervalsAll + 2];
      System.arraycopy(result.getKnots().getData(), 0, newKnots, 0, nIntervalsAll + 1);
      newKnots[nIntervalsAll + 1] = xValues[xValues.length - 1];
      double[][] newCoefMatrix = new double[nIntervalsAll + 1][];
      for (int i = 0; i < nIntervalsAll; ++i) {
        newCoefMatrix[i] = Arrays.copyOf(result.getCoefMatrix().getRowVector(i).getData(), result.getOrder());
      }
      newCoefMatrix[nIntervalsAll] = new double[result.getOrder()];
      newCoefMatrix[nIntervalsAll][result.getOrder() - 1] = xValues[xValues.length - 1] * yValues[xValues.length - 1];
      newCoefMatrix[nIntervalsAll][result.getOrder() - 2] = extraDerivative;
      result = new PiecewisePolynomialResult(new DoubleMatrix1D(newKnots), new DoubleMatrix2D(newCoefMatrix),
          result.getOrder(), 1);
    }
    return result;
  }

  @Override
  public PiecewisePolynomialResult interpolate(double[] xValues, double[][] yValuesMatrix) {
    throw new NotImplementedException("Use 1D interpolation method");
  }

  @Override
  public PiecewisePolynomialResultsWithSensitivity interpolateWithSensitivity(double[] xValues, double[] yValues) {
    PiecewisePolynomialResultsWithSensitivity result;
    if (isClamped()) {
      double[][] xyValuesAll = getDataTotal(xValues, yValues);
      result = _baseMethod.interpolateWithSensitivity(xyValuesAll[0], xyValuesAll[1]);
    } else {
      double[] xyValues = getProduct(xValues, yValues);
      result = _baseMethod.interpolateWithSensitivity(xValues, xyValues);
    }
    int nIntervalsAll = result.getNumberOfIntervals();
    if (Math.abs(xValues[xValues.length - 1] - result.getKnots().getData()[nIntervalsAll]) < EPS) {
      double extraXValue = 2.0 * xValues[xValues.length - 1] - xValues[xValues.length - 2];
      double extraDerivative = FUNC.differentiate(result, xValues[xValues.length - 1]).getEntry(0);
      double[] extraSense = FUNC.nodeSensitivity(result, xValues[xValues.length - 1]).getData();
      double[] extraSenseDer = FUNC.differentiateNodeSensitivity(result, xValues[xValues.length - 1]).getData();
      double[] newKnots = new double[nIntervalsAll + 2];
      System.arraycopy(result.getKnots().getData(), 0, newKnots, 0, nIntervalsAll + 1);
      newKnots[nIntervalsAll + 1] = extraXValue;
      double[][] newCoefMatrix = new double[nIntervalsAll + 1][];
      DoubleMatrix2D[] newCoefSense = new DoubleMatrix2D[nIntervalsAll + 1];
      for (int i = 0; i < nIntervalsAll; ++i) {
        newCoefMatrix[i] = Arrays.copyOf(result.getCoefMatrix().getRowVector(i).getData(), result.getOrder());
        newCoefSense[i] = result.getCoefficientSensitivity(i);
      }
      newCoefMatrix[nIntervalsAll] = new double[result.getOrder()];
      newCoefMatrix[nIntervalsAll][result.getOrder() - 1] = xValues[xValues.length - 1] * yValues[xValues.length - 1];
      newCoefMatrix[nIntervalsAll][result.getOrder() - 2] = extraDerivative;
      double[][] extraCoefSense = new double[result.getOrder()][extraSense.length];
      extraCoefSense[result.getOrder() - 1] = Arrays.copyOf(extraSense, extraSense.length);
      extraCoefSense[result.getOrder() - 2] = Arrays.copyOf(extraSenseDer, extraSenseDer.length);
      newCoefSense[nIntervalsAll] = new DoubleMatrix2D(extraCoefSense);
      result = new PiecewisePolynomialResultsWithSensitivity(new DoubleMatrix1D(newKnots), new DoubleMatrix2D(
          newCoefMatrix),
          result.getOrder(), 1, newCoefSense);
    }
    return result;
  }

  /**
   * @return True if the interpolation is clamped
   */
  private boolean isClamped() {
    if (_xValuesClamped == null || _yValuesClamped == null) {
      return false;
    }
    return true;
  }

  private double[][] getDataTotal(double[] xData, double[] yData) {
    int nExtraPoints = _xValuesClamped.length;
    int nData = xData.length;
    int nTotal = nExtraPoints + nData;
    double[] xValuesTotal = new double[nTotal];
    double[] yValuesTotal = new double[nTotal];
    System.arraycopy(xData, 0, xValuesTotal, 0, nData);
    System.arraycopy(yData, 0, yValuesTotal, 0, nData);
    System.arraycopy(_xValuesClamped, 0, xValuesTotal, nData, nExtraPoints);
    System.arraycopy(_yValuesClamped, 0, yValuesTotal, nData, nExtraPoints);
    ParallelArrayBinarySort.parallelBinarySort(xValuesTotal, yValuesTotal);
    double[] xyTotal = getProduct(xValuesTotal, yValuesTotal);
    return new double[][] {xValuesTotal, xyTotal };
  }

  private double[] getProduct(double[] x, double[] y) {
    int n = x.length;
    double[] xy = new double[n];
    for (int i = 0; i < n; ++i) {
      xy[i] = x[i] * y[i];
    }
    return xy;
  }
}
