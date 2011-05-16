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
import com.opengamma.financial.interestrate.bond.definition.BondForward;
import com.opengamma.financial.interestrate.bond.definition.BondTransaction;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.ZZZForwardRateAgreement;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.InterestRateFutureTransaction;
import com.opengamma.financial.interestrate.future.definition.BondFuture;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.payments.CapFloorCMS;
import com.opengamma.financial.interestrate.payments.CapFloorIbor;
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
import com.opengamma.financial.interestrate.swaption.SwaptionCashFixedIbor;
import com.opengamma.financial.interestrate.swaption.SwaptionPhysicalFixedIbor;

/**
 * 
 * @param <S> The type of the data
 * @param <T> The return type of the calculation
 */
public abstract class AbstractInterestRateDerivativeVisitor<S, T> implements InterestRateDerivativeVisitor<S, T> {

  @Override
  public T visit(final InterestRateDerivative derivative, final S data) {
    Validate.notNull(derivative, "derivative");
    Validate.notNull(data, "data");
    return derivative.accept(this, data);
  }

  @Override
  public T[] visit(final InterestRateDerivative[] derivative, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visit(derivative[], data)");
  }

  @Override
  public T visit(final InterestRateDerivative derivative) {
    Validate.notNull(derivative, "derivative");
    return derivative.accept(this);
  }

  @Override
  public T[] visit(final InterestRateDerivative[] derivative) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visit(derivative[])");
  }

  @Override
  public T visitBond(final Bond bond, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBond()");
  }

  @Override
  public T visitBondForward(final BondForward bondForward, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondForward()");
  }

  @Override
  public T visitBondFuture(final BondFuture bondFuture, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFuture()");
  }

  @Override
  public T visitBondTransaction(BondTransaction<? extends Payment> bond, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondTransaction()");
  }

  @Override
  public T visitFixedCouponAnnuity(final AnnuityCouponFixed fixedCouponAnnuity, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedCouponAnnuity()");
  }

  @Override
  public T visitForwardLiborAnnuity(final AnnuityCouponIbor forwardLiborAnnuity, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForwardLiborAnnuity()");
  }

  @Override
  public T visitFixedFloatSwap(final FixedFloatSwap swap, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedFloatSwap()");
  }

  //  @Override
  //  public T visitFloatingRateNote(final FloatingRateNote frn, final S data) {
  //    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFloatingRateNote()");
  //  }

  @Override
  public T visitTenorSwap(final TenorSwap<? extends Payment> tenorSwap, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitTenorSwap()");
  }

  @Override
  public T visitCash(final Cash cash, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCash()");
  }

  //  @Override
  //  public T visitLibor(final Libor libor, final S data) {
  //    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitLibor()");
  //  }

  @Override
  public T visitInterestRateFuture(final InterestRateFuture future, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFuture()");
  }

  @Override
  public T visitInterestRateFutureTransaction(final InterestRateFutureTransaction future, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureTransaction()");
  }

  @Override
  public T visitContinuouslyMonitoredAverageRatePayment(final ContinuouslyMonitoredAverageRatePayment payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitContinuouslyMonitoredAverageRatePayment()");
  }

  @Override
  public T visitFixedCouponPayment(final CouponFixed payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedCouponPayment()");
  }

  @Override
  public T visitCouponIbor(final CouponIbor payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponIbor()");
  }

  @Override
  public T visitCapFloorIbor(final CapFloorIbor payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCapFloorIbor()");
  }

  @Override
  public T visitCouponCMS(final CouponCMS payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponCMS()");
  }

  @Override
  public T visitCapFloorCMS(final CapFloorCMS payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponCMS()");
  }

  @Override
  public T visitForwardRateAgreement(final ForwardRateAgreement fra, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForwardRateAgreement()");
  }

  @Override
  public T visitZZZForwardRateAgreement(final ZZZForwardRateAgreement fra, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForwardRateAgreement()");
  }

  @Override
  public T visitGenericAnnuity(final GenericAnnuity<? extends Payment> genericAnnuity, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitGenericAnnuity()");
  }

  @Override
  public T visitSwap(final Swap<?, ?> swap, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwap()");
  }

  @Override
  public T visitFixedCouponSwap(final FixedCouponSwap<?> swap, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedCouponSwap()");
  }

  @Override
  public T visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwap()");
  }

  @Override
  public T visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwap()");
  }

  @Override
  public T visitFixedPayment(final PaymentFixed payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedPayment()");
  }

  @Override
  public T visitBond(final Bond bond) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBond()");
  }

  @Override
  public T visitBondForward(final BondForward bondForward) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondForward()");
  }

  @Override
  public T visitBondFuture(final BondFuture bondFuture) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFuture()");
  }

  @Override
  public T visitBondTransaction(BondTransaction<? extends Payment> bond) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondTransaction()");
  }

  @Override
  public T visitFixedCouponAnnuity(final AnnuityCouponFixed fixedCouponAnnuity) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedCouponAnnuity()");
  }

  @Override
  public T visitForwardLiborAnnuity(final AnnuityCouponIbor forwardLiborAnnuity) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForwardLiborAnnuity()");
  }

  @Override
  public T visitFixedFloatSwap(final FixedFloatSwap swap) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedFloatSwap()");
  }

  @Override
  public T visitFloatingRateNote(final FloatingRateNote frn) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFloatingRateNote()");
  }

  @Override
  public T visitTenorSwap(final TenorSwap<? extends Payment> tenorSwap) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitTenorSwap()");
  }

  @Override
  public T visitCash(final Cash cash) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCash()");
  }

  //  @Override
  //  public T visitLibor(final Libor libor) {
  //    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitLibor()");
  //  }

  @Override
  public T visitInterestRateFuture(final InterestRateFuture future) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFuture()");
  }

  @Override
  public T visitInterestRateFutureTransaction(final InterestRateFutureTransaction future) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureTransaction()");
  }

  @Override
  public T visitContinuouslyMonitoredAverageRatePayment(final ContinuouslyMonitoredAverageRatePayment payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitContinuouslyMonitoredAverageRatePayment()");
  }

  @Override
  public T visitFixedCouponPayment(final CouponFixed payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedCouponPayment()");
  }

  @Override
  public T visitCouponIbor(final CouponIbor payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponIbor()");
  }

  @Override
  public T visitCapFloorIbor(final CapFloorIbor payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCapFloorIbor()");
  }

  @Override
  public T visitCouponCMS(final CouponCMS payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponCMS()");
  }

  @Override
  public T visitCapFloorCMS(final CapFloorCMS payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponCMS()");
  }

  @Override
  public T visitForwardRateAgreement(final ForwardRateAgreement fra) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForwardRateAgreement()");
  }

  @Override
  public T visitZZZForwardRateAgreement(final ZZZForwardRateAgreement fra) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForwardRateAgreement()");
  }

  @Override
  public T visitGenericAnnuity(final GenericAnnuity<? extends Payment> genericAnnuity) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitGenericAnnuity()");
  }

  @Override
  public T visitSwap(final Swap<?, ?> swap) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwap()");
  }

  @Override
  public T visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwap()");
  }

  @Override
  public T visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwap()");
  }

  @Override
  public T visitFixedCouponSwap(final FixedCouponSwap<?> swap) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedCouponSwap()");
  }

  @Override
  public T visitFixedPayment(final PaymentFixed payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedPayment()");
  }

}
