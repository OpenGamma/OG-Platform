/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator2D;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.surface.ConstantDoublesSurface;
import com.opengamma.math.surface.InterpolatedDoublesSurface;
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
    final Interpolator1D<Interpolator1DDataBundle> linear = new LinearInterpolator1D();
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

}
