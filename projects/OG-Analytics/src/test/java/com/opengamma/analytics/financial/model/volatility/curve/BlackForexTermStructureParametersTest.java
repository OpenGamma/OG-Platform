/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.curve;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.test.TestGroup;

/**
 * Test constructor and volatility provider for BlackForexTermStructureParameters.
 */
@Test(groups = TestGroup.UNIT)
public class BlackForexTermStructureParametersTest {
  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final double[] NODES = new double[] {0.01, 0.50, 1.00, 2.01, 5.00};
  private static final double[] VOL = new double[] {0.20, 0.25, 0.20, 0.15, 0.20};
  private static final InterpolatedDoublesCurve TERM_STRUCTURE_VOL = InterpolatedDoublesCurve.fromSorted(NODES, VOL, LINEAR_FLAT);
  private static final BlackForexTermStructureParameters BLACK_TERM_STRUCTURE = new BlackForexTermStructureParameters(TERM_STRUCTURE_VOL);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurve() {
    new BlackForexTermStructureParameters(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTime() {
    BLACK_TERM_STRUCTURE.getVolatility(null);
  }

  @Test
  public void testObject() {
    assertEquals("Black Forex Term Structure: getter", TERM_STRUCTURE_VOL, BLACK_TERM_STRUCTURE.getVolatilityCurve());
    BlackForexTermStructureParameters other = new BlackForexTermStructureParameters(InterpolatedDoublesCurve.fromSorted(NODES, VOL, LINEAR_FLAT, TERM_STRUCTURE_VOL.getName()));
    assertEquals(BLACK_TERM_STRUCTURE, other);
    assertEquals(BLACK_TERM_STRUCTURE.hashCode(), other.hashCode());
    other = new BlackForexTermStructureParameters(InterpolatedDoublesCurve.fromSorted(NODES, VOL, LINEAR_FLAT));
    assertFalse(BLACK_TERM_STRUCTURE.equals(other));
  }

  /**
   * Tests the volatility.
   */
  @Test
  public void getVolatility() {
    final double[] times = new double[] {0.30, 2.54, 5.0, 10.1};
    for (final double time : times) {
      assertEquals("Black Forex Term Structure: getVolatility", TERM_STRUCTURE_VOL.getYValue(time), BLACK_TERM_STRUCTURE.getVolatility(time));
    }
  }

  @Test
  public void testVolatilitySensitivity() {
    final double[] times = new double[] {0.3, 2.54, 5, 10.1};
    for (final double time : times) {
      assertArrayEquals(TERM_STRUCTURE_VOL.getYValueParameterSensitivity(time), BLACK_TERM_STRUCTURE.getVolatilityTimeSensitivity(time));
    }
  }

}
