/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.method;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.ParRateCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.analytics.financial.interestrate.sensitivity.PresentValueSwaptionSurfaceSensitivity;
import com.opengamma.analytics.financial.interestrate.swap.method.SwapFixedCouponDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  Class used to compute the price and sensitivity of a physical delivery swaption with Black model.
 *  The implied Black volatilities are expiry and underlying maturity dependent.
 *  The swap underlying the swaption should be a Fixed for Ibor (without spread) swap.
 *  @deprecated Use {@link SwaptionPhysicalFixedIborBlackMethod}
 */
@Deprecated
public final class SwaptionPhysicalFixedIborBlackMethod implements PricingMethod {

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
   * The par rate sensitivity calculator.
   */
  private static final ParRateCurveSensitivityCalculator PRSC = ParRateCurveSensitivityCalculator.getInstance();
  /**
   * The par rate calculator.
   */
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  /**
   * The swap method.
   */
  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();

  /**
   * Computes the present value of a physical delivery European swaption in the Black model.
   * @param swaption The swaption.
   * @param curveBlack The curves with Black volatility data.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final SwaptionPhysicalFixedIbor swaption, final YieldCurveWithBlackSwaptionBundle curveBlack) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(curveBlack, "Curves with Black volatility");
    final GeneratorInstrument<GeneratorAttributeIR> generatorSwap = curveBlack.getBlackParameters().getGeneratorSwap();
    final GeneratorSwapFixedIbor fixedIborGenerator = (GeneratorSwapFixedIbor) generatorSwap;
    final Calendar calendar = fixedIborGenerator.getCalendar();
    final DayCount fixedLegDayCount = fixedIborGenerator.getFixedLegDayCount();
    final double pvbpModified = METHOD_SWAP.presentValueBasisPoint(swaption.getUnderlyingSwap(), fixedLegDayCount,
        calendar, curveBlack);
    final double forwardModified = PRC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), fixedLegDayCount, curveBlack, calendar);
    final double strikeModified = METHOD_SWAP.couponEquivalent(swaption.getUnderlyingSwap(), pvbpModified, curveBlack);
    final double maturity = swaption.getMaturityTime();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strikeModified, swaption.getTimeToExpiry(), swaption.isCall());
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final double volatility = curveBlack.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), maturity);
    final BlackFunctionData dataBlack = new BlackFunctionData(forwardModified, pvbpModified, volatility);
    final Function1D<BlackFunctionData, Double> func = blackFunction.getPriceFunction(option);
    final double pv = func.evaluate(dataBlack) * (swaption.isLong() ? 1.0 : -1.0);
    return CurrencyAmount.of(swaption.getCurrency(), pv);
  }

  /**
   * Computes the present value of a physical delivery European swaption in the Black model.
   * @param swaption The swaption.
   * @param curveBlack The curves with Black volatility data.
   * @return The present value.
   */
  public double forward(final SwaptionPhysicalFixedIbor swaption, final YieldCurveWithBlackSwaptionBundle curveBlack) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(curveBlack, "Curves with Black volatility");
    final GeneratorInstrument<GeneratorAttributeIR> generatorSwap = curveBlack.getBlackParameters().getGeneratorSwap();
    final GeneratorSwapFixedIbor fixedIborGenerator = (GeneratorSwapFixedIbor) generatorSwap;
    final Calendar calendar = fixedIborGenerator.getCalendar();
    final DayCount fixedLegDayCount = fixedIborGenerator.getFixedLegDayCount();
    return PRC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), fixedLegDayCount, curveBlack, calendar);
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    ArgumentChecker.isTrue(instrument instanceof SwaptionPhysicalFixedIbor, "Physical delivery swaption");
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackSwaptionBundle, "Bundle should contain Black Swaption data");
    return presentValue((SwaptionPhysicalFixedIbor) instrument, (YieldCurveWithBlackSwaptionBundle) curves);
  }

  /**
   * Computes the implied Black volatility of the vanilla swaption.
   * @param swaption The swaption.
   * @param curves The yield curve bundle.
   * @return The implied volatility.
   */
  public double impliedVolatility(final SwaptionPhysicalFixedIbor swaption, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "Curves");
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackSwaptionBundle, "Yield curve bundle should contain Black swaption data");
    final YieldCurveWithBlackSwaptionBundle curvesBlack = (YieldCurveWithBlackSwaptionBundle) curves;
    ArgumentChecker.notNull(swaption, "Forex option");
    final double tenor = swaption.getMaturityTime();
    final double volatility = curvesBlack.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), tenor);
    return volatility;
  }

  /**
   * Computes the present value rate sensitivity to rates of a physical delivery European swaption in the SABR model.
   * @param swaption The swaption.
   * @param curveBlack The curves with Black volatility data.
   * @return The present value curve sensitivity.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final SwaptionPhysicalFixedIbor swaption, final YieldCurveWithBlackSwaptionBundle curveBlack) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(curveBlack, "Curves with Black volatility");
    final GeneratorInstrument<GeneratorAttributeIR> generatorSwap = curveBlack.getBlackParameters().getGeneratorSwap();
    final GeneratorSwapFixedIbor fixedIborGenerator = (GeneratorSwapFixedIbor) generatorSwap;
    final Calendar calendar = fixedIborGenerator.getCalendar();
    final DayCount dayCountModification = fixedIborGenerator.getFixedLegDayCount();
    final double pvbpModified = METHOD_SWAP.presentValueBasisPoint(swaption.getUnderlyingSwap(), dayCountModification, calendar, curveBlack);
    final double forwardModified = PRC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), dayCountModification, curveBlack, calendar);
    final double strikeModified = METHOD_SWAP.couponEquivalent(swaption.getUnderlyingSwap(), pvbpModified, curveBlack);
    final double maturity = swaption.getMaturityTime();
    // Derivative of the forward and pvbp with respect to the rates.
    final InterestRateCurveSensitivity pvbpModifiedDr = METHOD_SWAP.presentValueBasisPointCurveSensitivity(swaption.getUnderlyingSwap(), dayCountModification, calendar, curveBlack);
    final InterestRateCurveSensitivity forwardModifiedDr = new InterestRateCurveSensitivity(PRSC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), dayCountModification, curveBlack));
    // Implementation note: strictly speaking, the strike equivalent is curve dependent; that dependency is ignored.
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strikeModified, swaption.getTimeToExpiry(), swaption.isCall());
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final double volatility = curveBlack.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), maturity);
    final BlackFunctionData dataBlack = new BlackFunctionData(forwardModified, 1.0, volatility);
    final double[] bsAdjoint = blackFunction.getPriceAdjoint(option, dataBlack);
    InterestRateCurveSensitivity result = pvbpModifiedDr.multipliedBy(bsAdjoint[0]);
    result = result.plus(forwardModifiedDr.multipliedBy(pvbpModified * bsAdjoint[1]));
    if (!swaption.isLong()) {
      result = result.multipliedBy(-1);
    }
    return result;
  }

  /**
   * Computes the present value sensitivity to the Black volatility (also called vega) of a physical delivery European swaption in the Black swaption model.
   * @param swaption The swaption.
   * @param curveBlack The curves with Black volatility data.
   * @return The present value Black sensitivity.
   */
  public PresentValueSwaptionSurfaceSensitivity presentValueBlackSensitivity(final SwaptionPhysicalFixedIbor swaption, final YieldCurveWithBlackSwaptionBundle curveBlack) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(curveBlack, "Curves with Black volatility");
    final GeneratorInstrument<GeneratorAttributeIR> generatorSwap = curveBlack.getBlackParameters().getGeneratorSwap();
    final GeneratorSwapFixedIbor fixedIborGenerator = (GeneratorSwapFixedIbor) generatorSwap;
    final Calendar calendar = fixedIborGenerator.getCalendar();
    final DayCount dayCountModification = fixedIborGenerator.getFixedLegDayCount();
    final double pvbpModified = METHOD_SWAP.presentValueBasisPoint(swaption.getUnderlyingSwap(), dayCountModification, calendar,
        curveBlack);
    final double forwardModified = PRC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), dayCountModification, curveBlack, calendar);
    final double strikeModified = METHOD_SWAP.couponEquivalent(swaption.getUnderlyingSwap(), pvbpModified, curveBlack);
    final double maturity = swaption.getMaturityTime();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strikeModified, swaption.getTimeToExpiry(), swaption.isCall());
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    final DoublesPair point = DoublesPair.of(swaption.getTimeToExpiry(), maturity);
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final double volatility = curveBlack.getBlackParameters().getVolatility(point);
    final BlackFunctionData dataBlack = new BlackFunctionData(forwardModified, 1.0, volatility);
    final double[] bsAdjoint = blackFunction.getPriceAdjoint(option, dataBlack);
    final Map<DoublesPair, Double> sensitivity = new HashMap<>();
    sensitivity.put(point, bsAdjoint[2] * pvbpModified * (swaption.isLong() ? 1.0 : -1.0));
    return new PresentValueSwaptionSurfaceSensitivity(sensitivity, curveBlack.getBlackParameters().getGeneratorSwap());
  }

  /**
   * Compute first derivative of present value with respect to forward rate
   * @param swaption The swaption.
   * @param curveBlack The curves with Black volatility data.
   * @return The forward delta
   */
  public double forwardDeltaTheoretical(final SwaptionPhysicalFixedIbor swaption, final YieldCurveWithBlackSwaptionBundle curveBlack) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(curveBlack, "Curves with Black volatility");
    final GeneratorInstrument<GeneratorAttributeIR> generatorSwap = curveBlack.getBlackParameters().getGeneratorSwap();
    final GeneratorSwapFixedIbor fixedIborGenerator = (GeneratorSwapFixedIbor) generatorSwap;
    final Calendar calendar = fixedIborGenerator.getCalendar();
    final DayCount fixedLegDayCount = fixedIborGenerator.getFixedLegDayCount();
    final double pvbpModified = METHOD_SWAP.presentValueBasisPoint(swaption.getUnderlyingSwap(), fixedLegDayCount,
        calendar, curveBlack);
    final double forwardModified = PRC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), fixedLegDayCount, curveBlack, calendar);
    final double strikeModified = METHOD_SWAP.couponEquivalent(swaption.getUnderlyingSwap(), pvbpModified, curveBlack);
    final double maturity = swaption.getMaturityTime();
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    final double volatility = curveBlack.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), maturity);

    final double expiry = swaption.getTimeToExpiry();
    return BlackFormulaRepository.delta(forwardModified, strikeModified, expiry, volatility, swaption.isCall()) * (swaption.isLong() ? 1.0 : -1.0);
  }

  /**
   * Calculates the delta
   * @param swaption The swaption, not null
   * @param curves Yield curves and swaption volatility surface, not null
   * @return The delta
   */
  public CurrencyAmount delta(final SwaptionPhysicalFixedIbor swaption, final YieldCurveWithBlackSwaptionBundle curves) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(curves, "Curves with Black volatility");
    final GeneratorInstrument<GeneratorAttributeIR> generatorSwap = curves.getBlackParameters().getGeneratorSwap();
    final GeneratorSwapFixedIbor fixedIborGenerator = (GeneratorSwapFixedIbor) generatorSwap;
    final Calendar calendar = fixedIborGenerator.getCalendar();
    final DayCount dayCountModification = fixedIborGenerator.getFixedLegDayCount();
    final double forwardModified = PRC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), dayCountModification, curves, calendar);
    final double sign = swaption.isLong() ? 1.0 : -1.0;
    return CurrencyAmount.of(swaption.getCurrency(), forwardDeltaTheoretical(swaption, curves) * forwardModified * sign);
  }

  /**
   * Computes the gamma of the swaption. The gamma is the second order derivative of the option present value to the spot fx rate.
   * @param swaption The Forex option.
   * @param curves The yield curve bundle.
   * @return The gamma.
   */
  public CurrencyAmount gamma(final SwaptionPhysicalFixedIbor swaption, final YieldCurveWithBlackSwaptionBundle curves) {
    ArgumentChecker.notNull(curves, "Curves");
    final double gamma = forwardGammaTheoretical(swaption, curves);
    final GeneratorInstrument<GeneratorAttributeIR> generatorSwap = curves.getBlackParameters().getGeneratorSwap();
    final GeneratorSwapFixedIbor fixedIborGenerator = (GeneratorSwapFixedIbor) generatorSwap;
    final Calendar calendar = fixedIborGenerator.getCalendar();
    final DayCount dayCountModification = fixedIborGenerator.getFixedLegDayCount();
    final double forwardModified = PRC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), dayCountModification, curves, calendar);
    final double sign = swaption.isLong() ? 1.0 : -1.0;
    return CurrencyAmount.of(swaption.getCurrency(), gamma * forwardModified * forwardModified * sign);
  }

  /**
   * Calculates the theta
   * @param swaption The swaption, not null
   * @param curves Yield curves and swaption volatility surface, not null
   * @return The delta
   */
  public CurrencyAmount theta(final SwaptionPhysicalFixedIbor swaption, final YieldCurveWithBlackSwaptionBundle curves) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(curves, "Curves with Black volatility");
    final double sign = swaption.isLong() ? 1.0 : -1.0;
    return CurrencyAmount.of(swaption.getCurrency(), forwardThetaTheoretical(swaption, curves) * sign);
  }

  /**
   * Compute second derivative of present value with respect to forward rate
   * @param swaption The swaption.
   * @param curveBlack The curves with Black volatility data.
   * @return The forward gamma
   */
  public double forwardGammaTheoretical(final SwaptionPhysicalFixedIbor swaption, final YieldCurveWithBlackSwaptionBundle curveBlack) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(curveBlack, "Curves with Black volatility");
    final GeneratorInstrument<GeneratorAttributeIR> generatorSwap = curveBlack.getBlackParameters().getGeneratorSwap();
    final GeneratorSwapFixedIbor fixedIborGenerator = (GeneratorSwapFixedIbor) generatorSwap;
    final Calendar calendar = fixedIborGenerator.getCalendar();
    final DayCount fixedLegDayCount = fixedIborGenerator.getFixedLegDayCount();
    final double pvbpModified = METHOD_SWAP.presentValueBasisPoint(swaption.getUnderlyingSwap(), fixedLegDayCount,
        calendar, curveBlack);
    final double forwardModified = PRC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), fixedLegDayCount, curveBlack, calendar);
    final double strikeModified = METHOD_SWAP.couponEquivalent(swaption.getUnderlyingSwap(), pvbpModified, curveBlack);
    final double maturity = swaption.getMaturityTime();
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    final double volatility = curveBlack.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), maturity);

    final double expiry = swaption.getTimeToExpiry();
    return BlackFormulaRepository.gamma(forwardModified, strikeModified, expiry, volatility) * (swaption.isLong() ? 1.0 : -1.0);
  }

  /**
   * Compute minus of first derivative of present value with respect to time, setting drift term to be 0
   * @param swaption The swaption.
   * @param curveBlack The curves with Black volatility data.
   * @return The driftless theta
   */
  public double driftlessThetaTheoretical(final SwaptionPhysicalFixedIbor swaption, final YieldCurveWithBlackSwaptionBundle curveBlack) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(curveBlack, "Curves with Black volatility");
    final GeneratorInstrument<GeneratorAttributeIR> generatorSwap = curveBlack.getBlackParameters().getGeneratorSwap();
    final GeneratorSwapFixedIbor fixedIborGenerator = (GeneratorSwapFixedIbor) generatorSwap;
    final Calendar calendar = fixedIborGenerator.getCalendar();
    final DayCount fixedLegDayCount = fixedIborGenerator.getFixedLegDayCount();
    final double pvbpModified = METHOD_SWAP.presentValueBasisPoint(swaption.getUnderlyingSwap(), fixedLegDayCount,
        calendar, curveBlack);
    final double forwardModified = PRC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), fixedLegDayCount, curveBlack, calendar);
    final double strikeModified = METHOD_SWAP.couponEquivalent(swaption.getUnderlyingSwap(), pvbpModified, curveBlack);
    final double maturity = swaption.getMaturityTime();
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    final double volatility = curveBlack.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), maturity);

    final double expiry = swaption.getTimeToExpiry();
    return BlackFormulaRepository.driftlessTheta(forwardModified, strikeModified, expiry, volatility) * (swaption.isLong() ? 1.0 : -1.0);
  }

  /**
   * Compute minus of first derivative of present value with respect to time
   * @param swaption The swaption.
   * @param curveBlack The curves with Black volatility data.
   * @return The forward theta
   */
  public double forwardThetaTheoretical(final SwaptionPhysicalFixedIbor swaption, final YieldCurveWithBlackSwaptionBundle curveBlack) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(curveBlack, "Curves with Black volatility");
    final GeneratorInstrument<GeneratorAttributeIR> generatorSwap = curveBlack.getBlackParameters().getGeneratorSwap();
    final GeneratorSwapFixedIbor fixedIborGenerator = (GeneratorSwapFixedIbor) generatorSwap;
    final Calendar calendar = fixedIborGenerator.getCalendar();
    final DayCount fixedLegDayCount = fixedIborGenerator.getFixedLegDayCount();
    final double pvbpModified = METHOD_SWAP.presentValueBasisPoint(swaption.getUnderlyingSwap(), fixedLegDayCount,
        calendar, curveBlack);
    final double forwardModified = PRC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), fixedLegDayCount, curveBlack, calendar);
    final double strikeModified = METHOD_SWAP.couponEquivalent(swaption.getUnderlyingSwap(), pvbpModified, curveBlack);
    final double maturity = swaption.getMaturityTime();
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    final double volatility = curveBlack.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), maturity);

    final double expiry = swaption.getTimeToExpiry();
    return pvbpModified * BlackFormulaRepository.driftlessTheta(forwardModified, strikeModified, expiry, volatility);
  }

  /**
   * Compute first derivative of present value with respect to volatility
   * @param swaption The swaption.
   * @param curveBlack The curves with Black volatility data.
   * @return The forward vega
   */
  public double forwardVegaTheoretical(final SwaptionPhysicalFixedIbor swaption, final YieldCurveWithBlackSwaptionBundle curveBlack) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(curveBlack, "Curves with Black volatility");
    final GeneratorInstrument<GeneratorAttributeIR> generatorSwap = curveBlack.getBlackParameters().getGeneratorSwap();
    final GeneratorSwapFixedIbor fixedIborGenerator = (GeneratorSwapFixedIbor) generatorSwap;
    final Calendar calendar = fixedIborGenerator.getCalendar();
    final DayCount fixedLegDayCount = fixedIborGenerator.getFixedLegDayCount();
    final double pvbpModified = METHOD_SWAP.presentValueBasisPoint(swaption.getUnderlyingSwap(), fixedLegDayCount,
        calendar, curveBlack);
    final double forwardModified = PRC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), fixedLegDayCount, curveBlack, calendar);
    final double strikeModified = METHOD_SWAP.couponEquivalent(swaption.getUnderlyingSwap(), pvbpModified, curveBlack);
    final double maturity = swaption.getMaturityTime();
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    final double volatility = curveBlack.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), maturity);

    final double expiry = swaption.getTimeToExpiry();

    return BlackFormulaRepository.vega(forwardModified, strikeModified, expiry, volatility) * (swaption.isLong() ? 1.0 : -1.0);
  }

}
