/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import java.util.List;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.function.Function2D;
import com.opengamma.analytics.math.interpolation.BasisFunctionAggregation;
import com.opengamma.analytics.math.interpolation.BasisFunctionGenerator;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.analytics.math.surface.FunctionalSurface;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.ArgumentChecker;

/**
 * Represent a volatility term structure (in this context a volatility surface with no strike dependence) using
 * 1D basis-splines  
 */
public class BasisSplineVolatilityTermStructureProvider implements VolatilitySurfaceProvider {

  private final List<Function1D<Double, Double>> _bSplines;

  /**
   * Represent a volatility term structure (in this context a volatility surface with no strike dependence) using
  * 1D basis-splines 
   * @param bSlines List of 1D functions (of time-to-expiry)  - the basis functions 
   */
  public BasisSplineVolatilityTermStructureProvider(final List<Function1D<Double, Double>> bSlines) {
    ArgumentChecker.noNulls(bSlines, "null bSplines");
    _bSplines = bSlines;
  }

  /**
   *  Represent a volatility term structure (in this context a volatility surface with no strike dependence) using
  * 1D basis-splines 
   * @param t1 The lower time
   * @param t2 The upper time
   * @param nKnots Number of knots
   * @param degree The degree of the spline
   */
  public BasisSplineVolatilityTermStructureProvider(final double t1, final double t2, final int nKnots, final int degree) {
    final BasisFunctionGenerator gen = new BasisFunctionGenerator();
    _bSplines = gen.generateSet(t1, t2, nKnots, degree);
  }

  /**
   * {@inheritDoc}
   * The model parameters in this case are the weights of the basis functions 
   */
  @Override
  public VolatilitySurface getVolSurface(final DoubleMatrix1D modelParameters) {
    final Function1D<Double, Double> func = new BasisFunctionAggregation<>(_bSplines, modelParameters.getData());

    final Function2D<Double, Double> func2D = new Function2D<Double, Double>() {
      @Override
      public Double evaluate(final Double t, final Double k) {
        return func.evaluate(t);
      }
    };

    final FunctionalDoublesSurface surface = new FunctionalDoublesSurface(func2D);
    return new VolatilitySurface(surface);
  }

  /**
   * {@inheritDoc}
   * The model parameters in this case are the weights of the basis functions 
   */
  @Override
  public Surface<Double, Double, DoubleMatrix1D> getVolSurfaceAdjoint(final DoubleMatrix1D modelParameters) {
    final BasisFunctionAggregation<Double> bSpline = new BasisFunctionAggregation<>(_bSplines, modelParameters.getData());

    final Function2D<Double, DoubleMatrix1D> func = new Function2D<Double, DoubleMatrix1D>() {
      @Override
      public DoubleMatrix1D evaluate(final Double t, final Double k) {
        return bSpline.weightSensitivity(t);
      }
    };

    return new FunctionalSurface<>(func);
  }

  /**
   * {@inheritDoc}
   * The number of parameters is the number of basis functions, which is #knots + degree - 1 
   */
  @Override
  public int getNumModelParameters() {
    return _bSplines.size();
  }
}
