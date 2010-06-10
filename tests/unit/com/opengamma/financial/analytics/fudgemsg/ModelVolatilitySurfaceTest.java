/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.opengamma.financial.model.volatility.surface.ConstantVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.InterpolatedVolatilitySurface;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DModel;
import com.opengamma.math.interpolation.Interpolator2D;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.tuple.Pair;

public class ModelVolatilitySurfaceTest extends AnalyticsTestBase {
  
  @Test
  public void testConstantVolatilitySurface () {
    ConstantVolatilitySurface vs1 = new ConstantVolatilitySurface (0.2);
    ConstantVolatilitySurface vs2 = cycleObject (ConstantVolatilitySurface.class, vs1);
    assertEquals (vs1, vs2);
  }
  
  @Test
  public void testInterpolatedVolatilitySurface () {
    double sigma = 0.4;
    Interpolator1D<Interpolator1DModel> linear = new LinearInterpolator1D ();
    Interpolator2D interpolator = new GridInterpolator2D (linear, linear);
    Map<Pair<Double,Double>,Double> data = new HashMap<Pair<Double,Double>,Double> ();
    data.put(Pair.of(0., 1.), sigma);
    data.put(Pair.of(1., 0.), sigma);
    data.put(Pair.of(0., 0.), sigma);
    data.put(Pair.of(1., 1.), sigma);
    InterpolatedVolatilitySurface vs1 = new InterpolatedVolatilitySurface (data, interpolator);
    InterpolatedVolatilitySurface vs2 = cycleObject (InterpolatedVolatilitySurface.class, vs1);
    assertEquals (vs1, vs2);
  }
  
}