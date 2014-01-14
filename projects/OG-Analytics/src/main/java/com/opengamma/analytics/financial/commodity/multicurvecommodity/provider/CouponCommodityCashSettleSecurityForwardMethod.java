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

import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.CouponCommodityCashSettle;
import com.opengamma.analytics.financial.provider.description.commodity.CommodityProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.commodity.CommoditySensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.commodity.MultipleCurrencyCommoditySensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to compute present value and present value sensitivity for cash settle commodity coupon.
 */
public final class CouponCommodityCashSettleSecurityForwardMethod {

  /**
   * The method unique instance.
   */
  private static final CouponCommodityCashSettleSecurityForwardMethod INSTANCE = new CouponCommodityCashSettleSecurityForwardMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponCommodityCashSettleSecurityForwardMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponCommodityCashSettleSecurityForwardMethod() {
  }

  /**
   * Compute the present value of a commodity cash settle coupon by discounting.
   * @param coupon The coupon.
   * @param multicurve The commodity multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final CouponCommodityCashSettle coupon, final CommodityProviderInterface multicurve) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurve, "Multi-curves provider");
    final double forward = multicurve.getForwardValue(coupon.getUnderlying(), coupon.getSettlementTime());
    final double df = multicurve.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    final double pv = coupon.getNotional() * forward * df;
    return MultipleCurrencyAmount.of(coupon.getCurrency(), pv);
  }

  /**
   * Compute the present value sensitivity  of a commodity cash settle coupon by discounting.
   * @param coupon The coupon.
   * @param multicurve The commodity multi-curve provider.
   * @return The present value sensitivity.
   */
  public MultipleCurrencyCommoditySensitivity presentValueCurveSensitivity(final CouponCommodityCashSettle coupon, final CommodityProviderInterface multicurve) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurve, "Curves");
    final double forward = multicurve.getForwardValue(coupon.getUnderlying(), coupon.getSettlementTime());
    final double df = multicurve.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    // Backward sweep
    final double pvBar = 1.0;
    final double forwardBar = coupon.getNotional() * df * pvBar;
    final double dfBar = coupon.getNotional() * forward * pvBar;
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(coupon.getPaymentTime(), -coupon.getPaymentTime() * df * dfBar));
    mapDsc.put(multicurve.getName(coupon.getCurrency()), listDiscounting);
    final Map<String, List<DoublesPair>> mapFwd = new HashMap<>();
    final List<DoublesPair> listForward = new ArrayList<>();
    listForward.add(DoublesPair.of(coupon.getSettlementTime(), forwardBar));
    mapFwd.put(multicurve.getName(coupon.getUnderlying()), listForward);
    return MultipleCurrencyCommoditySensitivity.of(coupon.getCurrency(), CommoditySensitivity.of(mapDsc, mapFwd));
  }

  public MultipleCurrencyCommoditySensitivity presentValueSecondOrderCurveSensitivity(final CouponCommodityCashSettle coupon, final CommodityProviderInterface multicurve) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurve, "Curves");
    final double forward = multicurve.getForwardValue(coupon.getUnderlying(), coupon.getSettlementTime());
    final double df = multicurve.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    // Backward sweep
    final double pvBar = 1.0;
    final double forwardBar = coupon.getNotional() * df * pvBar;
    final double dfBar = coupon.getNotional() * forward * pvBar;
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(coupon.getPaymentTime(), coupon.getPaymentTime() * coupon.getPaymentTime() * df * dfBar));
    mapDsc.put(multicurve.getName(coupon.getCurrency()), listDiscounting);
    final Map<String, List<DoublesPair>> mapFwd = new HashMap<>();
    final List<DoublesPair> listForward = new ArrayList<>();
    listForward.add(DoublesPair.of(coupon.getPaymentTime(), -2. * coupon.getPaymentTime() * forwardBar));
    mapFwd.put(multicurve.getName(coupon.getUnderlying()), listForward);
    return MultipleCurrencyCommoditySensitivity.of(coupon.getCurrency(), CommoditySensitivity.of(mapDsc, mapFwd));
  }

}
