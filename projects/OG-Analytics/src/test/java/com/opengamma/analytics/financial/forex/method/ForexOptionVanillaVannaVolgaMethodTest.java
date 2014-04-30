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

import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivityUtils;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaParameters;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureVannaVolgaDataBundle;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParameters;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.util.amount.SurfaceValue;
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
import com.opengamma.util.tuple.Pairs;

/**
 * @deprecated This class tests deprecated code
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class ForexOptionVanillaVannaVolgaMethodTest {
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
  private static final double SPOT = 1.25;
  private static final FXMatrix FX_MATRIX = new FXMatrix(EUR, USD, SPOT);
  private static final double[] ATM = {0.11, 0.115, 0.12, 0.12, 0.125, 0.13 };
  private static final double[] DELTA = new double[] {0.25 };
  private static final double[][] RISK_REVERSAL = new double[][] { {0.015 }, {0.020 }, {0.025 }, {0.03 }, {0.025 }, {0.030 } };
  private static final double[][] STRANGLE = new double[][] { {0.002 }, {0.003 }, {0.004 }, {0.0045 }, {0.0045 }, {0.0045 } };
  private static final double[][] RISK_REVERSAL_FLAT = new double[][] { {0.0 }, {0.0 }, {0.0 }, {0.0 }, {0.0 }, {0.0 } };
  private static final double[][] STRANGLE_FLAT = new double[][] { {0.0 }, {0.0 }, {0.0 }, {0.0 }, {0.0 }, {0.0 } };
  private static final Interpolator1D INTERPOLATOR_STRIKE = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR,
      Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
  private static final SmileDeltaTermStructureParameters SMILE_TERM = new SmileDeltaTermStructureParameters(TIME_TO_EXPIRY, DELTA, ATM, RISK_REVERSAL, STRANGLE);
  private static final SmileDeltaTermStructureParametersStrikeInterpolation SMILE_TERM_STRIKE_INT = new SmileDeltaTermStructureParametersStrikeInterpolation(TIME_TO_EXPIRY, DELTA, ATM, RISK_REVERSAL,
      STRANGLE, INTERPOLATOR_STRIKE);
  private static final SmileDeltaTermStructureParametersStrikeInterpolation SMILE_TERM_STRIKE_INT_FLAT = new SmileDeltaTermStructureParametersStrikeInterpolation(TIME_TO_EXPIRY, DELTA, ATM,
      RISK_REVERSAL_FLAT, STRANGLE_FLAT, INTERPOLATOR_STRIKE);

  // Methods and curves
  private static final String[] CURVES_NAME = TestsDataSetsForex.curveNames();
  private static final YieldCurveBundle CURVES = new YieldCurveBundle(FX_MATRIX, TestsDataSetsForex.curveCurrency(), TestsDataSetsForex.createCurvesForex().getCurvesMap());
  private static final ForexOptionVanillaVannaVolgaMethod METHOD_VANNA_VOLGA = ForexOptionVanillaVannaVolgaMethod.getInstance();
  private static final ForexOptionVanillaBlackSmileMethod METHOD_BLACK = ForexOptionVanillaBlackSmileMethod.getInstance();
  private static final ForexDiscountingMethod METHOD_DISC = ForexDiscountingMethod.getInstance();

  private static final SmileDeltaTermStructureDataBundle SMILE_BUNDLE_STRIKE_INT = new SmileDeltaTermStructureDataBundle(CURVES, SMILE_TERM_STRIKE_INT, Pairs.of(EUR, USD));
  private static final SmileDeltaTermStructureDataBundle SMILE_BUNDLE_STRIKE_INT_FLAT = new SmileDeltaTermStructureDataBundle(CURVES, SMILE_TERM_STRIKE_INT_FLAT, Pairs.of(EUR, USD));
  private static final SmileDeltaTermStructureVannaVolgaDataBundle SMILE_BUNDLE = new SmileDeltaTermStructureVannaVolgaDataBundle(CURVES, SMILE_TERM, Pairs.of(EUR, USD));
  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();
  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_W = 1.0E-10;

  @Test
  /**
   * Tests put/call parity.
   */
  public void putCallParity() {
    final int nbStrike = 20;
    final double strikeMin = 1.00;
    final double strikeRange = 0.80;
    final double[] strikes = new double[nbStrike + 1];
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime optionExpiry = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(18), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime optionPay = ScheduleCalculator.getAdjustedDate(optionExpiry, SETTLEMENT_DAYS, CALENDAR);
    final ForexOptionVanilla[] call = new ForexOptionVanilla[nbStrike + 1];
    final ForexOptionVanilla[] put = new ForexOptionVanilla[nbStrike + 1];
    for (int loopstrike = 0; loopstrike <= nbStrike; loopstrike++) {
      strikes[loopstrike] = strikeMin + loopstrike * strikeRange / nbStrike;
      final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, optionPay, notional, strikes[loopstrike]);
      final ForexOptionVanillaDefinition callDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, optionExpiry, isCall, isLong);
      final ForexOptionVanillaDefinition putDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, optionExpiry, !isCall, !isLong);
      call[loopstrike] = callDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
      put[loopstrike] = putDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
      final Forex forexForward = forexUnderlyingDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
      // Present value
      final MultipleCurrencyAmount pvCall = METHOD_VANNA_VOLGA.presentValue(call[loopstrike], SMILE_BUNDLE);
      final MultipleCurrencyAmount pvPut = METHOD_VANNA_VOLGA.presentValue(put[loopstrike], SMILE_BUNDLE);
      final MultipleCurrencyAmount pvForward = METHOD_DISC.presentValue(forexForward, SMILE_BUNDLE);
      assertEquals("Forex vanilla option: vanna-volga present value put/call parity", pvForward.getAmount(USD) + pvForward.getAmount(EUR) * SPOT, pvCall.getAmount(USD) + pvPut.getAmount(USD),
          TOLERANCE_PV);
      // Currency exposure
      final MultipleCurrencyAmount ceCall = METHOD_VANNA_VOLGA.currencyExposure(call[loopstrike], SMILE_BUNDLE);
      final MultipleCurrencyAmount cePut = METHOD_VANNA_VOLGA.currencyExposure(put[loopstrike], SMILE_BUNDLE);
      final MultipleCurrencyAmount ceForward = METHOD_DISC.currencyExposure(forexForward, SMILE_BUNDLE);
      assertEquals("Forex vanilla option: vanna-volga currency exposure put/call parity", ceForward.getAmount(USD), ceCall.getAmount(USD) + cePut.getAmount(USD), TOLERANCE_PV);
      assertEquals("Forex vanilla option: vanna-volga currency exposure put/call parity", ceForward.getAmount(EUR), ceCall.getAmount(EUR) + cePut.getAmount(EUR), TOLERANCE_PV);
      // Vega
      final PresentValueForexBlackVolatilitySensitivity pvbsCall = METHOD_VANNA_VOLGA.presentValueBlackVolatilitySensitivity(call[loopstrike], SMILE_BUNDLE);
      final PresentValueForexBlackVolatilitySensitivity pvbsPut = METHOD_VANNA_VOLGA.presentValueBlackVolatilitySensitivity(put[loopstrike], SMILE_BUNDLE);
      assertTrue("Forex vanilla option: vanna-volga sensitivity put/call parity - strike " + loopstrike, PresentValueForexBlackVolatilitySensitivity.compare(pvbsCall.plus(pvbsPut),
          new PresentValueForexBlackVolatilitySensitivity(EUR, USD, SurfaceValue.from(DoublesPair.of(0.0d, 0.0d), 0.0d)), TOLERANCE_PV));
      // Curve sensitivty
      final MultipleCurrencyInterestRateCurveSensitivity pvcsCall = METHOD_VANNA_VOLGA.presentValueCurveSensitivity(call[loopstrike], SMILE_BUNDLE);
      final MultipleCurrencyInterestRateCurveSensitivity pvcsPut = METHOD_VANNA_VOLGA.presentValueCurveSensitivity(put[loopstrike], SMILE_BUNDLE);
      final MultipleCurrencyInterestRateCurveSensitivity pvcsForward = METHOD_DISC.presentValueCurveSensitivity(forexForward, SMILE_BUNDLE);
      final MultipleCurrencyInterestRateCurveSensitivity pvcsOpt = pvcsCall.plus(pvcsPut).cleaned();
      assertTrue("Forex vanilla option: vanna-volga curve sensitivity put/call parity - strike " + loopstrike, InterestRateCurveSensitivityUtils.compare(pvcsForward.getSensitivity(USD)
          .getSensitivities().get(CURVES_NAME[1]), pvcsOpt.getSensitivity(USD).getSensitivities().get(CURVES_NAME[1]), TOLERANCE_PV));
      assertTrue(
          "Forex vanilla option: vanna-volga curve sensitivity put/call parity - strike " + loopstrike,
          InterestRateCurveSensitivityUtils.compare(pvcsForward.getSensitivity(EUR).multipliedBy(SPOT).getSensitivities().get(CURVES_NAME[0]),
              pvcsOpt.getSensitivity(USD).getSensitivities().get(CURVES_NAME[0]), TOLERANCE_PV));
      // Test note: the options sensitivities are in the pv currency (USD)
    }
  }

  @Test
  /**
   * Tests vanna-volga weights.
   */
  public void weight() {
    final int nbStrike = 10;
    final double strikeMin = 1.00;
    final double strikeRange = 0.80;
    final double[] strikes = new double[nbStrike + 1];
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime optionExpiry = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(18), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime optionPay = ScheduleCalculator.getAdjustedDate(optionExpiry, SETTLEMENT_DAYS, CALENDAR);
    final ForexOptionVanilla[] forexOption = new ForexOptionVanilla[nbStrike + 1];
    for (int loopstrike = 0; loopstrike <= nbStrike; loopstrike++) {
      strikes[loopstrike] = strikeMin + loopstrike * strikeRange / nbStrike;
      final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, optionPay, notional, strikes[loopstrike]);
      final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, optionExpiry, isCall, isLong);
      forexOption[loopstrike] = forexOptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    }
    final double forward = METHOD_BLACK.forwardForexRate(forexOption[0], SMILE_BUNDLE);
    final double dfDomestic = CURVES.getCurve(CURVES_NAME[1]).getDiscountFactor(forexOption[0].getUnderlyingForex().getPaymentTime()); // USD
    final SmileDeltaParameters smileAtTime = SMILE_BUNDLE.getSmile(EUR, USD, forexOption[0].getTimeToExpiry());
    final double[] strikesVV = smileAtTime.getStrike(forward);
    final double[] volVV = smileAtTime.getVolatility();
    for (int loopstrike = 0; loopstrike <= nbStrike; loopstrike++) {
      final double[] weightsComputed = METHOD_VANNA_VOLGA.vannaVolgaWeights(forexOption[loopstrike], forward, dfDomestic, strikesVV, volVV);
      final double[] vega = new double[3];
      final BlackFunctionData dataBlackATM = new BlackFunctionData(forward, dfDomestic, volVV[1]);
      for (int loopvv = 0; loopvv < 3; loopvv++) {
        final EuropeanVanillaOption optionVV = new EuropeanVanillaOption(strikesVV[loopvv], forexOption[loopstrike].getTimeToExpiry(), true);
        vega[loopvv] = BLACK_FUNCTION.getVegaFunction(optionVV).evaluate(dataBlackATM);
      }
      final double vegaFlat = BLACK_FUNCTION.getVegaFunction(forexOption[loopstrike]).evaluate(dataBlackATM);
      vega[1] = vegaFlat;
      final double lnk21 = Math.log(strikesVV[1] / strikesVV[0]);
      final double lnk31 = Math.log(strikesVV[2] / strikesVV[0]);
      final double lnk32 = Math.log(strikesVV[2] / strikesVV[1]);
      final double[] lnk = new double[3];
      for (int loopvv = 0; loopvv < 3; loopvv++) {
        lnk[loopvv] = Math.log(strikesVV[loopvv] / strikes[loopstrike]);
      }
      final double[] weightExpected = new double[3];
      weightExpected[0] = vegaFlat * lnk[1] * lnk[2] / (vega[0] * lnk21 * lnk31);
      weightExpected[1] = -vegaFlat * lnk[0] * lnk[2] / (vega[1] * lnk21 * lnk32);
      weightExpected[2] = vegaFlat * lnk[0] * lnk[1] / (vega[2] * lnk31 * lnk32);
      for (int loopvv = 0; loopvv < 3; loopvv = loopvv + 2) {
        assertEquals("Vanna-volga: adjustment weights", weightExpected[loopvv], weightsComputed[loopvv], TOLERANCE_W);
      }
    }
  }

  @Test
  /**
   * Tests the method with hard-coded values.
   */
  public void presentValueHardCoded() {
    final int nbStrike = 10;
    final double strikeMin = 1.00;
    final double strikeRange = 0.80;
    final double[] strikes = new double[nbStrike + 1];
    final double[] pvExpected = new double[] {2.424230702209554E7, 1.7281121064678535E7, 1.1555353254317978E7, 7427784.598448814, 4779647.986370251, 3172355.215467841, 2160614.7441477147,
        1463552.789015782, 956054.5074424933, 591821.3170718873, 345218.2942715228 };
    //        {2.2310683662940994E7, 1.5555161312097391E7, 1.0190176799879666E7, 6473496.491, 4163016.359783777, 2771166.010, 1878371.939, 1251328.424, 796469.444,
    //        477901.996, 269615.783 };
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime optionExpiry = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(18), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime optionPay = ScheduleCalculator.getAdjustedDate(optionExpiry, SETTLEMENT_DAYS, CALENDAR);
    final ForexOptionVanilla[] forexOption = new ForexOptionVanilla[nbStrike + 1];
    for (int loopstrike = 0; loopstrike <= nbStrike; loopstrike++) {
      strikes[loopstrike] = strikeMin + loopstrike * strikeRange / nbStrike;
      final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, optionPay, notional, strikes[loopstrike]);
      final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, optionExpiry, isCall, isLong);
      forexOption[loopstrike] = forexOptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    }
    final double[] pvVV = new double[nbStrike + 1];
    for (int loopstrike = 0; loopstrike <= nbStrike; loopstrike++) {
      pvVV[loopstrike] = METHOD_VANNA_VOLGA.presentValue(forexOption[loopstrike], SMILE_BUNDLE).getAmount(USD);
      assertEquals("Forex vanilla option: present value vanna-volga / hard-coded", pvExpected[loopstrike], pvVV[loopstrike], TOLERANCE_PV);
    }
  }

  @Test
  /**
   * Check the price implied by the vanna-volga method and compares it to the market prices at the market data points.
   */
  public void presentValueAtMarketData() {
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime optionExpiry = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(18), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime optionPay = ScheduleCalculator.getAdjustedDate(optionExpiry, SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingSpotDefinition = new ForexDefinition(EUR, USD, optionPay, notional, SPOT);
    final ForexOptionVanillaDefinition forexOptionSpotDefinition = new ForexOptionVanillaDefinition(forexUnderlyingSpotDefinition, optionExpiry, isCall, isLong);
    final ForexOptionVanilla forexOptionSpot = forexOptionSpotDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final double forward = METHOD_BLACK.forwardForexRate(forexOptionSpot, SMILE_BUNDLE_STRIKE_INT);
    final SmileDeltaParameters smileTime = SMILE_TERM.getSmileForTime(forexOptionSpot.getTimeToExpiry());
    final double[] strikes = smileTime.getStrike(forward);
    final int nbStrike = strikes.length;
    final ForexOptionVanilla[] forexOption = new ForexOptionVanilla[nbStrike];
    for (int loopstrike = 0; loopstrike < nbStrike; loopstrike++) {
      final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, optionPay, notional, strikes[loopstrike]);
      final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, optionExpiry, isCall, isLong);
      forexOption[loopstrike] = forexOptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    }
    final double[] pvVV = new double[nbStrike];
    final double[] pvInt = new double[nbStrike];
    for (int loopstrike = 0; loopstrike < nbStrike; loopstrike++) {
      pvVV[loopstrike] = METHOD_VANNA_VOLGA.presentValue(forexOption[loopstrike], SMILE_BUNDLE).getAmount(USD);
      pvInt[loopstrike] = METHOD_BLACK.presentValue(forexOption[loopstrike], SMILE_BUNDLE_STRIKE_INT).getAmount(USD);
      assertEquals("Forex vanilla option: currency exposure put/call parity domestic", pvInt[loopstrike], pvVV[loopstrike], TOLERANCE_PV);
    }
  }

  @Test
  /**
   * Tests the currency exposure in the Vanna-Volga method.
   */
  public void currencyExposure() {
    final int nbStrike = 10;
    final double strikeMin = 1.00;
    final double strikeRange = 0.80;
    final double[] strikes = new double[nbStrike + 1];
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime optionExpiry = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(18), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime optionPay = ScheduleCalculator.getAdjustedDate(optionExpiry, SETTLEMENT_DAYS, CALENDAR);
    final ForexOptionVanilla[] forexOption = new ForexOptionVanilla[nbStrike + 1];
    for (int loopstrike = 0; loopstrike <= nbStrike; loopstrike++) {
      strikes[loopstrike] = strikeMin + loopstrike * strikeRange / nbStrike;
      final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, optionPay, notional, strikes[loopstrike]);
      final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, optionExpiry, isCall, isLong);
      forexOption[loopstrike] = forexOptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    }
    final double forward = METHOD_BLACK.forwardForexRate(forexOption[0], SMILE_BUNDLE);
    final double dfDomestic = CURVES.getCurve(CURVES_NAME[1]).getDiscountFactor(forexOption[0].getUnderlyingForex().getPaymentTime()); // USD
    final SmileDeltaParameters smileAtTime = SMILE_BUNDLE.getSmile(EUR, USD, forexOption[0].getTimeToExpiry());
    final double[] strikesVV = smileAtTime.getStrike(forward);
    final double[] volVV = smileAtTime.getVolatility();
    final ForexOptionVanilla[] optReference = new ForexOptionVanilla[3];
    for (int loopvv = 0; loopvv < 3; loopvv++) {
      final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, optionPay, notional, strikesVV[loopvv]);
      final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, optionExpiry, isCall, isLong);
      optReference[loopvv] = forexOptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    }
    final MultipleCurrencyAmount[] ceVV = new MultipleCurrencyAmount[nbStrike + 1];
    final MultipleCurrencyAmount[] ceFlat = new MultipleCurrencyAmount[nbStrike + 1];
    final MultipleCurrencyAmount[] ceExpected = new MultipleCurrencyAmount[nbStrike + 1];
    final MultipleCurrencyAmount[] ceVVATM = new MultipleCurrencyAmount[3];
    final MultipleCurrencyAmount[] ceVVsmile = new MultipleCurrencyAmount[3];
    final MultipleCurrencyAmount[] ceVVadj = new MultipleCurrencyAmount[3];
    for (int loopvv = 0; loopvv < 3; loopvv++) {
      ceVVATM[loopvv] = METHOD_BLACK.currencyExposure(optReference[loopvv], SMILE_BUNDLE_STRIKE_INT_FLAT);
      ceVVsmile[loopvv] = METHOD_BLACK.currencyExposure(optReference[loopvv], SMILE_BUNDLE_STRIKE_INT);
      ceVVadj[loopvv] = ceVVsmile[loopvv].plus(ceVVATM[loopvv].multipliedBy(-1.0));
    }
    for (int loopstrike = 0; loopstrike <= nbStrike; loopstrike++) {
      ceVV[loopstrike] = METHOD_VANNA_VOLGA.currencyExposure(forexOption[loopstrike], SMILE_BUNDLE);
      final double[] weights = METHOD_VANNA_VOLGA.vannaVolgaWeights(forexOption[loopstrike], forward, dfDomestic, strikesVV, volVV);
      ceFlat[loopstrike] = METHOD_BLACK.currencyExposure(forexOption[loopstrike], SMILE_BUNDLE_STRIKE_INT_FLAT);
      ceExpected[loopstrike] = ceFlat[loopstrike];
      for (int loopvv = 0; loopvv < 3; loopvv++) {
        ceExpected[loopstrike] = ceExpected[loopstrike].plus(ceVVadj[loopvv].multipliedBy(weights[loopvv]));
      }
      assertEquals("Forex vanilla option: currency exposure vanna-volga", ceExpected[loopstrike].getAmount(EUR), ceVV[loopstrike].getAmount(EUR), TOLERANCE_PV);
      assertEquals("Forex vanilla option: currency exposure vanna-volga", ceExpected[loopstrike].getAmount(USD), ceVV[loopstrike].getAmount(USD), TOLERANCE_PV);
    }

  }

  @Test
  /**
   * Compare results with the Black results. They should be different but not too much.
   */
  public void comparisonBlack() {

    final int nbStrike = 20;
    final double strikeMin = 1.00;
    final double strikeRange = 0.50;
    final double[] strikes = new double[nbStrike + 1];

    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime optionExpiry = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(18), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime optionPay = ScheduleCalculator.getAdjustedDate(optionExpiry, SETTLEMENT_DAYS, CALENDAR);
    final ForexOptionVanilla[] forexOption = new ForexOptionVanilla[nbStrike + 1];
    for (int loopstrike = 0; loopstrike <= nbStrike; loopstrike++) {
      strikes[loopstrike] = strikeMin + loopstrike * strikeRange / nbStrike;
      final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, optionPay, notional, strikes[loopstrike]);
      final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, optionExpiry, isCall, isLong);
      forexOption[loopstrike] = forexOptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    }
    final double[] pvVV = new double[nbStrike + 1];
    final double[] pvInt = new double[nbStrike + 1];
    for (int loopstrike = 0; loopstrike <= nbStrike; loopstrike++) {
      pvVV[loopstrike] = METHOD_VANNA_VOLGA.presentValue(forexOption[loopstrike], SMILE_BUNDLE).getAmount(USD);
      pvInt[loopstrike] = METHOD_BLACK.presentValue(forexOption[loopstrike], SMILE_BUNDLE_STRIKE_INT).getAmount(USD);
      assertEquals("Forex vanilla option: present value vanna-volga vs Black " + loopstrike, 1, pvVV[loopstrike] / pvInt[loopstrike], 0.15);
    }
    final MultipleCurrencyAmount[] ceVV = new MultipleCurrencyAmount[nbStrike + 1];
    final MultipleCurrencyAmount[] ceInt = new MultipleCurrencyAmount[nbStrike + 1];
    for (int loopstrike = 0; loopstrike <= nbStrike; loopstrike++) {
      ceVV[loopstrike] = METHOD_VANNA_VOLGA.currencyExposure(forexOption[loopstrike], SMILE_BUNDLE);
      ceInt[loopstrike] = METHOD_BLACK.currencyExposure(forexOption[loopstrike], SMILE_BUNDLE_STRIKE_INT);
      assertEquals("Forex vanilla option: currency exposure vanna-volga vs Black " + loopstrike, 1, ceVV[loopstrike].getAmount(EUR) / ceInt[loopstrike].getAmount(EUR), 0.15);
    }

    final MultipleCurrencyInterestRateCurveSensitivity[] pvcsVV = new MultipleCurrencyInterestRateCurveSensitivity[nbStrike + 1];
    final MultipleCurrencyInterestRateCurveSensitivity[] pvcsInt = new MultipleCurrencyInterestRateCurveSensitivity[nbStrike + 1];
    for (int loopstrike = 0; loopstrike <= nbStrike; loopstrike++) {
      pvcsVV[loopstrike] = METHOD_VANNA_VOLGA.presentValueCurveSensitivity(forexOption[loopstrike], SMILE_BUNDLE);
      pvcsInt[loopstrike] = METHOD_BLACK.presentValueCurveSensitivity(forexOption[loopstrike], SMILE_BUNDLE_STRIKE_INT);
      assertEquals("Forex vanilla option: curve sensitivity vanna-volga vs Black " + loopstrike, 1, pvcsVV[loopstrike].getSensitivity(USD).getSensitivities().get(CURVES_NAME[1]).get(0).getSecond()
          / pvcsInt[loopstrike].getSensitivity(USD).getSensitivities().get(CURVES_NAME[1]).get(0).getSecond(), 0.15);
    }
  }

  @Test(enabled = false)
  /**
   * Analyzes the smile implied by the vanna-volga method and compares it to a quadratic interpolation/linear extrapolation.
   * Used to produce the graphs of the documentation.
   */
  public void analysisSmileCall() {

    final int nbStrike = 50;
    final double strikeMin = 1.00;
    final double strikeRange = 0.80;
    final double[] strikes = new double[nbStrike + 1];

    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime optionExpiry = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(18), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime optionPay = ScheduleCalculator.getAdjustedDate(optionExpiry, SETTLEMENT_DAYS, CALENDAR);
    final ForexOptionVanilla[] forexOption = new ForexOptionVanilla[nbStrike + 1];
    for (int loopstrike = 0; loopstrike <= nbStrike; loopstrike++) {
      strikes[loopstrike] = strikeMin + loopstrike * strikeRange / nbStrike;
      final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, optionPay, notional, strikes[loopstrike]);
      final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, optionExpiry, isCall, isLong);
      forexOption[loopstrike] = forexOptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    }
    final double[] pvVV = new double[nbStrike + 1];
    final double[] pvInt = new double[nbStrike + 1];
    final double[] volVV = new double[nbStrike + 1];
    final double[] volInt = new double[nbStrike + 1];
    for (int loopstrike = 0; loopstrike <= nbStrike; loopstrike++) {
      pvVV[loopstrike] = METHOD_VANNA_VOLGA.presentValue(forexOption[loopstrike], SMILE_BUNDLE).getAmount(USD);
      pvInt[loopstrike] = METHOD_BLACK.presentValue(forexOption[loopstrike], SMILE_BUNDLE_STRIKE_INT).getAmount(USD);
      final double forward = METHOD_BLACK.forwardForexRate(forexOption[loopstrike], SMILE_BUNDLE_STRIKE_INT);
      final double df = CURVES.getCurve(CURVES_NAME[1]).getDiscountFactor(forexOption[loopstrike].getUnderlyingForex().getPaymentTime());
      volVV[loopstrike] = BLACK_IMPLIED_VOL.getImpliedVolatility(new BlackFunctionData(forward, df, 0.20), forexOption[loopstrike], pvVV[loopstrike] / notional);
      volInt[loopstrike] = METHOD_BLACK.impliedVolatility(forexOption[loopstrike], SMILE_BUNDLE_STRIKE_INT);
    }
  }

  @Test(enabled = true)
  /**
   * Analyzes the vega for different strikes.
   * Used to produce the graphs of the documentation.
   */
  public void analysisVega() {

    final int nbStrike = 50;
    final double strikeMin = 0.85;
    final double strikeRange = 1.00;
    final double[] strikes = new double[nbStrike + 1];

    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 1000000;
    final ZonedDateTime optionExpiry = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(18), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime optionPay = ScheduleCalculator.getAdjustedDate(optionExpiry, SETTLEMENT_DAYS, CALENDAR);
    final ForexOptionVanilla[] forexOption = new ForexOptionVanilla[nbStrike + 1];
    for (int loopstrike = 0; loopstrike <= nbStrike; loopstrike++) {
      strikes[loopstrike] = strikeMin + loopstrike * strikeRange / nbStrike;
      final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, optionPay, notional, strikes[loopstrike]);
      final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, optionExpiry, isCall, isLong);
      forexOption[loopstrike] = forexOptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    }

    final double forward = METHOD_BLACK.forwardForexRate(forexOption[0], SMILE_BUNDLE_STRIKE_INT);
    final SmileDeltaParameters smileTime = SMILE_TERM.getSmileForTime(forexOption[0].getTimeToExpiry());
    final double[] strikesVV = smileTime.getStrike(forward);

    final PresentValueForexBlackVolatilitySensitivity[] vegaObject = new PresentValueForexBlackVolatilitySensitivity[nbStrike + 1];
    final double[][] vegaVV = new double[3][nbStrike + 1];
    final double[] vegaBlack = new double[nbStrike + 1];
    for (int loopstrike = 0; loopstrike <= nbStrike; loopstrike++) {
      vegaObject[loopstrike] = METHOD_VANNA_VOLGA.presentValueBlackVolatilitySensitivity(forexOption[loopstrike], SMILE_BUNDLE);
      for (int loopvv = 0; loopvv < 3; loopvv++) {
        final DoublesPair point = DoublesPair.of(forexOption[loopstrike].getTimeToExpiry(), strikesVV[loopvv]);
        vegaVV[loopvv][loopstrike] = vegaObject[loopstrike].getVega().getMap().get(point);
      }
      vegaBlack[loopstrike] = METHOD_BLACK.presentValueBlackVolatilitySensitivity(forexOption[loopstrike], SMILE_BUNDLE_STRIKE_INT).toSingleValue().getAmount();
    }
  }

  @Test(enabled = true)
  /**
   * Analyzes the price implied by the vanna-volga method and compares it to the market prices at the market data points.
   */
  public void analysisAtData() {

    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime optionExpiry = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(18), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime optionPay = ScheduleCalculator.getAdjustedDate(optionExpiry, SETTLEMENT_DAYS, CALENDAR);
    final ForexDefinition forexUnderlyingSpotDefinition = new ForexDefinition(EUR, USD, optionPay, notional, SPOT);
    final ForexOptionVanillaDefinition forexOptionSpotDefinition = new ForexOptionVanillaDefinition(forexUnderlyingSpotDefinition, optionExpiry, isCall, isLong);
    final ForexOptionVanilla forexOptionSpot = forexOptionSpotDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final double forward = METHOD_BLACK.forwardForexRate(forexOptionSpot, SMILE_BUNDLE_STRIKE_INT);

    final SmileDeltaParameters smileTime = SMILE_TERM.getSmileForTime(forexOptionSpot.getTimeToExpiry());

    final double[] strikes = smileTime.getStrike(forward);
    final int nbStrike = strikes.length;
    final ForexOptionVanilla[] forexOption = new ForexOptionVanilla[nbStrike];
    for (int loopstrike = 0; loopstrike < nbStrike; loopstrike++) {
      final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, optionPay, notional, strikes[loopstrike]);
      final ForexOptionVanillaDefinition forexOptionDefinition = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, optionExpiry, isCall, isLong);
      forexOption[loopstrike] = forexOptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    }
    final double[] pvVV = new double[nbStrike];
    final double[] pvInt = new double[nbStrike];
    final double[] volVV = new double[nbStrike];
    final double[] volInt = new double[nbStrike];
    for (int loopstrike = 0; loopstrike < nbStrike; loopstrike++) {
      pvVV[loopstrike] = METHOD_VANNA_VOLGA.presentValue(forexOption[loopstrike], SMILE_BUNDLE).getAmount(USD);
      pvInt[loopstrike] = METHOD_BLACK.presentValue(forexOption[loopstrike], SMILE_BUNDLE_STRIKE_INT).getAmount(USD);
      final double df = CURVES.getCurve(CURVES_NAME[1]).getDiscountFactor(forexOption[loopstrike].getUnderlyingForex().getPaymentTime()); // USD discounting
      volVV[loopstrike] = BLACK_IMPLIED_VOL.getImpliedVolatility(new BlackFunctionData(forward, df, 0.20), forexOption[loopstrike], pvVV[loopstrike] / notional);
      volInt[loopstrike] = METHOD_BLACK.impliedVolatility(forexOption[loopstrike], SMILE_BUNDLE_STRIKE_INT);
    }
  }

  @Test(enabled = false)
  /**
   * Analyzes the performance of the vanna-volga method.
   */
  public void performance() {
    long startTime, endTime;
    final int nbTest = 1000;

    final int nbStrike = 50;
    final double strikeMin = 1.00;
    final double strikeRange = 0.80;
    final double[] strikes = new double[nbStrike + 1];
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime optionExpiry = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(18), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime optionPay = ScheduleCalculator.getAdjustedDate(optionExpiry, SETTLEMENT_DAYS, CALENDAR);
    final ForexOptionVanilla[] forexOption = new ForexOptionVanilla[nbStrike + 1];
    final ForexOptionVanillaDefinition[] forexOptionDefinition = new ForexOptionVanillaDefinition[nbStrike + 1];
    for (int loopstrike = 0; loopstrike <= nbStrike; loopstrike++) {
      strikes[loopstrike] = strikeMin + loopstrike * strikeRange / nbStrike;
      final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, optionPay, notional, strikes[loopstrike]);
      forexOptionDefinition[loopstrike] = new ForexOptionVanillaDefinition(forexUnderlyingDefinition, optionExpiry, isCall, isLong);
    }
    final double[] pvVV = new double[nbStrike + 1];
    final double[] pvInt = new double[nbStrike + 1];
    final MultipleCurrencyAmount[] ceVV = new MultipleCurrencyAmount[nbStrike + 1];
    final MultipleCurrencyAmount[] ceInt = new MultipleCurrencyAmount[nbStrike + 1];
    final PresentValueForexBlackVolatilitySensitivity[] pvbsVV = new PresentValueForexBlackVolatilitySensitivity[nbStrike + 1];
    final PresentValueForexBlackVolatilitySensitivity[] pvbsInt = new PresentValueForexBlackVolatilitySensitivity[nbStrike + 1];
    final MultipleCurrencyInterestRateCurveSensitivity[] pvcsVV = new MultipleCurrencyInterestRateCurveSensitivity[nbStrike + 1];
    final MultipleCurrencyInterestRateCurveSensitivity[] pvcsInt = new MultipleCurrencyInterestRateCurveSensitivity[nbStrike + 1];

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      for (int loopstrike = 0; loopstrike <= nbStrike; loopstrike++) {
        forexOption[loopstrike] = forexOptionDefinition[loopstrike].toDerivative(REFERENCE_DATE, CURVES_NAME);
        pvInt[loopstrike] = METHOD_BLACK.presentValue(forexOption[loopstrike], SMILE_BUNDLE_STRIKE_INT).getAmount(USD);
        ceInt[loopstrike] = METHOD_BLACK.currencyExposure(forexOption[loopstrike], SMILE_BUNDLE_STRIKE_INT);
        pvbsInt[loopstrike] = METHOD_BLACK.presentValueBlackVolatilitySensitivity(forexOption[loopstrike], SMILE_BUNDLE_STRIKE_INT);
        pvcsInt[loopstrike] = METHOD_BLACK.presentValueCurveSensitivity(forexOption[loopstrike], SMILE_BUNDLE_STRIKE_INT);
      }
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " x " + (nbStrike + 1) + " vanilla forex options with Black: " + (endTime - startTime) + " ms");
    // Performance note: conversion + price Black: 20-Jun-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 115 ms for 1000x51 options.
    // Performance note: conversion + price Black + currency exposure: 21-Jun-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 200 ms for 1000x51 options.
    // Performance note: conversion + price Black + currency exposure + vol sensitivity: 24-Jun-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 325 ms for 1000x51 options.

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      for (int loopstrike = 0; loopstrike <= nbStrike; loopstrike++) {
        forexOption[loopstrike] = forexOptionDefinition[loopstrike].toDerivative(REFERENCE_DATE, CURVES_NAME);
        pvVV[loopstrike] = METHOD_VANNA_VOLGA.presentValue(forexOption[loopstrike], SMILE_BUNDLE).getAmount(USD);
        ceVV[loopstrike] = METHOD_VANNA_VOLGA.currencyExposure(forexOption[loopstrike], SMILE_BUNDLE);
        pvbsVV[loopstrike] = METHOD_VANNA_VOLGA.presentValueBlackVolatilitySensitivity(forexOption[loopstrike], SMILE_BUNDLE);
        pvcsVV[loopstrike] = METHOD_VANNA_VOLGA.presentValueCurveSensitivity(forexOption[loopstrike], SMILE_BUNDLE);
      }
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " x " + (nbStrike + 1) + " vanilla forex options with Vanna-Volga: " + (endTime - startTime) + " ms");
    // Performance note: conversion + price Vanna-Volga: 20-Jun-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 160 ms for 1000x51 options.
    // Performance note: conversion + price Vanna-Volga + currency exposure: 21-Jun-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 310 ms for 1000x51 options.
    // Performance note: conversion + price Vanna-Volga + currency exposure + vol sensitivity: 24-Jun-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 500 ms for 1000x51 options.

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      for (int loopstrike = 0; loopstrike <= nbStrike; loopstrike++) {
        forexOption[loopstrike] = forexOptionDefinition[loopstrike].toDerivative(REFERENCE_DATE, CURVES_NAME);
        pvInt[loopstrike] = METHOD_BLACK.presentValue(forexOption[loopstrike], SMILE_BUNDLE_STRIKE_INT).getAmount(USD);
        ceInt[loopstrike] = METHOD_BLACK.currencyExposure(forexOption[loopstrike], SMILE_BUNDLE_STRIKE_INT);
        pvbsInt[loopstrike] = METHOD_BLACK.presentValueBlackVolatilitySensitivity(forexOption[loopstrike], SMILE_BUNDLE_STRIKE_INT);
        pvcsInt[loopstrike] = METHOD_BLACK.presentValueCurveSensitivity(forexOption[loopstrike], SMILE_BUNDLE_STRIKE_INT);
      }
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " x " + (nbStrike + 1) + " vanilla forex options with Black: " + (endTime - startTime) + " ms");
    // Performance note: conversion + price Black: 20-Jun-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 115 ms for 1000x51 options.
    // Performance note: conversion + price Black + currency exposure: 21-Jun-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 200 ms for 1000x51 options.
    // Performance note: conversion + price Black + currency exposure + vol sensitivity: 24-Jun-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 325 ms for 1000x51 options.

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      for (int loopstrike = 0; loopstrike <= nbStrike; loopstrike++) {
        forexOption[loopstrike] = forexOptionDefinition[loopstrike].toDerivative(REFERENCE_DATE, CURVES_NAME);
        pvVV[loopstrike] = METHOD_VANNA_VOLGA.presentValue(forexOption[loopstrike], SMILE_BUNDLE).getAmount(USD);
        ceVV[loopstrike] = METHOD_VANNA_VOLGA.currencyExposure(forexOption[loopstrike], SMILE_BUNDLE);
        pvbsVV[loopstrike] = METHOD_VANNA_VOLGA.presentValueBlackVolatilitySensitivity(forexOption[loopstrike], SMILE_BUNDLE);
        pvcsVV[loopstrike] = METHOD_VANNA_VOLGA.presentValueCurveSensitivity(forexOption[loopstrike], SMILE_BUNDLE);
      }
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " x " + (nbStrike + 1) + " vanilla forex options with Vanna-Volga: " + (endTime - startTime) + " ms");
    // Performance note: conversion + price Vanna-Volga: 20-Jun-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 160 ms for 1000x51 options.
    // Performance note: conversion + price Vanna-Volga + currency exposure: 21-Jun-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 310 ms for 1000x51 options.
    // Performance note: conversion + price Vanna-Volga + currency exposure + vol sensitivity: 24-Jun-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 500 ms for 1000x51 options.

  }

}
