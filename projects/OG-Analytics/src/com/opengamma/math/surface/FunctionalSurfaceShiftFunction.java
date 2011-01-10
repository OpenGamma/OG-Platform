/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.surface;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function;
import com.opengamma.util.tuple.DoublesPair;

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
    final Function<DoublesPair, Double> f = surface.getFunction();
    final Function<DoublesPair, Double> shiftedFunction = new Function<DoublesPair, Double>() {

      @Override
      public Double evaluate(final DoublesPair... x) {
        return f.evaluate(x) + shift;
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
