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

import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationYearOnYearInterpolation;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
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
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CapFloorInflationYearOnYearInterpolationDefinitionTest {

  private static final String NAME = "Euro HICP x";
  private static final Currency CUR = Currency.EUR;
  private static final IndexPrice PRICE_INDEX = new IndexPrice(NAME, CUR);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final ZonedDateTime LAST_KNOWN_FIXING_DATE = DateUtils.getUTCDate(2008, 6, 1);
  private static final Period COUPON_TENOR = Period.ofYears(10);
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(START_DATE, COUPON_TENOR, BUSINESS_DAY, CALENDAR);
  private static final ZonedDateTime ACCRUAL_END_DATE = PAYMENT_DATE;
  private static final ZonedDateTime ACCRUAL_START_DATE = ACCRUAL_END_DATE.minusMonths(12);
  private static final double NOTIONAL = 98765432;
  private static final int MONTH_LAG = 3;
  private static final double STRIKE = .02;
  private static final boolean IS_CAP = true;
  private static final ZonedDateTime[] REFERENCE_START_DATE = new ZonedDateTime[2];
  static {
    REFERENCE_START_DATE[0] = ACCRUAL_START_DATE.minusMonths(MONTH_LAG).withDayOfMonth(1);
    REFERENCE_START_DATE[1] = REFERENCE_START_DATE[0].plusMonths(1);
  }
  private static final ZonedDateTime[] REFERENCE_END_DATE = new ZonedDateTime[2];
  static {
    REFERENCE_END_DATE[0] = PAYMENT_DATE.minusMonths(MONTH_LAG).withDayOfMonth(1);
    REFERENCE_END_DATE[1] = REFERENCE_END_DATE[0].plusMonths(1);
  }
  private static final double WEIGHT_START = 0.2;
  private static final double WEIGHT_END = 0.8;
  private static final CapFloorInflationYearOnYearInterpolationDefinition YoY_CAP_DEFINITION = new CapFloorInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE,
      ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, LAST_KNOWN_FIXING_DATE, MONTH_LAG, 3, REFERENCE_START_DATE, REFERENCE_END_DATE, WEIGHT_START, WEIGHT_END, STRIKE, IS_CAP);
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new CapFloorInflationYearOnYearInterpolationDefinition(null, PAYMENT_DATE, ACCRUAL_START_DATE,
        ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, LAST_KNOWN_FIXING_DATE, MONTH_LAG, 3, REFERENCE_START_DATE, REFERENCE_END_DATE, WEIGHT_START, WEIGHT_END, STRIKE, IS_CAP);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPay() {
    new CapFloorInflationYearOnYearInterpolationDefinition(CUR, null, ACCRUAL_START_DATE,
        ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, LAST_KNOWN_FIXING_DATE, MONTH_LAG, 3, REFERENCE_START_DATE, REFERENCE_END_DATE, WEIGHT_START, WEIGHT_END, STRIKE, IS_CAP);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStart() {
    new CapFloorInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, null,
        ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, LAST_KNOWN_FIXING_DATE, MONTH_LAG, 3, REFERENCE_START_DATE, REFERENCE_END_DATE, WEIGHT_START, WEIGHT_END, STRIKE, IS_CAP);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEnd() {
    new CapFloorInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE,
        null, 1.0, NOTIONAL, PRICE_INDEX, LAST_KNOWN_FIXING_DATE, MONTH_LAG, 3, REFERENCE_START_DATE, REFERENCE_END_DATE, WEIGHT_START, WEIGHT_END, STRIKE, IS_CAP);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex() {
    new CapFloorInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE,
        ACCRUAL_END_DATE, 1.0, NOTIONAL, null, LAST_KNOWN_FIXING_DATE, MONTH_LAG, 3, REFERENCE_START_DATE, REFERENCE_END_DATE, WEIGHT_START, WEIGHT_END, STRIKE, IS_CAP);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRefStart() {
    new CapFloorInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE,
        ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, LAST_KNOWN_FIXING_DATE, MONTH_LAG, 3, null, REFERENCE_END_DATE, WEIGHT_START, WEIGHT_END, STRIKE, IS_CAP);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRefEnd() {
    new CapFloorInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE,
        ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, LAST_KNOWN_FIXING_DATE, MONTH_LAG, 3, REFERENCE_START_DATE, null, WEIGHT_START, WEIGHT_END, STRIKE, IS_CAP);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullLastKnownFixingDate() {
    new CapFloorInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE,
        ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, null, MONTH_LAG, 3, REFERENCE_START_DATE, REFERENCE_END_DATE, WEIGHT_START, WEIGHT_END, STRIKE, IS_CAP);
  }

  @Test
  /**
   * Tests the class getter.
   */
  public void getter() {
    assertEquals("Inflation Year on Year cap: getter", CUR, YoY_CAP_DEFINITION.getCurrency());
    assertEquals("Inflation Year on Year cap: getter", PAYMENT_DATE, YoY_CAP_DEFINITION.getPaymentDate());
    assertEquals("Inflation Year on Year cap: getter", ACCRUAL_START_DATE, YoY_CAP_DEFINITION.getAccrualStartDate());
    assertEquals("Inflation Year on Year cap: getter", ACCRUAL_END_DATE, YoY_CAP_DEFINITION.getAccrualEndDate());
    assertEquals("Inflation Year on Year cap: getter", LAST_KNOWN_FIXING_DATE, YoY_CAP_DEFINITION.getLastKnownFixingDate());
    assertEquals("Inflation Year on Year cap: getter", 1.0, YoY_CAP_DEFINITION.getPaymentYearFraction());
    assertEquals("Inflation Year on Year cap: getter", NOTIONAL, YoY_CAP_DEFINITION.getNotional());
    assertEquals("Inflation Year on Year cap: getter", PRICE_INDEX, YoY_CAP_DEFINITION.getPriceIndex());
    assertEquals("Inflation Year on Year cap: getter", REFERENCE_START_DATE, YoY_CAP_DEFINITION.getReferenceStartDate());
    assertEquals("Inflation Year on Year cap: getter", REFERENCE_END_DATE, YoY_CAP_DEFINITION.getReferenceEndDate());
    assertEquals("Inflation Year on Year cap: getter", MONTH_LAG, YoY_CAP_DEFINITION.getConventionalMonthLag());
    assertEquals("Inflation Year on Year cap: getter", WEIGHT_START, YoY_CAP_DEFINITION.getWeightStart());
    assertEquals("Inflation Year on Year cap: getter", WEIGHT_END, YoY_CAP_DEFINITION.getWeightEnd());
    assertEquals("Inflation Year on Year cap: getter", IS_CAP, YoY_CAP_DEFINITION.isCap());
    assertEquals("Inflation Year on Year cap: getter", 1.0, YoY_CAP_DEFINITION.getPaymentYearFraction());
    assertEquals("Inflation Year on Year cap: getter", STRIKE, YoY_CAP_DEFINITION.getStrike());

  }

  @Test
  /**
   * Tests the equal and hash-code methods.
   */
  public void equalHash() {
    assertEquals(YoY_CAP_DEFINITION, YoY_CAP_DEFINITION);
    final CapFloorInflationYearOnYearInterpolationDefinition couponDuplicate = new CapFloorInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE,
        ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, LAST_KNOWN_FIXING_DATE, MONTH_LAG, 3, REFERENCE_START_DATE, REFERENCE_END_DATE, WEIGHT_START, WEIGHT_END, STRIKE, IS_CAP);
    assertEquals(YoY_CAP_DEFINITION, couponDuplicate);
    assertEquals(YoY_CAP_DEFINITION.hashCode(), couponDuplicate.hashCode());
    CapFloorInflationYearOnYearInterpolationDefinition modified;
    modified = new CapFloorInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE.minusDays(1), ACCRUAL_START_DATE,
        ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, LAST_KNOWN_FIXING_DATE, MONTH_LAG, 3, REFERENCE_START_DATE, REFERENCE_END_DATE, WEIGHT_START, WEIGHT_END, STRIKE, IS_CAP);
    assertFalse(YoY_CAP_DEFINITION.equals(modified));
    modified = new CapFloorInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE.minusDays(1),
        ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, LAST_KNOWN_FIXING_DATE, MONTH_LAG, 3, REFERENCE_START_DATE, REFERENCE_END_DATE, WEIGHT_START, WEIGHT_END, STRIKE, IS_CAP);
    assertFalse(YoY_CAP_DEFINITION.equals(modified));
    modified = new CapFloorInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE,
        ACCRUAL_END_DATE.minusDays(1), 1.0, NOTIONAL, PRICE_INDEX, LAST_KNOWN_FIXING_DATE, MONTH_LAG, 3, REFERENCE_START_DATE, REFERENCE_END_DATE, WEIGHT_START, WEIGHT_END, STRIKE, IS_CAP);
    assertFalse(YoY_CAP_DEFINITION.equals(modified));
    final ZonedDateTime[] modifiedReferenceStartDate = new ZonedDateTime[2];
    modifiedReferenceStartDate[0] = REFERENCE_START_DATE[0];
    modifiedReferenceStartDate[1] = REFERENCE_START_DATE[1].minusDays(1);
    modified = new CapFloorInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE,
        ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, LAST_KNOWN_FIXING_DATE, MONTH_LAG, 3, modifiedReferenceStartDate, REFERENCE_END_DATE, WEIGHT_START, WEIGHT_END, STRIKE, IS_CAP);
    assertFalse(YoY_CAP_DEFINITION.equals(modified));
    final ZonedDateTime[] modifiedReferenceEndDate = new ZonedDateTime[2];
    modifiedReferenceEndDate[0] = REFERENCE_END_DATE[0];
    modifiedReferenceEndDate[1] = REFERENCE_END_DATE[1].minusDays(1);
    modified = new CapFloorInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE,
        ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, LAST_KNOWN_FIXING_DATE, MONTH_LAG, 3, REFERENCE_START_DATE, modifiedReferenceEndDate, WEIGHT_START, WEIGHT_END, STRIKE, IS_CAP);
    assertFalse(YoY_CAP_DEFINITION.equals(modified));
    modified = new CapFloorInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE,
        ACCRUAL_END_DATE, 2.0, NOTIONAL, PRICE_INDEX, LAST_KNOWN_FIXING_DATE, MONTH_LAG, 3, REFERENCE_START_DATE, REFERENCE_END_DATE, WEIGHT_START, WEIGHT_END, STRIKE, IS_CAP);
    assertFalse(YoY_CAP_DEFINITION.equals(modified));
    modified = new CapFloorInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE,
        ACCRUAL_END_DATE, 1.0, NOTIONAL + 10.0, PRICE_INDEX, LAST_KNOWN_FIXING_DATE, MONTH_LAG, 3, REFERENCE_START_DATE, REFERENCE_END_DATE, WEIGHT_START, WEIGHT_END, STRIKE, IS_CAP);
    assertFalse(YoY_CAP_DEFINITION.equals(modified));
    modified = new CapFloorInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE,
        ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, LAST_KNOWN_FIXING_DATE, MONTH_LAG, 3, REFERENCE_START_DATE, REFERENCE_END_DATE, WEIGHT_START + .01, WEIGHT_END, STRIKE, IS_CAP);
    assertFalse(YoY_CAP_DEFINITION.equals(modified));
    modified = new CapFloorInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE,
        ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, LAST_KNOWN_FIXING_DATE, MONTH_LAG, 3, REFERENCE_START_DATE, REFERENCE_END_DATE, WEIGHT_START, WEIGHT_END + .01, STRIKE, IS_CAP);
    assertFalse(YoY_CAP_DEFINITION.equals(modified));
    modified = new CapFloorInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE,
        ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, LAST_KNOWN_FIXING_DATE, MONTH_LAG, 3, REFERENCE_START_DATE, REFERENCE_END_DATE, WEIGHT_START, WEIGHT_END, STRIKE + .01, IS_CAP);
    assertFalse(YoY_CAP_DEFINITION.equals(modified));
    modified = new CapFloorInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE,
        ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, LAST_KNOWN_FIXING_DATE, MONTH_LAG, 3, REFERENCE_START_DATE, REFERENCE_END_DATE, WEIGHT_START, WEIGHT_END, STRIKE, false);
    assertFalse(YoY_CAP_DEFINITION.equals(modified));
    modified = new CapFloorInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE,
        ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, LAST_KNOWN_FIXING_DATE.minusDays(1), MONTH_LAG, 3, REFERENCE_START_DATE, REFERENCE_END_DATE, WEIGHT_START, WEIGHT_END, STRIKE, IS_CAP);
    assertFalse(YoY_CAP_DEFINITION.equals(modified));
    modified = new CapFloorInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE,
        ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, LAST_KNOWN_FIXING_DATE, MONTH_LAG - 1, 3, REFERENCE_START_DATE, REFERENCE_END_DATE, WEIGHT_START, WEIGHT_END, STRIKE, IS_CAP);
    assertFalse(YoY_CAP_DEFINITION.equals(modified));
    final IndexPrice modifiedPriceIndex = new IndexPrice("US CPI x", Currency.USD);
    modified = new CapFloorInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE,
        ACCRUAL_END_DATE, 1.0, NOTIONAL, modifiedPriceIndex, LAST_KNOWN_FIXING_DATE, MONTH_LAG, 3, REFERENCE_START_DATE, REFERENCE_END_DATE, WEIGHT_START, WEIGHT_END, STRIKE, IS_CAP);
    assertFalse(YoY_CAP_DEFINITION.equals(modified));
  }

  @Test
  /**
   * Tests the first based on indexation lag.
   */
  public void from() {
    final CapFloorInflationYearOnYearInterpolationDefinition constructor = new CapFloorInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE,
        ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, LAST_KNOWN_FIXING_DATE, MONTH_LAG, 3, REFERENCE_START_DATE, REFERENCE_END_DATE, WEIGHT_START, WEIGHT_END, STRIKE, IS_CAP);

    final CouponInflationYearOnYearInterpolationDefinition yoyCoupon = new CouponInflationYearOnYearInterpolationDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE,
        ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, 3, REFERENCE_START_DATE, REFERENCE_END_DATE, false, WEIGHT_START, WEIGHT_END);
    final CapFloorInflationYearOnYearInterpolationDefinition from = CapFloorInflationYearOnYearInterpolationDefinition.from(yoyCoupon, LAST_KNOWN_FIXING_DATE, STRIKE, IS_CAP);
    assertEquals("Inflation zero-coupon : from", constructor, from);
  }

  @Test
  public void toDerivativesNoData() {
    final ZonedDateTime pricingDate = DateUtils.getUTCDate(2011, 7, 29);
    final Coupon zeroCouponConverted = YoY_CAP_DEFINITION.toDerivative(pricingDate);
    //lastKnownFixingTime could be negatif so we don't use the dayfraction
    final double lastKnownFixingTime = TimeCalculator.getTimeBetween(pricingDate, LAST_KNOWN_FIXING_DATE);
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
    final CapFloorInflationYearOnYearInterpolation zeroCoupon = new CapFloorInflationYearOnYearInterpolation(CUR, paymentTime, 1.0, NOTIONAL, PRICE_INDEX, lastKnownFixingTime,
        referenceStartTime, naturalPaymentStartPaymentTime, referenceEndTime, naturalPaymentEndPaymentTime, WEIGHT_START, WEIGHT_END, STRIKE, IS_CAP);
    assertEquals("Inflation zero-coupon: toDerivative", zeroCouponConverted, zeroCoupon);
  }

  @Test
  public void toDerivativesStartMonthNotknown() {
    final ZonedDateTime pricingDate = DateUtils.getUTCDate(2011, 7, 29);
    final DoubleTimeSeries<ZonedDateTime> priceIndexTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2017, 5, 1),
        DateUtils.getUTCDate(2017, 6, 1), DateUtils.getUTCDate(2018, 5, 1), DateUtils.getUTCDate(2018, 6, 1) },
        new double[] {
            127.23, 127.43, 128.23, 128.43 });
    final Coupon zeroCouponConverted = YoY_CAP_DEFINITION.toDerivative(pricingDate, priceIndexTS);
    // lastKnownFixingTime could be negatif so we don't use the dayfraction
    final double lastKnownFixingTime = TimeCalculator.getTimeBetween(pricingDate, LAST_KNOWN_FIXING_DATE);
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
    final CapFloorInflationYearOnYearInterpolation zeroCoupon = new CapFloorInflationYearOnYearInterpolation(CUR, paymentTime, 1.0, NOTIONAL, PRICE_INDEX, lastKnownFixingTime,
        referenceStartTime, naturalPaymentStartPaymentTime, referenceEndTime, naturalPaymentEndPaymentTime, WEIGHT_START, WEIGHT_END, STRIKE, IS_CAP);
    assertEquals("Inflation zero-coupon: toDerivative", zeroCoupon, zeroCouponConverted);
  }

  @Test
  public void toDerivativesStartMonthKnown() {
    final ZonedDateTime pricingDate = DateUtils.getUTCDate(2018, 6, 25);
    final DoubleTimeSeries<ZonedDateTime> priceIndexTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2017, 5, 1),
        DateUtils.getUTCDate(2017, 6, 1), DateUtils.getUTCDate(2018, 5, 1), DateUtils.getUTCDate(2018, 6, 1) },
        new double[] {
            127.23, 127.43, 128.23, 128.43 });
    final Coupon zeroCouponConverted = YoY_CAP_DEFINITION.toDerivative(pricingDate, priceIndexTS);
    final double paymentTime = ACT_ACT.getDayCountFraction(pricingDate, PAYMENT_DATE);
    final CouponFixed zeroCoupon = new CouponFixed(CUR, paymentTime, 1.0, NOTIONAL, Math.max((WEIGHT_END * 128.23 + (1 - WEIGHT_END) * 128.43) /
        (WEIGHT_START * 127.23 + (1 - WEIGHT_START) * 127.43) - 1.0 - STRIKE, 0.0));
    assertEquals("Inflation zero-coupon: toDerivative", zeroCoupon, zeroCouponConverted);
  }

}
