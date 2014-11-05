/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponFixedDiscountingMethod;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
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
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the OIS coupon definition.
 */
@SuppressWarnings("deprecation")
@Test(groups = TestGroup.UNIT)
public class CouponONDefinitionTest {

  private static final int EUR_SETTLEMENT_DAYS = 2;
  private static final BusinessDayConvention EUR_BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean EUR_IS_EOM = true;
  //EUR Eonia
  private static final String EUR_OIS_NAME = "EUR EONIA";
  private static final Currency EUR_CUR = Currency.EUR;
  private static final Calendar EUR_CALENDAR = new MondayToFridayCalendar("EUR");
  private static final int EUR_PUBLICATION_LAG = 0;
  private static final DayCount EUR_DAY_COUNT = DayCounts.ACT_360;
  private static final IndexON EUR_OIS = new IndexON(EUR_OIS_NAME, EUR_CUR, EUR_DAY_COUNT, EUR_PUBLICATION_LAG);
  // Coupon EONIA 3m
  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2011, 9, 7);
  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(TRADE_DATE, EUR_SETTLEMENT_DAYS, EUR_CALENDAR);
  private static final Period CPN_TENOR = Period.ofDays(7); // 1 week
  private static final ZonedDateTime START_ACCRUAL_DATE = SPOT_DATE;
  private static final ZonedDateTime EUR_END_ACCRUAL_DATE = ScheduleCalculator.getAdjustedDate(START_ACCRUAL_DATE, CPN_TENOR, EUR_BUSINESS_DAY, EUR_CALENDAR, EUR_IS_EOM);
  private static final ZonedDateTime EUR_LAST_FIXING_DATE = ScheduleCalculator.getAdjustedDate(EUR_END_ACCRUAL_DATE, -1, EUR_CALENDAR); // Overnight
  private static final ZonedDateTime EUR_LAST_PUBLICATION_DATE = ScheduleCalculator.getAdjustedDate(EUR_LAST_FIXING_DATE, EUR_PUBLICATION_LAG, EUR_CALENDAR); // Lag
  private static final ZonedDateTime EUR_PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(EUR_LAST_PUBLICATION_DATE, EUR_SETTLEMENT_DAYS, EUR_CALENDAR); // Payment is wrt last publication date
  private static final double EUR_PAYMENT_YEAR_FRACTION = EUR_DAY_COUNT.getDayCountFraction(START_ACCRUAL_DATE, EUR_END_ACCRUAL_DATE);
  private static final double NOTIONAL = 100000000;
  private static final double EUR_FIXING_YEAR_FRACTION = EUR_DAY_COUNT.getDayCountFraction(START_ACCRUAL_DATE, EUR_END_ACCRUAL_DATE);

  private static final CouponONDefinition EONIA_COUPON_DEFINITION = new CouponONDefinition(EUR_CUR, EUR_PAYMENT_DATE, START_ACCRUAL_DATE, EUR_END_ACCRUAL_DATE, EUR_PAYMENT_YEAR_FRACTION, NOTIONAL,
      EUR_OIS, START_ACCRUAL_DATE, EUR_END_ACCRUAL_DATE, EUR_CALENDAR);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullIndex() {
    new CouponONDefinition(EUR_CUR, EUR_PAYMENT_DATE, START_ACCRUAL_DATE, EUR_END_ACCRUAL_DATE, EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, null, START_ACCRUAL_DATE, EUR_END_ACCRUAL_DATE, EUR_CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullStartFixing() {
    new CouponONDefinition(EUR_CUR, EUR_PAYMENT_DATE, START_ACCRUAL_DATE, EUR_END_ACCRUAL_DATE, EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, null, EUR_END_ACCRUAL_DATE, EUR_CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullEndFixing() {
    new CouponONDefinition(EUR_CUR, EUR_PAYMENT_DATE, START_ACCRUAL_DATE, EUR_END_ACCRUAL_DATE, EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, START_ACCRUAL_DATE, null, EUR_CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void inmcompatibleCurrency() {
    new CouponONDefinition(Currency.USD, EUR_PAYMENT_DATE, START_ACCRUAL_DATE, EUR_END_ACCRUAL_DATE, EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, START_ACCRUAL_DATE, EUR_END_ACCRUAL_DATE,
        EUR_CALENDAR);
  }

  @Test
  public void getter() {
    assertEquals("CouponOISSimplified definition: getter", EUR_OIS, EONIA_COUPON_DEFINITION.getIndex());
    final ZonedDateTime[] FixingDateArray = EONIA_COUPON_DEFINITION.getFixingPeriodDate();
    final ZonedDateTime firstFixingPeriod = FixingDateArray[0];
    final ZonedDateTime lastFixingPeriodEndDate = FixingDateArray[FixingDateArray.length - 1];
    assertEquals("CouponOISSimplified definition: getter", START_ACCRUAL_DATE, firstFixingPeriod);
    assertEquals("CouponOISSimplified definition: getter", lastFixingPeriodEndDate, EUR_END_ACCRUAL_DATE);
    assertEquals("CouponOISSimplified definition: getter", EUR_LAST_FIXING_DATE, LocalDateTime.of(2011, 9, 15, 0, 0, 0, 0).atZone(ZoneOffset.UTC));
    assertEquals("CouponOISSimplified definition: getter", lastFixingPeriodEndDate, LocalDateTime.of(2011, 9, 16, 0, 0, 0, 0).atZone(ZoneOffset.UTC));
    double aftot = 0.0;
    for (int loopperiod = 0; loopperiod < EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor().length; loopperiod++) {
      aftot += EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[loopperiod];
    }
    assertEquals("CouponOISSimplified definition: getter", EUR_FIXING_YEAR_FRACTION, aftot, 1.0E-10);
  }

  @Test
  /**
   * Tests the builder from financial details.
   */
  public void from() {
    final CouponONDefinition cpnFrom = CouponONDefinition.from(EUR_OIS, SPOT_DATE, CPN_TENOR, NOTIONAL, EUR_SETTLEMENT_DAYS, EUR_BUSINESS_DAY, EUR_IS_EOM, EUR_CALENDAR);
    assertEquals("CouponOISSimplified definition: from", cpnFrom, EONIA_COUPON_DEFINITION);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeNoFixing() {
    final CouponON cpnConverted = EONIA_COUPON_DEFINITION.toDerivative(TRADE_DATE);
    final double paymentTime = TimeCalculator.getTimeBetween(TRADE_DATE, EUR_PAYMENT_DATE);
    final double fixingStartTime = TimeCalculator.getTimeBetween(TRADE_DATE, START_ACCRUAL_DATE);
    final double fixingEndTime = TimeCalculator.getTimeBetween(TRADE_DATE, EUR_END_ACCRUAL_DATE);
    final CouponON cpnExpected = new CouponON(EUR_CUR, paymentTime, EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, fixingStartTime, fixingEndTime, EUR_FIXING_YEAR_FRACTION,
        NOTIONAL);
    assertEquals("CouponOISSimplified definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFixingBeforeStart() {
    final ZonedDateTime referenceDate = ScheduleCalculator.getAdjustedDate(TRADE_DATE, 1, EUR_CALENDAR);
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7) }, new double[] {0.01 });
    final Payment cpnConverted = EONIA_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, EUR_PAYMENT_DATE);
    final double fixingStartTime = TimeCalculator.getTimeBetween(referenceDate, START_ACCRUAL_DATE);
    final double fixingEndTime = TimeCalculator.getTimeBetween(referenceDate, EUR_END_ACCRUAL_DATE);
    final CouponON cpnExpected = new CouponON(EUR_CUR, paymentTime, EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, fixingStartTime, fixingEndTime, EUR_FIXING_YEAR_FRACTION,
        NOTIONAL);
    assertEquals("CouponOISSimplified definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFixingOnStartNotYetFixed() {
    final ZonedDateTime referenceDate = SPOT_DATE;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8) },
        new double[] {
          0.01,
          0.01 });
    final Payment cpnConverted = EONIA_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, EUR_PAYMENT_DATE);
    final double fixingStartTime = TimeCalculator.getTimeBetween(referenceDate, START_ACCRUAL_DATE);
    final double fixingEndTime = TimeCalculator.getTimeBetween(referenceDate, EUR_END_ACCRUAL_DATE);
    final CouponON cpnExpected = new CouponON(EUR_CUR, paymentTime, EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, fixingStartTime, fixingEndTime, EUR_FIXING_YEAR_FRACTION,
        NOTIONAL);
    assertEquals("CouponOISSimplified definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFixingOnStartFixed() {
    final ZonedDateTime referenceDate = SPOT_DATE;
    final double fixingRate = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8),
      DateUtils.getUTCDate(2011, 9, 9) }, new double[] {fixingRate, fixingRate, fixingRate });
    final Payment cpnConverted = EONIA_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS);
    final ZonedDateTime startFixingLeft = ScheduleCalculator.getAdjustedDate(referenceDate, 1, EUR_CALENDAR);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, EUR_PAYMENT_DATE);
    final double fixingStartTime = TimeCalculator.getTimeBetween(referenceDate, startFixingLeft);
    final double fixingEndTime = TimeCalculator.getTimeBetween(referenceDate, EUR_END_ACCRUAL_DATE);
    final double notionalIncreased = NOTIONAL * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[0]);
    double yearFractionLeft = 0.0;
    for (int loopperiod = 1; loopperiod < EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor().length; loopperiod++) {
      yearFractionLeft += EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[loopperiod];
    }
    final CouponON cpnExpected = new CouponON(EUR_CUR, paymentTime, EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, fixingStartTime, fixingEndTime, yearFractionLeft,
        notionalIncreased);
    assertEquals("CouponOISSimplified definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFixingMiddleNotYetFixed() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 9, 13);
    final double fixingRate = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8),
      DateUtils.getUTCDate(2011, 9, 9), DateUtils.getUTCDate(2011, 9, 12) }, new double[] {fixingRate, fixingRate, fixingRate, fixingRate });
    final Payment cpnConverted = EONIA_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS);
    final ZonedDateTime startFixingLeft = referenceDate;
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, EUR_PAYMENT_DATE);
    final double fixingStartTime = TimeCalculator.getTimeBetween(referenceDate, startFixingLeft);
    final double fixingEndTime = TimeCalculator.getTimeBetween(referenceDate, EUR_END_ACCRUAL_DATE);
    final double notionalIncreased = NOTIONAL * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[0]) *
        (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[1]);
    double yearFractionLeft = 0.0;
    for (int loopperiod = 2; loopperiod < EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor().length; loopperiod++) {
      yearFractionLeft += EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[loopperiod];
    }
    final CouponON cpnExpected = new CouponON(EUR_CUR, paymentTime, EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, fixingStartTime, fixingEndTime, yearFractionLeft,
        notionalIncreased);
    assertEquals("CouponOISSimplified definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFixingMiddleFixed() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 9, 13);
    final double fixingRate = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8),
      DateUtils.getUTCDate(2011, 9, 9), DateUtils.getUTCDate(2011, 9, 12), DateUtils.getUTCDate(2011, 9, 13) }, new double[] {fixingRate, fixingRate, fixingRate, fixingRate, fixingRate });
    final Payment cpnConverted = EONIA_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS);
    final ZonedDateTime startFixingLeft = ScheduleCalculator.getAdjustedDate(referenceDate, 1, EUR_CALENDAR);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, EUR_PAYMENT_DATE);
    final double fixingStartTime = TimeCalculator.getTimeBetween(referenceDate, startFixingLeft);
    final double fixingEndTime = TimeCalculator.getTimeBetween(referenceDate, EUR_END_ACCRUAL_DATE);
    final double notionalIncreased = NOTIONAL * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[0]) *
        (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[1])
        * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[2]);
    double yearFractionLeft = 0.0;
    for (int loopperiod = 3; loopperiod < EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor().length; loopperiod++) {
      yearFractionLeft += EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[loopperiod];
    }
    final CouponON cpnExpected = new CouponON(EUR_CUR, paymentTime, EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, fixingStartTime, fixingEndTime, yearFractionLeft,
        notionalIncreased);
    assertEquals("CouponOISSimplified definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFixingSecondLastFixed() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 9, 15);
    final double fixingRate = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8),
      DateUtils.getUTCDate(2011, 9, 9), DateUtils.getUTCDate(2011, 9, 12), DateUtils.getUTCDate(2011, 9, 13), DateUtils.getUTCDate(2011, 9, 14), DateUtils.getUTCDate(2011, 9, 15) }, new double[] {
      fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate });
    final Payment cpnConverted = EONIA_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, EUR_PAYMENT_DATE);
    final double notionalIncreased = NOTIONAL * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[0]) *
        (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[1])
        * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[2]) * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[3])
        * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[4]);
    final CouponFixed cpnExpected = new CouponFixed(EUR_CUR, paymentTime, EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, (notionalIncreased / NOTIONAL - 1.0) / EUR_PAYMENT_YEAR_FRACTION);
    assertEquals("CouponOISSimplified definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method. Reference date is after the last fixing date and all the fixing are known.
   */
  public void toDerivativeFixingLast() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 9, 16);
    final double fixingRate = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8),
      DateUtils.getUTCDate(2011, 9, 9), DateUtils.getUTCDate(2011, 9, 12), DateUtils.getUTCDate(2011, 9, 13), DateUtils.getUTCDate(2011, 9, 14), DateUtils.getUTCDate(2011, 9, 15) }, new double[] {
      fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate });
    final Payment cpnConverted = EONIA_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, EUR_PAYMENT_DATE);
    final double notionalIncreased = NOTIONAL * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[0]) *
        (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[1])
        * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[2]) * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[3])
        * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[4]);
    final CouponFixed cpnExpected = new CouponFixed(EUR_CUR, paymentTime, EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, (notionalIncreased / NOTIONAL - 1.0) / EUR_PAYMENT_YEAR_FRACTION);
    assertEquals("CouponOISSimplified definition: toDerivative", cpnExpected, cpnConverted);
  }

  // TODO: add a couple of test on the last fixing date, with and without the fixing present.

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeAfterLast() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 9, 19);
    final double fixingRate = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8),
      DateUtils.getUTCDate(2011, 9, 9), DateUtils.getUTCDate(2011, 9, 12), DateUtils.getUTCDate(2011, 9, 13), DateUtils.getUTCDate(2011, 9, 14), DateUtils.getUTCDate(2011, 9, 15) }, new double[] {
      fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate });
    final Payment cpnConverted = EONIA_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, EUR_PAYMENT_DATE);
    final double notionalIncreased = NOTIONAL * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[0]) *
        (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[1])
        * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[2]) * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[3])
        * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[4]);
    final CouponFixed cpnExpected = new CouponFixed(EUR_CUR, paymentTime, EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, (notionalIncreased / NOTIONAL - 1.0) / EUR_PAYMENT_YEAR_FRACTION);
    assertEquals("CouponOISSimplified definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method on the payment date. valuation is at noon, payment set at midnight...
   */
  public void toDerivativeJustAfterPayment() {
    final ZonedDateTime valuationTimeIsNoon = DateUtils.getUTCDate(2011, 9, 19, 12, 0);
    assertTrue("valuationTimeIsNoon used to be after paymentDate, which was midnight. Confirm behaviour", valuationTimeIsNoon.isAfter(EONIA_COUPON_DEFINITION.getPaymentDate()));
    final double fixingRate = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8),
      DateUtils.getUTCDate(2011, 9, 9), DateUtils.getUTCDate(2011, 9, 12), DateUtils.getUTCDate(2011, 9, 13), DateUtils.getUTCDate(2011, 9, 14), DateUtils.getUTCDate(2011, 9, 15) }, new double[] {
      fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate });
    final Payment cpnConverted = EONIA_COUPON_DEFINITION.toDerivative(valuationTimeIsNoon, fixingTS);
    final double paymentTime = TimeCalculator.getTimeBetween(valuationTimeIsNoon, EUR_PAYMENT_DATE);
    final double notionalIncreased = NOTIONAL * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[0]) *
        (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[1])
        * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[2]) * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[3])
        * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[4]);

    final CouponFixed cpnExpected = new CouponFixed(EUR_CUR, paymentTime, EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, (notionalIncreased / NOTIONAL - 1.0) / EUR_PAYMENT_YEAR_FRACTION);
    assertEquals("CouponOISSimplified definition: toDerivative", cpnExpected, cpnConverted);

    // Test pricing, too. Notice that the value of a coupon on its payment date is non-zero
    final MulticurveProviderDiscount curves = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
    final MultipleCurrencyAmount pvConverted = com.opengamma.analytics.financial.interestrate.payments.provider.CouponFixedDiscountingMethod.getInstance().presentValue((CouponFixed) cpnConverted,
        curves);
    final MultipleCurrencyAmount pvExpected = com.opengamma.analytics.financial.interestrate.payments.provider.CouponFixedDiscountingMethod.getInstance().presentValue(cpnExpected, curves);
    assertEquals("CouponOIS definition: toDerivative", pvConverted, pvExpected);
    assertEquals("CouponOIS definition: toDerivative", pvConverted, MultipleCurrencyAmount.of(EUR_CUR, 19445.833380471457));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  /**
   * Tests the toDerivative method: after payment date
   */
  public void toDerivativeAfterPayment() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 9, 20);
    final double fixingRate = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8),
      DateUtils.getUTCDate(2011, 9, 9), DateUtils.getUTCDate(2011, 9, 12), DateUtils.getUTCDate(2011, 9, 13), DateUtils.getUTCDate(2011, 9, 14), DateUtils.getUTCDate(2011, 9, 15) }, new double[] {
      fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate });
    EONIA_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS);
  }

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final GeneratorSwapFixedON USD_GENERATOR = GeneratorSwapFixedONMaster.getInstance().getGenerator("USD1YFEDFUND", NYC);
  private static final IndexON USD_FEDFUND = USD_GENERATOR.getIndex();

  private static final ZonedDateTime USD_END_ACCRUAL_DATE = ScheduleCalculator.getAdjustedDate(START_ACCRUAL_DATE, CPN_TENOR, USD_GENERATOR.getBusinessDayConvention(), NYC,
      USD_GENERATOR.isEndOfMonth());
  private static ZonedDateTime USD_LAST_FIXING_DATE = ScheduleCalculator.getAdjustedDate(USD_END_ACCRUAL_DATE, -1, NYC); // Overnight
  static {
    USD_LAST_FIXING_DATE = ScheduleCalculator.getAdjustedDate(USD_LAST_FIXING_DATE, USD_GENERATOR.getIndex().getPublicationLag(), NYC); // Lag
  }
  private static final ZonedDateTime USD_PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(USD_LAST_FIXING_DATE, USD_GENERATOR.getSpotLag(), NYC);
  private static final double USD_PAYMENT_YEAR_FRACTION = USD_GENERATOR.getFixedLegDayCount().getDayCountFraction(START_ACCRUAL_DATE, USD_END_ACCRUAL_DATE);
  private static final double USD_FIXING_YEAR_FRACTION = USD_GENERATOR.getFixedLegDayCount().getDayCountFraction(START_ACCRUAL_DATE, USD_END_ACCRUAL_DATE);

  private static final CouponONDefinition OIS_COUPON_DEFINITION = new CouponONDefinition(USD_FEDFUND.getCurrency(), USD_PAYMENT_DATE, START_ACCRUAL_DATE, USD_END_ACCRUAL_DATE,
      USD_PAYMENT_YEAR_FRACTION, NOTIONAL, USD_FEDFUND, START_ACCRUAL_DATE, USD_END_ACCRUAL_DATE, EUR_CALENDAR);

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeUSDNoFixingOnFirst() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 9, 12);
    final double fixingRate = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8) },
        new double[] {
          fixingRate, fixingRate });
    final Payment cpnConverted = OIS_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS);
    final ZonedDateTime startFixingLeft = DateUtils.getUTCDate(2011, 9, 9);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, USD_PAYMENT_DATE);
    final double fixingStartTime = TimeCalculator.getTimeBetween(referenceDate, startFixingLeft);
    final double fixingEndTime = TimeCalculator.getTimeBetween(referenceDate, USD_END_ACCRUAL_DATE);
    final double notionalIncreased = NOTIONAL;
    final double yearFractionLeft = USD_FIXING_YEAR_FRACTION;
    final CouponON cpnExpected = new CouponON(USD_FEDFUND.getCurrency(), paymentTime, USD_PAYMENT_YEAR_FRACTION, NOTIONAL, USD_FEDFUND, fixingStartTime, fixingEndTime, yearFractionLeft,
        notionalIncreased);
    assertEquals("CouponOISSimplified definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeUSDFixingOnFirst() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 9, 12);
    final ZonedDateTime[] fixingZDTs = {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8), DateUtils.getUTCDate(2011, 9, 9), DateUtils.getUTCDate(2011, 9, 12) };
    final double[] fixingRates = {0.01, 0.011, 0.012, 0.13 };
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(fixingZDTs, fixingRates);
    final Payment cpnConverted = OIS_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS);
    final ZonedDateTime startFixingLeft = DateUtils.getUTCDate(2011, 9, 12);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, USD_PAYMENT_DATE);
    final double fixingStartTime = TimeCalculator.getTimeBetween(referenceDate, startFixingLeft);
    final double fixingEndTime = TimeCalculator.getTimeBetween(referenceDate, USD_END_ACCRUAL_DATE);
    final double notionalIncreased = NOTIONAL * (1 + fixingRates[2] * OIS_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[0]);
    double yearFractionLeft = 0.0;
    for (int loopperiod = 1; loopperiod < OIS_COUPON_DEFINITION.getFixingPeriodAccrualFactor().length; loopperiod++) {
      yearFractionLeft += OIS_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[loopperiod];
    }
    final CouponON cpnExpected = new CouponON(USD_FEDFUND.getCurrency(), paymentTime, USD_PAYMENT_YEAR_FRACTION, NOTIONAL, USD_FEDFUND, fixingStartTime, fixingEndTime,
        yearFractionLeft, notionalIncreased);
    assertEquals("CouponOISSimplified definition: toDerivative", cpnExpected, cpnConverted);
  }
}
