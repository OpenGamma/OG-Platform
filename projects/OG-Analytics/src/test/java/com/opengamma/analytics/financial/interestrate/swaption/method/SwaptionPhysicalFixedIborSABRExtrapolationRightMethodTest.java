/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.method;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRExtrapolationCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
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
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class to test the present value and present value rate sensitivity of the physical delivery swaption in the SABR with extrapolation method.
 * The SABR smile is extrapolated above a certain cut-off strike.
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class SwaptionPhysicalFixedIborSABRExtrapolationRightMethodTest {
  // Swaption description
  private static final ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2014, 3, 18);
  private static final boolean IS_LONG = true;
  private static final int SETTLEMENT_DAYS = 2;
  // Swap 5Y description
  private static final Currency CUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final int ANNUITY_TENOR_YEAR = 5;
  private static final Period ANNUITY_TENOR = Period.ofYears(ANNUITY_TENOR_YEAR);
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final double NOTIONAL = 100000000; //100m
  //  Fixed leg: Semi-annual bond
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final double RATE = 0.04;
  private static final boolean FIXED_IS_PAYER = true;
  //  Ibor leg: quarterly money
  private static final Period INDEX_TENOR = Period.ofMonths(3);
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  // Swaption construction
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, ANNUITY_TENOR, CALENDAR);
  private static final SwapFixedIborDefinition SWAP_DEFINITION_PAYER = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER, CALENDAR);
  private static final SwapFixedIborDefinition SWAP_DEFINITION_RECEIVER = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, !FIXED_IS_PAYER, CALENDAR);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_DEFINITION_LONG_PAYER = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_PAYER, true, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_DEFINITION_LONG_RECEIVER = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_RECEIVER, false, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_DEFINITION_SHORT_PAYER = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_PAYER, true, !IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_DEFINITION_SHORT_RECEIVER = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_RECEIVER, false, !IS_LONG);
  // to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2008, 8, 18);
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
  private static final PresentValueSABRExtrapolationCalculator PVC = new PresentValueSABRExtrapolationCalculator(CUT_OFF_STRIKE, MU);
  private static final PresentValueSABRCalculator PVC_NO_EXTRA = PresentValueSABRCalculator.getInstance();

  /**
   * Tests present value in the region where there is no extrapolation. Tests long/short parity.
   */
  @Test
  public void testPresentValueNoExtra() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSetsSABR.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    final double priceLongPayer = METHOD_EXTRAPOLATION.presentValue(SWAPTION_LONG_PAYER, sabrBundle);
    final double priceShortPayer = METHOD_EXTRAPOLATION.presentValue(SWAPTION_SHORT_PAYER, sabrBundle);
    final double priceLongReceiver = METHOD_EXTRAPOLATION.presentValue(SWAPTION_LONG_RECEIVER, sabrBundle);
    final double priceShortReceiver = METHOD_EXTRAPOLATION.presentValue(SWAPTION_SHORT_RECEIVER, sabrBundle);
    final double priceLongPayerNoExtra = SWAPTION_LONG_PAYER.accept(PVC_NO_EXTRA, sabrBundle);
    final double priceShortPayerNoExtra = SWAPTION_SHORT_PAYER.accept(PVC_NO_EXTRA, sabrBundle);
    final double priceLongReceiverNoExtra = SWAPTION_LONG_RECEIVER.accept(PVC_NO_EXTRA, sabrBundle);
    final double priceShortReceiverNoExtra = SWAPTION_SHORT_RECEIVER.accept(PVC_NO_EXTRA, sabrBundle);
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
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSetsSABR.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    final double highStrike = 0.0801;
    final SwapFixedIborDefinition swapPayerHighStrike = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, highStrike, FIXED_IS_PAYER, CALENDAR);
    final SwapFixedIborDefinition swapReceiverHighStrike = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, highStrike, !FIXED_IS_PAYER, CALENDAR);
    final SwaptionPhysicalFixedIborDefinition swaptionDefinitionLongPayerHighStrike = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapPayerHighStrike, true, IS_LONG);
    final SwaptionPhysicalFixedIborDefinition swaptionDefinitionShortPayerHighStrike = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapPayerHighStrike, true, !IS_LONG);
    final SwaptionPhysicalFixedIborDefinition swaptionDefinitionLongReceiverHighStrike = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapReceiverHighStrike, false, IS_LONG);
    final SwaptionPhysicalFixedIbor swaptionLongPayerHighStrike = swaptionDefinitionLongPayerHighStrike.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final SwaptionPhysicalFixedIbor swaptionShortPayerHighStrike = swaptionDefinitionShortPayerHighStrike.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final SwaptionPhysicalFixedIbor swaptionLongReceiverHighStrike = swaptionDefinitionLongReceiverHighStrike.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final double priceLongPayer = METHOD_EXTRAPOLATION.presentValue(swaptionLongPayerHighStrike, sabrBundle);
    final double priceShortPayer = METHOD_EXTRAPOLATION.presentValue(swaptionShortPayerHighStrike, sabrBundle);
    final double priceLongReceiver = METHOD_EXTRAPOLATION.presentValue(swaptionLongReceiverHighStrike, sabrBundle);
    final double priceLongPayerSABR = swaptionLongPayerHighStrike.accept(PVC_NO_EXTRA, sabrBundle);
    final double priceLongReceiverSABR = swaptionLongReceiverHighStrike.accept(PVC_NO_EXTRA, sabrBundle);
    assertEquals("Swaption SABR extrapolation: extrapolation limit", priceLongPayerSABR, priceLongPayer, 1E-1);
    assertEquals("Swaption SABR extrapolation: extrapolation limit", priceLongReceiverSABR, priceLongReceiver, 1E-1);
    assertEquals("Swaption SABR extrapolation: long/short parity", priceLongPayer, -priceShortPayer, 1E-2);
  }

  /**
   * Tests present value in the region where there is extrapolation. Test a hard-coded value. Tests long/short parity. Test payer/receiver/swap parity.
   */
  @Test
  public void testPresentValueExtra() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSetsSABR.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    final double highStrike = 0.10;
    final SwapFixedIborDefinition swapDefinitionPayerHighStrike = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, highStrike, FIXED_IS_PAYER, CALENDAR);
    final SwapFixedIborDefinition swapDefinitionReceiverHighStrike = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, highStrike, !FIXED_IS_PAYER, CALENDAR);
    final SwapFixedCoupon<Coupon> swapPayerHighStrike = swapDefinitionPayerHighStrike.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final SwaptionPhysicalFixedIborDefinition swaptionDefinitionLongPayerHighStrike = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapDefinitionPayerHighStrike, true, IS_LONG);
    final SwaptionPhysicalFixedIborDefinition swaptionDefinitionShortPayerHighStrike = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapDefinitionPayerHighStrike, true, !IS_LONG);
    final SwaptionPhysicalFixedIborDefinition swaptionDefinitionLongReceiverHighStrike = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapDefinitionReceiverHighStrike, false, IS_LONG);
    final SwaptionPhysicalFixedIbor swaptionLongPayerHighStrike = swaptionDefinitionLongPayerHighStrike.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final SwaptionPhysicalFixedIbor swaptionShortPayerHighStrike = swaptionDefinitionShortPayerHighStrike.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final SwaptionPhysicalFixedIbor swaptionLongReceiverHighStrike = swaptionDefinitionLongReceiverHighStrike.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final double priceLongPayer = METHOD_EXTRAPOLATION.presentValue(swaptionLongPayerHighStrike, sabrBundle);
    final double priceShortPayer = METHOD_EXTRAPOLATION.presentValue(swaptionShortPayerHighStrike, sabrBundle);
    final double priceLongReceiver = METHOD_EXTRAPOLATION.presentValue(swaptionLongReceiverHighStrike, sabrBundle);
    final double pricePayer = swapPayerHighStrike.accept(PVC, curves);
    final double priceLongPayerExpected = 543216.124; // Value from previous run
    final double priceLongReceiverExpected = 20215541.316; // Value from previous run
    assertEquals("Swaption SABR extrapolation: fixed value", priceLongPayerExpected, priceLongPayer, 1E-2);
    assertEquals("Swaption SABR extrapolation: fixed value", priceLongReceiverExpected, priceLongReceiver, 1E-2);
    assertEquals("Swaption SABR extrapolation: long/short parity", priceLongPayer, -priceShortPayer, 1E-2);
    assertEquals("Swaption SABR extrapolation: payer/receiver/swap parity", pricePayer, priceLongPayer - priceLongReceiver, 1E-2);
  }

  @Test
  /**
   * Test the present value sensitivity for a swaption with strike above the cut-off strike.
   */
  public void presentValueCurveSensitivityExtra() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSetsSABR.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    final double highStrike = 0.10;
    final SwapFixedIborDefinition swapDefinitionPayerHighStrike = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, highStrike, FIXED_IS_PAYER, CALENDAR);
    final SwaptionPhysicalFixedIborDefinition swaptionDefinitionLongPayerHighStrike = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapDefinitionPayerHighStrike, true, IS_LONG);
    final SwaptionPhysicalFixedIborDefinition swaptionDefinitionShortPayerHighStrike = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapDefinitionPayerHighStrike, true, !IS_LONG);
    final SwaptionPhysicalFixedIbor swaptionLongPayerHighStrike = swaptionDefinitionLongPayerHighStrike.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final SwaptionPhysicalFixedIbor swaptionShortPayerHighStrike = swaptionDefinitionShortPayerHighStrike.toDerivative(REFERENCE_DATE, CURVES_NAME);
    // Swaption sensitivity
    InterestRateCurveSensitivity pvsLongPayerExtra = METHOD_EXTRAPOLATION.presentValueCurveSensitivity(swaptionLongPayerHighStrike, sabrBundle);
    final InterestRateCurveSensitivity pvsShortPayerExtra = METHOD_EXTRAPOLATION.presentValueCurveSensitivity(swaptionShortPayerHighStrike, sabrBundle);
    // Long/short parity
    final InterestRateCurveSensitivity pvsShortPayer_1 = pvsShortPayerExtra.multipliedBy(-1);
    assertEquals(pvsLongPayerExtra.getSensitivities(), pvsShortPayer_1.getSensitivities());
    // Present value sensitivity comparison with finite difference.
    final double deltaTolerance = 5.0E+4;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
    final double deltaShift = 1.0E-5;
    pvsLongPayerExtra = pvsLongPayerExtra.cleaned();
    final double pv = METHOD_EXTRAPOLATION.presentValue(swaptionLongPayerHighStrike, sabrBundle);
    // 1. Forward curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    final String[] bumpedCurvesForwardName = {FUNDING_CURVE_NAME, bumpedCurveName};
    final SwaptionPhysicalFixedIbor swaptionBumpedForward = swaptionDefinitionLongPayerHighStrike.toDerivative(REFERENCE_DATE, bumpedCurvesForwardName);
    final YieldAndDiscountCurve curveForward = curves.getCurve(FORWARD_CURVE_NAME);
    final Set<Double> timeForwardSet = new TreeSet<>();
    for (final Payment pay : swaptionLongPayerHighStrike.getUnderlyingSwap().getSecondLeg().getPayments()) {
      final CouponIbor coupon = (CouponIbor) pay;
      timeForwardSet.add(coupon.getFixingPeriodStartTime());
      timeForwardSet.add(coupon.getFixingPeriodEndTime());
    }
    final int nbForwardDate = timeForwardSet.size();
    final List<Double> timeForwardList = new ArrayList<>(timeForwardSet);
    Double[] timeForwardArray = new Double[nbForwardDate];
    timeForwardArray = timeForwardList.toArray(timeForwardArray);
    final double[] yieldsForward = new double[nbForwardDate + 1];
    final double[] nodeTimesForward = new double[nbForwardDate + 1];
    yieldsForward[0] = curveForward.getInterestRate(0.0);
    for (int i = 0; i < nbForwardDate; i++) {
      nodeTimesForward[i + 1] = timeForwardArray[i];
      yieldsForward[i + 1] = curveForward.getInterestRate(nodeTimesForward[i + 1]);
    }
    final YieldAndDiscountCurve tempCurveForward = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(nodeTimesForward, yieldsForward, new LinearInterpolator1D()));
    final List<DoublesPair> tempForward = pvsLongPayerExtra.getSensitivities().get(FORWARD_CURVE_NAME);
    final double[] resFwd = new double[nbForwardDate];
    for (int i = 0; i < nbForwardDate; i++) {
      final YieldAndDiscountCurve bumpedCurveForward = tempCurveForward.withSingleShift(nodeTimesForward[i + 1], deltaShift);
      final YieldCurveBundle curvesBumpedForward = new YieldCurveBundle();
      curvesBumpedForward.addAll(curves);
      curvesBumpedForward.setCurve("Bumped Curve", bumpedCurveForward);
      final SABRInterestRateDataBundle sabrBundleBumped = new SABRInterestRateDataBundle(sabrParameter, curvesBumpedForward);
      final double bumpedpv = METHOD_EXTRAPOLATION.presentValue(swaptionBumpedForward, sabrBundleBumped);
      resFwd[i] = (bumpedpv - pv) / deltaShift;
      final DoublesPair pair = tempForward.get(i);
      assertEquals("Sensitivity to forward curve: Node " + i, nodeTimesForward[i + 1], pair.getFirst(), 1E-8);
      assertEquals("Sensitivity to forward curve: Node " + i, resFwd[i], pair.getSecond(), deltaTolerance);
    }
    // 2. Funding curve sensitivity
    final String[] bumpedCurvesFundingName = {bumpedCurveName, FORWARD_CURVE_NAME};
    final SwaptionPhysicalFixedIbor swaptionBumpedFunding = swaptionDefinitionLongPayerHighStrike.toDerivative(REFERENCE_DATE, bumpedCurvesFundingName);
    final SwapDefinition underlyingSwap = swaptionDefinitionLongPayerHighStrike.getUnderlyingSwap();
    AnnuityDefinition<? extends PaymentDefinition> floatLeg;
    if (underlyingSwap.getFirstLeg() instanceof AnnuityCouponFixedDefinition) {
      floatLeg = underlyingSwap.getSecondLeg();
    } else {
      floatLeg = underlyingSwap.getFirstLeg();
    }
    final int nbPayDate = floatLeg.getPayments().length;
    final YieldAndDiscountCurve curveFunding = curves.getCurve(FUNDING_CURVE_NAME);
    final double[] yieldsFunding = new double[nbPayDate + 1];
    final double[] nodeTimesFunding = new double[nbPayDate + 1];
    yieldsFunding[0] = curveFunding.getInterestRate(0.0);
    for (int i = 0; i < nbPayDate; i++) {
      nodeTimesFunding[i + 1] = swaptionLongPayerHighStrike.getUnderlyingSwap().getSecondLeg().getNthPayment(i).getPaymentTime();
      yieldsFunding[i + 1] = curveFunding.getInterestRate(nodeTimesFunding[i + 1]);
    }
    final YieldAndDiscountCurve tempCurveFunding = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(nodeTimesFunding, yieldsFunding, new LinearInterpolator1D()));
    final List<DoublesPair> tempFunding = pvsLongPayerExtra.getSensitivities().get(FUNDING_CURVE_NAME);
    final double[] resDsc = new double[nbPayDate];
    for (int i = 0; i < nbPayDate; i++) {
      final YieldAndDiscountCurve bumpedCurve = tempCurveFunding.withSingleShift(nodeTimesFunding[i + 1], deltaShift);
      final YieldCurveBundle curvesBumped = new YieldCurveBundle();
      curvesBumped.addAll(curves);
      curvesBumped.setCurve("Bumped Curve", bumpedCurve);
      final SABRInterestRateDataBundle sabrBundleBumped = new SABRInterestRateDataBundle(sabrParameter, curvesBumped);
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
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSetsSABR.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    final double highStrike = 0.10;
    final SwapFixedIborDefinition swapDefinitionPayerHighStrike = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, highStrike, FIXED_IS_PAYER, CALENDAR);
    final SwaptionPhysicalFixedIborDefinition swaptionDefinitionLongPayerHighStrike = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapDefinitionPayerHighStrike, true, IS_LONG);
    final SwaptionPhysicalFixedIborDefinition swaptionDefinitionShortPayerHighStrike = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapDefinitionPayerHighStrike, true, !IS_LONG);
    final SwaptionPhysicalFixedIbor swaptionLongPayerHighStrike = swaptionDefinitionLongPayerHighStrike.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final SwaptionPhysicalFixedIbor swaptionShortPayerHighStrike = swaptionDefinitionShortPayerHighStrike.toDerivative(REFERENCE_DATE, CURVES_NAME);
    // Swaption sensitivity
    final PresentValueSABRSensitivityDataBundle pvsLongPayer = METHOD_EXTRAPOLATION.presentValueSABRSensitivity(swaptionLongPayerHighStrike, sabrBundle);
    PresentValueSABRSensitivityDataBundle pvsShortPayer = METHOD_EXTRAPOLATION.presentValueSABRSensitivity(swaptionShortPayerHighStrike, sabrBundle);
    // Long/short parity
    pvsShortPayer = pvsShortPayer.multiplyBy(-1.0);
    assertEquals(pvsLongPayer.getAlpha(), pvsShortPayer.getAlpha());
    // SABR sensitivity vs finite difference
    final double pvLongPayer = METHOD_EXTRAPOLATION.presentValue(swaptionLongPayerHighStrike, sabrBundle);
    final DoublesPair expectedExpiryTenor = DoublesPair.of(swaptionLongPayerHighStrike.getTimeToExpiry(), ANNUITY_TENOR_YEAR);
    final double shift = 0.000005;
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterAlphaBumped = TestsDataSetsSABR.createSABR1AlphaBumped(shift);
    final SABRInterestRateDataBundle sabrBundleAlphaBumped = new SABRInterestRateDataBundle(sabrParameterAlphaBumped, curves);
    final double pvLongPayerAlphaBumped = METHOD_EXTRAPOLATION.presentValue(swaptionLongPayerHighStrike, sabrBundleAlphaBumped);
    final double expectedAlphaSensi = (pvLongPayerAlphaBumped - pvLongPayer) / shift;
    assertEquals("Number of alpha sensitivity", pvsLongPayer.getAlpha().getMap().keySet().size(), 1);
    assertEquals("Alpha sensitivity expiry/tenor", pvsLongPayer.getAlpha().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Alpha sensitivity value", expectedAlphaSensi, pvsLongPayer.getAlpha().getMap().get(expectedExpiryTenor), 2.0E+3);
    // Beta sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterBetaBumped = TestsDataSetsSABR.createSABR1BetaBumped(shift);
    final SABRInterestRateDataBundle sabrBundleBetaBumped = new SABRInterestRateDataBundle(sabrParameterBetaBumped, curves);
    final double pvLongPayerBetaBumped = METHOD_EXTRAPOLATION.presentValue(swaptionLongPayerHighStrike, sabrBundleBetaBumped);
    final double expectedBetaSensi = (pvLongPayerBetaBumped - pvLongPayer) / shift;
    assertEquals("Number of Beta sensitivity", pvsLongPayer.getBeta().getMap().keySet().size(), 1);
    assertEquals("Beta sensitivity expiry/tenor", pvsLongPayer.getBeta().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Beta sensitivity value", expectedBetaSensi, pvsLongPayer.getBeta().getMap().get(expectedExpiryTenor), 1.5E+2);
    // Rho sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterRhoBumped = TestsDataSetsSABR.createSABR1RhoBumped(shift);
    final SABRInterestRateDataBundle sabrBundleRhoBumped = new SABRInterestRateDataBundle(sabrParameterRhoBumped, curves);
    final double pvLongPayerRhoBumped = METHOD_EXTRAPOLATION.presentValue(swaptionLongPayerHighStrike, sabrBundleRhoBumped);
    final double expectedRhoSensi = (pvLongPayerRhoBumped - pvLongPayer) / shift;
    assertEquals("Number of rho sensitivity", pvsLongPayer.getRho().getMap().keySet().size(), 1);
    assertEquals("Rho sensitivity expiry/tenor", pvsLongPayer.getRho().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Rho sensitivity value", expectedRhoSensi, pvsLongPayer.getRho().getMap().get(expectedExpiryTenor), 1.0E+0);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterNuBumped = TestsDataSetsSABR.createSABR1NuBumped(shift);
    final SABRInterestRateDataBundle sabrBundleNuBumped = new SABRInterestRateDataBundle(sabrParameterNuBumped, curves);
    final double pvLongPayerNuBumped = METHOD_EXTRAPOLATION.presentValue(swaptionLongPayerHighStrike, sabrBundleNuBumped);
    final double expectedNuSensi = (pvLongPayerNuBumped - pvLongPayer) / shift;
    assertEquals("Number of nu sensitivity", pvsLongPayer.getNu().getMap().keySet().size(), 1);
    assertEquals("Nu sensitivity expiry/tenor", pvsLongPayer.getNu().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Nu sensitivity value", expectedNuSensi, pvsLongPayer.getNu().getMap().get(expectedExpiryTenor), 5.0E+1);
  }

  @Test(enabled = false)
  /**
   * Tests of performance. "enabled = false" for the standard testing.
   */
  public void testPerformance() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSetsSABR.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    final double cutOffStrike = 0.08;
    final double mu = 10.0;
    final double highStrike = 0.10;
    final SwapFixedIborDefinition swapDefinitionPayerHighStrike = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, highStrike, FIXED_IS_PAYER, CALENDAR);
    final SwapFixedIborDefinition swapDefinitionReceiverHighStrike = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, highStrike, !FIXED_IS_PAYER, CALENDAR);
    final SwaptionPhysicalFixedIborDefinition swaptionDefinitionLongPayerHighStrike = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapDefinitionPayerHighStrike, true, IS_LONG);
    final SwaptionPhysicalFixedIborDefinition swaptionDefinitionLongReceiverHighStrike = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapDefinitionReceiverHighStrike, false, IS_LONG);
    final SwaptionPhysicalFixedIbor swaptionLongPayerHighStrike = swaptionDefinitionLongPayerHighStrike.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final SwaptionPhysicalFixedIbor swaptionLongReceiverHighStrike = swaptionDefinitionLongReceiverHighStrike.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final SwaptionPhysicalFixedIborSABRExtrapolationRightMethod methodExtrapolation = new SwaptionPhysicalFixedIborSABRExtrapolationRightMethod(cutOffStrike, mu);
    final SwaptionPhysicalFixedIborSABRMethod methodNoExtrapolation = SwaptionPhysicalFixedIborSABRMethod.getInstance();

    long startTime, endTime;
    final int nbTest = 1000;

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      methodExtrapolation.presentValue(swaptionLongPayerHighStrike, sabrBundle);
      methodExtrapolation.presentValueCurveSensitivity(swaptionLongPayerHighStrike, sabrBundle);
      methodExtrapolation.presentValueSABRSensitivity(swaptionLongPayerHighStrike, sabrBundle);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " swaption payer price+delta+vega with SABR extrapolation: " + (endTime - startTime) + " ms");
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      methodNoExtrapolation.presentValue(swaptionLongPayerHighStrike, sabrBundle);
      methodNoExtrapolation.presentValueCurveSensitivity(swaptionLongPayerHighStrike, sabrBundle);
      methodExtrapolation.presentValueSABRSensitivity(swaptionLongPayerHighStrike, sabrBundle);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " swaption payer price+delta+vega with standard SABR: " + (endTime - startTime) + " ms");
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      methodExtrapolation.presentValue(swaptionLongReceiverHighStrike, sabrBundle);
      methodExtrapolation.presentValueCurveSensitivity(swaptionLongReceiverHighStrike, sabrBundle);
      methodExtrapolation.presentValueSABRSensitivity(swaptionLongReceiverHighStrike, sabrBundle);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " swaption receiver price+delta+vega with SABR extrapolation: " + (endTime - startTime) + " ms");

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      methodNoExtrapolation.presentValue(swaptionLongReceiverHighStrike, sabrBundle);
      methodExtrapolation.presentValueCurveSensitivity(swaptionLongReceiverHighStrike, sabrBundle);
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
