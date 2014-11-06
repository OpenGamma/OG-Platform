/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompounding;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the building of compounded Ibor coupons.
 */
@Test(groups = TestGroup.UNIT)
public class CouponIborCompoundingDefinitionTest {

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final IndexIborMaster MASTER_IBOR = IndexIborMaster.getInstance();
  private static final IborIndex USDLIBOR1M = MASTER_IBOR.getIndex("USDLIBOR1M");
  private static final BusinessDayConvention PREC = BusinessDayConventions.PRECEDING;

  private static final Period TENOR_3M = Period.ofMonths(3);
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2012, 8, 24);
  private static final double NOTIONAL = 123454321;

  private static final CouponIborCompoundingDefinition CPN_FROM_INDEX_DEFINITION = CouponIborCompoundingDefinition.from(NOTIONAL, START_DATE, TENOR_3M, USDLIBOR1M, NYC);
  
  private static final CouponIborCompoundingDefinition CPN_FROM_INDEX_DEFINITON_INITIAL_RATE =
      CouponIborCompoundingDefinition.from(
          USDLIBOR1M.getCurrency(),
          CPN_FROM_INDEX_DEFINITION.getPaymentDate(),
          CPN_FROM_INDEX_DEFINITION.getAccrualStartDate(),
          CPN_FROM_INDEX_DEFINITION.getAccrualEndDate(),
          CPN_FROM_INDEX_DEFINITION.getPaymentYearFraction(),
          CPN_FROM_INDEX_DEFINITION.getNotional(),
          CPN_FROM_INDEX_DEFINITION.getIndex(),
          CPN_FROM_INDEX_DEFINITION.getAccrualStartDates(),
          CPN_FROM_INDEX_DEFINITION.getAccrualEndDates(),
          CPN_FROM_INDEX_DEFINITION.getPaymentAccrualFactors(),
          CPN_FROM_INDEX_DEFINITION.getFixingDates(),
          CPN_FROM_INDEX_DEFINITION.getFixingPeriodStartDates(),
          CPN_FROM_INDEX_DEFINITION.getFixingPeriodEndDates(),
          CPN_FROM_INDEX_DEFINITION.getFixingPeriodAccrualFactors(),
          0.002);

  private static final ZonedDateTime[] ACCRUAL_END_DATES = ScheduleCalculator.getAdjustedDateSchedule(START_DATE, TENOR_3M, true, false, USDLIBOR1M, NYC);
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
  private static final String DSC_NAME = "Dsc_USD";
  private static final String FWD_NAME = "Forward1M_USD";

  private static final double[] FIXING_RATES = new double[] {0.0010, 0.0011, 0.0012, 0.0013 };
  private static final DoubleTimeSeries<ZonedDateTime> FIXING_TS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {DateUtils.getUTCDate(2012, 8, 21), DateUtils.getUTCDate(2012, 8, 22),
        DateUtils.getUTCDate(2012, 9, 20), DateUtils.getUTCDate(2012, 10, 22) }, FIXING_RATES);

  @Test
  public void from() {
    final CouponIborCompoundingDefinition cpnFromAccrualDates = CouponIborCompoundingDefinition.from(ACCRUAL_END_DATES[NB_SUB_PERIOD - 1], NOTIONAL, USDLIBOR1M, ACCRUAL_START_DATES,
        ACCRUAL_END_DATES,
        PAYMENT_ACCRUAL_FACTORS, NYC);
    assertEquals("CouponIborCompoundedDefinition: from", cpnFromAccrualDates, CPN_FROM_INDEX_DEFINITION);
    assertArrayEquals("CouponIborCompoundedDefinition: getter", ACCRUAL_START_DATES, CPN_FROM_INDEX_DEFINITION.getAccrualStartDates());
    assertArrayEquals("CouponIborCompoundedDefinition: getter", ACCRUAL_START_DATES, CPN_FROM_INDEX_DEFINITION.getFixingPeriodStartDates());
    assertArrayEquals("CouponIborCompoundedDefinition: getter", ACCRUAL_END_DATES, CPN_FROM_INDEX_DEFINITION.getAccrualEndDates());
    assertArrayEquals("CouponIborCompoundedDefinition: getter", FIXING_DATES, CPN_FROM_INDEX_DEFINITION.getFixingDates());
    assertArrayEquals("CouponIborCompoundedDefinition: getter", FIXING_PERIOD_END_DATES, CPN_FROM_INDEX_DEFINITION.getFixingPeriodEndDates());
  }

  @Test
  public void fromShortStub() {
    final ZonedDateTime startDate = DateUtils.getUTCDate(2012, 8, 7);
    final ZonedDateTime endDate = DateUtils.getUTCDate(2012, 11, 23);
    final CouponIborCompoundingDefinition cpn = CouponIborCompoundingDefinition.from(NOTIONAL, startDate, endDate, USDLIBOR1M, StubType.SHORT_START, PREC, true, NYC);
    assertEquals("CouponIborCompoundedSpreadDefinition: from", startDate, cpn.getAccrualStartDate());
    assertEquals("CouponIborCompoundedSpreadDefinition: from", cpn.getAccrualStartDate(), cpn.getAccrualStartDates()[0]);
    int nbSubPeriod = cpn.getAccrualStartDates().length;
    for (int loops = 0; loops < nbSubPeriod; loops++) {
      assertEquals("CouponIborCompoundedSpreadDefinition: dates - " + loops, cpn.getAccrualEndDates()[nbSubPeriod - 1 - loops],
          ScheduleCalculator.getAdjustedDate(endDate, Period.ofMonths(-loops), PREC, NYC, false));
      assertEquals("CouponIborCompoundedSpreadDefinition: dates - " + loops, cpn.getFixingPeriodEndDates()[loops],
          ScheduleCalculator.getAdjustedDate(cpn.getFixingPeriodStartDates()[loops], USDLIBOR1M, NYC));
    }
  }

  @Test
  public void getter() {
    assertEquals("CouponIborCompoundedDefinition: getter", USDLIBOR1M, CPN_FROM_INDEX_DEFINITION.getIndex());
    assertEquals("CouponIborCompoundedDefinition: getter", START_DATE, CPN_FROM_INDEX_DEFINITION.getAccrualStartDate());
    assertEquals("CouponIborCompoundedDefinition: getter", START_DATE, CPN_FROM_INDEX_DEFINITION.getAccrualStartDates()[0]);
    assertEquals("CouponIborCompoundedDefinition: getter", CPN_FROM_INDEX_DEFINITION.getPaymentDate(),
        CPN_FROM_INDEX_DEFINITION.getAccrualEndDates()[CPN_FROM_INDEX_DEFINITION.getAccrualEndDates().length - 1]);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongDate() {
    CPN_FROM_INDEX_DEFINITION.toDerivative(DateUtils.getUTCDate(2012, 8, 25));
  }

  @Test
  public void toDerivativeNoTS() {
    final CouponIborCompounding cpnConverted = CPN_FROM_INDEX_DEFINITION.toDerivative(REFERENCE_DATE);
    final CouponIborCompounding cpnExpected = new CouponIborCompounding(USDLIBOR1M.getCurrency(), PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, NOTIONAL, USDLIBOR1M, PAYMENT_ACCRUAL_FACTORS,
        FIXING_TIMES, ACCRUAL_START_TIMES, FIXING_PERIOD_END_TIMES, FIXING_ACCRUAL_FACTORS);
    assertEquals("CouponIborCompoundedDefinition: toDerivatives", cpnExpected, cpnConverted);
    final Coupon cpnConverted2 = CPN_FROM_INDEX_DEFINITION.toDerivative(REFERENCE_DATE, FIXING_TS);
    assertEquals("CouponIborCompoundedDefinition: toDerivatives", cpnExpected, cpnConverted2);
  }

  @Test
  public void toDerivativeAfter1Fixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 8, 28);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, CPN_FROM_INDEX_DEFINITION.getPaymentDate());
    final double accruedNotional = (1.0 + PAYMENT_ACCRUAL_FACTORS[0] * FIXING_RATES[1]) * NOTIONAL;
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
    final CouponIborCompounding cpnExpected = new CouponIborCompounding(USDLIBOR1M.getCurrency(), paymentTime, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, accruedNotional, USDLIBOR1M,
        paymentAccrualFactorsLeft, fixingTimesLeft, fixingPeriodStartTimesLeft, fixingPeriodEndTimesLeft, fixingPeriodAccrualFactorsLeft);
    assertEquals("CouponIborCompoundedDefinition: toDerivatives", cpnExpected, cpnConverted);
  }

  @Test
  public void toDerivativeAfter2Fixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 9, 20);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, CPN_FROM_INDEX_DEFINITION.getPaymentDate());
    final double accruedNotional = (1.0 + PAYMENT_ACCRUAL_FACTORS[0] * FIXING_RATES[1]) * (1.0 + PAYMENT_ACCRUAL_FACTORS[1] * FIXING_RATES[2]) * NOTIONAL;
    final double[] paymentAccrualFactorsLeft = new double[] {PAYMENT_ACCRUAL_FACTORS[2] };
    final double[] fixingTimesLeft = new double[] {TimeCalculator.getTimeBetween(referenceDate, FIXING_DATES[2]) };
    final double[] fixingPeriodStartTimesLeft = new double[] {TimeCalculator.getTimeBetween(referenceDate, ACCRUAL_START_DATES[2]) };
    final double[] fixingPeriodEndTimesLeft = new double[] {TimeCalculator.getTimeBetween(referenceDate, FIXING_PERIOD_END_DATES[2]) };
    final double[] fixingPeriodAccrualFactorsLeft = new double[] {FIXING_ACCRUAL_FACTORS[2] };
    final Coupon cpnConverted = CPN_FROM_INDEX_DEFINITION.toDerivative(referenceDate, FIXING_TS);
    final CouponIborCompounding cpnExpected = new CouponIborCompounding(USDLIBOR1M.getCurrency(), paymentTime, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, accruedNotional, USDLIBOR1M,
        paymentAccrualFactorsLeft, fixingTimesLeft, fixingPeriodStartTimesLeft, fixingPeriodEndTimesLeft, fixingPeriodAccrualFactorsLeft);
    assertEquals("CouponIborCompoundedDefinition: toDerivatives", cpnExpected, cpnConverted);
  }

  @Test
  public void toDerivativeAfterLastFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 10, 25);
    final Coupon cpnConverted = CPN_FROM_INDEX_DEFINITION.toDerivative(referenceDate, FIXING_TS);
    final double rate = ((1.0 + PAYMENT_ACCRUAL_FACTORS[0] * FIXING_RATES[1]) * (1.0 + PAYMENT_ACCRUAL_FACTORS[1] * FIXING_RATES[2]) * (1.0 + PAYMENT_ACCRUAL_FACTORS[2] * FIXING_RATES[3]) - 1.0)
        / PAYMENT_ACCRUAL_FACTOR;
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, CPN_FROM_INDEX_DEFINITION.getPaymentDate());
    final CouponFixed cpnExpected = new CouponFixed(USDLIBOR1M.getCurrency(), paymentTime, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, rate, ACCRUAL_START_DATES[0], ACCRUAL_END_DATES[NB_SUB_PERIOD - 1]);
    assertEquals("CouponIborCompoundedDefinition: toDerivatives", cpnExpected, cpnConverted);
  }

  @Test
  public void toDerivativeAfter1FixingInitialRate() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 8, 28);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, CPN_FROM_INDEX_DEFINITION.getPaymentDate());
    final double accruedNotional = (1.0 + PAYMENT_ACCRUAL_FACTORS[0] * CPN_FROM_INDEX_DEFINITON_INITIAL_RATE.getInitialRate()) * NOTIONAL;
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
    final Coupon cpnConverted = CPN_FROM_INDEX_DEFINITON_INITIAL_RATE.toDerivative(referenceDate, FIXING_TS);
    final CouponIborCompounding cpnExpected = new CouponIborCompounding(USDLIBOR1M.getCurrency(), paymentTime, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, accruedNotional, USDLIBOR1M,
        paymentAccrualFactorsLeft, fixingTimesLeft, fixingPeriodStartTimesLeft, fixingPeriodEndTimesLeft, fixingPeriodAccrualFactorsLeft);
    assertEquals("CouponIborCompoundedDefinition: toDerivatives", cpnExpected, cpnConverted);
  }

  @Test
  public void toDerivativeAfter2FixingInitialRate() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 9, 20);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, CPN_FROM_INDEX_DEFINITION.getPaymentDate());
    final double accruedNotional = (1.0 + PAYMENT_ACCRUAL_FACTORS[0] * CPN_FROM_INDEX_DEFINITON_INITIAL_RATE.getInitialRate()) * (1.0 + PAYMENT_ACCRUAL_FACTORS[1] * FIXING_RATES[2]) * NOTIONAL;
    final double[] paymentAccrualFactorsLeft = new double[] {PAYMENT_ACCRUAL_FACTORS[2] };
    final double[] fixingTimesLeft = new double[] {TimeCalculator.getTimeBetween(referenceDate, FIXING_DATES[2]) };
    final double[] fixingPeriodStartTimesLeft = new double[] {TimeCalculator.getTimeBetween(referenceDate, ACCRUAL_START_DATES[2]) };
    final double[] fixingPeriodEndTimesLeft = new double[] {TimeCalculator.getTimeBetween(referenceDate, FIXING_PERIOD_END_DATES[2]) };
    final double[] fixingPeriodAccrualFactorsLeft = new double[] {FIXING_ACCRUAL_FACTORS[2] };
    final Coupon cpnConverted = CPN_FROM_INDEX_DEFINITON_INITIAL_RATE.toDerivative(referenceDate, FIXING_TS);
    final CouponIborCompounding cpnExpected = new CouponIborCompounding(USDLIBOR1M.getCurrency(), paymentTime, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, accruedNotional, USDLIBOR1M,
        paymentAccrualFactorsLeft, fixingTimesLeft, fixingPeriodStartTimesLeft, fixingPeriodEndTimesLeft, fixingPeriodAccrualFactorsLeft);
    assertEquals("CouponIborCompoundedDefinition: toDerivatives", cpnExpected, cpnConverted);
  }
  
  @Test
  public void toDerivativeAfterLastFixingInitialRate() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 10, 25);
    final Coupon cpnConverted = CPN_FROM_INDEX_DEFINITON_INITIAL_RATE.toDerivative(referenceDate, FIXING_TS);
    final double rate = ((1.0 + PAYMENT_ACCRUAL_FACTORS[0] * CPN_FROM_INDEX_DEFINITON_INITIAL_RATE.getInitialRate()) * (1.0 + PAYMENT_ACCRUAL_FACTORS[1] * FIXING_RATES[2]) * (1.0 + PAYMENT_ACCRUAL_FACTORS[2] * FIXING_RATES[3]) - 1.0)
        / PAYMENT_ACCRUAL_FACTOR;
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, CPN_FROM_INDEX_DEFINITION.getPaymentDate());
    final CouponFixed cpnExpected = new CouponFixed(USDLIBOR1M.getCurrency(), paymentTime, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, rate, ACCRUAL_START_DATES[0], ACCRUAL_END_DATES[NB_SUB_PERIOD - 1]);
    assertEquals("CouponIborCompoundedDefinition: toDerivatives", cpnExpected, cpnConverted);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void toDerivativeAfter1FixingDeprecated() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 8, 28);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, CPN_FROM_INDEX_DEFINITION.getPaymentDate());
    final double accruedNotional = (1.0 + PAYMENT_ACCRUAL_FACTORS[0] * FIXING_RATES[1]) * NOTIONAL;
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
    final Coupon cpnConverted = CPN_FROM_INDEX_DEFINITION.toDerivative(referenceDate, FIXING_TS, new String[] {DSC_NAME, FWD_NAME });
    final CouponIborCompounding cpnExpected = new CouponIborCompounding(USDLIBOR1M.getCurrency(), paymentTime, DSC_NAME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, accruedNotional, USDLIBOR1M,
        paymentAccrualFactorsLeft, fixingTimesLeft, fixingPeriodStartTimesLeft, fixingPeriodEndTimesLeft, fixingPeriodAccrualFactorsLeft, FWD_NAME);
    assertEquals("CouponIborCompoundedDefinition: toDerivatives", cpnExpected, cpnConverted);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void toDerivativeAfter2FixingDeprecated() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 9, 20);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, CPN_FROM_INDEX_DEFINITION.getPaymentDate());
    final double accruedNotional = (1.0 + PAYMENT_ACCRUAL_FACTORS[0] * FIXING_RATES[1]) * (1.0 + PAYMENT_ACCRUAL_FACTORS[1] * FIXING_RATES[2]) * NOTIONAL;
    final double[] paymentAccrualFactorsLeft = new double[] {PAYMENT_ACCRUAL_FACTORS[2] };
    final double[] fixingTimesLeft = new double[] {TimeCalculator.getTimeBetween(referenceDate, FIXING_DATES[2]) };
    final double[] fixingPeriodStartTimesLeft = new double[] {TimeCalculator.getTimeBetween(referenceDate, ACCRUAL_START_DATES[2]) };
    final double[] fixingPeriodEndTimesLeft = new double[] {TimeCalculator.getTimeBetween(referenceDate, FIXING_PERIOD_END_DATES[2]) };
    final double[] fixingPeriodAccrualFactorsLeft = new double[] {FIXING_ACCRUAL_FACTORS[2] };
    final Coupon cpnConverted = CPN_FROM_INDEX_DEFINITION.toDerivative(referenceDate, FIXING_TS, new String[] {DSC_NAME, FWD_NAME });
    final CouponIborCompounding cpnExpected = new CouponIborCompounding(USDLIBOR1M.getCurrency(), paymentTime, DSC_NAME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, accruedNotional, USDLIBOR1M,
        paymentAccrualFactorsLeft, fixingTimesLeft, fixingPeriodStartTimesLeft, fixingPeriodEndTimesLeft, fixingPeriodAccrualFactorsLeft, FWD_NAME);
    assertEquals("CouponIborCompoundedDefinition: toDerivatives", cpnExpected, cpnConverted);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void toDerivativeAfterLastFixingDeprecated() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 10, 25);
    final Coupon cpnConverted = CPN_FROM_INDEX_DEFINITION.toDerivative(referenceDate, FIXING_TS, new String[] {DSC_NAME, FWD_NAME });
    final double rate = ((1.0 + PAYMENT_ACCRUAL_FACTORS[0] * FIXING_RATES[1]) * (1.0 + PAYMENT_ACCRUAL_FACTORS[1] * FIXING_RATES[2]) * (1.0 + PAYMENT_ACCRUAL_FACTORS[2] * FIXING_RATES[3]) - 1.0)
        / PAYMENT_ACCRUAL_FACTOR;
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, CPN_FROM_INDEX_DEFINITION.getPaymentDate());
    final CouponFixed cpnExpected = new CouponFixed(USDLIBOR1M.getCurrency(), paymentTime, DSC_NAME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, rate, ACCRUAL_START_DATES[0],
        ACCRUAL_END_DATES[NB_SUB_PERIOD - 1]);
    assertEquals("CouponIborCompoundedDefinition: toDerivatives", cpnExpected, cpnConverted);
  }
}
