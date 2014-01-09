/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.method;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.payment.CapFloorCMSDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivitySABRExtrapolationCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRExtrapolationCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivitySABRRightExtrapolationCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
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
 *  Test class for the replication method for CMS caplet/floorlet using a SABR smile with extrapolation.
 *  @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class CapFloorCMSSABRExtrapolationRightReplicationMethodTest {
  //Swap 5Y
  private static final Currency CUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Period ANNUITY_TENOR = Period.ofYears(5);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2020, 4, 28);
  //Fixed leg: Semi-annual bond
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final AnnuityCouponFixedDefinition FIXED_ANNUITY = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, FIXED_PAYMENT_PERIOD, CALENDAR, FIXED_DAY_COUNT,
      BUSINESS_DAY, IS_EOM, 1.0, RATE, FIXED_IS_PAYER);
  //Ibor leg: quarterly money
  private static final Period INDEX_TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  private static final AnnuityCouponIborDefinition IBOR_ANNUITY = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, 1.0, IBOR_INDEX, !FIXED_IS_PAYER, CALENDAR);
  // CMS coupon construction
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, ANNUITY_TENOR, CALENDAR);
  private static final SwapFixedIborDefinition SWAP_DEFINITION = new SwapFixedIborDefinition(FIXED_ANNUITY, IBOR_ANNUITY);
  private static final ZonedDateTime FIXING_DATE = ScheduleCalculator.getAdjustedDate(SETTLEMENT_DATE, -SETTLEMENT_DAYS, CALENDAR);
  private static final ZonedDateTime ACCRUAL_START_DATE = SETTLEMENT_DATE; // pre-fixed
  private static final ZonedDateTime ACCRUAL_END_DATE = ScheduleCalculator.getAdjustedDate(ACCRUAL_START_DATE, FIXED_PAYMENT_PERIOD, BUSINESS_DAY, CALENDAR);
  private static final ZonedDateTime PAYMENT_DATE = ACCRUAL_END_DATE;
  private static final DayCount PAYMENT_DAY_COUNT = DayCounts.ACT_360;
  private static final double ACCRUAL_FACTOR = PAYMENT_DAY_COUNT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 1000000; //1m
  private static final CouponCMSDefinition CMS_COUPON_RECEIVER_DEFINITION = CouponCMSDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE,
      SWAP_DEFINITION, CMS_INDEX);
  private static final CouponCMSDefinition CMS_COUPON_PAYER_DEFINITION = CouponCMSDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, -NOTIONAL, FIXING_DATE,
      SWAP_DEFINITION, CMS_INDEX);
  // Cap/Floor construction
  private static final double STRIKE = 0.04;
  private static final boolean IS_CAP = true;
  private static final CapFloorCMSDefinition CMS_CAP_LONG_DEFINITION = CapFloorCMSDefinition.from(CMS_COUPON_RECEIVER_DEFINITION, STRIKE, IS_CAP);
  private static final CapFloorCMSDefinition CMS_CAP_SHORT_DEFINITION = CapFloorCMSDefinition.from(CMS_COUPON_PAYER_DEFINITION, STRIKE, IS_CAP);
  private static final CapFloorCMSDefinition CMS_CAP_0_DEFINITION = CapFloorCMSDefinition.from(CMS_COUPON_RECEIVER_DEFINITION, 0.0, IS_CAP);
  // to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 8, 18);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME };
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
  private static final SABRInterestRateParameters SABR_PARAMETER = TestsDataSetsSABR.createSABR1();
  private static final SABRInterestRateDataBundle SABR_BUNDLE = new SABRInterestRateDataBundle(SABR_PARAMETER, CURVES);
  private static final CouponCMS CMS_COUPON = (CouponCMS) CMS_COUPON_RECEIVER_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final CapFloorCMS CMS_CAP_0 = (CapFloorCMS) CMS_CAP_0_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final CapFloorCMS CMS_CAP_LONG = (CapFloorCMS) CMS_CAP_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final CapFloorCMS CMS_CAP_SHORT = (CapFloorCMS) CMS_CAP_SHORT_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  // Calculators & methods
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final CapFloorCMSSABRReplicationMethod METHOD_STANDARD_CAP = CapFloorCMSSABRReplicationMethod.getDefaultInstance();
  private static final CouponCMSSABRReplicationMethod METHOD_STANDARD_CPN = CouponCMSSABRReplicationMethod.getInstance();
  private static final double CUT_OFF_STRIKE = 0.10;
  private static final double MU = 2.50;
  private static final CapFloorCMSSABRExtrapolationRightReplicationMethod METHOD_EXTRAPOLATION_CAP = new CapFloorCMSSABRExtrapolationRightReplicationMethod(CUT_OFF_STRIKE, MU);
  private static final CouponCMSSABRExtrapolationRightReplicationMethod METHOD_EXTRAPOLATION_CPN = new CouponCMSSABRExtrapolationRightReplicationMethod(CUT_OFF_STRIKE, MU);
  private static final CouponCMSGenericMethod METHOD_GENERIC = new CouponCMSGenericMethod(METHOD_EXTRAPOLATION_CAP);
  // Calculators
  private static final PresentValueSABRExtrapolationCalculator PVC_SABR = new PresentValueSABRExtrapolationCalculator(CUT_OFF_STRIKE, MU);
  private static final PresentValueCurveSensitivitySABRExtrapolationCalculator PVCSC_EXTRA_SABR = new PresentValueCurveSensitivitySABRExtrapolationCalculator(CUT_OFF_STRIKE, MU);
  private static final PresentValueSABRSensitivitySABRRightExtrapolationCalculator PVSSC_SABR = new PresentValueSABRSensitivitySABRRightExtrapolationCalculator(CUT_OFF_STRIKE, MU);

  private static final double TOLERANCE_DELTA = 1.0E+2; // 0.01 currency unit for 1 bp.
  private static final double TOLERANCE_PRICE = 1.0E-2;

  @Test
  /**
   * Tests the price of CMS coupon and cap/floor using replication in the SABR framework.  Comparison with hard-coded value.
   */
  public void presentValueHardCoded() {
    final double pvComputed = METHOD_EXTRAPOLATION_CAP.presentValue(CMS_CAP_LONG, SABR_BUNDLE).getAmount();
    final double pvComparison = 6627.971;
    assertEquals("Extrapolation: CMS Cap present value", pvComparison, pvComputed, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Test the present value for a CMS coupon with pricing by replication in the SABR with extrapolation framework.
   * The present value is tested against hard-coded value and cap of strike 0.
   */
  public void presentValue() {
    // CMS cap/floor with strike 0 has the same price as a CMS coupon.
    final double priceCouponStd = METHOD_STANDARD_CPN.presentValue(CMS_COUPON, SABR_BUNDLE).getAmount();
    final double rateCouponStd = priceCouponStd / (CMS_COUPON.getPaymentYearFraction() * CMS_COUPON.getNotional() * CURVES.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(CMS_COUPON.getPaymentTime()));
    final double priceCouponExtra = METHOD_EXTRAPOLATION_CPN.presentValue(CMS_COUPON, SABR_BUNDLE).getAmount();
    final double rateCouponExtra = priceCouponExtra
        / (CMS_COUPON.getPaymentYearFraction() * CMS_COUPON.getNotional() * CURVES.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(CMS_COUPON.getPaymentTime()));
    final double priceCouponNoAdj = CMS_COUPON.accept(PVC, CURVES);
    final double rateCouponNoAdj = priceCouponNoAdj
        / (CMS_COUPON.getPaymentYearFraction() * CMS_COUPON.getNotional() * CURVES.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(CMS_COUPON.getPaymentTime()));
    assertEquals("Extrapolation: comparison with standard method", rateCouponStd > rateCouponExtra, true);
    assertEquals("Extrapolation: comparison with no convexity adjustment", rateCouponExtra > rateCouponNoAdj, true);
    final double rateCouponExtraExpected = 0.0485835; // From previous run.
    assertEquals("Extrapolation: hard-coded value", rateCouponExtraExpected, rateCouponExtra, 1E-6);
    final double priceCap0Extra = METHOD_EXTRAPOLATION_CAP.presentValue(CMS_CAP_0, SABR_BUNDLE).getAmount();
    assertEquals("Extrapolation: CMS coupon vs Cap 0", priceCouponExtra, priceCap0Extra, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests the method against the present value calculator.
   */
  public void presentValueCouponMethodSpecificVsGeneric() {
    final double pvSpecific = METHOD_EXTRAPOLATION_CPN.presentValue(CMS_COUPON, SABR_BUNDLE).getAmount();
    final CurrencyAmount pvGeneric = METHOD_GENERIC.presentValue(CMS_COUPON, SABR_BUNDLE);
    assertEquals("Coupon CMS SABR extrapolation: method : Specific vs Generic", pvSpecific, pvGeneric.getAmount(), TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests the price of CMS coupon and cap/floor using replication in the SABR framework.  Method v Calculator.
   */
  public void presentValueMethodVsCalculator() {
    final double pvMethod = METHOD_EXTRAPOLATION_CAP.presentValue(CMS_CAP_LONG, SABR_BUNDLE).getAmount();
    final double pvCalculator = CMS_CAP_LONG.accept(PVC_SABR, SABR_BUNDLE);
    assertEquals("CMS cap/floor SABR: Present value : method vs calculator", pvMethod, pvCalculator, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Test the present value for a CMS cap with pricing by replication in the SABR with extrapolation framework.
   * The present value is tested against hard-coded value and a long/short parity is tested.
   */
  public void testPriceReplicationCap() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSetsSABR.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    // CMS cap/floor with strike 0 has the same price as a CMS coupon.
    final double priceCapLongStd = METHOD_STANDARD_CAP.presentValue(CMS_CAP_LONG, sabrBundle).getAmount();
    final double priceCapLongExtra = METHOD_EXTRAPOLATION_CAP.presentValue(CMS_CAP_LONG, sabrBundle).getAmount();
    final double priceCapShortExtra = METHOD_EXTRAPOLATION_CAP.presentValue(CMS_CAP_SHORT, sabrBundle).getAmount();
    assertEquals("CMS cap by replication - Extrapolation: comparison with standard method", priceCapLongStd > priceCapLongExtra, true);
    final double priceCapExtraExpected = 6627.976; // From previous run.
    assertEquals("CMS cap by replication - Extrapolation: hard-coded value", priceCapExtraExpected, priceCapLongExtra, 1E-2);
    assertEquals("CMS cap by replication - Extrapolation: long/short parity", -priceCapShortExtra, priceCapLongExtra, 1E-2);
  }

  @Test
  /**
   * Test the present value rate sensitivity for a CMS cap with pricing by replication in the SABR with extrapolation framework.
   */
  public void presentValueCurveSensitivity() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSetsSABR.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    // Swaption sensitivity
    InterestRateCurveSensitivity pvsCapLong = METHOD_EXTRAPOLATION_CAP.presentValueCurveSensitivity(CMS_CAP_LONG, sabrBundle);
    InterestRateCurveSensitivity pvsCapLongStd = METHOD_STANDARD_CAP.presentValueCurveSensitivity(CMS_CAP_LONG, sabrBundle);
    final InterestRateCurveSensitivity pvsCapShort = METHOD_EXTRAPOLATION_CAP.presentValueCurveSensitivity(CMS_CAP_SHORT, sabrBundle);
    // Long/short parity
    final InterestRateCurveSensitivity pvsCapShort_1 = pvsCapShort.multipliedBy(-1);
    assertEquals(pvsCapLong.getSensitivities(), pvsCapShort_1.getSensitivities());
    // Present value sensitivity comparison with finite difference.
    pvsCapLong = pvsCapLong.cleaned();
    pvsCapLongStd = pvsCapLongStd.cleaned();
    final double deltaTolerance = 4.0E+2;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
    final double deltaShift = 1.0E-5;
    pvsCapLong = pvsCapLong.cleaned();
    final double pv = METHOD_EXTRAPOLATION_CAP.presentValue(CMS_CAP_LONG, sabrBundle).getAmount();
    // 1. Forward curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    final String[] bumpedCurvesForwardName = {FUNDING_CURVE_NAME, bumpedCurveName };
    final CapFloorCMS capBumpedForward = (CapFloorCMS) CMS_CAP_LONG_DEFINITION.toDerivative(REFERENCE_DATE, bumpedCurvesForwardName);
    final YieldAndDiscountCurve curveForward = curves.getCurve(FORWARD_CURVE_NAME);
    final Set<Double> timeForwardSet = new TreeSet<>();
    for (final Payment pay : CMS_CAP_LONG.getUnderlyingSwap().getSecondLeg().getPayments()) {
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
    final List<DoublesPair> tempForward = pvsCapLong.getSensitivities().get(FORWARD_CURVE_NAME);
    final double[] resFwd = new double[nbForwardDate];
    for (int i = 0; i < nbForwardDate; i++) {
      final YieldAndDiscountCurve bumpedCurveForward = tempCurveForward.withSingleShift(nodeTimesForward[i + 1], deltaShift);
      final YieldCurveBundle curvesBumpedForward = new YieldCurveBundle();
      curvesBumpedForward.addAll(curves);
      curvesBumpedForward.setCurve("Bumped Curve", bumpedCurveForward);
      final SABRInterestRateDataBundle sabrBundleBumped = new SABRInterestRateDataBundle(sabrParameter, curvesBumpedForward);
      final double bumpedpv = METHOD_EXTRAPOLATION_CAP.presentValue(capBumpedForward, sabrBundleBumped).getAmount();
      resFwd[i] = (bumpedpv - pv) / deltaShift;
      final DoublesPair pair = tempForward.get(i);
      assertEquals("Sensitivity to forward curve: Node " + i, nodeTimesForward[i + 1], pair.getFirst(), 1E-8);
      assertEquals("Sensitivity to forward curve: Node " + i, resFwd[i], pair.getSecond(), deltaTolerance);
    }
    // 2. Funding curve sensitivity
    final String[] bumpedCurvesFundingName = {bumpedCurveName, FORWARD_CURVE_NAME };
    final CapFloorCMS capBumpedFunding = (CapFloorCMS) CMS_CAP_LONG_DEFINITION.toDerivative(REFERENCE_DATE, bumpedCurvesFundingName);
    final int nbPayDate = CMS_CAP_LONG_DEFINITION.getUnderlyingSwap().getIborLeg().getPayments().length;
    final YieldAndDiscountCurve curveFunding = curves.getCurve(FUNDING_CURVE_NAME);
    final double[] yieldsFunding = new double[nbPayDate + 1];
    final double[] nodeTimesFunding = new double[nbPayDate + 1];
    yieldsFunding[0] = curveFunding.getInterestRate(0.0);
    for (int i = 0; i < nbPayDate; i++) {
      nodeTimesFunding[i + 1] = CMS_CAP_LONG.getUnderlyingSwap().getSecondLeg().getNthPayment(i).getPaymentTime();
      yieldsFunding[i + 1] = curveFunding.getInterestRate(nodeTimesFunding[i + 1]);
    }
    final YieldAndDiscountCurve tempCurveFunding = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(nodeTimesFunding, yieldsFunding, new LinearInterpolator1D()));
    final List<DoublesPair> tempFunding = pvsCapLong.getSensitivities().get(FUNDING_CURVE_NAME);
    final double[] resDsc = new double[nbPayDate];
    for (int i = 0; i < nbPayDate; i++) {
      final YieldAndDiscountCurve bumpedCurve = tempCurveFunding.withSingleShift(nodeTimesFunding[i + 1], deltaShift);
      final YieldCurveBundle curvesBumped = new YieldCurveBundle();
      curvesBumped.addAll(curves);
      curvesBumped.setCurve("Bumped Curve", bumpedCurve);
      final SABRInterestRateDataBundle sabrBundleBumped = new SABRInterestRateDataBundle(sabrParameter, curvesBumped);
      final double bumpedpv = METHOD_EXTRAPOLATION_CAP.presentValue(capBumpedFunding, sabrBundleBumped).getAmount();
      resDsc[i] = (bumpedpv - pv) / deltaShift;
      final DoublesPair pair = tempFunding.get(i);
      assertEquals("Sensitivity to discounting curve: Node " + i, nodeTimesFunding[i + 1], pair.getFirst(), 1E-8);
      assertEquals("Sensitivity to discounting curve: Node " + i, resDsc[i], pair.getSecond(), deltaTolerance);
    }
  }

  @Test
  /**
   * Test the present value rate sensitivity for a CMS cap with pricing by replication in the SABR with extrapolation framework. Method v Calculator.
   */
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final InterestRateCurveSensitivity pvcsMethod = METHOD_EXTRAPOLATION_CAP.presentValueCurveSensitivity(CMS_CAP_LONG, SABR_BUNDLE);
    final InterestRateCurveSensitivity pvcsCalculator = new InterestRateCurveSensitivity(CMS_CAP_LONG.accept(PVCSC_EXTRA_SABR, SABR_BUNDLE));
    AssertSensivityObjects.assertEquals("CMS cap/floor SABR: Present value : method vs calculator", pvcsMethod, pvcsCalculator, TOLERANCE_DELTA);
  }

  @Test
  /**
   * Tests the cap present value SABR parameters sensitivity vs finite difference.
   */
  public void presentValueSABRSensitivity() {
    final SABRInterestRateParameters sabrParameter = TestsDataSetsSABR.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, CURVES);
    final double pv = METHOD_EXTRAPOLATION_CAP.presentValue(CMS_CAP_LONG, sabrBundle).getAmount();
    final PresentValueSABRSensitivityDataBundle pvsCapLong = METHOD_EXTRAPOLATION_CAP.presentValueSABRSensitivity(CMS_CAP_LONG, sabrBundle);
    // SABR sensitivity vs finite difference
    final double shift = 0.0001;
    final double shiftAlpha = 0.00001;
    final double maturity = CMS_CAP_LONG.getUnderlyingSwap().getFixedLeg().getNthPayment(CMS_CAP_LONG.getUnderlyingSwap().getFixedLeg().getNumberOfPayments() - 1).getPaymentTime()
        - CMS_CAP_LONG.getSettlementTime();
    final DoublesPair expectedExpiryTenor = DoublesPair.of(CMS_CAP_LONG.getFixingTime(), maturity);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterAlphaBumped = TestsDataSetsSABR.createSABR1AlphaBumped(shiftAlpha);
    final SABRInterestRateDataBundle sabrBundleAlphaBumped = new SABRInterestRateDataBundle(sabrParameterAlphaBumped, CURVES);
    final double pvLongPayerAlphaBumped = METHOD_EXTRAPOLATION_CAP.presentValue(CMS_CAP_LONG, sabrBundleAlphaBumped).getAmount();
    final double expectedAlphaSensi = (pvLongPayerAlphaBumped - pv) / shiftAlpha;
    assertEquals("Number of alpha sensitivity", pvsCapLong.getAlpha().getMap().keySet().size(), 1);
    assertEquals("Alpha sensitivity expiry/tenor", pvsCapLong.getAlpha().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Alpha sensitivity value", expectedAlphaSensi, pvsCapLong.getAlpha().getMap().get(expectedExpiryTenor), TOLERANCE_DELTA);
    // Rho sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterRhoBumped = TestsDataSetsSABR.createSABR1RhoBumped();
    final SABRInterestRateDataBundle sabrBundleRhoBumped = new SABRInterestRateDataBundle(sabrParameterRhoBumped, CURVES);
    final double pvLongPayerRhoBumped = METHOD_EXTRAPOLATION_CAP.presentValue(CMS_CAP_LONG, sabrBundleRhoBumped).getAmount();
    final double expectedRhoSensi = (pvLongPayerRhoBumped - pv) / shift;
    assertEquals("Number of rho sensitivity", pvsCapLong.getRho().getMap().keySet().size(), 1);
    assertEquals("Rho sensitivity expiry/tenor", pvsCapLong.getRho().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Rho sensitivity value", expectedRhoSensi, pvsCapLong.getRho().getMap().get(expectedExpiryTenor), TOLERANCE_DELTA);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterNuBumped = TestsDataSetsSABR.createSABR1NuBumped();
    final SABRInterestRateDataBundle sabrBundleNuBumped = new SABRInterestRateDataBundle(sabrParameterNuBumped, CURVES);
    final double pvLongPayerNuBumped = METHOD_EXTRAPOLATION_CAP.presentValue(CMS_CAP_LONG, sabrBundleNuBumped).getAmount();
    final double expectedNuSensi = (pvLongPayerNuBumped - pv) / shift;
    assertEquals("Number of nu sensitivity", pvsCapLong.getNu().getMap().keySet().size(), 1);
    assertTrue("Nu sensitivity expiry/tenor", pvsCapLong.getNu().getMap().keySet().contains(expectedExpiryTenor));
    assertEquals("Nu sensitivity value", expectedNuSensi, pvsCapLong.getNu().getMap().get(expectedExpiryTenor), TOLERANCE_DELTA);
  }

  @Test
  /**
   * Tests the coupon present value SABR parameters sensitivity vs finite difference.
   */
  public void presentValueSABRSensitivityCoupon() {
    final SABRInterestRateParameters sabrParameter = TestsDataSetsSABR.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, CURVES);
    final double pv = METHOD_EXTRAPOLATION_CPN.presentValue(CMS_COUPON, sabrBundle).getAmount();
    final PresentValueSABRSensitivityDataBundle pvsCpn = METHOD_EXTRAPOLATION_CPN.presentValueSABRSensitivity(CMS_COUPON, sabrBundle);
    // SABR sensitivity vs finite difference
    final double shift = 0.0001;
    final double shiftAlpha = 0.00001;
    final double maturity = CMS_COUPON.getUnderlyingSwap().getFixedLeg().getNthPayment(CMS_COUPON.getUnderlyingSwap().getFixedLeg().getNumberOfPayments() - 1).getPaymentTime()
        - CMS_COUPON.getSettlementTime();
    final DoublesPair expectedExpiryTenor = DoublesPair.of(CMS_COUPON.getFixingTime(), maturity);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterAlphaBumped = TestsDataSetsSABR.createSABR1AlphaBumped(shiftAlpha);
    final SABRInterestRateDataBundle sabrBundleAlphaBumped = new SABRInterestRateDataBundle(sabrParameterAlphaBumped, CURVES);
    final double pvLongPayerAlphaBumped = METHOD_EXTRAPOLATION_CPN.presentValue(CMS_COUPON, sabrBundleAlphaBumped).getAmount();
    final double expectedAlphaSensi = (pvLongPayerAlphaBumped - pv) / shiftAlpha;
    assertEquals("Number of alpha sensitivity", pvsCpn.getAlpha().getMap().keySet().size(), 1);
    assertEquals("Alpha sensitivity expiry/tenor", pvsCpn.getAlpha().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Alpha sensitivity value", expectedAlphaSensi, pvsCpn.getAlpha().getMap().get(expectedExpiryTenor), TOLERANCE_DELTA);
    // Rho sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterRhoBumped = TestsDataSetsSABR.createSABR1RhoBumped();
    final SABRInterestRateDataBundle sabrBundleRhoBumped = new SABRInterestRateDataBundle(sabrParameterRhoBumped, CURVES);
    final double pvLongPayerRhoBumped = METHOD_EXTRAPOLATION_CPN.presentValue(CMS_COUPON, sabrBundleRhoBumped).getAmount();
    final double expectedRhoSensi = (pvLongPayerRhoBumped - pv) / shift;
    assertEquals("Number of rho sensitivity", pvsCpn.getRho().getMap().keySet().size(), 1);
    assertEquals("Rho sensitivity expiry/tenor", pvsCpn.getRho().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Rho sensitivity value", expectedRhoSensi, pvsCpn.getRho().getMap().get(expectedExpiryTenor), TOLERANCE_DELTA);
    // Nu sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterNuBumped = TestsDataSetsSABR.createSABR1NuBumped();
    final SABRInterestRateDataBundle sabrBundleNuBumped = new SABRInterestRateDataBundle(sabrParameterNuBumped, CURVES);
    final double pvLongPayerNuBumped = METHOD_EXTRAPOLATION_CPN.presentValue(CMS_COUPON, sabrBundleNuBumped).getAmount();
    final double expectedNuSensi = (pvLongPayerNuBumped - pv) / shift;
    assertEquals("Number of nu sensitivity", pvsCpn.getNu().getMap().keySet().size(), 1);
    assertTrue("Nu sensitivity expiry/tenor", pvsCpn.getNu().getMap().keySet().contains(expectedExpiryTenor));
    assertEquals("Nu sensitivity value", expectedNuSensi, pvsCpn.getNu().getMap().get(expectedExpiryTenor), TOLERANCE_DELTA);
  }

  @Test
  /**
   * Tests the present value SABR parameters sensitivity: Method vs Calculator.
   */
  public void presentValueSABRSensitivityMethodVsCalculator() {
    final PresentValueSABRSensitivityDataBundle pvssMethod = METHOD_EXTRAPOLATION_CAP.presentValueSABRSensitivity(CMS_CAP_LONG, SABR_BUNDLE);
    final PresentValueSABRSensitivityDataBundle pvssCalculator = CMS_CAP_LONG.accept(PVSSC_SABR, SABR_BUNDLE);
    assertEquals("CMS cap/floor SABR: Present value SABR sensitivity: method vs calculator", pvssMethod, pvssCalculator);
  }

  @Test
  /**
   * Tests the present value strike sensitivity: Cap.
   */
  public void presentValueStrikeSensitivityCap() {
    final double[] strikes = new double[] {0.0001, 0.0010, 0.0050, 0.0100, 0.0200, 0.0400, 0.0500 };
    final int nbStrikes = strikes.length;
    final double shift = 1.0E-5;
    final double[] errorRelative = new double[nbStrikes];
    for (int loopstrike = 0; loopstrike < nbStrikes; loopstrike++) {
      final CapFloorCMSDefinition cmsCapDefinition = CapFloorCMSDefinition.from(CMS_COUPON_RECEIVER_DEFINITION, strikes[loopstrike], IS_CAP);
      final CapFloorCMSDefinition cmsCapShiftUpDefinition = CapFloorCMSDefinition.from(CMS_COUPON_RECEIVER_DEFINITION, strikes[loopstrike] + shift, IS_CAP);
      final CapFloorCMSDefinition cmsCapShiftDoDefinition = CapFloorCMSDefinition.from(CMS_COUPON_RECEIVER_DEFINITION, strikes[loopstrike] - shift, IS_CAP);
      final CapFloorCMS cmsCap = (CapFloorCMS) cmsCapDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
      final CapFloorCMS cmsCapShiftUp = (CapFloorCMS) cmsCapShiftUpDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
      final CapFloorCMS cmsCapShiftDo = (CapFloorCMS) cmsCapShiftDoDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
      final double pvShiftUp = METHOD_EXTRAPOLATION_CAP.presentValue(cmsCapShiftUp, SABR_BUNDLE).getAmount();
      final double pvShiftDo = METHOD_EXTRAPOLATION_CAP.presentValue(cmsCapShiftDo, SABR_BUNDLE).getAmount();
      final double sensiExpected = (pvShiftUp - pvShiftDo) / (2 * shift);
      final double sensiComputed = METHOD_EXTRAPOLATION_CAP.presentValueStrikeSensitivity(cmsCap, SABR_BUNDLE);
      errorRelative[loopstrike] = (sensiExpected - sensiComputed) / sensiExpected;
      assertEquals("CMS cap/floor SABR: Present value strike sensitivity " + loopstrike, 0, errorRelative[loopstrike], 5.0E-3);
    }
  }

  @Test(enabled = false)
  /**
   * Tests to estimate the impact of mu on the CMS coupon pricing. "enabled = false" for the standard testing.
   */
  public void testPriceMultiMu() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSetsSABR.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    final double[] mu = new double[] {1.10, 1.30, 1.55, 2.25, 3.50, 6.00, 15.0 };
    final int nbMu = mu.length;
    final double priceCouponStd = METHOD_STANDARD_CPN.presentValue(CMS_COUPON, sabrBundle).getAmount();
    final double rateCouponStd = priceCouponStd / (CMS_COUPON.getPaymentYearFraction() * CMS_COUPON.getNotional() * curves.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(CMS_COUPON.getPaymentTime()));
    final double[] priceCouponExtra = new double[nbMu];
    final double[] rateCouponExtra = new double[nbMu];
    for (int loopmu = 0; loopmu < nbMu; loopmu++) {
      final CouponCMSSABRExtrapolationRightReplicationMethod methodExtrapolation = new CouponCMSSABRExtrapolationRightReplicationMethod(CUT_OFF_STRIKE, mu[loopmu]);
      priceCouponExtra[loopmu] = methodExtrapolation.presentValue(CMS_COUPON, sabrBundle).getAmount();
      rateCouponExtra[loopmu] = priceCouponExtra[loopmu]
          / (CMS_COUPON.getPaymentYearFraction() * CMS_COUPON.getNotional() * curves.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(CMS_COUPON.getPaymentTime()));
    }
    final double priceCouponNoAdj = CMS_COUPON.accept(PVC, curves);
    final double rateCouponNoAdj = priceCouponNoAdj
        / (CMS_COUPON.getPaymentYearFraction() * CMS_COUPON.getNotional() * curves.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(CMS_COUPON.getPaymentTime()));
    assertEquals("Extrapolation: comparison with standard method", rateCouponStd > rateCouponExtra[0], true);
    for (int loopmu = 1; loopmu < nbMu; loopmu++) {
      assertEquals("Extrapolation: comparison with standard method", rateCouponExtra[loopmu - 1] > rateCouponExtra[loopmu], true);
    }
    assertEquals("Extrapolation: comparison with standard method", rateCouponExtra[nbMu - 1] > rateCouponNoAdj, true);
  }

  @Test(enabled = false)
  /**
   * Tests of performance. "enabled = false" for the standard testing.
   */
  public void performanceCoupon() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSetsSABR.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    long startTime, endTime;
    final int nbTest = 1000;
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      METHOD_STANDARD_CPN.presentValue(CMS_COUPON, sabrBundle);
      METHOD_STANDARD_CPN.presentValueCurveSensitivity(CMS_COUPON, sabrBundle);
      METHOD_STANDARD_CPN.presentValueSABRSensitivity(CMS_COUPON, sabrBundle);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " CMS coupon by replication SABR standard (price+delta+vega): " + (endTime - startTime) + " ms");
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      METHOD_EXTRAPOLATION_CPN.presentValue(CMS_COUPON, sabrBundle);
      METHOD_EXTRAPOLATION_CPN.presentValueCurveSensitivity(CMS_COUPON, sabrBundle);
      METHOD_EXTRAPOLATION_CPN.presentValueSABRSensitivity(CMS_COUPON, sabrBundle);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " CMS coupon by replication SABR with extrapolation (price+delta+vega): " + (endTime - startTime) + " ms");
    // Performance note: price (standard SABR): 15-Nov-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 55 ms for 1000 CMS coupon 5Y.
    // Performance note: price (SABR with extrapolation): 15-Nov-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 80 ms for 1000 CMS coupon 5Y.
    // Performance note: price+delta (standard SABR): 15-Nov-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 215 ms for 1000 CMS coupon 5Y.
    // Performance note: price+delta (SABR with extrapolation): 15-Nov-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 455 ms for 1000 CMS coupon 5Y.
    // Performance note: price+delta+vega (standard SABR): 18-Apr-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 710 ms for 1000 CMS coupon 5Y.
    // Performance note: price+delta+vega (SABR with extrapolation): 18-Apr-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 690 ms for 1000 CMS coupon 5Y.
  }

  @Test(enabled = false)
  /**
   * Tests of performance. "enabled = false" for the standard testing.
   */
  public void performanceCap() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSetsSABR.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    long startTime, endTime;
    final int nbTest = 1000;

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      METHOD_STANDARD_CAP.presentValue(CMS_CAP_LONG, sabrBundle);
      METHOD_STANDARD_CAP.presentValueCurveSensitivity(CMS_CAP_LONG, sabrBundle);
      METHOD_STANDARD_CAP.presentValueSABRSensitivity(CMS_CAP_LONG, sabrBundle);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " CMS coupon by replication SABR standard (price+delta+vega): " + (endTime - startTime) + " ms");
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      METHOD_EXTRAPOLATION_CAP.presentValue(CMS_CAP_LONG, sabrBundle);
      METHOD_EXTRAPOLATION_CAP.presentValueCurveSensitivity(CMS_CAP_LONG, sabrBundle);
      METHOD_EXTRAPOLATION_CAP.presentValueSABRSensitivity(CMS_CAP_LONG, sabrBundle);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " CMS coupon by replication SABR with extrapolation (price+delta+vega): " + (endTime - startTime) + " ms");
    // Performance note: price+delta+vega (standard SABR): 18-Apr-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 700 ms for 1000 CMS cap 5Y.
    // Performance note: price+delta+vega (SABR with extrapolation): 18-Apr-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 500 ms for 1000 CMS cap 5Y.
  }

}
