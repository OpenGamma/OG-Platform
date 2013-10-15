/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.inflation.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponInterpolationGearing;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.InflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.MultipleCurrencyInflationSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Pricing method for inflation zero-coupon. The price is computed by index estimation and discounting.
 */
public class CouponInflationZeroCouponInterpolationGearingDiscountingMethod {

  /**
   * Computes the present value of the zero-coupon coupon with reference index at start of the month.
   * @param coupon The zero-coupon payment.
   * @param inflation The inflation provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final CouponInflationZeroCouponInterpolationGearing coupon, final InflationProviderInterface inflation) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(inflation, "Inflation");
    final double estimatedIndex = indexEstimation(coupon, inflation);
    final double discountFactor = inflation.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    final double pv = coupon.getFactor() * (estimatedIndex / coupon.getIndexStartValue() - (coupon.payNotional() ? 0.0 : 1.0)) * discountFactor * coupon.getNotional();
    return MultipleCurrencyAmount.of(coupon.getCurrency(), pv);
  }

  /**
   * Computes the net amount of the zero-coupon coupon with reference index at start of the month.
   * @param coupon The zero-coupon payment.
   * @param inflation The inflation provider.
   * @return The net amount.
   */
  public MultipleCurrencyAmount netAmount(final CouponInflationZeroCouponInterpolationGearing coupon, final InflationProviderInterface inflation) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(inflation, "Inflation");
    final double estimatedIndex = indexEstimation(coupon, inflation);
    final double netAmount = coupon.getFactor() * (estimatedIndex / coupon.getIndexStartValue() - (coupon.payNotional() ? 0.0 : 1.0)) * coupon.getNotional();
    return MultipleCurrencyAmount.of(coupon.getCurrency(), netAmount);
  }

  /**
   * Computes the estimated index with the weight and the two reference end dates.
   * @param coupon The zero-coupon payment.
   * @param inflation The inflation provider.
   * @return The net amount.
   */
  public double indexEstimation(final CouponInflationZeroCouponInterpolationGearing coupon, final InflationProviderInterface inflation) {
    final double estimatedIndexMonth0 = inflation.getPriceIndex(coupon.getPriceIndex(), coupon.getReferenceEndTime()[0]);
    final double estimatedIndexMonth1 = inflation.getPriceIndex(coupon.getPriceIndex(), coupon.getReferenceEndTime()[1]);
    return coupon.getWeight() * estimatedIndexMonth0 + (1 - coupon.getWeight()) * estimatedIndexMonth1;
  }

  /**
   * Compute the present value sensitivity to rates of a Inflation coupon.
   * @param coupon The coupon.
   * @param inflation The inflation provider.
   * @return The present value sensitivity.
   */
  public MultipleCurrencyInflationSensitivity presentValueCurveSensitivity(final CouponInflationZeroCouponInterpolationGearing coupon, final InflationProviderInterface inflation) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(inflation, "Inflation");
    final double estimatedIndexMonth0 = inflation.getPriceIndex(coupon.getPriceIndex(), coupon.getReferenceEndTime()[0]);
    final double estimatedIndexMonth1 = inflation.getPriceIndex(coupon.getPriceIndex(), coupon.getReferenceEndTime()[1]);
    final double estimatedIndex = coupon.getWeight() * estimatedIndexMonth0 + (1 - coupon.getWeight()) * estimatedIndexMonth1;
    final double discountFactor = inflation.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    // Backward sweep
    final double pvBar = 1.0;
    final double discountFactorBar = coupon.getFactor() * (estimatedIndex / coupon.getIndexStartValue() - (coupon.payNotional() ? 0.0 : 1.0)) * coupon.getNotional() * pvBar;
    final double estimatedIndexBar = coupon.getFactor() / coupon.getIndexStartValue() * discountFactor * coupon.getNotional() * pvBar;
    final double estimatedIndexMonth1Bar = (1 - coupon.getWeight()) * estimatedIndexBar;
    final double estimatedIndexMonth0Bar = coupon.getWeight() * estimatedIndexBar;
    final Map<String, List<DoublesPair>> resultMapDisc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(coupon.getPaymentTime(), -coupon.getPaymentTime() * discountFactor * discountFactorBar));
    resultMapDisc.put(inflation.getName(coupon.getCurrency()), listDiscounting);
    final Map<String, List<DoublesPair>> resultMapPrice = new HashMap<>();
    final List<DoublesPair> listPrice = new ArrayList<>();
    listPrice.add(DoublesPair.of(coupon.getReferenceEndTime()[0], estimatedIndexMonth0Bar));
    listPrice.add(DoublesPair.of(coupon.getReferenceEndTime()[1], estimatedIndexMonth1Bar));
    resultMapPrice.put(inflation.getName(coupon.getPriceIndex()), listPrice);
    final InflationSensitivity inflationSensitivity = InflationSensitivity.ofYieldDiscountingAndPriceIndex(resultMapDisc, resultMapPrice);
    return MultipleCurrencyInflationSensitivity.of(coupon.getCurrency(), inflationSensitivity);
  }

}
