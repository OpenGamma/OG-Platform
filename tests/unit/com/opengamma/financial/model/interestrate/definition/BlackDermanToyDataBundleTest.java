/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.definition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.model.interestrate.curve.ConstantYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.volatility.curve.ConstantVolatilityCurve;
import com.opengamma.financial.model.volatility.curve.VolatilityCurve;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class BlackDermanToyDataBundleTest {
  private static final double R = 0.04;
  private static final double SIGMA = 0.2;
  private static final YieldAndDiscountCurve R_CURVE = new ConstantYieldCurve(R);
  private static final VolatilityCurve SIGMA_CURVE = new ConstantVolatilityCurve(SIGMA);
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 7, 1);
  private static final BlackDermanToyDataBundle DATA = new BlackDermanToyDataBundle(R_CURVE, SIGMA_CURVE, DATE);

  @Test(expected = IllegalArgumentException.class)
  public void testNullYieldCurve() {
    new BlackDermanToyDataBundle(null, SIGMA_CURVE, DATE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullVolatilityCurve() {
    new BlackDermanToyDataBundle(R_CURVE, null, DATE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullDate() {
    new BlackDermanToyDataBundle(R_CURVE, SIGMA_CURVE, null);
  }

  @Test
  public void testGetters() {
    assertEquals(DATA.getYieldCurve(), R_CURVE);
    assertEquals(DATA.getVolatilityCurve(), SIGMA_CURVE);
    assertEquals(DATA.getDate(), DATE);
    double t = 0.2;
    assertEquals(DATA.getInterestRate(t), R_CURVE.getInterestRate(t), 1e-15);
    assertEquals(DATA.getVolatility(t), SIGMA_CURVE.getVolatility(t), 1e-15);
  }

  @Test
  public void testHashCodeAndEquals() {
    BlackDermanToyDataBundle other = new BlackDermanToyDataBundle(R_CURVE, SIGMA_CURVE, DATE);
    assertEquals(other, DATA);
    assertEquals(other.hashCode(), DATA.hashCode());
    other = new BlackDermanToyDataBundle(new ConstantYieldCurve(R + 0.01), SIGMA_CURVE, DATE);
    assertFalse(other.equals(DATA));
    other = new BlackDermanToyDataBundle(R_CURVE, new ConstantVolatilityCurve(SIGMA + 0.1), DATE);
    assertFalse(other.equals(DATA));
    other = new BlackDermanToyDataBundle(R_CURVE, SIGMA_CURVE, DATE.minusDays(2));
    assertFalse(other.equals(DATA));
  }
}
