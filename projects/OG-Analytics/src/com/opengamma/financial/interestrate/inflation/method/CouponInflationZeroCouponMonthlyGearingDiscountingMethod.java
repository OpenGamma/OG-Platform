/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.inflation.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.inflation.derivatives.CouponInflationZeroCouponMonthlyGearing;
import com.opengamma.financial.interestrate.market.MarketBundle;
import com.opengamma.financial.interestrate.market.PresentValueCurveSensitivityMarket;
import com.opengamma.financial.interestrate.method.PricingMarketMethod;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Pricing method for inflation zero-coupon. The price is computed by index estimation and discounting.
 */
public class CouponInflationZeroCouponMonthlyGearingDiscountingMethod implements PricingMarketMethod {

  /**
   * Computes the present value of the zero-coupon coupon with reference index at start of the month and multiplicative factor.
   * @param coupon The zero-coupon payment.
   * @param market The market bundle.
   * @return The present value.
   */
  public CurrencyAmount presentValue(CouponInflationZeroCouponMonthlyGearing coupon, MarketBundle market) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(market, "Market");
    double estimatedIndex = market.getPriceIndex(coupon.getPriceIndex(), coupon.getReferenceEndTime());
    double discountFactor = market.getDiscountingFactor(coupon.getCurrency(), coupon.getPaymentTime());
    double pv = coupon.getFactor() * (estimatedIndex / coupon.getIndexStartValue() - (coupon.payNotional() ? 0.0 : 1.0)) * discountFactor * coupon.getNotional();
    return CurrencyAmount.of(coupon.getCurrency(), pv);
  }

  @Override
  public CurrencyAmount presentValue(InstrumentDerivative instrument, MarketBundle market) {
    Validate.isTrue(instrument instanceof CouponInflationZeroCouponMonthlyGearing, "Zero-coupon inflation with start of month reference date.");
    return presentValue((CouponInflationZeroCouponMonthlyGearing) instrument, market);
  }

  /**
   * Compute the present value sensitivity to rates of a Inflation coupon.
   * @param coupon The coupon.
   * @param market The market curves.
   * @return The present value sensitivity.
   */
  public PresentValueCurveSensitivityMarket presentValueCurveSensitivity(final CouponInflationZeroCouponMonthlyGearing coupon, final MarketBundle market) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(market, "Market");
    double estimatedIndex = market.getPriceIndex(coupon.getPriceIndex(), coupon.getReferenceEndTime());
    double discountFactor = market.getDiscountingFactor(coupon.getCurrency(), coupon.getPaymentTime());
    // Backward sweep
    final double pvBar = 1.0;
    double discountFactorBar = coupon.getFactor() * (estimatedIndex / coupon.getIndexStartValue() - (coupon.payNotional() ? 0.0 : 1.0)) * coupon.getNotional() * pvBar;
    double estimatedIndexBar = coupon.getFactor() / coupon.getIndexStartValue() * discountFactor * coupon.getNotional() * pvBar;
    final Map<String, List<DoublesPair>> resultMapDisc = new HashMap<String, List<DoublesPair>>();
    final List<DoublesPair> listDiscounting = new ArrayList<DoublesPair>();
    listDiscounting.add(new DoublesPair(coupon.getPaymentTime(), -coupon.getPaymentTime() * discountFactor * discountFactorBar));
    resultMapDisc.put(market.getCurve(coupon.getCurrency()).getCurve().getName(), listDiscounting);
    final Map<String, List<DoublesPair>> resultMapPrice = new HashMap<String, List<DoublesPair>>();
    final List<DoublesPair> listPrice = new ArrayList<DoublesPair>();
    listPrice.add(new DoublesPair(coupon.getReferenceEndTime(), estimatedIndexBar));
    resultMapPrice.put(market.getCurve(coupon.getPriceIndex()).getCurve().getName(), listPrice);
    final PresentValueCurveSensitivityMarket result = new PresentValueCurveSensitivityMarket(resultMapDisc, resultMapPrice);
    return result;
  }

}
