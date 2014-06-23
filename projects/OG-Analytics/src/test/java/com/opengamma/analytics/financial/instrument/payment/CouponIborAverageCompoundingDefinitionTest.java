/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
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
public class CouponIborAverageCompoundingDefinitionTest {

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

  private static final CouponIborAverageCompoundingDefinition DFN1 = new CouponIborAverageCompoundingDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
      ACCRUAL_FACTORS, INDEX, FIXING_DATES, WEIGHTS, CALENDAR);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 12, 27);

  private static ZonedDateTime[][] EXP_START_DATES = new ZonedDateTime[NUM_PRDS][NUM_OBS];
  private static ZonedDateTime[][] EXP_END_DATES = new ZonedDateTime[NUM_PRDS][NUM_OBS];
  private static double[][] FIX_ACC_FACTORS = new double[NUM_PRDS][NUM_OBS];
  static {
    for (int i = 0; i < NUM_OBS; ++i) {
      for (int j = 0; j < NUM_PRDS; ++j) {
        EXP_START_DATES[j][i] = ScheduleCalculator.getAdjustedDate(FIXING_DATES[j][i], INDEX.getSpotLag(), CALENDAR);
        EXP_END_DATES[j][i] = ScheduleCalculator.getAdjustedDate(EXP_START_DATES[j][i], INDEX.getTenor(), INDEX.getBusinessDayConvention(), CALENDAR, INDEX.isEndOfMonth());
        FIX_ACC_FACTORS[j][i] = INDEX.getDayCount().getDayCountFraction(EXP_START_DATES[j][i], EXP_END_DATES[j][i], CALENDAR);
      }
    }
  }

  private static final CouponIborAverageCompoundingDefinition DFN2 = new CouponIborAverageCompoundingDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
      ACCRUAL_FACTORS, INDEX, FIXING_DATES, WEIGHTS, EXP_START_DATES, EXP_END_DATES, FIX_ACC_FACTORS);

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test
  public void exceptionTest() {
    try {
      new CouponIborAverageCompoundingDefinition(Currency.GBP, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
          ACCRUAL_FACTORS, INDEX, FIXING_DATES, WEIGHTS, CALENDAR);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("index currency different from payment currency", e.getMessage());
    }
    try {
      new CouponIborAverageCompoundingDefinition(Currency.USD, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
          ACCRUAL_FACTORS, INDEX, FIXING_DATES, WEIGHTS, EXP_START_DATES, EXP_END_DATES, FIX_ACC_FACTORS);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("index currency different from payment currency", e.getMessage());
    }

    double[][] shortWeight = Arrays.copyOf(WEIGHTS, NUM_PRDS - 1);
    try {
      new CouponIborAverageCompoundingDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
          ACCRUAL_FACTORS, INDEX, FIXING_DATES, shortWeight, CALENDAR);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("weights length different from fixingDate length", e.getMessage());
    }
    try {
      new CouponIborAverageCompoundingDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
          ACCRUAL_FACTORS, INDEX, FIXING_DATES, shortWeight, EXP_START_DATES, EXP_END_DATES, FIX_ACC_FACTORS);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("weights length different from fixingDate length", e.getMessage());
    }

    double[][] smallWeight = new double[NUM_PRDS][];
    for (int i = 0; i < NUM_PRDS; ++i) {
      smallWeight[i] = Arrays.copyOf(WEIGHTS[i], NUM_OBS - 1);
    }
    try {
      new CouponIborAverageCompoundingDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
          ACCRUAL_FACTORS, INDEX, FIXING_DATES, smallWeight, CALENDAR);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("weights length different from fixingDate length", e.getMessage());
    }
    try {
      new CouponIborAverageCompoundingDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
          ACCRUAL_FACTORS, INDEX, FIXING_DATES, smallWeight, EXP_START_DATES, EXP_END_DATES, FIX_ACC_FACTORS);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("weights length different from fixingDate length", e.getMessage());
    }

    final ZonedDateTime[][] shortStartDates = Arrays.copyOf(EXP_START_DATES, NUM_PRDS - 1);
    try {
      new CouponIborAverageCompoundingDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
          ACCRUAL_FACTORS, INDEX, FIXING_DATES, WEIGHTS, shortStartDates, EXP_END_DATES, FIX_ACC_FACTORS);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("fixingPeriodStartDates length different from fixingDate length", e.getMessage());
    }

    final ZonedDateTime[][] shortEndDates = Arrays.copyOf(EXP_END_DATES, NUM_PRDS - 1);
    try {
      new CouponIborAverageCompoundingDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
          ACCRUAL_FACTORS, INDEX, FIXING_DATES, WEIGHTS, EXP_START_DATES, shortEndDates, FIX_ACC_FACTORS);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("fixingPeriodEndDates length different from fixingDate length", e.getMessage());
    }

    final double[][] shortAcc = Arrays.copyOf(FIX_ACC_FACTORS, NUM_PRDS - 1);
    try {
      new CouponIborAverageCompoundingDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
          ACCRUAL_FACTORS, INDEX, FIXING_DATES, WEIGHTS, EXP_START_DATES, EXP_END_DATES, shortAcc);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("fixingPeriodAccrualFactors length different from fixingDate length", e.getMessage());
    }

    final ZonedDateTime[][] smallStartDates = new ZonedDateTime[NUM_PRDS][];
    final ZonedDateTime[][] smallEndDates = new ZonedDateTime[NUM_PRDS][];
    final double[][] smallAcc = new double[NUM_PRDS][];
    for (int i = 0; i < NUM_PRDS; ++i) {
      smallStartDates[i] = Arrays.copyOf(EXP_START_DATES[i], NUM_OBS - 1);
      smallEndDates[i] = Arrays.copyOf(EXP_END_DATES[i], NUM_OBS - 1);
      smallAcc[i] = Arrays.copyOf(FIX_ACC_FACTORS[i], NUM_OBS - 1);
    }
    try {
      new CouponIborAverageCompoundingDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
          ACCRUAL_FACTORS, INDEX, FIXING_DATES, WEIGHTS, smallStartDates, EXP_END_DATES, FIX_ACC_FACTORS);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("fixingPeriodStartDates length different from fixingDate length", e.getMessage());
    }
    try {
      new CouponIborAverageCompoundingDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
          ACCRUAL_FACTORS, INDEX, FIXING_DATES, WEIGHTS, EXP_START_DATES, smallEndDates, FIX_ACC_FACTORS);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("fixingPeriodEndDates length different from fixingDate length", e.getMessage());
    }
    try {
      new CouponIborAverageCompoundingDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
          ACCRUAL_FACTORS, INDEX, FIXING_DATES, WEIGHTS, EXP_START_DATES, EXP_END_DATES, smallAcc);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("fixingPeriodAccrualFactors length different from fixingDate length", e.getMessage());
    }

    final ZonedDateTime afterPayment = PAYMENT_DATE.plusDays(1);
    try {
      DFN1.toDerivative(afterPayment);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("date is after payment date", e.getMessage());
    }
    final ZonedDateTime afterFixing = FIXING_DATES[0][0].plusDays(1);
    try {
      DFN1.toDerivative(afterFixing);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("Do not have any fixing data but are asking for a derivative at " + afterFixing + " which is after fixing date " + FIXING_DATES[0][0], e.getMessage());
    }
  }

  /**
   * 
   */
  @Test
  public void consistencyTest() {
    final CouponIborAverageCompoundingDefinition dfn1WithDouble = DFN1.withNotional(NOTIONAL * 2);

    assertEquals(DFN1.getIndex(), DFN2.getIndex());
    assertEquals(DFN1.getIndex(), dfn1WithDouble.getIndex());

    for (int i = 0; i < NUM_OBS; ++i) {

      for (int j = 0; j < NUM_PRDS; ++j) {
        assertEquals(DFN1.getPaymentAccrualFactors()[j], DFN2.getPaymentAccrualFactors()[j]);
        assertEquals(DFN1.getFixingDates()[j][i], DFN2.getFixingDates()[j][i]);
        assertEquals(DFN1.getWeight()[j][i], DFN2.getWeight()[j][i]);
        assertEquals(DFN1.getFixingPeriodStartDates()[j][i], DFN2.getFixingPeriodStartDates()[j][i]);
        assertEquals(DFN1.getFixingPeriodEndDates()[j][i], DFN2.getFixingPeriodEndDates()[j][i]);
        assertEquals(DFN1.getFixingPeriodAccrualFactor()[j][i], DFN2.getFixingPeriodAccrualFactor()[j][i]);

        assertEquals(DFN1.getPaymentAccrualFactors()[j], dfn1WithDouble.getPaymentAccrualFactors()[j]);
        assertEquals(DFN1.getFixingDates()[j][i], dfn1WithDouble.getFixingDates()[j][i]);
        assertEquals(DFN1.getWeight()[j][i], dfn1WithDouble.getWeight()[j][i]);
        assertEquals(DFN1.getFixingPeriodStartDates()[j][i], dfn1WithDouble.getFixingPeriodStartDates()[j][i]);
        assertEquals(DFN1.getFixingPeriodEndDates()[j][i], dfn1WithDouble.getFixingPeriodEndDates()[j][i]);
        assertEquals(DFN1.getFixingPeriodAccrualFactor()[j][i], dfn1WithDouble.getFixingPeriodAccrualFactor()[j][i]);
      }
    }

    final CouponIborAverageCompoundingDefinition dfn1 = CouponIborAverageCompoundingDefinition.from(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, ACCRUAL_FACTORS,
        INDEX, FIXING_DATES, WEIGHTS, CALENDAR);
    final CouponIborAverageCompoundingDefinition dfn2 = CouponIborAverageCompoundingDefinition.from(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, ACCRUAL_FACTORS,
        INDEX, FIXING_DATES, WEIGHTS, EXP_START_DATES, EXP_END_DATES, FIX_ACC_FACTORS);

    assertTrue(DFN1.equals(dfn1));
    assertEquals(DFN1.hashCode(), dfn1.hashCode());
    assertTrue(DFN2.equals(dfn2));
    assertEquals(DFN2.hashCode(), dfn2.hashCode());

    assertFalse(DFN1.hashCode() == dfn1WithDouble.hashCode());
    assertFalse(DFN1.equals(dfn1WithDouble));

    assertTrue(DFN1.toDerivative(REFERENCE_DATE).equals(dfn1.toDerivative(REFERENCE_DATE)));

  }

}
