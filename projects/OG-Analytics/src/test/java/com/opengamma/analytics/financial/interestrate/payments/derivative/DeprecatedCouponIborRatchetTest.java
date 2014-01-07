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

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponIborRatchetDefinition;
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
 * Tests the CouponIborRatchet constructor.
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class DeprecatedCouponIborRatchetTest {

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
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 9, 5);
  private static final String DISCOUNTING_CURVE_NAME = "Discounting";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES = {DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME };
  private static final CouponIborRatchet RATCHET_IBOR = RATCHET_IBOR_DEFINITION.toDerivative(REFERENCE_DATE, CURVES);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullIndex() {
    new CouponIborRatchet(CUR, RATCHET_IBOR.getPaymentTime(), DISCOUNTING_CURVE_NAME, RATCHET_IBOR.getPaymentYearFraction(), NOTIONAL, RATCHET_IBOR.getFixingTime(),
        RATCHET_IBOR.getFixingPeriodStartTime(), RATCHET_IBOR.getFixingPeriodEndTime(), RATCHET_IBOR.getFixingAccrualFactor(), FORWARD_CURVE_NAME, null, MAIN_COEF, FLOOR_COEF, CAP_COEF);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullMain() {
    new CouponIborRatchet(CUR, RATCHET_IBOR.getPaymentTime(), DISCOUNTING_CURVE_NAME, RATCHET_IBOR.getPaymentYearFraction(), NOTIONAL, RATCHET_IBOR.getFixingTime(),
        RATCHET_IBOR.getFixingPeriodStartTime(), RATCHET_IBOR.getFixingPeriodEndTime(), RATCHET_IBOR.getFixingAccrualFactor(), FORWARD_CURVE_NAME, INDEX_IBOR, null, FLOOR_COEF, CAP_COEF);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullFloor() {
    new CouponIborRatchet(CUR, RATCHET_IBOR.getPaymentTime(), DISCOUNTING_CURVE_NAME, RATCHET_IBOR.getPaymentYearFraction(), NOTIONAL, RATCHET_IBOR.getFixingTime(),
        RATCHET_IBOR.getFixingPeriodStartTime(), RATCHET_IBOR.getFixingPeriodEndTime(), RATCHET_IBOR.getFixingAccrualFactor(), FORWARD_CURVE_NAME, INDEX_IBOR, MAIN_COEF, null, CAP_COEF);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCap() {
    new CouponIborRatchet(CUR, RATCHET_IBOR.getPaymentTime(), DISCOUNTING_CURVE_NAME, RATCHET_IBOR.getPaymentYearFraction(), NOTIONAL, RATCHET_IBOR.getFixingTime(),
        RATCHET_IBOR.getFixingPeriodStartTime(), RATCHET_IBOR.getFixingPeriodEndTime(), RATCHET_IBOR.getFixingAccrualFactor(), FORWARD_CURVE_NAME, INDEX_IBOR, MAIN_COEF, FLOOR_COEF, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void numberMain() {
    new CouponIborRatchet(CUR, RATCHET_IBOR.getPaymentTime(), DISCOUNTING_CURVE_NAME, RATCHET_IBOR.getPaymentYearFraction(), NOTIONAL, RATCHET_IBOR.getFixingTime(),
        RATCHET_IBOR.getFixingPeriodStartTime(), RATCHET_IBOR.getFixingPeriodEndTime(), RATCHET_IBOR.getFixingAccrualFactor(), FORWARD_CURVE_NAME, INDEX_IBOR, new double[2], FLOOR_COEF, CAP_COEF);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void numberFloor() {
    new CouponIborRatchet(CUR, RATCHET_IBOR.getPaymentTime(), DISCOUNTING_CURVE_NAME, RATCHET_IBOR.getPaymentYearFraction(), NOTIONAL, RATCHET_IBOR.getFixingTime(),
        RATCHET_IBOR.getFixingPeriodStartTime(), RATCHET_IBOR.getFixingPeriodEndTime(), RATCHET_IBOR.getFixingAccrualFactor(), FORWARD_CURVE_NAME, INDEX_IBOR, MAIN_COEF, new double[2], CAP_COEF);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void numberCap() {
    new CouponIborRatchet(CUR, RATCHET_IBOR.getPaymentTime(), DISCOUNTING_CURVE_NAME, RATCHET_IBOR.getPaymentYearFraction(), NOTIONAL, RATCHET_IBOR.getFixingTime(),
        RATCHET_IBOR.getFixingPeriodStartTime(), RATCHET_IBOR.getFixingPeriodEndTime(), RATCHET_IBOR.getFixingAccrualFactor(), FORWARD_CURVE_NAME, INDEX_IBOR, MAIN_COEF, FLOOR_COEF, new double[2]);
  }

  @Test
  public void getter() {
    assertEquals("Ratchet Ibor Coupon: getter", MAIN_COEF, RATCHET_IBOR.getMainCoefficients());
    assertEquals("Ratchet Ibor Coupon: getter", FLOOR_COEF, RATCHET_IBOR.getFloorCoefficients());
    assertEquals("Ratchet Ibor Coupon: getter", CAP_COEF, RATCHET_IBOR.getCapCoefficients());
  }

  @Test
  public void testWithNotional() {
    final double notional = NOTIONAL + 100;
    final CouponIborRatchet expected = new CouponIborRatchet(CUR, 0.25, DISCOUNTING_CURVE_NAME, 0.25, notional, 0.1, 0.1, 0.2, 0.25, FORWARD_CURVE_NAME, INDEX_IBOR, MAIN_COEF, FLOOR_COEF, CAP_COEF);
    final CouponIborRatchet coupon = new CouponIborRatchet(CUR, 0.25, DISCOUNTING_CURVE_NAME, 0.25, NOTIONAL, 0.1, 0.1, 0.2, 0.25, FORWARD_CURVE_NAME, INDEX_IBOR, MAIN_COEF, FLOOR_COEF, CAP_COEF);
    assertEquals(expected, coupon.withNotional(notional));
  }

  //TODO test other builder methods

  @Test
  public void testEqualHash() {
    assertEquals("Ratchet Ibor Coupon: equal/hash", RATCHET_IBOR, RATCHET_IBOR);
    final CouponIborRatchet other = new CouponIborRatchet(CUR, RATCHET_IBOR.getPaymentTime(), DISCOUNTING_CURVE_NAME, RATCHET_IBOR.getPaymentYearFraction(), NOTIONAL, RATCHET_IBOR.getFixingTime(),
        RATCHET_IBOR.getFixingPeriodStartTime(), RATCHET_IBOR.getFixingPeriodEndTime(), RATCHET_IBOR.getFixingAccrualFactor(), FORWARD_CURVE_NAME, INDEX_IBOR, MAIN_COEF, FLOOR_COEF, CAP_COEF);
    assertEquals("Ratchet Ibor Coupon: equal/hash", RATCHET_IBOR, other);
    assertEquals("Ratchet Ibor Coupon: equal/hash", RATCHET_IBOR.hashCode(), other.hashCode());
    CouponIborRatchet modified;
    modified = new CouponIborRatchet(CUR, RATCHET_IBOR.getPaymentTime(), DISCOUNTING_CURVE_NAME, RATCHET_IBOR.getPaymentYearFraction(), NOTIONAL, RATCHET_IBOR.getFixingTime(),
        RATCHET_IBOR.getFixingPeriodStartTime(), RATCHET_IBOR.getFixingPeriodEndTime(), RATCHET_IBOR.getFixingAccrualFactor(), FORWARD_CURVE_NAME, INDEX_IBOR, new double[3], FLOOR_COEF, CAP_COEF);
    assertFalse("Ratchet Ibor Coupon: equal/hash", RATCHET_IBOR_DEFINITION.equals(modified));
    modified = new CouponIborRatchet(CUR, RATCHET_IBOR.getPaymentTime(), DISCOUNTING_CURVE_NAME, RATCHET_IBOR.getPaymentYearFraction(), NOTIONAL, RATCHET_IBOR.getFixingTime(),
        RATCHET_IBOR.getFixingPeriodStartTime(), RATCHET_IBOR.getFixingPeriodEndTime(), RATCHET_IBOR.getFixingAccrualFactor(), FORWARD_CURVE_NAME, INDEX_IBOR, MAIN_COEF, new double[3], CAP_COEF);
    assertFalse("Ratchet Ibor Coupon: equal/hash", RATCHET_IBOR_DEFINITION.equals(modified));
    modified = new CouponIborRatchet(CUR, RATCHET_IBOR.getPaymentTime(), DISCOUNTING_CURVE_NAME, RATCHET_IBOR.getPaymentYearFraction(), NOTIONAL, RATCHET_IBOR.getFixingTime(),
        RATCHET_IBOR.getFixingPeriodStartTime(), RATCHET_IBOR.getFixingPeriodEndTime(), RATCHET_IBOR.getFixingAccrualFactor(), FORWARD_CURVE_NAME, INDEX_IBOR, MAIN_COEF, FLOOR_COEF, new double[3]);
    assertFalse("Ratchet Ibor Coupon: equal/hash", RATCHET_IBOR_DEFINITION.equals(modified));
  }

}
