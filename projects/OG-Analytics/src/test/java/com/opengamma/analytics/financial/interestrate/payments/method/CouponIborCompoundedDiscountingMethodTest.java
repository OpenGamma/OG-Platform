/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.method;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.ArrayUtils;
import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.calculator.PresentValueMCACalculator;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingDefinition;
import com.opengamma.analytics.financial.interestrate.FDCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivityUtils;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityIRSCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompounding;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Tests related to the pricing and sensitivities of Ibor compounded coupon in the discounting method.
 * 
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class CouponIborCompoundedDiscountingMethodTest {

  private static final Calendar TOR = new MondayToFridayCalendar("TOR");
  private static final IborIndex CDOR3M = IndexIborMaster.getInstance().getIndex("CADCDOR3M");
  private static final Period M6 = Period.ofMonths(6);
  private static final double NOTIONAL = 123000000;
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2012, 8, 24);
  private static final CouponIborCompoundingDefinition CPN_DEFINITION = CouponIborCompoundingDefinition.from(NOTIONAL, START_DATE, M6, CDOR3M, TOR);

  private static final CouponIborCompoundedDiscountingMethod METHOD_COMPOUNDED = CouponIborCompoundedDiscountingMethod.getInstance();

  private static final YieldCurveBundle CURVES_BUNDLE = TestsDataSetsSABR.createCurves1();
  private static final String[] CURVES_NAMES = TestsDataSetsSABR.curves1Names();

  private static final ZonedDateTime REFERENCE_DATE_BEFORE = DateUtils.getUTCDate(2012, 8, 7);
  private static final CouponIborCompounding CPN_BEFORE = CPN_DEFINITION.toDerivative(REFERENCE_DATE_BEFORE, CURVES_NAMES);

  private static final double[] FIXING_RATES = new double[] {0.0010, 0.0011, 0.0012 };
  private static final DoubleTimeSeries<ZonedDateTime> FIXING_TS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {DateUtils.getUTCDate(2012, 8, 23), DateUtils.getUTCDate(2012, 8, 24),
          DateUtils.getUTCDate(2012, 9, 20) }, FIXING_RATES);
  private static final ZonedDateTime REFERENCE_DATE_1 = DateUtils.getUTCDate(2012, 8, 28);
  private static final CouponIborCompounding CPN_1 = (CouponIborCompounding) CPN_DEFINITION.toDerivative(REFERENCE_DATE_1, FIXING_TS, CURVES_NAMES);

  private static final PresentValueMCACalculator PVC_MCA = PresentValueMCACalculator.getInstance();
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final PresentValueCurveSensitivityIRSCalculator PVCSC = PresentValueCurveSensitivityIRSCalculator.getInstance();
  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_SENSI = 1.0E-2;
  private static final double TOLERANCE_SENSI_2 = 1.0E-2;

  @Test
  public void presentValueBeforeFirstFixing() {
    final CurrencyAmount pvComputed = METHOD_COMPOUNDED.presentValue(CPN_BEFORE, CURVES_BUNDLE);
    double notionalAccrued = CPN_BEFORE.getNotional();
    final int nbSub = CPN_BEFORE.getFixingTimes().length;
    for (int loopsub = 0; loopsub < nbSub; loopsub++) {
      notionalAccrued *= CURVES_BUNDLE.getCurve(CURVES_NAMES[1]).getDiscountFactor(CPN_BEFORE.getFixingPeriodStartTimes()[loopsub])
          / CURVES_BUNDLE.getCurve(CURVES_NAMES[1]).getDiscountFactor(CPN_BEFORE.getFixingPeriodEndTimes()[loopsub]);
    }
    final double dfPayment = CURVES_BUNDLE.getCurve(CURVES_NAMES[0]).getDiscountFactor(CPN_BEFORE.getPaymentTime());
    final double pvExpected = (notionalAccrued - NOTIONAL) * dfPayment;
    assertEquals("CouponIborCompoundedDiscounting: Present value", pvExpected, pvComputed.getAmount(), TOLERANCE_PV);
  }

  @Test
  public void presentValueMethodVsCalculator() {
    final CurrencyAmount pvMethod = METHOD_COMPOUNDED.presentValue(CPN_BEFORE, CURVES_BUNDLE);
    final MultipleCurrencyAmount pvCalculatorMCA = CPN_BEFORE.accept(PVC_MCA, CURVES_BUNDLE);
    assertEquals("CouponIborCompoundedDiscounting: Present value", pvMethod.getAmount(), pvCalculatorMCA.getAmount(CDOR3M.getCurrency()), TOLERANCE_PV);
    final Double pvCalculator = CPN_BEFORE.accept(PVC, CURVES_BUNDLE);
    assertEquals("CouponIborCompoundedDiscounting: Present value", pvMethod.getAmount(), pvCalculator, TOLERANCE_PV);

  }

  @Test
  public void presentValueAfter1Fixing() {
    final CurrencyAmount pvComputed = METHOD_COMPOUNDED.presentValue(CPN_1, CURVES_BUNDLE);
    double accruedNotional = (1.0 + CPN_DEFINITION.getPaymentAccrualFactors()[0] * FIXING_RATES[1]) * NOTIONAL;
    accruedNotional *= CURVES_BUNDLE.getCurve(CURVES_NAMES[1]).getDiscountFactor(CPN_1.getFixingPeriodStartTimes()[0])
        / CURVES_BUNDLE.getCurve(CURVES_NAMES[1]).getDiscountFactor(CPN_1.getFixingPeriodEndTimes()[0]);
    final double dfPayment = CURVES_BUNDLE.getCurve(CURVES_NAMES[0]).getDiscountFactor(CPN_1.getPaymentTime());
    final double pvExpected = (accruedNotional - NOTIONAL) * dfPayment;
    assertEquals("CouponIborCompoundedDiscounting: Present value", pvExpected, pvComputed.getAmount(), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value curve sensitivity vs a finite difference calculation.
   */
  public void parSpreadCurveSensitivity() {
    InterestRateCurveSensitivity pscsComputed = METHOD_COMPOUNDED.presentValueCurveSensitivity(CPN_BEFORE, CURVES_BUNDLE);
    pscsComputed = pscsComputed.cleaned();
    assertEquals("CouponIborCompounded Discounting: present value curve sensitivity", 2, pscsComputed.getSensitivities().size()); // 2 curves
    assertEquals("CouponIborCompounded Discounting: present value curve sensitivity", 1, pscsComputed.getSensitivities().get(CURVES_NAMES[0]).size()); // 1 discounting
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 0.01 unit for a 1 bp move.
    final double deltaShift = 1.0E-6;
    // Credit curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    final CouponIborCompounding cpnBumped = CPN_DEFINITION.toDerivative(REFERENCE_DATE_BEFORE, CURVES_NAMES[0], bumpedCurveName);
    final double[] nodeTimesDsc = new double[] {cpnBumped.getPaymentTime() };
    final List<DoublesPair> sensiDscFD = FDCurveSensitivityCalculator.curveSensitvityFDCalculator(CPN_BEFORE, PVC, CURVES_BUNDLE, CURVES_NAMES[0], nodeTimesDsc, deltaShift);
    final List<DoublesPair> sensiDscComputed = pscsComputed.getSensitivities().get(CURVES_NAMES[0]);
    assertTrue("parSpread: curve sensitivity - dsc", InterestRateCurveSensitivityUtils.compare(sensiDscFD, sensiDscComputed, TOLERANCE_SENSI_2));
    final Set<Double> nodeTimesFwdSet = new TreeSet<>();
    final int nbSub = CPN_BEFORE.getFixingTimes().length;
    nodeTimesFwdSet.add(CPN_BEFORE.getFixingPeriodStartTimes()[0]);
    for (int loopsub = 1; loopsub < nbSub; loopsub++) {
      nodeTimesFwdSet.add(CPN_BEFORE.getFixingPeriodEndTimes()[loopsub - 1]);
      nodeTimesFwdSet.add(CPN_BEFORE.getFixingPeriodStartTimes()[loopsub]);
    }
    nodeTimesFwdSet.add(CPN_BEFORE.getFixingPeriodEndTimes()[nbSub - 1]);
    final double[] nodeTimesFwd = ArrayUtils.toPrimitive(nodeTimesFwdSet.toArray(new Double[nodeTimesFwdSet.size()]));
    final List<DoublesPair> sensiFwdFD = FDCurveSensitivityCalculator.curveSensitvityFDCalculator(CPN_BEFORE, PVC, CURVES_BUNDLE, CURVES_NAMES[1], nodeTimesFwd, deltaShift);
    final List<DoublesPair> sensiFwdComputed = pscsComputed.getSensitivities().get(CURVES_NAMES[1]);
    assertTrue("parSpread: curve sensitivity - fwd", InterestRateCurveSensitivityUtils.compare(sensiFwdFD, sensiFwdComputed, TOLERANCE_SENSI_2 * 10));
  }

  @Test
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final InterestRateCurveSensitivity pvcsMethod = METHOD_COMPOUNDED.presentValueCurveSensitivity(CPN_BEFORE, CURVES_BUNDLE);
    final InterestRateCurveSensitivity pvcsCalculator = PVCSC.visit(CPN_BEFORE, CURVES_BUNDLE);
    AssertSensivityObjects.assertEquals("CouponIborCompoundedDiscounting: Present value curve sensitivity", pvcsMethod, pvcsCalculator, TOLERANCE_SENSI);
  }

}
