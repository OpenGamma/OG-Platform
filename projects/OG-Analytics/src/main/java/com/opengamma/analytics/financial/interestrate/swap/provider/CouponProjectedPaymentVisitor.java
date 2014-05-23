/*
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swap.provider;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingFlatSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverageSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONCompounded;
import com.opengamma.analytics.financial.interestrate.payments.provider.IborForwardRateProvider;
import com.opengamma.analytics.financial.interestrate.payments.provider.OvernightForwardRateProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *
 */
public class CouponProjectedPaymentVisitor extends InstrumentDerivativeVisitorAdapter<MulticurveProviderInterface, CurrencyAmount> {

  @Override
  public CurrencyAmount visitCouponIbor(final CouponIbor payment, final MulticurveProviderInterface curves) {
    final double forward = curves.getSimplyCompoundForwardRate(payment.getIndex(), payment.getFixingPeriodStartTime(), payment.getFixingPeriodEndTime(), payment.getFixingAccrualFactor());
    return CurrencyAmount.of(payment.getCurrency(), payment.getNotional() * payment.getPaymentYearFraction() * forward);
  }

  @Override
  public CurrencyAmount visitCouponIborSpread(final CouponIborSpread payment, final MulticurveProviderInterface curves) {
    final double forward = curves.getSimplyCompoundForwardRate(payment.getIndex(), payment.getFixingPeriodStartTime(), payment.getFixingPeriodEndTime(), payment.getFixingAccrualFactor());
    return CurrencyAmount.of(payment.getCurrency(), payment.getNotional() * payment.getPaymentYearFraction() * forward);
  }

  @Override
  public CurrencyAmount visitCouponIborGearing(final CouponIborGearing payment, final MulticurveProviderInterface curves) {
    final double forward = curves.getSimplyCompoundForwardRate(payment.getIndex(), payment.getFixingPeriodStartTime(), payment.getFixingPeriodEndTime(), payment.getFixingAccrualFactor());
    return CurrencyAmount.of(payment.getCurrency(), payment.getNotional() * payment.getPaymentYearFraction() * forward);
  }

  @Override
  public CurrencyAmount visitCouponIborCompoundingFlatSpread(final CouponIborCompoundingFlatSpread coupon, final MulticurveProviderInterface curves) {
    final int nbSubPeriod = coupon.getFixingTimes().length;
    double cpaAccumulated = coupon.getCompoundingPeriodAmountAccumulated();
    for (int loopsub = 0; loopsub < nbSubPeriod; loopsub++) {
      final double forward = IborForwardRateProvider.getInstance().getRate(curves, coupon,
          coupon.getFixingSubperiodsStartTimes()[loopsub],
          coupon.getFixingSubperiodsEndTimes()[loopsub],
          coupon.getFixingSubperiodsAccrualFactors()[loopsub]);
      cpaAccumulated += cpaAccumulated * forward * coupon.getSubperiodsAccrualFactors()[loopsub]; // Additional Compounding Period Amount
      cpaAccumulated += coupon.getNotional() * (forward + coupon.getSpread()) * coupon.getSubperiodsAccrualFactors()[loopsub]; // Basic Compounding Period Amount
    }
    return CurrencyAmount.of(coupon.getCurrency(), cpaAccumulated);
  }


  @Override
  public CurrencyAmount visitCouponONArithmeticAverageSpread(final CouponONArithmeticAverageSpread payment, final MulticurveProviderInterface curves) {
    final double[] delta = payment.getFixingPeriodAccrualFactors();
    final double[] times = payment.getFixingPeriodTimes();
    final int nbFwd = delta.length;
    final double[] forwardON = new double[nbFwd];
    double rateAccrued = payment.getRateAccrued();
    for (int loopfwd = 0; loopfwd < nbFwd; loopfwd++) {
      forwardON[loopfwd] = curves.getSimplyCompoundForwardRate(payment.getIndex(), times[loopfwd], times[loopfwd + 1], delta[loopfwd]);
      rateAccrued += forwardON[loopfwd] * delta[loopfwd];
    }
    // Does not use the payment accrual factor.
    return CurrencyAmount.of(payment.getCurrency(), (rateAccrued * payment.getNotional() + payment.getSpreadAmount()));
  }

  @Override
  public CurrencyAmount visitCouponOIS(final CouponON payment, final MulticurveProviderInterface curves) {
    final double ratio = 1.0 + payment.getFixingPeriodAccrualFactor()
        * OvernightForwardRateProvider.getInstance().getRate(curves, payment, payment.getFixingPeriodStartTime(),
        payment.getFixingPeriodEndTime(), payment.getFixingPeriodAccrualFactor());
    return CurrencyAmount.of(payment.getCurrency(), payment.getNotionalAccrued() * ratio - payment.getNotional());
  }

  @Override
  public CurrencyAmount visitCouponONCompounded(final CouponONCompounded payment, final MulticurveProviderInterface curves) {
    double ratio = 1.0;
    for (int i = 0; i < payment.getFixingPeriodAccrualFactors().length; i++) {
      ratio *= Math.pow(
          1 + curves.getAnnuallyCompoundForwardRate(payment.getIndex(), payment.getFixingPeriodStartTimes()[i],
          payment.getFixingPeriodEndTimes()[i], payment.getFixingPeriodAccrualFactors()[i]), payment.getFixingPeriodAccrualFactors()[i]);
    }
    return CurrencyAmount.of(payment.getCurrency(), payment.getNotionalAccrued() * ratio);
  }

  @Override
  public CurrencyAmount visitCouponFixedCompounding(final CouponFixedCompounding payment, final MulticurveProviderInterface curves) {
    return CurrencyAmount.of(payment.getCurrency(), (payment.getNotionalAccrued() - payment.getNotional()));
  }

}
