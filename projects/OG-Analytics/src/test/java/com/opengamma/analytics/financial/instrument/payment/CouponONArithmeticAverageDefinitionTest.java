/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexONMaster;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverage;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CouponONArithmeticAverageDefinitionTest {

  private static final BusinessDayConvention MOD_FOL = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final IndexON FEDFUND = IndexONMaster.getInstance().getIndex("FED FUND");
  private static final IborIndex USDLIBOR3M = IndexIborMaster.getInstance().getIndex("USDLIBOR3M");

  private static final int US_SETTLEMENT_DAYS = 2;
  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2013, 4, 16);
  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(TRADE_DATE, US_SETTLEMENT_DAYS, NYC);
  private static final ZonedDateTime EFFECTIVE_DATE = SPOT_DATE;

  private static final Period TENOR_3M = Period.ofMonths(3);
  private static final Period TENOR_7D = Period.ofDays(7);
  private static final double NOTIONAL = 100000000; // 100m
  private static final int PAYMENT_LAG = 2;

  private static final ZonedDateTime ACCRUAL_END_DATE_3M = ScheduleCalculator.getAdjustedDate(EFFECTIVE_DATE, TENOR_3M, USDLIBOR3M, NYC);
  private static final double ACCURAL_FACTOR_3M = USDLIBOR3M.getDayCount().getDayCountFraction(EFFECTIVE_DATE, ACCRUAL_END_DATE_3M);
  private static final ZonedDateTime PAYMENT_DATE_3M = ScheduleCalculator.getAdjustedDate(ACCRUAL_END_DATE_3M, -1 + FEDFUND.getPublicationLag() + PAYMENT_LAG, NYC);

  private static final ZonedDateTime ACCRUAL_END_DATE_7D = ScheduleCalculator.getAdjustedDate(EFFECTIVE_DATE, TENOR_7D, USDLIBOR3M, NYC);
  private static final double ACCURAL_FACTOR_7D = USDLIBOR3M.getDayCount().getDayCountFraction(EFFECTIVE_DATE, ACCRUAL_END_DATE_7D);
  private static final ZonedDateTime PAYMENT_DATE_7D = ScheduleCalculator.getAdjustedDate(ACCRUAL_END_DATE_3M, -1 + FEDFUND.getPublicationLag() + PAYMENT_LAG, NYC);

  private static final CouponONArithmeticAverageDefinition FEDFUND_CPN_3M_DEF = CouponONArithmeticAverageDefinition.from(FEDFUND, EFFECTIVE_DATE, TENOR_3M, NOTIONAL, PAYMENT_LAG,
      MOD_FOL, true, NYC);
  private static final CouponONArithmeticAverageDefinition FEDFUND_CPN_3M_2_DEF = CouponONArithmeticAverageDefinition.from(FEDFUND, EFFECTIVE_DATE, ACCRUAL_END_DATE_3M, NOTIONAL, PAYMENT_LAG, NYC);

  private static final CouponONArithmeticAverageDefinition FEDFUND_CPN_7D_DEF = new CouponONArithmeticAverageDefinition(Currency.USD, PAYMENT_DATE_7D, EFFECTIVE_DATE, ACCRUAL_END_DATE_7D,
      ACCURAL_FACTOR_7D, NOTIONAL, FEDFUND, EFFECTIVE_DATE, ACCRUAL_END_DATE_7D, NYC);

  private static final CouponONArithmeticAverageDefinition FEDFUND_CPN_7D_FROM_DEF = CouponONArithmeticAverageDefinition.from(FEDFUND, EFFECTIVE_DATE, TENOR_7D, NOTIONAL, PAYMENT_LAG,
      MOD_FOL, true, NYC);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2013, 4, 16);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullIndex() {
    CouponONArithmeticAverageDefinition.from(null, EFFECTIVE_DATE, TENOR_3M, NOTIONAL, 0, MOD_FOL, true, NYC);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullStartDate() {
    CouponONArithmeticAverageDefinition.from(FEDFUND, null, TENOR_3M, NOTIONAL, 0, MOD_FOL, true, NYC);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullTenor() {
    CouponONArithmeticAverageDefinition.from(FEDFUND, EFFECTIVE_DATE, null, NOTIONAL, 0, MOD_FOL, true, NYC);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullBD() {
    CouponONArithmeticAverageDefinition.from(FEDFUND, EFFECTIVE_DATE, TENOR_3M, NOTIONAL, 0, null, true, NYC);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullEndDate() {
    CouponONArithmeticAverageDefinition.from(FEDFUND, EFFECTIVE_DATE, null, NOTIONAL, PAYMENT_LAG, NYC);
  }

  @Test
  public void getter() {
    assertEquals("CouponArithmeticAverageON: getter", FEDFUND_CPN_3M_DEF.getPaymentDate(), PAYMENT_DATE_3M);
    assertEquals("CouponArithmeticAverageON: getter", FEDFUND_CPN_3M_DEF.getAccrualStartDate(), EFFECTIVE_DATE);
    assertEquals("CouponArithmeticAverageON: getter", FEDFUND_CPN_3M_DEF.getAccrualEndDate(), ACCRUAL_END_DATE_3M);
    assertEquals("CouponArithmeticAverageON: getter", FEDFUND_CPN_3M_DEF.getFixingPeriodStartDates()[0], EFFECTIVE_DATE);
    assertEquals("CouponArithmeticAverageON: getter", FEDFUND_CPN_3M_DEF.getFixingPeriodEndDates()[FEDFUND_CPN_3M_DEF.getFixingPeriodEndDates().length - 1], ACCRUAL_END_DATE_3M);
    assertEquals("CouponArithmeticAverageON: getter", FEDFUND_CPN_3M_DEF.getCurrency(), FEDFUND.getCurrency());
    assertEquals("CouponArithmeticAverageON: getter", FEDFUND_CPN_3M_DEF.getIndex(), FEDFUND);
  }

  @Test
  public void from() {
    assertEquals("CouponArithmeticAverageON: from", FEDFUND_CPN_3M_DEF, FEDFUND_CPN_3M_2_DEF);
  }

  @Test
  public void equalHash() {
    assertEquals("CouponArithmeticAverageON: equal-hash", FEDFUND_CPN_3M_DEF, FEDFUND_CPN_3M_DEF);
    final CouponONArithmeticAverageDefinition other = CouponONArithmeticAverageDefinition.from(FEDFUND, EFFECTIVE_DATE, TENOR_3M, NOTIONAL, PAYMENT_LAG,
        MOD_FOL, true, NYC);
    assertEquals("CouponArithmeticAverageON: equal-hash", FEDFUND_CPN_3M_DEF, other);
    assertEquals("CouponArithmeticAverageON: equal-hash", FEDFUND_CPN_3M_DEF.hashCode(), other.hashCode());
    CouponONArithmeticAverageDefinition modified;
    final IndexON modifiedIndex = IndexONMaster.getInstance().getIndex("EONIA");
    modified = CouponONArithmeticAverageDefinition.from(modifiedIndex, EFFECTIVE_DATE, TENOR_3M, NOTIONAL, PAYMENT_LAG, MOD_FOL, true, NYC);
    assertFalse("CouponArithmeticAverageON: equal-hash", FEDFUND_CPN_3M_DEF.equals(modified));
    modified = CouponONArithmeticAverageDefinition.from(FEDFUND, EFFECTIVE_DATE.plusDays(1), ACCRUAL_END_DATE_3M, NOTIONAL, PAYMENT_LAG, NYC);
    assertFalse("CouponArithmeticAverageON: equal-hash", FEDFUND_CPN_3M_DEF.equals(modified));
    modified = CouponONArithmeticAverageDefinition.from(FEDFUND, EFFECTIVE_DATE, ACCRUAL_END_DATE_3M.plusDays(1), NOTIONAL, PAYMENT_LAG, NYC);
    assertFalse("CouponArithmeticAverageON: equal-hash", FEDFUND_CPN_3M_DEF.equals(modified));
    modified = CouponONArithmeticAverageDefinition.from(FEDFUND, EFFECTIVE_DATE, ACCRUAL_END_DATE_3M, NOTIONAL + 1000, PAYMENT_LAG, NYC);
    assertFalse("CouponArithmeticAverageON: equal-hash", FEDFUND_CPN_3M_DEF.equals(modified));
    modified = CouponONArithmeticAverageDefinition.from(FEDFUND, EFFECTIVE_DATE, ACCRUAL_END_DATE_3M, NOTIONAL, PAYMENT_LAG + 1, NYC);
    assertFalse("CouponArithmeticAverageON: equal-hash", FEDFUND_CPN_3M_DEF.equals(modified));
  }

  @Test
  public void toDerivativesNoData() {
    final CouponONArithmeticAverage cpnConverted = FEDFUND_CPN_3M_DEF.toDerivative(REFERENCE_DATE);
    final double payTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, FEDFUND_CPN_3M_DEF.getPaymentDate());
    final double[] fixingStartTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, FEDFUND_CPN_3M_DEF.getFixingPeriodStartDates());
    final double[] fixingEndTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, FEDFUND_CPN_3M_DEF.getFixingPeriodEndDates());
    final CouponONArithmeticAverage cpnExpected = CouponONArithmeticAverage.from(payTime, FEDFUND_CPN_3M_DEF.getPaymentYearFraction(), NOTIONAL, FEDFUND, fixingStartTime, fixingEndTime,
        FEDFUND_CPN_3M_DEF.getFixingPeriodAccrualFactors(), 0);
    assertEquals("CouponArithmeticAverageONDefinition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFixingBeforeStart() {
    final ZonedDateTime referenceDate = ScheduleCalculator.getAdjustedDate(TRADE_DATE, 1, NYC);
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7) }, new double[] {0.01 });
    final CouponONArithmeticAverage cpnConverted = (CouponONArithmeticAverage) FEDFUND_CPN_3M_DEF.toDerivative(referenceDate, fixingTS);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, PAYMENT_DATE_3M);
    final double[] fixingPeriodStartTimes = TimeCalculator.getTimeBetween(referenceDate, FEDFUND_CPN_3M_DEF.getFixingPeriodStartDates());
    final double[] fixingPeriodEndTimes = TimeCalculator.getTimeBetween(referenceDate, FEDFUND_CPN_3M_DEF.getFixingPeriodEndDates());
    final CouponONArithmeticAverage cpnExpected = CouponONArithmeticAverage.from(paymentTime, ACCURAL_FACTOR_3M, NOTIONAL, FEDFUND, fixingPeriodStartTimes, fixingPeriodEndTimes,
        FEDFUND_CPN_3M_DEF.getFixingPeriodAccrualFactors(), 0.0);

    assertEquals("CouponArithmeticAverageON definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFixingOnStartNotYetFixed() {
    final ZonedDateTime referenceDate = SPOT_DATE;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8) },
        new double[] {0.01, 0.01 });
    final Payment cpnConverted = FEDFUND_CPN_3M_DEF.toDerivative(referenceDate, fixingTS);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, PAYMENT_DATE_3M);
    final double[] fixingPeriodStartTimes = TimeCalculator.getTimeBetween(referenceDate, FEDFUND_CPN_3M_DEF.getFixingPeriodStartDates());
    final double[] fixingPeriodEndTimes = TimeCalculator.getTimeBetween(referenceDate, FEDFUND_CPN_3M_DEF.getFixingPeriodEndDates());
    final CouponONArithmeticAverage cpnExpected = CouponONArithmeticAverage.from(paymentTime, ACCURAL_FACTOR_3M, NOTIONAL, FEDFUND, fixingPeriodStartTimes, fixingPeriodEndTimes,
        FEDFUND_CPN_3M_DEF.getFixingPeriodAccrualFactors(), 0.0);
    assertEquals("CouponArithmeticAverageON definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFixingOnStartFixed() {
    final ZonedDateTime referenceDate = FEDFUND_CPN_3M_DEF.getFixingPeriodStartDates()[1];
    final double fixingRate = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2013, 4, 18), DateUtils.getUTCDate(2013, 4, 19),
      DateUtils.getUTCDate(2013, 4, 22), DateUtils.getUTCDate(2013, 4, 23) }, new double[] {fixingRate, fixingRate, fixingRate, fixingRate });
    final Payment cpnConverted = FEDFUND_CPN_3M_DEF.toDerivative(referenceDate, fixingTS);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, PAYMENT_DATE_3M);
    final double rateAccrued = fixingRate * FEDFUND_CPN_3M_DEF.getFixingPeriodAccrualFactors()[0];
    final double[] fixingPeriodStartTimes = new double[FEDFUND_CPN_3M_DEF.getFixingPeriodStartDates().length - 1];
    final double[] fixingPeriodEndTimes = new double[FEDFUND_CPN_3M_DEF.getFixingPeriodEndDates().length - 1];
    for (int loopperiod = 1; loopperiod < FEDFUND_CPN_3M_DEF.getFixingPeriodStartDates().length; loopperiod++) {
      fixingPeriodStartTimes[loopperiod - 1] = TimeCalculator.getTimeBetween(referenceDate, FEDFUND_CPN_3M_DEF.getFixingPeriodStartDates()[loopperiod]);
      fixingPeriodEndTimes[loopperiod - 1] = TimeCalculator.getTimeBetween(referenceDate, FEDFUND_CPN_3M_DEF.getFixingPeriodEndDates()[loopperiod]);
    }

    final double[] fixingAccrualFactorsLeft = new double[FEDFUND_CPN_3M_DEF.getFixingPeriodAccrualFactors().length - 1];
    for (int loopperiod = 1; loopperiod < FEDFUND_CPN_3M_DEF.getFixingPeriodAccrualFactors().length; loopperiod++) {
      fixingAccrualFactorsLeft[loopperiod - 1] = FEDFUND_CPN_3M_DEF.getFixingPeriodAccrualFactors()[loopperiod];
    }
    final CouponONArithmeticAverage cpnExpected = CouponONArithmeticAverage.from(paymentTime, ACCURAL_FACTOR_3M, NOTIONAL, FEDFUND, fixingPeriodStartTimes, fixingPeriodEndTimes,
        fixingAccrualFactorsLeft, rateAccrued);
    assertEquals("CouponArithmeticAverageON definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFixingMiddleNotYetFixed() {
    final ZonedDateTime referenceDate = FEDFUND_CPN_7D_DEF.getFixingPeriodStartDates()[2];
    final double fixingRate = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2013, 4, 18), DateUtils.getUTCDate(2013, 4, 19),
      DateUtils.getUTCDate(2013, 4, 22), DateUtils.getUTCDate(2013, 4, 23) }, new double[] {fixingRate, fixingRate, fixingRate, fixingRate });
    final Payment cpnConverted = FEDFUND_CPN_7D_DEF.toDerivative(referenceDate, fixingTS);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, PAYMENT_DATE_7D);
    final double rateAccrued = fixingRate * FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[0] + fixingRate * FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[1];
    final double[] fixingPeriodStartTimes = new double[FEDFUND_CPN_7D_DEF.getFixingPeriodStartDates().length - 2];
    final double[] fixingPeriodEndTimes = new double[FEDFUND_CPN_7D_DEF.getFixingPeriodEndDates().length - 2];

    for (int loopperiod = 2; loopperiod < FEDFUND_CPN_7D_DEF.getFixingPeriodStartDates().length; loopperiod++) {
      fixingPeriodStartTimes[loopperiod - 2] = TimeCalculator.getTimeBetween(referenceDate, FEDFUND_CPN_7D_DEF.getFixingPeriodStartDates()[loopperiod]);
      fixingPeriodEndTimes[loopperiod - 2] = TimeCalculator.getTimeBetween(referenceDate, FEDFUND_CPN_7D_DEF.getFixingPeriodEndDates()[loopperiod]);
    }

    final double[] fixingAccrualFactorsLeft = new double[FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors().length - 2];
    for (int loopperiod = 2; loopperiod < FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors().length; loopperiod++) {
      fixingAccrualFactorsLeft[loopperiod - 2] = FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[loopperiod];
    }
    final CouponONArithmeticAverage cpnExpected = CouponONArithmeticAverage.from(paymentTime, ACCURAL_FACTOR_7D, NOTIONAL, FEDFUND, fixingPeriodStartTimes, fixingPeriodEndTimes,
        fixingAccrualFactorsLeft, rateAccrued);
    assertEquals("CouponArithmeticAverageON definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFixingMiddleFixed() {
    final ZonedDateTime referenceDate = FEDFUND_CPN_7D_DEF.getFixingPeriodStartDates()[3];
    final double fixingRate = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2013, 4, 18), DateUtils.getUTCDate(2013, 4, 19),
      DateUtils.getUTCDate(2013, 4, 22), DateUtils.getUTCDate(2013, 4, 23) }, new double[] {fixingRate, fixingRate, fixingRate, fixingRate });
    final Payment cpnConverted = FEDFUND_CPN_7D_DEF.toDerivative(referenceDate, fixingTS);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, PAYMENT_DATE_7D);
    final double rateAccrued = fixingRate * FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[0] + fixingRate * FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[1] + fixingRate *
        FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[2];
    final double[] fixingPeriodStartTimes = new double[FEDFUND_CPN_7D_DEF.getFixingPeriodStartDates().length - 3];
    final double[] fixingPeriodEndTimes = new double[FEDFUND_CPN_7D_DEF.getFixingPeriodEndDates().length - 3];

    for (int loopperiod = 3; loopperiod < FEDFUND_CPN_7D_DEF.getFixingPeriodStartDates().length; loopperiod++) {
      fixingPeriodStartTimes[loopperiod - 3] = TimeCalculator.getTimeBetween(referenceDate, FEDFUND_CPN_7D_DEF.getFixingPeriodStartDates()[loopperiod]);
      fixingPeriodEndTimes[loopperiod - 3] = TimeCalculator.getTimeBetween(referenceDate, FEDFUND_CPN_7D_DEF.getFixingPeriodEndDates()[loopperiod]);
    }

    final double[] fixingAccrualFactorsLeft = new double[FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors().length - 3];
    for (int loopperiod = 3; loopperiod < FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors().length; loopperiod++) {
      fixingAccrualFactorsLeft[loopperiod - 3] = FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[loopperiod];
    }
    final CouponONArithmeticAverage cpnExpected = CouponONArithmeticAverage.from(paymentTime, ACCURAL_FACTOR_7D, NOTIONAL, FEDFUND, fixingPeriodStartTimes, fixingPeriodEndTimes,
        fixingAccrualFactorsLeft, rateAccrued);
    assertEquals("CouponArithmeticAverageON definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFixingSecondLastFixed() {
    final ZonedDateTime referenceDate = FEDFUND_CPN_7D_DEF.getFixingPeriodEndDates()[4].plusDays(1);
    final double fixingRate = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2013, 4, 18), DateUtils.getUTCDate(2013, 4, 19),
      DateUtils.getUTCDate(2013, 4, 22), DateUtils.getUTCDate(2013, 4, 23), DateUtils.getUTCDate(2013, 4, 24) }, new double[] {fixingRate, fixingRate, fixingRate, fixingRate, fixingRate });
    final Payment cpnConverted = FEDFUND_CPN_7D_DEF.toDerivative(referenceDate, fixingTS);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, PAYMENT_DATE_7D);
    final double rateAccrued = fixingRate * FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[0] + fixingRate * FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[1] + fixingRate *
        FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[2] + fixingRate * FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[3]
        + fixingRate * FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[4];
    final CouponFixed cpnExpected = new CouponFixed(Currency.USD, paymentTime, ACCURAL_FACTOR_7D, NOTIONAL, rateAccrued / FEDFUND_CPN_7D_DEF.getPaymentYearFraction());
    assertEquals("CouponArithmeticAverageON definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFixingLast() {
    final ZonedDateTime referenceDate = FEDFUND_CPN_7D_DEF.getFixingPeriodEndDates()[4].plusDays(2);
    final double fixingRate = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2013, 4, 18), DateUtils.getUTCDate(2013, 4, 19),
      DateUtils.getUTCDate(2013, 4, 22), DateUtils.getUTCDate(2013, 4, 23), DateUtils.getUTCDate(2013, 4, 24) }, new double[] {fixingRate, fixingRate, fixingRate, fixingRate, fixingRate });
    final Payment cpnConverted = FEDFUND_CPN_7D_DEF.toDerivative(referenceDate, fixingTS);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, PAYMENT_DATE_7D);
    final double rateAccrued = fixingRate * FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[0] + fixingRate * FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[1] + fixingRate *
        FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[2] + fixingRate * FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[3]
        + fixingRate * FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[4];
    final CouponFixed cpnExpected = new CouponFixed(Currency.USD, paymentTime, ACCURAL_FACTOR_7D, NOTIONAL, rateAccrued / FEDFUND_CPN_7D_DEF.getPaymentYearFraction());
    assertEquals("CouponArithmeticAverageON definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeAfterLast() {
    final ZonedDateTime referenceDate = FEDFUND_CPN_7D_DEF.getFixingPeriodEndDates()[4].plusDays(3);
    final double fixingRate = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2013, 4, 18), DateUtils.getUTCDate(2013, 4, 19),
      DateUtils.getUTCDate(2013, 4, 22), DateUtils.getUTCDate(2013, 4, 23), DateUtils.getUTCDate(2013, 4, 24) }, new double[] {fixingRate, fixingRate, fixingRate, fixingRate, fixingRate });
    final Payment cpnConverted = FEDFUND_CPN_7D_DEF.toDerivative(referenceDate, fixingTS);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, PAYMENT_DATE_7D);
    final double rateAccrued = fixingRate * FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[0] + fixingRate * FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[1] + fixingRate *
        FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[2] + fixingRate * FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[3]
        + fixingRate * FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[4];
    final CouponFixed cpnExpected = new CouponFixed(Currency.USD, paymentTime, ACCURAL_FACTOR_7D, NOTIONAL, rateAccrued / FEDFUND_CPN_7D_DEF.getPaymentYearFraction());
    assertEquals("CouponArithmeticAverageON definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method on the payment date. valuation is at noon, payment set at midnight...
   */
  public void toDerivativeJustAfterPayment() {
    final ZonedDateTime valuationTimeIsNoon = DateUtils.getUTCDate(2013, 7, 22, 12, 0);
    assertTrue("valuationTimeIsNoon used to be after paymentDate, which was midnight. Confirm behaviour", valuationTimeIsNoon.isAfter(FEDFUND_CPN_7D_DEF.getPaymentDate()));
    final double fixingRate = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2013, 4, 18), DateUtils.getUTCDate(2013, 4, 19),
      DateUtils.getUTCDate(2013, 4, 22), DateUtils.getUTCDate(2013, 4, 23), DateUtils.getUTCDate(2013, 4, 24) }, new double[] {fixingRate, fixingRate, fixingRate, fixingRate, fixingRate });
    final Payment cpnConverted = FEDFUND_CPN_7D_DEF.toDerivative(valuationTimeIsNoon, fixingTS);
    final double paymentTime = TimeCalculator.getTimeBetween(valuationTimeIsNoon, PAYMENT_DATE_7D);
    final double rateAccrued = fixingRate * FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[0] + fixingRate * FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[1] + fixingRate *
        FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[2] + fixingRate * FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[3]
        + fixingRate * FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[4];

    final CouponFixed cpnExpected = new CouponFixed(Currency.USD, paymentTime, ACCURAL_FACTOR_7D, NOTIONAL, rateAccrued / ACCURAL_FACTOR_7D);
    assertEquals("CouponArithmeticAverageON definition: toDerivative", cpnExpected, cpnConverted);

    // Test pricing, too. Notice that the value of a coupon on its payment date is non-zero
    final MulticurveProviderDiscount curves = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
    final MultipleCurrencyAmount pvConverted = com.opengamma.analytics.financial.interestrate.payments.provider.CouponFixedDiscountingMethod.getInstance().presentValue((CouponFixed) cpnConverted,
        curves);
    final MultipleCurrencyAmount pvExpected = com.opengamma.analytics.financial.interestrate.payments.provider.CouponFixedDiscountingMethod.getInstance().presentValue(cpnExpected, curves);
    assertEquals("CouponArithmeticAverageON definition: toDerivative", pvConverted, pvExpected);
    assertEquals("CouponArithmeticAverageON definition: toDerivative", pvConverted, MultipleCurrencyAmount.of(Currency.USD, 19444.44444444444));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  /**
   * Tests the toDerivative method: after payment date
   */
  public void toDerivativeAfterPayment() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2013, 7, 23);
    final double fixingRate = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8),
      DateUtils.getUTCDate(2011, 9, 9), DateUtils.getUTCDate(2011, 9, 12), DateUtils.getUTCDate(2011, 9, 13), DateUtils.getUTCDate(2011, 9, 14), DateUtils.getUTCDate(2011, 9, 15) }, new double[] {
      fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate });
    FEDFUND_CPN_7D_DEF.toDerivative(referenceDate, fixingTS);
  }

}
