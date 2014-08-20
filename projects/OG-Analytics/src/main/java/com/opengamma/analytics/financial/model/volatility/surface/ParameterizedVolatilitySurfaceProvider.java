/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.function.Function2D;
import com.opengamma.analytics.math.function.ParameterizedSurface;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.analytics.math.surface.FunctionalSurface;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * This effectively acts as a converter from a {@link ParameterizedSurface} to a {@link VolatilitySurfaceProvider}
 */
public class ParameterizedVolatilitySurfaceProvider implements VolatilitySurfaceProvider {
  private final ParameterizedSurface _surface;

  /**
   * Convert from a  {@link ParameterizedSurface} to a {@link VolatilitySurfaceProvider}
   * @param surface a {@link ParameterizedSurface}
   */
  public ParameterizedVolatilitySurfaceProvider(final ParameterizedSurface surface) {
    ArgumentChecker.notNull(surface, "surface");
    _surface = surface;
  }

  @Override
  public VolatilitySurface getVolSurface(final DoubleMatrix1D modelParameters) {

    //Comment - need to change a function that maps from a DoublesPair to a Double, to one that maps from two Doubles to a Double. 
    //method asFunctionOfArguments checks modelParameters for null, and its length
    final Function1D<DoublesPair, Double> func = _surface.asFunctionOfArguments(modelParameters);

    final Function2D<Double, Double> func2D = new Function2D<Double, Double>() {
      @Override
      public Double evaluate(final Double t, final Double k) {
        return func.evaluate(DoublesPair.of((double) t, (double) k));
      }
    };

    final FunctionalDoublesSurface surface = new FunctionalDoublesSurface(func2D);
    return new VolatilitySurface(surface);
  }

  @Override
  public Surface<Double, Double, DoubleMatrix1D> getParameterSensitivitySurface(final DoubleMatrix1D modelParameters) {
    final Function1D<DoublesPair, DoubleMatrix1D> senseFunc = _surface.getZParameterSensitivity(modelParameters);

    final Function2D<Double, DoubleMatrix1D> func = new Function2D<Double, DoubleMatrix1D>() {
      @Override
      public DoubleMatrix1D evaluate(final Double t, final Double k) {
        return senseFunc.evaluate(DoublesPair.of((double) t, (double) k));
      }
    };

    return new FunctionalSurface<>(func);

  }

  @Override
  public Surface<Double, Double, Pair<Double, DoubleMatrix1D>> getVolAndParameterSensitivitySurface(final DoubleMatrix1D modelParameters) {
    final Function1D<DoublesPair, Double> func = _surface.asFunctionOfArguments(modelParameters);
    final Function1D<DoublesPair, DoubleMatrix1D> senseFunc = _surface.getZParameterSensitivity(modelParameters);

    final Function2D<Double, Pair<Double, DoubleMatrix1D>> func2D = new Function2D<Double, Pair<Double, DoubleMatrix1D>>() {
      @Override
      public Pair<Double, DoubleMatrix1D> evaluate(final Double t, final Double k) {
        final DoublesPair dp = DoublesPair.of((double) t, (double) k);
        return ObjectsPair.of(func.evaluate(dp), senseFunc.evaluate(dp));
      }
    };

    return new FunctionalSurface<>(func2D);
  }

  @Override
  public int getNumModelParameters() {
    return _surface.getNumberOfParameters();
  }

}
