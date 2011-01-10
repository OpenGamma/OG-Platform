/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.math.surface.ConstantDoublesSurface;
import com.opengamma.math.surface.InterpolatedDoublesSurface;
import com.opengamma.math.surface.Surface;

/**
 * 
 */
public class MathSurfaceTest extends AnalyticsTestBase {

  @Test
  public void testConstantSurface() {
    Surface<Double, Double, Double> s1 = ConstantDoublesSurface.from(4.);
    Surface<Double, Double, Double> s2 = cycleObject(Surface.class, s1);
    assertEquals(s1, s2);
    s1 = ConstantDoublesSurface.from(4., "NAME");
    s2 = cycleObject(Surface.class, s1);
    assertEquals(s1, s2);
  }

  @Test
  public void testInterpolatedSurface() {
    final LinearInterpolator1D linear = new LinearInterpolator1D();
    final GridInterpolator2D interpolator = new GridInterpolator2D(linear, linear);
    Surface<Double, Double, Double> s1 = InterpolatedDoublesSurface.from(new double[] {1, 2, 3, 4}, new double[] {4, 5, 6, 7}, new double[] {8, 9, 10, 11}, interpolator);
    Surface<Double, Double, Double> s2 = cycleObject(Surface.class, s1);
    assertEquals(s1, s2);
    s1 = InterpolatedDoublesSurface.from(new double[] {1, 2, 3, 4}, new double[] {4, 5, 6, 7}, new double[] {8, 9, 10, 11}, interpolator, "NAME");
    s2 = cycleObject(Surface.class, s1);
    assertEquals(s1, s2);
  }
}
