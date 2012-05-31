/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.analytics.math.surface.Surface;

/**
 * 
 */
public final class ConstantSpreadSurfaceRolldownFunction implements RolldownFunction<Surface<Double, Double, Double>> {
  private static final ConstantSpreadSurfaceRolldownFunction INSTANCE = new ConstantSpreadSurfaceRolldownFunction();

  public static ConstantSpreadSurfaceRolldownFunction getInstance() {
    return INSTANCE;
  }

  private ConstantSpreadSurfaceRolldownFunction() {
  }

  @Override
  public Surface<Double, Double, Double> rollDown(final Surface<Double, Double, Double> surface, final double time) {
    final Function<Double, Double> shiftedFunction = new Function<Double, Double>() {

      @Override
      public Double evaluate(final Double... x) {
        return surface.getZValue(x[0] + time, x[1]);
      }

    };
    return FunctionalDoublesSurface.from(shiftedFunction);
  }

}
