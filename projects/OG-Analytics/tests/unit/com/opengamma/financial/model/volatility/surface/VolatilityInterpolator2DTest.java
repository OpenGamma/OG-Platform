/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.Interpolator2D;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class VolatilityInterpolator2DTest {
  private static final Interpolator2D INTERPOLATOR = new GridInterpolator2D(new LinearInterpolator1D(), new LinearInterpolator1D());
  private static final Map<DoublesPair, Double> FLAT_DATA = new HashMap<DoublesPair, Double>();
  private static final Map<DoublesPair, Double> VARIANCE_SURFACE = new HashMap<DoublesPair, Double>();
  private static final Map<DoublesPair, Double> VOLATILITY_SURFACE = new HashMap<DoublesPair, Double>();
  private static final VolatilityInterpolator2D VOL = new VolatilityInterpolator2D(INTERPOLATOR);
  private static final double FLAT_VALUE = 0.2;

  static {
    DoublesPair pair;
    double variance;
    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 10; j++) {
        pair = DoublesPair.of((double) i, (double) j);
        FLAT_DATA.put(pair, FLAT_VALUE);
        variance = Math.random();
        VARIANCE_SURFACE.put(pair, variance);
        VOLATILITY_SURFACE.put(pair, Math.sqrt(variance));
      }
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullInterpolator() {
    new VolatilityInterpolator2D(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    VOL.interpolate(null, DoublesPair.of(0.4, 0.7));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullValue() {
    VOL.interpolate(FLAT_DATA, null);
  }

  @Test
  public void test() {
    DoublesPair pair;
    for (int i = 0; i < 9; i++) {
      for (int j = 0; j < 9; j++) {
        pair = DoublesPair.of(i + 0.5, j + 0.5);
        assertEquals(VOL.interpolate(FLAT_DATA, pair), 0.2, 0);
        assertEquals(Math.sqrt(INTERPOLATOR.interpolate(VARIANCE_SURFACE, pair)), VOL.interpolate(VOLATILITY_SURFACE, pair), 1e-15);
      }
    }
  }
}
