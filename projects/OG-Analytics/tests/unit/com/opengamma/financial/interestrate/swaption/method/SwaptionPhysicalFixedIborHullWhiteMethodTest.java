/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swaption.method;

import static org.testng.AssertJUnit.assertEquals;
import it.unimi.dsi.fastutil.doubles.DoubleAVLTreeSet;

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
import com.opengamma.financial.instrument.index.CMSIndex;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.financial.interestrate.CashFlowEquivalentCalculator;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityPaymentFixed;
import com.opengamma.financial.interestrate.method.SensitivityFiniteDifference;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.financial.model.interestrate.HullWhiteTestsDataSet;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Tests related to the pricing of physical delivery swaption in Hull-White one factor model.
 */
public class SwaptionPhysicalFixedIborHullWhiteMethodTest {
  private static final Currency CUR = Currency.USD;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final int SETTLEMENT_DAYS = 2;
  private static final Period IBOR_TENOR = Period.ofMonths(3);
  private static final DayCount IBOR_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, IBOR_TENOR, SETTLEMENT_DAYS, CALENDAR, IBOR_DAY_COUNT, BUSINESS_DAY, IS_EOM);
  private static final int SWAP_TENOR_YEAR = 5;
  private static final Period SWAP_TENOR = Period.ofYears(SWAP_TENOR_YEAR);
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final CMSIndex CMS_INDEX = new CMSIndex(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, SWAP_TENOR);
  private static final ZonedDateTime EXPIRY_DATE = DateUtil.getUTCDate(2016, 7, 7);
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, CALENDAR, SETTLEMENT_DAYS);
  private static final double NOTIONAL = 100000000; //100m
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final SwapFixedIborDefinition SWAP_PAYER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER);
  private static final SwapFixedIborDefinition SWAP_RECEIVER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, !FIXED_IS_PAYER);
  private static final boolean IS_LONG = true;
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_PAYER_LONG_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_RECEIVER_LONG_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_DEFINITION, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_PAYER_SHORT_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, !IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_RECEIVER_SHORT_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_DEFINITION, !IS_LONG);
  //to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtil.getUTCDate(2011, 7, 7);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final YieldCurveBundle CURVES = TestsDataSets.createCurves1();
  private static final FixedCouponSwap<Coupon> SWAP_RECEIVER = SWAP_RECEIVER_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_PAYER_LONG = SWAPTION_PAYER_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_RECEIVER_LONG = SWAPTION_RECEIVER_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_PAYER_SHORT = SWAPTION_PAYER_SHORT_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_RECEIVER_SHORT = SWAPTION_RECEIVER_SHORT_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  // Calculator
  private static final CashFlowEquivalentCalculator CFEC = CashFlowEquivalentCalculator.getInstance();
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final SwaptionPhysicalFixedIborHullWhiteMethod METHOD_HW = new SwaptionPhysicalFixedIborHullWhiteMethod();
  private static final SwaptionPhysicalFixedIborHullWhiteNumericalIntegrationMethod METHOD_HW_INTEGRATION = new SwaptionPhysicalFixedIborHullWhiteNumericalIntegrationMethod();
  private static final HullWhiteOneFactorPiecewiseConstantParameters PARAMETERS_HW = HullWhiteTestsDataSet.createHullWhiteParameters();
  private static final HullWhiteOneFactorPiecewiseConstantDataBundle BUNDLE_HW = new HullWhiteOneFactorPiecewiseConstantDataBundle(PARAMETERS_HW, CURVES);
  private static final HullWhiteOneFactorPiecewiseConstantInterestRateModel MODEL = new HullWhiteOneFactorPiecewiseConstantInterestRateModel();
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  @Test
  /**
   * Test the present value.
   */
  public void presentValue() {
    CurrencyAmount pv = METHOD_HW.presentValue(SWAPTION_PAYER_LONG, BUNDLE_HW);
    double timeToExpiry = SWAPTION_PAYER_LONG.getTimeToExpiry();
    AnnuityPaymentFixed cfe = CFEC.visitSwap(SWAPTION_PAYER_LONG.getUnderlyingSwap(), CURVES);
    int numberOfPayments = cfe.getNumberOfPayments();
    double alpha[] = new double[numberOfPayments];
    double disccf[] = new double[numberOfPayments];
    for (int loopcf = 0; loopcf < numberOfPayments; loopcf++) {
      alpha[loopcf] = MODEL.alpha(0.0, timeToExpiry, timeToExpiry, cfe.getNthPayment(loopcf).getPaymentTime(), PARAMETERS_HW);
      disccf[loopcf] = CURVES.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(cfe.getNthPayment(loopcf).getPaymentTime()) * cfe.getNthPayment(loopcf).getAmount();
    }
    double kappa = MODEL.kappa(disccf, alpha);
    double pvExpected = 0.0;
    for (int loopcf = 0; loopcf < numberOfPayments; loopcf++) {
      pvExpected += disccf[loopcf] * NORMAL.getCDF(-kappa - alpha[loopcf]);
    }
    assertEquals("Swaption physical - Hull-White - present value", pvExpected, pv.getAmount(), 1E-2);
    CurrencyAmount pv2 = METHOD_HW.presentValue(SWAPTION_PAYER_LONG, cfe, BUNDLE_HW);
    assertEquals("Swaption physical - Hull-White - present value", pv, pv2);
  }

  @Test
  /**
   * Tests long/short parity.
   */
  public void longShortParity() {
    CurrencyAmount pvLong = METHOD_HW.presentValue(SWAPTION_PAYER_LONG, BUNDLE_HW);
    CurrencyAmount pvShort = METHOD_HW.presentValue(SWAPTION_PAYER_SHORT, BUNDLE_HW);
    assertEquals("Swaption physical - Hull-White - present value - long/short parity", pvLong.getAmount(), -pvShort.getAmount(), 1E-2);
  }

  @Test
  /**
   * Tests payer/receiver/swap parity.
   */
  public void payerReceiverParity() {
    CurrencyAmount pvReceiverLong = METHOD_HW.presentValue(SWAPTION_RECEIVER_LONG, BUNDLE_HW);
    CurrencyAmount pvPayerShort = METHOD_HW.presentValue(SWAPTION_PAYER_SHORT, BUNDLE_HW);
    double pvSwap = PVC.visit(SWAP_RECEIVER, CURVES);
    assertEquals("Swaption physical - Hull-White - present value - payer/receiver/swap parity", pvReceiverLong.getAmount() + pvPayerShort.getAmount(), pvSwap, 1E-2);
  }

  @Test
  /**
   * Compare explicit formula with numerical integration.
   */
  public void numericalIntegration() {
    CurrencyAmount pvPayerLongExplicit = METHOD_HW.presentValue(SWAPTION_PAYER_LONG, BUNDLE_HW);
    CurrencyAmount pvPayerLongIntegration = METHOD_HW_INTEGRATION.presentValue(SWAPTION_PAYER_LONG, BUNDLE_HW);
    assertEquals("Swaption physical - Hull-White - present value - explicit/numerical integration", pvPayerLongExplicit.getAmount(), pvPayerLongIntegration.getAmount(), 1.0E-0);
    CurrencyAmount pvPayerShortExplicit = METHOD_HW.presentValue(SWAPTION_PAYER_SHORT, BUNDLE_HW);
    CurrencyAmount pvPayerShortIntegration = METHOD_HW_INTEGRATION.presentValue(SWAPTION_PAYER_SHORT, BUNDLE_HW);
    assertEquals("Swaption physical - Hull-White - present value - explicit/numerical integration", pvPayerShortExplicit.getAmount(), pvPayerShortIntegration.getAmount(), 1.0E-0);
    CurrencyAmount pvReceiverLongExplicit = METHOD_HW.presentValue(SWAPTION_RECEIVER_LONG, BUNDLE_HW);
    CurrencyAmount pvReceiverLongIntegration = METHOD_HW_INTEGRATION.presentValue(SWAPTION_RECEIVER_LONG, BUNDLE_HW);
    assertEquals("Swaption physical - Hull-White - present value - explicit/numerical integration", pvReceiverLongExplicit.getAmount(), pvReceiverLongIntegration.getAmount(), 1.0E-0);
    CurrencyAmount pvReceiverShortExplicit = METHOD_HW.presentValue(SWAPTION_RECEIVER_SHORT, BUNDLE_HW);
    CurrencyAmount pvReceiverShortIntegration = METHOD_HW_INTEGRATION.presentValue(SWAPTION_RECEIVER_SHORT, BUNDLE_HW);
    assertEquals("Swaption physical - Hull-White - present value - explicit/numerical integration", pvReceiverShortExplicit.getAmount(), pvReceiverShortIntegration.getAmount(), 1.0E-0);
  }

  @Test
  /**
   * Tests the Hull-White parameters sensitivity.
   */
  public void hullWhiteSensitivity() {
    double[] hwSensitivity = METHOD_HW.presentValueHullWhiteSensitivity(SWAPTION_PAYER_LONG, BUNDLE_HW);
    int nbVolatility = PARAMETERS_HW.getVolatility().length;
    double shiftVol = 1.0E-6;
    double[] volatilityBumped = new double[nbVolatility];
    System.arraycopy(PARAMETERS_HW.getVolatility(), 0, volatilityBumped, 0, nbVolatility);
    double[] volatilityTime = new double[nbVolatility - 1];
    System.arraycopy(PARAMETERS_HW.getVolatilityTime(), 1, volatilityTime, 0, nbVolatility - 1);
    double[] pvBumpedPlus = new double[nbVolatility];
    double[] pvBumpedMinus = new double[nbVolatility];
    HullWhiteOneFactorPiecewiseConstantParameters parametersBumped = new HullWhiteOneFactorPiecewiseConstantParameters(PARAMETERS_HW.getMeanReversion(), volatilityBumped, volatilityTime);
    HullWhiteOneFactorPiecewiseConstantDataBundle bundleBumped = new HullWhiteOneFactorPiecewiseConstantDataBundle(parametersBumped, CURVES);
    for (int loopvol = 0; loopvol < nbVolatility; loopvol++) {
      volatilityBumped[loopvol] += shiftVol;
      parametersBumped.setVolatility(volatilityBumped);
      pvBumpedPlus[loopvol] = METHOD_HW.presentValue(SWAPTION_PAYER_LONG, bundleBumped).getAmount();
      volatilityBumped[loopvol] -= 2 * shiftVol;
      parametersBumped.setVolatility(volatilityBumped);
      pvBumpedMinus[loopvol] = METHOD_HW.presentValue(SWAPTION_PAYER_LONG, bundleBumped).getAmount();
      assertEquals(
          "Swaption - Hull-White sensitivity adjoint: derivative " + loopvol + " - difference:" + ((pvBumpedPlus[loopvol] - pvBumpedMinus[loopvol]) / (2 * shiftVol) - hwSensitivity[loopvol]),
          (pvBumpedPlus[loopvol] - pvBumpedMinus[loopvol]) / (2 * shiftVol), hwSensitivity[loopvol], 1.0E-0);
      volatilityBumped[loopvol] = PARAMETERS_HW.getVolatility()[loopvol];
    }
  }

  @Test
  /**
   * Tests the curve sensitivity.
   */
  public void presentValueCurveSensitivity() {
    PresentValueSensitivity pvsSwaption = METHOD_HW.presentValueCurveSensitivity(SWAPTION_PAYER_LONG, BUNDLE_HW);
    pvsSwaption = pvsSwaption.clean();
    final double deltaTolerancePrice = 1.0E+0;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
    final double deltaShift = 1.0E-6;
    // 1. Forward curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    final SwaptionPhysicalFixedIbor swptBumpedForward = SWAPTION_PAYER_LONG_DEFINITION.toDerivative(REFERENCE_DATE, new String[] {CURVES_NAME[0], bumpedCurveName});
    DoubleAVLTreeSet forwardTime = new DoubleAVLTreeSet();
    for (int loopcpn = 0; loopcpn < SWAPTION_PAYER_LONG.getUnderlyingSwap().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      CouponIbor cpn = (CouponIbor) SWAPTION_PAYER_LONG.getUnderlyingSwap().getSecondLeg().getNthPayment(loopcpn);
      forwardTime.add(cpn.getFixingPeriodStartTime());
      forwardTime.add(cpn.getFixingPeriodEndTime());
    }
    double[] nodeTimesForward = forwardTime.toDoubleArray();
    final double[] sensiForwardMethod = SensitivityFiniteDifference.curveSensitivity(swptBumpedForward, BUNDLE_HW, CURVES_NAME[1], bumpedCurveName, nodeTimesForward, deltaShift, METHOD_HW);
    //    assertEquals("Sensitivity finite difference method: number of node", 2, sensiForwardMethod.length);
    final List<DoublesPair> sensiPvForward = pvsSwaption.getSensitivities().get(CURVES_NAME[1]);
    for (int loopnode = 0; loopnode < sensiForwardMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvForward.get(loopnode);
      assertEquals("Sensitivity swaption pv to forward curve: Node " + loopnode, nodeTimesForward[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity " + loopnode, pairPv.second, sensiForwardMethod[loopnode], deltaTolerancePrice);
    }
    // 2. Discounting curve sensitivity
    final SwaptionPhysicalFixedIbor swptBumpedDisc = SWAPTION_PAYER_LONG_DEFINITION.toDerivative(REFERENCE_DATE, new String[] {bumpedCurveName, CURVES_NAME[1]});
    DoubleAVLTreeSet discTime = new DoubleAVLTreeSet();
    for (int loopcpn = 0; loopcpn < SWAPTION_PAYER_LONG.getUnderlyingSwap().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      CouponIbor cpn = (CouponIbor) SWAPTION_PAYER_LONG.getUnderlyingSwap().getSecondLeg().getNthPayment(loopcpn);
      discTime.add(cpn.getPaymentTime());
    }
    double[] nodeTimesDisc = discTime.toDoubleArray();
    final double[] sensiDiscMethod = SensitivityFiniteDifference.curveSensitivity(swptBumpedDisc, BUNDLE_HW, CURVES_NAME[0], bumpedCurveName, nodeTimesDisc, deltaShift, METHOD_HW);
    assertEquals("Sensitivity finite difference method: number of node", SWAP_TENOR_YEAR * 4, sensiDiscMethod.length);
    final List<DoublesPair> sensiPvDisc = pvsSwaption.getSensitivities().get(CURVES_NAME[0]);
    for (int loopnode = 0; loopnode < sensiDiscMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvDisc.get(loopnode + 1);
      assertEquals("Sensitivity swaption pv to forward curve: Node " + loopnode, nodeTimesDisc[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity", sensiDiscMethod[loopnode], pairPv.second, deltaTolerancePrice);
    }
  }

  @Test(enabled = false)
  /**
   * Tests of performance. "enabled = false" for the standard testing.
   */
  public void performance() {
    long startTime, endTime;
    final int nbTest = 10000;
    CurrencyAmount pvPayerLongExplicit = CurrencyAmount.of(CUR, 0.0);
    CurrencyAmount pvPayerLongIntegration = CurrencyAmount.of(CUR, 0.0);
    double[] pvhws = METHOD_HW.presentValueHullWhiteSensitivity(SWAPTION_PAYER_LONG, BUNDLE_HW);
    PresentValueSensitivity pvcs = METHOD_HW.presentValueCurveSensitivity(SWAPTION_PAYER_LONG, BUNDLE_HW);
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvPayerLongExplicit = METHOD_HW.presentValue(SWAPTION_PAYER_LONG, BUNDLE_HW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " pv swaption Hull-White explicit method: " + (endTime - startTime) + " ms");
    // Performance note: HW price: 19-Jul-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 330 ms for 10000 swaptions.
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvhws = METHOD_HW.presentValueHullWhiteSensitivity(SWAPTION_PAYER_LONG, BUNDLE_HW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " HW sensitivity swaption Hull-White explicit method: " + (endTime - startTime) + " ms");
    // Performance note: HW sensitivity (3): 19-Jul-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 410 ms for 10000 swaptions.
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvcs = METHOD_HW.presentValueCurveSensitivity(SWAPTION_PAYER_LONG, BUNDLE_HW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve sensitivity swaption Hull-White explicit method: " + (endTime - startTime) + " ms");
    // Performance note: curve sensitivity (40): 19-Jul-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 875 ms for 10000 swaptions.
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvPayerLongIntegration = METHOD_HW_INTEGRATION.presentValue(SWAPTION_PAYER_LONG, BUNDLE_HW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " swaption Hull-White numerical integration method: " + (endTime - startTime) + " ms");
    // Performance note: HW numerical integration: 19-Jul-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 1600 ms for 10000 swaptions.

    double difference = pvPayerLongExplicit.getAmount() - pvPayerLongIntegration.getAmount();
    System.out.println("Difference: " + difference);
    System.out.println("Curve sensitivity: " + pvcs.toString());
    System.out.println("HW sensitivity: " + pvhws.toString());
  }

}
