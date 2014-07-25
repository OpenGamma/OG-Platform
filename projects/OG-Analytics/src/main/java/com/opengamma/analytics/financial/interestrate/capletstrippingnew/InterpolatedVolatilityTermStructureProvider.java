/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.function.Function2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.analytics.math.surface.FunctionalSurface;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.ArgumentChecker;

/**
 *Produces a volatility surface that is backed by a single interpolated curve in the expiry dimension, i.e. there is no 
 *strike variation 
 */
public class InterpolatedVolatilityTermStructureProvider implements VolatilitySurfaceProvider {

  private final double[] _knots;
  private final Interpolator1D _interpolator;

  public InterpolatedVolatilityTermStructureProvider(final double[] knotPoints, final Interpolator1D interpolator) {
    ArgumentChecker.notEmpty(knotPoints, "null or empty knotPoints");
    ArgumentChecker.notNull(interpolator, "null interpolator");
    final int n = knotPoints.length;

    for (int i = 1; i < n; i++) {
      ArgumentChecker.isTrue(knotPoints[i] > knotPoints[i - 1], "knot points must be strictly ascending");
    }
    _knots = knotPoints;
    _interpolator = interpolator;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public VolatilitySurface getVolSurface(final DoubleMatrix1D modelParameters) {
    final InterpolatedDoublesCurve curve = new InterpolatedDoublesCurve(_knots, modelParameters.getData(), _interpolator, true);

    final Function<Double, Double> function = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tk) {
        return curve.getYValue(tk[0]);
      }
    };

    final FunctionalDoublesSurface surface = new FunctionalDoublesSurface(function);
    return new VolatilitySurface(surface);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Surface<Double, Double, DoubleMatrix1D> getVolSurfaceAdjoint(final DoubleMatrix1D modelParameters) {

    final InterpolatedDoublesCurve curve = new InterpolatedDoublesCurve(_knots, modelParameters.getData(), _interpolator, true);
    final Function2D<Double, DoubleMatrix1D> func = new Function2D<Double, DoubleMatrix1D>() {
      @Override
      public DoubleMatrix1D evaluate(final Double t, final Double k) {
        final Double[] sense = curve.getYValueParameterSensitivity(t);
        return new DoubleMatrix1D(sense);
      }
    };

    return new FunctionalSurface<>(func);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumModelParameters() {
    return _knots.length;
  }

}
