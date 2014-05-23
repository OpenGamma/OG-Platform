/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.provider;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilityNodeSensitivityDataBundle;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.volatility.VolatilityAndBucketedSensitivities;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.financial.provider.calculator.blackforex.CurrencyExposureForexBlackSmileCalculator;
import com.opengamma.analytics.financial.provider.calculator.blackforex.PercentageGammaForexBlackSmileCalculator;
import com.opengamma.analytics.financial.provider.calculator.blackforex.PresentValueCurveSensitivityForexBlackSmileCalculator;
import com.opengamma.analytics.financial.provider.calculator.blackforex.PresentValueForexBlackSmileCalculator;
import com.opengamma.analytics.financial.provider.calculator.blackforex.PresentValueForexVolatilitySensitivityForexBlackSmileCalculator;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProvider;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderDiscount;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.blackforex.ParameterSensitivityForexBlackSmileDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;
import com.opengamma.util.tuple.Triple;

/**
 * Tests related to the pricing method for vanilla Forex option transactions with Black function and a volatility provider.
 */
@Test(groups = TestGroup.UNIT)
public class ForexOptionVanillaBlackSmileMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountForexDataSets.createMulticurvesForex();

  private static final FXMatrix FX_MATRIX = MULTICURVES.getFxRates();
  private static final Currency EUR = Currency.EUR;
  private static final Currency USD = Currency.USD;
  private static final double SPOT = FX_MATRIX.getFxRate(EUR, USD);
  // General
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final int SETTLEMENT_DAYS = 2;
  // Smile data
  private static final Period[] EXPIRY_PERIOD = new Period[] {Period.ofMonths(3), Period.ofMonths(6), Period.ofYears(1),
      Period.ofYears(2), Period.ofYears(5) };
  private static final int NB_EXP = EXPIRY_PERIOD.length;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 6, 13);
  private static final ZonedDateTime REFERENCE_SPOT = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final ZonedDateTime[] PAY_DATE = new ZonedDateTime[NB_EXP];
  private static final ZonedDateTime[] EXPIRY_DATE = new ZonedDateTime[NB_EXP];
  private static final double[] TIME_TO_EXPIRY = new double[NB_EXP + 1];
  static {
    TIME_TO_EXPIRY[0] = 0.0;
    for (int loopexp = 0; loopexp < NB_EXP; loopexp++) {
      PAY_DATE[loopexp] = ScheduleCalculator.getAdjustedDate(REFERENCE_SPOT, EXPIRY_PERIOD[loopexp], BUSINESS_DAY, CALENDAR);
      EXPIRY_DATE[loopexp] = ScheduleCalculator.getAdjustedDate(PAY_DATE[loopexp], -SETTLEMENT_DAYS, CALENDAR);
      TIME_TO_EXPIRY[loopexp + 1] = TimeCalculator.getTimeBetween(REFERENCE_DATE, EXPIRY_DATE[loopexp]);
    }
  }
  private static final double[] ATM = {0.175, 0.185, 0.18, 0.17, 0.16, 0.16 };
  private static final double[] DELTA = new double[] {0.10, 0.25 };
  private static final double[][] RISK_REVERSAL = new double[][] { {-0.010, -0.0050 }, {-0.011, -0.0060 }, {-0.012, -0.0070 }, {-0.013, -0.0080 }, {-0.014, -0.0090 },
    {-0.014, -0.0090 } };
  private static final double[][] STRANGLE = new double[][] { {0.0300, 0.0100 }, {0.0310, 0.0110 }, {0.0320, 0.0120 }, {0.0330, 0.0130 }, {0.0340, 0.0140 }, {0.0340, 0.0140 } };
  private static final int NB_STRIKE = 2 * DELTA.length + 1;
  private static final SmileDeltaTermStructureParametersStrikeInterpolation SMILE_TERM = new SmileDeltaTermStructureParametersStrikeInterpolation(TIME_TO_EXPIRY, DELTA,
      ATM, RISK_REVERSAL, STRANGLE);
  private static final SmileDeltaTermStructureParametersStrikeInterpolation SMILE_TERM_FLAT = ForexSmileProviderDataSets.smileFlat(REFERENCE_DATE);
  private static final BlackForexSmileProviderDiscount SMILE_MULTICURVES = new BlackForexSmileProviderDiscount(MULTICURVES, SMILE_TERM, Pairs.of(EUR, USD));
  private static final BlackForexSmileProviderDiscount SMILE_FLAT_MULTICURVES = new BlackForexSmileProviderDiscount(MULTICURVES, SMILE_TERM_FLAT, Pairs.of(EUR, USD));

  private static final double SHIFT = 1.0E-6;
  private static final FXMatrix FX_MATRIX_M = new FXMatrix(EUR, USD, SPOT - SHIFT);
  private static final FXMatrix FX_MATRIX_P = new FXMatrix(EUR, USD, SPOT + SHIFT);
  private static final MulticurveProviderDiscount MULTICURVES_FX_M = MULTICURVES.copy();
  private static final MulticurveProviderDiscount MULTICURVES_FX_P = MULTICURVES.copy();
  static {
    MULTICURVES_FX_M.setForexMatrix(FX_MATRIX_M);
    MULTICURVES_FX_P.setForexMatrix(FX_MATRIX_P);
  }
  private static final BlackForexSmileProvider SMILE_M_MULTICURVES = new BlackForexSmileProvider(MULTICURVES_FX_M, SMILE_TERM_FLAT, Pairs.of(EUR, USD));
  private static final BlackForexSmileProvider SMILE_P_MULTICURVES = new BlackForexSmileProvider(MULTICURVES_FX_P, SMILE_TERM_FLAT, Pairs.of(EUR, USD));

  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  private static final ForexOptionVanillaBlackSmileMethod METHOD_OPTION = ForexOptionVanillaBlackSmileMethod.getInstance();
  private static final ForexDiscountingMethod METHOD_DISC = ForexDiscountingMethod.getInstance();

  private static final PresentValueForexBlackSmileCalculator PVFBC = PresentValueForexBlackSmileCalculator.getInstance();
  private static final CurrencyExposureForexBlackSmileCalculator CEFBC = CurrencyExposureForexBlackSmileCalculator.getInstance();
  private static final PresentValueCurveSensitivityForexBlackSmileCalculator PVCSFBC = PresentValueCurveSensitivityForexBlackSmileCalculator.getInstance();
  private static final PresentValueForexVolatilitySensitivityForexBlackSmileCalculator PVVSFBSC = PresentValueForexVolatilitySensitivityForexBlackSmileCalculator
      .getInstance();

  private static final double SHIFT_FD = 1.0E-6;
  private static final ParameterSensitivityParameterCalculator<BlackForexSmileProviderInterface> PS_FBS_C = new ParameterSensitivityParameterCalculator<>(
      PVCSFBC);
  private static final ParameterSensitivityForexBlackSmileDiscountInterpolatedFDCalculator PS_FBS_FDC = new ParameterSensitivityForexBlackSmileDiscountInterpolatedFDCalculator(
      PVFBC, SHIFT_FD);

  // option
  private static final double STRIKE = 1.45;
  private static final boolean IS_CALL = true;
  private static final boolean IS_LONG = true;
  private static final double NOTIONAL = 100000000;
  private static final ZonedDateTime OPTION_PAY_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
  private static final ZonedDateTime OPTION_EXP_DATE = ScheduleCalculator.getAdjustedDate(OPTION_PAY_DATE, -SETTLEMENT_DAYS, CALENDAR);
  private static final ForexDefinition FOREX_DEFINITION = new ForexDefinition(EUR, USD, OPTION_PAY_DATE, NOTIONAL, STRIKE);
  private static final ForexOptionVanillaDefinition FOREX_OPTION_CALL_DEFINITION = new ForexOptionVanillaDefinition(FOREX_DEFINITION, OPTION_EXP_DATE, IS_CALL, IS_LONG);
  private static final ForexOptionVanilla FOREX_CALL_OPTION = FOREX_OPTION_CALL_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final double TOLERANCE_RELATIVE = 1.0E-9;
  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+0;

  @Test
  /**
   * Tests the present value at a time grid point.
   */
  public void persentValueAtGridPoint() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final int indexPay = 2; // 1Y
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, PAY_DATE[indexPay], notional, strike);
    final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, EXPIRY_DATE[indexPay], isCall, isLong);
    final ForexOptionVanilla forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE);
    final double df = MULTICURVES.getDiscountFactor(USD, TimeCalculator.getTimeBetween(REFERENCE_DATE, PAY_DATE[indexPay]));
    final double forward = SPOT * MULTICURVES.getDiscountFactor(EUR, TimeCalculator.getTimeBetween(REFERENCE_DATE, PAY_DATE[indexPay])) / df;
    final double volatility = SMILE_TERM.getVolatility(Triple.of(TIME_TO_EXPIRY[indexPay + 1], strike, forward));
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, df, volatility);
    final Function1D<BlackFunctionData, Double> func = BLACK_FUNCTION.getPriceFunction(forexOption);
    final double priceExpected = func.evaluate(dataBlack) * notional;
    final MultipleCurrencyAmount priceComputed = METHOD_OPTION.presentValue(forexOption, SMILE_MULTICURVES);
    assertEquals("Forex vanilla option: present value", priceExpected, priceComputed.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value against an explicit computation.
   */
  public void presentValue() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final double timeToExpiry = TimeCalculator.getTimeBetween(REFERENCE_DATE, expDate);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, payDate, notional, strike);
    final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionVanilla forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE);
    final double df = MULTICURVES.getDiscountFactor(USD, TimeCalculator.getTimeBetween(REFERENCE_DATE, payDate));
    final double forward = SPOT * MULTICURVES.getDiscountFactor(EUR, TimeCalculator.getTimeBetween(REFERENCE_DATE, payDate)) / df;
    final double volatility = SMILE_TERM.getVolatility(Triple.of(timeToExpiry, strike, forward));
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, df, volatility);
    final Function1D<BlackFunctionData, Double> func = BLACK_FUNCTION.getPriceFunction(forexOption);
    final double priceExpected = func.evaluate(dataBlack) * notional;
    final MultipleCurrencyAmount priceComputed = METHOD_OPTION.presentValue(forexOption, SMILE_MULTICURVES);
    assertEquals("Forex vanilla option: present value", priceExpected, priceComputed.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests a EUR/USD call vs a USD/EUR put.
   */
  public void presentValueCallPut() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexEURUSDDefinition = new ForexDefinition(EUR, USD, payDate, notional, strike);
    final ForexDefinition forexUSDEURDefinition = new ForexDefinition(USD, EUR, payDate, -notional * strike, 1.0 / strike);
    final ForexOptionVanillaDefinition callEURUSDDefinition = new ForexOptionVanillaDefinition(forexEURUSDDefinition, expDate, isCall, isLong);
    final ForexOptionVanillaDefinition putUSDEURDefinition = new ForexOptionVanillaDefinition(forexUSDEURDefinition, expDate, isCall, isLong);
    final ForexOptionVanilla callEURUSD = callEURUSDDefinition.toDerivative(REFERENCE_DATE);
    final ForexOptionVanilla putUSDEUR = putUSDEURDefinition.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyAmount pvCallEURUSD = METHOD_OPTION.presentValue(callEURUSD, SMILE_MULTICURVES);
    final MultipleCurrencyAmount pvPutUSDEUR = METHOD_OPTION.presentValue(putUSDEUR, SMILE_MULTICURVES);
    assertEquals("Forex vanilla option: present value Method vs Calculator", pvCallEURUSD.getAmount(USD) / SPOT, pvPutUSDEUR.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value Method versus the Calculator.
   */
  public void presentValueMethodVsCalculator() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, payDate, notional, strike);
    final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionVanilla forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyAmount pvMethod = METHOD_OPTION.presentValue(forexOption, SMILE_MULTICURVES);
    final MultipleCurrencyAmount pvCalculator = forexOption.accept(PVFBC, SMILE_MULTICURVES);
    assertEquals("Forex vanilla option: present value Method vs Calculator", pvMethod.getAmount(USD), pvCalculator.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value long/short parity.
   */
  public void presentValueLongShort() {
    final ForexOptionVanillaDefinition forexOptionShortDefinition = new ForexOptionVanillaDefinition(FOREX_DEFINITION, OPTION_EXP_DATE, IS_CALL, !IS_LONG);
    final ForexOptionVanilla forexOptionShort = forexOptionShortDefinition.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyAmount pvShort = METHOD_OPTION.presentValue(forexOptionShort, SMILE_MULTICURVES);
    final MultipleCurrencyAmount pvLong = METHOD_OPTION.presentValue(FOREX_CALL_OPTION, SMILE_MULTICURVES);
    assertEquals("Forex vanilla option: present value long/short parity", pvLong.getAmount(USD), -pvShort.getAmount(USD), TOLERANCE_PV);
    final MultipleCurrencyAmount ceShort = METHOD_OPTION.currencyExposure(forexOptionShort, SMILE_MULTICURVES);
    final MultipleCurrencyAmount ceLong = METHOD_OPTION.currencyExposure(FOREX_CALL_OPTION, SMILE_MULTICURVES);
    assertEquals("Forex vanilla option: currency exposure long/short parity", ceLong.getAmount(USD), -ceShort.getAmount(USD), TOLERANCE_PV);
    assertEquals("Forex vanilla option: currency exposure long/short parity", ceLong.getAmount(EUR), -ceShort.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the currency exposure against an explicit computation.
   */
  public void currencyExposure() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final double timeToExpiry = TimeCalculator.getTimeBetween(REFERENCE_DATE, expDate);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, payDate, notional, strike);
    final ForexOptionVanillaDefinition forexOptionDefinitionCall = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionVanillaDefinition forexOptionDefinitionPut = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, !isCall, isLong);
    final ForexOptionVanilla forexOptionCall = forexOptionDefinitionCall.toDerivative(REFERENCE_DATE);
    final ForexOptionVanilla forexOptionPut = forexOptionDefinitionPut.toDerivative(REFERENCE_DATE);
    final double dfDomestic = MULTICURVES.getDiscountFactor(USD, TimeCalculator.getTimeBetween(REFERENCE_DATE, payDate)); // USD
    final double dfForeign = MULTICURVES.getDiscountFactor(EUR, TimeCalculator.getTimeBetween(REFERENCE_DATE, payDate)); // EUR
    final double forward = SPOT * dfForeign / dfDomestic;
    final double volatility = SMILE_TERM.getVolatility(Triple.of(timeToExpiry, strike, forward));
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, dfDomestic, volatility);
    final double[] priceAdjointCall = BLACK_FUNCTION.getPriceAdjoint(forexOptionCall, dataBlack);
    final double[] priceAdjointPut = BLACK_FUNCTION.getPriceAdjoint(forexOptionPut, dataBlack);
    final double deltaForwardCall = priceAdjointCall[1];
    final double deltaForwardPut = priceAdjointPut[1];
    final double deltaSpotCall = deltaForwardCall * dfForeign / dfDomestic;
    final double deltaSpotPut = deltaForwardPut * dfForeign / dfDomestic;
    final MultipleCurrencyAmount priceComputedCall = METHOD_OPTION.presentValue(forexOptionCall, SMILE_MULTICURVES);
    final MultipleCurrencyAmount priceComputedPut = METHOD_OPTION.presentValue(forexOptionPut, SMILE_MULTICURVES);
    final MultipleCurrencyAmount currencyExposureCallComputed = METHOD_OPTION.currencyExposure(forexOptionCall, SMILE_MULTICURVES);
    assertEquals("Forex vanilla option: currency exposure foreign - call", deltaSpotCall * notional, currencyExposureCallComputed.getAmount(EUR), TOLERANCE_PV);
    assertEquals("Forex vanilla option: currency exposure domestic - call", -deltaSpotCall * notional * SPOT + priceComputedCall.getAmount(USD),
        currencyExposureCallComputed.getAmount(USD), 1E-2);
    final MultipleCurrencyAmount currencyExposurePutComputed = METHOD_OPTION.currencyExposure(forexOptionPut, SMILE_MULTICURVES);
    assertEquals("Forex vanilla option: currency exposure foreign- put", deltaSpotPut * notional, currencyExposurePutComputed.getAmount(EUR), TOLERANCE_PV);
    assertEquals("Forex vanilla option: currency exposure domestic - put", -deltaSpotPut * notional * SPOT + priceComputedPut.getAmount(USD),
        currencyExposurePutComputed.getAmount(USD), 1E-2);
  }

  @Test
  /**
   * Tests the currency exposure against the present value.
   */
  public void currencyExposureVsPresentValue() {
    final MultipleCurrencyAmount pv = METHOD_OPTION.presentValue(FOREX_CALL_OPTION, SMILE_MULTICURVES);
    final MultipleCurrencyAmount ce = METHOD_OPTION.currencyExposure(FOREX_CALL_OPTION, SMILE_MULTICURVES);
    assertEquals("Forex vanilla option: currency exposure vs present value", ce.getAmount(USD) + ce.getAmount(EUR) * SPOT, pv.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests a EUR/USD call vs a USD/EUR put.
   */
  public void currencyExposureCallPut() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexEURUSDDefinition = new ForexDefinition(EUR, USD, payDate, notional, strike);
    final ForexDefinition forexUSDEURDefinition = new ForexDefinition(USD, EUR, payDate, -notional * strike, 1.0 / strike);
    final ForexOptionVanillaDefinition callEURUSDDefinition = new ForexOptionVanillaDefinition(forexEURUSDDefinition, expDate, isCall, isLong);
    final ForexOptionVanillaDefinition putUSDEURDefinition = new ForexOptionVanillaDefinition(forexUSDEURDefinition, expDate, isCall, isLong);
    final ForexOptionVanilla callEURUSD = callEURUSDDefinition.toDerivative(REFERENCE_DATE);
    final ForexOptionVanilla putUSDEUR = putUSDEURDefinition.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyAmount pvCallEURUSD = METHOD_OPTION.currencyExposure(callEURUSD, SMILE_MULTICURVES);
    final MultipleCurrencyAmount pvPutUSDEUR = METHOD_OPTION.currencyExposure(putUSDEUR, SMILE_MULTICURVES);
    assertEquals("Forex vanilla option: currency exposure", pvCallEURUSD.getAmount(EUR), pvPutUSDEUR.getAmount(EUR), TOLERANCE_PV);
    assertEquals("Forex vanilla option: currency exposure", pvCallEURUSD.getAmount(USD), pvPutUSDEUR.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the put/call parity currency exposure.
   */
  public void currencyExposurePutCallParity() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, payDate, notional, strike);
    final ForexOptionVanillaDefinition forexOptionDefinitionCall = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionVanillaDefinition forexOptionDefinitionPut = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, !isCall, isLong);
    final ForexOptionVanilla forexOptionCall = forexOptionDefinitionCall.toDerivative(REFERENCE_DATE);
    final ForexOptionVanilla forexOptionPut = forexOptionDefinitionPut.toDerivative(REFERENCE_DATE);
    final Forex forexForward = forexUnderlyingDefinition.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyAmount currencyExposureCall = METHOD_OPTION.currencyExposure(forexOptionCall, SMILE_MULTICURVES);
    final MultipleCurrencyAmount currencyExposurePut = METHOD_OPTION.currencyExposure(forexOptionPut, SMILE_MULTICURVES);
    final MultipleCurrencyAmount currencyExposureForward = METHOD_DISC.currencyExposure(forexForward, MULTICURVES);
    assertEquals("Forex vanilla option: currency exposure put/call parity foreign", currencyExposureForward.getAmount(EUR), currencyExposureCall.getAmount(EUR)
        - currencyExposurePut.getAmount(EUR), TOLERANCE_PV);
    assertEquals("Forex vanilla option: currency exposure put/call parity domestic", currencyExposureForward.getAmount(USD), currencyExposureCall.getAmount(USD)
        - currencyExposurePut.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests currency exposure Method vs Calculator.
   */
  public void currencyExposureMethodVsCalculator() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, payDate, notional, strike);
    final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionVanilla forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyAmount ceMethod = METHOD_OPTION.currencyExposure(forexOption, SMILE_MULTICURVES);
    final MultipleCurrencyAmount ceCalculator = forexOption.accept(CEFBC, SMILE_MULTICURVES);
    assertEquals("Forex vanilla option: currency exposure Method vs Calculator", ceMethod.getAmount(EUR), ceCalculator.getAmount(EUR), TOLERANCE_PV);
    assertEquals("Forex vanilla option: currency exposure Method vs Calculator", ceMethod.getAmount(USD), ceCalculator.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests forward Forex rate.
   */
  public void forwardForexRate() {
    final double fwd = METHOD_OPTION.forwardForexRate(FOREX_CALL_OPTION, MULTICURVES);
    final double fwdExpected = METHOD_DISC.forwardForexRate(FOREX_CALL_OPTION.getUnderlyingForex(), MULTICURVES);
    assertEquals("Forex vanilla option: forward forex rate", fwd, fwdExpected, TOLERANCE_RELATIVE);
  }

  //  @Test
  //  /**
  //   * Tests the forward Forex rate through the method and through the calculator.
  //   */
  //  public void forwardRateMethodVsCalculator() {
  //    final double fwdMethod = METHOD_OPTION.forwardForexRate(FOREX_CALL_OPTION, SMILE_BUNDLE);
  //    final ForwardRateForexCalculator FWDC = ForwardRateForexCalculator.getInstance();
  //    final double fwdCalculator = FOREX_CALL_OPTION.accept(FWDC, SMILE_BUNDLE);
  //    assertEquals("Forex: forward rate", fwdMethod, fwdCalculator, TOLERANCE_RELATIVE);
  //  }

  @Test
  /**
   * Tests the relative delta for Forex option.
   */
  public void deltaRelativeDirect() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 1;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, payDate, notional, strike);
    final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionVanilla forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyAmount ce = METHOD_OPTION.currencyExposure(forexOption, SMILE_MULTICURVES);
    final double delta = METHOD_OPTION.deltaRelative(forexOption, SMILE_MULTICURVES, true);
    assertEquals("Forex: relative delta", ce.getAmount(EUR), delta, TOLERANCE_RELATIVE);
    final MultipleCurrencyAmount pvM = METHOD_OPTION.presentValue(forexOption, SMILE_M_MULTICURVES);
    final MultipleCurrencyAmount pvP = METHOD_OPTION.presentValue(forexOption, SMILE_P_MULTICURVES);
    final double deltaFlat = METHOD_OPTION.deltaRelative(forexOption, SMILE_FLAT_MULTICURVES, true);
    assertEquals("Forex: relative delta", (pvP.getAmount(USD) - pvM.getAmount(USD)) / (2 * SHIFT), deltaFlat, TOLERANCE_RELATIVE);
  }

  @Test
  /**
   * Tests the relative delta for Forex option.
   */
  public void deltaRelativeReverse() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 1;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(USD, EUR, payDate, notional, 1.0 / strike);
    final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionVanilla forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyAmount pvM = METHOD_OPTION.presentValue(forexOption, SMILE_M_MULTICURVES);
    final MultipleCurrencyAmount pvP = METHOD_OPTION.presentValue(forexOption, SMILE_P_MULTICURVES);
    final double delta = METHOD_OPTION.deltaRelative(forexOption, SMILE_FLAT_MULTICURVES, false);
    assertEquals("Forex: relative gamma", (pvP.getAmount(EUR) - pvM.getAmount(EUR)) / (2 * SHIFT), delta, TOLERANCE_RELATIVE);
  }

  @Test
  /**
   * Tests the relative delta for Forex option.
   */
  public void deltaRelativeSpotDirect() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 1;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, payDate, notional, strike);
    final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionVanilla forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyAmount ce = METHOD_OPTION.currencyExposure(forexOption, SMILE_MULTICURVES);
    final double delta = METHOD_OPTION.deltaRelative(forexOption, SMILE_MULTICURVES, true);
    assertEquals("Forex: relative delta", ce.getAmount(EUR), delta, TOLERANCE_RELATIVE);
    final MultipleCurrencyAmount pvM = METHOD_OPTION.presentValue(forexOption, SMILE_M_MULTICURVES);
    final MultipleCurrencyAmount pvP = METHOD_OPTION.presentValue(forexOption, SMILE_P_MULTICURVES);
    final double deltaFlat = METHOD_OPTION.deltaRelativeSpot(forexOption, SMILE_FLAT_MULTICURVES, true);
    assertEquals("Forex: relative delta", (pvP.getAmount(USD) - pvM.getAmount(USD)) / (2 * SHIFT) * FX_MATRIX.getFxRate(EUR, USD), deltaFlat, TOLERANCE_RELATIVE);
  }

  @Test
  /**
   * Tests the relative delta for Forex option.
   */
  public void deltaRelativeSpotReverse() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 1;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(USD, EUR, payDate, notional, 1.0 / strike);
    final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionVanilla forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyAmount pvM = METHOD_OPTION.presentValue(forexOption, SMILE_M_MULTICURVES);
    final MultipleCurrencyAmount pvP = METHOD_OPTION.presentValue(forexOption, SMILE_P_MULTICURVES);
    final double delta = METHOD_OPTION.deltaRelativeSpot(forexOption, SMILE_FLAT_MULTICURVES, false);
    assertEquals("Forex: relative gamma", (pvP.getAmount(EUR) - pvM.getAmount(EUR)) / (2 * SHIFT / SPOT), delta, TOLERANCE_RELATIVE);
  }

  @Test
  /**
   * Tests the relative gamma for Forex option. Direct quote
   */
  public void gammaRelativeDirect() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 1;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, payDate, notional, strike);
    final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionVanilla forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyAmount pv = METHOD_OPTION.presentValue(forexOption, SMILE_FLAT_MULTICURVES);
    final MultipleCurrencyAmount pvM = METHOD_OPTION.presentValue(forexOption, SMILE_M_MULTICURVES);
    final MultipleCurrencyAmount pvP = METHOD_OPTION.presentValue(forexOption, SMILE_P_MULTICURVES);
    final double gamma = METHOD_OPTION.gammaRelative(forexOption, SMILE_FLAT_MULTICURVES, true);
    assertEquals("Forex: relative gamma", 1.0, (pvP.getAmount(USD) + pvM.getAmount(USD) - 2 * pv.getAmount(USD)) / (SHIFT * SHIFT) / gamma, 2.0E-4);
  }

  @Test
  /**
   * Tests the relative gamma for Forex option. Reverse quote
   */
  public void gammaRelativeReverse() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 1;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(USD, EUR, payDate, notional, 1.0 / strike);
    final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionVanilla forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyAmount pv = METHOD_OPTION.presentValue(forexOption, SMILE_FLAT_MULTICURVES);
    final MultipleCurrencyAmount pvM = METHOD_OPTION.presentValue(forexOption, SMILE_M_MULTICURVES);
    final MultipleCurrencyAmount pvP = METHOD_OPTION.presentValue(forexOption, SMILE_P_MULTICURVES);
    final double gamma = METHOD_OPTION.gammaRelative(forexOption, SMILE_FLAT_MULTICURVES, false);
    assertEquals("Forex: relative gamma", 1.0, (pvP.getAmount(EUR) + pvM.getAmount(EUR) - 2 * pv.getAmount(EUR)) / (SHIFT * SHIFT) / gamma, 1.0E-4);
  }

  @Test
  /**
   * Tests the relative gamma for Forex option. Direct quote
   */
  public void gammaRelativeSpotDirect() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 1;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, payDate, notional, strike);
    final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionVanilla forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE);
    final double deltaM = METHOD_OPTION.deltaRelative(forexOption, SMILE_M_MULTICURVES, true);
    final double deltaP = METHOD_OPTION.deltaRelative(forexOption, SMILE_P_MULTICURVES, true);
    final double gamma = METHOD_OPTION.gammaRelativeSpot(forexOption, SMILE_FLAT_MULTICURVES, true);
    assertEquals("Forex: relative gamma", gamma, (deltaP - deltaM) / (2 * SHIFT / SPOT), TOLERANCE_RELATIVE);
  }

  @Test
  /**
   * Tests the relative gamma for Forex option. Reverse quote
   */
  public void gammaRelativeSpotReverse() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 1;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(USD, EUR, payDate, notional, 1.0 / strike);
    final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionVanilla forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE);
    final double deltaM = METHOD_OPTION.deltaRelative(forexOption, SMILE_M_MULTICURVES, false);
    final double deltaP = METHOD_OPTION.deltaRelative(forexOption, SMILE_P_MULTICURVES, false);
    final double gamma = METHOD_OPTION.gammaRelativeSpot(forexOption, SMILE_FLAT_MULTICURVES, false);
    assertEquals("Forex: relative gamma", gamma, (deltaP - deltaM) / (2 * SHIFT / SPOT), TOLERANCE_RELATIVE);
  }

  @Test
  /**
   * Tests the gamma for Forex option.
   */
  public void gammaDirect() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, payDate, notional, strike);
    final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionVanilla forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE);
    final double gammaRelative = METHOD_OPTION.gammaRelative(forexOption, SMILE_MULTICURVES, true);
    final double gammaExpected = gammaRelative * notional;
    final CurrencyAmount gammaComputed = METHOD_OPTION.gamma(forexOption, SMILE_MULTICURVES, true);
    assertEquals("Forex: relative gamma", 1.0, gammaExpected / gammaComputed.getAmount(), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the gamma for Forex option.
   */
  public void gammaReverse() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(USD, EUR, payDate, notional, 1.0 / strike);
    final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionVanilla forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE);
    final double gammaRelative = METHOD_OPTION.gammaRelative(forexOption, SMILE_MULTICURVES, false);
    final double gammaExpected = gammaRelative * notional;
    final CurrencyAmount gammaComputed = METHOD_OPTION.gamma(forexOption, SMILE_MULTICURVES, false);
    assertEquals("Forex: relative gamma", 1.0, gammaExpected / gammaComputed.getAmount(), TOLERANCE_PV);
  }

  private static final PercentageGammaForexBlackSmileCalculator GSFBSC = PercentageGammaForexBlackSmileCalculator.getInstance();

  @Test
  /**
   * Tests the gamma for Forex option.
   */
  public void gammaSpotDirect() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, payDate, notional, strike);
    final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionVanilla forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE);
    final double gammaRelativeSpot = METHOD_OPTION.gammaRelativeSpot(forexOption, SMILE_MULTICURVES, true);
    final double gammaSpotExpected = gammaRelativeSpot * notional;
    final CurrencyAmount gammaSpotComputed = METHOD_OPTION.gammaSpot(forexOption, SMILE_MULTICURVES, true);
    assertEquals("Forex: relative gamma", 1.0, gammaSpotExpected / gammaSpotComputed.getAmount(), TOLERANCE_PV);
    assertEquals("Forex: relative gamma", 1.0, gammaSpotExpected / forexOption.accept(GSFBSC, SMILE_MULTICURVES).getAmount(), TOLERANCE_PV);
    final double gammaSpotExpected2 = METHOD_OPTION.gamma(forexOption, SMILE_MULTICURVES, true).getAmount() * SPOT;
    assertEquals("Forex: relative gamma", 1.0, gammaSpotExpected2 / gammaSpotComputed.getAmount(), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the gamma for Forex option.
   */
  public void gammaSpotReverse() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(USD, EUR, payDate, notional, 1.0 / strike);
    final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionVanilla forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE);
    final double gammaRelativeSpot = METHOD_OPTION.gammaRelativeSpot(forexOption, SMILE_MULTICURVES, false);
    final double gammaSpotExpected = gammaRelativeSpot * notional;
    final CurrencyAmount gammaSpotComputed = METHOD_OPTION.gammaSpot(forexOption, SMILE_MULTICURVES, false);
    assertEquals("Forex: relative gamma", 1.0, gammaSpotExpected / gammaSpotComputed.getAmount(), TOLERANCE_PV);
    final double gammaSpotExpected2 = METHOD_OPTION.gamma(forexOption, SMILE_MULTICURVES, false).getAmount() * SPOT;
    assertEquals("Forex: relative gamma", 1.0, gammaSpotExpected2 / gammaSpotComputed.getAmount(), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value curve sensitivity.
   */
  public void presentValueCurveSensitivity() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, payDate, notional, strike);
    final ForexOptionVanillaDefinition forexOptionDefinitionCall = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionVanilla forexOptionCall = forexOptionDefinitionCall.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyParameterSensitivity pvpsExact = PS_FBS_C.calculateSensitivity(forexOptionCall, SMILE_FLAT_MULTICURVES, SMILE_FLAT_MULTICURVES
        .getMulticurveProvider().getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsFD = PS_FBS_FDC.calculateSensitivity(forexOptionCall, SMILE_FLAT_MULTICURVES);
    AssertSensitivityObjects.assertEquals("SwaptionPhysicalFixedIborSABRMethod: presentValueCurveSensitivity ", pvpsExact, pvpsFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Test the present value curve sensitivity through the method and through the calculator.
   */
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final MultipleCurrencyMulticurveSensitivity pvcsMethod = METHOD_OPTION.presentValueCurveSensitivity(FOREX_CALL_OPTION, SMILE_MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvcsCalculator = FOREX_CALL_OPTION.accept(PVCSFBC, SMILE_MULTICURVES);
    assertEquals("Forex present value curve sensitivity: Method vs Calculator", pvcsMethod, pvcsCalculator);
  }

  @Test
  /**
   * Tests present value volatility sensitivity.
   */
  public void volatilitySensitivity() {
    final PresentValueForexBlackVolatilitySensitivity sensi = METHOD_OPTION.presentValueBlackVolatilitySensitivity(FOREX_CALL_OPTION, SMILE_MULTICURVES);
    final Pair<Currency, Currency> currencyPair = Pairs.of(EUR, USD);
    final DoublesPair point = DoublesPair.of(FOREX_CALL_OPTION.getTimeToExpiry(), STRIKE);
    assertEquals("Forex vanilla option: vega", currencyPair, sensi.getCurrencyPair());
    assertEquals("Forex vanilla option: vega size", 1, sensi.getVega().getMap().entrySet().size());
    assertTrue("Forex vanilla option: vega", sensi.getVega().getMap().containsKey(point));
    final double timeToExpiry = TimeCalculator.getTimeBetween(REFERENCE_DATE, OPTION_EXP_DATE);
    final double df = MULTICURVES.getDiscountFactor(USD, TimeCalculator.getTimeBetween(REFERENCE_DATE, OPTION_PAY_DATE));
    final double forward = SPOT * MULTICURVES.getDiscountFactor(EUR, TimeCalculator.getTimeBetween(REFERENCE_DATE, OPTION_PAY_DATE)) / df;
    final double volatility = SMILE_TERM.getVolatility(Triple.of(timeToExpiry, STRIKE, forward));
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, df, volatility);
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(FOREX_CALL_OPTION, dataBlack);
    assertEquals("Forex vanilla option: vega", priceAdjoint[2] * NOTIONAL, sensi.getVega().getMap().get(point));
    final ForexOptionVanillaDefinition optionShortDefinition = new ForexOptionVanillaDefinition(FOREX_DEFINITION, OPTION_EXP_DATE, IS_CALL, !IS_LONG);
    final ForexOptionVanilla optionShort = optionShortDefinition.toDerivative(REFERENCE_DATE);
    final PresentValueForexBlackVolatilitySensitivity sensiShort = METHOD_OPTION.presentValueBlackVolatilitySensitivity(optionShort, SMILE_MULTICURVES);
    assertEquals("Forex vanilla option: vega short", -sensi.getVega().getMap().get(point), sensiShort.getVega().getMap().get(point));
    // Put/call parity
    final ForexOptionVanillaDefinition optionShortPutDefinition = new ForexOptionVanillaDefinition(FOREX_DEFINITION, OPTION_EXP_DATE, !IS_CALL, !IS_LONG);
    final ForexOptionVanilla optionShortPut = optionShortPutDefinition.toDerivative(REFERENCE_DATE);
    final PresentValueForexBlackVolatilitySensitivity sensiShortPut = METHOD_OPTION.presentValueBlackVolatilitySensitivity(optionShortPut, SMILE_MULTICURVES);
    assertEquals("Forex vanilla option: vega short", sensiShortPut.getVega().getMap().get(point) + sensi.getVega().getMap().get(point), 0.0, 1.0E-2);
  }

  @Test
  /**
   * Test the present value curve sensitivity through the method and through the calculator.
   */
  public void volatilitySensitivityMethodVsCalculator() {
    final PresentValueForexBlackVolatilitySensitivity pvvsMethod = METHOD_OPTION.presentValueBlackVolatilitySensitivity(FOREX_CALL_OPTION, SMILE_MULTICURVES);
    final PresentValueForexBlackVolatilitySensitivity pvvsCalculator = FOREX_CALL_OPTION.accept(PVVSFBSC, SMILE_MULTICURVES);
    assertEquals("Forex present value curve sensitivity: Method vs Calculator", pvvsMethod, pvvsCalculator);
  }

  @Test
  /**
   * Tests a EUR/USD call vs a USD/EUR put.
   */
  public void volatilitySensitivityCallPut() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexEURUSDDefinition = new ForexDefinition(EUR, USD, payDate, notional, strike);
    final ForexDefinition forexUSDEURDefinition = new ForexDefinition(USD, EUR, payDate, -notional * strike, 1.0 / strike);
    final ForexOptionVanillaDefinition callEURUSDDefinition = new ForexOptionVanillaDefinition(forexEURUSDDefinition, expDate, isCall, isLong);
    final ForexOptionVanillaDefinition putUSDEURDefinition = new ForexOptionVanillaDefinition(forexUSDEURDefinition, expDate, isCall, isLong);
    final ForexOptionVanilla callEURUSD = callEURUSDDefinition.toDerivative(REFERENCE_DATE);
    final ForexOptionVanilla putUSDEUR = putUSDEURDefinition.toDerivative(REFERENCE_DATE);
    final PresentValueForexBlackVolatilitySensitivity vsCallEURUSD = METHOD_OPTION.presentValueBlackVolatilitySensitivity(callEURUSD, SMILE_MULTICURVES);
    final PresentValueForexBlackVolatilitySensitivity vsPutUSDEUR = METHOD_OPTION.presentValueBlackVolatilitySensitivity(putUSDEUR, SMILE_MULTICURVES);
    final DoublesPair point = DoublesPair.of(callEURUSD.getTimeToExpiry(), strike);
    assertEquals("Forex vanilla option: volatilityNode", vsCallEURUSD.getVega().getMap().get(point) / SPOT, vsPutUSDEUR.getVega().getMap().get(point), 1.0E-2);
  }

  @Test
  /**
   * Tests present value volatility node sensitivity.
   */
  public void volatilityNodeSensitivity() {
    final PresentValueForexBlackVolatilityNodeSensitivityDataBundle sensi = METHOD_OPTION
        .presentValueBlackVolatilityNodeSensitivity(FOREX_CALL_OPTION, SMILE_MULTICURVES);
    assertEquals("Forex vanilla option: vega node size", NB_EXP + 1, sensi.getVega().getData().length);
    assertEquals("Forex vanilla option: vega node size", NB_STRIKE, sensi.getVega().getData()[0].length);
    final Pair<Currency, Currency> currencyPair = Pairs.of(EUR, USD);
    assertEquals("Forex vanilla option: vega", currencyPair, sensi.getCurrencyPair());
    final PresentValueForexBlackVolatilitySensitivity pointSensitivity = METHOD_OPTION.presentValueBlackVolatilitySensitivity(FOREX_CALL_OPTION, SMILE_MULTICURVES);
    final double df = MULTICURVES.getDiscountFactor(USD, TimeCalculator.getTimeBetween(REFERENCE_DATE, OPTION_PAY_DATE));
    final double forward = SPOT * MULTICURVES.getDiscountFactor(EUR, TimeCalculator.getTimeBetween(REFERENCE_DATE, OPTION_PAY_DATE)) / df;
    final VolatilityAndBucketedSensitivities volAndSensitivities = SMILE_TERM.getVolatilityAndSensitivities(FOREX_CALL_OPTION.getTimeToExpiry(), STRIKE, forward);
    final double[][] nodeWeight = volAndSensitivities.getBucketedSensitivities();
    final DoublesPair point = DoublesPair.of(FOREX_CALL_OPTION.getTimeToExpiry(), STRIKE);
    for (int loopexp = 0; loopexp < NB_EXP; loopexp++) {
      for (int loopstrike = 0; loopstrike < NB_STRIKE; loopstrike++) {
        assertEquals("Forex vanilla option: vega node", nodeWeight[loopexp][loopstrike] * pointSensitivity.getVega().getMap().get(point),
            sensi.getVega().getData()[loopexp][loopstrike]);
      }
    }
  }

  @Test
  /**
   * Tests a EUR/USD call vs a USD/EUR put.
   */
  public void volatilityNodeCallPut() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexEURUSDDefinition = new ForexDefinition(EUR, USD, payDate, notional, strike);
    final ForexDefinition forexUSDEURDefinition = new ForexDefinition(USD, EUR, payDate, -notional * strike, 1.0 / strike);
    final ForexOptionVanillaDefinition callEURUSDDefinition = new ForexOptionVanillaDefinition(forexEURUSDDefinition, expDate, isCall, isLong);
    final ForexOptionVanillaDefinition putUSDEURDefinition = new ForexOptionVanillaDefinition(forexUSDEURDefinition, expDate, isCall, isLong);
    final ForexOptionVanilla callEURUSD = callEURUSDDefinition.toDerivative(REFERENCE_DATE);
    final ForexOptionVanilla putUSDEUR = putUSDEURDefinition.toDerivative(REFERENCE_DATE);
    final PresentValueForexBlackVolatilityNodeSensitivityDataBundle nsCallEURUSD = METHOD_OPTION
        .presentValueBlackVolatilityNodeSensitivity(callEURUSD, SMILE_MULTICURVES);
    final PresentValueForexBlackVolatilityNodeSensitivityDataBundle nsPutUSDEUR = METHOD_OPTION.presentValueBlackVolatilityNodeSensitivity(putUSDEUR, SMILE_MULTICURVES);
    for (int loopexp = 0; loopexp < nsCallEURUSD.getExpiries().getNumberOfElements(); loopexp++) {
      for (int loopdelta = 0; loopdelta < nsCallEURUSD.getDelta().getNumberOfElements(); loopdelta++) {
        assertEquals("Forex vanilla option: volatilityNode", nsCallEURUSD.getVega().getEntry(loopexp, loopdelta) / SPOT,
            nsPutUSDEUR.getVega().getEntry(loopexp, loopdelta), 1.0E-2);
      }
    }
  }

  @Test
  /**
   * Tests present value volatility quote sensitivity.
   */
  public void volatilityQuoteSensitivity() {
    final PresentValueForexBlackVolatilityNodeSensitivityDataBundle sensiStrike = METHOD_OPTION.presentValueBlackVolatilityNodeSensitivity(FOREX_CALL_OPTION,
        SMILE_MULTICURVES);
    final double[][] sensiQuote = METHOD_OPTION.presentValueBlackVolatilityNodeSensitivity(FOREX_CALL_OPTION, SMILE_MULTICURVES).quoteSensitivity().getVega();
    final double[][] sensiStrikeData = sensiStrike.getVega().getData();
    final double[] atm = new double[sensiQuote.length];
    for (int loopexp = 0; loopexp < sensiQuote.length; loopexp++) {
      for (int loopdelta = 0; loopdelta < DELTA.length; loopdelta++) {
        assertEquals("Forex vanilla option: vega quote - RR", sensiQuote[loopexp][1 + loopdelta], -0.5 * sensiStrikeData[loopexp][loopdelta] + 0.5
            * sensiStrikeData[loopexp][2 * DELTA.length - loopdelta], 1.0E-10);
        assertEquals("Forex vanilla option: vega quote - Strangle", sensiQuote[loopexp][DELTA.length + 1 + loopdelta], sensiStrikeData[loopexp][loopdelta]
            + sensiStrikeData[loopexp][2 * DELTA.length - loopdelta], 1.0E-10);
        atm[loopexp] += sensiStrikeData[loopexp][loopdelta] + sensiStrikeData[loopexp][2 * DELTA.length - loopdelta];
      }
      atm[loopexp] += sensiStrikeData[loopexp][DELTA.length];
      assertEquals("Forex vanilla option: vega quote", sensiQuote[loopexp][0], atm[loopexp], 1.0E-10); // ATM
    }
  }

  //    @Test
  //    /**
  //     * Tests the theoretical Theta (derivative with respect to time in Black formula).
  //     */
  //    public void thetaTheoretical() {
  //      final double strike = 1.45;
  //      final boolean isCall = true;
  //      final boolean isLong = true;
  //      final double notional = 100000000;
  //      final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
  //      final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
  //      final double timeToExpiry = TimeCalculator.getTimeBetween(REFERENCE_DATE, expDate);
  //      final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, payDate, notional, strike);
  //      final ForexOptionVanillaDefinition callDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
  //      final ForexOptionVanilla call = callDefinition.toDerivative(REFERENCE_DATE, NOT_USED_2);
  //      final double df = CURVES.getCurve(CURVES_NAME[1]).getDiscountFactor(TimeCalculator.getTimeBetween(REFERENCE_DATE, payDate));
  //      final double forward = SPOT * CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(TimeCalculator.getTimeBetween(REFERENCE_DATE, payDate)) / df;
  //      final double volatility = SMILE_TERM.getVolatility(timeToExpiry, strike, forward);
  //      final double thetaUnit = BlackFormulaRepository.theta(forward, strike, timeToExpiry, volatility);
  //      final double thetaExpected = thetaUnit * notional;
  //      final CurrencyAmount thetaCallComputed = METHOD_OPTION.thetaTheoretical(call, SMILE_MULTICURVES);
  //      assertEquals("Theta theoretical: forex option", thetaExpected, thetaCallComputed.getAmount(), TOLERANCE_PV);
  //      assertEquals("Theta theoretical: forex option", USD, thetaCallComputed.getCurrency());
  //      final ForexOptionVanillaDefinition putDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, !isCall, isLong);
  //      final ForexOptionVanilla put = putDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
  //      final CurrencyAmount thetaPutComputed = METHOD_OPTION.thetaTheoretical(put, SMILE_MULTICURVES);
  //      assertEquals("Theta theoretical: forex option", thetaCallComputed.getAmount(), thetaPutComputed.getAmount(), TOLERANCE_PV);
  //    }

}
