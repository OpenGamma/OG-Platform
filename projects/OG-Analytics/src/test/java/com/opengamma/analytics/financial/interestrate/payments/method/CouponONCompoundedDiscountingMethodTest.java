/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.method;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.payment.CouponONCompoundedDefinition;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.method.SensitivityFiniteDifference;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONCompounded;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Tests related to the pricing methods for ON Compounded coupon in the discounting method.
 */
@Test(groups = TestGroup.UNIT)
public class CouponONCompoundedDiscountingMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  @SuppressWarnings("deprecation")
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
  private static final String[] CURVES_NAMES = CURVES.getAllNames().toArray(new String[CURVES.size()]);

  private static final Calendar TARGET = new MondayToFridayCalendar("TARGET");
  private static final GeneratorSwapFixedON GENERATOR_SWAP_EONIA = GeneratorSwapFixedONMaster.getInstance().getGenerator("EUR1YEONIA", TARGET);
  private static final IndexON INDEX_ON = MulticurveProviderDiscountDataSets.getIndexesON()[2];
  private static final Currency EUR = INDEX_ON.getCurrency();
  private static final Calendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();
  private static final ZonedDateTime EFFECTIVE_DATE = DateUtils.getUTCDate(2011, 5, 23);
  private static final Period TENOR = Period.ofMonths(3);
  private static final double NOTIONAL = 100000000; // 100m
  private static final CouponONCompoundedDefinition CPN_ON_COMPOUNDED_DEFINITION = CouponONCompoundedDefinition.from(INDEX_ON, EFFECTIVE_DATE, TENOR, NOTIONAL, 2,
      GENERATOR_SWAP_EONIA.getBusinessDayConvention(),
      GENERATOR_SWAP_EONIA.isEndOfMonth(), CALENDAR);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 12, 27);
  private static final CouponONCompounded CPN_ON_COMPOUNDED = CPN_ON_COMPOUNDED_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAMES);

  @SuppressWarnings("deprecation")
  private static final CouponONCompoundedDiscountingMethod METHOD_CPN_ON = CouponONCompoundedDiscountingMethod.getInstance();
  private static final PresentValueCalculator PVDC = PresentValueCalculator.getInstance();
  private static final PresentValueCurveSensitivityCalculator PVCSDC = PresentValueCurveSensitivityCalculator.getInstance();

  private static final double TOLERANCE_PV_DELTA = 1.0E+2; //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move.

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_DELTA = 1.0E+2;

  @Test
  public void presentValue() {
    @SuppressWarnings("deprecation")
    final CurrencyAmount pvComputed = METHOD_CPN_ON.presentValue(CPN_ON_COMPOUNDED, CURVES);

    double ratio = 1.0;
    double forwardRatei;
    final YieldAndDiscountCurve forwardCurve = CURVES.getCurve(CPN_ON_COMPOUNDED.getForwardCurveName());
    for (int i = 0; i < CPN_ON_COMPOUNDED.getFixingPeriodAccrualFactors().length; i++) {
      forwardRatei = (forwardCurve.getDiscountFactor(CPN_ON_COMPOUNDED.getFixingPeriodStartTimes()[i]) / forwardCurve.getDiscountFactor(CPN_ON_COMPOUNDED.getFixingPeriodEndTimes()[i]) - 1.0d) /
          CPN_ON_COMPOUNDED.getFixingPeriodAccrualFactors()[i];
      ratio *= Math.pow(1 + forwardRatei, CPN_ON_COMPOUNDED.getFixingPeriodAccrualFactors()[i]);
    }
    final double df = CURVES.getCurve(CPN_ON_COMPOUNDED.getFundingCurveName()).getDiscountFactor(CPN_ON_COMPOUNDED.getPaymentTime());
    final double pvExpected = df * CPN_ON_COMPOUNDED.getNotionalAccrued() * ratio;
    assertEquals("CouponONCompoundedDiscountingMethod: present value", pvExpected, pvComputed.getAmount(), TOLERANCE_PV);
  }

  @Test
  public void presentValueStarted() {
    final double fixing = 0.0015;
    final ZonedDateTimeDoubleTimeSeries TS_ON = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 5, 20), DateUtils.getUTCDate(2011, 5, 23) }, new double[] {
      0.0010, fixing });
    final ZonedDateTime referenceDate = ScheduleCalculator.getAdjustedDate(EFFECTIVE_DATE, 1, TARGET);
    final CouponONCompounded cpnONCompoundedStarted = (CouponONCompounded) CPN_ON_COMPOUNDED_DEFINITION.toDerivative(referenceDate, TS_ON, CURVES_NAMES);
    final double notionalAccrued = NOTIONAL * Math.pow(1 + fixing, CPN_ON_COMPOUNDED_DEFINITION.getFixingPeriodAccrualFactors()[0]);
    assertEquals("CouponONCompoundedDiscountingMethod: present value", notionalAccrued, cpnONCompoundedStarted.getNotionalAccrued(), TOLERANCE_PV);
    final CurrencyAmount pvComputed = METHOD_CPN_ON.presentValue(cpnONCompoundedStarted, CURVES);
    double ratio = 1.0;
    double forwardRatei;
    final YieldAndDiscountCurve forwardCurve = CURVES.getCurve(cpnONCompoundedStarted.getForwardCurveName());
    for (int i = 0; i < cpnONCompoundedStarted.getFixingPeriodAccrualFactors().length; i++) {
      forwardRatei = (forwardCurve.getDiscountFactor(cpnONCompoundedStarted.getFixingPeriodStartTimes()[i]) / forwardCurve.getDiscountFactor(cpnONCompoundedStarted.getFixingPeriodEndTimes()[i]) - 1.0d) /
          cpnONCompoundedStarted.getFixingPeriodAccrualFactors()[i];
      ratio *= Math.pow(1 + forwardRatei, cpnONCompoundedStarted.getFixingPeriodAccrualFactors()[i]);
    }
    final double df = CURVES.getCurve(cpnONCompoundedStarted.getFundingCurveName()).getDiscountFactor(cpnONCompoundedStarted.getPaymentTime());
    final double pvExpected = cpnONCompoundedStarted.getNotionalAccrued() * ratio * df;
    assertEquals("CouponONCompoundedDiscountingMethod: present value", pvExpected, pvComputed.getAmount(), TOLERANCE_PV);
  }

  @Test
  public void presentValueMethodVsCalculator() {
    final CurrencyAmount pvMethod = METHOD_CPN_ON.presentValue(CPN_ON_COMPOUNDED, CURVES);
    final Double pvCalculator = CPN_ON_COMPOUNDED.accept(PVDC, CURVES);
    assertEquals("CouponONCompoundedDiscountingMethod: present value", pvMethod.getAmount(), pvCalculator, TOLERANCE_PV);
  }

  @Test
  /* 
   * Tests present value curve sensitivity when the valuation date is on trade date.
   */
  public void presentValueCurveSensitivity() {
    final InterestRateCurveSensitivity pvMethod = METHOD_CPN_ON.presentValueCurveSensitivity(CPN_ON_COMPOUNDED, CURVES);
    final double deltaTolerancePrice = 1.0E+2;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
    final double deltaShift = 1.0E-6;
    //    // 1. Forward curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    final Payment couponBumpedForward = CPN_ON_COMPOUNDED_DEFINITION.toDerivative(REFERENCE_DATE, new String[] {CURVES_NAMES[0], bumpedCurveName });
    final double[] nodeTimesForward = new double[CPN_ON_COMPOUNDED.getFixingPeriodStartTimes().length + 1];
    for (int i = 0; i < CPN_ON_COMPOUNDED.getFixingPeriodStartTimes().length; i++) {
      nodeTimesForward[i] = CPN_ON_COMPOUNDED.getFixingPeriodStartTimes()[i];
    }

    nodeTimesForward[CPN_ON_COMPOUNDED.getFixingPeriodStartTimes().length] = CPN_ON_COMPOUNDED.getFixingPeriodEndTimes()[CPN_ON_COMPOUNDED.getFixingPeriodAccrualFactors().length - 1];

    final double[] sensiForwardMethod = SensitivityFiniteDifference.curveSensitivity(couponBumpedForward, CURVES, CURVES_NAMES[1], bumpedCurveName, nodeTimesForward, deltaShift, METHOD_CPN_ON);
    AssertJUnit.assertEquals("Sensitivity finite difference method: number of node", 67, sensiForwardMethod.length);
    final List<DoublesPair> sensiPvForward = pvMethod.getSensitivities().get(CURVES_NAMES[1]);
    final List<DoublesPair> sensiPvDiscount = pvMethod.getSensitivities().get(CURVES_NAMES[0]);
    for (int loopnode = 0; loopnode < sensiForwardMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvForward.get(loopnode);
      assertEquals("Sensitivity coupon pv to forward curve: Node " + loopnode, nodeTimesForward[loopnode], pairPv.getFirst(), 1E-8);
      AssertJUnit.assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, sensiForwardMethod[loopnode], deltaTolerancePrice);

    }

  }
}
