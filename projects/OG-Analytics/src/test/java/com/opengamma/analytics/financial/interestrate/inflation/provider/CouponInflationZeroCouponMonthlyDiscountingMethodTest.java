/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.inflation.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedInflationMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedInflationZeroCoupon;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedInflationZeroCouponDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthly;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.provider.calculator.inflation.NetAmountInflationCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.ParSpreadInflationMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.ParSpreadInflationMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueCurveSensitivityDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.ParameterInflationProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.InflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.MultipleCurrencyInflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.ParameterSensitivityInflationMulticurveDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.ParameterSensitivityInflationParameterCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

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
      new ZonedDateTime[] {DateUtils.getUTCDate(2008, 5, 31), DateUtils.getUTCDate(2011, 5, 31), DateUtils.getUTCDate(2018, 5, 31), DateUtils.getUTCDate(2018, 6, 30) }, new double[] {108.23, 115.0,
        128.23, 128.43 });

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
  private static final ParameterSensitivityInflationParameterCalculator<ParameterInflationProviderInterface> PSC = new ParameterSensitivityInflationParameterCalculator<>(PVCSDC);
  private static final ParameterSensitivityInflationMulticurveDiscountInterpolatedFDCalculator PS_PV_FDC = new ParameterSensitivityInflationMulticurveDiscountInterpolatedFDCalculator(PVIC, SHIFT_FD);
  // Calculator and swap generator for the test of the parspread
  private static final ParSpreadInflationMarketQuoteDiscountingCalculator PSIMQDC = ParSpreadInflationMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadInflationMarketQuoteCurveSensitivityDiscountingCalculator PSIMQSDC = ParSpreadInflationMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();
  private static final GeneratorSwapFixedInflationZeroCoupon GENERATOR_INFLATION_SWAP = GeneratorSwapFixedInflationMaster.getInstance().getGenerator("EURHICP");
  private static final double MARKET_QUOTE = 0.017381814641219;
  private static final GeneratorAttributeIR GENERATOR = new GeneratorAttributeIR(Period.ofYears(10));
  private static final SwapFixedInflationZeroCouponDefinition SWAP_DEFINITION = GENERATOR_INFLATION_SWAP.generateInstrument(PRICING_DATE, MARKET_QUOTE, NOTIONAL,
      GENERATOR);
  private static final InstrumentDerivative SWAP_DERIVATIVE = SWAP_DEFINITION.toDerivative(PRICING_DATE, new ZonedDateTimeDoubleTimeSeries[] {(ZonedDateTimeDoubleTimeSeries) priceIndexTS,
    (ZonedDateTimeDoubleTimeSeries) priceIndexTS });

  /**
   * Tests the present value.
   */
  @Test
  public void parSpreadOnASwap() {
    final double parSpread = SWAP_DERIVATIVE.accept(PSIMQDC, MARKET.getInflationProvider());
    final Swap<?, ?> swap = (Swap<?, ?>) SWAP_DERIVATIVE;
    final double estimatedPriceIndex = MARKET.getInflationProvider().getPriceIndex(PRICE_INDEX_EUR, ((CouponInflationZeroCouponMonthly) swap.getSecondLeg().getNthPayment(0)).getReferenceEndTime());
    final double indexStartValue = ((CouponInflationZeroCouponMonthly) swap.getSecondLeg().getNthPayment(0)).getIndexStartValue();
    final Double parSpreadCalculated = Math.pow(estimatedPriceIndex / indexStartValue, 1.0 / 10.0) - 1 - MARKET_QUOTE;
    assertEquals("Zero-coupon inflation DiscountingMethod: Present value", parSpread, parSpreadCalculated, 10e-8);
  }

  @Test
  public void parSpreadSensitivityOnASwap() {
    final InflationSensitivity parSpreadSensitivity = SWAP_DERIVATIVE.accept(PSIMQSDC, MARKET.getInflationProvider());
    final CouponInflationZeroCouponMonthly secondLeg = (CouponInflationZeroCouponMonthly) (((Swap<?, ?>) SWAP_DERIVATIVE).getSecondLeg().getNthPayment(0));
    final double estimatedPriceIndex = MARKET.getInflationProvider().getPriceIndex(PRICE_INDEX_EUR, secondLeg.getReferenceEndTime());
    final double indexStartvalue = secondLeg.getIndexStartValue();
    final HashMap<String, List<DoublesPair>> sensitivityPriceCurve = new HashMap<>();
    final DoublesPair[] sensi = {DoublesPair.of(secondLeg.getReferenceEndTime(), 1 / indexStartvalue * 1.0 / 10.0 * Math.pow(estimatedPriceIndex / indexStartvalue, 1.0 / 10.0 - 1.0)) };
    final List<DoublesPair> sensiList = Arrays.asList(sensi);
    sensitivityPriceCurve.put(PRICE_INDEX_EUR.getName(), sensiList);
    final InflationSensitivity parSpreadSensitivityCalculated = InflationSensitivity.ofPriceIndex(sensitivityPriceCurve);
    AssertSensitivityObjects.assertEquals("Zero-coupon inflation DiscountingMethod: presentValueCurveSensitivity ", parSpreadSensitivity, parSpreadSensitivityCalculated, 10e-8);
  }

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

    AssertSensitivityObjects.assertEquals("Zero-coupon inflation DiscountingMethod: presentValueCurveSensitivity ", pvicsExact, pvicsFD, TOLERANCE_PV_DELTA);

  }

  /**
   * Test the present value curves sensitivity.
   */
  @Test
  public void presentValueCurveSensitivityNoNotional() {
    final MultipleCurrencyParameterSensitivity pvicsFD = PS_PV_FDC.calculateSensitivity(ZERO_COUPON_NO, MARKET.getInflationProvider());
    final MultipleCurrencyParameterSensitivity pvicsExact = PSC.calculateSensitivity(ZERO_COUPON_NO, MARKET.getInflationProvider());
    AssertSensitivityObjects.assertEquals("Zero-coupon inflation DiscountingMethod: presentValueCurveSensitivity ", pvicsExact, pvicsFD, TOLERANCE_PV_DELTA);
  }

  @Test
  public void presentValueMarketSensitivityMethodVsCalculatorNoNotional() {
    final MultipleCurrencyInflationSensitivity pvcisMethod = METHOD.presentValueCurveSensitivity(ZERO_COUPON_NO, MARKET.getInflationProvider());
    final MultipleCurrencyInflationSensitivity pvcisCalculator = ZERO_COUPON_NO.accept(PVCSDC, MARKET.getInflationProvider());
    AssertSensitivityObjects.assertEquals("Zero-coupon inflation DiscountingMethod: presentValueMarketSensitivity", pvcisMethod, pvcisCalculator, TOLERANCE_PV_DELTA);
  }

  @Test
  public void presentValueMarketSensitivityMethodVsCalculatorWithNotional() {
    final MultipleCurrencyInflationSensitivity pvcisMethod = METHOD.presentValueCurveSensitivity(ZERO_COUPON_WITH, MARKET.getInflationProvider());
    final MultipleCurrencyInflationSensitivity pvcisCalculator = ZERO_COUPON_WITH.accept(PVCSDC, MARKET.getInflationProvider());
    AssertSensitivityObjects.assertEquals("Zero-coupon inflation DiscountingMethod: presentValueMarketSensitivity", pvcisMethod, pvcisCalculator, TOLERANCE_PV_DELTA);
  }

}
