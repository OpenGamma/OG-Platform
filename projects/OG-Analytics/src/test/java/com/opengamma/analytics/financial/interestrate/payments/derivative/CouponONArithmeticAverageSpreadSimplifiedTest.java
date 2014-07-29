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
public class CouponONArithmeticAverageSpreadSimplifiedTest {

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2013, 4, 16);

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final IndexON FEDFUND = IndexONMaster.getInstance().getIndex("FED FUND");
  private static final IborIndex USDLIBOR3M = IndexIborMaster.getInstance().getIndex("USDLIBOR3M");

  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2013, 4, 18);
  private static final Period TENOR_3M = Period.ofMonths(3);
  private static final double NOTIONAL = 100000000; // 100m
  private static final double SPREAD = 0.0010; // 10 bps
  private static final int PAYMENT_LAG = 2;

  private static final ZonedDateTime ACCRUAL_END_DATE = ScheduleCalculator.getAdjustedDate(ACCRUAL_START_DATE, TENOR_3M, USDLIBOR3M, NYC);
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(ACCRUAL_END_DATE, -1 + FEDFUND.getPublicationLag() + PAYMENT_LAG, NYC);
  private static final double PAYMENT_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, PAYMENT_DATE);
  private static final double ACCRUAL_START_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, ACCRUAL_START_DATE);
  private static final double ACCRUAL_END_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, ACCRUAL_END_DATE);
  private static final double ACCRUAL_FACTOR = FEDFUND.getDayCount().getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);

  private static final CouponONArithmeticAverageDefinition FEDFUND_CPN_3M_2_DEF = CouponONArithmeticAverageDefinition.from(FEDFUND, ACCRUAL_START_DATE, ACCRUAL_END_DATE, NOTIONAL, PAYMENT_LAG, NYC);

  private static final CouponONArithmeticAverageSpreadSimplified CPN_AA_ON = CouponONArithmeticAverageSpreadSimplified.from(PAYMENT_TIME, FEDFUND_CPN_3M_2_DEF.getPaymentYearFraction(),
      NOTIONAL, FEDFUND, ACCRUAL_START_TIME, ACCRUAL_END_TIME, ACCRUAL_FACTOR, SPREAD);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullIndex() {
    CouponONArithmeticAverageSpreadSimplified.from(PAYMENT_TIME, FEDFUND_CPN_3M_2_DEF.getPaymentYearFraction(), NOTIONAL, null, ACCRUAL_START_TIME, ACCRUAL_END_TIME, ACCRUAL_FACTOR, SPREAD);
  }

  @Test
  public void testWithNotional() {
    assertNull(CPN_AA_ON.withNotional(NOTIONAL));
  }

  @Test
  public void getter() {
    assertEquals("CouponArithmeticAverageONSpread: getter", CPN_AA_ON.getIndex(), FEDFUND);
    assertEquals("CouponArithmeticAverageONSpread: getter", CPN_AA_ON.getPaymentTime(), PAYMENT_TIME);
    assertEquals("CouponArithmeticAverageONSpread: getter", CPN_AA_ON.getFixingPeriodStartTime(), ACCRUAL_START_TIME);
    assertEquals("CouponArithmeticAverageONSpread: getter", CPN_AA_ON.getFixingPeriodEndTime(), ACCRUAL_END_TIME);
    assertEquals("CouponArithmeticAverageONSpread: getter", CPN_AA_ON.getSpread(), SPREAD);
    assertEquals("CouponArithmeticAverageONSpread: getter", CPN_AA_ON.getSpreadAmount(), SPREAD * CPN_AA_ON.getPaymentYearFraction() * NOTIONAL);
  }

  @Test
  public void equalHash() {
    assertEquals("CouponArithmeticAverageONSpreadSimplified: equal-hash", CPN_AA_ON, CPN_AA_ON);
    final CouponONArithmeticAverageSpreadSimplified other = CouponONArithmeticAverageSpreadSimplified.from(PAYMENT_TIME, FEDFUND_CPN_3M_2_DEF.getPaymentYearFraction(),
        NOTIONAL, FEDFUND, ACCRUAL_START_TIME, ACCRUAL_END_TIME, ACCRUAL_FACTOR, SPREAD);
    assertEquals("CouponArithmeticAverageONSpreadSimplified: equal-hash", CPN_AA_ON, other);
    assertEquals("CouponArithmeticAverageONSpreadSimplified: equal-hash", CPN_AA_ON.hashCode(), other.hashCode());
    assertFalse("CouponArithmeticAverageONSpreadSimplified: equal-hash", CPN_AA_ON.equals(null));
    CouponONArithmeticAverageSpreadSimplified modified;
    modified = CouponONArithmeticAverageSpreadSimplified.from(PAYMENT_TIME + 0.1, FEDFUND_CPN_3M_2_DEF.getPaymentYearFraction(),
        NOTIONAL, FEDFUND, ACCRUAL_START_TIME, ACCRUAL_END_TIME, ACCRUAL_FACTOR, SPREAD);
    assertFalse("CouponArithmeticAverageONSpreadSimplified: equal-hash", CPN_AA_ON.equals(modified));
    modified = CouponONArithmeticAverageSpreadSimplified.from(PAYMENT_TIME, FEDFUND_CPN_3M_2_DEF.getPaymentYearFraction() + 0.1, NOTIONAL, FEDFUND, ACCRUAL_START_TIME, ACCRUAL_END_TIME,
        ACCRUAL_FACTOR, SPREAD);
    assertFalse("CouponArithmeticAverageONSpreadSimplified: equal-hash", CPN_AA_ON.equals(modified));
    modified = CouponONArithmeticAverageSpreadSimplified.from(PAYMENT_TIME, FEDFUND_CPN_3M_2_DEF.getPaymentYearFraction(), NOTIONAL + 0.1, FEDFUND, ACCRUAL_START_TIME, ACCRUAL_END_TIME,
        ACCRUAL_FACTOR, SPREAD);
    assertFalse("CouponArithmeticAverageONSpreadSimplified: equal-hash", CPN_AA_ON.equals(modified));
    final IndexON modifiedIndex = IndexONMaster.getInstance().getIndex("EONIA");
    modified = CouponONArithmeticAverageSpreadSimplified.from(PAYMENT_TIME, FEDFUND_CPN_3M_2_DEF.getPaymentYearFraction(), NOTIONAL, modifiedIndex, ACCRUAL_START_TIME, ACCRUAL_END_TIME,
        ACCRUAL_FACTOR, SPREAD);
    assertFalse("CouponArithmeticAverageONSpreadSimplified: equal-hash", CPN_AA_ON.equals(modified));
    modified = CouponONArithmeticAverageSpreadSimplified.from(PAYMENT_TIME, FEDFUND_CPN_3M_2_DEF.getPaymentYearFraction(), NOTIONAL, FEDFUND, ACCRUAL_START_TIME + 0.1, ACCRUAL_END_TIME,
        ACCRUAL_FACTOR, SPREAD);
    assertFalse("CouponArithmeticAverageONSpreadSimplified: equal-hash", CPN_AA_ON.equals(modified));
    modified = CouponONArithmeticAverageSpreadSimplified.from(PAYMENT_TIME, FEDFUND_CPN_3M_2_DEF.getPaymentYearFraction(), NOTIONAL, FEDFUND, ACCRUAL_START_TIME, ACCRUAL_END_TIME + 0.1,
        ACCRUAL_FACTOR, SPREAD + 0.1);
    assertFalse("CouponArithmeticAverageONSpreadSimplified: equal-hash", CPN_AA_ON.equals(modified));
  }

}
