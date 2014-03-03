/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import cern.jet.random.engine.MersenneTwister;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.model.interestrate.TestsDataSetLiborMarketModelDisplacedDiffusion;
import com.opengamma.analytics.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionParameters;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.montecarlo.provider.LiborMarketModelMonteCarloMethod;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.LiborMarketModelDisplacedDiffusionProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.random.NormalRandomNumberGenerator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the pricing of physical delivery swaption in LMM displaced diffusion.
 */
@Test(groups = TestGroup.UNIT)
public class CapFloorIborLMMDDMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex EURIBOR3M = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd()[0];
  private static final Currency EUR = EURIBOR3M.getCurrency();
  private static final Calendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 7, 7);

  // Swaption 5Yx5Y

  private static final int SWAP_TENOR_YEAR = 4;
  private static final Period SWAP_TENOR = Period.ofYears(SWAP_TENOR_YEAR);

  private static final GeneratorSwapFixedIbor EUR3MEURIBOR3M = new GeneratorSwapFixedIbor("Ibor", EURIBOR3M.getTenor(), EURIBOR3M.getDayCount(), EURIBOR3M, CALENDAR);
  private static final IndexSwap SWAP_INDEX = new IndexSwap(EUR3MEURIBOR3M, SWAP_TENOR);
  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, EURIBOR3M.getSpotLag(), CALENDAR);
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(SPOT_DATE, EURIBOR3M.getSpotLag(), CALENDAR);
  private static final double NOTIONAL = 100000000; //100m
  private static final double STRIKE = 0.0375;
  private static final boolean FIXED_IS_PAYER = true;
  private static final SwapFixedIborDefinition SWAP_PAYER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, SWAP_INDEX, NOTIONAL, STRIKE, FIXED_IS_PAYER, CALENDAR);
  //to derivatives

  private static final SwapFixedCoupon<Coupon> SWAP_PAYER = SWAP_PAYER_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final int NB_CPN_IBOR = SWAP_PAYER.getSecondLeg().getNumberOfPayments();
  private static final boolean IS_CAP = true;

  private static final CouponIbor COUPON_IBOR_LAST = (CouponIbor) SWAP_PAYER.getSecondLeg().getNthPayment(NB_CPN_IBOR - 1);
  private static final CouponFixed COUPON_FIXED_LAST = SWAP_PAYER.getFirstLeg().getNthPayment(NB_CPN_IBOR - 1);
  private static final CapFloorIbor CAP_LAST = new CapFloorIbor(EUR, COUPON_IBOR_LAST.getPaymentTime(), COUPON_IBOR_LAST.getPaymentYearFraction(), NOTIONAL,
      COUPON_IBOR_LAST.getFixingTime(), EURIBOR3M, COUPON_IBOR_LAST.getFixingPeriodStartTime(), COUPON_IBOR_LAST.getFixingPeriodEndTime(), COUPON_IBOR_LAST.getFixingAccrualFactor(),
      STRIKE, IS_CAP);
  private static final CapFloorIbor FLOOR_LAST = new CapFloorIbor(EUR, COUPON_IBOR_LAST.getPaymentTime(), COUPON_IBOR_LAST.getPaymentYearFraction(), NOTIONAL,
      COUPON_IBOR_LAST.getFixingTime(), EURIBOR3M, COUPON_IBOR_LAST.getFixingPeriodStartTime(), COUPON_IBOR_LAST.getFixingPeriodEndTime(), COUPON_IBOR_LAST.getFixingAccrualFactor(),
      STRIKE, !IS_CAP);
  private static final CapFloorIbor CAP_LAST_SHORT = new CapFloorIbor(EUR, COUPON_IBOR_LAST.getPaymentTime(), COUPON_IBOR_LAST.getPaymentYearFraction(), -NOTIONAL,
      COUPON_IBOR_LAST.getFixingTime(), EURIBOR3M, COUPON_IBOR_LAST.getFixingPeriodStartTime(), COUPON_IBOR_LAST.getFixingPeriodEndTime(), COUPON_IBOR_LAST.getFixingAccrualFactor(),
      STRIKE, IS_CAP);

  private static final CouponIbor COUPON_IBOR_6 = (CouponIbor) SWAP_PAYER.getSecondLeg().getNthPayment(6);
  private static final CapFloorIbor CAP_6 = new CapFloorIbor(EUR, COUPON_IBOR_6.getPaymentTime(), COUPON_IBOR_6.getPaymentYearFraction(), NOTIONAL,
      COUPON_IBOR_6.getFixingTime(), EURIBOR3M, COUPON_IBOR_6.getFixingPeriodStartTime(), COUPON_IBOR_6.getFixingPeriodEndTime(), COUPON_IBOR_6.getFixingAccrualFactor(),
      STRIKE, IS_CAP);
  // Parameters and methods
  private static final int NB_PATH = 12500;

  private static final LiborMarketModelDisplacedDiffusionParameters PARAMETERS_LMM = TestsDataSetLiborMarketModelDisplacedDiffusion.createLMMParameters(REFERENCE_DATE,
      SWAP_PAYER_DEFINITION.getIborLeg());
  private static final LiborMarketModelDisplacedDiffusionProviderDiscount LMM_MULTICURVES = new LiborMarketModelDisplacedDiffusionProviderDiscount(MULTICURVES, PARAMETERS_LMM, EUR);

  private static final CapFloorIborLMMDDMethod METHOD_LMM_CAP = CapFloorIborLMMDDMethod.getInstance();
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();

  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  private static final double TOLERANCE_PV = 1.0E-2;

  //  private static final double TOLERANCE_PV_DELTA = 1.0E+0; // 0.01 currency unit for 1bp

  @Test
  /**
   * Test the present value explicit formula in the multi-curves framework.
   */
  public void presentValueExplicit() {
    final MultipleCurrencyAmount pvLastExplicit = METHOD_LMM_CAP.presentValue(CAP_LAST, LMM_MULTICURVES);
    final int index = PARAMETERS_LMM.getTimeIndex(CAP_LAST.getFixingPeriodStartTime());
    double volatility = 0;
    for (int loopfact = 0; loopfact < PARAMETERS_LMM.getNbFactor(); loopfact++) {
      volatility += PARAMETERS_LMM.getVolatility()[index][loopfact] * PARAMETERS_LMM.getVolatility()[index][loopfact];
    }
    volatility = Math.sqrt(volatility);
    final double timeDependentFactor = Math.sqrt((Math.exp(2 * PARAMETERS_LMM.getMeanReversion() * CAP_LAST.getFixingTime()) - 1.0) / (2.0 * PARAMETERS_LMM.getMeanReversion()));
    volatility *= timeDependentFactor;
    final double displacement = PARAMETERS_LMM.getDisplacement()[index];
    final double forward = MULTICURVES.getSimplyCompoundForwardRate(CAP_LAST.getIndex(), CAP_LAST.getFixingPeriodStartTime(), CAP_LAST.getFixingPeriodEndTime(), CAP_LAST.getFixingAccrualFactor());
    final double beta = (1.0 + CAP_LAST.getFixingAccrualFactor() * forward) * MULTICURVES.getDiscountFactor(EUR, CAP_LAST.getFixingPeriodEndTime())
        / MULTICURVES.getDiscountFactor(EUR, CAP_LAST.getFixingPeriodStartTime());
    final double strikeAdjusted = (STRIKE - (beta - 1) / CAP_LAST.getFixingAccrualFactor()) / beta;
    // Strike adjusted from Forward on forward curve and Forward on discount curve.
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strikeAdjusted + displacement, 1.0, CAP_LAST.isCap());
    final double forwardDsc = (MULTICURVES.getDiscountFactor(EUR, CAP_LAST.getFixingPeriodStartTime()) / MULTICURVES.getDiscountFactor(EUR, CAP_LAST.getFixingPeriodEndTime()) - 1.0)
        / CAP_LAST.getFixingAccrualFactor();
    final double df = MULTICURVES.getDiscountFactor(EUR, CAP_LAST.getPaymentTime());
    final BlackFunctionData dataBlack = new BlackFunctionData(forwardDsc + displacement, df, volatility);
    final Function1D<BlackFunctionData, Double> func = BLACK_FUNCTION.getPriceFunction(option);
    final double pvLastExpected = beta * func.evaluate(dataBlack) * NOTIONAL * CAP_LAST.getPaymentYearFraction();
    assertEquals("Cap/floor: LMM pricing by explicit formula - Multi-curves", pvLastExpected, pvLastExplicit.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Test the present value explicit formula in the multi-curves framework.
   */
  public void presentValueLongShort() {
    final MultipleCurrencyAmount pvLastLong = METHOD_LMM_CAP.presentValue(CAP_LAST, LMM_MULTICURVES);
    final MultipleCurrencyAmount pvLastShort = METHOD_LMM_CAP.presentValue(CAP_LAST_SHORT, LMM_MULTICURVES);
    assertEquals("Cap/floor: LMM pricing by explicit formula", pvLastLong.getAmount(EUR), -pvLastShort.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Test the present value explicit formula in the multi-curves framework.
   */
  public void presentValuePutCallParity() {
    final MultipleCurrencyAmount pvCapShort = METHOD_LMM_CAP.presentValue(CAP_LAST_SHORT, LMM_MULTICURVES);
    final MultipleCurrencyAmount pvFloorLong = METHOD_LMM_CAP.presentValue(FLOOR_LAST, LMM_MULTICURVES);
    final MultipleCurrencyAmount pvFixed = COUPON_FIXED_LAST.accept(PVDC, MULTICURVES);
    final MultipleCurrencyAmount pvIbor = COUPON_IBOR_LAST.accept(PVDC, MULTICURVES);
    assertEquals("Cap/floor: LMM pricing by explicit formula", -pvIbor.getAmount(EUR) - pvFixed.getAmount(EUR), pvCapShort.getAmount(EUR) + pvFloorLong.getAmount(EUR), TOLERANCE_PV);
  }

  @Test(enabled = true)
  /**
   * Test the present value.
   */
  public void presentValueMCMultiCurves() {
    LiborMarketModelMonteCarloMethod methodLmmMc;
    methodLmmMc = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);
    final MultipleCurrencyAmount pvLastMC = methodLmmMc.presentValue(CAP_LAST, EUR, LMM_MULTICURVES);
    final double pvLastPreviousRun = 45829.535; // 12500 paths - 1Y jump
    assertEquals("Cap/floor: LMM pricing by Monte Carlo", pvLastPreviousRun, pvLastMC.getAmount(EUR), TOLERANCE_PV);
    final MultipleCurrencyAmount pvLastExplicit = METHOD_LMM_CAP.presentValue(CAP_LAST, LMM_MULTICURVES);
    assertEquals("Cap/floor: LMM pricing by Monte Carlo", pvLastExplicit.getAmount(EUR), pvLastMC.getAmount(EUR), 2.5E+2);
    methodLmmMc = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);
    final MultipleCurrencyAmount pv6MC = methodLmmMc.presentValue(CAP_6, EUR, LMM_MULTICURVES);
    final double pv6PreviousRun = 12081.062; // 12500 paths - 1Y jump
    assertEquals("Cap/floor: LMM pricing by Monte Carlo", pv6PreviousRun, pv6MC.getAmount(EUR), TOLERANCE_PV);
    final MultipleCurrencyAmount pv6Explicit = METHOD_LMM_CAP.presentValue(CAP_6, LMM_MULTICURVES);
    assertEquals("Cap/floor: LMM pricing by Monte Carlo", pv6Explicit.getAmount(EUR), pv6MC.getAmount(EUR), 1.0E+2);
  }

  @Test
  /**
   * Tests long/short parity.
   */
  public void longShortParity() {
    final MultipleCurrencyAmount pvLongExplicit = METHOD_LMM_CAP.presentValue(CAP_LAST, LMM_MULTICURVES);
    final MultipleCurrencyAmount pvShortExplicit = METHOD_LMM_CAP.presentValue(CAP_LAST_SHORT, LMM_MULTICURVES);
    assertEquals("Cap/floor - LMM - present value - long/short parity", pvLongExplicit.getAmount(EUR), -pvShortExplicit.getAmount(EUR), TOLERANCE_PV);
    LiborMarketModelMonteCarloMethod methodLmmMc;
    methodLmmMc = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);
    final MultipleCurrencyAmount pvLongMC = methodLmmMc.presentValue(CAP_LAST, EUR, LMM_MULTICURVES);
    methodLmmMc = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);
    final MultipleCurrencyAmount pvShortMC = methodLmmMc.presentValue(CAP_LAST_SHORT, EUR, LMM_MULTICURVES);
    assertEquals("Cap/floor - LMM - present value MC- long/short parity", pvLongMC.getAmount(EUR), -pvShortMC.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests payer/receiver/fixed parity.
   */
  public void capFloorParity() {
    final MultipleCurrencyAmount pvCapExplicit = METHOD_LMM_CAP.presentValue(CAP_LAST, LMM_MULTICURVES);
    final MultipleCurrencyAmount pvFloorExplicit = METHOD_LMM_CAP.presentValue(FLOOR_LAST, LMM_MULTICURVES);
    final MultipleCurrencyAmount pvFixedExplicit = SWAP_PAYER.getFirstLeg().getNthPayment(NB_CPN_IBOR - 1).accept(PVDC, MULTICURVES);
    final MultipleCurrencyAmount pvIborExplicit = SWAP_PAYER.getSecondLeg().getNthPayment(NB_CPN_IBOR - 1).accept(PVDC, MULTICURVES);
    assertEquals("Cap/floor - LMM - present value Explcit- cap/floor/strike/Ibor parity", pvCapExplicit.getAmount(EUR) - pvFloorExplicit.getAmount(EUR) - pvFixedExplicit.getAmount(EUR),
        pvIborExplicit.getAmount(EUR), TOLERANCE_PV);
    LiborMarketModelMonteCarloMethod methodLmmMc;
    methodLmmMc = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);
    final MultipleCurrencyAmount pvCapMC = methodLmmMc.presentValue(CAP_LAST, EUR, LMM_MULTICURVES);
    methodLmmMc = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);
    final MultipleCurrencyAmount pvFloorMC = methodLmmMc.presentValue(FLOOR_LAST, EUR, LMM_MULTICURVES);
    assertEquals("Cap/floor - LMM - present value - cap/floor/strike/Ibor parity", pvCapMC.getAmount(EUR) - pvFloorMC.getAmount(EUR) - pvFixedExplicit.getAmount(EUR), pvIborExplicit.getAmount(EUR),
        1.0E+3);
  }

  @Test(enabled = false)
  /**
   * Tests of performance. "enabled = false" for the standard testing.
   */
  public void performance() {
    long startTime, endTime;
    final int nbTest = 10;

    LiborMarketModelMonteCarloMethod methodLmmMc;
    methodLmmMc = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);
    final double[] pvMC = new double[nbTest];

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvMC[looptest] = methodLmmMc.presentValue(CAP_LAST, EUR, LMM_MULTICURVES).getAmount(EUR);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " cap/floor LMM Monte Carlo method (" + NB_PATH + " paths): " + (endTime - startTime) + " ms");
    // Performance note: LMM Monte Carlo: 15-Sep-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 995 ms for 10 cap (12,500 paths).

  }

}
