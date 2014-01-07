/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.provider;

import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.interestrate.swap.provider.SwapFixedIborSpreadDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSwaptionFlatProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *  Class used to compute the price and sensitivity of a physical delivery swaption on a swap with spread in the Black model.
 *  The implied Black volatilities are expiry and underlying maturity dependent.
 *  The swap underlying the swaption should be a Fixed for Ibor with spread swap.
 */
public final class SwaptionPhysicalFixedIborSpreadBlackMethod {

  /**
   * The method unique instance.
   */
  private static final SwaptionPhysicalFixedIborSpreadBlackMethod INSTANCE = new SwaptionPhysicalFixedIborSpreadBlackMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static SwaptionPhysicalFixedIborSpreadBlackMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private SwaptionPhysicalFixedIborSpreadBlackMethod() {
  }

  /**
   * The swap method.
   */
  private static final SwapFixedIborSpreadDiscountingMethod METHOD_SWAP = SwapFixedIborSpreadDiscountingMethod.getInstance();

  /**
   * Computes the present value of a physical delivery European swaption in the Black model.
   * @param swaption The swaption.
   * @param blackMulticurves Black volatility for swaption and multi-curves provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final SwaptionPhysicalFixedIbor swaption, final BlackSwaptionFlatProviderInterface blackMulticurves) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(blackMulticurves, "Black volatility for swaption and multicurve");
    final GeneratorInstrument<GeneratorAttributeIR> generatorSwap = blackMulticurves.getBlackParameters().getGeneratorSwap();
    Calendar calendar;
    DayCount dayCountModification;
    if (generatorSwap instanceof GeneratorSwapFixedIbor) {
      final GeneratorSwapFixedIbor fixedIborGenerator = (GeneratorSwapFixedIbor) generatorSwap;
      calendar = fixedIborGenerator.getCalendar();
      dayCountModification = fixedIborGenerator.getFixedLegDayCount();
    } else if (generatorSwap instanceof GeneratorSwapFixedON) {
      final GeneratorSwapFixedON fixedONGenerator = (GeneratorSwapFixedON) generatorSwap;
      calendar = fixedONGenerator.getOvernightCalendar();
      dayCountModification = fixedONGenerator.getFixedLegDayCount();
    } else if (generatorSwap instanceof GeneratorSwapFixedCompoundedONCompounded) {
      final GeneratorSwapFixedCompoundedONCompounded fixedCompoundedON = (GeneratorSwapFixedCompoundedONCompounded) generatorSwap;
      calendar = fixedCompoundedON.getOvernightCalendar();
      dayCountModification = fixedCompoundedON.getFixedLegDayCount();
    } else {
      throw new IllegalArgumentException("Cannot handle swap with underlying generator of type " + generatorSwap.getClass());
    }
    final MulticurveProviderInterface multicurves = blackMulticurves.getMulticurveProvider();
    final double pvbpModified = METHOD_SWAP.presentValueBasisPoint(swaption.getUnderlyingSwap(), dayCountModification, calendar, multicurves);
    final double forwardModified = METHOD_SWAP.forwardSwapSpreadModified(swaption.getUnderlyingSwap(), pvbpModified, multicurves);
    final double strikeModified = METHOD_SWAP.couponEquivalentSpreadModified(swaption.getUnderlyingSwap(), pvbpModified, multicurves);
    final double maturity = swaption.getMaturityTime();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strikeModified, swaption.getTimeToExpiry(), swaption.isCall());
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final double volatility = blackMulticurves.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), maturity);
    final BlackFunctionData dataBlack = new BlackFunctionData(forwardModified, pvbpModified, volatility);
    final Function1D<BlackFunctionData, Double> func = blackFunction.getPriceFunction(option);
    final double pv = func.evaluate(dataBlack) * (swaption.isLong() ? 1.0 : -1.0);
    return MultipleCurrencyAmount.of(swaption.getCurrency(), pv);
  }

}
