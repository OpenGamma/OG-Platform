/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.inflation;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.financial.instrument.index.PriceIndex;
import com.opengamma.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.financial.instrument.payment.PaymentDefinition;
import com.opengamma.financial.instrument.swap.SwapFixedInflationZeroCouponDefinition;
import com.opengamma.financial.interestrate.inflation.derivatives.CouponInflationZeroCouponInterpolation;
import com.opengamma.financial.interestrate.market.MarketDataSets;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.TimeCalculator;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;

/**
 * Tests the zero-coupon inflation constructors.
 */
public class CouponInflationZeroCouponInterpolationDefinitionTest {
  private static final PriceIndex[] PRICE_INDEXES = MarketDataSets.getPriceIndexes();
  private static final PriceIndex PRICE_INDEX_EUR = PRICE_INDEXES[0];
  private static final Currency CUR = PRICE_INDEX_EUR.getCurrency();
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean EOM = true;
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final int COUPON_TENOR_YEAR = 10;
  private static final Period COUPON_TENOR = Period.ofYears(COUPON_TENOR_YEAR);
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(START_DATE, BUSINESS_DAY, CALENDAR, EOM, COUPON_TENOR);
  private static final ZonedDateTime ACCRUAL_END_DATE = PAYMENT_DATE.minusDays(1); // For getter test
  private static final double NOTIONAL = 98765432;
  private static final int MONTH_LAG = 3;
  private static final double INDEX_MAY_2008 = 108.45483870967742; // May index: 108.23 - June Index = 108.64
  private static final ZonedDateTime REFERENCE_START_DATE = DateUtils.getUTCDate(2008, 5, 18);
  private static final ZonedDateTime[] REFERENCE_END_DATE = new ZonedDateTime[2];
  static {
    REFERENCE_END_DATE[0] = PAYMENT_DATE.minusMonths(MONTH_LAG).withDayOfMonth(1);
    REFERENCE_END_DATE[1] = PAYMENT_DATE.minusMonths(MONTH_LAG - 1).withDayOfMonth(1);
  }
  private static final ZonedDateTime FIXING_DATE = REFERENCE_END_DATE[1].plusMonths(1).withDayOfMonth(1).plusWeeks(2);
  private static final CouponInflationZeroCouponInterpolationDefinition ZERO_COUPON_DEFINITION = new CouponInflationZeroCouponInterpolationDefinition(CUR, PAYMENT_DATE, START_DATE, ACCRUAL_END_DATE,
      1.0, NOTIONAL, PRICE_INDEX_EUR, MONTH_LAG, REFERENCE_START_DATE, INDEX_MAY_2008, REFERENCE_END_DATE, FIXING_DATE, false);
  private static final String DISCOUNTING_CURVE_NAME = "Discounting";
  private static final String PRICE_INDEX_CURVE_NAME = "Price index";
  private static final String[] CURVE_NAMES = new String[] {DISCOUNTING_CURVE_NAME, PRICE_INDEX_CURVE_NAME};
  //  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final DoubleTimeSeries<ZonedDateTime> HICPX_TS = MarketDataSets.euroHICPXFrom2009();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new CouponInflationZeroCouponInterpolationDefinition(null, PAYMENT_DATE, START_DATE, ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX_EUR, MONTH_LAG, REFERENCE_START_DATE, INDEX_MAY_2008,
        REFERENCE_END_DATE, FIXING_DATE, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPay() {
    new CouponInflationZeroCouponInterpolationDefinition(CUR, null, START_DATE, ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX_EUR, MONTH_LAG, REFERENCE_START_DATE, INDEX_MAY_2008, REFERENCE_END_DATE,
        FIXING_DATE, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStart() {
    new CouponInflationZeroCouponInterpolationDefinition(CUR, PAYMENT_DATE, null, ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX_EUR, MONTH_LAG, REFERENCE_START_DATE, INDEX_MAY_2008,
        REFERENCE_END_DATE, FIXING_DATE, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEnd() {
    new CouponInflationZeroCouponInterpolationDefinition(CUR, PAYMENT_DATE, START_DATE, null, 1.0, NOTIONAL, PRICE_INDEX_EUR, MONTH_LAG, REFERENCE_START_DATE, INDEX_MAY_2008, REFERENCE_END_DATE,
        FIXING_DATE, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex() {
    new CouponInflationZeroCouponInterpolationDefinition(CUR, PAYMENT_DATE, START_DATE, ACCRUAL_END_DATE, 1.0, NOTIONAL, null, MONTH_LAG, REFERENCE_START_DATE, INDEX_MAY_2008, REFERENCE_END_DATE,
        FIXING_DATE, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRefStart() {
    new CouponInflationZeroCouponInterpolationDefinition(CUR, PAYMENT_DATE, START_DATE, ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX_EUR, MONTH_LAG, null, INDEX_MAY_2008, REFERENCE_END_DATE,
        FIXING_DATE, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRefEnd() {
    new CouponInflationZeroCouponInterpolationDefinition(CUR, PAYMENT_DATE, START_DATE, ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX_EUR, MONTH_LAG, REFERENCE_START_DATE, INDEX_MAY_2008, null,
        FIXING_DATE, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFixing() {
    new CouponInflationZeroCouponInterpolationDefinition(CUR, PAYMENT_DATE, START_DATE, ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX_EUR, MONTH_LAG, REFERENCE_START_DATE, INDEX_MAY_2008,
        REFERENCE_END_DATE, null, false);
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
    assertEquals("Inflation Zero-coupon: getter", PRICE_INDEX_EUR, ZERO_COUPON_DEFINITION.getPriceIndex());
    assertEquals("Inflation Zero-coupon: getter", REFERENCE_START_DATE, ZERO_COUPON_DEFINITION.getReferenceStartDate());
    assertEquals("Inflation Zero-coupon: getter", REFERENCE_END_DATE, ZERO_COUPON_DEFINITION.getReferenceEndDate());
    assertEquals("Inflation Zero-coupon: getter", INDEX_MAY_2008, ZERO_COUPON_DEFINITION.getIndexStartValue());
    assertEquals("Inflation Zero-coupon: getter", FIXING_DATE, ZERO_COUPON_DEFINITION.getFixingEndDate());
    double weight = 1.0 - (PAYMENT_DATE.getDayOfMonth() - 1.0) / PAYMENT_DATE.getMonthOfYear().getLastDayOfMonth(PAYMENT_DATE.isLeapYear());
    assertEquals("Inflation Zero-coupon: getter", weight, ZERO_COUPON_DEFINITION.getWeight());
  }

  @Test
  /**
   * Tests the equal and hash-code methods.
   */
  public void equalHash() {
    assertEquals(ZERO_COUPON_DEFINITION, ZERO_COUPON_DEFINITION);
    CouponInflationZeroCouponInterpolationDefinition couponDuplicate = new CouponInflationZeroCouponInterpolationDefinition(CUR, PAYMENT_DATE, START_DATE, ACCRUAL_END_DATE, 1.0, NOTIONAL,
        PRICE_INDEX_EUR, MONTH_LAG, REFERENCE_START_DATE, INDEX_MAY_2008, REFERENCE_END_DATE, FIXING_DATE, false);
    assertEquals(ZERO_COUPON_DEFINITION, couponDuplicate);
    assertEquals(ZERO_COUPON_DEFINITION.hashCode(), couponDuplicate.hashCode());
    CouponInflationZeroCouponInterpolationDefinition modified;
    modified = new CouponInflationZeroCouponInterpolationDefinition(CUR, ACCRUAL_END_DATE, START_DATE, ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX_EUR, MONTH_LAG, REFERENCE_START_DATE,
        INDEX_MAY_2008, REFERENCE_END_DATE, FIXING_DATE, false);
    assertFalse(ZERO_COUPON_DEFINITION.equals(modified));
    modified = new CouponInflationZeroCouponInterpolationDefinition(CUR, PAYMENT_DATE, START_DATE.minusDays(1), ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX_EUR, MONTH_LAG, REFERENCE_START_DATE,
        INDEX_MAY_2008, REFERENCE_END_DATE, FIXING_DATE, false);
    assertFalse(ZERO_COUPON_DEFINITION.equals(modified));
    modified = new CouponInflationZeroCouponInterpolationDefinition(CUR, PAYMENT_DATE, START_DATE, ACCRUAL_END_DATE.minusDays(1), 1.0, NOTIONAL, PRICE_INDEX_EUR, MONTH_LAG, REFERENCE_START_DATE,
        INDEX_MAY_2008, REFERENCE_END_DATE, FIXING_DATE, false);
    assertFalse(ZERO_COUPON_DEFINITION.equals(modified));
    modified = new CouponInflationZeroCouponInterpolationDefinition(CUR, PAYMENT_DATE, START_DATE, ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX_EUR, MONTH_LAG, REFERENCE_START_DATE.minusDays(1),
        INDEX_MAY_2008, REFERENCE_END_DATE, FIXING_DATE, false);
    assertFalse(ZERO_COUPON_DEFINITION.equals(modified));
    ZonedDateTime[] modifiedReference = new ZonedDateTime[2];
    modifiedReference[0] = REFERENCE_END_DATE[0];
    modifiedReference[0] = REFERENCE_END_DATE[0].minusDays(1);
    modified = new CouponInflationZeroCouponInterpolationDefinition(CUR, PAYMENT_DATE, START_DATE, ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX_EUR, MONTH_LAG, REFERENCE_START_DATE, INDEX_MAY_2008,
        modifiedReference, FIXING_DATE, false);
    assertFalse(ZERO_COUPON_DEFINITION.equals(modified));
    modified = new CouponInflationZeroCouponInterpolationDefinition(CUR, PAYMENT_DATE, START_DATE, ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX_EUR, MONTH_LAG, REFERENCE_START_DATE, INDEX_MAY_2008,
        REFERENCE_END_DATE, FIXING_DATE.minusDays(1), false);
    assertFalse(ZERO_COUPON_DEFINITION.equals(modified));
    modified = new CouponInflationZeroCouponInterpolationDefinition(CUR, PAYMENT_DATE, START_DATE, ACCRUAL_END_DATE, 2.0, NOTIONAL, PRICE_INDEX_EUR, MONTH_LAG, REFERENCE_START_DATE, INDEX_MAY_2008,
        REFERENCE_END_DATE, FIXING_DATE, false);
    assertFalse(ZERO_COUPON_DEFINITION.equals(modified));
    modified = new CouponInflationZeroCouponInterpolationDefinition(CUR, PAYMENT_DATE, START_DATE, ACCRUAL_END_DATE, 1.0, NOTIONAL + 10, PRICE_INDEX_EUR, MONTH_LAG, REFERENCE_START_DATE,
        INDEX_MAY_2008, REFERENCE_END_DATE, FIXING_DATE, false);
    assertFalse(ZERO_COUPON_DEFINITION.equals(modified));
    modified = new CouponInflationZeroCouponInterpolationDefinition(CUR, PAYMENT_DATE, START_DATE, ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX_EUR, MONTH_LAG, REFERENCE_START_DATE,
        INDEX_MAY_2008 + 1.0, REFERENCE_END_DATE, FIXING_DATE, false);
    assertFalse(ZERO_COUPON_DEFINITION.equals(modified));
  }

  @Test
  /**
   * Tests the first builder.
   */
  public void from1() {
    CouponInflationZeroCouponInterpolationDefinition constructor = new CouponInflationZeroCouponInterpolationDefinition(CUR, PAYMENT_DATE, START_DATE, PAYMENT_DATE, 1.0, NOTIONAL, PRICE_INDEX_EUR,
        MONTH_LAG, START_DATE, INDEX_MAY_2008, REFERENCE_END_DATE, FIXING_DATE, false);
    CouponInflationZeroCouponInterpolationDefinition from = CouponInflationZeroCouponInterpolationDefinition.from(START_DATE, PAYMENT_DATE, NOTIONAL, PRICE_INDEX_EUR, MONTH_LAG, INDEX_MAY_2008,
        REFERENCE_END_DATE, FIXING_DATE, false);
    assertEquals("Inflation zero-coupon : from", constructor, from);
  }

  @Test
  /**
   * Tests the builder based on indexation lag.
   */
  public void from2() {
    CouponInflationZeroCouponInterpolationDefinition constructor = new CouponInflationZeroCouponInterpolationDefinition(CUR, PAYMENT_DATE, START_DATE, PAYMENT_DATE, 1.0, NOTIONAL, PRICE_INDEX_EUR,
        MONTH_LAG, REFERENCE_START_DATE, INDEX_MAY_2008, REFERENCE_END_DATE, FIXING_DATE, false);
    CouponInflationZeroCouponInterpolationDefinition from = CouponInflationZeroCouponInterpolationDefinition
        .from(START_DATE, PAYMENT_DATE, NOTIONAL, PRICE_INDEX_EUR, INDEX_MAY_2008, MONTH_LAG, false);
    assertEquals("Inflation zero-coupon : from", constructor, from);
  }

  @Test
  /**
   * Tests the builder based on price index series.
   */
  public void from3() {
    CouponInflationZeroCouponInterpolationDefinition constructor = new CouponInflationZeroCouponInterpolationDefinition(CUR, PAYMENT_DATE, START_DATE, PAYMENT_DATE, 1.0, NOTIONAL, PRICE_INDEX_EUR,
        MONTH_LAG, REFERENCE_START_DATE, INDEX_MAY_2008, REFERENCE_END_DATE, FIXING_DATE, false);
    CouponInflationZeroCouponInterpolationDefinition from = CouponInflationZeroCouponInterpolationDefinition.from(START_DATE, PAYMENT_DATE, NOTIONAL, PRICE_INDEX_EUR, HICPX_TS, MONTH_LAG, false);
    assertEquals("Inflation zero-coupon : from", constructor, from);
  }

  @Test
  public void toDerivativesNoData() {
    final ZonedDateTime pricingDate = DateUtils.getUTCDate(2011, 7, 29);
    Coupon zeroCouponConverted = ZERO_COUPON_DEFINITION.toDerivative(pricingDate, CURVE_NAMES);
    double paymentTime = TimeCalculator.getTimeBetween(pricingDate, PAYMENT_DATE);
    final double[] referenceEndTime = new double[2];
    referenceEndTime[0] = TimeCalculator.getTimeBetween(pricingDate, REFERENCE_END_DATE[0]);
    referenceEndTime[1] = TimeCalculator.getTimeBetween(pricingDate, REFERENCE_END_DATE[1]);
    final double fixingTime = TimeCalculator.getTimeBetween(pricingDate, FIXING_DATE);
    CouponInflationZeroCouponInterpolation zeroCoupon = new CouponInflationZeroCouponInterpolation(CUR, paymentTime, DISCOUNTING_CURVE_NAME, 1.0, NOTIONAL, PRICE_INDEX_EUR, INDEX_MAY_2008,
        referenceEndTime, ZERO_COUPON_DEFINITION.getWeight(), fixingTime, false);
    assertEquals("Inflation zero-coupon: toDerivative", zeroCouponConverted, zeroCoupon);
  }

  @Test
  public void toDerivativesInterpolatedKnown() {
    final ZonedDateTime pricingDate = DateUtils.getUTCDate(2018, 7, 25);
    final DoubleTimeSeries<ZonedDateTime> priceIndexTS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2018, 5, 1), DateUtils.getUTCDate(2018, 6, 1)}, new double[] {
        128.23, 128.43});
    CouponInflationZeroCouponInterpolationDefinition zeroCouponInterpolated = CouponInflationZeroCouponInterpolationDefinition.from(START_DATE, PAYMENT_DATE, NOTIONAL, PRICE_INDEX_EUR,
        INDEX_MAY_2008, MONTH_LAG, false);
    Coupon zeroCouponConverted = zeroCouponInterpolated.toDerivative(pricingDate, priceIndexTS, CURVE_NAMES);
    double paymentTime = TimeCalculator.getTimeBetween(pricingDate, PAYMENT_DATE);
    double endIndex = 128.23 + (PAYMENT_DATE.getDayOfMonth() - 1.0) / (PAYMENT_DATE.getMonthOfYear().getLastDayOfMonth(PAYMENT_DATE.isLeapYear())) * (128.43 - 128.23);
    CouponFixed zeroCoupon = new CouponFixed(CUR, paymentTime, DISCOUNTING_CURVE_NAME, 1.0, NOTIONAL, endIndex / INDEX_MAY_2008 - 1.0);
    assertEquals("Inflation zero-coupon: toDerivative", zeroCoupon, zeroCouponConverted);
  }

  @Test
  public void toDerivativesInterpolatedOneKnown() {
    final ZonedDateTime pricingDate = DateUtils.getUTCDate(2018, 6, 25);
    final DoubleTimeSeries<ZonedDateTime> priceIndexTS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2018, 4, 1), DateUtils.getUTCDate(2018, 5, 1)}, new double[] {
        128.03, 128.23});
    CouponInflationZeroCouponInterpolationDefinition zeroCouponInterpolated = CouponInflationZeroCouponInterpolationDefinition.from(START_DATE, PAYMENT_DATE, NOTIONAL, PRICE_INDEX_EUR,
        INDEX_MAY_2008, MONTH_LAG, false);
    Coupon zeroCouponConverted = zeroCouponInterpolated.toDerivative(pricingDate, priceIndexTS, CURVE_NAMES);
    double paymentTime = TimeCalculator.getTimeBetween(pricingDate, PAYMENT_DATE);
    final double[] referenceEndTime = new double[2];
    referenceEndTime[0] = -TimeCalculator.getTimeBetween(REFERENCE_END_DATE[0], pricingDate);
    referenceEndTime[1] = -TimeCalculator.getTimeBetween(REFERENCE_END_DATE[1], pricingDate);
    final double fixingTime = TimeCalculator.getTimeBetween(pricingDate, zeroCouponInterpolated.getFixingEndDate());
    CouponInflationZeroCouponInterpolation zeroCoupon = new CouponInflationZeroCouponInterpolation(CUR, paymentTime, DISCOUNTING_CURVE_NAME, 1.0, NOTIONAL, PRICE_INDEX_EUR, INDEX_MAY_2008,
        referenceEndTime, ZERO_COUPON_DEFINITION.getWeight(), fixingTime, false);
    assertEquals("Inflation zero-coupon: toDerivative", zeroCoupon, zeroCouponConverted);
  }

  @Test
  public void toDerivativesInterpolatedShouldBeKnown() {
    final ZonedDateTime pricingDate = DateUtils.getUTCDate(2018, 7, 25);
    final DoubleTimeSeries<ZonedDateTime> priceIndexTS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2018, 4, 1), DateUtils.getUTCDate(2018, 5, 1)}, new double[] {
        128.03, 128.23});
    CouponInflationZeroCouponInterpolationDefinition zeroCouponInterpolated = CouponInflationZeroCouponInterpolationDefinition.from(START_DATE, PAYMENT_DATE, NOTIONAL, PRICE_INDEX_EUR,
        INDEX_MAY_2008, MONTH_LAG, false);
    Coupon zeroCouponConverted = zeroCouponInterpolated.toDerivative(pricingDate, priceIndexTS, CURVE_NAMES);
    double paymentTime = TimeCalculator.getTimeBetween(pricingDate, PAYMENT_DATE);
    final double[] referenceEndTime = new double[2];
    referenceEndTime[0] = -TimeCalculator.getTimeBetween(REFERENCE_END_DATE[0], pricingDate);
    referenceEndTime[1] = -TimeCalculator.getTimeBetween(REFERENCE_END_DATE[1], pricingDate);
    final double fixingTime = 0.0;
    CouponInflationZeroCouponInterpolation zeroCoupon = new CouponInflationZeroCouponInterpolation(CUR, paymentTime, DISCOUNTING_CURVE_NAME, 1.0, NOTIONAL, PRICE_INDEX_EUR, INDEX_MAY_2008,
        referenceEndTime, ZERO_COUPON_DEFINITION.getWeight(), fixingTime, false);
    assertEquals("Inflation zero-coupon: toDerivative", zeroCoupon, zeroCouponConverted);
  }

  @Test
  /**
   * Tests the construction of zero-coupon inflation swaps.
   */
  public void swapFixedInflationZeroCouponConstructor() {
    double zeroCpnRate = 0.02;
    //    ZonedDateTime paymentDate = ScheduleCalculator.getAdjustedDate(START_DATE, BUSINESS_DAY, CALENDAR, EOM, COUPON_TENOR);
    CouponInflationZeroCouponInterpolationDefinition inflationCpn = new CouponInflationZeroCouponInterpolationDefinition(CUR, PAYMENT_DATE, START_DATE, PAYMENT_DATE, 1.0, -NOTIONAL, PRICE_INDEX_EUR,
        MONTH_LAG, REFERENCE_START_DATE, INDEX_MAY_2008, REFERENCE_END_DATE, FIXING_DATE, false);
    double compoundedRate = Math.pow(1 + zeroCpnRate, COUPON_TENOR_YEAR);
    CouponFixedDefinition fixedCpn = CouponFixedDefinition.from(CUR, PAYMENT_DATE, START_DATE, PAYMENT_DATE, 1.0, NOTIONAL, compoundedRate);
    SwapFixedInflationZeroCouponDefinition swap = new SwapFixedInflationZeroCouponDefinition(fixedCpn, inflationCpn);
    assertTrue("Swap zero-coupon inflation constructor", swap.getFirstLeg().equals(new AnnuityCouponFixedDefinition(new CouponFixedDefinition[] {fixedCpn})));
    assertTrue("Swap zero-coupon inflation constructor", swap.getSecondLeg().equals(new AnnuityDefinition<PaymentDefinition>(new PaymentDefinition[] {inflationCpn})));
  }

  @Test
  /**
   * Tests the construction of zero-coupon inflation swaps.
   */
  public void swapFixedInflationZeroCouponFrom() {
    double zeroCpnRate = 0.02;
    CouponInflationZeroCouponInterpolationDefinition inflationCpn = new CouponInflationZeroCouponInterpolationDefinition(CUR, PAYMENT_DATE, START_DATE, PAYMENT_DATE, 1.0, NOTIONAL, PRICE_INDEX_EUR,
        MONTH_LAG, REFERENCE_START_DATE, INDEX_MAY_2008, REFERENCE_END_DATE, FIXING_DATE, false);
    double compoundedRate = Math.pow(1 + zeroCpnRate, COUPON_TENOR_YEAR) - 1;
    CouponFixedDefinition fixedCpn = CouponFixedDefinition.from(CUR, PAYMENT_DATE, START_DATE, PAYMENT_DATE, 1.0, -NOTIONAL, compoundedRate);
    SwapFixedInflationZeroCouponDefinition swap = new SwapFixedInflationZeroCouponDefinition(fixedCpn, inflationCpn);
    SwapFixedInflationZeroCouponDefinition swapFrom = SwapFixedInflationZeroCouponDefinition.fromInterpolation(PRICE_INDEX_EUR, START_DATE, COUPON_TENOR_YEAR, zeroCpnRate, NOTIONAL, true,
        BUSINESS_DAY, CALENDAR, EOM, MONTH_LAG, HICPX_TS);
    assertEquals("Swap zero-coupon inflation constructor", swap, swapFrom);
  }

}
