/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableForward;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositZero;
import com.opengamma.analytics.financial.interestrate.fra.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFuture;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponOIS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the payment amounts due on the valuation date (|time to payment|<small). 
 */
public final class TodayPaymentCalculator extends AbstractInstrumentDerivativeVisitor<Object, MultipleCurrencyAmount> {
  /**
   * The default time limit below which the payment is consider as being today.
   */
  private static final double DEFAULT_TIME_LIMIT_TODAY = 0.002;
  /**
   * The method unique instance.
   */
  private static final TodayPaymentCalculator INSTANCE = new TodayPaymentCalculator(DEFAULT_TIME_LIMIT_TODAY);

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static TodayPaymentCalculator getInstance() {
    return INSTANCE;
  }

  public static TodayPaymentCalculator getInstance(final double timeLimit) {
    return new TodayPaymentCalculator(timeLimit);
  }

  private final double _timeLimit;

  /**
   * Constructor.
   */
  private TodayPaymentCalculator(final double timeLimit) {
    ArgumentChecker.isTrue(timeLimit > 0, "Time limit must be greater than zero; have {}", timeLimit);
    _timeLimit = timeLimit;
  }

  @Override
  public MultipleCurrencyAmount visit(final InstrumentDerivative derivative) {
    ArgumentChecker.notNull(derivative, "derivative");
    return derivative.accept(this);
  }

  @Override
  public MultipleCurrencyAmount visitCash(final Cash deposit) {
    ArgumentChecker.notNull(deposit, "instrument");
    MultipleCurrencyAmount cash = MultipleCurrencyAmount.of(deposit.getCurrency(), 0.0);
    if (deposit.getStartTime() < _timeLimit) {
      cash = cash.plus(deposit.getCurrency(), -deposit.getInitialAmount());
    }
    if (deposit.getEndTime() < _timeLimit) {
      cash = cash.plus(deposit.getCurrency(), deposit.getNotional() + deposit.getInterestAmount());
    }
    return cash;
  }

  @Override
  public MultipleCurrencyAmount visitDepositZero(final DepositZero deposit) {
    ArgumentChecker.notNull(deposit, "instrument");
    MultipleCurrencyAmount cash = MultipleCurrencyAmount.of(deposit.getCurrency(), 0.0);
    if (deposit.getStartTime() < _timeLimit) {
      cash = cash.plus(deposit.getCurrency(), -deposit.getInitialAmount());
    }
    if (deposit.getEndTime() < _timeLimit) {
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
  public MultipleCurrencyAmount visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction futureOption) {
    ArgumentChecker.notNull(futureOption, "instrument");
    return MultipleCurrencyAmount.of(futureOption.getUnderlyingOption().getCurrency(), 0.0);
  }

  @Override
  public MultipleCurrencyAmount visitCouponFixed(final CouponFixed payment) {
    ArgumentChecker.notNull(payment, "instrument");
    MultipleCurrencyAmount cash = MultipleCurrencyAmount.of(payment.getCurrency(), 0.0);
    if (payment.getPaymentTime() < _timeLimit) {
      cash = cash.plus(payment.getCurrency(), payment.getAmount());
    }
    return cash;
  }

  @Override
  public MultipleCurrencyAmount visitFixedPayment(final PaymentFixed payment) {
    ArgumentChecker.notNull(payment, "instrument");
    MultipleCurrencyAmount cash = MultipleCurrencyAmount.of(payment.getCurrency(), 0.0);
    if (payment.getPaymentTime() < _timeLimit) {
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
    final MultipleCurrencyAmount cash = visit(swap.getFirstLeg());
    return cash.plus(visit(swap.getSecondLeg()));
  }

  @Override
  public MultipleCurrencyAmount visitFixedCouponSwap(final SwapFixedCoupon<?> swap) {
    return visitSwap(swap);
  }

  @Override
  public MultipleCurrencyAmount visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption) {
    ArgumentChecker.notNull(swaption, "instrument");
    return MultipleCurrencyAmount.of(swaption.getCurrency(), 0.0);
  }

  @Override
  public MultipleCurrencyAmount visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption) {
    ArgumentChecker.notNull(swaption, "instrument");
    return MultipleCurrencyAmount.of(swaption.getCurrency(), 0.0);
  }

  @Override
  public MultipleCurrencyAmount visitForex(final Forex forex) {
    ArgumentChecker.notNull(forex, "instrument");
    return visitFixedPayment(forex.getPaymentCurrency1()).plus(visitFixedPayment(forex.getPaymentCurrency2()));
  }

  @Override
  public MultipleCurrencyAmount visitForexSwap(final ForexSwap forex) {
    ArgumentChecker.notNull(forex, "instrument");
    return visitForex(forex.getNearLeg()).plus(visitForex(forex.getFarLeg()));
  }

  @Override
  public MultipleCurrencyAmount visitForexOptionVanilla(final ForexOptionVanilla forex) {
    ArgumentChecker.notNull(forex, "instrument");
    return MultipleCurrencyAmount.of(forex.getCurrency1(), 0.0);
  }

  @Override
  public MultipleCurrencyAmount visitForexOptionSingleBarrier(final ForexOptionSingleBarrier forex) {
    ArgumentChecker.notNull(forex, "instrument");
    return MultipleCurrencyAmount.of(forex.getCurrency1(), 0.0);
  }

  @Override
  public MultipleCurrencyAmount visitForexOptionDigital(final ForexOptionDigital forex) {
    ArgumentChecker.notNull(forex, "instrument");
    return MultipleCurrencyAmount.of(forex.getCurrency1(), 0.0);
  }

  @Override
  public MultipleCurrencyAmount visitForexNonDeliverableForward(final ForexNonDeliverableForward forex) {
    ArgumentChecker.notNull(forex, "instrument");
    return MultipleCurrencyAmount.of(forex.getCurrency1(), 0.0);
  }

}
