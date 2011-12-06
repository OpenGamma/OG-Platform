/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.IndexON;
import com.opengamma.financial.instrument.payment.CouponOISSimplifiedDefinition;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.TimeCalculator;

/**
 * Tests related to the OIS coupon derivative.
 */
public class CouponOISTest {
  private static final int EUR_SETTLEMENT_DAYS = 2;
  private static final BusinessDayConvention EUR_BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean EUR_IS_EOM = true;
  //EUR Eonia
  private static final String EUR_OIS_NAME = "EUR EONIA";
  private static final Currency EUR_CUR = Currency.EUR;
  private static final Calendar EUR_CALENDAR = new MondayToFridayCalendar("EUR");
  private static final int EUR_PUBLICATION_LAG = 0;
  private static final DayCount EUR_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final IndexON EUR_OIS = new IndexON(EUR_OIS_NAME, EUR_CUR, EUR_DAY_COUNT, EUR_PUBLICATION_LAG, EUR_CALENDAR);
  // USD OIS
  private static final String US_OIS_NAME = "US OIS";
  private static final Currency US_CUR = Currency.USD;
  private static final DayCount US_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final int US_PUBLICATION_LAG = 1;
  private static final Calendar US_CALENDAR = new MondayToFridayCalendar("A");
  private static final IndexON US_OIS = new IndexON(US_OIS_NAME, US_CUR, US_DAY_COUNT, US_PUBLICATION_LAG, US_CALENDAR);

