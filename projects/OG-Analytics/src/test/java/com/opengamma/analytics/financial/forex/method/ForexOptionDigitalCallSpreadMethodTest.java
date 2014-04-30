/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.method;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.forex.calculator.CurrencyExposureCallSpreadBlackForexCalculator;
import com.opengamma.analytics.financial.forex.calculator.GammaSpotCallSpreadBlackForexCalculator;
import com.opengamma.analytics.financial.forex.calculator.PresentValueBlackVolatilityNodeSensitivityCallSpreadBlackForexCalculator;
import com.opengamma.analytics.financial.forex.calculator.PresentValueBlackVolatilitySensitivityCallSpreadBlackForexCalculator;
import com.opengamma.analytics.financial.forex.calculator.PresentValueCallSpreadBlackForexCalculator;
import com.opengamma.analytics.financial.forex.calculator.PresentValueCurveSensitivityCallSpreadBlackForexCalculator;
import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionDigitalDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.horizon.ConstantSpreadFXOptionBlackRolldown;
import com.opengamma.analytics.financial.horizon.ConstantSpreadHorizonThetaCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.analytics.financial.model.volatility.VolatilityAndBucketedSensitivities;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
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

/**
 * Tests related to the pricing method for digital Forex option transactions with Black function and a volatility provider.
 * @deprecated This class tests deprecated code
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class ForexOptionDigitalCallSpreadMethodTest {
  // General
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final int SETTLEMENT_DAYS = 2;
  // Smile data
  private static final Currency EUR = Currency.EUR;
  private static final Currency USD = Currency.USD;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 6, 13);
  private static final FXMatrix FX_MATRIX = TestsDataSetsForex.fxMatrix();
  private static final double SPOT = FX_MATRIX.getFxRate(EUR, USD);
  private static final SmileDeltaTermStructureParametersStrikeInterpolation SMILE_TERM = TestsDataSetsForex.smile5points(REFERENCE_DATE);
  private static final SmileDeltaTermStructureParametersStrikeInterpolation SMILE_TERM_FLAT = TestsDataSetsForex.smileFlat(REFERENCE_DATE);
  // Methods and curves
  private static final YieldCurveBundle CURVES = TestsDataSetsForex.createCurvesForex();
  private static final String[] CURVES_NAME = TestsDataSetsForex.curveNames();
  private static final SmileDeltaTermStructureDataBundle SMILE_BUNDLE = new SmileDeltaTermStructureDataBundle(CURVES, SMILE_TERM, Pairs.of(EUR, USD));
  private static final SmileDeltaTermStructureDataBundle SMILE_BUNDLE_FLAT = new SmileDeltaTermStructureDataBundle(CURVES, SMILE_TERM_FLAT, Pairs.of(EUR, USD));
  private static final ForexOptionVanillaBlackSmileMethod METHOD_VANILLA_BLACK = ForexOptionVanillaBlackSmileMethod.getInstance();
  private static final ForexOptionDigitalBlackMethod METHOD_DIGITAL_BLACK = ForexOptionDigitalBlackMethod.getInstance();
  private static final double STANDARD_SPREAD = 0.0001;
  private static final ForexOptionDigitalCallSpreadBlackMethod METHOD_DIGITAL_SPREAD = new ForexOptionDigitalCallSpreadBlackMethod(STANDARD_SPREAD);
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final PresentValueCallSpreadBlackForexCalculator PVC_CALLSPREAD = new PresentValueCallSpreadBlackForexCalculator(STANDARD_SPREAD);
  private static final ConstantSpreadHorizonThetaCalculator THETAC = ConstantSpreadHorizonThetaCalculator.getInstance();
  private static final ConstantSpreadFXOptionBlackRolldown FX_OPTION_ROLLDOWN = ConstantSpreadFXOptionBlackRolldown.getInstance();
  // option
  private static final double STRIKE = 1.45;
  private static final boolean IS_CALL = true;
  private static final boolean IS_LONG = true;
  private static final double NOTIONAL = 100000000;
  private static final ZonedDateTime OPTION_PAY_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
  private static final ZonedDateTime OPTION_EXP_DATE = ScheduleCalculator.getAdjustedDate(OPTION_PAY_DATE, -SETTLEMENT_DAYS, CALENDAR);
  private static final ForexDefinition FOREX_DEFINITION = new ForexDefinition(EUR, USD, OPTION_PAY_DATE, NOTIONAL, STRIKE);
  private static final Forex FOREX = FOREX_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final ForexOptionDigitalDefinition FOREX_DIGITAL_CALL_DOM_DEFINITION = new ForexOptionDigitalDefinition(FOREX_DEFINITION, OPTION_EXP_DATE, IS_CALL, IS_LONG, true);
  private static final ForexOptionDigital FOREX_DIGITAL_CALL_DOM = FOREX_DIGITAL_CALL_DOM_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final ForexOptionDigitalDefinition FOREX_DIGITAL_CALL_FOR_DEFINITION = new ForexOptionDigitalDefinition(FOREX_DEFINITION, OPTION_EXP_DATE, IS_CALL, IS_LONG, false);
  private static final ForexOptionDigital FOREX_DIGITAL_CALL_FOR = FOREX_DIGITAL_CALL_FOR_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_FLAT = 1.0E+1; // The spread size will create a discrepancy.
  private static final double TOLERANCE_CE_FLAT = 1.0E+2; // The spread size will create a discrepancy.
  private static final double TOLERANCE_DELTA = 1.0E+2; // 0.01 currency unit for 1 bp
  private static final double TOLERANCE_RELATIVE = 1.0E-6;

  @Test
  /**
   * Tests the present value in a flat smile case.
   */
  public void presentValueFlat() {
    final MultipleCurrencyAmount pvSpread = METHOD_DIGITAL_SPREAD.presentValue(FOREX_DIGITAL_CALL_DOM, SMILE_BUNDLE_FLAT);
    final MultipleCurrencyAmount pvBlack = METHOD_DIGITAL_BLACK.presentValue(FOREX_DIGITAL_CALL_DOM, SMILE_BUNDLE_FLAT);
    assertEquals("Forex Digital option: call spread method - present value", pvBlack.getAmount(USD), pvSpread.getAmount(USD), TOLERANCE_PV_FLAT);
  }

  @Test
  /**
   * Tests the present value with an explicit computation.
   */
  public void presentValue() {
    final double strikeM = STRIKE * (1 - STANDARD_SPREAD);
    final double strikeP = STRIKE * (1 + STANDARD_SPREAD);
    final Forex forexM = new Forex(FOREX.getPaymentCurrency1().withAmount(1.0), FOREX.getPaymentCurrency2().withAmount(-strikeM));
    final Forex forexP = new Forex(FOREX.getPaymentCurrency1().withAmount(1.0), FOREX.getPaymentCurrency2().withAmount(-strikeP));
    final ForexOptionVanilla vanillaM = new ForexOptionVanilla(forexM, FOREX_DIGITAL_CALL_DOM.getExpirationTime(), IS_CALL, IS_LONG);
    final ForexOptionVanilla vanillaP = new ForexOptionVanilla(forexP, FOREX_DIGITAL_CALL_DOM.getExpirationTime(), IS_CALL, IS_LONG);
    final MultipleCurrencyAmount pvP = METHOD_VANILLA_BLACK.presentValue(vanillaP, SMILE_BUNDLE);
    final MultipleCurrencyAmount pvM = METHOD_VANILLA_BLACK.presentValue(vanillaM, SMILE_BUNDLE);
    final MultipleCurrencyAmount pvExpected = pvM.plus(pvP.multipliedBy(-1.0)).multipliedBy(1.0 / (strikeP - strikeM) * Math.abs(FOREX.getPaymentCurrency2().getAmount()));
    final MultipleCurrencyAmount pvComputed = METHOD_DIGITAL_SPREAD.presentValue(FOREX_DIGITAL_CALL_DOM, SMILE_BUNDLE);
    assertEquals("Forex Digital option: call spread method - present value", pvExpected.getAmount(USD), pvComputed.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value with an explicit computation.
   */
  public void presentValueDoubleQuadratic() {
    final Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR,
        Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    final SmileDeltaTermStructureParametersStrikeInterpolation smileTerm = TestsDataSetsForex.smile3points(REFERENCE_DATE, interpolator);
    final SmileDeltaTermStructureDataBundle smileBundle = new SmileDeltaTermStructureDataBundle(CURVES, smileTerm, Pairs.of(EUR, USD));
    final double strikeM = STRIKE * (1 - STANDARD_SPREAD);
    final double strikeP = STRIKE * (1 + STANDARD_SPREAD);
    final Forex forexM = new Forex(FOREX.getPaymentCurrency1().withAmount(1.0), FOREX.getPaymentCurrency2().withAmount(-strikeM));
    final Forex forexP = new Forex(FOREX.getPaymentCurrency1().withAmount(1.0), FOREX.getPaymentCurrency2().withAmount(-strikeP));
    final ForexOptionVanilla vanillaM = new ForexOptionVanilla(forexM, FOREX_DIGITAL_CALL_DOM.getExpirationTime(), IS_CALL, IS_LONG);
    final ForexOptionVanilla vanillaP = new ForexOptionVanilla(forexP, FOREX_DIGITAL_CALL_DOM.getExpirationTime(), IS_CALL, IS_LONG);
    final MultipleCurrencyAmount pvP = METHOD_VANILLA_BLACK.presentValue(vanillaP, smileBundle);
    final MultipleCurrencyAmount pvM = METHOD_VANILLA_BLACK.presentValue(vanillaM, smileBundle);
    final MultipleCurrencyAmount pvExpected = pvM.plus(pvP.multipliedBy(-1.0)).multipliedBy(1.0 / (strikeP - strikeM) * Math.abs(FOREX.getPaymentCurrency2().getAmount()));
    final MultipleCurrencyAmount pvComputed = METHOD_DIGITAL_SPREAD.presentValue(FOREX_DIGITAL_CALL_DOM, smileBundle);
    assertEquals("Forex Digital option: call spread method - present value", pvExpected.getAmount(USD), pvComputed.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests put call parity.
   */
  public void presentValuePutCallParityDomestic() {
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
    final Double pvCash = Math.abs(put.getUnderlyingForex().getPaymentCurrency2().accept(PVC, CURVES));
    assertEquals("Forex Digital option: call spread method - present value", pvCall.getAmount(USD) + pvPut.getAmount(USD), Math.abs(pvCash), TOLERANCE_PV_FLAT);
  }

  @Test
  /**
   * Tests put call parity.
   */
  public void presentValuePutCallParityForeign() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, payDate, notional, strike);
    final ForexOptionDigitalDefinition callDefinition = new ForexOptionDigitalDefinition(forexUnderlyingDefinition, expDate, isCall, isLong, false);
    final ForexOptionDigitalDefinition putDefinition = new ForexOptionDigitalDefinition(forexUnderlyingDefinition, expDate, !isCall, isLong, false);
    final ForexOptionDigital call = callDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final ForexOptionDigital put = putDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final MultipleCurrencyAmount pvCall = METHOD_DIGITAL_SPREAD.presentValue(call, SMILE_BUNDLE);
    final MultipleCurrencyAmount pvPut = METHOD_DIGITAL_SPREAD.presentValue(put, SMILE_BUNDLE);
    final Double pvCash = Math.abs(put.getUnderlyingForex().getPaymentCurrency1().accept(PVC, CURVES));
    assertEquals("Forex Digital option: call spread method - present value", pvCall.getAmount(EUR) + pvPut.getAmount(EUR), Math.abs(pvCash), TOLERANCE_PV_FLAT);
  }

  @Test
  /**
   * Tests the present value long/short parity.
   */
  public void presentValueLongShort() {
    final ForexOptionDigitalDefinition forexOptionShortDefinition = new ForexOptionDigitalDefinition(FOREX_DEFINITION, OPTION_EXP_DATE, IS_CALL, !IS_LONG);
    final InstrumentDerivative forexOptionShort = forexOptionShortDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final MultipleCurrencyAmount pvShort = METHOD_DIGITAL_SPREAD.presentValue(forexOptionShort, SMILE_BUNDLE);
    final MultipleCurrencyAmount pvLong = METHOD_DIGITAL_SPREAD.presentValue(FOREX_DIGITAL_CALL_DOM, SMILE_BUNDLE);
    assertEquals("Forex Digital option: present value long/short parity", pvLong.getAmount(USD), -pvShort.getAmount(USD), 1E-2);
    final MultipleCurrencyAmount ceShort = METHOD_DIGITAL_SPREAD.currencyExposure(forexOptionShort, SMILE_BUNDLE);
    final MultipleCurrencyAmount ceLong = METHOD_DIGITAL_SPREAD.currencyExposure(FOREX_DIGITAL_CALL_DOM, SMILE_BUNDLE);
    assertEquals("Forex Digital option: currency exposure long/short parity", ceLong.getAmount(USD), -ceShort.getAmount(USD), 1E-2);
    assertEquals("Forex Digital option: currency exposure long/short parity", ceLong.getAmount(EUR), -ceShort.getAmount(EUR), 1E-2);
  }

  @Test
  /**
   * Tests the currency exposure in a flat smile case.
   */
  public void currencyExposureFlatDomestic() {
    final MultipleCurrencyAmount ceSpread = METHOD_DIGITAL_SPREAD.currencyExposure(FOREX_DIGITAL_CALL_DOM, SMILE_BUNDLE_FLAT);
    final MultipleCurrencyAmount ceBlack = METHOD_DIGITAL_BLACK.currencyExposure(FOREX_DIGITAL_CALL_DOM, SMILE_BUNDLE_FLAT);
    assertEquals("Forex Digital option: call spread method - currency exposure", ceBlack.getAmount(USD), ceSpread.getAmount(USD), TOLERANCE_CE_FLAT);
    assertEquals("Forex Digital option: call spread method - currency exposure", ceBlack.getAmount(EUR), ceSpread.getAmount(EUR), TOLERANCE_CE_FLAT);
  }

  @Test
  /**
   * Tests the currency exposure with an explicit computation.
   */
  public void currencyExposureDomestic() {
    final double spread = 0.0001; // Relative spread.
    final double strikeM = STRIKE * (1 - spread);
    final double strikeP = STRIKE * (1 + spread);
    final Forex forexM = new Forex(FOREX.getPaymentCurrency1().withAmount(1.0), FOREX.getPaymentCurrency2().withAmount(-strikeM));
    final Forex forexP = new Forex(FOREX.getPaymentCurrency1().withAmount(1.0), FOREX.getPaymentCurrency2().withAmount(-strikeP));
    final ForexOptionVanilla vanillaM = new ForexOptionVanilla(forexM, FOREX_DIGITAL_CALL_DOM.getExpirationTime(), IS_CALL, IS_LONG);
    final ForexOptionVanilla vanillaP = new ForexOptionVanilla(forexP, FOREX_DIGITAL_CALL_DOM.getExpirationTime(), IS_CALL, IS_LONG);
    final MultipleCurrencyAmount ceP = METHOD_VANILLA_BLACK.currencyExposure(vanillaP, SMILE_BUNDLE);
    final MultipleCurrencyAmount ceM = METHOD_VANILLA_BLACK.currencyExposure(vanillaM, SMILE_BUNDLE);
    final MultipleCurrencyAmount ceExpected = ceM.plus(ceP.multipliedBy(-1.0)).multipliedBy(1.0 / (strikeP - strikeM) * Math.abs(FOREX.getPaymentCurrency2().getAmount()));
    final MultipleCurrencyAmount ceComputed = METHOD_DIGITAL_SPREAD.currencyExposure(FOREX_DIGITAL_CALL_DOM, SMILE_BUNDLE);
    assertEquals("Forex Digital option: call spread method - currency exposure", ceExpected.getAmount(USD), ceComputed.getAmount(USD), TOLERANCE_PV);
    assertEquals("Forex Digital option: call spread method - currency exposure", ceExpected.getAmount(EUR), ceComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the currency exposure with an explicit computation.
   */
  public void currencyExposureForeign() {
    final double strikeM = STRIKE * (1 - STANDARD_SPREAD);
    final double strikeP = STRIKE * (1 + STANDARD_SPREAD);
    final double amountPaid = Math.abs(FOREX_DIGITAL_CALL_FOR.getUnderlyingForex().getPaymentCurrency1().getAmount());
    final double strikeRelM = 1.0 / strikeP;
    final double strikeRelP = 1.0 / strikeM;
    final double amount = amountPaid / (strikeRelP - strikeRelM);
    final Forex forexM = new Forex(FOREX_DIGITAL_CALL_FOR.getUnderlyingForex().getPaymentCurrency2().withAmount(amount), FOREX_DIGITAL_CALL_FOR.getUnderlyingForex().getPaymentCurrency1()
        .withAmount(-strikeRelM * amount));
    final Forex forexP = new Forex(FOREX_DIGITAL_CALL_FOR.getUnderlyingForex().getPaymentCurrency2().withAmount(amount), FOREX_DIGITAL_CALL_FOR.getUnderlyingForex().getPaymentCurrency1()
        .withAmount(-strikeRelP * amount));
    final ForexOptionVanilla vanillaM = new ForexOptionVanilla(forexM, FOREX_DIGITAL_CALL_FOR.getExpirationTime(), !IS_CALL, false);
    final ForexOptionVanilla vanillaP = new ForexOptionVanilla(forexP, FOREX_DIGITAL_CALL_FOR.getExpirationTime(), !IS_CALL, true);
    final MultipleCurrencyAmount ceP = METHOD_VANILLA_BLACK.currencyExposure(vanillaP, SMILE_BUNDLE);
    final MultipleCurrencyAmount ceM = METHOD_VANILLA_BLACK.currencyExposure(vanillaM, SMILE_BUNDLE);
    final MultipleCurrencyAmount ceExpected = ceM.plus(ceP);
    final MultipleCurrencyAmount ceComputed = METHOD_DIGITAL_SPREAD.currencyExposure(FOREX_DIGITAL_CALL_FOR, SMILE_BUNDLE);
    assertEquals("Forex Digital option: call spread method - currency exposure", ceExpected.getAmount(USD), ceComputed.getAmount(USD), TOLERANCE_PV);
    assertEquals("Forex Digital option: call spread method - currency exposure", ceExpected.getAmount(EUR), ceComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the currency exposure with a FD rate shift.
   */
  public void currencyExposureForeign2() {
    final double shift = 0.000005;
    final FXMatrix fxMatrix = new FXMatrix(EUR, USD, SPOT);
    final FXMatrix fxMatrixP = new FXMatrix(EUR, USD, SPOT + shift);
    final YieldCurveBundle curves = new YieldCurveBundle(fxMatrix, CURVES.getCurrencyMap(), CURVES.getCurvesMap());
    final YieldCurveBundle curvesP = new YieldCurveBundle(fxMatrixP, CURVES.getCurrencyMap(), CURVES.getCurvesMap());
    final SmileDeltaTermStructureDataBundle smileBundle = new SmileDeltaTermStructureDataBundle(curves, SMILE_TERM_FLAT, Pairs.of(EUR, USD));
    final SmileDeltaTermStructureDataBundle smileBundleP = new SmileDeltaTermStructureDataBundle(curvesP, SMILE_TERM_FLAT, Pairs.of(EUR, USD));
    final MultipleCurrencyAmount ce = METHOD_DIGITAL_SPREAD.currencyExposure(FOREX_DIGITAL_CALL_FOR, smileBundle);
    final MultipleCurrencyAmount pv = METHOD_DIGITAL_SPREAD.presentValue(FOREX_DIGITAL_CALL_FOR, smileBundle);
    final MultipleCurrencyAmount pvP = METHOD_DIGITAL_SPREAD.presentValue(FOREX_DIGITAL_CALL_FOR, smileBundleP);
    assertEquals("Forex Digital option: call spread method - currency exposure - PL EUR", pvP.getAmount(EUR) - pv.getAmount(EUR), ce.getAmount(USD) * (1.0 / (SPOT + shift) - 1 / SPOT), TOLERANCE_PV);
    assertEquals("Forex Digital option: call spread method - currency exposure - PL USD", pvP.getAmount(EUR) * (SPOT + shift) - pv.getAmount(EUR) * SPOT, ce.getAmount(EUR) * (SPOT + shift - SPOT),
        TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the currency exposure against the present value.
   */
  public void currencyExposureVsPresentValue() {
    final MultipleCurrencyAmount pv = METHOD_DIGITAL_SPREAD.presentValue(FOREX_DIGITAL_CALL_DOM, SMILE_BUNDLE);
    final MultipleCurrencyAmount ce = METHOD_DIGITAL_SPREAD.currencyExposure(FOREX_DIGITAL_CALL_DOM, SMILE_BUNDLE);
    assertEquals("Forex Digital option: call spread method - currency exposure vs present value", ce.getAmount(USD) + ce.getAmount(EUR) * SPOT, pv.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the put/call parity currency exposure.
   */
  public void currencyExposurePutCallParityDomestic() {
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
    final Double pvCash = forexOptionPut.getUnderlyingForex().getPaymentCurrency2().accept(PVC, CURVES);
    assertEquals("Forex Digital option: currency exposure put/call parity foreign", 0, currencyExposureCall.getAmount(EUR) + currencyExposurePut.getAmount(EUR), TOLERANCE_PV);
    assertEquals("Forex Digital option: currency exposure put/call parity domestic", Math.abs(pvCash), currencyExposureCall.getAmount(USD) + currencyExposurePut.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the put/call parity currency exposure.
   */
  public void currencyExposurePutCallParityForeign() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, payDate, notional, strike);
    final ForexOptionDigitalDefinition forexOptionDefinitionCall = new ForexOptionDigitalDefinition(forexUnderlyingDefinition, expDate, isCall, isLong, false);
    final ForexOptionDigitalDefinition forexOptionDefinitionPut = new ForexOptionDigitalDefinition(forexUnderlyingDefinition, expDate, !isCall, isLong, false);
    final ForexOptionDigital forexOptionCall = forexOptionDefinitionCall.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final ForexOptionDigital forexOptionPut = forexOptionDefinitionPut.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final MultipleCurrencyAmount currencyExposureCall = METHOD_DIGITAL_SPREAD.currencyExposure(forexOptionCall, SMILE_BUNDLE);
    final MultipleCurrencyAmount currencyExposurePut = METHOD_DIGITAL_SPREAD.currencyExposure(forexOptionPut, SMILE_BUNDLE);
    final Double pvCash = forexOptionPut.getUnderlyingForex().getPaymentCurrency1().accept(PVC, CURVES);
    assertEquals("Forex Digital option: currency exposure put/call parity foreign", 0, currencyExposureCall.getAmount(USD) + currencyExposurePut.getAmount(USD), TOLERANCE_PV);
    assertEquals("Forex Digital option: currency exposure put/call parity domestic", Math.abs(pvCash), currencyExposureCall.getAmount(EUR) + currencyExposurePut.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the gamma for Forex option. Payment in domestic currency.
   */
  public void gammaDomestic() {
    final double strikeM = STRIKE * (1 - STANDARD_SPREAD);
    final double strikeP = STRIKE * (1 + STANDARD_SPREAD);
    final double notional = Math.abs(FOREX.getPaymentCurrency2().getAmount()) / (strikeP - strikeM);
    final Forex forexM = new Forex(FOREX.getPaymentCurrency1().withAmount(notional), FOREX.getPaymentCurrency2().withAmount(-notional * strikeM));
    final Forex forexP = new Forex(FOREX.getPaymentCurrency1().withAmount(notional), FOREX.getPaymentCurrency2().withAmount(-notional * strikeP));
    final ForexOptionVanilla vanillaM = new ForexOptionVanilla(forexM, FOREX_DIGITAL_CALL_DOM.getExpirationTime(), IS_CALL, IS_LONG);
    final ForexOptionVanilla vanillaP = new ForexOptionVanilla(forexP, FOREX_DIGITAL_CALL_DOM.getExpirationTime(), IS_CALL, !IS_LONG);
    final CurrencyAmount gammaP = METHOD_VANILLA_BLACK.gamma(vanillaP, SMILE_BUNDLE, true);
    final CurrencyAmount gammaM = METHOD_VANILLA_BLACK.gamma(vanillaM, SMILE_BUNDLE, true);
    final CurrencyAmount gammaExpected = gammaM.plus(gammaP);
    final CurrencyAmount gammaComputed = METHOD_DIGITAL_SPREAD.gamma(FOREX_DIGITAL_CALL_DOM, SMILE_BUNDLE);
    assertEquals("Forex Digital option: call spread method - gamma", gammaExpected.getAmount(), gammaComputed.getAmount(), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the gamma for Forex option. Payment in foreign currency.
   */
  public void gammaForeign() {
    final ForexOptionDigitalDefinition digitalForeignDefinition = new ForexOptionDigitalDefinition(FOREX_DEFINITION, OPTION_EXP_DATE, IS_CALL, IS_LONG, false);
    final ForexOptionDigital digitalForeign = digitalForeignDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final double strikeM = STRIKE * (1 - STANDARD_SPREAD);
    final double strikeP = STRIKE * (1 + STANDARD_SPREAD);
    final double amountPaid = Math.abs(digitalForeign.getUnderlyingForex().getPaymentCurrency1().getAmount());
    final double strikeRelM = 1.0 / strikeP;
    final double strikeRelP = 1.0 / strikeM;
    final double amount = amountPaid / (strikeRelP - strikeRelM);
    final Forex forexM = new Forex(digitalForeign.getUnderlyingForex().getPaymentCurrency2().withAmount(amount), digitalForeign.getUnderlyingForex().getPaymentCurrency1()
        .withAmount(-strikeRelM * amount));
    final Forex forexP = new Forex(digitalForeign.getUnderlyingForex().getPaymentCurrency2().withAmount(amount), digitalForeign.getUnderlyingForex().getPaymentCurrency1()
        .withAmount(-strikeRelP * amount));
    final ForexOptionVanilla vanillaM = new ForexOptionVanilla(forexM, digitalForeign.getExpirationTime(), !IS_CALL, false);
    final ForexOptionVanilla vanillaP = new ForexOptionVanilla(forexP, digitalForeign.getExpirationTime(), !IS_CALL, true);
    final CurrencyAmount gammaP = METHOD_VANILLA_BLACK.gamma(vanillaP, SMILE_BUNDLE, false);
    final CurrencyAmount gammaM = METHOD_VANILLA_BLACK.gamma(vanillaM, SMILE_BUNDLE, false);
    final CurrencyAmount gammaExpected = gammaM.plus(gammaP);
    final CurrencyAmount gammaComputed = METHOD_DIGITAL_SPREAD.gamma(digitalForeign, SMILE_BUNDLE);
    assertEquals("Forex Digital option: call spread method - gamma", gammaExpected.getAmount(), gammaComputed.getAmount(), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the gamma for Forex option. Payment in domestic currency.
   */
  public void gammaSpotDomestic() {
    final double strikeM = STRIKE * (1 - STANDARD_SPREAD);
    final double strikeP = STRIKE * (1 + STANDARD_SPREAD);
    final double notional = Math.abs(FOREX.getPaymentCurrency2().getAmount()) / (strikeP - strikeM);
    final Forex forexM = new Forex(FOREX.getPaymentCurrency1().withAmount(notional), FOREX.getPaymentCurrency2().withAmount(-notional * strikeM));
    final Forex forexP = new Forex(FOREX.getPaymentCurrency1().withAmount(notional), FOREX.getPaymentCurrency2().withAmount(-notional * strikeP));
    final ForexOptionVanilla vanillaM = new ForexOptionVanilla(forexM, FOREX_DIGITAL_CALL_DOM.getExpirationTime(), IS_CALL, IS_LONG);
    final ForexOptionVanilla vanillaP = new ForexOptionVanilla(forexP, FOREX_DIGITAL_CALL_DOM.getExpirationTime(), IS_CALL, !IS_LONG);
    final CurrencyAmount gammaP = METHOD_VANILLA_BLACK.gammaSpot(vanillaP, SMILE_BUNDLE, true);
    final CurrencyAmount gammaM = METHOD_VANILLA_BLACK.gammaSpot(vanillaM, SMILE_BUNDLE, true);
    final CurrencyAmount gammaExpected = gammaM.plus(gammaP);
    final CurrencyAmount gammaComputed = METHOD_DIGITAL_SPREAD.gammaSpot(FOREX_DIGITAL_CALL_DOM, SMILE_BUNDLE);
    assertEquals("Forex Digital option: call spread method - gamma spot", gammaExpected.getAmount(), gammaComputed.getAmount(), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the gamma for Forex option. Payment in foreign currency.
   */
  public void gammaSpotForeign() {
    final ForexOptionDigitalDefinition digitalForeignDefinition = new ForexOptionDigitalDefinition(FOREX_DEFINITION, OPTION_EXP_DATE, IS_CALL, IS_LONG, false);
    final ForexOptionDigital digitalForeign = digitalForeignDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final double strikeM = STRIKE * (1 - STANDARD_SPREAD);
    final double strikeP = STRIKE * (1 + STANDARD_SPREAD);
    final double amountPaid = Math.abs(digitalForeign.getUnderlyingForex().getPaymentCurrency1().getAmount());
    final double strikeRelM = 1.0 / strikeP;
    final double strikeRelP = 1.0 / strikeM;
    final double amount = amountPaid / (strikeRelP - strikeRelM);
    final Forex forexM = new Forex(digitalForeign.getUnderlyingForex().getPaymentCurrency2().withAmount(amount), digitalForeign.getUnderlyingForex().getPaymentCurrency1()
        .withAmount(-strikeRelM * amount));
    final Forex forexP = new Forex(digitalForeign.getUnderlyingForex().getPaymentCurrency2().withAmount(amount), digitalForeign.getUnderlyingForex().getPaymentCurrency1()
        .withAmount(-strikeRelP * amount));
    final ForexOptionVanilla vanillaM = new ForexOptionVanilla(forexM, digitalForeign.getExpirationTime(), !IS_CALL, false);
    final ForexOptionVanilla vanillaP = new ForexOptionVanilla(forexP, digitalForeign.getExpirationTime(), !IS_CALL, true);
    final CurrencyAmount gammaP = METHOD_VANILLA_BLACK.gammaSpot(vanillaP, SMILE_BUNDLE, false);
    final CurrencyAmount gammaM = METHOD_VANILLA_BLACK.gammaSpot(vanillaM, SMILE_BUNDLE, false);
    final CurrencyAmount gammaExpected = gammaM.plus(gammaP);
    final CurrencyAmount gammaComputed = METHOD_DIGITAL_SPREAD.gammaSpot(digitalForeign, SMILE_BUNDLE);
    assertEquals("Forex Digital option: call spread method - gamma spot", gammaExpected.getAmount(), gammaComputed.getAmount(), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the gamma for Forex option. Payment in foreign currency.
   */
  public void gammaSpotMethodVsCalculator() {
    final GammaSpotCallSpreadBlackForexCalculator calculator = new GammaSpotCallSpreadBlackForexCalculator(STANDARD_SPREAD);
    final ForexOptionDigitalDefinition digitalForeignDefinition = new ForexOptionDigitalDefinition(FOREX_DEFINITION, OPTION_EXP_DATE, IS_CALL, IS_LONG, false);
    final ForexOptionDigital digitalForeign = digitalForeignDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final CurrencyAmount gammaForeignMethod = METHOD_DIGITAL_SPREAD.gammaSpot(digitalForeign, SMILE_BUNDLE);
    final CurrencyAmount gammaForeignCalculator = digitalForeign.accept(calculator, SMILE_BUNDLE);
    assertEquals("Forex Digital option: call spread method - gamma spot", gammaForeignCalculator.getAmount(), gammaForeignMethod.getAmount(), TOLERANCE_PV);
    final CurrencyAmount gammaDomesticMethod = METHOD_DIGITAL_SPREAD.gammaSpot(FOREX_DIGITAL_CALL_DOM, SMILE_BUNDLE);
    final CurrencyAmount gammaDomesticCalculator = FOREX_DIGITAL_CALL_DOM.accept(calculator, SMILE_BUNDLE);
    assertEquals("Forex Digital option: call spread method - gamma spot", gammaDomesticCalculator.getAmount(), gammaDomesticMethod.getAmount(), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the relative gamma for Forex option.
   */
  public void gammaRelative() {
    final CurrencyAmount gamma = METHOD_DIGITAL_SPREAD.gamma(FOREX_DIGITAL_CALL_DOM, SMILE_BUNDLE);
    final double gammaRelativeExpected = gamma.getAmount() / Math.abs(FOREX_DIGITAL_CALL_DOM.getUnderlyingForex().getPaymentCurrency2().getAmount());
    final double gammaRelativeComputed = METHOD_DIGITAL_SPREAD.gammaRelative(FOREX_DIGITAL_CALL_DOM, SMILE_BUNDLE);
    assertEquals("Forex: relative gamma", gammaRelativeExpected, gammaRelativeComputed, TOLERANCE_RELATIVE);
  }

  @Test
  /**
   * Tests the present value curve sensitivity.
   */
  public void presentValueCurveSensitivity() {
    final double spread = 0.0001; // Relative spread.
    final double strikeM = STRIKE * (1 - spread);
    final double strikeP = STRIKE * (1 + spread);
    final Forex forexM = new Forex(FOREX.getPaymentCurrency1().withAmount(1.0), FOREX.getPaymentCurrency2().withAmount(-strikeM));
    final Forex forexP = new Forex(FOREX.getPaymentCurrency1().withAmount(1.0), FOREX.getPaymentCurrency2().withAmount(-strikeP));
    final ForexOptionVanilla vanillaM = new ForexOptionVanilla(forexM, FOREX_DIGITAL_CALL_DOM.getExpirationTime(), IS_CALL, IS_LONG);
    final ForexOptionVanilla vanillaP = new ForexOptionVanilla(forexP, FOREX_DIGITAL_CALL_DOM.getExpirationTime(), IS_CALL, !IS_LONG);
    final MultipleCurrencyInterestRateCurveSensitivity pvcsP = METHOD_VANILLA_BLACK.presentValueCurveSensitivity(vanillaP, SMILE_BUNDLE);
    final MultipleCurrencyInterestRateCurveSensitivity pvcsM = METHOD_VANILLA_BLACK.presentValueCurveSensitivity(vanillaM, SMILE_BUNDLE);
    final MultipleCurrencyInterestRateCurveSensitivity pvcsExpected = pvcsM.plus(pvcsP).multipliedBy(1.0 / (strikeP - strikeM) * Math.abs(FOREX.getPaymentCurrency2().getAmount()));
    final MultipleCurrencyInterestRateCurveSensitivity pvcsComputed = METHOD_DIGITAL_SPREAD.presentValueCurveSensitivity(FOREX_DIGITAL_CALL_DOM, SMILE_BUNDLE);
    AssertSensivityObjects.assertEquals("Forex Digital option: call spread method - present value", pvcsExpected, pvcsComputed, TOLERANCE_DELTA);
  }

  @Test
  /**
   * Tests the present value curve sensitivity.
   */
  public void presentValueBlackVolatilitySensitivity() {
    final double strikeM = STRIKE * (1 - STANDARD_SPREAD);
    final double strikeP = STRIKE * (1 + STANDARD_SPREAD);
    final Forex forexM = new Forex(FOREX.getPaymentCurrency1().withAmount(1.0), FOREX.getPaymentCurrency2().withAmount(-strikeM));
    final Forex forexP = new Forex(FOREX.getPaymentCurrency1().withAmount(1.0), FOREX.getPaymentCurrency2().withAmount(-strikeP));
    final ForexOptionVanilla vanillaM = new ForexOptionVanilla(forexM, FOREX_DIGITAL_CALL_DOM.getExpirationTime(), IS_CALL, IS_LONG);
    final ForexOptionVanilla vanillaP = new ForexOptionVanilla(forexP, FOREX_DIGITAL_CALL_DOM.getExpirationTime(), IS_CALL, IS_LONG);
    final PresentValueForexBlackVolatilitySensitivity pvbvP = METHOD_VANILLA_BLACK.presentValueBlackVolatilitySensitivity(vanillaP, SMILE_BUNDLE);
    final PresentValueForexBlackVolatilitySensitivity pvbvM = METHOD_VANILLA_BLACK.presentValueBlackVolatilitySensitivity(vanillaM, SMILE_BUNDLE);
    final PresentValueForexBlackVolatilitySensitivity pvbvExpected = pvbvM.plus(pvbvP.multipliedBy(-1.0)).multipliedBy(1.0 / (strikeP - strikeM) * Math.abs(FOREX.getPaymentCurrency2().getAmount()));
    final PresentValueForexBlackVolatilitySensitivity pvbvComputed = METHOD_DIGITAL_SPREAD.presentValueBlackVolatilitySensitivity(FOREX_DIGITAL_CALL_DOM, SMILE_BUNDLE);
    assertEquals("Forex Digital option: call spread method - present value volatility sensitivity", pvbvComputed.getVega().getMap().size(), 2);
    assertTrue("Forex Digital option: call spread method - present value volatility sensitivity", PresentValueForexBlackVolatilitySensitivity.compare(pvbvExpected, pvbvComputed, TOLERANCE_DELTA));
  }

  @Test
  /**
   * Tests the present value. Spread change.
   */
  public void presentValueSpreadChange() {
    final double spread1 = 0.0001;
    final double spread2 = 0.0002;
    final ForexOptionDigitalCallSpreadBlackMethod methodCallSpreadBlack1 = new ForexOptionDigitalCallSpreadBlackMethod(spread1);
    final ForexOptionDigitalCallSpreadBlackMethod methodCallSpreadBlack2 = new ForexOptionDigitalCallSpreadBlackMethod(spread2);
    final MultipleCurrencyAmount pv1 = methodCallSpreadBlack1.presentValue(FOREX_DIGITAL_CALL_DOM, SMILE_BUNDLE);
    final MultipleCurrencyAmount pv2 = methodCallSpreadBlack2.presentValue(FOREX_DIGITAL_CALL_DOM, SMILE_BUNDLE);
    assertEquals("Forex Digital option: call spread method - present value", pv1.getAmount(USD), pv2.getAmount(USD), 10.0);
    //    MultipleCurrencyAmount pvBlack = METHOD_DIGITAL_BLACK.presentValue(FOREX_CALL_OPTION, SMILE_BUNDLE);
    //    assertEquals("Forex Digital option: call spread method - present value", pvBlack.getAmount(USD), pv2.getAmount(USD), 10.0); // Should fail
  }

  @Test
  /**
   * Tests the present value. Method vs Calculator.
   */
  public void presentValueMethodVCalculator() {
    final MultipleCurrencyAmount pv1 = METHOD_DIGITAL_SPREAD.presentValue(FOREX_DIGITAL_CALL_DOM, SMILE_BUNDLE);
    final PresentValueCallSpreadBlackForexCalculator calculator = new PresentValueCallSpreadBlackForexCalculator(STANDARD_SPREAD);
    final MultipleCurrencyAmount pvCalculator = FOREX_DIGITAL_CALL_DOM.accept(calculator, SMILE_BUNDLE);
    assertEquals("Forex Digital option: call spread method - present value", pv1.getAmount(USD), pvCalculator.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the currency exposure. Method vs Calculator.
   */
  public void currencyExposureMethodVCalculator() {
    final MultipleCurrencyAmount ceMethod = METHOD_DIGITAL_SPREAD.currencyExposure(FOREX_DIGITAL_CALL_DOM, SMILE_BUNDLE);
    final CurrencyExposureCallSpreadBlackForexCalculator calculator = new CurrencyExposureCallSpreadBlackForexCalculator(STANDARD_SPREAD);
    final MultipleCurrencyAmount ceCalculator = FOREX_DIGITAL_CALL_DOM.accept(calculator, SMILE_BUNDLE);
    assertEquals("Forex Digital option: call spread method - currency exposure", ceMethod.getAmount(USD), ceCalculator.getAmount(USD), TOLERANCE_PV);
    assertEquals("Forex Digital option: call spread method - currency exposure", ceMethod.getAmount(EUR), ceCalculator.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value curve sensitivity. Method vs Calculator.
   */
  public void presentValueCurveSensitivityMethodVCalculator() {
    final MultipleCurrencyInterestRateCurveSensitivity pvcsMethod = METHOD_DIGITAL_SPREAD.presentValueCurveSensitivity(FOREX_DIGITAL_CALL_DOM, SMILE_BUNDLE);
    final PresentValueCurveSensitivityCallSpreadBlackForexCalculator calculator = new PresentValueCurveSensitivityCallSpreadBlackForexCalculator(STANDARD_SPREAD);
    final MultipleCurrencyInterestRateCurveSensitivity pvcsCalculator = FOREX_DIGITAL_CALL_DOM.accept(calculator, SMILE_BUNDLE);
    AssertSensivityObjects.assertEquals("Forex Digital option: call spread method - present value", pvcsMethod, pvcsCalculator, TOLERANCE_DELTA);
  }

  @Test
  /**
   * Tests the present value volatility sensitivity.  Method vs Calculator.
   */
  public void presentValueBlackVolatilitySensitivityMethodVCalculator() {
    final PresentValueForexBlackVolatilitySensitivity pvbvMethod = METHOD_DIGITAL_SPREAD.presentValueBlackVolatilitySensitivity(FOREX_DIGITAL_CALL_DOM, SMILE_BUNDLE);
    final PresentValueBlackVolatilitySensitivityCallSpreadBlackForexCalculator calculator = new PresentValueBlackVolatilitySensitivityCallSpreadBlackForexCalculator(STANDARD_SPREAD);
    final PresentValueForexBlackVolatilitySensitivity pvbvCalculator = FOREX_DIGITAL_CALL_DOM.accept(calculator, SMILE_BUNDLE);
    assertTrue("Forex Digital option: call spread method - present value volatility sensitivity", PresentValueForexBlackVolatilitySensitivity.compare(pvbvMethod, pvbvCalculator, TOLERANCE_DELTA));
  }

  @Test
  /**
   * Tests present value volatility node sensitivity.
   */
  public void presentValueBlackVolatilityNodeSensitivity() {
    final double strikeM = STRIKE * (1 - STANDARD_SPREAD);
    final double strikeP = STRIKE * (1 + STANDARD_SPREAD);
    final PresentValueForexBlackVolatilityNodeSensitivityDataBundle sensi = METHOD_DIGITAL_SPREAD.presentValueBlackVolatilityNodeSensitivity(FOREX_DIGITAL_CALL_DOM, SMILE_BUNDLE);
    assertEquals("Forex vanilla option: vega node size", SMILE_TERM.getNumberExpiration(), sensi.getVega().getData().length);
    assertEquals("Forex vanilla option: vega node size", SMILE_TERM.getNumberStrike(), sensi.getVega().getData()[0].length);
    final Pair<Currency, Currency> currencyPair = Pairs.of(EUR, USD);
    assertEquals("Forex vanilla option: vega", currencyPair, sensi.getCurrencyPair());
    final PresentValueForexBlackVolatilitySensitivity pointSensitivity = METHOD_DIGITAL_SPREAD.presentValueBlackVolatilitySensitivity(FOREX_DIGITAL_CALL_DOM, SMILE_BUNDLE);
    final double df = CURVES.getCurve(CURVES_NAME[1]).getDiscountFactor(TimeCalculator.getTimeBetween(REFERENCE_DATE, OPTION_PAY_DATE));
    final double forward = SPOT * CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(TimeCalculator.getTimeBetween(REFERENCE_DATE, OPTION_PAY_DATE)) / df;
    final VolatilityAndBucketedSensitivities volAndSensitivitiesDown = SMILE_TERM.getVolatilityAndSensitivities(FOREX_DIGITAL_CALL_DOM.getExpirationTime(), strikeM, forward);
    final VolatilityAndBucketedSensitivities volAndSensitivitiesUp = SMILE_TERM.getVolatilityAndSensitivities(FOREX_DIGITAL_CALL_DOM.getExpirationTime(), strikeP, forward);
    final double[][] nodeWeightM = volAndSensitivitiesDown.getBucketedSensitivities();
    final double[][] nodeWeightP = volAndSensitivitiesUp.getBucketedSensitivities();
    final DoublesPair pointM = DoublesPair.of(FOREX_DIGITAL_CALL_DOM.getExpirationTime(), strikeM);
    final DoublesPair pointP = DoublesPair.of(FOREX_DIGITAL_CALL_DOM.getExpirationTime(), strikeP);
    for (int loopexp = 0; loopexp < SMILE_TERM.getNumberExpiration(); loopexp++) {
      for (int loopstrike = 0; loopstrike < SMILE_TERM.getNumberStrike(); loopstrike++) {
        assertEquals("Forex vanilla digital: vega node", nodeWeightM[loopexp][loopstrike] * pointSensitivity.getVega().getMap().get(pointM) + nodeWeightP[loopexp][loopstrike]
            * pointSensitivity.getVega().getMap().get(pointP), sensi.getVega().getData()[loopexp][loopstrike], TOLERANCE_DELTA);
      }
    }
  }

  @Test
  /**
   * Tests present value volatility node sensitivity.
   */
  public void presentValueBlackVolatilityNodeSensitivityMethodVCalculator() {
    final PresentValueForexBlackVolatilityNodeSensitivityDataBundle pvbnsMethod = METHOD_DIGITAL_SPREAD.presentValueBlackVolatilityNodeSensitivity(FOREX_DIGITAL_CALL_DOM, SMILE_BUNDLE);
    final PresentValueBlackVolatilityNodeSensitivityCallSpreadBlackForexCalculator calculator = new PresentValueBlackVolatilityNodeSensitivityCallSpreadBlackForexCalculator(STANDARD_SPREAD);
    final PresentValueForexBlackVolatilityNodeSensitivityDataBundle pvbnsCalculator = FOREX_DIGITAL_CALL_DOM.accept(calculator, SMILE_BUNDLE);
    assertTrue("Forex Digital option: call spread method - Black node sensitivity", PresentValueForexBlackVolatilityNodeSensitivityDataBundle.compare(pvbnsMethod, pvbnsCalculator, TOLERANCE_DELTA));
  }

  @Test
  /**
   * Tests the Theta (1 day change of pv) for forex options transactions.
   */
  public void thetaBeforeExpiration() {
    final MultipleCurrencyAmount theta = THETAC.getTheta(FOREX_DIGITAL_CALL_DOM_DEFINITION, REFERENCE_DATE, CURVES_NAME, SMILE_BUNDLE, PVC_CALLSPREAD, 1);
    final ForexOptionDigital swapToday = FOREX_DIGITAL_CALL_DOM_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final ForexOptionDigital swapTomorrow = FOREX_DIGITAL_CALL_DOM_DEFINITION.toDerivative(REFERENCE_DATE.plusDays(1), CURVES_NAME);
    final MultipleCurrencyAmount pvToday = swapToday.accept(PVC_CALLSPREAD, SMILE_BUNDLE);
    final YieldCurveBundle tomorrowData = FX_OPTION_ROLLDOWN.rollDown(SMILE_BUNDLE, TimeCalculator.getTimeBetween(REFERENCE_DATE, REFERENCE_DATE.plusDays(1)));
    final MultipleCurrencyAmount pvTomorrow = swapTomorrow.accept(PVC_CALLSPREAD, tomorrowData);
    final MultipleCurrencyAmount thetaExpected = pvTomorrow.plus(pvToday.multipliedBy(-1.0));
    assertEquals("ThetaCalculator: forex option", thetaExpected.getAmount(USD), theta.getAmount(USD), TOLERANCE_PV);
  }

  @Test(enabled = false)
  /**
   * Analyzes the profile for digital options.
   */
  public void profile() {
    final Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR,
        Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    final SmileDeltaTermStructureParametersStrikeInterpolation smileTerm = TestsDataSetsForex.smile5points(REFERENCE_DATE, interpolator);
    final SmileDeltaTermStructureDataBundle smileBundle = new SmileDeltaTermStructureDataBundle(CURVES, smileTerm, Pairs.of(EUR, USD));

    final int nbStrike = 100;
    final double range = 0.40; // Spot = 1.40
    final double shift = 2 * range / nbStrike;
    final double[] strike = new double[nbStrike + 1];
    final ForexOptionDigital[] forexOptionDigital = new ForexOptionDigital[nbStrike + 1];
    final ForexOptionVanilla[] forexOptionVanilla = new ForexOptionVanilla[nbStrike + 1];
    for (int loopstrike = 0; loopstrike <= nbStrike; loopstrike++) {
      strike[loopstrike] = SPOT - range + loopstrike * shift;
      final ForexDefinition forexDefinitonUSD = ForexDefinition.fromAmounts(EUR, USD, OPTION_EXP_DATE, -NOTIONAL / strike[loopstrike], NOTIONAL);
      final ForexDefinition forexDefinitonEUR = ForexDefinition.fromAmounts(EUR, USD, OPTION_EXP_DATE, -1.0, strike[loopstrike]);
      final ForexOptionDigitalDefinition forexOptionDigitalDefiniton = new ForexOptionDigitalDefinition(forexDefinitonUSD, OPTION_EXP_DATE, IS_CALL, IS_LONG);
      final ForexOptionVanillaDefinition forexOptionVanillaDefiniton = new ForexOptionVanillaDefinition(forexDefinitonEUR, OPTION_EXP_DATE, IS_CALL, IS_LONG);
      forexOptionDigital[loopstrike] = forexOptionDigitalDefiniton.toDerivative(REFERENCE_DATE, CURVES_NAME);
      forexOptionVanilla[loopstrike] = forexOptionVanillaDefiniton.toDerivative(REFERENCE_DATE, CURVES_NAME);
    }
    final double[] pvDigitalSpread = new double[nbStrike + 1];
    final double[] pvDigitalBlack = new double[nbStrike + 1];
    final double[] pvVanillaBlack = new double[nbStrike + 1];
    final double[] gammaDigitalSpread = new double[nbStrike + 1];
    for (int loopstrike = 0; loopstrike <= nbStrike; loopstrike++) {
      pvDigitalSpread[loopstrike] = METHOD_DIGITAL_SPREAD.presentValue(forexOptionDigital[loopstrike], smileBundle).getAmount(USD);
      pvDigitalBlack[loopstrike] = METHOD_DIGITAL_BLACK.presentValue(forexOptionDigital[loopstrike], smileBundle).getAmount(USD);
      pvVanillaBlack[loopstrike] = METHOD_VANILLA_BLACK.presentValue(forexOptionVanilla[loopstrike], smileBundle).getAmount(USD);
      gammaDigitalSpread[loopstrike] = METHOD_DIGITAL_SPREAD.gamma(forexOptionDigital[loopstrike], smileBundle).getAmount();
    }

    final double dfDomestic = smileBundle.getCurve(forexOptionDigital[0].getUnderlyingForex().getPaymentCurrency2().getFundingCurveName()).getDiscountFactor(
        forexOptionDigital[0].getUnderlyingForex().getPaymentTime());
    final double dfForeign = smileBundle.getCurve(forexOptionDigital[0].getUnderlyingForex().getPaymentCurrency1().getFundingCurveName()).getDiscountFactor(
        forexOptionDigital[0].getUnderlyingForex().getPaymentTime());
    final double forward = SPOT * dfForeign / dfDomestic;
    final double[] volBlack = new double[nbStrike + 1];
    for (int loopstrike = 0; loopstrike <= nbStrike; loopstrike++) {
      volBlack[loopstrike] = FXVolatilityUtils.getVolatility(smileBundle, EUR, USD, forexOptionDigital[loopstrike].getExpirationTime(), strike[loopstrike], forward);
    }
    final double[] density = new double[nbStrike - 1];
    for (int loopstrike = 0; loopstrike < nbStrike - 1; loopstrike++) {
      density[loopstrike] = (pvVanillaBlack[loopstrike + 2] + pvVanillaBlack[loopstrike] - 2 * pvVanillaBlack[loopstrike + 1]) / (shift * shift);
    }
  }

}
