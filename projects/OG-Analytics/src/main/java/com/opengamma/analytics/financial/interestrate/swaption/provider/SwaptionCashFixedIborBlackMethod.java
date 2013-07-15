/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.PresentValueBlackSwaptionSensitivity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.swap.method.SwapFixedCouponDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSwaptionFlatProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  Class used to compute the price and sensitivity of a physical delivery swaption with SABR model.
 */
public final class SwaptionCashFixedIborBlackMethod {

  /**
   * The method unique instance.
   */
  private static final SwaptionCashFixedIborBlackMethod INSTANCE = new SwaptionCashFixedIborBlackMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static SwaptionCashFixedIborBlackMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private SwaptionCashFixedIborBlackMethod() {
  }

  /**
   * The par rate sensitivity calculator.
   */
  private static final ParRateCurveSensitivityDiscountingCalculator PRCS = ParRateCurveSensitivityDiscountingCalculator.getInstance();
  /**
   * The par rate calculator.
   */
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();
  /**
   * The swap method.
   */
  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();

  /**
   * Computes the present value of a cash-settled European swaption in the Black model.
   * @param swaption The swaption.
   * @param curveBlack The curves with Black volatility data.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final SwaptionCashFixedIbor swaption, final BlackSwaptionFlatProviderInterface curveBlack) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(curveBlack, "Curves with Black volatility");
    ArgumentChecker.isTrue(curveBlack.getBlackParameters().getGeneratorSwap().getCurrency().equals(swaption.getCurrency()), "Black data currency should be equal to swaption currency");
    final double tenor = swaption.getMaturityTime();
    final double forward = swaption.getUnderlyingSwap().accept(PRDC, curveBlack.getMulticurveProvider());
    final double pvbp = METHOD_SWAP.getAnnuityCash(swaption.getUnderlyingSwap(), forward);
    // Implementation comment: cash-settled swaptions make sense only for constant strike, the computation of coupon equivalent is not required.
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final double volatility = curveBlack.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), tenor);
    final double discountFactorSettle = curveBlack.getMulticurveProvider().getDiscountFactor(swaption.getCurrency(), swaption.getSettlementTime());
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, discountFactorSettle * pvbp, volatility);
    final Function1D<BlackFunctionData, Double> func = blackFunction.getPriceFunction(swaption);
    final double price = func.evaluate(dataBlack) * (swaption.isLong() ? 1.0 : -1.0);
    return MultipleCurrencyAmount.of(swaption.getCurrency(), price);
  }

  /**
   * Computes the present value rate sensitivity to rates of a cash-settled European swaption in the Black model.
   * @param swaption The swaption.
   * @param curveBlack The curves with Black volatility data.
   * @return The present value curve sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final SwaptionCashFixedIbor swaption, final BlackSwaptionFlatProviderInterface curveBlack) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(curveBlack, "Curves with Black volatility");
    ArgumentChecker.isTrue(curveBlack.getBlackParameters().getGeneratorSwap().getCurrency().equals(swaption.getCurrency()), "Black data currency should be equal to swaption currency");
    final AnnuityCouponFixed annuityFixed = swaption.getUnderlyingSwap().getFixedLeg();
    final double tenor = swaption.getMaturityTime();
    final double forward = swaption.getUnderlyingSwap().accept(PRDC, curveBlack.getMulticurveProvider());
    // Derivative of the forward with respect to the rates.
    final MulticurveSensitivity forwardDr = swaption.getUnderlyingSwap().accept(PRCS, curveBlack.getMulticurveProvider());
    final double pvbp = METHOD_SWAP.getAnnuityCash(swaption.getUnderlyingSwap(), forward);
    // Derivative of the cash annuity with respect to the forward.
    final double pvbpDf = METHOD_SWAP.getAnnuityCashDerivative(swaption.getUnderlyingSwap(), forward);
    // Implementation note: strictly speaking, the strike equivalent is curve dependent; that dependency is ignored.
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final double volatility = curveBlack.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), tenor);
    final double discountFactorSettle = curveBlack.getMulticurveProvider().getDiscountFactor(swaption.getCurrency(), swaption.getSettlementTime());
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatility);
    final double[] bsAdjoint = blackFunction.getPriceAdjoint(swaption, dataBlack);
    final double sensiDF = -swaption.getSettlementTime() * discountFactorSettle * pvbp * bsAdjoint[0];
    final List<DoublesPair> list = new ArrayList<DoublesPair>();
    list.add(new DoublesPair(swaption.getSettlementTime(), sensiDF));
    final Map<String, List<DoublesPair>> resultMap = new HashMap<String, List<DoublesPair>>();
    resultMap.put(annuityFixed.getNthPayment(0).getFundingCurveName(), list);
    MulticurveSensitivity result = forwardDr.multipliedBy(discountFactorSettle * (pvbpDf * bsAdjoint[0] + pvbp * bsAdjoint[1]));
    if (!swaption.isLong()) {
      result = result.multipliedBy(-1);
    }
    return MultipleCurrencyMulticurveSensitivity.of(swaption.getCurrency(), result);
  }

  /**
   * Computes the present value sensitivity to the Black volatility (also called vega) of a cash-settled European swaption in the Black swaption model.
   * @param swaption The swaption.
   * @param curveBlack The curves with Black volatility data.
   * @return The present value Black sensitivity.
   */
  public PresentValueBlackSwaptionSensitivity presentValueBlackSensitivity(final SwaptionCashFixedIbor swaption, final BlackSwaptionFlatProviderInterface curveBlack) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(curveBlack, "Curves with Black volatility");
    ArgumentChecker.isTrue(curveBlack.getBlackParameters().getGeneratorSwap().getCurrency().equals(swaption.getCurrency()), "Black data currency should be equal to swaption currency");
    final double forward = swaption.getUnderlyingSwap().accept(PRDC, curveBlack.getMulticurveProvider());
    final double pvbp = METHOD_SWAP.getAnnuityCash(swaption.getUnderlyingSwap(), forward);
    final double discountFactorSettle = curveBlack.getMulticurveProvider().getDiscountFactor(swaption.getCurrency(), swaption.getSettlementTime());
    final DoublesPair point = new DoublesPair(swaption.getTimeToExpiry(), swaption.getMaturityTime());
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final double volatility = curveBlack.getBlackParameters().getVolatility(point);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatility);
    final double[] bsAdjoint = blackFunction.getPriceAdjoint(swaption, dataBlack);
    final Map<DoublesPair, Double> sensitivity = new HashMap<DoublesPair, Double>();
    sensitivity.put(point, bsAdjoint[2] * pvbp * discountFactorSettle * (swaption.isLong() ? 1.0 : -1.0));
    return new PresentValueBlackSwaptionSensitivity(sensitivity, curveBlack.getBlackParameters().getGeneratorSwap());
  }

}
