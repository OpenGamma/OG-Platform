/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONCompounded;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponFixedDiscountingMethod;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.analytics.util.time.TimeCalculatorBUS252;
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
 * Tests related to the ON compounded coupon definition.
 */
@SuppressWarnings("deprecation")
@Test(groups = TestGroup.UNIT)
public class CouponONCompoundedDefinitionTest {

  private static final int EUR_SETTLEMENT_DAYS = 2;
  private static final BusinessDayConvention EUR_BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean EUR_IS_EOM = true;
  //EUR Eonia
  private static final String EUR_OIS_NAME = "EUR EONIA";
  private static final Currency EUR_CUR = Currency.EUR;
  private static final Calendar EUR_CALENDAR = new MondayToFridayCalendar("EUR");
  private static final int EUR_PUBLICATION_LAG = 0;
  private static final DayCount EUR_DAY_COUNT = DayCounts.BUSINESS_252;
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
  private static final double EUR_PAYMENT_YEAR_FRACTION = EUR_DAY_COUNT.getDayCountFraction(START_ACCRUAL_DATE, EUR_END_ACCRUAL_DATE, EUR_CALENDAR);
  private static final double NOTIONAL = 100000000;
  private static final double EUR_FIXING_YEAR_FRACTION = EUR_DAY_COUNT.getDayCountFraction(START_ACCRUAL_DATE, EUR_END_ACCRUAL_DATE, EUR_CALENDAR);

