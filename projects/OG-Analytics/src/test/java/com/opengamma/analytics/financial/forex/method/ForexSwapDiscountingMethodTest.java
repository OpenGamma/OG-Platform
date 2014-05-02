/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.method;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.calculator.PresentValueCurveSensitivityMCSCalculator;
import com.opengamma.analytics.financial.calculator.PresentValueMCACalculator;
import com.opengamma.analytics.financial.forex.calculator.CurrencyExposureForexCalculator;
import com.opengamma.analytics.financial.forex.definition.ForexSwapDefinition;
import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.ParSpreadMarketQuoteCalculator;
import com.opengamma.analytics.financial.interestrate.ParSpreadMarketQuoteCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.calculator.generic.TodayPaymentCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Test related to the method for Forex Swap transaction by discounting on each payment.
 * @deprecated The class tests deprecated code
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class ForexSwapDiscountingMethodTest {

  private static final Currency CUR_1 = Currency.EUR;
  private static final Currency CUR_2 = Currency.USD;
  private static final ZonedDateTime NEAR_DATE = DateUtils.getUTCDate(2011, 5, 26);
  private static final ZonedDateTime FAR_DATE = DateUtils.getUTCDate(2011, 6, 27); // 1m
  private static final double NOMINAL_1 = 100000000;
  private static final double FX_RATE = 1.4177;
  private static final FXMatrix FX_MATRIX = new FXMatrix(CUR_1, CUR_2, FX_RATE);
  private static final double FORWARD_POINTS = -0.0007;
  private static final ForexSwapDefinition FX_SWAP_DEFINITION = new ForexSwapDefinition(CUR_1, CUR_2, NEAR_DATE, FAR_DATE, NOMINAL_1, FX_RATE, FORWARD_POINTS);

  private static final YieldCurveBundle CURVES = TestsDataSetsForex.createCurvesForex();
  private static final String[] CURVES_NAME = TestsDataSetsForex.curveNames();
  private static final Map<String, Currency> CCY_MAP = TestsDataSetsForex.curveCurrency();
  private static final YieldCurveBundle CURVES_FX = new YieldCurveBundle(CURVES.getCurvesMap(), FX_MATRIX, CCY_MAP);
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 5, 20);
  private static final InstrumentDerivative FX_SWAP = FX_SWAP_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);

  private static final ForexSwapDiscountingMethod METHOD_FX_SWAP = ForexSwapDiscountingMethod.getInstance();
  private static final ForexDiscountingMethod METHOD_FX = ForexDiscountingMethod.getInstance();
  private static final PresentValueMCACalculator PVC_FX = PresentValueMCACalculator.getInstance();
  private static final CurrencyExposureForexCalculator CEC_FX = CurrencyExposureForexCalculator.getInstance();
  private static final PresentValueCurveSensitivityMCSCalculator PVCSC_FX = PresentValueCurveSensitivityMCSCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityCalculator PSCSC = ParSpreadMarketQuoteCurveSensitivityCalculator.getInstance();
  private static final TodayPaymentCalculator TPC = TodayPaymentCalculator.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2; // one cent out of 100m
  private static final double TOLERANCE_RATE = 1.0E-10;
  private static final double TOLERANCE_SPREAD_DELTA = 1.0E-6;
  private static final double TOLERANCE_TIME = 1.0E-6;

  @Test
  /**
   * Tests the present value computation.
   */
  public void presentValue() {
    final MultipleCurrencyAmount pv = METHOD_FX_SWAP.presentValue(FX_SWAP, CURVES);
    final MultipleCurrencyAmount pvNear = METHOD_FX.presentValue(((ForexSwap) FX_SWAP).getNearLeg(), CURVES);
    final MultipleCurrencyAmount pvFar = METHOD_FX.presentValue(((ForexSwap) FX_SWAP).getFarLeg(), CURVES);
    assertEquals(pvNear.getAmount(CUR_1) + pvFar.getAmount(CUR_1), pv.getAmount(CUR_1));
    assertEquals(pvNear.getAmount(CUR_2) + pvFar.getAmount(CUR_2), pv.getAmount(CUR_2));
  }

  @Test
  /**
   * Test the present value through the method and through the calculator.
   */
  public void presentValueMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD_FX_SWAP.presentValue(FX_SWAP, CURVES);
    final MultipleCurrencyAmount pvCalculator = FX_SWAP.accept(PVC_FX, CURVES);
    assertEquals("Forex present value: Method vs Calculator", pvMethod, pvCalculator);
    final InstrumentDerivative fxSwap = FX_SWAP;
    final MultipleCurrencyAmount pvMethod2 = METHOD_FX_SWAP.presentValue(fxSwap, CURVES);
    assertEquals("Forex present value: Method ForexSwap vs Method ForexDerivative", pvMethod, pvMethod2);
  }

  @Test
  /**
   * Tests the currency exposure computation.
   */
  public void currencyExposure() {
    final MultipleCurrencyAmount exposureMethod = METHOD_FX_SWAP.currencyExposure(FX_SWAP, CURVES);
    final MultipleCurrencyAmount pv = METHOD_FX_SWAP.presentValue(FX_SWAP, CURVES);
    assertEquals("Currency exposure", pv, exposureMethod);
    final MultipleCurrencyAmount exposureCalculator = FX_SWAP.accept(CEC_FX, CURVES);
    assertEquals("Currency exposure: Method vs Calculator", exposureMethod, exposureCalculator);
  }

  @Test
  /**
   * Test the present value sensitivity to interest rate.
   */
  public void presentValueCurveSensitivity() {
    MultipleCurrencyInterestRateCurveSensitivity pvs = METHOD_FX_SWAP.presentValueCurveSensitivity(FX_SWAP, CURVES);
    pvs = pvs.cleaned();
    MultipleCurrencyInterestRateCurveSensitivity pvsNear = METHOD_FX.presentValueCurveSensitivity(((ForexSwap) FX_SWAP).getNearLeg(), CURVES);
    final MultipleCurrencyInterestRateCurveSensitivity pvsFar = METHOD_FX.presentValueCurveSensitivity(((ForexSwap) FX_SWAP).getFarLeg(), CURVES);
    pvsNear = pvsNear.plus(pvsFar);
    pvsNear = pvsNear.cleaned();
    assertTrue("Forex swap present value curve sensitivity", pvs.equals(pvsNear));
  }

  @Test
  /**
   * Test the present value curve sensitivity through the method and through the calculator.
   */
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final MultipleCurrencyInterestRateCurveSensitivity pvcsMethod = METHOD_FX_SWAP.presentValueCurveSensitivity(FX_SWAP, CURVES);
    final MultipleCurrencyInterestRateCurveSensitivity pvcsCalculator = FX_SWAP.accept(PVCSC_FX, CURVES);
    assertEquals("Forex swap present value curve sensitivity: Method vs Calculator", pvcsMethod, pvcsCalculator);
  }

  @Test
  /**
   * Tests the TodayPaymentCalculator for forex transactions.
   */
  public void forexTodayPaymentBeforeNearDate() {
    final InstrumentDerivative fx = FX_SWAP_DEFINITION.toDerivative(NEAR_DATE.minusDays(1), CURVES_NAME);
    final MultipleCurrencyAmount cash = fx.accept(TPC);
    assertEquals("TodayPaymentCalculator: forex", 0.0, cash.getAmount(FX_SWAP_DEFINITION.getNearLeg().getCurrency1()), TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: forex", 0.0, cash.getAmount(FX_SWAP_DEFINITION.getNearLeg().getCurrency2()), TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: forex", 2, cash.getCurrencyAmounts().length);
  }

  @Test
  /**
   * Tests the TodayPaymentCalculator for forex transactions.
   */
  public void forexTodayPaymentOnNearDate() {
    final InstrumentDerivative fx = FX_SWAP_DEFINITION.toDerivative(NEAR_DATE, CURVES_NAME);
    final MultipleCurrencyAmount cash = fx.accept(TPC);
    assertEquals("TodayPaymentCalculator: forex", FX_SWAP_DEFINITION.getNearLeg().getPaymentCurrency1().getReferenceAmount(), cash.getAmount(FX_SWAP_DEFINITION.getNearLeg().getCurrency1()),
        TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: forex", FX_SWAP_DEFINITION.getNearLeg().getPaymentCurrency2().getReferenceAmount(), cash.getAmount(FX_SWAP_DEFINITION.getNearLeg().getCurrency2()),
        TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: forex", 2, cash.getCurrencyAmounts().length);
  }

  @Test
  /**
   * Tests the TodayPaymentCalculator for forex transactions.
   */
  public void forexTodayPaymentBeforeFarDate() {
    final InstrumentDerivative fx = FX_SWAP_DEFINITION.toDerivative(FAR_DATE.minusDays(1), CURVES_NAME);
    final MultipleCurrencyAmount cash = fx.accept(TPC);
    assertEquals("TodayPaymentCalculator: forex", 0.0, cash.getAmount(FX_SWAP_DEFINITION.getFarLeg().getCurrency1()), TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: forex", 0.0, cash.getAmount(FX_SWAP_DEFINITION.getFarLeg().getCurrency2()), TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: forex", 2, cash.getCurrencyAmounts().length);
  }

  @Test
  /**
   * Tests the TodayPaymentCalculator for forex transactions.
   */
  public void forexTodayPaymentOnFarDate() {
    final InstrumentDerivative fx = FX_SWAP_DEFINITION.toDerivative(FAR_DATE, CURVES_NAME);
    final MultipleCurrencyAmount cash = fx.accept(TPC);
    assertEquals("TodayPaymentCalculator: forex", FX_SWAP_DEFINITION.getFarLeg().getPaymentCurrency1().getReferenceAmount(), cash.getAmount(FX_SWAP_DEFINITION.getFarLeg().getCurrency1()),
        TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: forex", FX_SWAP_DEFINITION.getFarLeg().getPaymentCurrency2().getReferenceAmount(), cash.getAmount(FX_SWAP_DEFINITION.getFarLeg().getCurrency2()),
        TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: forex", 2, cash.getCurrencyAmounts().length);
  }

  @Test
  /**
   * Tests the parSpread method.
   */
  public void parSpread() {
    final double parSpread = METHOD_FX_SWAP.parSpread((ForexSwap) FX_SWAP, CURVES_FX);
    final ForexSwapDefinition fxSwap0Definition = new ForexSwapDefinition(CUR_1, CUR_2, NEAR_DATE, FAR_DATE, NOMINAL_1, FX_RATE, FORWARD_POINTS + parSpread);
    final ForexSwap fxSwap0 = (ForexSwap) fxSwap0Definition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final MultipleCurrencyAmount pv0 = METHOD_FX_SWAP.presentValue(fxSwap0, CURVES);
    assertEquals("Forex swap: par spread", 0, FX_MATRIX.convert(pv0, CUR_1).getAmount(), TOLERANCE_PV);
  }

  @Test
  /**
   * Test the par spread method vs calculator
   */
  public void parSpreadMethodVsCalculator() {
    final double parSpreadMethod = METHOD_FX_SWAP.parSpread((ForexSwap) FX_SWAP, CURVES_FX);
    final double parSpreadCalculator = FX_SWAP.accept(ParSpreadMarketQuoteCalculator.getInstance(), CURVES_FX);
    assertEquals("Forex swap: par spread", parSpreadMethod, parSpreadCalculator, TOLERANCE_RATE);
  }

  @Test
  /**
   * Tests parSpread curve sensitivity.
   */
  public void parSpreadCurveSensitivity() {
    final ForexSwap fxSwap = (ForexSwap) FX_SWAP;
    InterestRateCurveSensitivity pscsMethod = METHOD_FX_SWAP.parSpreadCurveSensitivity(fxSwap, CURVES_FX);
    pscsMethod = pscsMethod.cleaned(0.0, 1.0E-8);
    final double ps = METHOD_FX_SWAP.parSpread(fxSwap, CURVES_FX);
    final double deltaShift = 0.000001;
    final int nbNode = 2;
    final double[] result = new double[nbNode];
    final double[] nodeTimesExtended = new double[nbNode + 1];
    nodeTimesExtended[1] = fxSwap.getNearLeg().getPaymentTime();
    nodeTimesExtended[2] = fxSwap.getFarLeg().getPaymentTime();
    final double[] yields = new double[nbNode + 1];
    YieldAndDiscountCurve curveToBump;
    YieldAndDiscountCurve curveNode;
    // EUR Dsc
    final List<DoublesPair> sensi1 = pscsMethod.getSensitivities().get(CURVES_NAME[0]);
    curveToBump = CURVES.getCurve(CURVES_NAME[0]);
    yields[0] = curveToBump.getInterestRate(0.0);
    yields[1] = curveToBump.getInterestRate(nodeTimesExtended[1]);
    yields[2] = curveToBump.getInterestRate(nodeTimesExtended[2]);
    curveNode = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(nodeTimesExtended, yields, new LinearInterpolator1D()));
    for (int loopnode = 0; loopnode < nbNode; loopnode++) {
      final YieldAndDiscountCurve curveBumped = curveNode.withSingleShift(nodeTimesExtended[loopnode + 1], deltaShift);
      CURVES_FX.replaceCurve(CURVES_NAME[0], curveBumped);
      final double psBumped = METHOD_FX_SWAP.parSpread(fxSwap, CURVES_FX);
      result[loopnode] = (psBumped - ps) / deltaShift;
      final DoublesPair pairPv = sensi1.get(loopnode);
      assertEquals("Sensitivity par spread to curve: Node " + loopnode, nodeTimesExtended[loopnode + 1], pairPv.getFirst(), TOLERANCE_TIME);
      assertEquals("Sensitivity par spread to curve: Node", pairPv.second, result[loopnode], TOLERANCE_SPREAD_DELTA);
    }
    CURVES_FX.replaceCurve(CURVES_NAME[0], curveToBump);
    // USD Dsc
    final List<DoublesPair> sensi2 = pscsMethod.getSensitivities().get(CURVES_NAME[0]);
    curveToBump = CURVES.getCurve(CURVES_NAME[1]);
    yields[0] = curveToBump.getInterestRate(0.0);
    yields[1] = curveToBump.getInterestRate(nodeTimesExtended[1]);
    yields[2] = curveToBump.getInterestRate(nodeTimesExtended[2]);
    curveNode = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(nodeTimesExtended, yields, new LinearInterpolator1D()));
    for (int loopnode = 0; loopnode < nbNode; loopnode++) {
      final YieldAndDiscountCurve curveBumped = curveNode.withSingleShift(nodeTimesExtended[loopnode + 1], deltaShift);
      CURVES_FX.replaceCurve(CURVES_NAME[1], curveBumped);
      final double psBumped = METHOD_FX_SWAP.parSpread(fxSwap, CURVES_FX);
      result[loopnode] = (psBumped - ps) / deltaShift;
      final DoublesPair pairPv = sensi2.get(loopnode);
      assertEquals("Sensitivity par spread to curve: Node " + loopnode, nodeTimesExtended[loopnode + 1], pairPv.getFirst(), TOLERANCE_TIME);
    }
    CURVES_FX.replaceCurve(CURVES_NAME[1], curveToBump);
    InterestRateCurveSensitivity prcsCalculator = fxSwap.accept(PSCSC, CURVES_FX);
    prcsCalculator = prcsCalculator.cleaned(0.0, 1.0E-4);
    AssertSensitivityObjects.assertEquals("Forex swap: par rate curve sensitivity", pscsMethod, prcsCalculator, TOLERANCE_SPREAD_DELTA);
  }

}
