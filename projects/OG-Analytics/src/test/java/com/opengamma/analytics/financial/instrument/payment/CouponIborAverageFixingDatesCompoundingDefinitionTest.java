/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;
import org.testng.internal.junit.ArrayAsserts;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverageFixingDatesCompounding;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the construction of CouponIborAverageCompoundingDefinition.
 */
public class CouponIborAverageFixingDatesCompoundingDefinitionTest {

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

  //Example 1: EUR with same numbers of fixing in all periods
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
    ACCRUAL_FACTORS[0] *= 1.01;
    ACCRUAL_FACTORS[NUM_PRDS - 2] *= 0.99;
  }
  private static final double NOTIONAL = 1000000;
  private static final CouponIborAverageFixingDatesCompoundingDefinition DFN1 =
      new CouponIborAverageFixingDatesCompoundingDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE,
          ACCRUAL_FACTOR, NOTIONAL, ACCRUAL_FACTORS, INDEX, FIXING_DATES, WEIGHTS, CALENDAR);

  //Example 2: USD with same numbers of fixing in all periods, from full detail
  private static ZonedDateTime[][] EXP_START_DATES = new ZonedDateTime[NUM_PRDS][NUM_OBS];
  private static ZonedDateTime[][] EXP_END_DATES = new ZonedDateTime[NUM_PRDS][NUM_OBS];
  private static double[][] FIX_ACC_FACTORS = new double[NUM_PRDS][NUM_OBS];
  static {
    for (int i = 0; i < NUM_OBS; ++i) {
      for (int j = 0; j < NUM_PRDS; ++j) {
        EXP_START_DATES[j][i] = ScheduleCalculator.getAdjustedDate(FIXING_DATES[j][i], INDEX.getSpotLag(), CALENDAR);
        EXP_END_DATES[j][i] = ScheduleCalculator.getAdjustedDate(EXP_START_DATES[j][i], INDEX.getTenor(),
            INDEX.getBusinessDayConvention(), CALENDAR, INDEX.isEndOfMonth());
        FIX_ACC_FACTORS[j][i] = INDEX.getDayCount().getDayCountFraction(EXP_START_DATES[j][i], EXP_END_DATES[j][i], CALENDAR);
      }
    }
  }
  private static final CouponIborAverageFixingDatesCompoundingDefinition DFN2 =
      new CouponIborAverageFixingDatesCompoundingDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE,
          ACCRUAL_FACTOR, NOTIONAL, ACCRUAL_FACTORS, INDEX, FIXING_DATES, WEIGHTS, EXP_START_DATES, EXP_END_DATES, FIX_ACC_FACTORS);

  // Example 3: different number of fixing in each subperiod
  private static final int NB_SUBPERIODS = 3;
  private static final ZonedDateTime[] ACCRUAL_START_DATE_SUB_3 = new ZonedDateTime[NB_SUBPERIODS + 1];
  private static final ZonedDateTime[][] FIXING_DATES_3 = new ZonedDateTime[NB_SUBPERIODS][];
  private static final double[][] WEIGHTS_3 = new double[NB_SUBPERIODS][];
  private static final double[] ACCRUAL_FACTORS_3 = new double[NB_SUBPERIODS];
  static {
    for (int loopsub = 0; loopsub <= NB_SUBPERIODS; loopsub++) {
      ACCRUAL_START_DATE_SUB_3[loopsub] = ScheduleCalculator.getAdjustedDate(ACCRUAL_START_DATE, Period.ofMonths(loopsub), INDEX, CALENDAR);
    }
    for (int loopsub = 0; loopsub < NB_SUBPERIODS; loopsub++) {
      List<ZonedDateTime> listDates = new ArrayList<>();
      ZonedDateTime startFixPeriod = ACCRUAL_START_DATE_SUB_3[loopsub];
      listDates.add(ScheduleCalculator.getAdjustedDate(startFixPeriod, -INDEX.getSpotLag(), CALENDAR));
      startFixPeriod = ScheduleCalculator.getAdjustedDate(startFixPeriod, 1, CALENDAR);
      while (startFixPeriod.isBefore(ACCRUAL_START_DATE_SUB_3[loopsub + 1])) {
        listDates.add(ScheduleCalculator.getAdjustedDate(startFixPeriod, -INDEX.getSpotLag(), CALENDAR));
        startFixPeriod = ScheduleCalculator.getAdjustedDate(startFixPeriod, 1, CALENDAR);
      }
      FIXING_DATES_3[loopsub] = listDates.toArray(new ZonedDateTime[0]);
      WEIGHTS_3[loopsub] = new double[FIXING_DATES_3[loopsub].length];
      ACCRUAL_FACTORS_3[loopsub] = DAY_COUNT_INDEX.getDayCountFraction(ACCRUAL_START_DATE_SUB_3[loopsub],
          ACCRUAL_START_DATE_SUB_3[loopsub + 1]);
      for (int loopf = 0; loopf < FIXING_DATES_3[loopsub].length; loopf++) {
        WEIGHTS_3[loopsub][loopf] = 1.0d / FIXING_DATES_3[loopsub].length;
      }
    }
  }
  private static final CouponIborAverageFixingDatesCompoundingDefinition DFN3 =
      new CouponIborAverageFixingDatesCompoundingDefinition(CUR, ACCRUAL_START_DATE_SUB_3[3], ACCRUAL_START_DATE_SUB_3[0],
          ACCRUAL_START_DATE_SUB_3[3], ACCRUAL_FACTOR, NOTIONAL, ACCRUAL_FACTORS_3, INDEX, FIXING_DATES_3, WEIGHTS_3, CALENDAR);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 12, 27);

  /**
   * 
   */
  @Test
  public void getter() {
    ArrayAsserts.assertArrayEquals("CouponIborAverageCompoundingDefinition: getter", FIXING_DATES, DFN2.getFixingDates());
    ArrayAsserts.assertArrayEquals("CouponIborAverageCompoundingDefinition: getter", WEIGHTS, DFN2.getWeight());
    ArrayAsserts.assertArrayEquals("CouponIborAverageCompoundingDefinition: getter", EXP_START_DATES, DFN2.getFixingPeriodStartDates());
    ArrayAsserts.assertArrayEquals("CouponIborAverageCompoundingDefinition: getter", EXP_END_DATES, DFN2.getFixingPeriodEndDates());
    ArrayAsserts.assertArrayEquals("CouponIborAverageCompoundingDefinition: getter", FIX_ACC_FACTORS, DFN2.getFixingPeriodAccrualFactor());
    ArrayAsserts.assertArrayEquals("CouponIborAverageCompoundingDefinition: getter", FIXING_DATES_3, DFN3.getFixingDates());
  }

  /**
   * all fixed, none fixed, partially fixed cases
   */
  @Test
  public void toDerivativeGeneralTest() {
    final ZonedDateTime[] dates1 = new ZonedDateTime[NUM_PRDS * NUM_OBS];
    final double[] rates1 = new double[NUM_PRDS * NUM_OBS];
    Arrays.fill(rates1, 0.01);
    for (int i = 0; i < NUM_PRDS; ++i) {
      for (int j = 0; j < NUM_OBS; ++j) {
        dates1[NUM_OBS * i + j] = FIXING_DATES[i][j];
      }
    }
    final DoubleTimeSeries<ZonedDateTime> fixingTS1 = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(dates1, rates1);
    final Coupon derivative1 = DFN1.toDerivative(FIXING_DATES[0][0].minusDays(10), fixingTS1);
    final Coupon derivative2 = DFN1.toDerivative(FIXING_DATES[NUM_PRDS - 1][NUM_OBS - 1].plusDays(1), fixingTS1);
    assertTrue((derivative2 instanceof CouponFixed));
    double fixed = 1.0;
    for (int i = 0; i < NUM_PRDS; ++i) {
      double fwd = 0.0;
      for (int j = 0; j < NUM_OBS; ++j) {
        fwd += 0.01 * WEIGHTS[i][j];
      }
      fixed *= (1.0 + fwd * DFN1.getPaymentAccrualFactors()[i]);
    }
    assertEquals((fixed - 1.0) / DFN1.getPaymentYearFraction(), ((CouponFixed) derivative2).getFixedRate());

    assertTrue((derivative1 instanceof CouponIborAverageFixingDatesCompounding));
    checkOutputs(FIXING_DATES[0][0].minusDays(10), DFN1, (CouponIborAverageFixingDatesCompounding) derivative1, 0, 0);
    assertEquals(0.0, ((CouponIborAverageFixingDatesCompounding) derivative1).getAmountAccrued());
    assertEquals(0.0, ((CouponIborAverageFixingDatesCompounding) derivative1).getInvestmentFactor());

    final int prd = 2;
    final int dt = 3;
    final ZonedDateTime refDate3 = FIXING_DATES[prd][dt].minusDays(1);
    final CouponIborAverageFixingDatesCompounding derivative3 = (CouponIborAverageFixingDatesCompounding) DFN1.toDerivative(refDate3, fixingTS1);
    checkOutputs(refDate3, DFN1, derivative3, prd, dt);

    double refAcc = 1.0;
    for (int i = 0; i < prd; ++i) {
      double rate = 0.0;
      for (int j = 0; j < NUM_OBS; ++j) {
        rate += WEIGHTS[i][j] * 0.01;
      }
      refAcc *= (1.0 + rate * ACCRUAL_FACTORS[i]);
    }
    assertEquals(refAcc, derivative3.getAmountAccrued(), 1.e-14);
    double refRate = 0.0;
    for (int j = 0; j < dt; ++j) {
      refRate += WEIGHTS[prd][j] * 0.01;
    }
    assertEquals(refRate, derivative3.getInvestmentFactor(), 1.e-14);

    try {
      DFN1.toDerivative(PAYMENT_DATE.plusDays(10), fixingTS1);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("date is after payment date", e.getMessage());
    }
    try {
      DFN1.toDerivative(DateUtils.getUTCDate(2011, 5, 3), ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
          new ZonedDateTime[] {DateUtils.getUTCDate(2010, 2, 7) }, new double[] {0.01 }));
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("Could not get fixing value for date " + FIXING_DATES[0][0], e.getMessage());
    }
  }

  /**
   * reference date == final fixing date of a period
   */
  @Test
  public void toDerivativeEndOfPeriodTest() {
    final ZonedDateTime[] dates1 = new ZonedDateTime[NUM_PRDS * NUM_OBS];
    final double[] rates1 = new double[NUM_PRDS * NUM_OBS];
    Arrays.fill(rates1, 0.01);
    for (int i = 0; i < NUM_PRDS; ++i) {
      for (int j = 0; j < NUM_OBS; ++j) {
        dates1[NUM_OBS * i + j] = FIXING_DATES[i][j];
      }
    }
    final DoubleTimeSeries<ZonedDateTime> fixingTS1 = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(dates1, rates1);

    final ZonedDateTime refDate4 = FIXING_DATES[2][4];
    final CouponIborAverageFixingDatesCompounding derivative4 = (CouponIborAverageFixingDatesCompounding) DFN1.toDerivative(refDate4, fixingTS1);
    checkOutputs(refDate4, DFN1, derivative4, 2 + 1, 0);

    double refAcc = 1.0;
    for (int i = 0; i < 3; ++i) {
      double rate = 0.0;
      for (int j = 0; j < NUM_OBS; ++j) {
        rate += WEIGHTS[i][j] * 0.01;
      }
      refAcc *= (1.0 + rate * ACCRUAL_FACTORS[i]);
    }
    assertEquals(refAcc, derivative4.getAmountAccrued(), 1.e-14);
    assertEquals(0., derivative4.getInvestmentFactor(), 1.e-14);
  }

  /**
   * fixing Date == reference date, rate is available
   */
  @Test
  public void toDerivativeCoincideTest() {
    final ZonedDateTime[] dates1 = new ZonedDateTime[NUM_PRDS * NUM_OBS];
    final double[] rates1 = new double[NUM_PRDS * NUM_OBS];
    Arrays.fill(rates1, 0.01);
    for (int i = 0; i < NUM_PRDS; ++i) {
      for (int j = 0; j < NUM_OBS; ++j) {
        dates1[NUM_OBS * i + j] = FIXING_DATES[i][j];
      }
    }
    final DoubleTimeSeries<ZonedDateTime> fixingTS1 = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(dates1, rates1);
    final ZonedDateTime refDate5 = FIXING_DATES[1][2];
    final CouponIborAverageFixingDatesCompounding derivative5 = (CouponIborAverageFixingDatesCompounding) DFN1.toDerivative(refDate5, fixingTS1);
    checkOutputs(refDate5, DFN1, derivative5, 1, 3);

    double refAcc = 1.0;
    for (int i = 0; i < 1; ++i) {
      double rate = 0.0;
      for (int j = 0; j < NUM_OBS; ++j) {
        rate += WEIGHTS[i][j] * 0.01;
      }
      refAcc *= (1.0 + rate * ACCRUAL_FACTORS[i]);
    }
    assertEquals(refAcc, derivative5.getAmountAccrued(), 1.e-14);
    double refRate = 0.0;
    for (int j = 0; j < 3; ++j) {
      refRate += WEIGHTS[1][j] * 0.01;
    }
    assertEquals(refRate, derivative5.getInvestmentFactor(), 1.e-14);
  }

  /**
   * fixing Date == reference date, but rate is null
   */
  @Test
  public void toDerivativeNullRateTest() {
    final ZonedDateTime[] dates1 = new ZonedDateTime[NUM_PRDS * NUM_OBS];
    final double[] rates1 = new double[NUM_PRDS * NUM_OBS];
    Arrays.fill(rates1, 0.01);
    for (int i = 0; i < NUM_PRDS; ++i) {
      for (int j = 0; j < NUM_OBS; ++j) {
        dates1[NUM_OBS * i + j] = FIXING_DATES[i][j];
      }
    }
    dates1[7] = dates1[7].plusDays(1);
    final DoubleTimeSeries<ZonedDateTime> fixingTS1 = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(dates1, rates1);

    final ZonedDateTime refDate5 = FIXING_DATES[1][2];
    final CouponIborAverageFixingDatesCompounding derivative5 = (CouponIborAverageFixingDatesCompounding) DFN1.toDerivative(refDate5, fixingTS1);
    checkOutputs(refDate5, DFN1, derivative5, 1, 2);

    double refAcc = 1.0;
    for (int i = 0; i < 1; ++i) {
      double rate = 0.0;
      for (int j = 0; j < NUM_OBS; ++j) {
        rate += WEIGHTS[i][j] * 0.01;
      }
      refAcc *= (1.0 + rate * ACCRUAL_FACTORS[i]);
    }
    assertEquals(refAcc, derivative5.getAmountAccrued(), 1.e-14);
    double refRate = 0.0;
    for (int j = 0; j < 2; ++j) {
      refRate += WEIGHTS[1][j] * 0.01;
    }
    assertEquals(refRate, derivative5.getInvestmentFactor(), 1.e-14);
  }

  /**
   * Fixing dates arrays are not the same size for all subperiods
   */
  @Test
  public void toDerivativeDifferentFixingSize() {

    int dim = 0;
    final int[] nDates = new int[NB_SUBPERIODS];
    for (int i = 0; i < NB_SUBPERIODS; ++i) {
      nDates[i] = FIXING_DATES_3[i].length;
      dim += FIXING_DATES_3[i].length;
    }

    final ZonedDateTime[] dates1 = new ZonedDateTime[dim];
    final double[] rates1 = new double[dim];
    Arrays.fill(rates1, 0.01);
    int k = 0;
    for (int i = 0; i < NB_SUBPERIODS; ++i) {
      for (int j = 0; j < nDates[i]; ++j) {
        dates1[k] = FIXING_DATES_3[i][j];
        ++k;
      }
    }
    dates1[nDates[0] + 1] = dates1[nDates[0] + 1].plusDays(1);
    final DoubleTimeSeries<ZonedDateTime> fixingTS1 = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(dates1, rates1);
    final ZonedDateTime refDate = FIXING_DATES_3[1][1];
    final CouponIborAverageFixingDatesCompounding deriv = (CouponIborAverageFixingDatesCompounding) DFN3.toDerivative(refDate, fixingTS1);

    checkOutputs(refDate, DFN3, deriv, 1, 1);

    double refAcc = 1.0;
    for (int i = 0; i < 1; ++i) {
      double rate = 0.0;
      for (int j = 0; j < nDates[i]; ++j) {
        rate += WEIGHTS_3[i][j] * 0.01;
      }
      refAcc *= (1.0 + rate * ACCRUAL_FACTORS_3[i]);
    }
    assertEquals(refAcc, deriv.getAmountAccrued(), 1.e-14);
    double refRate = 0.0;
    for (int j = 0; j < 1; ++j) {
      refRate += WEIGHTS_3[1][j] * 0.01;
    }
    assertEquals(refRate, deriv.getInvestmentFactor(), 1.e-14);
  }

  /**
   * Exception expected
   */
  @Test
  public void exceptionTest() {
    try {
      new CouponIborAverageFixingDatesCompoundingDefinition(Currency.GBP, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
          ACCRUAL_FACTORS, INDEX, FIXING_DATES, WEIGHTS, CALENDAR);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("index currency different from payment currency", e.getMessage());
    }
    try {
      new CouponIborAverageFixingDatesCompoundingDefinition(Currency.USD, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
          ACCRUAL_FACTORS, INDEX, FIXING_DATES, WEIGHTS, EXP_START_DATES, EXP_END_DATES, FIX_ACC_FACTORS);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("index currency different from payment currency", e.getMessage());
    }

    double[][] shortWeight = Arrays.copyOf(WEIGHTS, NUM_PRDS - 1);
    try {
      new CouponIborAverageFixingDatesCompoundingDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
          ACCRUAL_FACTORS, INDEX, FIXING_DATES, shortWeight, CALENDAR);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("weights length different from fixingDate length", e.getMessage());
    }
    try {
      new CouponIborAverageFixingDatesCompoundingDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
          ACCRUAL_FACTORS, INDEX, FIXING_DATES, shortWeight, EXP_START_DATES, EXP_END_DATES, FIX_ACC_FACTORS);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("weights length different from fixingDate length", e.getMessage());
    }

    double[][] smallWeight = new double[NUM_PRDS][];
    for (int i = 0; i < NUM_PRDS; ++i) {
      smallWeight[i] = Arrays.copyOf(WEIGHTS[i], NUM_OBS - 1);
    }

    final ZonedDateTime[][] shortStartDates = Arrays.copyOf(EXP_START_DATES, NUM_PRDS - 1);
    try {
      new CouponIborAverageFixingDatesCompoundingDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
          ACCRUAL_FACTORS, INDEX, FIXING_DATES, WEIGHTS, shortStartDates, EXP_END_DATES, FIX_ACC_FACTORS);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("fixingPeriodStartDates length different from fixingDate length", e.getMessage());
    }

    final ZonedDateTime[][] shortEndDates = Arrays.copyOf(EXP_END_DATES, NUM_PRDS - 1);
    try {
      new CouponIborAverageFixingDatesCompoundingDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
          ACCRUAL_FACTORS, INDEX, FIXING_DATES, WEIGHTS, EXP_START_DATES, shortEndDates, FIX_ACC_FACTORS);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("fixingPeriodEndDates length different from fixingDate length", e.getMessage());
    }

    final double[][] shortAcc = Arrays.copyOf(FIX_ACC_FACTORS, NUM_PRDS - 1);
    try {
      new CouponIborAverageFixingDatesCompoundingDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
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
    final CouponIborAverageFixingDatesCompoundingDefinition dfn1WithDouble = DFN1.withNotional(NOTIONAL * 2);

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

    final CouponIborAverageFixingDatesCompoundingDefinition dfn1 = CouponIborAverageFixingDatesCompoundingDefinition.from(CUR,
        PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, ACCRUAL_FACTORS,
        INDEX, FIXING_DATES, WEIGHTS, CALENDAR);
    final CouponIborAverageFixingDatesCompoundingDefinition dfn2 = CouponIborAverageFixingDatesCompoundingDefinition.from(CUR,
        PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, ACCRUAL_FACTORS,
        INDEX, FIXING_DATES, WEIGHTS, EXP_START_DATES, EXP_END_DATES, FIX_ACC_FACTORS);

    assertTrue(DFN1.equals(dfn1));
    assertEquals(DFN1.hashCode(), dfn1.hashCode());
    assertTrue(DFN2.equals(dfn2));
    assertEquals(DFN2.hashCode(), dfn2.hashCode());

    assertFalse(DFN1.hashCode() == dfn1WithDouble.hashCode());
    assertFalse(DFN1.equals(dfn1WithDouble));

    assertTrue(DFN1.toDerivative(REFERENCE_DATE).equals(dfn1.toDerivative(REFERENCE_DATE)));

  }

  private void checkOutputs(final ZonedDateTime refDate, final CouponIborAverageFixingDatesCompoundingDefinition def,
      final CouponIborAverageFixingDatesCompounding dev, final int posPeriod, final int posDate) {
    final int nPrds = def.getPaymentAccrualFactors().length;
    assertEquals(nPrds - posPeriod, dev.getFixingPeriodAccrualFactor().length);
    assertEquals(nPrds - posPeriod, dev.getFixingPeriodEndTime().length);
    assertEquals(nPrds - posPeriod, dev.getFixingPeriodStartTime().length);
    assertEquals(nPrds - posPeriod, dev.getFixingTime().length);
    assertEquals(nPrds - posPeriod, dev.getPaymentAccrualFactors().length);
    for (int i = 0; i < nPrds - posPeriod; ++i) {
      assertEquals(def.getPaymentAccrualFactors()[i + posPeriod], dev.getPaymentAccrualFactors()[i]);
    }

    for (int i = 1; i < nPrds - posPeriod; ++i) {
      assertEquals(def.getPaymentAccrualFactors()[i + posPeriod], dev.getPaymentAccrualFactors()[i]);
      assertEquals(def.getFixingPeriodAccrualFactor()[i + posPeriod].length, dev.getFixingPeriodAccrualFactor()[i].length);
      assertEquals(def.getFixingPeriodEndDates()[i + posPeriod].length, dev.getFixingPeriodEndTime()[i].length);
      assertEquals(def.getFixingPeriodStartDates()[i + posPeriod].length, dev.getFixingPeriodStartTime()[i].length);
      assertEquals(def.getFixingDates()[i + posPeriod].length, dev.getFixingTime()[i].length);
      final int nDates = def.getFixingPeriodAccrualFactor()[i + posPeriod].length;
      for (int j = 0; j < nDates; ++j) {
        assertEquals(def.getFixingPeriodAccrualFactor()[i + posPeriod][j], dev.getFixingPeriodAccrualFactor()[i][j]);
        assertEquals(TimeCalculator.getTimeBetween(refDate, def.getFixingDates()[i + posPeriod][j]), dev.getFixingTime()[i][j]);
        assertEquals(TimeCalculator.getTimeBetween(refDate, def.getFixingPeriodEndDates()[i + posPeriod][j]), dev.getFixingPeriodEndTime()[i][j]);
        assertEquals(TimeCalculator.getTimeBetween(refDate, def.getFixingPeriodStartDates()[i + posPeriod][j]), dev.getFixingPeriodStartTime()[i][j]);
      }
    }

    final int nDates0 = def.getFixingPeriodAccrualFactor()[posPeriod].length;
    assertEquals(nDates0 - posDate, dev.getFixingPeriodAccrualFactor()[0].length);
    assertEquals(nDates0 - posDate, dev.getFixingPeriodEndTime()[0].length);
    assertEquals(nDates0 - posDate, dev.getFixingPeriodStartTime()[0].length);
    assertEquals(nDates0 - posDate, dev.getFixingTime()[0].length);
    for (int j = 0; j < nDates0 - posDate; ++j) {
      assertEquals(def.getFixingPeriodAccrualFactor()[posPeriod][j + posDate], dev.getFixingPeriodAccrualFactor()[0][j]);
      assertEquals(TimeCalculator.getTimeBetween(refDate, def.getFixingDates()[posPeriod][j + posDate]), dev.getFixingTime()[0][j]);
      assertEquals(TimeCalculator.getTimeBetween(refDate, def.getFixingPeriodEndDates()[posPeriod][j + posDate]), dev.getFixingPeriodEndTime()[0][j]);
      assertEquals(TimeCalculator.getTimeBetween(refDate, def.getFixingPeriodStartDates()[posPeriod][j + posDate]), dev.getFixingPeriodStartTime()[0][j]);
    }
  }
}
