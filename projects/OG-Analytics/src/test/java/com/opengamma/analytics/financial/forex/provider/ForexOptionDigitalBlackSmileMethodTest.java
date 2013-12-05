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
import com.opengamma.analytics.financial.forex.definition.ForexOptionDigitalDefinition;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilityNodeSensitivityDataBundle;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.financial.provider.calculator.blackforex.CurrencyExposureForexBlackSmileCalculator;
import com.opengamma.analytics.financial.provider.calculator.blackforex.PresentValueCurveSensitivityForexBlackSmileCalculator;
import com.opengamma.analytics.financial.provider.calculator.blackforex.PresentValueForexBlackSmileCalculator;
import com.opengamma.analytics.financial.provider.calculator.blackforex.PresentValueForexVolatilitySensitivityForexBlackSmileCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderDiscount;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.blackforex.ParameterSensitivityForexBlackSmileDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;
import com.opengamma.util.tuple.Triple;

/**
 * Tests related to the pricing method for digital Forex option transactions with Black function and a volatility provider.
 */
@Test(groups = TestGroup.UNIT)
public class ForexOptionDigitalBlackSmileMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountForexDataSets.createMulticurvesForex();

  // General
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final int SETTLEMENT_DAYS = 2;
  // Smile data
  private static final Currency EUR = Currency.EUR;
  private static final Currency USD = Currency.USD;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 6, 13);
  private static final SmileDeltaTermStructureParametersStrikeInterpolation SMILE_TERM = ForexSmileProviderDataSets.smile5points(REFERENCE_DATE);
  private static final SmileDeltaTermStructureParametersStrikeInterpolation SMILE_TERM_FLAT = ForexSmileProviderDataSets.smileFlat(REFERENCE_DATE);
  private static final BlackForexSmileProviderDiscount SMILE_MULTICURVES = new BlackForexSmileProviderDiscount(MULTICURVES, SMILE_TERM, Pairs.of(EUR, USD));
  private static final BlackForexSmileProviderDiscount SMILE_FLAT_MULTICURVES = new BlackForexSmileProviderDiscount(MULTICURVES, SMILE_TERM_FLAT, Pairs.of(EUR, USD));
  // Methods and curves
  private static final double SPOT = MULTICURVES.getFxRate(EUR, USD);
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);
  private static final ForexOptionDigitalBlackSmileMethod METHOD_BLACK_DIGITAL = ForexOptionDigitalBlackSmileMethod.getInstance();

  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();

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
  private static final ForexOptionDigitalDefinition FOREX_DIGITAL_CALL_DOM_DEFINITION = new ForexOptionDigitalDefinition(FOREX_DEFINITION, OPTION_EXP_DATE, IS_CALL,
      IS_LONG, true);
  private static final ForexOptionDigital FOREX_DIGITAL_CALL_DOM = FOREX_DIGITAL_CALL_DOM_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final ForexOptionDigitalDefinition FOREX_DIGITAL_CALL_FOR_DEFINITION = new ForexOptionDigitalDefinition(FOREX_DEFINITION, OPTION_EXP_DATE, IS_CALL,
      IS_LONG, false);
  private static final ForexOptionDigital FOREX_DIGITAL_CALL_FOR = FOREX_DIGITAL_CALL_FOR_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2; // 0.01 currency unit for 1 bp

  @Test
  /**
   * Tests the present value at a time grid point.
   */
  public void persentValueAtGridPoint() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime expiryDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofYears(1), BUSINESS_DAY, CALENDAR, true);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, ScheduleCalculator.getAdjustedDate(expiryDate, SETTLEMENT_DAYS, CALENDAR), notional,
        strike);
    final ForexOptionDigitalDefinition forexOptionDefinition = new ForexOptionDigitalDefinition(forexUnderlyingDefinition, expiryDate, isCall, isLong);
    final ForexOptionDigital forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE);
    final double dfDomestic = MULTICURVES.getDiscountFactor(USD, forexOption.getUnderlyingForex().getPaymentTime());
    final double dfForeign = MULTICURVES.getDiscountFactor(EUR, forexOption.getUnderlyingForex().getPaymentTime());
    final double forward = SPOT * dfForeign / dfDomestic;
    final double volatility = SMILE_TERM.getVolatility(Triple.of(TimeCalculator.getTimeBetween(REFERENCE_DATE, expiryDate), strike, forward));
    final double sigmaRootT = volatility * Math.sqrt(forexOption.getExpirationTime());
    final double dM = Math.log(forward / strike) / sigmaRootT - 0.5 * sigmaRootT;
    final int omega = isCall ? 1 : -1;
    final double pvExpected = Math.abs(forexOption.getUnderlyingForex().getPaymentCurrency2().getAmount()) * dfDomestic * NORMAL.getCDF(omega * dM)
        * (isLong ? 1.0 : -1.0);
    final MultipleCurrencyAmount pvComputed = METHOD_BLACK_DIGITAL.presentValue(forexOption, SMILE_MULTICURVES);
    assertEquals("Forex Digital option: present value", pvExpected, pvComputed.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value against an explicit computation. The amount is paid in the domestic currency.
   */
  public void presentValueDomestic() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final double timeToExpiry = TimeCalculator.getTimeBetween(REFERENCE_DATE, expDate);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, payDate, notional, strike);
    final ForexOptionDigitalDefinition forexOptionDefinition = new ForexOptionDigitalDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionDigital forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE);
    final double dfDomestic = MULTICURVES.getDiscountFactor(USD, forexOption.getUnderlyingForex().getPaymentTime());
    final double dfForeign = MULTICURVES.getDiscountFactor(EUR, forexOption.getUnderlyingForex().getPaymentTime());
    final double forward = SPOT * dfForeign / dfDomestic;
    final double volatility = SMILE_TERM.getVolatility(Triple.of(timeToExpiry, strike, forward));
    final double sigmaRootT = volatility * Math.sqrt(forexOption.getExpirationTime());
    final double dM = Math.log(forward / strike) / sigmaRootT - 0.5 * sigmaRootT;
    final int omega = isCall ? 1 : -1;
    final double pvExpected = Math.abs(forexOption.getUnderlyingForex().getPaymentCurrency2().getAmount()) * dfDomestic * NORMAL.getCDF(omega * dM)
        * (isLong ? 1.0 : -1.0);
    final MultipleCurrencyAmount pvComputed = METHOD_BLACK_DIGITAL.presentValue(forexOption, SMILE_MULTICURVES);
    assertEquals("Forex Digital option: present value", pvExpected, pvComputed.getAmount(USD), TOLERANCE_PV);
  }

  @Test(enabled = false)
  /**
   * Profile the present value against an explicit computation. The amount is paid in the domestic currency.
   */
  public void presentValueDomesticProfile() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, payDate, 1.0 / strike, strike);
    final ForexOptionDigitalDefinition forexOptionDefinition = new ForexOptionDigitalDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionDigital forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE);
    final int nbSpot = 50;
    final double range = 0.75;
    final double[] spot = new double[nbSpot + 1];
    final double[] pv = new double[nbSpot + 1];
    final MulticurveProviderDiscount multicurveForex = MULTICURVES.copy();
    for (int loopspot = 0; loopspot <= nbSpot; loopspot++) {
      spot[loopspot] = strike - range + 2.0d * range * loopspot / nbSpot;
      final FXMatrix fxMatrix = new FXMatrix(EUR, USD, spot[loopspot]);
      multicurveForex.setForexMatrix(fxMatrix);
      final BlackForexSmileProviderDiscount smile = new BlackForexSmileProviderDiscount(multicurveForex, SMILE_TERM, Pairs.of(EUR, USD));
      pv[loopspot] = METHOD_BLACK_DIGITAL.presentValue(forexOption, smile).getAmount(USD);
    }
  }

  @Test
  /**
   * Tests the present value against an explicit computation. The amount is paid in the foreign currency.
   */
  public void presentValueForeign() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final boolean payDomestic = false;
    final double notional = 100000000;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final double timeToExpiry = TimeCalculator.getTimeBetween(REFERENCE_DATE, expDate);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, payDate, notional, strike);
    final ForexOptionDigitalDefinition forexOptionDefinition = new ForexOptionDigitalDefinition(forexUnderlyingDefinition, expDate, isCall, isLong, payDomestic);
    final ForexOptionDigital forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE);
    final double dfDomestic = MULTICURVES.getDiscountFactor(EUR, forexOption.getUnderlyingForex().getPaymentTime());
    final double dfForeign = MULTICURVES.getDiscountFactor(USD, forexOption.getUnderlyingForex().getPaymentTime());
    final double forward = 1 / SPOT * dfForeign / dfDomestic;
    final double volatility = SMILE_TERM.getVolatility(Triple.of(timeToExpiry, strike, 1 / forward));
    final double sigmaRootT = volatility * Math.sqrt(forexOption.getExpirationTime());
    final double dM = Math.log(forward * strike) / sigmaRootT - 0.5 * sigmaRootT;
    final double omega = isCall ? -1.0 : 1.0;
    final double pvExpected = Math.abs(forexOption.getUnderlyingForex().getPaymentCurrency1().getAmount()) * dfDomestic * NORMAL.getCDF(omega * dM)
        * (isLong ? 1.0 : -1.0);
    final MultipleCurrencyAmount pvComputed = METHOD_BLACK_DIGITAL.presentValue(forexOption, SMILE_MULTICURVES);
    assertEquals("Forex Digital option: present value", pvExpected, pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test(enabled = false)
  /**
   * Profile the present value against an explicit computation. The amount is paid in the domestic currency.
   */
  public void presentValueDomesticForeign() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, payDate, 1.0, strike);
    final ForexOptionDigitalDefinition forexOptionDefinition = new ForexOptionDigitalDefinition(forexUnderlyingDefinition, expDate, isCall, isLong, false);
    final ForexOptionDigital forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE);
    final int nbSpot = 50;
    final double range = 0.75;
    final double[] spot = new double[nbSpot + 1];
    final double[] pv = new double[nbSpot + 1];
    final MulticurveProviderDiscount multicurveForex = MULTICURVES.copy();
    for (int loopspot = 0; loopspot <= nbSpot; loopspot++) {
      spot[loopspot] = strike - range + 2.0d * range * loopspot / nbSpot;
      final FXMatrix fxMatrix = new FXMatrix(EUR, USD, spot[loopspot]);
      multicurveForex.setForexMatrix(fxMatrix);
      final BlackForexSmileProviderDiscount smile = new BlackForexSmileProviderDiscount(multicurveForex, SMILE_TERM, Pairs.of(EUR, USD));
      pv[loopspot] = METHOD_BLACK_DIGITAL.presentValue(forexOption, smile).getAmount(EUR);
    }
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
    final ForexOptionDigital call = callDefinition.toDerivative(REFERENCE_DATE);
    final ForexOptionDigital put = putDefinition.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyAmount pvCall = METHOD_BLACK_DIGITAL.presentValue(call, SMILE_MULTICURVES);
    final MultipleCurrencyAmount pvPut = METHOD_BLACK_DIGITAL.presentValue(put, SMILE_MULTICURVES);
    final MultipleCurrencyAmount pvCash = put.getUnderlyingForex().getPaymentCurrency2().accept(PVDC, MULTICURVES);
    assertEquals("Forex Digital option: present value", pvCall.getAmount(USD) + pvPut.getAmount(USD), Math.abs(pvCash.getAmount(USD)), TOLERANCE_PV);
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
    final ForexOptionDigitalDefinition forexOptionDefinition = new ForexOptionDigitalDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionDigital forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyAmount pvMethod = METHOD_BLACK_DIGITAL.presentValue(forexOption, SMILE_MULTICURVES);
    final MultipleCurrencyAmount pvCalculator = forexOption.accept(PVFBC, SMILE_MULTICURVES);
    assertEquals("Forex Digital option: present value Method vs Calculator", pvMethod.getAmount(USD), pvCalculator.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value long/short parity.
   */
  public void presentValueLongShort() {
    final ForexOptionDigitalDefinition forexOptionShortDefinition = new ForexOptionDigitalDefinition(FOREX_DEFINITION, OPTION_EXP_DATE, IS_CALL, !IS_LONG);
    final ForexOptionDigital forexOptionShort = forexOptionShortDefinition.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyAmount pvShort = METHOD_BLACK_DIGITAL.presentValue(forexOptionShort, SMILE_MULTICURVES);
    final MultipleCurrencyAmount pvLong = METHOD_BLACK_DIGITAL.presentValue(FOREX_DIGITAL_CALL_DOM, SMILE_MULTICURVES);
    assertEquals("Forex Digital option: present value long/short parity", pvLong.getAmount(USD), -pvShort.getAmount(USD), TOLERANCE_PV);
    final MultipleCurrencyAmount ceShort = METHOD_BLACK_DIGITAL.currencyExposure(forexOptionShort, SMILE_MULTICURVES);
    final MultipleCurrencyAmount ceLong = METHOD_BLACK_DIGITAL.currencyExposure(FOREX_DIGITAL_CALL_DOM, SMILE_MULTICURVES);
    assertEquals("Forex Digital option: currency exposure long/short parity", ceLong.getAmount(USD), -ceShort.getAmount(USD), TOLERANCE_PV);
    assertEquals("Forex Digital option: currency exposure long/short parity", ceLong.getAmount(EUR), -ceShort.getAmount(EUR), TOLERANCE_PV);
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
    final ForexOptionDigitalDefinition forexOptionDefinitionCall = new ForexOptionDigitalDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionDigitalDefinition forexOptionDefinitionPut = new ForexOptionDigitalDefinition(forexUnderlyingDefinition, expDate, !isCall, isLong);
    final ForexOptionDigital forexOptionCall = forexOptionDefinitionCall.toDerivative(REFERENCE_DATE);
    final ForexOptionDigital forexOptionPut = forexOptionDefinitionPut.toDerivative(REFERENCE_DATE);
    final double dfDomestic = MULTICURVES.getDiscountFactor(USD, TimeCalculator.getTimeBetween(REFERENCE_DATE, payDate)); // USD
    final double dfForeign = MULTICURVES.getDiscountFactor(EUR, TimeCalculator.getTimeBetween(REFERENCE_DATE, payDate)); // EUR
    final double forward = SPOT * dfForeign / dfDomestic;
    final double volatility = SMILE_TERM.getVolatility(Triple.of(timeToExpiry, strike, forward));
    final double sigmaRootT = volatility * Math.sqrt(forexOptionCall.getExpirationTime());
    final double dM = Math.log(forward / strike) / sigmaRootT - 0.5 * sigmaRootT;
    final double deltaSpotCall = dfDomestic * notional * strike * NORMAL.getPDF(dM) / (sigmaRootT * SPOT);
    final double deltaSpotPut = -dfDomestic * notional * strike * NORMAL.getPDF(dM) / (sigmaRootT * SPOT);
    final MultipleCurrencyAmount priceComputedCall = METHOD_BLACK_DIGITAL.presentValue(forexOptionCall, SMILE_MULTICURVES);
    final MultipleCurrencyAmount priceComputedPut = METHOD_BLACK_DIGITAL.presentValue(forexOptionPut, SMILE_MULTICURVES);
    final MultipleCurrencyAmount currencyExposureCallComputed = METHOD_BLACK_DIGITAL.currencyExposure(forexOptionCall, SMILE_MULTICURVES);
    assertEquals("Forex Digital option: currency exposure foreign - call", deltaSpotCall, currencyExposureCallComputed.getAmount(EUR), TOLERANCE_PV);
    assertEquals("Forex Digital option: currency exposure domestic - call", -deltaSpotCall * SPOT + priceComputedCall.getAmount(USD),
        currencyExposureCallComputed.getAmount(USD), TOLERANCE_PV);
    final MultipleCurrencyAmount currencyExposurePutComputed = METHOD_BLACK_DIGITAL.currencyExposure(forexOptionPut, SMILE_MULTICURVES);
    assertEquals("Forex Digital option: currency exposure foreign- put", deltaSpotPut, currencyExposurePutComputed.getAmount(EUR), TOLERANCE_PV);
    assertEquals("Forex Digital option: currency exposure domestic - put", -deltaSpotPut * SPOT + priceComputedPut.getAmount(USD),
        currencyExposurePutComputed.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the currency exposure with a FD rate shift.
   */
  public void currencyExposureForeign2() {
    final double shift = 0.000005;
    final MulticurveProviderDiscount multicurveP = MULTICURVES.copy();
    final FXMatrix fxMatrixP = new FXMatrix(EUR, USD, SPOT + shift);
    multicurveP.setForexMatrix(fxMatrixP);
    final BlackForexSmileProviderDiscount smileP = new BlackForexSmileProviderDiscount(multicurveP, SMILE_TERM_FLAT, Pairs.of(EUR, USD));
    final MultipleCurrencyAmount ce = METHOD_BLACK_DIGITAL.currencyExposure(FOREX_DIGITAL_CALL_FOR, SMILE_FLAT_MULTICURVES);
    final MultipleCurrencyAmount pv = METHOD_BLACK_DIGITAL.presentValue(FOREX_DIGITAL_CALL_FOR, SMILE_FLAT_MULTICURVES);
    final MultipleCurrencyAmount pvP = METHOD_BLACK_DIGITAL.presentValue(FOREX_DIGITAL_CALL_FOR, smileP);
    assertEquals("Forex Digital option: call spread method - currency exposure - PL EUR", pvP.getAmount(EUR) - pv.getAmount(EUR), ce.getAmount(USD)
        * (1.0 / (SPOT + shift) - 1 / SPOT), TOLERANCE_PV);
    assertEquals("Forex Digital option: call spread method - currency exposure - PL USD", pvP.getAmount(EUR) * (SPOT + shift) - pv.getAmount(EUR) * SPOT,
        ce.getAmount(EUR) * (SPOT + shift - SPOT), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the currency exposure against the present value.
   */
  public void currencyExposureVsPresentValue() {
    final MultipleCurrencyAmount pv = METHOD_BLACK_DIGITAL.presentValue(FOREX_DIGITAL_CALL_DOM, SMILE_MULTICURVES);
    final MultipleCurrencyAmount ce = METHOD_BLACK_DIGITAL.currencyExposure(FOREX_DIGITAL_CALL_DOM, SMILE_MULTICURVES);
    assertEquals("Forex Digital option: currency exposure vs present value", ce.getAmount(USD) + ce.getAmount(EUR) * SPOT, pv.getAmount(USD), 1E-2);
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
    final ForexOptionDigital forexOptionCall = forexOptionDefinitionCall.toDerivative(REFERENCE_DATE);
    final ForexOptionDigital forexOptionPut = forexOptionDefinitionPut.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyAmount currencyExposureCall = METHOD_BLACK_DIGITAL.currencyExposure(forexOptionCall, SMILE_MULTICURVES);
    final MultipleCurrencyAmount currencyExposurePut = METHOD_BLACK_DIGITAL.currencyExposure(forexOptionPut, SMILE_MULTICURVES);
    final MultipleCurrencyAmount pvCash = forexOptionPut.getUnderlyingForex().getPaymentCurrency2().accept(PVDC, MULTICURVES);
    assertEquals("Forex Digital option: currency exposure put/call parity foreign", 0, currencyExposureCall.getAmount(EUR) + currencyExposurePut.getAmount(EUR),
        TOLERANCE_PV);
    assertEquals("Forex Digital option: currency exposure put/call parity domestic", Math.abs(pvCash.getAmount(USD)), currencyExposureCall.getAmount(USD)
        + currencyExposurePut.getAmount(USD), TOLERANCE_PV);
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
    final ForexOptionDigitalDefinition forexOptionDefinition = new ForexOptionDigitalDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionDigital forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyAmount ceMethod = METHOD_BLACK_DIGITAL.currencyExposure(forexOption, SMILE_MULTICURVES);
    final MultipleCurrencyAmount ceCalculator = forexOption.accept(CEFBC, SMILE_MULTICURVES);
    assertEquals("Forex Digital option: currency exposure Method vs Calculator", ceMethod.getAmount(EUR), ceCalculator.getAmount(EUR), 1E-2);
    assertEquals("Forex Digital option: currency exposure Method vs Calculator", ceMethod.getAmount(USD), ceCalculator.getAmount(USD), 1E-2);
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
    final ForexOptionDigitalDefinition forexOptionDefinitionCall = new ForexOptionDigitalDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionDigital forexOptionCall = forexOptionDefinitionCall.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyParameterSensitivity pvpsExact = PS_FBS_C.calculateSensitivity(forexOptionCall, SMILE_FLAT_MULTICURVES, SMILE_FLAT_MULTICURVES
        .getMulticurveProvider().getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsFD = PS_FBS_FDC.calculateSensitivity(forexOptionCall, SMILE_FLAT_MULTICURVES);
    AssertSensivityObjects.assertEquals("FX digital option: presentValueCurveSensitivity ", pvpsExact, pvpsFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Test the present value curve sensitivity through the method and through the calculator.
   */
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final MultipleCurrencyMulticurveSensitivity pvcsMethod = METHOD_BLACK_DIGITAL.presentValueCurveSensitivity(FOREX_DIGITAL_CALL_DOM, SMILE_MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvcsCalculator = FOREX_DIGITAL_CALL_DOM.accept(PVCSFBC, SMILE_MULTICURVES);
    assertEquals("Forex present value curve sensitivity: Method vs Calculator", pvcsMethod, pvcsCalculator);
  }

  @Test
  /**
   * Tests present value volatility sensitivity.
   */
  public void volatilitySensitivity() {
    final double shift = 1.0E-6;
    final PresentValueForexBlackVolatilitySensitivity sensi = METHOD_BLACK_DIGITAL.presentValueBlackVolatilitySensitivity(FOREX_DIGITAL_CALL_DOM, SMILE_MULTICURVES);
    final Pair<Currency, Currency> currencyPair = Pairs.of(EUR, USD);
    final DoublesPair point = DoublesPair.of(FOREX_DIGITAL_CALL_DOM.getExpirationTime(), STRIKE);
    assertEquals("Forex Digital option: vega", currencyPair, sensi.getCurrencyPair());
    assertEquals("Forex Digital option: vega size", 1, sensi.getVega().getMap().entrySet().size());
    assertTrue("Forex Digital option: vega", sensi.getVega().getMap().containsKey(point));
    final double strike = FOREX_DIGITAL_CALL_DOM.getStrike();
    final int omega = FOREX_DIGITAL_CALL_DOM.isCall() ? 1 : -1;
    final double dfDomestic = MULTICURVES.getDiscountFactor(USD, FOREX_DIGITAL_CALL_DOM.getUnderlyingForex().getPaymentTime());
    final double dfForeign = MULTICURVES.getDiscountFactor(EUR, FOREX_DIGITAL_CALL_DOM.getUnderlyingForex().getPaymentTime());
    final double forward = SPOT * dfForeign / dfDomestic;
    final double volatility = SMILE_TERM.getVolatility(Triple.of(FOREX_DIGITAL_CALL_DOM.getExpirationTime(), strike, forward));
    final double sigmaRootTPlus = (volatility + shift) * Math.sqrt(FOREX_DIGITAL_CALL_DOM.getExpirationTime());
    final double dMPlus = Math.log(forward / strike) / sigmaRootTPlus - 0.5 * sigmaRootTPlus;
    final double pvPlus = Math.abs(FOREX_DIGITAL_CALL_DOM.getUnderlyingForex().getPaymentCurrency2().getAmount()) * dfDomestic * NORMAL.getCDF(omega * dMPlus)
        * (FOREX_DIGITAL_CALL_DOM.isLong() ? 1.0 : -1.0);
    final double sigmaRootTMinus = (volatility - shift) * Math.sqrt(FOREX_DIGITAL_CALL_DOM.getExpirationTime());
    final double dMMinus = Math.log(forward / strike) / sigmaRootTMinus - 0.5 * sigmaRootTMinus;
    final double pvMinus = Math.abs(FOREX_DIGITAL_CALL_DOM.getUnderlyingForex().getPaymentCurrency2().getAmount()) * dfDomestic * NORMAL.getCDF(omega * dMMinus)
        * (FOREX_DIGITAL_CALL_DOM.isLong() ? 1.0 : -1.0);
    assertEquals("Forex Digital option: vega", (pvPlus - pvMinus) / (2 * shift), sensi.getVega().getMap().get(point), TOLERANCE_PV);
    final ForexOptionDigitalDefinition optionShortDefinition = new ForexOptionDigitalDefinition(FOREX_DEFINITION, OPTION_EXP_DATE, IS_CALL, !IS_LONG);
    final ForexOptionDigital optionShort = optionShortDefinition.toDerivative(REFERENCE_DATE);
    final PresentValueForexBlackVolatilitySensitivity sensiShort = METHOD_BLACK_DIGITAL.presentValueBlackVolatilitySensitivity(optionShort, SMILE_MULTICURVES);
    assertEquals("Forex Digital option: vega short", -sensi.getVega().getMap().get(point), sensiShort.getVega().getMap().get(point));
    // Put/call parity
    final ForexOptionDigitalDefinition optionShortPutDefinition = new ForexOptionDigitalDefinition(FOREX_DEFINITION, OPTION_EXP_DATE, !IS_CALL, IS_LONG);
    final ForexOptionDigital optionShortPut = optionShortPutDefinition.toDerivative(REFERENCE_DATE);
    final PresentValueForexBlackVolatilitySensitivity sensiShortPut = METHOD_BLACK_DIGITAL.presentValueBlackVolatilitySensitivity(optionShortPut, SMILE_MULTICURVES);
    assertEquals("Forex Digital option: vega short", sensiShortPut.getVega().getMap().get(point) + sensi.getVega().getMap().get(point), 0.0, TOLERANCE_PV);
  }

  @Test
  /**
   * Test the present value curve sensitivity through the method and through the calculator.
   */
  public void volatilitySensitivityMethodVsCalculator() {
    final PresentValueForexBlackVolatilitySensitivity pvvsMethod = METHOD_BLACK_DIGITAL.presentValueBlackVolatilitySensitivity(FOREX_DIGITAL_CALL_DOM, SMILE_MULTICURVES);
    final PresentValueForexBlackVolatilitySensitivity pvvsCalculator = FOREX_DIGITAL_CALL_DOM.accept(PVVSFBSC, SMILE_MULTICURVES);
    assertEquals("Forex present value curve sensitivity: Method vs Calculator", pvvsMethod, pvvsCalculator);
  }

  @Test
  /**
   * Tests present value volatility quote sensitivity.
   */
  public void volatilityQuoteSensitivity() {
    final PresentValueForexBlackVolatilityNodeSensitivityDataBundle sensiStrike = METHOD_BLACK_DIGITAL.presentValueBlackVolatilityNodeSensitivity(FOREX_DIGITAL_CALL_DOM,
        SMILE_MULTICURVES);
    final double[][] sensiQuote = METHOD_BLACK_DIGITAL.presentValueBlackVolatilityNodeSensitivity(FOREX_DIGITAL_CALL_DOM, SMILE_MULTICURVES).quoteSensitivity().getVega();
    final double[][] sensiStrikeData = sensiStrike.getVega().getData();
    final double[] atm = new double[sensiQuote.length];
    final int nbDelta = SMILE_TERM.getDelta().length;
    for (int loopexp = 0; loopexp < sensiQuote.length; loopexp++) {
      for (int loopdelta = 0; loopdelta < nbDelta; loopdelta++) {
        assertEquals("Forex Digital option: vega quote - RR", sensiQuote[loopexp][1 + loopdelta], -0.5 * sensiStrikeData[loopexp][loopdelta] + 0.5
            * sensiStrikeData[loopexp][2 * nbDelta - loopdelta], 1.0E-10);
        assertEquals("Forex Digital option: vega quote - Strangle", sensiQuote[loopexp][nbDelta + 1 + loopdelta], sensiStrikeData[loopexp][loopdelta]
            + sensiStrikeData[loopexp][2 * nbDelta - loopdelta], 1.0E-10);
        atm[loopexp] += sensiStrikeData[loopexp][loopdelta] + sensiStrikeData[loopexp][2 * nbDelta - loopdelta];
      }
      atm[loopexp] += sensiStrikeData[loopexp][nbDelta];
      assertEquals("Forex Digital option: vega quote", sensiQuote[loopexp][0], atm[loopexp], 1.0E-10); // ATM
    }
  }

}
