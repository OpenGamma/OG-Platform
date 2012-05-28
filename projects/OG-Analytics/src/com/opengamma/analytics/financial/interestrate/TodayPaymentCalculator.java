/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositZero;
import com.opengamma.analytics.financial.interestrate.fra.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFuture;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponOIS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the payment amounts due on the valuation date (|time to payment|<small). 
 */
public class TodayPaymentCalculator extends AbstractInstrumentDerivativeVisitor<Object, MultipleCurrencyAmount> {

  /**
   * The method unique instance.
   */
  private static final TodayPaymentCalculator INSTANCE = new TodayPaymentCalculator();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static TodayPaymentCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  TodayPaymentCalculator() {
  }

  /**
   * The default time limit below which the payment is consider as being today.
   */
  private static final double DEFAULT_TIME_LIMIT_TODAY = 0.002;

  @Override
  public MultipleCurrencyAmount visit(final InstrumentDerivative derivative) {
    Validate.notNull(derivative);
    return derivative.accept(this);
  }

  @Override
  public MultipleCurrencyAmount visitCash(final Cash deposit) {
    ArgumentChecker.notNull(deposit, "instrument");
    MultipleCurrencyAmount cash = MultipleCurrencyAmount.of(deposit.getCurrency(), 0.0);
    if (deposit.getStartTime() < DEFAULT_TIME_LIMIT_TODAY) {
      cash = cash.plus(deposit.getCurrency(), -deposit.getInitialAmount());
    }
    if (deposit.getEndTime() < DEFAULT_TIME_LIMIT_TODAY) {
      cash = cash.plus(deposit.getCurrency(), deposit.getNotional() + deposit.getInterestAmount());
    }
    return cash;
  }

  @Override
  public MultipleCurrencyAmount visitDepositZero(final DepositZero deposit) {
    ArgumentChecker.notNull(deposit, "instrument");
    MultipleCurrencyAmount cash = MultipleCurrencyAmount.of(deposit.getCurrency(), 0.0);
    if (deposit.getStartTime() < DEFAULT_TIME_LIMIT_TODAY) {
      cash = cash.plus(deposit.getCurrency(), -deposit.getInitialAmount());
    }
    if (deposit.getEndTime() < DEFAULT_TIME_LIMIT_TODAY) {
      cash = cash.plus(deposit.getCurrency(), deposit.getNotional() + deposit.getInterestAmount());
    }
    return cash;
  }

  @Override
  public MultipleCurrencyAmount visitForwardRateAgreement(final ForwardRateAgreement fra) {
    ArgumentChecker.notNull(fra, "instrument");
    return MultipleCurrencyAmount.of(fra.getCurrency(), 0.0);
  }

  @Override
  public MultipleCurrencyAmount visitInterestRateFuture(final InterestRateFuture future) {
    ArgumentChecker.notNull(future, "instrument");
    return MultipleCurrencyAmount.of(future.getCurrency(), 0.0);
  }

  @Override
  public MultipleCurrencyAmount visitCouponFixed(final CouponFixed payment) {
    ArgumentChecker.notNull(payment, "instrument");
    MultipleCurrencyAmount cash = MultipleCurrencyAmount.of(payment.getCurrency(), 0.0);
    if (payment.getPaymentTime() < DEFAULT_TIME_LIMIT_TODAY) {
      cash = cash.plus(payment.getCurrency(), payment.getAmount());
    }
    return cash;
  }

  @Override
  public MultipleCurrencyAmount visitFixedPayment(final PaymentFixed payment) {
    ArgumentChecker.notNull(payment, "instrument");
    MultipleCurrencyAmount cash = MultipleCurrencyAmount.of(payment.getCurrency(), 0.0);
    if (payment.getPaymentTime() < DEFAULT_TIME_LIMIT_TODAY) {
      cash = cash.plus(payment.getCurrency(), payment.getAmount());
    }
    return cash;
  }

  @Override
  public MultipleCurrencyAmount visitCouponIbor(final CouponIbor payment) {
    ArgumentChecker.notNull(payment, "instrument");
    return MultipleCurrencyAmount.of(payment.getCurrency(), 0.0);
  }

  @Override
  public MultipleCurrencyAmount visitCouponIborSpread(final CouponIborSpread payment) {
    ArgumentChecker.notNull(payment, "instrument");
    return MultipleCurrencyAmount.of(payment.getCurrency(), 0.0);
  }

  @Override
  public MultipleCurrencyAmount visitCouponIborGearing(final CouponIborGearing payment) {
    ArgumentChecker.notNull(payment, "instrument");
    return MultipleCurrencyAmount.of(payment.getCurrency(), 0.0);
  }

  @Override
  public MultipleCurrencyAmount visitCouponOIS(final CouponOIS payment) {
    ArgumentChecker.notNull(payment, "instrument");
    return MultipleCurrencyAmount.of(payment.getCurrency(), 0.0);
  }

  @Override
  public MultipleCurrencyAmount visitGenericAnnuity(final Annuity<? extends Payment> annuity) {
    ArgumentChecker.notNull(annuity, "instrument");
    MultipleCurrencyAmount pv = MultipleCurrencyAmount.of(annuity.getCurrency(), 0.0);
    for (final Payment p : annuity.getPayments()) {
      pv = pv.plus(visit(p));
    }
    return pv;
  }

  @Override
  public MultipleCurrencyAmount visitFixedCouponAnnuity(final AnnuityCouponFixed annuity) {
    return visitGenericAnnuity(annuity);
  }

  @Override
  public MultipleCurrencyAmount visitSwap(final Swap<?, ?> swap) {
    ArgumentChecker.notNull(swap, "instrument");
    MultipleCurrencyAmount cash = visit(swap.getFirstLeg());
    return cash.plus(visit(swap.getSecondLeg()));
  }

  @Override
  public MultipleCurrencyAmount visitFixedCouponSwap(final SwapFixedCoupon<?> swap) {
    return visitSwap(swap);
  }

}
