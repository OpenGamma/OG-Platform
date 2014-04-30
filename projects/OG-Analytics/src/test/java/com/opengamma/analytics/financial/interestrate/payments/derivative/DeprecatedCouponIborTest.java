/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the construction of Ibor coupon.
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class DeprecatedCouponIborTest {

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 12, 27);
  private static final Calendar TARGET = new MondayToFridayCalendar("TARGET");
  private static final IndexIborMaster INDEX_IBOR_MASTER = IndexIborMaster.getInstance();
  private static final IborIndex INDEX_EURIBOR3M = INDEX_IBOR_MASTER.getIndex("EURIBOR3M");
  private static final Currency EUR = INDEX_EURIBOR3M.getCurrency();
  // Coupon
  private static final DayCount DAY_COUNT_COUPON = DayCounts.ACT_365;
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 5, 23);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 8, 22);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 8, 24);
  private static final double ACCRUAL_FACTOR = DAY_COUNT_COUPON.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 1000000; //1m
  private static final ZonedDateTime FIXING_DATE = ScheduleCalculator.getAdjustedDate(ACCRUAL_END_DATE, -INDEX_EURIBOR3M.getSpotLag(), TARGET); // In arrears
  private static final ZonedDateTime FIXING_START_DATE = ACCRUAL_END_DATE;
  private static final ZonedDateTime FIXING_END_DATE = ScheduleCalculator.getAdjustedDate(FIXING_START_DATE, INDEX_EURIBOR3M, TARGET);

  private static final double PAYMENT_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, PAYMENT_DATE);
  private static final double FIXING_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, FIXING_DATE);
  private static final double FIXING_START_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, FIXING_START_DATE);
  private static final double FIXING_END_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, FIXING_END_DATE);
  private static final double FIXING_ACCRUAL_FACTOR = INDEX_EURIBOR3M.getDayCount().getDayCountFraction(FIXING_START_DATE, FIXING_END_DATE);
  private static final String DISCOUNTING_CURVE_NAME = "Discounting";
  private static final String FORWARD_CURVE_NAME = "Forward";

  private static final CouponIbor CPN_IBOR = new CouponIbor(EUR, PAYMENT_TIME, DISCOUNTING_CURVE_NAME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME, FIXING_END_TIME,
      FIXING_ACCRUAL_FACTOR, FORWARD_CURVE_NAME);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency() {
    new CouponIbor(null, PAYMENT_TIME, DISCOUNTING_CURVE_NAME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR, FORWARD_CURVE_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullIndex() {
    new CouponIbor(EUR, PAYMENT_TIME, DISCOUNTING_CURVE_NAME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, null, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR, FORWARD_CURVE_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void incompatibleCurrency() {
    new CouponIbor(Currency.USD, PAYMENT_TIME, DISCOUNTING_CURVE_NAME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR,
        FORWARD_CURVE_NAME);
  }

  @Test
  /**
   * Tests the getters.
   */
  public void getter() {
    assertEquals("CouponIbor: getter", EUR, CPN_IBOR.getCurrency());
    assertEquals("CouponIbor: getter", INDEX_EURIBOR3M, CPN_IBOR.getIndex());
    assertEquals("CouponIbor: getter", FIXING_START_TIME, CPN_IBOR.getFixingPeriodStartTime());
    assertEquals("CouponIbor: getter", FIXING_END_TIME, CPN_IBOR.getFixingPeriodEndTime());
    assertEquals("CouponIbor: getter", FIXING_ACCRUAL_FACTOR, CPN_IBOR.getFixingAccrualFactor());
    assertEquals("CouponIbor: getter", FORWARD_CURVE_NAME, CPN_IBOR.getForwardCurveName());
  }

  @Test
  public void testWithNotional() {
    final double notional = NOTIONAL + 1000;
    final CouponIbor expected = new CouponIbor(EUR, PAYMENT_TIME, DISCOUNTING_CURVE_NAME, ACCRUAL_FACTOR, notional, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_ACCRUAL_FACTOR, FORWARD_CURVE_NAME);
    assertEquals(expected, CPN_IBOR.withNotional(notional));
  }

  @Test
  /**
   * Tests the equal and hash code.
   */
  public void testEqualHash() {
    assertEquals("CouponIbor: equal-hash", CPN_IBOR, CPN_IBOR);
    final CouponIbor other = new CouponIbor(EUR, PAYMENT_TIME, DISCOUNTING_CURVE_NAME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR,
        FORWARD_CURVE_NAME);
    assertEquals("CouponIbor: equal-hash", other, CPN_IBOR);
    assertEquals("CouponIbor: equal-hash", other.hashCode(), CPN_IBOR.hashCode());
    CouponIbor modified;
    modified = new CouponIbor(EUR, PAYMENT_TIME + 0.1, DISCOUNTING_CURVE_NAME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR,
        FORWARD_CURVE_NAME);
    assertFalse("CouponIbor: equal-hash", CPN_IBOR.equals(modified));
    modified = new CouponIbor(EUR, PAYMENT_TIME, "wrong", ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR, FORWARD_CURVE_NAME);
    assertFalse("CouponIbor: equal-hash", CPN_IBOR.equals(modified));
    modified = new CouponIbor(EUR, PAYMENT_TIME, DISCOUNTING_CURVE_NAME, ACCRUAL_FACTOR + 0.1, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR,
        FORWARD_CURVE_NAME);
    assertFalse("CouponIbor: equal-hash", CPN_IBOR.equals(modified));
    modified = new CouponIbor(EUR, PAYMENT_TIME, DISCOUNTING_CURVE_NAME, ACCRUAL_FACTOR, NOTIONAL + 1.0, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR,
        FORWARD_CURVE_NAME);
    assertFalse("CouponIbor: equal-hash", CPN_IBOR.equals(modified));
    modified = new CouponIbor(EUR, PAYMENT_TIME, DISCOUNTING_CURVE_NAME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME - 0.1, INDEX_EURIBOR3M, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR,
        FORWARD_CURVE_NAME);
    assertFalse("CouponIbor: equal-hash", CPN_IBOR.equals(modified));
    modified = new CouponIbor(Currency.USD, PAYMENT_TIME, DISCOUNTING_CURVE_NAME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_IBOR_MASTER.getIndex("USDLIBOR3M"), FIXING_START_TIME,
        FIXING_END_TIME, FIXING_ACCRUAL_FACTOR, FORWARD_CURVE_NAME);
    assertFalse("CouponIbor: equal-hash", CPN_IBOR.equals(modified));
    modified = new CouponIbor(EUR, PAYMENT_TIME, DISCOUNTING_CURVE_NAME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME + 0.1, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR,
        FORWARD_CURVE_NAME);
    assertFalse("CouponIbor: equal-hash", CPN_IBOR.equals(modified));
    modified = new CouponIbor(EUR, PAYMENT_TIME, DISCOUNTING_CURVE_NAME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME, FIXING_END_TIME + 0.1, FIXING_ACCRUAL_FACTOR,
        FORWARD_CURVE_NAME);
    assertFalse("CouponIbor: equal-hash", CPN_IBOR.equals(modified));
    modified = new CouponIbor(EUR, PAYMENT_TIME, DISCOUNTING_CURVE_NAME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR + 0.1,
        FORWARD_CURVE_NAME);
    assertFalse("CouponIbor: equal-hash", CPN_IBOR.equals(modified));
    modified = new CouponIbor(EUR, PAYMENT_TIME, DISCOUNTING_CURVE_NAME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR, "wrong");
    assertFalse("CouponIbor: equal-hash", CPN_IBOR.equals(modified));
  }

}
