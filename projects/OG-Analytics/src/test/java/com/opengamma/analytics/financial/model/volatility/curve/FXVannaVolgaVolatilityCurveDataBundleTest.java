/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.curve;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class FXVannaVolgaVolatilityCurveDataBundleTest {
  private static final double DELTA = 0.1;
  private static final double RR = 0.01;
  private static final double ATM = 0.2;
  private static final double VWB = 0.05;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 7, 1);
  private static final FXVannaVolgaVolatilityCurveDataBundle DATA = new FXVannaVolgaVolatilityCurveDataBundle(DELTA, RR, ATM, VWB, DATE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMaturity() {
    new FXVannaVolgaVolatilityCurveDataBundle(DELTA, RR, ATM, VWB, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeATMVol() {
    new FXVannaVolgaVolatilityCurveDataBundle(DELTA, RR, -ATM, VWB, DATE);
  }

  @Test
  public void testGetters() {
    assertEquals(ATM, DATA.getAtTheMoney(), 0);
    assertEquals(DELTA, DATA.getDelta(), 0);
    assertEquals(DATE, DATA.getMaturity());
    assertEquals(RR, DATA.getRiskReversal(), 0);
    assertEquals(VWB, DATA.getVegaWeightedButterfly(), 0);
  }

  @Test
  public void testEqualsAndHashCode() {
    FXVannaVolgaVolatilityCurveDataBundle other = new FXVannaVolgaVolatilityCurveDataBundle(DELTA, RR, ATM, VWB, DATE);
    assertEquals(DATA, other);
    assertEquals(DATA.hashCode(), other.hashCode());
    other = new FXVannaVolgaVolatilityCurveDataBundle(-DELTA, RR, ATM, VWB, DATE);
    assertFalse(other.equals(DATA));
    other = new FXVannaVolgaVolatilityCurveDataBundle(DELTA, -RR, ATM, VWB, DATE);
    assertFalse(other.equals(DATA));
    other = new FXVannaVolgaVolatilityCurveDataBundle(DELTA, RR, ATM + 1, VWB, DATE);
    assertFalse(other.equals(DATA));
    other = new FXVannaVolgaVolatilityCurveDataBundle(DELTA, RR, ATM, -VWB, DATE);
    assertFalse(other.equals(DATA));
    other = new FXVannaVolgaVolatilityCurveDataBundle(DELTA, RR, ATM, VWB, DateUtils.getDateOffsetWithYearFraction(DATE, 1));
    assertFalse(other.equals(DATA));
  }
}
