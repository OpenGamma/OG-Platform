/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swap.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.swap.SwapMultilegDefinition;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapMultileg;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueMarketQuoteSensitivityCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueMarketQuoteSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test the swaps with multiple legs present value and related figures.
 */
@Test(groups = TestGroup.UNIT)
public class SwapMultilegCalculatorTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();

  private static final Calendar TARGET = new MondayToFridayCalendar("TRAGET");
  private static final IndexIborMaster INDEX_MASTER = IndexIborMaster.getInstance();
  private static final IborIndex EURIBOR3M = INDEX_MASTER.getIndex("EURIBOR3M");
  private static final IborIndex EURIBOR6M = INDEX_MASTER.getIndex("EURIBOR6M");
  private static final GeneratorSwapFixedIborMaster SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR6M = SWAP_MASTER.getGenerator("EUR1YEURIBOR6M", TARGET);
  private static final Period ANNUITY_TENOR = Period.ofYears(2);
  private static final Currency EUR = EURIBOR3M.getCurrency();

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2013, 3, 20);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2013, 10, 16);
  private static final double NOTIONAL = 100000000; // 100 m
  private static final double SPREAD = 0.0010; // 10 bps
  private static final StubType STUB = StubType.SHORT_START;

  // Swap represeting a EUR basis swap: 1 spread leg and 2 Euribor leg.
  private static final boolean IS_PAYER_SPREAD = true;
  private static final ZonedDateTime MATURITY_DATE = SETTLEMENT_DATE.plus(ANNUITY_TENOR);
  private static final int NB_LEGS = 3;
  @SuppressWarnings("rawtypes")
  private static final AnnuityDefinition[] LEGS_DEFINITION = new AnnuityDefinition[NB_LEGS];
  static {
    LEGS_DEFINITION[0] = AnnuityDefinitionBuilder.couponFixed(EUR, SETTLEMENT_DATE, MATURITY_DATE, EUR1YEURIBOR6M.getFixedLegPeriod(), TARGET,
        EUR1YEURIBOR6M.getFixedLegDayCount(), EUR1YEURIBOR6M.getBusinessDayConvention(), EUR1YEURIBOR6M.isEndOfMonth(), NOTIONAL, SPREAD, IS_PAYER_SPREAD, STUB, 0);
    LEGS_DEFINITION[1] = AnnuityDefinitionBuilder.couponIbor(SETTLEMENT_DATE, MATURITY_DATE, EURIBOR3M.getTenor(), NOTIONAL, EURIBOR3M,
        IS_PAYER_SPREAD, EURIBOR3M.getDayCount(), EURIBOR3M.getBusinessDayConvention(), EURIBOR3M.isEndOfMonth(), TARGET, STUB, 0);
    LEGS_DEFINITION[2] = AnnuityDefinitionBuilder.couponIbor(SETTLEMENT_DATE, MATURITY_DATE, EURIBOR6M.getTenor(), NOTIONAL, EURIBOR6M,
        !IS_PAYER_SPREAD, EURIBOR6M.getDayCount(), EURIBOR6M.getBusinessDayConvention(), EURIBOR6M.isEndOfMonth(), TARGET, STUB, 0);
  }
  @SuppressWarnings("unchecked")
  private static final SwapMultilegDefinition SWAP_MULTI_LEG_DEFINITION = new SwapMultilegDefinition(LEGS_DEFINITION);
  private static final SwapMultileg SWAP_MULTI_LEG = SWAP_MULTI_LEG_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQDC = ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator PSMQCSDC = ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();
  private static final PresentValueMarketQuoteSensitivityDiscountingCalculator PVMQSC = PresentValueMarketQuoteSensitivityDiscountingCalculator.getInstance();
  private static final PresentValueMarketQuoteSensitivityCurveSensitivityDiscountingCalculator PVMQSCSC =
      PresentValueMarketQuoteSensitivityCurveSensitivityDiscountingCalculator.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E-2;
  private static final double TOLERANCE_RATE = 1.0E-8;
  private static final double TOLERANCE_RATE_DELTA = 1.0E-8;

  @Test
  public void presentValueDiscountingCalculator() {
    final MultipleCurrencyAmount pvSwap = SWAP_MULTI_LEG.accept(PVDC, MULTICURVES);
    MultipleCurrencyAmount pvLegs = MultipleCurrencyAmount.of(EUR, 0.0);
    for (int loopleg = 0; loopleg < NB_LEGS; loopleg++) {
      pvLegs = pvLegs.plus(SWAP_MULTI_LEG.getLegs()[loopleg].accept(PVDC, MULTICURVES));
    }
    assertEquals("SwapMultileg: presentValueDiscountingCalculator", pvSwap.getAmount(EUR), pvLegs.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  public void presentValueCurveSensitivityDiscountingCalculator() {
    final MultipleCurrencyMulticurveSensitivity pvcsSwap = SWAP_MULTI_LEG.accept(PVCSDC, MULTICURVES);
    MultipleCurrencyMulticurveSensitivity pvcsLegs = SWAP_MULTI_LEG.getLegs()[0].accept(PVCSDC, MULTICURVES);
    for (int loopleg = 1; loopleg < NB_LEGS; loopleg++) {
      pvcsLegs = pvcsLegs.plus(SWAP_MULTI_LEG.getLegs()[loopleg].accept(PVCSDC, MULTICURVES));
    }
    AssertSensitivityObjects.assertEquals("SwapMultileg: presentValueCurveSensitivityDiscountingCalculator", pvcsLegs, pvcsSwap, TOLERANCE_PV_DELTA);
  }

  @Test
  public void parSpreadMarketQuoteDiscountingCalculator() {
    final double psmq = SWAP_MULTI_LEG.accept(PSMQDC, MULTICURVES);
    final double pv = -MULTICURVES.getFxRates().convert(SWAP_MULTI_LEG.accept(PVDC, MULTICURVES), SWAP_MULTI_LEG.getLegs()[0].getCurrency()).getAmount();
    final double pvbp = SWAP_MULTI_LEG.getLegs()[0].accept(PVMQSC, MULTICURVES);
    assertEquals("SwapMultileg: parSpreadMarketQuoteDiscountingCalculator", psmq, pv / pvbp, TOLERANCE_RATE);
  }

  @Test
  public void parSpreadMarketQuoteCurveSensitivityDiscountingCalculator() {
    final double pv = MULTICURVES.getFxRates().convert(SWAP_MULTI_LEG.accept(PVDC, MULTICURVES), SWAP_MULTI_LEG.getLegs()[0].getCurrency()).getAmount();
    final double pvbp = SWAP_MULTI_LEG.getLegs()[0].accept(PVMQSC, MULTICURVES);
    final MulticurveSensitivity pvcs = SWAP_MULTI_LEG.accept(PVCSDC, MULTICURVES).converted(EUR, MULTICURVES.getFxRates()).getSensitivity(EUR);
    final MulticurveSensitivity pvbpcs = SWAP_MULTI_LEG.getLegs()[0].accept(PVMQSCSC, MULTICURVES);
    final MulticurveSensitivity psmqcsExpected = pvcs.multipliedBy(-1.0d / pvbp).plus(pvbpcs.multipliedBy(pv / (pvbp * pvbp))).cleaned();
    final MulticurveSensitivity psmqcs = SWAP_MULTI_LEG.accept(PSMQCSDC, MULTICURVES).cleaned();
    AssertSensitivityObjects.assertEquals("SwapMultileg: presentValueCurveSensitivityDiscountingCalculator", psmqcs, psmqcsExpected, TOLERANCE_RATE_DELTA);
  }

}
