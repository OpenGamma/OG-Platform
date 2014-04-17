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
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test of fixed compounding coupon class.
 */
@Test(groups = TestGroup.UNIT)
public class CouponFixedCompoundingTest {

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final IndexIborMaster MASTER_IBOR = IndexIborMaster.getInstance();
  private static final IborIndex USDLIBOR1M = MASTER_IBOR.getIndex("USDLIBOR1M");

  private static final Period TENOR_3M = Period.ofMonths(3);
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2012, 8, 24);
  private static final double NOTIONAL = 123454321;

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

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 8, 17);

  private static final double[] ACCRUAL_END_TIMES = TimeCalculator.getTimeBetween(REFERENCE_DATE, ACCRUAL_END_DATES);
  private static final double PAYMENT_TIME = ACCRUAL_END_TIMES[NB_SUB_PERIOD - 1];
  private static final double FIXED_RATE = .02;

  private static final CouponFixedCompounding CPN = new CouponFixedCompounding(USDLIBOR1M.getCurrency(), PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL,
      PAYMENT_ACCRUAL_FACTORS, FIXED_RATE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency() {
    new CouponFixedCompounding(null, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL,
        PAYMENT_ACCRUAL_FACTORS, FIXED_RATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativePaymentTime() {
    new CouponFixedCompounding(USDLIBOR1M.getCurrency(), -1, PAYMENT_ACCRUAL_FACTOR, NOTIONAL,
        PAYMENT_ACCRUAL_FACTORS, FIXED_RATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativePaymentAccrualFactor() {
    new CouponFixedCompounding(USDLIBOR1M.getCurrency(), PAYMENT_TIME, -1, NOTIONAL,
        PAYMENT_ACCRUAL_FACTORS, FIXED_RATE);
  }

  @Test
  /**
   * Tests the getters.
   */
  public void getter() {
    assertEquals("CouponFixedCompounding: getter", USDLIBOR1M.getCurrency(), CPN.getCurrency());
    assertEquals("CouponFixedCompounding: getter", PAYMENT_ACCRUAL_FACTOR, CPN.getPaymentYearFraction());
    assertEquals("CouponFixedCompounding: getter", NOTIONAL, CPN.getNotional());
    assertEquals("CouponFixedCompounding: getter", PAYMENT_ACCRUAL_FACTORS, CPN.getPaymentAccrualFactors());
    assertEquals("CouponFixedCompounding: getter", PAYMENT_TIME, CPN.getPaymentTime());
    assertEquals("CouponFixedCompounding: getter", PAYMENT_ACCRUAL_FACTOR, CPN.getPaymentYearFraction());
    assertEquals("CouponFixedCompounding: getter", FIXED_RATE, CPN.getFixedRate());
  }

  @Test
  public void testWithNotional() {
    final double notional = NOTIONAL + 10000;
    final CouponFixedCompounding expected = new CouponFixedCompounding(USDLIBOR1M.getCurrency(), PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, notional,
        PAYMENT_ACCRUAL_FACTORS, FIXED_RATE);
    assertEquals(expected, CPN.withNotional(notional));
  }

  @Test
  /**
   * Tests the equal and hash code.
   */
  public void testEqualHash() {
    assertEquals("CouponIbor: equal-hash", CPN, CPN);
    final CouponFixedCompounding other = new CouponFixedCompounding(USDLIBOR1M.getCurrency(), PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL,
        PAYMENT_ACCRUAL_FACTORS, FIXED_RATE);
    assertEquals("CouponIbor: equal-hash", other, CPN);
    assertEquals("CouponIbor: equal-hash", other.hashCode(), CPN.hashCode());
    CouponFixedCompounding modified;
    modified = new CouponFixedCompounding(USDLIBOR1M.getCurrency(), PAYMENT_TIME + .1, PAYMENT_ACCRUAL_FACTOR, NOTIONAL,
        PAYMENT_ACCRUAL_FACTORS, FIXED_RATE);
    assertFalse("CouponIbor: equal-hash", CPN.equals(modified));
    modified = new CouponFixedCompounding(USDLIBOR1M.getCurrency(), PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR + .1, NOTIONAL,
        PAYMENT_ACCRUAL_FACTORS, FIXED_RATE);
    assertFalse("CouponIbor: equal-hash", CPN.equals(modified));
    modified = new CouponFixedCompounding(USDLIBOR1M.getCurrency(), PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL + .1,
        PAYMENT_ACCRUAL_FACTORS, FIXED_RATE);
    assertFalse("CouponIbor: equal-hash", CPN.equals(modified));
    modified = new CouponFixedCompounding(USDLIBOR1M.getCurrency(), PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL,
        new double[1], FIXED_RATE);
    assertFalse("CouponIbor: equal-hash", CPN.equals(modified));
    modified = new CouponFixedCompounding(USDLIBOR1M.getCurrency(), PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL,
        PAYMENT_ACCRUAL_FACTORS, FIXED_RATE + .1);
    assertFalse("CouponIbor: equal-hash", CPN.equals(modified));
  }

}
