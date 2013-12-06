/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition.twoasset;

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
public class StandardTwoAssetOptionDataBundleTest {
  private static final YieldAndDiscountCurve CURVE1 = YieldCurve.from(ConstantDoublesCurve.from(0.1));
  private static final YieldAndDiscountCurve CURVE2 = YieldCurve.from(ConstantDoublesCurve.from(0.05));
  private static final double B1 = 0.04;
  private static final double B2 = 0.06;
  private static final VolatilitySurface SURFACE1 = new VolatilitySurface(ConstantDoublesSurface.from(0.2));
  private static final VolatilitySurface SURFACE2 = new VolatilitySurface(ConstantDoublesSurface.from(0.3));
  private static final double SPOT1 = 100;
  private static final double SPOT2 = 90;
  private static final double RHO = 0.5;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 1, 1);
  private static final ZonedDateTime DATE2 = DateUtils.getDateOffsetWithYearFraction(DATE, 1);
  private static final StandardTwoAssetOptionDataBundle DATA = new StandardTwoAssetOptionDataBundle(CURVE1, B1, B2, SURFACE1, SURFACE2, SPOT1, SPOT2, RHO, DATE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    new StandardTwoAssetOptionDataBundle(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLowCorrelation() {
    new StandardTwoAssetOptionDataBundle(CURVE1, B1, B2, SURFACE1, SURFACE2, SPOT1, SPOT2, -1 - RHO, DATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighCorrelation() {
    new StandardTwoAssetOptionDataBundle(CURVE1, B1, B2, SURFACE1, SURFACE2, SPOT1, SPOT2, 1 + RHO, DATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSetLowCorrelation() {
    DATA.withCorrelation(-1 - RHO);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSetHighCorrelation() {
    DATA.withCorrelation(1 + RHO);
  }

  @Test
  public void testGetters() {
    assertEquals(DATA.getCorrelation(), RHO, 0);
    assertEquals(DATA.getDate(), DATE);
    assertEquals(DATA.getFirstCostOfCarry(), B1, 0);
    assertEquals(DATA.getFirstSpot(), SPOT1, 0);
    assertEquals(DATA.getFirstVolatilitySurface(), SURFACE1);
    assertEquals(DATA.getInterestRateCurve(), CURVE1);
    assertEquals(DATA.getSecondCostOfCarry(), B2, 0);
    assertEquals(DATA.getSecondSpot(), SPOT2, 0);
    assertEquals(DATA.getSecondVolatilitySurface(), SURFACE2);
  }

  @Test
  public void test() {
    assertEquals(DATA.getInterestRate(Math.random()), 0.1, 0);
    assertEquals(DATA.getFirstVolatility(Math.random(), Math.random()), 0.2, 0);
    assertEquals(DATA.getSecondVolatility(Math.random(), Math.random()), 0.3, 0);
  }

  @Test
  public void testEqualsAndHashCode() {
    StandardTwoAssetOptionDataBundle other = new StandardTwoAssetOptionDataBundle(CURVE1, B1, B2, SURFACE1, SURFACE2, SPOT1, SPOT2, RHO, DATE);
    assertEquals(other, DATA);
    assertEquals(other.hashCode(), DATA.hashCode());
    other = new StandardTwoAssetOptionDataBundle(DATA);
    assertEquals(other, DATA);
    assertEquals(other.hashCode(), DATA.hashCode());
    other = new StandardTwoAssetOptionDataBundle(CURVE2, B1, B2, SURFACE1, SURFACE2, SPOT1, SPOT2, RHO, DATE);
    assertFalse(other.equals(DATA));
    other = new StandardTwoAssetOptionDataBundle(CURVE1, B1, B1, SURFACE1, SURFACE2, SPOT1, SPOT2, RHO, DATE);
    assertFalse(other.equals(DATA));
    other = new StandardTwoAssetOptionDataBundle(CURVE1, B2, B2, SURFACE1, SURFACE2, SPOT1, SPOT2, RHO, DATE);
    assertFalse(other.equals(DATA));
    other = new StandardTwoAssetOptionDataBundle(CURVE1, B1, B2, SURFACE2, SURFACE2, SPOT1, SPOT2, RHO, DATE);
    assertFalse(other.equals(DATA));
    other = new StandardTwoAssetOptionDataBundle(CURVE1, B1, B2, SURFACE1, SURFACE1, SPOT1, SPOT2, RHO, DATE);
    assertFalse(other.equals(DATA));
    other = new StandardTwoAssetOptionDataBundle(CURVE1, B1, B2, SURFACE1, SURFACE2, SPOT2, SPOT2, RHO, DATE);
    assertFalse(other.equals(DATA));
    other = new StandardTwoAssetOptionDataBundle(CURVE1, B1, B2, SURFACE1, SURFACE2, SPOT1, SPOT1, RHO, DATE);
    assertFalse(other.equals(DATA));
    other = new StandardTwoAssetOptionDataBundle(CURVE1, B1, B2, SURFACE1, SURFACE2, SPOT1, SPOT2, RHO / 2, DATE);
    assertFalse(other.equals(DATA));
    other = new StandardTwoAssetOptionDataBundle(CURVE1, B1, B2, SURFACE1, SURFACE2, SPOT1, SPOT2, RHO, DATE2);
    assertFalse(other.equals(DATA));
  }

  @Test
  public void testBuilders() {
    StandardTwoAssetOptionDataBundle other = DATA.withCorrelation(RHO / 2);
    assertEquals(other, new StandardTwoAssetOptionDataBundle(CURVE1, B1, B2, SURFACE1, SURFACE2, SPOT1, SPOT2, RHO / 2, DATE));
    other = DATA.withDate(DATE2);
    assertEquals(other, new StandardTwoAssetOptionDataBundle(CURVE1, B1, B2, SURFACE1, SURFACE2, SPOT1, SPOT2, RHO, DATE2));
    other = DATA.withFirstCostOfCarry(B2);
    assertEquals(other, new StandardTwoAssetOptionDataBundle(CURVE1, B2, B2, SURFACE1, SURFACE2, SPOT1, SPOT2, RHO, DATE));
    other = DATA.withFirstSpot(SPOT2);
    assertEquals(other, new StandardTwoAssetOptionDataBundle(CURVE1, B1, B2, SURFACE1, SURFACE2, SPOT2, SPOT2, RHO, DATE));
    other = DATA.withFirstVolatilitySurface(SURFACE2);
    assertEquals(other, new StandardTwoAssetOptionDataBundle(CURVE1, B1, B2, SURFACE2, SURFACE2, SPOT1, SPOT2, RHO, DATE));
    other = DATA.withInterestRateCurve(CURVE2);
    assertEquals(other, new StandardTwoAssetOptionDataBundle(CURVE2, B1, B2, SURFACE1, SURFACE2, SPOT1, SPOT2, RHO, DATE));
    other = DATA.withSecondCostOfCarry(B1);
    assertEquals(other, new StandardTwoAssetOptionDataBundle(CURVE1, B1, B1, SURFACE1, SURFACE2, SPOT1, SPOT2, RHO, DATE));
    other = DATA.withSecondSpot(SPOT1);
    assertEquals(other, new StandardTwoAssetOptionDataBundle(CURVE1, B1, B2, SURFACE1, SURFACE2, SPOT1, SPOT1, RHO, DATE));
    other = DATA.withSecondVolatilitySurface(SURFACE1);
    assertEquals(other, new StandardTwoAssetOptionDataBundle(CURVE1, B1, B2, SURFACE1, SURFACE1, SPOT1, SPOT2, RHO, DATE));
  }
}
