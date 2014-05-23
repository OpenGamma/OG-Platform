/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swap.provider;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;

/**
 *
 */
public class CouponForwardRateVisitor extends InstrumentDerivativeVisitorAdapter<MulticurveProviderInterface, Double> {

  @Override
  public Double visitCouponIbor(final CouponIbor payment, final MulticurveProviderInterface curves) {
    return curves.getSimplyCompoundForwardRate(payment.getIndex(), payment.getFixingPeriodStartTime(), payment.getFixingPeriodEndTime(), payment.getFixingAccrualFactor());
  }

  @Override
  public Double visitCouponIborSpread(final CouponIborSpread payment, final MulticurveProviderInterface curves) {
    return curves.getSimplyCompoundForwardRate(payment.getIndex(), payment.getFixingPeriodStartTime(), payment.getFixingPeriodEndTime(), payment.getFixingAccrualFactor());
  }

  @Override
  public Double visitCouponIborGearing(final CouponIborGearing payment, final MulticurveProviderInterface curves) {
    return curves.getSimplyCompoundForwardRate(payment.getIndex(), payment.getFixingPeriodStartTime(), payment.getFixingPeriodEndTime(), payment.getFixingAccrualFactor());
  }

}
