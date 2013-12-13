/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.analytics.financial.instrument.index.IndexONMaster;
import com.opengamma.analytics.financial.instrument.payment.CouponONSpreadSimplifiedDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the coupon overnight-indexed with spread derivative.
 */
@Test(groups = TestGroup.UNIT)
public class CouponONSpreadTest {

  private static final int EUR_SETTLEMENT_DAYS = 2;
  private static final BusinessDayConvention EUR_BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean EUR_IS_EOM = true;

  private static final IndexON EONIA = IndexONMaster.getInstance().getIndex("EONIA");
  private static final IndexON FEDFUND = IndexONMaster.getInstance().getIndex("FED FUND");
  private static final Calendar EUR_CALENDAR = new MondayToFridayCalendar("EUR");
  private static final Currency EUR = EONIA.getCurrency();

  // Coupon EONIA 3m
  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2011, 9, 7);
  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(TRADE_DATE, EUR_SETTLEMENT_DAYS, EUR_CALENDAR);
  private static final Period EUR_CPN_TENOR = Period.ofMonths(3);
  private static final ZonedDateTime START_ACCRUAL_DATE = SPOT_DATE;
  private static final ZonedDateTime END_ACCRUAL_DATE = ScheduleCalculator.getAdjustedDate(START_ACCRUAL_DATE, EUR_CPN_TENOR, EUR_BUSINESS_DAY, EUR_CALENDAR, EUR_IS_EOM);
  private static ZonedDateTime LAST_FIXING_DATE = ScheduleCalculator.getAdjustedDate(END_ACCRUAL_DATE, -1, EUR_CALENDAR); // Overnight
  static {
    LAST_FIXING_DATE = ScheduleCalculator.getAdjustedDate(LAST_FIXING_DATE, EONIA.getPublicationLag(), EUR_CALENDAR); // Lag
  }
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(LAST_FIXING_DATE, EUR_SETTLEMENT_DAYS, EUR_CALENDAR);
  private static final double PAYMENT_ACCRUAL_FACTOR = EONIA.getDayCount().getDayCountFraction(START_ACCRUAL_DATE, END_ACCRUAL_DATE);
  private static final double NOTIONAL = 100000000;
  private static final double SPREAD = 0.0010;
  private static final double SPREAD_AMOUNT = SPREAD * NOTIONAL * PAYMENT_ACCRUAL_FACTOR;
  private static final double FIXING_YEAR_FRACTION = EONIA.getDayCount().getDayCountFraction(START_ACCRUAL_DATE, END_ACCRUAL_DATE);
  private static final CouponONSpreadSimplifiedDefinition EONIA_COUPON_DEFINITION = new CouponONSpreadSimplifiedDefinition(EUR, PAYMENT_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE,
      PAYMENT_ACCRUAL_FACTOR, NOTIONAL, EONIA, START_ACCRUAL_DATE, END_ACCRUAL_DATE, FIXING_YEAR_FRACTION, SPREAD);

  private static final ZonedDateTime REFERENCE_DATE_1 = DateUtils.getUTCDate(2011, 9, 7);
  private static final double PAYMENT_TIME_1 = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, PAYMENT_DATE);
  private static final double START_ACCRUAL_TIME_1 = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, START_ACCRUAL_DATE);
  private static final double END_ACCRUAL_TIME_1 = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, END_ACCRUAL_DATE);
  private static final CouponONSpread EONIA_COUPON_NOTSTARTED = new CouponONSpread(EUR, PAYMENT_TIME_1, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, EONIA, START_ACCRUAL_TIME_1,
      END_ACCRUAL_TIME_1, FIXING_YEAR_FRACTION, NOTIONAL, SPREAD_AMOUNT);

  private static final ZonedDateTime REFERENCE_DATE_2 = DateUtils.getUTCDate(2011, 10, 7);
  private static final ZonedDateTime NEXT_FIXING_DATE_2 = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE_2, 1, EUR_CALENDAR); // Overnight
  private static final double PAYMENT_TIME_2 = TimeCalculator.getTimeBetween(REFERENCE_DATE_2, PAYMENT_DATE);
  private static final double START_FIXING_TIME_2 = TimeCalculator.getTimeBetween(REFERENCE_DATE_2, NEXT_FIXING_DATE_2);
  private static final double END_FIXING_TIME_2 = TimeCalculator.getTimeBetween(REFERENCE_DATE_2, END_ACCRUAL_DATE);
  private static final double FIXING_YEAR_FRACTION_2 = EONIA.getDayCount().getDayCountFraction(NEXT_FIXING_DATE_2, END_ACCRUAL_DATE);
  private static final double NOTIONAL_WITH_ACCRUED = NOTIONAL * (1.0 + 0.01 / 12); // 1% over a month (roughly)
  private static final CouponONSpread EONIA_COUPON_STARTED = new CouponONSpread(EUR, PAYMENT_TIME_2, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, EONIA, START_FIXING_TIME_2, END_FIXING_TIME_2,
      FIXING_YEAR_FRACTION_2, NOTIONAL_WITH_ACCRUED, SPREAD_AMOUNT);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullIndex() {
    new CouponONSpread(EUR, PAYMENT_TIME_1, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, null, START_ACCRUAL_TIME_1, END_ACCRUAL_TIME_1, FIXING_YEAR_FRACTION, NOTIONAL, SPREAD);
  }

  @Test
  public void getterNotStarted() {
    assertEquals("CouponONSpread: getter", EONIA, EONIA_COUPON_NOTSTARTED.getIndex());
    assertEquals("CouponONSpread: getter", START_ACCRUAL_TIME_1, EONIA_COUPON_NOTSTARTED.getFixingPeriodStartTime());
    assertEquals("CouponONSpread: getter", END_ACCRUAL_TIME_1, EONIA_COUPON_NOTSTARTED.getFixingPeriodEndTime());
    assertEquals("CouponONSpread: getter", FIXING_YEAR_FRACTION, EONIA_COUPON_NOTSTARTED.getFixingPeriodAccrualFactor());
    assertEquals("CouponONSpread: getter", NOTIONAL, EONIA_COUPON_NOTSTARTED.getNotionalAccrued());
    assertEquals("CouponONSpread: getter", SPREAD_AMOUNT, EONIA_COUPON_NOTSTARTED.getSpreadAmount());
  }

  @Test
  public void getterStarted() {
    assertEquals("CouponONSpread: getter", EONIA, EONIA_COUPON_STARTED.getIndex());
    assertEquals("CouponONSpread: getter", START_FIXING_TIME_2, EONIA_COUPON_STARTED.getFixingPeriodStartTime());
    assertEquals("CouponONSpread: getter", END_FIXING_TIME_2, EONIA_COUPON_STARTED.getFixingPeriodEndTime());
    assertEquals("CouponONSpread: getter", FIXING_YEAR_FRACTION_2, EONIA_COUPON_STARTED.getFixingPeriodAccrualFactor());
    assertEquals("CouponONSpread: getter", NOTIONAL_WITH_ACCRUED, EONIA_COUPON_STARTED.getNotionalAccrued());
    assertEquals("CouponONSpread: getter", SPREAD_AMOUNT, EONIA_COUPON_STARTED.getSpreadAmount());
  }

  @Test
  /**
   * Tests the equal and hashCode methods.
   */
  public void equalHash() {
    assertEquals("CouponOIS derivative: equal/hash code", EONIA_COUPON_STARTED, EONIA_COUPON_STARTED);
    final CouponONSpread other = new CouponONSpread(EUR, PAYMENT_TIME_2, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, EONIA, START_FIXING_TIME_2, END_FIXING_TIME_2, FIXING_YEAR_FRACTION_2,
        NOTIONAL_WITH_ACCRUED, SPREAD_AMOUNT);
    assertEquals("CouponOIS derivative: equal/hash code", EONIA_COUPON_STARTED, other);
    assertEquals("CouponOIS derivative: equal/hash code", EONIA_COUPON_STARTED.hashCode(), other.hashCode());
    CouponONSpread modified;
    modified = new CouponONSpread(EUR, PAYMENT_TIME_2, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FEDFUND, START_FIXING_TIME_2, END_FIXING_TIME_2, FIXING_YEAR_FRACTION_2,
        NOTIONAL_WITH_ACCRUED, SPREAD_AMOUNT);
    assertFalse("CouponOIS derivative: equal/hash code", EONIA_COUPON_DEFINITION.equals(modified));
    modified = new CouponONSpread(EUR, PAYMENT_TIME_2, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, EONIA, START_FIXING_TIME_2 + 0.1, END_FIXING_TIME_2, FIXING_YEAR_FRACTION_2,
        NOTIONAL_WITH_ACCRUED, SPREAD_AMOUNT);
    assertFalse("CouponOIS derivative: equal/hash code", EONIA_COUPON_DEFINITION.equals(modified));
    modified = new CouponONSpread(EUR, PAYMENT_TIME_2, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, EONIA, START_FIXING_TIME_2, END_FIXING_TIME_2 + 0.1, FIXING_YEAR_FRACTION_2,
        NOTIONAL_WITH_ACCRUED, SPREAD_AMOUNT);
    assertFalse("CouponOIS derivative: equal/hash code", EONIA_COUPON_DEFINITION.equals(modified));
    modified = new CouponONSpread(EUR, PAYMENT_TIME_2, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, EONIA, START_FIXING_TIME_2, END_FIXING_TIME_2, FIXING_YEAR_FRACTION_2 + 0.1,
        NOTIONAL_WITH_ACCRUED, SPREAD_AMOUNT);
    assertFalse("CouponOIS derivative: equal/hash code", EONIA_COUPON_DEFINITION.equals(modified));
    modified = new CouponONSpread(EUR, PAYMENT_TIME_2, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, EONIA, START_FIXING_TIME_2, END_FIXING_TIME_2, FIXING_YEAR_FRACTION_2,
        NOTIONAL_WITH_ACCRUED + 123.4, SPREAD_AMOUNT);
    assertFalse("CouponOIS derivative: equal/hash code", EONIA_COUPON_DEFINITION.equals(modified));
    modified = new CouponONSpread(EUR, PAYMENT_TIME_2, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, EONIA, START_FIXING_TIME_2, END_FIXING_TIME_2, FIXING_YEAR_FRACTION_2,
        NOTIONAL_WITH_ACCRUED, SPREAD_AMOUNT + 12.34);
    assertFalse("CouponOIS derivative: equal/hash code", EONIA_COUPON_DEFINITION.equals(modified));
  }

}
