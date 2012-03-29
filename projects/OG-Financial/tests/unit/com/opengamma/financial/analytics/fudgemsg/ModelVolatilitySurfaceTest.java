/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator2D;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * Test ConstantVolatilitySurface/InterpolatedVolatilitySurface.
 */
public class ModelVolatilitySurfaceTest extends AnalyticsTestBase {

  @Test
  public void testConstantVolatilitySurface() {
    final VolatilitySurface vs1 = new VolatilitySurface(ConstantDoublesSurface.from(0.2));
    final VolatilitySurface vs2 = cycleObject(VolatilitySurface.class, vs1);
    assertEquals(vs1, vs2);
  }

  @Test
  public void testInterpolatedVolatilitySurface() {
    final double sigma = 0.4;
    final Interpolator1D linear = new LinearInterpolator1D();
    final Interpolator2D interpolator = new GridInterpolator2D(linear, linear);
    final Map<DoublesPair, Double> data = new HashMap<DoublesPair, Double>();
    data.put(Pair.of(0., 1.), sigma);
    data.put(Pair.of(1., 0.), sigma);
    data.put(Pair.of(0., 0.), sigma);
    data.put(Pair.of(1., 1.), sigma);
    final VolatilitySurface vs1 = new VolatilitySurface(InterpolatedDoublesSurface.from(data, interpolator));
    final VolatilitySurface vs2 = cycleObject(VolatilitySurface.class, vs1);
    assertEquals(vs1, vs2);
  }

  @Test
  public void testMoneynessSurface() {
    final ConstantDoublesSurface surface = ConstantDoublesSurface.from(0.5);
    final ForwardCurve curve = new ForwardCurve(1);
    final BlackVolatilitySurfaceMoneyness moneyness1 = new BlackVolatilitySurfaceMoneyness(surface, curve);
    BlackVolatilitySurfaceMoneyness moneyness2 = cycleObject(BlackVolatilitySurfaceMoneyness.class, moneyness1);
    assertEquals(moneyness1, moneyness2);
    moneyness2 = cycleObject(BlackVolatilitySurfaceMoneyness.class, new BlackVolatilitySurfaceMoneyness(moneyness1));
    assertEquals(moneyness1, moneyness2);
  }
}
