/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Pricing method for Forex swap transactions by discounting each payment.
 */
public final class ForexSwapDiscountingMethod {

  /**
   * The method unique instance.
   */
  private static final ForexSwapDiscountingMethod INSTANCE = new ForexSwapDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static ForexSwapDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private ForexSwapDiscountingMethod() {
  }

  /**
   * Forex method by discounting.
   */
  private static final ForexDiscountingMethod METHOD_FX = ForexDiscountingMethod.getInstance();

  /**
   * Compute the present value by discounting the payments in their own currency.
   * @param fx The Forex swap.
   * @param multicurves The multi-curves provider.
   * @return The multi-currency present value.
   */
  public MultipleCurrencyAmount presentValue(final ForexSwap fx, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(fx, "Forex swap");
    ArgumentChecker.notNull(multicurves, "Multi-curves provider");
    final MultipleCurrencyAmount pv = METHOD_FX.presentValue(fx.getNearLeg(), multicurves);
    return pv.plus(METHOD_FX.presentValue(fx.getFarLeg(), multicurves));
  }

  // TODO: do we need this method as it is the same as present value
  public MultipleCurrencyAmount currencyExposure(final ForexSwap fx, final MulticurveProviderInterface multicurves) {
    return presentValue(fx, multicurves);
  }

  /**
   * The par spread is the spread that should be added to the forex forward points to have a zero value.
   * @param fx The forex swap.
   * @param multicurves The multi-curves provider.
   * @return The spread.
   */
  public double parSpread(final ForexSwap fx, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(fx, "Forex swap");
    ArgumentChecker.notNull(multicurves, "Multi-curves provider");
    final Currency ccy2 = fx.getNearLeg().getCurrency2();
    final double pv2 = multicurves.getFxRates().convert(presentValue(fx, multicurves), ccy2).getAmount();
    final double dfEnd = multicurves.getDiscountFactor(fx.getFarLeg().getCurrency2(), fx.getFarLeg().getPaymentTime());
    final double notional1 = fx.getNearLeg().getPaymentCurrency1().getAmount();
    return -pv2 / (notional1 * dfEnd);
  }

  /**
   * Compute the present value sensitivity to rates of a forex swap transaction.
   * @param fx The forex swap transaction.
   * @param multicurves The multi-curves provider.
   * @return The sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final ForexSwap fx, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(fx, "Forex swap");
    MultipleCurrencyMulticurveSensitivity result = METHOD_FX.presentValueCurveSensitivity(fx.getNearLeg(), multicurves);
    result = result.plus(METHOD_FX.presentValueCurveSensitivity(fx.getFarLeg(), multicurves));
    return result;
  }

  /**
   * Computes the par spread curve sensitivity.
   * @param fx The forex swap.
   * @param multicurves The multi-curves provider.
   * @return The par spread sensitivity.
   */
  public MulticurveSensitivity parSpreadCurveSensitivity(final ForexSwap fx, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(fx, "Forex swap");
    ArgumentChecker.notNull(multicurves, "Multi-curves provider");
    final Currency ccy2 = fx.getNearLeg().getCurrency2();
    //    final String name2 = fx.getFarLeg().getPaymentCurrency2().getFundingCurveName();
    final double payTime = fx.getFarLeg().getPaymentTime();
    final double pv2 = multicurves.getFxRates().convert(presentValue(fx, multicurves), ccy2).getAmount();
    final double dfEnd = multicurves.getDiscountFactor(fx.getFarLeg().getCurrency2(), fx.getFarLeg().getPaymentTime());
    final double notional1 = fx.getNearLeg().getPaymentCurrency1().getAmount();
    //    double spread = -pv2 / (notional1 * dfEnd);
    // Backward sweep
    final double spreadBar = 1.0;
    final double dfEndBar = pv2 / (notional1 * dfEnd * dfEnd) * spreadBar;
    final double pv2Bar = -spreadBar / (notional1 * dfEnd);
    final MultipleCurrencyMulticurveSensitivity pv2DrMC = presentValueCurveSensitivity(fx, multicurves);
    final MulticurveSensitivity pv2Dr = pv2DrMC.converted(ccy2, multicurves.getFxRates()).getSensitivity(ccy2);
    final List<DoublesPair> list = new ArrayList<>();
    list.add(DoublesPair.of(payTime, -payTime * dfEnd * dfEndBar));
    final Map<String, List<DoublesPair>> result = new HashMap<>();
    result.put(multicurves.getName(ccy2), list);
    final MulticurveSensitivity dfEndDr = MulticurveSensitivity.ofYieldDiscounting(result);
    return pv2Dr.multipliedBy(pv2Bar).plus(dfEndDr);
  }

}
