/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.swap;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedInflationZeroCoupon;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 *  Class testing the Fixed vs Index price zero coupon swap definition.
 */
@Test(groups = TestGroup.UNIT)
public class SwapFixedInflationZeroCouponDefinitionTest {

  private static final IndexPrice[] PRICE_INDEXES = MulticurveProviderDiscountDataSets.getPriceIndexes();
  private static final IndexPrice PRICE_INDEX_EUR = PRICE_INDEXES[0];
  private static final Currency CUR = PRICE_INDEX_EUR.getCurrency();
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean EOM = true;
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final int COUPON_TENOR_YEAR = 10;
  private static final Period COUPON_TENOR = Period.ofYears(COUPON_TENOR_YEAR);
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(START_DATE, COUPON_TENOR, BUSINESS_DAY, CALENDAR, EOM);
  private static final double NOTIONAL = 98765432;
  private static final int MONTH_LAG = 3;
  private static final int SPOT_LAG = 2;
  //  private static final double INDEX_MAY_2008_INTERPOLATED = 108.45483870967742; // May index: 108.23 - June Index = 108.64
  //  private static final double INDEX_MAY_2008 = 108.23;
  private static final ZonedDateTime REFERENCE_START_DATE = DateUtils.getUTCDate(2008, 5, 18);
  private static final ZonedDateTime REFERENCE_START_DATE_MONTHLY = DateUtils.getUTCDate(2008, 5, 31);
  private static final ZonedDateTime[] REFERENCE_START_DATES = new ZonedDateTime[2];
  static {
    REFERENCE_START_DATES[0] = REFERENCE_START_DATE.with(TemporalAdjusters.lastDayOfMonth());
    REFERENCE_START_DATES[1] = REFERENCE_START_DATE.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
  }
  private static final ZonedDateTime[] REFERENCE_END_DATES = new ZonedDateTime[2];
  static {
    REFERENCE_END_DATES[0] = PAYMENT_DATE.minusMonths(MONTH_LAG).with(TemporalAdjusters.lastDayOfMonth());
    REFERENCE_END_DATES[1] = PAYMENT_DATE.minusMonths(MONTH_LAG - 1).with(TemporalAdjusters.lastDayOfMonth());
  }

  //  private static final DoubleTimeSeries<ZonedDateTime> HICPX_TS = MulticurveProviderDiscountDataSets.euroHICPXFrom2009();

  private static final GeneratorSwapFixedInflationZeroCoupon GENERATOR_SWAP_INFLATION = new GeneratorSwapFixedInflationZeroCoupon("generator", PRICE_INDEX_EUR, BUSINESS_DAY, CALENDAR, EOM, MONTH_LAG,
      SPOT_LAG,
      true);

  @Test
  /**
   * Tests the construction of zero-coupon inflation swaps.
   */
  public void swapFixedInflationZeroCouponInterpolationConstructor() {
    final double zeroCpnRate = 0.02;
    //    ZonedDateTime paymentDate = ScheduleCalculator.getAdjustedDate(START_DATE, BUSINESS_DAY, CALENDAR, EOM, COUPON_TENOR);
    final CouponInflationZeroCouponInterpolationDefinition inflationCpn = CouponInflationZeroCouponInterpolationDefinition.from(CUR, PAYMENT_DATE, START_DATE, PAYMENT_DATE, 1.0, -NOTIONAL,
        PRICE_INDEX_EUR, MONTH_LAG, REFERENCE_START_DATES, REFERENCE_END_DATES, false);
    final CouponFixedCompoundingDefinition fixedCpn = CouponFixedCompoundingDefinition.from(CUR, START_DATE, PAYMENT_DATE, NOTIONAL, COUPON_TENOR_YEAR, zeroCpnRate);
    final SwapFixedInflationZeroCouponDefinition swap = new SwapFixedInflationZeroCouponDefinition(fixedCpn, inflationCpn, CALENDAR);
    assertTrue("Swap zero-coupon inflation constructor", swap.getFirstLeg().equals(new AnnuityDefinition<>(new PaymentDefinition[] {fixedCpn }, CALENDAR)));
    assertTrue("Swap zero-coupon inflation constructor", swap.getSecondLeg().equals(new AnnuityDefinition<>(new PaymentDefinition[] {inflationCpn }, CALENDAR)));
  }

