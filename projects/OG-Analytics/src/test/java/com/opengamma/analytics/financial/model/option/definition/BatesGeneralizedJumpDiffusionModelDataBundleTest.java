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
public class BatesGeneralizedJumpDiffusionModelDataBundleTest {
  private static final double R = 0.05;
  private static final double SIGMA = 0.5;
  private static final YieldAndDiscountCurve CURVE = YieldCurve.from(ConstantDoublesCurve.from(R));
  private static final YieldAndDiscountCurve OTHER_CURVE = YieldCurve.from(ConstantDoublesCurve.from(0.1));
  private static final VolatilitySurface SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(SIGMA));
  private static final VolatilitySurface OTHER_SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(0.55));
  private static final double B = 0.02;
  private static final double OTHER_B = 0.03;
  private static final double SPOT = 100;
  private static final double OTHER_SPOT = 110;
  private static final double LAMBDA = 0.3;
  private static final double OTHER_LAMBDA = 0.4;
  private static final double JUMP = 0.23;
  private static final double OTHER_JUMP = 0.45;
  private static final double DELTA = 0.9;
  private static final double OTHER_DELTA = 0.86;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 5, 1);
  private static final ZonedDateTime OTHER_DATE = DateUtils.getUTCDate(2010, 6, 1);
  private static final BatesGeneralizedJumpDiffusionModelDataBundle DATA = new BatesGeneralizedJumpDiffusionModelDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, JUMP, DELTA);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBundle() {
    new BatesGeneralizedJumpDiffusionModelDataBundle(null);
  }

  @Test
  public void testGetters() {
    assertEquals(DATA.getCostOfCarry(), B, 0);
    assertEquals(DATA.getDate(), DATE);
    assertEquals(DATA.getDelta(), DELTA, 0);
    assertEquals(DATA.getInterestRateCurve(), CURVE);
    assertEquals(DATA.getExpectedJumpSize(), JUMP, 0);
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
    final BatesGeneralizedJumpDiffusionModelDataBundle data1 = new BatesGeneralizedJumpDiffusionModelDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, JUMP, DELTA);
    final BatesGeneralizedJumpDiffusionModelDataBundle data2 = new BatesGeneralizedJumpDiffusionModelDataBundle(DATA);
    final BatesGeneralizedJumpDiffusionModelDataBundle data3 = new BatesGeneralizedJumpDiffusionModelDataBundle(new StandardOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE), LAMBDA, JUMP, DELTA);
    assertEquals(DATA, data1);
    assertEquals(DATA, data2);
    assertEquals(DATA, data3);
    assertEquals(DATA.hashCode(), data1.hashCode());
    assertEquals(DATA.hashCode(), data2.hashCode());
    assertEquals(DATA.hashCode(), data3.hashCode());
    assertFalse(DATA.equals(new BatesGeneralizedJumpDiffusionModelDataBundle(OTHER_CURVE, B, SURFACE, SPOT, DATE, LAMBDA, JUMP, DELTA)));
    assertFalse(DATA.equals(new BatesGeneralizedJumpDiffusionModelDataBundle(CURVE, OTHER_B, SURFACE, SPOT, DATE, LAMBDA, JUMP, DELTA)));
    assertFalse(DATA.equals(new BatesGeneralizedJumpDiffusionModelDataBundle(CURVE, B, OTHER_SURFACE, SPOT, DATE, LAMBDA, JUMP, DELTA)));
    assertFalse(DATA.equals(new BatesGeneralizedJumpDiffusionModelDataBundle(CURVE, B, SURFACE, OTHER_SPOT, DATE, LAMBDA, JUMP, DELTA)));
    assertFalse(DATA.equals(new BatesGeneralizedJumpDiffusionModelDataBundle(CURVE, B, SURFACE, SPOT, OTHER_DATE, OTHER_LAMBDA, JUMP, DELTA)));
    assertFalse(DATA.equals(new BatesGeneralizedJumpDiffusionModelDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, OTHER_JUMP, DELTA)));
    assertFalse(DATA.equals(new BatesGeneralizedJumpDiffusionModelDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, JUMP, OTHER_DELTA)));
  }

  @Test
  public void testBuilders() {
    assertEquals(new BatesGeneralizedJumpDiffusionModelDataBundle(OTHER_CURVE, B, SURFACE, SPOT, DATE, LAMBDA, JUMP, DELTA), DATA.withInterestRateCurve(OTHER_CURVE));
    assertEquals(new BatesGeneralizedJumpDiffusionModelDataBundle(CURVE, OTHER_B, SURFACE, SPOT, DATE, LAMBDA, JUMP, DELTA), DATA.withCostOfCarry(OTHER_B));
    assertEquals(new BatesGeneralizedJumpDiffusionModelDataBundle(CURVE, B, OTHER_SURFACE, SPOT, DATE, LAMBDA, JUMP, DELTA), DATA.withVolatilitySurface(OTHER_SURFACE));
    assertEquals(new BatesGeneralizedJumpDiffusionModelDataBundle(CURVE, B, SURFACE, OTHER_SPOT, DATE, LAMBDA, JUMP, DELTA), DATA.withSpot(OTHER_SPOT));
    assertEquals(new BatesGeneralizedJumpDiffusionModelDataBundle(CURVE, B, SURFACE, SPOT, OTHER_DATE, LAMBDA, JUMP, DELTA), DATA.withDate(OTHER_DATE));
    assertEquals(new BatesGeneralizedJumpDiffusionModelDataBundle(CURVE, B, SURFACE, SPOT, DATE, OTHER_LAMBDA, JUMP, DELTA), DATA.withLambda(OTHER_LAMBDA));
    assertEquals(new BatesGeneralizedJumpDiffusionModelDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, OTHER_JUMP, DELTA), DATA.withExpectedJumpSize(OTHER_JUMP));
    assertEquals(new BatesGeneralizedJumpDiffusionModelDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, JUMP, OTHER_DELTA), DATA.withDelta(OTHER_DELTA));
  }
}
