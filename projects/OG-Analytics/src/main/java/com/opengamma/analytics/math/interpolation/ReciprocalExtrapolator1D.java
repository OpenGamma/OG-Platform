/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import com.opengamma.analytics.math.function.PiecewisePolynomialWithSensitivityFunction1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;


/**
 * Given a data set {x[i], y[i]}, extrapolate {x[i], x[i] * y[i]} by a linear function.  
 */
public class ReciprocalExtrapolator1D extends ProductPolynomialExtrapolator1D {
  private static final long serialVersionUID = 1L;
  private static final PiecewisePolynomialWithSensitivityFunction1D FUNC = new LinearlWithSensitivityFunction1D();

  //  private static final PiecewisePolynomialWithSensitivityFunction1D FUNC = new PiecewisePolynomialWithSensitivityFunction1D();

  public ReciprocalExtrapolator1D(Interpolator1D interpolator) {
    super(interpolator, FUNC);
  }

  @Override
  public Double interpolate(final Interpolator1DDataBundle data, final Double value) {
    //    Interpolator1DDataBundle modifiedBundle = getLinearExtrapolation(data);
    //    return super.interpolate(modifiedBundle, value);
    return super.interpolate(data, value);
  }

  @Override
  public double firstDerivative(final Interpolator1DDataBundle data, final Double value) {
    //    Interpolator1DDataBundle modifiedBundle = getLinearExtrapolation(data);
    //    return super.firstDerivative(modifiedBundle, value);
    return super.firstDerivative(data, value);
  }

  @Override
  public double[] getNodeSensitivitiesForValue(final Interpolator1DDataBundle data, final Double value) {
    //    Interpolator1DDataBundle modifiedBundle = getLinearExtrapolation(data);
    //    return super.getNodeSensitivitiesForValue(modifiedBundle, value);
    return super.getNodeSensitivitiesForValue(data, value);
  }

  //  private Interpolator1DDataBundle getLinearExtrapolation(Interpolator1DDataBundle data) {
  //    Interpolator1DDataBundle newData;
  //    if (data instanceof Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle) {
  //      Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle polyData = (Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle) data;
  //      PiecewisePolynomialResult originalResult = polyData.getPiecewisePolynomialResult();
  //      PiecewisePolynomialResult[] originalResultUp = polyData.getPiecewisePolynomialResultUp();
  //      PiecewisePolynomialResult[] originalResultDw = polyData.getPiecewisePolynomialResultDw();
  //      int newOrder = 2;
  //      DoubleMatrix2D newCoef = trimCoefMatrix(originalResult.getCoefMatrix());
  //      PiecewisePolynomialResult newResult = new PiecewisePolynomialResult(originalResult.getKnots(), newCoef, newOrder,
  //          1);
  //      int nSense = originalResultUp.length;
  //      PiecewisePolynomialResult[] newResultUp = new PiecewisePolynomialResult[nSense];
  //      PiecewisePolynomialResult[] newResultDw = new PiecewisePolynomialResult[nSense];
  //      for (int i = 0; i < nSense; ++i) {
  //        DoubleMatrix2D newCoefUp = trimCoefMatrix(originalResultUp[i].getCoefMatrix());
  //        newResultUp[i] = new PiecewisePolynomialResult(originalResultUp[i].getKnots(), newCoefUp, newOrder, 1);
  //        DoubleMatrix2D newCoefDw = trimCoefMatrix(originalResultDw[i].getCoefMatrix());
  //        newResultDw[i] = new PiecewisePolynomialResult(originalResultDw[i].getKnots(), newCoefDw, newOrder, 1);
  //      }
  //      newData = new Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle(polyData.getUnderlyingData(), newResult,
  //          newResultUp, newResultDw);
  //    } else if (data instanceof Interpolator1DPiecewisePoynomialDataBundle) {
  //      Interpolator1DPiecewisePoynomialDataBundle polyData = (Interpolator1DPiecewisePoynomialDataBundle) data;
  //      PiecewisePolynomialResultsWithSensitivity originalResult = polyData
  //          .getPiecewisePolynomialResultsWithSensitivity();
  //      DoubleMatrix2D[] coefSense = originalResult.getCoefficientSensitivityAll();
  //      int nSense = coefSense.length;
  //      DoubleMatrix2D[] newCoefSense = new DoubleMatrix2D[nSense];
  //      for (int i = 0; i < nSense; ++i) {
  //        newCoefSense[i] = trimCoefMatrix(coefSense[i]);
  //      }
  //      DoubleMatrix2D newCoef = trimCoefMatrix(originalResult.getCoefMatrix());
  //      int newOrder = 2;
  //      PiecewisePolynomialResultsWithSensitivity newResult = new PiecewisePolynomialResultsWithSensitivity(
  //          originalResult.getKnots(), newCoef, newOrder, 1, newCoefSense);
  //      newData = new Interpolator1DPiecewisePoynomialDataBundle(polyData.getUnderlyingData(), newResult);
  //    } else {
  //      throw new IllegalArgumentException(
  //          "data should be Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle or Interpolator1DPiecewisePoynomialDataBundle");
  //    }
  //    return newData;
  //  }
  //
  //  private DoubleMatrix2D trimCoefMatrix(DoubleMatrix2D matrix) {
  //    int nColumn = matrix.getNumberOfColumns();
  //    int nRows = matrix.getNumberOfRows();
  //    double[][] res = new double[nRows][2];
  //    DoubleMatrix1D firstCoef = matrix.getColumnVector(nColumn - 2);
  //    DoubleMatrix1D zerothCoef = matrix.getColumnVector(nColumn - 1);
  //    for (int i = 0; i < nRows; ++i) {
  //      res[i][0] = firstCoef.getData()[i];
  //      res[i][1] = zerothCoef.getData()[i];
  //    }
  //    return new DoubleMatrix2D(res);
  //  }

  private static class LinearlWithSensitivityFunction1D extends PiecewisePolynomialWithSensitivityFunction1D {
    private static final MatrixAlgebra MA = new OGMatrixAlgebra();

    @Override
    protected double getValue(final double[] coefs, final double x, final double leftknot) {
      int nCoefs = coefs.length;
      double res = coefs[nCoefs - 2] * (x - leftknot) + coefs[nCoefs - 1];
      return res;
    }

    @Override
    protected DoubleMatrix1D getSensitivity(DoubleMatrix2D coefficientSensitivity, double sValue) {
      int nCoefs = coefficientSensitivity.getNumberOfRows();
      DoubleMatrix1D res = coefficientSensitivity.getRowVector(nCoefs - 2);
      res = (DoubleMatrix1D) MA.scale(res, sValue);
      res = (DoubleMatrix1D) MA.add(res, coefficientSensitivity.getRowVector(nCoefs - 1));
      return res;
    }
  }
}
