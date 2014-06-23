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
public class CouponIborAverageCompoundingTest {

  private static final Period TENOR = Period.ofMonths(1);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.EUR;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM, "Index");

  private static final int NUM_PRDS = 6;
  private static final int NUM_OBS = 5;

  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 1, 6);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 7, 4);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 7, 6);
  // The above dates are not standard but selected for insure correct testing.
  private static final ZonedDateTime[][] FIXING_DATES = new ZonedDateTime[NUM_PRDS][NUM_OBS];
  private static final double[][] WEIGHTS = new double[NUM_PRDS][NUM_OBS];
  static {
    for (int i = 0; i < NUM_OBS; ++i) {
      for (int j = 0; j < NUM_PRDS; ++j) {
        FIXING_DATES[j][i] = DateUtils.getUTCDate(2011, j + 1, 3 + 6 * i);
        WEIGHTS[j][i] = 2. * (NUM_OBS - i) / NUM_OBS / (NUM_OBS + 1.);
      }
    }
  }

  private static final DayCount DAY_COUNT_PAYMENT = DayCounts.ACT_365;
  private static final double ACCRUAL_FACTOR = DAY_COUNT_PAYMENT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double[] ACCRUAL_FACTORS = new double[NUM_PRDS];
  static {
    Arrays.fill(ACCRUAL_FACTORS, ACCRUAL_FACTOR / NUM_PRDS);
  }
  private static final double NOTIONAL = 1000000;

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 12, 27);

  private static ZonedDateTime[][] EXP_START_DATES = new ZonedDateTime[NUM_PRDS][NUM_OBS];
  private static ZonedDateTime[][] EXP_END_DATES = new ZonedDateTime[NUM_PRDS][NUM_OBS];
  static {
    for (int i = 0; i < NUM_OBS; ++i) {
      for (int j = 0; j < NUM_PRDS; ++j) {
        EXP_START_DATES[j][i] = ScheduleCalculator.getAdjustedDate(FIXING_DATES[j][i], INDEX.getSpotLag(), CALENDAR);
        EXP_END_DATES[j][i] = ScheduleCalculator.getAdjustedDate(EXP_START_DATES[j][i], INDEX.getTenor(), INDEX.getBusinessDayConvention(), CALENDAR, INDEX.isEndOfMonth());
      }
    }
  }

  private static final double PAYMENT_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, PAYMENT_DATE);
  private static final double[][] FIXING_TIMES = new double[NUM_PRDS][NUM_OBS];
  private static final double[][] FIXING_PERIOD_START_TIMES = new double[NUM_PRDS][NUM_OBS];
  private static final double[][] FIXING_PERIOD_END_TIMES = new double[NUM_PRDS][NUM_OBS];

  static {
    for (int i = 0; i < NUM_PRDS; ++i) {
      FIXING_TIMES[i] = TimeCalculator.getTimeBetween(REFERENCE_DATE, FIXING_DATES[i]);
      FIXING_PERIOD_START_TIMES[i] = TimeCalculator.getTimeBetween(REFERENCE_DATE, EXP_START_DATES[i]);
      FIXING_PERIOD_END_TIMES[i] = TimeCalculator.getTimeBetween(REFERENCE_DATE, EXP_END_DATES[i]);
    }
  }

  private static final CouponIborAverageCompounding DFN1 = new CouponIborAverageCompounding(CUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, ACCRUAL_FACTORS, INDEX, FIXING_TIMES, WEIGHTS,
      FIXING_PERIOD_START_TIMES, FIXING_PERIOD_END_TIMES);
  private static final CouponIborAverageCompounding DFN2 = DFN1.withNotional(NOTIONAL);

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test
  public void exceptionTest() {
    try {
      new CouponIborAverageCompounding(Currency.GBP, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, ACCRUAL_FACTORS, INDEX, FIXING_TIMES, WEIGHTS,
          FIXING_PERIOD_START_TIMES, FIXING_PERIOD_END_TIMES);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("index currency different from payment currency", e.getMessage());
    }

    double[][] shortWeight = Arrays.copyOf(WEIGHTS, NUM_PRDS - 1);
    try {
      new CouponIborAverageCompounding(CUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, ACCRUAL_FACTORS, INDEX, FIXING_TIMES, shortWeight,
          FIXING_PERIOD_START_TIMES, FIXING_PERIOD_END_TIMES);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("weight length different from fixingTime length", e.getMessage());
    }

    double[][] smallWeight = new double[NUM_PRDS][];
    for (int i = 0; i < NUM_PRDS; ++i) {
      smallWeight[i] = Arrays.copyOf(WEIGHTS[i], NUM_OBS - 1);
    }
    try {
      new CouponIborAverageCompounding(CUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, ACCRUAL_FACTORS, INDEX, FIXING_TIMES, smallWeight,
          FIXING_PERIOD_START_TIMES, FIXING_PERIOD_END_TIMES);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("weight length different from fixingTime length", e.getMessage());
    }

    final double[][] shortStartDates = Arrays.copyOf(FIXING_PERIOD_START_TIMES, NUM_PRDS - 1);
    try {
      new CouponIborAverageCompounding(CUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, ACCRUAL_FACTORS, INDEX, FIXING_TIMES, WEIGHTS,
          shortStartDates, FIXING_PERIOD_END_TIMES);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("fixingPeriodStartDates length different from fixingTime length", e.getMessage());
    }

    final double[][] shortEndDates = Arrays.copyOf(FIXING_PERIOD_END_TIMES, NUM_PRDS - 1);
    try {
      new CouponIborAverageCompounding(CUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, ACCRUAL_FACTORS, INDEX, FIXING_TIMES, WEIGHTS,
          FIXING_PERIOD_START_TIMES, shortEndDates);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("fixingPeriodEndDates length different from fixingTime length", e.getMessage());
    }

    final double[][] smallStartDates = new double[NUM_PRDS][];
    final double[][] smallEndDates = new double[NUM_PRDS][];
    for (int i = 0; i < NUM_PRDS; ++i) {
      smallStartDates[i] = Arrays.copyOf(FIXING_PERIOD_START_TIMES[i], NUM_OBS - 1);
      smallEndDates[i] = Arrays.copyOf(FIXING_PERIOD_END_TIMES[i], NUM_OBS - 1);
    }
    try {
      new CouponIborAverageCompounding(CUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, ACCRUAL_FACTORS, INDEX, FIXING_TIMES, WEIGHTS,
          smallStartDates, FIXING_PERIOD_END_TIMES);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("fixingPeriodStartDates length different from fixingTime length", e.getMessage());
    }
    try {
      new CouponIborAverageCompounding(CUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, ACCRUAL_FACTORS, INDEX, FIXING_TIMES, WEIGHTS,
          FIXING_PERIOD_START_TIMES, smallEndDates);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("fixingPeriodEndDates length different from fixingTime length", e.getMessage());
    }

  }

  /**
   * 
   */
  @Test
  public void consistencyTest() {
    final CouponIborAverageCompounding dfn1WithDouble = DFN1.withNotional(NOTIONAL * 2);

    assertEquals(DFN1.getIndex(), DFN2.getIndex());
    assertEquals(DFN1.getIndex(), dfn1WithDouble.getIndex());

    for (int i = 0; i < NUM_OBS; ++i) {
      for (int j = 0; j < NUM_PRDS; ++j) {
        assertEquals(DFN1.getPaymentAccrualFactors()[j], DFN2.getPaymentAccrualFactors()[j]);
        assertEquals(DFN1.getFixingTime()[j][i], DFN2.getFixingTime()[j][i]);
        assertEquals(DFN1.getWeight()[j][i], DFN2.getWeight()[j][i]);
        assertEquals(DFN1.getFixingPeriodStartTime()[j][i], DFN2.getFixingPeriodStartTime()[j][i]);
        assertEquals(DFN1.getFixingPeriodEndTime()[j][i], DFN2.getFixingPeriodEndTime()[j][i]);

        assertEquals(DFN1.getPaymentAccrualFactors()[j], dfn1WithDouble.getPaymentAccrualFactors()[j]);
        assertEquals(DFN1.getFixingTime()[j][i], dfn1WithDouble.getFixingTime()[j][i]);
        assertEquals(DFN1.getWeight()[j][i], dfn1WithDouble.getWeight()[j][i]);
        assertEquals(DFN1.getFixingPeriodStartTime()[j][i], dfn1WithDouble.getFixingPeriodStartTime()[j][i]);
        assertEquals(DFN1.getFixingPeriodEndTime()[j][i], dfn1WithDouble.getFixingPeriodEndTime()[j][i]);
      }
    }

    assertTrue(DFN1.equals(DFN2));
    assertTrue(DFN1.hashCode() == DFN2.hashCode());

    assertFalse(DFN1.hashCode() == dfn1WithDouble.hashCode());
    assertFalse(DFN1.equals(dfn1WithDouble));
  }
}