  @Test
  /**
   * Tests the construction of zero-coupon inflation swaps.
   */
  public void couponFixedInflationZeroCouponInterpolationFrom() {
    final double zeroCpnRate = 0.02;
    final CouponInflationZeroCouponInterpolationDefinition inflationCpn = CouponInflationZeroCouponInterpolationDefinition.from(CUR, PAYMENT_DATE, START_DATE, PAYMENT_DATE, 1.0, NOTIONAL,
        PRICE_INDEX_EUR, MONTH_LAG, REFERENCE_START_DATES, REFERENCE_END_DATES, false);
    final CouponFixedCompoundingDefinition fixedCpn = CouponFixedCompoundingDefinition.from(CUR, START_DATE, PAYMENT_DATE, -NOTIONAL, COUPON_TENOR_YEAR, zeroCpnRate);
    final SwapFixedInflationZeroCouponDefinition swap = new SwapFixedInflationZeroCouponDefinition(fixedCpn, inflationCpn, CALENDAR);
    final SwapFixedInflationZeroCouponDefinition swapFrom = SwapFixedInflationZeroCouponDefinition.fromInterpolation(PRICE_INDEX_EUR, START_DATE, COUPON_TENOR_YEAR, zeroCpnRate, NOTIONAL,
        true, BUSINESS_DAY, CALENDAR, EOM, MONTH_LAG, MONTH_LAG);
    assertEquals("Swap zero-coupon inflation constructor", swap, swapFrom);
  }

  @Test
  /**
   * Tests the construction of zero-coupon inflation swaps.
   */
  public void couponFixedInflationZeroCouponInterpolationWithGenerator() {
    final double zeroCpnRate = 0.02;
    final CouponInflationZeroCouponInterpolationDefinition inflationCpn = CouponInflationZeroCouponInterpolationDefinition.from(CUR, PAYMENT_DATE, START_DATE, PAYMENT_DATE, 1.0, NOTIONAL,
        PRICE_INDEX_EUR, MONTH_LAG, REFERENCE_START_DATES, REFERENCE_END_DATES, false);
    final CouponFixedCompoundingDefinition fixedCpn = CouponFixedCompoundingDefinition.from(CUR, START_DATE, PAYMENT_DATE, -NOTIONAL, COUPON_TENOR_YEAR, zeroCpnRate);
    final SwapFixedInflationZeroCouponDefinition swap = new SwapFixedInflationZeroCouponDefinition(fixedCpn, inflationCpn, CALENDAR);
    final SwapFixedInflationZeroCouponDefinition swapFrom = SwapFixedInflationZeroCouponDefinition.fromGeneratorInterpolation(START_DATE, zeroCpnRate, NOTIONAL, COUPON_TENOR,
        GENERATOR_SWAP_INFLATION,
        true);
    assertEquals("Swap zero-coupon inflation constructor", swap, swapFrom);
  }

