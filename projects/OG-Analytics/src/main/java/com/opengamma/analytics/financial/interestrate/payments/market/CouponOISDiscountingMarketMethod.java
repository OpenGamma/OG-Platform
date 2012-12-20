/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.market;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.market.description.CurveSensitivityMarket;
import com.opengamma.analytics.financial.interestrate.market.description.IMarketBundle;
import com.opengamma.analytics.financial.interestrate.market.description.MarketForwardSensitivity;
import com.opengamma.analytics.financial.interestrate.market.description.MultipleCurrencyCurveSensitivityMarket;
import com.opengamma.analytics.financial.interestrate.method.PricingMarketMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponOIS;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to compute present value and its sensitivities for OIS coupons.
 */
public final class CouponOISDiscountingMarketMethod implements PricingMarketMethod {

  /**
   * The method unique instance.
   */
  private static final CouponOISDiscountingMarketMethod INSTANCE = new CouponOISDiscountingMarketMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponOISDiscountingMarketMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponOISDiscountingMarketMethod() {
  }

  /**
   * Computes the present value.
   * @param coupon The coupon.
   * @param market The market curves.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final CouponOIS coupon, final IMarketBundle market) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(market, "Market");
    final double ratio = 1.0 + coupon.getFixingPeriodAccrualFactor()
        * market.getForwardRate(coupon.getIndex(), coupon.getFixingPeriodStartTime(), coupon.getFixingPeriodEndTime(), coupon.getFixingPeriodAccrualFactor());
    final double df = market.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    final double pv = (coupon.getNotionalAccrued() * ratio - coupon.getNotional()) * df;
    return MultipleCurrencyAmount.of(coupon.getCurrency(), pv);
  }

  @Override
  public MultipleCurrencyAmount presentValue(final InstrumentDerivative instrument, final IMarketBundle market) {
    Validate.isTrue(instrument instanceof CouponOIS, "Coupon OIS");
    return presentValue((CouponOIS) instrument, market);
  }

  /**
   * Compute the present value sensitivity to rates of a OIS coupon by discounting.
   * @param coupon The coupon.
   * @param market The market curves.
   * @return The present value curve sensitivities.
   */
  public MultipleCurrencyCurveSensitivityMarket presentValueMarketSensitivity(final CouponOIS coupon, final IMarketBundle market) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(market, "Market");
    final double df = market.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    final double forward = market.getForwardRate(coupon.getIndex(), coupon.getFixingPeriodStartTime(), coupon.getFixingPeriodEndTime(),
        coupon.getFixingPeriodAccrualFactor());
    final double ratio = 1.0 + coupon.getFixingPeriodAccrualFactor() * forward;
    // Backward sweep
    final double pvBar = 1.0;
    final double ratioBar = coupon.getNotionalAccrued() * df * pvBar;
    final double forwardBar = coupon.getFixingPeriodAccrualFactor() * ratioBar;
    final double dfBar = (coupon.getNotionalAccrued() * ratio - coupon.getNotional()) * pvBar;
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<String, List<DoublesPair>>();
    final List<DoublesPair> listDiscounting = new ArrayList<DoublesPair>();
    listDiscounting.add(new DoublesPair(coupon.getPaymentTime(), -coupon.getPaymentTime() * df * dfBar));
    mapDsc.put(market.getName(coupon.getCurrency()), listDiscounting);
    final Map<String, List<MarketForwardSensitivity>> mapFwd = new HashMap<String, List<MarketForwardSensitivity>>();
    final List<MarketForwardSensitivity> listForward = new ArrayList<MarketForwardSensitivity>();
    listForward.add(new MarketForwardSensitivity(coupon.getFixingPeriodStartTime(), coupon.getFixingPeriodEndTime(), coupon.getFixingPeriodAccrualFactor(), forwardBar));
    mapFwd.put(market.getName(coupon.getIndex()), listForward);
    final MultipleCurrencyCurveSensitivityMarket result = MultipleCurrencyCurveSensitivityMarket.of(coupon.getCurrency(),
        CurveSensitivityMarket.ofYieldDiscountingAndForward(mapDsc, mapFwd));
    return result;
  }

  /**
   * Computes the par rate, i.e. the fair rate for the remaining period. 
   * @param coupon The coupon.
   * @param market The market curves.
   * @return The par rate.
   */
  public double parRate(final CouponOIS coupon, final IMarketBundle market) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(market, "Market");
    return market.getForwardRate(coupon.getIndex(), coupon.getFixingPeriodStartTime(), coupon.getFixingPeriodEndTime(), coupon.getFixingPeriodAccrualFactor());
  }

  /**
   * Computes the par rate sensitivity to the curve rates.
   * @param coupon The coupon.
   * @param market The market curves.
   * @return The sensitivities.
   */
  public MultipleCurrencyCurveSensitivityMarket parRateCurveSensitivity(final CouponOIS coupon, final IMarketBundle market) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(market, "Market");
    // Backward sweep.
    final double forwardBar = 1.0;
    final Map<String, List<MarketForwardSensitivity>> mapFwd = new HashMap<String, List<MarketForwardSensitivity>>();
    final List<MarketForwardSensitivity> listForward = new ArrayList<MarketForwardSensitivity>();
    listForward.add(new MarketForwardSensitivity(coupon.getFixingPeriodStartTime(), coupon.getFixingPeriodEndTime(), coupon.getFixingPeriodAccrualFactor(), forwardBar));
    mapFwd.put(market.getName(coupon.getIndex()), listForward);
    final MultipleCurrencyCurveSensitivityMarket result = MultipleCurrencyCurveSensitivityMarket.of(coupon.getCurrency(), CurveSensitivityMarket.ofForward(mapFwd));
    return result;
  }

}
