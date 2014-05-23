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
public class SABRDataBundleTest {
  private static final YieldCurve CURVE = YieldCurve.from(ConstantDoublesCurve.from(0.03));
  private static final YieldCurve OTHER_CURVE = YieldCurve.from(ConstantDoublesCurve.from(0.035));
  private static final double B = 0.01;
  private static final double OTHER_B = 0.02;
  private static final VolatilitySurface SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(0.4));
  private static final VolatilitySurface OTHER_SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(0.45));
  private static final double SPOT = 100;
  private static final double OTHER_SPOT = 110;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 8, 1);
  private static final ZonedDateTime OTHER_DATE = DateUtils.getUTCDate(2010, 9, 1);
  private static final double ALPHA = 0.5;
  private static final double OTHER_ALPHA = 0.55;
  private static final double BETA = 1;
  private static final double OTHER_BETA = 0.4;
  private static final double RHO = -0.5;
  private static final double OTHER_RHO = 0.5;
  private static final double KSI = 0.1;
  private static final double OTHER_KSI = 0.11;
  private static final SABRDataBundle DATA = new SABRDataBundle(CURVE, B, SURFACE, SPOT, DATE, ALPHA, BETA, RHO, KSI);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadCorrelation1() {
    new SABRDataBundle(CURVE, B, SURFACE, SPOT, DATE, ALPHA, BETA, -2, KSI);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadCorrelation2() {
    new SABRDataBundle(new StandardOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE), ALPHA, BETA, -2, KSI);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadCorrelation3() {
    new SABRDataBundle(CURVE, B, SURFACE, SPOT, DATE, ALPHA, BETA, 2, KSI);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadCorrelation4() {
    new SABRDataBundle(new StandardOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE), ALPHA, BETA, -2, KSI);
  }

  @Test
  public void testGetters() {
    assertEquals(DATA.getAlpha(), ALPHA, 0);
    assertEquals(DATA.getBeta(), BETA, 0);
    assertEquals(DATA.getCostOfCarry(), B, 0);
    assertEquals(DATA.getDate(), DATE);
    assertEquals(DATA.getInterestRateCurve(), CURVE);
    assertEquals(DATA.getRho(), RHO, 0);
    assertEquals(DATA.getSpot(), SPOT, 0);
    assertEquals(DATA.getVolatilitySurface(), SURFACE);
    assertEquals(DATA.getVolOfVol(), KSI, 0);
  }

  @Test
  public void testHashCodeEqualsAndBuilders() {
    SABRDataBundle other = new SABRDataBundle(CURVE, B, SURFACE, SPOT, DATE, ALPHA, BETA, RHO, KSI);
    assertEquals(DATA, other);
    assertEquals(DATA.hashCode(), other.hashCode());
    other = new SABRDataBundle(DATA);
    assertEquals(DATA, other);
    assertEquals(DATA.hashCode(), other.hashCode());
    other = new SABRDataBundle(new StandardOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE), ALPHA, BETA, RHO, KSI);
    assertEquals(DATA, other);
    assertEquals(DATA.hashCode(), other.hashCode());
    other = new SABRDataBundle(OTHER_CURVE, B, SURFACE, SPOT, DATE, ALPHA, BETA, RHO, KSI);
    assertFalse(other.equals(DATA));
    assertEquals(other, DATA.withInterestRateCurve(OTHER_CURVE));
    other = new SABRDataBundle(CURVE, OTHER_B, SURFACE, SPOT, DATE, ALPHA, BETA, RHO, KSI);
    assertFalse(other.equals(DATA));
    assertEquals(other, DATA.withCostOfCarry(OTHER_B));
    other = new SABRDataBundle(CURVE, B, OTHER_SURFACE, SPOT, DATE, ALPHA, BETA, RHO, KSI);
    assertFalse(other.equals(DATA));
    assertEquals(other, DATA.withVolatilitySurface(OTHER_SURFACE));
    other = new SABRDataBundle(CURVE, B, SURFACE, OTHER_SPOT, DATE, ALPHA, BETA, RHO, KSI);
    assertFalse(other.equals(DATA));
    assertEquals(other, DATA.withSpot(OTHER_SPOT));
    other = new SABRDataBundle(CURVE, B, SURFACE, SPOT, OTHER_DATE, ALPHA, BETA, RHO, KSI);
    assertFalse(other.equals(DATA));
    assertEquals(other, DATA.withDate(OTHER_DATE));
    other = new SABRDataBundle(CURVE, B, SURFACE, SPOT, DATE, OTHER_ALPHA, BETA, RHO, KSI);
    assertFalse(other.equals(DATA));
    assertEquals(other, DATA.withAlpha(OTHER_ALPHA));
    other = new SABRDataBundle(CURVE, B, SURFACE, SPOT, DATE, ALPHA, OTHER_BETA, RHO, KSI);
    assertFalse(other.equals(DATA));
    assertEquals(other, DATA.withBeta(OTHER_BETA));
    other = new SABRDataBundle(CURVE, B, SURFACE, SPOT, DATE, ALPHA, BETA, OTHER_RHO, KSI);
    assertFalse(other.equals(DATA));
    assertEquals(other, DATA.withRho(OTHER_RHO));
    other = new SABRDataBundle(CURVE, B, SURFACE, SPOT, DATE, ALPHA, BETA, RHO, OTHER_KSI);
    assertFalse(other.equals(DATA));
    assertEquals(other, DATA.withVolOfVol(OTHER_KSI));
  }
}
