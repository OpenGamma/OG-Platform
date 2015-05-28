/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swap.provider;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.payments.derivative.*;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class CouponForwardRateVisitor extends InstrumentDerivativeVisitorAdapter<MulticurveProviderInterface, Double> {

  @Override
  public Double visitCouponIbor(final CouponIbor payment, final MulticurveProviderInterface curves) {
    return curves.getSimplyCompoundForwardRate(payment.getIndex(), payment.getFixingPeriodStartTime(), payment.getFixingPeriodEndTime(), payment.getFixingAccrualFactor());
  }

  @Override
  public Double visitCouponOIS(final CouponON payment, final MulticurveProviderInterface curves) {
    final double ratio = 1.0 + payment.getFixingPeriodAccrualFactor() *
        curves.getSimplyCompoundForwardRate(
            payment.getIndex(),
            payment.getFixingPeriodStartTime(),
            payment.getFixingPeriodEndTime(),
            payment.getFixingPeriodAccrualFactor()
        );
    if (payment.getCurrency().equals(Currency.BRL)) {
      return Math.pow(ratio, 1.0/payment.getFixingPeriodAccrualFactor()) - 1;
    }
    return (payment.getNotionalAccrued() * ratio - payment.getNotional()) / payment.getNotional() /payment.getPaymentYearFraction();
  }

  @Override
  public Double visitCouponIborSpread(final CouponIborSpread payment, final MulticurveProviderInterface curves) {
    return curves.getSimplyCompoundForwardRate(payment.getIndex(), payment.getFixingPeriodStartTime(), payment.getFixingPeriodEndTime(), payment.getFixingAccrualFactor());
  }

  @Override
  public Double visitCouponIborGearing(final CouponIborGearing payment, final MulticurveProviderInterface curves) {
    return curves.getSimplyCompoundForwardRate(payment.getIndex(), payment.getFixingPeriodStartTime(), payment.getFixingPeriodEndTime(), payment.getFixingAccrualFactor());
  }

  @Override
  public Double visitCouponFixed(final CouponFixed payment, final MulticurveProviderInterface curves) {
    return payment.getFixedRate();
  }

  @Override
  public Double visitCouponIborCompounding(final CouponIborCompounding payment, final MulticurveProviderInterface curves) {
    double notionalAccrued = payment.getNotionalAccrued();
    for (int i = 0; i < payment.getFixingPeriodAccrualFactors().length; i++) {
      final double forwardRate = curves.getSimplyCompoundForwardRate(
          payment.getIndex(),
          payment.getFixingPeriodStartTimes()[i],
          payment.getFixingPeriodEndTimes()[i],
          payment.getFixingPeriodAccrualFactors()[i]
      );
      final double ratioForward = (1.0 + payment.getPaymentAccrualFactors()[i] * forwardRate);
      notionalAccrued *= ratioForward;
    }
    return  (notionalAccrued - payment.getNotional()) / payment.getNotional() /payment.getPaymentYearFraction();
  }
}