  private static final CouponONCompoundedDefinition ON_COMPOUNDED_COUPON_DEFINITION = new CouponONCompoundedDefinition(EUR_CUR, EUR_PAYMENT_DATE, START_ACCRUAL_DATE, EUR_END_ACCRUAL_DATE,
      EUR_PAYMENT_YEAR_FRACTION, NOTIONAL,
      EUR_OIS, START_ACCRUAL_DATE, EUR_END_ACCRUAL_DATE, EUR_CALENDAR);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullIndex() {
    new CouponONCompoundedDefinition(EUR_CUR, EUR_PAYMENT_DATE, START_ACCRUAL_DATE, EUR_END_ACCRUAL_DATE, EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, null, START_ACCRUAL_DATE, EUR_END_ACCRUAL_DATE,
        EUR_CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullStartFixing() {
    new CouponONCompoundedDefinition(EUR_CUR, EUR_PAYMENT_DATE, START_ACCRUAL_DATE, EUR_END_ACCRUAL_DATE, EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, null, EUR_END_ACCRUAL_DATE, EUR_CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullEndFixing() {
    new CouponONCompoundedDefinition(EUR_CUR, EUR_PAYMENT_DATE, START_ACCRUAL_DATE, EUR_END_ACCRUAL_DATE, EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, START_ACCRUAL_DATE, null, EUR_CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void inmcompatibleCurrency() {
    new CouponONCompoundedDefinition(Currency.USD, EUR_PAYMENT_DATE, START_ACCRUAL_DATE, EUR_END_ACCRUAL_DATE, EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, START_ACCRUAL_DATE, EUR_END_ACCRUAL_DATE,
        EUR_CALENDAR);
  }

  @Test
  public void getter() {
    assertEquals("CouponONCompounded definition: getter", EUR_OIS, ON_COMPOUNDED_COUPON_DEFINITION.getIndex());
    final ZonedDateTime[] FixingDateArray = ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates();
    final ZonedDateTime firstFixingPeriod = FixingDateArray[0];
    final ZonedDateTime lastFixingPeriodEndDate = FixingDateArray[FixingDateArray.length - 1];
    assertEquals("CouponONCompounded definition: getter", START_ACCRUAL_DATE, firstFixingPeriod);
    assertEquals("CouponONCompounded definition: getter", lastFixingPeriodEndDate, EUR_END_ACCRUAL_DATE);
    assertEquals("CouponONCompounded definition: getter", EUR_LAST_FIXING_DATE, LocalDateTime.of(2011, 9, 15, 0, 0, 0, 0).atZone(ZoneOffset.UTC));
    assertEquals("CouponONCompounded definition: getter", lastFixingPeriodEndDate, LocalDateTime.of(2011, 9, 16, 0, 0, 0, 0).atZone(ZoneOffset.UTC));
    double aftot = 0.0;
    for (int loopperiod = 0; loopperiod < ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors().length; loopperiod++) {
      aftot += ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[loopperiod];
    }
    assertEquals("CouponONCompounded definition: getter", EUR_FIXING_YEAR_FRACTION, aftot, 1.0E-10);
  }

  @Test
  /**
   * Tests the builder from financial details.
   */
  public void from() {
    final CouponONCompoundedDefinition cpnFrom = CouponONCompoundedDefinition.from(EUR_OIS, SPOT_DATE, CPN_TENOR, NOTIONAL, EUR_SETTLEMENT_DAYS, EUR_BUSINESS_DAY, EUR_IS_EOM, EUR_CALENDAR);
    assertEquals("CouponONCompounded definition: from", cpnFrom, ON_COMPOUNDED_COUPON_DEFINITION);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeNoFixingDeprecated() {
    final String[] curveNames = new String[] {"a", "b" };
    final CouponONCompounded cpnConverted = ON_COMPOUNDED_COUPON_DEFINITION.toDerivative(TRADE_DATE, curveNames);
    final double paymentTime = TimeCalculatorBUS252.getTimeBetween(TRADE_DATE, EUR_PAYMENT_DATE, EUR_CALENDAR);
    final double[] FIXING_PERIOD_START_TIMES = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 1];
    final double[] FIXING_PERIOD_END_TIMES = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 1];
    final double[] FIXING_PERIOD_ACCRUAL_FACTOR = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 1];
    for (int i = 0; i < ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 1; i++) {
      FIXING_PERIOD_START_TIMES[i] = EUR_DAY_COUNT.getDayCountFraction(TRADE_DATE, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i], EUR_CALENDAR);
      FIXING_PERIOD_END_TIMES[i] = EUR_DAY_COUNT.getDayCountFraction(TRADE_DATE, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 1], EUR_CALENDAR);
      FIXING_PERIOD_ACCRUAL_FACTOR[i] = EUR_DAY_COUNT.getDayCountFraction(ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i], ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 1],
          EUR_CALENDAR);
    }
    final CouponONCompounded cpnExpected = new CouponONCompounded(EUR_CUR, paymentTime, curveNames[0], EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, FIXING_PERIOD_START_TIMES,
        FIXING_PERIOD_END_TIMES, FIXING_PERIOD_ACCRUAL_FACTOR, NOTIONAL, curveNames[1]);
    assertEquals("CouponONCompounded definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeNoFixing() {
    final CouponONCompounded cpnConverted = ON_COMPOUNDED_COUPON_DEFINITION.toDerivative(TRADE_DATE);
    final double paymentTime = EUR_DAY_COUNT.getDayCountFraction(TRADE_DATE, EUR_PAYMENT_DATE, EUR_CALENDAR);
    final double[] FIXING_PERIOD_START_TIMES = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 1];
    final double[] FIXING_PERIOD_END_TIMES = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 1];
    final double[] FIXING_PERIOD_ACCRUAL_FACTOR = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 1];
    for (int i = 0; i < ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 1; i++) {
      FIXING_PERIOD_START_TIMES[i] = EUR_DAY_COUNT.getDayCountFraction(TRADE_DATE, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i], EUR_CALENDAR);
      FIXING_PERIOD_END_TIMES[i] = EUR_DAY_COUNT.getDayCountFraction(TRADE_DATE, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 1], EUR_CALENDAR);
      FIXING_PERIOD_ACCRUAL_FACTOR[i] = EUR_DAY_COUNT.getDayCountFraction(ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i], ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 1],
          EUR_CALENDAR);
    }
    final CouponONCompounded cpnExpected = new CouponONCompounded(EUR_CUR, paymentTime, EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, FIXING_PERIOD_START_TIMES,
        FIXING_PERIOD_END_TIMES, FIXING_PERIOD_ACCRUAL_FACTOR, NOTIONAL);
    assertEquals("CouponONCompounded definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFixingBeforeStartDeprecated() {
    final String[] curveNames = new String[] {"a", "b" };
    final ZonedDateTime referenceDate = ScheduleCalculator.getAdjustedDate(TRADE_DATE, 1, EUR_CALENDAR);
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7) }, new double[] {0.01 });
    final Payment cpnConverted = ON_COMPOUNDED_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS, curveNames);
    final double paymentTime = EUR_DAY_COUNT.getDayCountFraction(referenceDate, EUR_PAYMENT_DATE, EUR_CALENDAR);
    final double[] FIXING_PERIOD_START_TIMES = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 1];
    final double[] FIXING_PERIOD_END_TIMES = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 1];
    final double[] FIXING_PERIOD_ACCRUAL_FACTOR = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 1];
    for (int i = 0; i < ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 1; i++) {
      FIXING_PERIOD_START_TIMES[i] = EUR_DAY_COUNT.getDayCountFraction(referenceDate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i], EUR_CALENDAR);
      FIXING_PERIOD_END_TIMES[i] = EUR_DAY_COUNT.getDayCountFraction(referenceDate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 1], EUR_CALENDAR);
      FIXING_PERIOD_ACCRUAL_FACTOR[i] = EUR_DAY_COUNT.getDayCountFraction(ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i], ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 1],
          EUR_CALENDAR);
    }
    final CouponONCompounded cpnExpected = new CouponONCompounded(EUR_CUR, paymentTime, curveNames[0], EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, FIXING_PERIOD_START_TIMES,
        FIXING_PERIOD_END_TIMES, FIXING_PERIOD_ACCRUAL_FACTOR, NOTIONAL, curveNames[1]);
    assertEquals("CouponONCompounded definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFixingBeforeStart() {
    final ZonedDateTime referenceDate = ScheduleCalculator.getAdjustedDate(TRADE_DATE, 1, EUR_CALENDAR);
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7) }, new double[] {0.01 });
    final Payment cpnConverted = ON_COMPOUNDED_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS);
    final double paymentTime = EUR_DAY_COUNT.getDayCountFraction(referenceDate, EUR_PAYMENT_DATE, EUR_CALENDAR);
    final double[] FIXING_PERIOD_START_TIMES = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 1];
    final double[] FIXING_PERIOD_END_TIMES = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 1];
    final double[] FIXING_PERIOD_ACCRUAL_FACTOR = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 1];
    for (int i = 0; i < ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 1; i++) {
      FIXING_PERIOD_START_TIMES[i] = EUR_DAY_COUNT.getDayCountFraction(referenceDate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i], EUR_CALENDAR);
      FIXING_PERIOD_END_TIMES[i] = EUR_DAY_COUNT.getDayCountFraction(referenceDate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 1], EUR_CALENDAR);
      FIXING_PERIOD_ACCRUAL_FACTOR[i] = EUR_DAY_COUNT.getDayCountFraction(ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i], ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 1],
          EUR_CALENDAR);

    }
    final CouponONCompounded cpnExpected = new CouponONCompounded(EUR_CUR, paymentTime, EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, FIXING_PERIOD_START_TIMES,
        FIXING_PERIOD_END_TIMES, FIXING_PERIOD_ACCRUAL_FACTOR, NOTIONAL);
    assertEquals("CouponONCompounded definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFixingOnStartNotYetFixedDeprecated() {
    final String[] curveNames = new String[] {"a", "b" };
    final ZonedDateTime referenceDate = SPOT_DATE;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8) },
        new double[] {
          0.01,
          0.01 });
    final Payment cpnConverted = ON_COMPOUNDED_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS, curveNames);
    final double paymentTime = EUR_DAY_COUNT.getDayCountFraction(referenceDate, EUR_PAYMENT_DATE, EUR_CALENDAR);
    final double[] FIXING_PERIOD_START_TIMES = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 1];
    final double[] FIXING_PERIOD_END_TIMES = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 1];
    final double[] FIXING_PERIOD_ACCRUAL_FACTOR = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 1];
    final double[] FIXING_PERIOD_ACCRUAL_FACTOR_ACT_ACT = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 1];
    for (int i = 0; i < ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 1; i++) {
      FIXING_PERIOD_START_TIMES[i] = EUR_DAY_COUNT.getDayCountFraction(referenceDate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i], EUR_CALENDAR);
      FIXING_PERIOD_END_TIMES[i] = EUR_DAY_COUNT.getDayCountFraction(referenceDate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 1], EUR_CALENDAR);
      FIXING_PERIOD_ACCRUAL_FACTOR[i] = EUR_DAY_COUNT.getDayCountFraction(ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i], ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 1],
          EUR_CALENDAR);
      FIXING_PERIOD_ACCRUAL_FACTOR_ACT_ACT[i] = TimeCalculator.getTimeBetween(ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i], ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 1]);
    }
    final CouponONCompounded cpnExpected = new CouponONCompounded(EUR_CUR, paymentTime, curveNames[0], EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, FIXING_PERIOD_START_TIMES,
        FIXING_PERIOD_END_TIMES, FIXING_PERIOD_ACCRUAL_FACTOR, NOTIONAL, curveNames[1]);
    assertEquals("CouponONCompounded definition: toDerivative", cpnExpected, cpnConverted);
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
    final Payment cpnConverted = ON_COMPOUNDED_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS);
    final double paymentTime = EUR_DAY_COUNT.getDayCountFraction(referenceDate, EUR_PAYMENT_DATE, EUR_CALENDAR);
    final double[] FIXING_PERIOD_START_TIMES = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 1];
    final double[] FIXING_PERIOD_END_TIMES = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 1];
    final double[] FIXING_PERIOD_ACCRUAL_FACTOR = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 1];
    final double[] FIXING_PERIOD_ACCRUAL_FACTOR_ACT_ACT = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 1];
    for (int i = 0; i < ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 1; i++) {
      FIXING_PERIOD_START_TIMES[i] = EUR_DAY_COUNT.getDayCountFraction(referenceDate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i], EUR_CALENDAR);
      FIXING_PERIOD_END_TIMES[i] = EUR_DAY_COUNT.getDayCountFraction(referenceDate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 1], EUR_CALENDAR);
      FIXING_PERIOD_ACCRUAL_FACTOR[i] = EUR_DAY_COUNT.getDayCountFraction(ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i], ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 1],
          EUR_CALENDAR);
      FIXING_PERIOD_ACCRUAL_FACTOR_ACT_ACT[i] = TimeCalculator.getTimeBetween(ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i], ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 1]);
    }
    final CouponONCompounded cpnExpected = new CouponONCompounded(EUR_CUR, paymentTime, EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, FIXING_PERIOD_START_TIMES,
        FIXING_PERIOD_END_TIMES, FIXING_PERIOD_ACCRUAL_FACTOR, NOTIONAL);
    assertEquals("CouponONCompounded definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFixingOnStartFixedDeprecated() {
    final String[] curveNames = new String[] {"a", "b" };
    final ZonedDateTime referenceDate = SPOT_DATE;
    final double fixingRate = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8),
      DateUtils.getUTCDate(2011, 9, 9) }, new double[] {fixingRate, fixingRate, fixingRate });
    final Payment cpnConverted = ON_COMPOUNDED_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS, curveNames);
    final double paymentTime = EUR_DAY_COUNT.getDayCountFraction(referenceDate, EUR_PAYMENT_DATE, EUR_CALENDAR);
    final double notionalAccrued = NOTIONAL * Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[0]);
    final double[] FIXING_PERIOD_START_TIMES = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 2];
    final double[] FIXING_PERIOD_END_TIMES = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 2];
    final double[] FIXING_PERIOD_ACCRUAL_FACTOR = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 2];
    final double[] FIXING_PERIOD_ACCRUAL_FACTOR_ACT_ACT = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 2];
    for (int i = 0; i < ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 2; i++) {
      FIXING_PERIOD_START_TIMES[i] = EUR_DAY_COUNT.getDayCountFraction(referenceDate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 1], EUR_CALENDAR);
      FIXING_PERIOD_END_TIMES[i] = EUR_DAY_COUNT.getDayCountFraction(referenceDate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 2], EUR_CALENDAR);
      FIXING_PERIOD_ACCRUAL_FACTOR[i] = EUR_DAY_COUNT.getDayCountFraction(ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 1], ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 2],
          EUR_CALENDAR);
      FIXING_PERIOD_ACCRUAL_FACTOR_ACT_ACT[i] = TimeCalculator.getTimeBetween(ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 1],
          ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 2]);
    }
    final CouponONCompounded cpnExpected = new CouponONCompounded(EUR_CUR, paymentTime, curveNames[0], EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, FIXING_PERIOD_START_TIMES,
        FIXING_PERIOD_END_TIMES, FIXING_PERIOD_ACCRUAL_FACTOR, notionalAccrued, curveNames[1]);
    assertEquals("CouponONCompounded definition: toDerivative", cpnExpected, cpnConverted);
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
    final Payment cpnConverted = ON_COMPOUNDED_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS);
    final double paymentTime = EUR_DAY_COUNT.getDayCountFraction(referenceDate, EUR_PAYMENT_DATE, EUR_CALENDAR);
    final double notionalAccrued = NOTIONAL * Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[0]);
    final double[] FIXING_PERIOD_START_TIMES = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 2];
    final double[] FIXING_PERIOD_END_TIMES = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 2];
    final double[] FIXING_PERIOD_ACCRUAL_FACTOR = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 2];
    final double[] FIXING_PERIOD_ACCRUAL_FACTOR_ACT_ACT = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 2];
    for (int i = 0; i < ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 2; i++) {
      FIXING_PERIOD_START_TIMES[i] = EUR_DAY_COUNT.getDayCountFraction(referenceDate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 1], EUR_CALENDAR);
      FIXING_PERIOD_END_TIMES[i] = EUR_DAY_COUNT.getDayCountFraction(referenceDate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 2], EUR_CALENDAR);
      FIXING_PERIOD_ACCRUAL_FACTOR[i] = EUR_DAY_COUNT.getDayCountFraction(ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 1], ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 2],
          EUR_CALENDAR);
      FIXING_PERIOD_ACCRUAL_FACTOR_ACT_ACT[i] = TimeCalculator.getTimeBetween(ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 1],
          ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 2]);
    }
    final CouponONCompounded cpnExpected = new CouponONCompounded(EUR_CUR, paymentTime, EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, FIXING_PERIOD_START_TIMES,
        FIXING_PERIOD_END_TIMES, FIXING_PERIOD_ACCRUAL_FACTOR, notionalAccrued);
    assertEquals("CouponONCompounded definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFixingMiddleNotYetFixedDeprecated() {
    final String[] curveNames = new String[] {"a", "b" };
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 9, 13);
    final double fixingRate = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8),
      DateUtils.getUTCDate(2011, 9, 9), DateUtils.getUTCDate(2011, 9, 12) }, new double[] {fixingRate, fixingRate, fixingRate, fixingRate });
    final Payment cpnConverted = ON_COMPOUNDED_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS, curveNames);
    final double paymentTime = EUR_DAY_COUNT.getDayCountFraction(referenceDate, EUR_PAYMENT_DATE, EUR_CALENDAR);
    final double notionalAccrued = NOTIONAL * Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[0]) *
        Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[1]);
    final double[] FIXING_PERIOD_START_TIMES = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 3];
    final double[] FIXING_PERIOD_END_TIMES = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 3];
    final double[] FIXING_PERIOD_ACCRUAL_FACTOR = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 3];
    final double[] FIXING_PERIOD_ACCRUAL_FACTOR_ACT_ACT = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 3];
    for (int i = 0; i < ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 3; i++) {
      FIXING_PERIOD_START_TIMES[i] = EUR_DAY_COUNT.getDayCountFraction(referenceDate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 2], EUR_CALENDAR);
      FIXING_PERIOD_END_TIMES[i] = EUR_DAY_COUNT.getDayCountFraction(referenceDate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 3], EUR_CALENDAR);
      FIXING_PERIOD_ACCRUAL_FACTOR[i] = EUR_DAY_COUNT.getDayCountFraction(ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 2], ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 3],
          EUR_CALENDAR);
      FIXING_PERIOD_ACCRUAL_FACTOR_ACT_ACT[i] = TimeCalculator.getTimeBetween(ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 2],
          ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 3]);
    }
    final CouponONCompounded cpnExpected = new CouponONCompounded(EUR_CUR, paymentTime, curveNames[0], EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, FIXING_PERIOD_START_TIMES,
        FIXING_PERIOD_END_TIMES, FIXING_PERIOD_ACCRUAL_FACTOR, notionalAccrued, curveNames[1]);
    assertEquals("CouponONCompounded definition: toDerivative", cpnExpected, cpnConverted);
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
    final Payment cpnConverted = ON_COMPOUNDED_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS);
    final double paymentTime = EUR_DAY_COUNT.getDayCountFraction(referenceDate, EUR_PAYMENT_DATE, EUR_CALENDAR);
    final double notionalAccrued = NOTIONAL * Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[0]) *
        Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[1]);
    final double[] FIXING_PERIOD_START_TIMES = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 3];
    final double[] FIXING_PERIOD_END_TIMES = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 3];
    final double[] FIXING_PERIOD_ACCRUAL_FACTOR = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 3];
    final double[] FIXING_PERIOD_ACCRUAL_FACTOR_ACT_ACT = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 3];
    for (int i = 0; i < ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 3; i++) {
      FIXING_PERIOD_START_TIMES[i] = EUR_DAY_COUNT.getDayCountFraction(referenceDate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 2], EUR_CALENDAR);
      FIXING_PERIOD_END_TIMES[i] = EUR_DAY_COUNT.getDayCountFraction(referenceDate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 3], EUR_CALENDAR);
      FIXING_PERIOD_ACCRUAL_FACTOR[i] = EUR_DAY_COUNT.getDayCountFraction(ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 2], ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 3],
          EUR_CALENDAR);
      FIXING_PERIOD_ACCRUAL_FACTOR_ACT_ACT[i] = TimeCalculator.getTimeBetween(ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 2],
          ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 3]);
    }
    final CouponONCompounded cpnExpected = new CouponONCompounded(EUR_CUR, paymentTime, EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, FIXING_PERIOD_START_TIMES,
        FIXING_PERIOD_END_TIMES, FIXING_PERIOD_ACCRUAL_FACTOR, notionalAccrued);
    assertEquals("CouponONCompounded definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFixingMiddleFixedDeprecated() {
    final String[] curveNames = new String[] {"a", "b" };
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 9, 13);
    final double fixingRate = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8),
      DateUtils.getUTCDate(2011, 9, 9), DateUtils.getUTCDate(2011, 9, 12), DateUtils.getUTCDate(2011, 9, 13) }, new double[] {fixingRate, fixingRate, fixingRate, fixingRate, fixingRate });
    final Payment cpnConverted = ON_COMPOUNDED_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS, curveNames);
    final double paymentTime = EUR_DAY_COUNT.getDayCountFraction(referenceDate, EUR_PAYMENT_DATE, EUR_CALENDAR);
    final double notionalAccrued = NOTIONAL * Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[0]) *
        Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[1])
        * Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[2]);
    final double[] FIXING_PERIOD_START_TIMES = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 4];
    final double[] FIXING_PERIOD_END_TIMES = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 4];
    final double[] FIXING_PERIOD_ACCRUAL_FACTOR = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 4];
    final double[] FIXING_PERIOD_ACCRUAL_FACTOR_ACT_ACT = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 4];
    for (int i = 0; i < ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 4; i++) {
      FIXING_PERIOD_START_TIMES[i] = EUR_DAY_COUNT.getDayCountFraction(referenceDate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 3], EUR_CALENDAR);
      FIXING_PERIOD_END_TIMES[i] = EUR_DAY_COUNT.getDayCountFraction(referenceDate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 4], EUR_CALENDAR);
      FIXING_PERIOD_ACCRUAL_FACTOR[i] = EUR_DAY_COUNT.getDayCountFraction(ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 3], ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 4],
          EUR_CALENDAR);
      FIXING_PERIOD_ACCRUAL_FACTOR_ACT_ACT[i] = TimeCalculator.getTimeBetween(ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 3],
          ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 4]);
    }
    final CouponONCompounded cpnExpected = new CouponONCompounded(EUR_CUR, paymentTime, curveNames[0], EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, FIXING_PERIOD_START_TIMES,
        FIXING_PERIOD_END_TIMES, FIXING_PERIOD_ACCRUAL_FACTOR, notionalAccrued, curveNames[1]);
    assertEquals("CouponONCompounded definition: toDerivative", cpnExpected, cpnConverted);
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
    final Payment cpnConverted = ON_COMPOUNDED_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS);
    final double paymentTime = EUR_DAY_COUNT.getDayCountFraction(referenceDate, EUR_PAYMENT_DATE, EUR_CALENDAR);
    final double notionalAccrued = NOTIONAL * Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[0]) *
        Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[1])
        * Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[2]);
    final double[] FIXING_PERIOD_START_TIMES = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 4];
    final double[] FIXING_PERIOD_END_TIMES = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 4];
    final double[] FIXING_PERIOD_ACCRUAL_FACTOR = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 4];
    final double[] FIXING_PERIOD_ACCRUAL_FACTOR_ACT_ACT = new double[ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 4];
    for (int i = 0; i < ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates().length - 4; i++) {
      FIXING_PERIOD_START_TIMES[i] = EUR_DAY_COUNT.getDayCountFraction(referenceDate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 3], EUR_CALENDAR);
      FIXING_PERIOD_END_TIMES[i] = EUR_DAY_COUNT.getDayCountFraction(referenceDate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 4], EUR_CALENDAR);
      FIXING_PERIOD_ACCRUAL_FACTOR[i] = EUR_DAY_COUNT.getDayCountFraction(ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 3], ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 4],
          EUR_CALENDAR);
      FIXING_PERIOD_ACCRUAL_FACTOR_ACT_ACT[i] = TimeCalculator.getTimeBetween(ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 3],
          ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodDates()[i + 4]);
    }
    final CouponONCompounded cpnExpected = new CouponONCompounded(EUR_CUR, paymentTime, EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, FIXING_PERIOD_START_TIMES,
        FIXING_PERIOD_END_TIMES, FIXING_PERIOD_ACCRUAL_FACTOR, notionalAccrued);
    assertEquals("CouponONCompounded definition: toDerivative", cpnExpected, cpnConverted);
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
    final Payment cpnConverted = ON_COMPOUNDED_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS);
    final double paymentTime = EUR_DAY_COUNT.getDayCountFraction(referenceDate, EUR_PAYMENT_DATE, EUR_CALENDAR);
    final double notionalAccrued = NOTIONAL * Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[0]) *
        Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[1]) * Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[2]) *
        Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[3]) * Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[4]);
    final CouponFixed cpnExpected = new CouponFixed(EUR_CUR, paymentTime, EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, (notionalAccrued / NOTIONAL - 1.0) / EUR_PAYMENT_YEAR_FRACTION);
    assertEquals("CouponONCompounded definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFixingSecondLastFixedDeprecated() {
    final String[] curveNames = new String[] {"a", "b" };
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 9, 15);
    final double fixingRate = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8),
      DateUtils.getUTCDate(2011, 9, 9), DateUtils.getUTCDate(2011, 9, 12), DateUtils.getUTCDate(2011, 9, 13), DateUtils.getUTCDate(2011, 9, 14), DateUtils.getUTCDate(2011, 9, 15) }, new double[] {
      fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate });
    final Payment cpnConverted = ON_COMPOUNDED_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS, curveNames);
    final double paymentTime = EUR_DAY_COUNT.getDayCountFraction(referenceDate, EUR_PAYMENT_DATE, EUR_CALENDAR);
    final double notionalAccrued = NOTIONAL * Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[0]) *
        Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[1]) * Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[2]) *
        Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[3]) * Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[4]);
    final CouponFixed cpnExpected = new CouponFixed(EUR_CUR, paymentTime, curveNames[0], EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, (notionalAccrued / NOTIONAL - 1.0) / EUR_PAYMENT_YEAR_FRACTION);
    assertEquals("CouponONCompounded definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFixingLastDeprecated() {
    final String[] curveNames = new String[] {"a", "b" };
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 9, 16);
    final double fixingRate = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8),
      DateUtils.getUTCDate(2011, 9, 9), DateUtils.getUTCDate(2011, 9, 12), DateUtils.getUTCDate(2011, 9, 13), DateUtils.getUTCDate(2011, 9, 14), DateUtils.getUTCDate(2011, 9, 15) }, new double[] {
      fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate });
    final Payment cpnConverted = ON_COMPOUNDED_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS, curveNames);
    final double paymentTime = EUR_DAY_COUNT.getDayCountFraction(referenceDate, EUR_PAYMENT_DATE, EUR_CALENDAR);
    final double notionalAccrued = NOTIONAL * Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[0]) *
        Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[1]) * Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[2]) *
        Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[3]) * Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[4]);
    final CouponFixed cpnExpected = new CouponFixed(EUR_CUR, paymentTime, curveNames[0], EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, (notionalAccrued / NOTIONAL - 1.0) / EUR_PAYMENT_YEAR_FRACTION);
    assertEquals("CouponONCompounded definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFixingLast() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 9, 16);
    final double fixingRate = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8),
      DateUtils.getUTCDate(2011, 9, 9), DateUtils.getUTCDate(2011, 9, 12), DateUtils.getUTCDate(2011, 9, 13), DateUtils.getUTCDate(2011, 9, 14), DateUtils.getUTCDate(2011, 9, 15) }, new double[] {
      fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate });
    final Payment cpnConverted = ON_COMPOUNDED_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS);
    final double paymentTime = EUR_DAY_COUNT.getDayCountFraction(referenceDate, EUR_PAYMENT_DATE, EUR_CALENDAR);
    final double notionalAccrued = NOTIONAL * Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[0]) *
        Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[1]) * Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[2]) *
        Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[3]) * Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[4]);
    final CouponFixed cpnExpected = new CouponFixed(EUR_CUR, paymentTime, EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, (notionalAccrued / NOTIONAL - 1.0) / EUR_PAYMENT_YEAR_FRACTION);
    assertEquals("CouponONCompounded definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeAfterLastDeprecated() {
    final String[] curveNames = new String[] {"a", "b" };
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 9, 19);
    final double fixingRate = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8),
      DateUtils.getUTCDate(2011, 9, 9), DateUtils.getUTCDate(2011, 9, 12), DateUtils.getUTCDate(2011, 9, 13), DateUtils.getUTCDate(2011, 9, 14), DateUtils.getUTCDate(2011, 9, 15) }, new double[] {
      fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate });
    final Payment cpnConverted = ON_COMPOUNDED_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS, curveNames);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, EUR_PAYMENT_DATE);
    final double notionalAccrued = NOTIONAL * Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[0]) *
        Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[1]) * Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[2]) *
        Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[3]) * Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[4]);
    final CouponFixed cpnExpected = new CouponFixed(EUR_CUR, paymentTime, curveNames[0], EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, (notionalAccrued / NOTIONAL - 1.0) / EUR_PAYMENT_YEAR_FRACTION);
    assertEquals("CouponONCompounded definition: toDerivative", cpnExpected, cpnConverted);
  }

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
    final Payment cpnConverted = ON_COMPOUNDED_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, EUR_PAYMENT_DATE);
    final double notionalAccrued = NOTIONAL * Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[0]) *
        Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[1]) * Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[2]) *
        Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[3]) * Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[4]);
    final CouponFixed cpnExpected = new CouponFixed(EUR_CUR, paymentTime, EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, (notionalAccrued / NOTIONAL - 1.0) / EUR_PAYMENT_YEAR_FRACTION);
    assertEquals("CouponONCompounded definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method on the payment date. valuation is at noon, payment set at midnight...
   */
  public void toDerivativeJustAfterPayment() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 9, 19);
    final ZonedDateTime valuationTimeIsNoon = DateUtils.getUTCDate(2011, 9, 19, 12, 0);
    assertTrue("valuationTimeIsNoon used to be after paymentDate, which was midnight. Confirm behaviour", valuationTimeIsNoon.isAfter(ON_COMPOUNDED_COUPON_DEFINITION.getPaymentDate()));
    final double fixingRate = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8),
      DateUtils.getUTCDate(2011, 9, 9), DateUtils.getUTCDate(2011, 9, 12), DateUtils.getUTCDate(2011, 9, 13), DateUtils.getUTCDate(2011, 9, 14), DateUtils.getUTCDate(2011, 9, 15) }, new double[] {
      fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate });
    final Payment cpnConverted = ON_COMPOUNDED_COUPON_DEFINITION.toDerivative(valuationTimeIsNoon, fixingTS);
    final double paymentTime = -TimeCalculator.getTimeBetween(referenceDate, EUR_PAYMENT_DATE, EUR_DAY_COUNT, EUR_CALENDAR);
    final double notionalAccrued = NOTIONAL * Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[0]) *
        Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[1]) * Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[2]) *
        Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[3]) * Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[4]);
    final CouponFixed cpnExpected = new CouponFixed(EUR_CUR, paymentTime, EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, (notionalAccrued / NOTIONAL - 1.0) / EUR_PAYMENT_YEAR_FRACTION);
    assertEquals("CouponONCompounded definition: toDerivative", cpnExpected, cpnConverted);

    // Test pricing, too. Notice that the value of a coupon on its payment date is non-zero
    final MulticurveProviderDiscount curves = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
    final MultipleCurrencyAmount pvConverted = com.opengamma.analytics.financial.interestrate.payments.provider.CouponFixedDiscountingMethod.getInstance().presentValue((CouponFixed) cpnConverted,
        curves);
    final MultipleCurrencyAmount pvExpected = com.opengamma.analytics.financial.interestrate.payments.provider.CouponFixedDiscountingMethod.getInstance().presentValue(cpnExpected, curves);
    assertEquals("CouponONCompounded definition: toDerivative", pvConverted, pvExpected);
    assertEquals("CouponONCompounded definition: toDerivative", pvConverted, MultipleCurrencyAmount.of(EUR_CUR, 19744.6689499392));

  }

  @Test
  /**
   * Tests the toDerivative method on the payment date. valuation is at noon, payment set at midnight...
   */
  public void toDerivativeJustAfterPaymentDeprecated() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 9, 19);
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final String[] curveNames = curves.getAllNames().toArray(new String[curves.size()]);
    final ZonedDateTime valuationTimeIsNoon = DateUtils.getUTCDate(2011, 9, 19, 12, 0);
    assertTrue("valuationTimeIsNoon used to be after paymentDate, which was midnight. Confirm behaviour", valuationTimeIsNoon.isAfter(ON_COMPOUNDED_COUPON_DEFINITION.getPaymentDate()));
    final double fixingRate = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8),
      DateUtils.getUTCDate(2011, 9, 9), DateUtils.getUTCDate(2011, 9, 12), DateUtils.getUTCDate(2011, 9, 13), DateUtils.getUTCDate(2011, 9, 14), DateUtils.getUTCDate(2011, 9, 15) }, new double[] {
      fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate });
    final Payment cpnConverted = ON_COMPOUNDED_COUPON_DEFINITION.toDerivative(valuationTimeIsNoon, fixingTS, curveNames);
    final double paymentTime = -EUR_DAY_COUNT.getDayCountFraction(referenceDate, EUR_PAYMENT_DATE, EUR_CALENDAR);
    final double notionalAccrued = NOTIONAL * Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[0]) *
        Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[1]) * Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[2]) *
        Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[3]) * Math.pow(1 + fixingRate, ON_COMPOUNDED_COUPON_DEFINITION.getFixingPeriodAccrualFactors()[4]);
    final CouponFixed cpnExpected = new CouponFixed(EUR_CUR, paymentTime, curveNames[0], EUR_PAYMENT_YEAR_FRACTION, NOTIONAL, (notionalAccrued / NOTIONAL - 1.0) / EUR_PAYMENT_YEAR_FRACTION);
    assertEquals("CouponONCompounded definition: toDerivative", cpnExpected, cpnConverted);

    // Test pricing, too. Notice that the value of a coupon on its payment date is non-zero
    final CurrencyAmount pvConverted = CouponFixedDiscountingMethod.getInstance().presentValue((CouponFixed) cpnConverted, curves);
    final CurrencyAmount pvExpected = CouponFixedDiscountingMethod.getInstance().presentValue(cpnExpected, curves);
    assertEquals("CouponONCompounded definition: toDerivative", pvConverted, pvExpected);
    assertEquals("CouponONCompounded definition: toDerivative", pvConverted, CurrencyAmount.of(EUR_CUR, 19744.6689499392));

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
    ON_COMPOUNDED_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  /**
   * Tests the toDerivative method: after payment date
   */
  public void toDerivativeAfterPaymentDeprecated() {
    final String[] curveNames = new String[] {"a", "b" };
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 9, 20);
    final double fixingRate = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8),
      DateUtils.getUTCDate(2011, 9, 9), DateUtils.getUTCDate(2011, 9, 12), DateUtils.getUTCDate(2011, 9, 13), DateUtils.getUTCDate(2011, 9, 14), DateUtils.getUTCDate(2011, 9, 15) }, new double[] {
      fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate });
    ON_COMPOUNDED_COUPON_DEFINITION.toDerivative(referenceDate, fixingTS, curveNames);
  }
}
