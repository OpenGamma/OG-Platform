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
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueCurveSensitivitySABRSwaptionRightExtrapolationCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueSABRSwaptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueSABRSwaptionRightExtrapolationCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.SABRDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.sabrswaption.ParameterSensitivitySABRSwaptionDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class to test the present value and present value rate sensitivity of the physical delivery swaption in the SABR with extrapolation method.
 * The SABR smile is extrapolated above a certain cut-off strike.
 */
@Test(groups = TestGroup.UNIT)
public class SwaptionPhysicalFixedIborSABRExtrapolationRightMethodTest {

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
  private static final double RATE = 0.02;
  private static final boolean FIXED_IS_PAYER = true;
  //  Ibor leg: quarterly money
  // Swaption construction
  private static final SwapFixedIborDefinition SWAP_PAYER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, EUR1YEURIBOR6M, NOTIONAL, RATE, FIXED_IS_PAYER);
  private static final SwapFixedIborDefinition SWAP_RECEIVER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, EUR1YEURIBOR6M, NOTIONAL, RATE, !FIXED_IS_PAYER);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_LONG_PAYER_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, true, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_LONG_RECEIVER_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_DEFINITION, false, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_SHORT_PAYER_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, true, !IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_SHORT_RECEIVER_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_DEFINITION, false, !IS_LONG);
  // to derivatives
  private static final SwaptionPhysicalFixedIbor SWAPTION_LONG_PAYER = SWAPTION_LONG_PAYER_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionPhysicalFixedIbor SWAPTION_LONG_RECEIVER = SWAPTION_LONG_RECEIVER_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionPhysicalFixedIbor SWAPTION_SHORT_PAYER = SWAPTION_SHORT_PAYER_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionPhysicalFixedIbor SWAPTION_SHORT_RECEIVER = SWAPTION_SHORT_RECEIVER_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final double HIGH_STRIKE = 0.10;
  private static final SwapFixedIborDefinition SWAP_PAYER_HIGH_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, EUR1YEURIBOR6M, NOTIONAL, HIGH_STRIKE, FIXED_IS_PAYER);
  private static final SwapFixedIborDefinition SWAP_RECEIVER_HIGH_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, EUR1YEURIBOR6M, NOTIONAL, HIGH_STRIKE, !FIXED_IS_PAYER);
  private static final SwapFixedCoupon<Coupon> SWAP_PAYER_HIGH = SWAP_PAYER_HIGH_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_LONG_PAYER_HIGH_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_HIGH_DEFINITION, true, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_SHORT_PAYER_HIGH_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_HIGH_DEFINITION, true, !IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_LONG_RECEIVER_HIGH_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_HIGH_DEFINITION, false, IS_LONG);
  private static final SwaptionPhysicalFixedIbor SWAPTION_LONG_PAYER_HIGH = SWAPTION_LONG_PAYER_HIGH_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionPhysicalFixedIbor SWAPTION_SHORT_PAYER_HIGH = SWAPTION_SHORT_PAYER_HIGH_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionPhysicalFixedIbor SWAPTION_LONG_RECEIVER_HIGH = SWAPTION_LONG_RECEIVER_HIGH_DEFINITION.toDerivative(REFERENCE_DATE);
  // Extrapolation
  private static final SwaptionPhysicalFixedIborSABRMethod METHOD_SABR = SwaptionPhysicalFixedIborSABRMethod.getInstance();
  private static final double CUT_OFF_STRIKE = 0.08;
  private static final double MU = 10.0;
  private static final SwaptionPhysicalFixedIborSABRExtrapolationRightMethod METHOD_SABR_EXTRAPOLATION = new SwaptionPhysicalFixedIborSABRExtrapolationRightMethod(CUT_OFF_STRIKE, MU);
  // Calculators
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueSABRSwaptionCalculator PVSSC = PresentValueSABRSwaptionCalculator.getInstance();
  private static final PresentValueSABRSwaptionRightExtrapolationCalculator PVSSXC = new PresentValueSABRSwaptionRightExtrapolationCalculator(CUT_OFF_STRIKE, MU);
  private static final PresentValueCurveSensitivitySABRSwaptionRightExtrapolationCalculator PVCSSSXC = new PresentValueCurveSensitivitySABRSwaptionRightExtrapolationCalculator(CUT_OFF_STRIKE, MU);

  private static final double SHIFT = 1.0E-6;
  private static final ParameterSensitivityParameterCalculator<SABRSwaptionProviderInterface> PS_SSX_C = new ParameterSensitivityParameterCalculator<>(PVCSSSXC);
  private static final ParameterSensitivitySABRSwaptionDiscountInterpolatedFDCalculator PS_SSX_FDC = new ParameterSensitivitySABRSwaptionDiscountInterpolatedFDCalculator(PVSSXC, SHIFT);

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+5;

  /**
   * Tests present value in the region where there is no extrapolation. Tests long/short parity.
   */
  @Test
  public void presentValueNoExtra() {
    final MultipleCurrencyAmount priceLongPayer = METHOD_SABR_EXTRAPOLATION.presentValue(SWAPTION_LONG_PAYER, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceShortPayer = METHOD_SABR_EXTRAPOLATION.presentValue(SWAPTION_SHORT_PAYER, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceLongReceiver = METHOD_SABR_EXTRAPOLATION.presentValue(SWAPTION_LONG_RECEIVER, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceShortReceiver = METHOD_SABR_EXTRAPOLATION.presentValue(SWAPTION_SHORT_RECEIVER, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceLongPayerNoExtra = SWAPTION_LONG_PAYER.accept(PVSSC, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceShortPayerNoExtra = SWAPTION_SHORT_PAYER.accept(PVSSC, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceLongReceiverNoExtra = SWAPTION_LONG_RECEIVER.accept(PVSSC, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceShortReceiverNoExtra = SWAPTION_SHORT_RECEIVER.accept(PVSSC, SABR_MULTICURVES);
    assertEquals("Swaption SABR extrapolation: below cut-off strike", priceLongPayerNoExtra.getAmount(EUR), priceLongPayer.getAmount(EUR), TOLERANCE_PV);
    assertEquals("Swaption SABR extrapolation: below cut-off strike", priceShortPayerNoExtra.getAmount(EUR), priceShortPayer.getAmount(EUR), TOLERANCE_PV);
    assertEquals("Swaption SABR extrapolation: below cut-off strike", priceLongReceiverNoExtra.getAmount(EUR), priceLongReceiver.getAmount(EUR), TOLERANCE_PV);
    assertEquals("Swaption SABR extrapolation: below cut-off strike", priceShortReceiverNoExtra.getAmount(EUR), priceShortReceiver.getAmount(EUR), TOLERANCE_PV);
    assertEquals("Swaption SABR extrapolation: below cut-off strike long/short parity", priceLongPayer.getAmount(EUR), -priceShortPayer.getAmount(EUR), TOLERANCE_PV);
    assertEquals("Swaption SABR extrapolation: below cut-off strike long/short parity", priceLongReceiver.getAmount(EUR), -priceShortReceiver.getAmount(EUR), TOLERANCE_PV);
  }

  /**
   * Tests present value at the limit of extrapolation. Tests long/short parity.
   */
  @Test
  public void presentValueLimit() {
    final double highStrike = 0.0801;
    final SwapFixedIborDefinition swapPayerHighStrike = SwapFixedIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, EUR1YEURIBOR6M, NOTIONAL, highStrike, FIXED_IS_PAYER);
    final SwapFixedIborDefinition swapReceiverHighStrike = SwapFixedIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, EUR1YEURIBOR6M, NOTIONAL, highStrike, !FIXED_IS_PAYER);
    final SwaptionPhysicalFixedIborDefinition swaptionDefinitionLongPayerHighStrike = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapPayerHighStrike, true, IS_LONG);
    final SwaptionPhysicalFixedIborDefinition swaptionDefinitionShortPayerHighStrike = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapPayerHighStrike, true, !IS_LONG);
    final SwaptionPhysicalFixedIborDefinition swaptionDefinitionLongReceiverHighStrike = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapReceiverHighStrike, false, IS_LONG);
    final SwaptionPhysicalFixedIbor swaptionLongPayerHighStrike = swaptionDefinitionLongPayerHighStrike.toDerivative(REFERENCE_DATE);
    final SwaptionPhysicalFixedIbor swaptionShortPayerHighStrike = swaptionDefinitionShortPayerHighStrike.toDerivative(REFERENCE_DATE);
    final SwaptionPhysicalFixedIbor swaptionLongReceiverHighStrike = swaptionDefinitionLongReceiverHighStrike.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyAmount priceLongPayer = METHOD_SABR_EXTRAPOLATION.presentValue(swaptionLongPayerHighStrike, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceShortPayer = METHOD_SABR_EXTRAPOLATION.presentValue(swaptionShortPayerHighStrike, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceLongReceiver = METHOD_SABR_EXTRAPOLATION.presentValue(swaptionLongReceiverHighStrike, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceLongPayerSABR = swaptionLongPayerHighStrike.accept(PVSSXC, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceLongReceiverSABR = swaptionLongReceiverHighStrike.accept(PVSSXC, SABR_MULTICURVES);
    assertEquals("Swaption SABR extrapolation: extrapolation limit", priceLongPayerSABR.getAmount(EUR), priceLongPayer.getAmount(EUR), TOLERANCE_PV);
    assertEquals("Swaption SABR extrapolation: extrapolation limit", priceLongReceiverSABR.getAmount(EUR), priceLongReceiver.getAmount(EUR), TOLERANCE_PV);
    assertEquals("Swaption SABR extrapolation: long/short parity", priceLongPayer.getAmount(EUR), -priceShortPayer.getAmount(EUR), TOLERANCE_PV);
  }

  /**
   * Tests present value in the region where there is extrapolation. Test a hard-coded value. Tests long/short parity. Test payer/receiver/swap parity.
   */
  @Test
  public void presentValueExtra() {
    final MultipleCurrencyAmount priceLongPayer = METHOD_SABR_EXTRAPOLATION.presentValue(SWAPTION_LONG_PAYER_HIGH, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceShortPayer = METHOD_SABR_EXTRAPOLATION.presentValue(SWAPTION_SHORT_PAYER_HIGH, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceLongReceiver = METHOD_SABR_EXTRAPOLATION.presentValue(SWAPTION_LONG_RECEIVER_HIGH, SABR_MULTICURVES);
    final MultipleCurrencyAmount pricePayer = SWAP_PAYER_HIGH.accept(PVDC, MULTICURVES);
    final double priceLongPayerExpected = 189776.119; // Value from previous run
    final double priceLongReceiverExpected = 37512770.957; // Value from previous run
    assertEquals("Swaption SABR extrapolation: fixed value", priceLongPayerExpected, priceLongPayer.getAmount(EUR), TOLERANCE_PV);
    assertEquals("Swaption SABR extrapolation: fixed value", priceLongReceiverExpected, priceLongReceiver.getAmount(EUR), TOLERANCE_PV);
    assertEquals("Swaption SABR extrapolation: long/short parity", priceLongPayer.getAmount(EUR), -priceShortPayer.getAmount(EUR), TOLERANCE_PV);
    assertEquals("Swaption SABR extrapolation: payer/receiver/swap parity", pricePayer.getAmount(EUR), priceLongPayer.getAmount(EUR) - priceLongReceiver.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Test the present value sensitivity for a swaption with strike above the cut-off strike.
   */
  public void presentValueCurveSensitivityLongPayerExtra() {
    final MultipleCurrencyParameterSensitivity pvpsExact = PS_SSX_C.calculateSensitivity(SWAPTION_LONG_PAYER_HIGH, SABR_MULTICURVES, SABR_MULTICURVES.getMulticurveProvider().getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsFD = PS_SSX_FDC.calculateSensitivity(SWAPTION_LONG_PAYER_HIGH, SABR_MULTICURVES);
    AssertSensitivityObjects.assertEquals("SwaptionPhysicalFixedIborSABRMethod: presentValueCurveSensitivity ", pvpsExact, pvpsFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Test the present value sensitivity for a swaption with strike above the cut-off strike.
   */
  public void presentValueCurveSensitivityLongReceiverExtra() {
    final MultipleCurrencyParameterSensitivity pvpsExact = PS_SSX_C.calculateSensitivity(SWAPTION_LONG_RECEIVER_HIGH, SABR_MULTICURVES, SABR_MULTICURVES.getMulticurveProvider().getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsFD = PS_SSX_FDC.calculateSensitivity(SWAPTION_LONG_RECEIVER_HIGH, SABR_MULTICURVES);
    AssertSensitivityObjects.assertEquals("SwaptionPhysicalFixedIborSABRMethod: presentValueCurveSensitivity ", pvpsExact, pvpsFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Test the present value sensitivity for a swaption with strike above the cut-off strike.
   */
  public void presentValueCurveSensitivityShortReceiverExtra() {
    final MultipleCurrencyParameterSensitivity pvpsExact = PS_SSX_C.calculateSensitivity(SWAPTION_SHORT_PAYER_HIGH, SABR_MULTICURVES, SABR_MULTICURVES.getMulticurveProvider().getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsFD = PS_SSX_FDC.calculateSensitivity(SWAPTION_SHORT_PAYER_HIGH, SABR_MULTICURVES);
    AssertSensitivityObjects.assertEquals("SwaptionPhysicalFixedIborSABRMethod: presentValueCurveSensitivity ", pvpsExact, pvpsFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Test the present value sensitivity to SABR parameters for a swaption with strike above the cut-off strike.
   */
  public void presentValueSABRSensitivity() {
    // Swaption sensitivity
    final PresentValueSABRSensitivityDataBundle pvsLongPayer = METHOD_SABR_EXTRAPOLATION.presentValueSABRSensitivity(SWAPTION_LONG_PAYER_HIGH, SABR_MULTICURVES);
    PresentValueSABRSensitivityDataBundle pvsShortPayer = METHOD_SABR_EXTRAPOLATION.presentValueSABRSensitivity(SWAPTION_SHORT_PAYER_HIGH, SABR_MULTICURVES);
    // Long/short parity
    pvsShortPayer = pvsShortPayer.multiplyBy(-1.0);
    assertEquals(pvsLongPayer.getAlpha(), pvsShortPayer.getAlpha());
    // SABR sensitivity vs finite difference
    final MultipleCurrencyAmount pvLongPayer = METHOD_SABR_EXTRAPOLATION.presentValue(SWAPTION_LONG_PAYER_HIGH, SABR_MULTICURVES);
    final DoublesPair expectedExpiryTenor = DoublesPair.of(SWAPTION_LONG_PAYER_HIGH.getTimeToExpiry(), ANNUITY_TENOR_YEAR);
    final double shift = 0.000005;
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterAlphaBumped = SABRDataSets.createSABR1AlphaBumped(shift);
    final SABRSwaptionProviderDiscount sabrBundleAlphaBumped = new SABRSwaptionProviderDiscount(MULTICURVES, sabrParameterAlphaBumped, EUR1YEURIBOR6M);
    final MultipleCurrencyAmount pvLongPayerAlphaBumped = METHOD_SABR_EXTRAPOLATION.presentValue(SWAPTION_LONG_PAYER_HIGH, sabrBundleAlphaBumped);
    final double expectedAlphaSensi = (pvLongPayerAlphaBumped.getAmount(EUR) - pvLongPayer.getAmount(EUR)) / shift;
    assertEquals("Number of alpha sensitivity", pvsLongPayer.getAlpha().getMap().keySet().size(), 1);
    assertEquals("Alpha sensitivity expiry/tenor", pvsLongPayer.getAlpha().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Alpha sensitivity value", expectedAlphaSensi, pvsLongPayer.getAlpha().getMap().get(expectedExpiryTenor), 2.0E+3);
    // Beta sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterBetaBumped = SABRDataSets.createSABR1BetaBumped(shift);
    final SABRSwaptionProviderDiscount sabrBundleBetaBumped = new SABRSwaptionProviderDiscount(MULTICURVES, sabrParameterBetaBumped, EUR1YEURIBOR6M);
    final MultipleCurrencyAmount pvLongPayerBetaBumped = METHOD_SABR_EXTRAPOLATION.presentValue(SWAPTION_LONG_PAYER_HIGH, sabrBundleBetaBumped);
    final double expectedBetaSensi = (pvLongPayerBetaBumped.getAmount(EUR) - pvLongPayer.getAmount(EUR)) / shift;
    assertEquals("Number of Beta sensitivity", pvsLongPayer.getBeta().getMap().keySet().size(), 1);
    assertEquals("Beta sensitivity expiry/tenor", pvsLongPayer.getBeta().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Beta sensitivity value", expectedBetaSensi, pvsLongPayer.getBeta().getMap().get(expectedExpiryTenor), 1.5E+3);
    // Rho sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterRhoBumped = SABRDataSets.createSABR1RhoBumped(shift);
    final SABRSwaptionProviderDiscount sabrBundleRhoBumped = new SABRSwaptionProviderDiscount(MULTICURVES, sabrParameterRhoBumped, EUR1YEURIBOR6M);
    final MultipleCurrencyAmount pvLongPayerRhoBumped = METHOD_SABR_EXTRAPOLATION.presentValue(SWAPTION_LONG_PAYER_HIGH, sabrBundleRhoBumped);
    final double expectedRhoSensi = (pvLongPayerRhoBumped.getAmount(EUR) - pvLongPayer.getAmount(EUR)) / shift;
    assertEquals("Number of rho sensitivity", pvsLongPayer.getRho().getMap().keySet().size(), 1);
    assertEquals("Rho sensitivity expiry/tenor", pvsLongPayer.getRho().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Rho sensitivity value", expectedRhoSensi, pvsLongPayer.getRho().getMap().get(expectedExpiryTenor), 3.0E+0);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterNuBumped = SABRDataSets.createSABR1NuBumped(shift);
    final SABRSwaptionProviderDiscount sabrBundleNuBumped = new SABRSwaptionProviderDiscount(MULTICURVES, sabrParameterNuBumped, EUR1YEURIBOR6M);
    final MultipleCurrencyAmount pvLongPayerNuBumped = METHOD_SABR_EXTRAPOLATION.presentValue(SWAPTION_LONG_PAYER_HIGH, sabrBundleNuBumped);
    final double expectedNuSensi = (pvLongPayerNuBumped.getAmount(EUR) - pvLongPayer.getAmount(EUR)) / shift;
    assertEquals("Number of nu sensitivity", pvsLongPayer.getNu().getMap().keySet().size(), 1);
    assertEquals("Nu sensitivity expiry/tenor", pvsLongPayer.getNu().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Nu sensitivity value", expectedNuSensi, pvsLongPayer.getNu().getMap().get(expectedExpiryTenor), 5.0E+1);
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
      METHOD_SABR_EXTRAPOLATION.presentValue(SWAPTION_LONG_PAYER_HIGH, SABR_MULTICURVES);
      METHOD_SABR_EXTRAPOLATION.presentValueCurveSensitivity(SWAPTION_LONG_PAYER_HIGH, SABR_MULTICURVES);
      METHOD_SABR_EXTRAPOLATION.presentValueSABRSensitivity(SWAPTION_LONG_PAYER_HIGH, SABR_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " swaption payer price+delta+vega with SABR extrapolation: " + (endTime - startTime) + " ms");
    // Performance note: price+delta+vega payer extrapolation: 12-Dec-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 155 ms for 1000 swaptions.

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      METHOD_SABR.presentValue(SWAPTION_LONG_PAYER_HIGH, SABR_MULTICURVES);
      METHOD_SABR.presentValueCurveSensitivity(SWAPTION_LONG_PAYER_HIGH, SABR_MULTICURVES);
      METHOD_SABR.presentValueSABRSensitivity(SWAPTION_LONG_PAYER_HIGH, SABR_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " swaption payer price+delta+vega with standard SABR: " + (endTime - startTime) + " ms");
    // Performance note: price+delta+vega payer standard: 12-Dec-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 95 ms for 1000 swaptions.
  }

}
