/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.method;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;
import org.testng.internal.junit.ArrayAsserts;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.forex.calculator.CurrencyExposureBlackForexCalculator;
import com.opengamma.financial.forex.calculator.ForexDerivative;
import com.opengamma.financial.forex.calculator.PresentValueBlackForexCalculator;
import com.opengamma.financial.forex.calculator.PresentValueCurveSensitivityBlackForexCalculator;
import com.opengamma.financial.forex.calculator.PresentValueForexVegaQuoteSensitivityCalculator;
import com.opengamma.financial.forex.calculator.PresentValueVolatilitySensitivityBlackCalculator;
import com.opengamma.financial.forex.definition.ForexDefinition;
import com.opengamma.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.option.definition.Barrier;
import com.opengamma.financial.model.option.definition.Barrier.BarrierType;
import com.opengamma.financial.model.option.definition.Barrier.KnockType;
import com.opengamma.financial.model.option.definition.Barrier.ObservationType;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureParameter;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackBarrierPriceFunction;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.TimeCalculator;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/**
 * Tests related to the Black world pricing method for single barrier Forex option.
 */
public class ForexOptionSingleBarrierBlackMethodTest {
  // General
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final int SETTLEMENT_DAYS = 2;
  // Smile data
  private static final Currency CUR_1 = Currency.EUR;
  private static final Currency CUR_2 = Currency.USD;
  private static final Period[] EXPIRY_PERIOD = new Period[] {Period.ofMonths(3), Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2)};
  private static final int NB_EXP = EXPIRY_PERIOD.length;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 6, 13);
  private static final ZonedDateTime REFERENCE_SPOT = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, CALENDAR, SETTLEMENT_DAYS);
  private static final ZonedDateTime[] PAY_DATE = new ZonedDateTime[NB_EXP];
  private static final ZonedDateTime[] EXPIRY_DATE = new ZonedDateTime[NB_EXP];
  private static final double[] TIME_TO_EXPIRY = new double[NB_EXP + 1];
  static {
    TIME_TO_EXPIRY[0] = 0.0;
    for (int loopexp = 0; loopexp < NB_EXP; loopexp++) {
      PAY_DATE[loopexp] = ScheduleCalculator.getAdjustedDate(REFERENCE_SPOT, BUSINESS_DAY, CALENDAR, EXPIRY_PERIOD[loopexp]);
      EXPIRY_DATE[loopexp] = ScheduleCalculator.getAdjustedDate(PAY_DATE[loopexp], CALENDAR, -SETTLEMENT_DAYS);
      TIME_TO_EXPIRY[loopexp + 1] = TimeCalculator.getTimeBetween(REFERENCE_DATE, EXPIRY_DATE[loopexp]);
    }
  }
  private static final double SPOT = 1.40;
  private static final FXMatrix FX_MATRIX = new FXMatrix(CUR_1, CUR_2, SPOT);
  private static final double[] ATM = {0.175, 0.185, 0.18, 0.17, 0.16};
  private static final double[] DELTA = new double[] {0.10, 0.25};
  private static final double[][] RISK_REVERSAL = new double[][] { {-0.010, -0.0050}, {-0.011, -0.0060}, {-0.012, -0.0070}, {-0.013, -0.0080}, {-0.014, -0.0090}};
  private static final double[][] STRANGLE = new double[][] { {0.0300, 0.0100}, {0.0310, 0.0110}, {0.0320, 0.0120}, {0.0330, 0.0130}, {0.0340, 0.0140}};
  private static final int NB_STRIKE = 2 * DELTA.length + 1;
  private static final SmileDeltaTermStructureParameter SMILE_TERM = new SmileDeltaTermStructureParameter(TIME_TO_EXPIRY, DELTA, ATM, RISK_REVERSAL, STRANGLE);
  // Methods and curves
  private static final YieldCurveBundle CURVES = ForexTestsDataSets.createCurvesForex();
  private static final String[] CURVES_NAME = CURVES.getAllNames().toArray(new String[0]);
  private static final SmileDeltaTermStructureDataBundle SMILE_BUNDLE = new SmileDeltaTermStructureDataBundle(CURVES, FX_MATRIX, SMILE_TERM, Pair.of(CUR_1, CUR_2));
  private static final ForexOptionVanillaBlackMethod METHOD_VANILLA = ForexOptionVanillaBlackMethod.getInstance();
  private static final ForexOptionSingleBarrierBlackMethod METHOD_BARRIER = ForexOptionSingleBarrierBlackMethod.getInstance();
  private static final BlackBarrierPriceFunction BLACK_BARRIER_FUNCTION = BlackBarrierPriceFunction.getInstance();
  // Option
  private static final double STRIKE = 1.45;
  private static final boolean IS_CALL = true;
  private static final boolean IS_LONG = true;
  private static final double NOTIONAL = 100000000;
  private static final Barrier BARRIER = new Barrier(KnockType.IN, BarrierType.DOWN, ObservationType.CLOSE, 1.35);
  private static final double REBATE = 50000;
  private static final ZonedDateTime OPTION_PAY_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, BUSINESS_DAY, CALENDAR, Period.ofMonths(9));
  private static final ZonedDateTime OPTION_EXPIRY_DATE = ScheduleCalculator.getAdjustedDate(OPTION_PAY_DATE, CALENDAR, -SETTLEMENT_DAYS);
  private static final ForexDefinition FOREX_DEFINITION = new ForexDefinition(CUR_1, CUR_2, OPTION_PAY_DATE, NOTIONAL, STRIKE);
  private static final ForexOptionVanillaDefinition VANILLA_LONG_DEFINITION = new ForexOptionVanillaDefinition(FOREX_DEFINITION, OPTION_EXPIRY_DATE, IS_CALL, IS_LONG);
  private static final ForexOptionVanilla VANILLA_LONG = VANILLA_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final ForexOptionSingleBarrier OPTION_BARRIER = new ForexOptionSingleBarrier(VANILLA_LONG, BARRIER, REBATE);
  private static final ForexOptionVanillaDefinition VANILLA_SHORT_DEFINITION = new ForexOptionVanillaDefinition(FOREX_DEFINITION, OPTION_EXPIRY_DATE, IS_CALL, !IS_LONG);
  private static final ForexOptionVanilla VANILLA_SHORT = VANILLA_SHORT_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final ForexOptionSingleBarrier BARRIER_SHORT = new ForexOptionSingleBarrier(VANILLA_SHORT, BARRIER, REBATE);

  private static final PresentValueBlackForexCalculator PVC_BLACK = PresentValueBlackForexCalculator.getInstance();
  private static final PresentValueCurveSensitivityBlackForexCalculator PVCSC_BLACK = PresentValueCurveSensitivityBlackForexCalculator.getInstance();
  private static final PresentValueVolatilitySensitivityBlackCalculator PVVSC_BLACK = PresentValueVolatilitySensitivityBlackCalculator.getInstance();
  private static final CurrencyExposureBlackForexCalculator CEC_BLACK = CurrencyExposureBlackForexCalculator.getInstance();

  @Test
  /**
   * Comparison with the underlying vanilla option (the vanilla option is more expensive).
   */
  public void comparisonVanilla() {
    final MultipleCurrencyAmount priceVanilla = METHOD_VANILLA.presentValue(VANILLA_LONG, SMILE_BUNDLE);
    final MultipleCurrencyAmount priceBarrier = METHOD_BARRIER.presentValue(OPTION_BARRIER, SMILE_BUNDLE);
    assertTrue("Barriers are cheaper than vanilla", priceVanilla.getAmount(CUR_2) > priceBarrier.getAmount(CUR_2));
  }

  @Test
  /**
   * Tests present value with a direct computation.
   */
  public void presentValue() {
    final MultipleCurrencyAmount priceMethod = METHOD_BARRIER.presentValue(OPTION_BARRIER, SMILE_BUNDLE);
    final double payTime = VANILLA_LONG.getUnderlyingForex().getPaymentTime();
    final double rateDomestic = CURVES.getCurve(CURVES_NAME[1]).getInterestRate(payTime);
    final double rateForeign = CURVES.getCurve(CURVES_NAME[0]).getInterestRate(payTime);
    final double forward = SPOT * Math.exp(-rateForeign * payTime) / Math.exp(-rateDomestic * payTime);
    final double volatility = SMILE_TERM.getVolatility(new Triple<Double, Double, Double>(VANILLA_LONG.getTimeToExpiry(), STRIKE, forward));
    final double priceComputed = BLACK_BARRIER_FUNCTION.getPrice(VANILLA_LONG, BARRIER, REBATE / NOTIONAL, SPOT, rateForeign, rateDomestic, volatility) * NOTIONAL;
    assertEquals("Barriers present value", priceComputed, priceMethod.getAmount(CUR_2), 1.0E-2);
  }

  @Test
  /**
   * Test the price scaling and the long/short parity.
   */
  public void scaleLongShortParity() {
    final MultipleCurrencyAmount priceBarrier = METHOD_BARRIER.presentValue(OPTION_BARRIER, SMILE_BUNDLE);
    final double scale = 10;
    final ForexDefinition fxDefinitionScale = new ForexDefinition(CUR_1, CUR_2, OPTION_PAY_DATE, NOTIONAL * scale, STRIKE);
    final ForexOptionVanillaDefinition optionDefinitionScale = new ForexOptionVanillaDefinition(fxDefinitionScale, OPTION_EXPIRY_DATE, IS_CALL, IS_LONG);
    final ForexOptionVanilla optionScale = optionDefinitionScale.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final ForexOptionSingleBarrier optionBarrierScale = new ForexOptionSingleBarrier(optionScale, BARRIER, scale * REBATE);
    final MultipleCurrencyAmount priceBarrierScale = METHOD_BARRIER.presentValue(optionBarrierScale, SMILE_BUNDLE);
    assertEquals("Barriers are cheaper than vanilla", priceBarrier.getAmount(CUR_2) * scale, priceBarrierScale.getAmount(CUR_2), 1.0E-2);
    final MultipleCurrencyAmount priceBarrierShort = METHOD_BARRIER.presentValue(BARRIER_SHORT, SMILE_BUNDLE);
    assertEquals("Barriers are cheaper than vanilla", -priceBarrier.getAmount(CUR_2), priceBarrierShort.getAmount(CUR_2), 1.0E-2);
  }

  @Test
  /**
   * Tests that the present value given by the method for a generic ForexDerivatrive is the same as for a specific ForexOptionSingleBarrier.
   */
  public void methodForexBarrier() {
    final ForexDerivative fx = OPTION_BARRIER;
    final YieldCurveBundle curves = SMILE_BUNDLE;
    final MultipleCurrencyAmount priceGeneric = METHOD_BARRIER.presentValue(fx, curves);
    final MultipleCurrencyAmount priceSpecific = METHOD_BARRIER.presentValue(OPTION_BARRIER, SMILE_BUNDLE);
    assertEquals("Barrier price: generic vs specific", priceSpecific, priceGeneric);
  }

  @Test
  /**
   * Tests present value method vs calculator.
   */
  public void presentValueMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD_BARRIER.presentValue(OPTION_BARRIER, SMILE_BUNDLE);
    final MultipleCurrencyAmount pvCalculator = PVC_BLACK.visit(OPTION_BARRIER, SMILE_BUNDLE);
    assertEquals("Forex vanilla option: present value Method vs Calculator", pvMethod.getAmount(CUR_2), pvCalculator.getAmount(CUR_2), 1E-2);
  }

  @Test
  /**
   * Tests the currency exposure vs a finite difference computation. The computation is with fixed Black volatility (Black world). 
   * The volatility used in the shifted price is flat with the volatility equal to the volatility used for the original price.
   */
  public void currencyExposure() {
    final MultipleCurrencyAmount ce = METHOD_BARRIER.currencyExposure(OPTION_BARRIER, SMILE_BUNDLE);
    final double shiftSpotEURUSD = 1E-6;
    final MultipleCurrencyAmount pv = METHOD_BARRIER.presentValue(OPTION_BARRIER, SMILE_BUNDLE);
    final double payTime = VANILLA_LONG.getUnderlyingForex().getPaymentTime();
    final double rateDomestic = CURVES.getCurve(CURVES_NAME[1]).getInterestRate(payTime);
    final double rateForeign = CURVES.getCurve(CURVES_NAME[0]).getInterestRate(payTime);
    final double forward = SPOT * Math.exp(-rateForeign * payTime) / Math.exp(-rateDomestic * payTime);
    final double volatility = SMILE_TERM.getVolatility(VANILLA_LONG.getTimeToExpiry(), STRIKE, forward);
    final double[] atmFlat = {volatility, volatility, volatility, volatility, volatility};
    final double[][] rrFlat = new double[][] { {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}};
    final double[][] sFlat = new double[][] { {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}};
    final SmileDeltaTermStructureParameter smileTermFlat = new SmileDeltaTermStructureParameter(TIME_TO_EXPIRY, DELTA, atmFlat, rrFlat, sFlat);
    final FXMatrix fxMatrixShift = new FXMatrix(CUR_1, CUR_2, SPOT + shiftSpotEURUSD);
    final SmileDeltaTermStructureDataBundle smileBumpedSpot = new SmileDeltaTermStructureDataBundle(CURVES, fxMatrixShift, smileTermFlat, Pair.of(CUR_1, CUR_2));
    final MultipleCurrencyAmount pvBumpedSpot = METHOD_BARRIER.presentValue(OPTION_BARRIER, smileBumpedSpot);
    final double ceDomesticFD = (pvBumpedSpot.getAmount(CUR_2) - pv.getAmount(CUR_2));
    assertEquals("Barrier currency exposure: domestic currency", ceDomesticFD, ce.getAmount(CUR_1) * shiftSpotEURUSD, 2.0E-4);
    final double spotGBPUSD = 1.60;
    final double spotGBPEUR = spotGBPUSD / SPOT;
    final double shiftSpotGBPUSD = 2.0E-6;
    final double spotEURUSDshifted = SPOT + shiftSpotEURUSD;
    final double spotGBPUSDshifted = spotGBPUSD + shiftSpotGBPUSD;
    final double spotGBPEURshifted = spotGBPUSDshifted / spotEURUSDshifted;
    final double pvInGBPBeforeShift = pv.getAmount(CUR_2) / spotGBPUSD;
    final double pvInGBPAfterShift = pvBumpedSpot.getAmount(CUR_2) / spotGBPUSDshifted;
    assertEquals("Barrier currency exposure: all currencies", pvInGBPAfterShift - pvInGBPBeforeShift, ce.getAmount(CUR_1) * (1 / spotGBPEURshifted - 1 / spotGBPEUR) + ce.getAmount(CUR_2)
        * (1 / spotGBPUSDshifted - 1 / spotGBPUSD), 1.0E-4);
  }

  @Test
  /**
   * Tests that the currency exposure given by the method for a generic ForexDerivatrive is the same as for a specific ForexOptionSingleBarrier.
   */
  public void currencyExposureDerivative() {
    final ForexDerivative fx = OPTION_BARRIER;
    final YieldCurveBundle curves = SMILE_BUNDLE;
    final MultipleCurrencyAmount ceGeneric = METHOD_BARRIER.currencyExposure(fx, curves);
    final MultipleCurrencyAmount ceSpecific = METHOD_BARRIER.currencyExposure(OPTION_BARRIER, SMILE_BUNDLE);
    assertEquals("Barrier price: generic vs specific", ceSpecific, ceGeneric);
  }

  @Test
  /**
   * Tests currency exposure Method vs Calculator.
   */
  public void currencyExposureMethodVsCalculator() {
    final MultipleCurrencyAmount ceMethod = METHOD_BARRIER.currencyExposure(OPTION_BARRIER, SMILE_BUNDLE);
    final MultipleCurrencyAmount ceCalculator = CEC_BLACK.visit(OPTION_BARRIER, SMILE_BUNDLE);
    assertEquals("Forex vanilla option: currency exposure Method vs Calculator", ceMethod.getAmount(CUR_1), ceCalculator.getAmount(CUR_1), 1E-2);
    assertEquals("Forex vanilla option: currency exposure Method vs Calculator", ceMethod.getAmount(CUR_2), ceCalculator.getAmount(CUR_2), 1E-2);
  }

  @Test
  /**
   * Tests the present value curve sensitivity.
   */
  public void presentValueCurveSensitivity() {
    final double payTime = VANILLA_LONG.getUnderlyingForex().getPaymentTime();
    final double rateDomestic = CURVES.getCurve(CURVES_NAME[1]).getInterestRate(payTime);
    final double rateForeign = CURVES.getCurve(CURVES_NAME[0]).getInterestRate(payTime);
    final MultipleCurrencyInterestRateCurveSensitivity sensi = METHOD_BARRIER.presentValueCurveSensitivity(OPTION_BARRIER, SMILE_BUNDLE);
    final double dfDomestic = CURVES.getCurve(CURVES_NAME[1]).getDiscountFactor(payTime);
    final double dfForeign = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(payTime);
    final double forward = SPOT * dfForeign / dfDomestic;
    final double volatility = SMILE_TERM.getVolatility(new Triple<Double, Double, Double>(VANILLA_LONG.getTimeToExpiry(), STRIKE, forward));
    final double rebateByForeignUnit = REBATE / Math.abs(NOTIONAL);
    // Finite difference
    final double deltaShift = 0.00001; // 0.1 bp
    final double bumpedPvForeignPlus = BLACK_BARRIER_FUNCTION.getPrice(VANILLA_LONG, BARRIER, rebateByForeignUnit, SPOT, rateForeign + deltaShift, rateDomestic, volatility) * NOTIONAL;
    final double bumpedPvForeignMinus = BLACK_BARRIER_FUNCTION.getPrice(VANILLA_LONG, BARRIER, rebateByForeignUnit, SPOT, rateForeign - deltaShift, rateDomestic, volatility) * NOTIONAL;
    final double resultForeign = (bumpedPvForeignPlus - bumpedPvForeignMinus) / (2 * deltaShift);
    assertEquals("Forex vanilla option: curve exposure", payTime, sensi.getSensitivity(CUR_2).getSensitivities().get(CURVES_NAME[0]).get(0).first, 1E-2);
    assertEquals("Forex vanilla option: curve exposure", resultForeign, sensi.getSensitivity(CUR_2).getSensitivities().get(CURVES_NAME[0]).get(0).second, 1E-2);
    //Domestic
    final double bumpedPvDomesticPlus = BLACK_BARRIER_FUNCTION.getPrice(VANILLA_LONG, BARRIER, rebateByForeignUnit, SPOT, rateForeign, rateDomestic + deltaShift, volatility) * NOTIONAL;
    final double bumpedPvDomesticMinus = BLACK_BARRIER_FUNCTION.getPrice(VANILLA_LONG, BARRIER, rebateByForeignUnit, SPOT, rateForeign, rateDomestic - deltaShift, volatility) * NOTIONAL;
    final double resultDomestic = (bumpedPvDomesticPlus - bumpedPvDomesticMinus) / (2 * deltaShift);
    assertEquals("Forex vanilla option: curve exposure", payTime, sensi.getSensitivity(CUR_2).getSensitivities().get(CURVES_NAME[1]).get(0).first, 1E-2);
    assertEquals("Forex vanilla option: curve exposure", resultDomestic, sensi.getSensitivity(CUR_2).getSensitivities().get(CURVES_NAME[1]).get(0).second, 1E-2);
  }

  @Test
  /**
   * Test the present value curve sensitivity through the method and through the calculator.
   */
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final MultipleCurrencyInterestRateCurveSensitivity pvcsMethod = METHOD_BARRIER.presentValueCurveSensitivity(OPTION_BARRIER, SMILE_BUNDLE);
    final MultipleCurrencyInterestRateCurveSensitivity pvcsCalculator = PVCSC_BLACK.visit(OPTION_BARRIER, SMILE_BUNDLE);
    assertEquals("Forex present value curve sensitivity: Method vs Calculator", pvcsMethod, pvcsCalculator);
  }

  @Test
  /**
   * Tests the long/short parity.
   */
  public void longShort() {
    final MultipleCurrencyAmount pvShort = METHOD_BARRIER.presentValue(BARRIER_SHORT, SMILE_BUNDLE);
    final MultipleCurrencyAmount pvLong = METHOD_BARRIER.presentValue(OPTION_BARRIER, SMILE_BUNDLE);
    assertEquals("Forex single barrier option: present value long/short parity", pvLong.getAmount(CUR_2), -pvShort.getAmount(CUR_2), 1E-2);
    final MultipleCurrencyAmount ceShort = METHOD_BARRIER.currencyExposure(BARRIER_SHORT, SMILE_BUNDLE);
    final MultipleCurrencyAmount ceLong = METHOD_BARRIER.currencyExposure(OPTION_BARRIER, SMILE_BUNDLE);
    assertEquals("Forex single barrier option: currency exposure long/short parity", ceLong.getAmount(CUR_2), -ceShort.getAmount(CUR_2), 1E-2);
    assertEquals("Forex single barrier option: currency exposure long/short parity", ceLong.getAmount(CUR_1), -ceShort.getAmount(CUR_1), 1E-2);
    final MultipleCurrencyInterestRateCurveSensitivity pvcsShort = METHOD_BARRIER.presentValueCurveSensitivity(BARRIER_SHORT, SMILE_BUNDLE);
    final MultipleCurrencyInterestRateCurveSensitivity pvcsLong = METHOD_BARRIER.presentValueCurveSensitivity(OPTION_BARRIER, SMILE_BUNDLE);
    assertEquals("Forex single barrier option: curve sensitivity long/short parity", pvcsLong.getSensitivity(CUR_2), pvcsShort.getSensitivity(CUR_2).multiply(-1.0));
    final PresentValueVolatilitySensitivityDataBundle pvvsShort = METHOD_BARRIER.presentValueVolatilitySensitivity(BARRIER_SHORT, SMILE_BUNDLE);
    final PresentValueVolatilitySensitivityDataBundle pvvsLong = METHOD_BARRIER.presentValueVolatilitySensitivity(OPTION_BARRIER, SMILE_BUNDLE);
    assertEquals("Forex single barrier option: volatility sensitivity long/short parity", pvvsLong, pvvsShort.multiply(-1.0));
  }

  @Test
  /**
   * Tests present value volatility sensitivity.
   */
  public void volatilitySensitivity() {
    final PresentValueVolatilitySensitivityDataBundle sensi = METHOD_BARRIER.presentValueVolatilitySensitivity(OPTION_BARRIER, SMILE_BUNDLE);
    final Pair<Currency, Currency> currencyPair = ObjectsPair.of(CUR_1, CUR_2);
    final DoublesPair point = new DoublesPair(OPTION_BARRIER.getUnderlyingOption().getTimeToExpiry(), STRIKE);
    assertEquals("Forex vanilla option: vega", currencyPair, sensi.getCurrencyPair());
    assertEquals("Forex vanilla option: vega size", 1, sensi.getVega().entrySet().size());
    assertTrue("Forex vanilla option: vega", sensi.getVega().containsKey(point));
    final double timeToExpiry = TimeCalculator.getTimeBetween(REFERENCE_DATE, OPTION_EXPIRY_DATE);
    final double dfDomestic = CURVES.getCurve(CURVES_NAME[1]).getDiscountFactor(TimeCalculator.getTimeBetween(REFERENCE_DATE, OPTION_PAY_DATE));
    final double dfForeign = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(TimeCalculator.getTimeBetween(REFERENCE_DATE, OPTION_PAY_DATE));
    final double forward = SPOT * dfForeign / dfDomestic;
    final double rateDomestic = CURVES.getCurve(CURVES_NAME[1]).getInterestRate(TimeCalculator.getTimeBetween(REFERENCE_DATE, OPTION_PAY_DATE));
    final double rateForeign = CURVES.getCurve(CURVES_NAME[0]).getInterestRate(TimeCalculator.getTimeBetween(REFERENCE_DATE, OPTION_PAY_DATE));
    final double volatility = SMILE_TERM.getVolatility(new Triple<Double, Double, Double>(timeToExpiry, STRIKE, forward));
    final double[] derivatives = new double[5];
    BLACK_BARRIER_FUNCTION.getPriceAdjoint(VANILLA_LONG, BARRIER, REBATE / NOTIONAL, SPOT, rateForeign, rateDomestic, volatility, derivatives);
    assertEquals("Forex vanilla option: vega", derivatives[4] * NOTIONAL, sensi.getVega().get(point));
    final PresentValueVolatilitySensitivityDataBundle sensiShort = METHOD_BARRIER.presentValueVolatilitySensitivity(BARRIER_SHORT, SMILE_BUNDLE);
    assertEquals("Forex vanilla option: vega short", -sensi.getVega().get(point), sensiShort.getVega().get(point));
  }

  @Test
  /**
   * Test the present value curve sensitivity through the method and through the calculator.
   */
  public void volatilitySensitivityMethodVsCalculator() {
    final PresentValueVolatilitySensitivityDataBundle pvvsMethod = METHOD_BARRIER.presentValueVolatilitySensitivity(OPTION_BARRIER, SMILE_BUNDLE);
    final PresentValueVolatilitySensitivityDataBundle pvvsCalculator = PVVSC_BLACK.visit(OPTION_BARRIER, SMILE_BUNDLE);
    assertEquals("Forex present value curve sensitivity: Method vs Calculator", pvvsMethod, pvvsCalculator);
  }

  @Test
  /**
   * Tests present value volatility node sensitivity.
   */
  public void volatilityNodeSensitivity() {
    final PresentValueVolatilityNodeSensitivityDataBundle sensi = METHOD_BARRIER.presentValueVolatilityNodeSensitivity(OPTION_BARRIER, SMILE_BUNDLE);
    assertEquals("Forex vanilla option: vega node size", NB_EXP + 1, sensi.getVega().getData().length);
    assertEquals("Forex vanilla option: vega node size", NB_STRIKE, sensi.getVega().getData()[0].length);
    final Pair<Currency, Currency> currencyPair = ObjectsPair.of(CUR_1, CUR_2);
    assertEquals("Forex vanilla option: vega", currencyPair, sensi.getCurrencyPair());
    final PresentValueVolatilitySensitivityDataBundle pointSensitivity = METHOD_BARRIER.presentValueVolatilitySensitivity(OPTION_BARRIER, SMILE_BUNDLE);
    final double[][] nodeWeight = new double[NB_EXP + 1][NB_STRIKE];
    final double df = CURVES.getCurve(CURVES_NAME[1]).getDiscountFactor(TimeCalculator.getTimeBetween(REFERENCE_DATE, OPTION_PAY_DATE));
    final double forward = SPOT * CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(TimeCalculator.getTimeBetween(REFERENCE_DATE, OPTION_PAY_DATE)) / df;
    SMILE_TERM.getVolatility(OPTION_BARRIER.getUnderlyingOption().getTimeToExpiry(), STRIKE, forward, nodeWeight);
    final DoublesPair point = DoublesPair.of(OPTION_BARRIER.getUnderlyingOption().getTimeToExpiry(), STRIKE);
    for (int loopexp = 0; loopexp < NB_EXP; loopexp++) {
      for (int loopstrike = 0; loopstrike < NB_STRIKE; loopstrike++) {
        assertEquals("Forex vanilla option: vega node", nodeWeight[loopexp][loopstrike] * pointSensitivity.getVega().get(point), sensi.getVega().getData()[loopexp][loopstrike]);
      }
    }
  }

  @Test
  /**
   * Tests present value volatility quote sensitivity.
   */
  public void volatilityQuoteSensitivity() {
    final PresentValueVolatilityNodeSensitivityDataBundle sensiStrike = METHOD_BARRIER.presentValueVolatilityNodeSensitivity(OPTION_BARRIER, SMILE_BUNDLE);
    double[][] sensiQuote = METHOD_BARRIER.presentValueVolatilityNodeSensitivity(OPTION_BARRIER, SMILE_BUNDLE).quoteSensitivity().getVega();
    double[][] sensiStrikeData = sensiStrike.getVega().getData();
    double[] atm = new double[sensiQuote.length];
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
    double[][] sensiMethod = METHOD_BARRIER.presentValueVolatilityNodeSensitivity(OPTION_BARRIER, SMILE_BUNDLE).quoteSensitivity().getVega();
    double[][] sensiCalculator = PresentValueForexVegaQuoteSensitivityCalculator.getInstance().visit(OPTION_BARRIER, SMILE_BUNDLE).getVega();
    for (int loopexp = 0; loopexp < NB_EXP; loopexp++) {
      ArrayAsserts.assertArrayEquals("Forex option - quote sensitivity", sensiMethod[loopexp], sensiCalculator[loopexp], 1.0E-10);
    }
  }

}
