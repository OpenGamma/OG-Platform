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
import com.opengamma.financial.instrument.index.IndexSwap;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.method.SensitivityFiniteDifference;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.financial.model.interestrate.HullWhiteTestsDataSet;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Tests related to the pricing of cash-settled swaption in Hull-White one factor model.
 */
public class SwaptionCashFixedIborHullWhiteMethodTest {
  private static final Currency CUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final int SETTLEMENT_DAYS = 2;
  private static final Period IBOR_TENOR = Period.ofMonths(6);
  private static final DayCount IBOR_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, IBOR_TENOR, SETTLEMENT_DAYS, CALENDAR, IBOR_DAY_COUNT, BUSINESS_DAY, IS_EOM);
  private static final int SWAP_TENOR_YEAR = 5;
  private static final Period SWAP_TENOR = Period.ofYears(SWAP_TENOR_YEAR);
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(12);
  private static final DayCount FIXED_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, SWAP_TENOR);
  private static final ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2016, 7, 7);
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final double NOTIONAL = 100000000; //100m
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final SwapFixedIborDefinition SWAP_PAYER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER);
  private static final SwapFixedIborDefinition SWAP_RECEIVER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, !FIXED_IS_PAYER);
  private static final boolean IS_LONG = true;
  private static final SwaptionCashFixedIborDefinition SWAPTION_PAYER_LONG_DEFINITION = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_RECEIVER_LONG_DEFINITION = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_DEFINITION, IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_PAYER_SHORT_DEFINITION = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, !IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_RECEIVER_SHORT_DEFINITION = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_DEFINITION, !IS_LONG);
  //to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 7, 7);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final YieldCurveBundle CURVES = TestsDataSets.createCurves1();
  private static final FixedCouponSwap<Coupon> SWAP_PAYER = SWAP_PAYER_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionCashFixedIbor SWAPTION_PAYER_LONG = SWAPTION_PAYER_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionCashFixedIbor SWAPTION_RECEIVER_LONG = SWAPTION_RECEIVER_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionCashFixedIbor SWAPTION_PAYER_SHORT = SWAPTION_PAYER_SHORT_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionCashFixedIbor SWAPTION_RECEIVER_SHORT = SWAPTION_RECEIVER_SHORT_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  // Calculator
  private static final SwaptionCashFixedIborHullWhiteNumericalIntegrationMethod METHOD_HW_INTEGRATION = new SwaptionCashFixedIborHullWhiteNumericalIntegrationMethod();
  private static final SwaptionCashFixedIborHullWhiteApproximationMethod METHOD_HW_APPROXIMATION = new SwaptionCashFixedIborHullWhiteApproximationMethod();
  private static final HullWhiteOneFactorPiecewiseConstantParameters PARAMETERS_HW = HullWhiteTestsDataSet.createHullWhiteParameters();
  private static final HullWhiteOneFactorPiecewiseConstantDataBundle BUNDLE_HW = new HullWhiteOneFactorPiecewiseConstantDataBundle(PARAMETERS_HW, CURVES);

  @Test
  /**
   * Tests long/short parity.
   */
  public void longShortParity() {
    CurrencyAmount pvLong = METHOD_HW_INTEGRATION.presentValue(SWAPTION_PAYER_LONG, BUNDLE_HW);
    CurrencyAmount pvShort = METHOD_HW_INTEGRATION.presentValue(SWAPTION_PAYER_SHORT, BUNDLE_HW);
    assertEquals("Swaption cash - Hull-White - present value - long/short parity", pvLong.getAmount(), -pvShort.getAmount(), 1E-2);
  }

  @Test
  /**
   * Tests long/short parity.
   */
  public void scaling() {
    double scale = 12.3;
    SwapFixedIborDefinition scaledSwapDefinition = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, scale * NOTIONAL, RATE, FIXED_IS_PAYER);
    SwaptionCashFixedIborDefinition scaledSwaptionDefinition = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, scaledSwapDefinition, IS_LONG);
    SwaptionCashFixedIbor scaledSwaption = scaledSwaptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    CurrencyAmount pvOriginal = METHOD_HW_INTEGRATION.presentValue(SWAPTION_PAYER_LONG, BUNDLE_HW);
    CurrencyAmount pvScaled = METHOD_HW_INTEGRATION.presentValue(scaledSwaption, BUNDLE_HW);
    assertEquals("Swaption cash - Hull-White - present value - scaling", scale * pvOriginal.getAmount(), pvScaled.getAmount(), 1E-1);
  }

  @Test
  /**
   * Compare approximate formula with numerical integration.
   */
  public void comparison() {
    double bp1 = 10000;
    CurrencyAmount pvPayerLongExplicit = METHOD_HW_APPROXIMATION.presentValue(SWAPTION_PAYER_LONG, BUNDLE_HW);
    CurrencyAmount pvPayerLongIntegration = METHOD_HW_INTEGRATION.presentValue(SWAPTION_PAYER_LONG, BUNDLE_HW);
    assertEquals("Swaption cash - Hull-White - present value - explicit/numerical integration", pvPayerLongExplicit.getAmount() / NOTIONAL * bp1, pvPayerLongIntegration.getAmount() / NOTIONAL * bp1,
        3.0E-1);
    CurrencyAmount pvPayerShortExplicit = METHOD_HW_APPROXIMATION.presentValue(SWAPTION_PAYER_SHORT, BUNDLE_HW);
    CurrencyAmount pvPayerShortIntegration = METHOD_HW_INTEGRATION.presentValue(SWAPTION_PAYER_SHORT, BUNDLE_HW);
    assertEquals("Swaption cash - Hull-White - present value - explicit/numerical integration", pvPayerShortExplicit.getAmount() / NOTIONAL * bp1,
        pvPayerShortIntegration.getAmount() / NOTIONAL * bp1, 3.0E-1);
    CurrencyAmount pvReceiverLongExplicit = METHOD_HW_APPROXIMATION.presentValue(SWAPTION_RECEIVER_LONG, BUNDLE_HW);
    CurrencyAmount pvReceiverLongIntegration = METHOD_HW_INTEGRATION.presentValue(SWAPTION_RECEIVER_LONG, BUNDLE_HW);
    assertEquals("Swaption cash - Hull-White - present value - explicit/numerical integration", pvReceiverLongExplicit.getAmount() / NOTIONAL * bp1, pvReceiverLongIntegration.getAmount() / NOTIONAL
        * bp1, 5.0E-1);
    CurrencyAmount pvReceiverShortExplicit = METHOD_HW_APPROXIMATION.presentValue(SWAPTION_RECEIVER_SHORT, BUNDLE_HW);
    CurrencyAmount pvReceiverShortIntegration = METHOD_HW_INTEGRATION.presentValue(SWAPTION_RECEIVER_SHORT, BUNDLE_HW);
    assertEquals("Swaption cash - Hull-White - present value - explicit/numerical integration", pvReceiverShortExplicit.getAmount() / NOTIONAL * bp1, pvReceiverShortIntegration.getAmount() / NOTIONAL
        * bp1, 5.0E-1);
  }

  @Test
  /**
   * Tests the Hull-White parameters sensitivity.
   */
  public void hullWhiteSensitivity() {
    double[] hwSensitivity = METHOD_HW_APPROXIMATION.presentValueHullWhiteSensitivity(SWAPTION_PAYER_LONG, BUNDLE_HW);
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
    double[] hwSensitivityExpected = new double[nbVolatility];
    for (int loopvol = 0; loopvol < nbVolatility; loopvol++) {
      volatilityBumped[loopvol] += shiftVol;
      parametersBumped.setVolatility(volatilityBumped);
      pvBumpedPlus[loopvol] = METHOD_HW_APPROXIMATION.presentValue(SWAPTION_PAYER_LONG, bundleBumped).getAmount();
      volatilityBumped[loopvol] -= 2 * shiftVol;
      parametersBumped.setVolatility(volatilityBumped);
      pvBumpedMinus[loopvol] = METHOD_HW_APPROXIMATION.presentValue(SWAPTION_PAYER_LONG, bundleBumped).getAmount();
      hwSensitivityExpected[loopvol] = (pvBumpedPlus[loopvol] - pvBumpedMinus[loopvol]) / (2 * shiftVol);
      assertEquals("Swaption - Hull-White sensitivity adjoint: derivative " + loopvol + " - difference:" + (hwSensitivityExpected[loopvol] - hwSensitivity[loopvol]), hwSensitivityExpected[loopvol],
          hwSensitivity[loopvol], 2.0E+5);
      volatilityBumped[loopvol] = PARAMETERS_HW.getVolatility()[loopvol];
    }
  }

  @Test(enabled = true)
  /**
   * Tests approximation error. "enabled = false" for the standard testing.
   */
  public void errorAnalysis() {
    double bp1 = 10000;
    double errorLimit = 5.0E-1; // 0.5 bp
    ParRateCalculator prc = ParRateCalculator.getInstance();
    double forward = prc.visit(SWAP_PAYER, CURVES);
    double[] strikeRel = new double[] {-0.0250, -0.0150, -0.0050, 0.0, 0.0050, 0.0150, 0.0250};
    double[] pvPayerApproximation = new double[strikeRel.length];
    double[] pvPayerIntegration = new double[strikeRel.length];
    double[] pvReceiverApproximation = new double[strikeRel.length];
    double[] pvReceiverIntegration = new double[strikeRel.length];
    for (int loopstrike = 0; loopstrike < strikeRel.length; loopstrike++) {
      SwapFixedIborDefinition swapStrikePayerDefinition = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, bp1, forward + strikeRel[loopstrike], FIXED_IS_PAYER);
      SwaptionCashFixedIborDefinition swaptionStrikePayerDefinition = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, swapStrikePayerDefinition, IS_LONG);
      SwaptionCashFixedIbor swaptionStrikePayer = swaptionStrikePayerDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
      pvPayerApproximation[loopstrike] = METHOD_HW_APPROXIMATION.presentValue(swaptionStrikePayer, BUNDLE_HW).getAmount();
      pvPayerIntegration[loopstrike] = METHOD_HW_INTEGRATION.presentValue(swaptionStrikePayer, BUNDLE_HW).getAmount();
      assertEquals("Swaption cash - Hull-White - present value - explicit/numerical integration", pvPayerApproximation[loopstrike], pvPayerIntegration[loopstrike], errorLimit);
      SwapFixedIborDefinition swapStrikeReceiverDefinition = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, bp1, forward + strikeRel[loopstrike], !FIXED_IS_PAYER);
      SwaptionCashFixedIborDefinition swaptionStrikeReceiverDefinition = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, swapStrikeReceiverDefinition, IS_LONG);
      SwaptionCashFixedIbor swaptionStrikeReceiver = swaptionStrikeReceiverDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
      pvReceiverApproximation[loopstrike] = METHOD_HW_APPROXIMATION.presentValue(swaptionStrikeReceiver, BUNDLE_HW).getAmount();
      pvReceiverIntegration[loopstrike] = METHOD_HW_INTEGRATION.presentValue(swaptionStrikeReceiver, BUNDLE_HW).getAmount();
      assertEquals("Swaption cash - Hull-White - present value - explicit/numerical integration", pvReceiverApproximation[loopstrike], pvReceiverIntegration[loopstrike], errorLimit);
    }
  }

  @Test
  /**
   * Tests the curve sensitivity.
   */
  public void presentValueCurveSensitivity() {
    InterestRateCurveSensitivity pvsSwaption = METHOD_HW_APPROXIMATION.presentValueCurveSensitivity(SWAPTION_PAYER_LONG, BUNDLE_HW);
    pvsSwaption = pvsSwaption.clean();
    final double deltaTolerancePrice = 1.0E+4;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
    final double deltaShift = 1.0E-6;
    // 1. Forward curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    final SwaptionCashFixedIbor swptBumpedForward = SWAPTION_PAYER_LONG_DEFINITION.toDerivative(REFERENCE_DATE, new String[] {CURVES_NAME[0], bumpedCurveName});
    DoubleAVLTreeSet forwardTime = new DoubleAVLTreeSet();
    for (int loopcpn = 0; loopcpn < SWAPTION_PAYER_LONG.getUnderlyingSwap().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      CouponIbor cpn = (CouponIbor) SWAPTION_PAYER_LONG.getUnderlyingSwap().getSecondLeg().getNthPayment(loopcpn);
      forwardTime.add(cpn.getFixingPeriodStartTime());
      forwardTime.add(cpn.getFixingPeriodEndTime());
    }
    double[] nodeTimesForward = forwardTime.toDoubleArray();
    final double[] sensiForwardMethod = SensitivityFiniteDifference.curveSensitivity(swptBumpedForward, BUNDLE_HW, CURVES_NAME[1], bumpedCurveName, nodeTimesForward, deltaShift,
        METHOD_HW_APPROXIMATION);
    final List<DoublesPair> sensiPvForward = pvsSwaption.getSensitivities().get(CURVES_NAME[1]);
    for (int loopnode = 0; loopnode < sensiForwardMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvForward.get(loopnode);
      assertEquals("Sensitivity swaption pv to forward curve: Node " + loopnode, nodeTimesForward[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity " + loopnode, sensiForwardMethod[loopnode], pairPv.second, deltaTolerancePrice);
    }
    // 2. Discounting curve sensitivity
    final SwaptionCashFixedIbor swptBumpedDisc = SWAPTION_PAYER_LONG_DEFINITION.toDerivative(REFERENCE_DATE, new String[] {bumpedCurveName, CURVES_NAME[1]});
    DoubleAVLTreeSet discTime = new DoubleAVLTreeSet();
    discTime.add(SWAPTION_PAYER_LONG.getSettlementTime());
    for (int loopcpn = 0; loopcpn < SWAPTION_PAYER_LONG.getUnderlyingSwap().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      CouponIbor cpn = (CouponIbor) SWAPTION_PAYER_LONG.getUnderlyingSwap().getSecondLeg().getNthPayment(loopcpn);
      discTime.add(cpn.getPaymentTime());
    }
    double[] nodeTimesDisc = discTime.toDoubleArray();
    final double[] sensiDiscMethod = SensitivityFiniteDifference.curveSensitivity(swptBumpedDisc, BUNDLE_HW, CURVES_NAME[0], bumpedCurveName, nodeTimesDisc, deltaShift, METHOD_HW_APPROXIMATION);
    assertEquals("Sensitivity finite difference method: number of node", 11, sensiDiscMethod.length);
    final List<DoublesPair> sensiPvDisc = pvsSwaption.getSensitivities().get(CURVES_NAME[0]);
    for (int loopnode = 0; loopnode < sensiDiscMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvDisc.get(loopnode);
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
    final int nbTest = 1000;
    CurrencyAmount pvPayerLongExplicit = CurrencyAmount.of(CUR, 0.0);
    CurrencyAmount pvPayerLongIntegration = CurrencyAmount.of(CUR, 0.0);
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvPayerLongExplicit = METHOD_HW_APPROXIMATION.presentValue(SWAPTION_PAYER_LONG, BUNDLE_HW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " pv swaption Hull-White approximation method: " + (endTime - startTime) + " ms");
    // Performance note: HW price: 8-Jul-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 330 ms for 10000 swaptions.
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      METHOD_HW_APPROXIMATION.presentValueHullWhiteSensitivity(SWAPTION_PAYER_LONG, BUNDLE_HW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " HW sensitivity swaption Hull-White approximation method: " + (endTime - startTime) + " ms");
    // Performance note: HW parameters sensitivity: 8-Jul-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 525 ms for 10000 swaptions.
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      METHOD_HW_APPROXIMATION.presentValueCurveSensitivity(SWAPTION_PAYER_LONG, BUNDLE_HW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve sensitivity swaption Hull-White approximation method: " + (endTime - startTime) + " ms");
    // Performance note: HW curve sensitivity: 8-Jul-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 550 ms for 10000 swaptions.
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvPayerLongIntegration = METHOD_HW_INTEGRATION.presentValue(SWAPTION_PAYER_LONG, BUNDLE_HW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " cash swaption Hull-White numerical integration method: " + (endTime - startTime) + " ms");
    // Performance note: HW numerical integration: 8-Jul-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 1300 ms for 10000 swaptions.

    double difference = 0.0;
    difference = pvPayerLongExplicit.getAmount() - pvPayerLongIntegration.getAmount();
    System.out.println("Difference: " + difference);
  }

}
