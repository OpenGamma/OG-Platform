/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.BlackSwaptionSensitivityNodeCalculator;
import com.opengamma.analytics.financial.interestrate.sensitivity.PresentValueSwaptionSurfaceSensitivity;
import com.opengamma.analytics.financial.interestrate.swap.provider.SwapFixedCouponDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.model.option.parameters.BlackFlatSwaptionParameters;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.provider.calculator.blackswaption.PresentValueBlackSensitivityBlackSwaptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.blackswaption.PresentValueBlackSwaptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.blackswaption.PresentValueCurveSensitivityBlackSwaptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.BlackDataSets;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSwaptionFlatProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSwaptionFlatProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSwaptionFlatProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.blackswaption.ParameterSensitivityBlackSwaptionDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SwaptionCashFixedIborBlackMethodTest {
  // Data
  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final Calendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();

  private static final BlackFlatSwaptionParameters BLACK = BlackDataSets.createBlackSwaptionEUR6();
  private static final BlackSwaptionFlatProviderDiscount BLACK_MULTICURVES = new BlackSwaptionFlatProviderDiscount(MULTICURVES, BLACK);
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor GENERATOR_EUR1YEURIBOR6M = GENERATOR_SWAP_MASTER.getGenerator("EUR1YEURIBOR6M", CALENDAR);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 1, 10);
  // Swaption
  private static final Period EXPIRY_TENOR = Period.ofMonths(26); // To be between nodes.
  private static final ZonedDateTime EXPIRY_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, EXPIRY_TENOR, GENERATOR_EUR1YEURIBOR6M.getBusinessDayConvention(), CALENDAR,
      GENERATOR_EUR1YEURIBOR6M.isEndOfMonth());
  private static final ZonedDateTime SETTLE_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, GENERATOR_EUR1YEURIBOR6M.getSpotLag(), CALENDAR);
  private static final int SWAP_TENOR_YEAR = 5;
  private static final Period SWAP_TENOR = Period.ofYears(SWAP_TENOR_YEAR);
  private static final double NOTIONAL = 123456789.0;
  private static final double RATE = 0.02;
  private static final SwapFixedIborDefinition SWAP_DEFINITION_REC = SwapFixedIborDefinition.from(SETTLE_DATE, SWAP_TENOR, GENERATOR_EUR1YEURIBOR6M, NOTIONAL, RATE, false);
  private static final SwaptionCashFixedIborDefinition SWAPTION_DEFINITION_LONG_REC = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_REC, false, true);
  private static final SwaptionCashFixedIbor SWAPTION_LONG_REC = SWAPTION_DEFINITION_LONG_REC.toDerivative(REFERENCE_DATE);

  private static final PresentValueBlackSwaptionCalculator PVBSC = PresentValueBlackSwaptionCalculator.getInstance();
  private static final PresentValueCurveSensitivityBlackSwaptionCalculator PVCSBSC = PresentValueCurveSensitivityBlackSwaptionCalculator.getInstance();
  private static final PresentValueBlackSensitivityBlackSwaptionCalculator PVBSSBSC = PresentValueBlackSensitivityBlackSwaptionCalculator.getInstance();

  private static final double SHIFT = 1.0E-6;
  private static final ParameterSensitivityParameterCalculator<BlackSwaptionFlatProviderInterface> PS_BS_C = new ParameterSensitivityParameterCalculator<>(PVCSBSC);
  private static final ParameterSensitivityBlackSwaptionDiscountInterpolatedFDCalculator PS_BS_FDC = new ParameterSensitivityBlackSwaptionDiscountInterpolatedFDCalculator(PVBSC, SHIFT);

  // Method - calculator
  private static final double TOLERANCE_PRICE = 1.0E-2;
  private static final double TOLERANCE_DELTA = 1.0E+2;
  //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move.
  private static final SwaptionCashFixedIborBlackMethod METHOD_BLACK = SwaptionCashFixedIborBlackMethod.getInstance();
  private static final ParRateDiscountingCalculator PRC = ParRateDiscountingCalculator.getInstance();
  private static final BlackSwaptionSensitivityNodeCalculator BSSNC = new BlackSwaptionSensitivityNodeCalculator();
  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();

  @Test
  public void presentValue() {
    final MultipleCurrencyAmount pvMethod = METHOD_BLACK.presentValue(SWAPTION_LONG_REC, BLACK_MULTICURVES);
    final double forward = SWAPTION_LONG_REC.getUnderlyingSwap().accept(PRC, MULTICURVES);
    final double pvbp = METHOD_SWAP.getAnnuityCash(SWAPTION_LONG_REC.getUnderlyingSwap(), forward);
    final double discountFactorSettle = MULTICURVES.getDiscountFactor(SWAPTION_LONG_REC.getCurrency(), SWAPTION_LONG_REC.getSettlementTime());
    final double volatility = BLACK.getVolatility(SWAPTION_LONG_REC.getTimeToExpiry(), SWAPTION_LONG_REC.getMaturityTime());
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, pvbp * discountFactorSettle, volatility);
    final Function1D<BlackFunctionData, Double> func = blackFunction.getPriceFunction(SWAPTION_LONG_REC);
    final double pvExpected = func.evaluate(dataBlack);
    assertEquals("Swaption Black method: present value", 1, pvMethod.size());
    final CurrencyAmount ca = pvMethod.getCurrencyAmounts()[0];
    assertEquals("Swaption Black method: present value", SWAPTION_LONG_REC.getCurrency(), ca.getCurrency());
    assertEquals("Swaption Black method: present value", pvExpected, ca.getAmount(), TOLERANCE_PRICE);
  }

  @Test
  /**
   * Compare the method figures to the Calculator figures.
   */
  public void presentValueMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD_BLACK.presentValue(SWAPTION_LONG_REC, BLACK_MULTICURVES);
    final MultipleCurrencyAmount pvCalculator = SWAPTION_LONG_REC.accept(PVBSC, BLACK_MULTICURVES);
    assertEquals("Swaption Black method: present value", pvCalculator.getAmount(Currency.EUR), pvMethod.getAmount(Currency.EUR), TOLERANCE_PRICE);
  }

  @Test(enabled = false) //TODO
  /**
   * Tests the curve sensitivity for the explicit formula.
   */
  public void presentValueCurveSensitivity() {
    final MultipleCurrencyParameterSensitivity pvpsExact = PS_BS_C.calculateSensitivity(SWAPTION_LONG_REC, BLACK_MULTICURVES, BLACK_MULTICURVES.getMulticurveProvider().getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsFD = PS_BS_FDC.calculateSensitivity(SWAPTION_LONG_REC, BLACK_MULTICURVES);
    AssertSensitivityObjects.assertEquals("Swaption Black method: presentValueCurveSensitivity ", pvpsExact, pvpsFD, TOLERANCE_DELTA);
  }

  @Test
  /**
   * Compare the method figures to the Calculator figures.
   */
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final MultipleCurrencyMulticurveSensitivity pvcsMethod = METHOD_BLACK.presentValueCurveSensitivity(SWAPTION_LONG_REC, BLACK_MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvcsCalculator = SWAPTION_LONG_REC.accept(PVCSBSC, BLACK_MULTICURVES);
    AssertSensitivityObjects.assertEquals("Swaption Black method: present value", pvcsMethod, pvcsCalculator, TOLERANCE_DELTA);
  }

  @Test
  /**
   * Tests the Black volatility sensitivity (vega).
   */
  public void presentValueBlackSensitivity() {
    final double shift = 1.0E-6;
    final PresentValueSwaptionSurfaceSensitivity pvbvs = METHOD_BLACK.presentValueBlackSensitivity(SWAPTION_LONG_REC, BLACK_MULTICURVES);
    final BlackFlatSwaptionParameters blackP = BlackDataSets.createBlackSwaptionEUR6Shift(shift);
    final BlackSwaptionFlatProvider curvesBlackP = new BlackSwaptionFlatProvider(MULTICURVES, blackP);
    final MultipleCurrencyAmount pvP = METHOD_BLACK.presentValue(SWAPTION_LONG_REC, curvesBlackP);
    final BlackFlatSwaptionParameters blackM = BlackDataSets.createBlackSwaptionEUR6Shift(-shift);
    final BlackSwaptionFlatProvider curvesBlackM = new BlackSwaptionFlatProvider(MULTICURVES, blackM);
    final MultipleCurrencyAmount pvM = METHOD_BLACK.presentValue(SWAPTION_LONG_REC, curvesBlackM);
    final DoublesPair point = DoublesPair.of(SWAPTION_LONG_REC.getTimeToExpiry(), SWAPTION_LONG_REC.getMaturityTime());
    final Double volatilitySensitivity = pvbvs.getSensitivity().getMap().get(point);
    assertEquals("Swaption Black method: present value volatility sensitivity", (pvP.getCurrencyAmounts()[0].getAmount() - pvM.getCurrencyAmounts()[0].getAmount()) / (2 * shift),
        volatilitySensitivity, TOLERANCE_DELTA);
  }

  @Test
  /**
   * Tests the Black volatility sensitivity (vega).
   */
  public void presentValueBlackSensitivityMethodVsCalculator() {
    final PresentValueSwaptionSurfaceSensitivity pvbsMethod = METHOD_BLACK.presentValueBlackSensitivity(SWAPTION_LONG_REC, BLACK_MULTICURVES);
    final PresentValueSwaptionSurfaceSensitivity pvbsCalculator = SWAPTION_LONG_REC.accept(PVBSSBSC, BLACK_MULTICURVES);
    assertEquals("Swaption Black method: vega", pvbsMethod, pvbsCalculator);
  }

  @Test
  /**
   * Tests the Black volatility sensitivity (vega).
   */
  public void presentValueBlackNodeSensitivity() {
    final double shift = 1.0E-6;
    final PresentValueSwaptionSurfaceSensitivity pvbvs = METHOD_BLACK.presentValueBlackSensitivity(SWAPTION_LONG_REC, BLACK_MULTICURVES);
    final PresentValueSwaptionSurfaceSensitivity pvbns = BSSNC.calculateNodeSensitivities(pvbvs, BLACK);
    final double[] x = ((InterpolatedDoublesSurface) BLACK.getVolatilitySurface()).getXDataAsPrimitive();
    final double[] y = ((InterpolatedDoublesSurface) BLACK.getVolatilitySurface()).getYDataAsPrimitive();
    for (int loopindex = 0; loopindex < x.length; loopindex++) {
      final BlackFlatSwaptionParameters blackP = BlackDataSets.createBlackSwaptionEUR6Shift(loopindex, shift);
      final BlackSwaptionFlatProvider curvesBlackP = new BlackSwaptionFlatProvider(MULTICURVES, blackP);
      final MultipleCurrencyAmount pvP = METHOD_BLACK.presentValue(SWAPTION_LONG_REC, curvesBlackP);
      final BlackFlatSwaptionParameters blackM = BlackDataSets.createBlackSwaptionEUR6Shift(loopindex, -shift);
      final BlackSwaptionFlatProvider curvesBlackM = new BlackSwaptionFlatProvider(MULTICURVES, blackM);
      final MultipleCurrencyAmount pvM = METHOD_BLACK.presentValue(SWAPTION_LONG_REC, curvesBlackM);
      assertEquals("Swaption Black method: present value volatility sensitivity", (pvP.getCurrencyAmounts()[0].getAmount() - pvM.getCurrencyAmounts()[0].getAmount()) / (2 * shift), pvbns
          .getSensitivity().getMap().get(DoublesPair.of(x[loopindex], y[loopindex])), TOLERANCE_DELTA);
    }
  }

}
