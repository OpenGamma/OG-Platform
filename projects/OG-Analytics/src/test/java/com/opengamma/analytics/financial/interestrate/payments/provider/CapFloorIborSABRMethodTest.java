/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

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
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrcap.PresentValueCurveSensitivitySABRCapCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrcap.PresentValueSABRCapCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrcap.PresentValueSABRSensitivitySABRCapCalculator;
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
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Test related to the pricing and sensitivity of the Ibor cap/floor with the SABR model.
 */
@Test(groups = TestGroup.UNIT)
public class CapFloorIborSABRMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex EURIBOR3M = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd()[0];
  private static final Currency EUR = EURIBOR3M.getCurrency();
  private static final Calendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();

  private static final SABRInterestRateParameters SABR_PARAMETER = SABRDataSets.createSABR1();
  private static final SABRCapProviderDiscount SABR_MULTICURVES = new SABRCapProviderDiscount(MULTICURVES, SABR_PARAMETER, EURIBOR3M);

  // Details
  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2011, 1, 3);
  private static final double NOTIONAL = 1000000; //1m
  private static final double STRIKE = 0.04;
  private static final boolean IS_CAP = true;
  // Definition description
  private static final CapFloorIborDefinition CAP_LONG_DEFINITION = CapFloorIborDefinition.from(NOTIONAL, FIXING_DATE, EURIBOR3M, STRIKE, IS_CAP, CALENDAR);
  private static final CouponIborDefinition COUPON_IBOR_DEFINITION = CouponIborDefinition.from(NOTIONAL, FIXING_DATE, EURIBOR3M, CALENDAR);
  private static final CouponFixedDefinition COUPON_STRIKE_DEFINITION = new CouponFixedDefinition(COUPON_IBOR_DEFINITION, STRIKE);
  private static final CapFloorIborDefinition CAP_SHORT_DEFINITION = CapFloorIborDefinition.from(-NOTIONAL, FIXING_DATE, EURIBOR3M, STRIKE, IS_CAP, CALENDAR);
  private static final CapFloorIborDefinition FLOOR_SHORT_DEFINITION = CapFloorIborDefinition.from(-NOTIONAL, FIXING_DATE, EURIBOR3M, STRIKE, !IS_CAP, CALENDAR);
  // Methods and calculator
  private static final CapFloorIborSABRCapMethod METHOD_CAP_SABR = CapFloorIborSABRCapMethod.getInstance();
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();
  private static final PresentValueDiscountingCalculator PVC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueSABRCapCalculator PVSCC = PresentValueSABRCapCalculator.getInstance();
  private static final PresentValueSABRSensitivitySABRCapCalculator PVSSSCC = PresentValueSABRSensitivitySABRCapCalculator.getInstance();
  private static final PresentValueCurveSensitivitySABRCapCalculator PVCSSCC = PresentValueCurveSensitivitySABRCapCalculator.getInstance();

  private static final double SHIFT = 1.0E-7;
  private static final ParameterSensitivityParameterCalculator<SABRCapProviderInterface> PS_SS_C = new ParameterSensitivityParameterCalculator<>(PVCSSCC);
  private static final ParameterSensitivitySABRCapDiscountInterpolatedFDCalculator PS_SS_FDC = new ParameterSensitivitySABRCapDiscountInterpolatedFDCalculator(PVSCC, SHIFT);
  // To derivative
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final CapFloorIbor CAP_LONG = (CapFloorIbor) CAP_LONG_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CouponIbor COUPON_IBOR = (CouponIbor) COUPON_IBOR_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CouponFixed COUPON_STRIKE = COUPON_STRIKE_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CapFloorIbor CAP_SHORT = (CapFloorIbor) CAP_SHORT_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CapFloorIbor FLOOR_SHORT = (CapFloorIbor) FLOOR_SHORT_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2;

  @Test
  /**
   * Test the present value using the method with the direct formula (Black with implied volatility).
   */
  public void presentValue() {
    final MultipleCurrencyAmount methodPrice = METHOD_CAP_SABR.presentValue(CAP_LONG, SABR_MULTICURVES);
    final double df = MULTICURVES.getDiscountFactor(EUR, CAP_LONG.getPaymentTime());
    final double forward = MULTICURVES.getSimplyCompoundForwardRate(EURIBOR3M, CAP_LONG.getFixingPeriodStartTime(), CAP_LONG.getFixingPeriodEndTime(), CAP_LONG.getFixingAccrualFactor());
    final double maturity = CAP_LONG.getFixingPeriodEndTime() - CAP_LONG.getFixingPeriodStartTime();
    final double volatility = SABR_PARAMETER.getVolatility(CAP_LONG.getFixingTime(), maturity, STRIKE, forward);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, df, volatility);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(STRIKE, CAP_LONG.getFixingTime(), IS_CAP);
    final Function1D<BlackFunctionData, Double> funcBlack = BLACK_FUNCTION.getPriceFunction(option);
    final double expectedPrice = funcBlack.evaluate(dataBlack) * CAP_LONG.getNotional() * CAP_LONG.getPaymentYearFraction();
    assertEquals("Cap/floor: SABR pricing", expectedPrice, methodPrice.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Test the present value using the method and the calculator.
   */
  public void presentValueMethodVsCalculator() {
    final double expectedPv = METHOD_CAP_SABR.presentValue(CAP_LONG, SABR_MULTICURVES).getAmount(EUR);
    final double pv = CAP_LONG.accept(PVSCC, SABR_MULTICURVES).getAmount(EUR);
    assertEquals("Cap/floor SABR pricing: method and calculator", expectedPv, pv, TOLERANCE_PV);
  }

  @Test
  /**
   * Test several present value parities: long/short, cap/floor/forward
   */
  public void presentValueParity() {
    final double priceCapLong = METHOD_CAP_SABR.presentValue(CAP_LONG, SABR_MULTICURVES).getAmount(EUR);
    final double priceCapShort = METHOD_CAP_SABR.presentValue(CAP_SHORT, SABR_MULTICURVES).getAmount(EUR);
    assertEquals("Cap/floor - SABR pricing: long/short parity", -priceCapLong, priceCapShort, TOLERANCE_PV);
    final double priceFloorShort = METHOD_CAP_SABR.presentValue(FLOOR_SHORT, SABR_MULTICURVES).getAmount(EUR);
    final double priceIbor = COUPON_IBOR.accept(PVC, MULTICURVES).getAmount(EUR);
    final double priceStrike = COUPON_STRIKE.accept(PVC, MULTICURVES).getAmount(EUR);
    assertEquals("Cap/floor - SABR pricing: cap/floor parity", priceIbor - priceStrike, priceCapLong + priceFloorShort, TOLERANCE_PV);
  }

  @Test
  /**
   * Tests present value curve sensitivity when the valuation date is on trade date.
   */
  public void presentValueCurveSensitivityCap() {
    final MultipleCurrencyParameterSensitivity pvpsExact = PS_SS_C.calculateSensitivity(CAP_LONG, SABR_MULTICURVES, SABR_MULTICURVES.getMulticurveProvider().getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsFD = PS_SS_FDC.calculateSensitivity(CAP_LONG, SABR_MULTICURVES);
    AssertSensitivityObjects.assertEquals("SwaptionPhysicalFixedIborSABRMethod: presentValueCurveSensitivity ", pvpsExact, pvpsFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests the present value SABR parameters sensitivity: Method vs Calculator.
   */
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final MultipleCurrencyMulticurveSensitivity pvcsMethod = METHOD_CAP_SABR.presentValueCurveSensitivity(CAP_LONG, SABR_MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvcsCalculator = CAP_LONG.accept(PVCSSCC, SABR_MULTICURVES);
    assertEquals("Cap/floor SABR: Present value SABR sensitivity: method vs calculator", pvcsMethod, pvcsCalculator);
  }

  @Test
  /**
   * Test the present value SABR parameters sensitivity against a finite difference computation.
   */
  public void presentValueSABRSensitivity() {
    final double pv = METHOD_CAP_SABR.presentValue(CAP_LONG, SABR_MULTICURVES).getAmount(EUR);
    final PresentValueSABRSensitivityDataBundle pvsCapLong = METHOD_CAP_SABR.presentValueSABRSensitivity(CAP_LONG, SABR_MULTICURVES);
    PresentValueSABRSensitivityDataBundle pvsCapShort = METHOD_CAP_SABR.presentValueSABRSensitivity(CAP_SHORT, SABR_MULTICURVES);
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
    final double pvLongPayerAlphaBumped = METHOD_CAP_SABR.presentValue(CAP_LONG, sabrBundleAlphaBumped).getAmount(EUR);
    final double expectedAlphaSensi = (pvLongPayerAlphaBumped - pv) / shiftAlpha;
    assertEquals("Number of alpha sensitivity", pvsCapLong.getAlpha().getMap().keySet().size(), 1);
    assertEquals("Alpha sensitivity expiry/tenor", pvsCapLong.getAlpha().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Alpha sensitivity value", expectedAlphaSensi, pvsCapLong.getAlpha().getMap().get(expectedExpiryTenor), 2.0E+0);
    // Rho sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterRhoBumped = SABRDataSets.createSABR1RhoBumped();
    final SABRCapProviderDiscount sabrBundleRhoBumped = new SABRCapProviderDiscount(MULTICURVES, sabrParameterRhoBumped, EURIBOR3M);
    final double pvLongPayerRhoBumped = METHOD_CAP_SABR.presentValue(CAP_LONG, sabrBundleRhoBumped).getAmount(EUR);
    final double expectedRhoSensi = (pvLongPayerRhoBumped - pv) / shift;
    assertEquals("Number of rho sensitivity", pvsCapLong.getRho().getMap().keySet().size(), 1);
    assertEquals("Rho sensitivity expiry/tenor", pvsCapLong.getRho().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Rho sensitivity value", pvsCapLong.getRho().getMap().get(expectedExpiryTenor), expectedRhoSensi, 1.0E-2);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterNuBumped = SABRDataSets.createSABR1NuBumped();
    final SABRCapProviderDiscount sabrBundleNuBumped = new SABRCapProviderDiscount(MULTICURVES, sabrParameterNuBumped, EURIBOR3M);
    final double pvLongPayerNuBumped = METHOD_CAP_SABR.presentValue(CAP_LONG, sabrBundleNuBumped).getAmount(EUR);
    final double expectedNuSensi = (pvLongPayerNuBumped - pv) / shift;
    assertEquals("Number of nu sensitivity", pvsCapLong.getNu().getMap().keySet().size(), 1);
    assertTrue("Nu sensitivity expiry/tenor", pvsCapLong.getNu().getMap().keySet().contains(expectedExpiryTenor));
    assertEquals("Nu sensitivity value", pvsCapLong.getNu().getMap().get(expectedExpiryTenor), expectedNuSensi, 5.0E-2);
  }

  @Test
  /**
   * Tests the present value SABR parameters sensitivity: Method vs Calculator.
   */
  public void presentValueSABRSensitivityMethodVsCalculator() {
    final PresentValueSABRSensitivityDataBundle pvssMethod = METHOD_CAP_SABR.presentValueSABRSensitivity(CAP_LONG, SABR_MULTICURVES);
    final PresentValueSABRSensitivityDataBundle pvssCalculator = CAP_LONG.accept(PVSSSCC, SABR_MULTICURVES);
    assertEquals("Cap/floor SABR: Present value SABR sensitivity: method vs calculator", pvssMethod, pvssCalculator);
  }

}
