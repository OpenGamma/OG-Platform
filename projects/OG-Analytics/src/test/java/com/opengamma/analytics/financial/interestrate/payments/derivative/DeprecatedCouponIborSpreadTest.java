/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class DeprecatedCouponIborSpreadTest {
  private static final double PAYMENT_TIME = 0.67;
  private static final double RESET_TIME = 0.25;
  private static final double MATURITY = 0.52;
  private static final double PAYMENT_YEAR_FRACTION = 0.25;
  private static final double FORWARD_YEAR_FRACTION = 0.27;
  private static final double FIXING_PERIOD_START_TIME = 0.25;
  private static final double FIXING_PERIOD_END_TIME = 0.52;
  private static final double NOTIONAL = 10000.0;
  private static final String FUNDING_CURVE_NAME = "funding";
  private static final String LIBOR_CURVE_NAME = "libor";
  private static final Currency CUR = Currency.EUR;

  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT_INDEX = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM, "Ibor");

  private static final double SPREAD = 0.02;
  private static final CouponIborSpread PAYMENT1 = new CouponIborSpread(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_PERIOD_START_TIME, INDEX,
      FIXING_PERIOD_END_TIME, MATURITY,
      FORWARD_YEAR_FRACTION, LIBOR_CURVE_NAME);
  private static final CouponIborSpread PAYMENT2 = new CouponIborSpread(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, NOTIONAL, RESET_TIME, INDEX, FIXING_PERIOD_START_TIME,
      FIXING_PERIOD_END_TIME, FORWARD_YEAR_FRACTION, SPREAD, LIBOR_CURVE_NAME);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativePaymentTime() {
    new CouponIborSpread(CUR, -1, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, NOTIONAL, RESET_TIME, INDEX, RESET_TIME, MATURITY, FORWARD_YEAR_FRACTION, LIBOR_CURVE_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeResetTime() {
    new CouponIborSpread(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, NOTIONAL, -0.1, INDEX, RESET_TIME, MATURITY, FORWARD_YEAR_FRACTION, LIBOR_CURVE_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMaturityBeforereset() {
    new CouponIborSpread(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, NOTIONAL, RESET_TIME, INDEX, RESET_TIME, RESET_TIME - 0.1, FORWARD_YEAR_FRACTION, LIBOR_CURVE_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeYearFraction1() {
    new CouponIborSpread(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, -0.25, NOTIONAL, RESET_TIME, INDEX, RESET_TIME, MATURITY, FORWARD_YEAR_FRACTION, LIBOR_CURVE_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeYearFraction2() {
    new CouponIborSpread(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, NOTIONAL, RESET_TIME, INDEX, RESET_TIME, MATURITY, -0.25, LIBOR_CURVE_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFundingCurve() {
    new CouponIborSpread(CUR, PAYMENT_TIME, null, PAYMENT_YEAR_FRACTION, NOTIONAL, RESET_TIME, INDEX, RESET_TIME, MATURITY, FORWARD_YEAR_FRACTION, LIBOR_CURVE_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullLiborCurve() {
    new CouponIborSpread(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, NOTIONAL, RESET_TIME, INDEX, RESET_TIME, MATURITY, FORWARD_YEAR_FRACTION, null);
  }

  @Test
  public void testWithNotional() {
    final double notional = NOTIONAL + 100;
    final CouponIborSpread expected = new CouponIborSpread(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, notional, FIXING_PERIOD_START_TIME, INDEX, FIXING_PERIOD_END_TIME, MATURITY,
        FORWARD_YEAR_FRACTION, LIBOR_CURVE_NAME);
    assertEquals(expected, PAYMENT1.withNotional(notional));
  }

  @Test
  public void testWithSpread() {
    final double spread = SPREAD * 1.1;
    final CouponIborSpread coupon = new CouponIborSpread(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_PERIOD_START_TIME, INDEX, FIXING_PERIOD_END_TIME, MATURITY,
        FORWARD_YEAR_FRACTION, SPREAD, LIBOR_CURVE_NAME);
    final CouponIborSpread expected = new CouponIborSpread(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_PERIOD_START_TIME, INDEX, FIXING_PERIOD_END_TIME, MATURITY,
        FORWARD_YEAR_FRACTION, spread, LIBOR_CURVE_NAME);
    assertEquals(expected, coupon.withSpread(spread));
  }

  @Test
  public void testWithZeroSpread() {
    final CouponIborSpread coupon = new CouponIborSpread(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_PERIOD_START_TIME, INDEX, FIXING_PERIOD_END_TIME, MATURITY,
        FORWARD_YEAR_FRACTION, SPREAD, LIBOR_CURVE_NAME);
    final CouponIborSpread expected = new CouponIborSpread(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_PERIOD_START_TIME, INDEX, FIXING_PERIOD_END_TIME, MATURITY,
        FORWARD_YEAR_FRACTION, 0, LIBOR_CURVE_NAME);
    assertEquals(expected, coupon.withZeroSpread());
  }

  @Test
  public void testHashCodeAndEquals() {
    CouponIborSpread other = new CouponIborSpread(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_PERIOD_START_TIME, INDEX, FIXING_PERIOD_END_TIME, MATURITY,
        FORWARD_YEAR_FRACTION,
        LIBOR_CURVE_NAME);
    assertEquals(other, PAYMENT1);
    assertEquals(other.hashCode(), PAYMENT1.hashCode());
    other = new CouponIborSpread(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_PERIOD_START_TIME, INDEX, FIXING_PERIOD_END_TIME, MATURITY, FORWARD_YEAR_FRACTION, 0.0,
        LIBOR_CURVE_NAME);
    assertEquals(other, PAYMENT1);
    assertEquals(other.hashCode(), PAYMENT1.hashCode());
    other = new CouponIborSpread(CUR, PAYMENT_TIME - 0.1, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_PERIOD_START_TIME, INDEX, FIXING_PERIOD_END_TIME, MATURITY,
        FORWARD_YEAR_FRACTION,
        LIBOR_CURVE_NAME);
    assertFalse(other.equals(PAYMENT1));
    other = new CouponIborSpread(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION + 0.01, NOTIONAL, FIXING_PERIOD_START_TIME, INDEX, FIXING_PERIOD_END_TIME, MATURITY,
        FORWARD_YEAR_FRACTION,
        LIBOR_CURVE_NAME);
    assertFalse(other.equals(PAYMENT1));
    other = new CouponIborSpread(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, NOTIONAL + 10, FIXING_PERIOD_START_TIME, INDEX, FIXING_PERIOD_END_TIME, MATURITY, FORWARD_YEAR_FRACTION,
        LIBOR_CURVE_NAME);
    assertFalse(other.equals(PAYMENT1));
    other = new CouponIborSpread(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_PERIOD_START_TIME + 0.01, INDEX, FIXING_PERIOD_END_TIME, MATURITY,
        FORWARD_YEAR_FRACTION,
        LIBOR_CURVE_NAME);
    assertFalse(other.equals(PAYMENT1));
    other = new CouponIborSpread(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_PERIOD_START_TIME, INDEX, FIXING_PERIOD_END_TIME - 0.01, MATURITY,
        FORWARD_YEAR_FRACTION,
        LIBOR_CURVE_NAME);
    assertFalse(other.equals(PAYMENT1));
    other = new CouponIborSpread(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_PERIOD_START_TIME, INDEX, FIXING_PERIOD_END_TIME, MATURITY + 0.01,
        FORWARD_YEAR_FRACTION,
        LIBOR_CURVE_NAME);
    assertFalse(other.equals(PAYMENT1));
    other = new CouponIborSpread(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_PERIOD_START_TIME, INDEX, FIXING_PERIOD_END_TIME, MATURITY,
        FORWARD_YEAR_FRACTION + 0.01,
        LIBOR_CURVE_NAME);
    assertFalse(other.equals(PAYMENT1));
    other = new CouponIborSpread(CUR, PAYMENT_TIME, "false", PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_PERIOD_START_TIME, INDEX, FIXING_PERIOD_END_TIME, MATURITY, FORWARD_YEAR_FRACTION,
        LIBOR_CURVE_NAME);
    assertFalse(other.equals(PAYMENT1));
    other = new CouponIborSpread(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_PERIOD_START_TIME, INDEX, FIXING_PERIOD_END_TIME, MATURITY, FORWARD_YEAR_FRACTION,
        "false");
    assertFalse(other.equals(PAYMENT1));
    other = new CouponIborSpread(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_PERIOD_START_TIME, INDEX, FIXING_PERIOD_END_TIME, MATURITY, FORWARD_YEAR_FRACTION,
        SPREAD,
        LIBOR_CURVE_NAME);
    assertFalse(other.equals(PAYMENT1));
  }

  @Test
  public void testGetters() {
    assertEquals(CUR, PAYMENT2.getCurrency());
    assertEquals(FIXING_PERIOD_END_TIME, PAYMENT2.getFixingPeriodEndTime(), 0);
    assertEquals(FIXING_PERIOD_START_TIME, PAYMENT2.getFixingPeriodStartTime(), 0);
    assertEquals(RESET_TIME, PAYMENT2.getFixingTime(), 0);
    assertEquals(FORWARD_YEAR_FRACTION, PAYMENT2.getFixingAccrualFactor(), 0);
    assertEquals(LIBOR_CURVE_NAME, PAYMENT2.getForwardCurveName());
    assertEquals(FUNDING_CURVE_NAME, PAYMENT2.getFundingCurveName());
    assertEquals(NOTIONAL, PAYMENT2.getNotional(), 0);
    assertEquals(PAYMENT_TIME, PAYMENT2.getPaymentTime(), 0);
    assertEquals(PAYMENT_YEAR_FRACTION, PAYMENT2.getPaymentYearFraction(), 0);
    assertEquals(NOTIONAL, PAYMENT2.getReferenceAmount(), 0);
    assertEquals(SPREAD, PAYMENT2.getSpread(), 0);
  }

}
