/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.analytics.financial.instrument.inflation.CapFloorInflationZeroCouponInterpolationDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationDefinition;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationZeroCouponInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponInterpolation;
import com.opengamma.analytics.financial.model.option.parameters.BlackSmileCapInflationZeroCouponParameters;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueBlackSmileInflationZeroCouponCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueCurveSensitivityBlackSmileInflationZeroCouponCalculator;
import com.opengamma.analytics.financial.provider.description.BlackDataSets;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.inflation.BlackSmileCapInflationZeroCouponProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.BlackSmileCapInflationZeroCouponProviderInterface;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.MultipleCurrencyInflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.ParameterSensitivityBlackSmileZeroCouponCapDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterInflationSensitivityParameterCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests the present value and its sensitivities for zero-coupon cap/floor with reference index interpolated between months.
 */
@Test(groups = TestGroup.UNIT)
public class CapFloorInflationZeroCouponInterpolationBlackSmileMethodTest {
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
  private static final int MATURITY = 10;
  private static final Period COUPON_TENOR = Period.ofYears(MATURITY);
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(START_DATE, COUPON_TENOR, BUSINESS_DAY, CALENDAR_EUR);
  private static final double NOTIONAL = 98765432;
  private static final int MONTH_LAG = 3;
  private static final double INDEX_MAY_2008_INT = 108.4548387; // May index: 108.23 - June Index = 108.64
  private static final double STRIKE = .02;
  private static final boolean IS_CAP = true;
  private static final ZonedDateTime LAST_KNOWN_FIXING_DATE = DateUtils.getUTCDate(2008, 7, 01);

  private static final double SHIFT_FD = 1.0E-7;
  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2;

  private static final InterpolatedDoublesSurface BLACK_SURF = BlackDataSets.createBlackSurfaceExpiryStrike();
  private static final BlackSmileCapInflationZeroCouponParameters BLACK_PARAM = new BlackSmileCapInflationZeroCouponParameters(BLACK_SURF, PRICE_INDEX_EUR);
  private static final BlackSmileCapInflationZeroCouponProviderDiscount BLACK_INFLATION = new BlackSmileCapInflationZeroCouponProviderDiscount(MARKET.getInflationProvider(), BLACK_PARAM);

  private static final CouponInflationZeroCouponInterpolationDefinition ZERO_COUPON_DEFINITION = CouponInflationZeroCouponInterpolationDefinition.from(START_DATE, PAYMENT_DATE, NOTIONAL,
      PRICE_INDEX_EUR, MONTH_LAG, MONTH_LAG, false);

  private static final ZonedDateTime PRICING_DATE = DateUtils.getUTCDate(2011, 8, 3);
  private static final CapFloorInflationZeroCouponInterpolationDefinition ZERO_COUPON_DEFINITION_CAP = CapFloorInflationZeroCouponInterpolationDefinition.from(ZERO_COUPON_DEFINITION,
      LAST_KNOWN_FIXING_DATE, MATURITY, STRIKE, IS_CAP);

