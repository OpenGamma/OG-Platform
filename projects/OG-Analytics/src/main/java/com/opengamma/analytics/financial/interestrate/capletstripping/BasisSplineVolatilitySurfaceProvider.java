/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import java.util.List;

import com.opengamma.analytics.financial.interestrate.capletstrippingnew.VolatilitySurfaceProvider;
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
public class BasisSplineVolatilitySurfaceProvider implements VolatilitySurfaceProvider {

  private final List<Function1D<double[], Double>> _bSplines;

  public BasisSplineVolatilitySurfaceProvider(final List<Function1D<double[], Double>> bSlines) {
    ArgumentChecker.noNulls(bSlines, "null bSplines");
    _bSplines = bSlines;
  }

  public BasisSplineVolatilitySurfaceProvider(final double k1, final double k2, final int nStrikeKnots, final int strikeDegree, final double t1, final double t2, final int nTimeKnots,
      final int timeDegree) {
    final BasisFunctionGenerator gen = new BasisFunctionGenerator();
    _bSplines = gen.generateSet(new double[] {t1, k1 }, new double[] {t2, k2 }, new int[] {nTimeKnots, nStrikeKnots }, new int[] {timeDegree, strikeDegree });

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public VolatilitySurface getVolSurface(final DoubleMatrix1D modelParameters) {
    final Function1D<double[], Double> func = new BasisFunctionAggregation<>(_bSplines, modelParameters.getData());

    final Function2D<Double, Double> func2D = new Function2D<Double, Double>() {
      @Override
      public Double evaluate(final Double t, final Double k) {
        return func.evaluate(new double[] {t, k });
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
    final BasisFunctionAggregation<double[]> bSpline = new BasisFunctionAggregation<>(_bSplines, modelParameters.getData());

    final Function2D<Double, DoubleMatrix1D> func = new Function2D<Double, DoubleMatrix1D>() {
      @Override
      public DoubleMatrix1D evaluate(final Double t, final Double k) {
        return bSpline.weightSensitivity(new double[] {t, k });
      }
    };

    return new FunctionalSurface<>(func);
  }

  @Override
  public int getNumModelParameters() {
    return _bSplines.size();
  }

}
