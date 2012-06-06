/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.analytics.math.surface.Surface;

/**
 * 
 */
public class ConstantSpreadVolatilitySurfaceRolldownFunction implements RolldownFunction<VolatilitySurface> {

  @Override
  public VolatilitySurface rollDown(final VolatilitySurface volatilitySurface, final double time) {
    final Surface<Double, Double, Double> surface = volatilitySurface.getSurface();
    final Function<Double, Double> shiftedFunction = new Function<Double, Double>() {

      @Override
      public Double evaluate(final Double... x) {
        return surface.getZValue(x[0] + time, x[1]);
      }

    };
    return new VolatilitySurface(FunctionalDoublesSurface.from(shiftedFunction));
  }

}
