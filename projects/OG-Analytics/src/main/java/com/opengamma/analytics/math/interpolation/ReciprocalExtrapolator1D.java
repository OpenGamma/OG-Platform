/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.analytics.math.function.PiecewisePolynomialFunction1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;


/**
 * Given a data set {x[i], y[i]}, extrapolate {x[i], x[i] * y[i]} by a linear function by using polynomial coefficients 
 * obtained in ProductPiecewisePolynomialInterpolator1D. 
 * 
 * Even if the interpolator is clamped at (0,0), this extrapolator does not ensure the resulting extrapolation curve goes through the origin. 
 * Thus a reference value is returned for Math.abs(value) < SMALL, where SMALL is defined in the super class. 
 * Use {@link ZeroClampedProductExtrapolator1D} if one needs extrapolation such that the resulting curve go through the origin.  
 */
public class ReciprocalExtrapolator1D extends ProductPolynomialExtrapolator1D {
  private static final long serialVersionUID = 1L;
  private static final PiecewisePolynomialFunction1D FUNC = new LinearlFunction1D();

  /**
   * Construct the extrapolator
   * @param interpolator The interpolator
   */
  public ReciprocalExtrapolator1D(Interpolator1D interpolator) {
    super(interpolator, FUNC);
  }

  /**
   * {@inheritDoc}
   * For small Math.abs(value), the resulting value diverges in general. 
   * In such a case this method returns a reference value
   */
  @Override
  public Double interpolate(final Interpolator1DDataBundle data, final Double value) {
    return super.interpolate(data, value);
  }

  /**
   * {@inheritDoc}
   * For small Math.abs(value), the resulting value diverges in general. 
   * In such a case this method returns a reference value
   */
  @Override
  public double firstDerivative(final Interpolator1DDataBundle data, final Double value) {
    return super.firstDerivative(data, value);
  }

  /**
   * {@inheritDoc}
   * For small Math.abs(value), the resulting value diverges in general. 
   * In such a case this method returns a reference value
   */
  @Override
  public double[] getNodeSensitivitiesForValue(final Interpolator1DDataBundle data, final Double value) {
    return super.getNodeSensitivitiesForValue(data, value);
  }

  private static class LinearlFunction1D extends PiecewisePolynomialFunction1D {
    
    @Override
    public DoubleMatrix1D evaluate(final PiecewisePolynomialResult pp, final double xKey) {
      ArgumentChecker.notNull(pp, "pp");
      double[] knots = pp.getKnots().getData();
      int nKnots = knots.length;
      DoubleMatrix2D coefMatrix = pp.getCoefMatrix();
      int dim = pp.getDimensions();
      double[] res = new double[dim];
      int indicator = FunctionUtils.getLowerBoundIndex(knots, xKey);
      if (indicator == nKnots - 1) {
        indicator--; //there is 1 less interval that knots 
      }
      for (int j = 0; j < dim; ++j) {
        double[] coefs = coefMatrix.getRowVector(dim * indicator + j).getData();
        res[j] = getValue(coefs, xKey, knots[indicator]);
      }

      return new DoubleMatrix1D(res);
    }

    @Override
    public DoubleMatrix1D differentiate(final PiecewisePolynomialResult pp, final double xKey) {
      ArgumentChecker.notNull(pp, "pp");
      double[] knots = pp.getKnots().getData();
      int nKnots = pp.getNumberOfIntervals() + 1;
      int nCoefs = pp.getOrder();
      int dim = pp.getDimensions();
      double[] res = new double[dim];
      int indicator = FunctionUtils.getLowerBoundIndex(knots, xKey);
      if (indicator == nKnots - 1) {
        indicator--; //there is 1 less interval that knots 
      }
      DoubleMatrix2D coefMatrix = pp.getCoefMatrix();
      for (int j = 0; j < dim; ++j) {
        final double[] coefs = coefMatrix.getRowVector(dim * indicator + j).getData();
        res[j] = coefs[nCoefs - 2];
      }
      return new DoubleMatrix1D(res);
    }

    @Override
    public DoubleMatrix1D differentiateTwice(final PiecewisePolynomialResult pp, final double xKey) {
      ArgumentChecker.notNull(pp, "pp");
      int dim = pp.getDimensions();
      double[] result = new double[dim];
      return new DoubleMatrix1D(result);
    }

    @Override
    protected double getValue(final double[] coefs, final double x, final double leftknot) {
      int nCoefs = coefs.length;
      double res = coefs[nCoefs - 2] * (x - leftknot) + coefs[nCoefs - 1];
      return res;
    }
  }
}
