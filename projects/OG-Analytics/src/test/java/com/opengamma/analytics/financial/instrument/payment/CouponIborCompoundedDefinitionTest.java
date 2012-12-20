/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompounded;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;

/**
 * Tests related to the building of compounded Ibor coupons.
 */
public class CouponIborCompoundedDefinitionTest {

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final IndexIborMaster MASTER_IBOR = IndexIborMaster.getInstance();
  private static final IborIndex USDLIBOR1M = MASTER_IBOR.getIndex("USDLIBOR1M", NYC);

  private static final Period TENOR_3M = Period.ofMonths(3);
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2012, 8, 24);
  private static final double NOTIONAL = 123454321;

  private static final CouponIborCompoundedDefinition CPN_FROM_INDEX_DEFINITION = CouponIborCompoundedDefinition.from(NOTIONAL, START_DATE, TENOR_3M, USDLIBOR1M);

  private static final ZonedDateTime[] ACCRUAL_END_DATES = ScheduleCalculator.getAdjustedDateSchedule(START_DATE, TENOR_3M, true, false, USDLIBOR1M);
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
  private static final ZonedDateTime[] FIXING_PERIOD_END_DATES = ScheduleCalculator.getAdjustedDate(ACCRUAL_START_DATES, USDLIBOR1M);
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

  private static final double[] FIXING_RATES = new double[] {0.0010, 0.0011, 0.0012, 0.0013};
  private static final DoubleTimeSeries<ZonedDateTime> FIXING_TS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2012, 8, 21), DateUtils.getUTCDate(2012, 8, 22),
      DateUtils.getUTCDate(2012, 9, 20), DateUtils.getUTCDate(2012, 10, 22)}, FIXING_RATES);

  @Test
  public void from() {
    CouponIborCompoundedDefinition cpnFromAccrualDates = CouponIborCompoundedDefinition.from(ACCRUAL_END_DATES[NB_SUB_PERIOD - 1], NOTIONAL, USDLIBOR1M, ACCRUAL_START_DATES, ACCRUAL_END_DATES,
        PAYMENT_ACCRUAL_FACTORS);
    assertEquals("CouponIborCompoundedDefinition: from", cpnFromAccrualDates, CPN_FROM_INDEX_DEFINITION);
    assertArrayEquals("CouponIborCompoundedDefinition: getter", ACCRUAL_START_DATES, CPN_FROM_INDEX_DEFINITION.getAccrualStartDates());
    assertArrayEquals("CouponIborCompoundedDefinition: getter", ACCRUAL_START_DATES, CPN_FROM_INDEX_DEFINITION.getFixingPeriodStartDates());
    assertArrayEquals("CouponIborCompoundedDefinition: getter", ACCRUAL_END_DATES, CPN_FROM_INDEX_DEFINITION.getAccrualEndDates());
    assertArrayEquals("CouponIborCompoundedDefinition: getter", FIXING_DATES, CPN_FROM_INDEX_DEFINITION.getFixingDates());
    assertArrayEquals("CouponIborCompoundedDefinition: getter", FIXING_PERIOD_END_DATES, CPN_FROM_INDEX_DEFINITION.getFixingPeriodEndDates());
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
    CPN_FROM_INDEX_DEFINITION.toDerivative(DateUtils.getUTCDate(2012, 8, 25), new String[] {DSC_NAME, FWD_NAME});
  }

  @Test
  public void toDerivativeNoTS() {
    CouponIborCompounded cpnConverted = CPN_FROM_INDEX_DEFINITION.toDerivative(REFERENCE_DATE, new String[] {DSC_NAME, FWD_NAME});
    CouponIborCompounded cpnExpected = new CouponIborCompounded(USDLIBOR1M.getCurrency(), PAYMENT_TIME, DSC_NAME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, NOTIONAL, USDLIBOR1M, PAYMENT_ACCRUAL_FACTORS,
        FIXING_TIMES, ACCRUAL_START_TIMES, FIXING_PERIOD_END_TIMES, FIXING_ACCRUAL_FACTORS, FWD_NAME);
    assertEquals("CouponIborCompoundedDefinition: toDerivatives", cpnExpected, cpnConverted);
    Coupon cpnConverted2 = CPN_FROM_INDEX_DEFINITION.toDerivative(REFERENCE_DATE, FIXING_TS, new String[] {DSC_NAME, FWD_NAME});
    assertEquals("CouponIborCompoundedDefinition: toDerivatives", cpnExpected, cpnConverted2);
  }

  @Test
  public void toDerivativeAfter1Fixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 8, 28);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, CPN_FROM_INDEX_DEFINITION.getPaymentDate());
    double accruedNotional = (1.0 + PAYMENT_ACCRUAL_FACTORS[0] * FIXING_RATES[1]) * NOTIONAL;
    double[] paymentAccrualFactorsLeft = new double[NB_SUB_PERIOD - 1];
    System.arraycopy(PAYMENT_ACCRUAL_FACTORS, 1, paymentAccrualFactorsLeft, 0, NB_SUB_PERIOD - 1);
    final double[] fixingTimesLeft = new double[NB_SUB_PERIOD - 1];
    System.arraycopy(TimeCalculator.getTimeBetween(referenceDate, FIXING_DATES), 1, fixingTimesLeft, 0, NB_SUB_PERIOD - 1);
    final double[] fixingPeriodStartTimesLeft = new double[NB_SUB_PERIOD - 1];
    System.arraycopy(TimeCalculator.getTimeBetween(referenceDate, ACCRUAL_START_DATES), 1, fixingPeriodStartTimesLeft, 0, NB_SUB_PERIOD - 1);
    final double[] fixingPeriodEndTimesLeft = new double[NB_SUB_PERIOD - 1];
    System.arraycopy(TimeCalculator.getTimeBetween(referenceDate, FIXING_PERIOD_END_DATES), 1, fixingPeriodEndTimesLeft, 0, NB_SUB_PERIOD - 1);
    final double[] fixingPeriodAccrualFactorsLeft = new double[NB_SUB_PERIOD - 1];
    System.arraycopy(FIXING_ACCRUAL_FACTORS, 1, fixingPeriodAccrualFactorsLeft, 0, NB_SUB_PERIOD - 1);
    Coupon cpnConverted = CPN_FROM_INDEX_DEFINITION.toDerivative(referenceDate, FIXING_TS, new String[] {DSC_NAME, FWD_NAME});
    CouponIborCompounded cpnExpected = new CouponIborCompounded(USDLIBOR1M.getCurrency(), paymentTime, DSC_NAME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, accruedNotional, USDLIBOR1M,
        paymentAccrualFactorsLeft, fixingTimesLeft, fixingPeriodStartTimesLeft, fixingPeriodEndTimesLeft, fixingPeriodAccrualFactorsLeft, FWD_NAME);
    assertEquals("CouponIborCompoundedDefinition: toDerivatives", cpnExpected, cpnConverted);
  }

  @Test
  public void toDerivativeAfter2Fixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 9, 20);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, CPN_FROM_INDEX_DEFINITION.getPaymentDate());
    double accruedNotional = (1.0 + PAYMENT_ACCRUAL_FACTORS[0] * FIXING_RATES[1]) * (1.0 + PAYMENT_ACCRUAL_FACTORS[1] * FIXING_RATES[2]) * NOTIONAL;
    double[] paymentAccrualFactorsLeft = new double[] {PAYMENT_ACCRUAL_FACTORS[2]};
    final double[] fixingTimesLeft = new double[] {TimeCalculator.getTimeBetween(referenceDate, FIXING_DATES[2])};
    final double[] fixingPeriodStartTimesLeft = new double[] {TimeCalculator.getTimeBetween(referenceDate, ACCRUAL_START_DATES[2])};
    final double[] fixingPeriodEndTimesLeft = new double[] {TimeCalculator.getTimeBetween(referenceDate, FIXING_PERIOD_END_DATES[2])};
    final double[] fixingPeriodAccrualFactorsLeft = new double[] {FIXING_ACCRUAL_FACTORS[2]};
    Coupon cpnConverted = CPN_FROM_INDEX_DEFINITION.toDerivative(referenceDate, FIXING_TS, new String[] {DSC_NAME, FWD_NAME});
    CouponIborCompounded cpnExpected = new CouponIborCompounded(USDLIBOR1M.getCurrency(), paymentTime, DSC_NAME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, accruedNotional, USDLIBOR1M,
        paymentAccrualFactorsLeft, fixingTimesLeft, fixingPeriodStartTimesLeft, fixingPeriodEndTimesLeft, fixingPeriodAccrualFactorsLeft, FWD_NAME);
    assertEquals("CouponIborCompoundedDefinition: toDerivatives", cpnExpected, cpnConverted);
  }

  @Test
  public void toDerivativeAfterLastFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 10, 25);
    Coupon cpnConverted = CPN_FROM_INDEX_DEFINITION.toDerivative(referenceDate, FIXING_TS, new String[] {DSC_NAME, FWD_NAME});
    double rate = ((1.0 + PAYMENT_ACCRUAL_FACTORS[0] * FIXING_RATES[1]) * (1.0 + PAYMENT_ACCRUAL_FACTORS[1] * FIXING_RATES[2]) * (1.0 + PAYMENT_ACCRUAL_FACTORS[2] * FIXING_RATES[3]) - 1.0)
        / PAYMENT_ACCRUAL_FACTOR;
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, CPN_FROM_INDEX_DEFINITION.getPaymentDate());
    CouponFixed cpnExpected = new CouponFixed(USDLIBOR1M.getCurrency(), paymentTime, DSC_NAME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, rate, ACCRUAL_START_DATES[0], ACCRUAL_END_DATES[NB_SUB_PERIOD - 1]);
    assertEquals("CouponIborCompoundedDefinition: toDerivatives", cpnExpected, cpnConverted);
  }

}
