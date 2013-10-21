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

import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableForward;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Pricing method for Forex non-deliverable forward transactions by discounting.
 */
public final class ForexNonDeliverableForwardDiscountingMethod {

  /**
   * The method unique instance.
   */
  private static final ForexNonDeliverableForwardDiscountingMethod INSTANCE = new ForexNonDeliverableForwardDiscountingMethod();

  /**
   * Private constructor.
   */
  private ForexNonDeliverableForwardDiscountingMethod() {
  }

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static ForexNonDeliverableForwardDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Computes the present value of the non-deliverable forward. The present value is in currency2 and equal to P*N*(1-X/F) where
   * <i>P</i> is the currency2 discount factor for the payment date, <i>N</i> is the notional, <i>X</i> is NDF rate and <i>F</i> the estimated forward exchange rate at the payment date.
   * @param ndf The non-deliverable forward.
   * @param multicurves The multi-curves provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final ForexNonDeliverableForward ndf, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(ndf, "Non deliverable forward");
    ArgumentChecker.notNull(multicurves, "Multi-curves provider");
    final double df2 = multicurves.getDiscountFactor(ndf.getCurrency2(), ndf.getPaymentTime());
    final double df1 = multicurves.getDiscountFactor(ndf.getCurrency1(), ndf.getPaymentTime());
    final double spot = multicurves.getFxRate(ndf.getCurrency2(), ndf.getCurrency1());
    final double pv2 = ndf.getNotionalCurrency2() * (df2 - ndf.getExchangeRate() / spot * df1);
    return MultipleCurrencyAmount.of(ndf.getCurrency2(), pv2);
  }

  /**
   * Computes the currency exposure of the non-deliverable forward. The currency exposure is P_2 * N in currency2 and -P_1 * N * X in currency1
   * where <i>P_2</i> is the currency2 discount factor for the payment date, <i>N</i> is the notional, <i>P_1</i> is the currency1 discount factor for the payment date
   * and <i>X</i> is NDF rate.
   * @param ndf The non-deliverable forward.
   * @param multicurves The multi-curves provider.
   * @return The currency exposure.
   */
  public MultipleCurrencyAmount currencyExposure(final ForexNonDeliverableForward ndf, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(ndf, "Non deliverable forward");
    ArgumentChecker.notNull(multicurves, "Multi-curves provider");
    final double df2 = multicurves.getDiscountFactor(ndf.getCurrency2(), ndf.getPaymentTime());
    final double df1 = multicurves.getDiscountFactor(ndf.getCurrency1(), ndf.getPaymentTime());
    final double pv1 = -ndf.getNotionalCurrency2() * ndf.getExchangeRate() * df1;
    final double pv2 = ndf.getNotionalCurrency2() * df2;
    return MultipleCurrencyAmount.of(new Currency[] {ndf.getCurrency1(), ndf.getCurrency2()}, new double[] {pv1, pv2});
  }

  /**
   * Computes the forward exchange rate associated to the NDF (1 Cyy2 = fwd Cyy1).
   * @param ndf The non-deliverable forward.
   * @param multicurves The multi-curves provider.
   * @return The forward rate.
   */
  public double forwardForexRate(final ForexNonDeliverableForward ndf, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(ndf, "Non deliverable forward");
    ArgumentChecker.notNull(multicurves, "Multi-curves provider");
    final double dfDelivery = multicurves.getDiscountFactor(ndf.getCurrency2(), ndf.getPaymentTime());
    final double dfNonDelivery = multicurves.getDiscountFactor(ndf.getCurrency1(), ndf.getPaymentTime());
    final double spot = multicurves.getFxRate(ndf.getCurrency2(), ndf.getCurrency1());
    return spot * dfDelivery / dfNonDelivery;
  }

  /**
   * The present value curve sensitivity for the non-deliverable forward.
   * @param ndf The non-deliverable forward.
   * @param multicurves The multi-curves provider.
   * @return The present value currency exposure.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final ForexNonDeliverableForward ndf, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(ndf, "Non deliverable forward");
    ArgumentChecker.notNull(multicurves, "Multi-curves provider");
    final double df2 = multicurves.getDiscountFactor(ndf.getCurrency2(), ndf.getPaymentTime());
    final double df1 = multicurves.getDiscountFactor(ndf.getCurrency1(), ndf.getPaymentTime());
    final double spot = multicurves.getFxRate(ndf.getCurrency2(), ndf.getCurrency1());
    // Backward sweep
    final double pvBar = 1.0;
    final double df1Bar = -ndf.getNotionalCurrency2() * ndf.getExchangeRate() / spot * pvBar;
    final double df2Bar = ndf.getNotionalCurrency2() * pvBar;
    final Map<String, List<DoublesPair>> resultMap = new HashMap<>();
    final List<DoublesPair> listDiscounting1 = new ArrayList<>();
    listDiscounting1.add(DoublesPair.of(ndf.getPaymentTime(), -ndf.getPaymentTime() * df1 * df1Bar));
    resultMap.put(multicurves.getName(ndf.getCurrency1()), listDiscounting1);
    final List<DoublesPair> listDiscounting2 = new ArrayList<>();
    listDiscounting2.add(DoublesPair.of(ndf.getPaymentTime(), -ndf.getPaymentTime() * df2 * df2Bar));
    resultMap.put(multicurves.getName(ndf.getCurrency2()), listDiscounting2);
    final MulticurveSensitivity result = MulticurveSensitivity.ofYieldDiscounting(resultMap);
    return MultipleCurrencyMulticurveSensitivity.of(ndf.getCurrency2(), result);
  }

}