  private static final ZonedDateTimeDoubleTimeSeries TS_PRICE_INDEX_USD = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {DateUtils.getUTCDate(2008, 5, 31), DateUtils.getUTCDate(2008, 6, 30), DateUtils.getUTCDate(2011, 9, 27),
        DateUtils.getUTCDate(2011, 9, 28) }, new double[] {108.23, 108.64, 200, 200 });
  private static final CapFloorInflationZeroCouponInterpolation ZERO_COUPON_CAP = (CapFloorInflationZeroCouponInterpolation) ZERO_COUPON_DEFINITION_CAP.toDerivative(PRICING_DATE, TS_PRICE_INDEX_USD);

  private static final CapFloorInflationZeroCouponInterpolationBlackSmileMethod METHOD = CapFloorInflationZeroCouponInterpolationBlackSmileMethod.getInstance();
  private static final PresentValueBlackSmileInflationZeroCouponCalculator PVIC = PresentValueBlackSmileInflationZeroCouponCalculator.getInstance();
  private static final PresentValueCurveSensitivityBlackSmileInflationZeroCouponCalculator PVCSDC = PresentValueCurveSensitivityBlackSmileInflationZeroCouponCalculator.getInstance();
  private static final ParameterInflationSensitivityParameterCalculator<BlackSmileCapInflationZeroCouponProviderInterface> PSC = new ParameterInflationSensitivityParameterCalculator<>(PVCSDC);
  private static final ParameterSensitivityBlackSmileZeroCouponCapDiscountInterpolatedFDCalculator PS_PV_FDC = new ParameterSensitivityBlackSmileZeroCouponCapDiscountInterpolatedFDCalculator(PVIC,
      SHIFT_FD);

  /**
   * The Black function used in the pricing.
   */
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  /**
   * Tests the present value.
   */
  @Test
  public void presentValue() {
    final MultipleCurrencyAmount pv = METHOD.presentValue(ZERO_COUPON_CAP, BLACK_INFLATION);
    final double timeToMaturity = ZERO_COUPON_CAP.getReferenceEndTime()[1] - ZERO_COUPON_CAP.getLastKnownFixingTime();
    final double df = MARKET.getCurve(ZERO_COUPON_CAP.getCurrency()).getDiscountFactor(ZERO_COUPON_CAP.getPaymentTime());
    final double indexMonth0 = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(ZERO_COUPON_CAP.getReferenceEndTime()[0]);
    final double indexMonth1 = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(ZERO_COUPON_CAP.getReferenceEndTime()[1]);
    final double finalIndex = ZERO_COUPON_DEFINITION.getWeight() * indexMonth0 + (1 - ZERO_COUPON_DEFINITION.getWeight()) * indexMonth1;
    final double forward = finalIndex / (ZERO_COUPON_DEFINITION.getWeight() * 108.23 + (1 - ZERO_COUPON_DEFINITION.getWeight()) * 108.64);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(Math.pow(1 + ZERO_COUPON_CAP.getStrike(), ZERO_COUPON_CAP.getMaturity()), timeToMaturity, ZERO_COUPON_CAP.isCap());
    final double volatility = BLACK_INFLATION.getBlackParameters().getVolatility(ZERO_COUPON_CAP.getReferenceEndTime()[1], ZERO_COUPON_CAP.getStrike());
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatility);
    final Function1D<BlackFunctionData, Double> func = BLACK_FUNCTION.getPriceFunction(option);
    final double pvExpected = df * func.evaluate(dataBlack) * ZERO_COUPON_CAP.getNotional() * ZERO_COUPON_CAP.getPaymentYearFraction();
    assertEquals("Zero-coupon inflation DiscountingMethod: Present value", pvExpected, pv.getAmount(ZERO_COUPON_CAP.getCurrency()), TOLERANCE_PV);
  }

  /**
   * Tests the present value: Method vs Calculator.
   */
  @Test
  public void presentValueMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD.presentValue(ZERO_COUPON_CAP, BLACK_INFLATION);
    final MultipleCurrencyAmount pvCalculator = ZERO_COUPON_CAP.accept(PVIC, BLACK_INFLATION);
    assertEquals("Zero-coupon inflation DiscountingMethod: Present value", pvMethod, pvCalculator);
  }

  /**
   * Test the present value curves sensitivity.
   */
  @Test
  public void presentValueCurveSensitivity() {

    final MultipleCurrencyParameterSensitivity pvicsFD = PS_PV_FDC.calculateSensitivity(ZERO_COUPON_CAP, BLACK_INFLATION);
    final MultipleCurrencyParameterSensitivity pvicsExact = PSC.calculateSensitivity(ZERO_COUPON_CAP, BLACK_INFLATION);

    AssertSensitivityObjects.assertEquals("Zero-coupon inflation DiscountingMethod: presentValueCurveSensitivity ", pvicsExact, pvicsFD, TOLERANCE_PV_DELTA);

  }

  @Test
  public void presentValueMarketSensitivityMethodVsCalculator() {
    final MultipleCurrencyInflationSensitivity pvcisMethod = METHOD.presentValueCurveSensitivity(ZERO_COUPON_CAP, BLACK_INFLATION);
    final MultipleCurrencyInflationSensitivity pvcisCalculator = ZERO_COUPON_CAP.accept(PVCSDC, BLACK_INFLATION);
    AssertSensitivityObjects.assertEquals("Zero-coupon inflation DiscountingMethod: presentValueMarketSensitivity", pvcisMethod, pvcisCalculator, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests the present value for curves with seasonal adjustment.
   */
  public void presentValueSeasonality() {
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final InflationIssuerProviderDiscount marketSeason = MulticurveProviderDiscountDataSets.createMarket2(PRICING_DATE);
    final BlackSmileCapInflationZeroCouponProviderDiscount blackInflation = new BlackSmileCapInflationZeroCouponProviderDiscount(marketSeason.getInflationProvider(), BLACK_PARAM);
    final int tenorYear = 5;
    final double notional = 100000000;
    final ZonedDateTime settleDate = ScheduleCalculator.getAdjustedDate(PRICING_DATE, USDLIBOR3M.getSpotLag(), CALENDAR_USD);
    final ZonedDateTime paymentDate = ScheduleCalculator.getAdjustedDate(settleDate, Period.ofYears(tenorYear), BUSINESS_DAY, CALENDAR_USD, USDLIBOR3M.isEndOfMonth());
    final double weightSettle = 1.0 - (paymentDate.getDayOfMonth() - 1.0) / paymentDate.toLocalDate().lengthOfMonth();
    final double indexStart = weightSettle * 225.964 + (1 - weightSettle) * 225.722;
    final CouponInflationZeroCouponInterpolationDefinition zeroCouponUsdDefinition = CouponInflationZeroCouponInterpolationDefinition.from(settleDate, paymentDate, notional, PRICE_INDEX_US,
        MONTH_LAG, MONTH_LAG, false);
    final CapFloorInflationZeroCouponInterpolationDefinition capZeroCouponUsdDefinition = CapFloorInflationZeroCouponInterpolationDefinition.from(zeroCouponUsdDefinition,
        LAST_KNOWN_FIXING_DATE, MATURITY, STRIKE, IS_CAP);

    final ZonedDateTimeDoubleTimeSeries ts = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
        new ZonedDateTime[] {DateUtils.getUTCDate(2008, 4, 30), DateUtils.getUTCDate(2008, 5, 31), DateUtils.getUTCDate(2011, 5, 31), DateUtils.getUTCDate(2011, 6, 30),
          DateUtils.getUTCDate(2011, 9, 27),
          DateUtils.getUTCDate(2011, 9, 28) }, new double[] {108.23, 108.64, 225.964, 225.722, 200, 200 });

    final CapFloorInflationZeroCouponInterpolation capZeroCouponUsd = (CapFloorInflationZeroCouponInterpolation) capZeroCouponUsdDefinition.toDerivative(PRICING_DATE, ts);
    final CouponInflationZeroCouponInterpolation zeroCouponUsd = (CouponInflationZeroCouponInterpolation) zeroCouponUsdDefinition.toDerivative(PRICING_DATE, ts);
    final MultipleCurrencyAmount pvInflation = METHOD.presentValue(capZeroCouponUsd, blackInflation);
    final double df = marketSeason.getCurve(zeroCouponUsd.getCurrency()).getDiscountFactor(zeroCouponUsd.getPaymentTime());
    final double indexMonth0 = marketSeason.getCurve(PRICE_INDEX_US).getPriceIndex(zeroCouponUsd.getReferenceEndTime()[0]);
    final double indexMonth1 = marketSeason.getCurve(PRICE_INDEX_US).getPriceIndex(zeroCouponUsd.getReferenceEndTime()[1]);
    final double finalIndex = zeroCouponUsdDefinition.getWeight() * indexMonth0 + (1 - zeroCouponUsdDefinition.getWeight()) * indexMonth1;
    final double forward = finalIndex / indexStart;
    final double timeToMaturity = capZeroCouponUsd.getReferenceEndTime()[1] - capZeroCouponUsd.getLastKnownFixingTime();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(Math.pow(1 + capZeroCouponUsd.getStrike(), capZeroCouponUsd.getMaturity()), timeToMaturity, capZeroCouponUsd.isCap());
    final double volatility = blackInflation.getBlackParameters().getVolatility(capZeroCouponUsd.getReferenceEndTime()[1], capZeroCouponUsd.getStrike());
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatility);
    final Function1D<BlackFunctionData, Double> func = blackFunction.getPriceFunction(option);
    final double pvExpected = df * func.evaluate(dataBlack) * capZeroCouponUsd.getNotional() * capZeroCouponUsd.getPaymentYearFraction();
    assertEquals("PV in market with seasonal adjustment", pvExpected, pvInflation.getAmount(zeroCouponUsd.getCurrency()), 1E-2);
  }
}