  @Test
  /**
   * Tests the construction of zero-coupon inflation swaps.
   */
  public void swapFixedInflationZeroCouponMonthlyConstructor() {
    final double zeroCpnRate = 0.02;
    //    ZonedDateTime paymentDate = ScheduleCalculator.getAdjustedDate(START_DATE, BUSINESS_DAY, CALENDAR, EOM, COUPON_TENOR);
    final CouponInflationZeroCouponMonthlyDefinition inflationCpn = new CouponInflationZeroCouponMonthlyDefinition(CUR, PAYMENT_DATE, START_DATE, PAYMENT_DATE, 1.0, -NOTIONAL, PRICE_INDEX_EUR,
        MONTH_LAG, MONTH_LAG, REFERENCE_START_DATE, REFERENCE_END_DATES[0], false);
    final CouponFixedCompoundingDefinition fixedCpn = CouponFixedCompoundingDefinition.from(CUR, START_DATE, PAYMENT_DATE, NOTIONAL, COUPON_TENOR_YEAR, zeroCpnRate);
    final SwapFixedInflationZeroCouponDefinition swap = new SwapFixedInflationZeroCouponDefinition(fixedCpn, inflationCpn, CALENDAR);
    assertTrue("Swap zero-coupon inflation constructor", swap.getFirstLeg().equals(new AnnuityDefinition<>(new PaymentDefinition[] {fixedCpn }, CALENDAR)));
    assertTrue("Swap zero-coupon inflation constructor", swap.getSecondLeg().equals(new AnnuityDefinition<>(new PaymentDefinition[] {inflationCpn }, CALENDAR)));
  }

  @Test
  /**
   * Tests the construction of zero-coupon inflation swaps.
   */
  public void couponFixedInflationZeroCouponMonthlyFrom() {
    final double zeroCpnRate = 0.02;
    final CouponInflationZeroCouponMonthlyDefinition inflationCpn = new CouponInflationZeroCouponMonthlyDefinition(CUR, PAYMENT_DATE, START_DATE, PAYMENT_DATE, 1.0, NOTIONAL, PRICE_INDEX_EUR,
        MONTH_LAG, MONTH_LAG, REFERENCE_START_DATE_MONTHLY, REFERENCE_END_DATES[0], false);
    final CouponFixedCompoundingDefinition fixedCpn = CouponFixedCompoundingDefinition.from(CUR, START_DATE, PAYMENT_DATE, -NOTIONAL, COUPON_TENOR_YEAR, zeroCpnRate);
    final SwapFixedInflationZeroCouponDefinition swap = new SwapFixedInflationZeroCouponDefinition(fixedCpn, inflationCpn, CALENDAR);
    final SwapFixedInflationZeroCouponDefinition swapFrom = SwapFixedInflationZeroCouponDefinition.fromMonthly(PRICE_INDEX_EUR, START_DATE, COUPON_TENOR_YEAR, zeroCpnRate, NOTIONAL, true,
        BUSINESS_DAY, CALENDAR, EOM, MONTH_LAG, MONTH_LAG);
    assertEquals("Swap zero-coupon inflation constructor", swap, swapFrom);
  }

  @Test
  /**
   * Tests the construction of zero-coupon inflation swaps.
   */
  public void couponFixedInflationZeroCouponMonthlyWithGenerator() {
    final double zeroCpnRate = 0.02;
    final CouponInflationZeroCouponMonthlyDefinition inflationCpn = new CouponInflationZeroCouponMonthlyDefinition(CUR, PAYMENT_DATE, START_DATE, PAYMENT_DATE, 1.0, NOTIONAL, PRICE_INDEX_EUR,
        MONTH_LAG, MONTH_LAG, REFERENCE_START_DATE_MONTHLY, REFERENCE_END_DATES[0], false);
    final CouponFixedCompoundingDefinition fixedCpn = CouponFixedCompoundingDefinition.from(CUR, START_DATE, PAYMENT_DATE, -NOTIONAL, COUPON_TENOR_YEAR, zeroCpnRate);
    final SwapFixedInflationZeroCouponDefinition swap = new SwapFixedInflationZeroCouponDefinition(fixedCpn, inflationCpn, CALENDAR);
    final SwapFixedInflationZeroCouponDefinition swapFrom = SwapFixedInflationZeroCouponDefinition
        .fromGeneratorMonthly(START_DATE, zeroCpnRate, NOTIONAL, COUPON_TENOR, GENERATOR_SWAP_INFLATION, true);
    assertEquals("Swap zero-coupon inflation constructor", swap, swapFrom);
  }

}
