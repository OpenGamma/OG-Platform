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
import com.opengamma.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.financial.interestrate.bond.definition.BondIborSecurity;
import com.opengamma.financial.interestrate.bond.definition.BondIborTransaction;
import com.opengamma.financial.interestrate.cash.derivative.Cash;
import com.opengamma.financial.interestrate.cash.derivative.DepositCounterpart;
import com.opengamma.financial.interestrate.cash.derivative.DepositIbor;
import com.opengamma.financial.interestrate.fra.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.payments.CouponCMS;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.payments.CouponIborFixed;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.financial.interestrate.payments.derivative.CouponOIS;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.FloatingRateNote;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;

/**
 * Get the last time (in years from now) referenced in the instrument description.
 */
public final class LastTimeCalculator extends AbstractInstrumentDerivativeVisitor<Object, Double> {
  private static final LastTimeCalculator CALCULATOR = new LastTimeCalculator();

  public static LastTimeCalculator getInstance() {
    return CALCULATOR;
  }

  private LastTimeCalculator() {
  }

  @Override
  public Double visit(final InstrumentDerivative ird, final Object data) {
    Validate.notNull(ird, "ird");
    return ird.accept(this);
  }

  @Override
  public Double[] visit(final InstrumentDerivative[] derivative) {
    Validate.notNull(derivative, "derivative");
    Validate.noNullElements(derivative, "derivative");
    final Double[] output = new Double[derivative.length];
    for (int loopderivative = 0; loopderivative < derivative.length; loopderivative++) {
      output[loopderivative] = derivative[loopderivative].accept(this);
    }
    return output;
  }

  @Override
  public Double visitTenorSwap(final TenorSwap<? extends Payment> swap) {
    return visitSwap(swap);
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
  public Double visitFixedCouponSwap(final FixedCouponSwap<?> swap) {
    return visitSwap(swap);
  }

  @Override
  public Double visitFloatingRateNote(final FloatingRateNote frn) {
    return visitSwap(frn);
  }

  @Override
  public Double visitForwardRateAgreement(final ForwardRateAgreement fra) {
    return fra.getFixingPeriodEndTime();
  }

  @Override
  public Double visitInterestRateFutureSecurity(final InterestRateFuture future) {
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
  public Double visitFixedCouponAnnuity(final AnnuityCouponFixed annuity) {
    return visitGenericAnnuity(annuity);
  }

  @Override
  public Double visitFixedCouponPayment(final CouponFixed payment) {
    return payment.getPaymentTime();
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
  public Double visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption) {
    return visitSwap(swaption.getUnderlyingSwap());
  }

  @Override
  public Double visitCouponCMS(final CouponCMS payment) {
    final double swapLastTime = visit(payment.getUnderlyingSwap());
    final double paymentTime = payment.getPaymentTime();
    return Math.max(swapLastTime, paymentTime);
  }

  @Override
  public Double visitCouponOIS(final CouponOIS payment) {
    return payment.getPaymentTime();
  }

  @Override
  public Double visitCouponIborFixed(CouponIborFixed payment) {
    return visitFixedCouponPayment(payment);
  }

  @Override
  public Double visitBondFixedSecurity(final BondFixedSecurity bond) {
    return Math.max(visit(bond.getCoupon()), visit(bond.getNominal()));
  }

  @Override
  public Double visitBondFixedTransaction(final BondFixedTransaction bond) {
    return visit(bond.getBondStandard());
  }

  @Override
  public Double visitBondIborSecurity(final BondIborSecurity bond) {
    return Math.max(visit(bond.getCoupon()), visit(bond.getNominal()));
  }

  @Override
  public Double visitBondIborTransaction(final BondIborTransaction bond) {
    return visit(bond.getBondStandard());
  }
}
