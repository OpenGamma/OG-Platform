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

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test of fixed accrued compounding coupon class.
 */
@Test(groups = TestGroup.UNIT)
public class CouponFixedAccruedCompoundingTest {

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final IndexIborMaster MASTER_IBOR = IndexIborMaster.getInstance();
  private static final IborIndex USDLIBOR1M = MASTER_IBOR.getIndex("USDLIBOR1M");

  private static final Period TENOR_3M = Period.ofMonths(3);
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2012, 8, 24);
  private static final double NOTIONAL = 123454321;
  private static final DayCount DAY_COUNT = DayCounts.BUSINESS_252;

  private static final ZonedDateTime ACCRUAL_END_DATE = ScheduleCalculator.getAdjustedDate(START_DATE, TENOR_3M, NYC);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 8, 17);

  private static final double PAYMENT_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, ACCRUAL_END_DATE);
  private static final double PAYMENT_ACCRUAL_FACTOR = DAY_COUNT.getDayCountFraction(REFERENCE_DATE, ACCRUAL_END_DATE, NYC);
  private static final double FIXED_RATE = .02;

  private static final CouponFixedAccruedCompounding CPN = new CouponFixedAccruedCompounding(USDLIBOR1M.getCurrency(), PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXED_RATE);
  private static final CouponFixedAccruedCompounding CPN_WITH_ACCRUAL_TIMES = new CouponFixedAccruedCompounding(USDLIBOR1M.getCurrency(), PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXED_RATE,
      START_DATE, ACCRUAL_END_DATE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency() {
    new CouponFixedAccruedCompounding(null, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXED_RATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativePaymentTime() {
    new CouponFixedAccruedCompounding(USDLIBOR1M.getCurrency(), -PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXED_RATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativePaymentAccrualFactor() {
    new CouponFixedAccruedCompounding(USDLIBOR1M.getCurrency(), PAYMENT_TIME, -PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXED_RATE);
  }

  @Test
  /**
   * Tests the getters.
   */
  public void getter() {
    assertEquals("CouponFixedCompounding: getter", USDLIBOR1M.getCurrency(), CPN.getCurrency());
    assertEquals("CouponFixedCompounding: getter", PAYMENT_ACCRUAL_FACTOR, CPN.getPaymentYearFraction());
    assertEquals("CouponFixedCompounding: getter", NOTIONAL, CPN.getNotional());
    assertEquals("CouponFixedCompounding: getter", NOTIONAL * Math.pow(1 + FIXED_RATE, PAYMENT_ACCRUAL_FACTOR), CPN.getAmount());
    assertEquals("CouponFixedCompounding: getter", PAYMENT_TIME, CPN.getPaymentTime());
    assertEquals("CouponFixedCompounding: getter", PAYMENT_ACCRUAL_FACTOR, CPN.getPaymentYearFraction());
    assertEquals("CouponFixedCompounding: getter", FIXED_RATE, CPN.getFixedRate());
    assertEquals("CouponFixedCompounding: getter", START_DATE, CPN_WITH_ACCRUAL_TIMES.getAccrualStartDate());
    assertEquals("CouponFixedCompounding: getter", ACCRUAL_END_DATE, CPN_WITH_ACCRUAL_TIMES.getAccrualEndDate());
  }

  @Test
  /**
   * Tests the equal and hash code.
   */
  public void testEqualHash() {
    assertEquals("CouponIbor: equal-hash", CPN, CPN);
    final CouponFixedAccruedCompounding other = new CouponFixedAccruedCompounding(USDLIBOR1M.getCurrency(), PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXED_RATE);
    assertEquals("CouponIbor: equal-hash", other, CPN);
    assertEquals("CouponIbor: equal-hash", other.hashCode(), CPN.hashCode());
    CouponFixedAccruedCompounding modified;
    modified = new CouponFixedAccruedCompounding(USDLIBOR1M.getCurrency(), PAYMENT_TIME + .1, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXED_RATE);
    assertFalse("CouponIbor: equal-hash", CPN.equals(modified));
    modified = new CouponFixedAccruedCompounding(USDLIBOR1M.getCurrency(), PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR + .1, NOTIONAL, FIXED_RATE);
    assertFalse("CouponIbor: equal-hash", CPN.equals(modified));
    modified = new CouponFixedAccruedCompounding(USDLIBOR1M.getCurrency(), PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL + .1, FIXED_RATE);
    assertFalse("CouponIbor: equal-hash", CPN.equals(modified));
    modified = new CouponFixedAccruedCompounding(USDLIBOR1M.getCurrency(), PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXED_RATE + .1);
    assertFalse("CouponIbor: equal-hash", CPN.equals(modified));
  }

}
