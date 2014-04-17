/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.method;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedCompoundedONCompoundedDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedCompoundedONCompoundedDefinition;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.PresentValueBlackCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueBlackSwaptionSensitivityBlackCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityBlackCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsBlack;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedAccruedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONCompounded;
import com.opengamma.analytics.financial.interestrate.sensitivity.PresentValueBlackSwaptionSensitivity;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.method.SwapFixedCompoundingONCompoundingDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.analytics.financial.model.option.parameters.BlackFlatSwaptionParameters;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SwaptionPhysicalFixedCompoundedONCompoundedBlackMethodTest {

  // Data
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2013, 9, 25);

  private static final YieldCurveBundle CURVES = TestsDataSetsBlack.createCurvesBRL();
  private static final BlackFlatSwaptionParameters BLACK = TestsDataSetsBlack.createBlackSwaptionBRL();
  private static final YieldCurveWithBlackSwaptionBundle CURVES_BLACK = new YieldCurveWithBlackSwaptionBundle(BLACK, CURVES);
  private static final String[] CURVES_NAME = TestsDataSetsBlack.curvesBRLNames();
  private static final Calendar CALENDAR = ((GeneratorSwapFixedCompoundedONCompounded) BLACK.getGeneratorSwap()).getOvernightCalendar();

  private static final GeneratorSwapFixedCompoundedONCompounded GENERATOR_OIS_BRL = (GeneratorSwapFixedCompoundedONCompounded) BLACK.getGeneratorSwap();

  private static final Period EXPIRY_TENOR = Period.ofMonths(26); // To be between nodes.
  private static final ZonedDateTime EXPIRY_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, EXPIRY_TENOR, GENERATOR_OIS_BRL.getBusinessDayConvention(), CALENDAR,
      GENERATOR_OIS_BRL.isEndOfMonth());
  private static final ZonedDateTime SETTLE_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, GENERATOR_OIS_BRL.getSpotLag(), CALENDAR);
  private static final int SWAP_TENOR_YEAR = 2;
  private static final Period SWAP_TENOR = Period.ofYears(SWAP_TENOR_YEAR);
  private static final double NOTIONAL = 123456789.0;
  private static final double RATE = 0.02;
  private static final SwapFixedCompoundedONCompoundedDefinition SWAP_DEFINITION_REC = SwapFixedCompoundedONCompoundedDefinition
      .from(SETTLE_DATE, SWAP_TENOR, NOTIONAL, GENERATOR_OIS_BRL, RATE, false);
  private static final boolean IS_LONG = false;
  private static final SwaptionPhysicalFixedCompoundedONCompoundedDefinition SWAPTION_DEFINITION_LONG_REC =
      SwaptionPhysicalFixedCompoundedONCompoundedDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_REC, false, IS_LONG);
  private static final SwaptionPhysicalFixedCompoundedONCompounded SWAPTION_LONG_REC = SWAPTION_DEFINITION_LONG_REC.toDerivative(REFERENCE_DATE, CURVES_NAME[0], CURVES_NAME[0]);

  private static final SwaptionPhysicalFixedCompoundedONCompoundedBlackMethod METHOD_BLACK = SwaptionPhysicalFixedCompoundedONCompoundedBlackMethod.getInstance();
  private static final SwapFixedCompoundingONCompoundingDiscountingMethod METHOD_SWAP = SwapFixedCompoundingONCompoundingDiscountingMethod.getInstance();
  private static final PresentValueBlackCalculator PVBC = PresentValueBlackCalculator.getInstance();
  private static final PresentValueCurveSensitivityBlackCalculator PVCSBC = PresentValueCurveSensitivityBlackCalculator.getInstance();
  private static final PresentValueBlackSwaptionSensitivityBlackCalculator PVBSBC = PresentValueBlackSwaptionSensitivityBlackCalculator.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_DELTA = 3.0E+2;

  @Test
  public void presentValue() {
    final Swap<CouponFixedAccruedCompounding, CouponONCompounded> swap = SWAPTION_LONG_REC.getUnderlyingSwap();
    final CouponFixedAccruedCompounding cpn0 = swap.getFirstLeg().getNthPayment(0);
    final double forwardModified = METHOD_SWAP.forwardModified(swap, CURVES);
    final double strikeModified = Math.pow(1.0 + RATE, cpn0.getPaymentYearFraction()) - 1.0d;
    final double num = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(cpn0.getPaymentTime()) * NOTIONAL;
    final double expiry = SWAPTION_LONG_REC.getTimeToExpiry();
    final double vol = BLACK.getVolatility(expiry, SWAPTION_LONG_REC.getMaturityTime());
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strikeModified, expiry, SWAPTION_LONG_REC.isCall());
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final BlackFunctionData dataBlack = new BlackFunctionData(forwardModified, num, vol);
    final Function1D<BlackFunctionData, Double> func = blackFunction.getPriceFunction(option);
    final double pvExpected = -func.evaluate(dataBlack); // Short
    final CurrencyAmount pvComputed = METHOD_BLACK.presentValue(SWAPTION_LONG_REC, CURVES_BLACK);
    assertEquals("SwaptionPhysicalFixedCompoundedONCompoundedBlackMethod: forward", pvExpected, pvComputed.getAmount(), TOLERANCE_PV);
  }

  @Test
  public void presentValueMethodVsCalculator() {
    final CurrencyAmount pvMethod = METHOD_BLACK.presentValue(SWAPTION_LONG_REC, CURVES_BLACK);
    final double pvCalculator = SWAPTION_LONG_REC.accept(PVBC, CURVES_BLACK);
    assertEquals("SwaptionPhysicalFixedCompoundedONCompoundedBlackMethod: forward", pvMethod.getAmount(), pvCalculator, TOLERANCE_PV);
  }

