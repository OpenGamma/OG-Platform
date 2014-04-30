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

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationDefinition;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponInterpolation;
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
 * Tests the present value and its sensitivities for zero-coupon with reference index interpolated between months.
 */
@Test(groups = TestGroup.UNIT)
public class CouponInflationZeroCouponInterpolationDiscountingMethodTest {

  private static final InflationIssuerProviderDiscount MARKET = MulticurveProviderDiscountDataSets.createMarket1();
  private static final IndexPrice[] PRICE_INDEXES = MulticurveProviderDiscountDataSets.getPriceIndexes();
  private static final IndexPrice PRICE_INDEX_EUR = PRICE_INDEXES[0];
  private static final IndexPrice PRICE_INDEX_US = PRICE_INDEXES[2];
  private static final IborIndex[] IBOR_INDEXES = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd();
  private static final IborIndex USDLIBOR3M = IBOR_INDEXES[2];
  private static final Calendar CALENDAR_EUR = MulticurveProviderDiscountDataSets.getEURCalendar();
  private static final Calendar CALENDAR_USD = MulticurveProviderDiscountDataSets.getUSDCalendar();
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final Period COUPON_TENOR = Period.ofYears(10);
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(START_DATE, COUPON_TENOR, BUSINESS_DAY, CALENDAR_EUR);
  private static final double NOTIONAL = 98765432;
  private static final int MONTH_LAG = 3;
  private static final double INDEX_MAY_2008_INT = 108.48129032258066; // May index: 108.23 - June Index = 108.64
  private static final double INDEX_MAY_2011_INT = 225.93277419354837;
  private static final double SHIFT_FD = 1.0E-7;
  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2;

