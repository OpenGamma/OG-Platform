/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.payment;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.IndexON;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.financial.interestrate.payments.derivative.CouponOIS;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.TimeCalculator;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;

/**
 * Tests related to the OIS coupon definition.
 */
public class CouponOISDefinitionTest {

  private static final int EUR_SETTLEMENT_DAYS = 2;
  private static final BusinessDayConvention EUR_BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean EUR_IS_EOM = true;
  //EUR Eonia
  private static final String EUR_OIS_NAME = "EUR EONIA";
  private static final Currency EUR_CUR = Currency.EUR;
  private static final Calendar EUR_CALENDAR = new MondayToFridayCalendar("EUR");
  private static final int EUR_PUBLICATION_LAG = 0;
  private static final DayCount EUR_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final IndexON EUR_OIS = new IndexON(EUR_OIS_NAME, EUR_CUR, EUR_DAY_COUNT, EUR_PUBLICATION_LAG, EUR_CALENDAR);
  // Coupon EONIA 3m
  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2011, 9, 7);
  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(TRADE_DATE, EUR_CALENDAR, EUR_SETTLEMENT_DAYS);
  private static final Period EUR_CPN_TENOR = Period.ofDays(7); // 1 week
  private static final ZonedDateTime START_ACCRUAL_DATE = SPOT_DATE;
  private static final ZonedDateTime END_ACCRUAL_DATE = ScheduleCalculator.getAdjustedDate(START_ACCRUAL_DATE, EUR_BUSINESS_DAY, EUR_CALENDAR, EUR_IS_EOM, EUR_CPN_TENOR);
  private static ZonedDateTime LAST_FIXING_DATE = ScheduleCalculator.getAdjustedDate(END_ACCRUAL_DATE, EUR_CALENDAR, -1); // Overnight
  static {
    LAST_FIXING_DATE = ScheduleCalculator.getAdjustedDate(LAST_FIXING_DATE, EUR_CALENDAR, EUR_PUBLICATION_LAG); // Lag
  }
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(LAST_FIXING_DATE, EUR_CALENDAR, EUR_SETTLEMENT_DAYS);
  private static final double PAYMENT_YEAR_FRACTION = EUR_DAY_COUNT.getDayCountFraction(START_ACCRUAL_DATE, END_ACCRUAL_DATE);
  private static final double NOTIONAL = 100000000;
  private static final double FIXING_YEAR_FRACTION = EUR_DAY_COUNT.getDayCountFraction(START_ACCRUAL_DATE, END_ACCRUAL_DATE);
  //  private static final CouponOISSimplifiedDefinition EONIA_COUPON_DEFINITION = new CouponOISSimplifiedDefinition(EUR_CUR, PAYMENT_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, PAYMENT_YEAR_FRACTION,
  //      NOTIONAL, EUR_OIS, START_ACCRUAL_DATE, END_ACCRUAL_DATE, FIXING_YEAR_FRACTION);

  private static final CouponOISDefinition EONIA_COUPON_DEFINITION = new CouponOISDefinition(EUR_CUR, PAYMENT_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS,
      START_ACCRUAL_DATE, END_ACCRUAL_DATE);

  private static final YieldCurveBundle CURVES = TestsDataSets.createCurves1();
  private static final String[] CURVES_NAMES = CURVES.getAllNames().toArray(new String[0]);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullIndex() {
    new CouponOISDefinition(EUR_CUR, PAYMENT_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, PAYMENT_YEAR_FRACTION, NOTIONAL, null, START_ACCRUAL_DATE, END_ACCRUAL_DATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullStartFixing() {
    new CouponOISDefinition(EUR_CUR, PAYMENT_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, null, END_ACCRUAL_DATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullEndFixing() {
    new CouponOISDefinition(EUR_CUR, PAYMENT_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, START_ACCRUAL_DATE, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void inmcompatibleCurrency() {
    new CouponOISDefinition(Currency.USD, PAYMENT_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, START_ACCRUAL_DATE, END_ACCRUAL_DATE);
  }

  @Test
  public void getter() {
    assertEquals("CouponOISSimplified definition: getter", EUR_OIS, EONIA_COUPON_DEFINITION.getIndex());
    assertEquals("CouponOISSimplified definition: getter", START_ACCRUAL_DATE, EONIA_COUPON_DEFINITION.getFixingPeriodDate()[0]);
    assertEquals("CouponOISSimplified definition: getter", END_ACCRUAL_DATE, EONIA_COUPON_DEFINITION.getFixingPeriodDate()[EONIA_COUPON_DEFINITION.getFixingPeriodDate().length - 1]);
    double aftot = 0.0;
    for (int loopperiod = 0; loopperiod < EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor().length; loopperiod++) {
      aftot += EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[loopperiod];
    }
    assertEquals("CouponOISSimplified definition: getter", FIXING_YEAR_FRACTION, aftot, 1.0E-10);
  }

  @Test
  /**
   * Tests the builder from financial details.
   */
  public void from() {
    CouponOISDefinition cpnFrom = CouponOISDefinition.from(EUR_OIS, SPOT_DATE, EUR_CPN_TENOR, NOTIONAL, EUR_SETTLEMENT_DAYS, EUR_BUSINESS_DAY, EUR_IS_EOM);
    assertEquals("CouponOISSimplified definition: from", cpnFrom, EONIA_COUPON_DEFINITION);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeNoFixing() {
    CouponOIS cpnConverted = EONIA_COUPON_DEFINITION.toDerivative(TRADE_DATE, CURVES_NAMES);
    double paymentTime = TimeCalculator.getTimeBetween(TRADE_DATE, PAYMENT_DATE);
    double fixingStartTime = TimeCalculator.getTimeBetween(TRADE_DATE, START_ACCRUAL_DATE);
    double fixingEndTime = TimeCalculator.getTimeBetween(TRADE_DATE, END_ACCRUAL_DATE);
    CouponOIS cpnExpected = new CouponOIS(EUR_CUR, paymentTime, CURVES_NAMES[0], PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, fixingStartTime, fixingEndTime, FIXING_YEAR_FRACTION, NOTIONAL,
        CURVES_NAMES[1]);
    assertEquals("CouponOISSimplified definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFixingBeforeStart() {
    ZonedDateTime referenceDate = ScheduleCalculator.getAdjustedDate(TRADE_DATE, EUR_CALENDAR, 1);
    DoubleTimeSeries<ZonedDateTime> fixingTS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7)}, new double[] {0.01});
    Payment cpnConverted = EONIA_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS, CURVES_NAMES);
    double paymentTime = TimeCalculator.getTimeBetween(referenceDate, PAYMENT_DATE);
    double fixingStartTime = TimeCalculator.getTimeBetween(referenceDate, START_ACCRUAL_DATE);
    double fixingEndTime = TimeCalculator.getTimeBetween(referenceDate, END_ACCRUAL_DATE);
    CouponOIS cpnExpected = new CouponOIS(EUR_CUR, paymentTime, CURVES_NAMES[0], PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, fixingStartTime, fixingEndTime, FIXING_YEAR_FRACTION, NOTIONAL,
        CURVES_NAMES[1]);
    assertEquals("CouponOISSimplified definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFixingOnStartNotYetFixed() {
    ZonedDateTime referenceDate = SPOT_DATE;
    DoubleTimeSeries<ZonedDateTime> fixingTS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8)}, new double[] {0.01,
        0.01});
    Payment cpnConverted = EONIA_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS, CURVES_NAMES);
    double paymentTime = TimeCalculator.getTimeBetween(referenceDate, PAYMENT_DATE);
    double fixingStartTime = TimeCalculator.getTimeBetween(referenceDate, START_ACCRUAL_DATE);
    double fixingEndTime = TimeCalculator.getTimeBetween(referenceDate, END_ACCRUAL_DATE);
    CouponOIS cpnExpected = new CouponOIS(EUR_CUR, paymentTime, CURVES_NAMES[0], PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, fixingStartTime, fixingEndTime, FIXING_YEAR_FRACTION, NOTIONAL,
        CURVES_NAMES[1]);
    assertEquals("CouponOISSimplified definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFixingOnStartFixed() {
    ZonedDateTime referenceDate = SPOT_DATE;
    double fixingRate = 0.01;
    DoubleTimeSeries<ZonedDateTime> fixingTS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8),
        DateUtils.getUTCDate(2011, 9, 9)}, new double[] {fixingRate, fixingRate, fixingRate});
    Payment cpnConverted = EONIA_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS, CURVES_NAMES);
    ZonedDateTime startFixingLeft = ScheduleCalculator.getAdjustedDate(referenceDate, EUR_CALENDAR, 1);
    double paymentTime = TimeCalculator.getTimeBetween(referenceDate, PAYMENT_DATE);
    double fixingStartTime = TimeCalculator.getTimeBetween(referenceDate, startFixingLeft);
    double fixingEndTime = TimeCalculator.getTimeBetween(referenceDate, END_ACCRUAL_DATE);
    double notionalIncreased = NOTIONAL * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[0]);
    double yearFractionLeft = 0.0;
    for (int loopperiod = 1; loopperiod < EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor().length; loopperiod++) {
      yearFractionLeft += EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[loopperiod];
    }
    CouponOIS cpnExpected = new CouponOIS(EUR_CUR, paymentTime, CURVES_NAMES[0], PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, fixingStartTime, fixingEndTime, yearFractionLeft, notionalIncreased,
        CURVES_NAMES[1]);
    assertEquals("CouponOISSimplified definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFixingMiddleNotYetFixed() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 9, 13);
    double fixingRate = 0.01;
    DoubleTimeSeries<ZonedDateTime> fixingTS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8),
        DateUtils.getUTCDate(2011, 9, 9), DateUtils.getUTCDate(2011, 9, 12)}, new double[] {fixingRate, fixingRate, fixingRate, fixingRate});
    Payment cpnConverted = EONIA_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS, CURVES_NAMES);
    ZonedDateTime startFixingLeft = referenceDate;
    double paymentTime = TimeCalculator.getTimeBetween(referenceDate, PAYMENT_DATE);
    double fixingStartTime = TimeCalculator.getTimeBetween(referenceDate, startFixingLeft);
    double fixingEndTime = TimeCalculator.getTimeBetween(referenceDate, END_ACCRUAL_DATE);
    double notionalIncreased = NOTIONAL * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[0]) * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[1]);
    double yearFractionLeft = 0.0;
    for (int loopperiod = 2; loopperiod < EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor().length; loopperiod++) {
      yearFractionLeft += EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[loopperiod];
    }
    CouponOIS cpnExpected = new CouponOIS(EUR_CUR, paymentTime, CURVES_NAMES[0], PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, fixingStartTime, fixingEndTime, yearFractionLeft, notionalIncreased,
        CURVES_NAMES[1]);
    assertEquals("CouponOISSimplified definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFixingMiddleFixed() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 9, 13);
    double fixingRate = 0.01;
    DoubleTimeSeries<ZonedDateTime> fixingTS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8),
        DateUtils.getUTCDate(2011, 9, 9), DateUtils.getUTCDate(2011, 9, 12), DateUtils.getUTCDate(2011, 9, 13)}, new double[] {fixingRate, fixingRate, fixingRate, fixingRate, fixingRate});
    Payment cpnConverted = EONIA_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS, CURVES_NAMES);
    ZonedDateTime startFixingLeft = ScheduleCalculator.getAdjustedDate(referenceDate, EUR_CALENDAR, 1);
    double paymentTime = TimeCalculator.getTimeBetween(referenceDate, PAYMENT_DATE);
    double fixingStartTime = TimeCalculator.getTimeBetween(referenceDate, startFixingLeft);
    double fixingEndTime = TimeCalculator.getTimeBetween(referenceDate, END_ACCRUAL_DATE);
    double notionalIncreased = NOTIONAL * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[0]) * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[1])
        * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[2]);
    double yearFractionLeft = 0.0;
    for (int loopperiod = 3; loopperiod < EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor().length; loopperiod++) {
      yearFractionLeft += EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[loopperiod];
    }
    CouponOIS cpnExpected = new CouponOIS(EUR_CUR, paymentTime, CURVES_NAMES[0], PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, fixingStartTime, fixingEndTime, yearFractionLeft, notionalIncreased,
        CURVES_NAMES[1]);
    assertEquals("CouponOISSimplified definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFixingSecondLastFixed() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 9, 15);
    double fixingRate = 0.01;
    DoubleTimeSeries<ZonedDateTime> fixingTS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8),
        DateUtils.getUTCDate(2011, 9, 9), DateUtils.getUTCDate(2011, 9, 12), DateUtils.getUTCDate(2011, 9, 13), DateUtils.getUTCDate(2011, 9, 14), DateUtils.getUTCDate(2011, 9, 15)}, new double[] {
        fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate});
    Payment cpnConverted = EONIA_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS, CURVES_NAMES);
    double paymentTime = TimeCalculator.getTimeBetween(referenceDate, PAYMENT_DATE);
    double notionalIncreased = NOTIONAL * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[0]) * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[1])
        * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[2]) * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[3])
        * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[4]);
    PaymentFixed cpnExpected = new PaymentFixed(EUR_CUR, paymentTime, notionalIncreased, CURVES_NAMES[0]);
    assertEquals("CouponOISSimplified definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFixingLast() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 9, 16);
    double fixingRate = 0.01;
    DoubleTimeSeries<ZonedDateTime> fixingTS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8),
        DateUtils.getUTCDate(2011, 9, 9), DateUtils.getUTCDate(2011, 9, 12), DateUtils.getUTCDate(2011, 9, 13), DateUtils.getUTCDate(2011, 9, 14), DateUtils.getUTCDate(2011, 9, 15)}, new double[] {
        fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate});
    Payment cpnConverted = EONIA_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS, CURVES_NAMES);
    double paymentTime = TimeCalculator.getTimeBetween(referenceDate, PAYMENT_DATE);
    double notionalIncreased = NOTIONAL * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[0]) * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[1])
        * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[2]) * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[3])
        * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[4]);
    PaymentFixed cpnExpected = new PaymentFixed(EUR_CUR, paymentTime, notionalIncreased, CURVES_NAMES[0]);
    assertEquals("CouponOISSimplified definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeAfterLast() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 9, 19);
    double fixingRate = 0.01;
    DoubleTimeSeries<ZonedDateTime> fixingTS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8),
        DateUtils.getUTCDate(2011, 9, 9), DateUtils.getUTCDate(2011, 9, 12), DateUtils.getUTCDate(2011, 9, 13), DateUtils.getUTCDate(2011, 9, 14), DateUtils.getUTCDate(2011, 9, 15)}, new double[] {
        fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate});
    Payment cpnConverted = EONIA_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS, CURVES_NAMES);
    double paymentTime = TimeCalculator.getTimeBetween(referenceDate, PAYMENT_DATE);
    double notionalIncreased = NOTIONAL * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[0]) * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[1])
        * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[2]) * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[3])
        * (1 + fixingRate * EONIA_COUPON_DEFINITION.getFixingPeriodAccrualFactor()[4]);
    PaymentFixed cpnExpected = new PaymentFixed(EUR_CUR, paymentTime, notionalIncreased, CURVES_NAMES[0]);
    assertEquals("CouponOISSimplified definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  /**
   * Tests the toDerivative method: after payment date
   */
  public void toDerivativeAfterPayment() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 9, 20);
    double fixingRate = 0.01;
    DoubleTimeSeries<ZonedDateTime> fixingTS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8),
        DateUtils.getUTCDate(2011, 9, 9), DateUtils.getUTCDate(2011, 9, 12), DateUtils.getUTCDate(2011, 9, 13), DateUtils.getUTCDate(2011, 9, 14), DateUtils.getUTCDate(2011, 9, 15)}, new double[] {
        fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate});
    EONIA_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS, CURVES_NAMES);
  }

}
