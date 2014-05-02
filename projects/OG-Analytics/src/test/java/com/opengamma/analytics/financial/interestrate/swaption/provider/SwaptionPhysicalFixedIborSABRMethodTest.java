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
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.provider.SwapFixedCouponDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganAlternativeVolatilityFunction;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.PresentValueSABRHullWhiteMonteCarloCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueCurveSensitivitySABRSwaptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueSABRSensitivitySABRSwaptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueSABRSwaptionCalculator;
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
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Triple;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SwaptionPhysicalFixedIborSABRMethodTest {

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
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, EURIBOR6M.getSpotLag(), CALENDAR);
  private static final int ANNUITY_TENOR_YEAR = 5;
  private static final Period ANNUITY_TENOR = Period.ofYears(ANNUITY_TENOR_YEAR);
  private static final double NOTIONAL = 100000000; //100m
  private static final double RATE = 0.0325;
  private static final SwapFixedIborDefinition SWAP_PAYER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, EUR1YEURIBOR6M, NOTIONAL, RATE, true);
  private static final SwapFixedIborDefinition SWAP_RECEIVER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, EUR1YEURIBOR6M, NOTIONAL, RATE, false);

  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_LONG_PAYER_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, true, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_LONG_RECEIVER_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_DEFINITION, false, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_SHORT_PAYER_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, true, !IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_SHORT_RECEIVER_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_DEFINITION, false, !IS_LONG);

  private static final SwapFixedCoupon<Coupon> SWAP_PAYER = SWAP_PAYER_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwapFixedCoupon<Coupon> SWAP_RECEIVER = SWAP_RECEIVER_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionPhysicalFixedIbor SWAPTION_LONG_PAYER = SWAPTION_LONG_PAYER_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionPhysicalFixedIbor SWAPTION_LONG_RECEIVER = SWAPTION_LONG_RECEIVER_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionPhysicalFixedIbor SWAPTION_SHORT_PAYER = SWAPTION_SHORT_PAYER_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionPhysicalFixedIbor SWAPTION_SHORT_RECEIVER = SWAPTION_SHORT_RECEIVER_DEFINITION.toDerivative(REFERENCE_DATE);

  // Calculators

  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();
  private static final SwaptionPhysicalFixedIborSABRMethod METHOD_SWPT_SABR = SwaptionPhysicalFixedIborSABRMethod.getInstance();

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

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNoSABRHaganSensi() {
    final SABRInterestRateParameters sabrParameter = SABRDataSets.createSABR1(new SABRHaganAlternativeVolatilityFunction());
    final SABRSwaptionProviderDiscount sabrBundle = new SABRSwaptionProviderDiscount(MULTICURVES, sabrParameter, EUR1YEURIBOR6M);
    PVCSSSC.visit(SWAPTION_LONG_PAYER, sabrBundle);
  }

  /**
   * Tests present value with respect to a hard-coded value. Tests against the explicit formula. Tests long/short parity and payer/receiver/swap parity.
   */
  @Test
  public void testPresentValue() {
    // Swaption pricing.
    final MultipleCurrencyAmount priceLongPayer = METHOD_SWPT_SABR.presentValue(SWAPTION_LONG_PAYER, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceShortPayer = METHOD_SWPT_SABR.presentValue(SWAPTION_SHORT_PAYER, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceLongReceiver = METHOD_SWPT_SABR.presentValue(SWAPTION_LONG_RECEIVER, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceShortReceiver = METHOD_SWPT_SABR.presentValue(SWAPTION_SHORT_RECEIVER, SABR_MULTICURVES);
    final double pvbp = METHOD_SWAP.presentValueBasisPoint(SWAP_PAYER, MULTICURVES);
    final double forward = SWAP_PAYER.accept(PRDC, MULTICURVES);
    final double maturity = SWAP_PAYER.getFirstLeg().getNthPayment(SWAP_PAYER.getFirstLeg().getNumberOfPayments() - 1).getPaymentTime() - SWAPTION_LONG_PAYER.getSettlementTime();
    assertEquals(maturity, ANNUITY_TENOR_YEAR, 1E-2);
    final double volatility = SABR_PARAMETER.getVolatility(SWAPTION_LONG_PAYER.getTimeToExpiry(), maturity, RATE, forward);
    assertEquals("SwaptionPhysicalFixedIborSABRMethod: implied volatility", volatility, METHOD_SWPT_SABR.impliedVolatility(SWAPTION_LONG_PAYER, SABR_MULTICURVES), TOLERANCE_PV);
    final BlackFunctionData data = new BlackFunctionData(forward, pvbp, volatility);
    final Function1D<BlackFunctionData, Double> func = BLACK_FUNCTION.getPriceFunction(SWAPTION_LONG_PAYER);
    final double expectedPrice = func.evaluate(data);
    assertEquals("SwaptionPhysicalFixedIborSABRMethod: presentValue", expectedPrice, priceLongPayer.getAmount(EUR), TOLERANCE_PV);
    // Long/Short parity
    assertEquals("SwaptionPhysicalFixedIborSABRMethod: presentValue", priceLongPayer.getAmount(EUR), -priceShortPayer.getAmount(EUR), TOLERANCE_PV);
    assertEquals("SwaptionPhysicalFixedIborSABRMethod: presentValue", priceLongReceiver.getAmount(EUR), -priceShortReceiver.getAmount(EUR), TOLERANCE_PV);
    // Payer/Receiver parity
    final MultipleCurrencyAmount priceSwapPayer = SWAP_PAYER.accept(PVDC, MULTICURVES);
    final MultipleCurrencyAmount priceSwapReceiver = SWAP_RECEIVER.accept(PVDC, MULTICURVES);
    assertEquals("SwaptionPhysicalFixedIborSABRMethod: presentValue", priceSwapPayer.getAmount(EUR), priceLongPayer.getAmount(EUR) + priceShortReceiver.getAmount(EUR), TOLERANCE_PV);
    assertEquals("SwaptionPhysicalFixedIborSABRMethod: presentValue", priceSwapReceiver.getAmount(EUR), priceLongReceiver.getAmount(EUR) + priceShortPayer.getAmount(EUR), TOLERANCE_PV);
  }

  /**
   * Test the absence of arbitrage between swaptions with same cash-flows but different conventions.
   */
  @Test
  public void testPresentValueConventionArbitrage() {
    final double rate360 = 0.0360;
    final IndexSwap index360 = new IndexSwap(EUR1YEURIBOR6M.getFixedLegPeriod(), DayCounts.ACT_360, EURIBOR6M, ANNUITY_TENOR, CALENDAR);
    final SwapFixedIborDefinition swap360 = SwapFixedIborDefinition.from(SETTLEMENT_DATE, index360, NOTIONAL, rate360, true, CALENDAR);
    final SwaptionPhysicalFixedIborDefinition swaption360Definition = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swap360, true, IS_LONG);
    final SwaptionPhysicalFixedIbor swaption360 = swaption360Definition.toDerivative(REFERENCE_DATE);
    final double rate365 = 0.0365;
    final IndexSwap index365 = new IndexSwap(EUR1YEURIBOR6M.getFixedLegPeriod(), DayCounts.ACT_365, EURIBOR6M, ANNUITY_TENOR, CALENDAR);
    final SwapFixedIborDefinition swap365 = SwapFixedIborDefinition.from(SETTLEMENT_DATE, index365, NOTIONAL, rate365, true, CALENDAR);
    final SwaptionPhysicalFixedIborDefinition swaption365Definition = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swap365, true, IS_LONG);
    final SwaptionPhysicalFixedIbor swaption365 = swaption365Definition.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyAmount price360 = METHOD_SWPT_SABR.presentValue(swaption360, SABR_MULTICURVES);
    final MultipleCurrencyAmount price365 = METHOD_SWPT_SABR.presentValue(swaption365, SABR_MULTICURVES);
    assertEquals("SwaptionPhysicalFixedIborSABRMethod: presentValue", price360.getAmount(EUR), price365.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the method against the present value calculator.
   */
  public void presentValueMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD_SWPT_SABR.presentValue(SWAPTION_LONG_PAYER, SABR_MULTICURVES);
    final MultipleCurrencyAmount pvCalculator = PVSSC.visit(SWAPTION_LONG_PAYER, SABR_MULTICURVES);
    assertEquals("SwaptionPhysicalFixedIborSABRMethod: present value : method and calculator", pvMethod, pvCalculator);
  }

  @Test
  /**
   * Tests present value curve sensitivity when the valuation date is on trade date.
   */
  public void presentValueCurveSensitivity() {
    final MultipleCurrencyParameterSensitivity pvpsExact = PS_SS_C.calculateSensitivity(SWAPTION_SHORT_RECEIVER, SABR_MULTICURVES, SABR_MULTICURVES.getMulticurveProvider().getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsFD = PS_SS_FDC.calculateSensitivity(SWAPTION_SHORT_RECEIVER, SABR_MULTICURVES);
    AssertSensitivityObjects.assertEquals("SwaptionPhysicalFixedIborSABRMethod: presentValueCurveSensitivity ", pvpsExact, pvpsFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests the method against the present value curve sensitivity calculator.
   */
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final MultipleCurrencyMulticurveSensitivity pvcsMethod = METHOD_SWPT_SABR.presentValueCurveSensitivity(SWAPTION_LONG_PAYER, SABR_MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvcsCalculator = PVCSSSC.visit(SWAPTION_LONG_PAYER, SABR_MULTICURVES);
    assertEquals("SwaptionPhysicalFixedIborSABRMethod: present value curve sensitivity: method and calculator", pvcsMethod, pvcsCalculator);
  }

  @Test
  public void presentValueSABRSensitivity() {
    // Swaption sensitivity
    final PresentValueSABRSensitivityDataBundle pvsLongPayer = METHOD_SWPT_SABR.presentValueSABRSensitivity(SWAPTION_LONG_PAYER, SABR_MULTICURVES);
    PresentValueSABRSensitivityDataBundle pvsShortPayer = METHOD_SWPT_SABR.presentValueSABRSensitivity(SWAPTION_SHORT_PAYER, SABR_MULTICURVES);
    // Long/short parity
    pvsShortPayer = pvsShortPayer.multiplyBy(-1.0);
    assertEquals("SwaptionPhysicalFixedIborSABRMethod: presentValueSABRSensitivity", pvsLongPayer.getAlpha(), pvsShortPayer.getAlpha());
    // SABR sensitivity vs finite difference
    final double pvLongPayer = METHOD_SWPT_SABR.presentValue(SWAPTION_LONG_PAYER, SABR_MULTICURVES).getAmount(EUR);
    final double shift = 1.0E-8;
    final DoublesPair expectedExpiryTenor = DoublesPair.of(SWAPTION_LONG_PAYER.getTimeToExpiry(), ANNUITY_TENOR_YEAR);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterAlphaBumped = SABRDataSets.createSABR1AlphaBumped(shift);
    final SABRSwaptionProviderDiscount sabrBundleAlphaBumped = new SABRSwaptionProviderDiscount(MULTICURVES, sabrParameterAlphaBumped, EUR1YEURIBOR6M);
    final double pvLongPayerAlphaBumped = METHOD_SWPT_SABR.presentValue(SWAPTION_LONG_PAYER, sabrBundleAlphaBumped).getAmount(EUR);
    final double expectedAlphaSensi = (pvLongPayerAlphaBumped - pvLongPayer) / shift;
    assertEquals("Number of alpha sensitivity", pvsLongPayer.getAlpha().getMap().keySet().size(), 1);
    assertEquals("Alpha sensitivity expiry/tenor", pvsLongPayer.getAlpha().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Alpha sensitivity value", pvsLongPayer.getAlpha().getMap().get(expectedExpiryTenor), expectedAlphaSensi, 1.0E+1);
    // Rho sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterRhoBumped = SABRDataSets.createSABR1RhoBumped(shift);
    final SABRSwaptionProviderDiscount sabrBundleRhoBumped = new SABRSwaptionProviderDiscount(MULTICURVES, sabrParameterRhoBumped, EUR1YEURIBOR6M);
    final double pvLongPayerRhoBumped = METHOD_SWPT_SABR.presentValue(SWAPTION_LONG_PAYER, sabrBundleRhoBumped).getAmount(EUR);
    final double expectedRhoSensi = (pvLongPayerRhoBumped - pvLongPayer) / shift;
    assertEquals("Number of rho sensitivity", pvsLongPayer.getRho().getMap().keySet().size(), 1);
    assertEquals("Rho sensitivity expiry/tenor", pvsLongPayer.getRho().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Rho sensitivity value", pvsLongPayer.getRho().getMap().get(expectedExpiryTenor), expectedRhoSensi, 5.0E-1);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterNuBumped = SABRDataSets.createSABR1NuBumped(shift);
    final SABRSwaptionProviderDiscount sabrBundleNuBumped = new SABRSwaptionProviderDiscount(MULTICURVES, sabrParameterNuBumped, EUR1YEURIBOR6M);
    final double pvLongPayerNuBumped = METHOD_SWPT_SABR.presentValue(SWAPTION_LONG_PAYER, sabrBundleNuBumped).getAmount(EUR);
    final double expectedNuSensi = (pvLongPayerNuBumped - pvLongPayer) / shift;
    assertEquals("Number of nu sensitivity", pvsLongPayer.getNu().getMap().keySet().size(), 1);
    assertEquals("Nu sensitivity expiry/tenor", pvsLongPayer.getNu().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Nu sensitivity value", pvsLongPayer.getNu().getMap().get(expectedExpiryTenor), expectedNuSensi, 1.0E+0);
  }

  @Test
  /**
   * Tests the present value SABR parameters sensitivity: Method vs Calculator.
   */
  public void presentValueSABRSensitivityMethodVsCalculator() {
    final PresentValueSABRSensitivityDataBundle pvssMethod = METHOD_SWPT_SABR.presentValueSABRSensitivity(SWAPTION_LONG_PAYER, SABR_MULTICURVES);
    final PresentValueSABRSensitivityDataBundle pvssCalculator = PVSSSSC.visit(SWAPTION_LONG_PAYER, SABR_MULTICURVES);
    assertEquals("Swaption Physical SABR: Present value SABR sensitivity: method vs calculator", pvssMethod, pvssCalculator);
  }

  @Test(enabled = true)
  /**
   * Tests the present value of the Hull-White Monte-Carlo calibrated to SABR swaption.
   */
  public void presentValueSABRHullWhiteMonteCarlo() {
    final MultipleCurrencyAmount pvSABR = METHOD_SWPT_SABR.presentValue(SWAPTION_LONG_PAYER, SABR_MULTICURVES);
    final PresentValueSABRHullWhiteMonteCarloCalculator pvcSABRHWMC = PresentValueSABRHullWhiteMonteCarloCalculator.getInstance();
    final MultipleCurrencyAmount pvMC = SWAPTION_LONG_PAYER.accept(pvcSABRHWMC, SABR_MULTICURVES);
    assertEquals("Swaption Physical SABR: Present value using Hull-White by Monte Carlo", pvSABR.getAmount(EUR), pvMC.getAmount(EUR), 2.5E+4);
  }

  //  @Test(enabled = false)
  //  /**
  //   * Analyzes the smoothness of sensitivities.
  //   */
  //  public void analysisSensitivities() {
  //    IborIndex USDLIBOR3M = IndexIborMaster.getInstance().getIndex("USDLIBOR3M", CALENDAR);
  //    Period expiryTenor = Period.ofYears(5);
  //    Period underlyingTenor = Period.ofYears(10);
  //    ZonedDateTime expiryDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, expiryTenor, USDLIBOR3M);
  //    ZonedDateTime settleDate = ScheduleCalculator.getAdjustedDate(expiryDate, USDLIBOR3M.getSpotLag(), CALENDAR);
  //    GeneratorSwapFixedIbor USD6MLIBOR3M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("USD6MLIBOR3M", CALENDAR);
  //    double notional = 1000000; // 1m
  //    double strikeRange = 0.1150;
  //    double strikeStart = 0.0050;
  //    int nbStrike = 50;
  //    double[] strikes = new double[nbStrike + 1];
  //    SwaptionPhysicalFixedIbor[] swaptions = new SwaptionPhysicalFixedIbor[nbStrike + 1];
  //    double[] pv = new double[nbStrike + 1];
  //    double[] pv01Dsc = new double[nbStrike + 1];
  //    double[] pv01Fwd = new double[nbStrike + 1];
  //    double[] alphaSensi = new double[nbStrike + 1];
  //    double[] rhoSensi = new double[nbStrike + 1];
  //    double[] nuSensi = new double[nbStrike + 1];
  //    for (int loopstrike = 0; loopstrike <= nbStrike; loopstrike++) {
  //      strikes[loopstrike] = strikeStart + loopstrike * strikeRange / nbStrike;
  //      SwapFixedIborDefinition swapDefinition = SwapFixedIborDefinition.from(settleDate, underlyingTenor, USD6MLIBOR3M, notional, strikes[loopstrike], true);
  //      SwaptionPhysicalFixedIborDefinition swaptionDefinition = SwaptionPhysicalFixedIborDefinition.from(expiryDate, swapDefinition, true);
  //      swaptions[loopstrike] = swaptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
  //      pv[loopstrike] = METHOD_SWAT_SABR.presentValue(swaptions[loopstrike], SABR_BUNDLE).getAmount();
  //      PresentValueSABRSensitivityDataBundle sabrSensi = METHOD_SWAT_SABR.presentValueSABRSensitivity(swaptions[loopstrike], SABR_BUNDLE);
  //      Map<String, Double> pv01 = PV01C.visit(swaptions[loopstrike], SABR_BUNDLE);
  //      alphaSensi[loopstrike] = sabrSensi.getAlpha().toSingleValue();
  //      rhoSensi[loopstrike] = sabrSensi.getRho().toSingleValue();
  //      nuSensi[loopstrike] = sabrSensi.getNu().toSingleValue();
  //      pv01Dsc[loopstrike] = pv01.get(CURVES_NAME[0]);
  //      pv01Fwd[loopstrike] = pv01.get(CURVES_NAME[1]);
  //    }
  //    @SuppressWarnings("unused")
  //    double atm = PRDC.visit(swaptions[0].getUnderlyingSwap(), CURVES);
  //  }

  @SuppressWarnings("unused")
  @Test(enabled = false)
  /**
   * Test of performance. In normal testing, "enabled = false".
   */
  public void performance() {
    long startTime, endTime;
    final int nbTest = 5000;
    final MultipleCurrencyAmount[] pv = new MultipleCurrencyAmount[nbTest];
    final MultipleCurrencyMulticurveSensitivity[] pvcs = new MultipleCurrencyMulticurveSensitivity[nbTest];
    final PresentValueSABRSensitivityDataBundle[] pvss = new PresentValueSABRSensitivityDataBundle[nbTest];
    Triple<MultipleCurrencyAmount, MultipleCurrencyMulticurveSensitivity, PresentValueSABRSensitivityDataBundle> pvad;

    // 1. Separately compute: Price, Curve Sensitivity and SABR Parameter Sensitivity
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pv[looptest] = METHOD_SWPT_SABR.presentValue(SWAPTION_LONG_PAYER, SABR_MULTICURVES);
      pvcs[looptest] = METHOD_SWPT_SABR.presentValueCurveSensitivity(SWAPTION_LONG_PAYER, SABR_MULTICURVES);
      pvss[looptest] = METHOD_SWPT_SABR.presentValueSABRSensitivity(SWAPTION_LONG_PAYER, SABR_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println("SwaptionPhysicalFixedIborSABRMethodTest: " + nbTest + " physical swaptions SABR (price+delta+vega separately): " + (endTime - startTime) + " ms");
    // Performance note: price+delta: 16-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 380 ms for 5000 swaptions.

    // 2. Together compute: Price, Curve Sensitivity and SABR Parameter Sensitivity
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvad = METHOD_SWPT_SABR.presentValueAD(SWAPTION_LONG_PAYER, SABR_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println("SwaptionPhysicalFixedIborSABRMethodTest: " + nbTest + " physical swaptions SABR (price+delta+vega together): " + (endTime - startTime) + " ms");
    // Performance note: price: 16-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 210 ms for 5000 swaptions.

    // 3. Compute only Present Value
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pv[looptest] = METHOD_SWPT_SABR.presentValue(SWAPTION_LONG_PAYER, SABR_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println("SwaptionPhysicalFixedIborSABRMethodTest: " + nbTest + " physical swaptions SABR (price): " + (endTime - startTime) + " ms");
    // Performance note: price: 16-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 75 ms for 5000 swaptions.

    // 4. Compute present value using Hull-White Monte Carlo. Only for MC testing.
    final int nbTest2 = 10;
    final PresentValueSABRHullWhiteMonteCarloCalculator pvcSABRHWMC = PresentValueSABRHullWhiteMonteCarloCalculator.getInstance();
    final MultipleCurrencyAmount[] pvMC = new MultipleCurrencyAmount[nbTest2];

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest2; looptest++) {
      pvMC[looptest] = SWAPTION_LONG_PAYER.accept(pvcSABRHWMC, SABR_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println("SwaptionPhysicalFixedIborSABRMethodTest: " + nbTest2 + " physical swaptions SABR + Hull-White Monte Carlo: " + (endTime - startTime) + " ms");
    //     Performance note: price+delta+vega: 12-Jun-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 310 ms for 10 swaptions.

    //    double sum = 0.0;
    //    for (int looptest = 0; looptest < nbTest; looptest++) {
    //      sum += pv[looptest];
    //      sum += pvss[looptest].getAlpha().hashCode();
    //    }
  }

  //  @Test(enabled = false)
  //  /**
  //   * Test of relative performance of constructor, toDerivative and pricing. In normal testing, "enabled = false".
  //   */
  //  public void constructorPerformance() {
  //    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
  //    final SABRInterestRateParameters sabrParameter = TestsDataSetsSABR.createSABR1();
  //    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
  //    SwapFixedIborDefinition swap = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER);
  //    SwaptionPhysicalFixedIborDefinition swaptionDefinition = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swap, IS_LONG);
  //    SwaptionPhysicalFixedIbor swaption = swaptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
  //    long startTime, endTime;
  //    final int nbTest = 1000;
  //    startTime = System.currentTimeMillis();
  //    for (int looptest = 0; looptest < nbTest; looptest++) {
  //      swap = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER);
  //      swaptionDefinition = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swap, IS_LONG);
  //    }
  //    endTime = System.currentTimeMillis();
  //    System.out.println(nbTest + " physical swaptions SABR (definition construction): " + (endTime - startTime) + " ms");
  //    startTime = System.currentTimeMillis();
  //    for (int looptest = 0; looptest < nbTest; looptest++) {
  //      swaption = swaptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
  //    }
  //    endTime = System.currentTimeMillis();
  //    System.out.println(nbTest + " physical swaptions SABR (to derivatives): " + (endTime - startTime) + " ms");
  //    startTime = System.currentTimeMillis();
  //    for (int looptest = 0; looptest < nbTest; looptest++) {
  //      PVDC.visit(swaption, sabrBundle);
  //      PVCSC_SABR.visit(swaption, sabrBundle);
  //      PVSSC_SABR.visit(swaption, sabrBundle);
  //    }
  //    endTime = System.currentTimeMillis();
  //    System.out.println(nbTest + " physical swaptions SABR (pv+delta+SABR vega): " + (endTime - startTime) + " ms");
  //    // Performance note: definition construction: 15-Jun-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 45 ms for 1000 swaptions.
  //    // Performance note: to derivatives: 15-Jun-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 40 ms for 1000 swaptions.
  //    // Performance note: pv: 15-Jun-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 45 ms for 1000 swaptions.
  //    // Performance note: pv+delta: 15-Jun-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 130 ms for 1000 swaptions.
  //    // Performance note: pv+delta+SABR vega: 15-Jun-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 175 ms for 1000 swaptions.
  //  }
}
