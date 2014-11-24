/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborRatchet;
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
 * Tests the CouponIborRatchetDefinition constructor and toDerivatives.
 */
@Test(groups = TestGroup.UNIT)
public class CouponIborRatchetDefinitionTest {

  private static final Currency CUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");

  private static final Period TENOR_IBOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT_INDEX = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final IborIndex INDEX_IBOR = new IborIndex(CUR, TENOR_IBOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM, "Ibor");

  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2014, 9, 5);
  private static final ZonedDateTime ACCRUAL_START_DATE = ScheduleCalculator.getAdjustedDate(FIXING_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final ZonedDateTime ACCRUAL_END_DATE = ScheduleCalculator.getAdjustedDate(ACCRUAL_START_DATE, TENOR_IBOR, BUSINESS_DAY, CALENDAR, IS_EOM);
  private static final ZonedDateTime PAYMENT_DATE = ACCRUAL_END_DATE;
  private static final DayCount DAY_COUNT_PAYMENT = DayCounts.ACT_365;
  private static final double ACCRUAL_FACTOR = DAY_COUNT_PAYMENT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double[] MAIN_COEF = new double[] {0.4, 0.5, 0.0010 };
  private static final double[] FLOOR_COEF = new double[] {0.75, 0.00, 0.00 };
  private static final double[] CAP_COEF = new double[] {1.50, 1.00, 0.0050 };
  private static final double NOTIONAL = 1000000; //1m
  private static final CouponIborRatchetDefinition RATCHET_IBOR_DEFINITION = new CouponIborRatchetDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
      FIXING_DATE, INDEX_IBOR, MAIN_COEF, FLOOR_COEF, CAP_COEF, CALENDAR);
  private static final String DISCOUNTING_CURVE_NAME = "Discounting";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES = {DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME };

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 9, 5);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullMain() {
    new CouponIborRatchetDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX_IBOR, null, FLOOR_COEF, CAP_COEF, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullFloor() {
    new CouponIborRatchetDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX_IBOR, MAIN_COEF, null, CAP_COEF, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCap() {
    new CouponIborRatchetDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX_IBOR, MAIN_COEF, FLOOR_COEF, null, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void numberMain() {
    new CouponIborRatchetDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX_IBOR, new double[2], FLOOR_COEF, CAP_COEF, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void numberFloor() {
    new CouponIborRatchetDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX_IBOR, MAIN_COEF, new double[1], CAP_COEF, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void numberCap() {
    new CouponIborRatchetDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX_IBOR, MAIN_COEF, FLOOR_COEF, new double[4], CALENDAR);
  }

  @Test
  public void getter() {
    assertEquals("Ratchet Ibor Coupon: getter", MAIN_COEF, RATCHET_IBOR_DEFINITION.getMainCoefficients());
    assertEquals("Ratchet Ibor Coupon: getter", FLOOR_COEF, RATCHET_IBOR_DEFINITION.getFloorCoefficients());
    assertEquals("Ratchet Ibor Coupon: getter", CAP_COEF, RATCHET_IBOR_DEFINITION.getCapCoefficients());
  }

  @Test
  public void testEqualHash() {
    assertEquals("Ratchet Ibor Coupon: equal/hash", RATCHET_IBOR_DEFINITION, RATCHET_IBOR_DEFINITION);
    final CouponIborRatchetDefinition other = new CouponIborRatchetDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX_IBOR, MAIN_COEF,
        FLOOR_COEF, CAP_COEF, CALENDAR);
    assertEquals("Ratchet Ibor Coupon: equal/hash", RATCHET_IBOR_DEFINITION, other);
    assertEquals("Ratchet Ibor Coupon: equal/hash", RATCHET_IBOR_DEFINITION.hashCode(), other.hashCode());
    CouponIborRatchetDefinition modified;
    modified = new CouponIborRatchetDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX_IBOR, new double[3], FLOOR_COEF, CAP_COEF, CALENDAR);
    assertFalse("Ratchet Ibor Coupon: equal/hash", RATCHET_IBOR_DEFINITION.equals(modified));
    modified = new CouponIborRatchetDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX_IBOR, MAIN_COEF, new double[3], CAP_COEF, CALENDAR);
    assertFalse("Ratchet Ibor Coupon: equal/hash", RATCHET_IBOR_DEFINITION.equals(modified));
    modified = new CouponIborRatchetDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX_IBOR, MAIN_COEF, FLOOR_COEF, new double[3], CALENDAR);
    assertFalse("Ratchet Ibor Coupon: equal/hash", RATCHET_IBOR_DEFINITION.equals(modified));
  }

  @Test
  /**
   * Test the toDerivative of Ratchet coupon with no fixing rate available.
   */
  public void toDerivativesNoFixing() {
    final CouponIborRatchet cpnConverted = RATCHET_IBOR_DEFINITION.toDerivative(REFERENCE_DATE);
    final double paymentTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, PAYMENT_DATE);
    final double fixingTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, RATCHET_IBOR_DEFINITION.getFixingDate());
    final double fixingPeriodStartTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, RATCHET_IBOR_DEFINITION.getFixingPeriodStartDate());
    final double fixingPeriodEndTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, RATCHET_IBOR_DEFINITION.getFixingPeriodEndDate());
    final CouponIborRatchet cpnExpected = new CouponIborRatchet(CUR, paymentTime, ACCRUAL_FACTOR, NOTIONAL, fixingTime, fixingPeriodStartTime, fixingPeriodEndTime,
        RATCHET_IBOR_DEFINITION.getFixingPeriodAccrualFactor(), INDEX_IBOR, MAIN_COEF, FLOOR_COEF, CAP_COEF);
    assertEquals("Ratchet Ibor Coupon: toDerivatives", cpnExpected, cpnConverted);
  }

}
