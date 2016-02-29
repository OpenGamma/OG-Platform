/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.StandardSmileSurfaceDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneynessFcnBackedByGrid;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurfaceInterpolator;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator2D;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Test ConstantVolatilitySurface/InterpolatedVolatilitySurface.
 */
@Test(groups = TestGroup.UNIT)
public class ModelVolatilitySurfaceTest extends AnalyticsTestBase {

  @Test
  public void testConstantVolatilitySurface() {
    final VolatilitySurface vs1 = new VolatilitySurface(ConstantDoublesSurface.from(0.2));
    final VolatilitySurface vs2 = cycleObject(VolatilitySurface.class, vs1);
    assertEquals(vs1, vs2);
  }

  // Disabled because new implementation of InterpolatedDoublesSurface may have
  // different actual arrays but have same semantic meaning
  @Test(enabled = false)
  public void testInterpolatedVolatilitySurface() {
    final double sigma = 0.4;
    final Interpolator1D linear = new LinearInterpolator1D();
    final Interpolator2D interpolator = new GridInterpolator2D(linear, linear);
    final Map<DoublesPair, Double> data = new HashMap<>();
    data.put(DoublesPair.of(0., 1.), sigma);
    data.put(DoublesPair.of(1., 0.), sigma);
    data.put(DoublesPair.of(0., 0.), sigma);
    data.put(DoublesPair.of(1., 1.), sigma);
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

  @Test
  public void testMoneynessSurfaceBackedByGrid() {
    final ConstantDoublesSurface surface = ConstantDoublesSurface.from(0.5);
    final ForwardCurve curve = new ForwardCurve(1);
    final StandardSmileSurfaceDataBundle gridData = new StandardSmileSurfaceDataBundle(100.0, new double[] {101,102,103}, new double[] {1,2,3},
        new double[][] {{80,80},{100,100},{120,120}}, new double[][] {{.3,.25},{.2,.2},{.3,.25}}, new LinearInterpolator1D() );
    final VolatilitySurfaceInterpolator interpolator = new VolatilitySurfaceInterpolator();
    final BlackVolatilitySurfaceMoneynessFcnBackedByGrid moneyness1 = new BlackVolatilitySurfaceMoneynessFcnBackedByGrid(surface, curve, gridData, interpolator);
    BlackVolatilitySurfaceMoneynessFcnBackedByGrid moneyness2 = cycleObject(BlackVolatilitySurfaceMoneynessFcnBackedByGrid.class, moneyness1);
    assertArrayEquals(moneyness1.getGridData().getExpiries(), moneyness2.getGridData().getExpiries(), 0);
    assertArrayEquals(moneyness1.getGridData().getForwards(), moneyness2.getGridData().getForwards(), 0);
    assert2DArrayEquals(moneyness1.getGridData().getStrikes(), moneyness2.getGridData().getStrikes(), 0);
    assert2DArrayEquals(moneyness1.getGridData().getVolatilities(), moneyness2.getGridData().getVolatilities(), 0);
    assertCurveEquals(moneyness1.getGridData().getForwardCurve(), moneyness2.getGridData().getForwardCurve());
    assertCurveEquals(moneyness1.getForwardCurve(), moneyness2.getForwardCurve());
    assertEquals(moneyness1.getInterpolator(), moneyness2.getInterpolator());
    assertEquals(moneyness1.getSurface(), moneyness2.getSurface());
    moneyness2 = cycleObject(BlackVolatilitySurfaceMoneynessFcnBackedByGrid.class, new BlackVolatilitySurfaceMoneynessFcnBackedByGrid(moneyness1));
    assertArrayEquals(moneyness1.getGridData().getExpiries(), moneyness2.getGridData().getExpiries(), 0);
    assertArrayEquals(moneyness1.getGridData().getForwards(), moneyness2.getGridData().getForwards(), 0);
    assert2DArrayEquals(moneyness1.getGridData().getStrikes(), moneyness2.getGridData().getStrikes(), 0);
    assert2DArrayEquals(moneyness1.getGridData().getVolatilities(), moneyness2.getGridData().getVolatilities(), 0);
    assertCurveEquals(moneyness1.getGridData().getForwardCurve(), moneyness2.getGridData().getForwardCurve());
    assertCurveEquals(moneyness1.getForwardCurve(), moneyness2.getForwardCurve());
    assertEquals(moneyness1.getInterpolator(), moneyness2.getInterpolator());
    assertEquals(moneyness1.getSurface(), moneyness2.getSurface());
  }

  private void assert2DArrayEquals(final double[][] a1, final double[][] a2, final double eps) {
    assertEquals(a1.length, a2.length);
    for (int i = 0; i < a1.length; i++) {
      assertArrayEquals(a1[i], a2[i], eps);
    }
  }

  private void assertCurveEquals(final ForwardCurve c1, final ForwardCurve c2) {
    assertEquals(c1.getSpot(), c2.getSpot());
    if (c1 != c2) {
      for (double x = 0.1; x < 3.0; x += 0.02) {
        assertEquals(c1.getForward(x), c2.getForward(x));
        assertEquals(c1.getDrift(x), c2.getDrift(x));
      }
    }
  }

}
