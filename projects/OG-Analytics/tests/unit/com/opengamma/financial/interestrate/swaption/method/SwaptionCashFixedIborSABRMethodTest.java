/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swaption.method;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.PresentValueCurveSensitivitySABRCalculator;
import com.opengamma.financial.interestrate.PresentValueSABRCalculator;
import com.opengamma.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.financial.interestrate.PresentValueSABRSensitivitySABRCalculator;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.PresentValueSensitivityCalculator;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.financial.interestrate.swap.SwapFixedIborMethod;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swaption.SwaptionCashFixedIbor;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameters;
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

/**
 * Class to test the present value and present value rate sensitivity of the cash-settled European swaption in the SABR model. 
 */
public class SwaptionCashFixedIborSABRMethodTest {
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
  private static final SwaptionCashFixedIborDefinition SWAPTION_DEFINITION_LONG_PAYER = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_PAYER, IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_DEFINITION_LONG_RECEIVER = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_RECEIVER, IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_DEFINITION_SHORT_PAYER = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_PAYER, !IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_DEFINITION_SHORT_RECEIVER = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_RECEIVER, !IS_LONG);
  // to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtil.getUTCDate(2008, 8, 18);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final FixedCouponSwap<Payment> SWAP_PAYER = SWAP_DEFINITION_PAYER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final YieldCurveBundle CURVES = TestsDataSets.createCurves1();
  private static final SABRInterestRateParameters SABR_PARAMETER = TestsDataSets.createSABR1();
  private static final SABRInterestRateDataBundle SABR_BUNDLE = new SABRInterestRateDataBundle(SABR_PARAMETER, CURVES);
  private static final SwaptionCashFixedIbor SWAPTION_LONG_PAYER = SWAPTION_DEFINITION_LONG_PAYER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionCashFixedIbor SWAPTION_LONG_RECEIVER = SWAPTION_DEFINITION_LONG_RECEIVER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionCashFixedIbor SWAPTION_SHORT_PAYER = SWAPTION_DEFINITION_SHORT_PAYER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionCashFixedIbor SWAPTION_SHORT_RECEIVER = SWAPTION_DEFINITION_SHORT_RECEIVER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  // Calculators
  private static final PresentValueSABRCalculator PVC = PresentValueSABRCalculator.getInstance();
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  private static final PresentValueSensitivityCalculator PVSC = PresentValueSensitivityCalculator.getInstance();
  private static final PresentValueCurveSensitivitySABRCalculator PVCSC_SABR = PresentValueCurveSensitivitySABRCalculator.getInstance();
  private static final PresentValueSABRSensitivitySABRCalculator PVSSC_SABR = PresentValueSABRSensitivitySABRCalculator.getInstance();
  private static final SwaptionCashFixedIborSABRMethod METHOD = SwaptionCashFixedIborSABRMethod.getInstance();
  // Pricing functions
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  @Test
  public void testPresentValueSABRParameters() {
    final YieldCurveBundle curves = TestsDataSets.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSets.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    // Swaption pricing.
    final double priceLongPayer = PVC.visit(SWAPTION_LONG_PAYER, sabrBundle);
    final double priceShortPayer = PVC.visit(SWAPTION_SHORT_PAYER, sabrBundle);
    final double priceLongReceiver = PVC.visit(SWAPTION_LONG_RECEIVER, sabrBundle);
    final double priceShortReceiver = PVC.visit(SWAPTION_SHORT_RECEIVER, sabrBundle);
    // From previous run
    final double expectedPriceLongPayer = 5107666.869;
    assertEquals(expectedPriceLongPayer, priceLongPayer, 1E-2);
    final double forward = PRC.visit(SWAP_PAYER, curves);
    final double pvbp = SwapFixedIborMethod.getAnnuityCash(SWAP_PAYER, forward);
    final double maturity = SWAP_PAYER.getFirstLeg().getNthPayment(SWAP_PAYER.getFirstLeg().getNumberOfPayments() - 1).getPaymentTime() - SWAPTION_LONG_PAYER.getSettlementTime();
    assertEquals(maturity, ANNUITY_TENOR_YEAR, 1E-2);
    final double volatility = sabrParameter.getVolatility(SWAPTION_LONG_PAYER.getTimeToExpiry(), maturity, RATE, forward);
    final BlackFunctionData data = new BlackFunctionData(forward, 1.0, volatility);
    final Function1D<BlackFunctionData, Double> func = BLACK_FUNCTION.getPriceFunction(SWAPTION_LONG_PAYER);
    final double df = curves.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(SWAPTION_LONG_PAYER.getSettlementTime());
    final double expectedPrice = df * pvbp * func.evaluate(data);
    assertEquals(expectedPrice, priceLongPayer, 1E-2);
    // Long/Short parity
    assertEquals(priceLongPayer, -priceShortPayer, 1E-2);
    assertEquals(priceLongReceiver, -priceShortReceiver, 1E-2);
    // No payer/Receiver parity for cash-settled swaptions.
  }

  @Test
  /**
   * Test the present value calculator with an array of derivative: one for the premium payment and one for the actual swaption.
   */
  public void presentValueWithPremium() {
    final YieldCurveBundle curves = TestsDataSets.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSets.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    final double expectedPriceLongPayer = 5107666.869;
    final double premiumAmount = expectedPriceLongPayer / curves.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(SWAPTION_LONG_PAYER.getSettlementTime());
    final PaymentFixedDefinition premiumDefinition = new PaymentFixedDefinition(CUR, SETTLEMENT_DATE, -premiumAmount);
    final PaymentFixed premium = premiumDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final InterestRateDerivative[] totalSwaption = new InterestRateDerivative[] {premium, SWAPTION_LONG_PAYER};
    final Double[] presentValue = PVC.visit(totalSwaption, sabrBundle);
    assertEquals("swaption present value with premium", -expectedPriceLongPayer, presentValue[0], 1.0E-2);
    assertEquals("swaption present value with premium", expectedPriceLongPayer, presentValue[1], 1.0E-2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNoSABRHaganSensi() {
    final YieldCurveBundle curves = TestsDataSets.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSets.createSABR1(new SABRHaganAlternativeVolatilityFunction());
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    PVCSC_SABR.visit(SWAPTION_LONG_PAYER, sabrBundle);
  }

  @Test
  public void testPresentValueSensitivitySABRParameters() {
    final YieldCurveBundle curves = TestsDataSets.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSets.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    final SwaptionCashFixedIborSABRMethod method = SwaptionCashFixedIborSABRMethod.getInstance();
    // Swaption sensitivity
    final PresentValueSensitivity pvsLongPayer = method.presentValueSensitivity(SWAPTION_LONG_PAYER, sabrBundle);
    final PresentValueSensitivity pvsShortPayer = method.presentValueSensitivity(SWAPTION_SHORT_PAYER, sabrBundle);
    // Long/short parity
    final PresentValueSensitivity pvsShortPayer_1 = pvsShortPayer.multiply(-1);
    assertEquals(pvsLongPayer.getSensitivity(), pvsShortPayer_1.getSensitivity());
    // PresentValueCalculator
    final Map<String, List<DoublesPair>> pvscLongPayer = PVCSC_SABR.visit(SWAPTION_LONG_PAYER, sabrBundle);
    assertEquals(pvsLongPayer.getSensitivity(), pvscLongPayer);
    // Present value sensitivity comparison with finite difference.
    final double deltaTolerance = 1E+2; //Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move.
    final double deltaShift = 1e-9;
    PresentValueSensitivity pvsSwapPayer = new PresentValueSensitivity(PVSC.visit(SWAP_PAYER, sabrBundle));
    pvsSwapPayer = pvsSwapPayer.clean();
    PresentValueSensitivity sensi = new PresentValueSensitivity(pvscLongPayer);
    sensi = sensi.clean();
    final double pv = PVC.visit(SWAPTION_LONG_PAYER, sabrBundle);
    // 1. Forward curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    final String[] bumpedCurvesForwardName = {FUNDING_CURVE_NAME, bumpedCurveName};
    final SwaptionCashFixedIbor swaptionBumpedForward = SWAPTION_DEFINITION_LONG_PAYER.toDerivative(REFERENCE_DATE, bumpedCurvesForwardName);
    final YieldAndDiscountCurve curveForward = curves.getCurve(FORWARD_CURVE_NAME);
    final Set<Double> timeForwardSet = new TreeSet<Double>();
    for (final Payment pay : SWAPTION_LONG_PAYER.getUnderlyingSwap().getSecondLeg().getPayments()) {
      final CouponIbor coupon = (CouponIbor) pay;
      timeForwardSet.add(coupon.getFixingPeriodStartTime());
      timeForwardSet.add(coupon.getFixingPeriodEndTime());
    }
    final int nbForwardDate = timeForwardSet.size();
    final List<Double> timeForwardList = new ArrayList<Double>(timeForwardSet);
    Double[] timeForwardArray = new Double[nbForwardDate];
    timeForwardArray = timeForwardList.toArray(timeForwardArray);
    final double[] yieldsForward = new double[nbForwardDate + 1];
    final double[] nodeTimesForward = new double[nbForwardDate + 1];
    yieldsForward[0] = curveForward.getInterestRate(0.0);
    for (int i = 0; i < nbForwardDate; i++) {
      nodeTimesForward[i + 1] = timeForwardArray[i];
      yieldsForward[i + 1] = curveForward.getInterestRate(nodeTimesForward[i + 1]);
    }
    final YieldAndDiscountCurve tempCurveForward = new YieldCurve(InterpolatedDoublesCurve.fromSorted(nodeTimesForward, yieldsForward, new LinearInterpolator1D()));
    final List<DoublesPair> tempForward = sensi.getSensitivity().get(FORWARD_CURVE_NAME);
    for (int i = 0; i < nbForwardDate; i++) {
      final YieldAndDiscountCurve bumpedCurveForward = tempCurveForward.withSingleShift(nodeTimesForward[i + 1], deltaShift);
      final YieldCurveBundle curvesBumpedForward = new YieldCurveBundle();
      curvesBumpedForward.addAll(curves);
      curvesBumpedForward.setCurve("Bumped Curve", bumpedCurveForward);
      final SABRInterestRateDataBundle sabrBundleBumped = new SABRInterestRateDataBundle(sabrParameter, curvesBumpedForward);
      final double bumpedpv = PVC.visit(swaptionBumpedForward, sabrBundleBumped);
      final double res = (bumpedpv - pv) / deltaShift;
      final DoublesPair pair = tempForward.get(i);
      assertEquals("Node " + i, nodeTimesForward[i + 1], pair.getFirst(), 1E-8);
      assertEquals("Node " + i, res, pair.getSecond(), deltaTolerance);
    }
    // 2. Funding curve sensitivity
    final String[] bumpedCurvesFundingName = {bumpedCurveName, FORWARD_CURVE_NAME};
    final SwaptionCashFixedIbor swaptionBumpedFunding = SWAPTION_DEFINITION_LONG_PAYER.toDerivative(REFERENCE_DATE, bumpedCurvesFundingName);
    final int nbPayDate = SWAPTION_DEFINITION_LONG_PAYER.getUnderlyingSwap().getIborLeg().getPayments().length;
    final YieldAndDiscountCurve curveFunding = curves.getCurve(FUNDING_CURVE_NAME);
    final double[] yieldsFunding = new double[nbPayDate + 2];
    final double[] nodeTimesFunding = new double[nbPayDate + 2];
    yieldsFunding[0] = curveFunding.getInterestRate(0.0);
    nodeTimesFunding[1] = SWAPTION_LONG_PAYER.getSettlementTime();
    yieldsFunding[1] = curveFunding.getInterestRate(nodeTimesFunding[1]);
    for (int i = 0; i < nbPayDate; i++) {
      nodeTimesFunding[i + 2] = SWAPTION_LONG_PAYER.getUnderlyingSwap().getSecondLeg().getNthPayment(i).getPaymentTime();
      yieldsFunding[i + 2] = curveFunding.getInterestRate(nodeTimesFunding[i + 1]);
    }
    final YieldAndDiscountCurve tempCurveFunding = new YieldCurve(InterpolatedDoublesCurve.fromSorted(nodeTimesFunding, yieldsFunding, new LinearInterpolator1D()));
    final List<DoublesPair> tempFunding = sensi.getSensitivity().get(FUNDING_CURVE_NAME);
    for (int i = 0; i < nbPayDate; i++) {
      final YieldAndDiscountCurve bumpedCurve = tempCurveFunding.withSingleShift(nodeTimesFunding[i + 1], deltaShift);
      final YieldCurveBundle curvesBumped = new YieldCurveBundle();
      curvesBumped.addAll(curves);
      curvesBumped.setCurve("Bumped Curve", bumpedCurve);
      final SABRInterestRateDataBundle sabrBundleBumped = new SABRInterestRateDataBundle(sabrParameter, curvesBumped);
      final double bumpedpv = PVC.visit(swaptionBumpedFunding, sabrBundleBumped);
      final double res = (bumpedpv - pv) / deltaShift;
      final DoublesPair pair = tempFunding.get(i);
      assertEquals("Node " + i, nodeTimesFunding[i + 1], pair.getFirst(), 1E-8);
      assertEquals("Node " + i, res, pair.getSecond(), deltaTolerance);
    }
  }

  @Test
  public void testPresentValueSABRSensitivitySABRParameters() {
    // Swaption sensitivity
    final PresentValueSABRSensitivityDataBundle pvsLongPayer = METHOD.presentValueSABRSensitivity(SWAPTION_LONG_PAYER, SABR_BUNDLE);
    final PresentValueSABRSensitivityDataBundle pvsShortPayer = METHOD.presentValueSABRSensitivity(SWAPTION_SHORT_PAYER, SABR_BUNDLE);
    // Long/short parity
    pvsShortPayer.multiply(-1.0);
    assertEquals(pvsLongPayer.getAlpha(), pvsShortPayer.getAlpha());
    // SABR sensitivity vs finite difference
    final double pvLongPayer = METHOD.presentValue(SWAPTION_LONG_PAYER, SABR_BUNDLE);
    final double shift = 0.0001;
    final DoublesPair expectedExpiryTenor = new DoublesPair(SWAPTION_LONG_PAYER.getTimeToExpiry(), ANNUITY_TENOR_YEAR);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterAlphaBumped = TestsDataSets.createSABR1AlphaBumped();
    final SABRInterestRateDataBundle sabrBundleAlphaBumped = new SABRInterestRateDataBundle(sabrParameterAlphaBumped, CURVES);
    final double pvLongPayerAlphaBumped = METHOD.presentValue(SWAPTION_LONG_PAYER, sabrBundleAlphaBumped);
    final double expectedAlphaSensi = (pvLongPayerAlphaBumped - pvLongPayer) / shift;
    assertEquals("Number of alpha sensitivity", pvsLongPayer.getAlpha().keySet().size(), 1);
    assertEquals("Alpha sensitivity expiry/tenor", pvsLongPayer.getAlpha().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Alpha sensitivity value", pvsLongPayer.getAlpha().get(expectedExpiryTenor), expectedAlphaSensi, 1E+4);
    // Rho sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterRhoBumped = TestsDataSets.createSABR1RhoBumped();
    final SABRInterestRateDataBundle sabrBundleRhoBumped = new SABRInterestRateDataBundle(sabrParameterRhoBumped, CURVES);
    final double pvLongPayerRhoBumped = METHOD.presentValue(SWAPTION_LONG_PAYER, sabrBundleRhoBumped);
    final double expectedRhoSensi = (pvLongPayerRhoBumped - pvLongPayer) / shift;
    assertEquals("Number of rho sensitivity", pvsLongPayer.getRho().keySet().size(), 1);
    assertEquals("Rho sensitivity expiry/tenor", pvsLongPayer.getRho().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Rho sensitivity value", pvsLongPayer.getRho().get(expectedExpiryTenor), expectedRhoSensi, 1E+3);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterNuBumped = TestsDataSets.createSABR1NuBumped();
    final SABRInterestRateDataBundle sabrBundleNuBumped = new SABRInterestRateDataBundle(sabrParameterNuBumped, CURVES);
    final double pvLongPayerNuBumped = METHOD.presentValue(SWAPTION_LONG_PAYER, sabrBundleNuBumped);
    final double expectedNuSensi = (pvLongPayerNuBumped - pvLongPayer) / shift;
    assertEquals("Number of nu sensitivity", pvsLongPayer.getNu().keySet().size(), 1);
    assertEquals("Nu sensitivity expiry/tenor", pvsLongPayer.getNu().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Nu sensitivity value", pvsLongPayer.getNu().get(expectedExpiryTenor), expectedNuSensi, 1E+3);
  }

  @Test
  /**
   * Tests the present value SABR parameters sensitivity: Method vs Calculator.
   */
  public void presentValueSABRSensitivityMethodVsCalculator() {
    final PresentValueSABRSensitivityDataBundle pvssMethod = METHOD.presentValueSABRSensitivity(SWAPTION_LONG_PAYER, SABR_BUNDLE);
    final PresentValueSABRSensitivityDataBundle pvssCalculator = PVSSC_SABR.visit(SWAPTION_LONG_PAYER, SABR_BUNDLE);
    assertEquals("Swaption Cash SABR: Present value SABR sensitivity: method vs calculator", pvssMethod, pvssCalculator);
  }

  @Test(enabled = false)
  /**
   * Test of performance. In normal testing, "enabled = false".
   */
  public void testPerformance() {
    long startTime, endTime;
    final int nbTest = 1000;
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      PVC.visit(SWAPTION_LONG_PAYER, SABR_BUNDLE);
      PVCSC_SABR.visit(SWAPTION_LONG_PAYER, SABR_BUNDLE);
      PVSSC_SABR.visit(SWAPTION_LONG_PAYER, SABR_BUNDLE);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " cash swaptions SABR (price+delta+vega): " + (endTime - startTime) + " ms");
    // Performance note: price+delta+vega: 15-Jun-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 190 ms for 1000 swaptions.
  }

}
