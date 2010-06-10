/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.model.interestrate.curve.ConstantYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.volatility.surface.ConstantVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class BatesGeneralizedJumpDiffusionModelOptionDataBundleTest {
  private static final double R = 0.05;
  private static final double SIGMA = 0.5;
  private static final YieldAndDiscountCurve CURVE = new ConstantYieldCurve(R);
  private static final YieldAndDiscountCurve OTHER_CURVE = new ConstantYieldCurve(0.1);
  private static final VolatilitySurface SURFACE = new ConstantVolatilitySurface(SIGMA);
  private static final VolatilitySurface OTHER_SURFACE = new ConstantVolatilitySurface(0.55);
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
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 5, 1);
  private static final ZonedDateTime OTHER_DATE = DateUtil.getUTCDate(2010, 6, 1);
  private static final BatesGeneralizedJumpDiffusionModelOptionDataBundle DATA = new BatesGeneralizedJumpDiffusionModelOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, JUMP, DELTA);

  @Test(expected = IllegalArgumentException.class)
  public void testNullBundle() {
    new BatesGeneralizedJumpDiffusionModelOptionDataBundle(null);
  }

  @Test
  public void testGetters() {
    assertEquals(DATA.getCostOfCarry(), B, 0);
    assertEquals(DATA.getDate(), DATE);
    assertEquals(DATA.getDelta(), DELTA, 0);
    assertEquals(DATA.getDiscountCurve(), CURVE);
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
    final BatesGeneralizedJumpDiffusionModelOptionDataBundle data1 = new BatesGeneralizedJumpDiffusionModelOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, JUMP, DELTA);
    final BatesGeneralizedJumpDiffusionModelOptionDataBundle data2 = new BatesGeneralizedJumpDiffusionModelOptionDataBundle(DATA);
    final BatesGeneralizedJumpDiffusionModelOptionDataBundle data3 = new BatesGeneralizedJumpDiffusionModelOptionDataBundle(new StandardOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE), LAMBDA, JUMP,
        DELTA);
    assertEquals(DATA, data1);
    assertEquals(DATA, data2);
    assertEquals(DATA, data3);
    assertEquals(DATA.hashCode(), data1.hashCode());
    assertEquals(DATA.hashCode(), data2.hashCode());
    assertEquals(DATA.hashCode(), data3.hashCode());
    assertFalse(DATA.equals(new BatesGeneralizedJumpDiffusionModelOptionDataBundle(OTHER_CURVE, B, SURFACE, SPOT, DATE, LAMBDA, JUMP, DELTA)));
    assertFalse(DATA.equals(new BatesGeneralizedJumpDiffusionModelOptionDataBundle(CURVE, OTHER_B, SURFACE, SPOT, DATE, LAMBDA, JUMP, DELTA)));
    assertFalse(DATA.equals(new BatesGeneralizedJumpDiffusionModelOptionDataBundle(CURVE, B, OTHER_SURFACE, SPOT, DATE, LAMBDA, JUMP, DELTA)));
    assertFalse(DATA.equals(new BatesGeneralizedJumpDiffusionModelOptionDataBundle(CURVE, B, SURFACE, OTHER_SPOT, DATE, LAMBDA, JUMP, DELTA)));
    assertFalse(DATA.equals(new BatesGeneralizedJumpDiffusionModelOptionDataBundle(CURVE, B, SURFACE, SPOT, OTHER_DATE, OTHER_LAMBDA, JUMP, DELTA)));
    assertFalse(DATA.equals(new BatesGeneralizedJumpDiffusionModelOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, OTHER_JUMP, DELTA)));
    assertFalse(DATA.equals(new BatesGeneralizedJumpDiffusionModelOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, JUMP, OTHER_DELTA)));
  }

  @Test
  public void testBuilders() {
    assertEquals(new BatesGeneralizedJumpDiffusionModelOptionDataBundle(OTHER_CURVE, B, SURFACE, SPOT, DATE, LAMBDA, JUMP, DELTA), DATA.withDiscountCurve(OTHER_CURVE));
    assertEquals(new BatesGeneralizedJumpDiffusionModelOptionDataBundle(CURVE, OTHER_B, SURFACE, SPOT, DATE, LAMBDA, JUMP, DELTA), DATA.withCostOfCarry(OTHER_B));
    assertEquals(new BatesGeneralizedJumpDiffusionModelOptionDataBundle(CURVE, B, OTHER_SURFACE, SPOT, DATE, LAMBDA, JUMP, DELTA), DATA.withVolatilitySurface(OTHER_SURFACE));
    assertEquals(new BatesGeneralizedJumpDiffusionModelOptionDataBundle(CURVE, B, SURFACE, OTHER_SPOT, DATE, LAMBDA, JUMP, DELTA), DATA.withSpot(OTHER_SPOT));
    assertEquals(new BatesGeneralizedJumpDiffusionModelOptionDataBundle(CURVE, B, SURFACE, SPOT, OTHER_DATE, LAMBDA, JUMP, DELTA), DATA.withDate(OTHER_DATE));
    assertEquals(new BatesGeneralizedJumpDiffusionModelOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE, OTHER_LAMBDA, JUMP, DELTA), DATA.withLambda(OTHER_LAMBDA));
    assertEquals(new BatesGeneralizedJumpDiffusionModelOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, OTHER_JUMP, DELTA), DATA.withExpectedJumpSize(OTHER_JUMP));
    assertEquals(new BatesGeneralizedJumpDiffusionModelOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, JUMP, OTHER_DELTA), DATA.withDelta(OTHER_DELTA));
  }
}
