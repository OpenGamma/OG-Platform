/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests for CouponIborFxReset.
 */
@SuppressWarnings("unused")
@Test(groups = TestGroup.UNIT)
public class CouponIborFxResetTest {
  private static final double RESET_TIME = 0.24;
  private static final double FORWARD_YEAR_FRACTION = 0.27;
  private static final double FIXING_PERIOD_START_TIME = 0.25;
  private static final double FIXING_PERIOD_END_TIME = 0.52;
  private static final Currency CUR = Currency.EUR;

  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT_INDEX = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY,
      IS_EOM, "Ibor");

  private static final Currency CUR_REF = Currency.EUR;
  private static final Currency CUR_PAY = Currency.USD;
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 4, 6);
  private static final ZonedDateTime FX_FIXING_DATE = DateUtils.getUTCDate(2011, 1, 3);
  private static final ZonedDateTime FX_DELIVERY_DATE = DateUtils.getUTCDate(2011, 1, 6);
  private static final double ACCRUAL_FACTOR = 0.267;
  private static final double NOTIONAL = 1000000; //1m
  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2010, 7, 1);
  private static final double PAYMENT_TIME = TimeCalculator.getTimeBetween(VALUATION_DATE, PAYMENT_DATE);
  private static final double FX_FIXING_TIME = TimeCalculator.getTimeBetween(VALUATION_DATE, FX_FIXING_DATE);
  private static final double FX_DELIVERY_TIME = TimeCalculator.getTimeBetween(VALUATION_DATE, FX_DELIVERY_DATE);
  private static final double SPREAD = 0.02;

  private static final CouponIborFxReset CPN = new CouponIborFxReset(CUR_PAY, PAYMENT_TIME, ACCRUAL_FACTOR,
      NOTIONAL, RESET_TIME, INDEX, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME, FORWARD_YEAR_FRACTION, SPREAD,
      CUR_REF, FX_FIXING_TIME, FX_DELIVERY_TIME);

  /**
   * Test getters
   */
  @Test
  public void getter() {
    assertEquals("CouponIborFxReset", CUR_REF, CPN.getReferenceCurrency());
    assertEquals("CouponIborFxReset", FX_FIXING_TIME, CPN.getFxFixingTime());
    assertEquals("CouponIborFxReset", FX_DELIVERY_TIME, CPN.getFxDeliveryTime());
    assertEquals("CouponIborFxReset", INDEX, CPN.getIndex());
    assertEquals("CouponIborFxReset", SPREAD, CPN.getSpread());
    assertEquals("CouponIborFxReset", FORWARD_YEAR_FRACTION, CPN.getIborIndexFixingAccrualFactor());
    assertEquals("CouponIborFxReset", RESET_TIME, CPN.getIborIndexFixingTime());
    assertEquals("CouponIborFxReset", FIXING_PERIOD_START_TIME, CPN.getIborIndexFixingPeriodStartTime());
    assertEquals("CouponIborFxReset", FIXING_PERIOD_END_TIME, CPN.getIborIndexFixingPeriodEndTime());
  }

  /**
   * iborIndexFixingPeriodStartTime >= iborIndexFixingTime violated
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void largeFixingiImeTest() {
    new CouponIborFxReset(CUR_PAY, PAYMENT_TIME, ACCRUAL_FACTOR,
        NOTIONAL, RESET_TIME * 10.0, INDEX, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME, FORWARD_YEAR_FRACTION,
        SPREAD, CUR_REF, FX_FIXING_TIME, FX_DELIVERY_TIME);
  }

  /**
   * iborIndexFixingPeriodEndTime >= iborIndexFixingPeriodStartTime violated
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void startAfterEndTest() {
    new CouponIborFxReset(CUR_PAY, PAYMENT_TIME, ACCRUAL_FACTOR,
        NOTIONAL, RESET_TIME, INDEX, FIXING_PERIOD_START_TIME, FIXING_PERIOD_START_TIME * 0.9, FORWARD_YEAR_FRACTION,
        SPREAD, CUR_REF, FX_FIXING_TIME, FX_DELIVERY_TIME);
  }

  /**
   * Ibor fixing period year fraction is negative
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeFixingYearFractionTest() {
    new CouponIborFxReset(CUR_PAY, PAYMENT_TIME, ACCRUAL_FACTOR,
        NOTIONAL, RESET_TIME, INDEX, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME, -FORWARD_YEAR_FRACTION, SPREAD,
        CUR_REF, FX_FIXING_TIME, FX_DELIVERY_TIME);
  }

  /**
   * Ibor index fixing time is negative
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeFixingTimeTest() {
    new CouponIborFxReset(CUR_PAY, PAYMENT_TIME, ACCRUAL_FACTOR,
        NOTIONAL, -RESET_TIME, INDEX, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME, FORWARD_YEAR_FRACTION, SPREAD,
        CUR_REF, FX_FIXING_TIME, FX_DELIVERY_TIME);
  }

  /**
   * Test hashCode and equals
   */
  @Test
  public void hashCodeAndEqualsTest() {
    CouponIborFxReset cpn0 = new CouponIborFxReset(CUR_PAY, PAYMENT_TIME, ACCRUAL_FACTOR,
        NOTIONAL, RESET_TIME, INDEX, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME, FORWARD_YEAR_FRACTION,
        SPREAD, CUR_REF, FX_FIXING_TIME, FX_DELIVERY_TIME);
    assertTrue(CPN.equals(CPN));
    assertTrue(cpn0.equals(CPN));
    assertTrue(CPN.equals(cpn0));
    assertTrue(cpn0.hashCode() == CPN.hashCode());

    CouponIborFxReset cpn1 = new CouponIborFxReset(CUR_PAY, PAYMENT_TIME, ACCRUAL_FACTOR,
        NOTIONAL, RESET_TIME * 0.99, INDEX, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME, FORWARD_YEAR_FRACTION,
        SPREAD, CUR_REF, FX_FIXING_TIME, FX_DELIVERY_TIME);
    CouponIborFxReset cpn2 = new CouponIborFxReset(CUR_PAY, PAYMENT_TIME, ACCRUAL_FACTOR,
        NOTIONAL, RESET_TIME, INDEX, FIXING_PERIOD_START_TIME * 0.99, FIXING_PERIOD_END_TIME, FORWARD_YEAR_FRACTION,
        SPREAD, CUR_REF, FX_FIXING_TIME, FX_DELIVERY_TIME);
    CouponIborFxReset cpn3 = new CouponIborFxReset(CUR_PAY, PAYMENT_TIME, ACCRUAL_FACTOR,
        NOTIONAL, RESET_TIME, INDEX, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME * 0.99, FORWARD_YEAR_FRACTION,
        SPREAD, CUR_REF, FX_FIXING_TIME, FX_DELIVERY_TIME);
    CouponIborFxReset cpn4 = new CouponIborFxReset(CUR_PAY, PAYMENT_TIME, ACCRUAL_FACTOR,
        NOTIONAL, RESET_TIME, INDEX, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME, FORWARD_YEAR_FRACTION * 0.99,
        SPREAD, CUR_REF, FX_FIXING_TIME, FX_DELIVERY_TIME);
    CouponIborFxReset cpn5 = new CouponIborFxReset(CUR_PAY, PAYMENT_TIME, ACCRUAL_FACTOR,
        NOTIONAL, RESET_TIME, INDEX, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME, FORWARD_YEAR_FRACTION,
        SPREAD * 0.99, CUR_REF, FX_FIXING_TIME, FX_DELIVERY_TIME);
    CouponIborFxReset cpn6 = new CouponIborFxReset(CUR_PAY, PAYMENT_TIME, ACCRUAL_FACTOR,
        NOTIONAL, RESET_TIME, INDEX, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME, FORWARD_YEAR_FRACTION,
        SPREAD, Currency.AUD, FX_FIXING_TIME, FX_DELIVERY_TIME);
    CouponIborFxReset cpn7 = new CouponIborFxReset(CUR_PAY, PAYMENT_TIME, ACCRUAL_FACTOR,
        NOTIONAL, RESET_TIME, INDEX, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME, FORWARD_YEAR_FRACTION,
        SPREAD, CUR_REF, FX_FIXING_TIME * 0.99, FX_DELIVERY_TIME);
    CouponIborFxReset cpn8 = new CouponIborFxReset(CUR_PAY, PAYMENT_TIME, ACCRUAL_FACTOR,
        NOTIONAL, RESET_TIME, INDEX, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME, FORWARD_YEAR_FRACTION,
        SPREAD, CUR_REF, FX_FIXING_TIME, FX_DELIVERY_TIME * 0.99);
    IborIndex index = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY,
        false, "Deprecated");
    CouponIborFxReset cpn9 = new CouponIborFxReset(CUR_PAY, PAYMENT_TIME, ACCRUAL_FACTOR,
        NOTIONAL, RESET_TIME, index, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME, FORWARD_YEAR_FRACTION,
        SPREAD, CUR_REF, FX_FIXING_TIME, FX_DELIVERY_TIME);
    CouponIborFxReset[] cpnArray = new CouponIborFxReset[] {cpn1, cpn2, cpn3, cpn4, cpn5, cpn6, cpn7, cpn8, cpn9, null };

    for (int i = 0; i < cpnArray.length; ++i) {
      assertFalse(CPN.equals(cpnArray[i]));
    }
    assertFalse(CPN.equals(index));
  }
}
