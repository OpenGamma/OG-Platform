/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.provider;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.interestrate.sensitivity.PresentValueBlackSwaptionSensitivity;
import com.opengamma.analytics.financial.interestrate.swap.provider.SwapFixedCouponDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSwaptionFlatProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  Class used to compute the price and sensitivity of a physical delivery swaption with Black model.
 *  The implied Black volatilities are expiry and underlying maturity dependent (no smile).
 *  The swap underlying the swaption should be a Fixed for Ibor (without spread) swap.
 */
public final class SwaptionPhysicalFixedIborBlackMethod {

  /**
   * The method unique instance.
   */
  private static final SwaptionPhysicalFixedIborBlackMethod INSTANCE = new SwaptionPhysicalFixedIborBlackMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static SwaptionPhysicalFixedIborBlackMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private SwaptionPhysicalFixedIborBlackMethod() {
  }

  /**
   * The calculator and methods.
   */
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();
  private static final ParRateCurveSensitivityDiscountingCalculator PRCSDC = ParRateCurveSensitivityDiscountingCalculator.getInstance();
  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();

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
    final double pvbpModified = METHOD_SWAP.presentValueBasisPoint(swaption.getUnderlyingSwap(), dayCountModification,
        calendar, multicurves);
    final double forwardModified = PRDC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), dayCountModification, multicurves);
    final double strikeModified = METHOD_SWAP.couponEquivalent(swaption.getUnderlyingSwap(), pvbpModified, multicurves);
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

  /**
   * Computes the implied Black volatility of the vanilla swaption.
   * @param swaption The swaption.
   * @param blackMulticurves Black volatility for swaption and multi-curves provider.
   * @return The implied volatility.
   */
  public double impliedVolatility(final SwaptionPhysicalFixedIbor swaption, final BlackSwaptionFlatProviderInterface blackMulticurves) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(blackMulticurves, "Black volatility for swaption and multicurve");
    final double tenor = swaption.getMaturityTime();
    final double volatility = blackMulticurves.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), tenor);
    return volatility;
  }

  /**
   * Computes the present value rate sensitivity to rates of a physical delivery European swaption in the SABR model.
   * @param swaption The swaption.
   * @param blackMulticurves Black volatility for swaption and multi-curves provider.
   * @return The present value curve sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final SwaptionPhysicalFixedIbor swaption, final BlackSwaptionFlatProviderInterface blackMulticurves) {
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
    final double forwardModified = PRDC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), dayCountModification, multicurves);
    final double strikeModified = METHOD_SWAP.couponEquivalent(swaption.getUnderlyingSwap(), pvbpModified, multicurves);
    final double maturity = swaption.getMaturityTime();
    // Derivative of the forward and pvbp with respect to the rates.
    final MulticurveSensitivity pvbpModifiedDr = METHOD_SWAP.presentValueBasisPointCurveSensitivity(swaption.getUnderlyingSwap(), dayCountModification,
        calendar, multicurves);
    final MulticurveSensitivity forwardModifiedDr = PRCSDC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), dayCountModification, multicurves);
    // Implementation note: strictly speaking, the strike equivalent is curve dependent; that dependency is ignored.
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strikeModified, swaption.getTimeToExpiry(), swaption.isCall());
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final double volatility = blackMulticurves.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), maturity);
    final BlackFunctionData dataBlack = new BlackFunctionData(forwardModified, 1.0, volatility);
    final double[] bsAdjoint = blackFunction.getPriceAdjoint(option, dataBlack);
    MulticurveSensitivity result = pvbpModifiedDr.multipliedBy(bsAdjoint[0]);
    result = result.plus(forwardModifiedDr.multipliedBy(pvbpModified * bsAdjoint[1]));
    if (!swaption.isLong()) {
      result = result.multipliedBy(-1);
    }
    return MultipleCurrencyMulticurveSensitivity.of(swaption.getCurrency(), result);
  }

  /**
   * Computes the 2nd order sensitivity of the present value to rates of a physical delivery European swaption in the SABR model.
   * @param swaption The swaption.
   * @param blackMulticurves Black volatility for swaption and multi-curves provider.
   * @return The present value curve sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueSecondOrderCurveSensitivity(final SwaptionPhysicalFixedIbor swaption, final BlackSwaptionFlatProviderInterface blackMulticurves) {
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
    final double forwardModified = PRDC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), dayCountModification, multicurves);
    final double strikeModified = METHOD_SWAP.couponEquivalent(swaption.getUnderlyingSwap(), pvbpModified, multicurves);
    final double maturity = swaption.getMaturityTime();
    final MulticurveSensitivity pvbpModifiedDr = METHOD_SWAP.presentValueBasisPointCurveSensitivity(swaption.getUnderlyingSwap(), dayCountModification,
        calendar, multicurves);
    final MulticurveSensitivity forwardModifiedDr = PRCSDC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), dayCountModification, multicurves);
    final MulticurveSensitivity pvbpModifiedDr2 = METHOD_SWAP.presentValueBasisPointSecondOrderCurveSensitivity(swaption.getUnderlyingSwap(), dayCountModification,
        calendar, multicurves);
    final MulticurveSensitivity forwardModifiedDr2 = PRCSDC.visitFixedCouponSwapDerivative(swaption.getUnderlyingSwap(), dayCountModification, multicurves);

    final double volatility = blackMulticurves.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), maturity);

    final double price = BlackFormulaRepository.price(forwardModified, strikeModified, volatility, swaption.getTimeToExpiry(), swaption.isCall());
    final double delta = BlackFormulaRepository.delta(forwardModified, strikeModified, volatility, swaption.getTimeToExpiry(), swaption.isCall());
    final double gamma = BlackFormulaRepository.gamma(forwardModified, strikeModified, volatility, swaption.getTimeToExpiry());

    MulticurveSensitivity result = pvbpModifiedDr2.multipliedBy(price);
    result = result.plus(pvbpModifiedDr.productOf(forwardModifiedDr.multipliedBy(2. * pvbpModified * delta)));
    result = result.plus(forwardModifiedDr2.multipliedBy(pvbpModified * delta));
    result = result.plus(forwardModifiedDr.productOf(forwardModifiedDr.multipliedBy(pvbpModified * gamma)));
    if (!swaption.isLong()) {
      result = result.multipliedBy(-1);
    }
    return MultipleCurrencyMulticurveSensitivity.of(swaption.getCurrency(), result);
  }

  /**
   * Computes the present value sensitivity to the Black volatility (also called vega) of a physical delivery European swaption in the Black swaption model.
   * @param swaption The swaption.
   * @param blackMulticurves Black volatility for swaption and multi-curves provider.
   * @return The present value Black sensitivity.
   */
  public PresentValueBlackSwaptionSensitivity presentValueBlackSensitivity(final SwaptionPhysicalFixedIbor swaption, final BlackSwaptionFlatProviderInterface blackMulticurves) {
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
    final double forwardModified = PRDC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), dayCountModification, multicurves);
    final double strikeModified = METHOD_SWAP.couponEquivalent(swaption.getUnderlyingSwap(), pvbpModified, multicurves);
    final double maturity = swaption.getMaturityTime();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strikeModified, swaption.getTimeToExpiry(), swaption.isCall());
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    final DoublesPair point = DoublesPair.of(swaption.getTimeToExpiry(), maturity);
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final double volatility = blackMulticurves.getBlackParameters().getVolatility(point);
    final BlackFunctionData dataBlack = new BlackFunctionData(forwardModified, 1.0, volatility);
    final double[] bsAdjoint = blackFunction.getPriceAdjoint(option, dataBlack);
    final Map<DoublesPair, Double> sensitivity = new HashMap<>();
    sensitivity.put(point, bsAdjoint[2] * pvbpModified * (swaption.isLong() ? 1.0 : -1.0));
    return new PresentValueBlackSwaptionSensitivity(sensitivity, blackMulticurves.getBlackParameters().getGeneratorSwap());
  }

}
