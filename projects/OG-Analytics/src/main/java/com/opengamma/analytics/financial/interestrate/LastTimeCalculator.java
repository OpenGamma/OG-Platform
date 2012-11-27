/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborTransaction;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositCounterpart;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositIbor;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositZero;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFuture;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponOIS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;

/**
 * Get the last time (in years from now) referenced in the instrument description.
 */
public final class LastTimeCalculator extends InstrumentDerivativeVisitorAdapter<Object, Double> {
  private static final LastTimeCalculator CALCULATOR = new LastTimeCalculator();

  public static LastTimeCalculator getInstance() {
    return CALCULATOR;
  }

  private LastTimeCalculator() {
  }

  @Override
  public Double visitCash(final Cash cash) {
    return cash.getEndTime();
  }

  @Override
  public Double visitDepositIbor(final DepositIbor deposit) {
    return deposit.getEndTime();
  }

  @Override
  public Double visitDepositCounterpart(final DepositCounterpart deposit) {
    return deposit.getEndTime();
  }

  @Override
  public Double visitFixedCouponSwap(final SwapFixedCoupon<?> swap) {
    return visitSwap(swap);
  }

  @Override
  public Double visitForwardRateAgreement(final ForwardRateAgreement fra) {
    return fra.getFixingPeriodEndTime();
  }

  @Override
  public Double visitInterestRateFuture(final InterestRateFuture future) {
    return future.getFixingPeriodEndTime();
  }

  @Override
  public Double visitFixedPayment(final PaymentFixed payment) {
    return payment.getPaymentTime();
  }

  @Override
  public Double visitCouponIbor(final CouponIbor payment) {
    return Math.max(payment.getFixingPeriodEndTime(), payment.getPaymentTime());
  }

  @Override
  public Double visitCouponIborSpread(final CouponIborSpread payment) {
    return Math.max(payment.getFixingPeriodEndTime(), payment.getPaymentTime());
  }

  @Override
  public Double visitGenericAnnuity(final Annuity<? extends Payment> annuity) {
    return annuity.getNthPayment(annuity.getNumberOfPayments() - 1).accept(this);
  }

  @Override
  public Double visitSwap(final Swap<?, ?> swap) {
    final double a = swap.getFirstLeg().accept(this);
    final double b = swap.getSecondLeg().accept(this);
    return Math.max(a, b);
  }

  @Override
  public Double visitFixedCouponAnnuity(final AnnuityCouponFixed annuity) {
    return visitGenericAnnuity(annuity);
  }

  @Override
  public Double visitCouponFixed(final CouponFixed payment) {
    return payment.getPaymentTime();
  }

  @Override
  public Double visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption) {
    return visitSwap(swaption.getUnderlyingSwap());
  }

  @Override
  public Double visitCouponCMS(final CouponCMS payment) {
    final double swapLastTime = payment.getUnderlyingSwap().accept(this);
    final double paymentTime = payment.getPaymentTime();
    return Math.max(swapLastTime, paymentTime);
  }

  @Override
  public Double visitCouponOIS(final CouponOIS payment) {
    return payment.getPaymentTime();
  }

  @Override
  public Double visitDepositZero(final DepositZero deposit) {
    return deposit.getEndTime();
  }

  // -----     Bond     -----

  @Override
  public Double visitBondFixedSecurity(final BondFixedSecurity bond) {
    return Math.max(bond.getCoupon().accept(this), bond.getNominal().accept(this));
  }

  @Override
  public Double visitBondFixedTransaction(final BondFixedTransaction bond) {
    return bond.getBondStandard().accept(this);
  }

  @Override
  public Double visitBondIborSecurity(final BondIborSecurity bond) {
    return Math.max(bond.getCoupon().accept(this), bond.getNominal().accept(this));
  }

  @Override
  public Double visitBondIborTransaction(final BondIborTransaction bond) {
    return bond.getBondStandard().accept(this);
  }

  // -----     Bond     -----

  @Override
  public Double visitBillTransaction(final BillTransaction bill) {
    return bill.getBillStandard().getEndTime();
  }

  // -----     Forex     -----

  @Override
  public Double visitForexSwap(final ForexSwap fx) {
    return fx.getFarLeg().getPaymentTime();
  }

}
