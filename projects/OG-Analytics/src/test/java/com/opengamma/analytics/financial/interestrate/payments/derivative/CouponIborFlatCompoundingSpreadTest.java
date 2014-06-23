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
public class CouponIborFlatCompoundingSpreadTest {

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
  private static final double SPREAD = 0.02;

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
  private static double[][] FIX_ACC_FACTORS = new double[NUM_PRDS][NUM_OBS];

  static {
    for (int i = 0; i < NUM_PRDS; ++i) {
      FIXING_TIMES[i] = TimeCalculator.getTimeBetween(REFERENCE_DATE, FIXING_DATES[i]);
      FIXING_PERIOD_START_TIMES[i] = TimeCalculator.getTimeBetween(REFERENCE_DATE, EXP_START_DATES[i]);
      FIXING_PERIOD_END_TIMES[i] = TimeCalculator.getTimeBetween(REFERENCE_DATE, EXP_END_DATES[i]);
    }
    for (int i = 0; i < NUM_OBS; ++i) {
      for (int j = 0; j < NUM_PRDS; ++j) {
        FIX_ACC_FACTORS[j][i] = INDEX.getDayCount().getDayCountFraction(EXP_START_DATES[j][i], EXP_END_DATES[j][i], CALENDAR);
      }
    }
  }

  private static final CouponIborFlatCompoundingSpread DFN1 = new CouponIborFlatCompoundingSpread(CUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, ACCRUAL_FACTORS, INDEX, FIXING_TIMES, WEIGHTS,
      FIXING_PERIOD_START_TIMES, FIXING_PERIOD_END_TIMES, FIX_ACC_FACTORS, SPREAD);
  private static final CouponIborFlatCompoundingSpread DFN2 = DFN1.withNotional(NOTIONAL);

  /**
   * 
   */
  @Test
  public void consistencyTest() {
    final CouponIborFlatCompoundingSpread dfn1WithDouble = DFN1.withNotional(NOTIONAL * 2);

    assertEquals(DFN1.getSpread(), DFN2.getSpread());
    assertEquals(DFN1.getSpread(), dfn1WithDouble.getSpread());

    assertTrue(DFN1.equals(DFN2));
    assertTrue(DFN1.hashCode() == DFN2.hashCode());

    assertFalse(DFN1.hashCode() == dfn1WithDouble.hashCode());
    assertFalse(DFN1.equals(dfn1WithDouble));

  }
}
