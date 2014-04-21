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
public class MertonJumpDiffusionModelDataBundleTest {
  private static final double R = 0.01;
  private static final double SIGMA = 0.3;
  private static final YieldAndDiscountCurve CURVE = YieldCurve.from(ConstantDoublesCurve.from(R));
  private static final YieldAndDiscountCurve OTHER_CURVE = YieldCurve.from(ConstantDoublesCurve.from(0.015));
  private static final VolatilitySurface SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(SIGMA));
  private static final VolatilitySurface OTHER_SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(0.6));
  private static final double B = 0.04;
  private static final double OTHER_B = 0.;
  private static final double SPOT = 100;
  private static final double OTHER_SPOT = 105;
  private static final double LAMBDA = 0.1;
  private static final double OTHER_LAMBDA = 0.2;
  private static final double GAMMA = 0.3;
  private static final double OTHER_GAMMA = 0.5;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 5, 1);
  private static final ZonedDateTime OTHER_DATE = DateUtils.getUTCDate(2011, 5, 1);
  private static final MertonJumpDiffusionModelDataBundle DATA = new MertonJumpDiffusionModelDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, GAMMA);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBundle() {
    new MertonJumpDiffusionModelDataBundle(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testZeroLambda1() {
    new MertonJumpDiffusionModelDataBundle(CURVE, B, SURFACE, SPOT, DATE, 0, GAMMA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testZeroLambda2() {
    new MertonJumpDiffusionModelDataBundle(new StandardOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE), 0, GAMMA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testZeroLambda3() {
    DATA.withLambda(0);
  }

  @Test
  public void testGetters() {
    assertEquals(DATA.getCostOfCarry(), B, 0);
    assertEquals(DATA.getDate(), DATE);
    assertEquals(DATA.getInterestRateCurve(), CURVE);
    assertEquals(DATA.getGamma(), GAMMA, 0);
    assertEquals(DATA.getLambda(), LAMBDA, 0);
    assertEquals(DATA.getSpot(), SPOT, 0);
    assertEquals(DATA.getVolatilitySurface(), SURFACE);
  }

  @Test
  public void testGetData() {
    assertEquals(DATA.getInterestRate(Math.random()), R, 0);
    assertEquals(DATA.getVolatility(Math.random(), Math.random()), SIGMA, 0);
  }

  @Test
  public void testEqualsAndHashCode() {
    final MertonJumpDiffusionModelDataBundle data1 = new MertonJumpDiffusionModelDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, GAMMA);
    final MertonJumpDiffusionModelDataBundle data2 = new MertonJumpDiffusionModelDataBundle(DATA);
    final MertonJumpDiffusionModelDataBundle data3 = new MertonJumpDiffusionModelDataBundle(new StandardOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE), LAMBDA, GAMMA);
    assertEquals(DATA, data1);
    assertEquals(DATA, data2);
    assertEquals(DATA, data3);
    assertEquals(DATA.hashCode(), data1.hashCode());
    assertEquals(DATA.hashCode(), data2.hashCode());
    assertEquals(DATA.hashCode(), data3.hashCode());
    assertFalse(DATA.equals(new MertonJumpDiffusionModelDataBundle(OTHER_CURVE, B, SURFACE, SPOT, DATE, LAMBDA, GAMMA)));
    assertFalse(DATA.equals(new MertonJumpDiffusionModelDataBundle(CURVE, OTHER_B, SURFACE, SPOT, DATE, LAMBDA, GAMMA)));
    assertFalse(DATA.equals(new MertonJumpDiffusionModelDataBundle(CURVE, B, OTHER_SURFACE, SPOT, DATE, LAMBDA, GAMMA)));
    assertFalse(DATA.equals(new MertonJumpDiffusionModelDataBundle(CURVE, B, SURFACE, OTHER_SPOT, DATE, LAMBDA, GAMMA)));
    assertFalse(DATA.equals(new MertonJumpDiffusionModelDataBundle(CURVE, B, SURFACE, SPOT, OTHER_DATE, OTHER_LAMBDA, GAMMA)));
    assertFalse(DATA.equals(new MertonJumpDiffusionModelDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, OTHER_GAMMA)));
  }

  @Test
  public void testBuilders() {
    assertEquals(new MertonJumpDiffusionModelDataBundle(OTHER_CURVE, B, SURFACE, SPOT, DATE, LAMBDA, GAMMA), DATA.withInterestRateCurve(OTHER_CURVE));
    assertEquals(new MertonJumpDiffusionModelDataBundle(CURVE, OTHER_B, SURFACE, SPOT, DATE, LAMBDA, GAMMA), DATA.withCostOfCarry(OTHER_B));
    assertEquals(new MertonJumpDiffusionModelDataBundle(CURVE, B, OTHER_SURFACE, SPOT, DATE, LAMBDA, GAMMA), DATA.withVolatilitySurface(OTHER_SURFACE));
    assertEquals(new MertonJumpDiffusionModelDataBundle(CURVE, B, SURFACE, OTHER_SPOT, DATE, LAMBDA, GAMMA), DATA.withSpot(OTHER_SPOT));
    assertEquals(new MertonJumpDiffusionModelDataBundle(CURVE, B, SURFACE, SPOT, OTHER_DATE, LAMBDA, GAMMA), DATA.withDate(OTHER_DATE));
    assertEquals(new MertonJumpDiffusionModelDataBundle(CURVE, B, SURFACE, SPOT, DATE, OTHER_LAMBDA, GAMMA), DATA.withLambda(OTHER_LAMBDA));
    assertEquals(new MertonJumpDiffusionModelDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, OTHER_GAMMA), DATA.withGamma(OTHER_GAMMA));
  }
}
