/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONSpread;
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
 * Tests related to the simplified version of the OIS coupon definition.
 */
@Test(groups = TestGroup.UNIT)
public class CouponONSpreadSimplifiedDefinitionTest {
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
  private static final double PAYMENT_ACCRUAL_FACTOR = EUR_DAY_COUNT.getDayCountFraction(START_ACCRUAL_DATE, END_ACCRUAL_DATE);
  private static final double NOTIONAL = 100000000;
  private static final double SPREAD = 0.0010;
  private static final double FIXING_YEAR_FRACTION = EUR_DAY_COUNT.getDayCountFraction(START_ACCRUAL_DATE, END_ACCRUAL_DATE);
  private static final CouponONSpreadSimplifiedDefinition EONIA_COUPON_DEFINITION = new CouponONSpreadSimplifiedDefinition(EUR_CUR, PAYMENT_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE,
      PAYMENT_ACCRUAL_FACTOR,
      NOTIONAL, EUR_OIS, START_ACCRUAL_DATE, END_ACCRUAL_DATE, FIXING_YEAR_FRACTION, SPREAD);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullIndex() {
    new CouponONSpreadSimplifiedDefinition(EUR_CUR, PAYMENT_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, null, START_ACCRUAL_DATE, END_ACCRUAL_DATE,
        FIXING_YEAR_FRACTION, SPREAD);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullStartFixing() {
    new CouponONSpreadSimplifiedDefinition(EUR_CUR, PAYMENT_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, EUR_OIS, null, END_ACCRUAL_DATE, FIXING_YEAR_FRACTION, SPREAD);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullEndFixing() {
    new CouponONSpreadSimplifiedDefinition(EUR_CUR, PAYMENT_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, EUR_OIS, START_ACCRUAL_DATE, null, FIXING_YEAR_FRACTION,
        SPREAD);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void inmcompatibleCurrency() {
    new CouponONSpreadSimplifiedDefinition(Currency.EUR, PAYMENT_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, EUR_OIS, START_ACCRUAL_DATE, null, FIXING_YEAR_FRACTION,
        SPREAD);
  }

  @Test
  public void getter() {
    assertEquals("CouponONSpreadSimplified definition: getter", EUR_OIS, EONIA_COUPON_DEFINITION.getIndex());
    assertEquals("CouponONSpreadSimplified definition: getter", START_ACCRUAL_DATE, EONIA_COUPON_DEFINITION.getFixingPeriodStartDate());
    assertEquals("CouponONSpreadSimplified definition: getter", END_ACCRUAL_DATE, EONIA_COUPON_DEFINITION.getFixingPeriodEndDate());
    assertEquals("CouponONSpreadSimplified definition: getter", FIXING_YEAR_FRACTION, EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor());
    assertEquals("CouponONSpreadSimplified definition: getter", NOTIONAL, EONIA_COUPON_DEFINITION.getNotional());
    assertEquals("CouponONSpreadSimplified definition: getter", SPREAD, EONIA_COUPON_DEFINITION.getSpread());
    assertEquals("CouponONSpreadSimplified definition: getter", SPREAD * NOTIONAL * PAYMENT_ACCRUAL_FACTOR, EONIA_COUPON_DEFINITION.getSpreadAmount());
  }

  @Test
  /**
   * Tests the builder from financial details.
   */
  public void from1() {
    final CouponONSpreadSimplifiedDefinition cpnFrom = CouponONSpreadSimplifiedDefinition.from(EUR_OIS, SPOT_DATE, EUR_CPN_TENOR, NOTIONAL, SPREAD, EUR_SETTLEMENT_DAYS, EUR_BUSINESS_DAY, EUR_IS_EOM,
        EUR_CALENDAR);
    assertEquals("CouponOISSimplified definition: from", cpnFrom, EONIA_COUPON_DEFINITION);
  }

  @Test
  /**
   * Tests the builder from financial details.
   */
  public void from2() {
    final CouponONSpreadSimplifiedDefinition cpnFrom = CouponONSpreadSimplifiedDefinition.from(EUR_OIS, SPOT_DATE, END_ACCRUAL_DATE, NOTIONAL, SPREAD, EUR_SETTLEMENT_DAYS, EUR_CALENDAR);
    assertEquals("CouponOISSimplified definition: from", cpnFrom, EONIA_COUPON_DEFINITION);
  }

  @Test
  /**
   * Tests the equal and hashCode methods.
   */
  public void equalHash() {
    assertEquals("CouponOISSimplified definition: equal/hash code", EONIA_COUPON_DEFINITION, EONIA_COUPON_DEFINITION);
    final CouponONSpreadSimplifiedDefinition other = new CouponONSpreadSimplifiedDefinition(EUR_CUR, PAYMENT_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, EUR_OIS,
        START_ACCRUAL_DATE, END_ACCRUAL_DATE, FIXING_YEAR_FRACTION, SPREAD);
    assertEquals("CouponOISSimplified definition: equal/hash code", EONIA_COUPON_DEFINITION, other);
    assertEquals("CouponOISSimplified definition: equal/hash code", EONIA_COUPON_DEFINITION.hashCode(), other.hashCode());
    CouponONSpreadSimplifiedDefinition modified;
    modified = new CouponONSpreadSimplifiedDefinition(EUR_CUR, PAYMENT_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, new IndexON(EUR_OIS_NAME, EUR_CUR, EUR_DAY_COUNT,
        1),
        START_ACCRUAL_DATE, END_ACCRUAL_DATE, FIXING_YEAR_FRACTION, SPREAD);
    assertFalse("CouponOISSimplified definition: equal/hash code", EONIA_COUPON_DEFINITION.equals(modified));
    modified = new CouponONSpreadSimplifiedDefinition(EUR_CUR, PAYMENT_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, EUR_OIS, END_ACCRUAL_DATE, END_ACCRUAL_DATE,
        FIXING_YEAR_FRACTION, SPREAD);
    assertFalse("CouponOISSimplified definition: equal/hash code", EONIA_COUPON_DEFINITION.equals(modified));
    modified = new CouponONSpreadSimplifiedDefinition(EUR_CUR, PAYMENT_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, EUR_OIS, START_ACCRUAL_DATE, START_ACCRUAL_DATE,
        FIXING_YEAR_FRACTION, SPREAD);
    assertFalse("CouponOISSimplified definition: equal/hash code", EONIA_COUPON_DEFINITION.equals(modified));
    modified = new CouponONSpreadSimplifiedDefinition(EUR_CUR, PAYMENT_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, EUR_OIS, START_ACCRUAL_DATE, END_ACCRUAL_DATE,
        3.14, SPREAD);
    assertFalse("CouponOISSimplified definition: equal/hash code", EONIA_COUPON_DEFINITION.equals(modified));
    modified = new CouponONSpreadSimplifiedDefinition(EUR_CUR, PAYMENT_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, EUR_OIS, START_ACCRUAL_DATE, END_ACCRUAL_DATE,
        FIXING_YEAR_FRACTION, 3.14);
    assertFalse("CouponOISSimplified definition: equal/hash code", EONIA_COUPON_DEFINITION.equals(modified));
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivative() {
    final CouponONSpread cpnConverted = EONIA_COUPON_DEFINITION.toDerivative(TRADE_DATE);
    final double paymentTime = TimeCalculator.getTimeBetween(TRADE_DATE, PAYMENT_DATE);
    final double fixingStartTime = TimeCalculator.getTimeBetween(TRADE_DATE, START_ACCRUAL_DATE);
    final double fixingEndTime = TimeCalculator.getTimeBetween(TRADE_DATE, END_ACCRUAL_DATE);
    final double spreadAmount = SPREAD * NOTIONAL * PAYMENT_ACCRUAL_FACTOR;
    final CouponONSpread cpnExpected = new CouponONSpread(EUR_CUR, paymentTime, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, EUR_OIS, fixingStartTime, fixingEndTime, FIXING_YEAR_FRACTION,
        NOTIONAL, spreadAmount);
    assertEquals("CouponOISSimplified definition: toDerivative", cpnExpected, cpnConverted);
  }

  @SuppressWarnings("deprecation")
  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeDeprecated() {
    final String[] curveNames = new String[] {"Funding", "Forward"};
    final CouponONSpread cpnConverted = EONIA_COUPON_DEFINITION.toDerivative(TRADE_DATE, curveNames);
    final double paymentTime = TimeCalculator.getTimeBetween(TRADE_DATE, PAYMENT_DATE);
    final double fixingStartTime = TimeCalculator.getTimeBetween(TRADE_DATE, START_ACCRUAL_DATE);
    final double fixingEndTime = TimeCalculator.getTimeBetween(TRADE_DATE, END_ACCRUAL_DATE);
    final double spreadAmount = SPREAD * NOTIONAL * PAYMENT_ACCRUAL_FACTOR;
    final CouponONSpread cpnExpected = new CouponONSpread(EUR_CUR, paymentTime, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, EUR_OIS, fixingStartTime, fixingEndTime, FIXING_YEAR_FRACTION,
        NOTIONAL, spreadAmount);
    assertEquals("CouponOISSimplified definition: toDerivative", cpnExpected, cpnConverted);
  }
}
