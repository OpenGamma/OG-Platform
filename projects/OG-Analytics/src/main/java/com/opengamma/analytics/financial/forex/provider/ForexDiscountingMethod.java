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

import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.interestrate.payments.provider.PaymentFixedDiscountingMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Pricing method for Forex transactions (spot or forward) by discounting each payment.
 */
public final class ForexDiscountingMethod {

  /**
   * The method unique instance.
   */
  private static final ForexDiscountingMethod INSTANCE = new ForexDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static ForexDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private ForexDiscountingMethod() {
  }

  /**
   * Fixed payments method.
   */
  private static final PaymentFixedDiscountingMethod METHOD_PAY = PaymentFixedDiscountingMethod.getInstance();

  /**
   * Computes the present value by discounting each payment in its own currency.
   * @param fx The Forex derivative.
   * @param multicurves The multi-curves provider.
   * @return The multi-currency present value.
   */
  public MultipleCurrencyAmount presentValue(final Forex fx, final MulticurveProviderInterface multicurves) {
    final MultipleCurrencyAmount pv1 = METHOD_PAY.presentValue(fx.getPaymentCurrency1(), multicurves);
    final MultipleCurrencyAmount pv2 = METHOD_PAY.presentValue(fx.getPaymentCurrency2(), multicurves);
    return pv1.plus(pv2);
  }

  /**
   * Computes the currency exposure by discounting each payment in its own currency.
   * @param fx The Forex derivative.
   * @param multicurves The multi-curves provider.
   * @return The multi-currency present value.
   */
  public MultipleCurrencyAmount currencyExposure(final Forex fx, final MulticurveProviderInterface multicurves) {
    return presentValue(fx, multicurves);
  }

  /**
   * The par spread is the spread that should be added to the forex forward points to have a zero value.
   * @param fx The forex swap.
   * @param multicurves The multi-curves provider.
   * @return The spread.
   */
  public double parSpread(final Forex fx, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(fx, "Forex");
    ArgumentChecker.notNull(multicurves, "Multi-curves provider");
    final Currency ccy2 = fx.getCurrency2();
    final double pv2 = multicurves.getFxRates().convert(presentValue(fx, multicurves), ccy2).getAmount();
    final double dfEnd = multicurves.getDiscountFactor(fx.getCurrency2(), fx.getPaymentTime());
    final double notional1 = fx.getPaymentCurrency1().getAmount();
    return pv2 / (notional1 * dfEnd);
  }

  /**
   * Computes the forward exchange rate associated to the Forex instrument (1 Cyy1 = fwd Cyy2).
   * @param fx The Forex derivative.
   * @param multicurves The multi-curve provider.
   * @return The forward rate.
   */
  public double forwardForexRate(final Forex fx, final MulticurveProviderInterface multicurves) {
    final double dfDomestic = multicurves.getDiscountFactor(fx.getCurrency2(), fx.getPaymentTime());
    final double dfForeign = multicurves.getDiscountFactor(fx.getCurrency1(), fx.getPaymentTime());
    final double spot = multicurves.getFxRate(fx.getCurrency1(), fx.getCurrency2());
    return spot * dfForeign / dfDomestic;
  }

  /**
   * Compute the present value sensitivity to rates of a forex transaction.
   * @param fx The Forex transaction.
   * @param multicurves The multi-curve provider.
   * @return The sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final Forex fx, final MulticurveProviderInterface multicurves) {
    final MultipleCurrencyMulticurveSensitivity pvcs1 = METHOD_PAY.presentValueCurveSensitivity(fx.getPaymentCurrency1(), multicurves);
    final MultipleCurrencyMulticurveSensitivity pvcs2 = METHOD_PAY.presentValueCurveSensitivity(fx.getPaymentCurrency2(), multicurves);
    return pvcs1.plus(pvcs2);
  }

  /**
   * Computes the par spread curve sensitivity.
   * @param fx The forex swap.
   * @param multicurves The multi-curves provider.
   * @return The par spread sensitivity.
   */
  public MulticurveSensitivity parSpreadCurveSensitivity(final Forex fx, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(fx, "Forex");
    ArgumentChecker.notNull(multicurves, "Multi-curves provider");
    final Currency ccy2 = fx.getCurrency2();
    final double payTime = fx.getPaymentTime();
    final double pv2 = multicurves.getFxRates().convert(presentValue(fx, multicurves), ccy2).getAmount();
    final double dfEnd = multicurves.getDiscountFactor(fx.getCurrency2(), fx.getPaymentTime());
    final double notional1 = fx.getPaymentCurrency1().getAmount();
    // Backward sweep
    final double spreadBar = 1.0;
    final double dfEndBar = -pv2 / (notional1 * dfEnd * dfEnd) * spreadBar;
    final double pv2Bar = spreadBar / (notional1 * dfEnd);
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
