/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.multicurvecommodity.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.ForwardCommodityCashSettle;
import com.opengamma.analytics.financial.provider.description.commodity.CommodityProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.commodity.CommoditySensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.commodity.MultipleCurrencyCommoditySensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  Method to compute present value and present value sensitivity for cash settle commodity forward.
 */
public final class ForwardCommodityCashSettleSecurityForwardMethod {

  /**
   * The method unique instance.
   */
  private static final ForwardCommodityCashSettleSecurityForwardMethod INSTANCE = new ForwardCommodityCashSettleSecurityForwardMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static ForwardCommodityCashSettleSecurityForwardMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private ForwardCommodityCashSettleSecurityForwardMethod() {
  }

  /**
   * Compute the present value of a commodity cash settle forward by discounting.
   * @param forward The forward.
   * @param multicurve The commodity multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final ForwardCommodityCashSettle forward, final CommodityProviderInterface multicurve) {
    ArgumentChecker.notNull(forward, "Forward");
    ArgumentChecker.notNull(multicurve, "Multi-curves provider");
    final double fwd = multicurve.getForwardValue(forward.getUnderlying(), forward.getSettlementTime());
    final double df = multicurve.getDiscountFactor(forward.getCurrency(), forward.getPaymentTime());
    final double pv = forward.getNotional() * (fwd - forward.getRate()) * df;
    return MultipleCurrencyAmount.of(forward.getCurrency(), pv);
  }

  /**
   * Compute the present value sensitivity of a commodity cash settle forward by discounting.
   * @param forward The forward.
   * @param multicurve The commodity multi-curve provider.
   * @return The present value sensitivity.
   */
  public MultipleCurrencyCommoditySensitivity presentValueCurveSensitivity(final ForwardCommodityCashSettle forward, final CommodityProviderInterface multicurve) {
    ArgumentChecker.notNull(forward, "Forward");
    ArgumentChecker.notNull(multicurve, "Curves");
    final double fwd = multicurve.getForwardValue(forward.getUnderlying(), forward.getSettlementTime());
    final double df = multicurve.getDiscountFactor(forward.getCurrency(), forward.getPaymentTime());
    // Backward sweep
    final double pvBar = 1.0;
    final double forwardBar = forward.getNotional() * df * pvBar;
    final double dfBar = forward.getNotional() * (fwd - forward.getRate()) * pvBar;
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(forward.getPaymentTime(), -forward.getPaymentTime() * df * dfBar));
    mapDsc.put(multicurve.getName(forward.getCurrency()), listDiscounting);
    final Map<String, List<DoublesPair>> mapFwd = new HashMap<>();
    final List<DoublesPair> listForward = new ArrayList<>();
    listForward.add(DoublesPair.of(forward.getSettlementTime(), forwardBar));
    mapFwd.put(multicurve.getName(forward.getUnderlying()), listForward);
    return MultipleCurrencyCommoditySensitivity.of(forward.getCurrency(), CommoditySensitivity.of(mapDsc, mapFwd));
  }

  public MultipleCurrencyCommoditySensitivity presentValueSecondOrderCurveSensitivity(final ForwardCommodityCashSettle forward, final CommodityProviderInterface multicurve) {
    ArgumentChecker.notNull(forward, "Coupon");
    ArgumentChecker.notNull(multicurve, "Curves");
    final double fwd = multicurve.getForwardValue(forward.getUnderlying(), forward.getSettlementTime());
    final double df = multicurve.getDiscountFactor(forward.getCurrency(), forward.getPaymentTime());
    // Backward sweep
    final double pvBar = 1.0;
    final double forwardBar = forward.getNotional() * df * pvBar;
    final double dfBar = forward.getNotional() * (fwd - forward.getRate()) * pvBar;
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(forward.getPaymentTime(), forward.getPaymentTime() * forward.getPaymentTime() * df * dfBar));
    mapDsc.put(multicurve.getName(forward.getCurrency()), listDiscounting);
    final Map<String, List<DoublesPair>> mapFwd = new HashMap<>();
    final List<DoublesPair> listForward = new ArrayList<>();
    listForward.add(DoublesPair.of(forward.getPaymentTime(), -2. * forward.getPaymentTime() * forwardBar));
    mapFwd.put(multicurve.getName(forward.getUnderlying()), listForward);
    return MultipleCurrencyCommoditySensitivity.of(forward.getCurrency(), CommoditySensitivity.of(mapDsc, mapFwd));
  }

  /**
   * Computes the par spread (spread to be added to the fixed rate to have a present value of 0).
   * @param forward The forward.
   * @param multicurve The multi-curve provider.
   * @return The par spread.
   */
  public double parSpread(final ForwardCommodityCashSettle forward, final CommodityProviderInterface multicurve) {
    ArgumentChecker.notNull(forward, "forward");
    ArgumentChecker.notNull(multicurve, "Multiurves");
    final double fwd = multicurve.getForwardValue(forward.getUnderlying(), forward.getSettlementTime());
    return fwd - forward.getRate();
  }

}