  // Coupon EONIA 3m
  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2011, 9, 7);
  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(TRADE_DATE, EUR_CALENDAR, EUR_SETTLEMENT_DAYS);
  private static final Period EUR_CPN_TENOR = Period.ofMonths(3);
  private static final ZonedDateTime START_ACCRUAL_DATE = SPOT_DATE;
  private static final ZonedDateTime END_ACCRUAL_DATE = ScheduleCalculator.getAdjustedDate(START_ACCRUAL_DATE, EUR_BUSINESS_DAY, EUR_CALENDAR, EUR_IS_EOM, EUR_CPN_TENOR);
  private static ZonedDateTime LAST_FIXING_DATE = ScheduleCalculator.getAdjustedDate(END_ACCRUAL_DATE, EUR_CALENDAR, -1); // Overnight
  static {
    LAST_FIXING_DATE = ScheduleCalculator.getAdjustedDate(LAST_FIXING_DATE, EUR_CALENDAR, EUR_PUBLICATION_LAG); // Lag
  }
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(LAST_FIXING_DATE, EUR_CALENDAR, EUR_SETTLEMENT_DAYS);
  private static final double PAYMENT_YEAR_FRACTION = EUR_DAY_COUNT.getDayCountFraction(START_ACCRUAL_DATE, END_ACCRUAL_DATE);
  private static final double NOTIONAL = 100000000;
  private static final double FIXING_YEAR_FRACTION = EUR_DAY_COUNT.getDayCountFraction(START_ACCRUAL_DATE, END_ACCRUAL_DATE);
  private static final CouponOISSimplifiedDefinition EONIA_COUPON_DEFINITION = new CouponOISSimplifiedDefinition(EUR_CUR, PAYMENT_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, PAYMENT_YEAR_FRACTION,
      NOTIONAL, EUR_OIS, START_ACCRUAL_DATE, END_ACCRUAL_DATE, FIXING_YEAR_FRACTION);

  private static final YieldCurveBundle CURVES = TestsDataSets.createCurves1();
  private static final String[] CURVES_NAMES = CURVES.getAllNames().toArray(new String[0]);

  private static final ZonedDateTime REFERENCE_DATE_1 = DateUtils.getUTCDate(2011, 9, 7);
  private static final double PAYMENT_TIME_1 = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, PAYMENT_DATE);
  private static final double START_ACCRUAL_TIME_1 = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, START_ACCRUAL_DATE);
  private static final double END_ACCRUAL_TIME_1 = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, END_ACCRUAL_DATE);
  private static final CouponOIS EONIA_COUPON_NOTSTARTED = new CouponOIS(EUR_CUR, PAYMENT_TIME_1, CURVES_NAMES[0], PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, START_ACCRUAL_TIME_1, END_ACCRUAL_TIME_1,
      FIXING_YEAR_FRACTION, NOTIONAL, CURVES_NAMES[1]);

  private static final ZonedDateTime REFERENCE_DATE_2 = DateUtils.getUTCDate(2011, 10, 7);
  private static final ZonedDateTime NEXT_FIXING_DATE_2 = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE_2, EUR_CALENDAR, 1); // Overnight
  private static final double PAYMENT_TIME_2 = TimeCalculator.getTimeBetween(REFERENCE_DATE_2, PAYMENT_DATE);
  private static final double START_FIXING_TIME_2 = TimeCalculator.getTimeBetween(REFERENCE_DATE_2, NEXT_FIXING_DATE_2);
  private static final double END_FIXING_TIME_2 = TimeCalculator.getTimeBetween(REFERENCE_DATE_2, END_ACCRUAL_DATE);
  private static final double FIXING_YEAR_FRACTION_2 = EUR_DAY_COUNT.getDayCountFraction(NEXT_FIXING_DATE_2, END_ACCRUAL_DATE);
  private static final double NOTIONAL_WITH_ACCRUED = NOTIONAL * (1.0 + 0.01 / 12); // 1% over a month (roughly)
  private static final CouponOIS EONIA_COUPON_STARTED = new CouponOIS(EUR_CUR, PAYMENT_TIME_2, CURVES_NAMES[0], PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, START_FIXING_TIME_2, END_FIXING_TIME_2,
      FIXING_YEAR_FRACTION_2, NOTIONAL_WITH_ACCRUED, CURVES_NAMES[1]);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullIndex() {
    new CouponOIS(EUR_CUR, PAYMENT_TIME_1, CURVES_NAMES[0], PAYMENT_YEAR_FRACTION, NOTIONAL, null, START_ACCRUAL_TIME_1, END_ACCRUAL_TIME_1, FIXING_YEAR_FRACTION, NOTIONAL, CURVES_NAMES[1]);
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
    CouponOIS other = new CouponOIS(EUR_CUR, PAYMENT_TIME_2, CURVES_NAMES[0], PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, START_FIXING_TIME_2, END_FIXING_TIME_2, FIXING_YEAR_FRACTION_2,
        NOTIONAL_WITH_ACCRUED, CURVES_NAMES[1]);
    assertEquals("CouponOIS derivative: equal/hash code", EONIA_COUPON_STARTED, other);
    assertEquals("CouponOIS derivative: equal/hash code", EONIA_COUPON_STARTED.hashCode(), other.hashCode());
    CouponOIS modified;
    modified = new CouponOIS(EUR_CUR, PAYMENT_TIME_2, CURVES_NAMES[0], PAYMENT_YEAR_FRACTION, NOTIONAL, US_OIS, START_FIXING_TIME_2, END_FIXING_TIME_2, FIXING_YEAR_FRACTION_2, NOTIONAL_WITH_ACCRUED,
        CURVES_NAMES[1]);
    assertFalse("CouponOIS derivative: equal/hash code", EONIA_COUPON_DEFINITION.equals(modified));
    modified = new CouponOIS(EUR_CUR, PAYMENT_TIME_2, CURVES_NAMES[0], PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, START_FIXING_TIME_2 + 0.1, END_FIXING_TIME_2, FIXING_YEAR_FRACTION_2,
        NOTIONAL_WITH_ACCRUED, CURVES_NAMES[1]);
    assertFalse("CouponOIS derivative: equal/hash code", EONIA_COUPON_DEFINITION.equals(modified));
    modified = new CouponOIS(EUR_CUR, PAYMENT_TIME_2, CURVES_NAMES[0], PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, START_FIXING_TIME_2, END_FIXING_TIME_2 + 0.1, FIXING_YEAR_FRACTION_2,
        NOTIONAL_WITH_ACCRUED, CURVES_NAMES[1]);
    assertFalse("CouponOIS derivative: equal/hash code", EONIA_COUPON_DEFINITION.equals(modified));
    modified = new CouponOIS(EUR_CUR, PAYMENT_TIME_2, CURVES_NAMES[0], PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, START_FIXING_TIME_2, END_FIXING_TIME_2, FIXING_YEAR_FRACTION_2 + 0.1,
        NOTIONAL_WITH_ACCRUED, CURVES_NAMES[1]);
    assertFalse("CouponOIS derivative: equal/hash code", EONIA_COUPON_DEFINITION.equals(modified));
    modified = new CouponOIS(EUR_CUR, PAYMENT_TIME_2, CURVES_NAMES[0], PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, START_FIXING_TIME_2, END_FIXING_TIME_2, FIXING_YEAR_FRACTION_2,
        NOTIONAL_WITH_ACCRUED + 123.4, CURVES_NAMES[1]);
    assertFalse("CouponOIS derivative: equal/hash code", EONIA_COUPON_DEFINITION.equals(modified));
  }

}
