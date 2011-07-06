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

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.forex.calculator.CurrencyExposureBlackForexCalculator;
import com.opengamma.financial.forex.calculator.ForexDerivative;
import com.opengamma.financial.forex.calculator.PresentValueBlackForexCalculator;
import com.opengamma.financial.forex.calculator.PresentValueCurveSensitivityBlackForexCalculator;
import com.opengamma.financial.forex.calculator.PresentValueVolatilitySensitivityBlackCalculator;
import com.opengamma.financial.forex.definition.ForexDefinition;
import com.opengamma.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.financial.forex.derivative.Forex;
import com.opengamma.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureParameter;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/**
 * Tests related to the pricing method for vanilla Forex option transactions with Black function and a volatility provider.
 */
public class ForexOptionVanillaMethodTest {
  // General
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  // Smile data
  private static final Currency CUR_1 = Currency.EUR;
  private static final Currency CUR_2 = Currency.USD;
  private static final Period[] EXPIRY_PERIOD = new Period[] {Period.ofMonths(3), Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2)};
  private static final int NB_EXP = EXPIRY_PERIOD.length;
  private static final ZonedDateTime REFERENCE_DATE = DateUtil.getUTCDate(2011, 6, 13);
  private static final ZonedDateTime REFERENCE_SPOT = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, CALENDAR, SETTLEMENT_DAYS);
  private static final ZonedDateTime[] PAY_DATE = new ZonedDateTime[NB_EXP];
  private static final ZonedDateTime[] EXPIRY_DATE = new ZonedDateTime[NB_EXP];
  private static final double[] TIME_TO_EXPIRY = new double[NB_EXP + 1];
  static {
    TIME_TO_EXPIRY[0] = 0.0;
    for (int loopexp = 0; loopexp < NB_EXP; loopexp++) {
      PAY_DATE[loopexp] = ScheduleCalculator.getAdjustedDate(REFERENCE_SPOT, BUSINESS_DAY, CALENDAR, EXPIRY_PERIOD[loopexp]);
      EXPIRY_DATE[loopexp] = ScheduleCalculator.getAdjustedDate(PAY_DATE[loopexp], CALENDAR, -SETTLEMENT_DAYS);
      TIME_TO_EXPIRY[loopexp + 1] = ACT_ACT.getDayCountFraction(REFERENCE_DATE, EXPIRY_DATE[loopexp]);
    }
  }
  private static final double SPOT = 1.40;
  private static final double[] ATM = {0.175, 0.185, 0.18, 0.17, 0.16};
  private static final double[] DELTA = new double[] {0.10, 0.25};
  private static final double[][] RISK_REVERSAL = new double[][] { {-0.010, -0.0050}, {-0.011, -0.0060}, {-0.012, -0.0070}, {-0.013, -0.0080}, {-0.014, -0.0090}};
  private static final double[][] STRANGLE = new double[][] { {0.0300, 0.0100}, {0.0310, 0.0110}, {0.0320, 0.0120}, {0.0330, 0.0130}, {0.0340, 0.0140}};
  private static final int NB_STRIKE = 2 * DELTA.length + 1;
  private static final SmileDeltaTermStructureParameter SMILE_TERM = new SmileDeltaTermStructureParameter(TIME_TO_EXPIRY, DELTA, ATM, RISK_REVERSAL, STRANGLE);
  // Methods and curves
  private static final YieldCurveBundle CURVES = ForexTestsDataSets.createCurvesForex();
  private static final String[] CURVES_NAME = CURVES.getAllNames().toArray(new String[0]);
  private static final SmileDeltaTermStructureDataBundle SMILE_BUNDLE = new SmileDeltaTermStructureDataBundle(SMILE_TERM, SPOT, CURVES);
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();
  private static final ForexOptionVanillaBlackMethod METHOD_OPTION = new ForexOptionVanillaBlackMethod();
  private static final ForexDiscountingMethod METHOD_DISC = new ForexDiscountingMethod();
  private static final PresentValueBlackForexCalculator PVC_BLACK = PresentValueBlackForexCalculator.getInstance();
  private static final CurrencyExposureBlackForexCalculator CEC_BLACK = CurrencyExposureBlackForexCalculator.getInstance();
  private static final PresentValueCurveSensitivityBlackForexCalculator PVCSC_BLACK = PresentValueCurveSensitivityBlackForexCalculator.getInstance();
  private static final PresentValueVolatilitySensitivityBlackCalculator PVVSC_BLACK = PresentValueVolatilitySensitivityBlackCalculator.getInstance();
  // option
  private static final double STRIKE = 1.45;
  private static final boolean IS_CALL = true;
  private static final boolean IS_LONG = true;
  private static final double NOTIONAL = 100000000;
  private static final ZonedDateTime OPTION_PAY_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, BUSINESS_DAY, CALENDAR, Period.ofMonths(9));
  private static final ZonedDateTime OPTION_EXP_DATE = ScheduleCalculator.getAdjustedDate(OPTION_PAY_DATE, CALENDAR, -SETTLEMENT_DAYS);
  private static final ForexDefinition FOREX_DEFINITION = new ForexDefinition(CUR_1, CUR_2, OPTION_PAY_DATE, NOTIONAL, STRIKE);
  private static final ForexOptionVanillaDefinition FOREX_OPTION_DEFINITION = new ForexOptionVanillaDefinition(FOREX_DEFINITION, OPTION_EXP_DATE, IS_CALL, IS_LONG);
  private static final ForexOptionVanilla FOREX_OPTION = FOREX_OPTION_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);

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
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(CUR_1, CUR_2, PAY_DATE[indexPay], notional, strike);
    final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, EXPIRY_DATE[indexPay], isCall, isLong);
    final ForexOptionVanilla forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final double df = CURVES.getCurve(CURVES_NAME[1]).getDiscountFactor(ACT_ACT.getDayCountFraction(REFERENCE_DATE, PAY_DATE[indexPay]));
    final double forward = SPOT * CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(ACT_ACT.getDayCountFraction(REFERENCE_DATE, PAY_DATE[indexPay])) / df;
    final double volatility = SMILE_TERM.getVolatility(new Triple<Double, Double, Double>(TIME_TO_EXPIRY[indexPay + 1], strike, forward));
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, df, volatility);
    final Function1D<BlackFunctionData, Double> func = BLACK_FUNCTION.getPriceFunction(forexOption);
    final double priceExpected = func.evaluate(dataBlack) * notional;
    final MultipleCurrencyAmount priceComputed = METHOD_OPTION.presentValue(forexOption, SMILE_BUNDLE);
    assertEquals("Forex vanilla option: present value", priceExpected, priceComputed.getAmount(CUR_2), 1E-2);
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
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, BUSINESS_DAY, CALENDAR, Period.ofMonths(9));
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, CALENDAR, -SETTLEMENT_DAYS);
    final double timeToExpiry = ACT_ACT.getDayCountFraction(REFERENCE_DATE, expDate);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(CUR_1, CUR_2, payDate, notional, strike);
    final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionVanilla forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final double df = CURVES.getCurve(CURVES_NAME[1]).getDiscountFactor(ACT_ACT.getDayCountFraction(REFERENCE_DATE, payDate));
    final double forward = SPOT * CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(ACT_ACT.getDayCountFraction(REFERENCE_DATE, payDate)) / df;
    final double volatility = SMILE_TERM.getVolatility(new Triple<Double, Double, Double>(timeToExpiry, strike, forward));
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, df, volatility);
    final Function1D<BlackFunctionData, Double> func = BLACK_FUNCTION.getPriceFunction(forexOption);
    final double priceExpected = func.evaluate(dataBlack) * notional;
    final MultipleCurrencyAmount priceComputed = METHOD_OPTION.presentValue(forexOption, SMILE_BUNDLE);
    assertEquals("Forex vanilla option: present value", priceExpected, priceComputed.getAmount(CUR_2), 1E-2);
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
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, BUSINESS_DAY, CALENDAR, Period.ofMonths(9));
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, CALENDAR, -SETTLEMENT_DAYS);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(CUR_1, CUR_2, payDate, notional, strike);
    final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexDerivative forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final MultipleCurrencyAmount pvMethod = METHOD_OPTION.presentValue(forexOption, SMILE_BUNDLE);
    final MultipleCurrencyAmount pvCalculator = PVC_BLACK.visit(forexOption, SMILE_BUNDLE);
    assertEquals("Forex vanilla option: present value Method vs Calculator", pvMethod.getAmount(CUR_2), pvCalculator.getAmount(CUR_2), 1E-2);
  }

  @Test
  /**
   * Tests the present value long/short parity.
   */
  public void presentValueLongShort() {
    final ForexOptionVanillaDefinition forexOptionShortDefinition = new ForexOptionVanillaDefinition(FOREX_DEFINITION, OPTION_EXP_DATE, IS_CALL, !IS_LONG);
    final ForexDerivative forexOptionShort = forexOptionShortDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final MultipleCurrencyAmount pvShort = METHOD_OPTION.presentValue(forexOptionShort, SMILE_BUNDLE);
    final MultipleCurrencyAmount pvLong = METHOD_OPTION.presentValue(FOREX_OPTION, SMILE_BUNDLE);
    assertEquals("Forex vanilla option: present value long/short parity", pvLong.getAmount(CUR_2), -pvShort.getAmount(CUR_2), 1E-2);
    final MultipleCurrencyAmount ceShort = METHOD_OPTION.currencyExposure(forexOptionShort, SMILE_BUNDLE);
    final MultipleCurrencyAmount ceLong = METHOD_OPTION.currencyExposure(FOREX_OPTION, SMILE_BUNDLE);
    assertEquals("Forex vanilla option: currency exposure long/short parity", ceLong.getAmount(CUR_2), -ceShort.getAmount(CUR_2), 1E-2);
    assertEquals("Forex vanilla option: currency exposure long/short parity", ceLong.getAmount(CUR_1), -ceShort.getAmount(CUR_1), 1E-2);
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
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, BUSINESS_DAY, CALENDAR, Period.ofMonths(9));
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, CALENDAR, -SETTLEMENT_DAYS);
    final double timeToExpiry = ACT_ACT.getDayCountFraction(REFERENCE_DATE, expDate);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(CUR_1, CUR_2, payDate, notional, strike);
    final ForexOptionVanillaDefinition forexOptionDefinitionCall = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionVanillaDefinition forexOptionDefinitionPut = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, !isCall, isLong);
    final ForexOptionVanilla forexOptionCall = forexOptionDefinitionCall.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final ForexOptionVanilla forexOptionPut = forexOptionDefinitionPut.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final double dfDomestic = CURVES.getCurve(CURVES_NAME[1]).getDiscountFactor(ACT_ACT.getDayCountFraction(REFERENCE_DATE, payDate)); // USD
    final double dfForeign = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(ACT_ACT.getDayCountFraction(REFERENCE_DATE, payDate)); // EUR
    final double forward = SPOT * CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(ACT_ACT.getDayCountFraction(REFERENCE_DATE, payDate)) / dfDomestic;
    final double volatility = SMILE_TERM.getVolatility(new Triple<Double, Double, Double>(timeToExpiry, strike, forward));
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
    assertEquals("Forex vanilla option: currency exposure foreign - call", deltaSpotCall * notional, currencyExposureCallComputed.getAmount(CUR_1), 1E-2);
    assertEquals("Forex vanilla option: currency exposure domestic - call", -deltaSpotCall * notional * SPOT + priceComputedCall.getAmount(CUR_2), currencyExposureCallComputed.getAmount(CUR_2), 1E-2);
    final MultipleCurrencyAmount currencyExposurePutComputed = METHOD_OPTION.currencyExposure(forexOptionPut, SMILE_BUNDLE);
    assertEquals("Forex vanilla option: currency exposure foreign- put", deltaSpotPut * notional, currencyExposurePutComputed.getAmount(CUR_1), 1E-2);
    assertEquals("Forex vanilla option: currency exposure domestic - put", -deltaSpotPut * notional * SPOT + priceComputedPut.getAmount(CUR_2), currencyExposurePutComputed.getAmount(CUR_2), 1E-2);
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
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, BUSINESS_DAY, CALENDAR, Period.ofMonths(9));
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, CALENDAR, -SETTLEMENT_DAYS);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(CUR_1, CUR_2, payDate, notional, strike);
    final ForexOptionVanillaDefinition forexOptionDefinitionCall = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionVanillaDefinition forexOptionDefinitionPut = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, !isCall, isLong);
    final ForexOptionVanilla forexOptionCall = forexOptionDefinitionCall.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final ForexOptionVanilla forexOptionPut = forexOptionDefinitionPut.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final Forex forexForward = forexUnderlyingDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final MultipleCurrencyAmount currencyExposureCall = METHOD_OPTION.currencyExposure(forexOptionCall, SMILE_BUNDLE);
    final MultipleCurrencyAmount currencyExposurePut = METHOD_OPTION.currencyExposure(forexOptionPut, SMILE_BUNDLE);
    final MultipleCurrencyAmount currencyExposureForward = METHOD_DISC.currencyExposure(forexForward, SMILE_BUNDLE);
    assertEquals("Forex vanilla option: currency exposure put/call parity foreign", currencyExposureForward.getAmount(CUR_1),
        currencyExposureCall.getAmount(CUR_1) - currencyExposurePut.getAmount(CUR_1), 1E-2);
    assertEquals("Forex vanilla option: currency exposure put/call parity domestic", currencyExposureForward.getAmount(CUR_2),
        currencyExposureCall.getAmount(CUR_2) - currencyExposurePut.getAmount(CUR_2), 1E-2);
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
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, BUSINESS_DAY, CALENDAR, Period.ofMonths(9));
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, CALENDAR, -SETTLEMENT_DAYS);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(CUR_1, CUR_2, payDate, notional, strike);
    final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexDerivative forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final MultipleCurrencyAmount ceMethod = METHOD_OPTION.currencyExposure(forexOption, SMILE_BUNDLE);
    final MultipleCurrencyAmount ceCalculator = CEC_BLACK.visit(forexOption, SMILE_BUNDLE);
    assertEquals("Forex vanilla option: currency exposure Method vs Calculator", ceMethod.getAmount(CUR_1), ceCalculator.getAmount(CUR_1), 1E-2);
    assertEquals("Forex vanilla option: currency exposure Method vs Calculator", ceMethod.getAmount(CUR_2), ceCalculator.getAmount(CUR_2), 1E-2);
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
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, BUSINESS_DAY, CALENDAR, Period.ofMonths(9));
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, CALENDAR, -SETTLEMENT_DAYS);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(CUR_1, CUR_2, payDate, notional, strike);
    final ForexOptionVanillaDefinition forexOptionDefinitionCall = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionVanilla forexOptionCall = forexOptionDefinitionCall.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final Forex forexForward = forexUnderlyingDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final PresentValueSensitivity sensi = METHOD_OPTION.presentValueCurveSensitivity(forexOptionCall, SMILE_BUNDLE);
    final double dfDomestic = CURVES.getCurve(CURVES_NAME[1]).getDiscountFactor(forexForward.getPaymentTime());
    final double dfForeign = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(forexForward.getPaymentTime());
    final double forward = SPOT * dfForeign / dfDomestic;
    final double volatility = SMILE_TERM.getVolatility(new Triple<Double, Double, Double>(forexOptionCall.getTimeToExpiry(), strike, forward));
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
    curveNode = new YieldCurve(InterpolatedDoublesCurve.fromSorted(nodeTimes, yields, new LinearInterpolator1D()));
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
    assertEquals("Forex vanilla option: curve exposure", forexForward.getPaymentTime(), sensi.getSensitivity().get(CURVES_NAME[0]).get(0).first, 1E-2);
    assertEquals("Forex vanilla option: curve exposure", resultForeign, sensi.getSensitivity().get(CURVES_NAME[0]).get(0).second, 1E-2);
    //Domestic
    yields[0] = curveDomestic.getInterestRate(nodeTimes[0]);
    yields[1] = curveDomestic.getInterestRate(nodeTimes[1]);
    curveNode = new YieldCurve(InterpolatedDoublesCurve.fromSorted(nodeTimes, yields, new LinearInterpolator1D()));
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
    assertEquals("Forex vanilla option: curve exposure", forexForward.getPaymentTime(), sensi.getSensitivity().get(CURVES_NAME[1]).get(0).first, 1E-2);
    assertEquals("Forex vanilla option: curve exposure", resultDomestic, sensi.getSensitivity().get(CURVES_NAME[1]).get(0).second, 1E-2);
  }

  @Test
  /**
   * Test the present value curve sensitivity through the method and through the calculator.
   */
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final PresentValueSensitivity pvcsMethod = METHOD_OPTION.presentValueCurveSensitivity(FOREX_OPTION, SMILE_BUNDLE);
    final PresentValueSensitivity pvcsCalculator = PVCSC_BLACK.visit(FOREX_OPTION, SMILE_BUNDLE);
    assertEquals("Forex present value curve sensitivity: Method vs Calculator", pvcsMethod, pvcsCalculator);
  }

  @Test
  /**
   * Tests present value volatility sensitivity.
   */
  public void volatilitySensitivity() {
    PresentValueVolatilitySensitivityDataBundle sensi = METHOD_OPTION.presentValueVolatilitySensitivity(FOREX_OPTION, SMILE_BUNDLE);
    Pair<Currency, Currency> currencyPair = ObjectsPair.of(CUR_1, CUR_2);
    DoublesPair point = new DoublesPair(FOREX_OPTION.getTimeToExpiry(), STRIKE);
    assertEquals("Forex vanilla option: vega", currencyPair, sensi.getCurrencyPair());
    assertEquals("Forex vanilla option: vega size", 1, sensi.getVega().entrySet().size());
    assertTrue("Forex vanilla option: vega", sensi.getVega().containsKey(point));
    final double timeToExpiry = ACT_ACT.getDayCountFraction(REFERENCE_DATE, OPTION_EXP_DATE);
    final double df = CURVES.getCurve(CURVES_NAME[1]).getDiscountFactor(ACT_ACT.getDayCountFraction(REFERENCE_DATE, OPTION_PAY_DATE));
    final double forward = SPOT * CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(ACT_ACT.getDayCountFraction(REFERENCE_DATE, OPTION_PAY_DATE)) / df;
    final double volatility = SMILE_TERM.getVolatility(new Triple<Double, Double, Double>(timeToExpiry, STRIKE, forward));
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, df, volatility);
    double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(FOREX_OPTION, dataBlack);
    assertEquals("Forex vanilla option: vega", priceAdjoint[2] * NOTIONAL, sensi.getVega().get(point));
    final ForexOptionVanillaDefinition optionShortDefinition = new ForexOptionVanillaDefinition(FOREX_DEFINITION, OPTION_EXP_DATE, IS_CALL, !IS_LONG);
    final ForexOptionVanilla optionShort = optionShortDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    PresentValueVolatilitySensitivityDataBundle sensiShort = METHOD_OPTION.presentValueVolatilitySensitivity(optionShort, SMILE_BUNDLE);
    assertEquals("Forex vanilla option: vega short", -sensi.getVega().get(point), sensiShort.getVega().get(point));
  }

  @Test
  /**
   * Test the present value curve sensitivity through the method and through the calculator.
   */
  public void volatilitySensitivityMethodVsCalculator() {
    final PresentValueVolatilitySensitivityDataBundle pvvsMethod = METHOD_OPTION.presentValueVolatilitySensitivity(FOREX_OPTION, SMILE_BUNDLE);
    final PresentValueVolatilitySensitivityDataBundle pvvsCalculator = PVVSC_BLACK.visit(FOREX_OPTION, SMILE_BUNDLE);
    assertEquals("Forex present value curve sensitivity: Method vs Calculator", pvvsMethod, pvvsCalculator);
  }

  @Test
  /**
   * Tests present value volatility node sensitivity.
   */
  public void volatilityNodeSensitivity() {
    PresentValueVolatilityNodeSensitivityDataBundle sensi = METHOD_OPTION.presentValueVolatilityNodeSensitivity(FOREX_OPTION, SMILE_BUNDLE);
    assertEquals("Forex vanilla option: vega node size", NB_EXP + 1, sensi.getVega().getData().length);
    assertEquals("Forex vanilla option: vega node size", NB_STRIKE, sensi.getVega().getData()[0].length);
    Pair<Currency, Currency> currencyPair = ObjectsPair.of(CUR_1, CUR_2);
    assertEquals("Forex vanilla option: vega", currencyPair, sensi.getCurrencyPair());
    PresentValueVolatilitySensitivityDataBundle pointSensitivity = METHOD_OPTION.presentValueVolatilitySensitivity(FOREX_OPTION, SMILE_BUNDLE);
    double[][] nodeWeight = new double[NB_EXP + 1][NB_STRIKE];
    final double df = CURVES.getCurve(CURVES_NAME[1]).getDiscountFactor(ACT_ACT.getDayCountFraction(REFERENCE_DATE, OPTION_PAY_DATE));
    final double forward = SPOT * CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(ACT_ACT.getDayCountFraction(REFERENCE_DATE, OPTION_PAY_DATE)) / df;
    SMILE_TERM.getVolatilityAdjoint(FOREX_OPTION.getTimeToExpiry(), STRIKE, forward, nodeWeight);
    DoublesPair point = DoublesPair.of(FOREX_OPTION.getTimeToExpiry(), STRIKE);
    for (int loopexp = 0; loopexp < NB_EXP; loopexp++) {
      for (int loopstrike = 0; loopstrike < NB_STRIKE; loopstrike++) {
        assertEquals("Forex vanilla option: vega node", nodeWeight[loopexp][loopstrike] * pointSensitivity.getVega().get(point), sensi.getVega().getData()[loopexp][loopstrike]);
      }
    }
  }

}
