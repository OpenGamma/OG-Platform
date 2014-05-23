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

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test class for the coupon fixed accrued compounding.
 */
@Test(groups = TestGroup.UNIT)
public class CouponFixedAccruedCompoundingDefinitionTest {

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final IndexIborMaster MASTER_IBOR = IndexIborMaster.getInstance();
  private static final IborIndex USDLIBOR1M = MASTER_IBOR.getIndex("USDLIBOR1M");
  private static final Currency CURRENCY = USDLIBOR1M.getCurrency();

  private static final Period TENOR_3M = Period.ofMonths(3);
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2012, 8, 24);
  private static final double NOTIONAL = 123454321;
  private static final double FIXED_RATE = .02;

  private static final ZonedDateTime[] ACCRUAL_END_DATES = ScheduleCalculator.getAdjustedDateSchedule(START_DATE, TENOR_3M, true, false, USDLIBOR1M, NYC);
  private static final int NB_SUB_PERIOD = ACCRUAL_END_DATES.length;
  private static final ZonedDateTime[] ACCRUAL_START_DATES = new ZonedDateTime[NB_SUB_PERIOD];
  private static final double[] PAYMENT_ACCRUAL_FACTORS = new double[NB_SUB_PERIOD];
  private static final double PAYMENT_ACCRUAL_FACTOR;

  static {
    ACCRUAL_START_DATES[0] = START_DATE;
    for (int loopsub = 1; loopsub < NB_SUB_PERIOD; loopsub++) {
      ACCRUAL_START_DATES[loopsub] = ACCRUAL_END_DATES[loopsub - 1];
    }
    double af = 0.0;
    for (int loopsub = 0; loopsub < NB_SUB_PERIOD; loopsub++) {
      PAYMENT_ACCRUAL_FACTORS[loopsub] = USDLIBOR1M.getDayCount().getDayCountFraction(ACCRUAL_START_DATES[loopsub], ACCRUAL_END_DATES[loopsub]);
      af += PAYMENT_ACCRUAL_FACTORS[loopsub];
    }
    PAYMENT_ACCRUAL_FACTOR = af;
  }
  private static final ZonedDateTime[] FIXING_PERIOD_END_DATES = ScheduleCalculator.getAdjustedDate(ACCRUAL_START_DATES, USDLIBOR1M, NYC);
  private static final double[] FIXING_ACCRUAL_FACTORS = new double[NB_SUB_PERIOD];
  static {
    for (int loopsub = 0; loopsub < NB_SUB_PERIOD; loopsub++) {
      FIXING_ACCRUAL_FACTORS[loopsub] = USDLIBOR1M.getDayCount().getDayCountFraction(ACCRUAL_START_DATES[loopsub], FIXING_PERIOD_END_DATES[loopsub]);
    }
  }
  private static final ZonedDateTime PAYMENT_DATE = ACCRUAL_END_DATES[NB_SUB_PERIOD - 1];

  private static final CouponFixedAccruedCompoundingDefinition COUPON = CouponFixedAccruedCompoundingDefinition.from(CURRENCY, PAYMENT_DATE, ACCRUAL_START_DATES[0],
      ACCRUAL_END_DATES[NB_SUB_PERIOD - 1], PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXED_RATE, NYC);

  @Test
  /**
   * Tests the getters.
   */
  public void testGetters() {
    assertEquals(COUPON.getPaymentDate(), PAYMENT_DATE);
    assertEquals(COUPON.getAccrualStartDate(), ACCRUAL_START_DATES[0]);
    assertEquals(COUPON.getAccrualEndDate(), ACCRUAL_END_DATES[NB_SUB_PERIOD - 1]);
    assertEquals(COUPON.getPaymentYearFraction(), PAYMENT_ACCRUAL_FACTOR, 1E-10);
    assertEquals(COUPON.getNotional(), NOTIONAL, 1E-2);
    assertEquals(COUPON.getRate(), FIXED_RATE, 1E-10);
    assertEquals(COUPON.getAmount(), NOTIONAL * Math.pow(1 + FIXED_RATE, PAYMENT_ACCRUAL_FACTOR));
    assertEquals(COUPON.getCalendar(), NYC);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency() {
    CouponFixedAccruedCompoundingDefinition.from(null, PAYMENT_DATE, ACCRUAL_START_DATES[0],
        ACCRUAL_END_DATES[NB_SUB_PERIOD - 1], PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXED_RATE, NYC);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPaymentDate() {
    CouponFixedAccruedCompoundingDefinition.from(CURRENCY, null, ACCRUAL_START_DATES[0],
        ACCRUAL_END_DATES[NB_SUB_PERIOD - 1], PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXED_RATE, NYC);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativePaymentAccrualFactor() {
    CouponFixedAccruedCompoundingDefinition.from(CURRENCY, PAYMENT_DATE, ACCRUAL_START_DATES[0],
        ACCRUAL_END_DATES[NB_SUB_PERIOD - 1], -PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXED_RATE, NYC);
  }

  @Test
  /**
   * Tests the equal and hash code.
   */
  public void testEqualHash() {
    assertEquals("CouponIbor: equal-hash", COUPON, COUPON);
    final CouponFixedAccruedCompoundingDefinition other = CouponFixedAccruedCompoundingDefinition.from(CURRENCY, PAYMENT_DATE, ACCRUAL_START_DATES[0],
        ACCRUAL_END_DATES[NB_SUB_PERIOD - 1], PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXED_RATE, NYC);
    assertEquals("CouponFixedCompoundingDefinition: equal-hash", other, COUPON);
    assertEquals("CouponFixedCompoundingDefinition: equal-hash", other.hashCode(), COUPON.hashCode());
    CouponFixedAccruedCompoundingDefinition modified;
    modified = CouponFixedAccruedCompoundingDefinition.from(CURRENCY, PAYMENT_DATE.plusDays(1), ACCRUAL_START_DATES[0],
        ACCRUAL_END_DATES[NB_SUB_PERIOD - 1], PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXED_RATE, NYC);
    assertFalse("CouponIbor: equal-hash", COUPON.equals(modified));
    modified = CouponFixedAccruedCompoundingDefinition.from(CURRENCY, PAYMENT_DATE, ACCRUAL_START_DATES[0].plusDays(1),
        ACCRUAL_END_DATES[NB_SUB_PERIOD - 1], PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXED_RATE, NYC);
    assertFalse("CouponIbor: equal-hash", COUPON.equals(modified));
    modified = CouponFixedAccruedCompoundingDefinition.from(CURRENCY, PAYMENT_DATE, ACCRUAL_START_DATES[0],
        ACCRUAL_END_DATES[NB_SUB_PERIOD - 1].plusDays(1), PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXED_RATE, NYC);
    assertFalse("CouponIbor: equal-hash", COUPON.equals(modified));
    modified = CouponFixedAccruedCompoundingDefinition.from(CURRENCY, PAYMENT_DATE, ACCRUAL_START_DATES[0],
        ACCRUAL_END_DATES[NB_SUB_PERIOD - 1], PAYMENT_ACCRUAL_FACTOR + 1, NOTIONAL, FIXED_RATE, NYC);
    assertFalse("CouponIbor: equal-hash", COUPON.equals(modified));
    modified = CouponFixedAccruedCompoundingDefinition.from(CURRENCY, PAYMENT_DATE, ACCRUAL_START_DATES[0],
        ACCRUAL_END_DATES[NB_SUB_PERIOD - 1], PAYMENT_ACCRUAL_FACTOR, NOTIONAL + 1, FIXED_RATE, NYC);
    assertFalse("CouponIbor: equal-hash", COUPON.equals(modified));
    modified = CouponFixedAccruedCompoundingDefinition.from(CURRENCY, PAYMENT_DATE, ACCRUAL_START_DATES[0],
        ACCRUAL_END_DATES[NB_SUB_PERIOD - 1], PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXED_RATE + 1, NYC);
    assertFalse("CouponIbor: equal-hash", COUPON.equals(modified));
  }

}
