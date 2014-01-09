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

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.provider.SwapFixedCouponDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganAlternativeVolatilityFunction;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueCurveSensitivitySABRSwaptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueSABRSensitivitySABRSwaptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueSABRSwaptionCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.SABRDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.sabrswaption.ParameterSensitivitySABRSwaptionDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class to test the present value and present value rate sensitivity of the cash-settled European swaption in the SABR model.
 */
@Test(groups = TestGroup.UNIT)
public class SwaptionCashFixedIborSABRMethodTest {

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
  private static final boolean IS_EOM = true;
  private static final int ANNUITY_TENOR_YEAR = 5;
  private static final Period ANNUITY_TENOR = Period.ofYears(ANNUITY_TENOR_YEAR);
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, EURIBOR6M.getSpotLag(), CALENDAR);
  private static final double NOTIONAL = 100000000; //100m
  //  Fixed leg: Semi-annual bond
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final double RATE = 0.0175;
  private static final boolean FIXED_IS_PAYER = true;
  private static final AnnuityCouponFixedDefinition FIXED_ANNUITY_PAYER = AnnuityCouponFixedDefinition.from(EUR, SETTLEMENT_DATE, ANNUITY_TENOR, FIXED_PAYMENT_PERIOD, CALENDAR, FIXED_DAY_COUNT,
      EURIBOR6M.getBusinessDayConvention(), IS_EOM, NOTIONAL, RATE, FIXED_IS_PAYER);
  private static final AnnuityCouponFixedDefinition FIXED_ANNUITY_RECEIVER = AnnuityCouponFixedDefinition.from(EUR, SETTLEMENT_DATE, ANNUITY_TENOR, FIXED_PAYMENT_PERIOD, CALENDAR, FIXED_DAY_COUNT,
      EURIBOR6M.getBusinessDayConvention(), IS_EOM, NOTIONAL, RATE, !FIXED_IS_PAYER);
  //  Ibor leg: quarterly money
  private static final AnnuityCouponIborDefinition IBOR_ANNUITY_RECEIVER = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, EURIBOR6M, !FIXED_IS_PAYER, CALENDAR);
  private static final AnnuityCouponIborDefinition IBOR_ANNUITY_PAYER = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, EURIBOR6M, FIXED_IS_PAYER, CALENDAR);
  // Swaption construction: All combinations
  private static final SwapFixedIborDefinition SWAP_DEFINITION_PAYER = new SwapFixedIborDefinition(FIXED_ANNUITY_PAYER, IBOR_ANNUITY_RECEIVER);
  private static final SwapFixedIborDefinition SWAP_DEFINITION_RECEIVER = new SwapFixedIborDefinition(FIXED_ANNUITY_RECEIVER, IBOR_ANNUITY_PAYER);
  private static final SwaptionCashFixedIborDefinition SWAPTION_DEFINITION_LONG_PAYER = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_PAYER, true, IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_DEFINITION_LONG_RECEIVER = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_RECEIVER, false, IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_DEFINITION_SHORT_PAYER = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_PAYER, true, !IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_DEFINITION_SHORT_RECEIVER = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_RECEIVER, false, !IS_LONG);
  // to derivatives
  private static final SwapFixedCoupon<Coupon> SWAP_PAYER = SWAP_DEFINITION_PAYER.toDerivative(REFERENCE_DATE);
  private static final SwaptionCashFixedIbor SWAPTION_LONG_PAYER = SWAPTION_DEFINITION_LONG_PAYER.toDerivative(REFERENCE_DATE);
  private static final SwaptionCashFixedIbor SWAPTION_LONG_RECEIVER = SWAPTION_DEFINITION_LONG_RECEIVER.toDerivative(REFERENCE_DATE);
  private static final SwaptionCashFixedIbor SWAPTION_SHORT_PAYER = SWAPTION_DEFINITION_SHORT_PAYER.toDerivative(REFERENCE_DATE);
  private static final SwaptionCashFixedIbor SWAPTION_SHORT_RECEIVER = SWAPTION_DEFINITION_SHORT_RECEIVER.toDerivative(REFERENCE_DATE);
  // Calculators
  private static final SwaptionCashFixedIborSABRMethod METHOD_SWPT_CASH = SwaptionCashFixedIborSABRMethod.getInstance();
  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();

  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();
  private static final PresentValueSABRSwaptionCalculator PVSSC = PresentValueSABRSwaptionCalculator.getInstance();
  private static final PresentValueCurveSensitivitySABRSwaptionCalculator PVCSSSC = PresentValueCurveSensitivitySABRSwaptionCalculator.getInstance();
  private static final PresentValueSABRSensitivitySABRSwaptionCalculator PVSSSSC = PresentValueSABRSensitivitySABRSwaptionCalculator.getInstance();

  private static final double SHIFT = 1.0E-7;
  private static final ParameterSensitivityParameterCalculator<SABRSwaptionProviderInterface> PS_SS_C = new ParameterSensitivityParameterCalculator<>(PVCSSSC);
  private static final ParameterSensitivitySABRSwaptionDiscountInterpolatedFDCalculator PS_SS_FDC = new ParameterSensitivitySABRSwaptionDiscountInterpolatedFDCalculator(PVSSC, SHIFT);
  // Pricing functions
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+0; //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move.

  @Test
  public void presentValue() {
    // Swaption pricing.
    final MultipleCurrencyAmount priceLongPayer = SWAPTION_LONG_PAYER.accept(PVSSC, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceShortPayer = SWAPTION_SHORT_PAYER.accept(PVSSC, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceLongReceiver = SWAPTION_LONG_RECEIVER.accept(PVSSC, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceShortReceiver = SWAPTION_SHORT_RECEIVER.accept(PVSSC, SABR_MULTICURVES);
    // From previous run
    final double expectedPriceLongPayer = 2419978.690;
    assertEquals("SwaptionCashFixedIborSABRMethod: presentValue", expectedPriceLongPayer, priceLongPayer.getAmount(EUR), TOLERANCE_PV);
    final double forward = SWAP_PAYER.accept(PRDC, MULTICURVES);
    final double pvbp = METHOD_SWAP.getAnnuityCash(SWAP_PAYER, forward);
    final double maturity = SWAP_PAYER.getFirstLeg().getNthPayment(SWAP_PAYER.getFirstLeg().getNumberOfPayments() - 1).getPaymentTime() - SWAPTION_LONG_PAYER.getSettlementTime();
    assertEquals(maturity, ANNUITY_TENOR_YEAR, 1E-2);
    final double volatility = SABR_PARAMETER.getVolatility(SWAPTION_LONG_PAYER.getTimeToExpiry(), maturity, RATE, forward);
    final BlackFunctionData data = new BlackFunctionData(forward, 1.0, volatility);
    final Function1D<BlackFunctionData, Double> func = BLACK_FUNCTION.getPriceFunction(SWAPTION_LONG_PAYER);
    final double df = MULTICURVES.getDiscountFactor(EUR, SWAPTION_LONG_PAYER.getSettlementTime());
    final double expectedPrice = df * pvbp * func.evaluate(data);
    assertEquals("SwaptionCashFixedIborSABRMethod: presentValue", expectedPrice, priceLongPayer.getAmount(EUR), TOLERANCE_PV);
    // Long/Short parity
    assertEquals("SwaptionCashFixedIborSABRMethod: presentValue", priceLongPayer.getAmount(EUR), -priceShortPayer.getAmount(EUR), TOLERANCE_PV);
    assertEquals("SwaptionCashFixedIborSABRMethod: presentValue", priceLongReceiver.getAmount(EUR), -priceShortReceiver.getAmount(EUR), TOLERANCE_PV);
    // No payer/Receiver parity for cash-settled swaptions.
  }

  @Test
  /**
   * Test the present value calculator with an array of derivative: one for the premium payment and one for the actual swaption.
   */
  //REVIEW: the method that this is testing (one that took an array of InstrumentDerivative has gone - leaving this test in for now
  public void presentValueWithPremium() {
    final double expectedPriceLongPayer = 2419978.690;
    final double premiumAmount = expectedPriceLongPayer / MULTICURVES.getDiscountFactor(EUR, SWAPTION_LONG_PAYER.getSettlementTime());
    final PaymentFixedDefinition premiumDefinition = new PaymentFixedDefinition(EUR, SETTLEMENT_DATE, -premiumAmount);
    final PaymentFixed premium = premiumDefinition.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyAmount pvPremium = premium.accept(PVDC, MULTICURVES);
    final MultipleCurrencyAmount swaptionPV = SWAPTION_LONG_PAYER.accept(PVSSC, SABR_MULTICURVES);
    assertEquals("swaption present value with premium", -expectedPriceLongPayer, pvPremium.getAmount(EUR), TOLERANCE_PV);
    assertEquals("swaption present value with premium", expectedPriceLongPayer, swaptionPV.getAmount(EUR), TOLERANCE_PV);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNoSABRHaganSensi() {
    final SABRInterestRateParameters sabrParameter2 = SABRDataSets.createSABR1(new SABRHaganAlternativeVolatilityFunction());
    final SABRSwaptionProviderDiscount sabr2 = new SABRSwaptionProviderDiscount(MULTICURVES, sabrParameter2, EUR1YEURIBOR6M);
    SWAPTION_LONG_PAYER.accept(PVSSSSC, sabr2);
  }

  @Test
  /**
   * Tests present value curve sensitivity when the valuation date is on trade date.
   */
  public void presentValueCurveSensitivity() {
    final MultipleCurrencyParameterSensitivity pvpsExact = PS_SS_C.calculateSensitivity(SWAPTION_LONG_PAYER, SABR_MULTICURVES, SABR_MULTICURVES.getMulticurveProvider().getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsFD = PS_SS_FDC.calculateSensitivity(SWAPTION_LONG_PAYER, SABR_MULTICURVES);
    AssertSensivityObjects.assertEquals("SwaptionPhysicalFixedIborSABRMethod: presentValueCurveSensitivity ", pvpsExact, pvpsFD, TOLERANCE_PV_DELTA);
  }

  @Test
  public void presentValueSABRSensitivity() {
    // Swaption sensitivity
    final PresentValueSABRSensitivityDataBundle pvsLongPayer = METHOD_SWPT_CASH.presentValueSABRSensitivity(SWAPTION_LONG_PAYER, SABR_MULTICURVES);
    PresentValueSABRSensitivityDataBundle pvsShortPayer = METHOD_SWPT_CASH.presentValueSABRSensitivity(SWAPTION_SHORT_PAYER, SABR_MULTICURVES);
    // Long/short parity
    pvsShortPayer = pvsShortPayer.multiplyBy(-1.0);
    assertEquals(pvsLongPayer.getAlpha(), pvsShortPayer.getAlpha());
    // SABR sensitivity vs finite difference
    final double pvLongPayer = METHOD_SWPT_CASH.presentValue(SWAPTION_LONG_PAYER, SABR_MULTICURVES).getAmount(EUR);
    final double shift = 0.00001;
    final DoublesPair expectedExpiryTenor = DoublesPair.of(SWAPTION_LONG_PAYER.getTimeToExpiry(), ANNUITY_TENOR_YEAR);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterAlphaBumped = SABRDataSets.createSABR1AlphaBumped(shift);
    final SABRSwaptionProviderDiscount sabrBundleAlphaBumped = new SABRSwaptionProviderDiscount(MULTICURVES, sabrParameterAlphaBumped, EUR1YEURIBOR6M);
    final double pvLongPayerAlphaBumped = METHOD_SWPT_CASH.presentValue(SWAPTION_LONG_PAYER, sabrBundleAlphaBumped).getAmount(EUR);
    final double expectedAlphaSensi = (pvLongPayerAlphaBumped - pvLongPayer) / shift;
    assertEquals("Number of alpha sensitivity", pvsLongPayer.getAlpha().getMap().keySet().size(), 1);
    assertEquals("Alpha sensitivity expiry/tenor", pvsLongPayer.getAlpha().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Alpha sensitivity value", pvsLongPayer.getAlpha().getMap().get(expectedExpiryTenor), expectedAlphaSensi, 1.5E+3);
    // Rho sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterRhoBumped = SABRDataSets.createSABR1RhoBumped(shift);
    final SABRSwaptionProviderDiscount sabrBundleRhoBumped = new SABRSwaptionProviderDiscount(MULTICURVES, sabrParameterRhoBumped, EUR1YEURIBOR6M);
    final double pvLongPayerRhoBumped = METHOD_SWPT_CASH.presentValue(SWAPTION_LONG_PAYER, sabrBundleRhoBumped).getAmount(EUR);
    final double expectedRhoSensi = (pvLongPayerRhoBumped - pvLongPayer) / shift;
    assertEquals("Number of rho sensitivity", pvsLongPayer.getRho().getMap().keySet().size(), 1);
    assertEquals("Rho sensitivity expiry/tenor", pvsLongPayer.getRho().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Rho sensitivity value", pvsLongPayer.getRho().getMap().get(expectedExpiryTenor), expectedRhoSensi, 1E+3);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterNuBumped = SABRDataSets.createSABR1NuBumped(shift);
    final SABRSwaptionProviderDiscount sabrBundleNuBumped = new SABRSwaptionProviderDiscount(MULTICURVES, sabrParameterNuBumped, EUR1YEURIBOR6M);
    final double pvLongPayerNuBumped = METHOD_SWPT_CASH.presentValue(SWAPTION_LONG_PAYER, sabrBundleNuBumped).getAmount(EUR);
    final double expectedNuSensi = (pvLongPayerNuBumped - pvLongPayer) / shift;
    assertEquals("Number of nu sensitivity", pvsLongPayer.getNu().getMap().keySet().size(), 1);
    assertEquals("Nu sensitivity expiry/tenor", pvsLongPayer.getNu().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Nu sensitivity value", pvsLongPayer.getNu().getMap().get(expectedExpiryTenor), expectedNuSensi, 1E+3);
  }

  @Test
  /**
   * Tests the present value SABR parameters sensitivity: Method vs Calculator.
   */
  public void presentValueSABRSensitivityMethodVsCalculator() {
    final PresentValueSABRSensitivityDataBundle pvssMethod = METHOD_SWPT_CASH.presentValueSABRSensitivity(SWAPTION_LONG_PAYER, SABR_MULTICURVES);
    final PresentValueSABRSensitivityDataBundle pvssCalculator = SWAPTION_LONG_PAYER.accept(PVSSSSC, SABR_MULTICURVES);
    assertEquals("Swaption Cash SABR: Present value SABR sensitivity: method vs calculator", pvssMethod, pvssCalculator);
  }

  @Test(enabled = false)
  /**
   * Test of performance. In normal testing, "enabled = false".
   */
  public void performance() {
    long startTime, endTime;
    final int nbTest = 1000;

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      SWAPTION_LONG_PAYER.accept(PVSSC, SABR_MULTICURVES);
      SWAPTION_LONG_PAYER.accept(PVCSSSC, SABR_MULTICURVES);
      SWAPTION_LONG_PAYER.accept(PVSSSSC, SABR_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " cash swaptions SABR (price+delta+vega): " + (endTime - startTime) + " ms");
    // Performance note: price+delta+vega: 11-Dec-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 145 ms for 1000 swaptions.
  }

}
