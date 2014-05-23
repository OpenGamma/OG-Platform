/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.method;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import it.unimi.dsi.fastutil.doubles.DoubleAVLTreeSet;

import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCapFloorCMSDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.payment.CapFloorCMSDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivitySABRCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivitySABRCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.method.SensitivityFiniteDifference;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
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
 *  Test class for the replication method for CMS caplet/floorlet with a SABR smile.
 *  @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class CapFloorCMSSABRReplicationMethodTest {
  //Swap 5Y
  private static final Currency CUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Period ANNUITY_TENOR = Period.ofYears(5);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2011, 3, 17);
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
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM, "Ibor");
  private static final AnnuityCouponIborDefinition IBOR_ANNUITY = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, 1.0, IBOR_INDEX, !FIXED_IS_PAYER, CALENDAR);
  // CMS coupon construction
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, ANNUITY_TENOR, CALENDAR);
  private static final SwapFixedIborDefinition SWAP_DEFINITION = new SwapFixedIborDefinition(FIXED_ANNUITY, IBOR_ANNUITY);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 4, 6);
  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2010, 12, 30);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 1, 5);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 4, 5);
  private static final DayCount PAYMENT_DAY_COUNT = DayCounts.ACT_360;
  private static final double ACCRUAL_FACTOR = PAYMENT_DAY_COUNT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 10000000; //10m
  private static final CouponCMSDefinition CMS_COUPON_DEFINITION = CouponCMSDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION,
      CMS_INDEX);
  // Cap/Floor construction
  private static final double STRIKE = 0.02;
  private static final boolean IS_CAP = true;
  private static final CapFloorCMSDefinition CMS_CAP_DEFINITION = CapFloorCMSDefinition.from(CMS_COUPON_DEFINITION, STRIKE, IS_CAP);
  private static final CapFloorCMSDefinition CMS_CAP_0_DEFINITION = CapFloorCMSDefinition.from(CMS_COUPON_DEFINITION, 0.0, IS_CAP);
  private static final CapFloorCMSDefinition CMS_FLOOR_DEFINITION = CapFloorCMSDefinition.from(CMS_COUPON_DEFINITION, STRIKE, !IS_CAP);
  private static final CouponFixedDefinition COUPON_STRIKE_DEFINITION = new CouponFixedDefinition(CMS_COUPON_DEFINITION, STRIKE);
  // to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME };
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
  private static final SABRInterestRateParameters SABR_PARAMETER = TestsDataSetsSABR.createSABR1();
  private static final SABRInterestRateDataBundle SABR_BUNDLE = new SABRInterestRateDataBundle(SABR_PARAMETER, CURVES);

  private static final CouponCMS CMS_COUPON = (CouponCMS) CMS_COUPON_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final CapFloorCMS CMS_CAP_0 = (CapFloorCMS) CMS_CAP_0_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final CapFloorCMS CMS_CAP = (CapFloorCMS) CMS_CAP_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final CapFloorCMS CMS_FLOOR = (CapFloorCMS) CMS_FLOOR_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final CouponFixed COUPON_STRIKE = COUPON_STRIKE_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  // Calculators
  private static final PresentValueSABRCalculator PVC_SABR = PresentValueSABRCalculator.getInstance();
  private static final PresentValueCurveSensitivitySABRCalculator PVCSC_SABR = PresentValueCurveSensitivitySABRCalculator.getInstance();
  private static final PresentValueSABRSensitivitySABRCalculator PVSSC_SABR = PresentValueSABRSensitivitySABRCalculator.getInstance();
  private static final CapFloorCMSSABRReplicationMethod METHOD = CapFloorCMSSABRReplicationMethod.getDefaultInstance();

  private static final GeneratorSwapFixedIbor USD_GENERATOR = GeneratorSwapFixedIborMaster.getInstance().getGenerator("USD6MLIBOR3M", CALENDAR);
  private static final IndexSwap USD_SWAP_10Y = new IndexSwap(USD_GENERATOR, Period.ofYears(5));
  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, USD_GENERATOR.getIborIndex().getSpotLag(), CALENDAR);

  @Test
  /**
   * Tests the price of CMS coupon and cap/floor using replication in the SABR framework. Values are tested against hard-coded values.
   */
  public void testPriceReplication() {
    // CMS cap/floor with strike 0 has the same price as a CMS coupon.
    final double priceCMSCoupon = CMS_COUPON.accept(PVC_SABR, SABR_BUNDLE);
    final double priceCMSCap0 = CMS_CAP_0.accept(PVC_SABR, SABR_BUNDLE);
    assertEquals(priceCMSCoupon, priceCMSCap0, 1E-2);
    final double priceCMSCap = CMS_CAP.accept(PVC_SABR, SABR_BUNDLE);
    assertEquals(48695.371, priceCMSCap, 1E-2); //From previous run
    final double priceCMSFloor = CMS_FLOOR.accept(PVC_SABR, SABR_BUNDLE);
    assertEquals(1981.190, priceCMSFloor, 1E-2); //From previous run
    final double priceStrike = COUPON_STRIKE.accept(PVC_SABR, CURVES);
    // Cap/floor parity: !cash-settled swaption price is arbitrable: no exact cap/floor/swap parity!
    assertEquals(priceCMSCap - priceCMSFloor, priceCMSCoupon - priceStrike, 2.0E+2);
  }

  @Test
  /**
   * Tests the present value of an annuity vs the sum of pv of each caplet.
   */
  public void presentValueAnnuity() {
    final Period START_CMSCAP = Period.ofYears(5);
    final Period LENGTH_CMSCAP = Period.ofYears(10);
    final ZonedDateTime START_DATE = ScheduleCalculator.getAdjustedDate(SPOT_DATE, START_CMSCAP, USD_GENERATOR.getIborIndex().getBusinessDayConvention(), CALENDAR, USD_GENERATOR.getIborIndex()
        .isEndOfMonth());
    final ZonedDateTime END_DATE = START_DATE.plus(LENGTH_CMSCAP);
    final Period capPeriod = Period.ofMonths(6);
    final DayCount capDayCount = DayCounts.ACT_360;
    final AnnuityCapFloorCMSDefinition capDefinition = AnnuityCapFloorCMSDefinition.from(START_DATE, END_DATE, NOTIONAL, USD_SWAP_10Y, capPeriod, capDayCount, false, STRIKE, IS_CAP, CALENDAR);
    final Annuity<? extends Payment> cap = capDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final double pvCalculator = cap.accept(PVC_SABR, SABR_BUNDLE);
    double pvExpected = 0.0;
    for (int loopcpn = 0; loopcpn < cap.getNumberOfPayments(); loopcpn++) {
      pvExpected += cap.getNthPayment(loopcpn).accept(PVC_SABR, SABR_BUNDLE);
    }
    assertEquals("Cap annuity - SABR pv", pvExpected, pvCalculator, 1.0E-2);
  }

  @Test
  /**
   * Tests the price of CMS coupon and cap/floor using replication in the SABR framework. Values are tested against hard-coded values.
   */
  public void presentValueMethodVsCalculator() {
    final double pvMethod = METHOD.presentValue(CMS_CAP, SABR_BUNDLE).getAmount();
    final double pvCalculator = CMS_CAP.accept(PVC_SABR, SABR_BUNDLE);
    assertEquals("CMS cap/floor SABR: Present value : method vs calculator", pvMethod, pvCalculator);
  }

  @Test
  /**
   * Tests the price curve sensitivity of CMS coupon and cap/floor using replication in the SABR framework. Values are tested against finite difference values.
   */
  public void presentValueCurveSensitivityCap() {
    InterestRateCurveSensitivity pvcsCap = METHOD.presentValueCurveSensitivity(CMS_CAP, SABR_BUNDLE);
    pvcsCap = pvcsCap.cleaned();
    final double deltaTolerancePrice = 1.0E+2;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move.
    final double deltaShift = 1.0E-6;
    final String bumpedCurveName = "Bumped Curve";
    // 1. Forward curve sensitivity
    final String[] CurveNameBumpedForward = {FUNDING_CURVE_NAME, bumpedCurveName };
    final CapFloorCMS capBumpedForward = (CapFloorCMS) CMS_CAP_DEFINITION.toDerivative(REFERENCE_DATE, CurveNameBumpedForward);
    final DoubleAVLTreeSet forwardTime = new DoubleAVLTreeSet();
    for (int loopcpn = 0; loopcpn < CMS_CAP.getUnderlyingSwap().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      final CouponIbor cpn = (CouponIbor) CMS_CAP.getUnderlyingSwap().getSecondLeg().getNthPayment(loopcpn);
      forwardTime.add(cpn.getFixingPeriodStartTime());
      forwardTime.add(cpn.getFixingPeriodEndTime());
    }
    final double[] nodeTimesForward = forwardTime.toDoubleArray();
    final double[] sensiForwardMethod = SensitivityFiniteDifference.curveSensitivity(capBumpedForward, SABR_BUNDLE, FORWARD_CURVE_NAME, bumpedCurveName, nodeTimesForward, deltaShift, METHOD);
    assertEquals("Sensitivity finite difference method: number of node", nodeTimesForward.length, sensiForwardMethod.length);
    final List<DoublesPair> sensiPvForward = pvcsCap.getSensitivities().get(FORWARD_CURVE_NAME);
    for (int loopnode = 0; loopnode < sensiForwardMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvForward.get(loopnode);
      assertEquals("Sensitivity CMS cap/floor pv to forward curve: Node " + loopnode, nodeTimesForward[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity " + loopnode, pairPv.second, sensiForwardMethod[loopnode], deltaTolerancePrice);
    }
    // 2. Discounting curve sensitivity
    final String[] CurveNameBumpedDisc = {bumpedCurveName, FORWARD_CURVE_NAME };
    final CapFloorCMS capBumpedDisc = (CapFloorCMS) CMS_CAP_DEFINITION.toDerivative(REFERENCE_DATE, CurveNameBumpedDisc);
    final DoubleAVLTreeSet discTime = new DoubleAVLTreeSet();
    discTime.add(capBumpedDisc.getPaymentTime());
    for (int loopcpn = 0; loopcpn < CMS_CAP.getUnderlyingSwap().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      final CouponIbor cpn = (CouponIbor) CMS_CAP.getUnderlyingSwap().getSecondLeg().getNthPayment(loopcpn);
      discTime.add(cpn.getPaymentTime());
    }
    final double[] nodeTimesDisc = discTime.toDoubleArray();
    final double[] sensiDiscMethod = SensitivityFiniteDifference.curveSensitivity(capBumpedDisc, SABR_BUNDLE, FUNDING_CURVE_NAME, bumpedCurveName, nodeTimesDisc, deltaShift, METHOD);
    final List<DoublesPair> sensiPvDisc = pvcsCap.getSensitivities().get(FUNDING_CURVE_NAME);
    for (int loopnode = 0; loopnode < sensiDiscMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvDisc.get(loopnode);
      assertEquals("Sensitivity CMS cap/floor pv to forward curve: Node " + loopnode, nodeTimesDisc[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity " + loopnode, pairPv.second, sensiDiscMethod[loopnode], deltaTolerancePrice);
    }
  }

  @Test
  /**
   * Tests the price curve sensitivity of CMS coupon and cap/floor using replication in the SABR framework. Values are tested against finite difference values.
   */
  public void presentValueCurveSensitivityFloor() {
    InterestRateCurveSensitivity pvcsCap = METHOD.presentValueCurveSensitivity(CMS_FLOOR, SABR_BUNDLE);
    pvcsCap = pvcsCap.cleaned();
    final double deltaTolerancePrice = 1.0E+2;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move.
    final double deltaShift = 1.0E-6;
    final String bumpedCurveName = "Bumped Curve";
    // 1. Forward curve sensitivity
    final String[] CurveNameBumpedForward = {FUNDING_CURVE_NAME, bumpedCurveName };
    final CapFloorCMS capBumpedForward = (CapFloorCMS) CMS_FLOOR_DEFINITION.toDerivative(REFERENCE_DATE, CurveNameBumpedForward);
    final DoubleAVLTreeSet forwardTime = new DoubleAVLTreeSet();
    for (int loopcpn = 0; loopcpn < CMS_FLOOR.getUnderlyingSwap().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      final CouponIbor cpn = (CouponIbor) CMS_FLOOR.getUnderlyingSwap().getSecondLeg().getNthPayment(loopcpn);
      forwardTime.add(cpn.getFixingPeriodStartTime());
      forwardTime.add(cpn.getFixingPeriodEndTime());
    }
    final double[] nodeTimesForward = forwardTime.toDoubleArray();
    final double[] sensiForwardMethod = SensitivityFiniteDifference.curveSensitivity(capBumpedForward, SABR_BUNDLE, FORWARD_CURVE_NAME, bumpedCurveName, nodeTimesForward, deltaShift, METHOD);
    assertEquals("Sensitivity finite difference method: number of node", nodeTimesForward.length, sensiForwardMethod.length);
    final List<DoublesPair> sensiPvForward = pvcsCap.getSensitivities().get(FORWARD_CURVE_NAME);
    for (int loopnode = 0; loopnode < sensiForwardMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvForward.get(loopnode);
      assertEquals("Sensitivity CMS cap/floor pv to forward curve: Node " + loopnode, nodeTimesForward[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity " + loopnode, pairPv.second, sensiForwardMethod[loopnode], deltaTolerancePrice);
    }
    // 2. Discounting curve sensitivity
    final String[] CurveNameBumpedDisc = {bumpedCurveName, FORWARD_CURVE_NAME };
    final CapFloorCMS capBumpedDisc = (CapFloorCMS) CMS_FLOOR_DEFINITION.toDerivative(REFERENCE_DATE, CurveNameBumpedDisc);
    final DoubleAVLTreeSet discTime = new DoubleAVLTreeSet();
    discTime.add(capBumpedDisc.getPaymentTime());
    for (int loopcpn = 0; loopcpn < CMS_FLOOR.getUnderlyingSwap().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      final CouponIbor cpn = (CouponIbor) CMS_FLOOR.getUnderlyingSwap().getSecondLeg().getNthPayment(loopcpn);
      discTime.add(cpn.getPaymentTime());
    }
    final double[] nodeTimesDisc = discTime.toDoubleArray();
    final double[] sensiDiscMethod = SensitivityFiniteDifference.curveSensitivity(capBumpedDisc, SABR_BUNDLE, FUNDING_CURVE_NAME, bumpedCurveName, nodeTimesDisc, deltaShift, METHOD);
    final List<DoublesPair> sensiPvDisc = pvcsCap.getSensitivities().get(FUNDING_CURVE_NAME);
    for (int loopnode = 0; loopnode < sensiDiscMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvDisc.get(loopnode);
      assertEquals("Sensitivity CMS cap/floor pv to forward curve: Node " + loopnode, nodeTimesDisc[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity " + loopnode, pairPv.second, sensiDiscMethod[loopnode], deltaTolerancePrice);
    }
  }

  @Test
  /**
   * Tests the present value of an annuity vs the sum of pv of each caplet.
   */
  public void presentValueCurveSensitivityAnnuity() {
    final Period START_CMSCAP = Period.ofYears(5);
    final Period LENGTH_CMSCAP = Period.ofYears(10);
    final ZonedDateTime START_DATE = ScheduleCalculator.getAdjustedDate(SPOT_DATE, START_CMSCAP, USD_GENERATOR.getIborIndex().getBusinessDayConvention(), CALENDAR, USD_GENERATOR.getIborIndex()
        .isEndOfMonth());
    final ZonedDateTime END_DATE = START_DATE.plus(LENGTH_CMSCAP);
    final Period capPeriod = Period.ofMonths(6);
    final DayCount capDayCount = DayCounts.ACT_360;
    final AnnuityCapFloorCMSDefinition capDefinition = AnnuityCapFloorCMSDefinition.from(START_DATE, END_DATE, NOTIONAL, USD_SWAP_10Y, capPeriod, capDayCount, false, STRIKE, IS_CAP, CALENDAR);
    final Annuity<? extends Payment> cap = capDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    InterestRateCurveSensitivity pvcsCalculator = new InterestRateCurveSensitivity(cap.accept(PVCSC_SABR, SABR_BUNDLE));
    pvcsCalculator = pvcsCalculator.cleaned();
    InterestRateCurveSensitivity pvcsExpected = new InterestRateCurveSensitivity();
    for (int loopcpn = 0; loopcpn < cap.getNumberOfPayments(); loopcpn++) {
      pvcsExpected = pvcsExpected.plus(new InterestRateCurveSensitivity(cap.getNthPayment(loopcpn).accept(PVCSC_SABR, SABR_BUNDLE)));
    }
    pvcsExpected = pvcsExpected.cleaned();
    AssertSensitivityObjects.assertEquals("Cap annuity - SABR pv", pvcsExpected, pvcsCalculator, 1.0E-2);
  }

  @Test
  /**
   * Tests the present value SABR parameters sensitivity vs finite difference.
   */
  public void presentValueSABRSensitivity() {
    final double pv = METHOD.presentValue(CMS_CAP, SABR_BUNDLE).getAmount();
    final PresentValueSABRSensitivityDataBundle pvsCapLong = METHOD.presentValueSABRSensitivity(CMS_CAP, SABR_BUNDLE);
    // SABR sensitivity vs finite difference
    final double shift = 0.0001;
    final double shiftAlpha = 0.00001;
    final double maturity = CMS_CAP.getUnderlyingSwap().getFixedLeg().getNthPayment(CMS_CAP.getUnderlyingSwap().getFixedLeg().getNumberOfPayments() - 1).getPaymentTime() - CMS_CAP.getSettlementTime();
    final DoublesPair expectedExpiryTenor = DoublesPair.of(CMS_CAP.getFixingTime(), maturity);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterAlphaBumped = TestsDataSetsSABR.createSABR1AlphaBumped(shiftAlpha);
    final SABRInterestRateDataBundle sabrBundleAlphaBumped = new SABRInterestRateDataBundle(sabrParameterAlphaBumped, CURVES);
    final double pvLongPayerAlphaBumped = METHOD.presentValue(CMS_CAP, sabrBundleAlphaBumped).getAmount();
    final double expectedAlphaSensi = (pvLongPayerAlphaBumped - pv) / shiftAlpha;
    assertEquals("Number of alpha sensitivity", pvsCapLong.getAlpha().getMap().keySet().size(), 1);
    assertEquals("Alpha sensitivity expiry/tenor", pvsCapLong.getAlpha().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Alpha sensitivity value", expectedAlphaSensi, pvsCapLong.getAlpha().getMap().get(expectedExpiryTenor), 3.0E+1);
    // Rho sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterRhoBumped = TestsDataSetsSABR.createSABR1RhoBumped();
    final SABRInterestRateDataBundle sabrBundleRhoBumped = new SABRInterestRateDataBundle(sabrParameterRhoBumped, CURVES);
    final double pvLongPayerRhoBumped = METHOD.presentValue(CMS_CAP, sabrBundleRhoBumped).getAmount();
    final double expectedRhoSensi = (pvLongPayerRhoBumped - pv) / shift;
    assertEquals("Number of rho sensitivity", pvsCapLong.getRho().getMap().keySet().size(), 1);
    assertEquals("Rho sensitivity expiry/tenor", pvsCapLong.getRho().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Rho sensitivity value", expectedRhoSensi, pvsCapLong.getRho().getMap().get(expectedExpiryTenor), 1.0E+0);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterNuBumped = TestsDataSetsSABR.createSABR1NuBumped();
    final SABRInterestRateDataBundle sabrBundleNuBumped = new SABRInterestRateDataBundle(sabrParameterNuBumped, CURVES);
    final double pvLongPayerNuBumped = METHOD.presentValue(CMS_CAP, sabrBundleNuBumped).getAmount();
    final double expectedNuSensi = (pvLongPayerNuBumped - pv) / shift;
    assertEquals("Number of nu sensitivity", pvsCapLong.getNu().getMap().keySet().size(), 1);
    assertTrue("Nu sensitivity expiry/tenor", pvsCapLong.getNu().getMap().keySet().contains(expectedExpiryTenor));
    assertEquals("Nu sensitivity value", expectedNuSensi, pvsCapLong.getNu().getMap().get(expectedExpiryTenor), 2.0E+0);
  }

  @Test
  /**
   * Tests the present value SABR parameters sensitivity: Method vs Calculator.
   */
  public void presentValueSABRSensitivityMethodVsCalculator() {
    final PresentValueSABRSensitivityDataBundle pvssMethod = METHOD.presentValueSABRSensitivity(CMS_CAP, SABR_BUNDLE);
    final PresentValueSABRSensitivityDataBundle pvssCalculator = CMS_CAP.accept(PVSSC_SABR, SABR_BUNDLE);
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
      final CapFloorCMSDefinition cmsCapDefinition = CapFloorCMSDefinition.from(CMS_COUPON_DEFINITION, strikes[loopstrike], IS_CAP);
      final CapFloorCMSDefinition cmsCapShiftUpDefinition = CapFloorCMSDefinition.from(CMS_COUPON_DEFINITION, strikes[loopstrike] + shift, IS_CAP);
      final CapFloorCMSDefinition cmsCapShiftDoDefinition = CapFloorCMSDefinition.from(CMS_COUPON_DEFINITION, strikes[loopstrike] - shift, IS_CAP);
      final CapFloorCMS cmsCap = (CapFloorCMS) cmsCapDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
      final CapFloorCMS cmsCapShiftUp = (CapFloorCMS) cmsCapShiftUpDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
      final CapFloorCMS cmsCapShiftDo = (CapFloorCMS) cmsCapShiftDoDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
      final double pvShiftUp = METHOD.presentValue(cmsCapShiftUp, SABR_BUNDLE).getAmount();
      final double pvShiftDo = METHOD.presentValue(cmsCapShiftDo, SABR_BUNDLE).getAmount();
      final double sensiExpected = (pvShiftUp - pvShiftDo) / (2 * shift);
      final double sensiComputed = METHOD.presentValueStrikeSensitivity(cmsCap, SABR_BUNDLE);
      errorRelative[loopstrike] = (sensiExpected - sensiComputed) / sensiExpected;
      assertEquals("CMS cap/floor SABR: Present value strike sensitivity " + loopstrike, 0, errorRelative[loopstrike], 5.0E-4); // Numerical imprecision, reduce to E-6 when nbInteration = 1000;
    }
  }

  @Test
  /**
   * Tests the present value strike sensitivity: Floor.
   */
  public void presentValueStrikeSensitivityFloor() {
    final double[] strikes = new double[] {0.0001, 0.0010, 0.0050, 0.0100, 0.0200, 0.0400 };
    final int nbStrikes = strikes.length;
    final double shift = 1.0E-5;
    final double[] errorRelative = new double[nbStrikes];
    for (int loopstrike = 0; loopstrike < nbStrikes; loopstrike++) {
      final CapFloorCMSDefinition cmsFloorDefinition = CapFloorCMSDefinition.from(CMS_COUPON_DEFINITION, strikes[loopstrike], !IS_CAP);
      final CapFloorCMSDefinition cmsFloorShiftUpDefinition = CapFloorCMSDefinition.from(CMS_COUPON_DEFINITION, strikes[loopstrike] + shift, !IS_CAP);
      final CapFloorCMSDefinition cmsFloorShiftDoDefinition = CapFloorCMSDefinition.from(CMS_COUPON_DEFINITION, strikes[loopstrike] - shift, !IS_CAP);
      final CapFloorCMS cmsFloor = (CapFloorCMS) cmsFloorDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
      final CapFloorCMS cmsFloorShiftUp = (CapFloorCMS) cmsFloorShiftUpDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
      final CapFloorCMS cmsFloorShiftDo = (CapFloorCMS) cmsFloorShiftDoDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
      final double pvShiftUp = METHOD.presentValue(cmsFloorShiftUp, SABR_BUNDLE).getAmount();
      final double pvShiftDo = METHOD.presentValue(cmsFloorShiftDo, SABR_BUNDLE).getAmount();
      final double sensiExpected = (pvShiftUp - pvShiftDo) / (2 * shift);
      final double sensiComputed = METHOD.presentValueStrikeSensitivity(cmsFloor, SABR_BUNDLE);
      errorRelative[loopstrike] = (sensiExpected - sensiComputed) / sensiExpected;
      assertEquals("CMS cap/floor SABR: Present value strike sensitivity " + loopstrike, 0, errorRelative[loopstrike], 3.0E-5);
    }
  }

  @Test(enabled = false)
  /**
   * Tests of performance. "enabled = false" for the standard testing.
   */
  public void performance() {
    long startTime, endTime;
    final int nbTest = 1000;

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      CMS_CAP.accept(PVC_SABR, SABR_BUNDLE);
      CMS_CAP.accept(PVCSC_SABR, SABR_BUNDLE);
      CMS_CAP.accept(PVSSC_SABR, SABR_BUNDLE);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " CMS cap by replication (price+delta+vega): " + (endTime - startTime) + " ms");
    // Performance note: price+delta: 9-Dec-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 280 ms for 1000 cap 5Y.
    // Performance note: price+delta+vega: 9-Dec-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 790 ms for 1000 cap 5Y.

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      CMS_FLOOR.accept(PVC_SABR, SABR_BUNDLE);
      CMS_FLOOR.accept(PVCSC_SABR, SABR_BUNDLE);
      CMS_FLOOR.accept(PVSSC_SABR, SABR_BUNDLE);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " CMS floor by replication (price+delta+vega): " + (endTime - startTime) + " ms");
    // Performance note: price+delta: 9-Dec-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 230 ms for 1000 floor 5Y.
    // Performance note: price+delta+vega: 9-Dec-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 555 ms for 1000 cap 5Y.
  }

}
