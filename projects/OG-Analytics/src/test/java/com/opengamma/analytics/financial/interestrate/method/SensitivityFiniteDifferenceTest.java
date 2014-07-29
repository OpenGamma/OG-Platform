/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.method;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.fra.method.ForwardRateAgreementDiscountingBundleMethod;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.differentiation.FiniteDifferenceType;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests finite difference computation of curve sensitivity.
 * @deprecated This class tests deprecated functionality
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class SensitivityFiniteDifferenceTest {
  // Index
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.EUR;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM, "Ibor");
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
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2009, 8, 18);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final ForwardRateAgreement FRA = (ForwardRateAgreement) FRA_DEFINITION.toDerivative(REFERENCE_DATE, CURVES);
  private static final ForwardRateAgreementDiscountingBundleMethod FRA_METHOD = ForwardRateAgreementDiscountingBundleMethod.getInstance();

  @Test
  public void curveSensitivityFRA() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
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
    final YieldAndDiscountCurve tempCurveFunding = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(nodeTimesFunding, yieldsFunding, new LinearInterpolator1D()));
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
  public void curveSensitivityCentered() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final double deltaShift = 1.0E-8;
    final double pv = FRA_METHOD.presentValue(FRA, curves).getAmount();
    // 1. Forward curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    final String[] bumpedCurvesForwardName = {FUNDING_CURVE_NAME, bumpedCurveName};
    final ForwardRateAgreement fraBumpedForward = (ForwardRateAgreement) FRA_DEFINITION.toDerivative(REFERENCE_DATE, bumpedCurvesForwardName);
    final double[] nodeTimesForwardMethod = new double[] {FRA.getFixingPeriodStartTime(), FRA.getFixingPeriodEndTime()};
    final double[] sensiForward = SensitivityFiniteDifference.curveSensitivity(fraBumpedForward, curves, pv, FORWARD_CURVE_NAME, bumpedCurveName, nodeTimesForwardMethod, deltaShift, FRA_METHOD);
    final double[] sensiForwardCentered = SensitivityFiniteDifference.curveSensitivity(fraBumpedForward, curves, pv, FORWARD_CURVE_NAME, bumpedCurveName, nodeTimesForwardMethod, deltaShift,
        FRA_METHOD, FiniteDifferenceType.CENTRAL);
    final double[] sensiForwardCentered2 = SensitivityFiniteDifference.curveSensitivity(fraBumpedForward, curves, FORWARD_CURVE_NAME, bumpedCurveName, nodeTimesForwardMethod, deltaShift, FRA_METHOD);
    final double[] sensiForwardForward = SensitivityFiniteDifference.curveSensitivity(fraBumpedForward, curves, pv, FORWARD_CURVE_NAME, bumpedCurveName, nodeTimesForwardMethod, deltaShift,
        FRA_METHOD, FiniteDifferenceType.FORWARD);
    final double[] sensiForwardBackward = SensitivityFiniteDifference.curveSensitivity(fraBumpedForward, curves, pv, FORWARD_CURVE_NAME, bumpedCurveName, nodeTimesForwardMethod, deltaShift,
        FRA_METHOD, FiniteDifferenceType.BACKWARD);
    assertEquals("Sensitivity finite difference method: number of node", 2, sensiForward.length);
    assertEquals("Sensitivity finite difference method: number of node", 2, sensiForwardCentered.length);
    assertEquals("Sensitivity finite difference method: number of node", 2, sensiForwardCentered2.length);
    assertEquals("Sensitivity finite difference method: number of node", 2, sensiForwardForward.length);
    assertEquals("Sensitivity finite difference method: number of node", 2, sensiForwardBackward.length);
    for (int loopnode = 0; loopnode < sensiForward.length; loopnode++) {
      assertEquals("Sensitivity finite difference method: centered vs non-centered", sensiForward[loopnode], sensiForwardForward[loopnode], 1.0E-10);
      assertEquals("Sensitivity finite difference method: centered vs non-centered", sensiForwardForward[loopnode], sensiForwardCentered[loopnode], 1.0E-1);
      assertEquals("Sensitivity finite difference method: centered vs non-centered", sensiForwardBackward[loopnode], sensiForwardCentered[loopnode], 1.0E-1);
      assertEquals("Sensitivity finite difference method: centered vs non-centered", sensiForwardCentered[loopnode], sensiForwardCentered2[loopnode], 1.0E-10);
    }
  }

}
