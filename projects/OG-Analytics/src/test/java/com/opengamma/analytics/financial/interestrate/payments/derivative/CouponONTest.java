/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.payment.CouponONSimplifiedDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
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
 * Tests related to the OIS coupon derivative.
 */
@Test(groups = TestGroup.UNIT)
public class CouponONTest {
  private static final int EUR_SETTLEMENT_DAYS = 2;
  private static final BusinessDayConvention EUR_BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean EUR_IS_EOM = true;
  //EUR Eonia
  private static final String EUR_OIS_NAME = "EUR EONIA";
  private static final Currency EUR_CUR = Currency.EUR;
  private static final Calendar EUR_CALENDAR = new MondayToFridayCalendar("EUR");
  private static final int EUR_PUBLICATION_LAG = 0;
  private static final DayCount EUR_DAY_COUNT = DayCounts.ACT_360;
  private static final IndexON EUR_OIS = new IndexON(EUR_OIS_NAME, EUR_CUR, EUR_DAY_COUNT, EUR_PUBLICATION_LAG);
  // USD OIS
  private static final String US_OIS_NAME = "US OIS";
  private static final Currency US_CUR = Currency.EUR;
  private static final DayCount US_DAY_COUNT = DayCounts.ACT_360;
  private static final int US_PUBLICATION_LAG = 1;
  private static final IndexON US_OIS = new IndexON(US_OIS_NAME, US_CUR, US_DAY_COUNT, US_PUBLICATION_LAG);

