/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.market;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.market.MarketBundle;
import com.opengamma.analytics.financial.interestrate.method.PricingMarketMethod;
import com.opengamma.analytics.financial.interestrate.payments.CouponFixed;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Method to compute present value and present value sensitivity for fixed coupon.
 */
public class CouponFixedDiscountingMarketMethod implements PricingMarketMethod {

  public CurrencyAmount presentValue(CouponFixed coupon, MarketBundle market) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(market, "Market");
    final double df = market.getDiscountingFactor(coupon.getCurrency(), coupon.getPaymentTime());
    final double value = coupon.getAmount() * df;
    return CurrencyAmount.of(coupon.getCurrency(), value);
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final MarketBundle market) {
    Validate.isTrue(instrument instanceof CouponFixed, "Coupon Fixed");
    return presentValue((CouponFixed) instrument, market);
  }

}
