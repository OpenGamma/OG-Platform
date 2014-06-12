/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.analytics.financial.interestrate.inflation.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearInterpolation;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.InflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.MultipleCurrencyInflationSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Pricing method for inflation Year on Year. The price is computed by index estimation and discounting.
 */
public class CouponInflationYearOnYearInterpolationDiscountingMethod {

  /**
   * Computes the net amount of the Year on Year coupon with reference index at start of the month.
   * @param coupon The zero-coupon payment.
   * @param inflation The inflation provider.
   * @return The net amount.
   */

  public MultipleCurrencyAmount netAmount(final CouponInflationYearOnYearInterpolation coupon, final InflationProviderInterface inflation) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(inflation, "Inflation");
    final double estimatedIndexStart = indexEstimationStart(coupon, inflation);
    final double estimatedIndexEnd = indexEstimationEnd(coupon, inflation);
    final double na = (estimatedIndexEnd / estimatedIndexStart - (coupon.payNotional() ? 0.0 : 1.0)) * coupon.getNotional();
    return MultipleCurrencyAmount.of(coupon.getCurrency(), na);
  }

  /**
   * Computes the present value of the Year on Year coupon without convexity adjustment.
   * @param coupon The zero-coupon payment.
   * @param inflation The inflation provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final CouponInflationYearOnYearInterpolation coupon, final InflationProviderInterface inflation) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(inflation, "Inflation");
    final double discountFactor = inflation.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    return netAmount(coupon, inflation).multipliedBy(discountFactor);
  }

  /**
   * Computes the estimated index with the weight and the reference start date.
   * @param coupon The zero-coupon payment.
   * @param inflation The inflation provider.
   * @return The estimated index for the reference start date.
   */
  public double indexEstimationStart(final CouponInflationYearOnYearInterpolation coupon, final InflationProviderInterface inflation) {
    final double estimatedIndexMonth0 = inflation.getPriceIndex(coupon.getPriceIndex(), coupon.getReferenceStartTime()[0]);
    final double estimatedIndexMonth1 = inflation.getPriceIndex(coupon.getPriceIndex(), coupon.getReferenceStartTime()[1]);
    return coupon.getWeightStart() * estimatedIndexMonth0 + (1 - coupon.getWeightStart()) * estimatedIndexMonth1;

  }

  /**
   * Computes the estimated index with the weight and the reference end date.
   * @param coupon The zero-coupon payment.
   * @param inflation The inflation provider.
   * @return The estimated index for the reference end date.
   */
  public double indexEstimationEnd(final CouponInflationYearOnYearInterpolation coupon, final InflationProviderInterface inflation) {
    final double estimatedIndexMonth0 = inflation.getPriceIndex(coupon.getPriceIndex(), coupon.getReferenceEndTime()[0]);
    final double estimatedIndexMonth1 = inflation.getPriceIndex(coupon.getPriceIndex(), coupon.getReferenceEndTime()[1]);
    return coupon.getWeightEnd() * estimatedIndexMonth0 + (1 - coupon.getWeightEnd()) * estimatedIndexMonth1;
  }

  /**
   * Compute the present value sensitivity to rates of a Inflation coupon.
   * @param coupon The coupon.
   * @param inflation The inflation provider.
   * @return The present value sensitivity.
   */
  public MultipleCurrencyInflationSensitivity presentValueCurveSensitivity(final CouponInflationYearOnYearInterpolation coupon, final InflationProviderInterface inflation) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(inflation, "Inflation");
    final double estimatedIndexStartMonth0 = inflation.getPriceIndex(coupon.getPriceIndex(), coupon.getReferenceStartTime()[0]);
    final double estimatedIndexStartMonth1 = inflation.getPriceIndex(coupon.getPriceIndex(), coupon.getReferenceStartTime()[1]);
    final double estimatedIndexEndMonth0 = inflation.getPriceIndex(coupon.getPriceIndex(), coupon.getReferenceEndTime()[0]);
    final double estimatedIndexEndMonth1 = inflation.getPriceIndex(coupon.getPriceIndex(), coupon.getReferenceEndTime()[1]);
    final double estimatedIndexStart = coupon.getWeightStart() * estimatedIndexStartMonth0 + (1 - coupon.getWeightStart()) * estimatedIndexStartMonth1;
    final double estimatedIndexEnd = coupon.getWeightEnd() * estimatedIndexEndMonth0 + (1 - coupon.getWeightEnd()) * estimatedIndexEndMonth1;
    final double discountFactor = inflation.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    // Backward sweep
    final double pvBar = 1.0;
    final double discountFactorBar = (estimatedIndexEnd / estimatedIndexStart - (coupon.payNotional() ? 0.0 : 1.0)) * coupon.getNotional() * pvBar;
    final double estimatedIndexEndBar = 1.0 / estimatedIndexStart * discountFactor * coupon.getNotional() * pvBar;
    final double estimatedIndexStartBar = -estimatedIndexEnd / (estimatedIndexStart * estimatedIndexStart) * discountFactor * coupon.getNotional() * pvBar;
    final double estimatedIndexEndMonth1bar = (1 - coupon.getWeightEnd()) * estimatedIndexEndBar;
    final double estimatedIndexEndMonth0bar = coupon.getWeightEnd() * estimatedIndexEndBar;
    final double estimatedIndexStartMonth1bar = (1 - coupon.getWeightStart()) * estimatedIndexStartBar;
    final double estimatedIndexStartMonth0bar = coupon.getWeightStart() * estimatedIndexStartBar;
    final Map<String, List<DoublesPair>> resultMapDisc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(coupon.getPaymentTime(), -coupon.getPaymentTime() * discountFactor * discountFactorBar));
    resultMapDisc.put(inflation.getName(coupon.getCurrency()), listDiscounting);
    final Map<String, List<DoublesPair>> resultMapPrice = new HashMap<>();
    final List<DoublesPair> listPrice = new ArrayList<>();
    listPrice.add(DoublesPair.of(coupon.getReferenceEndTime()[0], estimatedIndexEndMonth0bar));
    listPrice.add(DoublesPair.of(coupon.getReferenceEndTime()[1], estimatedIndexEndMonth1bar));
    listPrice.add(DoublesPair.of(coupon.getReferenceStartTime()[0], estimatedIndexStartMonth0bar));
    listPrice.add(DoublesPair.of(coupon.getReferenceStartTime()[1], estimatedIndexStartMonth1bar));
    resultMapPrice.put(inflation.getName(coupon.getPriceIndex()), listPrice);
    final InflationSensitivity inflationSensitivity = InflationSensitivity.ofYieldDiscountingAndPriceIndex(resultMapDisc, resultMapPrice);
    return MultipleCurrencyInflationSensitivity.of(coupon.getCurrency(), inflationSensitivity);
  }

}
