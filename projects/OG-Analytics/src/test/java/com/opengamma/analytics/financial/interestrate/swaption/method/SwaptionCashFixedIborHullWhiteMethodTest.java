/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.method;

import static org.testng.AssertJUnit.assertEquals;
import it.unimi.dsi.fastutil.doubles.DoubleAVLTreeSet;

import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.method.SensitivityFiniteDifference;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.model.interestrate.TestsDataSetHullWhite;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
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
 * Tests related to the pricing of cash-settled swaption in Hull-White one factor model.
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class SwaptionCashFixedIborHullWhiteMethodTest {
  private static final Currency CUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final int SETTLEMENT_DAYS = 2;
  private static final Period IBOR_TENOR = Period.ofMonths(6);
  private static final DayCount IBOR_DAY_COUNT = DayCounts.ACT_360;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, IBOR_TENOR, SETTLEMENT_DAYS, IBOR_DAY_COUNT, BUSINESS_DAY, IS_EOM, "Ibor");
  private static final int SWAP_TENOR_YEAR = 5;
  private static final Period SWAP_TENOR = Period.ofYears(SWAP_TENOR_YEAR);
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(12);
  private static final DayCount FIXED_DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, SWAP_TENOR, CALENDAR);
  private static final ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2016, 7, 7);
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final double NOTIONAL = 100000000; //100m
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final SwapFixedIborDefinition SWAP_PAYER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER, CALENDAR);
  private static final SwapFixedIborDefinition SWAP_RECEIVER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, !FIXED_IS_PAYER, CALENDAR);
  private static final boolean IS_LONG = true;
  private static final SwaptionCashFixedIborDefinition SWAPTION_PAYER_LONG_DEFINITION = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, true, IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_RECEIVER_LONG_DEFINITION = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_DEFINITION, false, IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_PAYER_SHORT_DEFINITION = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, true, !IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_RECEIVER_SHORT_DEFINITION = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_DEFINITION, false, !IS_LONG);
  //to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 7, 7);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME };
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
  private static final SwapFixedCoupon<Coupon> SWAP_PAYER = SWAP_PAYER_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionCashFixedIbor SWAPTION_PAYER_LONG = SWAPTION_PAYER_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionCashFixedIbor SWAPTION_RECEIVER_LONG = SWAPTION_RECEIVER_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionCashFixedIbor SWAPTION_PAYER_SHORT = SWAPTION_PAYER_SHORT_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionCashFixedIbor SWAPTION_RECEIVER_SHORT = SWAPTION_RECEIVER_SHORT_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  // Calculator
  private static final SwaptionCashFixedIborHullWhiteNumericalIntegrationMethod METHOD_HW_INTEGRATION = new SwaptionCashFixedIborHullWhiteNumericalIntegrationMethod();
  private static final SwaptionCashFixedIborHullWhiteApproximationMethod METHOD_HW_APPROXIMATION = new SwaptionCashFixedIborHullWhiteApproximationMethod();
  private static final HullWhiteOneFactorPiecewiseConstantParameters PARAMETERS_HW = TestsDataSetHullWhite.createHullWhiteParameters();
  private static final HullWhiteOneFactorPiecewiseConstantDataBundle BUNDLE_HW = new HullWhiteOneFactorPiecewiseConstantDataBundle(PARAMETERS_HW, CURVES);

  @Test
  /**
   * Tests long/short parity.
   */
  public void longShortParity() {
    final CurrencyAmount pvLong = METHOD_HW_INTEGRATION.presentValue(SWAPTION_PAYER_LONG, BUNDLE_HW);
    final CurrencyAmount pvShort = METHOD_HW_INTEGRATION.presentValue(SWAPTION_PAYER_SHORT, BUNDLE_HW);
    assertEquals("Swaption cash - Hull-White - present value - long/short parity", pvLong.getAmount(), -pvShort.getAmount(), 1E-2);
  }

  @Test
  /**
   * Tests long/short parity.
   */
  public void scaling() {
    final double scale = 12.3;
    final SwapFixedIborDefinition scaledSwapDefinition = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, scale * NOTIONAL, RATE, FIXED_IS_PAYER, CALENDAR);
    final SwaptionCashFixedIborDefinition scaledSwaptionDefinition = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, scaledSwapDefinition, true, IS_LONG);
    final SwaptionCashFixedIbor scaledSwaption = scaledSwaptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final CurrencyAmount pvOriginal = METHOD_HW_INTEGRATION.presentValue(SWAPTION_PAYER_LONG, BUNDLE_HW);
    final CurrencyAmount pvScaled = METHOD_HW_INTEGRATION.presentValue(scaledSwaption, BUNDLE_HW);
    assertEquals("Swaption cash - Hull-White - present value - scaling", scale * pvOriginal.getAmount(), pvScaled.getAmount(), 1E-1);
  }

  @Test
  /**
   * Compare approximate formula with numerical integration.
   */
  public void comparison() {
    final double bp1 = 10000;
    final CurrencyAmount pvPayerLongExplicit = METHOD_HW_APPROXIMATION.presentValue(SWAPTION_PAYER_LONG, BUNDLE_HW);
    final CurrencyAmount pvPayerLongIntegration = METHOD_HW_INTEGRATION.presentValue(SWAPTION_PAYER_LONG, BUNDLE_HW);
    assertEquals("Swaption cash - Hull-White - present value - explicit/numerical integration", pvPayerLongExplicit.getAmount() / NOTIONAL * bp1, pvPayerLongIntegration.getAmount() / NOTIONAL * bp1,
        3.0E-1);
    final CurrencyAmount pvPayerShortExplicit = METHOD_HW_APPROXIMATION.presentValue(SWAPTION_PAYER_SHORT, BUNDLE_HW);
    final CurrencyAmount pvPayerShortIntegration = METHOD_HW_INTEGRATION.presentValue(SWAPTION_PAYER_SHORT, BUNDLE_HW);
    assertEquals("Swaption cash - Hull-White - present value - explicit/numerical integration", pvPayerShortExplicit.getAmount() / NOTIONAL * bp1,
        pvPayerShortIntegration.getAmount() / NOTIONAL * bp1, 3.0E-1);
    final CurrencyAmount pvReceiverLongExplicit = METHOD_HW_APPROXIMATION.presentValue(SWAPTION_RECEIVER_LONG, BUNDLE_HW);
    final CurrencyAmount pvReceiverLongIntegration = METHOD_HW_INTEGRATION.presentValue(SWAPTION_RECEIVER_LONG, BUNDLE_HW);
    assertEquals("Swaption cash - Hull-White - present value - explicit/numerical integration", pvReceiverLongExplicit.getAmount() / NOTIONAL * bp1, pvReceiverLongIntegration.getAmount() / NOTIONAL
        * bp1, 5.0E-1);
    final CurrencyAmount pvReceiverShortExplicit = METHOD_HW_APPROXIMATION.presentValue(SWAPTION_RECEIVER_SHORT, BUNDLE_HW);
    final CurrencyAmount pvReceiverShortIntegration = METHOD_HW_INTEGRATION.presentValue(SWAPTION_RECEIVER_SHORT, BUNDLE_HW);
    assertEquals("Swaption cash - Hull-White - present value - explicit/numerical integration", pvReceiverShortExplicit.getAmount() / NOTIONAL * bp1, pvReceiverShortIntegration.getAmount() / NOTIONAL
        * bp1, 5.0E-1);
  }

  @Test
  /**
   * Tests the Hull-White parameters sensitivity.
   */
  public void hullWhiteSensitivity() {
    final double[] hwSensitivity = METHOD_HW_APPROXIMATION.presentValueHullWhiteSensitivity(SWAPTION_PAYER_LONG, BUNDLE_HW);
    final int nbVolatility = PARAMETERS_HW.getVolatility().length;
    final double shiftVol = 1.0E-6;
    final double[] volatilityBumped = new double[nbVolatility];
    System.arraycopy(PARAMETERS_HW.getVolatility(), 0, volatilityBumped, 0, nbVolatility);
    final double[] volatilityTime = new double[nbVolatility - 1];
    System.arraycopy(PARAMETERS_HW.getVolatilityTime(), 1, volatilityTime, 0, nbVolatility - 1);
    final double[] pvBumpedPlus = new double[nbVolatility];
    final double[] pvBumpedMinus = new double[nbVolatility];
    final HullWhiteOneFactorPiecewiseConstantParameters parametersBumped = new HullWhiteOneFactorPiecewiseConstantParameters(PARAMETERS_HW.getMeanReversion(), volatilityBumped, volatilityTime);
    final HullWhiteOneFactorPiecewiseConstantDataBundle bundleBumped = new HullWhiteOneFactorPiecewiseConstantDataBundle(parametersBumped, CURVES);
    final double[] hwSensitivityExpected = new double[nbVolatility];
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
    final double bp1 = 10000;
    final double errorLimit = 5.0E-1; // 0.5 bp
    final ParRateCalculator prc = ParRateCalculator.getInstance();
    final double forward = SWAP_PAYER.accept(prc, CURVES);
    final double[] strikeRel = new double[] {-0.0250, -0.0150, -0.0050, 0.0, 0.0050, 0.0150, 0.0250 };
    final double[] pvPayerApproximation = new double[strikeRel.length];
    final double[] pvPayerIntegration = new double[strikeRel.length];
    final double[] pvReceiverApproximation = new double[strikeRel.length];
    final double[] pvReceiverIntegration = new double[strikeRel.length];
    for (int loopstrike = 0; loopstrike < strikeRel.length; loopstrike++) {
      final SwapFixedIborDefinition swapStrikePayerDefinition = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, bp1, forward + strikeRel[loopstrike], FIXED_IS_PAYER, CALENDAR);
      final SwaptionCashFixedIborDefinition swaptionStrikePayerDefinition = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, swapStrikePayerDefinition, true, IS_LONG);
      final SwaptionCashFixedIbor swaptionStrikePayer = swaptionStrikePayerDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
      pvPayerApproximation[loopstrike] = METHOD_HW_APPROXIMATION.presentValue(swaptionStrikePayer, BUNDLE_HW).getAmount();
      pvPayerIntegration[loopstrike] = METHOD_HW_INTEGRATION.presentValue(swaptionStrikePayer, BUNDLE_HW).getAmount();
      assertEquals("Swaption cash - Hull-White - present value - explicit/numerical integration", pvPayerApproximation[loopstrike], pvPayerIntegration[loopstrike], errorLimit);
      final SwapFixedIborDefinition swapStrikeReceiverDefinition = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, bp1, forward + strikeRel[loopstrike], !FIXED_IS_PAYER, CALENDAR);
      final SwaptionCashFixedIborDefinition swaptionStrikeReceiverDefinition = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, swapStrikeReceiverDefinition, false, IS_LONG);
      final SwaptionCashFixedIbor swaptionStrikeReceiver = swaptionStrikeReceiverDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
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
    pvsSwaption = pvsSwaption.cleaned();
    final double deltaTolerancePrice = 1.0E+4;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
    final double deltaShift = 1.0E-6;
    // 1. Forward curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    final SwaptionCashFixedIbor swptBumpedForward = SWAPTION_PAYER_LONG_DEFINITION.toDerivative(REFERENCE_DATE, new String[] {CURVES_NAME[0], bumpedCurveName });
    final DoubleAVLTreeSet forwardTime = new DoubleAVLTreeSet();
    for (int loopcpn = 0; loopcpn < SWAPTION_PAYER_LONG.getUnderlyingSwap().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      final CouponIbor cpn = (CouponIbor) SWAPTION_PAYER_LONG.getUnderlyingSwap().getSecondLeg().getNthPayment(loopcpn);
      forwardTime.add(cpn.getFixingPeriodStartTime());
      forwardTime.add(cpn.getFixingPeriodEndTime());
    }
    final double[] nodeTimesForward = forwardTime.toDoubleArray();
    final double[] sensiForwardMethod = SensitivityFiniteDifference.curveSensitivity(swptBumpedForward, BUNDLE_HW, CURVES_NAME[1], bumpedCurveName, nodeTimesForward, deltaShift,
        METHOD_HW_APPROXIMATION);
    final List<DoublesPair> sensiPvForward = pvsSwaption.getSensitivities().get(CURVES_NAME[1]);
    for (int loopnode = 0; loopnode < sensiForwardMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvForward.get(loopnode);
      assertEquals("Sensitivity swaption pv to forward curve: Node " + loopnode, nodeTimesForward[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity " + loopnode, sensiForwardMethod[loopnode], pairPv.second, deltaTolerancePrice);
    }
    // 2. Discounting curve sensitivity
    final SwaptionCashFixedIbor swptBumpedDisc = SWAPTION_PAYER_LONG_DEFINITION.toDerivative(REFERENCE_DATE, new String[] {bumpedCurveName, CURVES_NAME[1] });
    final DoubleAVLTreeSet discTime = new DoubleAVLTreeSet();
    discTime.add(SWAPTION_PAYER_LONG.getSettlementTime());
    for (int loopcpn = 0; loopcpn < SWAPTION_PAYER_LONG.getUnderlyingSwap().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      final CouponIbor cpn = (CouponIbor) SWAPTION_PAYER_LONG.getUnderlyingSwap().getSecondLeg().getNthPayment(loopcpn);
      discTime.add(cpn.getPaymentTime());
    }
    final double[] nodeTimesDisc = discTime.toDoubleArray();
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
