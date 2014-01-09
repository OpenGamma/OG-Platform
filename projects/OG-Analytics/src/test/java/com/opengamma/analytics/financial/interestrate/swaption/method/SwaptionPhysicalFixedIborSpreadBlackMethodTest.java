/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.method;

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
import com.opengamma.analytics.financial.interestrate.TestsDataSetsBlack;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.method.SwapFixedIborSpreadDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.analytics.financial.model.option.parameters.BlackFlatSwaptionParameters;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the pricing and sensitivities of Swaption physical delivary on swaps with spread. The option is priced with a Black implied volatility.
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class SwaptionPhysicalFixedIborSpreadBlackMethodTest {

  private static final Calendar TARGET = new MondayToFridayCalendar("TARGET");
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR3M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("EUR1YEURIBOR3M", TARGET);
  private static final IborIndex EURIBOR3M = EUR1YEURIBOR3M.getIborIndex();
  //  private static final Currency EUR = EURIBOR3M.getCurrency();
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

  private static final YieldCurveBundle CURVES = TestsDataSetsBlack.createCurvesEUR();
  private static final String[] CURVE_NAMES = TestsDataSetsBlack.curvesEURNames();
  private static final BlackFlatSwaptionParameters BLACK = TestsDataSetsBlack.createBlackSwaptionEUR3();
  private static final YieldCurveWithBlackSwaptionBundle BUNDLE = new YieldCurveWithBlackSwaptionBundle(BLACK, CURVES);

  private static final SwapFixedCoupon<Coupon> SWAP_SPREAD_EUR1Y3M = SWAP_SPREAD_EUR1Y3M_DEFINITION.toDerivative(REFERENCE_DATE, CURVE_NAMES);
  private static final SwaptionPhysicalFixedIbor SWAPTION_SPREAD_EUR1Y3M = SWAPTION_SPREAD_EUR1Y3M_DEFINITION.toDerivative(REFERENCE_DATE, CURVE_NAMES);
  private static final SwaptionPhysicalFixedIbor SWAPTION_SPREAD_EUR3M3M = SWAPTION_SPREAD_EUR3M3M_DEFINITION.toDerivative(REFERENCE_DATE, CURVE_NAMES);
  private static final SwaptionPhysicalFixedIbor SWAPTION_NOSPREAD_EUR3M3M = SWAPTION_NOSPREAD_EUR3M3M_DEFINITION.toDerivative(REFERENCE_DATE, CURVE_NAMES);

  private static final SwapFixedIborSpreadDiscountingMethod METHOD_SWAP_SPREAD = SwapFixedIborSpreadDiscountingMethod.getInstance();
  private static final SwaptionPhysicalFixedIborSpreadBlackMethod METHOD_SWAPTION_SPREAD = SwaptionPhysicalFixedIborSpreadBlackMethod.getInstance();
  private static final SwaptionPhysicalFixedIborBlackMethod METHOD_SWAPTION = SwaptionPhysicalFixedIborBlackMethod.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;

  @Test
  public void presentValue() {
    final CurrencyAmount pvComputed = METHOD_SWAPTION_SPREAD.presentValue(SWAPTION_SPREAD_EUR1Y3M, BUNDLE);
    final double pvbp = METHOD_SWAP_SPREAD.presentValueBasisPoint(SWAP_SPREAD_EUR1Y3M, EUR1YEURIBOR3M.getFixedLegDayCount(), CURVES);
    final double forward = METHOD_SWAP_SPREAD.forwardSwapSpreadModified(SWAP_SPREAD_EUR1Y3M, pvbp, CURVES);
    final double strike = METHOD_SWAP_SPREAD.couponEquivalentSpreadModified(SWAP_SPREAD_EUR1Y3M, pvbp, BUNDLE);
    final double volatility = BLACK.getVolatility(SWAPTION_SPREAD_EUR1Y3M.getTimeToExpiry(), SWAPTION_SPREAD_EUR1Y3M.getMaturityTime());
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, SWAPTION_SPREAD_EUR1Y3M.getTimeToExpiry(), SWAPTION_SPREAD_EUR1Y3M.isCall());
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, pvbp, volatility);
    final Function1D<BlackFunctionData, Double> func = blackFunction.getPriceFunction(option);
    final double pvExpected = func.evaluate(dataBlack) * (IS_LONG ? 1.0 : -1.0);
    assertEquals("SwaptionPhysicalFixedIborSpreadBlackMethod: presentValue", pvExpected, pvComputed.getAmount(), TOLERANCE_PV);
  }

  @Test
  /**
   * Compare the present value of a swaption with spread to a swaption wihout spread and an adjusted strike.
   */
  public void presentValueNoSpread() {
    final CurrencyAmount pvComputed = METHOD_SWAPTION_SPREAD.presentValue(SWAPTION_SPREAD_EUR3M3M, BUNDLE);
    final CurrencyAmount pvExpected = METHOD_SWAPTION.presentValue(SWAPTION_NOSPREAD_EUR3M3M, BUNDLE);
    assertEquals("SwaptionPhysicalFixedIborSpreadBlackMethod: presentValue", pvExpected.getAmount(), pvComputed.getAmount(), TOLERANCE_PV);
  }

}
