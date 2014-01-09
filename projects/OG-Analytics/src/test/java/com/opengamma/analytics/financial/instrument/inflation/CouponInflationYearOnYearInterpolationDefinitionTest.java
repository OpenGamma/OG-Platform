/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.inflation;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearInterpolation;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
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
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CouponInflationYearOnYearInterpolationDefinitionTest {

  private static final String NAME = "Euro HICP x";
  private static final Currency CUR = Currency.EUR;
  private static final IndexPrice PRICE_INDEX = new IndexPrice(NAME, CUR);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final Period COUPON_TENOR = Period.ofYears(10);
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(START_DATE, COUPON_TENOR, BUSINESS_DAY, CALENDAR);
  private static final ZonedDateTime ACCRUAL_END_DATE = PAYMENT_DATE;
  private static final ZonedDateTime ACCRUAL_START_DATE = ACCRUAL_END_DATE.minusMonths(12);
  private static final double NOTIONAL = 98765432;
  private static final int MONTH_LAG = 3;
  private static final ZonedDateTime[] REFERENCE_START_DATE = new ZonedDateTime[2];
  static {
    REFERENCE_START_DATE[0] = ACCRUAL_START_DATE.minusMonths(MONTH_LAG).withDayOfMonth(1).with(TemporalAdjusters.lastDayOfMonth());
    REFERENCE_START_DATE[1] = REFERENCE_START_DATE[0].plusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
  }
  private static final ZonedDateTime[] REFERENCE_END_DATE = new ZonedDateTime[2];
  static {
    REFERENCE_END_DATE[0] = PAYMENT_DATE.minusMonths(MONTH_LAG).withDayOfMonth(1).with(TemporalAdjusters.lastDayOfMonth());
    REFERENCE_END_DATE[1] = REFERENCE_END_DATE[0].plusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
  }
  private static final double WEIGHT_START = 0.2;
  private static final double WEIGHT_END = 0.8;
  private static final CouponInflationYearOnYearInterpolationDefinition YoY_COUPON_DEFINITION = new CouponInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE,
      ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, 3, REFERENCE_START_DATE, REFERENCE_END_DATE, false, WEIGHT_START, WEIGHT_END);
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new CouponInflationYearOnYearInterpolationDefinition(null, PAYMENT_DATE, ACCRUAL_START_DATE, PAYMENT_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, 3, REFERENCE_START_DATE,
        REFERENCE_END_DATE, false, WEIGHT_START, WEIGHT_END);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPay() {
    new CouponInflationYearOnYearInterpolationDefinition(CUR, null, ACCRUAL_START_DATE, PAYMENT_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, 3, REFERENCE_START_DATE,
        REFERENCE_END_DATE, false, WEIGHT_START, WEIGHT_END);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStart() {
    new CouponInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, null, PAYMENT_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, 3, REFERENCE_START_DATE,
        REFERENCE_END_DATE, false, WEIGHT_START, WEIGHT_END);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEnd() {
    new CouponInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, null, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, 3, REFERENCE_START_DATE,
        REFERENCE_END_DATE, false, WEIGHT_START, WEIGHT_END);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex() {
    new CouponInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, PAYMENT_DATE, 1.0, NOTIONAL, null, MONTH_LAG, 3, REFERENCE_START_DATE,
        REFERENCE_END_DATE, false, WEIGHT_START, WEIGHT_END);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRefStart() {
    new CouponInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, PAYMENT_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, 3, null, REFERENCE_END_DATE, false,
        WEIGHT_START, WEIGHT_END);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRefEnd() {
    new CouponInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, PAYMENT_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, 3, REFERENCE_START_DATE, null, false,
        WEIGHT_START, WEIGHT_END);
  }

  @Test
  /**
   * Tests the class getter.
   */
  public void getter() {
    assertEquals("Inflation Year on Year coupon: getter", CUR, YoY_COUPON_DEFINITION.getCurrency());
    assertEquals("Inflation Year on Year coupon: getter", PAYMENT_DATE, YoY_COUPON_DEFINITION.getPaymentDate());
    assertEquals("Inflation Year on Year coupon: getter", ACCRUAL_START_DATE, YoY_COUPON_DEFINITION.getAccrualStartDate());
    assertEquals("Inflation Year on Year coupon: getter", ACCRUAL_END_DATE, YoY_COUPON_DEFINITION.getAccrualEndDate());
    assertEquals("Inflation Year on Year coupon: getter", 1.0, YoY_COUPON_DEFINITION.getPaymentYearFraction());
    assertEquals("Inflation Year on Year coupon: getter", NOTIONAL, YoY_COUPON_DEFINITION.getNotional());
    assertEquals("Inflation Year on Year coupon: getter", PRICE_INDEX, YoY_COUPON_DEFINITION.getPriceIndex());
    assertEquals("Inflation Year on Year coupon: getter", REFERENCE_START_DATE, YoY_COUPON_DEFINITION.getReferenceStartDates());
    assertEquals("Inflation Year on Year coupon: getter", REFERENCE_END_DATE, YoY_COUPON_DEFINITION.getReferenceEndDate());
    assertEquals("Inflation Year on Year coupon: getter", MONTH_LAG, YoY_COUPON_DEFINITION.getConventionalMonthLag());
  }

  @Test
  /**
   * Tests the equal and hash-code methods.
   */
  public void equalHash() {
    assertEquals(YoY_COUPON_DEFINITION, YoY_COUPON_DEFINITION);
    final CouponInflationYearOnYearInterpolationDefinition couponDuplicate = new CouponInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, 1.0,
        NOTIONAL,
        PRICE_INDEX, MONTH_LAG, 3, REFERENCE_START_DATE, REFERENCE_END_DATE, false, WEIGHT_START, WEIGHT_END);
    assertEquals(YoY_COUPON_DEFINITION, couponDuplicate);
    assertEquals(YoY_COUPON_DEFINITION.hashCode(), couponDuplicate.hashCode());
    CouponInflationYearOnYearInterpolationDefinition modified;
    modified = new CouponInflationYearOnYearInterpolationDefinition(CUR, ACCRUAL_END_DATE.minusDays(1), ACCRUAL_START_DATE, ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG,
        3, REFERENCE_START_DATE, REFERENCE_END_DATE, false, WEIGHT_START, WEIGHT_END);
    assertFalse(YoY_COUPON_DEFINITION.equals(modified));
    modified = new CouponInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE.minusDays(1), ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, 3,
        REFERENCE_START_DATE, REFERENCE_END_DATE, false, WEIGHT_START, WEIGHT_END);
    assertFalse(YoY_COUPON_DEFINITION.equals(modified));
    modified = new CouponInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE.minusDays(1), 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, 3,
        REFERENCE_START_DATE, REFERENCE_END_DATE, false, WEIGHT_START, WEIGHT_END);
    assertFalse(YoY_COUPON_DEFINITION.equals(modified));
    final ZonedDateTime[] modifiedReferenceStartTime = new ZonedDateTime[2];
    modifiedReferenceStartTime[0] = REFERENCE_START_DATE[0];
    modifiedReferenceStartTime[1] = REFERENCE_START_DATE[1].minusDays(1);
    modified = new CouponInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG,
        3, modifiedReferenceStartTime, REFERENCE_END_DATE, false, WEIGHT_START, WEIGHT_END);
    assertFalse(YoY_COUPON_DEFINITION.equals(modified));
    final ZonedDateTime[] modifiedReferenceEndTime = new ZonedDateTime[2];
    modifiedReferenceEndTime[0] = REFERENCE_START_DATE[0];
    modifiedReferenceEndTime[1] = REFERENCE_START_DATE[1].minusDays(1);
    modified = new CouponInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, 3,
        REFERENCE_START_DATE, modifiedReferenceEndTime, false, WEIGHT_START, WEIGHT_END);
    assertFalse(YoY_COUPON_DEFINITION.equals(modified));
    modified = new CouponInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, 2.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, 3,
        REFERENCE_START_DATE, REFERENCE_END_DATE, false, WEIGHT_START, WEIGHT_END);
    assertFalse(YoY_COUPON_DEFINITION.equals(modified));
    modified = new CouponInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, 1.0, NOTIONAL + 10.0, PRICE_INDEX, MONTH_LAG, 3,
        REFERENCE_START_DATE, REFERENCE_END_DATE, false, WEIGHT_START, WEIGHT_END);
    assertFalse(YoY_COUPON_DEFINITION.equals(modified));
    modified = new CouponInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, 3,
        REFERENCE_START_DATE, REFERENCE_END_DATE, false, WEIGHT_START + .1, WEIGHT_END);
    assertFalse(YoY_COUPON_DEFINITION.equals(modified));
    modified = new CouponInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, 3,
        REFERENCE_START_DATE, REFERENCE_END_DATE, false, WEIGHT_START, WEIGHT_END + .1);
    assertFalse(YoY_COUPON_DEFINITION.equals(modified));
  }

  @Test
  /**
   * Tests the first based on indexation lag.
   */
  public void from2() {
    final CouponInflationYearOnYearInterpolationDefinition constructor = new CouponInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, PAYMENT_DATE, 1.0, NOTIONAL,
        PRICE_INDEX, MONTH_LAG, 3, REFERENCE_START_DATE, REFERENCE_END_DATE, false, WEIGHT_START, WEIGHT_END);
    final CouponInflationYearOnYearInterpolationDefinition from = CouponInflationYearOnYearInterpolationDefinition.from(ACCRUAL_START_DATE, PAYMENT_DATE, NOTIONAL, PRICE_INDEX, MONTH_LAG, false,
        WEIGHT_START, WEIGHT_END);
    assertEquals("Inflation zero-coupon : from", constructor, from);
  }

  @Test
  public void toDerivativesNoData() {
    final ZonedDateTime pricingDate = DateUtils.getUTCDate(2011, 7, 29);
    final Coupon yearOnYearCouponConverted = YoY_COUPON_DEFINITION.toDerivative(pricingDate);
    final double paymentTime = ACT_ACT.getDayCountFraction(pricingDate, PAYMENT_DATE);
    final double referenceStartTime0 = ACT_ACT.getDayCountFraction(pricingDate, REFERENCE_START_DATE[0]);
    final double referenceEndTime0 = ACT_ACT.getDayCountFraction(pricingDate, REFERENCE_END_DATE[0]);
    final double referenceStartTime1 = ACT_ACT.getDayCountFraction(pricingDate, REFERENCE_START_DATE[1]);
    final double referenceEndTime1 = ACT_ACT.getDayCountFraction(pricingDate, REFERENCE_END_DATE[1]);
    final double naturalPaymentStartPaymentTime = ACT_ACT.getDayCountFraction(pricingDate, ACCRUAL_START_DATE);
    final double naturalPaymentEndPaymentTime = ACT_ACT.getDayCountFraction(pricingDate, ACCRUAL_END_DATE);
    final double[] referenceStartTime = new double[2];
    final double[] referenceEndTime = new double[2];
    referenceStartTime[0] = referenceStartTime0;
    referenceStartTime[1] = referenceStartTime1;
    referenceEndTime[0] = referenceEndTime0;
    referenceEndTime[1] = referenceEndTime1;
    final CouponInflationYearOnYearInterpolation yearOnYearCoupon = new CouponInflationYearOnYearInterpolation(CUR, paymentTime, 1.0, NOTIONAL, PRICE_INDEX, referenceStartTime,
        naturalPaymentStartPaymentTime, referenceEndTime, naturalPaymentEndPaymentTime, false, WEIGHT_START, WEIGHT_END);
    assertEquals("Inflation year on year coupon: toDerivative", yearOnYearCouponConverted, yearOnYearCoupon);
  }

  @Test
  public void toDerivativesStartMonthNotknown() {
    final ZonedDateTime pricingDate = DateUtils.getUTCDate(2011, 7, 29);
    final DoubleTimeSeries<ZonedDateTime> priceIndexTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2017, 5, 1),
      DateUtils.getUTCDate(2017, 6, 1), DateUtils.getUTCDate(2018, 5, 1), DateUtils.getUTCDate(2018, 6, 1) },
        new double[] {
          127.23, 127.43, 128.23, 128.43 });
    final Coupon yearOnYearCouponConverted = YoY_COUPON_DEFINITION.toDerivative(pricingDate, priceIndexTS);
    final double paymentTime = ACT_ACT.getDayCountFraction(pricingDate, PAYMENT_DATE);
    final double referenceStartTime0 = ACT_ACT.getDayCountFraction(pricingDate, REFERENCE_START_DATE[0]);
    final double referenceEndTime0 = ACT_ACT.getDayCountFraction(pricingDate, REFERENCE_END_DATE[0]);
    final double referenceStartTime1 = ACT_ACT.getDayCountFraction(pricingDate, REFERENCE_START_DATE[1]);
    final double referenceEndTime1 = ACT_ACT.getDayCountFraction(pricingDate, REFERENCE_END_DATE[1]);
    final double naturalPaymentStartPaymentTime = ACT_ACT.getDayCountFraction(pricingDate, ACCRUAL_START_DATE);
    final double naturalPaymentEndPaymentTime = ACT_ACT.getDayCountFraction(pricingDate, ACCRUAL_END_DATE);
    final double[] referenceStartTime = new double[2];
    final double[] referenceEndTime = new double[2];
    referenceStartTime[0] = referenceStartTime0;
    referenceStartTime[1] = referenceStartTime1;
    referenceEndTime[0] = referenceEndTime0;
    referenceEndTime[1] = referenceEndTime1;
    final CouponInflationYearOnYearInterpolation yearOnYearCoupon = new CouponInflationYearOnYearInterpolation(CUR, paymentTime, 1.0, NOTIONAL, PRICE_INDEX, referenceStartTime,
        naturalPaymentStartPaymentTime, referenceEndTime, naturalPaymentEndPaymentTime, false, WEIGHT_START, WEIGHT_END);
    assertEquals("Inflation zero-coupon: toDerivative", yearOnYearCoupon, yearOnYearCouponConverted);
  }

  @Test
  public void toDerivativesStartMonthKnown() {
    final ZonedDateTime pricingDate = DateUtils.getUTCDate(2018, 7, 25);
    final DoubleTimeSeries<ZonedDateTime> priceIndexTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2017, 5, 31),
      DateUtils.getUTCDate(2017, 6, 30), DateUtils.getUTCDate(2018, 5, 31), DateUtils.getUTCDate(2018, 6, 30) },
        new double[] {
          127.23, 127.43, 128.23, 128.43 });
    final Coupon zeroCouponConverted = YoY_COUPON_DEFINITION.toDerivative(pricingDate, priceIndexTS);
    final double paymentTime = ACT_ACT.getDayCountFraction(pricingDate, PAYMENT_DATE);
    final CouponFixed zeroCoupon = new CouponFixed(CUR, paymentTime, 1.0, NOTIONAL, (WEIGHT_END * 128.23 + (1 - WEIGHT_END) * 128.43) /
        (WEIGHT_START * 127.23 + (1 - WEIGHT_START) * 127.43) - 1.0);
    assertEquals("Inflation zero-coupon: toDerivative", zeroCoupon, zeroCouponConverted);
  }

}
