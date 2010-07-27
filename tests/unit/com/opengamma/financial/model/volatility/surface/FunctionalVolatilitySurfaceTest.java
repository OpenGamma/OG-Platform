/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import com.opengamma.math.function.Function1D;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class FunctionalVolatilitySurfaceTest {
  private static final Function1D<DoublesPair, Double> F = new Function1D<DoublesPair, Double>() {

    @Override
    public Double evaluate(final DoublesPair x) {
      return null;
    }

  };
}
