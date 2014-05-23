/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.cashflow;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.provider.IborForwardRateProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Calculates the projected payment for a coupon.
 */
public class CouponProjectedPaymentVisitor extends InstrumentDerivativeVisitorAdapter<MulticurveProviderInterface, CurrencyAmount> {
  /** The ibor forward rate provider */
  private static final IborForwardRateProvider IBOR_FORWARD_PROVIDER = IborForwardRateProvider.getInstance();

  @Override
  public CurrencyAmount visitCouponIbor(final CouponIbor coupon, final MulticurveProviderInterface curves) {
    final double forward = IBOR_FORWARD_PROVIDER.getRate(curves, coupon, coupon.getFixingPeriodStartTime(), coupon.getFixingPeriodEndTime(),
        coupon.getFixingAccrualFactor());
    return CurrencyAmount.of(coupon.getCurrency(), coupon.getNotional() * coupon.getPaymentYearFraction() * forward);
  }

  @Override
  public CurrencyAmount visitCouponIborSpread(final CouponIborSpread coupon, final MulticurveProviderInterface curves) {
    final double forward = IBOR_FORWARD_PROVIDER.getRate(curves, coupon, coupon.getFixingPeriodStartTime(), coupon.getFixingPeriodEndTime(),
        coupon.getFixingAccrualFactor());
    final double spread = coupon.getSpread();
    return CurrencyAmount.of(coupon.getCurrency(), coupon.getNotional() * coupon.getPaymentYearFraction() * (forward + spread));
  }

//  @Override
//  public CurrencyAmount visitCouponIborGearing(final CouponIborGearing coupon, final MulticurveProviderInterface curves) {
//    final double forward = IBOR_FORWARD_PROVIDER.getRate(curves, coupon, coupon.getFixingPeriodStartTime(), coupon.getFixingPeriodEndTime(),
//        coupon.getFixingAccrualFactor());
//    final double spread = coupon.getSpread();
//    return CurrencyAmount.of(coupon.getCurrency(), coupon.getNotional() * coupon.getPaymentYearFraction() * forward * spread);
//  }
//
}
