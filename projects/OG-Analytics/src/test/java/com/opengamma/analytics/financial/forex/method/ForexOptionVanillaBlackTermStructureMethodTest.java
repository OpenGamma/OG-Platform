/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.method;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.forex.calculator.CurrencyExposureBlackTermStructureForexCalculator;
import com.opengamma.analytics.financial.forex.calculator.PresentValueBlackTermStructureForexCalculator;
import com.opengamma.analytics.financial.forex.calculator.PresentValueCurveSensitivityBlackTermStructureForexCalculator;
import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackForexTermStructureBundle;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.volatility.curve.BlackForexTermStructureParameters;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * @deprecated The class tests deprecated code
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class ForexOptionVanillaBlackTermStructureMethodTest {
  // General
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final int SETTLEMENT_DAYS = 2;
  // Vol data
  private static final Currency EUR = Currency.EUR;
  private static final Currency USD = Currency.USD;
  private static final Pair<Currency, Currency> CURRENCY_PAIR = Pairs.of(EUR, USD);
  private static final double SPOT = 1.40;
  private static final FXMatrix FX_MATRIX = new FXMatrix(EUR, USD, SPOT);
  private static final Period[] EXPIRY_PERIOD = new Period[] {Period.ofMonths(3), Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2), Period.ofYears(5) };
  private static final double[] VOL = new double[] {0.20, 0.25, 0.20, 0.15, 0.20 };
  private static final int NB_EXP = EXPIRY_PERIOD.length;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 6, 13);
  private static final ZonedDateTime REFERENCE_SPOT = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final ZonedDateTime[] PAY_DATE = new ZonedDateTime[NB_EXP];
  private static final ZonedDateTime[] EXPIRY_DATE = new ZonedDateTime[NB_EXP];
  private static final double[] TIME_TO_EXPIRY = new double[NB_EXP];
  static {
    for (int loopexp = 0; loopexp < NB_EXP; loopexp++) {
      PAY_DATE[loopexp] = ScheduleCalculator.getAdjustedDate(REFERENCE_SPOT, EXPIRY_PERIOD[loopexp], BUSINESS_DAY, CALENDAR);
      EXPIRY_DATE[loopexp] = ScheduleCalculator.getAdjustedDate(PAY_DATE[loopexp], -SETTLEMENT_DAYS, CALENDAR);
      TIME_TO_EXPIRY[loopexp] = TimeCalculator.getTimeBetween(REFERENCE_DATE, EXPIRY_DATE[loopexp]);
    }
  }
  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final InterpolatedDoublesCurve TERM_STRUCTURE_VOL = new InterpolatedDoublesCurve(TIME_TO_EXPIRY, VOL, LINEAR_FLAT, true);

  // Methods and curves
  private static final YieldCurveBundle CURVES = TestsDataSetsForex.createCurvesForex();
  private static final Map<String, Currency> CURVE_CURRENCY = TestsDataSetsForex.curveCurrency();
  private static final YieldCurveBundle CURVES_FX = new YieldCurveBundle(CURVES.getCurvesMap(), FX_MATRIX, CURVE_CURRENCY);
  private static final String[] CURVES_NAME = TestsDataSetsForex.curveNames();
  private static final Pair<Currency, Currency> CCY = Pairs.of(EUR, USD);
  private static final BlackForexTermStructureParameters BLACK_TS_VOL = new BlackForexTermStructureParameters(TERM_STRUCTURE_VOL);
  private static final YieldCurveWithBlackForexTermStructureBundle BUNDLE_BLACK_TS = new YieldCurveWithBlackForexTermStructureBundle(CURVES, BLACK_TS_VOL, CURRENCY_PAIR);
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();
  private static final ForexOptionVanillaBlackTermStructureMethod METHOD_BLACK_TS = ForexOptionVanillaBlackTermStructureMethod.getInstance();
  private static final PresentValueBlackTermStructureForexCalculator PVC_BLACK_TS = PresentValueBlackTermStructureForexCalculator.getInstance();
  private static final PresentValueCurveSensitivityBlackTermStructureForexCalculator PVCSC_BLACK_TS = PresentValueCurveSensitivityBlackTermStructureForexCalculator.getInstance();
  private static final CurrencyExposureBlackTermStructureForexCalculator CEC_BLACK_TS = CurrencyExposureBlackTermStructureForexCalculator.getInstance();
  private static final ForexOptionVanillaBlackSmileMethod METHOD_SMILE = ForexOptionVanillaBlackSmileMethod.getInstance();

  // For comparison
  private static final double[] DELTA = new double[] {0.10, 0.25 };
  private static final double[][] RISK_REVERSAL_FLAT = new double[][] { {0.0, 0.0 }, {0.0, 0.0 }, {0.0, 0.0 }, {0.0, 0.0 }, {0.0, 0.0 } };
  private static final double[][] STRANGLE_FLAT = new double[][] { {0.0, 0.0 }, {0.0, 0.0 }, {0.0, 0.0 }, {0.0, 0.0 }, {0.0, 0.0 } };
  private static final SmileDeltaTermStructureParametersStrikeInterpolation SMILE_TERM_FLAT = new SmileDeltaTermStructureParametersStrikeInterpolation(TIME_TO_EXPIRY, DELTA, VOL, RISK_REVERSAL_FLAT,
      STRANGLE_FLAT, LINEAR_FLAT, LINEAR_FLAT);
  private static final SmileDeltaTermStructureDataBundle BUNDLE_SMILE = new SmileDeltaTermStructureDataBundle(CURVES, SMILE_TERM_FLAT, CCY);

  // Some options
  private static final double STRIKE = 1.45;
  private static final boolean IS_CALL = true;
  private static final boolean IS_LONG = true;
  private static final double NOTIONAL_EUR = 100000000;
  private static final ZonedDateTime OPT_PAY_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
  private static final ZonedDateTime OPT_EXP_DATE = ScheduleCalculator.getAdjustedDate(OPT_PAY_DATE, -SETTLEMENT_DAYS, CALENDAR);
  private static final ForexDefinition FX_DEFINITION = new ForexDefinition(EUR, USD, OPT_PAY_DATE, NOTIONAL_EUR, STRIKE);
  private static final ForexOptionVanillaDefinition CALL_LONG_DEFINITION = new ForexOptionVanillaDefinition(FX_DEFINITION, OPT_EXP_DATE, IS_CALL, IS_LONG);
  private static final ForexOptionVanillaDefinition PUT_SHORT_DEFINITION = new ForexOptionVanillaDefinition(FX_DEFINITION, OPT_EXP_DATE, !IS_CALL, !IS_LONG);
  private static final Forex FX = FX_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final ForexOptionVanilla CALL_LONG = CALL_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final ForexOptionVanilla PUT_SHORT = PUT_SHORT_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_VOL = 1.0E-8;

  @Test
  /**
   * Tests the present value against an explicit computation.
   */
  public void presentValue() {
    final double timeToExpiry = TimeCalculator.getTimeBetween(REFERENCE_DATE, OPT_EXP_DATE);
    final double df = CURVES.getCurve(CURVES_NAME[1]).getDiscountFactor(TimeCalculator.getTimeBetween(REFERENCE_DATE, OPT_PAY_DATE));
    final double forward = SPOT * CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(TimeCalculator.getTimeBetween(REFERENCE_DATE, OPT_PAY_DATE)) / df;
    final double volatility = TERM_STRUCTURE_VOL.getYValue(timeToExpiry);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, df, volatility);
    final Function1D<BlackFunctionData, Double> func = BLACK_FUNCTION.getPriceFunction(CALL_LONG);
    final double priceExpected = func.evaluate(dataBlack) * NOTIONAL_EUR;
    final MultipleCurrencyAmount priceComputed = METHOD_BLACK_TS.presentValue(CALL_LONG, BUNDLE_BLACK_TS);
    assertEquals("Forex vanilla option: present value", priceExpected, priceComputed.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the put/call parity present value.
   */
  public void presentValuePutCallParity() {
    final MultipleCurrencyAmount pvCall = METHOD_BLACK_TS.presentValue(CALL_LONG, BUNDLE_BLACK_TS);
    final MultipleCurrencyAmount pvPut = METHOD_BLACK_TS.presentValue(PUT_SHORT, BUNDLE_BLACK_TS);
    final MultipleCurrencyAmount pvForward = FX.accept(PVC_BLACK_TS, CURVES_FX);
    assertEquals("Forex vanilla option: present value put/call parity", BUNDLE_BLACK_TS.getFxRates().convert(pvForward, EUR).getAmount(), BUNDLE_BLACK_TS.getFxRates().convert(pvCall.plus(pvPut), EUR)
        .getAmount(), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value Method versus the Calculator.
   */
  public void presentValueMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD_BLACK_TS.presentValue(CALL_LONG, BUNDLE_BLACK_TS);
    final MultipleCurrencyAmount pvCalculator = CALL_LONG.accept(PVC_BLACK_TS, BUNDLE_BLACK_TS);
    assertEquals("Forex vanilla option: present value Method vs Calculator", pvMethod.getAmount(USD), pvCalculator.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value curve sensitivity Method versus the Calculator.
   */
  public void presentValueCurveSensitivity() {
    MultipleCurrencyInterestRateCurveSensitivity pvcsTS = METHOD_BLACK_TS.presentValueCurveSensitivity(CALL_LONG, BUNDLE_BLACK_TS);
    pvcsTS = pvcsTS.cleaned();
    MultipleCurrencyInterestRateCurveSensitivity pvcsSmile = METHOD_SMILE.presentValueCurveSensitivity(CALL_LONG, BUNDLE_SMILE);
    pvcsSmile = pvcsSmile.cleaned();
    AssertSensivityObjects.assertEquals("Forex vanilla option: present value curve sensitivity vs flat smile", pvcsTS, pvcsSmile, TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value curve sensitivity Method versus the Calculator.
   */
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final MultipleCurrencyInterestRateCurveSensitivity pvcsMethod = METHOD_BLACK_TS.presentValueCurveSensitivity(CALL_LONG, BUNDLE_BLACK_TS);
    final MultipleCurrencyInterestRateCurveSensitivity pvcsCalculator = CALL_LONG.accept(PVCSC_BLACK_TS, BUNDLE_BLACK_TS);
    AssertSensivityObjects.assertEquals("Forex vanilla option: present value curve sensitivity Method vs Calculator", pvcsMethod, pvcsCalculator, TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the currency exposure against the present value.
   */
  public void currencyExposureVsPresentValue() {
    final MultipleCurrencyAmount pv = METHOD_BLACK_TS.presentValue(CALL_LONG, BUNDLE_BLACK_TS);
    final MultipleCurrencyAmount ce = METHOD_BLACK_TS.currencyExposure(CALL_LONG, BUNDLE_BLACK_TS);
    assertEquals("Forex vanilla option: currency exposure vs present value", ce.getAmount(USD) + ce.getAmount(EUR) * SPOT, pv.getAmount(USD), TOLERANCE_PV);
    final InstrumentDerivative instrument = CALL_LONG;
    final MultipleCurrencyAmount ce2 = METHOD_BLACK_TS.currencyExposure(instrument, BUNDLE_BLACK_TS);
    assertEquals("Forex vanilla option: currency exposure", ce.getAmount(USD), ce2.getAmount(USD), TOLERANCE_PV);
    assertEquals("Forex vanilla option: currency exposure", ce.getAmount(EUR), ce2.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the put/call parity currency exposure.
   */
  public void currencyExposurePutCallParity() {
    final MultipleCurrencyAmount currencyExposureCall = METHOD_BLACK_TS.currencyExposure(CALL_LONG, BUNDLE_BLACK_TS);
    final MultipleCurrencyAmount currencyExposurePut = METHOD_BLACK_TS.currencyExposure(PUT_SHORT, BUNDLE_BLACK_TS);
    final MultipleCurrencyAmount currencyExposureForward = FX.accept(CEC_BLACK_TS, CURVES_FX);
    assertEquals("Forex vanilla option: currency exposure put/call parity foreign", currencyExposureForward.getAmount(EUR), currencyExposureCall.getAmount(EUR) + currencyExposurePut.getAmount(EUR),
        TOLERANCE_PV);
    assertEquals("Forex vanilla option: currency exposure put/call parity domestic", currencyExposureForward.getAmount(USD), currencyExposureCall.getAmount(USD) + currencyExposurePut.getAmount(USD),
        TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the implied volatility.
   */
  public void impliedVolatility() {
    final double ivCall = METHOD_BLACK_TS.impliedVolatility(CALL_LONG, BUNDLE_BLACK_TS);
    final double ivPut = METHOD_BLACK_TS.impliedVolatility(PUT_SHORT, BUNDLE_BLACK_TS);
    final double volExpected = BLACK_TS_VOL.getVolatility(CALL_LONG.getTimeToExpiry());
    assertEquals("Forex vanilla option: implied volatility", ivCall, volExpected, TOLERANCE_VOL);
    assertEquals("Forex vanilla option: implied volatility", ivPut, volExpected, TOLERANCE_VOL);
  }

  // TODO: test delta relative and delta relative spot

}
