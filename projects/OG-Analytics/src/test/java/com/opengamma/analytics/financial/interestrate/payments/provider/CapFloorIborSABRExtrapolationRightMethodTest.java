/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CapFloorIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.SABRExtrapolationRightFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrcap.PresentValueCurveSensitivitySABRCapRightExtrapolationCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrcap.PresentValueSABRCapRightExtrapolationCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.SABRDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRCapProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRCapProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.sabrcap.ParameterSensitivitySABRCapDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Test related to the pricing and sensitivity of the Ibor cap/floor with the SABR model and extrapolation for high strikes.
 */
@Test(groups = TestGroup.UNIT)
public class CapFloorIborSABRExtrapolationRightMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex EURIBOR3M = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd()[0];
  private static final Currency EUR = EURIBOR3M.getCurrency();
  private static final Calendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();

  private static final SABRInterestRateParameters SABR_PARAMETERS = SABRDataSets.createSABR1();
  private static final SABRCapProviderDiscount SABR_MULTICURVES = new SABRCapProviderDiscount(MULTICURVES, SABR_PARAMETERS, EURIBOR3M);

  // Details
  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2011, 1, 3);
  private static final double NOTIONAL = 1000000; //1m
  private static final double STRIKE = 0.04;
  private static final double STRIKE_HIGH = 0.09;
  private static final boolean IS_CAP = true;
  // Definition description
  private static final CapFloorIborDefinition CAP_LONG_DEFINITION = CapFloorIborDefinition.from(NOTIONAL, FIXING_DATE, EURIBOR3M, STRIKE, IS_CAP, CALENDAR);
  private static final CapFloorIborDefinition CAP_HIGH_LONG_DEFINITION = CapFloorIborDefinition.from(NOTIONAL, FIXING_DATE, EURIBOR3M, STRIKE_HIGH, IS_CAP, CALENDAR);
  private static final CouponIborDefinition COUPON_IBOR_DEFINITION = CouponIborDefinition.from(NOTIONAL, FIXING_DATE, EURIBOR3M, CALENDAR);
  private static final CouponFixedDefinition COUPON_STRIKE_DEFINITION = new CouponFixedDefinition(COUPON_IBOR_DEFINITION, STRIKE);
  private static final CouponFixedDefinition COUPON_STRIKE_HIGH_DEFINITION = new CouponFixedDefinition(COUPON_IBOR_DEFINITION, STRIKE_HIGH);
  private static final CapFloorIborDefinition CAP_SHORT_DEFINITION = CapFloorIborDefinition.from(-NOTIONAL, FIXING_DATE, EURIBOR3M, STRIKE, IS_CAP, CALENDAR);
  private static final CapFloorIborDefinition CAP_HIGH_SHORT_DEFINITION = CapFloorIborDefinition.from(-NOTIONAL, FIXING_DATE, EURIBOR3M, STRIKE_HIGH, IS_CAP, CALENDAR);
  private static final CapFloorIborDefinition FLOOR_SHORT_DEFINITION = CapFloorIborDefinition.from(-NOTIONAL, FIXING_DATE, EURIBOR3M, STRIKE, !IS_CAP, CALENDAR);
  private static final CapFloorIborDefinition FLOOR_HIGH_SHORT_DEFINITION = CapFloorIborDefinition.from(-NOTIONAL, FIXING_DATE, EURIBOR3M, STRIKE_HIGH, !IS_CAP, CALENDAR);
  // Methods and calculator
  private static final double CUT_OFF_STRIKE = 0.08;
  private static final double MU = 2.50;
  private static final CapFloorIborSABRCapExtrapolationRightMethod METHOD_CAP_X = new CapFloorIborSABRCapExtrapolationRightMethod(CUT_OFF_STRIKE, MU);
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueSABRCapRightExtrapolationCalculator PVSCXC = new PresentValueSABRCapRightExtrapolationCalculator(CUT_OFF_STRIKE, MU);
  private static final PresentValueCurveSensitivitySABRCapRightExtrapolationCalculator PVCSSCXC = new PresentValueCurveSensitivitySABRCapRightExtrapolationCalculator(CUT_OFF_STRIKE, MU);
  private static final double SHIFT = 1.0E-7;
  private static final ParameterSensitivityParameterCalculator<SABRCapProviderInterface> PS_SS_C = new ParameterSensitivityParameterCalculator<>(PVCSSCXC);
  private static final ParameterSensitivitySABRCapDiscountInterpolatedFDCalculator PS_SS_FDC = new ParameterSensitivitySABRCapDiscountInterpolatedFDCalculator(PVSCXC, SHIFT);
  // To derivative
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final CapFloorIbor CAP_LONG = (CapFloorIbor) CAP_LONG_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CapFloorIbor CAP_HIGH_LONG = (CapFloorIbor) CAP_HIGH_LONG_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CouponIbor COUPON_IBOR = (CouponIbor) COUPON_IBOR_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CouponFixed COUPON_STRIKE = COUPON_STRIKE_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CouponFixed COUPON_STRIKE_HIGH = COUPON_STRIKE_HIGH_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CapFloorIbor CAP_SHORT = (CapFloorIbor) CAP_SHORT_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CapFloorIbor CAP_HIGH_SHORT = (CapFloorIbor) CAP_HIGH_SHORT_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CapFloorIbor FLOOR_SHORT = (CapFloorIbor) FLOOR_SHORT_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CapFloorIbor FLOOR_HIGH_SHORT = (CapFloorIbor) FLOOR_HIGH_SHORT_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2;

  // Data

  @Test
  /**
   * Test the present value using the method with the direct formula with extrapolation.
   */
  public void presentValueBelowCutOff() {
    final MultipleCurrencyAmount methodPrice = METHOD_CAP_X.presentValue(CAP_LONG, SABR_MULTICURVES);
    final double df = MULTICURVES.getDiscountFactor(EUR, CAP_LONG.getPaymentTime());
    final double forward = MULTICURVES.getSimplyCompoundForwardRate(EURIBOR3M, CAP_LONG.getFixingPeriodStartTime(), CAP_LONG.getFixingPeriodEndTime(), CAP_LONG.getFixingAccrualFactor());
    final double maturity = CAP_LONG.getFixingPeriodEndTime() - CAP_LONG.getFixingPeriodStartTime();
    final DoublesPair expiryMaturity = DoublesPair.of(CAP_LONG.getFixingTime(), maturity);
    final double alpha = SABR_PARAMETERS.getAlpha(expiryMaturity);
    final double beta = SABR_PARAMETERS.getBeta(expiryMaturity);
    final double rho = SABR_PARAMETERS.getRho(expiryMaturity);
    final double nu = SABR_PARAMETERS.getNu(expiryMaturity);
    final SABRFormulaData sabrParam = new SABRFormulaData(alpha, beta, rho, nu);
    final SABRExtrapolationRightFunction sabrExtrapolation = new SABRExtrapolationRightFunction(forward, sabrParam, CUT_OFF_STRIKE, CAP_LONG.getFixingTime(), MU);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(CAP_LONG.getStrike(), CAP_LONG.getFixingTime(), CAP_LONG.isCap());
    final double expectedPrice = sabrExtrapolation.price(option) * CAP_LONG.getNotional() * CAP_LONG.getPaymentYearFraction() * df;
    assertEquals("Cap/floor: SABR with extrapolation pricing", expectedPrice, methodPrice.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Test the present value using the method with the direct formula with extrapolation.
   */
  public void presentValueAboveCutOff() {
    final MultipleCurrencyAmount methodPrice = METHOD_CAP_X.presentValue(CAP_HIGH_LONG, SABR_MULTICURVES);
    final double df = MULTICURVES.getDiscountFactor(EUR, CAP_HIGH_LONG.getPaymentTime());
    final double forward = MULTICURVES.getSimplyCompoundForwardRate(EURIBOR3M, CAP_HIGH_LONG.getFixingPeriodStartTime(), CAP_HIGH_LONG.getFixingPeriodEndTime(), CAP_HIGH_LONG.getFixingAccrualFactor());
    final double maturity = CAP_HIGH_LONG.getFixingPeriodEndTime() - CAP_LONG.getFixingPeriodStartTime();
    final DoublesPair expiryMaturity = DoublesPair.of(CAP_HIGH_LONG.getFixingTime(), maturity);
    final double alpha = SABR_PARAMETERS.getAlpha(expiryMaturity);
    final double beta = SABR_PARAMETERS.getBeta(expiryMaturity);
    final double rho = SABR_PARAMETERS.getRho(expiryMaturity);
    final double nu = SABR_PARAMETERS.getNu(expiryMaturity);
    final SABRFormulaData sabrParam = new SABRFormulaData(alpha, beta, rho, nu);
    final SABRExtrapolationRightFunction sabrExtrapolation = new SABRExtrapolationRightFunction(forward, sabrParam, CUT_OFF_STRIKE, CAP_HIGH_LONG.getFixingTime(), MU);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(CAP_HIGH_LONG.getStrike(), CAP_HIGH_LONG.getFixingTime(), CAP_HIGH_LONG.isCap());
    final double expectedPrice = sabrExtrapolation.price(option) * CAP_HIGH_LONG.getNotional() * CAP_HIGH_LONG.getPaymentYearFraction() * df;
    assertEquals("Cap/floor: SABR with extrapolation pricing", expectedPrice, methodPrice.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Test the present value using the method with the direct formula with extrapolation.
   */
  public void presentValueLongShortParityBelowCutOff() {
    final MultipleCurrencyAmount priceLong = METHOD_CAP_X.presentValue(CAP_LONG, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceShort = METHOD_CAP_X.presentValue(CAP_SHORT, SABR_MULTICURVES);
    assertEquals("Cap/floor: SABR with extrapolation pricing: long/short parity", priceLong.getAmount(EUR), -priceShort.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Test the present value using the method with the direct formula with extrapolation.
   */
  public void presentValueLongShortParityAboveCutOff() {
    final MultipleCurrencyAmount priceLong = METHOD_CAP_X.presentValue(CAP_HIGH_LONG, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceShort = METHOD_CAP_X.presentValue(CAP_HIGH_SHORT, SABR_MULTICURVES);
    assertEquals("Cap/floor: SABR with extrapolation pricing: long/short parity", priceLong.getAmount(EUR), -priceShort.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Test the cap/floor/forward parity below the cut-off strike.
   */
  public void presentValueCapFloorParityBelowCutOff() {
    final MultipleCurrencyAmount priceCap = METHOD_CAP_X.presentValue(CAP_LONG, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceFloor = METHOD_CAP_X.presentValue(FLOOR_SHORT, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceCouponStrike = COUPON_STRIKE.accept(PVDC, MULTICURVES);
    final MultipleCurrencyAmount priceCouponIbor = COUPON_IBOR.accept(PVDC, MULTICURVES);
    assertEquals("Cap/floor: SABR with extrapolation pricing: cap/floor parity", priceCouponIbor.getAmount(EUR) - priceCouponStrike.getAmount(EUR),
        priceCap.getAmount(EUR) + priceFloor.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Test the cap/floor/forward parity above the cut-off strike.
   */
  public void presentValueCapFloorParityAboveCutOff() {
    final MultipleCurrencyAmount priceCap = METHOD_CAP_X.presentValue(CAP_HIGH_LONG, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceFloor = METHOD_CAP_X.presentValue(FLOOR_HIGH_SHORT, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceCouponStrike = COUPON_STRIKE_HIGH.accept(PVDC, MULTICURVES);
    final MultipleCurrencyAmount priceCouponIbor = COUPON_IBOR.accept(PVDC, MULTICURVES);
    assertEquals("Cap/floor: SABR with extrapolation pricing: cap/floor parity", priceCouponIbor.getAmount(EUR) - priceCouponStrike.getAmount(EUR),
        priceCap.getAmount(EUR) + priceFloor.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Test the present value using the method with the direct formula with extrapolation.
   */
  public void presentValueMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD_CAP_X.presentValue(CAP_LONG, SABR_MULTICURVES);
    final MultipleCurrencyAmount pvCalculator = CAP_LONG.accept(PVSCXC, SABR_MULTICURVES);
    assertEquals("Cap/floor: SABR with extrapolation pricing - Method vs Calculator", pvMethod.getAmount(EUR), pvCalculator.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Test the present value rate sensitivity against a finite difference computation; strike below the cut-off strike. Test sensitivity long/short parity.
   */
  public void presentValueCurveSensitivityBelowCutOff() {
    final MultipleCurrencyParameterSensitivity pvpsExact = PS_SS_C.calculateSensitivity(CAP_LONG, SABR_MULTICURVES, SABR_MULTICURVES.getMulticurveProvider().getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsFD = PS_SS_FDC.calculateSensitivity(CAP_LONG, SABR_MULTICURVES);
    AssertSensitivityObjects.assertEquals("SwaptionPhysicalFixedIborSABRMethod: presentValueCurveSensitivity ", pvpsExact, pvpsFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Test the present value rate sensitivity against a finite difference computation; strike above the cut-off strike. Test sensitivity long/short parity.
   */
  public void testPresentValueSensitivityAboveCutOff() {
    final MultipleCurrencyParameterSensitivity pvpsExact = PS_SS_C.calculateSensitivity(CAP_HIGH_LONG, SABR_MULTICURVES, SABR_MULTICURVES.getMulticurveProvider().getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsFD = PS_SS_FDC.calculateSensitivity(CAP_HIGH_LONG, SABR_MULTICURVES);
    AssertSensitivityObjects.assertEquals("SwaptionPhysicalFixedIborSABRMethod: presentValueCurveSensitivity ", pvpsExact, pvpsFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Test the present value using the method with the direct formula with extrapolation.
   */
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final MultipleCurrencyMulticurveSensitivity pvsMethod = METHOD_CAP_X.presentValueCurveSensitivity(CAP_HIGH_LONG, SABR_MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvsCalculator = CAP_HIGH_LONG.accept(PVCSSCXC, SABR_MULTICURVES);
    AssertSensitivityObjects.assertEquals("Cap/floor: SABR with extrapolation pv curve sensitivity - Method vs Calculator", pvsMethod, pvsCalculator, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Test the present value SABR parameters sensitivity against a finite difference computation; strike below the cut-off strike.
   */
  public void testPresentValueSABRSensitivityBelowCutOff() {
    final MultipleCurrencyAmount pv = METHOD_CAP_X.presentValue(CAP_LONG, SABR_MULTICURVES);
    final PresentValueSABRSensitivityDataBundle pvsCapLong = METHOD_CAP_X.presentValueSABRSensitivity(CAP_LONG, SABR_MULTICURVES);
    PresentValueSABRSensitivityDataBundle pvsCapShort = METHOD_CAP_X.presentValueSABRSensitivity(CAP_SHORT, SABR_MULTICURVES);
    // Long/short parity
    pvsCapShort = pvsCapShort.multiplyBy(-1.0);
    assertEquals(pvsCapShort.getAlpha(), pvsCapLong.getAlpha());
    // SABR sensitivity vs finite difference
    final double shift = 0.0001;
    final double shiftAlpha = 0.00001;
    final DoublesPair expectedExpiryTenor = DoublesPair.of(CAP_LONG.getFixingTime(), CAP_LONG.getFixingPeriodEndTime() - CAP_LONG.getFixingPeriodStartTime());
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterAlphaBumped = SABRDataSets.createSABR1AlphaBumped(shiftAlpha);
    final SABRCapProviderDiscount sabrBundleAlphaBumped = new SABRCapProviderDiscount(MULTICURVES, sabrParameterAlphaBumped, EURIBOR3M);
    final MultipleCurrencyAmount pvLongPayerAlphaBumped = METHOD_CAP_X.presentValue(CAP_LONG, sabrBundleAlphaBumped);
    final double expectedAlphaSensi = (pvLongPayerAlphaBumped.getAmount(EUR) - pv.getAmount(EUR)) / shiftAlpha;
    assertEquals("Number of alpha sensitivity", pvsCapLong.getAlpha().getMap().keySet().size(), 1);
    assertEquals("Alpha sensitivity expiry/tenor", pvsCapLong.getAlpha().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Alpha sensitivity value", expectedAlphaSensi, pvsCapLong.getAlpha().getMap().get(expectedExpiryTenor), 1.5E-0);
    // Rho sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterRhoBumped = SABRDataSets.createSABR1RhoBumped();
    final SABRCapProviderDiscount sabrBundleRhoBumped = new SABRCapProviderDiscount(MULTICURVES, sabrParameterRhoBumped, EURIBOR3M);
    final MultipleCurrencyAmount pvLongPayerRhoBumped = METHOD_CAP_X.presentValue(CAP_LONG, sabrBundleRhoBumped);
    final double expectedRhoSensi = (pvLongPayerRhoBumped.getAmount(EUR) - pv.getAmount(EUR)) / shift;
    assertEquals("Number of rho sensitivity", pvsCapLong.getRho().getMap().keySet().size(), 1);
    assertEquals("Rho sensitivity expiry/tenor", pvsCapLong.getRho().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Rho sensitivity value", pvsCapLong.getRho().getMap().get(expectedExpiryTenor), expectedRhoSensi, 1.0E-2);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterNuBumped = SABRDataSets.createSABR1NuBumped();
    final SABRCapProviderDiscount sabrBundleNuBumped = new SABRCapProviderDiscount(MULTICURVES, sabrParameterNuBumped, EURIBOR3M);
    final MultipleCurrencyAmount pvLongPayerNuBumped = METHOD_CAP_X.presentValue(CAP_LONG, sabrBundleNuBumped);
    final double expectedNuSensi = (pvLongPayerNuBumped.getAmount(EUR) - pv.getAmount(EUR)) / shift;
    assertEquals("Number of nu sensitivity", pvsCapLong.getNu().getMap().keySet().size(), 1);
    assertEquals("Nu sensitivity expiry/tenor", pvsCapLong.getNu().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Nu sensitivity value", pvsCapLong.getNu().getMap().get(expectedExpiryTenor), expectedNuSensi, 5.0E-2);
  }

  @Test
  /**
   * Test the present value SABR parameters sensitivity against a finite difference computation; strike above the cut-off strike.
   */
  public void testPresentValueSABRSensitivityAboveCutOff() {
    final MultipleCurrencyAmount pv = METHOD_CAP_X.presentValue(CAP_HIGH_LONG, SABR_MULTICURVES);
    final PresentValueSABRSensitivityDataBundle pvsCapLong = METHOD_CAP_X.presentValueSABRSensitivity(CAP_HIGH_LONG, SABR_MULTICURVES);
    PresentValueSABRSensitivityDataBundle pvsCapShort = METHOD_CAP_X.presentValueSABRSensitivity(CAP_HIGH_SHORT, SABR_MULTICURVES);
    // Long/short parity
    pvsCapShort = pvsCapShort.multiplyBy(-1.0);
    assertEquals(pvsCapShort.getAlpha(), pvsCapLong.getAlpha());
    // SABR sensitivity vs finite difference
    final double shift = 0.0001;
    final double shiftAlpha = 0.00001;
    final DoublesPair expectedExpiryTenor = DoublesPair.of(CAP_HIGH_LONG.getFixingTime(), CAP_HIGH_LONG.getFixingPeriodEndTime() - CAP_HIGH_LONG.getFixingPeriodStartTime());
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterAlphaBumped = SABRDataSets.createSABR1AlphaBumped(shiftAlpha);
    final SABRCapProviderDiscount sabrBundleAlphaBumped = new SABRCapProviderDiscount(MULTICURVES, sabrParameterAlphaBumped, EURIBOR3M);
    final MultipleCurrencyAmount pvLongPayerAlphaBumped = METHOD_CAP_X.presentValue(CAP_HIGH_LONG, sabrBundleAlphaBumped);
    final double expectedAlphaSensi = (pvLongPayerAlphaBumped.getAmount(EUR) - pv.getAmount(EUR)) / shiftAlpha;
    assertEquals("Number of alpha sensitivity", pvsCapLong.getAlpha().getMap().keySet().size(), 1);
    assertEquals("Alpha sensitivity expiry/tenor", pvsCapLong.getAlpha().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Alpha sensitivity value", expectedAlphaSensi, pvsCapLong.getAlpha().getMap().get(expectedExpiryTenor), 1.0E-0);
    // Rho sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterRhoBumped = SABRDataSets.createSABR1RhoBumped();
    final SABRCapProviderDiscount sabrBundleRhoBumped = new SABRCapProviderDiscount(MULTICURVES, sabrParameterRhoBumped, EURIBOR3M);
    final MultipleCurrencyAmount pvLongPayerRhoBumped = METHOD_CAP_X.presentValue(CAP_HIGH_LONG, sabrBundleRhoBumped);
    final double expectedRhoSensi = (pvLongPayerRhoBumped.getAmount(EUR) - pv.getAmount(EUR)) / shift;
    assertEquals("Number of rho sensitivity", pvsCapLong.getRho().getMap().keySet().size(), 1);
    assertEquals("Rho sensitivity expiry/tenor", pvsCapLong.getRho().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Rho sensitivity value", pvsCapLong.getRho().getMap().get(expectedExpiryTenor), expectedRhoSensi, 1.0E-1);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterNuBumped = SABRDataSets.createSABR1NuBumped();
    final SABRCapProviderDiscount sabrBundleNuBumped = new SABRCapProviderDiscount(MULTICURVES, sabrParameterNuBumped, EURIBOR3M);
    final MultipleCurrencyAmount pvLongPayerNuBumped = METHOD_CAP_X.presentValue(CAP_HIGH_LONG, sabrBundleNuBumped);
    final double expectedNuSensi = (pvLongPayerNuBumped.getAmount(EUR) - pv.getAmount(EUR)) / shift;
    assertEquals("Number of nu sensitivity", pvsCapLong.getNu().getMap().keySet().size(), 1);
    assertEquals("Nu sensitivity expiry/tenor", pvsCapLong.getNu().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Nu sensitivity value", pvsCapLong.getNu().getMap().get(expectedExpiryTenor), expectedNuSensi, 2.0E-1);
  }

}
