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
import com.opengamma.analytics.financial.instrument.inflation.CapFloorInflationYearOnYearMonthlyDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationYearOnYearMonthlyDefinition;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationYearOnYearMonthly;
import com.opengamma.analytics.financial.model.option.parameters.BlackSmileCapInflationYearOnYearParameters;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.NormalFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.NormalPriceFunction;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueBlackSmileInflationYearOnYearCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueCurveSensitivityBlackSmileInflationYearOnYearCalculator;
import com.opengamma.analytics.financial.provider.description.BlackDataSets;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.inflation.BlackSmileCapInflationYearOnYearProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.BlackSmileCapInflationYearOnYearProviderInterface;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.MultipleCurrencyInflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.ParameterSensitivityBlackSmileYearOnYearCapDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterInflationSensitivityParameterCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 *  Tests the present value and its sensitivities for year on year cap/floor with reference index on the first of the month.
 */
@Test(groups = TestGroup.UNIT)
public class CapFloorInflationYearOnYearMonthlyBlackNormalSmileMethodTest {
  private static final InflationIssuerProviderDiscount MARKET = MulticurveProviderDiscountDataSets.createMarket1();
  private static final IndexPrice[] PRICE_INDEXES = MARKET.getPriceIndexes().toArray(new IndexPrice[MARKET.getPriceIndexes().size()]);
  private static final IndexPrice PRICE_INDEX_EUR = PRICE_INDEXES[0];
  private static final Calendar CALENDAR_EUR = MulticurveProviderDiscountDataSets.getEURCalendar();
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final Period COUPON_TENOR = Period.ofYears(10);
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(START_DATE, COUPON_TENOR, BUSINESS_DAY, CALENDAR_EUR);
  private static final ZonedDateTime PAYMENT_DATE_MINUS1 = ScheduleCalculator.getAdjustedDate(START_DATE, Period.ofYears(9), BUSINESS_DAY, CALENDAR_EUR);
  private static final double NOTIONAL = 98765432;
  private static final int MONTH_LAG = 3;
  private static final ZonedDateTime PRICING_DATE = DateUtils.getUTCDate(2011, 8, 3);
  private static final double STRIKE = .02;
  private static final boolean IS_CAP = true;
  private static final ZonedDateTime LAST_KNOWN_FIXING_DATE = DateUtils.getUTCDate(2008, 7, 01);

  private static final InterpolatedDoublesSurface BLACK_SURF = BlackDataSets.createBlackSurfaceExpiryStrike();
  private static final BlackSmileCapInflationYearOnYearParameters BLACK_PARAM = new BlackSmileCapInflationYearOnYearParameters(BLACK_SURF, PRICE_INDEX_EUR);
  private static final BlackSmileCapInflationYearOnYearProviderDiscount BLACK_INFLATION = new BlackSmileCapInflationYearOnYearProviderDiscount(MARKET.getInflationProvider(), BLACK_PARAM);

  private static final double SHIFT_FD = 1.0E-7;
  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2;

  private static final CouponInflationYearOnYearMonthlyDefinition YEAR_ON_YEAR_DEFINITION = CouponInflationYearOnYearMonthlyDefinition.from(PAYMENT_DATE_MINUS1, PAYMENT_DATE, NOTIONAL,
      PRICE_INDEX_EUR,
      MONTH_LAG, false);
  private static final CapFloorInflationYearOnYearMonthlyDefinition YEAR_ON_YEAR_DEFINITION_CAP = CapFloorInflationYearOnYearMonthlyDefinition.from(YEAR_ON_YEAR_DEFINITION,
      LAST_KNOWN_FIXING_DATE, STRIKE, IS_CAP);
  private static final CapFloorInflationYearOnYearMonthly YEAR_ON_YEAR_CAP = YEAR_ON_YEAR_DEFINITION_CAP.toDerivative(PRICING_DATE);

