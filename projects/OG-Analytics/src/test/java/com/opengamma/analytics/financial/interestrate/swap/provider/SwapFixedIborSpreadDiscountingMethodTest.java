/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swap.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborSpreadDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the pricing and sensitivities of Swap Ibor with spread in the discounting method.
 */
@Test(groups = TestGroup.UNIT)
public class SwapFixedIborSpreadDiscountingMethodTest {

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 8, 31);
  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex[] INDEX_LIST = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd();
  private static final IborIndex EURIBOR3M = INDEX_LIST[0];
  private static final Calendar TARGET = MulticurveProviderDiscountDataSets.getEURCalendar();
  private static final Currency EUR = EURIBOR3M.getCurrency();

  private static final GeneratorSwapFixedIbor EUR1YEURIBOR3M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("EUR1YEURIBOR3M", TARGET);
  private static final GeneratorSwapFixedIbor EUR3MEURIBOR3M = new GeneratorSwapFixedIbor("EUR3MEURIBOR3M", EURIBOR3M.getTenor(), EURIBOR3M.getDayCount(), EURIBOR3M, TARGET);

  private static final Period START_TENOR = Period.ofMonths(6);
  private static final Period SWAP_TENOR = Period.ofYears(5);
  private static final ZonedDateTime START_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, START_TENOR, EURIBOR3M, TARGET);
  private static final double NOTIONAL = 123000000;
  private static final double SPREAD = 0.0010;
  private static final double FIXED_RATE = 0.0150;
  private static final boolean IS_PAYER = false;

  private static final SwapFixedIborSpreadDefinition SWAP_SPREAD_EUR1Y3M_DEFINITION = SwapFixedIborSpreadDefinition
      .from(START_DATE, SWAP_TENOR, EUR1YEURIBOR3M, NOTIONAL, FIXED_RATE, SPREAD, IS_PAYER, TARGET);
  private static final SwapFixedIborSpreadDefinition SWAP_SPREAD_EUR3M3M_DEFINITION = SwapFixedIborSpreadDefinition
      .from(START_DATE, SWAP_TENOR, EUR3MEURIBOR3M, NOTIONAL, FIXED_RATE, SPREAD, IS_PAYER, TARGET);
  private static final AnnuityCouponFixedDefinition ANNUITY_SPREAD_DEFINITION = AnnuityCouponFixedDefinition.from(EUR, START_DATE, SWAP_TENOR, EURIBOR3M.getTenor(), TARGET, EURIBOR3M.getDayCount(),
      EURIBOR3M.getBusinessDayConvention(), EURIBOR3M.isEndOfMonth(), NOTIONAL, SPREAD, !IS_PAYER);

  private static final SwapFixedCoupon<Coupon> SWAP_SPREAD_EUR1Y3M = SWAP_SPREAD_EUR1Y3M_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwapFixedCoupon<Coupon> SWAP_SPREAD_EUR3M3M = SWAP_SPREAD_EUR3M3M_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final AnnuityCouponFixed ANNUITY_SPREAD = ANNUITY_SPREAD_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final SwapFixedIborSpreadDiscountingMethod METHOD_SWAP_SPREAD = SwapFixedIborSpreadDiscountingMethod.getInstance();
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_RATE = 1.0E-8;

  @Test
  public void couponEquivalentSpreadModified() {
    final double pvbp3MMod = METHOD_SWAP_SPREAD.presentValueBasisPoint(SWAP_SPREAD_EUR3M3M, EURIBOR3M.getDayCount(), MULTICURVES);
    final double pvbp3M = METHOD_SWAP_SPREAD.presentValueBasisPoint(SWAP_SPREAD_EUR3M3M, MULTICURVES);
    assertEquals("SwapFixedIborSpreadDiscountingMethod: presentValueBasisPoint - modification", pvbp3M, pvbp3MMod, TOLERANCE_PV);
    final double cesm3M = METHOD_SWAP_SPREAD.couponEquivalentSpreadModified(SWAP_SPREAD_EUR3M3M, pvbp3M, MULTICURVES);
    final double cesmExpected = FIXED_RATE - SPREAD; // The convention is the same on both legs.
    assertEquals("SwapFixedIborSpreadDiscountingMethod: couponEquivalentSpreadModified", cesmExpected, cesm3M, TOLERANCE_RATE);
    final double pvbp1Y = METHOD_SWAP_SPREAD.presentValueBasisPoint(SWAP_SPREAD_EUR3M3M, MULTICURVES);
    final double cesm1Y = METHOD_SWAP_SPREAD.couponEquivalentSpreadModified(SWAP_SPREAD_EUR1Y3M, pvbp1Y, MULTICURVES);
    final double pvFixed = SWAP_SPREAD_EUR1Y3M.getFixedLeg().accept(PVDC, MULTICURVES).getAmount(EUR);
    final double pvAnnuitySpread = ANNUITY_SPREAD.accept(PVDC, MULTICURVES).getAmount(EUR);
    final CurrencyAmount pvIborNoSpread = METHOD_SWAP_SPREAD.presentValueIborNoSpreadPositiveNotional(SWAP_SPREAD_EUR1Y3M.getSecondLeg(), MULTICURVES);
    final double cesm1YExpected = (pvFixed + pvAnnuitySpread) / pvbp1Y;
    assertEquals("SwapFixedIborSpreadDiscountingMethod: couponEquivalentSpreadModified", cesm1YExpected, cesm1Y, TOLERANCE_RATE);
    final double pvIbor = SWAP_SPREAD_EUR1Y3M.getSecondLeg().accept(PVDC, MULTICURVES).getAmount(EUR);
    final double pvIborNoSpreadExpected = -(pvIbor - pvAnnuitySpread);
    assertEquals("SwapFixedIborSpreadDiscountingMethod: presentValueIborNoSpreadPositiveNotional", pvIborNoSpreadExpected, pvIborNoSpread.getAmount(), TOLERANCE_PV);
  }

  @Test
  public void presentValueIborNoSpreadPositiveNotional() {
    final CurrencyAmount pvs = METHOD_SWAP_SPREAD.presentValueSpreadPositiveNotional(SWAP_SPREAD_EUR1Y3M.getSecondLeg(), MULTICURVES);
    final MultipleCurrencyAmount pvAnnuitySpread = ANNUITY_SPREAD.accept(PVDC, MULTICURVES); // Should be negative: pay float
    assertEquals("SwapFixedIborSpreadDiscountingMethod: presentValueIborNoSpreadPositiveNotional", -pvAnnuitySpread.getAmount(EUR), pvs.getAmount(), TOLERANCE_PV);
  }

  @Test
  public void forwardSwapSpreadModified() {
    final double pvbp1Y = METHOD_SWAP_SPREAD.presentValueBasisPoint(SWAP_SPREAD_EUR3M3M, MULTICURVES);
    final double forwardComputed = METHOD_SWAP_SPREAD.forwardSwapSpreadModified(SWAP_SPREAD_EUR1Y3M, pvbp1Y, MULTICURVES);
    final double pvAnnuitySpread = ANNUITY_SPREAD.accept(PVDC, MULTICURVES).getAmount(EUR);
    final double pvAnnuityIbor = SWAP_SPREAD_EUR1Y3M.getSecondLeg().accept(PVDC, MULTICURVES).getAmount(EUR);
    final double forwardExpected = -(pvAnnuityIbor - pvAnnuitySpread) / pvbp1Y;
    assertEquals("SwapFixedIborSpreadDiscountingMethod: forwardSwapSpreadModified", forwardExpected, forwardComputed, TOLERANCE_RATE);
  }

  @SuppressWarnings("unused")
  @Test(enabled = false)
  /**
   * Test the performance of building swaps and computing their PV and delta.
   */
  public void performanceBuildPV() {
    final int nbSwap = 2500;
    long startTime, endTime;
    final double strikeMin = 0.01;
    final double strikeMax = 0.02;
    final Period tenor = Period.ofYears(10);
    final SwapFixedIborSpreadDefinition[] swapDefinition = new SwapFixedIborSpreadDefinition[nbSwap + 1];
    final double[] pv = new double[nbSwap + 1];

    startTime = System.currentTimeMillis();
    for (int loopswap = 0; loopswap <= nbSwap; loopswap++) {
      final double strike = strikeMin + loopswap * (strikeMax - strikeMin) / nbSwap;
      swapDefinition[loopswap] = SwapFixedIborSpreadDefinition.from(START_DATE, tenor, EUR1YEURIBOR3M, NOTIONAL, strike, SPREAD, IS_PAYER, TARGET);
      final SwapFixedCoupon<Coupon> swap = swapDefinition[loopswap].toDerivative(REFERENCE_DATE);
      pv[loopswap] = swap.accept(PVDC, MULTICURVES).getAmount(EUR);
      final MultipleCurrencyMulticurveSensitivity pvcs = swap.accept(PVCSDC, MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbSwap + " swap construction/pv/delta: " + (endTime - startTime) + " ms");
    // Performance note: build/pv/delta: 22-Dec-2012: On Mac Air 1.86 GHz Core 2 Duo: 900 ms for 1250 swaps.

    startTime = System.currentTimeMillis();
    for (int loopswap = 0; loopswap <= nbSwap; loopswap++) {
      final double strike = strikeMin + loopswap * (strikeMax - strikeMin) / nbSwap;
      swapDefinition[loopswap] = SwapFixedIborSpreadDefinition.from(START_DATE, tenor, EUR1YEURIBOR3M, NOTIONAL, strike, SPREAD, IS_PAYER, TARGET);
      final SwapFixedCoupon<Coupon> swap = swapDefinition[loopswap].toDerivative(REFERENCE_DATE);
      pv[loopswap] = swap.accept(PVDC, MULTICURVES).getAmount(EUR);
      final MultipleCurrencyMulticurveSensitivity pvcs = swap.accept(PVCSDC, MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbSwap + " swap construction/pv/delta: " + (endTime - startTime) + " ms");
    // Performance note: build/pv/delta: 22-Dec-2012: On Mac Air 1.86 GHz Core 2 Duo: xx ms for 1250 swaps.

  }

}
