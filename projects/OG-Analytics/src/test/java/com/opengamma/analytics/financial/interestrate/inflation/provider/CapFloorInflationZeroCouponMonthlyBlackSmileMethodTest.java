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

import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CapFloorInflationZeroCouponMonthlyDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyDefinition;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationZeroCouponMonthly;
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
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 *  Tests the present value and its sensitivities for zero-coupon cap/floor with reference index on the first of the month.
 */
@Test(groups = TestGroup.UNIT)
public class CapFloorInflationZeroCouponMonthlyBlackSmileMethodTest {
  private static final InflationIssuerProviderDiscount MARKET = MulticurveProviderDiscountDataSets.createMarket1();
  private static final IndexPrice[] PRICE_INDEXES = MARKET.getPriceIndexes().toArray(new IndexPrice[MARKET.getPriceIndexes().size()]);
  private static final IndexPrice PRICE_INDEX_EUR = PRICE_INDEXES[0];
  private static final Calendar CALENDAR_EUR = MulticurveProviderDiscountDataSets.getEURCalendar();
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final int MATURITY = 10;
  private static final Period COUPON_TENOR = Period.ofYears(MATURITY);
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(START_DATE, COUPON_TENOR, BUSINESS_DAY, CALENDAR_EUR);
  private static final double NOTIONAL = 98765432;
  private static final int MONTH_LAG = 3;
  private static final double INDEX_1MAY_2008 = 108.23; // 3 m before Aug: May / 1 May index = May index: 108.23

  private static final double STRIKE = .02;
  private static final boolean IS_CAP = true;
  private static final ZonedDateTime LAST_KNOWN_FIXING_DATE = DateUtils.getUTCDate(2008, 7, 01);

  private static final double SHIFT_FD = 1.0E-7;
  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2;

  private static final InterpolatedDoublesSurface BLACK_SURF = BlackDataSets.createBlackSurfaceExpiryStrike();
  private static final BlackSmileCapInflationZeroCouponParameters BLACK_PARAM = new BlackSmileCapInflationZeroCouponParameters(BLACK_SURF, PRICE_INDEX_EUR);
  private static final BlackSmileCapInflationZeroCouponProviderDiscount BLACK_INFLATION = new BlackSmileCapInflationZeroCouponProviderDiscount(MARKET.getInflationProvider(), BLACK_PARAM);

  private static final ZonedDateTime PRICING_DATE = DateUtils.getUTCDate(2011, 8, 3);
  private static final CouponInflationZeroCouponMonthlyDefinition ZERO_COUPON_DEFINITION = CouponInflationZeroCouponMonthlyDefinition.from(START_DATE, PAYMENT_DATE, NOTIONAL, PRICE_INDEX_EUR,
      MONTH_LAG, MONTH_LAG, false);
  private static final CapFloorInflationZeroCouponMonthlyDefinition ZERO_COUPON_DEFINITION_CAP = CapFloorInflationZeroCouponMonthlyDefinition.from(ZERO_COUPON_DEFINITION,
      LAST_KNOWN_FIXING_DATE, MATURITY, STRIKE, IS_CAP);

  private static final DoubleTimeSeries<ZonedDateTime> priceIndexTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {DateUtils.getUTCDate(2008, 5, 31), DateUtils.getUTCDate(2018, 5, 31), DateUtils.getUTCDate(2018, 6, 30) }, new double[] {108.23, 128.23, 128.43 });

  private static final CapFloorInflationZeroCouponMonthly ZERO_COUPON_CAP = (CapFloorInflationZeroCouponMonthly) ZERO_COUPON_DEFINITION_CAP.toDerivative(PRICING_DATE, priceIndexTS);

  private static final CapFloorInflationZeroCouponMonthlyBlackSmileMethod METHOD = CapFloorInflationZeroCouponMonthlyBlackSmileMethod.getInstance();
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
  public void presentValueNoNotional() {
    final MultipleCurrencyAmount pv = METHOD.presentValue(ZERO_COUPON_CAP, BLACK_INFLATION);
    final double timeToMaturity = ZERO_COUPON_CAP.getReferenceEndTime() - ZERO_COUPON_CAP.getLastKnownFixingTime();
    final double df = MARKET.getCurve(ZERO_COUPON_CAP.getCurrency()).getDiscountFactor(ZERO_COUPON_CAP.getPaymentTime());
    final double finalIndex = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(ZERO_COUPON_CAP.getReferenceEndTime());
    final double forward = finalIndex / INDEX_1MAY_2008;
    final EuropeanVanillaOption option = new EuropeanVanillaOption(Math.pow(1 + ZERO_COUPON_CAP.getStrike(), ZERO_COUPON_CAP.getMaturity()), timeToMaturity, ZERO_COUPON_CAP.isCap());
    final double volatility = BLACK_INFLATION.getBlackParameters().getVolatility(ZERO_COUPON_CAP.getReferenceEndTime(), ZERO_COUPON_CAP.getStrike());
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

}
