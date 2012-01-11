/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments.method;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.payment.CapFloorIborDefinition;
import com.opengamma.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.PresentValueCurveSensitivitySABRExtrapolationCalculator;
import com.opengamma.financial.interestrate.PresentValueSABRExtrapolationCalculator;
import com.opengamma.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.method.SensitivityFiniteDifference;
import com.opengamma.financial.interestrate.payments.CapFloorIbor;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateExtrapolationParameters;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.option.pricing.analytic.formula.SABRExtrapolationRightFunction;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Test related to the pricing and sensitivity of the Ibor cap/floor with the SABR model and extrapolation for high strikes. 
 */
public class CapFloorIborSABRExtrapolationRightMethodTest {
  // Details
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.USD;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);
  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2011, 1, 3);
  private static final double NOTIONAL = 1000000; //1m
  private static final double STRIKE = 0.04;
  private static final double STRIKE_HIGH = 0.09;
  private static final boolean IS_CAP = true;
  // Definition description
  private static final CapFloorIborDefinition CAP_LONG_DEFINITION = CapFloorIborDefinition.from(NOTIONAL, FIXING_DATE, INDEX, STRIKE, IS_CAP);
  private static final CapFloorIborDefinition CAP_HIGH_LONG_DEFINITION = CapFloorIborDefinition.from(NOTIONAL, FIXING_DATE, INDEX, STRIKE_HIGH, IS_CAP);
  private static final CouponIborDefinition COUPON_IBOR_DEFINITION = CouponIborDefinition.from(NOTIONAL, FIXING_DATE, INDEX);
  private static final CouponFixedDefinition COUPON_STRIKE_DEFINITION = new CouponFixedDefinition(COUPON_IBOR_DEFINITION, STRIKE);
  private static final CouponFixedDefinition COUPON_STRIKE_HIGH_DEFINITION = new CouponFixedDefinition(COUPON_IBOR_DEFINITION, STRIKE_HIGH);
  private static final CapFloorIborDefinition CAP_SHORT_DEFINITION = CapFloorIborDefinition.from(-NOTIONAL, FIXING_DATE, INDEX, STRIKE, IS_CAP);
  private static final CapFloorIborDefinition CAP_HIGH_SHORT_DEFINITION = CapFloorIborDefinition.from(-NOTIONAL, FIXING_DATE, INDEX, STRIKE_HIGH, IS_CAP);
  private static final CapFloorIborDefinition FLOOR_SHORT_DEFINITION = CapFloorIborDefinition.from(-NOTIONAL, FIXING_DATE, INDEX, STRIKE, !IS_CAP);
  private static final CapFloorIborDefinition FLOOR_HIGH_SHORT_DEFINITION = CapFloorIborDefinition.from(-NOTIONAL, FIXING_DATE, INDEX, STRIKE_HIGH, !IS_CAP);
  // Methods and calculator
  private static final double CUT_OFF_STRIKE = 0.08;
  private static final double MU = 2.50;
  private static final CapFloorIborSABRExtrapolationRightMethod METHOD = new CapFloorIborSABRExtrapolationRightMethod(CUT_OFF_STRIKE, MU);
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final PresentValueCurveSensitivitySABRExtrapolationCalculator PVSC = PresentValueCurveSensitivitySABRExtrapolationCalculator.getInstance();
  // To derivative
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final CapFloorIbor CAP_LONG = (CapFloorIbor) CAP_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final CapFloorIbor CAP_HIGH_LONG = (CapFloorIbor) CAP_HIGH_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final CouponIbor COUPON_IBOR = (CouponIbor) COUPON_IBOR_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final CouponFixed COUPON_STRIKE = COUPON_STRIKE_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final CouponFixed COUPON_STRIKE_HIGH = COUPON_STRIKE_HIGH_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final CapFloorIbor CAP_SHORT = (CapFloorIbor) CAP_SHORT_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final CapFloorIbor CAP_HIGH_SHORT = (CapFloorIbor) CAP_HIGH_SHORT_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final CapFloorIbor FLOOR_SHORT = (CapFloorIbor) FLOOR_SHORT_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final CapFloorIbor FLOOR_HIGH_SHORT = (CapFloorIbor) FLOOR_HIGH_SHORT_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  // Data
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
  private static final SABRInterestRateParameters SABR_PARAMETERS = TestsDataSetsSABR.createSABR1();
  private static final SABRInterestRateDataBundle SABR_BUNDLE = new SABRInterestRateDataBundle(SABR_PARAMETERS, CURVES);

  @Test
  /**
   * Test the present value using the method with the direct formula with extrapolation.
   */
  public void presentValueBelowCutOff() {
    final CurrencyAmount methodPrice = METHOD.presentValue(CAP_LONG, SABR_BUNDLE);
    final double df = CURVES.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(CAP_LONG.getPaymentTime());
    final double forward = PRC.visit(CAP_LONG, CURVES);
    final double maturity = CAP_LONG.getFixingPeriodEndTime() - CAP_LONG.getFixingPeriodStartTime();
    DoublesPair expiryMaturity = new DoublesPair(CAP_LONG.getFixingTime(), maturity);
    double alpha = SABR_PARAMETERS.getAlpha(expiryMaturity);
    double beta = SABR_PARAMETERS.getBeta(expiryMaturity);
    double rho = SABR_PARAMETERS.getRho(expiryMaturity);
    double nu = SABR_PARAMETERS.getNu(expiryMaturity);
    SABRFormulaData sabrParam = new SABRFormulaData(alpha, beta, rho, nu);
    SABRExtrapolationRightFunction sabrExtrapolation = new SABRExtrapolationRightFunction(forward, sabrParam, CUT_OFF_STRIKE, CAP_LONG.getFixingTime(), MU);
    EuropeanVanillaOption option = new EuropeanVanillaOption(CAP_LONG.getStrike(), CAP_LONG.getFixingTime(), CAP_LONG.isCap());
    final double expectedPrice = sabrExtrapolation.price(option) * CAP_LONG.getNotional() * CAP_LONG.getPaymentYearFraction() * df;
    assertEquals("Cap/floor: SABR with extrapolation pricing", expectedPrice, methodPrice.getAmount(), 1E-2);
  }

  @Test
  /**
   * Test the present value using the method with the direct formula with extrapolation.
   */
  public void presentValueAboveCutOff() {
    CurrencyAmount methodPrice = METHOD.presentValue(CAP_HIGH_LONG, SABR_BUNDLE);
    final double df = CURVES.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(CAP_HIGH_LONG.getPaymentTime());
    final double forward = PRC.visit(CAP_HIGH_LONG, CURVES);
    final double maturity = CAP_HIGH_LONG.getFixingPeriodEndTime() - CAP_LONG.getFixingPeriodStartTime();
    DoublesPair expiryMaturity = new DoublesPair(CAP_HIGH_LONG.getFixingTime(), maturity);
    double alpha = SABR_PARAMETERS.getAlpha(expiryMaturity);
    double beta = SABR_PARAMETERS.getBeta(expiryMaturity);
    double rho = SABR_PARAMETERS.getRho(expiryMaturity);
    double nu = SABR_PARAMETERS.getNu(expiryMaturity);
    SABRFormulaData sabrParam = new SABRFormulaData(alpha, beta, rho, nu);
    SABRExtrapolationRightFunction sabrExtrapolation = new SABRExtrapolationRightFunction(forward, sabrParam, CUT_OFF_STRIKE, CAP_HIGH_LONG.getFixingTime(), MU);
    EuropeanVanillaOption option = new EuropeanVanillaOption(CAP_HIGH_LONG.getStrike(), CAP_HIGH_LONG.getFixingTime(), CAP_HIGH_LONG.isCap());
    final double expectedPrice = sabrExtrapolation.price(option) * CAP_HIGH_LONG.getNotional() * CAP_HIGH_LONG.getPaymentYearFraction() * df;
    assertEquals("Cap/floor: SABR with extrapolation pricing", expectedPrice, methodPrice.getAmount(), 1E-2);
    methodPrice = METHOD.presentValue(CAP_HIGH_LONG, SABR_BUNDLE);
    assertEquals("Cap/floor: SABR with extrapolation pricing", expectedPrice, methodPrice.getAmount(), 1E-2);
  }

  @Test
  /**
   * Test the present value using the method with the direct formula with extrapolation.
   */
  public void presentValueLongShortParityBelowCutOff() {
    final CurrencyAmount priceLong = METHOD.presentValue(CAP_LONG, SABR_BUNDLE);
    final CurrencyAmount priceShort = METHOD.presentValue(CAP_SHORT, SABR_BUNDLE);
    assertEquals("Cap/floor: SABR with extrapolation pricing: long/short parity", priceLong.getAmount(), -priceShort.getAmount(), 1E-2);
  }

  @Test
  /**
   * Test the present value using the method with the direct formula with extrapolation.
   */
  public void presentValueLongShortParityAboveCutOff() {
    final CurrencyAmount priceLong = METHOD.presentValue(CAP_HIGH_LONG, SABR_BUNDLE);
    final CurrencyAmount priceShort = METHOD.presentValue(CAP_HIGH_SHORT, SABR_BUNDLE);
    assertEquals("Cap/floor: SABR with extrapolation pricing: long/short parity", priceLong.getAmount(), -priceShort.getAmount(), 1E-2);
  }

  @Test
  /**
   * Test the cap/floor/forward parity below the cut-off strike.
   */
  public void presentValueCapFloorParityBelowCutOff() {
    final CurrencyAmount priceCap = METHOD.presentValue(CAP_LONG, SABR_BUNDLE);
    final CurrencyAmount priceFloor = METHOD.presentValue(FLOOR_SHORT, SABR_BUNDLE);
    double priceCouponStrike = PVC.visit(COUPON_STRIKE, CURVES);
    double priceCouponIbor = PVC.visit(COUPON_IBOR, CURVES);
    assertEquals("Cap/floor: SABR with extrapolation pricing: cap/floor parity", priceCouponIbor - priceCouponStrike, priceCap.getAmount() + priceFloor.getAmount(), 1E-2);
  }

  @Test
  /**
   * Test the cap/floor/forward parity above the cut-off strike.
   */
  public void presentValueCapFloorParityAboveCutOff() {
    final CurrencyAmount priceCap = METHOD.presentValue(CAP_HIGH_LONG, SABR_BUNDLE);
    final CurrencyAmount priceFloor = METHOD.presentValue(FLOOR_HIGH_SHORT, SABR_BUNDLE);
    double priceCouponStrike = PVC.visit(COUPON_STRIKE_HIGH, CURVES);
    double priceCouponIbor = PVC.visit(COUPON_IBOR, CURVES);
    assertEquals("Cap/floor: SABR with extrapolation pricing: cap/floor parity", priceCouponIbor - priceCouponStrike, priceCap.getAmount() + priceFloor.getAmount(), 1E-2);
  }

  @Test
  /**
   * Test the present value using the method with the direct formula with extrapolation.
   */
  public void presentValueMethodVsCalculator() {
    SABRInterestRateExtrapolationParameters sabrExtra = SABRInterestRateExtrapolationParameters.from(SABR_PARAMETERS, CUT_OFF_STRIKE, MU);
    SABRInterestRateDataBundle sabrExtraBundle = new SABRInterestRateDataBundle(sabrExtra, CURVES);
    CurrencyAmount pvMethod = METHOD.presentValue(CAP_LONG, SABR_BUNDLE);
    PresentValueSABRExtrapolationCalculator pvc = PresentValueSABRExtrapolationCalculator.getInstance();
    double pvCalculator = pvc.visit(CAP_LONG, sabrExtraBundle);
    assertEquals("Cap/floor: SABR with extrapolation pricing - Method vs Calculator", pvMethod.getAmount(), pvCalculator, 1E-2);
  }

  @Test
  /**
   * Test the present value rate sensitivity against a finite difference computation; strike below the cut-off strike. Test sensitivity long/short parity.
   */
  public void testPresentValueSensitivityBelowCutOff() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSetsSABR.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    InterestRateCurveSensitivity pvsCapLong = METHOD.presentValueSensitivity(CAP_LONG, sabrBundle);
    final InterestRateCurveSensitivity pvsCapShort = METHOD.presentValueSensitivity(CAP_SHORT, sabrBundle);
    // Long/short parity
    final InterestRateCurveSensitivity pvsCapShort_1 = pvsCapShort.multiply(-1);
    assertEquals(pvsCapLong.getSensitivities(), pvsCapShort_1.getSensitivities());
    // Present value sensitivity comparison with finite difference.
    final double deltaTolerancePrice = 1.0E-1;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move.
    final double deltaShift = 1.0E-7;
    pvsCapLong = pvsCapLong.clean();
    String bumpedCurveName = "Bumped Curve";
    // 1. Forward curve sensitivity
    final String[] CurveNameBumpedForward = {FUNDING_CURVE_NAME, bumpedCurveName};
    final CapFloorIbor capBumpedForward = (CapFloorIbor) CAP_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CurveNameBumpedForward);
    double[] nodeTimesForward = new double[] {capBumpedForward.getFixingPeriodStartTime(), capBumpedForward.getFixingPeriodEndTime()};
    double[] sensiForwardMethod = SensitivityFiniteDifference.curveSensitivity(capBumpedForward, SABR_BUNDLE, FORWARD_CURVE_NAME, bumpedCurveName, nodeTimesForward, deltaShift, METHOD);
    assertEquals("Sensitivity finite difference method: number of node", 2, sensiForwardMethod.length);
    List<DoublesPair> sensiPvForward = pvsCapLong.getSensitivities().get(FORWARD_CURVE_NAME);
    for (int loopnode = 0; loopnode < sensiForwardMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvForward.get(loopnode);
      assertEquals("Sensitivity cap/floor pv to forward curve: Node " + loopnode, nodeTimesForward[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, sensiForwardMethod[loopnode], deltaTolerancePrice);
    }
    // 2. Discounting curve sensitivity
    final String[] CurveNameBumpedDisc = {bumpedCurveName, FORWARD_CURVE_NAME};
    final CapFloorIbor capBumpedDisc = (CapFloorIbor) CAP_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CurveNameBumpedDisc);
    double[] nodeTimesDisc = new double[] {capBumpedDisc.getPaymentTime()};
    double[] sensiDiscMethod = SensitivityFiniteDifference.curveSensitivity(capBumpedDisc, SABR_BUNDLE, FUNDING_CURVE_NAME, bumpedCurveName, nodeTimesDisc, deltaShift, METHOD);
    assertEquals("Sensitivity finite difference method: number of node", 1, sensiDiscMethod.length);
    List<DoublesPair> sensiPvDisc = pvsCapLong.getSensitivities().get(FUNDING_CURVE_NAME);
    for (int loopnode = 0; loopnode < sensiDiscMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvDisc.get(loopnode);
      assertEquals("Sensitivity cap/floor pv to forward curve: Node " + loopnode, nodeTimesDisc[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, sensiDiscMethod[loopnode], deltaTolerancePrice);
    }
  }

  @Test
  /**
   * Test the present value rate sensitivity against a finite difference computation; strike above the cut-off strike. Test sensitivity long/short parity.
   */
  public void testPresentValueSensitivityAboveCutOff() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSetsSABR.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    InterestRateCurveSensitivity pvsCapLong = METHOD.presentValueSensitivity(CAP_HIGH_LONG, sabrBundle);
    final InterestRateCurveSensitivity pvsCapShort = METHOD.presentValueSensitivity(CAP_HIGH_SHORT, sabrBundle);
    // Long/short parity
    final InterestRateCurveSensitivity pvsCapShort_1 = pvsCapShort.multiply(-1);
    assertEquals(pvsCapLong.getSensitivities(), pvsCapShort_1.getSensitivities());
    // Present value sensitivity comparison with finite difference.
    final double deltaTolerancePrice = 1.0E-1;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move.
    final double deltaShift = 1.0E-7;
    pvsCapLong = pvsCapLong.clean();
    String bumpedCurveName = "Bumped Curve";
    // 1. Forward curve sensitivity
    final String[] CurveNameBumpedForward = {FUNDING_CURVE_NAME, bumpedCurveName};
    final CapFloorIbor capBumpedForward = (CapFloorIbor) CAP_HIGH_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CurveNameBumpedForward);
    double[] nodeTimesForward = new double[] {capBumpedForward.getFixingPeriodStartTime(), capBumpedForward.getFixingPeriodEndTime()};
    double[] sensiForwardMethod = SensitivityFiniteDifference.curveSensitivity(capBumpedForward, SABR_BUNDLE, FORWARD_CURVE_NAME, bumpedCurveName, nodeTimesForward, deltaShift, METHOD);
    assertEquals("Sensitivity finite difference method: number of node", 2, sensiForwardMethod.length);
    List<DoublesPair> sensiPvForward = pvsCapLong.getSensitivities().get(FORWARD_CURVE_NAME);
    for (int loopnode = 0; loopnode < sensiForwardMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvForward.get(loopnode);
      assertEquals("Sensitivity cap/floor pv to forward curve: Node " + loopnode, nodeTimesForward[loopnode], pairPv.getFirst(), 1E-8);
      //      assertEquals("Sensitivity finite difference method: node sensitivity: Node " + loopnode, pairPv.second, sensiForwardMethod[loopnode], deltaTolerancePrice);
    }
    // 2. Discounting curve sensitivity
    final String[] CurveNameBumpedDisc = {bumpedCurveName, FORWARD_CURVE_NAME};
    final CapFloorIbor capBumpedDisc = (CapFloorIbor) CAP_HIGH_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CurveNameBumpedDisc);
    double[] nodeTimesDisc = new double[] {capBumpedDisc.getPaymentTime()};
    double[] sensiDiscMethod = SensitivityFiniteDifference.curveSensitivity(capBumpedDisc, SABR_BUNDLE, FUNDING_CURVE_NAME, bumpedCurveName, nodeTimesDisc, deltaShift, METHOD);
    assertEquals("Sensitivity finite difference method: number of node", 1, sensiDiscMethod.length);
    List<DoublesPair> sensiPvDisc = pvsCapLong.getSensitivities().get(FUNDING_CURVE_NAME);
    for (int loopnode = 0; loopnode < sensiDiscMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvDisc.get(loopnode);
      assertEquals("Sensitivity cap/floor pv to forward curve: Node " + loopnode, nodeTimesDisc[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, sensiDiscMethod[loopnode], deltaTolerancePrice);
    }
  }

  @Test
  /**
   * Test the present value using the method with the direct formula with extrapolation.
   */
  public void presentValueCurveSensitivityMethodVsCalculator() {
    SABRInterestRateExtrapolationParameters sabrExtra = SABRInterestRateExtrapolationParameters.from(SABR_PARAMETERS, CUT_OFF_STRIKE, MU);
    SABRInterestRateDataBundle sabrExtraBundle = new SABRInterestRateDataBundle(sabrExtra, CURVES);
    InterestRateCurveSensitivity pvsMethod = METHOD.presentValueSensitivity(CAP_HIGH_LONG, SABR_BUNDLE);
    InterestRateCurveSensitivity pvsCalculator = new InterestRateCurveSensitivity(PVSC.visit(CAP_HIGH_LONG, sabrExtraBundle));
    assertEquals("Cap/floor: SABR with extrapolation pv curve sensitivity - Method vs Calculator", pvsMethod, pvsCalculator);
  }

  @Test
  /**
   * Test the present value SABR parameters sensitivity against a finite difference computation; strike below the cut-off strike.
   */
  public void testPresentValueSABRSensitivityBelowCutOff() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSetsSABR.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    final CurrencyAmount pv = METHOD.presentValue(CAP_LONG, sabrBundle);
    final PresentValueSABRSensitivityDataBundle pvsCapLong = METHOD.presentValueSABRSensitivity(CAP_LONG, sabrBundle);
    PresentValueSABRSensitivityDataBundle pvsCapShort = METHOD.presentValueSABRSensitivity(CAP_SHORT, sabrBundle);
    // Long/short parity
    pvsCapShort = PresentValueSABRSensitivityDataBundle.multiplyBy(pvsCapShort, -1.0);
    assertEquals(pvsCapShort.getAlpha(), pvsCapLong.getAlpha());
    // SABR sensitivity vs finite difference
    final double shift = 0.0001;
    final double shiftAlpha = 0.00001;
    final DoublesPair expectedExpiryTenor = new DoublesPair(CAP_LONG.getFixingTime(), CAP_LONG.getFixingPeriodEndTime() - CAP_LONG.getFixingPeriodStartTime());
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterAlphaBumped = TestsDataSetsSABR.createSABR1AlphaBumped(shiftAlpha);
    final SABRInterestRateDataBundle sabrBundleAlphaBumped = new SABRInterestRateDataBundle(sabrParameterAlphaBumped, curves);
    final CurrencyAmount pvLongPayerAlphaBumped = METHOD.presentValue(CAP_LONG, sabrBundleAlphaBumped);
    final double expectedAlphaSensi = (pvLongPayerAlphaBumped.getAmount() - pv.getAmount()) / shiftAlpha;
    assertEquals("Number of alpha sensitivity", pvsCapLong.getAlpha().getMap().keySet().size(), 1);
    assertEquals("Alpha sensitivity expiry/tenor", pvsCapLong.getAlpha().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Alpha sensitivity value", expectedAlphaSensi, pvsCapLong.getAlpha().getMap().get(expectedExpiryTenor), 2.0E-1);
    // Rho sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterRhoBumped = TestsDataSetsSABR.createSABR1RhoBumped();
    final SABRInterestRateDataBundle sabrBundleRhoBumped = new SABRInterestRateDataBundle(sabrParameterRhoBumped, curves);
    final CurrencyAmount pvLongPayerRhoBumped = METHOD.presentValue(CAP_LONG, sabrBundleRhoBumped);
    final double expectedRhoSensi = (pvLongPayerRhoBumped.getAmount() - pv.getAmount()) / shift;
    assertEquals("Number of rho sensitivity", pvsCapLong.getRho().getMap().keySet().size(), 1);
    assertEquals("Rho sensitivity expiry/tenor", pvsCapLong.getRho().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Rho sensitivity value", pvsCapLong.getRho().getMap().get(expectedExpiryTenor), expectedRhoSensi, 1.0E-2);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterNuBumped = TestsDataSetsSABR.createSABR1NuBumped();
    final SABRInterestRateDataBundle sabrBundleNuBumped = new SABRInterestRateDataBundle(sabrParameterNuBumped, curves);
    final CurrencyAmount pvLongPayerNuBumped = METHOD.presentValue(CAP_LONG, sabrBundleNuBumped);
    final double expectedNuSensi = (pvLongPayerNuBumped.getAmount() - pv.getAmount()) / shift;
    assertEquals("Number of nu sensitivity", pvsCapLong.getNu().getMap().keySet().size(), 1);
    assertEquals("Nu sensitivity expiry/tenor", pvsCapLong.getNu().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Nu sensitivity value", pvsCapLong.getNu().getMap().get(expectedExpiryTenor), expectedNuSensi, 3.0E-2);
  }

  @Test
  /**
   * Test the present value SABR parameters sensitivity against a finite difference computation; strike above the cut-off strike.
   */
  public void testPresentValueSABRSensitivityAboveCutOff() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSetsSABR.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    final CurrencyAmount pv = METHOD.presentValue(CAP_HIGH_LONG, sabrBundle);
    final PresentValueSABRSensitivityDataBundle pvsCapLong = METHOD.presentValueSABRSensitivity(CAP_HIGH_LONG, sabrBundle);
    PresentValueSABRSensitivityDataBundle pvsCapShort = METHOD.presentValueSABRSensitivity(CAP_HIGH_SHORT, sabrBundle);
    // Long/short parity
    pvsCapShort = PresentValueSABRSensitivityDataBundle.multiplyBy(pvsCapShort, -1.0);
    assertEquals(pvsCapShort.getAlpha(), pvsCapLong.getAlpha());
    // SABR sensitivity vs finite difference
    final double shift = 0.0001;
    final double shiftAlpha = 0.00001;
    final DoublesPair expectedExpiryTenor = new DoublesPair(CAP_HIGH_LONG.getFixingTime(), CAP_HIGH_LONG.getFixingPeriodEndTime() - CAP_HIGH_LONG.getFixingPeriodStartTime());
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterAlphaBumped = TestsDataSetsSABR.createSABR1AlphaBumped(shiftAlpha);
    final SABRInterestRateDataBundle sabrBundleAlphaBumped = new SABRInterestRateDataBundle(sabrParameterAlphaBumped, curves);
    final CurrencyAmount pvLongPayerAlphaBumped = METHOD.presentValue(CAP_HIGH_LONG, sabrBundleAlphaBumped);
    final double expectedAlphaSensi = (pvLongPayerAlphaBumped.getAmount() - pv.getAmount()) / shiftAlpha;
    assertEquals("Number of alpha sensitivity", pvsCapLong.getAlpha().getMap().keySet().size(), 1);
    assertEquals("Alpha sensitivity expiry/tenor", pvsCapLong.getAlpha().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Alpha sensitivity value", expectedAlphaSensi, pvsCapLong.getAlpha().getMap().get(expectedExpiryTenor), 1.0E-0);
    // Rho sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterRhoBumped = TestsDataSetsSABR.createSABR1RhoBumped();
    final SABRInterestRateDataBundle sabrBundleRhoBumped = new SABRInterestRateDataBundle(sabrParameterRhoBumped, curves);
    final CurrencyAmount pvLongPayerRhoBumped = METHOD.presentValue(CAP_HIGH_LONG, sabrBundleRhoBumped);
    final double expectedRhoSensi = (pvLongPayerRhoBumped.getAmount() - pv.getAmount()) / shift;
    assertEquals("Number of rho sensitivity", pvsCapLong.getRho().getMap().keySet().size(), 1);
    assertEquals("Rho sensitivity expiry/tenor", pvsCapLong.getRho().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Rho sensitivity value", pvsCapLong.getRho().getMap().get(expectedExpiryTenor), expectedRhoSensi, 1.0E-1);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterNuBumped = TestsDataSetsSABR.createSABR1NuBumped();
    final SABRInterestRateDataBundle sabrBundleNuBumped = new SABRInterestRateDataBundle(sabrParameterNuBumped, curves);
    final CurrencyAmount pvLongPayerNuBumped = METHOD.presentValue(CAP_HIGH_LONG, sabrBundleNuBumped);
    final double expectedNuSensi = (pvLongPayerNuBumped.getAmount() - pv.getAmount()) / shift;
    assertEquals("Number of nu sensitivity", pvsCapLong.getNu().getMap().keySet().size(), 1);
    assertEquals("Nu sensitivity expiry/tenor", pvsCapLong.getNu().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Nu sensitivity value", pvsCapLong.getNu().getMap().get(expectedExpiryTenor), expectedNuSensi, 2.0E-1);
  }

}
