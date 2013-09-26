/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.PresentValueBlackSwaptionSensitivity;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedAccruedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONCompounded;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.method.SwapFixedCompoundingONCompoundingDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  Class used to compute the price and sensitivity of a physical delivery swaption with Black model.
 *  The implied Black volatilities are expiry and underlying maturity dependent.
 *  The swap underlying the swaption should be a Fixed for Ibor (without spread) swap.
 *  @deprecated Use {@link SwaptionPhysicalFixedCompoundedONCompoundedBlackMethod}
 */
@Deprecated
public final class SwaptionPhysicalFixedCompoundedONCompoundedBlackMethod implements PricingMethod {

  /**
   * The method unique instance.
   */
  private static final SwaptionPhysicalFixedCompoundedONCompoundedBlackMethod INSTANCE = new SwaptionPhysicalFixedCompoundedONCompoundedBlackMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static SwaptionPhysicalFixedCompoundedONCompoundedBlackMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private SwaptionPhysicalFixedCompoundedONCompoundedBlackMethod() {
  }

  /**
   * The present value curve sensitivity calculator.
   */
  private static final PresentValueCurveSensitivityCalculator PV_SENSITIVITY_CALCULATOR = PresentValueCurveSensitivityCalculator.getInstance();
  /**
   * The present value calculator.
   */
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  /**
   * The swap method.
   */
  private static final SwapFixedCompoundingONCompoundingDiscountingMethod METHOD_SWAP = SwapFixedCompoundingONCompoundingDiscountingMethod.getInstance();

