/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.payments.ContinuouslyMonitoredAverageRatePayment;
import com.opengamma.financial.interestrate.payments.CouponCMS;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.FloatingRateNote;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;

/**
 * Get the last date (time in years from now) on a yield curve for which an instrument will be sensitive - any change in the yield curve behold this point cannot affect the present value
 */
public final class LastDateCalculator extends AbstractInterestRateDerivativeVisitor<Object, Double> {
  private static final LastDateCalculator CALCULATOR = new LastDateCalculator();

  public static LastDateCalculator getInstance() {
    return CALCULATOR;
  }

  private LastDateCalculator() {
  }

  @Override
  public Double visit(final InterestRateDerivative ird, final Object data) {
    Validate.notNull(ird, "ird");
    return ird.accept(this);
  }

  @Override
  public Double visitTenorSwap(final TenorSwap<? extends Payment> swap) {
    return visitSwap(swap);
  }

  @Override
  public Double visitBond(final Bond bond) {
    return bond.getPrinciplePayment().getPaymentTime();
  }

  @Override
  public Double visitCash(final Cash cash) {
    return cash.getMaturity();
  }

  @Override
  public Double visitFixedCouponSwap(final FixedCouponSwap<?> swap) {
    return visitSwap(swap);
  }

  @Override
  public Double visitFloatingRateNote(final FloatingRateNote frn) {
    return visitSwap(frn);
  }

  @Override
  public Double visitForwardRateAgreement(final ForwardRateAgreement fra) {
    return fra.getMaturity();
  }

  @Override
  public Double visitInterestRateFuture(final InterestRateFuture future) {
    return future.getMaturity();
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
  public Double visitGenericAnnuity(final GenericAnnuity<? extends Payment> annuity) {
    return visit(annuity.getNthPayment(annuity.getNumberOfPayments() - 1));
  }

  @Override
  public Double visitSwap(final Swap<?, ?> swap) {
    final double a = visit(swap.getFirstLeg());
    final double b = visit(swap.getSecondLeg());
    return Math.max(a, b);
  }

  @Override
  public Double visitContinuouslyMonitoredAverageRatePayment(final ContinuouslyMonitoredAverageRatePayment payment) {
    return payment.getPaymentTime();
  }

  @Override
  public Double visitFixedCouponAnnuity(final AnnuityCouponFixed annuity) {
    return visitGenericAnnuity(annuity);
  }

  @Override
  public Double visitFixedCouponPayment(final CouponFixed payment) {
    return visitFixedPayment(payment);
  }

  @Override
  public Double visitForwardLiborAnnuity(final AnnuityCouponIbor annuity) {
    return visitGenericAnnuity(annuity);
  }

  @Override
  public Double visitFixedFloatSwap(final FixedFloatSwap swap) {
    return visitSwap(swap);
  }

  @Override
  public Double visitCouponCMS(CouponCMS payment, Object data) {
    final double swapLastTime = visit(payment.getUnderlyingSwap());
    final double paymentTime = payment.getPaymentTime();
    return Math.max(swapLastTime, paymentTime);
  }
}
