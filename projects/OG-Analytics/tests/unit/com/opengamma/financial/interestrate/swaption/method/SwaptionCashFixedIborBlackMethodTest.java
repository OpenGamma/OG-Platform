/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swaption.method;

import static com.opengamma.financial.interestrate.TestUtils.assertSensitivityEquals;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import it.unimi.dsi.fastutil.doubles.DoubleAVLTreeSet;

import java.util.List;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.instrument.index.GeneratorSwap;
import com.opengamma.financial.instrument.index.generator.EUR1YEURIBOR6M;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.financial.interestrate.BlackSwaptionSensitivityNodeCalculator;
import com.opengamma.financial.interestrate.FDCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.PresentValueBlackSwaptionCalculator;
import com.opengamma.financial.interestrate.PresentValueBlackSwaptionSensitivity;
import com.opengamma.financial.interestrate.PresentValueBlackSwaptionSensitivityBlackSwaptionCalculator;
import com.opengamma.financial.interestrate.PresentValueCurveSensitivityBlackSwaptionCalculator;
import com.opengamma.financial.interestrate.TestsDataSetsBlack;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.swap.SwapFixedDiscountingMethod;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.financial.model.option.definition.BlackSwaptionParameters;
import com.opengamma.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

public class SwaptionCashFixedIborBlackMethodTest {
  // Data
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 1, 10);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("TARGET");
  private static final GeneratorSwap GENERATOR_EUR1YEURIBOR6M = new EUR1YEURIBOR6M(CALENDAR);
  private static final YieldCurveBundle CURVES = TestsDataSetsBlack.createCurvesEUR();
  private static final BlackSwaptionParameters BLACK = TestsDataSetsBlack.createBlackSwaptionEUR6();
  private static final YieldCurveWithBlackSwaptionBundle CURVES_BLACK = new YieldCurveWithBlackSwaptionBundle(BLACK, CURVES);
  private static final String[] CURVES_NAME = TestsDataSetsBlack.curvesEURNames();
  // Swaption
  private static final Period EXPIRY_TENOR = Period.ofMonths(26); // To be between nodes.
  private static final ZonedDateTime EXPIRY_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, EXPIRY_TENOR, GENERATOR_EUR1YEURIBOR6M.getBusinessDayConvention(), CALENDAR,
      GENERATOR_EUR1YEURIBOR6M.isEndOfMonth());
  private static final ZonedDateTime SETTLE_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, GENERATOR_EUR1YEURIBOR6M.getSpotLag(), CALENDAR);
  private static final int SWAP_TENOR_YEAR = 5;
  private static final Period SWAP_TENOR = Period.ofYears(SWAP_TENOR_YEAR);
  private static final double NOTIONAL = 123456789.0;
  private static final double RATE = 0.02;
  private static final SwapFixedIborDefinition SWAP_DEFINITION_REC = SwapFixedIborDefinition.from(SETTLE_DATE, SWAP_TENOR, GENERATOR_EUR1YEURIBOR6M, NOTIONAL, RATE, false);
  private static final SwaptionCashFixedIborDefinition SWAPTION_DEFINITION_LONG_REC = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_REC, true);
  private static final SwaptionCashFixedIbor SWAPTION_LONG_REC = SWAPTION_DEFINITION_LONG_REC.toDerivative(REFERENCE_DATE, new String[] {CURVES_NAME[0], CURVES_NAME[2]});
  // Method - calculator
  private static final double TOLERANCE_PRICE = 1.0E-2;
  private static final double TOLERANCE_DELTA = 1.0E+2;
  //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move.
  private static final SwaptionCashFixedIborBlackMethod METHOD_BLACK = SwaptionCashFixedIborBlackMethod.getInstance();
  private static final PresentValueBlackSwaptionCalculator PVC_BLACK = PresentValueBlackSwaptionCalculator.getInstance();
  private static final PresentValueCurveSensitivityBlackSwaptionCalculator PVCSC_BLACK = PresentValueCurveSensitivityBlackSwaptionCalculator.getInstance();
  private static final PresentValueBlackSwaptionSensitivityBlackSwaptionCalculator PVBSC_BLACK = PresentValueBlackSwaptionSensitivityBlackSwaptionCalculator.getInstance();
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  private static final BlackSwaptionSensitivityNodeCalculator BSSNC = new BlackSwaptionSensitivityNodeCalculator();

  @Test
  public void presentValue() {
    CurrencyAmount pvMethod = METHOD_BLACK.presentValue(SWAPTION_LONG_REC, CURVES_BLACK);
    double forward = PRC.visit(SWAPTION_LONG_REC.getUnderlyingSwap(), CURVES);
    double pvbp = SwapFixedDiscountingMethod.getAnnuityCash(SWAPTION_LONG_REC.getUnderlyingSwap(), forward);
    double discountFactorSettle = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(SWAPTION_LONG_REC.getSettlementTime());
    double volatility = BLACK.getVolatility(SWAPTION_LONG_REC.getTimeToExpiry(), SWAPTION_LONG_REC.getMaturityTime());
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, pvbp * discountFactorSettle, volatility);
    final Function1D<BlackFunctionData, Double> func = blackFunction.getPriceFunction(SWAPTION_LONG_REC);
    final double pvExpected = func.evaluate(dataBlack);
    assertEquals("Swaption Black method: present value", SWAPTION_LONG_REC.getCurrency(), pvMethod.getCurrency());
    assertEquals("Swaption Black method: present value", pvExpected, pvMethod.getAmount(), TOLERANCE_PRICE);
  }

  @Test
  /**
   * Compare the method figures to the Calculator figures.
   */
  public void presentValueMethodVsCalculator() {
    CurrencyAmount pvMethod = METHOD_BLACK.presentValue(SWAPTION_LONG_REC, CURVES_BLACK);
    double pvCalculator = PVC_BLACK.visit(SWAPTION_LONG_REC, CURVES_BLACK);
    assertEquals("Swaption Black method: present value", pvCalculator, pvMethod.getAmount(), TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests the curve sensitivity for the explicit formula.
   */
  public void presentValueCurveSensitivity() {
    InterestRateCurveSensitivity pvcsSwaption = METHOD_BLACK.presentValueCurveSensitivity(SWAPTION_LONG_REC, CURVES_BLACK);
    pvcsSwaption = pvcsSwaption.clean();
    // 1. Forward curve sensitivity
    DoubleAVLTreeSet forwardTime = new DoubleAVLTreeSet();
    for (int loopcpn = 0; loopcpn < SWAPTION_LONG_REC.getUnderlyingSwap().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      CouponIbor cpn = (CouponIbor) SWAPTION_LONG_REC.getUnderlyingSwap().getSecondLeg().getNthPayment(loopcpn);
      forwardTime.add(cpn.getFixingPeriodStartTime());
      forwardTime.add(cpn.getFixingPeriodEndTime());
    }
    double[] nodeTimesForward = forwardTime.toDoubleArray();
    final List<DoublesPair> sensiPvFwd = pvcsSwaption.getSensitivities().get(CURVES_NAME[2]);
    List<DoublesPair> fdSenseFwd = FDCurveSensitivityCalculator.curveSensitvityFDCalculator(SWAPTION_LONG_REC, METHOD_BLACK, CURVES_BLACK, CURVES_NAME[2], nodeTimesForward, TOLERANCE_DELTA);
    assertSensitivityEquals(sensiPvFwd, fdSenseFwd, TOLERANCE_DELTA);
    // 2. Discounting curve sensitivity
    DoubleAVLTreeSet discTime = new DoubleAVLTreeSet();
    discTime.add(SWAPTION_LONG_REC.getSettlementTime());
    for (int loopcpn = 0; loopcpn < SWAPTION_LONG_REC.getUnderlyingSwap().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      CouponIbor cpn = (CouponIbor) SWAPTION_LONG_REC.getUnderlyingSwap().getSecondLeg().getNthPayment(loopcpn);
      discTime.add(cpn.getPaymentTime());
    }
    double[] nodeTimesDisc = discTime.toDoubleArray();
    final List<DoublesPair> sensiPvDisc = pvcsSwaption.getSensitivities().get(CURVES_NAME[0]);
    List<DoublesPair> fdSenseDsc = FDCurveSensitivityCalculator.curveSensitvityFDCalculator(SWAPTION_LONG_REC, METHOD_BLACK, CURVES_BLACK, CURVES_NAME[0], nodeTimesDisc, TOLERANCE_DELTA);
    assertSensitivityEquals(sensiPvDisc, fdSenseDsc, TOLERANCE_DELTA);
  }

  @Test
  /**
   * Compare the method figures to the Calculator figures.
   */
  public void presentValueCurveSensitivityMethodVsCalculator() {
    InterestRateCurveSensitivity pvcsMethod = METHOD_BLACK.presentValueCurveSensitivity(SWAPTION_LONG_REC, CURVES_BLACK);
    InterestRateCurveSensitivity pvcsCalculator = new InterestRateCurveSensitivity(PVCSC_BLACK.visit(SWAPTION_LONG_REC, CURVES_BLACK));
    assertTrue("Swaption Black method: present value", InterestRateCurveSensitivity.compare(pvcsMethod, pvcsCalculator, TOLERANCE_DELTA));
  }

  @Test
  /**
   * Tests the Black volatility sensitivity (vega).
   */
  public void presentValueBlackSensitivity() {
    double shift = 1.0E-6;
    PresentValueBlackSwaptionSensitivity pvbvs = METHOD_BLACK.presentValueBlackSensitivity(SWAPTION_LONG_REC, CURVES_BLACK);
    BlackSwaptionParameters BlackP = TestsDataSetsBlack.createBlackSwaptionEUR6Shift(shift);
    YieldCurveWithBlackSwaptionBundle curvesBlackP = new YieldCurveWithBlackSwaptionBundle(BlackP, CURVES);
    CurrencyAmount pvP = METHOD_BLACK.presentValue(SWAPTION_LONG_REC, curvesBlackP);
    BlackSwaptionParameters BlackM = TestsDataSetsBlack.createBlackSwaptionEUR6Shift(-shift);
    YieldCurveWithBlackSwaptionBundle curvesBlackM = new YieldCurveWithBlackSwaptionBundle(BlackM, CURVES);
    CurrencyAmount pvM = METHOD_BLACK.presentValue(SWAPTION_LONG_REC, curvesBlackM);
    DoublesPair point = new DoublesPair(SWAPTION_LONG_REC.getTimeToExpiry(), SWAPTION_LONG_REC.getMaturityTime());
    assertEquals("Swaption Black method: present value volatility sensitivity", (pvP.getAmount() - pvM.getAmount()) / (2 * shift), pvbvs.getSensitivity().getMap().get(point), TOLERANCE_DELTA);
  }

  @Test
  /**
   * Tests the Black volatility sensitivity (vega).
   */
  public void presentValueBlackSensitivityMethodVsCalculator() {
    PresentValueBlackSwaptionSensitivity pvbsMethod = METHOD_BLACK.presentValueBlackSensitivity(SWAPTION_LONG_REC, CURVES_BLACK);
    PresentValueBlackSwaptionSensitivity pvbsCalculator = PVBSC_BLACK.visit(SWAPTION_LONG_REC, CURVES_BLACK);
    assertEquals("Swaption Black method: present value", pvbsMethod, pvbsCalculator);
  }

  @Test
  /**
   * Tests the Black volatility sensitivity (vega).
   */
  public void presentValueBlackNodeSensitivity() {
    double shift = 1.0E-6;
    PresentValueBlackSwaptionSensitivity pvbvs = METHOD_BLACK.presentValueBlackSensitivity(SWAPTION_LONG_REC, CURVES_BLACK);
    PresentValueBlackSwaptionSensitivity pvbns = BSSNC.calculateNodeSensitivities(pvbvs, BLACK);
    double[] x = BLACK.getVolatilitySurface().getXDataAsPrimitive();
    double[] y = BLACK.getVolatilitySurface().getYDataAsPrimitive();
    for (int loopindex = 0; loopindex < x.length; loopindex++) {
      BlackSwaptionParameters BlackP = TestsDataSetsBlack.createBlackSwaptionEUR6Shift(loopindex, shift);
      YieldCurveWithBlackSwaptionBundle curvesBlackP = new YieldCurveWithBlackSwaptionBundle(BlackP, CURVES);
      CurrencyAmount pvP = METHOD_BLACK.presentValue(SWAPTION_LONG_REC, curvesBlackP);
      BlackSwaptionParameters BlackM = TestsDataSetsBlack.createBlackSwaptionEUR6Shift(loopindex, -shift);
      YieldCurveWithBlackSwaptionBundle curvesBlackM = new YieldCurveWithBlackSwaptionBundle(BlackM, CURVES);
      CurrencyAmount pvM = METHOD_BLACK.presentValue(SWAPTION_LONG_REC, curvesBlackM);
      assertEquals("Swaption Black method: present value volatility sensitivity", (pvP.getAmount() - pvM.getAmount()) / (2 * shift),
          pvbns.getSensitivity().getMap().get(new DoublesPair(x[loopindex], y[loopindex])), TOLERANCE_DELTA);
    }
  }

}