  private static final DoubleTimeSeries<ZonedDateTime> priceIndexTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {DateUtils.getUTCDate(2008, 5, 31), DateUtils.getUTCDate(2008, 6, 30), DateUtils.getUTCDate(2011, 5, 31), DateUtils.getUTCDate(2011, 6, 30),
        DateUtils.getUTCDate(2018, 5, 31), DateUtils.getUTCDate(2018, 6, 30) }, new double[] {108.23, 108.64, 225.964, 225.722,
        128.23, 128.43 });

  private static final CouponInflationZeroCouponInterpolationDefinition ZERO_COUPON_1_DEFINITION = CouponInflationZeroCouponInterpolationDefinition.from(START_DATE, PAYMENT_DATE, NOTIONAL,
      PRICE_INDEX_EUR, MONTH_LAG, MONTH_LAG, false);
  private static final ZonedDateTime PRICING_DATE = DateUtils.getUTCDate(2011, 8, 3);
  private static final CouponInflationZeroCouponInterpolation ZERO_COUPON_1 = (CouponInflationZeroCouponInterpolation) ZERO_COUPON_1_DEFINITION.toDerivative(PRICING_DATE, priceIndexTS);
  private static final CouponInflationZeroCouponInterpolationDiscountingMethod METHOD = new CouponInflationZeroCouponInterpolationDiscountingMethod();
  private static final PresentValueDiscountingInflationCalculator PVIC = PresentValueDiscountingInflationCalculator.getInstance();
  private static final NetAmountInflationCalculator NAIC = NetAmountInflationCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingInflationCalculator PVCSDC = PresentValueCurveSensitivityDiscountingInflationCalculator.getInstance();
  private static final ParameterInflationSensitivityParameterCalculator<InflationProviderInterface> PSC = new ParameterInflationSensitivityParameterCalculator<>(PVCSDC);
  private static final ParameterSensitivityInflationMulticurveDiscountInterpolatedFDCalculator PS_PV_FDC = new ParameterSensitivityInflationMulticurveDiscountInterpolatedFDCalculator(PVIC, SHIFT_FD);

  /**
   * Tests the present value.
   */
  @Test
  public void presentValue() {
    final MultipleCurrencyAmount pv = METHOD.presentValue(ZERO_COUPON_1, MARKET.getInflationProvider());
    final double df = MARKET.getCurve(ZERO_COUPON_1.getCurrency()).getDiscountFactor(ZERO_COUPON_1.getPaymentTime());
    final double indexMonth0 = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(ZERO_COUPON_1.getReferenceEndTime()[0]);
    final double indexMonth1 = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(ZERO_COUPON_1.getReferenceEndTime()[1]);
    final double finalIndex = ZERO_COUPON_1_DEFINITION.getWeight() * indexMonth0 + (1 - ZERO_COUPON_1_DEFINITION.getWeight()) * indexMonth1;
    final double pvExpected = (finalIndex / INDEX_MAY_2008_INT - 1) * df * NOTIONAL;
    assertEquals("Zero-coupon inflation DiscountingMethod: Present value", pvExpected, pv.getAmount(ZERO_COUPON_1.getCurrency()), TOLERANCE_PV);
  }

  /**
   * Tests the net amount.
   */
  @Test
  public void netAmount() {
    final MultipleCurrencyAmount pv = METHOD.netAmount(ZERO_COUPON_1, MARKET.getInflationProvider());
    final double indexMonth0 = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(ZERO_COUPON_1.getReferenceEndTime()[0]);
    final double indexMonth1 = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(ZERO_COUPON_1.getReferenceEndTime()[1]);
    final double finalIndex = ZERO_COUPON_1_DEFINITION.getWeight() * indexMonth0 + (1 - ZERO_COUPON_1_DEFINITION.getWeight()) * indexMonth1;
    final double pvExpected = (finalIndex / INDEX_MAY_2008_INT - 1) * NOTIONAL;
    assertEquals("Zero-coupon inflation DiscountingMethod: net amount", pvExpected, pv.getAmount(ZERO_COUPON_1.getCurrency()), TOLERANCE_PV);
  }

  /**
   * Tests the present value: Method vs Calculator.
   */
  @Test
  public void presentValueMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD.presentValue(ZERO_COUPON_1, MARKET.getInflationProvider());
    final MultipleCurrencyAmount pvCalculator = ZERO_COUPON_1.accept(PVIC, MARKET.getInflationProvider());
    assertEquals("Zero-coupon inflation DiscountingMethod: Present value", pvMethod, pvCalculator);
  }

  /**
   * Tests the net amount: Method vs Calculator.
   */
  @Test
  public void netAmountMethodVsCalculator() {
    final MultipleCurrencyAmount naMethod = METHOD.netAmount(ZERO_COUPON_1, MARKET.getInflationProvider());
    final MultipleCurrencyAmount naCalculator = ZERO_COUPON_1.accept(NAIC, MARKET.getInflationProvider());
    assertEquals("Zero-coupon inflation DiscountingMethod: Net amount", naMethod, naCalculator);
  }

  /**
   * Test the present value curves sensitivity.
   */
  @Test
  public void presentValueCurveSensitivityWithNotional() {

    final MultipleCurrencyParameterSensitivity pvicsFD = PS_PV_FDC.calculateSensitivity(ZERO_COUPON_1, MARKET.getInflationProvider());
    final MultipleCurrencyParameterSensitivity pvicsExact = PSC.calculateSensitivity(ZERO_COUPON_1, MARKET.getInflationProvider());

    AssertSensivityObjects.assertEquals("Zero-coupon inflation DiscountingMethod: presentValueCurveSensitivity ", pvicsExact, pvicsFD, TOLERANCE_PV_DELTA);

  }

  @Test
  public void presentValueMarketSensitivityMethodVsCalculatorNoNotional() {
    final MultipleCurrencyInflationSensitivity pvcisMethod = METHOD.presentValueCurveSensitivity(ZERO_COUPON_1, MARKET.getInflationProvider());
    final MultipleCurrencyInflationSensitivity pvcisCalculator = ZERO_COUPON_1.accept(PVCSDC, MARKET.getInflationProvider());
    AssertSensivityObjects.assertEquals("Zero-coupon inflation DiscountingMethod: presentValueMarketSensitivity", pvcisMethod, pvcisCalculator, TOLERANCE_PV_DELTA);
  }

  /**
   * Tests the present value for curves with seasonal adjustment.
   */
  @Test
  public void presentValueSeasonality() {
    final InflationIssuerProviderDiscount marketSeason = MulticurveProviderDiscountDataSets.createMarket2(PRICING_DATE);
    final int tenorYear = 5;
    final double notional = 100000000;
    final ZonedDateTime settleDate = ScheduleCalculator.getAdjustedDate(PRICING_DATE, USDLIBOR3M.getSpotLag(), CALENDAR_USD);
    final ZonedDateTime paymentDate = ScheduleCalculator.getAdjustedDate(settleDate, Period.ofYears(tenorYear), BUSINESS_DAY, CALENDAR_USD, USDLIBOR3M.isEndOfMonth());
    final double weightSettle = 1.0 - (settleDate.getDayOfMonth() - 1.0) / settleDate.toLocalDate().lengthOfMonth();
    final double indexStart = weightSettle * 225.964 + (1 - weightSettle) * 225.722;
    final CouponInflationZeroCouponInterpolationDefinition zeroCouponUsdDefinition = CouponInflationZeroCouponInterpolationDefinition.from(settleDate, paymentDate, notional, PRICE_INDEX_US,
        MONTH_LAG, MONTH_LAG, false);
    final CouponInflationZeroCouponInterpolation zeroCouponUsd = (CouponInflationZeroCouponInterpolation) zeroCouponUsdDefinition.toDerivative(PRICING_DATE, priceIndexTS);
    final MultipleCurrencyAmount pvInflation = METHOD.presentValue(zeroCouponUsd, marketSeason.getInflationProvider());
    final double df = MARKET.getCurve(zeroCouponUsd.getCurrency()).getDiscountFactor(zeroCouponUsd.getPaymentTime());
    final double indexMonth0 = marketSeason.getCurve(PRICE_INDEX_US).getPriceIndex(zeroCouponUsd.getReferenceEndTime()[0]);
    final double indexMonth1 = marketSeason.getCurve(PRICE_INDEX_US).getPriceIndex(zeroCouponUsd.getReferenceEndTime()[1]);
    final double finalIndex = zeroCouponUsdDefinition.getWeight() * indexMonth0 + (1 - zeroCouponUsdDefinition.getWeight()) * indexMonth1;
    final double pvExpected = (finalIndex / INDEX_MAY_2011_INT - 1) * df * notional;
    assertEquals("PV in market with seasonal adjustment", pvExpected, pvInflation.getAmount(zeroCouponUsd.getCurrency()), 1E-2);
  }
}
