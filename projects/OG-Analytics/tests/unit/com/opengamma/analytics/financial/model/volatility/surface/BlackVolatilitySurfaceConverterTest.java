/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceConverter;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceDelta;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;

/**
 * 
 */
public class BlackVolatilitySurfaceConverterTest {

  private static final BlackVolatilitySurfaceDelta DELTA_SURFACE;
  private static final ForwardCurve FORWARD_CURVE;
  private static final double SPOT = 167;
  private static final double DRIFT = 0.03;

  static {
    FORWARD_CURVE = new ForwardCurve(SPOT, DRIFT);
    Function<Double, Double> func = new Function<Double, Double>() {
      @Override
      public Double evaluate(Double... td) {
        double delta = td[1];
        return 0.2 + (delta - 0.4) * (delta - 0.4);
      }
    };
    DELTA_SURFACE = new BlackVolatilitySurfaceDelta(FunctionalDoublesSurface.from(func), FORWARD_CURVE);
  }

  @Test
  public void testValues() {
    for (int t = 0; t < 10; t++) {
      assertEquals(0.2, DELTA_SURFACE.getVolatilityForDelta(t, 0.4), 1e-6);
    }
  }

  @Test
  public void roundTripTest() {
    BlackVolatilitySurfaceStrike strikeSurface = BlackVolatilitySurfaceConverter.toStrikeSurface(DELTA_SURFACE);
    BlackVolatilitySurfaceDelta deltaSurface = BlackVolatilitySurfaceConverter.toDeltaSurface(strikeSurface, FORWARD_CURVE);


    for (int i = 0; i < 10; i++) {
      double t = Math.exp(i / 4.0) - 0.95;
      for (int j = 0; j < 10; j++) {
        double delta = 0.05 + 0.1 * j;
        //System.out.println(t+"\t"+delta+"\t"+deltaSurface.getVolatilityForDelta(t, delta));
        assertEquals(DELTA_SURFACE.getVolatilityForDelta(t, delta), deltaSurface.getVolatilityForDelta(t, delta), 2e-6);//TODO might want more accuracy on this
      }
    }
  }

}
