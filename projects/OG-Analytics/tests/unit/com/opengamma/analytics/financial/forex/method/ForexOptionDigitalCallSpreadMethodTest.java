/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.method;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Map;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.forex.calculator.CurrencyExposureCallSpreadBlackForexCalculator;
import com.opengamma.analytics.financial.forex.calculator.PresentValueCallSpreadBlackForexCalculator;
import com.opengamma.analytics.financial.forex.calculator.PresentValueCurveSensitivityCallSpreadBlackForexCalculator;
import com.opengamma.analytics.financial.forex.calculator.PresentValueVolatilitySensitivityCallSpreadBlackForexCalculator;
import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionDigitalDefinition;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.forex.method.ForexOptionDigitalBlackMethod;
import com.opengamma.analytics.financial.forex.method.ForexOptionDigitalCallSpreadBlackMethod;
import com.opengamma.analytics.financial.forex.method.ForexOptionVanillaBlackMethod;
import com.opengamma.analytics.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureParameter;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Tests related to the pricing method for digital Forex option transactions with Black function and a volatility provider.
 */
public class ForexOptionDigitalCallSpreadMethodTest {
  // General
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final int SETTLEMENT_DAYS = 2;
  // Smile data
  private static final Currency EUR = Currency.EUR;
  private static final Currency USD = Currency.USD;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 6, 13);
  private static final FXMatrix FX_MATRIX = TestsDataSetsForex.fxMatrix();
  private static final double SPOT = FX_MATRIX.getFxRate(EUR, USD);
  private static final SmileDeltaTermStructureParameter SMILE_TERM = TestsDataSetsForex.smile(REFERENCE_DATE);
  private static final SmileDeltaTermStructureParameter SMILE_TERM_FLAT = TestsDataSetsForex.smileFlat(REFERENCE_DATE);
  // Methods and curves
  private static final YieldCurveBundle CURVES = TestsDataSetsForex.createCurvesForex();
  private static final String[] CURVES_NAME = TestsDataSetsForex.curveNames();
  private static final Map<String, Currency> CURVE_CURRENCY = TestsDataSetsForex.curveCurrency();
  private static final SmileDeltaTermStructureDataBundle SMILE_BUNDLE = new SmileDeltaTermStructureDataBundle(FX_MATRIX, CURVE_CURRENCY, CURVES, SMILE_TERM, Pair.of(EUR, USD));
  private static final SmileDeltaTermStructureDataBundle SMILE_BUNDLE_FLAT = new SmileDeltaTermStructureDataBundle(FX_MATRIX, CURVE_CURRENCY, CURVES, SMILE_TERM_FLAT, Pair.of(EUR, USD));
  private static final ForexOptionVanillaBlackMethod METHOD_VANILLA_BLACK = ForexOptionVanillaBlackMethod.getInstance();
  private static final ForexOptionDigitalBlackMethod METHOD_DIGITAL_BLACK = ForexOptionDigitalBlackMethod.getInstance();
  private static final double STANDARD_SPREAD = 0.0001;
  private static final ForexOptionDigitalCallSpreadBlackMethod METHOD_DIGITAL_SPREAD = new ForexOptionDigitalCallSpreadBlackMethod(STANDARD_SPREAD);
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  // option
  private static final double STRIKE = 1.45;
  private static final boolean IS_CALL = true;
  private static final boolean IS_LONG = true;
  private static final double NOTIONAL = 100000000;
  private static final ZonedDateTime OPTION_PAY_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
  private static final ZonedDateTime OPTION_EXP_DATE = ScheduleCalculator.getAdjustedDate(OPTION_PAY_DATE, -SETTLEMENT_DAYS, CALENDAR);
  private static final ForexDefinition FOREX_DEFINITION = new ForexDefinition(EUR, USD, OPTION_PAY_DATE, NOTIONAL, STRIKE);
  private static final Forex FOREX = FOREX_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final ForexOptionDigitalDefinition FOREX_OPTION_CALL_DEFINITION = new ForexOptionDigitalDefinition(FOREX_DEFINITION, OPTION_EXP_DATE, IS_CALL, IS_LONG);
  private static final ForexOptionDigital FOREX_CALL_OPTION = FOREX_OPTION_CALL_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final double TOLERANCE_PRICE = 1.0E-2;
  private static final double TOLERANCE_PRICE_FLAT = 1.0E+1; // The spread size will create a discrepancy.
  private static final double TOLERANCE_CE_FLAT = 1.0E+2; // The spread size will create a discrepancy.
  private static final double TOLERANCE_DELTA = 1.0E+2; // 0.01 currency unit for 1 bp

  @Test
  /**
   * Tests the present value in a flat smile case.
   */
  public void presentValueFlat() {
    MultipleCurrencyAmount pvSpread = METHOD_DIGITAL_SPREAD.presentValue(FOREX_CALL_OPTION, SMILE_BUNDLE_FLAT);
    MultipleCurrencyAmount pvBlack = METHOD_DIGITAL_BLACK.presentValue(FOREX_CALL_OPTION, SMILE_BUNDLE_FLAT);
    assertEquals("Forex Digital option: call spread method - present value", pvBlack.getAmount(USD), pvSpread.getAmount(USD), TOLERANCE_PRICE_FLAT);
  }

  @Test
  /**
   * Tests the present value with an explicit computation.
   */
  public void presentValue() {
    double strikeM = STRIKE * (1 - STANDARD_SPREAD);
    double strikeP = STRIKE * (1 + STANDARD_SPREAD);
    Forex forexM = new Forex(FOREX.getPaymentCurrency1().withAmount(1.0), FOREX.getPaymentCurrency2().withAmount(-strikeM));
    Forex forexP = new Forex(FOREX.getPaymentCurrency1().withAmount(1.0), FOREX.getPaymentCurrency2().withAmount(-strikeP));
    ForexOptionVanilla vanillaM = new ForexOptionVanilla(forexM, FOREX_CALL_OPTION.getExpirationTime(), IS_CALL, IS_LONG);
    ForexOptionVanilla vanillaP = new ForexOptionVanilla(forexP, FOREX_CALL_OPTION.getExpirationTime(), IS_CALL, IS_LONG);
    MultipleCurrencyAmount pvP = METHOD_VANILLA_BLACK.presentValue(vanillaP, SMILE_BUNDLE);
    MultipleCurrencyAmount pvM = METHOD_VANILLA_BLACK.presentValue(vanillaM, SMILE_BUNDLE);
    MultipleCurrencyAmount pvExpected = pvM.plus(pvP.multipliedBy(-1.0)).multipliedBy(1.0 / (strikeP - strikeM) * Math.abs(FOREX.getPaymentCurrency2().getAmount()));
    MultipleCurrencyAmount pvComputed = METHOD_DIGITAL_SPREAD.presentValue(FOREX_CALL_OPTION, SMILE_BUNDLE);
    assertEquals("Forex Digital option: call spread method - present value", pvExpected.getAmount(USD), pvComputed.getAmount(USD), TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests put call parity.
   */
  public void presentValuePutCallParity() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, payDate, notional, strike);
    final ForexOptionDigitalDefinition callDefinition = new ForexOptionDigitalDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionDigitalDefinition putDefinition = new ForexOptionDigitalDefinition(forexUnderlyingDefinition, expDate, !isCall, isLong);
    final ForexOptionDigital call = callDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final ForexOptionDigital put = putDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final MultipleCurrencyAmount pvCall = METHOD_DIGITAL_SPREAD.presentValue(call, SMILE_BUNDLE);
    final MultipleCurrencyAmount pvPut = METHOD_DIGITAL_SPREAD.presentValue(put, SMILE_BUNDLE);
    final Double pvCash = PVC.visit(put.getUnderlyingForex().getPaymentCurrency2(), CURVES);
    assertEquals("Forex Digital option: call spread method - present value", pvCall.getAmount(USD) + pvPut.getAmount(USD), Math.abs(pvCash), TOLERANCE_PRICE_FLAT);
  }

  @Test
  /**
   * Tests the present value long/short parity.
   */
  public void presentValueLongShort() {
    final ForexOptionDigitalDefinition forexOptionShortDefinition = new ForexOptionDigitalDefinition(FOREX_DEFINITION, OPTION_EXP_DATE, IS_CALL, !IS_LONG);
    final InstrumentDerivative forexOptionShort = forexOptionShortDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final MultipleCurrencyAmount pvShort = METHOD_DIGITAL_SPREAD.presentValue(forexOptionShort, SMILE_BUNDLE);
    final MultipleCurrencyAmount pvLong = METHOD_DIGITAL_SPREAD.presentValue(FOREX_CALL_OPTION, SMILE_BUNDLE);
    assertEquals("Forex Digital option: present value long/short parity", pvLong.getAmount(USD), -pvShort.getAmount(USD), 1E-2);
    final MultipleCurrencyAmount ceShort = METHOD_DIGITAL_SPREAD.currencyExposure(forexOptionShort, SMILE_BUNDLE);
    final MultipleCurrencyAmount ceLong = METHOD_DIGITAL_SPREAD.currencyExposure(FOREX_CALL_OPTION, SMILE_BUNDLE);
    assertEquals("Forex Digital option: currency exposure long/short parity", ceLong.getAmount(USD), -ceShort.getAmount(USD), 1E-2);
    assertEquals("Forex Digital option: currency exposure long/short parity", ceLong.getAmount(EUR), -ceShort.getAmount(EUR), 1E-2);
  }

  @Test
  /**
   * Tests the currency exposure in a flat smile case.
   */
  public void currencyExposureFlat() {
    MultipleCurrencyAmount ceSpread = METHOD_DIGITAL_SPREAD.currencyExposure(FOREX_CALL_OPTION, SMILE_BUNDLE_FLAT);
    MultipleCurrencyAmount ceBlack = METHOD_DIGITAL_BLACK.currencyExposure(FOREX_CALL_OPTION, SMILE_BUNDLE_FLAT);
    assertEquals("Forex Digital option: call spread method - currency exposure", ceBlack.getAmount(USD), ceSpread.getAmount(USD), TOLERANCE_CE_FLAT);
    assertEquals("Forex Digital option: call spread method - currency exposure", ceBlack.getAmount(EUR), ceSpread.getAmount(EUR), TOLERANCE_CE_FLAT);
  }

  @Test
  /**
   * Tests the currency exposure with an explicit computation.
   */
  public void currencyExposure() {
    double spread = 0.0001; // Relative spread.
    double strikeM = STRIKE * (1 - spread);
    double strikeP = STRIKE * (1 + spread);
    Forex forexM = new Forex(FOREX.getPaymentCurrency1().withAmount(1.0), FOREX.getPaymentCurrency2().withAmount(-strikeM));
    Forex forexP = new Forex(FOREX.getPaymentCurrency1().withAmount(1.0), FOREX.getPaymentCurrency2().withAmount(-strikeP));
    ForexOptionVanilla vanillaM = new ForexOptionVanilla(forexM, FOREX_CALL_OPTION.getExpirationTime(), IS_CALL, IS_LONG);
    ForexOptionVanilla vanillaP = new ForexOptionVanilla(forexP, FOREX_CALL_OPTION.getExpirationTime(), IS_CALL, IS_LONG);
    MultipleCurrencyAmount ceP = METHOD_VANILLA_BLACK.currencyExposure(vanillaP, SMILE_BUNDLE);
    MultipleCurrencyAmount ceM = METHOD_VANILLA_BLACK.currencyExposure(vanillaM, SMILE_BUNDLE);
    MultipleCurrencyAmount ceExpected = ceM.plus(ceP.multipliedBy(-1.0)).multipliedBy(1.0 / (strikeP - strikeM) * Math.abs(FOREX.getPaymentCurrency2().getAmount()));
    MultipleCurrencyAmount ceComputed = METHOD_DIGITAL_SPREAD.currencyExposure(FOREX_CALL_OPTION, SMILE_BUNDLE);
    assertEquals("Forex Digital option: call spread method - currency exposure", ceExpected.getAmount(USD), ceComputed.getAmount(USD), TOLERANCE_PRICE);
    assertEquals("Forex Digital option: call spread method - currency exposure", ceExpected.getAmount(EUR), ceComputed.getAmount(EUR), TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests the currency exposure against the present value.
   */
  public void currencyExposureVsPresentValue() {
    MultipleCurrencyAmount pv = METHOD_DIGITAL_SPREAD.presentValue(FOREX_CALL_OPTION, SMILE_BUNDLE);
    MultipleCurrencyAmount ce = METHOD_DIGITAL_SPREAD.currencyExposure(FOREX_CALL_OPTION, SMILE_BUNDLE);
    assertEquals("Forex Digital option: call spread method - currency exposure vs present value", ce.getAmount(USD) + ce.getAmount(EUR) * SPOT, pv.getAmount(USD), TOLERANCE_PRICE);
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
    final ForexOptionDigitalDefinition forexOptionDefinitionCall = new ForexOptionDigitalDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionDigitalDefinition forexOptionDefinitionPut = new ForexOptionDigitalDefinition(forexUnderlyingDefinition, expDate, !isCall, isLong);
    final ForexOptionDigital forexOptionCall = forexOptionDefinitionCall.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final ForexOptionDigital forexOptionPut = forexOptionDefinitionPut.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final MultipleCurrencyAmount currencyExposureCall = METHOD_DIGITAL_SPREAD.currencyExposure(forexOptionCall, SMILE_BUNDLE);
    final MultipleCurrencyAmount currencyExposurePut = METHOD_DIGITAL_SPREAD.currencyExposure(forexOptionPut, SMILE_BUNDLE);
    final Double pvCash = PVC.visit(forexOptionPut.getUnderlyingForex().getPaymentCurrency2(), CURVES);
    assertEquals("Forex Digital option: currency exposure put/call parity foreign", 0, currencyExposureCall.getAmount(EUR) + currencyExposurePut.getAmount(EUR), TOLERANCE_PRICE);
    assertEquals("Forex Digital option: currency exposure put/call parity domestic", Math.abs(pvCash), currencyExposureCall.getAmount(USD) + currencyExposurePut.getAmount(USD), TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests the present value curve sensitivity.
   */
  public void presentValueCurveSensitivity() {
    double spread = 0.0001; // Relative spread.
    double strikeM = STRIKE * (1 - spread);
    double strikeP = STRIKE * (1 + spread);
    Forex forexM = new Forex(FOREX.getPaymentCurrency1().withAmount(1.0), FOREX.getPaymentCurrency2().withAmount(-strikeM));
    Forex forexP = new Forex(FOREX.getPaymentCurrency1().withAmount(1.0), FOREX.getPaymentCurrency2().withAmount(-strikeP));
    ForexOptionVanilla vanillaM = new ForexOptionVanilla(forexM, FOREX_CALL_OPTION.getExpirationTime(), IS_CALL, IS_LONG);
    ForexOptionVanilla vanillaP = new ForexOptionVanilla(forexP, FOREX_CALL_OPTION.getExpirationTime(), IS_CALL, IS_LONG);
    MultipleCurrencyInterestRateCurveSensitivity pvcsP = METHOD_VANILLA_BLACK.presentValueCurveSensitivity(vanillaP, SMILE_BUNDLE);
    MultipleCurrencyInterestRateCurveSensitivity pvcsM = METHOD_VANILLA_BLACK.presentValueCurveSensitivity(vanillaM, SMILE_BUNDLE);
    MultipleCurrencyInterestRateCurveSensitivity pvcsExpected = pvcsM.plus(pvcsP.multipliedBy(-1.0)).multipliedBy(1.0 / (strikeP - strikeM) * Math.abs(FOREX.getPaymentCurrency2().getAmount()));
    MultipleCurrencyInterestRateCurveSensitivity pvcsComputed = METHOD_DIGITAL_SPREAD.presentValueCurveSensitivity(FOREX_CALL_OPTION, SMILE_BUNDLE);
    assertTrue("Forex Digital option: call spread method - present value", MultipleCurrencyInterestRateCurveSensitivity.compare(pvcsExpected, pvcsComputed, TOLERANCE_DELTA));
  }

  @Test
  /**
   * Tests the present value curve sensitivity.
   */
  public void presentValueVolatilitySensitivity() {
    double strikeM = STRIKE * (1 - STANDARD_SPREAD);
    double strikeP = STRIKE * (1 + STANDARD_SPREAD);
    Forex forexM = new Forex(FOREX.getPaymentCurrency1().withAmount(1.0), FOREX.getPaymentCurrency2().withAmount(-strikeM));
    Forex forexP = new Forex(FOREX.getPaymentCurrency1().withAmount(1.0), FOREX.getPaymentCurrency2().withAmount(-strikeP));
    ForexOptionVanilla vanillaM = new ForexOptionVanilla(forexM, FOREX_CALL_OPTION.getExpirationTime(), IS_CALL, IS_LONG);
    ForexOptionVanilla vanillaP = new ForexOptionVanilla(forexP, FOREX_CALL_OPTION.getExpirationTime(), IS_CALL, IS_LONG);
    PresentValueForexBlackVolatilitySensitivity pvbvP = METHOD_VANILLA_BLACK.presentValueVolatilitySensitivity(vanillaP, SMILE_BUNDLE);
    PresentValueForexBlackVolatilitySensitivity pvbvM = METHOD_VANILLA_BLACK.presentValueVolatilitySensitivity(vanillaM, SMILE_BUNDLE);
    PresentValueForexBlackVolatilitySensitivity pvbvExpected = pvbvM.plus(pvbvP.multipliedBy(-1.0)).multipliedBy(1.0 / (strikeP - strikeM) * Math.abs(FOREX.getPaymentCurrency2().getAmount()));
    PresentValueForexBlackVolatilitySensitivity pvbvComputed = METHOD_DIGITAL_SPREAD.presentValueVolatilitySensitivity(FOREX_CALL_OPTION, SMILE_BUNDLE);
    assertEquals("Forex Digital option: call spread method - present value volatility sensitivity", pvbvComputed.getVega().getMap().size(), 2);
    assertTrue("Forex Digital option: call spread method - present value volatility sensitivity", PresentValueForexBlackVolatilitySensitivity.compare(pvbvExpected, pvbvComputed, TOLERANCE_DELTA));
  }

  @Test
  /**
   * Tests the present value. Spread change.
   */
  public void presentValueSpreadChange() {
    double spread1 = 0.0001;
    double spread2 = 0.0002;
    ForexOptionDigitalCallSpreadBlackMethod methodCallSpreadBlack1 = new ForexOptionDigitalCallSpreadBlackMethod(spread1);
    ForexOptionDigitalCallSpreadBlackMethod methodCallSpreadBlack2 = new ForexOptionDigitalCallSpreadBlackMethod(spread2);
    MultipleCurrencyAmount pv1 = methodCallSpreadBlack1.presentValue(FOREX_CALL_OPTION, SMILE_BUNDLE);
    MultipleCurrencyAmount pv2 = methodCallSpreadBlack2.presentValue(FOREX_CALL_OPTION, SMILE_BUNDLE);
    assertEquals("Forex Digital option: call spread method - present value", pv1.getAmount(USD), pv2.getAmount(USD), 10.0);
    //    MultipleCurrencyAmount pvBlack = METHOD_DIGITAL_BLACK.presentValue(FOREX_CALL_OPTION, SMILE_BUNDLE);
    //    assertEquals("Forex Digital option: call spread method - present value", pvBlack.getAmount(USD), pv2.getAmount(USD), 10.0); // Should fail
  }

  @Test
  /**
   * Tests the present value. Method vs Calculator.
   */
  public void presentValueMethodVCalculator() {
    MultipleCurrencyAmount pv1 = METHOD_DIGITAL_SPREAD.presentValue(FOREX_CALL_OPTION, SMILE_BUNDLE);
    PresentValueCallSpreadBlackForexCalculator calculator = new PresentValueCallSpreadBlackForexCalculator(STANDARD_SPREAD);
    MultipleCurrencyAmount pvCalculator = calculator.visit(FOREX_CALL_OPTION, SMILE_BUNDLE);
    assertEquals("Forex Digital option: call spread method - present value", pv1.getAmount(USD), pvCalculator.getAmount(USD), TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests the currency exposure. Method vs Calculator.
   */
  public void currencyExposureMethodVCalculator() {
    MultipleCurrencyAmount ceMethod = METHOD_DIGITAL_SPREAD.currencyExposure(FOREX_CALL_OPTION, SMILE_BUNDLE);
    CurrencyExposureCallSpreadBlackForexCalculator calculator = new CurrencyExposureCallSpreadBlackForexCalculator(STANDARD_SPREAD);
    MultipleCurrencyAmount ceCalculator = calculator.visit(FOREX_CALL_OPTION, SMILE_BUNDLE);
    assertEquals("Forex Digital option: call spread method - currency exposure", ceMethod.getAmount(USD), ceCalculator.getAmount(USD), TOLERANCE_PRICE);
    assertEquals("Forex Digital option: call spread method - currency exposure", ceMethod.getAmount(EUR), ceCalculator.getAmount(EUR), TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests the present value curve sensitivity. Method vs Calculator.
   */
  public void presentValueCurveSensitivityMethodVCalculator() {
    MultipleCurrencyInterestRateCurveSensitivity pvcsMethod = METHOD_DIGITAL_SPREAD.presentValueCurveSensitivity(FOREX_CALL_OPTION, SMILE_BUNDLE);
    PresentValueCurveSensitivityCallSpreadBlackForexCalculator calculator = new PresentValueCurveSensitivityCallSpreadBlackForexCalculator(STANDARD_SPREAD);
    MultipleCurrencyInterestRateCurveSensitivity pvcsCalculator = calculator.visit(FOREX_CALL_OPTION, SMILE_BUNDLE);
    assertTrue("Forex Digital option: call spread method - present value", MultipleCurrencyInterestRateCurveSensitivity.compare(pvcsMethod, pvcsCalculator, TOLERANCE_DELTA));
  }

  @Test
  /**
   * Tests the present value volatility sensitivity.  Method vs Calculator.
   */
  public void presentValueVolatilitySensitivityMethodVCalculator() {
    PresentValueForexBlackVolatilitySensitivity pvbvMethod = METHOD_DIGITAL_SPREAD.presentValueVolatilitySensitivity(FOREX_CALL_OPTION, SMILE_BUNDLE);
    PresentValueVolatilitySensitivityCallSpreadBlackForexCalculator calculator = new PresentValueVolatilitySensitivityCallSpreadBlackForexCalculator(STANDARD_SPREAD);
    PresentValueForexBlackVolatilitySensitivity pvbvCalculator = calculator.visit(FOREX_CALL_OPTION, SMILE_BUNDLE);
    assertTrue("Forex Digital option: call spread method - present value volatility sensitivity", PresentValueForexBlackVolatilitySensitivity.compare(pvbvMethod, pvbvCalculator, TOLERANCE_DELTA));
  }

}
