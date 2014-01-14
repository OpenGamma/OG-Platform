/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexONMaster;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Class describing a Fed Fund swap-like floating coupon (arithmetic average on overnight rates).
 */
@Test(groups = TestGroup.UNIT)
public class CouponONArithmeticAverageTest {

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2013, 4, 16);

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final IndexON FEDFUND = IndexONMaster.getInstance().getIndex("FED FUND");
  private static final IborIndex USDLIBOR3M = IndexIborMaster.getInstance().getIndex("USDLIBOR3M");

  private static final ZonedDateTime EFFECTIVE_DATE = DateUtils.getUTCDate(2013, 4, 18);
  private static final Period TENOR_3M = Period.ofMonths(3);
  private static final double NOTIONAL = 100000000; // 100m
  private static final int PAYMENT_LAG = 2;

  private static final ZonedDateTime ACCRUAL_END_DATE = ScheduleCalculator.getAdjustedDate(EFFECTIVE_DATE, TENOR_3M, USDLIBOR3M, NYC);
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(ACCRUAL_END_DATE, -1 + FEDFUND.getPublicationLag() + PAYMENT_LAG, NYC);
  private static final double PAYMENT_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, PAYMENT_DATE);

  private static final CouponONArithmeticAverageDefinition FEDFUND_CPN_3M_2_DEF = CouponONArithmeticAverageDefinition.from(FEDFUND, EFFECTIVE_DATE, ACCRUAL_END_DATE, NOTIONAL, PAYMENT_LAG, NYC);
  private static final double[] FIXING_START_TIMES = TimeCalculator.getTimeBetween(REFERENCE_DATE, FEDFUND_CPN_3M_2_DEF.getFixingPeriodStartDates());
  private static final double[] FIXING_END_TIMES = TimeCalculator.getTimeBetween(REFERENCE_DATE, FEDFUND_CPN_3M_2_DEF.getFixingPeriodEndDates());

  private static final double ACCRUED_RATE = 0.0001;
  private static final CouponONArithmeticAverage CPN_AA_ON = CouponONArithmeticAverage.from(PAYMENT_TIME, FEDFUND_CPN_3M_2_DEF.getPaymentYearFraction(),
      NOTIONAL, FEDFUND, FIXING_START_TIMES, FIXING_END_TIMES, FEDFUND_CPN_3M_2_DEF.getFixingPeriodAccrualFactors(), ACCRUED_RATE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullIndex() {
    CouponONArithmeticAverage.from(PAYMENT_TIME, FEDFUND_CPN_3M_2_DEF.getPaymentYearFraction(), NOTIONAL, null, FIXING_START_TIMES, FIXING_END_TIMES,
        FEDFUND_CPN_3M_2_DEF.getFixingPeriodAccrualFactors(), ACCRUED_RATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullFixStartTimes() {
    CouponONArithmeticAverage.from(PAYMENT_TIME, FEDFUND_CPN_3M_2_DEF.getPaymentYearFraction(), NOTIONAL, FEDFUND, null, FIXING_END_TIMES, FEDFUND_CPN_3M_2_DEF.getFixingPeriodAccrualFactors(),
        ACCRUED_RATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullFixEndTimes() {
    CouponONArithmeticAverage.from(PAYMENT_TIME, FEDFUND_CPN_3M_2_DEF.getPaymentYearFraction(), NOTIONAL, FEDFUND, FIXING_START_TIMES, null, FEDFUND_CPN_3M_2_DEF.getFixingPeriodAccrualFactors(),
        ACCRUED_RATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullFixAF() {
    CouponONArithmeticAverage.from(PAYMENT_TIME, FEDFUND_CPN_3M_2_DEF.getPaymentYearFraction(), NOTIONAL, FEDFUND, FIXING_START_TIMES, FIXING_END_TIMES, null, ACCRUED_RATE);
  }

  @Test
  public void getter() {
    assertEquals("CouponArithmeticAverageON: getter", CPN_AA_ON.getFixingPeriodStartTimes(), FIXING_START_TIMES);
    assertEquals("CouponArithmeticAverageON: getter", CPN_AA_ON.getFixingPeriodEndTimes(), FIXING_END_TIMES);
    assertEquals("CouponArithmeticAverageON: getter", CPN_AA_ON.getFixingPeriodAccrualFactors(), FEDFUND_CPN_3M_2_DEF.getFixingPeriodAccrualFactors());
    assertEquals("CouponArithmeticAverageON: getter", CPN_AA_ON.getRateAccrued(), ACCRUED_RATE);
  }

  @Test
  public void testWithNotional() {
    assertNull(CPN_AA_ON.withNotional(NOTIONAL));
  }

  @Test
  public void equalHash() {
    assertEquals("CouponArithmeticAverageON: equal-hash", CPN_AA_ON, CPN_AA_ON);
    final CouponONArithmeticAverage other = CouponONArithmeticAverage.from(PAYMENT_TIME, FEDFUND_CPN_3M_2_DEF.getPaymentYearFraction(),
        NOTIONAL, FEDFUND, FIXING_START_TIMES, FIXING_END_TIMES, FEDFUND_CPN_3M_2_DEF.getFixingPeriodAccrualFactors(), ACCRUED_RATE);
    assertEquals("CouponArithmeticAverageON: equal-hash", CPN_AA_ON, other);
    assertEquals("CouponArithmeticAverageON: equal-hash", CPN_AA_ON.hashCode(), other.hashCode());
    assertFalse("CouponArithmeticAverageON: equal-hash", CPN_AA_ON.equals(null));
    CouponONArithmeticAverage modified;
    modified = CouponONArithmeticAverage.from(PAYMENT_TIME + 0.1, FEDFUND_CPN_3M_2_DEF.getPaymentYearFraction(), NOTIONAL, FEDFUND, FIXING_START_TIMES, FIXING_END_TIMES,
        FEDFUND_CPN_3M_2_DEF.getFixingPeriodAccrualFactors(),
        ACCRUED_RATE);
    assertFalse("CouponArithmeticAverageON: equal-hash", CPN_AA_ON.equals(modified));
    modified = CouponONArithmeticAverage.from(PAYMENT_TIME, FEDFUND_CPN_3M_2_DEF.getPaymentYearFraction() + 0.1, NOTIONAL, FEDFUND, FIXING_START_TIMES, FIXING_END_TIMES,
        FEDFUND_CPN_3M_2_DEF.getFixingPeriodAccrualFactors(),
        ACCRUED_RATE);
    assertFalse("CouponArithmeticAverageON: equal-hash", CPN_AA_ON.equals(modified));
    modified = CouponONArithmeticAverage.from(PAYMENT_TIME, FEDFUND_CPN_3M_2_DEF.getPaymentYearFraction(), NOTIONAL + 10, FEDFUND, FIXING_START_TIMES, FIXING_END_TIMES,
        FEDFUND_CPN_3M_2_DEF.getFixingPeriodAccrualFactors(),
        ACCRUED_RATE);
    assertFalse("CouponArithmeticAverageON: equal-hash", CPN_AA_ON.equals(modified));
    final IndexON modifiedIndex = IndexONMaster.getInstance().getIndex("EONIA");
    modified = CouponONArithmeticAverage.from(PAYMENT_TIME, FEDFUND_CPN_3M_2_DEF.getPaymentYearFraction(), NOTIONAL, modifiedIndex, FIXING_START_TIMES, FIXING_END_TIMES,
        FEDFUND_CPN_3M_2_DEF.getFixingPeriodAccrualFactors(),
        ACCRUED_RATE);
    assertFalse("CouponArithmeticAverageON: equal-hash", CPN_AA_ON.equals(modified));
    modified = CouponONArithmeticAverage.from(PAYMENT_TIME, FEDFUND_CPN_3M_2_DEF.getPaymentYearFraction(), NOTIONAL, FEDFUND, FIXING_START_TIMES, FIXING_END_TIMES,
        FEDFUND_CPN_3M_2_DEF.getFixingPeriodAccrualFactors(),
        ACCRUED_RATE + 0.1);
    assertFalse("CouponArithmeticAverageON: equal-hash", CPN_AA_ON.equals(modified));
  }

}
