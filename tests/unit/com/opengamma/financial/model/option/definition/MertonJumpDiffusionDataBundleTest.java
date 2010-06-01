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

import com.opengamma.financial.model.interestrate.curve.ConstantInterestRateDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.volatility.surface.ConstantVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class MertonJumpDiffusionDataBundleTest {
  private static final double R = 0.01;
  private static final double SIGMA = 0.3;
  private static final DiscountCurve CURVE = new ConstantInterestRateDiscountCurve(R);
  private static final DiscountCurve OTHER_CURVE = new ConstantInterestRateDiscountCurve(0.015);
  private static final VolatilitySurface SURFACE = new ConstantVolatilitySurface(SIGMA);
  private static final VolatilitySurface OTHER_SURFACE = new ConstantVolatilitySurface(0.6);
  private static final double B = 0.04;
  private static final double OTHER_B = 0.;
  private static final double SPOT = 100;
  private static final double OTHER_SPOT = 105;
  private static final double LAMBDA = 0.1;
  private static final double OTHER_LAMBDA = 0.2;
  private static final double GAMMA = 0.3;
  private static final double OTHER_GAMMA = 0.5;
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 5, 1);
  private static final ZonedDateTime OTHER_DATE = DateUtil.getUTCDate(2011, 5, 1);
  private static final MertonJumpDiffusionModelOptionDataBundle DATA = new MertonJumpDiffusionModelOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, GAMMA);

  @Test(expected = IllegalArgumentException.class)
  public void testNullBundle() {
    new MertonJumpDiffusionModelOptionDataBundle(null);
  }

  @Test
  public void testGetters() {
    assertEquals(DATA.getCostOfCarry(), B, 0);
    assertEquals(DATA.getDate(), DATE);
    assertEquals(DATA.getDiscountCurve(), CURVE);
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
    final MertonJumpDiffusionModelOptionDataBundle data1 = new MertonJumpDiffusionModelOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, GAMMA);
    final MertonJumpDiffusionModelOptionDataBundle data2 = new MertonJumpDiffusionModelOptionDataBundle(DATA);
    assertEquals(DATA, data1);
    assertEquals(DATA, data2);
    assertEquals(DATA.hashCode(), data1.hashCode());
    assertEquals(DATA.hashCode(), data2.hashCode());
    assertFalse(DATA.equals(new MertonJumpDiffusionModelOptionDataBundle(OTHER_CURVE, B, SURFACE, SPOT, DATE, LAMBDA, GAMMA)));
    assertFalse(DATA.equals(new MertonJumpDiffusionModelOptionDataBundle(CURVE, OTHER_B, SURFACE, SPOT, DATE, LAMBDA, GAMMA)));
    assertFalse(DATA.equals(new MertonJumpDiffusionModelOptionDataBundle(CURVE, B, OTHER_SURFACE, SPOT, DATE, LAMBDA, GAMMA)));
    assertFalse(DATA.equals(new MertonJumpDiffusionModelOptionDataBundle(CURVE, B, SURFACE, OTHER_SPOT, DATE, LAMBDA, GAMMA)));
    assertFalse(DATA.equals(new MertonJumpDiffusionModelOptionDataBundle(CURVE, B, SURFACE, SPOT, OTHER_DATE, OTHER_LAMBDA, GAMMA)));
    assertFalse(DATA.equals(new MertonJumpDiffusionModelOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, OTHER_GAMMA)));
  }

  @Test
  public void testBuilders() {
    assertEquals(new MertonJumpDiffusionModelOptionDataBundle(OTHER_CURVE, B, SURFACE, SPOT, DATE, LAMBDA, GAMMA), DATA.withDiscountCurve(OTHER_CURVE));
    assertEquals(new MertonJumpDiffusionModelOptionDataBundle(CURVE, OTHER_B, SURFACE, SPOT, DATE, LAMBDA, GAMMA), DATA.withCostOfCarry(OTHER_B));
    assertEquals(new MertonJumpDiffusionModelOptionDataBundle(CURVE, B, OTHER_SURFACE, SPOT, DATE, LAMBDA, GAMMA), DATA.withVolatilitySurface(OTHER_SURFACE));
    assertEquals(new MertonJumpDiffusionModelOptionDataBundle(CURVE, B, SURFACE, OTHER_SPOT, DATE, LAMBDA, GAMMA), DATA.withSpot(OTHER_SPOT));
    assertEquals(new MertonJumpDiffusionModelOptionDataBundle(CURVE, B, SURFACE, SPOT, OTHER_DATE, LAMBDA, GAMMA), DATA.withDate(OTHER_DATE));
    assertEquals(new MertonJumpDiffusionModelOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE, OTHER_LAMBDA, GAMMA), DATA.withLambda(OTHER_LAMBDA));
    assertEquals(new MertonJumpDiffusionModelOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, OTHER_GAMMA), DATA.withGamma(OTHER_GAMMA));
  }
}
