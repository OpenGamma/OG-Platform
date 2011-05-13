/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swaption;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.List;
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
import com.opengamma.financial.instrument.index.CMSIndex;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.financial.interestrate.PresentValueSABRCalculator;
import com.opengamma.financial.interestrate.PresentValueSABRExtrapolationCalculator;
import com.opengamma.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swaption.method.SwaptionPhysicalFixedIborSABRExtrapolationRightMethod;
import com.opengamma.financial.interestrate.swaption.method.SwaptionPhysicalFixedIborSABRMethod;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameter;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class to test the present value and present value rate sensitivity of the physical delivery swaption in the SABR with extrapolation method. 
 * The SABR smile is extrapolated above a certain cut-off strike.
 */
public class SwaptionPhysicalFixedIborSABRExtrapolationRightMethodTest {
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
  private static final double RATE = 0.04;
  private static final boolean FIXED_IS_PAYER = true;
  //  Ibor leg: quarterly money
  private static final Period INDEX_TENOR = Period.ofMonths(3);
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  // Swaption construction
  private static final CMSIndex CMS_INDEX = new CMSIndex(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, ANNUITY_TENOR);
  private static final SwapFixedIborDefinition SWAP_DEFINITION_PAYER = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER);
  private static final SwapFixedIborDefinition SWAP_DEFINITION_RECEIVER = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, !FIXED_IS_PAYER);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_DEFINITION_LONG_PAYER = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_PAYER, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_DEFINITION_LONG_RECEIVER = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_RECEIVER, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_DEFINITION_SHORT_PAYER = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_PAYER, !IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_DEFINITION_SHORT_RECEIVER = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_RECEIVER, !IS_LONG);
  // to derivatives
  private static final LocalDate REFERENCE_DATE = LocalDate.of(2008, 8, 18);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final SwaptionPhysicalFixedIbor SWAPTION_LONG_PAYER = SWAPTION_DEFINITION_LONG_PAYER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_LONG_RECEIVER = SWAPTION_DEFINITION_LONG_RECEIVER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_SHORT_PAYER = SWAPTION_DEFINITION_SHORT_PAYER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_SHORT_RECEIVER = SWAPTION_DEFINITION_SHORT_RECEIVER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  // Extrapolation
  private static final double CUT_OFF_STRIKE = 0.08;
  private static final double MU = 10.0;
  private static final SwaptionPhysicalFixedIborSABRExtrapolationRightMethod METHOD_EXTRAPOLATION = new SwaptionPhysicalFixedIborSABRExtrapolationRightMethod(CUT_OFF_STRIKE, MU);
  // Calculators
  private static final PresentValueSABRExtrapolationCalculator PVC = PresentValueSABRExtrapolationCalculator.getInstance();
  private static final PresentValueSABRCalculator PVC_NO_EXTRA = PresentValueSABRCalculator.getInstance();

  /**
   * Tests present value in the region where there is no extrapolation. Tests long/short parity.
   */
  @Test
  public void testPresentValueNoExtra() {
    YieldCurveBundle curves = TestsDataSets.createCurves1();
    SABRInterestRateParameter sabrParameter = TestsDataSets.createSABR1();
    SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    double priceLongPayer = METHOD_EXTRAPOLATION.presentValue(SWAPTION_LONG_PAYER, sabrBundle);
    double priceShortPayer = METHOD_EXTRAPOLATION.presentValue(SWAPTION_SHORT_PAYER, sabrBundle);
    double priceLongReceiver = METHOD_EXTRAPOLATION.presentValue(SWAPTION_LONG_RECEIVER, sabrBundle);
    double priceShortReceiver = METHOD_EXTRAPOLATION.presentValue(SWAPTION_SHORT_RECEIVER, sabrBundle);
    double priceLongPayerNoExtra = PVC_NO_EXTRA.visit(SWAPTION_LONG_PAYER, sabrBundle);
    double priceShortPayerNoExtra = PVC_NO_EXTRA.visit(SWAPTION_SHORT_PAYER, sabrBundle);
    double priceLongReceiverNoExtra = PVC_NO_EXTRA.visit(SWAPTION_LONG_RECEIVER, sabrBundle);
    double priceShortReceiverNoExtra = PVC_NO_EXTRA.visit(SWAPTION_SHORT_RECEIVER, sabrBundle);
    assertEquals("Swaption SABR extrapolation: below cut-off strike", priceLongPayerNoExtra, priceLongPayer, 1E-2);
    assertEquals("Swaption SABR extrapolation: below cut-off strike", priceShortPayerNoExtra, priceShortPayer, 1E-2);
    assertEquals("Swaption SABR extrapolation: below cut-off strike", priceLongReceiverNoExtra, priceLongReceiver, 1E-2);
    assertEquals("Swaption SABR extrapolation: below cut-off strike", priceShortReceiverNoExtra, priceShortReceiver, 1E-2);
    assertEquals("Swaption SABR extrapolation: below cut-off strike long/short parity", priceLongPayer, -priceShortPayer, 1E-2);
    assertEquals("Swaption SABR extrapolation: below cut-off strike long/short parity", priceLongReceiver, -priceShortReceiver, 1E-2);
  }

  /**
   * Tests present value at the limit of extrapolation. Tests long/short parity.
   */
  @Test
  public void testPresentValueLimit() {
    YieldCurveBundle curves = TestsDataSets.createCurves1();
    SABRInterestRateParameter sabrParameter = TestsDataSets.createSABR1();
    SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    double highStrike = 0.0801;
    SwapFixedIborDefinition swapPayerHighStrike = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, highStrike, FIXED_IS_PAYER);
    SwapFixedIborDefinition swapReceiverHighStrike = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, highStrike, !FIXED_IS_PAYER);
    SwaptionPhysicalFixedIborDefinition swaptionDefinitionLongPayerHighStrike = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapPayerHighStrike, IS_LONG);
    SwaptionPhysicalFixedIborDefinition swaptionDefinitionShortPayerHighStrike = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapPayerHighStrike, !IS_LONG);
    SwaptionPhysicalFixedIborDefinition swaptionDefinitionLongReceiverHighStrike = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapReceiverHighStrike, IS_LONG);
    SwaptionPhysicalFixedIbor swaptionLongPayerHighStrike = swaptionDefinitionLongPayerHighStrike.toDerivative(REFERENCE_DATE, CURVES_NAME);
    SwaptionPhysicalFixedIbor swaptionShortPayerHighStrike = swaptionDefinitionShortPayerHighStrike.toDerivative(REFERENCE_DATE, CURVES_NAME);
    SwaptionPhysicalFixedIbor swaptionLongReceiverHighStrike = swaptionDefinitionLongReceiverHighStrike.toDerivative(REFERENCE_DATE, CURVES_NAME);
    double priceLongPayer = METHOD_EXTRAPOLATION.presentValue(swaptionLongPayerHighStrike, sabrBundle);
    double priceShortPayer = METHOD_EXTRAPOLATION.presentValue(swaptionShortPayerHighStrike, sabrBundle);
    double priceLongReceiver = METHOD_EXTRAPOLATION.presentValue(swaptionLongReceiverHighStrike, sabrBundle);
    double priceLongPayerSABR = PVC_NO_EXTRA.visit(swaptionLongPayerHighStrike, sabrBundle);
    double priceLongReceiverSABR = PVC_NO_EXTRA.visit(swaptionLongReceiverHighStrike, sabrBundle);
    assertEquals("Swaption SABR extrapolation: extrapolation limit", priceLongPayerSABR, priceLongPayer, 1E-1);
    assertEquals("Swaption SABR extrapolation: extrapolation limit", priceLongReceiverSABR, priceLongReceiver, 1E-1);
    assertEquals("Swaption SABR extrapolation: long/short parity", priceLongPayer, -priceShortPayer, 1E-2);
  }

  /**
   * Tests present value in the region where there is extrapolation. Test a hard-coded value. Tests long/short parity. Test payer/receiver/swap parity.
   */
  @Test
  public void testPresentValueExtra() {
    YieldCurveBundle curves = TestsDataSets.createCurves1();
    SABRInterestRateParameter sabrParameter = TestsDataSets.createSABR1();
    SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    double highStrike = 0.10;
    SwapFixedIborDefinition swapDefinitionPayerHighStrike = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, highStrike, FIXED_IS_PAYER);
    SwapFixedIborDefinition swapDefinitionReceiverHighStrike = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, highStrike, !FIXED_IS_PAYER);
    FixedCouponSwap<Payment> swapPayerHighStrike = swapDefinitionPayerHighStrike.toDerivative(REFERENCE_DATE, CURVES_NAME);
    SwaptionPhysicalFixedIborDefinition swaptionDefinitionLongPayerHighStrike = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapDefinitionPayerHighStrike, IS_LONG);
    SwaptionPhysicalFixedIborDefinition swaptionDefinitionShortPayerHighStrike = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapDefinitionPayerHighStrike, !IS_LONG);
    SwaptionPhysicalFixedIborDefinition swaptionDefinitionLongReceiverHighStrike = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapDefinitionReceiverHighStrike, IS_LONG);
    SwaptionPhysicalFixedIbor swaptionLongPayerHighStrike = swaptionDefinitionLongPayerHighStrike.toDerivative(REFERENCE_DATE, CURVES_NAME);
    SwaptionPhysicalFixedIbor swaptionShortPayerHighStrike = swaptionDefinitionShortPayerHighStrike.toDerivative(REFERENCE_DATE, CURVES_NAME);
    SwaptionPhysicalFixedIbor swaptionLongReceiverHighStrike = swaptionDefinitionLongReceiverHighStrike.toDerivative(REFERENCE_DATE, CURVES_NAME);
    double priceLongPayer = METHOD_EXTRAPOLATION.presentValue(swaptionLongPayerHighStrike, sabrBundle);
    double priceShortPayer = METHOD_EXTRAPOLATION.presentValue(swaptionShortPayerHighStrike, sabrBundle);
    double priceLongReceiver = METHOD_EXTRAPOLATION.presentValue(swaptionLongReceiverHighStrike, sabrBundle);
    double pricePayer = PVC.visit(swapPayerHighStrike, curves);
    double priceLongPayerExpected = 543216.124; // Value from previous run
    double priceLongReceiverExpected = 20215541.316; // Value from previous run
    assertEquals("Swaption SABR extrapolation: fixed value", priceLongPayerExpected, priceLongPayer, 1E-2);
    assertEquals("Swaption SABR extrapolation: fixed value", priceLongReceiverExpected, priceLongReceiver, 1E-2);
    assertEquals("Swaption SABR extrapolation: long/short parity", priceLongPayer, -priceShortPayer, 1E-2);
    assertEquals("Swaption SABR extrapolation: payer/receiver/swap parity", pricePayer, priceLongPayer - priceLongReceiver, 1E-2);
  }

  @Test
  /**
   * Test the present value sensitivity for a swaption with strike above the cut-off strike.
   */
  public void testPresentValueSensitivityExtra() {
    YieldCurveBundle curves = TestsDataSets.createCurves1();
    SABRInterestRateParameter sabrParameter = TestsDataSets.createSABR1();
    SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    double highStrike = 0.10;
    SwapFixedIborDefinition swapDefinitionPayerHighStrike = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, highStrike, FIXED_IS_PAYER);
    SwaptionPhysicalFixedIborDefinition swaptionDefinitionLongPayerHighStrike = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapDefinitionPayerHighStrike, IS_LONG);
    SwaptionPhysicalFixedIborDefinition swaptionDefinitionShortPayerHighStrike = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapDefinitionPayerHighStrike, !IS_LONG);
    SwaptionPhysicalFixedIbor swaptionLongPayerHighStrike = swaptionDefinitionLongPayerHighStrike.toDerivative(REFERENCE_DATE, CURVES_NAME);
    SwaptionPhysicalFixedIbor swaptionShortPayerHighStrike = swaptionDefinitionShortPayerHighStrike.toDerivative(REFERENCE_DATE, CURVES_NAME);
    // Swaption sensitivity
    PresentValueSensitivity pvsLongPayerExtra = METHOD_EXTRAPOLATION.presentValueSensitivity(swaptionLongPayerHighStrike, sabrBundle);
    PresentValueSensitivity pvsShortPayerExtra = METHOD_EXTRAPOLATION.presentValueSensitivity(swaptionShortPayerHighStrike, sabrBundle);
    // Long/short parity
    PresentValueSensitivity pvsShortPayer_1 = pvsShortPayerExtra.multiply(-1);
    assertEquals(pvsLongPayerExtra.getSensitivity(), pvsShortPayer_1.getSensitivity());
    // Present value sensitivity comparison with finite difference.
    double deltaTolerance = 5.0E+4;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
    final double deltaShift = 1.0E-5;
    pvsLongPayerExtra = pvsLongPayerExtra.clean();
    double pv = METHOD_EXTRAPOLATION.presentValue(swaptionLongPayerHighStrike, sabrBundle);
    // 1. Forward curve sensitivity
    String bumpedCurveName = "Bumped Curve";
    String[] bumpedCurvesForwardName = {FUNDING_CURVE_NAME, bumpedCurveName};
    SwaptionPhysicalFixedIbor swaptionBumpedForward = swaptionDefinitionLongPayerHighStrike.toDerivative(REFERENCE_DATE, bumpedCurvesForwardName);
    final YieldAndDiscountCurve curveForward = curves.getCurve(FORWARD_CURVE_NAME);
    Set<Double> timeForwardSet = new TreeSet<Double>();
    for (Payment pay : swaptionLongPayerHighStrike.getUnderlyingSwap().getSecondLeg().getPayments()) {
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
    List<DoublesPair> tempForward = pvsLongPayerExtra.getSensitivity().get(FORWARD_CURVE_NAME);
    double[] resFwd = new double[nbForwardDate];
    for (int i = 0; i < nbForwardDate; i++) {
      final YieldAndDiscountCurve bumpedCurveForward = tempCurveForward.withSingleShift(nodeTimesForward[i + 1], deltaShift);
      final YieldCurveBundle curvesBumpedForward = new YieldCurveBundle();
      curvesBumpedForward.addAll(curves);
      curvesBumpedForward.setCurve("Bumped Curve", bumpedCurveForward);
      SABRInterestRateDataBundle sabrBundleBumped = new SABRInterestRateDataBundle(sabrParameter, curvesBumpedForward);
      final double bumpedpv = METHOD_EXTRAPOLATION.presentValue(swaptionBumpedForward, sabrBundleBumped);
      resFwd[i] = (bumpedpv - pv) / deltaShift;
      final DoublesPair pair = tempForward.get(i);
      assertEquals("Sensitivity to forward curve: Node " + i, nodeTimesForward[i + 1], pair.getFirst(), 1E-8);
      assertEquals("Sensitivity to forward curve: Node " + i, resFwd[i], pair.getSecond(), deltaTolerance);
    }
    // 2. Funding curve sensitivity
    String[] bumpedCurvesFundingName = {bumpedCurveName, FORWARD_CURVE_NAME};
    SwaptionPhysicalFixedIbor swaptionBumpedFunding = swaptionDefinitionLongPayerHighStrike.toDerivative(REFERENCE_DATE, bumpedCurvesFundingName);
    int nbPayDate = swaptionDefinitionLongPayerHighStrike.getUnderlyingSwap().getIborLeg().getPayments().length;
    final YieldAndDiscountCurve curveFunding = curves.getCurve(FUNDING_CURVE_NAME);
    final double[] yieldsFunding = new double[nbPayDate + 1];
    double[] nodeTimesFunding = new double[nbPayDate + 1];
    yieldsFunding[0] = curveFunding.getInterestRate(0.0);
    for (int i = 0; i < nbPayDate; i++) {
      nodeTimesFunding[i + 1] = swaptionLongPayerHighStrike.getUnderlyingSwap().getSecondLeg().getNthPayment(i).getPaymentTime();
      yieldsFunding[i + 1] = curveFunding.getInterestRate(nodeTimesFunding[i + 1]);
    }
    final YieldAndDiscountCurve tempCurveFunding = new YieldCurve(InterpolatedDoublesCurve.fromSorted(nodeTimesFunding, yieldsFunding, new LinearInterpolator1D()));
    List<DoublesPair> tempFunding = pvsLongPayerExtra.getSensitivity().get(FUNDING_CURVE_NAME);
    double[] resDsc = new double[nbPayDate];
    for (int i = 0; i < nbPayDate; i++) {
      final YieldAndDiscountCurve bumpedCurve = tempCurveFunding.withSingleShift(nodeTimesFunding[i + 1], deltaShift);
      final YieldCurveBundle curvesBumped = new YieldCurveBundle();
      curvesBumped.addAll(curves);
      curvesBumped.setCurve("Bumped Curve", bumpedCurve);
      SABRInterestRateDataBundle sabrBundleBumped = new SABRInterestRateDataBundle(sabrParameter, curvesBumped);
      final double bumpedpv = METHOD_EXTRAPOLATION.presentValue(swaptionBumpedFunding, sabrBundleBumped);
      resDsc[i] = (bumpedpv - pv) / deltaShift;
      final DoublesPair pair = tempFunding.get(i);
      assertEquals("Sensitivity to discounting curve: Node " + i, nodeTimesFunding[i + 1], pair.getFirst(), 1E-8);
      assertEquals("Sensitivity to discounting curve: Node " + i, resDsc[i], pair.getSecond(), deltaTolerance);
    }
  }

  @Test
  /**
   * Test the present value sensitivity to SABR parameters for a swaption with strike above the cut-off strike.
   */
  public void testPresentValueSABRSensitivity() {
    YieldCurveBundle curves = TestsDataSets.createCurves1();
    SABRInterestRateParameter sabrParameter = TestsDataSets.createSABR1();
    SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    double highStrike = 0.10;
    SwapFixedIborDefinition swapDefinitionPayerHighStrike = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, highStrike, FIXED_IS_PAYER);
    SwaptionPhysicalFixedIborDefinition swaptionDefinitionLongPayerHighStrike = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapDefinitionPayerHighStrike, IS_LONG);
    SwaptionPhysicalFixedIborDefinition swaptionDefinitionShortPayerHighStrike = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapDefinitionPayerHighStrike, !IS_LONG);
    SwaptionPhysicalFixedIbor swaptionLongPayerHighStrike = swaptionDefinitionLongPayerHighStrike.toDerivative(REFERENCE_DATE, CURVES_NAME);
    SwaptionPhysicalFixedIbor swaptionShortPayerHighStrike = swaptionDefinitionShortPayerHighStrike.toDerivative(REFERENCE_DATE, CURVES_NAME);
    //    SwaptionCashFixedIborSABRExtrapolationRightMethod methodExtra = new SwaptionCashFixedIborSABRExtrapolationRightMethod(CUT_OFF_STRIKE, MU);
    // Swaption sensitivity
    PresentValueSABRSensitivityDataBundle pvsLongPayer = METHOD_EXTRAPOLATION.presentValueSABRSensitivity(swaptionLongPayerHighStrike, sabrBundle);
    PresentValueSABRSensitivityDataBundle pvsShortPayer = METHOD_EXTRAPOLATION.presentValueSABRSensitivity(swaptionShortPayerHighStrike, sabrBundle);
    // Long/short parity
    pvsShortPayer.multiply(-1.0);
    assertEquals(pvsLongPayer.getAlpha(), pvsShortPayer.getAlpha());
    // SABR sensitivity vs finite difference
    double pvLongPayer = METHOD_EXTRAPOLATION.presentValue(swaptionLongPayerHighStrike, sabrBundle);
    DoublesPair expectedExpiryTenor = new DoublesPair(swaptionLongPayerHighStrike.getTimeToExpiry(), ANNUITY_TENOR_YEAR);
    double shift = 0.000005;
    // Alpha sensitivity vs finite difference computation
    SABRInterestRateParameter sabrParameterAlphaBumped = TestsDataSets.createSABR1AlphaBumped(shift);
    SABRInterestRateDataBundle sabrBundleAlphaBumped = new SABRInterestRateDataBundle(sabrParameterAlphaBumped, curves);
    double pvLongPayerAlphaBumped = METHOD_EXTRAPOLATION.presentValue(swaptionLongPayerHighStrike, sabrBundleAlphaBumped);
    double expectedAlphaSensi = (pvLongPayerAlphaBumped - pvLongPayer) / shift;
    assertEquals("Number of alpha sensitivity", pvsLongPayer.getAlpha().keySet().size(), 1);
    assertEquals("Alpha sensitivity expiry/tenor", pvsLongPayer.getAlpha().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Alpha sensitivity value", expectedAlphaSensi, pvsLongPayer.getAlpha().get(expectedExpiryTenor), 2.0E+3);
    // Rho sensitivity vs finite difference computation
    SABRInterestRateParameter sabrParameterRhoBumped = TestsDataSets.createSABR1RhoBumped(shift);
    SABRInterestRateDataBundle sabrBundleRhoBumped = new SABRInterestRateDataBundle(sabrParameterRhoBumped, curves);
    double pvLongPayerRhoBumped = METHOD_EXTRAPOLATION.presentValue(swaptionLongPayerHighStrike, sabrBundleRhoBumped);
    double expectedRhoSensi = (pvLongPayerRhoBumped - pvLongPayer) / shift;
    assertEquals("Number of rho sensitivity", pvsLongPayer.getRho().keySet().size(), 1);
    assertEquals("Rho sensitivity expiry/tenor", pvsLongPayer.getRho().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Rho sensitivity value", expectedRhoSensi, pvsLongPayer.getRho().get(expectedExpiryTenor), 1.0E+0);
    // Alpha sensitivity vs finite difference computation
    SABRInterestRateParameter sabrParameterNuBumped = TestsDataSets.createSABR1NuBumped(shift);
    SABRInterestRateDataBundle sabrBundleNuBumped = new SABRInterestRateDataBundle(sabrParameterNuBumped, curves);
    double pvLongPayerNuBumped = METHOD_EXTRAPOLATION.presentValue(swaptionLongPayerHighStrike, sabrBundleNuBumped);
    double expectedNuSensi = (pvLongPayerNuBumped - pvLongPayer) / shift;
    assertEquals("Number of nu sensitivity", pvsLongPayer.getNu().keySet().size(), 1);
    assertEquals("Nu sensitivity expiry/tenor", pvsLongPayer.getNu().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Nu sensitivity value", expectedNuSensi, pvsLongPayer.getNu().get(expectedExpiryTenor), 5.0E+1);
  }

  @Test(enabled = false)
  /**
   * Tests of performance. "enabled = false" for the standard testing.
   */
  public void testPerformance() {
    YieldCurveBundle curves = TestsDataSets.createCurves1();
    SABRInterestRateParameter sabrParameter = TestsDataSets.createSABR1();
    SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    double cutOffStrike = 0.08;
    double mu = 10.0;
    double highStrike = 0.10;
    SwapFixedIborDefinition swapDefinitionPayerHighStrike = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, highStrike, FIXED_IS_PAYER);
    SwapFixedIborDefinition swapDefinitionReceiverHighStrike = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, highStrike, !FIXED_IS_PAYER);
    SwaptionPhysicalFixedIborDefinition swaptionDefinitionLongPayerHighStrike = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapDefinitionPayerHighStrike, IS_LONG);
    SwaptionPhysicalFixedIborDefinition swaptionDefinitionLongReceiverHighStrike = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapDefinitionReceiverHighStrike, IS_LONG);
    SwaptionPhysicalFixedIbor swaptionLongPayerHighStrike = swaptionDefinitionLongPayerHighStrike.toDerivative(REFERENCE_DATE, CURVES_NAME);
    SwaptionPhysicalFixedIbor swaptionLongReceiverHighStrike = swaptionDefinitionLongReceiverHighStrike.toDerivative(REFERENCE_DATE, CURVES_NAME);
    SwaptionPhysicalFixedIborSABRExtrapolationRightMethod methodExtrapolation = new SwaptionPhysicalFixedIborSABRExtrapolationRightMethod(cutOffStrike, mu);
    SwaptionPhysicalFixedIborSABRMethod methodNoExtrapolation = new SwaptionPhysicalFixedIborSABRMethod();

    long startTime, endTime;
    int nbTest = 1000;

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      methodExtrapolation.presentValue(swaptionLongPayerHighStrike, sabrBundle);
      methodExtrapolation.presentValueSensitivity(swaptionLongPayerHighStrike, sabrBundle);
      methodExtrapolation.presentValueSABRSensitivity(swaptionLongPayerHighStrike, sabrBundle);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " swaption payer price+delta+vega with SABR extrapolation: " + (endTime - startTime) + " ms");
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      methodNoExtrapolation.presentValue(swaptionLongPayerHighStrike, sabrBundle);
      methodNoExtrapolation.presentValueSensitivity(swaptionLongPayerHighStrike, sabrBundle);
      methodExtrapolation.presentValueSABRSensitivity(swaptionLongPayerHighStrike, sabrBundle);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " swaption payer price+delta+vega with standard SABR: " + (endTime - startTime) + " ms");
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      methodExtrapolation.presentValue(swaptionLongReceiverHighStrike, sabrBundle);
      methodExtrapolation.presentValueSensitivity(swaptionLongReceiverHighStrike, sabrBundle);
      methodExtrapolation.presentValueSABRSensitivity(swaptionLongReceiverHighStrike, sabrBundle);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " swaption receiver price+delta+vega with SABR extrapolation: " + (endTime - startTime) + " ms");

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      methodNoExtrapolation.presentValue(swaptionLongReceiverHighStrike, sabrBundle);
      methodExtrapolation.presentValueSensitivity(swaptionLongReceiverHighStrike, sabrBundle);
      methodExtrapolation.presentValueSABRSensitivity(swaptionLongReceiverHighStrike, sabrBundle);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " swaption receiver price+delta+vega with standard SABR: " + (endTime - startTime) + " ms");
    // Performance note: price+delta payer extrapolation: 06-May-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 1425 ms for 10000 swaptions.
    // Performance note: price+delta payer standard: 06-May-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 940 ms for 10000 swaptions.
    // Performance note: price+delta+vega payer extrapolation: 06-May-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 2050 ms for 10000 swaptions.
    // Performance note: price+delta+vega payer standard: 06-May-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 1575 ms for 10000 swaptions.
  }

}
