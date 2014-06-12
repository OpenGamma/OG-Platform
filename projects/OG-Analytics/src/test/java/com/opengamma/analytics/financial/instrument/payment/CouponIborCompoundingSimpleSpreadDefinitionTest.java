/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.datasets.CalendarUSD;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingSimpleSpread;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the building of compounded Ibor coupons with spread: "Compounding treating spread as simple interest"
 */
public class CouponIborCompoundingSimpleSpreadDefinitionTest {

  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final IndexIborMaster MASTER_IBOR = IndexIborMaster.getInstance();
  private static final IborIndex USDLIBOR1M = MASTER_IBOR.getIndex("USDLIBOR1M");
  private static final BusinessDayConvention PREC = BusinessDayConventions.PRECEDING;

  private static final Period TENOR_3M = Period.ofMonths(3);
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2012, 8, 24);
  private static final ZonedDateTime END_DATE = START_DATE.plus(TENOR_3M);
  private static final double NOTIONAL = 123454321;
  private static final double SPREAD = 0.0010; // 10 bps

  private static final CouponIborCompoundingSimpleSpreadDefinition CPN_FROM_INDEX_DEFINITION =
      CouponIborCompoundingSimpleSpreadDefinition.from(NOTIONAL, START_DATE, END_DATE, USDLIBOR1M, SPREAD, StubType.SHORT_START, PREC, false, NYC);

  private static final ZonedDateTime[] ACCRUAL_END_DATES = ScheduleCalculator.getAdjustedDateSchedule(START_DATE, TENOR_3M, USDLIBOR1M.getTenor(), true, true, PREC, NYC, false);
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
  private static final ZonedDateTime[] FIXING_DATES = ScheduleCalculator.getAdjustedDate(ACCRUAL_START_DATES, -USDLIBOR1M.getSpotLag(), NYC);
  private static final ZonedDateTime[] FIXING_PERIOD_END_DATES = ScheduleCalculator.getAdjustedDate(ACCRUAL_START_DATES, USDLIBOR1M, NYC);
  private static final double[] FIXING_ACCRUAL_FACTORS = new double[NB_SUB_PERIOD];
  static {
    for (int loopsub = 0; loopsub < NB_SUB_PERIOD; loopsub++) {
      FIXING_ACCRUAL_FACTORS[loopsub] = USDLIBOR1M.getDayCount().getDayCountFraction(ACCRUAL_START_DATES[loopsub], FIXING_PERIOD_END_DATES[loopsub]);
    }
  }
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 8, 17);
  private static final double[] FIXING_TIMES = TimeCalculator.getTimeBetween(REFERENCE_DATE, FIXING_DATES);
  private static final double[] FIXING_PERIOD_END_TIMES = TimeCalculator.getTimeBetween(REFERENCE_DATE, FIXING_PERIOD_END_DATES);
  private static final double[] ACCRUAL_START_TIMES = TimeCalculator.getTimeBetween(REFERENCE_DATE, ACCRUAL_START_DATES);
  private static final double[] ACCRUAL_END_TIMES = TimeCalculator.getTimeBetween(REFERENCE_DATE, ACCRUAL_END_DATES);
  private static final double PAYMENT_TIME = ACCRUAL_END_TIMES[NB_SUB_PERIOD - 1];

  private static final double[] FIXING_RATES = new double[] {0.0010, 0.0011, 0.0012, 0.0013 };
  private static final DoubleTimeSeries<ZonedDateTime> FIXING_TS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {DateUtils.getUTCDate(2012, 8, 21), DateUtils.getUTCDate(2012, 8, 22),
        DateUtils.getUTCDate(2012, 9, 20), DateUtils.getUTCDate(2012, 10, 22) }, FIXING_RATES);

  private static final double TOLERANCE_AMOUNT = 1.0E-4;
  private static final double TOLERANCE_TIME = 1.0E-8;

  @Test
  public void from() {
    final CouponIborCompoundingSimpleSpreadDefinition cpnFromAccrualDates = CouponIborCompoundingSimpleSpreadDefinition.from(ACCRUAL_END_DATES[NB_SUB_PERIOD - 1], NOTIONAL, USDLIBOR1M,
        ACCRUAL_START_DATES, ACCRUAL_END_DATES, PAYMENT_ACCRUAL_FACTORS, SPREAD, NYC);
    assertEquals("CouponIborCompoundedDefinition: from", cpnFromAccrualDates, CPN_FROM_INDEX_DEFINITION);
    assertArrayEquals("CouponIborCompoundingFlatSpreadDefinition: getter", ACCRUAL_START_DATES, CPN_FROM_INDEX_DEFINITION.getSubperiodsAccrualStartDates());
    assertArrayEquals("CouponIborCompoundingFlatSpreadDefinition: getter", ACCRUAL_START_DATES, CPN_FROM_INDEX_DEFINITION.getFixingSubperiodStartDates());
    assertArrayEquals("CouponIborCompoundingFlatSpreadDefinition: getter", ACCRUAL_END_DATES, CPN_FROM_INDEX_DEFINITION.getSubperiodsAccrualEndDates());
    assertArrayEquals("CouponIborCompoundingFlatSpreadDefinition: getter", FIXING_DATES, CPN_FROM_INDEX_DEFINITION.getFixingDates());
    assertArrayEquals("CouponIborCompoundingFlatSpreadDefinition: getter", FIXING_PERIOD_END_DATES, CPN_FROM_INDEX_DEFINITION.getFixingSubperiodEndDates());
    assertEquals("CouponIborCompoundingFlatSpreadDefinition: getter", SPREAD, CPN_FROM_INDEX_DEFINITION.getSpread());
    int nbSubPeriod = CPN_FROM_INDEX_DEFINITION.getSubperiodsAccrualStartDates().length;
    for (int loops = 0; loops < nbSubPeriod; loops++) {
      assertEquals("CouponIborCompoundingFlatSpreadDefinition: dates - " + loops, CPN_FROM_INDEX_DEFINITION.getSubperiodsAccrualEndDates()[nbSubPeriod - 1 - loops],
          ScheduleCalculator.getAdjustedDate(END_DATE, Period.ofMonths(-loops), PREC, NYC, false));
      assertEquals("CouponIborCompoundingFlatSpreadDefinition: dates - " + loops, CPN_FROM_INDEX_DEFINITION.getFixingSubperiodEndDates()[loops],
          ScheduleCalculator.getAdjustedDate(CPN_FROM_INDEX_DEFINITION.getFixingSubperiodStartDates()[loops], USDLIBOR1M, NYC));
    }
  }

  @Test
  public void fromShortStub() {
    final ZonedDateTime startDate = DateUtils.getUTCDate(2012, 8, 7);
    final ZonedDateTime endDate = DateUtils.getUTCDate(2012, 11, 23);
    final CouponIborCompoundingFlatSpreadDefinition cpn = CouponIborCompoundingFlatSpreadDefinition.from(NOTIONAL, startDate, endDate, USDLIBOR1M, SPREAD, StubType.SHORT_START, PREC, true, NYC);
    assertEquals("CouponIborCompoundingFlatSpreadDefinition: from", startDate, cpn.getAccrualStartDate());
    assertEquals("CouponIborCompoundingFlatSpreadDefinition: from", cpn.getAccrualStartDate(), cpn.getSubperiodsAccrualStartDates()[0]);
    int nbSubPeriod = cpn.getSubperiodsAccrualStartDates().length;
    for (int loops = 0; loops < nbSubPeriod; loops++) {
      assertEquals("CouponIborCompoundingFlatSpreadDefinition: dates - " + loops, cpn.getSubperiodsAccrualEndDates()[nbSubPeriod - 1 - loops],
          ScheduleCalculator.getAdjustedDate(endDate, Period.ofMonths(-loops), PREC, NYC, false));
      assertEquals("CouponIborCompoundingFlatSpreadDefinition: dates - " + loops, cpn.getFixingSubperiodEndDates()[loops],
          ScheduleCalculator.getAdjustedDate(cpn.getFixingSubperiodStartDates()[loops], USDLIBOR1M, NYC));
    }
  }

  @Test
  public void getter() {
    assertEquals("CouponIborCompoundingFlatSpreadDefinition: getter", USDLIBOR1M, CPN_FROM_INDEX_DEFINITION.getIndex());
    assertEquals("CouponIborCompoundingFlatSpreadDefinition: getter", START_DATE, CPN_FROM_INDEX_DEFINITION.getAccrualStartDate());
    assertEquals("CouponIborCompoundingFlatSpreadDefinition: getter", START_DATE, CPN_FROM_INDEX_DEFINITION.getSubperiodsAccrualStartDates()[0]);
    assertEquals("CouponIborCompoundingFlatSpreadDefinition: getter", CPN_FROM_INDEX_DEFINITION.getPaymentDate(),
        CPN_FROM_INDEX_DEFINITION.getSubperiodsAccrualEndDates()[CPN_FROM_INDEX_DEFINITION.getSubperiodsAccrualEndDates().length - 1]);
    assertEquals("CouponIborCompoundingFlatSpreadDefinition: getter", SPREAD, CPN_FROM_INDEX_DEFINITION.getSpread());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongDate() {
    CPN_FROM_INDEX_DEFINITION.toDerivative(DateUtils.getUTCDate(2012, 8, 25));
  }

  @Test
  public void toDerivativeNoFixing() {
    final CouponIborCompoundingSimpleSpread cpnConverted = CPN_FROM_INDEX_DEFINITION.toDerivative(REFERENCE_DATE);
    final CouponIborCompoundingSimpleSpread cpnExpected = new CouponIborCompoundingSimpleSpread(USDLIBOR1M.getCurrency(), PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, NOTIONAL, USDLIBOR1M,
        PAYMENT_ACCRUAL_FACTORS, FIXING_TIMES, ACCRUAL_START_TIMES, FIXING_PERIOD_END_TIMES, FIXING_ACCRUAL_FACTORS, SPREAD);
    assertEquals("CouponIborCompoundingSimpleSpreadDefinition: toDerivatives", cpnExpected, cpnConverted);
    final Coupon cpnConverted2 = CPN_FROM_INDEX_DEFINITION.toDerivative(REFERENCE_DATE, FIXING_TS);
    assertEquals("CouponIborCompoundingSimpleSpreadDefinition: toDerivative", cpnExpected, cpnConverted2);
  }

  @Test
  public void toDerivativeAfter1Fixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 8, 28);
    double cpa = NOTIONAL * (1.0d + FIXING_RATES[1] * PAYMENT_ACCRUAL_FACTORS[0]);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, CPN_FROM_INDEX_DEFINITION.getPaymentDate());
    final double[] paymentAccrualFactorsLeft = new double[NB_SUB_PERIOD - 1];
    System.arraycopy(PAYMENT_ACCRUAL_FACTORS, 1, paymentAccrualFactorsLeft, 0, NB_SUB_PERIOD - 1);
    final double[] fixingTimesLeft = new double[NB_SUB_PERIOD - 1];
    System.arraycopy(TimeCalculator.getTimeBetween(referenceDate, FIXING_DATES), 1, fixingTimesLeft, 0, NB_SUB_PERIOD - 1);
    final double[] fixingPeriodStartTimesLeft = new double[NB_SUB_PERIOD - 1];
    System.arraycopy(TimeCalculator.getTimeBetween(referenceDate, ACCRUAL_START_DATES), 1, fixingPeriodStartTimesLeft, 0, NB_SUB_PERIOD - 1);
    final double[] fixingPeriodEndTimesLeft = new double[NB_SUB_PERIOD - 1];
    System.arraycopy(TimeCalculator.getTimeBetween(referenceDate, FIXING_PERIOD_END_DATES), 1, fixingPeriodEndTimesLeft, 0, NB_SUB_PERIOD - 1);
    final double[] fixingPeriodAccrualFactorsLeft = new double[NB_SUB_PERIOD - 1];
    System.arraycopy(FIXING_ACCRUAL_FACTORS, 1, fixingPeriodAccrualFactorsLeft, 0, NB_SUB_PERIOD - 1);
    final Coupon cpnConverted = CPN_FROM_INDEX_DEFINITION.toDerivative(referenceDate, FIXING_TS);
    final CouponIborCompoundingSimpleSpread cpnExpected = new CouponIborCompoundingSimpleSpread(USDLIBOR1M.getCurrency(), paymentTime, PAYMENT_ACCRUAL_FACTOR,
        NOTIONAL, cpa, USDLIBOR1M, paymentAccrualFactorsLeft, fixingTimesLeft, fixingPeriodStartTimesLeft, fixingPeriodEndTimesLeft,
        fixingPeriodAccrualFactorsLeft, SPREAD);
    assertEquals("CouponIborCompoundingSimpleSpreadDefinition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  public void toDerivativeAfter2Fixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 9, 20);
    double cpa = NOTIONAL * (1.0d + FIXING_RATES[1] * PAYMENT_ACCRUAL_FACTORS[0]);
    cpa *= 1.0d + FIXING_RATES[2] * PAYMENT_ACCRUAL_FACTORS[1];
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, CPN_FROM_INDEX_DEFINITION.getPaymentDate());
    final double[] paymentAccrualFactorsLeft = new double[] {PAYMENT_ACCRUAL_FACTORS[2] };
    final double[] fixingTimesLeft = new double[] {TimeCalculator.getTimeBetween(referenceDate, FIXING_DATES[2]) };
    final double[] fixingPeriodStartTimesLeft = new double[] {TimeCalculator.getTimeBetween(referenceDate, ACCRUAL_START_DATES[2]) };
    final double[] fixingPeriodEndTimesLeft = new double[] {TimeCalculator.getTimeBetween(referenceDate, FIXING_PERIOD_END_DATES[2]) };
    final double[] fixingPeriodAccrualFactorsLeft = new double[] {FIXING_ACCRUAL_FACTORS[2] };
    final CouponIborCompoundingSimpleSpread cpnConverted = (CouponIborCompoundingSimpleSpread) CPN_FROM_INDEX_DEFINITION.toDerivative(referenceDate, FIXING_TS);
    final CouponIborCompoundingSimpleSpread cpnExpected = new CouponIborCompoundingSimpleSpread(USDLIBOR1M.getCurrency(), paymentTime, PAYMENT_ACCRUAL_FACTOR,
        NOTIONAL, cpa, USDLIBOR1M, paymentAccrualFactorsLeft, fixingTimesLeft, fixingPeriodStartTimesLeft, fixingPeriodEndTimesLeft,
        fixingPeriodAccrualFactorsLeft, SPREAD);
    assertEquals("CouponIborCompoundingSimpleSpreadDefinition: toDerivative", cpnExpected.getCompoundingPeriodAmountAccumulated(), cpnConverted.getCompoundingPeriodAmountAccumulated(),
        TOLERANCE_AMOUNT);
    assertArrayEquals("CouponIborCompoundingSimpleSpreadDefinition: toDerivative", cpnExpected.getFixingPeriodStartTimes(), cpnConverted.getFixingPeriodStartTimes(), TOLERANCE_TIME);
    assertArrayEquals("CouponIborCompoundingSimpleSpreadDefinition: toDerivative", cpnExpected.getFixingPeriodEndTimes(), cpnConverted.getFixingPeriodEndTimes(), TOLERANCE_TIME);
  }

  @Test
  public void toDerivativeAfterLastFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 10, 25);
    final Coupon cpnConverted = CPN_FROM_INDEX_DEFINITION.toDerivative(referenceDate, FIXING_TS);
    double cpa = 1.0d + FIXING_RATES[1] * PAYMENT_ACCRUAL_FACTORS[0];
    cpa *= 1.0d + FIXING_RATES[2] * PAYMENT_ACCRUAL_FACTORS[1];
    cpa *= 1.0d + FIXING_RATES[3] * PAYMENT_ACCRUAL_FACTORS[2];
    final double rate = (cpa - 1.0d) / PAYMENT_ACCRUAL_FACTOR + SPREAD;
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, CPN_FROM_INDEX_DEFINITION.getPaymentDate());
    final CouponFixed cpnExpected = new CouponFixed(USDLIBOR1M.getCurrency(), paymentTime, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, rate,
        ACCRUAL_START_DATES[0], ACCRUAL_END_DATES[NB_SUB_PERIOD - 1]);
    assertEquals("CouponIborCompoundingSimpleSpreadDefinition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  public void toDerivativeAfter1FixingInitialRate() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 8, 28);
    final double initialRate = 0.002;
    double cpa = NOTIONAL * (1.0d + initialRate * PAYMENT_ACCRUAL_FACTORS[0]);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, CPN_FROM_INDEX_DEFINITION.getPaymentDate());
    final double[] paymentAccrualFactorsLeft = new double[NB_SUB_PERIOD - 1];
    System.arraycopy(PAYMENT_ACCRUAL_FACTORS, 1, paymentAccrualFactorsLeft, 0, NB_SUB_PERIOD - 1);
    final double[] fixingTimesLeft = new double[NB_SUB_PERIOD - 1];
    System.arraycopy(TimeCalculator.getTimeBetween(referenceDate, FIXING_DATES), 1, fixingTimesLeft, 0, NB_SUB_PERIOD - 1);
    final double[] fixingPeriodStartTimesLeft = new double[NB_SUB_PERIOD - 1];
    System.arraycopy(TimeCalculator.getTimeBetween(referenceDate, ACCRUAL_START_DATES), 1, fixingPeriodStartTimesLeft, 0, NB_SUB_PERIOD - 1);
    final double[] fixingPeriodEndTimesLeft = new double[NB_SUB_PERIOD - 1];
    System.arraycopy(TimeCalculator.getTimeBetween(referenceDate, FIXING_PERIOD_END_DATES), 1, fixingPeriodEndTimesLeft, 0, NB_SUB_PERIOD - 1);
    final double[] fixingPeriodAccrualFactorsLeft = new double[NB_SUB_PERIOD - 1];
    System.arraycopy(FIXING_ACCRUAL_FACTORS, 1, fixingPeriodAccrualFactorsLeft, 0, NB_SUB_PERIOD - 1);
    final Coupon cpnConverted = CouponIborCompoundingSimpleSpreadDefinition.from(
        CPN_FROM_INDEX_DEFINITION.getCurrency(),
        CPN_FROM_INDEX_DEFINITION.getPaymentDate(),
        CPN_FROM_INDEX_DEFINITION.getAccrualStartDate(),
        CPN_FROM_INDEX_DEFINITION.getAccrualEndDate(),
        CPN_FROM_INDEX_DEFINITION.getPaymentYearFraction(),
        CPN_FROM_INDEX_DEFINITION.getNotional(),
        CPN_FROM_INDEX_DEFINITION.getIndex(),
        CPN_FROM_INDEX_DEFINITION.getSubperiodsAccrualStartDates(),
        CPN_FROM_INDEX_DEFINITION.getSubperiodsAccrualEndDates(),
        CPN_FROM_INDEX_DEFINITION.getSubperiodsAccrualFactors(),
        CPN_FROM_INDEX_DEFINITION.getFixingDates(),
        CPN_FROM_INDEX_DEFINITION.getFixingSubperiodStartDates(),
        CPN_FROM_INDEX_DEFINITION.getFixingSubperiodEndDates(),
        CPN_FROM_INDEX_DEFINITION.getFixingSubperiodAccrualFactors(),
        CPN_FROM_INDEX_DEFINITION.getSpread(),
        initialRate).toDerivative(referenceDate, FIXING_TS);
    final CouponIborCompoundingSimpleSpread cpnExpected = new CouponIborCompoundingSimpleSpread(USDLIBOR1M.getCurrency(), paymentTime, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, cpa, USDLIBOR1M,
        paymentAccrualFactorsLeft, fixingTimesLeft, fixingPeriodStartTimesLeft, fixingPeriodEndTimesLeft, fixingPeriodAccrualFactorsLeft, SPREAD);
    assertEquals("CouponIborCompoundingSimpleSpreadDefinition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  public void toDerivativeAfter2FixingInitialRate() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 9, 20);
    final double initialRate = 0.002;
    double cpa = 1.0d + initialRate * PAYMENT_ACCRUAL_FACTORS[0];
    cpa *= 1.0d + FIXING_RATES[2] * PAYMENT_ACCRUAL_FACTORS[1];
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, CPN_FROM_INDEX_DEFINITION.getPaymentDate());
    final double[] paymentAccrualFactorsLeft = new double[] {PAYMENT_ACCRUAL_FACTORS[2] };
    final double[] fixingTimesLeft = new double[] {TimeCalculator.getTimeBetween(referenceDate, FIXING_DATES[2]) };
    final double[] fixingPeriodStartTimesLeft = new double[] {TimeCalculator.getTimeBetween(referenceDate, ACCRUAL_START_DATES[2]) };
    final double[] fixingPeriodEndTimesLeft = new double[] {TimeCalculator.getTimeBetween(referenceDate, FIXING_PERIOD_END_DATES[2]) };
    final double[] fixingPeriodAccrualFactorsLeft = new double[] {FIXING_ACCRUAL_FACTORS[2] };
    final Coupon cpnConverted = CouponIborCompoundingSimpleSpreadDefinition.from(
        CPN_FROM_INDEX_DEFINITION.getCurrency(),
        CPN_FROM_INDEX_DEFINITION.getPaymentDate(),
        CPN_FROM_INDEX_DEFINITION.getAccrualStartDate(),
        CPN_FROM_INDEX_DEFINITION.getAccrualEndDate(),
        CPN_FROM_INDEX_DEFINITION.getPaymentYearFraction(),
        CPN_FROM_INDEX_DEFINITION.getNotional(),
        CPN_FROM_INDEX_DEFINITION.getIndex(),
        CPN_FROM_INDEX_DEFINITION.getSubperiodsAccrualStartDates(),
        CPN_FROM_INDEX_DEFINITION.getSubperiodsAccrualEndDates(),
        CPN_FROM_INDEX_DEFINITION.getSubperiodsAccrualFactors(),
        CPN_FROM_INDEX_DEFINITION.getFixingDates(),
        CPN_FROM_INDEX_DEFINITION.getFixingSubperiodStartDates(),
        CPN_FROM_INDEX_DEFINITION.getFixingSubperiodEndDates(),
        CPN_FROM_INDEX_DEFINITION.getFixingSubperiodAccrualFactors(),
        CPN_FROM_INDEX_DEFINITION.getSpread(),
        initialRate).toDerivative(referenceDate, FIXING_TS);
    final CouponIborCompoundingSimpleSpread cpnExpected = new CouponIborCompoundingSimpleSpread(USDLIBOR1M.getCurrency(), paymentTime, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, NOTIONAL * cpa, USDLIBOR1M,
        paymentAccrualFactorsLeft, fixingTimesLeft, fixingPeriodStartTimesLeft, fixingPeriodEndTimesLeft, fixingPeriodAccrualFactorsLeft, SPREAD);
    assertEquals("CouponIborCompoundingSimpleSpreadDefinition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  public void toDerivativeAfterLastFixingInitialRate() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 10, 25);
    final double initialRate = 0.002;
    final Coupon cpnConverted = CouponIborCompoundingSimpleSpreadDefinition.from(
        CPN_FROM_INDEX_DEFINITION.getCurrency(),
        CPN_FROM_INDEX_DEFINITION.getPaymentDate(),
        CPN_FROM_INDEX_DEFINITION.getAccrualStartDate(),
        CPN_FROM_INDEX_DEFINITION.getAccrualEndDate(),
        CPN_FROM_INDEX_DEFINITION.getPaymentYearFraction(),
        CPN_FROM_INDEX_DEFINITION.getNotional(),
        CPN_FROM_INDEX_DEFINITION.getIndex(),
        CPN_FROM_INDEX_DEFINITION.getSubperiodsAccrualStartDates(),
        CPN_FROM_INDEX_DEFINITION.getSubperiodsAccrualEndDates(),
        CPN_FROM_INDEX_DEFINITION.getSubperiodsAccrualFactors(),
        CPN_FROM_INDEX_DEFINITION.getFixingDates(),
        CPN_FROM_INDEX_DEFINITION.getFixingSubperiodStartDates(),
        CPN_FROM_INDEX_DEFINITION.getFixingSubperiodEndDates(),
        CPN_FROM_INDEX_DEFINITION.getFixingSubperiodAccrualFactors(),
        CPN_FROM_INDEX_DEFINITION.getSpread(),
        initialRate).toDerivative(referenceDate, FIXING_TS);
    double cpa = 1.0d + initialRate * PAYMENT_ACCRUAL_FACTORS[0];
    cpa *= 1.0d + FIXING_RATES[2] * PAYMENT_ACCRUAL_FACTORS[1];
    cpa *= 1.0d + FIXING_RATES[3] * PAYMENT_ACCRUAL_FACTORS[2];
    final double rate = (cpa - 1.0d) / PAYMENT_ACCRUAL_FACTOR + SPREAD;
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, CPN_FROM_INDEX_DEFINITION.getPaymentDate());
    final CouponFixed cpnExpected = new CouponFixed(USDLIBOR1M.getCurrency(), paymentTime, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, rate,
        ACCRUAL_START_DATES[0], ACCRUAL_END_DATES[NB_SUB_PERIOD - 1]);
    assertEquals("CouponIborCompoundingSimpleSpreadDefinition: toDerivative", cpnExpected, cpnConverted);
  }

}
