/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
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
 * Tests the zero-coupon inflation constructors.
 */
@Test(groups = TestGroup.UNIT)
public class CouponInflationZeroCouponMonthlyDefinitionTest {
  private static final String NAME = "Euro HICP x";
  private static final Currency CUR = Currency.EUR;
  private static final IndexPrice PRICE_INDEX = new IndexPrice(NAME, CUR);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final Period COUPON_TENOR = Period.ofYears(10);
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(START_DATE, COUPON_TENOR, BUSINESS_DAY, CALENDAR);
  private static final ZonedDateTime ACCRUAL_END_DATE = PAYMENT_DATE.minusDays(1); // For getter test
  private static final double NOTIONAL = 98765432;
  private static final int MONTH_LAG = 3;
  private static final double INDEX_APRIL_2008 = 108.23; // 3 m before Aug: May / 1 May index = May index: 108.23
  private static final DoubleTimeSeries<ZonedDateTime> HICPX_TS = MulticurveProviderDiscountDataSets.euroHICPXFrom2009();
  private static final ZonedDateTime REFERENCE_START_DATE = DateUtils.getUTCDate(2008, 5, 31);
  private static final ZonedDateTime REFERENCE_END_DATE = PAYMENT_DATE.minusMonths(MONTH_LAG).with(TemporalAdjusters.lastDayOfMonth());
  private static final CouponInflationZeroCouponMonthlyDefinition ZERO_COUPON_DEFINITION = new CouponInflationZeroCouponMonthlyDefinition(CUR, PAYMENT_DATE, START_DATE, ACCRUAL_END_DATE, 1.0,
      NOTIONAL, PRICE_INDEX, MONTH_LAG, 3, REFERENCE_START_DATE, REFERENCE_END_DATE, false);
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new CouponInflationZeroCouponMonthlyDefinition(null, PAYMENT_DATE, START_DATE, PAYMENT_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, 3, REFERENCE_START_DATE, REFERENCE_END_DATE,
        false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPay() {
    new CouponInflationZeroCouponMonthlyDefinition(CUR, null, START_DATE, PAYMENT_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, 3, REFERENCE_START_DATE, REFERENCE_END_DATE,
        false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStart() {
    new CouponInflationZeroCouponMonthlyDefinition(CUR, PAYMENT_DATE, null, PAYMENT_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, 3, REFERENCE_START_DATE, REFERENCE_END_DATE,
        false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEnd() {
    new CouponInflationZeroCouponMonthlyDefinition(CUR, PAYMENT_DATE, START_DATE, null, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, 3, REFERENCE_START_DATE, REFERENCE_END_DATE,
        false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex() {
    new CouponInflationZeroCouponMonthlyDefinition(CUR, PAYMENT_DATE, START_DATE, PAYMENT_DATE, 1.0, NOTIONAL, null, MONTH_LAG, 3, REFERENCE_START_DATE, REFERENCE_END_DATE,
        false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRefStart() {
    new CouponInflationZeroCouponMonthlyDefinition(CUR, PAYMENT_DATE, START_DATE, PAYMENT_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, 3, null, REFERENCE_END_DATE, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRefEnd() {
    new CouponInflationZeroCouponMonthlyDefinition(CUR, PAYMENT_DATE, START_DATE, PAYMENT_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, 3, REFERENCE_START_DATE, null, false);
  }

  @Test
  /**
   * Tests the class getter.
   */
  public void getter() {
    assertEquals("Inflation Zero-coupon: getter", CUR, ZERO_COUPON_DEFINITION.getCurrency());
    assertEquals("Inflation Zero-coupon: getter", PAYMENT_DATE, ZERO_COUPON_DEFINITION.getPaymentDate());
    assertEquals("Inflation Zero-coupon: getter", START_DATE, ZERO_COUPON_DEFINITION.getAccrualStartDate());
    assertEquals("Inflation Zero-coupon: getter", ACCRUAL_END_DATE, ZERO_COUPON_DEFINITION.getAccrualEndDate());
    assertEquals("Inflation Zero-coupon: getter", 1.0, ZERO_COUPON_DEFINITION.getPaymentYearFraction());
    assertEquals("Inflation Zero-coupon: getter", NOTIONAL, ZERO_COUPON_DEFINITION.getNotional());
    assertEquals("Inflation Zero-coupon: getter", PRICE_INDEX, ZERO_COUPON_DEFINITION.getPriceIndex());
    assertEquals("Inflation Zero-coupon: getter", REFERENCE_START_DATE, ZERO_COUPON_DEFINITION.getReferenceStartDate());
    assertEquals("Inflation Zero-coupon: getter", REFERENCE_END_DATE, ZERO_COUPON_DEFINITION.getReferenceEndDate());
    assertEquals("Inflation Zero-coupon: getter", MONTH_LAG, ZERO_COUPON_DEFINITION.getMonthLag());
  }

  @Test
  /**
   * Tests the equal and hash-code methods.
   */
  public void equalHash() {
    assertEquals(ZERO_COUPON_DEFINITION, ZERO_COUPON_DEFINITION);
    final CouponInflationZeroCouponMonthlyDefinition couponDuplicate = new CouponInflationZeroCouponMonthlyDefinition(CUR, PAYMENT_DATE, START_DATE, ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX,
        MONTH_LAG,
        3, REFERENCE_START_DATE, REFERENCE_END_DATE, false);
    assertEquals(ZERO_COUPON_DEFINITION, couponDuplicate);
    assertEquals(ZERO_COUPON_DEFINITION.hashCode(), couponDuplicate.hashCode());
    CouponInflationZeroCouponMonthlyDefinition modified;
    modified = new CouponInflationZeroCouponMonthlyDefinition(CUR, ACCRUAL_END_DATE, START_DATE, ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, MONTH_LAG, REFERENCE_START_DATE,
        REFERENCE_END_DATE, false);
    assertFalse(ZERO_COUPON_DEFINITION.equals(modified));
    modified = new CouponInflationZeroCouponMonthlyDefinition(CUR, PAYMENT_DATE, START_DATE.minusDays(1), ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, MONTH_LAG,
        REFERENCE_START_DATE, REFERENCE_END_DATE, false);
    assertFalse(ZERO_COUPON_DEFINITION.equals(modified));
    modified = new CouponInflationZeroCouponMonthlyDefinition(CUR, PAYMENT_DATE, START_DATE, ACCRUAL_END_DATE.minusDays(1), 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, MONTH_LAG,
        REFERENCE_START_DATE, REFERENCE_END_DATE, false);
    assertFalse(ZERO_COUPON_DEFINITION.equals(modified));
    modified = new CouponInflationZeroCouponMonthlyDefinition(CUR, PAYMENT_DATE, START_DATE, ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, MONTH_LAG,
        REFERENCE_START_DATE.minusDays(1), REFERENCE_END_DATE, false);
    assertFalse(ZERO_COUPON_DEFINITION.equals(modified));
    modified = new CouponInflationZeroCouponMonthlyDefinition(CUR, PAYMENT_DATE, START_DATE, ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, MONTH_LAG, REFERENCE_START_DATE,
        REFERENCE_END_DATE.minusDays(1), false);
    assertFalse(ZERO_COUPON_DEFINITION.equals(modified));
    modified = new CouponInflationZeroCouponMonthlyDefinition(CUR, PAYMENT_DATE, START_DATE, ACCRUAL_END_DATE, 2.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, MONTH_LAG, REFERENCE_START_DATE,
        REFERENCE_END_DATE, false);
    assertFalse(ZERO_COUPON_DEFINITION.equals(modified));
    modified = new CouponInflationZeroCouponMonthlyDefinition(CUR, PAYMENT_DATE, START_DATE, ACCRUAL_END_DATE, 1.0, NOTIONAL + 10.0, PRICE_INDEX, MONTH_LAG, MONTH_LAG, REFERENCE_START_DATE,
        REFERENCE_END_DATE, false);
    assertFalse(ZERO_COUPON_DEFINITION.equals(modified));
  }

  @Test
  /**
   * Tests the first builder.
   */
  public void from1() {
    final CouponInflationZeroCouponMonthlyDefinition constructor = new CouponInflationZeroCouponMonthlyDefinition(CUR, PAYMENT_DATE, START_DATE, PAYMENT_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG,
        3, START_DATE, REFERENCE_END_DATE, false);
    final CouponInflationZeroCouponMonthlyDefinition from = CouponInflationZeroCouponMonthlyDefinition.from(START_DATE, PAYMENT_DATE, NOTIONAL, PRICE_INDEX, MONTH_LAG, MONTH_LAG, REFERENCE_END_DATE
        );
    assertEquals("Inflation zero-coupon : from", constructor, from);
  }

  @Test
  /**
   * Tests the first based on indexation lag.
   */
  public void from2() {
    final CouponInflationZeroCouponMonthlyDefinition constructor = new CouponInflationZeroCouponMonthlyDefinition(CUR, PAYMENT_DATE, START_DATE, PAYMENT_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG,
        MONTH_LAG, REFERENCE_START_DATE, REFERENCE_END_DATE, false);
    final CouponInflationZeroCouponMonthlyDefinition from = CouponInflationZeroCouponMonthlyDefinition.from(START_DATE, PAYMENT_DATE, NOTIONAL, PRICE_INDEX, MONTH_LAG, MONTH_LAG, false);
    assertEquals("Inflation zero-coupon : from", constructor, from);
  }

  @Test(enabled = false)
  /**
   * Tests the first based on indexation lag.
   */
  public void from3() {
    final CouponInflationZeroCouponMonthlyDefinition constructor = new CouponInflationZeroCouponMonthlyDefinition(CUR, PAYMENT_DATE, START_DATE, PAYMENT_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG,
        3, REFERENCE_START_DATE, REFERENCE_END_DATE, false);
    final CouponInflationZeroCouponMonthlyDefinition from = CouponInflationZeroCouponMonthlyDefinition.from(START_DATE, PAYMENT_DATE, NOTIONAL, PRICE_INDEX, MONTH_LAG, MONTH_LAG, false);
    assertEquals("Inflation zero-coupon : from", constructor, from);
  }

  /*@Test
  public void toDerivativesNoData() {
    final ZonedDateTime pricingDate = DateUtils.getUTCDate(2011, 7, 29);
    final Coupon zeroCouponConverted = ZERO_COUPON_DEFINITION.toDerivative(pricingDate);
    final double paymentTime = ACT_ACT.getDayCountFraction(pricingDate, PAYMENT_DATE);
    final double referenceEndTime = ACT_ACT.getDayCountFraction(pricingDate, REFERENCE_END_DATE);
    final double naturalPaymentPaymentTime = TimeCalculator.getTimeBetween(pricingDate, PAYMENT_DATE);
    final CouponInflationZeroCouponMonthly zeroCoupon = new CouponInflationZeroCouponMonthly(CUR, paymentTime, 1.0, NOTIONAL, PRICE_INDEX, INDEX_APRIL_2008, referenceEndTime,
        naturalPaymentPaymentTime,
        false);
    assertEquals("Inflation zero-coupon: toDerivative", zeroCouponConverted, zeroCoupon);
  }*/

  // Implementation note : this test could be useful if we decide to add an indexstartvalue to the definition class.
  /*@Test
  public void toDerivativesStartMonthNotknown() {
    final ZonedDateTime pricingDate = DateUtils.getUTCDate(2011, 7, 29);
    final DoubleTimeSeries<ZonedDateTime> priceIndexTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2008, 5, 31), DateUtils.getUTCDate(2008, 6, 30) },
        new double[] {
          128.23, 128.43 });
    final Coupon zeroCouponConverted = ZERO_COUPON_DEFINITION.toDerivative(pricingDate, priceIndexTS);
    final double paymentTime = ACT_ACT.getDayCountFraction(pricingDate, PAYMENT_DATE);
    final double referenceEndTime = ACT_ACT.getDayCountFraction(pricingDate, REFERENCE_END_DATE);
    final double naturalPaymentPaymentTime = TimeCalculator.getTimeBetween(pricingDate, PAYMENT_DATE);
    final CouponInflationZeroCouponMonthly zeroCoupon = new CouponInflationZeroCouponMonthly(CUR, paymentTime, 1.0, NOTIONAL, PRICE_INDEX, INDEX_APRIL_2008, referenceEndTime,
        naturalPaymentPaymentTime, false);
    assertEquals("Inflation zero-coupon: toDerivative", zeroCoupon, zeroCouponConverted);

  }*/

  @Test
  public void toDerivativesStartMonthKnown() {
    final ZonedDateTime pricingDate = DateUtils.getUTCDate(2018, 8, 1);
    final DoubleTimeSeries<ZonedDateTime> priceIndexTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2008, 5, 31), DateUtils.getUTCDate(2018, 5, 31),
      DateUtils.getUTCDate(2018, 6, 30) },
        new double[] {108.23, 128.23, 128.43 });
    final Coupon zeroCouponConverted = ZERO_COUPON_DEFINITION.toDerivative(pricingDate, priceIndexTS);
    final double paymentTime = ACT_ACT.getDayCountFraction(pricingDate, PAYMENT_DATE);
    final CouponFixed zeroCoupon = new CouponFixed(CUR, paymentTime, 1.0, NOTIONAL, 128.23 / INDEX_APRIL_2008 - 1.0);
    assertEquals("Inflation zero-coupon: toDerivative", zeroCoupon, zeroCouponConverted);
  }

}
