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
public class CouponONArithmeticAverageSpreadTest {

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2013, 4, 16);

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final IndexON FEDFUND = IndexONMaster.getInstance().getIndex("FED FUND");
  private static final IborIndex USDLIBOR3M = IndexIborMaster.getInstance().getIndex("USDLIBOR3M");

  private static final ZonedDateTime EFFECTIVE_DATE = DateUtils.getUTCDate(2013, 4, 18);
  private static final Period TENOR_3M = Period.ofMonths(3);
  private static final double NOTIONAL = 100000000; // 100m
  private static final double SPREAD = 0.0010; // 10 bps
  private static final int PAYMENT_LAG = 2;

  private static final ZonedDateTime ACCRUAL_END_DATE = ScheduleCalculator.getAdjustedDate(EFFECTIVE_DATE, TENOR_3M, USDLIBOR3M, NYC);
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(ACCRUAL_END_DATE, -1 + FEDFUND.getPublicationLag() + PAYMENT_LAG, NYC);
  private static final double PAYMENT_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, PAYMENT_DATE);

  private static final CouponONArithmeticAverageDefinition FEDFUND_CPN_3M_2_DEF = CouponONArithmeticAverageDefinition.from(FEDFUND, EFFECTIVE_DATE, ACCRUAL_END_DATE, NOTIONAL, PAYMENT_LAG, NYC);
  private static final double[] FIXING_TIMES = TimeCalculator.getTimeBetween(REFERENCE_DATE, FEDFUND_CPN_3M_2_DEF.getFixingPeriodDates());

  private static final double ACCRUED_RATE = 0.0001;
  private static final CouponONArithmeticAverageSpread CPN_AA_ON = CouponONArithmeticAverageSpread.from(PAYMENT_TIME, FEDFUND_CPN_3M_2_DEF.getPaymentYearFraction(),
      NOTIONAL, FEDFUND, FIXING_TIMES, FEDFUND_CPN_3M_2_DEF.getFixingPeriodAccrualFactors(), ACCRUED_RATE, SPREAD);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullIndex() {
    CouponONArithmeticAverageSpread.from(PAYMENT_TIME, FEDFUND_CPN_3M_2_DEF.getPaymentYearFraction(), NOTIONAL, null, FIXING_TIMES, FEDFUND_CPN_3M_2_DEF.getFixingPeriodAccrualFactors(), ACCRUED_RATE,
        SPREAD);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullFixTimes() {
    CouponONArithmeticAverageSpread.from(PAYMENT_TIME, FEDFUND_CPN_3M_2_DEF.getPaymentYearFraction(), NOTIONAL, FEDFUND, null, FEDFUND_CPN_3M_2_DEF.getFixingPeriodAccrualFactors(), ACCRUED_RATE,
        SPREAD);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullFixAF() {
    CouponONArithmeticAverageSpread.from(PAYMENT_TIME, FEDFUND_CPN_3M_2_DEF.getPaymentYearFraction(), NOTIONAL, FEDFUND, FIXING_TIMES, null, ACCRUED_RATE, SPREAD);
  }

  @Test
  public void getter() {
    assertEquals("CouponArithmeticAverageONSpread: getter", CPN_AA_ON.getFixingPeriodTimes(), FIXING_TIMES);
    assertEquals("CouponArithmeticAverageONSpread: getter", CPN_AA_ON.getFixingPeriodAccrualFactors(), FEDFUND_CPN_3M_2_DEF.getFixingPeriodAccrualFactors());
    assertEquals("CouponArithmeticAverageONSpread: getter", CPN_AA_ON.getRateAccrued(), ACCRUED_RATE);
    assertEquals("CouponArithmeticAverageONSpread: getter", CPN_AA_ON.getSpread(), SPREAD);
    assertEquals("CouponArithmeticAverageONSpread: getter", CPN_AA_ON.getSpreadAmount(), SPREAD * CPN_AA_ON.getPaymentYearFraction() * NOTIONAL);
  }

  @Test
  public void testWithNotional() {
    assertNull(CPN_AA_ON.withNotional(NOTIONAL));
  }

  @Test
  public void equalHash() {
    assertEquals("CouponArithmeticAverageONSpread: equal-hash", CPN_AA_ON, CPN_AA_ON);
    final CouponONArithmeticAverageSpread other = CouponONArithmeticAverageSpread.from(PAYMENT_TIME, FEDFUND_CPN_3M_2_DEF.getPaymentYearFraction(),
        NOTIONAL, FEDFUND, FIXING_TIMES, FEDFUND_CPN_3M_2_DEF.getFixingPeriodAccrualFactors(), ACCRUED_RATE, SPREAD);
    assertEquals("CouponArithmeticAverageONSpread: equal-hash", CPN_AA_ON, other);
    assertEquals("CouponArithmeticAverageONSpread: equal-hash", CPN_AA_ON.hashCode(), other.hashCode());
    assertFalse("CouponArithmeticAverageONSpread: equal-hash", CPN_AA_ON.equals(null));
    CouponONArithmeticAverageSpread modified;
    modified = CouponONArithmeticAverageSpread.from(PAYMENT_TIME + 0.1, FEDFUND_CPN_3M_2_DEF.getPaymentYearFraction(), NOTIONAL, FEDFUND, FIXING_TIMES,
        FEDFUND_CPN_3M_2_DEF.getFixingPeriodAccrualFactors(), ACCRUED_RATE, SPREAD);
    assertFalse("CouponArithmeticAverageONSpread: equal-hash", CPN_AA_ON.equals(modified));
    modified = CouponONArithmeticAverageSpread.from(PAYMENT_TIME, FEDFUND_CPN_3M_2_DEF.getPaymentYearFraction() + 0.1, NOTIONAL, FEDFUND, FIXING_TIMES,
        FEDFUND_CPN_3M_2_DEF.getFixingPeriodAccrualFactors(), ACCRUED_RATE, SPREAD);
    assertFalse("CouponArithmeticAverageONSpread: equal-hash", CPN_AA_ON.equals(modified));
    modified = CouponONArithmeticAverageSpread.from(PAYMENT_TIME, FEDFUND_CPN_3M_2_DEF.getPaymentYearFraction(), NOTIONAL + 10, FEDFUND, FIXING_TIMES,
        FEDFUND_CPN_3M_2_DEF.getFixingPeriodAccrualFactors(), ACCRUED_RATE, SPREAD);
    assertFalse("CouponArithmeticAverageONSpread: equal-hash", CPN_AA_ON.equals(modified));
    final IndexON modifiedIndex = IndexONMaster.getInstance().getIndex("EONIA");
    modified = CouponONArithmeticAverageSpread.from(PAYMENT_TIME, FEDFUND_CPN_3M_2_DEF.getPaymentYearFraction(), NOTIONAL, modifiedIndex, FIXING_TIMES,
        FEDFUND_CPN_3M_2_DEF.getFixingPeriodAccrualFactors(), ACCRUED_RATE, SPREAD);
    assertFalse("CouponArithmeticAverageONSpread: equal-hash", CPN_AA_ON.equals(modified));
    modified = CouponONArithmeticAverageSpread.from(PAYMENT_TIME, FEDFUND_CPN_3M_2_DEF.getPaymentYearFraction(), NOTIONAL, FEDFUND, FIXING_TIMES, FEDFUND_CPN_3M_2_DEF.getFixingPeriodAccrualFactors(),
        ACCRUED_RATE + 0.1, SPREAD);
    assertFalse("CouponArithmeticAverageONSpread: equal-hash", CPN_AA_ON.equals(modified));
    modified = CouponONArithmeticAverageSpread.from(PAYMENT_TIME, FEDFUND_CPN_3M_2_DEF.getPaymentYearFraction(), NOTIONAL, FEDFUND, FIXING_TIMES, FEDFUND_CPN_3M_2_DEF.getFixingPeriodAccrualFactors(),
        ACCRUED_RATE, SPREAD + 0.1);
    assertFalse("CouponArithmeticAverageONSpread: equal-hash", CPN_AA_ON.equals(modified));
  }

}
