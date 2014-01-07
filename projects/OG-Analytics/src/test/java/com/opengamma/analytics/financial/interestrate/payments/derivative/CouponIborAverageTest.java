/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
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
 * Test of coupon Ibor Average class.
 */
@Test(groups = TestGroup.UNIT)
public class CouponIborAverageTest {
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 12, 27);
  private static final Calendar TARGET = new MondayToFridayCalendar("TARGET");
  private static final IndexIborMaster INDEX_IBOR_MASTER = IndexIborMaster.getInstance();
  private static final IborIndex INDEX_EURIBOR3M = INDEX_IBOR_MASTER.getIndex("EURIBOR3M");
  private static final IborIndex INDEX_EURIBOR6M = INDEX_IBOR_MASTER.getIndex("EURIBOR6M");
  private static final Currency EUR = INDEX_EURIBOR3M.getCurrency();
  // Coupon
  private static final DayCount DAY_COUNT_COUPON = DayCounts.ACT_365;
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 2, 23);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 8, 22);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 8, 24);
  private static final double ACCRUAL_FACTOR = DAY_COUNT_COUPON.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 1000000; //1m
  private static final ZonedDateTime FIXING_DATE = ScheduleCalculator.getAdjustedDate(ACCRUAL_END_DATE, -INDEX_EURIBOR3M.getSpotLag(), TARGET); // In arrears
  private static final ZonedDateTime FIXING_START_DATE_1 = ACCRUAL_END_DATE;
  private static final ZonedDateTime FIXING_END_DATE_1 = ScheduleCalculator.getAdjustedDate(FIXING_START_DATE_1, INDEX_EURIBOR3M, TARGET);
  private static final ZonedDateTime FIXING_START_DATE_2 = ACCRUAL_END_DATE;
  private static final ZonedDateTime FIXING_END_DATE_2 = ScheduleCalculator.getAdjustedDate(FIXING_START_DATE_2, INDEX_EURIBOR3M, TARGET);

  private static final double PAYMENT_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, PAYMENT_DATE);
  private static final double FIXING_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, FIXING_DATE);
  private static final double FIXING_START_TIME_1 = TimeCalculator.getTimeBetween(REFERENCE_DATE, FIXING_START_DATE_1);
  private static final double FIXING_END_TIME_1 = TimeCalculator.getTimeBetween(REFERENCE_DATE, FIXING_END_DATE_1);
  private static final double FIXING_ACCRUAL_FACTOR_1 = INDEX_EURIBOR3M.getDayCount().getDayCountFraction(FIXING_START_DATE_1, FIXING_END_DATE_1);
  private static final double FIXING_START_TIME_2 = TimeCalculator.getTimeBetween(REFERENCE_DATE, FIXING_START_DATE_2);
  private static final double FIXING_END_TIME_2 = TimeCalculator.getTimeBetween(REFERENCE_DATE, FIXING_END_DATE_2);
  private static final double FIXING_ACCRUAL_FACTOR_2 = INDEX_EURIBOR3M.getDayCount().getDayCountFraction(FIXING_START_DATE_2, FIXING_END_DATE_2);

  private static double WEIGHT_1 = 4.2;
  private static double WEIGHT_2 = -.003;
  private static final CouponIborAverage CPN_IBOR_AVERAGE = new CouponIborAverage(EUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME_1, FIXING_END_TIME_1,
      FIXING_ACCRUAL_FACTOR_1, INDEX_EURIBOR6M, FIXING_START_TIME_2, FIXING_END_TIME_2, FIXING_ACCRUAL_FACTOR_2, WEIGHT_1, WEIGHT_2);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency() {
    new CouponIborAverage(null, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME_1, FIXING_END_TIME_1,
        FIXING_ACCRUAL_FACTOR_1, INDEX_EURIBOR6M, FIXING_START_TIME_2, FIXING_END_TIME_2, FIXING_ACCRUAL_FACTOR_2, WEIGHT_1, WEIGHT_2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullIndex1() {
    new CouponIborAverage(EUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, null, FIXING_START_TIME_1, FIXING_END_TIME_1,
        FIXING_ACCRUAL_FACTOR_1, INDEX_EURIBOR6M, FIXING_START_TIME_2, FIXING_END_TIME_2, FIXING_ACCRUAL_FACTOR_2, WEIGHT_1, WEIGHT_2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullIndex2() {
    new CouponIborAverage(EUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME_1, FIXING_END_TIME_1,
        FIXING_ACCRUAL_FACTOR_1, null, FIXING_START_TIME_2, FIXING_END_TIME_2, FIXING_ACCRUAL_FACTOR_2, WEIGHT_1, WEIGHT_2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void incompatibleCurrency() {
    new CouponIborAverage(Currency.USD, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME_1, FIXING_END_TIME_1,
        FIXING_ACCRUAL_FACTOR_1, INDEX_EURIBOR6M, FIXING_START_TIME_2, FIXING_END_TIME_2, FIXING_ACCRUAL_FACTOR_2, WEIGHT_1, WEIGHT_2);
  }

  @Test
  /**
   * Tests the getters.
   */
  public void getter() {
    assertEquals("CouponIbor: getter", EUR, CPN_IBOR_AVERAGE.getCurrency());
    assertEquals("CouponIbor: getter", INDEX_EURIBOR3M, CPN_IBOR_AVERAGE.getIndex1());
    assertEquals("CouponIbor: getter", INDEX_EURIBOR6M, CPN_IBOR_AVERAGE.getIndex2());
    assertEquals("CouponIbor: getter", WEIGHT_1, CPN_IBOR_AVERAGE.getWeight1());
    assertEquals("CouponIbor: getter", WEIGHT_2, CPN_IBOR_AVERAGE.getWeight2());
    assertEquals("CouponIbor: getter", FIXING_START_TIME_1, CPN_IBOR_AVERAGE.getFixingPeriodStartTime1());
    assertEquals("CouponIbor: getter", FIXING_END_TIME_1, CPN_IBOR_AVERAGE.getFixingPeriodEndTime1());
    assertEquals("CouponIbor: getter", FIXING_ACCRUAL_FACTOR_1, CPN_IBOR_AVERAGE.getFixingAccrualFactor1());
    assertEquals("CouponIbor: getter", FIXING_START_TIME_2, CPN_IBOR_AVERAGE.getFixingPeriodStartTime2());
    assertEquals("CouponIbor: getter", FIXING_END_TIME_2, CPN_IBOR_AVERAGE.getFixingPeriodEndTime2());
    assertEquals("CouponIbor: getter", FIXING_ACCRUAL_FACTOR_2, CPN_IBOR_AVERAGE.getFixingAccrualFactor2());
  }

  @Test
  public void testWithNotional() {
    final double notional = NOTIONAL + 1000;
    final CouponIborAverage expected = new CouponIborAverage(EUR, PAYMENT_TIME, ACCRUAL_FACTOR, notional, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME_1, FIXING_END_TIME_1,
        FIXING_ACCRUAL_FACTOR_1, INDEX_EURIBOR6M, FIXING_START_TIME_2, FIXING_END_TIME_2, FIXING_ACCRUAL_FACTOR_2, WEIGHT_1, WEIGHT_2);
    assertEquals(expected, CPN_IBOR_AVERAGE.withNotional(notional));
  }

  @Test
  /**
   * Tests the equal and hash code.
   */
  public void testEqualHash() {
    assertEquals("CouponIbor: equal-hash", CPN_IBOR_AVERAGE, CPN_IBOR_AVERAGE);
    final CouponIborAverage other = new CouponIborAverage(EUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME_1, FIXING_END_TIME_1,
        FIXING_ACCRUAL_FACTOR_1, INDEX_EURIBOR6M, FIXING_START_TIME_2, FIXING_END_TIME_2, FIXING_ACCRUAL_FACTOR_2, WEIGHT_1, WEIGHT_2);
    assertEquals("CouponIbor: equal-hash", other, CPN_IBOR_AVERAGE);
    assertEquals("CouponIbor: equal-hash", other.hashCode(), CPN_IBOR_AVERAGE.hashCode());
    CouponIborAverage modified;
    modified = new CouponIborAverage(EUR, PAYMENT_TIME + 0.1, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME_1, FIXING_END_TIME_1,
        FIXING_ACCRUAL_FACTOR_1, INDEX_EURIBOR6M, FIXING_START_TIME_2, FIXING_END_TIME_2, FIXING_ACCRUAL_FACTOR_2, WEIGHT_1, WEIGHT_2);
    assertFalse("CouponIbor: equal-hash", CPN_IBOR_AVERAGE.equals(modified));
    modified = new CouponIborAverage(EUR, PAYMENT_TIME, ACCRUAL_FACTOR + 0.1, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME_1, FIXING_END_TIME_1,
        FIXING_ACCRUAL_FACTOR_1, INDEX_EURIBOR6M, FIXING_START_TIME_2, FIXING_END_TIME_2, FIXING_ACCRUAL_FACTOR_2, WEIGHT_1, WEIGHT_2);
    assertFalse("CouponIbor: equal-hash", CPN_IBOR_AVERAGE.equals(modified));
    modified = new CouponIborAverage(EUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL + 0.1, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME_1, FIXING_END_TIME_1,
        FIXING_ACCRUAL_FACTOR_1, INDEX_EURIBOR6M, FIXING_START_TIME_2, FIXING_END_TIME_2, FIXING_ACCRUAL_FACTOR_2, WEIGHT_1, WEIGHT_2);
    assertFalse("CouponIbor: equal-hash", CPN_IBOR_AVERAGE.equals(modified));
    modified = new CouponIborAverage(EUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME - 0.1, INDEX_EURIBOR3M, FIXING_START_TIME_1, FIXING_END_TIME_1,
        FIXING_ACCRUAL_FACTOR_1, INDEX_EURIBOR6M, FIXING_START_TIME_2, FIXING_END_TIME_2, FIXING_ACCRUAL_FACTOR_2, WEIGHT_1, WEIGHT_2);
    assertFalse("CouponIbor: equal-hash", CPN_IBOR_AVERAGE.equals(modified));
    modified = new CouponIborAverage(EUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME_1 + 0.1, FIXING_END_TIME_1,
        FIXING_ACCRUAL_FACTOR_1, INDEX_EURIBOR6M, FIXING_START_TIME_2, FIXING_END_TIME_2, FIXING_ACCRUAL_FACTOR_2, WEIGHT_1, WEIGHT_2);
    assertFalse("CouponIbor: equal-hash", CPN_IBOR_AVERAGE.equals(modified));
    modified = new CouponIborAverage(EUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME_1, FIXING_END_TIME_1 + 0.1,
        FIXING_ACCRUAL_FACTOR_1, INDEX_EURIBOR6M, FIXING_START_TIME_2, FIXING_END_TIME_2, FIXING_ACCRUAL_FACTOR_2, WEIGHT_1, WEIGHT_2);
    assertFalse("CouponIbor: equal-hash", CPN_IBOR_AVERAGE.equals(modified));
    modified = new CouponIborAverage(EUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME_1, FIXING_END_TIME_1,
        FIXING_ACCRUAL_FACTOR_1 + 0.1, INDEX_EURIBOR6M, FIXING_START_TIME_2, FIXING_END_TIME_2, FIXING_ACCRUAL_FACTOR_2, WEIGHT_1, WEIGHT_2);
    assertFalse("CouponIbor: equal-hash", CPN_IBOR_AVERAGE.equals(modified));
    modified = new CouponIborAverage(EUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME_1, FIXING_END_TIME_1,
        FIXING_ACCRUAL_FACTOR_1, INDEX_EURIBOR6M, FIXING_START_TIME_2 + 0.1, FIXING_END_TIME_2, FIXING_ACCRUAL_FACTOR_2, WEIGHT_1, WEIGHT_2);
    assertFalse("CouponIbor: equal-hash", CPN_IBOR_AVERAGE.equals(modified));
    modified = new CouponIborAverage(EUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME_1, FIXING_END_TIME_1,
        FIXING_ACCRUAL_FACTOR_1, INDEX_EURIBOR6M, FIXING_START_TIME_2, FIXING_END_TIME_2 + 0.1, FIXING_ACCRUAL_FACTOR_2, WEIGHT_1, WEIGHT_2);
    assertFalse("CouponIbor: equal-hash", CPN_IBOR_AVERAGE.equals(modified));
    modified = new CouponIborAverage(EUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME_1, FIXING_END_TIME_1,
        FIXING_ACCRUAL_FACTOR_1, INDEX_EURIBOR6M, FIXING_START_TIME_2, FIXING_END_TIME_2, FIXING_ACCRUAL_FACTOR_2 + 0.1, WEIGHT_1, WEIGHT_2);
    assertFalse("CouponIbor: equal-hash", CPN_IBOR_AVERAGE.equals(modified));
    modified = new CouponIborAverage(EUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME_1, FIXING_END_TIME_1,
        FIXING_ACCRUAL_FACTOR_1, INDEX_EURIBOR6M, FIXING_START_TIME_2, FIXING_END_TIME_2, FIXING_ACCRUAL_FACTOR_2, WEIGHT_1 + 0.1, WEIGHT_2);
    assertFalse("CouponIbor: equal-hash", CPN_IBOR_AVERAGE.equals(modified));
    modified = new CouponIborAverage(EUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME_1, FIXING_END_TIME_1,
        FIXING_ACCRUAL_FACTOR_1, INDEX_EURIBOR6M, FIXING_START_TIME_2, FIXING_END_TIME_2, FIXING_ACCRUAL_FACTOR_2, WEIGHT_1, WEIGHT_2 + 0.1);
    assertFalse("CouponIbor: equal-hash", CPN_IBOR_AVERAGE.equals(modified));
    modified = new CouponIborAverage(EUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR6M, FIXING_START_TIME_1, FIXING_END_TIME_1,
        FIXING_ACCRUAL_FACTOR_1, INDEX_EURIBOR6M, FIXING_START_TIME_2, FIXING_END_TIME_2, FIXING_ACCRUAL_FACTOR_2, WEIGHT_1, WEIGHT_2);
    assertFalse("CouponIbor: equal-hash", CPN_IBOR_AVERAGE.equals(modified));
    modified = new CouponIborAverage(EUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX_EURIBOR3M, FIXING_START_TIME_1, FIXING_END_TIME_1,
        FIXING_ACCRUAL_FACTOR_1, INDEX_EURIBOR3M, FIXING_START_TIME_2, FIXING_END_TIME_2, FIXING_ACCRUAL_FACTOR_2, WEIGHT_1, WEIGHT_2);
    assertFalse("CouponIbor: equal-hash", CPN_IBOR_AVERAGE.equals(modified));
  }
}
