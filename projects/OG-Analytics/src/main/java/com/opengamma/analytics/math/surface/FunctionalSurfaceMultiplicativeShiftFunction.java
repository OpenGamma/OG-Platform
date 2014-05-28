/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.surface;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function;

/**
 * Shifts a {@link FunctionalDoublesSurface}. Only parallel shifts of the surface are supported.
 */
public class FunctionalSurfaceMultiplicativeShiftFunction implements SurfaceShiftFunction<FunctionalDoublesSurface> {

  /**
   * {@inheritDoc}
   */
  @Override
  public FunctionalDoublesSurface evaluate(final FunctionalDoublesSurface surface, final double percentage) {
    Validate.notNull(surface, "surface");
    return evaluate(surface, percentage, "CONSTANT_MULTIPLIER_" + surface.getName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public FunctionalDoublesSurface evaluate(final FunctionalDoublesSurface surface, final double percentage, final String newName) {
    Validate.notNull(surface, "surface");
    final Function<Double, Double> f = surface.getFunction();
    final Function<Double, Double> shiftedFunction = new ShiftedFunction(percentage, f);
    return FunctionalDoublesSurface.from(shiftedFunction, newName);
  }

  
  private static class ShiftedFunction implements Function<Double, Double> {
    
    private final double _percentage;
    private final Function<Double, Double> _f;
    
    public ShiftedFunction(double percentage, Function<Double, Double> f) {
      this._percentage = percentage;
      this._f = f;
    }

    @Override
    public Double evaluate(final Double... xy) {
      return _f.evaluate(xy) * (1 + _percentage);
    }
    
  }
  
  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public FunctionalDoublesSurface evaluate(final FunctionalDoublesSurface surface, final double x, final double y, final double percentage) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public FunctionalDoublesSurface evaluate(final FunctionalDoublesSurface surface, final double x, final double y, final double percentage, final String newName) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public FunctionalDoublesSurface evaluate(final FunctionalDoublesSurface surface, final double[] xShift, final double[] yShift, final double[] percentage) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public FunctionalDoublesSurface evaluate(final FunctionalDoublesSurface surface, final double[] xShift, final double[] yShift, final double[] percentage, final String newName) {
    throw new UnsupportedOperationException();
  }

}
