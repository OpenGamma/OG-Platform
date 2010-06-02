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
public class HullWhiteStochasticVolatilityModelOptionDataBundleTest {
  private static final double R = 0.03;
  private static final double SIGMA = 0.3;
  private static final DiscountCurve CURVE = new ConstantInterestRateDiscountCurve(R);
  private static final DiscountCurve OTHER_CURVE = new ConstantInterestRateDiscountCurve(0.2);
  private static final VolatilitySurface SURFACE = new ConstantVolatilitySurface(SIGMA);
  private static final VolatilitySurface OTHER_SURFACE = new ConstantVolatilitySurface(0.25);
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
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 5, 1);
  private static final ZonedDateTime OTHER_DATE = DateUtil.getUTCDate(2010, 6, 1);
  private static final HullWhiteStochasticVolatilityModelOptionDataBundle DATA = new HullWhiteStochasticVolatilityModelOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, SIGMA_LR, VOL_OF_VOL,
      RHO);

  @Test(expected = IllegalArgumentException.class)
  public void testNullBundle() {
    new HullWhiteStochasticVolatilityModelOptionDataBundle(null);
  }

  @Test
  public void testGetters() {
    assertEquals(DATA.getCorrelation(), RHO, 0);
    assertEquals(DATA.getCostOfCarry(), B, 0);
    assertEquals(DATA.getDate(), DATE);
    assertEquals(DATA.getDiscountCurve(), CURVE);
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
    final HullWhiteStochasticVolatilityModelOptionDataBundle data1 = new HullWhiteStochasticVolatilityModelOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, SIGMA_LR, VOL_OF_VOL, RHO);
    final HullWhiteStochasticVolatilityModelOptionDataBundle data2 = new HullWhiteStochasticVolatilityModelOptionDataBundle(DATA);
    final HullWhiteStochasticVolatilityModelOptionDataBundle data3 = new HullWhiteStochasticVolatilityModelOptionDataBundle(new StandardOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE), LAMBDA,
        SIGMA_LR, VOL_OF_VOL, RHO);
    assertEquals(DATA, data1);
    assertEquals(DATA, data2);
    assertEquals(DATA, data3);
    assertEquals(DATA.hashCode(), data1.hashCode());
    assertEquals(DATA.hashCode(), data2.hashCode());
    assertEquals(DATA.hashCode(), data3.hashCode());
    assertFalse(DATA.equals(new HullWhiteStochasticVolatilityModelOptionDataBundle(OTHER_CURVE, B, SURFACE, SPOT, DATE, LAMBDA, SIGMA_LR, VOL_OF_VOL, RHO)));
    assertFalse(DATA.equals(new HullWhiteStochasticVolatilityModelOptionDataBundle(CURVE, OTHER_B, SURFACE, SPOT, DATE, LAMBDA, SIGMA_LR, VOL_OF_VOL, RHO)));
    assertFalse(DATA.equals(new HullWhiteStochasticVolatilityModelOptionDataBundle(CURVE, B, OTHER_SURFACE, SPOT, DATE, LAMBDA, SIGMA_LR, VOL_OF_VOL, RHO)));
    assertFalse(DATA.equals(new HullWhiteStochasticVolatilityModelOptionDataBundle(CURVE, B, SURFACE, OTHER_SPOT, DATE, LAMBDA, SIGMA_LR, VOL_OF_VOL, RHO)));
    assertFalse(DATA.equals(new HullWhiteStochasticVolatilityModelOptionDataBundle(CURVE, B, SURFACE, SPOT, OTHER_DATE, OTHER_LAMBDA, SIGMA_LR, VOL_OF_VOL, RHO)));
    assertFalse(DATA.equals(new HullWhiteStochasticVolatilityModelOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, OTHER_SIGMA_LR, VOL_OF_VOL, RHO)));
    assertFalse(DATA.equals(new HullWhiteStochasticVolatilityModelOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, SIGMA_LR, OTHER_VOL_OF_VOL, RHO)));
    assertFalse(DATA.equals(new HullWhiteStochasticVolatilityModelOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, SIGMA_LR, VOL_OF_VOL, OTHER_RHO)));
  }

  @Test
  public void testBuilders() {
    assertEquals(new HullWhiteStochasticVolatilityModelOptionDataBundle(OTHER_CURVE, B, SURFACE, SPOT, DATE, LAMBDA, SIGMA_LR, VOL_OF_VOL, RHO), DATA.withDiscountCurve(OTHER_CURVE));
    assertEquals(new HullWhiteStochasticVolatilityModelOptionDataBundle(CURVE, OTHER_B, SURFACE, SPOT, DATE, LAMBDA, SIGMA_LR, VOL_OF_VOL, RHO), DATA.withCostOfCarry(OTHER_B));
    assertEquals(new HullWhiteStochasticVolatilityModelOptionDataBundle(CURVE, B, OTHER_SURFACE, SPOT, DATE, LAMBDA, SIGMA_LR, VOL_OF_VOL, RHO), DATA.withVolatilitySurface(OTHER_SURFACE));
    assertEquals(new HullWhiteStochasticVolatilityModelOptionDataBundle(CURVE, B, SURFACE, OTHER_SPOT, DATE, LAMBDA, SIGMA_LR, VOL_OF_VOL, RHO), DATA.withSpot(OTHER_SPOT));
    assertEquals(new HullWhiteStochasticVolatilityModelOptionDataBundle(CURVE, B, SURFACE, SPOT, OTHER_DATE, LAMBDA, SIGMA_LR, VOL_OF_VOL, RHO), DATA.withDate(OTHER_DATE));
    assertEquals(new HullWhiteStochasticVolatilityModelOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE, OTHER_LAMBDA, SIGMA_LR, VOL_OF_VOL, RHO), DATA.withHalfLife(OTHER_LAMBDA));
    assertEquals(new HullWhiteStochasticVolatilityModelOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, OTHER_SIGMA_LR, VOL_OF_VOL, RHO), DATA.withLongRunVolatility(OTHER_SIGMA_LR));
    assertEquals(new HullWhiteStochasticVolatilityModelOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, SIGMA_LR, OTHER_VOL_OF_VOL, RHO), DATA.withVolatilityOfVolatility(OTHER_VOL_OF_VOL));
    assertEquals(new HullWhiteStochasticVolatilityModelOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, SIGMA_LR, VOL_OF_VOL, OTHER_RHO), DATA.withCorrelation(OTHER_RHO));
  }
}
