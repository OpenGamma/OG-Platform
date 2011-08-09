/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.inflation.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.inflation.derivatives.CouponInflationZeroCouponInterpolation;
import com.opengamma.financial.interestrate.market.MarketBundle;
import com.opengamma.financial.interestrate.method.PricingMarketMethod;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Pricing method for inflation zero-coupon. The price is computed by index estimation and discounting.
 */
public class CouponInflationZeroCouponInterpolationDiscountingMethod implements PricingMarketMethod {

  /**
   * Computes the present value of the zero-coupon coupon with reference index at start of the month.
   * @param coupon The zero-coupon payment.
   * @param market The market bundle.
   * @return The present value.
   */
  public CurrencyAmount presentValue(CouponInflationZeroCouponInterpolation coupon, MarketBundle market) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(market, "Market");
    double estimatedIndexMonth0 = market.getPriceIndex(coupon.getPriceIndex(), coupon.getReferenceEndTime()[0]);
    double estimatedIndexMonth1 = market.getPriceIndex(coupon.getPriceIndex(), coupon.getReferenceEndTime()[1]);
    double estimatedIndex = coupon.getWeight() * estimatedIndexMonth0 + (1 - coupon.getWeight()) * estimatedIndexMonth1;
    double discountFactor = market.getDiscountingFactor(coupon.getCurrency(), coupon.getPaymentTime());
    double pv = (estimatedIndex / coupon.getIndexStartValue() - 1) * discountFactor * coupon.getNotional();
    return CurrencyAmount.of(coupon.getCurrency(), pv);
  }

  @Override
  public CurrencyAmount presentValue(InterestRateDerivative instrument, MarketBundle market) {
    Validate.isTrue(instrument instanceof CouponInflationZeroCouponInterpolation, "Zero-coupon inflation with start of month reference date.");
    return presentValue((CouponInflationZeroCouponInterpolation) instrument, market);
  }

  // TODO: discounting curve sensitivity
  // TODO: price index curve sensitivity

}
