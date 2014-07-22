/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import java.util.List;

import com.opengamma.analytics.financial.interestrate.capletstripping.newstrippers.VolatilitySurfaceProvider;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
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
 * 
 */
public class BasisSplineVolatilityTermStructureProvider implements VolatilitySurfaceProvider {

  private final List<Function1D<Double, Double>> _bSplines;

  public BasisSplineVolatilityTermStructureProvider(final List<Function1D<Double, Double>> bSlines) {
    ArgumentChecker.noNulls(bSlines, "null bSplines");
    _bSplines = bSlines;
  }

  public BasisSplineVolatilityTermStructureProvider(final double t1, final double t2, final int nKnots, final int degree) {
    final BasisFunctionGenerator gen = new BasisFunctionGenerator();
    _bSplines = gen.generateSet(t1, t2, nKnots, degree);
  }

  /**
   * {@inheritDoc}
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

  @Override
  public int getNumModelParameters() {
    return _bSplines.size();
  }

}
