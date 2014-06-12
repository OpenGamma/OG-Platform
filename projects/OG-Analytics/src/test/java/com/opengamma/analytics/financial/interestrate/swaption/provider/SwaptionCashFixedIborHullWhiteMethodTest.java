/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.PresentValueCurveSensitivityHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.PresentValueHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.description.HullWhiteDataSets;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.hullwhite.ParameterSensitivityHullWhiteDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the pricing of cash-settled swaption in Hull-White one factor model.
 */
@Test(groups = TestGroup.UNIT)
public class SwaptionCashFixedIborHullWhiteMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex EURIBOR6M = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd()[1];

  private static final Currency EUR = EURIBOR6M.getCurrency();
  private static final Calendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();

  private static final HullWhiteOneFactorPiecewiseConstantParameters HW_PARAMETERS = HullWhiteDataSets.createHullWhiteParameters();
  private static final HullWhiteOneFactorProviderDiscount HW_MULTICURVES = new HullWhiteOneFactorProviderDiscount(MULTICURVES, HW_PARAMETERS, EUR);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 7, 7);

  private static final int SWAP_TENOR_YEAR = 5;
  private static final Period SWAP_TENOR = Period.ofYears(SWAP_TENOR_YEAR);
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(12);
  private static final DayCount FIXED_DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, EURIBOR6M, SWAP_TENOR, CALENDAR);
  private static final ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2016, 7, 7);
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, EURIBOR6M.getSpotLag(), CALENDAR);
  private static final double NOTIONAL = 100000000; //100m
  private static final double RATE = 0.0175;
  private static final boolean FIXED_IS_PAYER = true;
  private static final SwapFixedIborDefinition SWAP_PAYER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER, CALENDAR);
  private static final SwapFixedIborDefinition SWAP_RECEIVER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, !FIXED_IS_PAYER, CALENDAR);
  private static final boolean IS_LONG = true;
  private static final SwaptionCashFixedIborDefinition SWAPTION_PAYER_LONG_DEFINITION = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, true, IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_RECEIVER_LONG_DEFINITION = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_DEFINITION, false, IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_PAYER_SHORT_DEFINITION = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, true, !IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_RECEIVER_SHORT_DEFINITION = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_DEFINITION, false, !IS_LONG);
  //to derivatives
  private static final SwapFixedCoupon<Coupon> SWAP_PAYER = SWAP_PAYER_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionCashFixedIbor SWAPTION_PAYER_LONG = SWAPTION_PAYER_LONG_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionCashFixedIbor SWAPTION_RECEIVER_LONG = SWAPTION_RECEIVER_LONG_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionCashFixedIbor SWAPTION_PAYER_SHORT = SWAPTION_PAYER_SHORT_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionCashFixedIbor SWAPTION_RECEIVER_SHORT = SWAPTION_RECEIVER_SHORT_DEFINITION.toDerivative(REFERENCE_DATE);
  // Calculator
  private static final SwaptionCashFixedIborHullWhiteNumericalIntegrationMethod METHOD_HW_INTEGRATION = SwaptionCashFixedIborHullWhiteNumericalIntegrationMethod.getInstance();
  private static final SwaptionCashFixedIborHullWhiteApproximationMethod METHOD_HW_APPROXIMATION = SwaptionCashFixedIborHullWhiteApproximationMethod.getInstance();

  //  private static final CashFlowEquivalentCalculator CFEC = CashFlowEquivalentCalculator.getInstance();
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();
  //  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueHullWhiteCalculator PVHWC = PresentValueHullWhiteCalculator.getInstance();
  private static final PresentValueCurveSensitivityHullWhiteCalculator PVCSHWC = PresentValueCurveSensitivityHullWhiteCalculator.getInstance();

  private static final double SHIFT = 1.0E-7;
  private static final ParameterSensitivityParameterCalculator<HullWhiteOneFactorProviderInterface> PS_HW_C = new ParameterSensitivityParameterCalculator<>(PVCSHWC);
  private static final ParameterSensitivityHullWhiteDiscountInterpolatedFDCalculator PS_HW_FDC = new ParameterSensitivityHullWhiteDiscountInterpolatedFDCalculator(PVHWC, SHIFT);

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+4; //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move.

  @Test
  /**
   * Tests long/short parity.
   */
  public void longShortParity() {
    final MultipleCurrencyAmount pvLong = METHOD_HW_INTEGRATION.presentValue(SWAPTION_PAYER_LONG, HW_MULTICURVES);
    final MultipleCurrencyAmount pvShort = METHOD_HW_INTEGRATION.presentValue(SWAPTION_PAYER_SHORT, HW_MULTICURVES);
    assertEquals("Swaption cash - Hull-White - present value - long/short parity", pvLong.getAmount(EUR), -pvShort.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests long/short parity.
   */
  public void scaling() {
    final double scale = 12.3;
    final SwapFixedIborDefinition scaledSwapDefinition = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, scale * NOTIONAL, RATE, FIXED_IS_PAYER, CALENDAR);
    final SwaptionCashFixedIborDefinition scaledSwaptionDefinition = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, scaledSwapDefinition, true, IS_LONG);
    final SwaptionCashFixedIbor scaledSwaption = scaledSwaptionDefinition.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyAmount pvOriginal = METHOD_HW_INTEGRATION.presentValue(SWAPTION_PAYER_LONG, HW_MULTICURVES);
    final MultipleCurrencyAmount pvScaled = METHOD_HW_INTEGRATION.presentValue(scaledSwaption, HW_MULTICURVES);
    assertEquals("Swaption cash - Hull-White - present value - scaling", scale * pvOriginal.getAmount(EUR), pvScaled.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Compare approximate formula with numerical integration.
   */
  public void comparison() {
    final double bp1 = 10000;
    final MultipleCurrencyAmount pvPayerLongExplicit = METHOD_HW_APPROXIMATION.presentValue(SWAPTION_PAYER_LONG, HW_MULTICURVES);
    final MultipleCurrencyAmount pvPayerLongIntegration = METHOD_HW_INTEGRATION.presentValue(SWAPTION_PAYER_LONG, HW_MULTICURVES);
    assertEquals("Swaption cash - Hull-White - present value - explicit/numerical integration", pvPayerLongExplicit.getAmount(EUR) / NOTIONAL * bp1, pvPayerLongIntegration.getAmount(EUR) / NOTIONAL
        * bp1, 4.0E-1);
    final MultipleCurrencyAmount pvPayerShortExplicit = METHOD_HW_APPROXIMATION.presentValue(SWAPTION_PAYER_SHORT, HW_MULTICURVES);
    final MultipleCurrencyAmount pvPayerShortIntegration = METHOD_HW_INTEGRATION.presentValue(SWAPTION_PAYER_SHORT, HW_MULTICURVES);
    assertEquals("Swaption cash - Hull-White - present value - explicit/numerical integration", pvPayerShortExplicit.getAmount(EUR) / NOTIONAL * bp1, pvPayerShortIntegration.getAmount(EUR) / NOTIONAL
        * bp1, 4.0E-1);
    final MultipleCurrencyAmount pvReceiverLongExplicit = METHOD_HW_APPROXIMATION.presentValue(SWAPTION_RECEIVER_LONG, HW_MULTICURVES);
    final MultipleCurrencyAmount pvReceiverLongIntegration = METHOD_HW_INTEGRATION.presentValue(SWAPTION_RECEIVER_LONG, HW_MULTICURVES);
    assertEquals("Swaption cash - Hull-White - present value - explicit/numerical integration", pvReceiverLongExplicit.getAmount(EUR) / NOTIONAL * bp1, pvReceiverLongIntegration.getAmount(EUR)
        / NOTIONAL * bp1, 5.0E-1);
    final MultipleCurrencyAmount pvReceiverShortExplicit = METHOD_HW_APPROXIMATION.presentValue(SWAPTION_RECEIVER_SHORT, HW_MULTICURVES);
    final MultipleCurrencyAmount pvReceiverShortIntegration = METHOD_HW_INTEGRATION.presentValue(SWAPTION_RECEIVER_SHORT, HW_MULTICURVES);
    assertEquals("Swaption cash - Hull-White - present value - explicit/numerical integration", pvReceiverShortExplicit.getAmount(EUR) / NOTIONAL * bp1, pvReceiverShortIntegration.getAmount(EUR)
        / NOTIONAL * bp1, 5.0E-1);
  }

  @Test
  /**
   * Tests the Hull-White parameters sensitivity.
   */
  public void hullWhiteSensitivity() {
    final double[] hwSensitivity = METHOD_HW_APPROXIMATION.presentValueHullWhiteSensitivity(SWAPTION_PAYER_LONG, HW_MULTICURVES);
    final int nbVolatility = HW_PARAMETERS.getVolatility().length;
    final double shiftVol = 1.0E-6;
    final double[] volatilityBumped = new double[nbVolatility];
    System.arraycopy(HW_PARAMETERS.getVolatility(), 0, volatilityBumped, 0, nbVolatility);
    final double[] volatilityTime = new double[nbVolatility - 1];
    System.arraycopy(HW_PARAMETERS.getVolatilityTime(), 1, volatilityTime, 0, nbVolatility - 1);
    final double[] pvBumpedPlus = new double[nbVolatility];
    final double[] pvBumpedMinus = new double[nbVolatility];
    final HullWhiteOneFactorPiecewiseConstantParameters parametersBumped = new HullWhiteOneFactorPiecewiseConstantParameters(HW_PARAMETERS.getMeanReversion(), volatilityBumped, volatilityTime);
    final HullWhiteOneFactorProviderDiscount bundleBumped = new HullWhiteOneFactorProviderDiscount(MULTICURVES, parametersBumped, EUR);
    final double[] hwSensitivityExpected = new double[nbVolatility];
    for (int loopvol = 0; loopvol < nbVolatility; loopvol++) {
      volatilityBumped[loopvol] += shiftVol;
      parametersBumped.setVolatility(volatilityBumped);
      pvBumpedPlus[loopvol] = METHOD_HW_APPROXIMATION.presentValue(SWAPTION_PAYER_LONG, bundleBumped).getAmount(EUR);
      volatilityBumped[loopvol] -= 2 * shiftVol;
      parametersBumped.setVolatility(volatilityBumped);
      pvBumpedMinus[loopvol] = METHOD_HW_APPROXIMATION.presentValue(SWAPTION_PAYER_LONG, bundleBumped).getAmount(EUR);
      hwSensitivityExpected[loopvol] = (pvBumpedPlus[loopvol] - pvBumpedMinus[loopvol]) / (2 * shiftVol);
      assertEquals("Swaption - Hull-White sensitivity adjoint: derivative " + loopvol + " - difference:" + (hwSensitivityExpected[loopvol] - hwSensitivity[loopvol]), hwSensitivityExpected[loopvol],
          hwSensitivity[loopvol], 2.0E+5);
      volatilityBumped[loopvol] = HW_PARAMETERS.getVolatility()[loopvol];
    }
  }

  @Test(enabled = false)
  /**
   * Tests approximation error. "enabled = false" for the standard testing.
   */
  public void errorAnalysis() {
    final double bp1 = 10000;
    final double errorLimit = 5.0E-1; // 0.5 bp
    final double forward = SWAP_PAYER.accept(PRDC, MULTICURVES);
    final double[] strikeRel = new double[] {-0.0250, -0.0150, -0.0050, 0.0, 0.0050, 0.0150, 0.0250};
    final double[] pvPayerApproximation = new double[strikeRel.length];
    final double[] pvPayerIntegration = new double[strikeRel.length];
    final double[] pvReceiverApproximation = new double[strikeRel.length];
    final double[] pvReceiverIntegration = new double[strikeRel.length];
    for (int loopstrike = 0; loopstrike < strikeRel.length; loopstrike++) {
      final SwapFixedIborDefinition swapStrikePayerDefinition = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, bp1, forward + strikeRel[loopstrike], FIXED_IS_PAYER, CALENDAR);
      final SwaptionCashFixedIborDefinition swaptionStrikePayerDefinition = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, swapStrikePayerDefinition, true, IS_LONG);
      final SwaptionCashFixedIbor swaptionStrikePayer = swaptionStrikePayerDefinition.toDerivative(REFERENCE_DATE);
      pvPayerApproximation[loopstrike] = METHOD_HW_APPROXIMATION.presentValue(swaptionStrikePayer, HW_MULTICURVES).getAmount(EUR);
      pvPayerIntegration[loopstrike] = METHOD_HW_INTEGRATION.presentValue(swaptionStrikePayer, HW_MULTICURVES).getAmount(EUR);
      assertEquals("Swaption cash - Hull-White - present value - explicit/numerical integration", pvPayerApproximation[loopstrike], pvPayerIntegration[loopstrike], errorLimit);
      final SwapFixedIborDefinition swapStrikeReceiverDefinition = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, bp1, forward + strikeRel[loopstrike], !FIXED_IS_PAYER, CALENDAR);
      final SwaptionCashFixedIborDefinition swaptionStrikeReceiverDefinition = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, swapStrikeReceiverDefinition, false, IS_LONG);
      final SwaptionCashFixedIbor swaptionStrikeReceiver = swaptionStrikeReceiverDefinition.toDerivative(REFERENCE_DATE);
      pvReceiverApproximation[loopstrike] = METHOD_HW_APPROXIMATION.presentValue(swaptionStrikeReceiver, HW_MULTICURVES).getAmount(EUR);
      pvReceiverIntegration[loopstrike] = METHOD_HW_INTEGRATION.presentValue(swaptionStrikeReceiver, HW_MULTICURVES).getAmount(EUR);
      assertEquals("Swaption cash - Hull-White - present value - explicit/numerical integration", pvReceiverApproximation[loopstrike], pvReceiverIntegration[loopstrike], errorLimit);
    }
  }

  @Test
  /**
   * Tests the curve sensitivity.
   */
  public void presentValueCurveSensitivity() {
    final MultipleCurrencyParameterSensitivity pvpsExact = PS_HW_C.calculateSensitivity(SWAPTION_RECEIVER_SHORT, HW_MULTICURVES, HW_MULTICURVES.getMulticurveProvider().getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsFD = PS_HW_FDC.calculateSensitivity(SWAPTION_RECEIVER_SHORT, HW_MULTICURVES);
    AssertSensitivityObjects.assertEquals("SwaptionPhysicalFixedIborSABRMethod: presentValueCurveSensitivity ", pvpsExact, pvpsFD, TOLERANCE_PV_DELTA);
  }

  @Test(enabled = false)
  /**
   * Tests of performance. "enabled = false" for the standard testing.
   */
  public void performance() {
    long startTime, endTime;
    final int nbTest = 10000;
    MultipleCurrencyAmount pvPayerLongExplicit = MultipleCurrencyAmount.of(EUR, 0.0);
    MultipleCurrencyAmount pvPayerLongIntegration = MultipleCurrencyAmount.of(EUR, 0.0);

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvPayerLongExplicit = METHOD_HW_APPROXIMATION.presentValue(SWAPTION_PAYER_LONG, HW_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " pv swaption Hull-White approximation method: " + (endTime - startTime) + " ms");
    // Performance note: HW price: 10-Dec-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 390 ms for 10000 swaptions.
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      METHOD_HW_APPROXIMATION.presentValueHullWhiteSensitivity(SWAPTION_PAYER_LONG, HW_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " HW sensitivity swaption Hull-White approximation method: " + (endTime - startTime) + " ms");
    // Performance note: HW parameters sensitivity: 10-Dec-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 520 ms for 10000 swaptions.
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      METHOD_HW_APPROXIMATION.presentValueCurveSensitivity(SWAPTION_PAYER_LONG, HW_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve sensitivity swaption Hull-White approximation method: " + (endTime - startTime) + " ms");
    // Performance note: HW curve sensitivity: 10-Dec-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 570 ms for 10000 swaptions.
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvPayerLongIntegration = METHOD_HW_INTEGRATION.presentValue(SWAPTION_PAYER_LONG, HW_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " cash swaption Hull-White numerical integration method: " + (endTime - startTime) + " ms");
    // Performance note: HW numerical integration: 10-Dec-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 1800 ms for 10000 swaptions.

    double difference = 0.0;
    difference = pvPayerLongExplicit.getAmount(EUR) - pvPayerLongIntegration.getAmount(EUR);
    System.out.println("Difference: " + difference);
  }

}
