/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.inflation.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyDefinition;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthly;
import com.opengamma.analytics.financial.provider.calculator.inflation.NetAmountInflationCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueCurveSensitivityDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.MultipleCurrencyInflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.ParameterSensitivityInflationMulticurveDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterInflationSensitivityParameterCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests the present value and its sensitivities for zero-coupon with reference index on the first of the month.
 */
@Test(groups = TestGroup.UNIT)
public class CouponInflationZeroCouponMonthlyDiscountingMethodTest {
  private static final InflationIssuerProviderDiscount MARKET = MulticurveProviderDiscountDataSets.createMarket1();
  private static final IndexPrice[] PRICE_INDEXES = MARKET.getPriceIndexes().toArray(new IndexPrice[MARKET.getPriceIndexes().size()]);
  private static final IndexPrice PRICE_INDEX_EUR = PRICE_INDEXES[0];
  private static final Calendar CALENDAR_EUR = MulticurveProviderDiscountDataSets.getEURCalendar();
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final Period COUPON_TENOR = Period.ofYears(10);
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(START_DATE, COUPON_TENOR, BUSINESS_DAY, CALENDAR_EUR);
  private static final double NOTIONAL = 98765432;
  private static final int MONTH_LAG = 3;
  private static final double INDEX_1MAY_2008 = 108.23; // 3 m before Aug: May / 1 May index = May index: 108.23
  private static final ZonedDateTime PRICING_DATE = DateUtils.getUTCDate(2011, 8, 3);
  private static final CouponInflationZeroCouponMonthlyDefinition ZERO_COUPON_NO_DEFINITION = CouponInflationZeroCouponMonthlyDefinition.from(START_DATE, PAYMENT_DATE, NOTIONAL, PRICE_INDEX_EUR,
      MONTH_LAG, MONTH_LAG, false);
  private static final DoubleTimeSeries<ZonedDateTime> priceIndexTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {DateUtils.getUTCDate(2008, 5, 31), DateUtils.getUTCDate(2018, 5, 31), DateUtils.getUTCDate(2018, 6, 30) }, new double[] {108.23, 128.23, 128.43 });
  private static final CouponInflationZeroCouponMonthly ZERO_COUPON_NO = (CouponInflationZeroCouponMonthly) ZERO_COUPON_NO_DEFINITION.toDerivative(PRICING_DATE, priceIndexTS);
  private static final CouponInflationZeroCouponMonthlyDefinition ZERO_COUPON_WITH_DEFINITION = CouponInflationZeroCouponMonthlyDefinition.from(START_DATE, PAYMENT_DATE, NOTIONAL, PRICE_INDEX_EUR,
      MONTH_LAG, MONTH_LAG, true);
  private static final CouponInflationZeroCouponMonthly ZERO_COUPON_WITH = (CouponInflationZeroCouponMonthly) ZERO_COUPON_WITH_DEFINITION.toDerivative(PRICING_DATE, priceIndexTS);

  private static final double SHIFT_FD = 1.0E-7;
  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2;

  private static final CouponInflationZeroCouponMonthlyDiscountingMethod METHOD = new CouponInflationZeroCouponMonthlyDiscountingMethod();
  private static final PresentValueDiscountingInflationCalculator PVIC = PresentValueDiscountingInflationCalculator.getInstance();
  private static final NetAmountInflationCalculator NAIC = NetAmountInflationCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingInflationCalculator PVCSDC = PresentValueCurveSensitivityDiscountingInflationCalculator.getInstance();
  private static final ParameterInflationSensitivityParameterCalculator<InflationProviderInterface> PSC = new ParameterInflationSensitivityParameterCalculator<>(PVCSDC);
  private static final ParameterSensitivityInflationMulticurveDiscountInterpolatedFDCalculator PS_PV_FDC = new ParameterSensitivityInflationMulticurveDiscountInterpolatedFDCalculator(PVIC, SHIFT_FD);

  /**
   * Tests the present value.
   */
  @Test
  public void presentValueNoNotional() {
    final MultipleCurrencyAmount pv = METHOD.presentValue(ZERO_COUPON_NO, MARKET.getInflationProvider());
    final double df = MARKET.getCurve(ZERO_COUPON_NO.getCurrency()).getDiscountFactor(ZERO_COUPON_NO.getPaymentTime());
    final double finalIndex = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(ZERO_COUPON_NO.getReferenceEndTime());
    final double pvExpected = (finalIndex / INDEX_1MAY_2008 - 1) * df * NOTIONAL;
    assertEquals("Zero-coupon inflation DiscountingMethod: Present value", pvExpected, pv.getAmount(ZERO_COUPON_NO.getCurrency()), TOLERANCE_PV);
  }

  /**
   * Tests the net amount.
   */
  @Test
  public void netAmountNoNotional() {
    final MultipleCurrencyAmount pv = METHOD.netAmount(ZERO_COUPON_NO, MARKET.getInflationProvider());
    final double finalIndex = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(ZERO_COUPON_NO.getReferenceEndTime());
    final double naExpected = (finalIndex / INDEX_1MAY_2008 - 1) * NOTIONAL;
    assertEquals("Zero-coupon inflation DiscountingMethod: net amount", naExpected, pv.getAmount(ZERO_COUPON_NO.getCurrency()), TOLERANCE_PV);
  }

  /**
   * Tests the present value: Method vs Calculator.
   */
  @Test
  public void presentValueMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD.presentValue(ZERO_COUPON_NO, MARKET.getInflationProvider());
    final MultipleCurrencyAmount pvCalculator = ZERO_COUPON_NO.accept(PVIC, MARKET.getInflationProvider());
    assertEquals("Zero-coupon inflation DiscountingMethod: Present value", pvMethod, pvCalculator);
  }

  /**
   * Tests the present value.
   */
  @Test
  public void presentValueWithNotional() {
    final MultipleCurrencyAmount pv = METHOD.presentValue(ZERO_COUPON_WITH, MARKET.getInflationProvider());
    final double df = MARKET.getCurve(ZERO_COUPON_WITH.getCurrency()).getDiscountFactor(ZERO_COUPON_WITH.getPaymentTime());
    final double finalIndex = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(ZERO_COUPON_WITH.getReferenceEndTime());
    final double pvExpected = (finalIndex / INDEX_1MAY_2008) * df * NOTIONAL;
    assertEquals("Zero-coupon inflation DiscountingMethod: Present value", pvExpected, pv.getAmount(ZERO_COUPON_WITH.getCurrency()), TOLERANCE_PV);
  }

  /**
   * Tests the net amount.
   */
  @Test
  public void netAmountWithNotional() {
    final MultipleCurrencyAmount pv = METHOD.netAmount(ZERO_COUPON_WITH, MARKET.getInflationProvider());
    final double finalIndex = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(ZERO_COUPON_WITH.getReferenceEndTime());
    final double pvExpected = (finalIndex / INDEX_1MAY_2008) * NOTIONAL;
    assertEquals("Zero-coupon inflation DiscountingMethod: net amount", pvExpected, pv.getAmount(ZERO_COUPON_WITH.getCurrency()), TOLERANCE_PV);
  }

  /**
   * Tests the net amount: Method vs Calculator.
   */
  @Test
  public void netAmountMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD.netAmount(ZERO_COUPON_NO, MARKET.getInflationProvider());
    final MultipleCurrencyAmount pvCalculator = ZERO_COUPON_NO.accept(NAIC, MARKET.getInflationProvider());
    assertEquals("Zero-coupon inflation DiscountingMethod: Net amount", pvMethod, pvCalculator);
  }

  /**
   * Test the present value curves sensitivity.
   */
  @Test
  public void presentValueCurveSensitivityWithNotional() {

    final MultipleCurrencyParameterSensitivity pvicsFD = PS_PV_FDC.calculateSensitivity(ZERO_COUPON_WITH, MARKET.getInflationProvider());
    final MultipleCurrencyParameterSensitivity pvicsExact = PSC.calculateSensitivity(ZERO_COUPON_WITH, MARKET.getInflationProvider());

    AssertSensivityObjects.assertEquals("Zero-coupon inflation DiscountingMethod: presentValueCurveSensitivity ", pvicsExact, pvicsFD, TOLERANCE_PV_DELTA);

  }

  /**
   * Test the present value curves sensitivity.
   */
  @Test
  public void presentValueCurveSensitivityNoNotional() {
    final MultipleCurrencyParameterSensitivity pvicsFD = PS_PV_FDC.calculateSensitivity(ZERO_COUPON_NO, MARKET.getInflationProvider());
    final MultipleCurrencyParameterSensitivity pvicsExact = PSC.calculateSensitivity(ZERO_COUPON_NO, MARKET.getInflationProvider());
    AssertSensivityObjects.assertEquals("Zero-coupon inflation DiscountingMethod: presentValueCurveSensitivity ", pvicsExact, pvicsFD, TOLERANCE_PV_DELTA);
  }

  @Test
  public void presentValueMarketSensitivityMethodVsCalculatorNoNotional() {
    final MultipleCurrencyInflationSensitivity pvcisMethod = METHOD.presentValueCurveSensitivity(ZERO_COUPON_NO, MARKET.getInflationProvider());
    final MultipleCurrencyInflationSensitivity pvcisCalculator = ZERO_COUPON_NO.accept(PVCSDC, MARKET.getInflationProvider());
    AssertSensivityObjects.assertEquals("Zero-coupon inflation DiscountingMethod: presentValueMarketSensitivity", pvcisMethod, pvcisCalculator, TOLERANCE_PV_DELTA);
  }

  @Test
  public void presentValueMarketSensitivityMethodVsCalculatorWithNotional() {
    final MultipleCurrencyInflationSensitivity pvcisMethod = METHOD.presentValueCurveSensitivity(ZERO_COUPON_WITH, MARKET.getInflationProvider());
    final MultipleCurrencyInflationSensitivity pvcisCalculator = ZERO_COUPON_WITH.accept(PVCSDC, MARKET.getInflationProvider());
    AssertSensivityObjects.assertEquals("Zero-coupon inflation DiscountingMethod: presentValueMarketSensitivity", pvcisMethod, pvcisCalculator, TOLERANCE_PV_DELTA);
  }

}