//  @Test
//  public void presentValueCurveSensitivity() {
//    final InterestRateCurveSensitivity pvcsSwaption = METHOD_BLACK.presentValueCurveSensitivity(SWAPTION_LONG_REC, CURVES_BLACK);
//    // 1. Discounting curve sensitivity
//    final DoubleAVLTreeSet discTime = new DoubleAVLTreeSet();
//    final CouponONCompounded cpnON = SWAPTION_LONG_REC.getUnderlyingSwap().getSecondLeg().getNthPayment(0);
//    discTime.add(cpnON.getFixingPeriodStartTimes()[0]);
//    for (int loopp = 0; loopp < cpnON.getFixingPeriodStartTimes().length; loopp++) {
//      discTime.add(cpnON.getFixingPeriodEndTimes()[loopp]);
//    }
//    final CouponFixedAccruedCompounding cpnF = SWAPTION_LONG_REC.getUnderlyingSwap().getFirstLeg().getNthPayment(0);
//    discTime.add(cpnF.getPaymentTime());
//    final double[] nodeTimesDisc = discTime.toDoubleArray();
//    final List<DoublesPair> sensiPvDisc = pvcsSwaption.getSensitivities().get(CURVES_NAME[0]);
//    final List<DoublesPair> fdSense = FDCurveSensitivityCalculator.curveSensitvityFDCalculator(SWAPTION_LONG_REC, METHOD_BLACK, CURVES_BLACK, CURVES_NAME[0], nodeTimesDisc, 0.0);
//    assertSensitivityEquals(sensiPvDisc, fdSense, TOLERANCE_DELTA * NOTIONAL);
//  }

  @Test
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final InterestRateCurveSensitivity pvcsMethod = METHOD_BLACK.presentValueCurveSensitivity(SWAPTION_LONG_REC, CURVES_BLACK);
    final InterestRateCurveSensitivity pvcsCalculator = new InterestRateCurveSensitivity(SWAPTION_LONG_REC.accept(PVCSBC, CURVES_BLACK));
    AssertSensivityObjects.assertEquals("SwaptionPhysicalFixedCompoundedONCompoundedBlackMethod: presentValueCurveSensitivity", pvcsMethod, pvcsCalculator, TOLERANCE_DELTA);
  }

  @Test
  public void presentValueBlackSensitivity() {
    final double shift = 1.0E-6;
    final PresentValueBlackSwaptionSensitivity pvbvs = METHOD_BLACK.presentValueBlackSensitivity(SWAPTION_LONG_REC, CURVES_BLACK);
    final BlackFlatSwaptionParameters BlackP = TestsDataSetsBlack.createBlackSwaptionBRLShift(shift);
    final YieldCurveWithBlackSwaptionBundle curvesBlackP = new YieldCurveWithBlackSwaptionBundle(BlackP, CURVES);
    final CurrencyAmount pvP = METHOD_BLACK.presentValue(SWAPTION_LONG_REC, curvesBlackP);
    final BlackFlatSwaptionParameters BlackM = TestsDataSetsBlack.createBlackSwaptionBRLShift(-shift);
    final YieldCurveWithBlackSwaptionBundle curvesBlackM = new YieldCurveWithBlackSwaptionBundle(BlackM, CURVES);
    final CurrencyAmount pvM = METHOD_BLACK.presentValue(SWAPTION_LONG_REC, curvesBlackM);
    final DoublesPair point = DoublesPair.of(SWAPTION_LONG_REC.getTimeToExpiry(), SWAPTION_LONG_REC.getMaturityTime());
    assertEquals("Swaption Black method: present value volatility sensitivity", (pvP.getAmount() - pvM.getAmount()) / (2 * shift), pvbvs.getSensitivity().getMap().get(point), TOLERANCE_DELTA);
  }

  @Test
  public void presentValueBlackSensitivityMethodVsCalculator() {
    final PresentValueBlackSwaptionSensitivity pvbsMethod = METHOD_BLACK.presentValueBlackSensitivity(SWAPTION_LONG_REC, CURVES_BLACK);
    final PresentValueBlackSwaptionSensitivity pvbsCalculator = SWAPTION_LONG_REC.accept(PVBSBC, CURVES_BLACK);
    assertEquals("SwaptionPhysicalFixedCompoundedONCompoundedBlackMethod: presentValueBlackSensitivity", pvbsMethod, pvbsCalculator);

  }

}
