/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swaption;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.time.calendar.LocalDate;
import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.financial.instrument.index.CMSIndex;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.PresentValueSABRCalculator;
import com.opengamma.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.PresentValueSensitivityCalculator;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.SwapFixedIborMethod;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swaption.method.SwaptionPhysicalFixedIborSABRMethod;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameter;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.volatility.smile.function.SABRHaganAlternativeVolatilityFunction;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.tuple.DoublesPair;

public class SwaptionPhysicalFixedIborSABRMethodTest {
  // Swaption description
  private static final ZonedDateTime EXPIRY_DATE = DateUtil.getUTCDate(2014, 3, 18);
  private static final boolean IS_LONG = true;
  private static final int SETTLEMENT_DAYS = 2;
  // Swap 5Y description
  private static final Currency CUR = Currency.USD;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final int ANNUITY_TENOR_YEAR = 5;
  private static final Period ANNUITY_TENOR = Period.ofYears(ANNUITY_TENOR_YEAR);
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, BUSINESS_DAY, CALENDAR, SETTLEMENT_DAYS);
  private static final double NOTIONAL = 100000000; //100m
  //  Fixed leg: Semi-annual bond
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final AnnuityCouponFixedDefinition FIXED_ANNUITY_PAYER = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, FIXED_PAYMENT_PERIOD, CALENDAR, FIXED_DAY_COUNT,
      BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, FIXED_IS_PAYER);
  private static final AnnuityCouponFixedDefinition FIXED_ANNUITY_RECEIVER = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, FIXED_PAYMENT_PERIOD, CALENDAR, FIXED_DAY_COUNT,
      BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, !FIXED_IS_PAYER);
  //  Ibor leg: quarterly money
  private static final Period INDEX_TENOR = Period.ofMonths(3);
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final IborIndex INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  private static final AnnuityCouponIborDefinition IBOR_ANNUITY_RECEIVER = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, INDEX, !FIXED_IS_PAYER);
  private static final AnnuityCouponIborDefinition IBOR_ANNUITY_PAYER = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, INDEX, FIXED_IS_PAYER);
  // Swaption construction: All combinations
  private static final SwapFixedIborDefinition SWAP_DEFINITION_PAYER = new SwapFixedIborDefinition(FIXED_ANNUITY_PAYER, IBOR_ANNUITY_RECEIVER);
  private static final SwapFixedIborDefinition SWAP_DEFINITION_RECEIVER = new SwapFixedIborDefinition(FIXED_ANNUITY_RECEIVER, IBOR_ANNUITY_PAYER);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_DEFINITION_LONG_PAYER = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_PAYER, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_DEFINITION_LONG_RECEIVER = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_RECEIVER, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_DEFINITION_SHORT_PAYER = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_PAYER, !IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_DEFINITION_SHORT_RECEIVER = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_RECEIVER, !IS_LONG);
  // to derivatives
  private static final LocalDate REFERENCE_DATE = LocalDate.of(2008, 8, 18);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final FixedCouponSwap<Payment> SWAP_PAYER = SWAP_DEFINITION_PAYER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final FixedCouponSwap<Payment> SWAP_RECEIVER = SWAP_DEFINITION_RECEIVER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_LONG_PAYER = SWAPTION_DEFINITION_LONG_PAYER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_LONG_RECEIVER = SWAPTION_DEFINITION_LONG_RECEIVER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_SHORT_PAYER = SWAPTION_DEFINITION_SHORT_PAYER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_SHORT_RECEIVER = SWAPTION_DEFINITION_SHORT_RECEIVER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  // Calculators
  private static final PresentValueSABRCalculator PVC = PresentValueSABRCalculator.getInstance();
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  private static final PresentValueSensitivityCalculator PVSC = PresentValueSensitivityCalculator.getInstance();
  // Pricing functions
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNoSABRHaganSensi() {
    YieldCurveBundle curves = TestsDataSets.createCurves1();
    SABRInterestRateParameter sabrParameter = TestsDataSets.createSABR1(new SABRHaganAlternativeVolatilityFunction());
    SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    PVSC.visit(SWAPTION_LONG_PAYER, sabrBundle);
  }

  /**
   * Tests present value with respect to a hard-coded value. Tests against the explicit formula. Tests long/short parity and payer/receiver/swap parity.
   */
  @Test
  public void testPresentValue() {
    YieldCurveBundle curves = TestsDataSets.createCurves1();
    SABRInterestRateParameter sabrParameter = TestsDataSets.createSABR1();
    SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    // Swaption pricing.
    double priceLongPayer = PVC.visit(SWAPTION_LONG_PAYER, sabrBundle);
    double priceShortPayer = PVC.visit(SWAPTION_SHORT_PAYER, sabrBundle);
    double priceLongReceiver = PVC.visit(SWAPTION_LONG_RECEIVER, sabrBundle);
    double priceShortReceiver = PVC.visit(SWAPTION_SHORT_RECEIVER, sabrBundle);
    // From previous run
    double expectedPriceLongPayer = 4973866.250;
    assertEquals(expectedPriceLongPayer, priceLongPayer, 1E-2);
    double pvbp = SwapFixedIborMethod.presentValueBasisPoint(SWAP_PAYER, curves.getCurve(FUNDING_CURVE_NAME));
    double forward = PRC.visit(SWAP_PAYER, curves);
    double maturity = SWAP_PAYER.getFirstLeg().getNthPayment(SWAP_PAYER.getFirstLeg().getNumberOfPayments() - 1).getPaymentTime() - SWAPTION_LONG_PAYER.getSettlementTime();
    assertEquals(maturity, ANNUITY_TENOR_YEAR, 1E-2);
    double volatility = sabrParameter.getVolatility(SWAPTION_LONG_PAYER.getTimeToExpiry(), maturity, RATE, forward);
    BlackFunctionData data = new BlackFunctionData(forward, pvbp, volatility);
    Function1D<BlackFunctionData, Double> func = BLACK_FUNCTION.getPriceFunction(SWAPTION_LONG_PAYER);
    double expectedPrice = func.evaluate(data);
    assertEquals(expectedPrice, priceLongPayer, 1E-2);
    // Long/Short parity
    assertEquals(priceLongPayer, -priceShortPayer, 1E-2);
    assertEquals(priceLongReceiver, -priceShortReceiver, 1E-2);
    // Payer/Receiver parity
    double priceSwapPayer = PVC.visit(SWAP_PAYER, curves);
    double priceSwapReceiver = PVC.visit(SWAP_RECEIVER, curves);
    assertEquals(priceSwapPayer, priceLongPayer + priceShortReceiver, 1E-2);
    assertEquals(priceSwapReceiver, priceLongReceiver + priceShortPayer, 1E-2);
  }

  /**
   * Test the absence of arbitrage between swaptions with same cash-flows but different conventions.
   */
  @Test
  public void testPresentValueConventionArbitrage() {
    YieldCurveBundle curves = TestsDataSets.createCurves1();
    SABRInterestRateParameter sabrParameter = TestsDataSets.createSABR1();
    SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    double rate360 = 0.0360;
    CMSIndex index360 = new CMSIndex(FIXED_PAYMENT_PERIOD, DayCountFactory.INSTANCE.getDayCount("Actual/360"), INDEX, ANNUITY_TENOR);
    SwapFixedIborDefinition swap360 = SwapFixedIborDefinition.from(SETTLEMENT_DATE, index360, NOTIONAL, rate360, FIXED_IS_PAYER);
    SwaptionPhysicalFixedIborDefinition swaption360Definition = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swap360, IS_LONG);
    SwaptionPhysicalFixedIbor swaption360 = swaption360Definition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    double rate365 = 0.0365;
    CMSIndex index365 = new CMSIndex(FIXED_PAYMENT_PERIOD, DayCountFactory.INSTANCE.getDayCount("Actual/365"), INDEX, ANNUITY_TENOR);
    SwapFixedIborDefinition swap365 = SwapFixedIborDefinition.from(SETTLEMENT_DATE, index365, NOTIONAL, rate365, FIXED_IS_PAYER);
    SwaptionPhysicalFixedIborDefinition swaption365Definition = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swap365, IS_LONG);
    SwaptionPhysicalFixedIbor swaption365 = swaption365Definition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    double price360 = PVC.visit(swaption360, sabrBundle);
    double price365 = PVC.visit(swaption365, sabrBundle);
    assertEquals(price360, price365, 1E-2);
  }

  @Test
  public void testPresentValueSensitivity() {
    YieldCurveBundle curves = TestsDataSets.createCurves1();
    SABRInterestRateParameter sabrParameter = TestsDataSets.createSABR1();
    SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    SwaptionPhysicalFixedIborSABRMethod method = new SwaptionPhysicalFixedIborSABRMethod();
    // Swaption sensitivity
    PresentValueSensitivity pvsLongPayer = method.presentValueSensitivity(SWAPTION_LONG_PAYER, sabrBundle);
    PresentValueSensitivity pvsShortPayer = method.presentValueSensitivity(SWAPTION_SHORT_PAYER, sabrBundle);
    // Long/short parity
    PresentValueSensitivity pvsShortPayer_1 = pvsShortPayer.multiply(-1);
    assertEquals(pvsLongPayer.getSensitivity(), pvsShortPayer_1.getSensitivity());
    // PresentValueCalculator
    Map<String, List<DoublesPair>> pvscLongPayer = PVSC.visit(SWAPTION_LONG_PAYER, sabrBundle);
    assertEquals(pvsLongPayer.getSensitivity(), pvscLongPayer);
    // Present value sensitivity comparison with finite difference.
    double deltaTolerance = 1E+2; //Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move.
    final double deltaShift = 1e-9;
    PresentValueSensitivity pvsSwapPayer = new PresentValueSensitivity(PVSC.visit(SWAP_PAYER, sabrBundle));
    pvsSwapPayer = pvsSwapPayer.clean();
    PresentValueSensitivity sensi = new PresentValueSensitivity(pvscLongPayer);
    sensi = sensi.clean();
    double pv = PVC.visit(SWAPTION_LONG_PAYER, sabrBundle);
    // 1. Forward curve sensitivity
    String bumpedCurveName = "Bumped Curve";
    String[] bumpedCurvesForwardName = {FUNDING_CURVE_NAME, bumpedCurveName};
    SwaptionPhysicalFixedIbor swaptionBumpedForward = SWAPTION_DEFINITION_LONG_PAYER.toDerivative(REFERENCE_DATE, bumpedCurvesForwardName);
    final YieldAndDiscountCurve curveForward = curves.getCurve(FORWARD_CURVE_NAME);
    Set<Double> timeForwardSet = new TreeSet<Double>();
    for (Payment pay : SWAPTION_LONG_PAYER.getUnderlyingSwap().getSecondLeg().getPayments()) {
      CouponIbor coupon = (CouponIbor) pay;
      timeForwardSet.add(coupon.getFixingPeriodStartTime());
      timeForwardSet.add(coupon.getFixingPeriodEndTime());
    }
    int nbForwardDate = timeForwardSet.size();
    List<Double> timeForwardList = new ArrayList<Double>(timeForwardSet);
    Double[] timeForwardArray = new Double[nbForwardDate];
    timeForwardArray = timeForwardList.toArray(timeForwardArray);
    final double[] yieldsForward = new double[nbForwardDate + 1];
    double[] nodeTimesForward = new double[nbForwardDate + 1];
    yieldsForward[0] = curveForward.getInterestRate(0.0);
    for (int i = 0; i < nbForwardDate; i++) {
      nodeTimesForward[i + 1] = timeForwardArray[i];
      yieldsForward[i + 1] = curveForward.getInterestRate(nodeTimesForward[i + 1]);
    }
    final YieldAndDiscountCurve tempCurveForward = new YieldCurve(InterpolatedDoublesCurve.fromSorted(nodeTimesForward, yieldsForward, new LinearInterpolator1D()));
    List<DoublesPair> tempForward = sensi.getSensitivity().get(FORWARD_CURVE_NAME);
    for (int i = 0; i < nbForwardDate; i++) {
      final YieldAndDiscountCurve bumpedCurveForward = tempCurveForward.withSingleShift(nodeTimesForward[i + 1], deltaShift);
      final YieldCurveBundle curvesBumpedForward = new YieldCurveBundle();
      curvesBumpedForward.addAll(curves);
      curvesBumpedForward.setCurve("Bumped Curve", bumpedCurveForward);
      SABRInterestRateDataBundle sabrBundleBumped = new SABRInterestRateDataBundle(sabrParameter, curvesBumpedForward);
      final double bumpedpv = PVC.visit(swaptionBumpedForward, sabrBundleBumped);
      double res = (bumpedpv - pv) / deltaShift;
      final DoublesPair pair = tempForward.get(i);
      assertEquals("Node " + i, nodeTimesForward[i + 1], pair.getFirst(), 1E-8);
      assertEquals("Node " + i, res, pair.getSecond(), deltaTolerance);
    }
    // 2. Funding curve sensitivity
    String[] bumpedCurvesFundingName = {bumpedCurveName, FORWARD_CURVE_NAME};
    SwaptionPhysicalFixedIbor swaptionBumpedFunding = SWAPTION_DEFINITION_LONG_PAYER.toDerivative(REFERENCE_DATE, bumpedCurvesFundingName);
    int nbPayDate = SWAPTION_DEFINITION_LONG_PAYER.getUnderlyingSwap().getIborLeg().getPayments().length;
    final YieldAndDiscountCurve curveFunding = curves.getCurve(FUNDING_CURVE_NAME);
    final double[] yieldsFunding = new double[nbPayDate + 1];
    double[] nodeTimesFunding = new double[nbPayDate + 1];
    yieldsFunding[0] = curveFunding.getInterestRate(0.0);
    for (int i = 0; i < nbPayDate; i++) {
      nodeTimesFunding[i + 1] = SWAPTION_LONG_PAYER.getUnderlyingSwap().getSecondLeg().getNthPayment(i).getPaymentTime();
      yieldsFunding[i + 1] = curveFunding.getInterestRate(nodeTimesFunding[i + 1]);
    }
    final YieldAndDiscountCurve tempCurveFunding = new YieldCurve(InterpolatedDoublesCurve.fromSorted(nodeTimesFunding, yieldsFunding, new LinearInterpolator1D()));
    List<DoublesPair> tempFunding = sensi.getSensitivity().get(FUNDING_CURVE_NAME);
    for (int i = 0; i < nbPayDate; i++) {
      final YieldAndDiscountCurve bumpedCurve = tempCurveFunding.withSingleShift(nodeTimesFunding[i + 1], deltaShift);
      final YieldCurveBundle curvesBumped = new YieldCurveBundle();
      curvesBumped.addAll(curves);
      curvesBumped.setCurve("Bumped Curve", bumpedCurve);
      SABRInterestRateDataBundle sabrBundleBumped = new SABRInterestRateDataBundle(sabrParameter, curvesBumped);
      final double bumpedpv = PVC.visit(swaptionBumpedFunding, sabrBundleBumped);
      double res = (bumpedpv - pv) / deltaShift;
      final DoublesPair pair = tempFunding.get(i);
      assertEquals("Node " + i, nodeTimesFunding[i + 1], pair.getFirst(), 1E-8);
      assertEquals("Node " + i, res, pair.getSecond(), deltaTolerance);
    }

  }

  @Test
  public void testPresentValueSABRSensitivity() {
    YieldCurveBundle curves = TestsDataSets.createCurves1();
    SABRInterestRateParameter sabrParameter = TestsDataSets.createSABR1();
    SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    SwaptionPhysicalFixedIborSABRMethod method = new SwaptionPhysicalFixedIborSABRMethod();
    // Swaption sensitivity
    PresentValueSABRSensitivityDataBundle pvsLongPayer = method.presentValueSABRSensitivity(SWAPTION_LONG_PAYER, sabrBundle);
    PresentValueSABRSensitivityDataBundle pvsShortPayer = method.presentValueSABRSensitivity(SWAPTION_SHORT_PAYER, sabrBundle);
    // Long/short parity
    pvsShortPayer.multiply(-1.0);
    assertEquals(pvsLongPayer.getAlpha(), pvsShortPayer.getAlpha());
    // SABR sensitivity vs finite difference
    double pvLongPayer = method.presentValue(SWAPTION_LONG_PAYER, sabrBundle);
    double shift = 0.000001;
    DoublesPair expectedExpiryTenor = new DoublesPair(SWAPTION_LONG_PAYER.getTimeToExpiry(), ANNUITY_TENOR_YEAR);
    // Alpha sensitivity vs finite difference computation
    SABRInterestRateParameter sabrParameterAlphaBumped = TestsDataSets.createSABR1AlphaBumped(shift);
    SABRInterestRateDataBundle sabrBundleAlphaBumped = new SABRInterestRateDataBundle(sabrParameterAlphaBumped, curves);
    double pvLongPayerAlphaBumped = method.presentValue(SWAPTION_LONG_PAYER, sabrBundleAlphaBumped);
    double expectedAlphaSensi = (pvLongPayerAlphaBumped - pvLongPayer) / shift;
    assertEquals("Number of alpha sensitivity", pvsLongPayer.getAlpha().keySet().size(), 1);
    assertEquals("Alpha sensitivity expiry/tenor", pvsLongPayer.getAlpha().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Alpha sensitivity value", pvsLongPayer.getAlpha().get(expectedExpiryTenor), expectedAlphaSensi, 1.5E+1);
    // Rho sensitivity vs finite difference computation
    SABRInterestRateParameter sabrParameterRhoBumped = TestsDataSets.createSABR1RhoBumped(shift);
    SABRInterestRateDataBundle sabrBundleRhoBumped = new SABRInterestRateDataBundle(sabrParameterRhoBumped, curves);
    double pvLongPayerRhoBumped = method.presentValue(SWAPTION_LONG_PAYER, sabrBundleRhoBumped);
    double expectedRhoSensi = (pvLongPayerRhoBumped - pvLongPayer) / shift;
    assertEquals("Number of rho sensitivity", pvsLongPayer.getRho().keySet().size(), 1);
    assertEquals("Rho sensitivity expiry/tenor", pvsLongPayer.getRho().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Rho sensitivity value", pvsLongPayer.getRho().get(expectedExpiryTenor), expectedRhoSensi, 1.0E+0);
    // Alpha sensitivity vs finite difference computation
    SABRInterestRateParameter sabrParameterNuBumped = TestsDataSets.createSABR1NuBumped(shift);
    SABRInterestRateDataBundle sabrBundleNuBumped = new SABRInterestRateDataBundle(sabrParameterNuBumped, curves);
    double pvLongPayerNuBumped = method.presentValue(SWAPTION_LONG_PAYER, sabrBundleNuBumped);
    double expectedNuSensi = (pvLongPayerNuBumped - pvLongPayer) / shift;
    assertEquals("Number of nu sensitivity", pvsLongPayer.getNu().keySet().size(), 1);
    assertEquals("Nu sensitivity expiry/tenor", pvsLongPayer.getNu().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Nu sensitivity value", pvsLongPayer.getNu().get(expectedExpiryTenor), expectedNuSensi, 2.0E+0);
  }

  @Test(enabled = false)
  // Used only to assess performance
  public void testPerformance() {
    YieldCurveBundle curves = TestsDataSets.createCurves1();
    SABRInterestRateParameter sabrParameter = TestsDataSets.createSABR1();
    SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    SwaptionPhysicalFixedIborSABRMethod method = new SwaptionPhysicalFixedIborSABRMethod();
    long startTime, endTime;
    int nbTest = 1000;
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      PVC.visit(SWAPTION_LONG_PAYER, sabrBundle);
      method.presentValueSensitivity(SWAPTION_LONG_PAYER, sabrBundle);
      method.presentValueSABRSensitivity(SWAPTION_LONG_PAYER, sabrBundle);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " physical swaptions SABR (price+delta+vega): " + (endTime - startTime) + " ms");
    // Performance note: price+delta+vega: 04-Apr-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 150 ms for 1000 swaptions.
  }
}
