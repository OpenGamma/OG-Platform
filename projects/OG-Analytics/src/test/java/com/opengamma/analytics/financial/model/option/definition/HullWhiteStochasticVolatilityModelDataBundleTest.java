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
public class HullWhiteStochasticVolatilityModelDataBundleTest {
  private static final double R = 0.03;
  private static final double SIGMA = 0.3;
  private static final YieldAndDiscountCurve CURVE = YieldCurve.from(ConstantDoublesCurve.from(R));
  private static final YieldAndDiscountCurve OTHER_CURVE = YieldCurve.from(ConstantDoublesCurve.from(0.2));
  private static final VolatilitySurface SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(SIGMA));
  private static final VolatilitySurface OTHER_SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(0.25));
  private static final double B = 0.01;
  private static final double OTHER_B = 0.02;
  private static final double SPOT = 100;
  private static final double OTHER_SPOT = 99;
  private static final double LAMBDA = 0.1;
  private static final double OTHER_LAMBDA = 0.2;
  private static final double SIGMA_LR = 0.3;
  private static final double OTHER_SIGMA_LR = 0.4;
  private static final double VOL_OF_VOL = 0.6;
  private static final double OTHER_VOL_OF_VOL = 0.7;
  private static final double RHO = 0.5;
  private static final double OTHER_RHO = -0.5;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 5, 1);
  private static final ZonedDateTime OTHER_DATE = DateUtils.getUTCDate(2010, 6, 1);
  private static final HullWhiteStochasticVolatilityModelDataBundle DATA = new HullWhiteStochasticVolatilityModelDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, SIGMA_LR, VOL_OF_VOL, RHO);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBundle() {
    new HullWhiteStochasticVolatilityModelDataBundle(null);
  }

  @Test
  public void testGetters() {
    assertEquals(DATA.getCorrelation(), RHO, 0);
    assertEquals(DATA.getCostOfCarry(), B, 0);
    assertEquals(DATA.getDate(), DATE);
    assertEquals(DATA.getInterestRateCurve(), CURVE);
    assertEquals(DATA.getHalfLife(), LAMBDA, 0);
    assertEquals(DATA.getLongRunVolatility(), SIGMA_LR, 0);
    assertEquals(DATA.getSpot(), SPOT, 0);
    assertEquals(DATA.getVolatilityOfVolatility(), VOL_OF_VOL, 0);
    assertEquals(DATA.getVolatilitySurface(), SURFACE);
  }

  @Test
  public void testGetData() {
    assertEquals(DATA.getInterestRate(Math.random()), R, 0);
    assertEquals(DATA.getVolatility(Math.random(), Math.random()), SIGMA, 0);
  }

  @Test
  public void testEqualsAndHashCode() {
    final HullWhiteStochasticVolatilityModelDataBundle data1 = new HullWhiteStochasticVolatilityModelDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, SIGMA_LR, VOL_OF_VOL, RHO);
    final HullWhiteStochasticVolatilityModelDataBundle data2 = new HullWhiteStochasticVolatilityModelDataBundle(DATA);
    final HullWhiteStochasticVolatilityModelDataBundle data3 = new HullWhiteStochasticVolatilityModelDataBundle(new StandardOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE), LAMBDA, SIGMA_LR,
        VOL_OF_VOL, RHO);
    assertEquals(DATA, data1);
    assertEquals(DATA, data2);
    assertEquals(DATA, data3);
    assertEquals(DATA.hashCode(), data1.hashCode());
    assertEquals(DATA.hashCode(), data2.hashCode());
    assertEquals(DATA.hashCode(), data3.hashCode());
    assertFalse(DATA.equals(new HullWhiteStochasticVolatilityModelDataBundle(OTHER_CURVE, B, SURFACE, SPOT, DATE, LAMBDA, SIGMA_LR, VOL_OF_VOL, RHO)));
    assertFalse(DATA.equals(new HullWhiteStochasticVolatilityModelDataBundle(CURVE, OTHER_B, SURFACE, SPOT, DATE, LAMBDA, SIGMA_LR, VOL_OF_VOL, RHO)));
    assertFalse(DATA.equals(new HullWhiteStochasticVolatilityModelDataBundle(CURVE, B, OTHER_SURFACE, SPOT, DATE, LAMBDA, SIGMA_LR, VOL_OF_VOL, RHO)));
    assertFalse(DATA.equals(new HullWhiteStochasticVolatilityModelDataBundle(CURVE, B, SURFACE, OTHER_SPOT, DATE, LAMBDA, SIGMA_LR, VOL_OF_VOL, RHO)));
    assertFalse(DATA.equals(new HullWhiteStochasticVolatilityModelDataBundle(CURVE, B, SURFACE, SPOT, OTHER_DATE, OTHER_LAMBDA, SIGMA_LR, VOL_OF_VOL, RHO)));
    assertFalse(DATA.equals(new HullWhiteStochasticVolatilityModelDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, OTHER_SIGMA_LR, VOL_OF_VOL, RHO)));
    assertFalse(DATA.equals(new HullWhiteStochasticVolatilityModelDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, SIGMA_LR, OTHER_VOL_OF_VOL, RHO)));
    assertFalse(DATA.equals(new HullWhiteStochasticVolatilityModelDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, SIGMA_LR, VOL_OF_VOL, OTHER_RHO)));
  }

  @Test
  public void testBuilders() {
    assertEquals(new HullWhiteStochasticVolatilityModelDataBundle(OTHER_CURVE, B, SURFACE, SPOT, DATE, LAMBDA, SIGMA_LR, VOL_OF_VOL, RHO), DATA.withInterestRateCurve(OTHER_CURVE));
    assertEquals(new HullWhiteStochasticVolatilityModelDataBundle(CURVE, OTHER_B, SURFACE, SPOT, DATE, LAMBDA, SIGMA_LR, VOL_OF_VOL, RHO), DATA.withCostOfCarry(OTHER_B));
    assertEquals(new HullWhiteStochasticVolatilityModelDataBundle(CURVE, B, OTHER_SURFACE, SPOT, DATE, LAMBDA, SIGMA_LR, VOL_OF_VOL, RHO), DATA.withVolatilitySurface(OTHER_SURFACE));
    assertEquals(new HullWhiteStochasticVolatilityModelDataBundle(CURVE, B, SURFACE, OTHER_SPOT, DATE, LAMBDA, SIGMA_LR, VOL_OF_VOL, RHO), DATA.withSpot(OTHER_SPOT));
    assertEquals(new HullWhiteStochasticVolatilityModelDataBundle(CURVE, B, SURFACE, SPOT, OTHER_DATE, LAMBDA, SIGMA_LR, VOL_OF_VOL, RHO), DATA.withDate(OTHER_DATE));
    assertEquals(new HullWhiteStochasticVolatilityModelDataBundle(CURVE, B, SURFACE, SPOT, DATE, OTHER_LAMBDA, SIGMA_LR, VOL_OF_VOL, RHO), DATA.withHalfLife(OTHER_LAMBDA));
    assertEquals(new HullWhiteStochasticVolatilityModelDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, OTHER_SIGMA_LR, VOL_OF_VOL, RHO), DATA.withLongRunVolatility(OTHER_SIGMA_LR));
    assertEquals(new HullWhiteStochasticVolatilityModelDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, SIGMA_LR, OTHER_VOL_OF_VOL, RHO), DATA.withVolatilityOfVolatility(OTHER_VOL_OF_VOL));
    assertEquals(new HullWhiteStochasticVolatilityModelDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, SIGMA_LR, VOL_OF_VOL, OTHER_RHO), DATA.withCorrelation(OTHER_RHO));
  }
}
