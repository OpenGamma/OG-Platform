/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedFxReset;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborFxReset;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
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
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests CouponIborFxResetDefinition.
 */
@Test(groups = TestGroup.UNIT)
@SuppressWarnings("unchecked")
public class CouponIborFxResetDefinitionTest {

  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.EUR;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY,
      IS_EOM, "Deprecated");

  private static final Currency CUR_REF = Currency.EUR;
  private static final Currency CUR_PAY = Currency.USD;
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 4, 7);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 1, 6);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 4, 6);
  private static final ZonedDateTime FX_FIXING_DATE = DateUtils.getUTCDate(2011, 1, 3);
  private static final ZonedDateTime FX_DELIVERY_DATE = DateUtils.getUTCDate(2011, 1, 6);
  private static final double ACCRUAL_FACTOR = 0.267;
  private static final double NOTIONAL = 1000000; //1m
  private static final double SPREAD = -0.001; // -10 bps

  private static final ZonedDateTime FIXING_DATE_SAME_AS_FX = DateUtils.getUTCDate(2011, 1, 3);
  private static final CouponIborFxResetDefinition CPN_SAME_FIXING_DATES = new CouponIborFxResetDefinition(CUR_PAY,
      PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE_SAME_AS_FX, INDEX,
      SPREAD, CALENDAR, CUR_REF, FX_FIXING_DATE, FX_DELIVERY_DATE);

  private static final ZonedDateTime FIXING_DATE_AFTER_FX = FIXING_DATE_SAME_AS_FX.plusDays(2);
  private static final CouponIborFxResetDefinition CPN_FX_FIXED_FIRST = new CouponIborFxResetDefinition(CUR_PAY,
      PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE_AFTER_FX, INDEX,
      SPREAD, CALENDAR, CUR_REF, FX_FIXING_DATE, FX_DELIVERY_DATE);

  private static final ZonedDateTime FIXING_DATE_BEFORE_FX = FIXING_DATE_SAME_AS_FX.minusDays(2);
  private static final CouponIborFxResetDefinition CPN_INDEX_FIXED_FIRST = new CouponIborFxResetDefinition(CUR_PAY,
      PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE_BEFORE_FX, INDEX,
      SPREAD, CALENDAR, CUR_REF, FX_FIXING_DATE, FX_DELIVERY_DATE);

  private static final double FX_FIXING_RATE = 1.40;
  private static final DoubleTimeSeries<ZonedDateTime> FX_FIXING_TS_10 =
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
          new ZonedDateTime[] {FX_FIXING_DATE.minusDays(11), FX_FIXING_DATE.minusDays(10) },
          new double[] {1.38, 1.39 });
  private static final DoubleTimeSeries<ZonedDateTime> FX_FIXING_TS_1 =
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
          new ZonedDateTime[] {FX_FIXING_DATE.minusDays(2), FX_FIXING_DATE.minusDays(1) },
          new double[] {1.38, 1.39 });
  private static final DoubleTimeSeries<ZonedDateTime> FX_FIXING_TS_0 =
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
          new ZonedDateTime[] {FX_FIXING_DATE.minusDays(2), FX_FIXING_DATE.minusDays(1), FX_FIXING_DATE },
          new double[] {1.38, 1.39, FX_FIXING_RATE });

  private static final double FIXING_RATE = 0.04;
  private static final DoubleTimeSeries<ZonedDateTime> INDEX_FIXING_TS_SAME = ImmutableZonedDateTimeDoubleTimeSeries
      .ofUTC(new ZonedDateTime[] {FIXING_DATE_SAME_AS_FX }, new double[] {FIXING_RATE });
  private static final DoubleTimeSeries<ZonedDateTime> INDEX_FIXING_TS_AFTER_FX = ImmutableZonedDateTimeDoubleTimeSeries
      .ofUTC(new ZonedDateTime[] {FIXING_DATE_AFTER_FX }, new double[] {FIXING_RATE });
  private static final DoubleTimeSeries<ZonedDateTime> INDEX_FIXING_TS_BEFORE_FX = ImmutableZonedDateTimeDoubleTimeSeries
      .ofUTC(new ZonedDateTime[] {FIXING_DATE_BEFORE_FX }, new double[] {FIXING_RATE });
  private static final DoubleTimeSeries<ZonedDateTime> INDEX_FIXING_TS_OLD = ImmutableZonedDateTimeDoubleTimeSeries
      .ofUTC(new ZonedDateTime[] {FIXING_DATE_BEFORE_FX.minusDays(10) }, new double[] {FIXING_RATE });

  private static final double TOLERANCE_AMOUNT = 1.0E-6;

  /**
   * 
   */
  @Test
  public void testGetter() {
    ZonedDateTime expFixingPeriodStartDate = ScheduleCalculator.getAdjustedDate(FIXING_DATE_SAME_AS_FX,
        INDEX.getSpotLag(), CALENDAR);
    ZonedDateTime expFixingPeriodEndDate = ScheduleCalculator.getAdjustedDate(expFixingPeriodStartDate,
        INDEX.getTenor(), INDEX.getBusinessDayConvention(), CALENDAR, INDEX.isEndOfMonth());

    CouponIborFxResetDefinition cpnRe = new CouponIborFxResetDefinition(CUR_PAY, PAYMENT_DATE, ACCRUAL_START_DATE,
        ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FX_FIXING_DATE, expFixingPeriodStartDate, expFixingPeriodEndDate,
        INDEX.getDayCount().getDayCountFraction(expFixingPeriodStartDate, expFixingPeriodEndDate, CALENDAR), INDEX,
        SPREAD, CALENDAR, CUR_REF, FX_FIXING_DATE, FX_DELIVERY_DATE);
    assertEquals(cpnRe, CPN_SAME_FIXING_DATES);

    assertEquals(NOTIONAL, CPN_SAME_FIXING_DATES.getNotional(), TOLERANCE_AMOUNT);
    assertEquals(FIXING_DATE_SAME_AS_FX, CPN_SAME_FIXING_DATES.getIborIndexFixingDate());
    assertEquals(SPREAD, CPN_SAME_FIXING_DATES.getSpread(), TOLERANCE_AMOUNT);
    assertEquals(PAYMENT_DATE, CPN_SAME_FIXING_DATES.getPaymentDate());
    assertEquals(ACCRUAL_START_DATE, CPN_SAME_FIXING_DATES.getAccrualStartDate());
    assertEquals(ACCRUAL_END_DATE, CPN_SAME_FIXING_DATES.getAccrualEndDate());
    assertEquals(CALENDAR, CPN_SAME_FIXING_DATES.getCalendar());

    assertEquals(expFixingPeriodStartDate, CPN_SAME_FIXING_DATES.getIborIndexFixingPeriodStartDate());
    assertEquals(expFixingPeriodEndDate, CPN_SAME_FIXING_DATES.getIborIndexFixingPeriodEndDate());
    assertEquals(INDEX.getDayCount().getDayCountFraction(expFixingPeriodStartDate, expFixingPeriodEndDate, CALENDAR),
        CPN_SAME_FIXING_DATES.getIborIndexFixingPeriodAccrualFactor());
    assertEquals(CUR_REF, CPN_SAME_FIXING_DATES.getReferenceCurrency());
    assertEquals(FX_FIXING_DATE, CPN_SAME_FIXING_DATES.getFxFixingDate());
    assertEquals(FX_DELIVERY_DATE, CPN_SAME_FIXING_DATES.getFxDeliveryDate());
  }

  /**
   * Without time series
   */
  @Test
  public void toDerivativeWithoutHts() {
    /*
     * ibor index fixing same as fx fixing
     */
    ZonedDateTime valuationDate = FX_FIXING_DATE.minusDays(10);
    double fixingPeriodStartTime = TimeCalculator.getTimeBetween(valuationDate,
        CPN_SAME_FIXING_DATES.getIborIndexFixingPeriodStartDate());
    double fixingPeriodEndTime = TimeCalculator.getTimeBetween(valuationDate,
        CPN_SAME_FIXING_DATES.getIborIndexFixingPeriodEndDate());
    double paymentTime = TimeCalculator.getTimeBetween(valuationDate, PAYMENT_DATE);
    double fixingTime = TimeCalculator.getTimeBetween(valuationDate, FX_FIXING_DATE);
    double iborFixingTime = fixingTime;
    double deliveryTime = TimeCalculator.getTimeBetween(valuationDate, FX_DELIVERY_DATE);
    CouponIborFxReset cpnExpected = new CouponIborFxReset(CUR_PAY, paymentTime, ACCRUAL_FACTOR, NOTIONAL,
        iborFixingTime, INDEX, fixingPeriodStartTime, fixingPeriodEndTime,
        CPN_SAME_FIXING_DATES.getIborIndexFixingPeriodAccrualFactor(), SPREAD, CUR, fixingTime, deliveryTime);
    CouponIborFxReset cpnConverted = CPN_SAME_FIXING_DATES.toDerivative(valuationDate);
    assertEquals("CouponIborFxResetDefinition: toDerivative", cpnExpected, cpnConverted);
    /*
     * ibor index fixing before as fx fixing
     */
    fixingPeriodStartTime = TimeCalculator.getTimeBetween(valuationDate,
        CPN_INDEX_FIXED_FIRST.getIborIndexFixingPeriodStartDate());
    fixingPeriodEndTime = TimeCalculator.getTimeBetween(valuationDate,
        CPN_INDEX_FIXED_FIRST.getIborIndexFixingPeriodEndDate());
    iborFixingTime = TimeCalculator.getTimeBetween(valuationDate, FIXING_DATE_BEFORE_FX);
    cpnExpected = new CouponIborFxReset(CUR_PAY, paymentTime, ACCRUAL_FACTOR, NOTIONAL, iborFixingTime, INDEX,
        fixingPeriodStartTime, fixingPeriodEndTime, CPN_INDEX_FIXED_FIRST.getIborIndexFixingPeriodAccrualFactor(),
        SPREAD, CUR, fixingTime, deliveryTime);
    cpnConverted = CPN_INDEX_FIXED_FIRST.toDerivative(valuationDate);
    assertEquals("CouponIborFxResetDefinition: toDerivative", cpnExpected, cpnConverted);
    /*
     * ibor index fixing after as fx fixing
     */
    fixingPeriodStartTime = TimeCalculator.getTimeBetween(valuationDate,
        CPN_FX_FIXED_FIRST.getIborIndexFixingPeriodStartDate());
    fixingPeriodEndTime = TimeCalculator.getTimeBetween(valuationDate,
        CPN_FX_FIXED_FIRST.getIborIndexFixingPeriodEndDate());
    iborFixingTime = TimeCalculator.getTimeBetween(valuationDate, FIXING_DATE_AFTER_FX);
    cpnExpected = new CouponIborFxReset(CUR_PAY, paymentTime, ACCRUAL_FACTOR, NOTIONAL, iborFixingTime, INDEX,
        fixingPeriodStartTime, fixingPeriodEndTime, CPN_FX_FIXED_FIRST.getIborIndexFixingPeriodAccrualFactor(), SPREAD,
        CUR, fixingTime, deliveryTime);
    cpnConverted = CPN_FX_FIXED_FIRST.toDerivative(valuationDate);
    assertEquals("CouponIborFxResetDefinition: toDerivative", cpnExpected, cpnConverted);
  }

  /**
   * valuationDate is before FX fixing data, and before index fixing date
   */
  @Test
  public void toDerivativeBeforeFxBeforeIndex() {
    ZonedDateTime valuationDate = FX_FIXING_DATE.minusDays(10);

    double fixingPeriodStartTime = TimeCalculator.getTimeBetween(valuationDate,
        CPN_SAME_FIXING_DATES.getIborIndexFixingPeriodStartDate());
    double fixingPeriodEndTime = TimeCalculator.getTimeBetween(valuationDate,
        CPN_SAME_FIXING_DATES.getIborIndexFixingPeriodEndDate());
    double paymentTime = TimeCalculator.getTimeBetween(valuationDate, PAYMENT_DATE);
    double fixingTime = TimeCalculator.getTimeBetween(valuationDate, FX_FIXING_DATE);
    double deliveryTime = TimeCalculator.getTimeBetween(valuationDate, FX_DELIVERY_DATE);

    /*
     * ibor index fixing same as fx fixing
     */
    DoubleTimeSeries<ZonedDateTime>[] htsArray1 = new DoubleTimeSeries[] {INDEX_FIXING_TS_SAME, FX_FIXING_TS_10 };
    Payment cpnConverted1 = CPN_SAME_FIXING_DATES.toDerivative(valuationDate, htsArray1);
    CouponIborFxReset cpnExpected1 = new CouponIborFxReset(CUR_PAY, paymentTime, ACCRUAL_FACTOR, NOTIONAL, fixingTime,
        INDEX, fixingPeriodStartTime, fixingPeriodEndTime,
        CPN_SAME_FIXING_DATES.getIborIndexFixingPeriodAccrualFactor(), SPREAD, CUR, fixingTime, deliveryTime);
    assertEquals("CouponIborFxResetDefinition: toDerivative", cpnExpected1, cpnConverted1);

    /*
     * ibor index fixing before fx fixing
     */
    DoubleTimeSeries<ZonedDateTime>[] htsArray2 = new DoubleTimeSeries[] {INDEX_FIXING_TS_OLD, FX_FIXING_TS_10 };
    Payment cpnConverted2 = CPN_INDEX_FIXED_FIRST.toDerivative(valuationDate, htsArray2);
    double fixingTime2 = TimeCalculator.getTimeBetween(valuationDate, FIXING_DATE_BEFORE_FX);
    double fixingPeriodStartTime2 = TimeCalculator.getTimeBetween(valuationDate,
        CPN_INDEX_FIXED_FIRST.getIborIndexFixingPeriodStartDate());
    double fixingPeriodEndTime2 = TimeCalculator.getTimeBetween(valuationDate,
        CPN_INDEX_FIXED_FIRST.getIborIndexFixingPeriodEndDate());
    CouponIborFxReset cpnExpected2 = new CouponIborFxReset(CUR_PAY, paymentTime, ACCRUAL_FACTOR, NOTIONAL, fixingTime2,
        INDEX, fixingPeriodStartTime2, fixingPeriodEndTime2,
        CPN_INDEX_FIXED_FIRST.getIborIndexFixingPeriodAccrualFactor(), SPREAD, CUR, fixingTime, deliveryTime);
    assertEquals("CouponIborFxResetDefinition: toDerivative", cpnExpected2, cpnConverted2);

    /*
     * ibor index fixing after fx fixing
     */
    DoubleTimeSeries<ZonedDateTime>[] htsArray3 = new DoubleTimeSeries[] {INDEX_FIXING_TS_OLD, FX_FIXING_TS_10 };
    Payment cpnConverted3 = CPN_FX_FIXED_FIRST.toDerivative(valuationDate, htsArray3);
    double fixingTime3 = TimeCalculator.getTimeBetween(valuationDate, FIXING_DATE_AFTER_FX);
    double fixingPeriodStartTime3 = TimeCalculator.getTimeBetween(valuationDate,
        CPN_FX_FIXED_FIRST.getIborIndexFixingPeriodStartDate());
    double fixingPeriodEndTime3 = TimeCalculator.getTimeBetween(valuationDate,
        CPN_FX_FIXED_FIRST.getIborIndexFixingPeriodEndDate());
    CouponIborFxReset cpnExpected3 = new CouponIborFxReset(CUR_PAY, paymentTime, ACCRUAL_FACTOR, NOTIONAL, fixingTime3,
        INDEX, fixingPeriodStartTime3, fixingPeriodEndTime3,
        CPN_FX_FIXED_FIRST.getIborIndexFixingPeriodAccrualFactor(), SPREAD, CUR, fixingTime, deliveryTime);
    assertEquals("CouponIborFxResetDefinition: toDerivative", cpnExpected3, cpnConverted3);
  }

  /**
   * valuationDate is on FX fixing data, and on index fixing date
   */
  @Test
  public void toDerivativeOnFxOnIndex() {
    ZonedDateTime valuationDate = FX_FIXING_DATE;
    double fixingTime = TimeCalculator.getTimeBetween(valuationDate, CPN_SAME_FIXING_DATES.getIborIndexFixingDate());
    double fixingPeriodStartTime = TimeCalculator.getTimeBetween(valuationDate,
        CPN_SAME_FIXING_DATES.getIborIndexFixingPeriodStartDate());
    double fixingPeriodEndTime = TimeCalculator.getTimeBetween(valuationDate,
        CPN_SAME_FIXING_DATES.getIborIndexFixingPeriodEndDate());
    double paymentTime = TimeCalculator.getTimeBetween(valuationDate, PAYMENT_DATE);
    double fxFixingTime = TimeCalculator.getTimeBetween(valuationDate, FX_FIXING_DATE);
    double fXDeliveryTime = TimeCalculator.getTimeBetween(valuationDate, FX_DELIVERY_DATE);

    /*
     * FX rate, index Rate, not available
     */
    DoubleTimeSeries<ZonedDateTime>[] htsArrayWithoutFxRateWithoutIndex = new DoubleTimeSeries[] {
        INDEX_FIXING_TS_BEFORE_FX, FX_FIXING_TS_1 };
    CouponIborFxReset cpnWithoutFxIndexExpected = new CouponIborFxReset(CUR_PAY, paymentTime, ACCRUAL_FACTOR, NOTIONAL,
        fixingTime, INDEX, fixingPeriodStartTime, fixingPeriodEndTime,
        CPN_SAME_FIXING_DATES.getIborIndexFixingPeriodAccrualFactor(), SPREAD, CUR, fxFixingTime, fXDeliveryTime);
    Payment cpnConvertedWithoutFxIndex = CPN_SAME_FIXING_DATES.toDerivative(valuationDate,
        htsArrayWithoutFxRateWithoutIndex);
    assertEquals("CouponIborFxResetDefinition: toDerivative", cpnWithoutFxIndexExpected, cpnConvertedWithoutFxIndex);

    /*
     * FX rate available, index Rate not available
     */
    DoubleTimeSeries<ZonedDateTime>[] htsArrayWithFxRateWithoutIndex = new DoubleTimeSeries[] {
        INDEX_FIXING_TS_BEFORE_FX, FX_FIXING_TS_0 };
    CouponIborSpread cpnWithFxExpected = new CouponIborSpread(CUR_PAY, paymentTime, ACCRUAL_FACTOR, NOTIONAL *
        FX_FIXING_RATE, fixingTime, INDEX, fixingPeriodStartTime, fixingPeriodEndTime,
        CPN_SAME_FIXING_DATES.getIborIndexFixingPeriodAccrualFactor(), SPREAD);
    Payment cpnConvertedWithFx = CPN_SAME_FIXING_DATES.toDerivative(valuationDate, htsArrayWithFxRateWithoutIndex);
    assertEquals("CouponIborFxResetDefinition: toDerivative", cpnWithFxExpected, cpnConvertedWithFx);

    /*
     * FX rate not available, index Rate available
     */
    DoubleTimeSeries<ZonedDateTime>[] htsArrayWithoutFxRateWithIndex = new DoubleTimeSeries[] {
        INDEX_FIXING_TS_SAME, FX_FIXING_TS_1 };
    CouponFixedFxReset cpnWithIndexExpected = new CouponFixedFxReset(CUR_PAY, paymentTime, ACCRUAL_FACTOR, NOTIONAL,
        FIXING_RATE + SPREAD, CUR_REF, fxFixingTime, fXDeliveryTime);
    Payment cpnConvertedWithIndex = CPN_SAME_FIXING_DATES.toDerivative(valuationDate, htsArrayWithoutFxRateWithIndex);
    assertEquals("CouponIborFxResetDefinition: toDerivative", cpnWithIndexExpected, cpnConvertedWithIndex);

    /*
     * FX rate, index Rate available
     */
    DoubleTimeSeries<ZonedDateTime>[] htsArrayWithFxRateIndex = new DoubleTimeSeries[] {
        INDEX_FIXING_TS_SAME, FX_FIXING_TS_0 };
    CouponFixed cpnWithFxIndexExpected = new CouponFixed(CUR_PAY, paymentTime, ACCRUAL_FACTOR, NOTIONAL *
        FX_FIXING_RATE, FIXING_RATE + SPREAD);
    Payment cpnConvertedWithFxIndex = CPN_SAME_FIXING_DATES.toDerivative(valuationDate, htsArrayWithFxRateIndex);
    assertEquals("CouponIborFxResetDefinition: toDerivative", cpnWithFxIndexExpected, cpnConvertedWithFxIndex);
  }

  /**
   * valuationDate is after FX fixing data, and after index fixing date
   */
  @Test
  public void toDerivativeAfterFxAfterIndex() {
    ZonedDateTime valuationDate1 = FX_FIXING_DATE.plusDays(1);

    /*
     * ibor index fixing same as fx fixing
     */
    DoubleTimeSeries<ZonedDateTime>[] htsArray1 = new DoubleTimeSeries[] {INDEX_FIXING_TS_SAME, FX_FIXING_TS_0 };
    double paymentTime1 = TimeCalculator.getTimeBetween(valuationDate1, PAYMENT_DATE);
    CouponFixed cpnWithFxIndexExpected1 = new CouponFixed(CUR_PAY, paymentTime1, ACCRUAL_FACTOR, NOTIONAL *
        FX_FIXING_RATE, FIXING_RATE + SPREAD);
    Payment cpnConvertedWithFxIndex1 = CPN_SAME_FIXING_DATES.toDerivative(valuationDate1, htsArray1);
    assertEquals("CouponIborFxResetDefinition: toDerivative", cpnWithFxIndexExpected1, cpnConvertedWithFxIndex1);

    /*
     * ibor index fixing before fx fixing
     */
    ZonedDateTime valuationDate2 = FX_FIXING_DATE.plusDays(1);
    DoubleTimeSeries<ZonedDateTime>[] htsArray2 = new DoubleTimeSeries[] {INDEX_FIXING_TS_BEFORE_FX,
        FX_FIXING_TS_0 };
    double paymentTime2 = TimeCalculator.getTimeBetween(valuationDate2, PAYMENT_DATE);
    CouponFixed cpnWithFxIndexExpected2 = new CouponFixed(CUR_PAY, paymentTime2, ACCRUAL_FACTOR, NOTIONAL *
        FX_FIXING_RATE, FIXING_RATE + SPREAD);
    Payment cpnConvertedWithFxRate2 = CPN_INDEX_FIXED_FIRST.toDerivative(valuationDate2, htsArray2);
    assertEquals("CouponIborFxResetDefinition: toDerivative", cpnWithFxIndexExpected2, cpnConvertedWithFxRate2);

    /*
     * ibor index fixing after fx fixing
     */
    ZonedDateTime valuationDate3 = FX_FIXING_DATE.plusDays(2);
    DoubleTimeSeries<ZonedDateTime>[] htsArray3 = new DoubleTimeSeries[] {INDEX_FIXING_TS_AFTER_FX,
        FX_FIXING_TS_0 };
    double paymentTime3 = TimeCalculator.getTimeBetween(valuationDate3, PAYMENT_DATE);
    CouponFixed cpnWithFxIndexExpected3 = new CouponFixed(CUR_PAY, paymentTime3, ACCRUAL_FACTOR, NOTIONAL *
        FX_FIXING_RATE, FIXING_RATE + SPREAD);
    Payment cpnConvertedWithFxRate3 = CPN_FX_FIXED_FIRST.toDerivative(valuationDate3, htsArray3);
    assertEquals("CouponIborFxResetDefinition: toDerivative", cpnWithFxIndexExpected3, cpnConvertedWithFxRate3);
  }

  /**
   * valuationDate is on FX fixing data, but before index fixing date
   */
  @Test
  public void toDerivativeOnFxBeforeIndex() {
    ZonedDateTime valuationDate = FX_FIXING_DATE;
    double fixingTime = TimeCalculator.getTimeBetween(valuationDate, CPN_FX_FIXED_FIRST.getIborIndexFixingDate());
    double fixingPeriodStartTime = TimeCalculator.getTimeBetween(valuationDate,
        CPN_FX_FIXED_FIRST.getIborIndexFixingPeriodStartDate());
    double fixingPeriodEndTime = TimeCalculator.getTimeBetween(valuationDate,
        CPN_FX_FIXED_FIRST.getIborIndexFixingPeriodEndDate());
    double paymentTime = TimeCalculator.getTimeBetween(valuationDate, PAYMENT_DATE);
    double fxFixingTime = TimeCalculator.getTimeBetween(valuationDate, FX_FIXING_DATE);
    double fXDeliveryTime = TimeCalculator.getTimeBetween(valuationDate, FX_DELIVERY_DATE);

    /*
     * FX rate not available
     */
    DoubleTimeSeries<ZonedDateTime>[] htsArrayWithoutFxRate = new DoubleTimeSeries[] {INDEX_FIXING_TS_AFTER_FX,
        FX_FIXING_TS_1 };
    CouponIborFxReset cpnWithoutFxRateExpected = new CouponIborFxReset(CUR_PAY, paymentTime, ACCRUAL_FACTOR, NOTIONAL,
        fixingTime, INDEX, fixingPeriodStartTime, fixingPeriodEndTime,
        CPN_FX_FIXED_FIRST.getIborIndexFixingPeriodAccrualFactor(), SPREAD, CUR, fxFixingTime, fXDeliveryTime);
    Payment cpnConvertedWithoutFxRate = CPN_FX_FIXED_FIRST.toDerivative(valuationDate, htsArrayWithoutFxRate);
    assertEquals("CouponIborFxResetDefinition: toDerivative", cpnWithoutFxRateExpected, cpnConvertedWithoutFxRate);

    /*
     * FX rate available
     */
    DoubleTimeSeries<ZonedDateTime>[] htsArrayWithFxRate = new DoubleTimeSeries[] {INDEX_FIXING_TS_AFTER_FX,
        FX_FIXING_TS_0 };
    CouponIborSpread cpnWithFxRateExpected = new CouponIborSpread(CUR_PAY, paymentTime, ACCRUAL_FACTOR, NOTIONAL *
        FX_FIXING_RATE, fixingTime, INDEX, fixingPeriodStartTime, fixingPeriodEndTime,
        CPN_FX_FIXED_FIRST.getIborIndexFixingPeriodAccrualFactor(), SPREAD);
    Payment cpnConvertedWithFxRate = CPN_FX_FIXED_FIRST.toDerivative(valuationDate, htsArrayWithFxRate);
    assertEquals("CouponIborFxResetDefinition: toDerivative", cpnWithFxRateExpected, cpnConvertedWithFxRate);
  }

  /**
   * valuationDate is on FX fixing data, but after index fixing date
   */
  @Test
  public void toDerivativeOnFxAfterIndex() {
    ZonedDateTime valuationDate = FX_FIXING_DATE;
    double paymentTime = TimeCalculator.getTimeBetween(valuationDate, PAYMENT_DATE);
    double fxFixingTime = TimeCalculator.getTimeBetween(valuationDate, FX_FIXING_DATE);
    double fXDeliveryTime = TimeCalculator.getTimeBetween(valuationDate, FX_DELIVERY_DATE);

    /*
     * FX rate not available
     */
    DoubleTimeSeries<ZonedDateTime>[] htsArrayWithoutFxRate = new DoubleTimeSeries[] {INDEX_FIXING_TS_BEFORE_FX,
        FX_FIXING_TS_1 };
    CouponFixedFxReset cpnWithIndexExpected = new CouponFixedFxReset(CUR_PAY, paymentTime, ACCRUAL_FACTOR, NOTIONAL,
        FIXING_RATE + SPREAD, CUR_REF, fxFixingTime, fXDeliveryTime);
    Payment cpnConvertedWithIndex = CPN_INDEX_FIXED_FIRST.toDerivative(valuationDate, htsArrayWithoutFxRate);
    assertEquals("CouponIborFxResetDefinition: toDerivative", cpnWithIndexExpected, cpnConvertedWithIndex);

    /*
     * FX rate available
     */
    DoubleTimeSeries<ZonedDateTime>[] htsArrayWithFxRate = new DoubleTimeSeries[] {INDEX_FIXING_TS_BEFORE_FX,
        FX_FIXING_TS_0 };
    CouponFixed cpnWithFxIndexExpected = new CouponFixed(CUR_PAY, paymentTime, ACCRUAL_FACTOR, NOTIONAL *
        FX_FIXING_RATE, FIXING_RATE + SPREAD);
    Payment cpnConvertedWithFxIndex = CPN_INDEX_FIXED_FIRST.toDerivative(valuationDate, htsArrayWithFxRate);
    assertEquals("CouponIborFxResetDefinition: toDerivative", cpnWithFxIndexExpected, cpnConvertedWithFxIndex);
  }

  /**
   * valuationDate is before FX fixing data, but on index fixing date
   */
  @Test
  public void toDerivativeBeforeFxOnIndex() {
    ZonedDateTime valuationDate = FIXING_DATE_BEFORE_FX;
    double fixingTime = TimeCalculator.getTimeBetween(valuationDate, CPN_INDEX_FIXED_FIRST.getIborIndexFixingDate());
    double fixingPeriodStartTime = TimeCalculator.getTimeBetween(valuationDate,
        CPN_INDEX_FIXED_FIRST.getIborIndexFixingPeriodStartDate());
    double fixingPeriodEndTime = TimeCalculator.getTimeBetween(valuationDate,
        CPN_INDEX_FIXED_FIRST.getIborIndexFixingPeriodEndDate());
    double paymentTime = TimeCalculator.getTimeBetween(valuationDate, PAYMENT_DATE);
    double fxFixingTime = TimeCalculator.getTimeBetween(valuationDate, FX_FIXING_DATE);
    double fXDeliveryTime = TimeCalculator.getTimeBetween(valuationDate, FX_DELIVERY_DATE);

    /*
     * index not available
     */
    DoubleTimeSeries<ZonedDateTime>[] htsArrayWithoutIndex = new DoubleTimeSeries[] {
        INDEX_FIXING_TS_OLD, FX_FIXING_TS_10 };
    CouponIborFxReset cpnWithoutIndexExpected = new CouponIborFxReset(CUR_PAY, paymentTime, ACCRUAL_FACTOR, NOTIONAL,
        fixingTime, INDEX, fixingPeriodStartTime, fixingPeriodEndTime,
        CPN_INDEX_FIXED_FIRST.getIborIndexFixingPeriodAccrualFactor(), SPREAD, CUR, fxFixingTime, fXDeliveryTime);
    Payment cpnConvertedWithoutIndex = CPN_INDEX_FIXED_FIRST.toDerivative(valuationDate, htsArrayWithoutIndex);
    assertEquals("CouponIborFxResetDefinition: toDerivative", cpnWithoutIndexExpected, cpnConvertedWithoutIndex);

    /*
     * Index available
     */
    DoubleTimeSeries<ZonedDateTime>[] htsArrayWithIndex = new DoubleTimeSeries[] {
        INDEX_FIXING_TS_BEFORE_FX, FX_FIXING_TS_10 };
    CouponFixedFxReset cpnWithIndexExpected = new CouponFixedFxReset(CUR_PAY, paymentTime, ACCRUAL_FACTOR, NOTIONAL,
        FIXING_RATE + SPREAD, CUR_REF, fxFixingTime, fXDeliveryTime);
    Payment cpnConvertedWithIndex = CPN_INDEX_FIXED_FIRST.toDerivative(valuationDate, htsArrayWithIndex);
    assertEquals("CouponIborFxResetDefinition: toDerivative", cpnWithIndexExpected, cpnConvertedWithIndex);
  }

  /**
   * valuationDate is after FX fixing data, but on index fixing date
   */
  @Test
  public void toDerivativeAfterFxOnIndex() {
    ZonedDateTime valuationDate = FIXING_DATE_AFTER_FX;
    double fixingTime = TimeCalculator.getTimeBetween(valuationDate, CPN_FX_FIXED_FIRST.getIborIndexFixingDate());
    double fixingPeriodStartTime = TimeCalculator.getTimeBetween(valuationDate,
        CPN_FX_FIXED_FIRST.getIborIndexFixingPeriodStartDate());
    double fixingPeriodEndTime = TimeCalculator.getTimeBetween(valuationDate,
        CPN_FX_FIXED_FIRST.getIborIndexFixingPeriodEndDate());
    double paymentTime = TimeCalculator.getTimeBetween(valuationDate, PAYMENT_DATE);

    /*
     * Index not available
     */
    DoubleTimeSeries<ZonedDateTime>[] htsArrayWithFxRateWithoutIndex = new DoubleTimeSeries[] {
        INDEX_FIXING_TS_SAME, FX_FIXING_TS_0 };
    CouponIborSpread cpnWithFxExpected = new CouponIborSpread(CUR_PAY, paymentTime, ACCRUAL_FACTOR, NOTIONAL *
        FX_FIXING_RATE, fixingTime, INDEX, fixingPeriodStartTime, fixingPeriodEndTime,
        CPN_FX_FIXED_FIRST.getIborIndexFixingPeriodAccrualFactor(), SPREAD);
    Payment cpnConvertedWithFx = CPN_FX_FIXED_FIRST.toDerivative(valuationDate, htsArrayWithFxRateWithoutIndex);
    assertEquals("CouponIborFxResetDefinition: toDerivative", cpnWithFxExpected, cpnConvertedWithFx);
    
    /*
     * Index available
     */
    DoubleTimeSeries<ZonedDateTime>[] htsArrayWithFxRateIndex = new DoubleTimeSeries[] {
        INDEX_FIXING_TS_AFTER_FX, FX_FIXING_TS_0 };
    CouponFixed cpnWithFxIndexExpected = new CouponFixed(CUR_PAY, paymentTime, ACCRUAL_FACTOR, NOTIONAL *
        FX_FIXING_RATE, FIXING_RATE + SPREAD);
    Payment cpnConvertedWithFxIndex = CPN_FX_FIXED_FIRST.toDerivative(valuationDate, htsArrayWithFxRateIndex);
    assertEquals("CouponIborFxResetDefinition: toDerivative", cpnWithFxIndexExpected, cpnConvertedWithFxIndex);
  }

  /**
   * valuationDate is before FX fixing data, but after index fixing date
   */
  @Test
  public void toDerivativeBeforeFxAfterIndex() {
    ZonedDateTime valuationDate = FX_FIXING_DATE.minusDays(1);
    double paymentTime = TimeCalculator.getTimeBetween(valuationDate, PAYMENT_DATE);
    double fxFixingTime = TimeCalculator.getTimeBetween(valuationDate, FX_FIXING_DATE);
    double fXDeliveryTime = TimeCalculator.getTimeBetween(valuationDate, FX_DELIVERY_DATE);

    DoubleTimeSeries<ZonedDateTime>[] htsArray = new DoubleTimeSeries[] {
        INDEX_FIXING_TS_BEFORE_FX, FX_FIXING_TS_1 };
    CouponFixedFxReset cpnWithIndexExpected = new CouponFixedFxReset(CUR_PAY, paymentTime, ACCRUAL_FACTOR, NOTIONAL,
        FIXING_RATE + SPREAD, CUR_REF, fxFixingTime, fXDeliveryTime);
    Payment cpnConvertedWithIndex = CPN_INDEX_FIXED_FIRST.toDerivative(valuationDate, htsArray);
    assertEquals("CouponIborFxResetDefinition: toDerivative", cpnWithIndexExpected, cpnConvertedWithIndex);
  }

  /**
   * valuationDate is after FX fixing data, but before index fixing date
   */
  @Test
  public void toDerivativeAfterFxBeforeIndex() {
    ZonedDateTime valuationDate = FX_FIXING_DATE.plusDays(1);
    double fixingTime = TimeCalculator.getTimeBetween(valuationDate, CPN_FX_FIXED_FIRST.getIborIndexFixingDate());
    double fixingPeriodStartTime = TimeCalculator.getTimeBetween(valuationDate,
        CPN_FX_FIXED_FIRST.getIborIndexFixingPeriodStartDate());
    double fixingPeriodEndTime = TimeCalculator.getTimeBetween(valuationDate,
        CPN_FX_FIXED_FIRST.getIborIndexFixingPeriodEndDate());
    double paymentTime = TimeCalculator.getTimeBetween(valuationDate, PAYMENT_DATE);

    DoubleTimeSeries<ZonedDateTime>[] htsArray = new DoubleTimeSeries[] {
        INDEX_FIXING_TS_AFTER_FX, FX_FIXING_TS_0 };
    CouponIborSpread cpnWithFxExpected = new CouponIborSpread(CUR_PAY, paymentTime, ACCRUAL_FACTOR, NOTIONAL *
        FX_FIXING_RATE, fixingTime, INDEX, fixingPeriodStartTime, fixingPeriodEndTime,
        CPN_FX_FIXED_FIRST.getIborIndexFixingPeriodAccrualFactor(), SPREAD);
    Payment cpnConvertedWithFx = CPN_FX_FIXED_FIRST.toDerivative(valuationDate, htsArray);
    assertEquals("CouponIborFxResetDefinition: toDerivative", cpnWithFxExpected, cpnConvertedWithFx);
  }
  
  /**
   * Test hashCode and equals 
   */
  @Test
  public void hashCodeEqualsTest() {
    ZonedDateTime expFixingPeriodStartDate = ScheduleCalculator.getAdjustedDate(FIXING_DATE_SAME_AS_FX,
        INDEX.getSpotLag(), CALENDAR);
    ZonedDateTime expFixingPeriodEndDate = ScheduleCalculator.getAdjustedDate(expFixingPeriodStartDate,
        INDEX.getTenor(), INDEX.getBusinessDayConvention(), CALENDAR, INDEX.isEndOfMonth());

    CouponIborFxResetDefinition cpnRe = new CouponIborFxResetDefinition(CUR_PAY, PAYMENT_DATE, ACCRUAL_START_DATE,
        ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FX_FIXING_DATE, expFixingPeriodStartDate, expFixingPeriodEndDate,
        INDEX.getDayCount().getDayCountFraction(expFixingPeriodStartDate, expFixingPeriodEndDate, CALENDAR), INDEX,
        SPREAD, CALENDAR, CUR_REF, FX_FIXING_DATE, FX_DELIVERY_DATE);

    assertFalse(cpnRe.hashCode() == CPN_FX_FIXED_FIRST.hashCode());
    assertFalse(cpnRe.equals(CPN_FX_FIXED_FIRST));
    assertFalse(cpnRe.equals(CPN_FX_FIXED_FIRST.getIndex()));

    assertTrue(cpnRe.equals(CPN_SAME_FIXING_DATES));
    assertTrue(cpnRe.hashCode() == CPN_SAME_FIXING_DATES.hashCode());
    assertTrue(cpnRe.equals(cpnRe));

    CouponIborFxResetDefinition cpn2 = new CouponIborFxResetDefinition(CUR_PAY, PAYMENT_DATE, ACCRUAL_START_DATE,
        ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FX_FIXING_DATE, expFixingPeriodStartDate, expFixingPeriodEndDate,
        INDEX.getDayCount().getDayCountFraction(expFixingPeriodStartDate, expFixingPeriodEndDate, CALENDAR), INDEX,
        SPREAD, CALENDAR, Currency.JPY, FX_FIXING_DATE, FX_DELIVERY_DATE);
    CouponIborFxResetDefinition cpn3 = new CouponIborFxResetDefinition(CUR_PAY, PAYMENT_DATE.plusDays(1),
        ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FX_FIXING_DATE, expFixingPeriodStartDate,
        expFixingPeriodEndDate, INDEX.getDayCount().getDayCountFraction(expFixingPeriodStartDate,
            expFixingPeriodEndDate, CALENDAR), INDEX, SPREAD, CALENDAR, CUR_REF, FX_FIXING_DATE, FX_DELIVERY_DATE);
    CouponIborFxResetDefinition cpn4 = new CouponIborFxResetDefinition(CUR_PAY, PAYMENT_DATE,
        ACCRUAL_START_DATE.plusDays(1), ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FX_FIXING_DATE,
        expFixingPeriodStartDate, expFixingPeriodEndDate, INDEX.getDayCount().getDayCountFraction(
            expFixingPeriodStartDate, expFixingPeriodEndDate, CALENDAR), INDEX, SPREAD, CALENDAR, CUR_REF,
        FX_FIXING_DATE, FX_DELIVERY_DATE);
    CouponIborFxResetDefinition cpn5 = new CouponIborFxResetDefinition(CUR_PAY, PAYMENT_DATE,
        ACCRUAL_START_DATE, ACCRUAL_END_DATE.plusDays(1), ACCRUAL_FACTOR, NOTIONAL, FX_FIXING_DATE,
        expFixingPeriodStartDate, expFixingPeriodEndDate, INDEX.getDayCount().getDayCountFraction(
            expFixingPeriodStartDate, expFixingPeriodEndDate, CALENDAR), INDEX, SPREAD, CALENDAR, CUR_REF,
        FX_FIXING_DATE, FX_DELIVERY_DATE);
    CouponIborFxResetDefinition cpn6 = new CouponIborFxResetDefinition(CUR_PAY, PAYMENT_DATE,
        ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR + 0.01, NOTIONAL, FX_FIXING_DATE,
        expFixingPeriodStartDate, expFixingPeriodEndDate, INDEX.getDayCount().getDayCountFraction(
            expFixingPeriodStartDate, expFixingPeriodEndDate, CALENDAR), INDEX, SPREAD, CALENDAR, CUR_REF,
        FX_FIXING_DATE, FX_DELIVERY_DATE);
    CouponIborFxResetDefinition cpn7 = new CouponIborFxResetDefinition(CUR_PAY, PAYMENT_DATE,
        ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL * 0.1, FX_FIXING_DATE,
        expFixingPeriodStartDate, expFixingPeriodEndDate, INDEX.getDayCount().getDayCountFraction(
            expFixingPeriodStartDate, expFixingPeriodEndDate, CALENDAR), INDEX, SPREAD, CALENDAR, CUR_REF,
        FX_FIXING_DATE, FX_DELIVERY_DATE);
    CouponIborFxResetDefinition cpn8 = new CouponIborFxResetDefinition(CUR_PAY, PAYMENT_DATE,
        ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FX_FIXING_DATE.plusDays(1),
        expFixingPeriodStartDate, expFixingPeriodEndDate, INDEX.getDayCount().getDayCountFraction(
            expFixingPeriodStartDate, expFixingPeriodEndDate, CALENDAR), INDEX, SPREAD, CALENDAR, CUR_REF,
        FX_FIXING_DATE, FX_DELIVERY_DATE);
    CouponIborFxResetDefinition cpn9 = new CouponIborFxResetDefinition(CUR_PAY, PAYMENT_DATE,
        ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FX_FIXING_DATE,
        expFixingPeriodStartDate.plusDays(1), expFixingPeriodEndDate, INDEX.getDayCount().getDayCountFraction(
            expFixingPeriodStartDate, expFixingPeriodEndDate, CALENDAR), INDEX, SPREAD, CALENDAR, CUR_REF,
        FX_FIXING_DATE, FX_DELIVERY_DATE);
    CouponIborFxResetDefinition cpn10 = new CouponIborFxResetDefinition(CUR_PAY, PAYMENT_DATE,
        ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FX_FIXING_DATE,
        expFixingPeriodStartDate, expFixingPeriodEndDate.plusDays(1), INDEX.getDayCount().getDayCountFraction(
            expFixingPeriodStartDate, expFixingPeriodEndDate, CALENDAR), INDEX, SPREAD, CALENDAR, CUR_REF,
        FX_FIXING_DATE, FX_DELIVERY_DATE);
    CouponIborFxResetDefinition cpn11 = new CouponIborFxResetDefinition(CUR_PAY, PAYMENT_DATE,
        ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FX_FIXING_DATE,
        expFixingPeriodStartDate, expFixingPeriodEndDate, INDEX.getDayCount().getDayCountFraction(
            expFixingPeriodStartDate, expFixingPeriodEndDate, CALENDAR) + 0.01, INDEX, SPREAD, CALENDAR, CUR_REF,
        FX_FIXING_DATE, FX_DELIVERY_DATE);
    CouponIborFxResetDefinition cpn12 = new CouponIborFxResetDefinition(CUR_PAY, PAYMENT_DATE,
        ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FX_FIXING_DATE,
        expFixingPeriodStartDate, expFixingPeriodEndDate, INDEX.getDayCount().getDayCountFraction(
            expFixingPeriodStartDate, expFixingPeriodEndDate, CALENDAR), INDEX, SPREAD + 0.01,
        new MondayToFridayCalendar("B"), CUR_REF,
        FX_FIXING_DATE, FX_DELIVERY_DATE);
    IborIndex index = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY,
        false, "Deprecated");
    CouponIborFxResetDefinition cpn13 = new CouponIborFxResetDefinition(CUR_PAY, PAYMENT_DATE,
        ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FX_FIXING_DATE,
        expFixingPeriodStartDate, expFixingPeriodEndDate, INDEX.getDayCount().getDayCountFraction(
            expFixingPeriodStartDate, expFixingPeriodEndDate, CALENDAR), index, SPREAD, CALENDAR, CUR_REF,
        FX_FIXING_DATE, FX_DELIVERY_DATE);
    CouponIborFxResetDefinition cpn14 = new CouponIborFxResetDefinition(CUR_PAY, PAYMENT_DATE,
        ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FX_FIXING_DATE,
        expFixingPeriodStartDate, expFixingPeriodEndDate, INDEX.getDayCount().getDayCountFraction(
            expFixingPeriodStartDate, expFixingPeriodEndDate, CALENDAR), INDEX, SPREAD, CALENDAR, CUR_REF,
        FX_FIXING_DATE.plusDays(1), FX_DELIVERY_DATE);
    CouponIborFxResetDefinition cpn15 = new CouponIborFxResetDefinition(CUR_PAY, PAYMENT_DATE,
        ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FX_FIXING_DATE,
        expFixingPeriodStartDate, expFixingPeriodEndDate, INDEX.getDayCount().getDayCountFraction(
            expFixingPeriodStartDate, expFixingPeriodEndDate, CALENDAR), INDEX, SPREAD, CALENDAR, CUR_REF,
        FX_FIXING_DATE, FX_DELIVERY_DATE.plusDays(1));

    CouponIborFxResetDefinition[] cpnArray = new CouponIborFxResetDefinition[] {cpn2, cpn3, cpn4, cpn5, cpn6,
        cpn7, cpn8, cpn9, cpn10, cpn11, cpn12, cpn13, cpn14, cpn15, null };
    for (int i = 0; i < cpnArray.length; ++i) {
      assertFalse(cpnRe.equals(cpnArray[i]));
    }

  }

  /**
   * FX rate not available
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void nullFxRateTest() {
    ZonedDateTime valuationDate = FX_FIXING_DATE.minusDays(1);
    DoubleTimeSeries<ZonedDateTime>[] htsArray = new DoubleTimeSeries[] {
        INDEX_FIXING_TS_OLD, FX_FIXING_TS_1 };
    CPN_INDEX_FIXED_FIRST.toDerivative(valuationDate, htsArray);
  }

  /**
   * Index rate not available
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void nullIndexRateTest() {
    ZonedDateTime valuationDate = FX_FIXING_DATE.plusDays(1);
    DoubleTimeSeries<ZonedDateTime>[] htsArray = new DoubleTimeSeries[] {
        INDEX_FIXING_TS_AFTER_FX, FX_FIXING_TS_10 };
    CPN_FX_FIXED_FIRST.toDerivative(valuationDate, htsArray);
  }

  /**
   * time series not available
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void dateAfterFixingTest() {
    ZonedDateTime valuationDate = FX_FIXING_DATE.plusDays(1);
    CPN_SAME_FIXING_DATES.toDerivative(valuationDate);
  }

  /**
   * too small number of time series
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void singleTimeSeriesTest() {
    ZonedDateTime valuationDate = PAYMENT_DATE;
    DoubleTimeSeries<ZonedDateTime>[] htsArray = new DoubleTimeSeries[] {INDEX_FIXING_TS_SAME };
    CPN_SAME_FIXING_DATES.toDerivative(valuationDate, htsArray);
  }

  /**
   * reference data is after payment date
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void dateAfterPaymentTest() {
    ZonedDateTime valuationDate = PAYMENT_DATE.plusDays(1);
    DoubleTimeSeries<ZonedDateTime>[] htsArray = new DoubleTimeSeries[] {INDEX_FIXING_TS_SAME, FX_FIXING_TS_10 };
    CPN_SAME_FIXING_DATES.toDerivative(valuationDate, htsArray);
  }

}