  // Coupon EONIA 3m
  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2011, 9, 7);
  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(TRADE_DATE, EUR_SETTLEMENT_DAYS, EUR_CALENDAR);
  private static final Period EUR_CPN_TENOR = Period.ofMonths(3);
  private static final ZonedDateTime START_ACCRUAL_DATE = SPOT_DATE;
  private static final ZonedDateTime END_ACCRUAL_DATE = ScheduleCalculator.getAdjustedDate(START_ACCRUAL_DATE, EUR_CPN_TENOR, EUR_BUSINESS_DAY, EUR_CALENDAR, EUR_IS_EOM);
  private static ZonedDateTime LAST_FIXING_DATE = ScheduleCalculator.getAdjustedDate(END_ACCRUAL_DATE, -1, EUR_CALENDAR); // Overnight
  static {
    LAST_FIXING_DATE = ScheduleCalculator.getAdjustedDate(LAST_FIXING_DATE, EUR_PUBLICATION_LAG, EUR_CALENDAR); // Lag
  }
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(LAST_FIXING_DATE, EUR_SETTLEMENT_DAYS, EUR_CALENDAR);
  private static final double PAYMENT_YEAR_FRACTION = EUR_DAY_COUNT.getDayCountFraction(START_ACCRUAL_DATE, END_ACCRUAL_DATE);
  private static final double NOTIONAL = 100000000;
  private static final double FIXING_YEAR_FRACTION = EUR_DAY_COUNT.getDayCountFraction(START_ACCRUAL_DATE, END_ACCRUAL_DATE);
  private static final CouponONSimplifiedDefinition EONIA_COUPON_DEFINITION = new CouponONSimplifiedDefinition(EUR_CUR, PAYMENT_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, PAYMENT_YEAR_FRACTION,
      NOTIONAL, EUR_OIS, START_ACCRUAL_DATE, END_ACCRUAL_DATE, FIXING_YEAR_FRACTION);

  private static final ZonedDateTime REFERENCE_DATE_1 = DateUtils.getUTCDate(2011, 9, 7);
  private static final double PAYMENT_TIME_1 = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, PAYMENT_DATE);
  private static final double START_ACCRUAL_TIME_1 = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, START_ACCRUAL_DATE);
  private static final double END_ACCRUAL_TIME_1 = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, END_ACCRUAL_DATE);
  private static final CouponON EONIA_COUPON_NOTSTARTED = new CouponON(EUR_CUR, PAYMENT_TIME_1, PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, START_ACCRUAL_TIME_1, END_ACCRUAL_TIME_1,
      FIXING_YEAR_FRACTION, NOTIONAL);

  private static final ZonedDateTime REFERENCE_DATE_2 = DateUtils.getUTCDate(2011, 10, 7);
  private static final ZonedDateTime NEXT_FIXING_DATE_2 = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE_2, 1, EUR_CALENDAR); // Overnight
  private static final double PAYMENT_TIME_2 = TimeCalculator.getTimeBetween(REFERENCE_DATE_2, PAYMENT_DATE);
  private static final double START_FIXING_TIME_2 = TimeCalculator.getTimeBetween(REFERENCE_DATE_2, NEXT_FIXING_DATE_2);
  private static final double END_FIXING_TIME_2 = TimeCalculator.getTimeBetween(REFERENCE_DATE_2, END_ACCRUAL_DATE);
  private static final double FIXING_YEAR_FRACTION_2 = EUR_DAY_COUNT.getDayCountFraction(NEXT_FIXING_DATE_2, END_ACCRUAL_DATE);
  private static final double NOTIONAL_WITH_ACCRUED = NOTIONAL * (1.0 + 0.01 / 12); // 1% over a month (roughly)
  private static final CouponON EONIA_COUPON_STARTED = new CouponON(EUR_CUR, PAYMENT_TIME_2, PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, START_FIXING_TIME_2, END_FIXING_TIME_2,
      FIXING_YEAR_FRACTION_2, NOTIONAL_WITH_ACCRUED);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullIndex() {
    new CouponON(EUR_CUR, PAYMENT_TIME_1, PAYMENT_YEAR_FRACTION, NOTIONAL, null, START_ACCRUAL_TIME_1, END_ACCRUAL_TIME_1, FIXING_YEAR_FRACTION, NOTIONAL);
  }

  @Test
  public void getterNotStarted() {
    assertEquals("CouponOIS derivative: getter", EUR_OIS, EONIA_COUPON_NOTSTARTED.getIndex());
    assertEquals("CouponOIS derivative: getter", START_ACCRUAL_TIME_1, EONIA_COUPON_NOTSTARTED.getFixingPeriodStartTime());
    assertEquals("CouponOIS derivative: getter", END_ACCRUAL_TIME_1, EONIA_COUPON_NOTSTARTED.getFixingPeriodEndTime());
    assertEquals("CouponOIS derivative: getter", FIXING_YEAR_FRACTION, EONIA_COUPON_NOTSTARTED.getFixingPeriodAccrualFactor());
    assertEquals("CouponOIS derivative: getter", NOTIONAL, EONIA_COUPON_NOTSTARTED.getNotionalAccrued());
  }

  @Test
  public void getterStarted() {
    assertEquals("CouponOIS derivative: getter", EUR_OIS, EONIA_COUPON_STARTED.getIndex());
    assertEquals("CouponOIS derivative: getter", START_FIXING_TIME_2, EONIA_COUPON_STARTED.getFixingPeriodStartTime());
    assertEquals("CouponOIS derivative: getter", END_FIXING_TIME_2, EONIA_COUPON_STARTED.getFixingPeriodEndTime());
    assertEquals("CouponOIS derivative: getter", FIXING_YEAR_FRACTION_2, EONIA_COUPON_STARTED.getFixingPeriodAccrualFactor());
    assertEquals("CouponOIS derivative: getter", NOTIONAL_WITH_ACCRUED, EONIA_COUPON_STARTED.getNotionalAccrued());
  }

  @Test
  /**
   * Tests the equal and hashCode methods.
   */
  public void equalHash() {
    assertEquals("CouponOIS derivative: equal/hash code", EONIA_COUPON_STARTED, EONIA_COUPON_STARTED);
    final CouponON other = new CouponON(EUR_CUR, PAYMENT_TIME_2, PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, START_FIXING_TIME_2, END_FIXING_TIME_2, FIXING_YEAR_FRACTION_2,
        NOTIONAL_WITH_ACCRUED);
    assertEquals("CouponOIS derivative: equal/hash code", EONIA_COUPON_STARTED, other);
    assertEquals("CouponOIS derivative: equal/hash code", EONIA_COUPON_STARTED.hashCode(), other.hashCode());
    CouponON modified;
    modified = new CouponON(EUR_CUR, PAYMENT_TIME_2, PAYMENT_YEAR_FRACTION, NOTIONAL, US_OIS, START_FIXING_TIME_2, END_FIXING_TIME_2, FIXING_YEAR_FRACTION_2, NOTIONAL_WITH_ACCRUED);
    assertFalse("CouponOIS derivative: equal/hash code", EONIA_COUPON_DEFINITION.equals(modified));
    modified = new CouponON(EUR_CUR, PAYMENT_TIME_2, PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, START_FIXING_TIME_2 + 0.1, END_FIXING_TIME_2, FIXING_YEAR_FRACTION_2,
        NOTIONAL_WITH_ACCRUED);
    assertFalse("CouponOIS derivative: equal/hash code", EONIA_COUPON_DEFINITION.equals(modified));
    modified = new CouponON(EUR_CUR, PAYMENT_TIME_2, PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, START_FIXING_TIME_2, END_FIXING_TIME_2 + 0.1, FIXING_YEAR_FRACTION_2,
        NOTIONAL_WITH_ACCRUED);
    assertFalse("CouponOIS derivative: equal/hash code", EONIA_COUPON_DEFINITION.equals(modified));
    modified = new CouponON(EUR_CUR, PAYMENT_TIME_2, PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, START_FIXING_TIME_2, END_FIXING_TIME_2, FIXING_YEAR_FRACTION_2 + 0.1,
        NOTIONAL_WITH_ACCRUED);
    assertFalse("CouponOIS derivative: equal/hash code", EONIA_COUPON_DEFINITION.equals(modified));
    modified = new CouponON(EUR_CUR, PAYMENT_TIME_2, PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, START_FIXING_TIME_2, END_FIXING_TIME_2, FIXING_YEAR_FRACTION_2,
        NOTIONAL_WITH_ACCRUED + 123.4);
    assertFalse("CouponOIS derivative: equal/hash code", EONIA_COUPON_DEFINITION.equals(modified));
  }

}
