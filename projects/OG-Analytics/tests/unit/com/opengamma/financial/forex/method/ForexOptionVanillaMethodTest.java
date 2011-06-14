/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.method;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.forex.definition.ForexDefinition;
import com.opengamma.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.financial.forex.derivative.Forex;
import com.opengamma.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.option.definition.SmileDeltaParameter;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureParameter;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtil;
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
  private static final double[][] DELTA = new double[][] { {0.10, 0.25}, {0.10, 0.25}, {0.10, 0.25}, {0.10, 0.25}, {0.10, 0.25}};
  private static final double[][] RISK_REVERSAL = new double[][] { {-0.010, -0.0050}, {-0.011, -0.0060}, {-0.012, -0.0070}, {-0.013, -0.0080}, {-0.014, -0.0090}};
  private static final double[][] STRANGLE = new double[][] { {0.0300, 0.0100}, {0.0310, 0.0110}, {0.0320, 0.0120}, {0.0330, 0.0130}, {0.0340, 0.0140}};
  private static final SmileDeltaParameter[] VOLATILITY_TERM = new SmileDeltaParameter[NB_EXP + 1];
  static {
    for (int loopexp = 0; loopexp < NB_EXP + 1; loopexp++) {
      VOLATILITY_TERM[loopexp] = new SmileDeltaParameter(TIME_TO_EXPIRY[loopexp], ATM[loopexp], DELTA[loopexp], RISK_REVERSAL[loopexp], STRANGLE[loopexp]);
    }
  }
  private static final SmileDeltaTermStructureParameter SMILE_TERM = new SmileDeltaTermStructureParameter(VOLATILITY_TERM);
  // Methods and curves
  private static final YieldCurveBundle CURVES = ForexTestsDataSets.createCurvesForex();
  private static final String[] CURVES_NAME = CURVES.getAllNames().toArray(new String[0]);
  private static final SmileDeltaTermStructureDataBundle SMILE_BUNDLE = new SmileDeltaTermStructureDataBundle(SMILE_TERM, SPOT, CURVES);
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();
  private static final ForexOptionVanillaMethod METHOD_OPTION = new ForexOptionVanillaMethod();
  private static final ForexDiscountingMethod METHOD_DISC = new ForexDiscountingMethod();

  @Test
  /**
   * Tests the present value at a time grid point.
   */
  public void persentValueAtGridPoint() {
    double strike = 1.45;
    boolean isCall = true;
    double notional = 100000000;
    int indexPay = 2; // 1Y
    ForexDefinition forexUnderlyingDefinition = new ForexDefinition(CUR_1, CUR_2, PAY_DATE[indexPay], notional, strike);
    ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, EXPIRY_DATE[indexPay], isCall);
    ForexOptionVanilla forexOption = (ForexOptionVanilla) forexOptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    double df = CURVES.getCurve(CURVES_NAME[1]).getDiscountFactor(ACT_ACT.getDayCountFraction(REFERENCE_DATE, PAY_DATE[indexPay]));
    double forward = SPOT * CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(ACT_ACT.getDayCountFraction(REFERENCE_DATE, PAY_DATE[indexPay])) / df;
    double volatility = SMILE_TERM.getVolatility(new Triple<Double, Double, Double>(TIME_TO_EXPIRY[indexPay + 1], strike, forward));
    BlackFunctionData dataBlack = new BlackFunctionData(forward, df, volatility);
    Function1D<BlackFunctionData, Double> func = BLACK_FUNCTION.getPriceFunction(forexOption);
    double priceExpected = func.evaluate(dataBlack) * notional;
    MultipleCurrencyAmount priceComputed = METHOD_OPTION.presentValue(forexOption, SMILE_BUNDLE);
    assertEquals("Forex vanilla option: present value", priceExpected, priceComputed.getAmount(CUR_2), 1E-2);
  }

  @Test
  /**
   * Tests the present value against an explicit computation.
   */
  public void persentValue() {
    double strike = 1.45;
    boolean isCall = true;
    double notional = 100000000;
    ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, BUSINESS_DAY, CALENDAR, Period.ofMonths(9));
    ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, CALENDAR, -SETTLEMENT_DAYS);
    double timeToExpiry = ACT_ACT.getDayCountFraction(REFERENCE_DATE, expDate);
    ForexDefinition forexUnderlyingDefinition = new ForexDefinition(CUR_1, CUR_2, payDate, notional, strike);
    ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall);
    ForexOptionVanilla forexOption = (ForexOptionVanilla) forexOptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    double df = CURVES.getCurve(CURVES_NAME[1]).getDiscountFactor(ACT_ACT.getDayCountFraction(REFERENCE_DATE, payDate));
    double forward = SPOT * CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(ACT_ACT.getDayCountFraction(REFERENCE_DATE, payDate)) / df;
    double volatility = SMILE_TERM.getVolatility(new Triple<Double, Double, Double>(timeToExpiry, strike, forward));
    BlackFunctionData dataBlack = new BlackFunctionData(forward, df, volatility);
    Function1D<BlackFunctionData, Double> func = BLACK_FUNCTION.getPriceFunction(forexOption);
    double priceExpected = func.evaluate(dataBlack) * notional;
    MultipleCurrencyAmount priceComputed = METHOD_OPTION.presentValue(forexOption, SMILE_BUNDLE);
    assertEquals("Forex vanilla option: present value", priceExpected, priceComputed.getAmount(CUR_2), 1E-2);
  }

  @Test
  /**
   * Tests the currency exposure against an explicit computation.
   */
  public void currencyExposure() {
    double strike = 1.45;
    boolean isCall = true;
    double notional = 100000000;
    ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, BUSINESS_DAY, CALENDAR, Period.ofMonths(9));
    ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, CALENDAR, -SETTLEMENT_DAYS);
    double timeToExpiry = ACT_ACT.getDayCountFraction(REFERENCE_DATE, expDate);
    ForexDefinition forexUnderlyingDefinition = new ForexDefinition(CUR_1, CUR_2, payDate, notional, strike);
    ForexOptionVanillaDefinition forexOptionDefinitionCall = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall);
    ForexOptionVanillaDefinition forexOptionDefinitionPut = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, !isCall);
    ForexOptionVanilla forexOptionCall = (ForexOptionVanilla) forexOptionDefinitionCall.toDerivative(REFERENCE_DATE, CURVES_NAME);
    ForexOptionVanilla forexOptionPut = (ForexOptionVanilla) forexOptionDefinitionPut.toDerivative(REFERENCE_DATE, CURVES_NAME);
    double dfDomestic = CURVES.getCurve(CURVES_NAME[1]).getDiscountFactor(ACT_ACT.getDayCountFraction(REFERENCE_DATE, payDate)); // USD
    double dfForeign = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(ACT_ACT.getDayCountFraction(REFERENCE_DATE, payDate)); // EUR
    double forward = SPOT * CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(ACT_ACT.getDayCountFraction(REFERENCE_DATE, payDate)) / dfDomestic;
    double volatility = SMILE_TERM.getVolatility(new Triple<Double, Double, Double>(timeToExpiry, strike, forward));
    BlackFunctionData dataBlack = new BlackFunctionData(forward, dfDomestic, volatility);
    double[] priceAdjointCall = BLACK_FUNCTION.getPriceAdjoint(forexOptionCall, dataBlack);
    double[] priceAdjointPut = BLACK_FUNCTION.getPriceAdjoint(forexOptionPut, dataBlack);
    double deltaForwardCall = priceAdjointCall[1];
    double deltaForwardPut = priceAdjointPut[1];
    double deltaSpotCall = deltaForwardCall * dfForeign / dfDomestic;
    double deltaSpotPut = deltaForwardPut * dfForeign / dfDomestic;
    MultipleCurrencyAmount priceComputedCall = METHOD_OPTION.presentValue(forexOptionCall, SMILE_BUNDLE);
    MultipleCurrencyAmount priceComputedPut = METHOD_OPTION.presentValue(forexOptionPut, SMILE_BUNDLE);
    MultipleCurrencyAmount currencyExposureCallComputed = METHOD_OPTION.currencyExposure(forexOptionCall, SMILE_BUNDLE);
    assertEquals("Forex vanilla option: currency exposure foreign - call", deltaSpotCall * notional, currencyExposureCallComputed.getAmount(CUR_1), 1E-2);
    assertEquals("Forex vanilla option: currency exposure domestic - call", -deltaSpotCall * notional * SPOT + priceComputedCall.getAmount(CUR_2), currencyExposureCallComputed.getAmount(CUR_2), 1E-2);
    MultipleCurrencyAmount currencyExposurePutComputed = METHOD_OPTION.currencyExposure(forexOptionPut, SMILE_BUNDLE);
    assertEquals("Forex vanilla option: currency exposure foreign- put", deltaSpotPut * notional, currencyExposurePutComputed.getAmount(CUR_1), 1E-2);
    assertEquals("Forex vanilla option: currency exposure domestic - put", -deltaSpotPut * notional * SPOT + priceComputedPut.getAmount(CUR_2), currencyExposurePutComputed.getAmount(CUR_2), 1E-2);
  }

  @Test
  /**
   * Tests the put/call parity currency exposure.
   */
  public void currencyExposurePutCallParity() {
    double strike = 1.45;
    boolean isCall = true;
    double notional = 100000000;
    ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, BUSINESS_DAY, CALENDAR, Period.ofMonths(9));
    ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, CALENDAR, -SETTLEMENT_DAYS);
    ForexDefinition forexUnderlyingDefinition = new ForexDefinition(CUR_1, CUR_2, payDate, notional, strike);
    ForexOptionVanillaDefinition forexOptionDefinitionCall = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, isCall);
    ForexOptionVanillaDefinition forexOptionDefinitionPut = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, expDate, !isCall);
    ForexOptionVanilla forexOptionCall = (ForexOptionVanilla) forexOptionDefinitionCall.toDerivative(REFERENCE_DATE, CURVES_NAME);
    ForexOptionVanilla forexOptionPut = (ForexOptionVanilla) forexOptionDefinitionPut.toDerivative(REFERENCE_DATE, CURVES_NAME);
    Forex forexForward = forexUnderlyingDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    MultipleCurrencyAmount currencyExposureCall = METHOD_OPTION.currencyExposure(forexOptionCall, SMILE_BUNDLE);
    MultipleCurrencyAmount currencyExposurePut = METHOD_OPTION.currencyExposure(forexOptionPut, SMILE_BUNDLE);
    MultipleCurrencyAmount currencyExposureForward = METHOD_DISC.currencyExposure(forexForward, SMILE_BUNDLE);
    assertEquals("Forex vanilla option: currency exposure put/call parity foreign", currencyExposureForward.getAmount(CUR_1),
        currencyExposureCall.getAmount(CUR_1) - currencyExposurePut.getAmount(CUR_1), 1E-2);
    assertEquals("Forex vanilla option: currency exposure put/call parity domestic", currencyExposureForward.getAmount(CUR_2),
        currencyExposureCall.getAmount(CUR_2) - currencyExposurePut.getAmount(CUR_2), 1E-2);
  }

}
