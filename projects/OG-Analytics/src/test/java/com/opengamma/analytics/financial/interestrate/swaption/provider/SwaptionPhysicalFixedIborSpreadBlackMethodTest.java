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
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborSpreadDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.provider.SwapFixedIborSpreadDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.option.parameters.BlackFlatSwaptionParameters;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.provider.description.BlackDataSets;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSwaptionFlatProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the pricing and sensitivities of Swaption physical delivary on swaps with spread. The option is priced with a Black implied volatility.
 */
@Test(groups = TestGroup.UNIT)
public class SwaptionPhysicalFixedIborSpreadBlackMethodTest {
  // Data
  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex EURIBOR3M = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd()[1];
  private static final Calendar TARGET = MulticurveProviderDiscountDataSets.getEURCalendar();
  private static final Currency EUR = EURIBOR3M.getCurrency();

  private static final BlackFlatSwaptionParameters BLACK = BlackDataSets.createBlackSwaptionEUR3();
  private static final BlackSwaptionFlatProvider BLACK_MULTICURVES = new BlackSwaptionFlatProvider(MULTICURVES, BLACK);
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR3M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("EUR1YEURIBOR3M", TARGET);

  private static final GeneratorSwapFixedIbor EUR3MEURIBOR3M = new GeneratorSwapFixedIbor("EUR3MEURIBOR3M", EURIBOR3M.getTenor(), EURIBOR3M.getDayCount(), EURIBOR3M, TARGET);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 8, 31);
  private static final Period START_TENOR = Period.ofMonths(6);
  private static final Period SWAP_TENOR = Period.ofYears(5);
  private static final ZonedDateTime START_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, START_TENOR, EURIBOR3M, TARGET);
  private static final double NOTIONAL = 123000000;
  private static final double SPREAD = 0.0010;
  private static final double FIXED_RATE = 0.0250;
  private static final boolean IS_PAYER = false;
  private static final ZonedDateTime EXPIRY_DATE = ScheduleCalculator.getAdjustedDate(START_DATE, -EURIBOR3M.getSpotLag(), TARGET);
  private static final boolean IS_LONG = false;

  private static final SwapFixedIborSpreadDefinition SWAP_SPREAD_EUR1Y3M_DEFINITION = SwapFixedIborSpreadDefinition
      .from(START_DATE, SWAP_TENOR, EUR1YEURIBOR3M, NOTIONAL, FIXED_RATE, SPREAD, IS_PAYER, TARGET);
  private static final SwapFixedIborSpreadDefinition SWAP_SPREAD_EUR3M3M_DEFINITION = SwapFixedIborSpreadDefinition
      .from(START_DATE, SWAP_TENOR, EUR3MEURIBOR3M, NOTIONAL, FIXED_RATE, SPREAD, IS_PAYER, TARGET);
  private static final SwapFixedIborDefinition SWAP_NOSPREAD_EUR3M3M_DEFINITION = SwapFixedIborDefinition.from(START_DATE, SWAP_TENOR, EUR3MEURIBOR3M, NOTIONAL, FIXED_RATE - SPREAD, IS_PAYER);
  private static final SwaptionPhysicalFixedIborSpreadDefinition SWAPTION_SPREAD_EUR1Y3M_DEFINITION = SwaptionPhysicalFixedIborSpreadDefinition.from(EXPIRY_DATE, SWAP_SPREAD_EUR1Y3M_DEFINITION,
      IS_PAYER, IS_LONG);
  private static final SwaptionPhysicalFixedIborSpreadDefinition SWAPTION_SPREAD_EUR3M3M_DEFINITION = SwaptionPhysicalFixedIborSpreadDefinition.from(EXPIRY_DATE, SWAP_SPREAD_EUR3M3M_DEFINITION,
      IS_PAYER, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_NOSPREAD_EUR3M3M_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_NOSPREAD_EUR3M3M_DEFINITION, IS_PAYER, IS_LONG);

  private static final SwapFixedCoupon<Coupon> SWAP_SPREAD_EUR1Y3M = SWAP_SPREAD_EUR1Y3M_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionPhysicalFixedIbor SWAPTION_SPREAD_EUR1Y3M = SWAPTION_SPREAD_EUR1Y3M_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionPhysicalFixedIbor SWAPTION_SPREAD_EUR3M3M = SWAPTION_SPREAD_EUR3M3M_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionPhysicalFixedIbor SWAPTION_NOSPREAD_EUR3M3M = SWAPTION_NOSPREAD_EUR3M3M_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final SwapFixedIborSpreadDiscountingMethod METHOD_SWAP_SPREAD = SwapFixedIborSpreadDiscountingMethod.getInstance();
  private static final SwaptionPhysicalFixedIborSpreadBlackMethod METHOD_SWAPTION_SPREAD = SwaptionPhysicalFixedIborSpreadBlackMethod.getInstance();
  private static final SwaptionPhysicalFixedIborBlackMethod METHOD_SWAPTION = SwaptionPhysicalFixedIborBlackMethod.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;

  @Test
  public void presentValue() {
    final MultipleCurrencyAmount pvComputed = METHOD_SWAPTION_SPREAD.presentValue(SWAPTION_SPREAD_EUR1Y3M, BLACK_MULTICURVES);
    final double pvbp = METHOD_SWAP_SPREAD.presentValueBasisPoint(SWAP_SPREAD_EUR1Y3M, EUR1YEURIBOR3M.getFixedLegDayCount(), MULTICURVES);
    final double forward = METHOD_SWAP_SPREAD.forwardSwapSpreadModified(SWAP_SPREAD_EUR1Y3M, pvbp, MULTICURVES);
    final double strike = METHOD_SWAP_SPREAD.couponEquivalentSpreadModified(SWAP_SPREAD_EUR1Y3M, pvbp, MULTICURVES);
    final double volatility = BLACK.getVolatility(SWAPTION_SPREAD_EUR1Y3M.getTimeToExpiry(), SWAPTION_SPREAD_EUR1Y3M.getMaturityTime());
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, SWAPTION_SPREAD_EUR1Y3M.getTimeToExpiry(), SWAPTION_SPREAD_EUR1Y3M.isCall());
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, pvbp, volatility);
    final Function1D<BlackFunctionData, Double> func = blackFunction.getPriceFunction(option);
    final double pvExpected = func.evaluate(dataBlack) * (IS_LONG ? 1.0 : -1.0);
    assertEquals("SwaptionPhysicalFixedIborSpreadBlackMethod: presentValue", pvExpected, pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Compare the present value of a swaption with spread to a swaption without spread and an adjusted strike.
   */
  public void presentValueNoSpread() {
    final MultipleCurrencyAmount pvComputed = METHOD_SWAPTION_SPREAD.presentValue(SWAPTION_SPREAD_EUR3M3M, BLACK_MULTICURVES);
    final MultipleCurrencyAmount pvExpected = METHOD_SWAPTION.presentValue(SWAPTION_NOSPREAD_EUR3M3M, BLACK_MULTICURVES);
    assertEquals("SwaptionPhysicalFixedIborSpreadBlackMethod: presentValue", pvExpected.getAmount(EUR), pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

}