  /**
   * Computes the present value of a physical delivery European swaption in the Black model.
   * @param swaption The swaption.
   * @param curveBlack The curves with Black volatility data.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final SwaptionPhysicalFixedCompoundedONCompounded swaption, final YieldCurveWithBlackSwaptionBundle curveBlack) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(curveBlack, "Curves with Black volatility");
    final Swap<CouponFixedAccruedCompounding, CouponONCompounded> swap = swaption.getUnderlyingSwap();
    final CouponFixedAccruedCompounding cpnFixed = swap.getFirstLeg().getNthPayment(0);
    final double numeraire = curveBlack.getCurve(cpnFixed.getFundingCurveName()).getDiscountFactor(cpnFixed.getPaymentTime()) * cpnFixed.getNotional();
    final double delta = swap.getFirstLeg().getNthPayment(0).getPaymentYearFraction();
    final double forwardModified = METHOD_SWAP.forwardModified(swap, curveBlack);
    final double strikeModified = Math.pow(1.0d + swaption.getStrike(), delta) - 1.0;
    // Implementation note: Modified strike: \bar K = (1+K)^\delta-1; the swaption payoff is pvbp*(\bar F - \bar K)^+
    final double maturity = swaption.getMaturityTime();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strikeModified, swaption.getTimeToExpiry(), swaption.isCall());
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final double volatility = curveBlack.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), maturity);
    final BlackFunctionData dataBlack = new BlackFunctionData(forwardModified, numeraire, volatility);
    final Function1D<BlackFunctionData, Double> func = blackFunction.getPriceFunction(option);
    final double pv = func.evaluate(dataBlack) * (swaption.isLong() ? 1.0 : -1.0);
    return CurrencyAmount.of(swaption.getCurrency(), pv);
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    ArgumentChecker.isTrue(instrument instanceof SwaptionPhysicalFixedCompoundedONCompounded, "Physical delivery swaption");
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackSwaptionBundle, "Bundle should contain Black Swaption data");
    return presentValue((SwaptionPhysicalFixedCompoundedONCompounded) instrument, (YieldCurveWithBlackSwaptionBundle) curves);
  }

  /**
   * Computes the implied Black volatility of the vanilla swaption.
   * @param swaption The swaption.
   * @param curves The yield curve bundle.
   * @return The implied volatility.
   */
  public double impliedVolatility(final SwaptionPhysicalFixedCompoundedONCompounded swaption, final YieldCurveBundle curves) {
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
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final SwaptionPhysicalFixedCompoundedONCompounded swaption, final YieldCurveWithBlackSwaptionBundle curveBlack) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(curveBlack, "Curves with Black volatility");
    final Swap<CouponFixedAccruedCompounding, CouponONCompounded> swap = swaption.getUnderlyingSwap();
    final CouponFixedAccruedCompounding cpnFixed = swap.getFirstLeg().getNthPayment(0);
    final double numeraire = curveBlack.getCurve(cpnFixed.getFundingCurveName()).getDiscountFactor(cpnFixed.getPaymentTime()) * cpnFixed.getNotional();
    final double delta = swap.getFirstLeg().getNthPayment(0).getPaymentYearFraction();
    final double forwardModified = METHOD_SWAP.forwardModified(swap, curveBlack);
    final double strikeModified = Math.pow(1.0d + swaption.getStrike(), delta) - 1.0;
    // Implementation note: Modified strike: \bar K = (1+K)^\delta-1; the swaption payoff is pvbp*(\bar F - \bar K)^+
    final double maturity = swaption.getMaturityTime();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strikeModified, swaption.getTimeToExpiry(), swaption.isCall());
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final double volatility = curveBlack.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), maturity);
    final BlackFunctionData dataBlack = new BlackFunctionData(forwardModified, 1, volatility);
    final double[] bsAdjoint = blackFunction.getPriceAdjoint(option, dataBlack);
    final double sign = (swaption.isLong() ? 1.0 : -1.0);
    //    final double pv = numeraire * bsAdjoint[0] * sign;
    // Backward sweep
    final double pvBar = 1.0;
    final double numeraireBar = bsAdjoint[0] * sign * pvBar;
    final double forwardModifiedBar = numeraire * bsAdjoint[1] * sign * pvBar;
    InterestRateCurveSensitivity forwardModifiedDr = METHOD_SWAP.forwardModifiedCurveSensitivity(swap, curveBlack);
    final double numeraireDr = -cpnFixed.getPaymentTime() * numeraire;
    final List<DoublesPair> list = new ArrayList<>();
    list.add(new DoublesPair(cpnFixed.getPaymentTime(), numeraireDr * numeraireBar));
    final Map<String, List<DoublesPair>> numeraireMap = new HashMap<>();
    numeraireMap.put(cpnFixed.getFundingCurveName(), list);
    InterestRateCurveSensitivity result = new InterestRateCurveSensitivity(numeraireMap);
    result = result.plus(forwardModifiedDr.multipliedBy(forwardModifiedBar));
    result = result.cleaned();
    return result;
  }

  /**
   * Computes the present value sensitivity to the Black volatility (also called vega) of a physical delivery European swaption in the Black swaption model.
   * @param swaption The swaption.
   * @param curveBlack The curves with Black volatility data.
   * @return The present value Black sensitivity.
   */
  public PresentValueBlackSwaptionSensitivity presentValueBlackSensitivity(final SwaptionPhysicalFixedCompoundedONCompounded swaption, final YieldCurveWithBlackSwaptionBundle curveBlack) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(curveBlack, "Curves with Black volatility");
    final Swap<CouponFixedAccruedCompounding, CouponONCompounded> swap = swaption.getUnderlyingSwap();
    final CouponFixedAccruedCompounding cpnFixed = swap.getFirstLeg().getNthPayment(0);
    final double numeraire = curveBlack.getCurve(cpnFixed.getFundingCurveName()).getDiscountFactor(cpnFixed.getPaymentTime()) * cpnFixed.getNotional();
    final double delta = swap.getFirstLeg().getNthPayment(0).getPaymentYearFraction();
    final double forwardModified = METHOD_SWAP.forwardModified(swap, curveBlack);
    final double strikeModified = Math.pow(1.0d + swaption.getStrike(), delta) - 1.0;
    // Implementation note: Modified strike: \bar K = (1+K)^\delta-1; the swaption payoff is pvbp*(\bar F - \bar K)^+
    final double maturity = swaption.getMaturityTime();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strikeModified, swaption.getTimeToExpiry(), swaption.isCall());
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final double volatility = curveBlack.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), maturity);
    final BlackFunctionData dataBlack = new BlackFunctionData(forwardModified, 1, volatility);
    final double[] bsAdjoint = blackFunction.getPriceAdjoint(option, dataBlack);
    final double sign = (swaption.isLong() ? 1.0 : -1.0);
    //    final double pv = numeraire * bsAdjoint[0] * sign;
    // Backward sweep
    final DoublesPair point = new DoublesPair(swaption.getTimeToExpiry(), maturity);
    final Map<DoublesPair, Double> sensitivity = new HashMap<>();
    sensitivity.put(point, bsAdjoint[2] * numeraire * sign);
    return new PresentValueBlackSwaptionSensitivity(sensitivity, curveBlack.getBlackParameters().getGeneratorSwap());
  }

}
