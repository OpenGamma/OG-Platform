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
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the construction of Ibor coupon with gearing factor and spread.
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class DeprecatedCouponIborGearingTest {
  // The index: Libor 3m
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.EUR;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM, "Ibor");
  // Coupon
  private static final DayCount DAY_COUNT_COUPON = DayCounts.ACT_365;
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 5, 23);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 8, 22);
  private static final double ACCRUAL_FACTOR = DAY_COUNT_COUPON.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 1000000; //1m
  private static final double FACTOR = 2.0;
  private static final double SPREAD = 0.0050;
  private static final ZonedDateTime FIXING_DATE = ScheduleCalculator.getAdjustedDate(ACCRUAL_START_DATE, -SETTLEMENT_DAYS, CALENDAR);
  private static final ZonedDateTime FIXING_START_DATE = DateUtils.getUTCDate(2011, 5, 23);
  private static final ZonedDateTime FIXING_END_DATE = ScheduleCalculator.getAdjustedDate(FIXING_START_DATE, TENOR, BUSINESS_DAY, CALENDAR);
  // Time
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 12, 27);
  private static final double ACCRUAL_END_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, ACCRUAL_END_DATE);
  private static final double FIXING_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, FIXING_DATE);
  private static final double FIXING_START_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, FIXING_START_DATE);
  private static final double FIXING_END_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, FIXING_END_DATE);
  private static final double FIXING_ACCRUAL_FACTOR = DAY_COUNT_INDEX.getDayCountFraction(FIXING_START_DATE, FIXING_END_DATE);
  private static final String DISCOUNTING_CURVE_NAME = "Discounting";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final CouponIborGearing COUPON = new CouponIborGearing(CUR, ACCRUAL_END_TIME, DISCOUNTING_CURVE_NAME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME,
      FIXING_END_TIME, FIXING_ACCRUAL_FACTOR, SPREAD, FACTOR, FORWARD_CURVE_NAME);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new CouponIborGearing(null, ACCRUAL_END_TIME, DISCOUNTING_CURVE_NAME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR, SPREAD, FACTOR,
        FORWARD_CURVE_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDiscounting() {
    new CouponIborGearing(CUR, ACCRUAL_END_TIME, null, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR, SPREAD, FACTOR, FORWARD_CURVE_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex() {
    new CouponIborGearing(CUR, ACCRUAL_END_TIME, DISCOUNTING_CURVE_NAME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, null, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR, SPREAD, FACTOR,
        FORWARD_CURVE_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullForward() {
    new CouponIborGearing(CUR, ACCRUAL_END_TIME, DISCOUNTING_CURVE_NAME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR, SPREAD, FACTOR, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongCurrency() {
    final Currency otherCurrency = Currency.USD;
    new CouponIborGearing(otherCurrency, ACCRUAL_END_TIME, DISCOUNTING_CURVE_NAME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR, SPREAD,
        FACTOR, FORWARD_CURVE_NAME);
  }

  @Test
  /**
   * Tests the getters.
   */
  public void getter() {
    assertEquals(CUR, COUPON.getCurrency());
    assertEquals(ACCRUAL_END_TIME, COUPON.getPaymentTime());
    assertEquals(FIXING_TIME, COUPON.getFixingTime());
    assertEquals(FIXING_START_TIME, COUPON.getFixingPeriodStartTime());
    assertEquals(FIXING_END_TIME, COUPON.getFixingPeriodEndTime());
    assertEquals(DISCOUNTING_CURVE_NAME, COUPON.getFundingCurveName());
    assertEquals(FORWARD_CURVE_NAME, COUPON.getForwardCurveName());
    assertEquals(INDEX, COUPON.getIndex());
    assertEquals(SPREAD, COUPON.getSpread());
    assertEquals(SPREAD * ACCRUAL_FACTOR * NOTIONAL, COUPON.getSpreadAmount());
    assertEquals(FACTOR, COUPON.getFactor());
  }

  @Test
  public void testWithNotional() {
    final double notional = NOTIONAL + 100;
    final CouponIborGearing expected = new CouponIborGearing(CUR, ACCRUAL_END_TIME, DISCOUNTING_CURVE_NAME, ACCRUAL_FACTOR, notional, FIXING_TIME, INDEX, FIXING_START_TIME,
        FIXING_END_TIME, FIXING_ACCRUAL_FACTOR, SPREAD, FACTOR, FORWARD_CURVE_NAME);
    assertEquals(expected, COUPON.withNotional(notional));
  }

  @Test
  /**
   * Tests the equal and hash code.
   */
  public void testEqualHash() {
    final CouponIborGearing newCoupon = new CouponIborGearing(CUR, ACCRUAL_END_TIME, DISCOUNTING_CURVE_NAME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_ACCRUAL_FACTOR, SPREAD, FACTOR, FORWARD_CURVE_NAME);
    assertEquals(newCoupon, COUPON);
    assertEquals(newCoupon.hashCode(), COUPON.hashCode());
    CouponIborGearing other;
    other = new CouponIborGearing(CUR, ACCRUAL_END_TIME + 0.1, DISCOUNTING_CURVE_NAME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR, SPREAD,
        FACTOR, FORWARD_CURVE_NAME);
    assertFalse(COUPON.equals(other));
    other = new CouponIborGearing(CUR, ACCRUAL_END_TIME, FORWARD_CURVE_NAME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR, SPREAD, FACTOR,
        FORWARD_CURVE_NAME);
    assertFalse(COUPON.equals(other));
    other = new CouponIborGearing(CUR, ACCRUAL_END_TIME, DISCOUNTING_CURVE_NAME, ACCRUAL_FACTOR + 0.1, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR, SPREAD,
        FACTOR, FORWARD_CURVE_NAME);
    assertFalse(COUPON.equals(other));
    other = new CouponIborGearing(CUR, ACCRUAL_END_TIME, DISCOUNTING_CURVE_NAME, ACCRUAL_FACTOR, NOTIONAL + 0.1, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR, SPREAD,
        FACTOR, FORWARD_CURVE_NAME);
    assertFalse(COUPON.equals(other));
    other = new CouponIborGearing(CUR, ACCRUAL_END_TIME, DISCOUNTING_CURVE_NAME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME + 0.1, INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR, SPREAD,
        FACTOR, FORWARD_CURVE_NAME);
    assertFalse(COUPON.equals(other));
    other = new CouponIborGearing(CUR, ACCRUAL_END_TIME, DISCOUNTING_CURVE_NAME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME + 0.1, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR, SPREAD,
        FACTOR, FORWARD_CURVE_NAME);
    assertFalse(COUPON.equals(other));
    other = new CouponIborGearing(CUR, ACCRUAL_END_TIME, DISCOUNTING_CURVE_NAME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME + 0.1, FIXING_ACCRUAL_FACTOR, SPREAD,
        FACTOR, FORWARD_CURVE_NAME);
    assertFalse(COUPON.equals(other));
    other = new CouponIborGearing(CUR, ACCRUAL_END_TIME, DISCOUNTING_CURVE_NAME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR + 0.1, SPREAD,
        FACTOR, FORWARD_CURVE_NAME);
    assertFalse(COUPON.equals(other));
    other = new CouponIborGearing(CUR, ACCRUAL_END_TIME, DISCOUNTING_CURVE_NAME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR, SPREAD + 0.1,
        FACTOR, FORWARD_CURVE_NAME);
    assertFalse(COUPON.equals(other));
    other = new CouponIborGearing(CUR, ACCRUAL_END_TIME, DISCOUNTING_CURVE_NAME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR, SPREAD,
        FACTOR + 0.1, FORWARD_CURVE_NAME);
    assertFalse(COUPON.equals(other));
    other = new CouponIborGearing(CUR, ACCRUAL_END_TIME, DISCOUNTING_CURVE_NAME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR, SPREAD,
        FACTOR, DISCOUNTING_CURVE_NAME);
    assertFalse(COUPON.equals(other));
  }

}
