/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.fra.method;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;
import java.util.Map;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.PresentValueCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.fra.ForwardRateAgreement;
import com.opengamma.financial.interestrate.method.SensitivityFiniteDifference;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Tests the ForwardRateAgreement discounting method.
 */
public class ForwardRateAgreementDiscountingMethodTest {
  // Index
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.USD;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);
  // Dates : The above dates are not standard but selected for insure correct testing.
  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2011, 1, 3);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 1, 6);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 4, 4);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 1, 7);
  private static final DayCount DAY_COUNT_PAYMENT = DayCountFactory.INSTANCE.getDayCount("Actual/365");
  private static final double ACCRUAL_FACTOR_PAYMENT = DAY_COUNT_PAYMENT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double FRA_RATE = 0.05;
  private static final double NOTIONAL = 1000000; //1m
  // Coupon with specific payment and accrual dates.
  private static final ForwardRateAgreementDefinition FRA_DEFINITION = new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL,
      FIXING_DATE, INDEX, FRA_RATE);
  // To derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 10, 9);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final ForwardRateAgreement FRA = (ForwardRateAgreement) FRA_DEFINITION.toDerivative(REFERENCE_DATE, CURVES);
  private static final ForwardRateAgreementDiscountingMethod FRA_METHOD = ForwardRateAgreementDiscountingMethod.getInstance();

  @Test
  public void parRate() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final double forward = FRA_METHOD.parRate(FRA, curves);
    final double dfForwardCurveStart = curves.getCurve(FORWARD_CURVE_NAME).getDiscountFactor(FRA.getFixingPeriodStartTime());
    final double dfForwardCurveEnd = curves.getCurve(FORWARD_CURVE_NAME).getDiscountFactor(FRA.getFixingPeriodEndTime());
    final double forwardExpected = (dfForwardCurveStart / dfForwardCurveEnd - 1) / FRA.getFixingYearFraction();
    assertEquals("FRA discounting: par rate", forwardExpected, forward, 1.0E-10);
  }

  @Test
  public void presentValue() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final double forward = FRA_METHOD.parRate(FRA, curves);
    final double dfSettle = curves.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(FRA.getPaymentTime());
    final double expectedPv = FRA.getNotional() * dfSettle * FRA.getPaymentYearFraction() * (forward - FRA_RATE) / (1 + FRA.getPaymentYearFraction() * forward);
    final CurrencyAmount pv = FRA_METHOD.presentValue(FRA, curves);
    assertEquals("FRA discounting: present value", expectedPv, pv.getAmount(), 1.0E-2);
  }

  @Test
  public void presentValueMethodVsCalculator() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final CurrencyAmount pvMethod = FRA_METHOD.presentValue(FRA, curves);
    final PresentValueCalculator calculator = PresentValueCalculator.getInstance();
    final double pvCalculator = calculator.visit(FRA, curves);
    assertEquals("FRA discounting: present value calculator vs method", pvCalculator, pvMethod.getAmount(), 1.0E-2);
  }

  @Test
  public void presentValueBuySellParity() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final ForwardRateAgreementDefinition fraDefinitionSell = new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, -NOTIONAL,
        FIXING_DATE, INDEX, FRA_RATE);
    final ForwardRateAgreement fraSell = (ForwardRateAgreement) fraDefinitionSell.toDerivative(REFERENCE_DATE, CURVES);
    final CurrencyAmount pvBuy = FRA_METHOD.presentValue(FRA, curves);
    final CurrencyAmount pvSell = FRA_METHOD.presentValue(fraSell, curves);
    assertEquals("FRA discounting: present value - buy/sell parity", pvSell.getAmount(), -pvBuy.getAmount(), 1.0E-2);
  }

  @Test
  public void sensitivity() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    // Par rate sensitivity
    final InterestRateCurveSensitivity prsFra = FRA_METHOD.parRateCurveSensitivity(FRA, curves);
    final InterestRateCurveSensitivity pvsFra = FRA_METHOD.presentValueCurveSensitivity(FRA, curves);
    prsFra.clean();
    final double deltaTolerancePrice = 1.0E+2;
    final double deltaToleranceRate = 1.0E-7;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
    final double deltaShift = 1.0E-8;
    final double forward = FRA_METHOD.parRate(FRA, curves);
    final double pv = FRA_METHOD.presentValue(FRA, curves).getAmount();
    // 1. Forward curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    final String[] bumpedCurvesForwardName = {FUNDING_CURVE_NAME, bumpedCurveName};
    final ForwardRateAgreement fraBumpedForward = (ForwardRateAgreement) FRA_DEFINITION.toDerivative(REFERENCE_DATE, bumpedCurvesForwardName);
    final YieldAndDiscountCurve curveForward = curves.getCurve(FORWARD_CURVE_NAME);
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
    final YieldAndDiscountCurve tempCurveForward = new YieldCurve(InterpolatedDoublesCurve.fromSorted(nodeTimesForward, yieldsForward, new LinearInterpolator1D()));
    final List<DoublesPair> sensiForwardForward = prsFra.getSensitivities().get(FORWARD_CURVE_NAME);
    final List<DoublesPair> sensiPvForward = pvsFra.getSensitivities().get(FORWARD_CURVE_NAME);
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
    final String[] bumpedCurvesFundingName = {bumpedCurveName, FORWARD_CURVE_NAME};
    final ForwardRateAgreement fraBumped = (ForwardRateAgreement) FRA_DEFINITION.toDerivative(REFERENCE_DATE, bumpedCurvesFundingName);
    final YieldAndDiscountCurve curveFunding = curves.getCurve(FUNDING_CURVE_NAME);
    final double[] yieldsFunding = new double[2];
    final double[] nodeTimesFunding = new double[2];
    yieldsFunding[0] = curveFunding.getInterestRate(0.0);
    nodeTimesFunding[1] = FRA.getPaymentTime();
    yieldsFunding[1] = curveFunding.getInterestRate(nodeTimesFunding[1]);
    final YieldAndDiscountCurve tempCurveFunding = new YieldCurve(InterpolatedDoublesCurve.fromSorted(nodeTimesFunding, yieldsFunding, new LinearInterpolator1D()));
    final List<DoublesPair> tempFunding = pvsFra.getSensitivities().get(FUNDING_CURVE_NAME);
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
    final String[] bumpedCurvesForwardName = {FUNDING_CURVE_NAME, bumpedCurveName};
    final ForwardRateAgreement fraBumpedForward = (ForwardRateAgreement) FRA_DEFINITION.toDerivative(REFERENCE_DATE, bumpedCurvesForwardName);
    final YieldAndDiscountCurve curveForward = curves.getCurve(FORWARD_CURVE_NAME);
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
    final YieldAndDiscountCurve tempCurveForward = new YieldCurve(InterpolatedDoublesCurve.fromSorted(nodeTimesForward, yieldsForward, new LinearInterpolator1D()));
    final double[] sensiPvForwardFD = new double[nbForwardDate];
    for (int i = 0; i < nbForwardDate; i++) {
      final YieldAndDiscountCurve bumpedCurveForward = tempCurveForward.withSingleShift(nodeTimesForward[i + 1], deltaShift);
      final YieldCurveBundle curvesBumpedForward = new YieldCurveBundle();
      curvesBumpedForward.addAll(curves);
      curvesBumpedForward.setCurve("Bumped Curve", bumpedCurveForward);
      final double bumpedPv = FRA_METHOD.presentValue(fraBumpedForward, curvesBumpedForward).getAmount();
      sensiPvForwardFD[i] = (bumpedPv - pv) / deltaShift;
    }

    final double[] nodeTimesForwardMethod = new double[] {FRA.getFixingPeriodStartTime(), FRA.getFixingPeriodEndTime()};
    final double[] sensiForwardMethod = SensitivityFiniteDifference.curveSensitivity(fraBumpedForward, curves, pv, FORWARD_CURVE_NAME, bumpedCurveName, nodeTimesForwardMethod, deltaShift, FRA_METHOD);
    assertEquals("Sensitivity finite difference method: number of node", 2, sensiForwardMethod.length);
    for (int loopnode = 0; loopnode < sensiForwardMethod.length; loopnode++) {
      assertEquals("Sensitivity finite difference method: node sensitivity", sensiPvForwardFD[loopnode], sensiForwardMethod[loopnode]);
    }

    // 2. Funding curve sensitivity
    final String[] bumpedCurvesFundingName = {bumpedCurveName, FORWARD_CURVE_NAME};
    final ForwardRateAgreement fraBumped = (ForwardRateAgreement) FRA_DEFINITION.toDerivative(REFERENCE_DATE, bumpedCurvesFundingName);
    final YieldAndDiscountCurve curveFunding = curves.getCurve(FUNDING_CURVE_NAME);
    final double[] yieldsFunding = new double[2];
    final double[] nodeTimesFunding = new double[2];
    yieldsFunding[0] = curveFunding.getInterestRate(0.0);
    nodeTimesFunding[1] = FRA.getPaymentTime();
    yieldsFunding[1] = curveFunding.getInterestRate(nodeTimesFunding[1]);
    final YieldAndDiscountCurve tempCurveFunding = new YieldCurve(InterpolatedDoublesCurve.fromSorted(nodeTimesFunding, yieldsFunding, new LinearInterpolator1D()));
    final YieldAndDiscountCurve bumpedCurve = tempCurveFunding.withSingleShift(nodeTimesFunding[1], deltaShift);
    final YieldCurveBundle curvesBumped = new YieldCurveBundle();
    curvesBumped.addAll(curves);
    curvesBumped.replaceCurve("Bumped Curve", bumpedCurve);
    final double bumpedPvDsc = FRA_METHOD.presentValue(fraBumped, curvesBumped).getAmount();
    final double[] resDsc = new double[1];
    resDsc[0] = (bumpedPvDsc - pv) / deltaShift;

    final double[] nodeTimesFundingMethod = new double[] {FRA.getPaymentTime()};
    final double[] sensiFundingMethod = SensitivityFiniteDifference.curveSensitivity(fraBumped, curves, pv, FUNDING_CURVE_NAME, bumpedCurveName, nodeTimesFundingMethod, deltaShift, FRA_METHOD);
    assertEquals("Sensitivity finite difference method: number of node", 1, sensiFundingMethod.length);
    for (int loopnode = 0; loopnode < sensiFundingMethod.length; loopnode++) {
      assertEquals("Sensitivity finite difference method: node sensitivity", resDsc[loopnode], sensiFundingMethod[loopnode]);
    }
  }

  @Test
  public void presentValueSensitivityMethodVsCalculator() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final InterestRateCurveSensitivity pvcsMethod = FRA_METHOD.presentValueCurveSensitivity(FRA, curves);
    final PresentValueCurveSensitivityCalculator calculator = PresentValueCurveSensitivityCalculator.getInstance();
    final Map<String, List<DoublesPair>> pvcsCalculator = calculator.visit(FRA, curves);
    assertEquals("FRA discounting: present value calculator vs method", pvcsCalculator, pvcsMethod.getSensitivities());
  }

}
