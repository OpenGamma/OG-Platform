/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.method;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.testng.internal.junit.ArrayAsserts;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.calculator.PresentValueCurveSensitivityConvertedCurveCurrencyCalculator;
import com.opengamma.analytics.financial.forex.calculator.CurrencyExposureBlackSmileForexCalculator;
import com.opengamma.analytics.financial.forex.calculator.ForwardRateForexCalculator;
import com.opengamma.analytics.financial.forex.calculator.GammaSpotBlackForexCalculator;
import com.opengamma.analytics.financial.forex.calculator.GammaValueBlackForexCalculator;
import com.opengamma.analytics.financial.forex.calculator.PresentValueBlackSmileForexCalculator;
import com.opengamma.analytics.financial.forex.calculator.PresentValueBlackVolatilityQuoteSensitivityForexCalculator;
import com.opengamma.analytics.financial.forex.calculator.PresentValueBlackVolatilitySensitivityBlackForexCalculator;
import com.opengamma.analytics.financial.forex.calculator.PresentValueCurveSensitivityBlackSmileForexCalculator;
import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.horizon.ConstantSpreadFXOptionBlackRolldown;
import com.opengamma.analytics.financial.horizon.ConstantSpreadHorizonThetaCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivityUtils;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.VolatilityAndBucketedSensitivities;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
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
 * @deprecated This class tests deprecated code
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class ForexOptionVanillaBlackSmileMethodTest {
  // General
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final int SETTLEMENT_DAYS = 2;
  // Smile data
  private static final Currency EUR = Currency.EUR;
  private static final Currency USD = Currency.USD;
  private static final Period[] EXPIRY_PERIOD = new Period[] {Period.ofMonths(3), Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2), Period.ofYears(5) };
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
  private static final double SPOT = 1.40;
  private static final double[] ATM = {0.175, 0.185, 0.18, 0.17, 0.16, 0.16 };
  private static final double[] DELTA = new double[] {0.10, 0.25 };
  private static final double[][] RISK_REVERSAL = new double[][] { {-0.010, -0.0050 }, {-0.011, -0.0060 }, {-0.012, -0.0070 }, {-0.013, -0.0080 }, {-0.014, -0.0090 }, {-0.014, -0.0090 } };
  private static final double[][] STRANGLE = new double[][] { {0.0300, 0.0100 }, {0.0310, 0.0110 }, {0.0320, 0.0120 }, {0.0330, 0.0130 }, {0.0340, 0.0140 }, {0.0340, 0.0140 } };
  private static final int NB_STRIKE = 2 * DELTA.length + 1;
  private static final SmileDeltaTermStructureParametersStrikeInterpolation SMILE_TERM = new SmileDeltaTermStructureParametersStrikeInterpolation(TIME_TO_EXPIRY, DELTA, ATM, RISK_REVERSAL, STRANGLE);
  private static final SmileDeltaTermStructureParametersStrikeInterpolation SMILE_TERM_FLAT = TestsDataSetsForex.smileFlat(REFERENCE_DATE);
  // Methods and curves
  private static final YieldCurveBundle CURVES = TestsDataSetsForex.createCurvesForex();
  private static final String[] CURVES_NAME = TestsDataSetsForex.curveNames();
  private static final SmileDeltaTermStructureDataBundle SMILE_BUNDLE = new SmileDeltaTermStructureDataBundle(CURVES, SMILE_TERM, Pairs.of(EUR, USD));
  private static final SmileDeltaTermStructureDataBundle SMILE_BUNDLE_FLAT = new SmileDeltaTermStructureDataBundle(CURVES, SMILE_TERM_FLAT, Pairs.of(EUR, USD));
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();
  private static final ForexOptionVanillaBlackSmileMethod METHOD_OPTION = ForexOptionVanillaBlackSmileMethod.getInstance();
  private static final ForexDiscountingMethod METHOD_DISC = ForexDiscountingMethod.getInstance();
  private static final PresentValueBlackSmileForexCalculator PVC_BLACK = PresentValueBlackSmileForexCalculator.getInstance();
  private static final CurrencyExposureBlackSmileForexCalculator CEC_BLACK = CurrencyExposureBlackSmileForexCalculator.getInstance();
  private static final PresentValueCurveSensitivityBlackSmileForexCalculator PVCSC_BLACK = PresentValueCurveSensitivityBlackSmileForexCalculator.getInstance();
  private static final PresentValueCurveSensitivityConvertedCurveCurrencyCalculator PVCSCC_BLACK = new PresentValueCurveSensitivityConvertedCurveCurrencyCalculator(PVCSC_BLACK);
  private static final PresentValueBlackVolatilitySensitivityBlackForexCalculator PVVSC_BLACK = PresentValueBlackVolatilitySensitivityBlackForexCalculator.getInstance();
  // option
  private static final double STRIKE = 1.45;
  private static final boolean IS_CALL = true;
  private static final boolean IS_LONG = true;
  private static final double NOTIONAL = 100000000;
  private static final ZonedDateTime OPTION_PAY_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
  private static final ZonedDateTime OPTION_EXP_DATE = ScheduleCalculator.getAdjustedDate(OPTION_PAY_DATE, -SETTLEMENT_DAYS, CALENDAR);
  private static final ForexDefinition FOREX_DEFINITION = new ForexDefinition(EUR, USD, OPTION_PAY_DATE, NOTIONAL, STRIKE);
  private static final ForexOptionVanillaDefinition FOREX_OPTION_CALL_DEFINITION = new ForexOptionVanillaDefinition(FOREX_DEFINITION, OPTION_EXP_DATE, IS_CALL, IS_LONG);
  private static final ForexOptionVanilla FOREX_CALL_OPTION = FOREX_OPTION_CALL_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final ConstantSpreadHorizonThetaCalculator THETAC = ConstantSpreadHorizonThetaCalculator.getInstance();
  private static final ConstantSpreadFXOptionBlackRolldown FX_OPTION_ROLLDOWN = ConstantSpreadFXOptionBlackRolldown.getInstance();

  private static final double TOLERANCE_RELATIVE = 1.0E-9;
  private static final double TOLERANCE_PV = 1.0E-2;

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
    final ForexOptionVanilla forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final double df = CURVES.getCurve(CURVES_NAME[1]).getDiscountFactor(TimeCalculator.getTimeBetween(REFERENCE_DATE, PAY_DATE[indexPay]));
    final double forward = SPOT * CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(TimeCalculator.getTimeBetween(REFERENCE_DATE, PAY_DATE[indexPay])) / df;
    final double volatility = SMILE_TERM.getVolatility(new Triple<>(TIME_TO_EXPIRY[indexPay + 1], strike, forward));
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, df, volatility);
    final Function1D<BlackFunctionData, Double> func = BLACK_FUNCTION.getPriceFunction(forexOption);
    final double priceExpected = func.evaluate(dataBlack) * notional;
    final MultipleCurrencyAmount priceComputed = METHOD_OPTION.presentValue(forexOption, SMILE_BUNDLE);
    assertEquals("Forex vanilla option: present value", priceExpected, priceComputed.getAmount(USD), 1E-2);
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
    final ForexOptionVanilla forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final double df = CURVES.getCurve(CURVES_NAME[1]).getDiscountFactor(TimeCalculator.getTimeBetween(REFERENCE_DATE, payDate));
    final double forward = SPOT * CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(TimeCalculator.getTimeBetween(REFERENCE_DATE, payDate)) / df;
    final double volatility = SMILE_TERM.getVolatility(new Triple<>(timeToExpiry, strike, forward));
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, df, volatility);
    final Function1D<BlackFunctionData, Double> func = BLACK_FUNCTION.getPriceFunction(forexOption);
    final double priceExpected = func.evaluate(dataBlack) * notional;
    final MultipleCurrencyAmount priceComputed = METHOD_OPTION.presentValue(forexOption, SMILE_BUNDLE);
    assertEquals("Forex vanilla option: present value", priceExpected, priceComputed.getAmount(USD), 1E-2);
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
    final InstrumentDerivative callEURUSD = callEURUSDDefinition.toDerivative(REFERENCE_DATE, new String[] {CURVES_NAME[0], CURVES_NAME[1] });
    final InstrumentDerivative putUSDEUR = putUSDEURDefinition.toDerivative(REFERENCE_DATE, new String[] {CURVES_NAME[1], CURVES_NAME[0] });
    final MultipleCurrencyAmount pvCallEURUSD = METHOD_OPTION.presentValue(callEURUSD, SMILE_BUNDLE);
    final MultipleCurrencyAmount pvPutUSDEUR = METHOD_OPTION.presentValue(putUSDEUR, SMILE_BUNDLE);
    assertEquals("Forex vanilla option: present value Method vs Calculator", pvCallEURUSD.getAmount(USD) / SPOT, pvPutUSDEUR.getAmount(EUR), 1E-2);
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
    final InstrumentDerivative forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final MultipleCurrencyAmount pvMethod = METHOD_OPTION.presentValue(forexOption, SMILE_BUNDLE);
    final MultipleCurrencyAmount pvCalculator = forexOption.accept(PVC_BLACK, SMILE_BUNDLE);
    assertEquals("Forex vanilla option: present value Method vs Calculator", pvMethod.getAmount(USD), pvCalculator.getAmount(USD), 1E-2);
  }

  @Test
  /**
   * Tests the present value long/short parity.
   */
  public void presentValueLongShort() {
    final ForexOptionVanillaDefinition forexOptionShortDefinition = new ForexOptionVanillaDefinition(FOREX_DEFINITION, OPTION_EXP_DATE, IS_CALL, !IS_LONG);
    final InstrumentDerivative forexOptionShort = forexOptionShortDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final MultipleCurrencyAmount pvShort = METHOD_OPTION.presentValue(forexOptionShort, SMILE_BUNDLE);
    final MultipleCurrencyAmount pvLong = METHOD_OPTION.presentValue(FOREX_CALL_OPTION, SMILE_BUNDLE);
    assertEquals("Forex vanilla option: present value long/short parity", pvLong.getAmount(USD), -pvShort.getAmount(USD), 1E-2);
    final MultipleCurrencyAmount ceShort = METHOD_OPTION.currencyExposure(forexOptionShort, SMILE_BUNDLE);
    final MultipleCurrencyAmount ceLong = METHOD_OPTION.currencyExposure(FOREX_CALL_OPTION, SMILE_BUNDLE);
    assertEquals("Forex vanilla option: currency exposure long/short parity", ceLong.getAmount(USD), -ceShort.getAmount(USD), 1E-2);
    assertEquals("Forex vanilla option: currency exposure long/short parity", ceLong.getAmount(EUR), -ceShort.getAmount(EUR), 1E-2);
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
    final ForexOptionVanilla forexOptionCall = forexOptionDefinitionCall.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final ForexOptionVanilla forexOptionPut = forexOptionDefinitionPut.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final double dfDomestic = CURVES.getCurve(CURVES_NAME[1]).getDiscountFactor(TimeCalculator.getTimeBetween(REFERENCE_DATE, payDate)); // USD
    final double dfForeign = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(TimeCalculator.getTimeBetween(REFERENCE_DATE, payDate)); // EUR
    final double forward = SPOT * CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(TimeCalculator.getTimeBetween(REFERENCE_DATE, payDate)) / dfDomestic;
    final double volatility = SMILE_TERM.getVolatility(new Triple<>(timeToExpiry, strike, forward));
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, dfDomestic, volatility);
    final double[] priceAdjointCall = BLACK_FUNCTION.getPriceAdjoint(forexOptionCall, dataBlack);
    final double[] priceAdjointPut = BLACK_FUNCTION.getPriceAdjoint(forexOptionPut, dataBlack);
    final double deltaForwardCall = priceAdjointCall[1];
    final double deltaForwardPut = priceAdjointPut[1];
    final double deltaSpotCall = deltaForwardCall * dfForeign / dfDomestic;
    final double deltaSpotPut = deltaForwardPut * dfForeign / dfDomestic;
    final MultipleCurrencyAmount priceComputedCall = METHOD_OPTION.presentValue(forexOptionCall, SMILE_BUNDLE);
    final MultipleCurrencyAmount priceComputedPut = METHOD_OPTION.presentValue(forexOptionPut, SMILE_BUNDLE);
    final MultipleCurrencyAmount currencyExposureCallComputed = METHOD_OPTION.currencyExposure(forexOptionCall, SMILE_BUNDLE);
    assertEquals("Forex vanilla option: currency exposure foreign - call", deltaSpotCall * notional, currencyExposureCallComputed.getAmount(EUR), 1E-2);
    assertEquals("Forex vanilla option: currency exposure domestic - call", -deltaSpotCall * notional * SPOT + priceComputedCall.getAmount(USD), currencyExposureCallComputed.getAmount(USD), 1E-2);
    final MultipleCurrencyAmount currencyExposurePutComputed = METHOD_OPTION.currencyExposure(forexOptionPut, SMILE_BUNDLE);
    assertEquals("Forex vanilla option: currency exposure foreign- put", deltaSpotPut * notional, currencyExposurePutComputed.getAmount(EUR), 1E-2);
    assertEquals("Forex vanilla option: currency exposure domestic - put", -deltaSpotPut * notional * SPOT + priceComputedPut.getAmount(USD), currencyExposurePutComputed.getAmount(USD), 1E-2);
  }

  @Test
  /**
   * Tests the currency exposure against the present value.
   */
  public void currencyExposureVsPresentValue() {
    final MultipleCurrencyAmount pv = METHOD_OPTION.presentValue(FOREX_CALL_OPTION, SMILE_BUNDLE);
    final MultipleCurrencyAmount ce = METHOD_OPTION.currencyExposure(FOREX_CALL_OPTION, SMILE_BUNDLE);
    assertEquals("Forex vanilla option: currency exposure vs present value", ce.getAmount(USD) + ce.getAmount(EUR) * SPOT, pv.getAmount(USD), 1E-2);
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
    final InstrumentDerivative callEURUSD = callEURUSDDefinition.toDerivative(REFERENCE_DATE, new String[] {CURVES_NAME[0], CURVES_NAME[1] });
    final InstrumentDerivative putUSDEUR = putUSDEURDefinition.toDerivative(REFERENCE_DATE, new String[] {CURVES_NAME[1], CURVES_NAME[0] });
    final MultipleCurrencyAmount pvCallEURUSD = METHOD_OPTION.currencyExposure(callEURUSD, SMILE_BUNDLE);
    final MultipleCurrencyAmount pvPutUSDEUR = METHOD_OPTION.currencyExposure(putUSDEUR, SMILE_BUNDLE);
    assertEquals("Forex vanilla option: currency exposure", pvCallEURUSD.getAmount(EUR), pvPutUSDEUR.getAmount(EUR), 1.0E-2);
    assertEquals("Forex vanilla option: currency exposure", pvCallEURUSD.getAmount(USD), pvPutUSDEUR.getAmount(USD), 1.0E-2);
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
    final ForexOptionVanilla forexOptionCall = forexOptionDefinitionCall.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final ForexOptionVanilla forexOptionPut = forexOptionDefinitionPut.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final Forex forexForward = forexUnderlyingDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final MultipleCurrencyAmount currencyExposureCall = METHOD_OPTION.currencyExposure(forexOptionCall, SMILE_BUNDLE);
    final MultipleCurrencyAmount currencyExposurePut = METHOD_OPTION.currencyExposure(forexOptionPut, SMILE_BUNDLE);
    final MultipleCurrencyAmount currencyExposureForward = METHOD_DISC.currencyExposure(forexForward, SMILE_BUNDLE);
    assertEquals("Forex vanilla option: currency exposure put/call parity foreign", currencyExposureForward.getAmount(EUR), currencyExposureCall.getAmount(EUR) - currencyExposurePut.getAmount(EUR),
        1E-2);
    assertEquals("Forex vanilla option: currency exposure put/call parity domestic", currencyExposureForward.getAmount(USD), currencyExposureCall.getAmount(USD) - currencyExposurePut.getAmount(USD),
        1E-2);
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
    final InstrumentDerivative forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final MultipleCurrencyAmount ceMethod = METHOD_OPTION.currencyExposure(forexOption, SMILE_BUNDLE);
    final MultipleCurrencyAmount ceCalculator = forexOption.accept(CEC_BLACK, SMILE_BUNDLE);
    assertEquals("Forex vanilla option: currency exposure Method vs Calculator", ceMethod.getAmount(EUR), ceCalculator.getAmount(EUR), 1E-2);
    assertEquals("Forex vanilla option: currency exposure Method vs Calculator", ceMethod.getAmount(USD), ceCalculator.getAmount(USD), 1E-2);
  }

  @Test
  /**
   * Tests forward Forex rate.
   */
  public void forwardForexRate() {
    final double fwd = METHOD_OPTION.forwardForexRate(FOREX_CALL_OPTION, SMILE_BUNDLE);
    final double fwdExpected = METHOD_DISC.forwardForexRate(FOREX_CALL_OPTION.getUnderlyingForex(), SMILE_BUNDLE);
    assertEquals("Forex vanilla option: forward forex rate", fwd, fwdExpected, TOLERANCE_RELATIVE);
  }

  @Test
  /**
   * Tests the forward Forex rate through the method and through the calculator.
   */
  public void forwardRateMethodVsCalculator() {
    final double fwdMethod = METHOD_OPTION.forwardForexRate(FOREX_CALL_OPTION, SMILE_BUNDLE);
    final ForwardRateForexCalculator FWDC = ForwardRateForexCalculator.getInstance();
    final double fwdCalculator = FOREX_CALL_OPTION.accept(FWDC, SMILE_BUNDLE);
    assertEquals("Forex: forward rate", fwdMethod, fwdCalculator, TOLERANCE_RELATIVE);
  }

  @Test
  /**
   * Tests the relative delta for Forex option.
   */
  public void deltaRelativeDirect() {
    final double shift = 1.0E-6;
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 1;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, payDate, notional, strike);
    final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionVanilla forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final MultipleCurrencyAmount ce = METHOD_OPTION.currencyExposure(forexOption, SMILE_BUNDLE);
    final double delta = METHOD_OPTION.deltaRelative(forexOption, SMILE_BUNDLE, true);
    assertEquals("Forex: relative delta", ce.getAmount(EUR), delta, TOLERANCE_RELATIVE);
    final FXMatrix fxMatrixM = new FXMatrix(EUR, USD, SPOT - shift);
    final FXMatrix fxMatrixP = new FXMatrix(EUR, USD, SPOT + shift);
    final YieldCurveBundle fxDown = new YieldCurveBundle(fxMatrixM, CURVES.getCurrencyMap(), CURVES.getCurvesMap());
    final YieldCurveBundle fxUp = new YieldCurveBundle(fxMatrixP, CURVES.getCurrencyMap(), CURVES.getCurvesMap());
    final SmileDeltaTermStructureDataBundle smileBundleM = new SmileDeltaTermStructureDataBundle(fxDown, SMILE_TERM_FLAT, Pairs.of(EUR, USD));
    final SmileDeltaTermStructureDataBundle smileBundleP = new SmileDeltaTermStructureDataBundle(fxUp, SMILE_TERM_FLAT, Pairs.of(EUR, USD));
    final MultipleCurrencyAmount pvM = METHOD_OPTION.presentValue(forexOption, smileBundleM);
    final MultipleCurrencyAmount pvP = METHOD_OPTION.presentValue(forexOption, smileBundleP);
    final double deltaFlat = METHOD_OPTION.deltaRelative(forexOption, SMILE_BUNDLE_FLAT, true);
    assertEquals("Forex: relative delta", (pvP.getAmount(USD) - pvM.getAmount(USD)) / (2 * shift), deltaFlat, TOLERANCE_RELATIVE);
  }

  @Test
  /**
   * Tests the relative delta for Forex option.
   */
  public void deltaRelativeReverse() {
    final double shift = 1.0E-6;
    final FXMatrix fxMatrixM = new FXMatrix(EUR, USD, SPOT - shift);
    final FXMatrix fxMatrixP = new FXMatrix(EUR, USD, SPOT + shift);
    final YieldCurveBundle fxDown = new YieldCurveBundle(fxMatrixM, CURVES.getCurrencyMap(), CURVES.getCurvesMap());
    final YieldCurveBundle fxUp = new YieldCurveBundle(fxMatrixP, CURVES.getCurrencyMap(), CURVES.getCurvesMap());
    final SmileDeltaTermStructureDataBundle smileBundleM = new SmileDeltaTermStructureDataBundle(fxDown, SMILE_TERM_FLAT, Pairs.of(EUR, USD));
    final SmileDeltaTermStructureDataBundle smileBundleP = new SmileDeltaTermStructureDataBundle(fxUp, SMILE_TERM_FLAT, Pairs.of(EUR, USD));
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 1;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(USD, EUR, payDate, notional, 1.0 / strike);
    final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionVanilla forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final MultipleCurrencyAmount pvM = METHOD_OPTION.presentValue(forexOption, smileBundleM);
    final MultipleCurrencyAmount pvP = METHOD_OPTION.presentValue(forexOption, smileBundleP);
    final double delta = METHOD_OPTION.deltaRelative(forexOption, SMILE_BUNDLE_FLAT, false);
    assertEquals("Forex: relative gamma", (pvP.getAmount(EUR) - pvM.getAmount(EUR)) / (2 * shift), delta, TOLERANCE_RELATIVE);
  }

  @Test
  /**
   * Tests the relative delta for Forex option.
   */
  public void deltaRelativeSpotDirect() {
    final double shift = 1.0E-6;
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 1;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, payDate, notional, strike);
    final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionVanilla forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final MultipleCurrencyAmount ce = METHOD_OPTION.currencyExposure(forexOption, SMILE_BUNDLE);
    final double delta = METHOD_OPTION.deltaRelative(forexOption, SMILE_BUNDLE, true);
    assertEquals("Forex: relative delta", ce.getAmount(EUR), delta, TOLERANCE_RELATIVE);
    final FXMatrix fxMatrixM = new FXMatrix(EUR, USD, SPOT * (1 - shift));
    final FXMatrix fxMatrixP = new FXMatrix(EUR, USD, SPOT * (1 + shift));
    final YieldCurveBundle fxDown = new YieldCurveBundle(fxMatrixM, CURVES.getCurrencyMap(), CURVES.getCurvesMap());
    final YieldCurveBundle fxUp = new YieldCurveBundle(fxMatrixP, CURVES.getCurrencyMap(), CURVES.getCurvesMap());
    final SmileDeltaTermStructureDataBundle smileBundleM = new SmileDeltaTermStructureDataBundle(fxDown, SMILE_TERM_FLAT, Pairs.of(EUR, USD));
    final SmileDeltaTermStructureDataBundle smileBundleP = new SmileDeltaTermStructureDataBundle(fxUp, SMILE_TERM_FLAT, Pairs.of(EUR, USD));
    final MultipleCurrencyAmount pvM = METHOD_OPTION.presentValue(forexOption, smileBundleM);
    final MultipleCurrencyAmount pvP = METHOD_OPTION.presentValue(forexOption, smileBundleP);
    final double deltaFlat = METHOD_OPTION.deltaRelativeSpot(forexOption, SMILE_BUNDLE_FLAT, true);
    assertEquals("Forex: relative delta", (pvP.getAmount(USD) - pvM.getAmount(USD)) / (2 * shift), deltaFlat, TOLERANCE_RELATIVE);
  }

  @Test
  /**
   * Tests the relative delta for Forex option.
   */
  public void deltaRelativeSpotReverse() {
    final double shift = 1.0E-6;
    final FXMatrix fxMatrixM = new FXMatrix(EUR, USD, SPOT * (1 - shift));
    final FXMatrix fxMatrixP = new FXMatrix(EUR, USD, SPOT * (1 + shift));
    final YieldCurveBundle fxDown = new YieldCurveBundle(fxMatrixM, CURVES.getCurrencyMap(), CURVES.getCurvesMap());
    final YieldCurveBundle fxUp = new YieldCurveBundle(fxMatrixP, CURVES.getCurrencyMap(), CURVES.getCurvesMap());
    final SmileDeltaTermStructureDataBundle smileBundleM = new SmileDeltaTermStructureDataBundle(fxDown, SMILE_TERM_FLAT, Pairs.of(EUR, USD));
    final SmileDeltaTermStructureDataBundle smileBundleP = new SmileDeltaTermStructureDataBundle(fxUp, SMILE_TERM_FLAT, Pairs.of(EUR, USD));
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 1;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(USD, EUR, payDate, notional, 1.0 / strike);
    final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionVanilla forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final MultipleCurrencyAmount pvM = METHOD_OPTION.presentValue(forexOption, smileBundleM);
    final MultipleCurrencyAmount pvP = METHOD_OPTION.presentValue(forexOption, smileBundleP);
    final double delta = METHOD_OPTION.deltaRelativeSpot(forexOption, SMILE_BUNDLE_FLAT, false);
    assertEquals("Forex: relative gamma", (pvP.getAmount(EUR) - pvM.getAmount(EUR)) / (2 * shift), delta, TOLERANCE_RELATIVE);
  }

  @Test
  /**
   * Tests the relative gamma for Forex option. Direct quote
   */
  public void gammaRelativeDirect() {
    final double shift = 1.0E-6;
    final FXMatrix fxMatrixM = new FXMatrix(EUR, USD, SPOT - shift);
    final FXMatrix fxMatrixP = new FXMatrix(EUR, USD, SPOT + shift);
    final YieldCurveBundle fxDown = new YieldCurveBundle(fxMatrixM, CURVES.getCurrencyMap(), CURVES.getCurvesMap());
    final YieldCurveBundle fxUp = new YieldCurveBundle(fxMatrixP, CURVES.getCurrencyMap(), CURVES.getCurvesMap());
    final SmileDeltaTermStructureDataBundle smileBundleM = new SmileDeltaTermStructureDataBundle(fxDown, SMILE_TERM_FLAT, Pairs.of(EUR, USD));
    final SmileDeltaTermStructureDataBundle smileBundleP = new SmileDeltaTermStructureDataBundle(fxUp, SMILE_TERM_FLAT, Pairs.of(EUR, USD));
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 1;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, payDate, notional, strike);
    final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionVanilla forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final MultipleCurrencyAmount pv = METHOD_OPTION.presentValue(forexOption, SMILE_BUNDLE_FLAT);
    final MultipleCurrencyAmount pvM = METHOD_OPTION.presentValue(forexOption, smileBundleM);
    final MultipleCurrencyAmount pvP = METHOD_OPTION.presentValue(forexOption, smileBundleP);
    final double gamma = METHOD_OPTION.gammaRelative(forexOption, SMILE_BUNDLE_FLAT, true);
    assertEquals("Forex: relative gamma", 1.0, (pvP.getAmount(USD) + pvM.getAmount(USD) - 2 * pv.getAmount(USD)) / (shift * shift) / gamma, 2.0E-4);
  }

  @Test
  /**
   * Tests the relative gamma for Forex option. Reverse quote
   */
  public void gammaRelativeReverse() {
    final double shift = 1.0E-6;
    final FXMatrix fxMatrixM = new FXMatrix(EUR, USD, SPOT - shift);
    final FXMatrix fxMatrixP = new FXMatrix(EUR, USD, SPOT + shift);
    final YieldCurveBundle fxDown = new YieldCurveBundle(fxMatrixM, CURVES.getCurrencyMap(), CURVES.getCurvesMap());
    final YieldCurveBundle fxUp = new YieldCurveBundle(fxMatrixP, CURVES.getCurrencyMap(), CURVES.getCurvesMap());
    final SmileDeltaTermStructureDataBundle smileBundleM = new SmileDeltaTermStructureDataBundle(fxDown, SMILE_TERM_FLAT, Pairs.of(EUR, USD));
    final SmileDeltaTermStructureDataBundle smileBundleP = new SmileDeltaTermStructureDataBundle(fxUp, SMILE_TERM_FLAT, Pairs.of(EUR, USD));
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 1;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(USD, EUR, payDate, notional, 1.0 / strike);
    final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionVanilla forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final MultipleCurrencyAmount pv = METHOD_OPTION.presentValue(forexOption, SMILE_BUNDLE_FLAT);
    final MultipleCurrencyAmount pvM = METHOD_OPTION.presentValue(forexOption, smileBundleM);
    final MultipleCurrencyAmount pvP = METHOD_OPTION.presentValue(forexOption, smileBundleP);
    final double gamma = METHOD_OPTION.gammaRelative(forexOption, SMILE_BUNDLE_FLAT, false);
    assertEquals("Forex: relative gamma", 1.0, (pvP.getAmount(EUR) + pvM.getAmount(EUR) - 2 * pv.getAmount(EUR)) / (shift * shift) / gamma, 1.0E-4);
  }

  @Test
  /**
   * Tests the relative gamma for Forex option. Direct quote
   */
  public void gammaRelativeSpotDirect() {
    final double shift = 1.0E-6;
    final FXMatrix fxMatrixM = new FXMatrix(EUR, USD, SPOT * (1 - shift));
    final FXMatrix fxMatrixP = new FXMatrix(EUR, USD, SPOT * (1 + shift));
    final YieldCurveBundle fxDown = new YieldCurveBundle(fxMatrixM, CURVES.getCurrencyMap(), CURVES.getCurvesMap());
    final YieldCurveBundle fxUp = new YieldCurveBundle(fxMatrixP, CURVES.getCurrencyMap(), CURVES.getCurvesMap());
    final SmileDeltaTermStructureDataBundle smileBundleM = new SmileDeltaTermStructureDataBundle(fxDown, SMILE_TERM_FLAT, Pairs.of(EUR, USD));
    final SmileDeltaTermStructureDataBundle smileBundleP = new SmileDeltaTermStructureDataBundle(fxUp, SMILE_TERM_FLAT, Pairs.of(EUR, USD));
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 1;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, payDate, notional, strike);
    final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionVanilla forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final double deltaM = METHOD_OPTION.deltaRelative(forexOption, smileBundleM, true);
    final double deltaP = METHOD_OPTION.deltaRelative(forexOption, smileBundleP, true);
    final double gamma = METHOD_OPTION.gammaRelativeSpot(forexOption, SMILE_BUNDLE_FLAT, true);
    assertEquals("Forex: relative gamma", gamma, (deltaP - deltaM) / (2 * shift), TOLERANCE_RELATIVE);
  }

  @Test
  /**
   * Tests the relative gamma for Forex option. Reverse quote
   */
  public void gammaRelativeSpotReverse() {
    final double shift = 1.0E-6;
    final FXMatrix fxMatrixM = new FXMatrix(EUR, USD, SPOT * (1 - shift));
    final FXMatrix fxMatrixP = new FXMatrix(EUR, USD, SPOT * (1 + shift));
    final YieldCurveBundle fxDown = new YieldCurveBundle(fxMatrixM, CURVES.getCurrencyMap(), CURVES.getCurvesMap());
    final YieldCurveBundle fxUp = new YieldCurveBundle(fxMatrixP, CURVES.getCurrencyMap(), CURVES.getCurvesMap());
    final SmileDeltaTermStructureDataBundle smileBundleM = new SmileDeltaTermStructureDataBundle(fxDown, SMILE_TERM_FLAT, Pairs.of(EUR, USD));
    final SmileDeltaTermStructureDataBundle smileBundleP = new SmileDeltaTermStructureDataBundle(fxUp, SMILE_TERM_FLAT, Pairs.of(EUR, USD));
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 1;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(USD, EUR, payDate, notional, 1.0 / strike);
    final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionVanilla forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final double deltaM = METHOD_OPTION.deltaRelative(forexOption, smileBundleM, false);
    final double deltaP = METHOD_OPTION.deltaRelative(forexOption, smileBundleP, false);
    final double gamma = METHOD_OPTION.gammaRelativeSpot(forexOption, SMILE_BUNDLE_FLAT, false);
    assertEquals("Forex: relative gamma", gamma, (deltaP - deltaM) / (2 * shift), TOLERANCE_RELATIVE);
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
    final ForexOptionVanilla forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final double gammaRelative = METHOD_OPTION.gammaRelative(forexOption, SMILE_BUNDLE, true);
    final double gammaExpected = gammaRelative * notional;
    final CurrencyAmount gammaComputed = METHOD_OPTION.gamma(forexOption, SMILE_BUNDLE, true);
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
    final ForexOptionVanilla forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final double gammaRelative = METHOD_OPTION.gammaRelative(forexOption, SMILE_BUNDLE, false);
    final double gammaExpected = gammaRelative * notional;
    final CurrencyAmount gammaComputed = METHOD_OPTION.gamma(forexOption, SMILE_BUNDLE, false);
    assertEquals("Forex: relative gamma", 1.0, gammaExpected / gammaComputed.getAmount(), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the gamma for Forex option.
   */
  public void gammaMethodVsCalculator() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, payDate, notional, strike);
    final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionVanilla forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final GammaValueBlackForexCalculator calculator = GammaValueBlackForexCalculator.getInstance();
    final CurrencyAmount gammaCalculator = forexOption.accept(calculator, SMILE_BUNDLE);
    final CurrencyAmount gammaMethod = METHOD_OPTION.gamma(forexOption, SMILE_BUNDLE, true);
    assertEquals("Forex: relative gamma", 1.0, gammaCalculator.getAmount() / gammaMethod.getAmount(), TOLERANCE_PV);
  }

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
    final ForexOptionVanilla forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final double gammaRelativeSpot = METHOD_OPTION.gammaRelativeSpot(forexOption, SMILE_BUNDLE, true);
    final double gammaSpotExpected = gammaRelativeSpot * notional;
    final CurrencyAmount gammaSpotComputed = METHOD_OPTION.gammaSpot(forexOption, SMILE_BUNDLE, true);
    assertEquals("Forex: relative gamma", 1.0, gammaSpotExpected / gammaSpotComputed.getAmount(), TOLERANCE_PV);
    final double gammaSpotExpected2 = METHOD_OPTION.gamma(forexOption, SMILE_BUNDLE, true).getAmount() * SPOT;
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
    final ForexOptionVanilla forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final double gammaRelativeSpot = METHOD_OPTION.gammaRelativeSpot(forexOption, SMILE_BUNDLE, false);
    final double gammaSpotExpected = gammaRelativeSpot * notional;
    final CurrencyAmount gammaSpotComputed = METHOD_OPTION.gammaSpot(forexOption, SMILE_BUNDLE, false);
    assertEquals("Forex: relative gamma", 1.0, gammaSpotExpected / gammaSpotComputed.getAmount(), TOLERANCE_PV);
    final double gammaSpotExpected2 = METHOD_OPTION.gamma(forexOption, SMILE_BUNDLE, false).getAmount() * SPOT;
    assertEquals("Forex: relative gamma", 1.0, gammaSpotExpected2 / gammaSpotComputed.getAmount(), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the gamma for Forex option.
   */
  public void gammaSpotMethodVsCalculator() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, payDate, notional, strike);
    final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionVanilla forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final GammaSpotBlackForexCalculator calculator = GammaSpotBlackForexCalculator.getInstance();
    final CurrencyAmount gammaSpotCalculator = forexOption.accept(calculator, SMILE_BUNDLE);
    final CurrencyAmount gammaSpotMethod = METHOD_OPTION.gammaSpot(forexOption, SMILE_BUNDLE, true);
    assertEquals("Forex: relative gamma", 1.0, gammaSpotCalculator.getAmount() / gammaSpotMethod.getAmount(), TOLERANCE_PV);
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
    final ForexOptionVanilla forexOptionCall = forexOptionDefinitionCall.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final Forex forexForward = forexUnderlyingDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final MultipleCurrencyInterestRateCurveSensitivity sensi = METHOD_OPTION.presentValueCurveSensitivity(forexOptionCall, SMILE_BUNDLE);
    final double dfDomestic = CURVES.getCurve(CURVES_NAME[1]).getDiscountFactor(forexForward.getPaymentTime());
    final double dfForeign = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(forexForward.getPaymentTime());
    final double forward = SPOT * dfForeign / dfDomestic;
    final double volatility = SMILE_TERM.getVolatility(new Triple<>(forexOptionCall.getTimeToExpiry(), strike, forward));
    final Function1D<BlackFunctionData, Double> func = BLACK_FUNCTION.getPriceFunction(forexOptionCall);
    // Finite difference
    final YieldAndDiscountCurve curveDomestic = CURVES.getCurve(forexOptionCall.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName());
    final YieldAndDiscountCurve curveForeign = CURVES.getCurve(forexOptionCall.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName());
    double forwardBumped;
    double dfForeignBumped;
    double dfDomesticBumped;
    final double deltaShift = 0.00001; // 0.1 bp
    final double[] nodeTimes = new double[2];
    nodeTimes[0] = 0.0;
    nodeTimes[1] = forexOptionCall.getUnderlyingForex().getPaymentTime();
    final double[] yields = new double[2];
    YieldAndDiscountCurve curveNode;
    YieldAndDiscountCurve curveBumpedPlus;
    YieldAndDiscountCurve curveBumpedMinus;
    final String bumpedCurveName = "Bumped";
    BlackFunctionData dataBlack;
    //Foreign
    yields[0] = curveForeign.getInterestRate(nodeTimes[0]);
    yields[1] = curveForeign.getInterestRate(nodeTimes[1]);
    curveNode = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(nodeTimes, yields, new LinearInterpolator1D()));
    curveBumpedPlus = curveNode.withSingleShift(nodeTimes[1], deltaShift);
    curveBumpedMinus = curveNode.withSingleShift(nodeTimes[1], -deltaShift);
    final YieldCurveBundle curvesForeign = new YieldCurveBundle();
    curvesForeign.setCurve(bumpedCurveName, curveBumpedPlus);
    curvesForeign.setCurve(CURVES_NAME[1], CURVES.getCurve(CURVES_NAME[1]));
    dfForeignBumped = curveBumpedPlus.getDiscountFactor(forexForward.getPaymentTime());
    forwardBumped = SPOT * dfForeignBumped / dfDomestic;
    dataBlack = new BlackFunctionData(forwardBumped, dfDomestic, volatility);
    final double bumpedPvForeignPlus = func.evaluate(dataBlack) * notional;
    curvesForeign.replaceCurve(bumpedCurveName, curveBumpedMinus);
    dfForeignBumped = curveBumpedMinus.getDiscountFactor(forexForward.getPaymentTime());
    forwardBumped = SPOT * dfForeignBumped / dfDomestic;
    dataBlack = new BlackFunctionData(forwardBumped, dfDomestic, volatility);
    final double bumpedPvForeignMinus = func.evaluate(dataBlack) * notional;
    final double resultForeign = (bumpedPvForeignPlus - bumpedPvForeignMinus) / (2 * deltaShift);
    assertEquals("Forex vanilla option: curve exposure", forexForward.getPaymentTime(), sensi.getSensitivity(USD).getSensitivities().get(CURVES_NAME[0]).get(0).first, 1E-2);
    assertEquals("Forex vanilla option: curve exposure", resultForeign, sensi.getSensitivity(USD).getSensitivities().get(CURVES_NAME[0]).get(0).second, 1E-2);
    //Domestic
    yields[0] = curveDomestic.getInterestRate(nodeTimes[0]);
    yields[1] = curveDomestic.getInterestRate(nodeTimes[1]);
    curveNode = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(nodeTimes, yields, new LinearInterpolator1D()));
    curveBumpedPlus = curveNode.withSingleShift(nodeTimes[1], deltaShift);
    curveBumpedMinus = curveNode.withSingleShift(nodeTimes[1], -deltaShift);
    final YieldCurveBundle curvesDomestic = new YieldCurveBundle();
    curvesDomestic.setCurve(CURVES_NAME[0], CURVES.getCurve(CURVES_NAME[0]));
    curvesDomestic.setCurve(bumpedCurveName, curveBumpedPlus);
    dfDomesticBumped = curveBumpedPlus.getDiscountFactor(forexForward.getPaymentTime());
    forwardBumped = SPOT * dfForeign / dfDomesticBumped;
    dataBlack = new BlackFunctionData(forwardBumped, dfDomesticBumped, volatility);
    final double bumpedPvDomesticPlus = func.evaluate(dataBlack) * notional;
    curvesForeign.replaceCurve(bumpedCurveName, curveBumpedMinus);
    dfDomesticBumped = curveBumpedMinus.getDiscountFactor(forexForward.getPaymentTime());
    forwardBumped = SPOT * dfForeign / dfDomesticBumped;
    dataBlack = new BlackFunctionData(forwardBumped, dfDomesticBumped, volatility);
    final double bumpedPvDomesticMinus = func.evaluate(dataBlack) * notional;
    final double resultDomestic = (bumpedPvDomesticPlus - bumpedPvDomesticMinus) / (2 * deltaShift);
    assertEquals("Forex vanilla option: curve exposure", forexForward.getPaymentTime(), sensi.getSensitivity(USD).getSensitivities().get(CURVES_NAME[1]).get(0).first, 1E-2);
    assertEquals("Forex vanilla option: curve exposure", resultDomestic, sensi.getSensitivity(USD).getSensitivities().get(CURVES_NAME[1]).get(0).second, 1E-2);
  }

  @Test
  /**
   * Test the present value curve sensitivity through the method and through the calculator.
   */
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final MultipleCurrencyInterestRateCurveSensitivity pvcsMethod = METHOD_OPTION.presentValueCurveSensitivity(FOREX_CALL_OPTION, SMILE_BUNDLE);
    final MultipleCurrencyInterestRateCurveSensitivity pvcsCalculator = FOREX_CALL_OPTION.accept(PVCSC_BLACK, SMILE_BUNDLE);
    assertEquals("Forex present value curve sensitivity: Method vs Calculator", pvcsMethod, pvcsCalculator);
  }

  @Test
  /**
   * Tests present value volatility sensitivity.
   */
  public void volatilitySensitivity() {
    final PresentValueForexBlackVolatilitySensitivity sensi = METHOD_OPTION.presentValueBlackVolatilitySensitivity(FOREX_CALL_OPTION, SMILE_BUNDLE);
    final Pair<Currency, Currency> currencyPair = Pairs.of(EUR, USD);
    final DoublesPair point = DoublesPair.of(FOREX_CALL_OPTION.getTimeToExpiry(), STRIKE);
    assertEquals("Forex vanilla option: vega", currencyPair, sensi.getCurrencyPair());
    assertEquals("Forex vanilla option: vega size", 1, sensi.getVega().getMap().entrySet().size());
    assertTrue("Forex vanilla option: vega", sensi.getVega().getMap().containsKey(point));
    final double timeToExpiry = TimeCalculator.getTimeBetween(REFERENCE_DATE, OPTION_EXP_DATE);
    final double df = CURVES.getCurve(CURVES_NAME[1]).getDiscountFactor(TimeCalculator.getTimeBetween(REFERENCE_DATE, OPTION_PAY_DATE));
    final double forward = SPOT * CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(TimeCalculator.getTimeBetween(REFERENCE_DATE, OPTION_PAY_DATE)) / df;
    final double volatility = SMILE_TERM.getVolatility(new Triple<>(timeToExpiry, STRIKE, forward));
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, df, volatility);
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(FOREX_CALL_OPTION, dataBlack);
    assertEquals("Forex vanilla option: vega", priceAdjoint[2] * NOTIONAL, sensi.getVega().getMap().get(point));
    final ForexOptionVanillaDefinition optionShortDefinition = new ForexOptionVanillaDefinition(FOREX_DEFINITION, OPTION_EXP_DATE, IS_CALL, !IS_LONG);
    final ForexOptionVanilla optionShort = optionShortDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final PresentValueForexBlackVolatilitySensitivity sensiShort = METHOD_OPTION.presentValueBlackVolatilitySensitivity(optionShort, SMILE_BUNDLE);
    assertEquals("Forex vanilla option: vega short", -sensi.getVega().getMap().get(point), sensiShort.getVega().getMap().get(point));
    // Put/call parity
    final ForexOptionVanillaDefinition optionShortPutDefinition = new ForexOptionVanillaDefinition(FOREX_DEFINITION, OPTION_EXP_DATE, !IS_CALL, !IS_LONG);
    final ForexOptionVanilla optionShortPut = optionShortPutDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final PresentValueForexBlackVolatilitySensitivity sensiShortPut = METHOD_OPTION.presentValueBlackVolatilitySensitivity(optionShortPut, SMILE_BUNDLE);
    assertEquals("Forex vanilla option: vega short", sensiShortPut.getVega().getMap().get(point) + sensi.getVega().getMap().get(point), 0.0, 1.0E-2);
  }

  @Test
  /**
   * Test the present value curve sensitivity through the method and through the calculator.
   */
  public void volatilitySensitivityMethodVsCalculator() {
    final PresentValueForexBlackVolatilitySensitivity pvvsMethod = METHOD_OPTION.presentValueBlackVolatilitySensitivity(FOREX_CALL_OPTION, SMILE_BUNDLE);
    final PresentValueForexBlackVolatilitySensitivity pvvsCalculator = FOREX_CALL_OPTION.accept(PVVSC_BLACK, SMILE_BUNDLE);
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
    final ForexOptionVanilla callEURUSD = callEURUSDDefinition.toDerivative(REFERENCE_DATE, new String[] {CURVES_NAME[0], CURVES_NAME[1] });
    final ForexOptionVanilla putUSDEUR = putUSDEURDefinition.toDerivative(REFERENCE_DATE, new String[] {CURVES_NAME[1], CURVES_NAME[0] });
    final PresentValueForexBlackVolatilitySensitivity vsCallEURUSD = METHOD_OPTION.presentValueBlackVolatilitySensitivity(callEURUSD, SMILE_BUNDLE);
    final PresentValueForexBlackVolatilitySensitivity vsPutUSDEUR = METHOD_OPTION.presentValueBlackVolatilitySensitivity(putUSDEUR, SMILE_BUNDLE);
    final DoublesPair point = DoublesPair.of(callEURUSD.getTimeToExpiry(), strike);
    assertEquals("Forex vanilla option: volatilityNode", vsCallEURUSD.getVega().getMap().get(point) / SPOT, vsPutUSDEUR.getVega().getMap().get(point), 1.0E-2);
  }

  @Test
  /**
   * Tests present value volatility node sensitivity.
   */
  public void volatilityNodeSensitivity() {
    final PresentValueForexBlackVolatilityNodeSensitivityDataBundle sensi = METHOD_OPTION.presentValueBlackVolatilityNodeSensitivity(FOREX_CALL_OPTION, SMILE_BUNDLE);
    assertEquals("Forex vanilla option: vega node size", NB_EXP + 1, sensi.getVega().getData().length);
    assertEquals("Forex vanilla option: vega node size", NB_STRIKE, sensi.getVega().getData()[0].length);
    final Pair<Currency, Currency> currencyPair = Pairs.of(EUR, USD);
    assertEquals("Forex vanilla option: vega", currencyPair, sensi.getCurrencyPair());
    final PresentValueForexBlackVolatilitySensitivity pointSensitivity = METHOD_OPTION.presentValueBlackVolatilitySensitivity(FOREX_CALL_OPTION, SMILE_BUNDLE);
    final double df = CURVES.getCurve(CURVES_NAME[1]).getDiscountFactor(TimeCalculator.getTimeBetween(REFERENCE_DATE, OPTION_PAY_DATE));
    final double forward = SPOT * CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(TimeCalculator.getTimeBetween(REFERENCE_DATE, OPTION_PAY_DATE)) / df;
    final VolatilityAndBucketedSensitivities volAndSensitivities = SMILE_TERM.getVolatilityAndSensitivities(FOREX_CALL_OPTION.getTimeToExpiry(), STRIKE, forward);
    final double[][] nodeWeight = volAndSensitivities.getBucketedSensitivities();
    final DoublesPair point = DoublesPair.of(FOREX_CALL_OPTION.getTimeToExpiry(), STRIKE);
    for (int loopexp = 0; loopexp < NB_EXP; loopexp++) {
      for (int loopstrike = 0; loopstrike < NB_STRIKE; loopstrike++) {
        assertEquals("Forex vanilla option: vega node", nodeWeight[loopexp][loopstrike] * pointSensitivity.getVega().getMap().get(point), sensi.getVega().getData()[loopexp][loopstrike]);
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
    final ForexOptionVanilla callEURUSD = callEURUSDDefinition.toDerivative(REFERENCE_DATE, new String[] {CURVES_NAME[0], CURVES_NAME[1] });
    final ForexOptionVanilla putUSDEUR = putUSDEURDefinition.toDerivative(REFERENCE_DATE, new String[] {CURVES_NAME[1], CURVES_NAME[0] });
    final PresentValueForexBlackVolatilityNodeSensitivityDataBundle nsCallEURUSD = METHOD_OPTION.presentValueBlackVolatilityNodeSensitivity(callEURUSD, SMILE_BUNDLE);
    final PresentValueForexBlackVolatilityNodeSensitivityDataBundle nsPutUSDEUR = METHOD_OPTION.presentValueBlackVolatilityNodeSensitivity(putUSDEUR, SMILE_BUNDLE);
    for (int loopexp = 0; loopexp < nsCallEURUSD.getExpiries().getNumberOfElements(); loopexp++) {
      for (int loopdelta = 0; loopdelta < nsCallEURUSD.getDelta().getNumberOfElements(); loopdelta++) {
        assertEquals("Forex vanilla option: volatilityNode", nsCallEURUSD.getVega().getEntry(loopexp, loopdelta) / SPOT, nsPutUSDEUR.getVega().getEntry(loopexp, loopdelta), 1.0E-2);
      }
    }
  }

  @Test
  /**
   * Tests present value volatility quote sensitivity.
   */
  public void volatilityQuoteSensitivity() {
    final PresentValueForexBlackVolatilityNodeSensitivityDataBundle sensiStrike = METHOD_OPTION.presentValueBlackVolatilityNodeSensitivity(FOREX_CALL_OPTION, SMILE_BUNDLE);
    final double[][] sensiQuote = METHOD_OPTION.presentValueBlackVolatilityNodeSensitivity(FOREX_CALL_OPTION, SMILE_BUNDLE).quoteSensitivity().getVega();
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

  @Test
  /**
   * Tests present value volatility quote sensitivity: method vs calculator.
   */
  public void volatilityQuoteSensitivityMethodVsCalculator() {
    final double[][] sensiMethod = METHOD_OPTION.presentValueBlackVolatilityNodeSensitivity(FOREX_CALL_OPTION, SMILE_BUNDLE).quoteSensitivity().getVega();
    final double[][] sensiCalculator = PresentValueBlackVolatilityQuoteSensitivityForexCalculator.getInstance().visit(FOREX_CALL_OPTION, SMILE_BUNDLE).getVega();
    for (int loopexp = 0; loopexp < NB_EXP; loopexp++) {
      ArrayAsserts.assertArrayEquals("Forex option - quote sensitivity", sensiMethod[loopexp], sensiCalculator[loopexp], 1.0E-10);
    }
  }

  private static final double TOLERANCE_DELTA = 1.0E-0;

  @Test
  /**
   * Tests the present value curve sensitivity.
   */
  public void presentValueCurveSensitivityConverted() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, payDate, notional, strike);
    final ForexOptionVanillaDefinition forexOptionDefinitionCall = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionVanilla forexOptionCall = forexOptionDefinitionCall.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final MultipleCurrencyInterestRateCurveSensitivity sensi = forexOptionCall.accept(PVCSC_BLACK, SMILE_BUNDLE);
    final InterestRateCurveSensitivity sensiConverted = PVCSCC_BLACK.visit(forexOptionCall, SMILE_BUNDLE);
    InterestRateCurveSensitivity sensiComp = new InterestRateCurveSensitivity();
    sensiComp = sensiComp.plus(CURVES_NAME[1], sensi.getSensitivity(USD).getSensitivities().get(CURVES_NAME[1]));
    sensiComp = sensiComp.plus(CURVES_NAME[0], InterestRateCurveSensitivityUtils.multiplySensitivity(sensi.getSensitivity(USD).getSensitivities().get(CURVES_NAME[0]), SPOT));
    AssertSensivityObjects.assertEquals("Forex Option: present value curve sensitivity converted", sensiConverted, sensiComp, TOLERANCE_DELTA);
  }

  @Test
  /**
   * Tests the Theta (1 day change of pv) for forex options transactions.
   */
  public void thetaBeforeExpiration() {
    final MultipleCurrencyAmount theta = THETAC.getTheta(FOREX_OPTION_CALL_DEFINITION, REFERENCE_DATE, CURVES_NAME, SMILE_BUNDLE, 1);
    final ForexOptionVanilla swapToday = FOREX_OPTION_CALL_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final ForexOptionVanilla swapTomorrow = FOREX_OPTION_CALL_DEFINITION.toDerivative(REFERENCE_DATE.plusDays(1), CURVES_NAME);
    final MultipleCurrencyAmount pvToday = swapToday.accept(PVC_BLACK, SMILE_BUNDLE);
    final YieldCurveBundle tomorrowData = FX_OPTION_ROLLDOWN.rollDown(SMILE_BUNDLE, TimeCalculator.getTimeBetween(REFERENCE_DATE, REFERENCE_DATE.plusDays(1)));
    final MultipleCurrencyAmount pvTomorrow = swapTomorrow.accept(PVC_BLACK, tomorrowData);
    final MultipleCurrencyAmount thetaExpected = pvTomorrow.plus(pvToday.multipliedBy(-1.0));
    assertEquals("ThetaCalculator: forex option", thetaExpected.getAmount(USD), theta.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the theoretical Theta (derivative with respect to time in Black formula).
   */
  public void thetaTheoretical() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final double timeToExpiry = TimeCalculator.getTimeBetween(REFERENCE_DATE, expDate);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, payDate, notional, strike);
    final ForexOptionVanillaDefinition callDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionVanilla call = callDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final double df = CURVES.getCurve(CURVES_NAME[1]).getDiscountFactor(TimeCalculator.getTimeBetween(REFERENCE_DATE, payDate));
    final double forward = SPOT * CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(TimeCalculator.getTimeBetween(REFERENCE_DATE, payDate)) / df;
    final double volatility = SMILE_TERM.getVolatility(timeToExpiry, strike, forward);
    final double thetaUnit = BlackFormulaRepository.driftlessTheta(forward, strike, timeToExpiry, volatility);
    final double thetaExpected = thetaUnit * notional;
    final CurrencyAmount thetaCallComputed = METHOD_OPTION.theta(call, SMILE_BUNDLE);
    assertEquals("Theta theoretical: forex option", thetaExpected, thetaCallComputed.getAmount(), TOLERANCE_PV);
    assertEquals("Theta theoretical: forex option", USD, thetaCallComputed.getCurrency());
    final ForexOptionVanillaDefinition putDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, !isCall, isLong);
    final ForexOptionVanilla put = putDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final CurrencyAmount thetaPutComputed = METHOD_OPTION.theta(put, SMILE_BUNDLE);
    assertEquals("Theta theoretical: forex option", thetaCallComputed.getAmount(), thetaPutComputed.getAmount(), TOLERANCE_PV);
  }

}
