/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.payment;

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
import com.opengamma.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.payments.derivative.CouponOIS;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.TimeCalculator;

/**
 * Tests related to the simplified version of the OIS coupon definition.
 */
public class CouponOISSimplifiedDefinitionTest {
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
  private static final CouponOISSimplifiedDefinition EONIA_COUPON_DEFINITION = new CouponOISSimplifiedDefinition(EUR_CUR, PAYMENT_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, PAYMENT_YEAR_FRACTION,
      NOTIONAL, EUR_OIS, START_ACCRUAL_DATE, END_ACCRUAL_DATE, FIXING_YEAR_FRACTION);

  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
  private static final String[] CURVES_NAMES = CURVES.getAllNames().toArray(new String[0]);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullIndex() {
    new CouponOISSimplifiedDefinition(EUR_CUR, PAYMENT_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, PAYMENT_YEAR_FRACTION, NOTIONAL, null, START_ACCRUAL_DATE, END_ACCRUAL_DATE, FIXING_YEAR_FRACTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullStartFixing() {
    new CouponOISSimplifiedDefinition(EUR_CUR, PAYMENT_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, null, END_ACCRUAL_DATE, FIXING_YEAR_FRACTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullEndFixing() {
    new CouponOISSimplifiedDefinition(EUR_CUR, PAYMENT_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, START_ACCRUAL_DATE, null, FIXING_YEAR_FRACTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void inmcompatibleCurrency() {
    new CouponOISSimplifiedDefinition(Currency.USD, PAYMENT_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, START_ACCRUAL_DATE, null, FIXING_YEAR_FRACTION);
  }

  @Test
  public void getter() {
    assertEquals("CouponOISSimplified definition: getter", EUR_OIS, EONIA_COUPON_DEFINITION.getIndex());
    assertEquals("CouponOISSimplified definition: getter", START_ACCRUAL_DATE, EONIA_COUPON_DEFINITION.getFixingPeriodStartDate());
    assertEquals("CouponOISSimplified definition: getter", END_ACCRUAL_DATE, EONIA_COUPON_DEFINITION.getFixingPeriodEndDate());
    assertEquals("CouponOISSimplified definition: getter", FIXING_YEAR_FRACTION, EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor());
  }

  @Test
  /**
   * Tests the builder from financial details.
   */
  public void from1() {
    CouponOISSimplifiedDefinition cpnFrom = CouponOISSimplifiedDefinition.from(EUR_OIS, SPOT_DATE, EUR_CPN_TENOR, NOTIONAL, EUR_SETTLEMENT_DAYS, EUR_BUSINESS_DAY, EUR_IS_EOM);
    assertEquals("CouponOISSimplified definition: from", cpnFrom, EONIA_COUPON_DEFINITION);
  }

  @Test
  /**
   * Tests the builder from financial details.
   */
  public void from2() {
    CouponOISSimplifiedDefinition cpnFrom = CouponOISSimplifiedDefinition.from(EUR_OIS, SPOT_DATE, END_ACCRUAL_DATE, NOTIONAL, EUR_SETTLEMENT_DAYS);
    assertEquals("CouponOISSimplified definition: from", cpnFrom, EONIA_COUPON_DEFINITION);
  }

  @Test
  /**
   * Tests the equal and hashCode methods.
   */
  public void equalHash() {
    assertEquals("CouponOISSimplified definition: equal/hash code", EONIA_COUPON_DEFINITION, EONIA_COUPON_DEFINITION);
    CouponOISSimplifiedDefinition other = new CouponOISSimplifiedDefinition(EUR_CUR, PAYMENT_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, START_ACCRUAL_DATE,
        END_ACCRUAL_DATE, FIXING_YEAR_FRACTION);
    assertEquals("CouponOISSimplified definition: equal/hash code", EONIA_COUPON_DEFINITION, other);
    assertEquals("CouponOISSimplified definition: equal/hash code", EONIA_COUPON_DEFINITION.hashCode(), other.hashCode());
    CouponOISSimplifiedDefinition modified;
    modified = new CouponOISSimplifiedDefinition(EUR_CUR, PAYMENT_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, PAYMENT_YEAR_FRACTION, NOTIONAL, new IndexON(EUR_OIS_NAME, EUR_CUR, EUR_DAY_COUNT, 27,
        EUR_CALENDAR), START_ACCRUAL_DATE, END_ACCRUAL_DATE, FIXING_YEAR_FRACTION);
    assertFalse("CouponOISSimplified definition: equal/hash code", EONIA_COUPON_DEFINITION.equals(modified));
    modified = new CouponOISSimplifiedDefinition(EUR_CUR, PAYMENT_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, END_ACCRUAL_DATE, END_ACCRUAL_DATE,
        FIXING_YEAR_FRACTION);
    assertFalse("CouponOISSimplified definition: equal/hash code", EONIA_COUPON_DEFINITION.equals(modified));
    modified = new CouponOISSimplifiedDefinition(EUR_CUR, PAYMENT_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, START_ACCRUAL_DATE, START_ACCRUAL_DATE,
        FIXING_YEAR_FRACTION);
    assertFalse("CouponOISSimplified definition: equal/hash code", EONIA_COUPON_DEFINITION.equals(modified));
    modified = new CouponOISSimplifiedDefinition(EUR_CUR, PAYMENT_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, START_ACCRUAL_DATE, END_ACCRUAL_DATE, 3.14);
    assertFalse("CouponOISSimplified definition: equal/hash code", EONIA_COUPON_DEFINITION.equals(modified));
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivative() {
    CouponOIS cpnConverted = EONIA_COUPON_DEFINITION.toDerivative(TRADE_DATE, CURVES_NAMES);
    double paymentTime = TimeCalculator.getTimeBetween(TRADE_DATE, PAYMENT_DATE);
    double fixingStartTime = TimeCalculator.getTimeBetween(TRADE_DATE, START_ACCRUAL_DATE);
    double fixingEndTime = TimeCalculator.getTimeBetween(TRADE_DATE, END_ACCRUAL_DATE);
    CouponOIS cpnExpected = new CouponOIS(EUR_CUR, paymentTime, CURVES_NAMES[0], PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, fixingStartTime, fixingEndTime, FIXING_YEAR_FRACTION, NOTIONAL,
        CURVES_NAMES[1]);
    assertEquals("CouponOISSimplified definition: toDerivative", cpnExpected, cpnConverted);
  }

}
