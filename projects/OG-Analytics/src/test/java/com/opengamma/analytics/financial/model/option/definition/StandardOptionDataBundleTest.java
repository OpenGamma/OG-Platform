/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class StandardOptionDataBundleTest {
  private static final double R = 0.05;
  private static final double SIGMA = 0.15;
  private static final YieldAndDiscountCurve CURVE = YieldCurve.from(ConstantDoublesCurve.from(R));
  private static final double B = 0.01;
  private static final VolatilitySurface SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(SIGMA));
  private static final double SPOT = 100;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 5, 1);
  private static final YieldAndDiscountCurve OTHER_CURVE = YieldCurve.from(ConstantDoublesCurve.from(R + 1));
  private static final double OTHER_B = B + 1;
  private static final VolatilitySurface OTHER_SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(SIGMA + 1));
  private static final double OTHER_SPOT = SPOT + 1;
  private static final ZonedDateTime OTHER_DATE = DateUtils.getDateOffsetWithYearFraction(DATE, 1);
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBundle() {
    new StandardOptionDataBundle(null);
  }

  @Test
  public void testGetters() {
    assertEquals(DATA.getInterestRateCurve(), CURVE);
    assertEquals(DATA.getCostOfCarry(), B, 0);
    assertEquals(DATA.getDate(), DATE);
    assertEquals(DATA.getSpot(), SPOT, 0);
    assertEquals(DATA.getVolatilitySurface(), SURFACE);
  }

  @Test
  public void testGetInterestRate() {
    for (int i = 0; i < 10; i++) {
      assertEquals(DATA.getInterestRate(Math.random()), R, 1e-15);
    }
  }

  @Test
  public void testGetVolatility() {
    for (int i = 0; i < 10; i++) {
      assertEquals(DATA.getVolatility(Math.random(), Math.random()), SIGMA, 1e-15);
    }
  }

  @Test
  public void testEqualsAndHashCode() {
    final StandardOptionDataBundle other = new StandardOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE);
    assertEquals(DATA, other);
    assertEquals(DATA.hashCode(), other.hashCode());
    assertFalse(DATA.equals(new StandardOptionDataBundle(OTHER_CURVE, B, SURFACE, SPOT, DATE)));
    assertFalse(DATA.equals(new StandardOptionDataBundle(CURVE, OTHER_B, SURFACE, SPOT, DATE)));
    assertFalse(DATA.equals(new StandardOptionDataBundle(CURVE, B, OTHER_SURFACE, SPOT, DATE)));
    assertFalse(DATA.equals(new StandardOptionDataBundle(CURVE, B, SURFACE, OTHER_SPOT, DATE)));
    assertFalse(DATA.equals(new StandardOptionDataBundle(CURVE, B, SURFACE, SPOT, OTHER_DATE)));
  }

  @Test
  public void testBuilders() {
    assertEquals(new StandardOptionDataBundle(OTHER_CURVE, B, SURFACE, SPOT, DATE), DATA.withInterestRateCurve(OTHER_CURVE));
    assertEquals(new StandardOptionDataBundle(CURVE, OTHER_B, SURFACE, SPOT, DATE), DATA.withCostOfCarry(OTHER_B));
    assertEquals(new StandardOptionDataBundle(CURVE, B, OTHER_SURFACE, SPOT, DATE), DATA.withVolatilitySurface(OTHER_SURFACE));
    assertEquals(new StandardOptionDataBundle(CURVE, B, SURFACE, OTHER_SPOT, DATE), DATA.withSpot(OTHER_SPOT));
    assertEquals(new StandardOptionDataBundle(CURVE, B, SURFACE, SPOT, OTHER_DATE), DATA.withDate(OTHER_DATE));
  }
}
