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

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueCurveSensitivitySABRSwaptionRightExtrapolationCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueSABRSensitivitySABRSwaptionRightExtrapolationCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueSABRSwaptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueSABRSwaptionRightExtrapolationCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.SABRDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.sabrswaption.ParameterSensitivitySABRSwaptionDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class to test the present value and present value rate sensitivity of the cash-settled European swaption in the SABR with extrapolation method.
 * The SABR smile is extrapolated above a certain cut-off strike.
 */
@Test(groups = TestGroup.UNIT)
public class SwaptionCashFixedIborSABRExtrapolationRightMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex EURIBOR6M = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd()[1];
  private static final Currency EUR = EURIBOR6M.getCurrency();
  private static final Calendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();

  private static final SABRInterestRateParameters SABR_PARAMETER = SABRDataSets.createSABR1();
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR6M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("EUR1YEURIBOR6M", CALENDAR);
  private static final SABRSwaptionProviderDiscount SABR_MULTICURVES = new SABRSwaptionProviderDiscount(MULTICURVES, SABR_PARAMETER, EUR1YEURIBOR6M);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2008, 8, 18);
  // Swaption description
  private static final ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2014, 3, 18);
  private static final boolean IS_LONG = true;
  // Swap 5Y description
  private static final int ANNUITY_TENOR_YEAR = 5;
  private static final Period ANNUITY_TENOR = Period.ofYears(ANNUITY_TENOR_YEAR);
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, EURIBOR6M.getSpotLag(), CALENDAR);
  private static final double NOTIONAL = 100000000; //100m
  //  Fixed leg: Semi-annual bond
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final double RATE = 0.02;
  private static final double RATE_HIGH = 0.10;
  private static final boolean FIXED_IS_PAYER = true;
  //  Ibor leg: quarterly money
  // Swaption construction
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, EURIBOR6M, ANNUITY_TENOR, CALENDAR);
  private static final SwapFixedIborDefinition SWAP_PAYER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER, CALENDAR);
  private static final SwapFixedIborDefinition SWAP_RECEIVER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, !FIXED_IS_PAYER, CALENDAR);
  private static final SwapFixedIborDefinition SWAP_PAYER_HIGH_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE_HIGH, FIXED_IS_PAYER, CALENDAR);
  private static final SwapFixedIborDefinition SWAP_RECEIVER_HIGH_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE_HIGH, !FIXED_IS_PAYER, CALENDAR);
  private static final SwaptionCashFixedIborDefinition SWAPTION_LONG_PAYER_DEFINITION = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, true, IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_LONG_RECEIVER_DEFINITION = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_DEFINITION, false, IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_SHORT_PAYER_DEFINITION = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, true, !IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_SHORT_RECEIVER_DEFINITION = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_DEFINITION, false, !IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_LONG_PAYER_HIGH_DEFINITION = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_HIGH_DEFINITION, true, IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_LONG_RECEIVER_HIGH_DEFINITION = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_HIGH_DEFINITION, false, IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_SHORT_PAYER_HIGH_DEFINITION = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_HIGH_DEFINITION, true, !IS_LONG);
  // to derivatives
  private static final SwaptionCashFixedIbor SWAPTION_LONG_PAYER = SWAPTION_LONG_PAYER_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionCashFixedIbor SWAPTION_LONG_RECEIVER = SWAPTION_LONG_RECEIVER_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionCashFixedIbor SWAPTION_SHORT_PAYER = SWAPTION_SHORT_PAYER_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionCashFixedIbor SWAPTION_SHORT_RECEIVER = SWAPTION_SHORT_RECEIVER_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionCashFixedIbor SWAPTION_LONG_PAYER_HIGH = SWAPTION_LONG_PAYER_HIGH_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionCashFixedIbor SWAPTION_LONG_RECEIVER_HIGH = SWAPTION_LONG_RECEIVER_HIGH_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionCashFixedIbor SWAPTION_SHORT_PAYER_HIGH = SWAPTION_SHORT_PAYER_HIGH_DEFINITION.toDerivative(REFERENCE_DATE);
  // Extrapolation
  private static final double CUT_OFF_STRIKE = 0.08;
  private static final double MU = 10.0;
  private static final SwaptionCashFixedIborSABRExtrapolationRightMethod METHOD_EXTRAPOLATION = new SwaptionCashFixedIborSABRExtrapolationRightMethod(CUT_OFF_STRIKE, MU);
  // Calculators
  private static final PresentValueSABRSwaptionCalculator PVSSC = PresentValueSABRSwaptionCalculator.getInstance();

  private static final PresentValueSABRSwaptionRightExtrapolationCalculator PVSSXC = new PresentValueSABRSwaptionRightExtrapolationCalculator(CUT_OFF_STRIKE, MU);
  private static final PresentValueCurveSensitivitySABRSwaptionRightExtrapolationCalculator PVCSSSXC = new PresentValueCurveSensitivitySABRSwaptionRightExtrapolationCalculator(CUT_OFF_STRIKE, MU);
  private static final PresentValueSABRSensitivitySABRSwaptionRightExtrapolationCalculator PVSSSSXC = new PresentValueSABRSensitivitySABRSwaptionRightExtrapolationCalculator(CUT_OFF_STRIKE, MU);

  private static final double SHIFT = 1.0E-7;
  private static final ParameterSensitivityParameterCalculator<SABRSwaptionProviderInterface> PS_SS_X_C = new ParameterSensitivityParameterCalculator<>(PVCSSSXC);
  private static final ParameterSensitivitySABRSwaptionDiscountInterpolatedFDCalculator PS_SS_X_FDC = new ParameterSensitivitySABRSwaptionDiscountInterpolatedFDCalculator(PVSSXC, SHIFT);

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2; //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move.

  /**
   * Tests present value in the region where there is no extrapolation. Tests long/short parity.
   */
  @Test
  public void presentValueNoExtra() {
    final MultipleCurrencyAmount priceLongPayer = METHOD_EXTRAPOLATION.presentValue(SWAPTION_LONG_PAYER, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceShortPayer = METHOD_EXTRAPOLATION.presentValue(SWAPTION_SHORT_PAYER, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceLongReceiver = METHOD_EXTRAPOLATION.presentValue(SWAPTION_LONG_RECEIVER, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceShortReceiver = METHOD_EXTRAPOLATION.presentValue(SWAPTION_SHORT_RECEIVER, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceLongPayerNoExtra = SWAPTION_LONG_PAYER.accept(PVSSC, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceShortPayerNoExtra = SWAPTION_SHORT_PAYER.accept(PVSSC, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceLongReceiverNoExtra = SWAPTION_LONG_RECEIVER.accept(PVSSC, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceShortReceiverNoExtra = SWAPTION_SHORT_RECEIVER.accept(PVSSC, SABR_MULTICURVES);
    assertEquals("Swaption cash SABR extrapolation: below cut-off strike", priceLongPayerNoExtra.getAmount(EUR), priceLongPayer.getAmount(EUR), TOLERANCE_PV);
    assertEquals("Swaption cash SABR extrapolation: below cut-off strike", priceShortPayerNoExtra.getAmount(EUR), priceShortPayer.getAmount(EUR), TOLERANCE_PV);
    assertEquals("Swaption cash SABR extrapolation: below cut-off strike", priceLongReceiverNoExtra.getAmount(EUR), priceLongReceiver.getAmount(EUR), TOLERANCE_PV);
    assertEquals("Swaption cash SABR extrapolation: below cut-off strike", priceShortReceiverNoExtra.getAmount(EUR), priceShortReceiver.getAmount(EUR), TOLERANCE_PV);
    assertEquals("Swaption cash SABR extrapolation: below cut-off strike long/short parity", priceLongPayer.getAmount(EUR), -priceShortPayer.getAmount(EUR), TOLERANCE_PV);
    assertEquals("Swaption cash SABR extrapolation: below cut-off strike long/short parity", priceLongReceiver.getAmount(EUR), -priceShortReceiver.getAmount(EUR), TOLERANCE_PV);
  }

  /**
   * Tests present value in the region where there is extrapolation. Test a hard-coded value. Tests long/short parity. Test payer/receiver/swap parity.
   */
  @Test
  public void presentValueExtra() {
    final MultipleCurrencyAmount priceLongPayer = METHOD_EXTRAPOLATION.presentValue(SWAPTION_LONG_PAYER_HIGH, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceShortPayer = METHOD_EXTRAPOLATION.presentValue(SWAPTION_SHORT_PAYER_HIGH, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceLongReceiver = METHOD_EXTRAPOLATION.presentValue(SWAPTION_LONG_RECEIVER_HIGH, SABR_MULTICURVES);
    final double priceLongPayerExpected = 189696.404; // Value from previous run
    final double priceLongReceiverExpected = 37678176.857; // Value from previous run
    assertEquals("Swaption cash SABR extrapolation: fixed value", priceLongPayerExpected, priceLongPayer.getAmount(EUR), TOLERANCE_PV);
    assertEquals("Swaption cash SABR extrapolation: fixed value", priceLongReceiverExpected, priceLongReceiver.getAmount(EUR), TOLERANCE_PV);
    assertEquals("Swaption cash SABR extrapolation: long/short parity", priceLongPayer.getAmount(EUR), -priceShortPayer.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Test the present value sensitivity to rate for a swaption with strike above the cut-off strike.
   */
  public void presentValueCurveSensitivityExtra() {
    final MultipleCurrencyParameterSensitivity pvpsExact = PS_SS_X_C.calculateSensitivity(SWAPTION_LONG_PAYER_HIGH, SABR_MULTICURVES, SABR_MULTICURVES.getMulticurveProvider().getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsFD = PS_SS_X_FDC.calculateSensitivity(SWAPTION_LONG_PAYER_HIGH, SABR_MULTICURVES);
    AssertSensivityObjects.assertEquals("SwaptionCashFixedIborSABRExtrapolationRightMethod: presentValueCurveSensitivity ", pvpsExact, pvpsFD, 500 * TOLERANCE_PV_DELTA);
    // TODO review the precision.
    final MultipleCurrencyMulticurveSensitivity pvcsLP = SWAPTION_LONG_PAYER_HIGH.accept(PVCSSSXC, SABR_MULTICURVES).cleaned();
    final MultipleCurrencyMulticurveSensitivity pvcsSP = SWAPTION_SHORT_PAYER_HIGH.accept(PVCSSSXC, SABR_MULTICURVES).multipliedBy(-1).cleaned();
    AssertSensivityObjects.assertEquals("SwaptionCashFixedIborSABRExtrapolationRightMethod: presentValueCurveSensitivity ", pvcsLP, pvcsSP, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Test the present value sensitivity to SABR parameters for a swaption with strike above the cut-off strike.
   */
  public void presentValueSABRSensitivity() {
    // Swaption sensitivity
    final PresentValueSABRSensitivityDataBundle pvsLongPayer = METHOD_EXTRAPOLATION.presentValueSABRSensitivity(SWAPTION_LONG_PAYER_HIGH, SABR_MULTICURVES);
    PresentValueSABRSensitivityDataBundle pvsShortPayer = METHOD_EXTRAPOLATION.presentValueSABRSensitivity(SWAPTION_SHORT_PAYER_HIGH, SABR_MULTICURVES);
    // Long/short parity
    pvsShortPayer = pvsShortPayer.multiplyBy(-1.0);
    assertEquals(pvsLongPayer.getAlpha(), pvsShortPayer.getAlpha());
    // SABR sensitivity vs finite difference
    final double pvLongPayer = METHOD_EXTRAPOLATION.presentValue(SWAPTION_LONG_PAYER_HIGH, SABR_MULTICURVES).getAmount(EUR);
    final DoublesPair expectedExpiryTenor = DoublesPair.of(SWAPTION_LONG_PAYER_HIGH.getTimeToExpiry(), ANNUITY_TENOR_YEAR);
    final double shift = 0.000005;
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterAlphaBumped = SABRDataSets.createSABR1AlphaBumped(shift);
    final SABRSwaptionProviderDiscount sabrBundleAlphaBumped = new SABRSwaptionProviderDiscount(MULTICURVES, sabrParameterAlphaBumped, EUR1YEURIBOR6M);
    final double pvLongPayerAlphaBumped = METHOD_EXTRAPOLATION.presentValue(SWAPTION_LONG_PAYER_HIGH, sabrBundleAlphaBumped).getAmount(EUR);
    final double expectedAlphaSensi = (pvLongPayerAlphaBumped - pvLongPayer) / shift;
    assertEquals("Number of alpha sensitivity", pvsLongPayer.getAlpha().getMap().keySet().size(), 1);
    assertEquals("Alpha sensitivity expiry/tenor", pvsLongPayer.getAlpha().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Alpha sensitivity value", expectedAlphaSensi, pvsLongPayer.getAlpha().getMap().get(expectedExpiryTenor), 2.0E+3);
    // Rho sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterRhoBumped = SABRDataSets.createSABR1RhoBumped(shift);
    final SABRSwaptionProviderDiscount sabrBundleRhoBumped = new SABRSwaptionProviderDiscount(MULTICURVES, sabrParameterRhoBumped, EUR1YEURIBOR6M);
    final double pvLongPayerRhoBumped = METHOD_EXTRAPOLATION.presentValue(SWAPTION_LONG_PAYER_HIGH, sabrBundleRhoBumped).getAmount(EUR);
    final double expectedRhoSensi = (pvLongPayerRhoBumped - pvLongPayer) / shift;
    assertEquals("Number of rho sensitivity", pvsLongPayer.getRho().getMap().keySet().size(), 1);
    assertEquals("Rho sensitivity expiry/tenor", pvsLongPayer.getRho().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Rho sensitivity value", expectedRhoSensi, pvsLongPayer.getRho().getMap().get(expectedExpiryTenor), 2.0E+0);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterNuBumped = SABRDataSets.createSABR1NuBumped(shift);
    final SABRSwaptionProviderDiscount sabrBundleNuBumped = new SABRSwaptionProviderDiscount(MULTICURVES, sabrParameterNuBumped, EUR1YEURIBOR6M);
    final double pvLongPayerNuBumped = METHOD_EXTRAPOLATION.presentValue(SWAPTION_LONG_PAYER_HIGH, sabrBundleNuBumped).getAmount(EUR);
    final double expectedNuSensi = (pvLongPayerNuBumped - pvLongPayer) / shift;
    assertEquals("Number of nu sensitivity", pvsLongPayer.getNu().getMap().keySet().size(), 1);
    assertEquals("Nu sensitivity expiry/tenor", pvsLongPayer.getNu().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Nu sensitivity value", expectedNuSensi, pvsLongPayer.getNu().getMap().get(expectedExpiryTenor), 5.0E+1);
  }

  @Test
  /**
   * Test the present value sensitivity to SABR parameters for a swaption with strike above the cut-off strike.
   */
  public void presentValueSABRSensitivityMethodVsCalculator() {
    final PresentValueSABRSensitivityDataBundle pvssMethod = METHOD_EXTRAPOLATION.presentValueSABRSensitivity(SWAPTION_LONG_PAYER_HIGH, SABR_MULTICURVES);
    final PresentValueSABRSensitivityDataBundle pvssCalculator = SWAPTION_LONG_PAYER_HIGH.accept(PVSSSSXC, SABR_MULTICURVES);
    assertEquals("SwaptionCashFixedIborSABRExtrapolationRightMethod: presentValueCurveSensitivity ", pvssMethod, pvssCalculator);
  }

}
