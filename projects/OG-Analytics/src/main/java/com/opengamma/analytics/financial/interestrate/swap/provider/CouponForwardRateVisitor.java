/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swap.provider;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverage;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;

/**
 * Computes the forward rate associated to different types of instruments.
 */
public class CouponForwardRateVisitor extends InstrumentDerivativeVisitorAdapter<MulticurveProviderInterface, Double> {

  @Override
  public Double visitCouponIbor(final CouponIbor payment, final MulticurveProviderInterface curves) {
    return curves.getSimplyCompoundForwardRate(payment.getIndex(), payment.getFixingPeriodStartTime(), 
        payment.getFixingPeriodEndTime(), payment.getFixingAccrualFactor());
  }

  @Override
  public Double visitCouponIborSpread(final CouponIborSpread payment, final MulticurveProviderInterface curves) {
    return curves.getSimplyCompoundForwardRate(payment.getIndex(), payment.getFixingPeriodStartTime(), 
        payment.getFixingPeriodEndTime(), payment.getFixingAccrualFactor());
  }

  @Override
  public Double visitCouponIborGearing(final CouponIborGearing payment, final MulticurveProviderInterface curves) {
    return curves.getSimplyCompoundForwardRate(payment.getIndex(), payment.getFixingPeriodStartTime(), 
        payment.getFixingPeriodEndTime(), payment.getFixingAccrualFactor());
  }

  @Override
  public Double visitCouponIborAverage(final CouponIborAverage payment, final MulticurveProviderInterface multicurve) {
    double forward1 = multicurve.getSimplyCompoundForwardRate(payment.getIndex1(), payment.getFixingPeriodStartTime1(), 
        payment.getFixingPeriodEndTime1(), payment.getFixingAccrualFactor1());
    double forward2 = multicurve.getSimplyCompoundForwardRate(payment.getIndex2(), payment.getFixingPeriodStartTime2(), 
        payment.getFixingPeriodEndTime2(), payment.getFixingAccrualFactor2());
    double forward = payment.getWeight1() * forward1 + payment.getWeight2() * forward2;
    return forward;
  }

  @Override
  public Double visitForwardRateAgreement(ForwardRateAgreement payment, MulticurveProviderInterface curves) {
    return curves.getSimplyCompoundForwardRate(payment.getIndex(), payment.getFixingPeriodStartTime(), 
        payment.getFixingPeriodEndTime(), payment.getFixingYearFraction());
  }
}
