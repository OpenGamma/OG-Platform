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
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverageCompounding;
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

    assertTrue((derivative1 instanceof CouponIborAverageCompounding));
    assertEquals(0.0, ((CouponIborAverageCompounding) derivative1).getAmountAccrued());
    assertEquals(0.0, ((CouponIborAverageCompounding) derivative1).getRateFixed());

    final ZonedDateTime refDate3 = FIXING_DATES[2][3].minusDays(1);
    final CouponIborAverageCompounding derivative3 = (CouponIborAverageCompounding) DFN1.toDerivative(FIXING_DATES[2][3].minusDays(1), fixingTS1);

    assertEquals(NUM_PRDS - 2, derivative3.getFixingPeriodAccrualFactor().length);
    assertEquals(NUM_PRDS - 2, derivative3.getFixingPeriodEndTime().length);
    assertEquals(NUM_PRDS - 2, derivative3.getFixingPeriodStartTime().length);
    assertEquals(NUM_PRDS - 2, derivative3.getFixingTime().length);
    assertEquals(NUM_PRDS - 2, derivative3.getPaymentAccrualFactors().length);

    assertEquals(NUM_OBS - 3, derivative3.getFixingPeriodAccrualFactor()[0].length);
    assertEquals(NUM_OBS - 3, derivative3.getFixingPeriodEndTime()[0].length);
    assertEquals(NUM_OBS - 3, derivative3.getFixingPeriodStartTime()[0].length);
    assertEquals(NUM_OBS - 3, derivative3.getFixingTime()[0].length);
    assertEquals(NUM_OBS, derivative3.getFixingPeriodAccrualFactor()[1].length);
    assertEquals(NUM_OBS, derivative3.getFixingPeriodEndTime()[1].length);
    assertEquals(NUM_OBS, derivative3.getFixingPeriodStartTime()[1].length);
    assertEquals(NUM_OBS, derivative3.getFixingTime()[1].length);

    for (int i = 1; i < NUM_PRDS - 2; ++i) {
      assertEquals(DFN1.getPaymentAccrualFactors()[i + 2], derivative3.getPaymentAccrualFactors()[i]);
      for (int j = 0; j < NUM_OBS; ++j) {
        assertEquals(DFN1.getFixingPeriodAccrualFactor()[i + 2][j], derivative3.getFixingPeriodAccrualFactor()[i][j]);
        assertEquals(TimeCalculator.getTimeBetween(refDate3, DFN1.getFixingDates()[i + 2][j]), derivative3.getFixingTime()[i][j]);
        assertEquals(TimeCalculator.getTimeBetween(refDate3, DFN1.getFixingPeriodEndDates()[i + 2][j]), derivative3.getFixingPeriodEndTime()[i][j]);
        assertEquals(TimeCalculator.getTimeBetween(refDate3, DFN1.getFixingPeriodStartDates()[i + 2][j]), derivative3.getFixingPeriodStartTime()[i][j]);
      }
    }
    for (int j = 0; j < NUM_OBS - 3; ++j) {
      assertEquals(DFN1.getFixingPeriodAccrualFactor()[2][j + 3], derivative3.getFixingPeriodAccrualFactor()[0][j]);
      assertEquals(TimeCalculator.getTimeBetween(refDate3, DFN1.getFixingDates()[2][j + 3]), derivative3.getFixingTime()[0][j]);
      assertEquals(TimeCalculator.getTimeBetween(refDate3, DFN1.getFixingPeriodEndDates()[2][j + 3]), derivative3.getFixingPeriodEndTime()[0][j]);
      assertEquals(TimeCalculator.getTimeBetween(refDate3, DFN1.getFixingPeriodStartDates()[2][j + 3]), derivative3.getFixingPeriodStartTime()[0][j]);
    }

    double refAcc = 1.0;
    for (int i = 0; i < 2; ++i) {
      double rate = 0.0;
      for (int j = 0; j < NUM_OBS; ++j) {
        rate += WEIGHTS[i][j] * 0.01;
      }
      refAcc *= (1.0 + rate * ACCRUAL_FACTORS[i]);
    }
    assertEquals(refAcc, derivative3.getAmountAccrued(), 1.e-14);
    double refRate = 0.0;
    for (int j = 0; j < 3; ++j) {
      refRate += WEIGHTS[2][j] * 0.01;
    }
    assertEquals(refRate, derivative3.getRateFixed(), 1.e-14);

    try {
      DFN1.toDerivative(PAYMENT_DATE.plusDays(10), fixingTS1);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("date is after payment date", e.getMessage());
    }
    try {
      DFN1.toDerivative(DateUtils.getUTCDate(2011, 5, 3), ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2010, 2, 7) }, new double[] {0.01 }));
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
    final CouponIborAverageCompounding derivative4 = (CouponIborAverageCompounding) DFN1.toDerivative(refDate4, fixingTS1);

    assertEquals(NUM_PRDS - 2 - 1, derivative4.getFixingPeriodAccrualFactor().length);
    assertEquals(NUM_PRDS - 2 - 1, derivative4.getFixingPeriodEndTime().length);
    assertEquals(NUM_PRDS - 2 - 1, derivative4.getFixingPeriodStartTime().length);
    assertEquals(NUM_PRDS - 2 - 1, derivative4.getFixingTime().length);
    assertEquals(NUM_PRDS - 2 - 1, derivative4.getPaymentAccrualFactors().length);
    for (int i = 0; i < NUM_PRDS - 2 - 1; ++i) { // No partially fixed period
      assertEquals(DFN1.getPaymentAccrualFactors()[i + 2 + 1], derivative4.getPaymentAccrualFactors()[i]);
      for (int j = 0; j < NUM_OBS; ++j) {
        assertEquals(DFN1.getFixingPeriodAccrualFactor()[i + 2 + 1][j], derivative4.getFixingPeriodAccrualFactor()[i][j]);
        assertEquals(TimeCalculator.getTimeBetween(refDate4, DFN1.getFixingDates()[i + 2 + 1][j]), derivative4.getFixingTime()[i][j]);
        assertEquals(TimeCalculator.getTimeBetween(refDate4, DFN1.getFixingPeriodEndDates()[i + 2 + 1][j]), derivative4.getFixingPeriodEndTime()[i][j]);
        assertEquals(TimeCalculator.getTimeBetween(refDate4, DFN1.getFixingPeriodStartDates()[i + 2 + 1][j]), derivative4.getFixingPeriodStartTime()[i][j]);
      }
    }

    double refAcc = 1.0;
    for (int i = 0; i < 3; ++i) {
      double rate = 0.0;
      for (int j = 0; j < NUM_OBS; ++j) {
        rate += WEIGHTS[i][j] * 0.01;
      }
      refAcc *= (1.0 + rate * ACCRUAL_FACTORS[i]);
    }
    assertEquals(refAcc, derivative4.getAmountAccrued(), 1.e-14);
    assertEquals(0., derivative4.getRateFixed(), 1.e-14);
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
    final CouponIborAverageCompounding derivative5 = (CouponIborAverageCompounding) DFN1.toDerivative(refDate5, fixingTS1);
    assertEquals(NUM_PRDS - 1, derivative5.getFixingPeriodAccrualFactor().length);
    assertEquals(NUM_PRDS - 1, derivative5.getFixingPeriodEndTime().length);
    assertEquals(NUM_PRDS - 1, derivative5.getFixingPeriodStartTime().length);
    assertEquals(NUM_PRDS - 1, derivative5.getFixingTime().length);
    assertEquals(NUM_PRDS - 1, derivative5.getPaymentAccrualFactors().length);
    assertEquals(NUM_OBS - 3, derivative5.getFixingPeriodAccrualFactor()[0].length);
    assertEquals(NUM_OBS - 3, derivative5.getFixingPeriodEndTime()[0].length);
    assertEquals(NUM_OBS - 3, derivative5.getFixingPeriodStartTime()[0].length);
    assertEquals(NUM_OBS - 3, derivative5.getFixingTime()[0].length);

    for (int i = 1; i < NUM_PRDS - 1; ++i) {
      assertEquals(DFN1.getPaymentAccrualFactors()[i + 1], derivative5.getPaymentAccrualFactors()[i]);
      for (int j = 0; j < NUM_OBS - 3; ++j) {
        assertEquals(DFN1.getFixingPeriodAccrualFactor()[i + 1][j], derivative5.getFixingPeriodAccrualFactor()[i][j]);
        assertEquals(TimeCalculator.getTimeBetween(refDate5, DFN1.getFixingDates()[i + 1][j]), derivative5.getFixingTime()[i][j]);
        assertEquals(TimeCalculator.getTimeBetween(refDate5, DFN1.getFixingPeriodEndDates()[i + 1][j]), derivative5.getFixingPeriodEndTime()[i][j]);
        assertEquals(TimeCalculator.getTimeBetween(refDate5, DFN1.getFixingPeriodStartDates()[i + 1][j]), derivative5.getFixingPeriodStartTime()[i][j]);
      }
    }
    for (int j = 0; j < NUM_OBS - 3; ++j) {
      assertEquals(DFN1.getFixingPeriodAccrualFactor()[1][j + 3], derivative5.getFixingPeriodAccrualFactor()[0][j]);
      assertEquals(TimeCalculator.getTimeBetween(refDate5, DFN1.getFixingDates()[1][j + 3]), derivative5.getFixingTime()[0][j]);
      assertEquals(TimeCalculator.getTimeBetween(refDate5, DFN1.getFixingPeriodEndDates()[1][j + 3]), derivative5.getFixingPeriodEndTime()[0][j]);
      assertEquals(TimeCalculator.getTimeBetween(refDate5, DFN1.getFixingPeriodStartDates()[1][j + 3]), derivative5.getFixingPeriodStartTime()[0][j]);
    }

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
    assertEquals(refRate, derivative5.getRateFixed(), 1.e-14);
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
    final CouponIborAverageCompounding derivative5 = (CouponIborAverageCompounding) DFN1.toDerivative(refDate5, fixingTS1);
    assertEquals(NUM_PRDS - 1, derivative5.getFixingPeriodAccrualFactor().length);
    assertEquals(NUM_PRDS - 1, derivative5.getFixingPeriodEndTime().length);
    assertEquals(NUM_PRDS - 1, derivative5.getFixingPeriodStartTime().length);
    assertEquals(NUM_PRDS - 1, derivative5.getFixingTime().length);
    assertEquals(NUM_PRDS - 1, derivative5.getPaymentAccrualFactors().length);
    assertEquals(NUM_OBS - 2, derivative5.getFixingPeriodAccrualFactor()[0].length);
    assertEquals(NUM_OBS - 2, derivative5.getFixingPeriodEndTime()[0].length);
    assertEquals(NUM_OBS - 2, derivative5.getFixingPeriodStartTime()[0].length);
    assertEquals(NUM_OBS - 2, derivative5.getFixingTime()[0].length);

    for (int i = 1; i < NUM_PRDS - 1; ++i) {
      assertEquals(DFN1.getPaymentAccrualFactors()[i + 1], derivative5.getPaymentAccrualFactors()[i]);
      for (int j = 0; j < NUM_OBS - 2; ++j) {
        assertEquals(DFN1.getFixingPeriodAccrualFactor()[i + 1][j], derivative5.getFixingPeriodAccrualFactor()[i][j]);
        assertEquals(TimeCalculator.getTimeBetween(refDate5, DFN1.getFixingDates()[i + 1][j]), derivative5.getFixingTime()[i][j]);
        assertEquals(TimeCalculator.getTimeBetween(refDate5, DFN1.getFixingPeriodEndDates()[i + 1][j]), derivative5.getFixingPeriodEndTime()[i][j]);
        assertEquals(TimeCalculator.getTimeBetween(refDate5, DFN1.getFixingPeriodStartDates()[i + 1][j]), derivative5.getFixingPeriodStartTime()[i][j]);
      }
    }
    for (int j = 0; j < NUM_OBS - 2; ++j) {
      assertEquals(DFN1.getFixingPeriodAccrualFactor()[1][j + 2], derivative5.getFixingPeriodAccrualFactor()[0][j]);
      assertEquals(TimeCalculator.getTimeBetween(refDate5, DFN1.getFixingDates()[1][j + 2]), derivative5.getFixingTime()[0][j]);
      assertEquals(TimeCalculator.getTimeBetween(refDate5, DFN1.getFixingPeriodEndDates()[1][j + 2]), derivative5.getFixingPeriodEndTime()[0][j]);
      assertEquals(TimeCalculator.getTimeBetween(refDate5, DFN1.getFixingPeriodStartDates()[1][j + 2]), derivative5.getFixingPeriodStartTime()[0][j]);
    }

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
    assertEquals(refRate, derivative5.getRateFixed(), 1.e-14);
  }

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
