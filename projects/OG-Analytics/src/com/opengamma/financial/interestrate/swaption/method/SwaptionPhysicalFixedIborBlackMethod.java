/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swaption.method;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.ParRateCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.PresentValueBlackSwaptionSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.method.PricingMethod;
import com.opengamma.financial.interestrate.swap.SwapFixedDiscountingMethod;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  Class used to compute the price and sensitivity of a physical delivery swaption with SABR model.
 */
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
   * Computes the present value of a physical delivery European swaption in the Black model.
   * @param swaption The swaption.
   * @param curveBlack The curves with Black volatility data.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final SwaptionPhysicalFixedIbor swaption, final YieldCurveWithBlackSwaptionBundle curveBlack) {
    Validate.notNull(swaption, "Swaption");
    Validate.notNull(curveBlack, "Curves with Black volatility");
    Validate.isTrue(curveBlack.getBlackParameters().getGeneratorSwap().getCurrency() == swaption.getCurrency(), "Black data currency should be equal to swaption currency");
    final double pvbp = SwapFixedDiscountingMethod.presentValueBasisPoint(swaption.getUnderlyingSwap(), curveBlack);
    final double pvbpModified = SwapFixedDiscountingMethod.presentValueBasisPoint(swaption.getUnderlyingSwap(), curveBlack.getBlackParameters().getGeneratorSwap().getFixedLegDayCount(), curveBlack);
    final double forward = PRC.visit(swaption.getUnderlyingSwap(), curveBlack);
    final double forwardModified = forward * pvbp / pvbpModified;
    final double strikeModified = SwapFixedDiscountingMethod.couponEquivalent(swaption.getUnderlyingSwap(), pvbpModified, curveBlack);
    final double tenor = swaption.getMaturityTime();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strikeModified, swaption.getTimeToExpiry(), swaption.isCall());
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final double volatility = curveBlack.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), tenor);
    final BlackFunctionData dataBlack = new BlackFunctionData(forwardModified, pvbpModified, volatility);
    final Function1D<BlackFunctionData, Double> func = blackFunction.getPriceFunction(option);
    final double pv = func.evaluate(dataBlack) * (swaption.isLong() ? 1.0 : -1.0);
    return CurrencyAmount.of(swaption.getCurrency(), pv);
  }

  @Override
  public CurrencyAmount presentValue(InstrumentDerivative instrument, YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof SwaptionPhysicalFixedIbor, "Physical delivery swaption");
    Validate.isTrue(curves instanceof YieldCurveWithBlackSwaptionBundle, "Bundle should contain Black Swaption data");
    return presentValue((SwaptionPhysicalFixedIbor) instrument, (YieldCurveWithBlackSwaptionBundle) curves);
  }

  /**
   * Computes the present value rate sensitivity to rates of a physical delivery European swaption in the SABR model.
   * @param swaption The swaption.
   * @param curveBlack The curves with Black volatility data.
   * @return The present value curve sensitivity.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final SwaptionPhysicalFixedIbor swaption, final YieldCurveWithBlackSwaptionBundle curveBlack) {
    Validate.notNull(swaption, "Swaption");
    Validate.notNull(curveBlack, "Curves with Black volatility");
    Validate.isTrue(curveBlack.getBlackParameters().getGeneratorSwap().getCurrency() == swaption.getCurrency(), "Black data currency should be equal to swaption currency");
    final double forward = PRC.visit(swaption.getUnderlyingSwap(), curveBlack);
    // Derivative of the forward with respect to the rates.
    final InterestRateCurveSensitivity forwardDr = new InterestRateCurveSensitivity(PRSC.visit(swaption.getUnderlyingSwap(), curveBlack));
    final double pvbp = SwapFixedDiscountingMethod.presentValueBasisPoint(swaption.getUnderlyingSwap(), curveBlack);
    // Derivative of the PVBP with respect to the rates.
    final InterestRateCurveSensitivity pvbpDr = SwapFixedDiscountingMethod.presentValueBasisPointCurveSensitivity(swaption.getUnderlyingSwap(), curveBlack);
    // Implementation note: strictly speaking, the strike equivalent is curve dependent; that dependency is ignored.
    final double strike = SwapFixedDiscountingMethod.couponEquivalent(swaption.getUnderlyingSwap(), pvbp, curveBlack);
    final double tenor = swaption.getMaturityTime();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, swaption.getTimeToExpiry(), swaption.isCall());
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final double volatility = curveBlack.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), tenor);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatility);
    final double[] bsAdjoint = blackFunction.getPriceAdjoint(option, dataBlack);
    InterestRateCurveSensitivity result = pvbpDr.multiply(bsAdjoint[0]);
    result = result.add(forwardDr.multiply(pvbp * bsAdjoint[1]));
    if (!swaption.isLong()) {
      result = result.multiply(-1);
    }
    return result;
  }

  /**
   * Computes the present value sensitivity to the Black volatility (also called vega) of a physical delivery European swaption in the Black swaption model.
   * @param swaption The swaption.
   * @param curveBlack The curves with Black volatility data.
   * @return The present value Black sensitivity.
   */
  public PresentValueBlackSwaptionSensitivity presentValueBlackSensitivity(final SwaptionPhysicalFixedIbor swaption, final YieldCurveWithBlackSwaptionBundle curveBlack) {
    Validate.notNull(swaption, "Swaption");
    Validate.notNull(curveBlack, "Curves with Black volatility");
    Validate.isTrue(curveBlack.getBlackParameters().getGeneratorSwap().getCurrency() == swaption.getCurrency(), "Black data currency should be equal to swaption currency");
    final double forward = PRC.visit(swaption.getUnderlyingSwap(), curveBlack);
    final double pvbp = SwapFixedDiscountingMethod.presentValueBasisPoint(swaption.getUnderlyingSwap(), curveBlack);
    // Implementation note: strictly speaking, the strike equivalent is curve dependent; that dependency is ignored.
    final double strike = SwapFixedDiscountingMethod.couponEquivalent(swaption.getUnderlyingSwap(), pvbp, curveBlack);
    final double tenor = swaption.getMaturityTime();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, swaption.getTimeToExpiry(), swaption.isCall());
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    DoublesPair point = new DoublesPair(swaption.getTimeToExpiry(), tenor);
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final double volatility = curveBlack.getBlackParameters().getVolatility(point);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatility);
    final double[] bsAdjoint = blackFunction.getPriceAdjoint(option, dataBlack);
    Map<DoublesPair, Double> sensitivity = new HashMap<DoublesPair, Double>();
    sensitivity.put(point, bsAdjoint[2] * pvbp * (swaption.isLong() ? 1.0 : -1.0));
    return new PresentValueBlackSwaptionSensitivity(sensitivity, curveBlack.getBlackParameters().getGeneratorSwap());
  }

}
