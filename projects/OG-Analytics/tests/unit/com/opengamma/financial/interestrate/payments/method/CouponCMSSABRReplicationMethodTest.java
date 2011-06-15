/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments.method;

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
import com.opengamma.financial.instrument.index.CMSIndex;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.interestrate.PresentValueCurveSensitivitySABRCalculator;
import com.opengamma.financial.interestrate.PresentValueSABRCalculator;
import com.opengamma.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.financial.interestrate.PresentValueSABRSensitivitySABRCalculator;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.payments.CouponCMS;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Tests for the pricing of CMS coupon in the SABR model and pricing by replication.
 */
public class CouponCMSSABRReplicationMethodTest {
  //Swap 5Y
  private static final Currency CUR = Currency.USD;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final int ANNUITY_TENOR_YEAR = 5;
  private static final Period ANNUITY_TENOR = Period.ofYears(ANNUITY_TENOR_YEAR);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtil.getUTCDate(2014, 3, 17);
  //Fixed leg: Semi-annual bond
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final AnnuityCouponFixedDefinition FIXED_ANNUITY = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, FIXED_PAYMENT_PERIOD, CALENDAR, FIXED_DAY_COUNT,
      BUSINESS_DAY, IS_EOM, 1.0, RATE, FIXED_IS_PAYER);
  //Ibor leg: quarterly money
  private static final Period INDEX_TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  private static final AnnuityCouponIborDefinition IBOR_ANNUITY = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, 1.0, IBOR_INDEX, !FIXED_IS_PAYER);
  // CMS coupon construction
  private static final CMSIndex CMS_INDEX = new CMSIndex(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, ANNUITY_TENOR);
  private static final SwapFixedIborDefinition SWAP_DEFINITION = new SwapFixedIborDefinition(FIXED_ANNUITY, IBOR_ANNUITY);
  private static final ZonedDateTime PAYMENT_DATE = DateUtil.getUTCDate(2014, 6, 17); // Prefixed
  private static final ZonedDateTime FIXING_DATE = ScheduleCalculator.getAdjustedDate(SETTLEMENT_DATE, BUSINESS_DAY, CALENDAR, -SETTLEMENT_DAYS);
  private static final ZonedDateTime ACCRUAL_START_DATE = SETTLEMENT_DATE;
  private static final ZonedDateTime ACCRUAL_END_DATE = PAYMENT_DATE;
  private static final DayCount PAYMENT_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final double ACCRUAL_FACTOR = PAYMENT_DAY_COUNT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 1000000; //1m
  private static final CouponCMSDefinition CMS_COUPON_RECEIVER_DEFINITION = CouponCMSDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE,
      SWAP_DEFINITION, CMS_INDEX);
  private static final CouponCMSDefinition CMS_COUPON_PAYER_DEFINITION = CouponCMSDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, -NOTIONAL, FIXING_DATE,
      SWAP_DEFINITION, CMS_INDEX);
  // to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtil.getUTCDate(2010, 8, 18);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final YieldCurveBundle CURVES = TestsDataSets.createCurves1();
  private static final SABRInterestRateParameters SABR_PARAMETER = TestsDataSets.createSABR1();
  private static final SABRInterestRateDataBundle SABR_BUNDLE = new SABRInterestRateDataBundle(SABR_PARAMETER, CURVES);
  private static final CouponCMS CMS_COUPON_RECEIVER = (CouponCMS) CMS_COUPON_RECEIVER_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final CouponCMS CMS_COUPON_PAYER = (CouponCMS) CMS_COUPON_PAYER_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  // Calculators
  private static final PresentValueSABRCalculator PVC_SABR = PresentValueSABRCalculator.getInstance();
  private static final PresentValueCurveSensitivitySABRCalculator PVCSC_SABR = PresentValueCurveSensitivitySABRCalculator.getInstance();
  private static final PresentValueSABRSensitivitySABRCalculator PVSSC_SABR = PresentValueSABRSensitivitySABRCalculator.getInstance();
  private static final CouponCMSSABRReplicationMethod METHOD = new CouponCMSSABRReplicationMethod();

  @Test
  public void testPriceReplicationPayerReceiver() {
    final YieldCurveBundle curves = TestsDataSets.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSets.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    final double priceReceiver = PVC_SABR.visit(CMS_COUPON_RECEIVER, sabrBundle);
    final double pricePayer = PVC_SABR.visit(CMS_COUPON_PAYER, sabrBundle);
    assertEquals("Payer/receiver", priceReceiver, -pricePayer);
  }

  @Test
  /**
   * Tests the method against the present value calculator.
   */
  public void presentValueMethodVsCalculator() {
    YieldCurveBundle curves = TestsDataSets.createCurves1();
    SABRInterestRateParameters sabrParameter = TestsDataSets.createSABR1();
    SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    double pvMethod = METHOD.presentValue(CMS_COUPON_PAYER, sabrBundle);
    double pvCalculator = PVC_SABR.visit(CMS_COUPON_PAYER, sabrBundle);
    assertEquals("Coupon CMS SABR: method and calculator", pvMethod, pvCalculator);
  }

  @Test
  /**
   * Tests the method against the present value curve sensitivity calculator.
   */
  public void presentValueCurveSensitivityMethodVsCalculator() {
    YieldCurveBundle curves = TestsDataSets.createCurves1();
    SABRInterestRateParameters sabrParameter = TestsDataSets.createSABR1();
    SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    PresentValueSensitivity pvsMethod = METHOD.presentValueSensitivity(CMS_COUPON_PAYER, sabrBundle);
    Map<String, List<DoublesPair>> pvsCalculator = PVCSC_SABR.visit(CMS_COUPON_PAYER, sabrBundle);
    assertEquals("Coupon CMS SABR: method and calculator", pvsMethod.getSensitivity(), pvsCalculator);
  }

  @Test
  /**
   * Test the present value sensitivity to the rates.
   */
  public void testPresentValueRateSensitivitySABRParameters() {
    final YieldCurveBundle curves = TestsDataSets.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSets.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    // Swaption sensitivity
    PresentValueSensitivity pvsReceiver = METHOD.presentValueSensitivity(CMS_COUPON_RECEIVER, sabrBundle);
    // Present value sensitivity comparison with finite difference.
    final double deltaTolerance = 1E+2; //Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move.
    final double deltaShift = 1e-9;
    pvsReceiver = pvsReceiver.clean();
    final double pv = METHOD.presentValue(CMS_COUPON_RECEIVER, sabrBundle);
    // 1. Forward curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    final String[] bumpedCurvesForwardName = {FUNDING_CURVE_NAME, bumpedCurveName};
    final CouponCMS cmsBumpedForward = (CouponCMS) CMS_COUPON_RECEIVER_DEFINITION.toDerivative(REFERENCE_DATE, bumpedCurvesForwardName);
    final YieldAndDiscountCurve curveForward = curves.getCurve(FORWARD_CURVE_NAME);
    final Set<Double> timeForwardSet = new TreeSet<Double>();
    for (final Payment pay : CMS_COUPON_RECEIVER.getUnderlyingSwap().getSecondLeg().getPayments()) {
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
    final List<DoublesPair> tempForward = pvsReceiver.getSensitivity().get(FORWARD_CURVE_NAME);
    for (int i = 0; i < nbForwardDate; i++) {
      final YieldAndDiscountCurve bumpedCurveForward = tempCurveForward.withSingleShift(nodeTimesForward[i + 1], deltaShift);
      final YieldCurveBundle curvesBumpedForward = new YieldCurveBundle();
      curvesBumpedForward.addAll(curves);
      curvesBumpedForward.setCurve("Bumped Curve", bumpedCurveForward);
      final SABRInterestRateDataBundle sabrBundleBumped = new SABRInterestRateDataBundle(sabrParameter, curvesBumpedForward);
      final double bumpedpv = PVC_SABR.visit(cmsBumpedForward, sabrBundleBumped);
      final double res = (bumpedpv - pv) / deltaShift;
      final DoublesPair pair = tempForward.get(i);
      assertEquals("Node " + i, nodeTimesForward[i + 1], pair.getFirst(), 1E-8);
      assertEquals("Node " + i, res, pair.getSecond(), deltaTolerance);
    }
    // 2. Funding curve sensitivity
    final String[] bumpedCurvesFundingName = {bumpedCurveName, FORWARD_CURVE_NAME};
    final CouponCMS cmsBumpedFunding = (CouponCMS) CMS_COUPON_RECEIVER_DEFINITION.toDerivative(REFERENCE_DATE, bumpedCurvesFundingName);
    final int nbPayDate = CMS_COUPON_RECEIVER_DEFINITION.getUnderlyingSwap().getIborLeg().getPayments().length;
    final YieldAndDiscountCurve curveFunding = curves.getCurve(FUNDING_CURVE_NAME);
    final double[] yieldsFunding = new double[nbPayDate + 1];
    final double[] nodeTimesFunding = new double[nbPayDate + 1];
    yieldsFunding[0] = curveFunding.getInterestRate(0.0);
    for (int i = 0; i < nbPayDate; i++) {
      nodeTimesFunding[i + 1] = CMS_COUPON_RECEIVER.getUnderlyingSwap().getSecondLeg().getNthPayment(i).getPaymentTime();
      yieldsFunding[i + 1] = curveFunding.getInterestRate(nodeTimesFunding[i + 1]);
    }
    final YieldAndDiscountCurve tempCurveFunding = new YieldCurve(InterpolatedDoublesCurve.fromSorted(nodeTimesFunding, yieldsFunding, new LinearInterpolator1D()));
    final List<DoublesPair> tempFunding = pvsReceiver.getSensitivity().get(FUNDING_CURVE_NAME);
    final double[] res = new double[nbPayDate];
    for (int i = 0; i < nbPayDate; i++) {
      final YieldAndDiscountCurve bumpedCurve = tempCurveFunding.withSingleShift(nodeTimesFunding[i + 1], deltaShift);
      final YieldCurveBundle curvesBumped = new YieldCurveBundle();
      curvesBumped.addAll(curves);
      curvesBumped.setCurve("Bumped Curve", bumpedCurve);
      final SABRInterestRateDataBundle sabrBundleBumped = new SABRInterestRateDataBundle(sabrParameter, curvesBumped);
      final double bumpedpv = METHOD.presentValue(cmsBumpedFunding, sabrBundleBumped);
      res[i] = (bumpedpv - pv) / deltaShift;
      final DoublesPair pair = tempFunding.get(i);
      assertEquals("Node " + i, nodeTimesFunding[i + 1], pair.getFirst(), 1E-8);
      assertEquals("Node " + i, res[i], pair.getSecond(), deltaTolerance);
    }
  }

  @Test
  /**
   * Test the present value sensitivity to the SABR parameters.
   */
  public void testPresentValueSABRSensitivitySABRParameters() {
    // Swaption sensitivity
    final PresentValueSABRSensitivityDataBundle pvsReceiver = METHOD.presentValueSABRSensitivity(CMS_COUPON_RECEIVER, SABR_BUNDLE);
    final PresentValueSABRSensitivityDataBundle pvsPayer = METHOD.presentValueSABRSensitivity(CMS_COUPON_PAYER, SABR_BUNDLE);
    // Long/short parity
    pvsPayer.multiply(-1.0);
    assertEquals(pvsPayer.getAlpha(), pvsReceiver.getAlpha());
    // SABR sensitivity vs finite difference
    final double pvLongPayer = METHOD.presentValue(CMS_COUPON_RECEIVER, SABR_BUNDLE);
    final double shift = 0.0001;
    final double shiftAlpha = 0.00001;
    final DoublesPair expectedExpiryTenor = new DoublesPair(CMS_COUPON_RECEIVER.getFixingTime(), ANNUITY_TENOR_YEAR + 1.0 / 365.0);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterAlphaBumped = TestsDataSets.createSABR1AlphaBumped(shiftAlpha);
    final SABRInterestRateDataBundle sabrBundleAlphaBumped = new SABRInterestRateDataBundle(sabrParameterAlphaBumped, CURVES);
    final double pvLongPayerAlphaBumped = METHOD.presentValue(CMS_COUPON_RECEIVER, sabrBundleAlphaBumped);
    final double expectedAlphaSensi = (pvLongPayerAlphaBumped - pvLongPayer) / shiftAlpha;
    assertEquals("Number of alpha sensitivity", pvsReceiver.getAlpha().keySet().size(), 1);
    assertEquals("Alpha sensitivity expiry/tenor", pvsReceiver.getAlpha().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Alpha sensitivity value", expectedAlphaSensi, pvsReceiver.getAlpha().get(expectedExpiryTenor), 150.0);
    // Rho sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterRhoBumped = TestsDataSets.createSABR1RhoBumped();
    final SABRInterestRateDataBundle sabrBundleRhoBumped = new SABRInterestRateDataBundle(sabrParameterRhoBumped, CURVES);
    final double pvLongPayerRhoBumped = METHOD.presentValue(CMS_COUPON_RECEIVER, sabrBundleRhoBumped);
    final double expectedRhoSensi = (pvLongPayerRhoBumped - pvLongPayer) / shift;
    assertEquals("Number of rho sensitivity", pvsReceiver.getRho().keySet().size(), 1);
    assertEquals("Rho sensitivity expiry/tenor", pvsReceiver.getRho().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Rho sensitivity value", pvsReceiver.getRho().get(expectedExpiryTenor), expectedRhoSensi, 2.0);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterNuBumped = TestsDataSets.createSABR1NuBumped();
    final SABRInterestRateDataBundle sabrBundleNuBumped = new SABRInterestRateDataBundle(sabrParameterNuBumped, CURVES);
    final double pvLongPayerNuBumped = METHOD.presentValue(CMS_COUPON_RECEIVER, sabrBundleNuBumped);
    final double expectedNuSensi = (pvLongPayerNuBumped - pvLongPayer) / shift;
    assertEquals("Number of nu sensitivity", pvsReceiver.getNu().keySet().size(), 1);
    assertEquals("Nu sensitivity expiry/tenor", pvsReceiver.getNu().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Nu sensitivity value", pvsReceiver.getNu().get(expectedExpiryTenor), expectedNuSensi, 15.0);
  }

  @Test
  /**
   * Tests the present value SABR parameters sensitivity: Method vs Calculator.
   */
  public void presentValueSABRSensitivityMethodVsCalculator() {
    final PresentValueSABRSensitivityDataBundle pvssMethod = METHOD.presentValueSABRSensitivity(CMS_COUPON_RECEIVER, SABR_BUNDLE);
    final PresentValueSABRSensitivityDataBundle pvssCalculator = PVSSC_SABR.visit(CMS_COUPON_RECEIVER, SABR_BUNDLE);
    assertEquals("CMS cap/floor SABR: Present value SABR sensitivity: method vs calculator", pvssMethod, pvssCalculator);
  }

  @Test(enabled = true)
  /**
   * Tests of performance. "enabled = false" for the standard testing.
   */
  public void testPerformance() {
    long startTime, endTime;
    final int nbTest = 100;
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      PVC_SABR.visit(CMS_COUPON_RECEIVER, SABR_BUNDLE);
      PVCSC_SABR.visit(CMS_COUPON_RECEIVER, SABR_BUNDLE);
      PVSSC_SABR.visit(CMS_COUPON_RECEIVER, SABR_BUNDLE);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " CMS swap by replication (price+delta+vega): " + (endTime - startTime) + " ms");
    // Performance note: price+delta: 15-Jun-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 84 ms for 100 coupon 5Y.
    // Performance note: price+delta+vega: 15-Jun-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 130 ms for 100 coupon 5Y.
  }
}
