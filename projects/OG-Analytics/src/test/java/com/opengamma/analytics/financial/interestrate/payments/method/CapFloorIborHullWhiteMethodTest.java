/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.method;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import cern.jet.random.engine.MersenneTwister;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CapFloorIborDefinition;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.method.SensitivityFiniteDifference;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.analytics.financial.model.interestrate.TestsDataSetHullWhite;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.montecarlo.HullWhiteMonteCarloMethod;
import com.opengamma.analytics.math.random.NormalRandomNumberGenerator;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
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
 * Tests on the Hull-White one factor method to price Cap/Floor on Ibor.
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class CapFloorIborHullWhiteMethodTest {
  // Cap/floor description
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.EUR;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);
  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2011, 1, 3);
  private static final double NOTIONAL = 100000000; //100m
  private static final double STRIKE = 0.04;
  private static final boolean IS_CAP = true;
  private static final CapFloorIborDefinition CAP_LONG_DEFINITION = CapFloorIborDefinition.from(NOTIONAL, FIXING_DATE, INDEX, STRIKE, IS_CAP, CALENDAR);
  private static final CapFloorIborDefinition CAP_SHORT_DEFINITION = CapFloorIborDefinition.from(-NOTIONAL, FIXING_DATE, INDEX, STRIKE, IS_CAP, CALENDAR);
  private static final CapFloorIborDefinition PUT_LONG_DEFINITION = CapFloorIborDefinition.from(NOTIONAL, FIXING_DATE, INDEX, STRIKE, !IS_CAP, CALENDAR);
  // To derivative
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final String[] CURVES_NAME = TestsDataSetsSABR.curves1Names();
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
  private static final CapFloorIbor CAP_LONG = (CapFloorIbor) CAP_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final CapFloorIbor CAP_SHORT = (CapFloorIbor) CAP_SHORT_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final CapFloorIbor PUT_LONG = (CapFloorIbor) PUT_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);

  private static final CapFloorIborHullWhiteMethod METHOD_HW = new CapFloorIborHullWhiteMethod();
  private static final HullWhiteOneFactorPiecewiseConstantInterestRateModel MODEL = new HullWhiteOneFactorPiecewiseConstantInterestRateModel();
  private static final HullWhiteOneFactorPiecewiseConstantParameters PARAMETERS_HW = TestsDataSetHullWhite.createHullWhiteParameters();
  private static final HullWhiteOneFactorPiecewiseConstantDataBundle BUNDLE_HW = new HullWhiteOneFactorPiecewiseConstantDataBundle(PARAMETERS_HW, CURVES);
  private static final int NB_PATH = 12500;
  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_DELTA = 1.0E+2; // 0.01 currency unit for 1bp

  @Test
  public void presentValueStandard() {
    final double tp = CAP_LONG.getPaymentTime();
    final double t0 = CAP_LONG.getFixingPeriodStartTime();
    final double t1 = CAP_LONG.getFixingPeriodEndTime();
    final double theta = CAP_LONG.getFixingTime();
    final double deltaF = CAP_LONG.getFixingAccrualFactor();
    final double deltaP = CAP_LONG.getPaymentYearFraction();
    final double alpha0 = MODEL.alpha(PARAMETERS_HW, 0.0, theta, tp, t0);
    final double alpha1 = MODEL.alpha(PARAMETERS_HW, 0.0, theta, tp, t1);
    final double ptp = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(tp);
    final double pt0 = CURVES.getCurve(CURVES_NAME[1]).getDiscountFactor(t0);
    final double pt1 = CURVES.getCurve(CURVES_NAME[1]).getDiscountFactor(t1);
    double kappa = Math.log((1.0 + deltaF * STRIKE) * pt1 / pt0);
    kappa += -(alpha1 * alpha1 - alpha0 * alpha0) / 2.0;
    kappa /= alpha1 - alpha0;
    final ProbabilityDistribution<Double> normal = new NormalDistribution(0, 1);
    double priceExpected = pt0 / pt1 * normal.getCDF(-kappa - alpha0) - (1.0 + deltaF * STRIKE) * normal.getCDF(-kappa - alpha1);
    priceExpected *= deltaP / deltaF * ptp;
    priceExpected *= NOTIONAL;
    final CurrencyAmount priceMethod = METHOD_HW.presentValue(CAP_LONG, BUNDLE_HW);
    assertEquals("Cap/floor: Hull-White pricing", priceExpected, priceMethod.getAmount(), TOLERANCE_PV);
    assertEquals("Cap/floor: Hull-White pricing", CUR, priceMethod.getCurrency());
  }

  //TODO: present value in arrears

  @Test
  public void presentValueLongShort() {
    final CurrencyAmount priceLong = METHOD_HW.presentValue(CAP_LONG, BUNDLE_HW);
    final CurrencyAmount priceShort = METHOD_HW.presentValue(CAP_SHORT, BUNDLE_HW);
    assertEquals("Cap/floor: Hull-White pricing", priceLong.getAmount(), -priceShort.getAmount(), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the price curve sensitivity of cap/floor Ibor with Hull-White. Values are tested against finite difference values.
   */
  public void presentValueCurveSensitivityCap() {
    InterestRateCurveSensitivity pvcsCap = METHOD_HW.presentValueCurveSensitivity(CAP_LONG, BUNDLE_HW);
    pvcsCap = pvcsCap.cleaned();
    final double deltaShift = 1.0E-6;
    final String bumpedCurveName = "Bumped Curve";
    // 1. Forward curve sensitivity
    final String[] CurveNameBumpedForward = {CURVES_NAME[0], bumpedCurveName};
    final CapFloorIbor capBumpedForward = (CapFloorIbor) CAP_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CurveNameBumpedForward);
    final double[] nodeTimesForward = new double[] {CAP_LONG.getFixingPeriodStartTime(), CAP_LONG.getFixingPeriodEndTime()};
    final double[] sensiForwardMethod = SensitivityFiniteDifference.curveSensitivity(capBumpedForward, BUNDLE_HW, CURVES_NAME[1], bumpedCurveName, nodeTimesForward, deltaShift, METHOD_HW);
    final List<DoublesPair> sensiPvForward = pvcsCap.getSensitivities().get(CURVES_NAME[1]);
    for (int loopnode = 0; loopnode < sensiForwardMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvForward.get(loopnode);
      assertEquals("Sensitivity cap/floor Ibor pv to forward curve with HW: Node " + loopnode, nodeTimesForward[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity " + loopnode + " - Difference " + (sensiForwardMethod[loopnode] - pairPv.second), sensiForwardMethod[loopnode],
          pairPv.second, TOLERANCE_DELTA);
    }
    // 2. Discounting curve sensitivity
    final String[] CurveNameBumpedDisc = {bumpedCurveName, CURVES_NAME[1]};
    final CapFloorIbor capBumpedDisc = (CapFloorIbor) CAP_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CurveNameBumpedDisc);
    final double[] nodeTimesDisc = new double[] {CAP_LONG.getPaymentTime()};
    final double[] sensiDiscMethod = SensitivityFiniteDifference.curveSensitivity(capBumpedDisc, BUNDLE_HW, CURVES_NAME[0], bumpedCurveName, nodeTimesDisc, deltaShift, METHOD_HW);
    final List<DoublesPair> sensiPvDisc = pvcsCap.getSensitivities().get(CURVES_NAME[0]);
    for (int loopnode = 0; loopnode < sensiDiscMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvDisc.get(loopnode);
      assertEquals("Sensitivity cap/floor Ibor pv to forward curve with HW: Node " + loopnode, nodeTimesDisc[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity " + loopnode, sensiDiscMethod[loopnode], pairPv.second, TOLERANCE_DELTA);
    }
  }

  @Test
  /**
   * Tests the Hull-White parameters sensitivity.
   */
  public void presentValueHullWhiteSensitivity() {
    presentValueHullWhiteSensitivityInstrument(CAP_LONG);
    presentValueHullWhiteSensitivityInstrument(CAP_SHORT);
    presentValueHullWhiteSensitivityInstrument(PUT_LONG);
  }

  private void presentValueHullWhiteSensitivityInstrument(final CapFloorIbor instrument) {
    final double[] hwSensitivity = METHOD_HW.presentValueHullWhiteSensitivity(instrument, BUNDLE_HW);
    final int nbVolatility = PARAMETERS_HW.getVolatility().length;
    final double shiftVol = 1.0E-6;
    final double[] volatilityBumped = new double[nbVolatility];
    System.arraycopy(PARAMETERS_HW.getVolatility(), 0, volatilityBumped, 0, nbVolatility);
    final double[] volatilityTime = new double[nbVolatility - 1];
    System.arraycopy(PARAMETERS_HW.getVolatilityTime(), 1, volatilityTime, 0, nbVolatility - 1);
    final double[] pvBumpedPlus = new double[nbVolatility];
    final double[] pvBumpedMinus = new double[nbVolatility];
    final HullWhiteOneFactorPiecewiseConstantParameters parametersBumped = new HullWhiteOneFactorPiecewiseConstantParameters(PARAMETERS_HW.getMeanReversion(), volatilityBumped, volatilityTime);
    final HullWhiteOneFactorPiecewiseConstantDataBundle bundleBumped = new HullWhiteOneFactorPiecewiseConstantDataBundle(parametersBumped, CURVES);
    final double[] hwSensitivityExpected = new double[hwSensitivity.length];
    for (int loopvol = 0; loopvol < nbVolatility; loopvol++) {
      volatilityBumped[loopvol] += shiftVol;
      parametersBumped.setVolatility(volatilityBumped);
      pvBumpedPlus[loopvol] = METHOD_HW.presentValue(instrument, bundleBumped).getAmount();
      volatilityBumped[loopvol] -= 2 * shiftVol;
      parametersBumped.setVolatility(volatilityBumped);
      pvBumpedMinus[loopvol] = METHOD_HW.presentValue(instrument, bundleBumped).getAmount();
      hwSensitivityExpected[loopvol] = (pvBumpedPlus[loopvol] - pvBumpedMinus[loopvol]) / (2 * shiftVol);
      assertEquals("Cap/floor Ibor - Hull-White sensitivity adjoint: derivative " + loopvol + " - difference:" + (hwSensitivityExpected[loopvol] - hwSensitivity[loopvol]),
          hwSensitivityExpected[loopvol], hwSensitivity[loopvol], 1.0E-0);
      volatilityBumped[loopvol] = PARAMETERS_HW.getVolatility()[loopvol];
    }
  }

  @Test(enabled = true)
  /**
   * Compare explicit formula with Monte-Carlo and long/short and payer/receiver parities.
   */
  public void monteCarlo() {
    HullWhiteMonteCarloMethod methodMC;
    methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), 10 * NB_PATH);
    // Seed fixed to the DEFAULT_SEED for testing purposes.
    final CurrencyAmount pvExplicit = METHOD_HW.presentValue(CAP_LONG, BUNDLE_HW);
    final CurrencyAmount pvMC = methodMC.presentValue(CAP_LONG, CUR, CURVES_NAME[0], BUNDLE_HW);
    assertEquals("Cap/floor - Hull-White - Monte Carlo", pvExplicit.getAmount(), pvMC.getAmount(), 4.0E+2);
    final double pvMCPreviousRun = 150060.593;
    assertEquals("Swaption physical - Hull-White - Monte Carlo", pvMCPreviousRun, pvMC.getAmount(), 1.0E-2);
    methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), 10 * NB_PATH);
    final CurrencyAmount pvShortMC = methodMC.presentValue(CAP_SHORT, CUR, CURVES_NAME[0], BUNDLE_HW);
    assertEquals("Swaption physical - Hull-White - Monte Carlo", -pvMC.getAmount(), pvShortMC.getAmount(), 1.0E-2);
  }

  @Test(enabled = false)
  /**
   * Performance for a high number of paths.
   */
  public void performance() {
    long startTime, endTime;
    final CurrencyAmount pvExplicit = METHOD_HW.presentValue(CAP_LONG, BUNDLE_HW);
    HullWhiteMonteCarloMethod methodMC;
    final int nbPath = 1000000;
    methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), nbPath);
    final int nbTest = 10;
    final double[] pv = new double[nbTest];
    final double[] pvDiff = new double[nbTest];
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pv[looptest] = methodMC.presentValue(CAP_LONG, CUR, CURVES_NAME[0], BUNDLE_HW).getAmount();
      pvDiff[looptest] = pv[looptest] - pvExplicit.getAmount();
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " pv cap/floor Hull-White MC method (" + nbPath + " paths): " + (endTime - startTime) + " ms");
    // Performance note: price: 12-Jun-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 2400 ms for 10 cap with 1,000,000 paths.
  }

}
