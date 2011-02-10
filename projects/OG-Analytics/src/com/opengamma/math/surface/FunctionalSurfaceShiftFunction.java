/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.surface;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function;

/**
 * 
 */
public class FunctionalSurfaceShiftFunction implements SurfaceShiftFunction<FunctionalDoublesSurface> {

  @Override
  public FunctionalDoublesSurface evaluate(final FunctionalDoublesSurface surface, final double shift) {
    Validate.notNull(surface, "surface");
    return evaluate(surface, shift, "PARALLEL_SHIFT_" + surface.getName());
  }

  @Override
  public FunctionalDoublesSurface evaluate(final FunctionalDoublesSurface surface, final double shift, final String newName) {
    Validate.notNull(surface, "surface");
    final Function<Double, Double> f = surface.getFunction();
    final Function<Double, Double> shiftedFunction = new Function<Double, Double>() {

      @Override
      public Double evaluate(final Double... xy) {
        return f.evaluate(xy) + shift;
      }

    };
    return FunctionalDoublesSurface.from(shiftedFunction, newName);
  }

  @Override
  public FunctionalDoublesSurface evaluate(final FunctionalDoublesSurface surface, final double x, final double y, final double shift) {
    throw new UnsupportedOperationException();
  }

  @Override
  public FunctionalDoublesSurface evaluate(final FunctionalDoublesSurface surface, final double x, final double y, final double shift, final String newName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public FunctionalDoublesSurface evaluate(final FunctionalDoublesSurface surface, final double[] xShift, final double[] yShift, final double[] shift) {
    throw new UnsupportedOperationException();
  }

  @Override
  public FunctionalDoublesSurface evaluate(final FunctionalDoublesSurface surface, final double[] xShift, final double[] yShift, final double[] shift, final String newName) {
    throw new UnsupportedOperationException();
  }

}
