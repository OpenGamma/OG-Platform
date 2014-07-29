/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.curve.VolatilityCurve;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class HullWhiteOneFactorDataBundleTest {
  private static final double R = 0.04;
  private static final double SIGMA = 0.2;
  private static final double SPEED = 0.1;
  private static final YieldAndDiscountCurve R_CURVE = YieldCurve.from(ConstantDoublesCurve.from(R));
  private static final VolatilityCurve SIGMA_CURVE = new VolatilityCurve(ConstantDoublesCurve.from(SIGMA));
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 7, 1);
  private static final HullWhiteOneFactorDataBundle DATA = new HullWhiteOneFactorDataBundle(R_CURVE, SIGMA_CURVE, DATE, SPEED);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullYieldCurve() {
    new HullWhiteOneFactorDataBundle(null, SIGMA_CURVE, DATE, SPEED);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVolatilityCurve() {
    new HullWhiteOneFactorDataBundle(R_CURVE, null, DATE, SPEED);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDate() {
    new HullWhiteOneFactorDataBundle(R_CURVE, SIGMA_CURVE, null, SPEED);
  }

  @Test
  public void testGetters() {
    assertEquals(DATA.getShortRateCurve(), R_CURVE);
    assertEquals(DATA.getShortRateVolatilityCurve(), SIGMA_CURVE);
    assertEquals(DATA.getDate(), DATE);
    assertEquals(DATA.getReversionSpeed(), SPEED, 0);
    final double t = 0.2;
    assertEquals(DATA.getShortRate(t), R_CURVE.getInterestRate(t), 1e-15);
    assertEquals(DATA.getShortRateVolatility(t), SIGMA_CURVE.getVolatility(t), 1e-15);
  }

  @Test
  public void testHashCodeAndEquals() {
    HullWhiteOneFactorDataBundle other = new HullWhiteOneFactorDataBundle(R_CURVE, SIGMA_CURVE, DATE, SPEED);
    assertEquals(other, DATA);
    assertEquals(other.hashCode(), DATA.hashCode());
    other = new HullWhiteOneFactorDataBundle(YieldCurve.from(ConstantDoublesCurve.from(R + 0.01)), SIGMA_CURVE, DATE, SPEED);
    assertFalse(other.equals(DATA));
    other = new HullWhiteOneFactorDataBundle(R_CURVE, new VolatilityCurve(ConstantDoublesCurve.from(SIGMA + 0.1)), DATE, SPEED);
    assertFalse(other.equals(DATA));
    other = new HullWhiteOneFactorDataBundle(R_CURVE, SIGMA_CURVE, DATE.minusDays(2), SPEED);
    assertFalse(other.equals(DATA));
    other = new HullWhiteOneFactorDataBundle(R_CURVE, SIGMA_CURVE, DATE, SPEED + 0.5);
    assertFalse(other.equals(DATA));
  }
}
