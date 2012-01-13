/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.math.function.Function;
import com.opengamma.math.surface.FunctionalDoublesSurface;

/**
 * 
 */
public abstract class BlackVolatilitySurfaceConverter {

  public static BlackVolatilityMoneynessSurface toMoneynessSurface(final BlackVolatilitySurface from, final ForwardCurve fwdCurve) {

    final Function<Double, Double> surFunc = new Function<Double, Double>() {

      @Override
      public Double evaluate(Double... tm) {
        double t = tm[0];
        double m = tm[1];
        double f = fwdCurve.getForward(t);
        double k = m * f;
        return from.getVolatility(t, k);
      }
    };

    return new BlackVolatilityMoneynessSurface(FunctionalDoublesSurface.from(surFunc), fwdCurve);

  }
}
