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
import com.opengamma.analytics.financial.interestrate.market.description.MultipleCurrencyCurveSensitivityMarket;
import com.opengamma.analytics.financial.interestrate.method.PricingMarketMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to compute present value and present value sensitivity for fixed coupon.
 */
public final class CouponFixedDiscountingMarketMethod implements PricingMarketMethod {

  /**
   * The method unique instance.
   */
  private static final CouponFixedDiscountingMarketMethod INSTANCE = new CouponFixedDiscountingMarketMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponFixedDiscountingMarketMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponFixedDiscountingMarketMethod() {
  }

  /**
   * Compute the present value of a Fixed coupon by discounting.
   * @param coupon The coupon.
   * @param market The market with the curves.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final CouponFixed coupon, final IMarketBundle market) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(market, "Market");
    final double df = market.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    final double value = coupon.getAmount() * df;
    return MultipleCurrencyAmount.of(coupon.getCurrency(), value);
  }

  @Override
  public MultipleCurrencyAmount presentValue(final InstrumentDerivative instrument, final IMarketBundle market) {
    Validate.isTrue(instrument instanceof CouponFixed, "Coupon Fixed");
    return presentValue((CouponFixed) instrument, market);
  }

  /**
   * Computes the present value curve sensitivity of a fixed coupon by discounting.
   * @param cpn The coupon.
   * @param market The market with the curves.
   * @return The sensitivity.
   */
  public MultipleCurrencyCurveSensitivityMarket presentValueMarketSensitivity(CouponFixed cpn, final IMarketBundle market) {
    final double time = cpn.getPaymentTime();
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<String, List<DoublesPair>>();
    final DoublesPair s = new DoublesPair(time, -time * cpn.getAmount() * market.getDiscountFactor(cpn.getCurrency(), time));
    final List<DoublesPair> list = new ArrayList<DoublesPair>();
    list.add(s);
    mapDsc.put(market.getName(cpn.getCurrency()), list);
    MultipleCurrencyCurveSensitivityMarket result = new MultipleCurrencyCurveSensitivityMarket();
    result = result.plus(cpn.getCurrency(), CurveSensitivityMarket.ofYieldDiscounting(mapDsc));
    return result;
  }

}
