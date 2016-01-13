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
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.model.option.parameters.BlackSmileCapParameters;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.provider.calculator.blackcap.PresentValueBlackSmileCapCalculator;
import com.opengamma.analytics.financial.provider.calculator.blackcap.PresentValueCurveSensitivityBlackSmileCapCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.BlackDataSets;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSmileCapProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSmileCapProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.blackcap.ParameterSensitivityBlackSmileCapDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test related to the pricing and sensitivity of the Ibor cap/floor with the Black model.
 */
@Test(groups = TestGroup.UNIT)
public class CapFloorIborBlackSmileMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex EURIBOR3M = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd()[0];
  private static final Currency EUR = EURIBOR3M.getCurrency();
  private static final Calendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();

  private static final InterpolatedDoublesSurface BLACK_SURF = BlackDataSets.createBlackSurfaceExpiryStrikeRate();
  private static final BlackSmileCapParameters BLACK_PARAM = new BlackSmileCapParameters(BLACK_SURF, EURIBOR3M);
  private static final BlackSmileCapProviderDiscount BLACK_MULTICURVES = new BlackSmileCapProviderDiscount(MULTICURVES, BLACK_PARAM);

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
  private static final CapFloorIborBlackSmileMethod METHOD_CAP_BLACK = CapFloorIborBlackSmileMethod.getInstance();
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();
  private static final PresentValueDiscountingCalculator PVC = PresentValueDiscountingCalculator.getInstance();

  private static final PresentValueBlackSmileCapCalculator PVBSCC = PresentValueBlackSmileCapCalculator.getInstance();
  private static final PresentValueCurveSensitivityBlackSmileCapCalculator PVCSBSCC = PresentValueCurveSensitivityBlackSmileCapCalculator.getInstance();

  private static final double SHIFT = 1.0E-7;
  private static final ParameterSensitivityParameterCalculator<BlackSmileCapProviderInterface> PS_SS_C = new ParameterSensitivityParameterCalculator<>(PVCSBSCC);
  private static final ParameterSensitivityBlackSmileCapDiscountInterpolatedFDCalculator PS_SS_FDC = new ParameterSensitivityBlackSmileCapDiscountInterpolatedFDCalculator(PVBSCC, SHIFT);
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
    final MultipleCurrencyAmount methodPrice = METHOD_CAP_BLACK.presentValue(CAP_LONG, BLACK_MULTICURVES);
    final double df = MULTICURVES.getDiscountFactor(EUR, CAP_LONG.getPaymentTime());
    final double forward = MULTICURVES.getSimplyCompoundForwardRate(EURIBOR3M, CAP_LONG.getFixingPeriodStartTime(), CAP_LONG.getFixingPeriodEndTime(), CAP_LONG.getFixingAccrualFactor());
    final double volatility = BLACK_SURF.getZValue(CAP_LONG.getFixingTime(), STRIKE);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, df, volatility);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(STRIKE, CAP_LONG.getFixingTime(), IS_CAP);
    final Function1D<BlackFunctionData, Double> funcBlack = BLACK_FUNCTION.getPriceFunction(option);
    final double expectedPrice = funcBlack.evaluate(dataBlack) * CAP_LONG.getNotional() * CAP_LONG.getPaymentYearFraction();
    assertEquals("Cap/floor: SABR pricing", expectedPrice, methodPrice.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Test several present value parities: long/short, cap/floor/forward
   */
  public void presentValueParity() {
    final double priceCapLong = METHOD_CAP_BLACK.presentValue(CAP_LONG, BLACK_MULTICURVES).getAmount(EUR);
    final double priceCapShort = METHOD_CAP_BLACK.presentValue(CAP_SHORT, BLACK_MULTICURVES).getAmount(EUR);
    assertEquals("Cap/floor - SABR pricing: long/short parity", -priceCapLong, priceCapShort, TOLERANCE_PV);
    final double priceFloorShort = METHOD_CAP_BLACK.presentValue(FLOOR_SHORT, BLACK_MULTICURVES).getAmount(EUR);
    final double priceIbor = COUPON_IBOR.accept(PVC, MULTICURVES).getAmount(EUR);
    final double priceStrike = COUPON_STRIKE.accept(PVC, MULTICURVES).getAmount(EUR);
    assertEquals("Cap/floor - SABR pricing: cap/floor parity", priceIbor - priceStrike, priceCapLong + priceFloorShort, TOLERANCE_PV);
  }

  @Test
  /**
   * Tests present value curve sensitivity when the valuation date is on trade date.
   */
  public void presentValueCurveSensitivityCap() {
    final MultipleCurrencyParameterSensitivity pvpsExact = PS_SS_C.calculateSensitivity(CAP_LONG, BLACK_MULTICURVES, BLACK_MULTICURVES.getMulticurveProvider().getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsFD = PS_SS_FDC.calculateSensitivity(CAP_LONG, BLACK_MULTICURVES);
    AssertSensitivityObjects.assertEquals("SwaptionPhysicalFixedIborSABRMethod: presentValueCurveSensitivity ", pvpsExact, pvpsFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests the present value SABR parameters sensitivity: Method vs Calculator.
   */
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final MultipleCurrencyMulticurveSensitivity pvcsMethod = METHOD_CAP_BLACK.presentValueCurveSensitivity(CAP_LONG, BLACK_MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvcsCalculator = CAP_LONG.accept(PVCSBSCC, BLACK_MULTICURVES);
    assertEquals("Cap/floor SABR: Present value SABR sensitivity: method vs calculator", pvcsMethod, pvcsCalculator);
  }

}
