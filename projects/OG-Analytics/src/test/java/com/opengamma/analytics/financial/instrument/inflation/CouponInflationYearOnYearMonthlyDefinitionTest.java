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
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearMonthly;
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
 * Tests the year on year inflation constructors.
 */
@Test(groups = TestGroup.UNIT)
public class CouponInflationYearOnYearMonthlyDefinitionTest {

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
  private static final ZonedDateTime REFERENCE_END_DATE = PAYMENT_DATE.minusMonths(MONTH_LAG).with(TemporalAdjusters.lastDayOfMonth());
  private static final ZonedDateTime REFERENCE_START_DATE = REFERENCE_END_DATE.minusMonths(12).with(TemporalAdjusters.lastDayOfMonth());
  private static final CouponInflationYearOnYearMonthlyDefinition YoY_COUPON_DEFINITION = new CouponInflationYearOnYearMonthlyDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, 1.0,
      NOTIONAL, PRICE_INDEX, MONTH_LAG, 3, REFERENCE_START_DATE, REFERENCE_END_DATE, false);
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new CouponInflationYearOnYearMonthlyDefinition(null, PAYMENT_DATE, ACCRUAL_START_DATE, PAYMENT_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, 3, REFERENCE_START_DATE,
        REFERENCE_END_DATE, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPay() {
    new CouponInflationYearOnYearMonthlyDefinition(CUR, null, ACCRUAL_START_DATE, PAYMENT_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, 3, REFERENCE_START_DATE,
        REFERENCE_END_DATE, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStart() {
    new CouponInflationYearOnYearMonthlyDefinition(CUR, PAYMENT_DATE, null, PAYMENT_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, 3, REFERENCE_START_DATE,
        REFERENCE_END_DATE, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEnd() {
    new CouponInflationYearOnYearMonthlyDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, null, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, 3, REFERENCE_START_DATE,
        REFERENCE_END_DATE, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex() {
    new CouponInflationYearOnYearMonthlyDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, PAYMENT_DATE, 1.0, NOTIONAL, null, MONTH_LAG, 3, REFERENCE_START_DATE,
        REFERENCE_END_DATE, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRefStart() {
    new CouponInflationYearOnYearMonthlyDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, PAYMENT_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, 3, null, REFERENCE_END_DATE, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRefEnd() {
    new CouponInflationYearOnYearMonthlyDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, PAYMENT_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, 3, REFERENCE_START_DATE, null, false);
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
    assertEquals("Inflation Year on Year coupon: getter", REFERENCE_START_DATE, YoY_COUPON_DEFINITION.getReferenceStartDate());
    assertEquals("Inflation Year on Year coupon: getter", REFERENCE_END_DATE, YoY_COUPON_DEFINITION.getReferenceEndDate());
    assertEquals("Inflation Year on Year coupon: getter", MONTH_LAG, YoY_COUPON_DEFINITION.getConventionalMonthLag());
  }

  /**
   *
   */
  @Test
  /**
   * Tests the equal and hash-code methods.
   */
  public void equalHash() {
    assertEquals(YoY_COUPON_DEFINITION, YoY_COUPON_DEFINITION);
    final CouponInflationYearOnYearMonthlyDefinition couponDuplicate = new CouponInflationYearOnYearMonthlyDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, 1.0, NOTIONAL,
        PRICE_INDEX,
        MONTH_LAG, 3, REFERENCE_START_DATE, REFERENCE_END_DATE, false);
    assertEquals(YoY_COUPON_DEFINITION, couponDuplicate);
    assertEquals(YoY_COUPON_DEFINITION.hashCode(), couponDuplicate.hashCode());
    CouponInflationYearOnYearMonthlyDefinition modified;
    modified = new CouponInflationYearOnYearMonthlyDefinition(Currency.AUD, ACCRUAL_END_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, 3,
        REFERENCE_START_DATE, REFERENCE_END_DATE, false);
    assertFalse(YoY_COUPON_DEFINITION.equals(modified));
    modified = new CouponInflationYearOnYearMonthlyDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE.minusDays(1), ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, 3,
        REFERENCE_START_DATE, REFERENCE_END_DATE, false);
    assertFalse(YoY_COUPON_DEFINITION.equals(modified));
    modified = new CouponInflationYearOnYearMonthlyDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE.minusDays(1), 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, 3,
        REFERENCE_START_DATE, REFERENCE_END_DATE, false);
    assertFalse(YoY_COUPON_DEFINITION.equals(modified));
    modified = new CouponInflationYearOnYearMonthlyDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, 3,
        REFERENCE_START_DATE.minusDays(1), REFERENCE_END_DATE, false);
    assertFalse(YoY_COUPON_DEFINITION.equals(modified));
    modified = new CouponInflationYearOnYearMonthlyDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, 3,
        REFERENCE_START_DATE, REFERENCE_END_DATE.minusDays(1), false);
    assertFalse(YoY_COUPON_DEFINITION.equals(modified));
    modified = new CouponInflationYearOnYearMonthlyDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, 2.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, 3,
        REFERENCE_START_DATE, REFERENCE_END_DATE, false);
    assertFalse(YoY_COUPON_DEFINITION.equals(modified));
    modified = new CouponInflationYearOnYearMonthlyDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, 1.0, NOTIONAL + 10.0, PRICE_INDEX, MONTH_LAG, 3,
        REFERENCE_START_DATE, REFERENCE_END_DATE, false);
    assertFalse(YoY_COUPON_DEFINITION.equals(modified));
  }

  @Test
  /**
   * Tests the first based on indexation lag.
   */
  public void from2() {
    final CouponInflationYearOnYearMonthlyDefinition constructor = new CouponInflationYearOnYearMonthlyDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, PAYMENT_DATE, 1.0, NOTIONAL, PRICE_INDEX,
        MONTH_LAG,
        3, REFERENCE_START_DATE, REFERENCE_END_DATE, false);
    final CouponInflationYearOnYearMonthlyDefinition from = CouponInflationYearOnYearMonthlyDefinition.from(ACCRUAL_START_DATE, PAYMENT_DATE, NOTIONAL, PRICE_INDEX, MONTH_LAG, false);
    assertEquals("Inflation zero-coupon : from", constructor, from);
  }

  @Test
  public void toDerivativesNoData() {
    final ZonedDateTime pricingDate = DateUtils.getUTCDate(2011, 7, 29);
    final Coupon yearOnYearCouponConverted = YoY_COUPON_DEFINITION.toDerivative(pricingDate);
    final double paymentTime = ACT_ACT.getDayCountFraction(pricingDate, PAYMENT_DATE);
    final double referenceStartTime = ACT_ACT.getDayCountFraction(pricingDate, REFERENCE_START_DATE);
    final double referenceEndTime = ACT_ACT.getDayCountFraction(pricingDate, REFERENCE_END_DATE);
    final double naturalPaymentStartPaymentTime = ACT_ACT.getDayCountFraction(pricingDate, ACCRUAL_START_DATE);
    final double naturalPaymentEndPaymentTime = ACT_ACT.getDayCountFraction(pricingDate, ACCRUAL_END_DATE);
    final CouponInflationYearOnYearMonthly yearOnYearCoupon = new CouponInflationYearOnYearMonthly(CUR, paymentTime, 1.0, NOTIONAL, PRICE_INDEX, referenceStartTime,
        naturalPaymentStartPaymentTime, referenceEndTime, naturalPaymentEndPaymentTime, false);
    assertEquals("Inflation zero-coupon: toDerivative", yearOnYearCouponConverted, yearOnYearCoupon);
  }

  @Test
  public void toDerivativesStartMonthNotknown() {
    final ZonedDateTime pricingDate = DateUtils.getUTCDate(2011, 7, 29);
    final DoubleTimeSeries<ZonedDateTime> priceIndexTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2017, 4, 30),
      DateUtils.getUTCDate(2017, 5, 31), DateUtils.getUTCDate(2018, 4, 30), DateUtils.getUTCDate(2018, 5, 31) },
        new double[] {
          127.23, 127.43, 128.23, 128.43 });
    final Coupon yearOnYearCouponConverted = YoY_COUPON_DEFINITION.toDerivative(pricingDate, priceIndexTS);
    final double paymentTime = ACT_ACT.getDayCountFraction(pricingDate, PAYMENT_DATE);
    final double referenceStartTime = ACT_ACT.getDayCountFraction(pricingDate, REFERENCE_START_DATE);
    final double referenceEndTime = ACT_ACT.getDayCountFraction(pricingDate, REFERENCE_END_DATE);
    final double naturalPaymentStartPaymentTime = ACT_ACT.getDayCountFraction(pricingDate, ACCRUAL_START_DATE);
    final double naturalPaymentEndPaymentTime = ACT_ACT.getDayCountFraction(pricingDate, ACCRUAL_END_DATE);
    final CouponInflationYearOnYearMonthly yearOnYearCoupon = new CouponInflationYearOnYearMonthly(CUR, paymentTime, 1.0, NOTIONAL, PRICE_INDEX, referenceStartTime,
        naturalPaymentStartPaymentTime, referenceEndTime, naturalPaymentEndPaymentTime, false);
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
    final CouponFixed zeroCoupon = new CouponFixed(CUR, paymentTime, 1.0, NOTIONAL, 128.23 / 127.23 - 1.0);
    assertEquals("Inflation zero-coupon: toDerivative", zeroCoupon, zeroCouponConverted);
  }

}
