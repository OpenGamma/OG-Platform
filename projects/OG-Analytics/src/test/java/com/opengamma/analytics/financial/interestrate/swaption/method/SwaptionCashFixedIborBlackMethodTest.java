/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.method;

import static com.opengamma.analytics.financial.interestrate.TestUtils.assertSensitivityEquals;
import static org.testng.AssertJUnit.assertEquals;
import it.unimi.dsi.fastutil.doubles.DoubleAVLTreeSet;

import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.BlackSwaptionSensitivityNodeCalculator;
import com.opengamma.analytics.financial.interestrate.FDCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueBlackCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueBlackSwaptionSensitivityBlackCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityBlackCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsBlack;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.sensitivity.PresentValueBlackSwaptionSensitivity;
import com.opengamma.analytics.financial.interestrate.swap.method.SwapFixedCouponDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.analytics.financial.model.option.parameters.BlackFlatSwaptionParameters;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class SwaptionCashFixedIborBlackMethodTest {
  // Data
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 1, 10);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("TARGET");
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor GENERATOR_EUR1YEURIBOR6M = GENERATOR_SWAP_MASTER.getGenerator("EUR1YEURIBOR6M", CALENDAR);
  private static final YieldCurveBundle CURVES = TestsDataSetsBlack.createCurvesEUR();
  private static final BlackFlatSwaptionParameters BLACK = TestsDataSetsBlack.createBlackSwaptionEUR6();
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
  private static final SwaptionCashFixedIborDefinition SWAPTION_DEFINITION_LONG_REC = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_REC, false, true);
  private static final SwaptionCashFixedIbor SWAPTION_LONG_REC = SWAPTION_DEFINITION_LONG_REC.toDerivative(REFERENCE_DATE, new String[] {CURVES_NAME[0], CURVES_NAME[2] });
  // Method - calculator
  private static final double TOLERANCE_PRICE = 1.0E-2;
  private static final double TOLERANCE_DELTA = 1.0E+2;
  //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move.
  private static final SwaptionCashFixedIborBlackMethod METHOD_BLACK = SwaptionCashFixedIborBlackMethod.getInstance();
  private static final PresentValueBlackCalculator PVC_BLACK = PresentValueBlackCalculator.getInstance();
  private static final PresentValueCurveSensitivityBlackCalculator PVCSC_BLACK = PresentValueCurveSensitivityBlackCalculator.getInstance();
  private static final PresentValueBlackSwaptionSensitivityBlackCalculator PVBSC_BLACK = PresentValueBlackSwaptionSensitivityBlackCalculator.getInstance();
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  private static final BlackSwaptionSensitivityNodeCalculator BSSNC = new BlackSwaptionSensitivityNodeCalculator();
  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();

  @Test
  public void presentValue() {
    final CurrencyAmount pvMethod = METHOD_BLACK.presentValue(SWAPTION_LONG_REC, CURVES_BLACK);
    final double forward = SWAPTION_LONG_REC.getUnderlyingSwap().accept(PRC, CURVES);
    final double pvbp = METHOD_SWAP.getAnnuityCash(SWAPTION_LONG_REC.getUnderlyingSwap(), forward);
    final double discountFactorSettle = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(SWAPTION_LONG_REC.getSettlementTime());
    final double volatility = BLACK.getVolatility(SWAPTION_LONG_REC.getTimeToExpiry(), SWAPTION_LONG_REC.getMaturityTime());
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
    final CurrencyAmount pvMethod = METHOD_BLACK.presentValue(SWAPTION_LONG_REC, CURVES_BLACK);
    final double pvCalculator = SWAPTION_LONG_REC.accept(PVC_BLACK, CURVES_BLACK);
    assertEquals("Swaption Black method: present value", pvCalculator, pvMethod.getAmount(), TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests the curve sensitivity for the explicit formula.
   */
  public void presentValueCurveSensitivity() {
    InterestRateCurveSensitivity pvcsSwaption = METHOD_BLACK.presentValueCurveSensitivity(SWAPTION_LONG_REC, CURVES_BLACK);
    pvcsSwaption = pvcsSwaption.cleaned();
    // 1. Forward curve sensitivity
    final DoubleAVLTreeSet forwardTime = new DoubleAVLTreeSet();
    for (int loopcpn = 0; loopcpn < SWAPTION_LONG_REC.getUnderlyingSwap().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      final CouponIbor cpn = (CouponIbor) SWAPTION_LONG_REC.getUnderlyingSwap().getSecondLeg().getNthPayment(loopcpn);
      forwardTime.add(cpn.getFixingPeriodStartTime());
      forwardTime.add(cpn.getFixingPeriodEndTime());
    }
    final double[] nodeTimesForward = forwardTime.toDoubleArray();
    final List<DoublesPair> sensiPvFwd = pvcsSwaption.getSensitivities().get(CURVES_NAME[2]);
    final List<DoublesPair> fdSenseFwd = FDCurveSensitivityCalculator.curveSensitvityFDCalculator(SWAPTION_LONG_REC, METHOD_BLACK, CURVES_BLACK, CURVES_NAME[2], nodeTimesForward, TOLERANCE_DELTA);
    assertSensitivityEquals(sensiPvFwd, fdSenseFwd, TOLERANCE_DELTA);
    // 2. Discounting curve sensitivity
    final DoubleAVLTreeSet discTime = new DoubleAVLTreeSet();
    discTime.add(SWAPTION_LONG_REC.getSettlementTime());
    for (int loopcpn = 0; loopcpn < SWAPTION_LONG_REC.getUnderlyingSwap().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      final CouponIbor cpn = (CouponIbor) SWAPTION_LONG_REC.getUnderlyingSwap().getSecondLeg().getNthPayment(loopcpn);
      discTime.add(cpn.getPaymentTime());
    }
    final double[] nodeTimesDisc = discTime.toDoubleArray();
    final List<DoublesPair> sensiPvDisc = pvcsSwaption.getSensitivities().get(CURVES_NAME[0]);
    final List<DoublesPair> fdSenseDsc = FDCurveSensitivityCalculator.curveSensitvityFDCalculator(SWAPTION_LONG_REC, METHOD_BLACK, CURVES_BLACK, CURVES_NAME[0], nodeTimesDisc, TOLERANCE_DELTA);
    assertSensitivityEquals(sensiPvDisc, fdSenseDsc, TOLERANCE_DELTA);
  }

  @Test
  /**
   * Compare the method figures to the Calculator figures.
   */
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final InterestRateCurveSensitivity pvcsMethod = METHOD_BLACK.presentValueCurveSensitivity(SWAPTION_LONG_REC, CURVES_BLACK);
    final InterestRateCurveSensitivity pvcsCalculator = new InterestRateCurveSensitivity(SWAPTION_LONG_REC.accept(PVCSC_BLACK, CURVES_BLACK));
    AssertSensivityObjects.assertEquals("Swaption Black method: present value", pvcsMethod, pvcsCalculator, TOLERANCE_DELTA);
  }

  @Test
  /**
   * Tests the Black volatility sensitivity (vega).
   */
  public void presentValueBlackSensitivity() {
    final double shift = 1.0E-6;
    final PresentValueBlackSwaptionSensitivity pvbvs = METHOD_BLACK.presentValueBlackSensitivity(SWAPTION_LONG_REC, CURVES_BLACK);
    final BlackFlatSwaptionParameters BlackP = TestsDataSetsBlack.createBlackSwaptionEUR6Shift(shift);
    final YieldCurveWithBlackSwaptionBundle curvesBlackP = new YieldCurveWithBlackSwaptionBundle(BlackP, CURVES);
    final CurrencyAmount pvP = METHOD_BLACK.presentValue(SWAPTION_LONG_REC, curvesBlackP);
    final BlackFlatSwaptionParameters BlackM = TestsDataSetsBlack.createBlackSwaptionEUR6Shift(-shift);
    final YieldCurveWithBlackSwaptionBundle curvesBlackM = new YieldCurveWithBlackSwaptionBundle(BlackM, CURVES);
    final CurrencyAmount pvM = METHOD_BLACK.presentValue(SWAPTION_LONG_REC, curvesBlackM);
    final DoublesPair point = DoublesPair.of(SWAPTION_LONG_REC.getTimeToExpiry(), SWAPTION_LONG_REC.getMaturityTime());
    assertEquals("Swaption Black method: present value volatility sensitivity", (pvP.getAmount() - pvM.getAmount()) / (2 * shift), pvbvs.getSensitivity().getMap().get(point), TOLERANCE_DELTA);
  }

  @Test
  /**
   * Tests the Black volatility sensitivity (vega).
   */
  public void presentValueBlackSensitivityMethodVsCalculator() {
    final PresentValueBlackSwaptionSensitivity pvbsMethod = METHOD_BLACK.presentValueBlackSensitivity(SWAPTION_LONG_REC, CURVES_BLACK);
    final PresentValueBlackSwaptionSensitivity pvbsCalculator = SWAPTION_LONG_REC.accept(PVBSC_BLACK, CURVES_BLACK);
    assertEquals("Swaption Black method: present value", pvbsMethod, pvbsCalculator);
  }

  @Test
  /**
   * Tests the Black volatility sensitivity (vega).
   */
  public void presentValueBlackNodeSensitivity() {
    final double shift = 1.0E-6;
    final PresentValueBlackSwaptionSensitivity pvbvs = METHOD_BLACK.presentValueBlackSensitivity(SWAPTION_LONG_REC, CURVES_BLACK);
    final PresentValueBlackSwaptionSensitivity pvbns = BSSNC.calculateNodeSensitivities(pvbvs, BLACK);
    final double[] x = ((InterpolatedDoublesSurface) BLACK.getVolatilitySurface()).getXDataAsPrimitive();
    final double[] y = ((InterpolatedDoublesSurface) BLACK.getVolatilitySurface()).getYDataAsPrimitive();
    for (int loopindex = 0; loopindex < x.length; loopindex++) {
      final BlackFlatSwaptionParameters BlackP = TestsDataSetsBlack.createBlackSwaptionEUR6Shift(loopindex, shift);
      final YieldCurveWithBlackSwaptionBundle curvesBlackP = new YieldCurveWithBlackSwaptionBundle(BlackP, CURVES);
      final CurrencyAmount pvP = METHOD_BLACK.presentValue(SWAPTION_LONG_REC, curvesBlackP);
      final BlackFlatSwaptionParameters BlackM = TestsDataSetsBlack.createBlackSwaptionEUR6Shift(loopindex, -shift);
      final YieldCurveWithBlackSwaptionBundle curvesBlackM = new YieldCurveWithBlackSwaptionBundle(BlackM, CURVES);
      final CurrencyAmount pvM = METHOD_BLACK.presentValue(SWAPTION_LONG_REC, curvesBlackM);
      assertEquals("Swaption Black method: present value volatility sensitivity", (pvP.getAmount() - pvM.getAmount()) / (2 * shift),
          pvbns.getSensitivity().getMap().get(DoublesPair.of(x[loopindex], y[loopindex])), TOLERANCE_DELTA);
    }
  }

}
