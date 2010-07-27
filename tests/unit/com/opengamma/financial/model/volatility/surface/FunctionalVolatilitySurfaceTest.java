/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import com.opengamma.math.function.Function1D;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class FunctionalVolatilitySurfaceTest {
  private static final Function1D<Pair<Double, Double>, Double> F = new Function1D<Pair<Double, Double>, Double>() {

    @Override
    public Double evaluate(Pair<Double, Double> x) {
      return null;
    }
    
  };
}
