/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
public class CouponIborAverageSinglePeriodTest {

  private static final Period TENOR = Period.ofMonths(1);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.EUR;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM, "Index");

  private static final int NUM_OBS = 6;

  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 1, 6);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 7, 4);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 7, 6);
  // The above dates are not standard but selected for insure correct testing.
  private static final ZonedDateTime[] FIXING_DATES = new ZonedDateTime[NUM_OBS];
  private static final double[] WEIGHTS = new double[NUM_OBS];
  static {
    for (int i = 0; i < NUM_OBS; ++i) {
      FIXING_DATES[i] = DateUtils.getUTCDate(2011, i + 1, 3);
      WEIGHTS[i] = 2. * (NUM_OBS - i) / NUM_OBS / (NUM_OBS + 1.);
    }
  }

  private static final DayCount DAY_COUNT_PAYMENT = DayCounts.ACT_365;
  private static final double ACCRUAL_FACTOR = DAY_COUNT_PAYMENT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 1000000;

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 12, 27);

  private static ZonedDateTime[] EXP_START_DATES = new ZonedDateTime[NUM_OBS];
  private static ZonedDateTime[] EXP_END_DATES = new ZonedDateTime[NUM_OBS];
  static {
    for (int i = 0; i < NUM_OBS; ++i) {
      EXP_START_DATES[i] = ScheduleCalculator.getAdjustedDate(FIXING_DATES[i], INDEX.getSpotLag(), CALENDAR);
      EXP_END_DATES[i] = ScheduleCalculator.getAdjustedDate(EXP_START_DATES[i], INDEX.getTenor(), INDEX.getBusinessDayConvention(), CALENDAR, INDEX.isEndOfMonth());
    }
  }

  private static final double PAYMENT_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, PAYMENT_DATE);
  private static final double[] FIXING_TIMES = TimeCalculator.getTimeBetween(REFERENCE_DATE, FIXING_DATES);
  private static final double[] FIXING_PERIOD_START_TIMES = TimeCalculator.getTimeBetween(REFERENCE_DATE, EXP_START_DATES);
  private static final double[] FIXING_PERIOD_END_TIMES = TimeCalculator.getTimeBetween(REFERENCE_DATE, EXP_END_DATES);
  private static final double[] FIX_ACC_FACTORS = new double[NUM_OBS];
  static {
    for (int i = 0; i < NUM_OBS; ++i) {
      FIX_ACC_FACTORS[i] = INDEX.getDayCount().getDayCountFraction(EXP_START_DATES[i], EXP_END_DATES[i]);
    }
  }

  private static final CouponIborAverageSinglePeriod DFN1 = new CouponIborAverageSinglePeriod(CUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, INDEX, FIXING_TIMES, WEIGHTS, FIXING_PERIOD_START_TIMES,
      FIXING_PERIOD_END_TIMES, FIX_ACC_FACTORS);
  private static final CouponIborAverageSinglePeriod DFN2 = DFN1.withNotional(NOTIONAL);

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test
  public void exceptionTest() {
    try {
      new CouponIborAverageSinglePeriod(Currency.GBP, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, INDEX, FIXING_TIMES, WEIGHTS, FIXING_PERIOD_START_TIMES,
          FIXING_PERIOD_END_TIMES, FIX_ACC_FACTORS);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("index currency different from payment currency", e.getMessage());
    }

    final double[] shortWeight = Arrays.copyOf(WEIGHTS, NUM_OBS - 1);
    try {
      new CouponIborAverageSinglePeriod(CUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, INDEX, FIXING_TIMES, shortWeight, FIXING_PERIOD_START_TIMES,
          FIXING_PERIOD_END_TIMES, FIX_ACC_FACTORS);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("weight length different from fixingTime length", e.getMessage());
    }

    final double[] shortStartTimes = Arrays.copyOf(FIXING_PERIOD_START_TIMES, NUM_OBS - 1);
    try {
      new CouponIborAverageSinglePeriod(CUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, INDEX, FIXING_TIMES, WEIGHTS, shortStartTimes,
          FIXING_PERIOD_END_TIMES, FIX_ACC_FACTORS);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("fixingPeriodStartTime length different from fixingTime length", e.getMessage());
    }

    final double[] shortEndTimes = Arrays.copyOf(FIXING_PERIOD_END_TIMES, NUM_OBS - 1);
    try {
      new CouponIborAverageSinglePeriod(CUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, INDEX, FIXING_TIMES, WEIGHTS, FIXING_PERIOD_START_TIMES,
          shortEndTimes, FIX_ACC_FACTORS);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("fixingPeriodEndTime length different from fixingTime length", e.getMessage());
    }

    final double[] shortfcc = Arrays.copyOf(FIX_ACC_FACTORS, NUM_OBS - 1);
    try {
      new CouponIborAverageSinglePeriod(CUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, INDEX, FIXING_TIMES, WEIGHTS, FIXING_PERIOD_START_TIMES,
          FIXING_PERIOD_END_TIMES, shortfcc);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("getFixingPeriodAccrualFactor length different from fixingTime length", e.getMessage());
    }
  }

  /**
   * 
   */
  @Test
  public void consistencyTest() {
    final CouponIborAverageSinglePeriod dfn1WithDouble = DFN1.withNotional(NOTIONAL * 2);

    assertEquals(DFN1.getIndex(), DFN2.getIndex());
    assertEquals(DFN1.getIndex(), dfn1WithDouble.getIndex());

    for (int i = 0; i < NUM_OBS; ++i) {
      assertEquals(DFN1.getWeight()[i], DFN2.getWeight()[i]);
      assertEquals(DFN1.getFixingTime()[i], DFN2.getFixingTime()[i]);
      assertEquals(DFN1.getFixingPeriodStartTime()[i], DFN2.getFixingPeriodStartTime()[i]);
      assertEquals(DFN1.getFixingPeriodEndTime()[i], DFN2.getFixingPeriodEndTime()[i]);
      assertEquals(DFN1.getFixingPeriodAccrualFactor()[i], DFN2.getFixingPeriodAccrualFactor()[i]);

      assertEquals(DFN1.getWeight()[i], dfn1WithDouble.getWeight()[i]);
      assertEquals(DFN1.getFixingTime()[i], dfn1WithDouble.getFixingTime()[i]);
      assertEquals(DFN1.getFixingPeriodStartTime()[i], dfn1WithDouble.getFixingPeriodStartTime()[i]);
      assertEquals(DFN1.getFixingPeriodEndTime()[i], dfn1WithDouble.getFixingPeriodEndTime()[i]);
      assertEquals(DFN1.getFixingPeriodAccrualFactor()[i], dfn1WithDouble.getFixingPeriodAccrualFactor()[i]);
    }

    assertTrue(DFN1.equals(DFN2));
    assertTrue(DFN1.hashCode() == DFN2.hashCode());

    assertFalse(DFN1.hashCode() == dfn1WithDouble.hashCode());
    assertFalse(DFN1.equals(dfn1WithDouble));
  }
}
