/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.generic;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositIbor;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableTransaction;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingFlatSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverageSpreadSimplified;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONCompounded;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapMultileg;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculator of the last time (in years from now) referenced in the instrument description and associated to a specific Ibor index.
 */
public class LastFixingTimeIndexCalculator extends InstrumentDerivativeVisitorAdapter<Object, Double> {

  /** The Ibor index for which the last time should be computed. */
  private final IborIndex _index;

  /**
   * Constructor of the last time calculator.
   * @param index The index for which the last time should be computed.
   */
  public LastFixingTimeIndexCalculator(IborIndex index) {
    ArgumentChecker.notNull(index, "Ibor index");
    _index = index;
  }

  // =====     Deposit     =====

  @Override
  public Double visitDepositIbor(final DepositIbor deposit) {
    double time = 0;
    if (deposit.getIndex().equals(_index)) {
      time = deposit.getEndTime();
    }
    return time;
  }

  // =====     Coupon     ===== 

  @Override
  public Double visitCouponFixed(final CouponFixed payment) {
    return 0.0;
  }

  @Override
  public Double visitCouponONCompounded(final CouponONCompounded payment) {
    return 0.0;
  }

  @Override
  public Double visitCouponOIS(final CouponON payment) {
    return 0.0;
  }

  @Override
  public Double visitCouponONSpread(final CouponONSpread payment) {
    return 0.0;
  }

  @Override
  public Double visitCouponONArithmeticAverageSpreadSimplified(final CouponONArithmeticAverageSpreadSimplified payment) {
    return 0.0;
  }

  @Override
  public Double visitCouponIbor(final CouponIbor payment) {
    double time = 0;
    if (payment.getIndex().equals(_index)) {
      time = payment.getFixingPeriodEndTime();
    }
    return time;
  }

  @Override
  public Double visitCouponIborSpread(final CouponIborSpread payment) {
    double time = 0;
    if (payment.getIndex().equals(_index)) {
      time = payment.getFixingPeriodEndTime();
    }
    return time;
  }

  @Override
  public Double visitCouponIborCompounding(final CouponIborCompounding payment) {
    double time = 0;
    if (payment.getIndex().equals(_index)) {
      time = payment.getFixingPeriodEndTimes()[payment.getFixingPeriodEndTimes().length - 1];
    }
    return time;
  }

  @Override
  public Double visitCouponIborCompoundingSpread(final CouponIborCompoundingSpread payment) {
    double time = 0;
    if (payment.getIndex().equals(_index)) {
      time = payment.getFixingPeriodEndTimes()[payment.getFixingPeriodEndTimes().length - 1];
    }
    return time;
  }

  @Override
  public Double visitCouponIborCompoundingFlatSpread(final CouponIborCompoundingFlatSpread payment) {
    double time = 0;
    if (payment.getIndex().equals(_index)) {
      time = payment.getFixingPeriodEndTimes()[payment.getFixingPeriodEndTimes().length - 1];
    }
    return time;
  }

  @Override
  public Double visitForwardRateAgreement(final ForwardRateAgreement fra) {
    double time = 0;
    if (fra.getIndex().equals(_index)) {
      time = fra.getFixingPeriodEndTime();
    }
    return time;
  }

  // =====     Annuity     ===== 

  @Override
  public Double visitGenericAnnuity(final Annuity<? extends Payment> annuity) {
    return annuity.getNthPayment(annuity.getNumberOfPayments() - 1).accept(this);
  }

  @Override
  public Double visitFixedCouponAnnuity(final AnnuityCouponFixed annuity) {
    return visitGenericAnnuity(annuity);
  }

  // =====     Swap     ===== 

  @Override
  public Double visitSwap(final Swap<?, ?> swap) {
    final double a = swap.getFirstLeg().accept(this);
    final double b = swap.getSecondLeg().accept(this);
    return Math.max(a, b);
  }

  @Override
  public Double visitFixedCouponSwap(final SwapFixedCoupon<?> swap) {
    return visitSwap(swap);
  }

  @Override
  public Double visitSwapMultileg(final SwapMultileg swap) {
    double timeMax = swap.getLegs()[0].accept(this);
    for (int looleg = 1; looleg < swap.getLegs().length; looleg++) {
      timeMax = Math.max(timeMax, swap.getLegs()[0].accept(this));
    }
    return timeMax;
  }

  // =====     Futures     ===== 

  @Override
  public Double visitSwapFuturesPriceDeliverableTransaction(final SwapFuturesPriceDeliverableTransaction future) {
    return visitSwap(future.getUnderlyingSecurity().getUnderlyingSwap());
  }

}
