/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.fra.method;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.ParSpreadMarketQuoteCalculator;
import com.opengamma.analytics.financial.interestrate.ParSpreadMarketQuoteCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.method.SensitivityFiniteDifference;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Tests the ForwardRateAgreement discounting method.
 * @deprecated This class tests deprecated functionality
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class ForwardRateAgreementDiscountingMethodTest {
  // Index
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.EUR;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);
  // Dates : The above dates are not standard but selected for insure correct testing.
  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2011, 1, 3);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 1, 6);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 4, 4);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 1, 7);
  private static final DayCount DAY_COUNT_PAYMENT = DayCounts.ACT_365;
  private static final double ACCRUAL_FACTOR_PAYMENT = DAY_COUNT_PAYMENT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double FRA_RATE = 0.05;
  private static final double NOTIONAL = 1000000; //1m
  // Coupon with specific payment and accrual dates.
  private static final ForwardRateAgreementDefinition FRA_DEFINITION = new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL,
      FIXING_DATE, INDEX, FRA_RATE, CALENDAR);
  // To derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 10, 9);

  private static final YieldCurveBundle CURVES_1 = TestsDataSetsSABR.createCurves1();
  private static final String[] CURVE_NAME_1 = TestsDataSetsSABR.curves1Names();
  private static final YieldCurveBundle CURVES_2 = TestsDataSetsSABR.createCurves2();
  private static final String[] CURVE_NAME_2 = TestsDataSetsSABR.curves2Names();
  private static final ForwardRateAgreement FRA = (ForwardRateAgreement) FRA_DEFINITION.toDerivative(REFERENCE_DATE, CURVE_NAME_1);
  private static final ForwardRateAgreementDiscountingBundleMethod FRA_METHOD = ForwardRateAgreementDiscountingBundleMethod.getInstance();
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final ParSpreadMarketQuoteCalculator PSC = ParSpreadMarketQuoteCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityCalculator PSCSC = ParSpreadMarketQuoteCurveSensitivityCalculator.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_RATE = 1.0E-10;
  private static final double TOLERANCE_SPREAD_DELTA = 1.0E-6;
  private static final double TOLERANCE_TIME = 1.0E-6;

  @Test
  public void parRate() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final double forward = FRA_METHOD.parRate(FRA, curves);
    final double dfForwardCurveStart = curves.getCurve(CURVE_NAME_1[1]).getDiscountFactor(FRA.getFixingPeriodStartTime());
    final double dfForwardCurveEnd = curves.getCurve(CURVE_NAME_1[1]).getDiscountFactor(FRA.getFixingPeriodEndTime());
    final double forwardExpected = (dfForwardCurveStart / dfForwardCurveEnd - 1) / FRA.getFixingYearFraction();
    assertEquals("FRA discounting: par rate", forwardExpected, forward, TOLERANCE_RATE);
  }

  @Test
  public void presentValue() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final double forward = FRA_METHOD.parRate(FRA, curves);
    final double dfSettle = curves.getCurve(CURVE_NAME_1[0]).getDiscountFactor(FRA.getPaymentTime());
    final double expectedPv = FRA.getNotional() * dfSettle * FRA.getPaymentYearFraction() * (forward - FRA_RATE) / (1 + FRA.getPaymentYearFraction() * forward);
    final CurrencyAmount pv = FRA_METHOD.presentValue(FRA, curves);
    assertEquals("FRA discounting: present value", expectedPv, pv.getAmount(), TOLERANCE_PV);
  }

  @Test
  public void presentValueMethodVsCalculator() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final CurrencyAmount pvMethod = FRA_METHOD.presentValue(FRA, curves);
    final double pvCalculator = FRA.accept(PVC, curves);
    assertEquals("FRA discounting: present value calculator vs method", pvCalculator, pvMethod.getAmount(), 1.0E-2);
  }

  @Test
  public void presentValueBuySellParity() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final ForwardRateAgreementDefinition fraDefinitionSell = new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, -NOTIONAL,
        FIXING_DATE, INDEX, FRA_RATE, CALENDAR);
    final ForwardRateAgreement fraSell = (ForwardRateAgreement) fraDefinitionSell.toDerivative(REFERENCE_DATE, CURVE_NAME_1);
    final CurrencyAmount pvBuy = FRA_METHOD.presentValue(FRA, curves);
    final CurrencyAmount pvSell = FRA_METHOD.presentValue(fraSell, curves);
    assertEquals("FRA discounting: present value - buy/sell parity", pvSell.getAmount(), -pvBuy.getAmount(), 1.0E-2);
  }

  @Test
  public void sensitivity() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    // Par rate sensitivity
    InterestRateCurveSensitivity prsFra = FRA_METHOD.parRateCurveSensitivity(FRA, curves);
    final InterestRateCurveSensitivity pvsFra = FRA_METHOD.presentValueCurveSensitivity(FRA, curves);
    prsFra = prsFra.cleaned();
    final double deltaTolerancePrice = 1.0E+2;
    final double deltaToleranceRate = 1.0E-7;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
    final double deltaShift = 1.0E-8;
    final double forward = FRA_METHOD.parRate(FRA, curves);
    final double pv = FRA_METHOD.presentValue(FRA, curves).getAmount();
    // 1. Forward curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    final String[] bumpedCurvesForwardName = {CURVE_NAME_1[0], bumpedCurveName };
    final ForwardRateAgreement fraBumpedForward = (ForwardRateAgreement) FRA_DEFINITION.toDerivative(REFERENCE_DATE, bumpedCurvesForwardName);
    final YieldAndDiscountCurve curveForward = curves.getCurve(CURVE_NAME_1[1]);
    final double[] timeForward = new double[2];
    timeForward[0] = FRA.getFixingPeriodStartTime();
    timeForward[1] = FRA.getFixingPeriodEndTime();
    final int nbForwardDate = timeForward.length;
    final double[] yieldsForward = new double[nbForwardDate + 1];
    final double[] nodeTimesForward = new double[nbForwardDate + 1];
    yieldsForward[0] = curveForward.getInterestRate(0.0);
    for (int i = 0; i < nbForwardDate; i++) {
      nodeTimesForward[i + 1] = timeForward[i];
      yieldsForward[i + 1] = curveForward.getInterestRate(nodeTimesForward[i + 1]);
    }
    final YieldAndDiscountCurve tempCurveForward = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(nodeTimesForward, yieldsForward, new LinearInterpolator1D()));
    final List<DoublesPair> sensiForwardForward = prsFra.getSensitivities().get(CURVE_NAME_1[1]);
    final List<DoublesPair> sensiPvForward = pvsFra.getSensitivities().get(CURVE_NAME_1[1]);
    final double[] sensiForwardForwardFD = new double[nbForwardDate];
    final double[] sensiPvForwardFD = new double[nbForwardDate];
    for (int i = 0; i < nbForwardDate; i++) {
      final YieldAndDiscountCurve bumpedCurveForward = tempCurveForward.withSingleShift(nodeTimesForward[i + 1], deltaShift);
      final YieldCurveBundle curvesBumpedForward = new YieldCurveBundle();
      curvesBumpedForward.addAll(curves);
      curvesBumpedForward.setCurve("Bumped Curve", bumpedCurveForward);
      final double bumpedForward = FRA_METHOD.parRate(fraBumpedForward, curvesBumpedForward);
      final double bumpedPv = FRA_METHOD.presentValue(fraBumpedForward, curvesBumpedForward).getAmount();
      sensiForwardForwardFD[i] = (bumpedForward - forward) / deltaShift;
      sensiPvForwardFD[i] = (bumpedPv - pv) / deltaShift;
      final DoublesPair pairForward = sensiForwardForward.get(i);
      final DoublesPair pairPv = sensiPvForward.get(i);
      assertEquals("Sensitivity forward to forward curve: Node " + i, nodeTimesForward[i + 1], pairForward.getFirst(), 1E-8);
      assertEquals("Sensitivity forward to forward curve: Node " + i, sensiForwardForwardFD[i], pairForward.getSecond(), deltaToleranceRate);
      assertEquals("Sensitivity pv to forward curve: Node " + i, nodeTimesForward[i + 1], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity pv to forward curve: Node " + i, sensiPvForwardFD[i], pairPv.getSecond(), deltaTolerancePrice);
    }
    // 2. Funding curve sensitivity
    final String[] bumpedCurvesFundingName = {bumpedCurveName, CURVE_NAME_1[1] };
    final ForwardRateAgreement fraBumped = (ForwardRateAgreement) FRA_DEFINITION.toDerivative(REFERENCE_DATE, bumpedCurvesFundingName);
    final YieldAndDiscountCurve curveFunding = curves.getCurve(CURVE_NAME_1[0]);
    final double[] yieldsFunding = new double[2];
    final double[] nodeTimesFunding = new double[2];
    yieldsFunding[0] = curveFunding.getInterestRate(0.0);
    nodeTimesFunding[1] = FRA.getPaymentTime();
    yieldsFunding[1] = curveFunding.getInterestRate(nodeTimesFunding[1]);
    final YieldAndDiscountCurve tempCurveFunding = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(nodeTimesFunding, yieldsFunding, new LinearInterpolator1D()));
    final List<DoublesPair> tempFunding = pvsFra.getSensitivities().get(CURVE_NAME_1[0]);
    final YieldAndDiscountCurve bumpedCurve = tempCurveFunding.withSingleShift(nodeTimesFunding[1], deltaShift);
    final YieldCurveBundle curvesBumped = new YieldCurveBundle();
    curvesBumped.addAll(curves);
    curvesBumped.setCurve("Bumped Curve", bumpedCurve);
    final double bumpedPvDsc = FRA_METHOD.presentValue(fraBumped, curvesBumped).getAmount();
    final double resDsc = (bumpedPvDsc - pv) / deltaShift;
    final DoublesPair pair = tempFunding.get(0);
    assertEquals("Sensitivity pv to discounting curve:", nodeTimesFunding[1], pair.getFirst(), 1E-8);
    assertEquals("Sensitivity pv to discounting curve:", resDsc, pair.getSecond(), deltaTolerancePrice);
  }

  @Test
  public void sensitivityMethod() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
    final double deltaShift = 1.0E-8;
    final double pv = FRA_METHOD.presentValue(FRA, curves).getAmount();
    // 1. Forward curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    final String[] bumpedCurvesForwardName = {CURVE_NAME_1[0], bumpedCurveName };
    final ForwardRateAgreement fraBumpedForward = (ForwardRateAgreement) FRA_DEFINITION.toDerivative(REFERENCE_DATE, bumpedCurvesForwardName);
    final YieldAndDiscountCurve curveForward = curves.getCurve(CURVE_NAME_1[1]);
    final double[] timeForward = new double[2];
    timeForward[0] = FRA.getFixingPeriodStartTime();
    timeForward[1] = FRA.getFixingPeriodEndTime();
    final int nbForwardDate = timeForward.length;
    final double[] yieldsForward = new double[nbForwardDate + 1];
    final double[] nodeTimesForward = new double[nbForwardDate + 1];
    yieldsForward[0] = curveForward.getInterestRate(0.0);
    for (int i = 0; i < nbForwardDate; i++) {
      nodeTimesForward[i + 1] = timeForward[i];
      yieldsForward[i + 1] = curveForward.getInterestRate(nodeTimesForward[i + 1]);
    }
    final YieldAndDiscountCurve tempCurveForward = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(nodeTimesForward, yieldsForward, new LinearInterpolator1D()));
    final double[] sensiPvForwardFD = new double[nbForwardDate];
    for (int i = 0; i < nbForwardDate; i++) {
      final YieldAndDiscountCurve bumpedCurveForward = tempCurveForward.withSingleShift(nodeTimesForward[i + 1], deltaShift);
      final YieldCurveBundle curvesBumpedForward = new YieldCurveBundle();
      curvesBumpedForward.addAll(curves);
      curvesBumpedForward.setCurve("Bumped Curve", bumpedCurveForward);
      final double bumpedPv = FRA_METHOD.presentValue(fraBumpedForward, curvesBumpedForward).getAmount();
      sensiPvForwardFD[i] = (bumpedPv - pv) / deltaShift;
    }

    final double[] nodeTimesForwardMethod = new double[] {FRA.getFixingPeriodStartTime(), FRA.getFixingPeriodEndTime() };
    final double[] sensiForwardMethod = SensitivityFiniteDifference.curveSensitivity(fraBumpedForward, curves, pv, CURVE_NAME_1[1], bumpedCurveName, nodeTimesForwardMethod, deltaShift, FRA_METHOD);
    assertEquals("Sensitivity finite difference method: number of node", 2, sensiForwardMethod.length);
    for (int loopnode = 0; loopnode < sensiForwardMethod.length; loopnode++) {
      assertEquals("Sensitivity finite difference method: node sensitivity", sensiPvForwardFD[loopnode], sensiForwardMethod[loopnode]);
    }

    // 2. Funding curve sensitivity
    final String[] bumpedCurvesFundingName = {bumpedCurveName, CURVE_NAME_1[1] };
    final ForwardRateAgreement fraBumped = (ForwardRateAgreement) FRA_DEFINITION.toDerivative(REFERENCE_DATE, bumpedCurvesFundingName);
    final YieldAndDiscountCurve curveFunding = curves.getCurve(CURVE_NAME_1[0]);
    final double[] yieldsFunding = new double[2];
    final double[] nodeTimesFunding = new double[2];
    yieldsFunding[0] = curveFunding.getInterestRate(0.0);
    nodeTimesFunding[1] = FRA.getPaymentTime();
    yieldsFunding[1] = curveFunding.getInterestRate(nodeTimesFunding[1]);
    final YieldAndDiscountCurve tempCurveFunding = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(nodeTimesFunding, yieldsFunding, new LinearInterpolator1D()));
    final YieldAndDiscountCurve bumpedCurve = tempCurveFunding.withSingleShift(nodeTimesFunding[1], deltaShift);
    final YieldCurveBundle curvesBumped = new YieldCurveBundle();
    curvesBumped.addAll(curves);
    curvesBumped.replaceCurve("Bumped Curve", bumpedCurve);
    final double bumpedPvDsc = FRA_METHOD.presentValue(fraBumped, curvesBumped).getAmount();
    final double[] resDsc = new double[1];
    resDsc[0] = (bumpedPvDsc - pv) / deltaShift;

    final double[] nodeTimesFundingMethod = new double[] {FRA.getPaymentTime() };
    final double[] sensiFundingMethod = SensitivityFiniteDifference.curveSensitivity(fraBumped, curves, pv, CURVE_NAME_1[0], bumpedCurveName, nodeTimesFundingMethod, deltaShift, FRA_METHOD);
    assertEquals("Sensitivity finite difference method: number of node", 1, sensiFundingMethod.length);
    for (int loopnode = 0; loopnode < sensiFundingMethod.length; loopnode++) {
      assertEquals("Sensitivity finite difference method: node sensitivity", resDsc[loopnode], sensiFundingMethod[loopnode]);
    }
  }

  @Test
  public void presentValueSensitivityMethodVsCalculator() {
    final InterestRateCurveSensitivity pvcsMethod = FRA_METHOD.presentValueCurveSensitivity(FRA, CURVES_1);
    final PresentValueCurveSensitivityCalculator calculator = PresentValueCurveSensitivityCalculator.getInstance();
    final Map<String, List<DoublesPair>> pvcsCalculator = FRA.accept(calculator, CURVES_1);
    assertEquals("FRA discounting: present value calculator vs method", pvcsCalculator, pvcsMethod.getSensitivities());
  }

  @Test
  public void parSpread() {
    final ForwardRateAgreement fra2 = (ForwardRateAgreement) FRA_DEFINITION.toDerivative(REFERENCE_DATE, CURVE_NAME_2);
    final double parSpread = FRA_METHOD.parSpread(fra2, CURVES_2);
    final ForwardRateAgreementDefinition fra0Definition = new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE,
        INDEX, FRA_RATE + parSpread, CALENDAR);
    final ForwardRateAgreement fra0 = (ForwardRateAgreement) fra0Definition.toDerivative(REFERENCE_DATE, CURVE_NAME_2);
    final double pv0 = fra0.accept(PVC, CURVES_2);
    assertEquals("FRA discounting: par spread", pv0, 0, TOLERANCE_PV);
  }

  @Test
  public void parSpreadMethodVsCalculator() {
    final ForwardRateAgreement fra2 = (ForwardRateAgreement) FRA_DEFINITION.toDerivative(REFERENCE_DATE, CURVE_NAME_2);
    final double parSpreadMethod = FRA_METHOD.parSpread(fra2, CURVES_2);
    final double parSpreadCalculator = fra2.accept(PSC, CURVES_2);
    assertEquals("FRA discounting: par spread", parSpreadMethod, parSpreadCalculator, TOLERANCE_RATE);
  }

  @Test
  /**
   * Tests parSpread curve sensitivity.
   */
  public void parSpreadCurveSensitivity() {
    final ForwardRateAgreement fra = (ForwardRateAgreement) FRA_DEFINITION.toDerivative(REFERENCE_DATE, CURVE_NAME_2);
    final InterestRateCurveSensitivity pscsMethod = FRA_METHOD.parSpreadCurveSensitivity(fra, CURVES_2);
    final List<DoublesPair> sensiPsFwd = pscsMethod.getSensitivities().get(CURVE_NAME_2[1]);
    final double ps = FRA_METHOD.parSpread(fra, CURVES_2);
    final YieldAndDiscountCurve curveToBump = CURVES_2.getCurve(CURVE_NAME_2[1]);
    final double deltaShift = 0.000001;
    final int nbNode = 2;
    final double[] result = new double[nbNode];
    final double[] nodeTimesExtended = new double[nbNode + 1];
    nodeTimesExtended[1] = fra.getFixingPeriodStartTime();
    nodeTimesExtended[2] = fra.getFixingPeriodEndTime();
    final double[] yields = new double[nbNode + 1];
    yields[0] = curveToBump.getInterestRate(0.0);
    yields[1] = curveToBump.getInterestRate(nodeTimesExtended[1]);
    yields[2] = curveToBump.getInterestRate(nodeTimesExtended[2]);
    final YieldAndDiscountCurve curveNode = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(nodeTimesExtended, yields, new LinearInterpolator1D()));
    for (int loopnode = 0; loopnode < nbNode; loopnode++) {
      final YieldAndDiscountCurve curveBumped = curveNode.withSingleShift(nodeTimesExtended[loopnode + 1], deltaShift);
      CURVES_2.replaceCurve(CURVE_NAME_2[1], curveBumped);
      final double psBumped = FRA_METHOD.parSpread(fra, CURVES_2);
      result[loopnode] = (psBumped - ps) / deltaShift;
      final DoublesPair pairPv = sensiPsFwd.get(loopnode);
      assertEquals("Sensitivity par spread to curve: Node " + loopnode, nodeTimesExtended[loopnode + 1], pairPv.getFirst(), TOLERANCE_TIME);
      assertEquals("Sensitivity par spread to curve: Node", pairPv.second, result[loopnode], TOLERANCE_SPREAD_DELTA);
    }
    CURVES_2.replaceCurve(CURVE_NAME_2[1], curveToBump);
    InterestRateCurveSensitivity prcsCalculator = fra.accept(PSCSC, CURVES_2);
    prcsCalculator = prcsCalculator.cleaned(0.0, 1.0E-4);
    AssertSensitivityObjects.assertEquals("FRA: par rate curve sensitivity", pscsMethod, prcsCalculator, TOLERANCE_SPREAD_DELTA);
  }

}