  private static final CapFloorInflationYearOnYearMonthlyBlackNormalSmileMethod METHOD = CapFloorInflationYearOnYearMonthlyBlackNormalSmileMethod.getInstance();
  private static final PresentValueBlackSmileInflationYearOnYearCalculator PVIC = PresentValueBlackSmileInflationYearOnYearCalculator.getInstance();
  private static final PresentValueCurveSensitivityBlackSmileInflationYearOnYearCalculator PVCSDC = PresentValueCurveSensitivityBlackSmileInflationYearOnYearCalculator.getInstance();
  private static final ParameterInflationSensitivityParameterCalculator<BlackSmileCapInflationYearOnYearProviderInterface> PSC = new ParameterInflationSensitivityParameterCalculator<>(PVCSDC);
  private static final ParameterSensitivityBlackSmileYearOnYearCapDiscountInterpolatedFDCalculator PS_PV_FDC = new ParameterSensitivityBlackSmileYearOnYearCapDiscountInterpolatedFDCalculator(PVIC,
      SHIFT_FD);

  /**
  * The Black function used in the pricing.
  */
  private static final NormalPriceFunction NORMAL_FUNCTION = new NormalPriceFunction();

  /**
   * Tests the present value.
   */
  @Test
  public void presentValue() {
    final MultipleCurrencyAmount pv = METHOD.presentValue(YEAR_ON_YEAR_CAP, BLACK_INFLATION);
    final double df = MARKET.getCurve(YEAR_ON_YEAR_CAP.getCurrency()).getDiscountFactor(YEAR_ON_YEAR_CAP.getPaymentTime());
    final double finalIndex = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(YEAR_ON_YEAR_CAP.getReferenceEndTime());
    final double initialIndex = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(YEAR_ON_YEAR_CAP.getReferenceStartTime());
    final double forward = finalIndex / initialIndex - 1;
    final double timeToMaturity = YEAR_ON_YEAR_CAP.getReferenceEndTime() - YEAR_ON_YEAR_CAP.getLastKnownFixingTime();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(YEAR_ON_YEAR_CAP.getStrike(), timeToMaturity, YEAR_ON_YEAR_CAP.isCap());
    final double volatility = BLACK_INFLATION.getBlackParameters().getVolatility(YEAR_ON_YEAR_CAP.getReferenceEndTime(), YEAR_ON_YEAR_CAP.getStrike());
    final NormalFunctionData dataBlack = new NormalFunctionData(forward, 1.0, volatility);
    final Function1D<NormalFunctionData, Double> func = NORMAL_FUNCTION.getPriceFunction(option);
    final double pvExpected = df * func.evaluate(dataBlack) * YEAR_ON_YEAR_CAP.getNotional() * YEAR_ON_YEAR_CAP.getPaymentYearFraction();
    assertEquals("Year on year coupon inflation DiscountingMethod: Present value", pvExpected, pv.getAmount(YEAR_ON_YEAR_CAP.getCurrency()), TOLERANCE_PV);
  }

  /**
   * Tests the present value: Method vs Calculator.
   */
  @Test
  public void presentValueMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD.presentValue(YEAR_ON_YEAR_CAP, BLACK_INFLATION);
    final MultipleCurrencyAmount pvCalculator = YEAR_ON_YEAR_CAP.accept(PVIC, BLACK_INFLATION);
    assertEquals("Year on year coupon inflation DiscountingMethod: Present value", pvMethod, pvCalculator);
  }

  /**
   * Test the present value curves sensitivity.
   */
  @Test
  public void presentValueCurveSensitivity() {

    final MultipleCurrencyParameterSensitivity pvicsFD = PS_PV_FDC.calculateSensitivity(YEAR_ON_YEAR_CAP, BLACK_INFLATION);
    final MultipleCurrencyParameterSensitivity pvicsExact = PSC.calculateSensitivity(YEAR_ON_YEAR_CAP, BLACK_INFLATION);

    AssertSensivityObjects.assertEquals("Year on year coupon inflation DiscountingMethod: presentValueCurveSensitivity ", pvicsExact, pvicsFD, TOLERANCE_PV_DELTA);

  }

  @Test
  public void presentValueMarketSensitivityMethodVsCalculator() {
    final MultipleCurrencyInflationSensitivity pvcisMethod = METHOD.presentValueCurveSensitivity(YEAR_ON_YEAR_CAP, BLACK_INFLATION);
    final MultipleCurrencyInflationSensitivity pvcisCalculator = YEAR_ON_YEAR_CAP.accept(PVCSDC, BLACK_INFLATION);
    AssertSensivityObjects.assertEquals("Year on year coupon inflation DiscountingMethod: presentValueMarketSensitivity", pvcisMethod, pvcisCalculator, TOLERANCE_PV_DELTA);
  }

}
