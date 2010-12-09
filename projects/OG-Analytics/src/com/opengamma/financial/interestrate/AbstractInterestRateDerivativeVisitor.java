/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.payments.ContinuouslyMonitoredAverageRatePayment;
import com.opengamma.financial.interestrate.payments.FixedPayment;
import com.opengamma.financial.interestrate.payments.ForwardLiborPayment;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swap.definition.FloatingRateNote;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;

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
  public T visit(final InterestRateDerivative derivative) {
    Validate.notNull(derivative, "derivative");
    return derivative.accept(this);
  }

  @Override
  public T visitCash(final Cash cash, final S data) {
    throw new UnsupportedOperationException("This visitor does not support visitCash()");
  }

  @Override
  public T visitForwardRateAgreement(final ForwardRateAgreement fra, final S data) {
    throw new UnsupportedOperationException("This visitor does not support visitForwardRateAgreement()");
  }

  @Override
  public T visitInterestRateFuture(final InterestRateFuture future, final S data) {
    throw new UnsupportedOperationException("This visitor does not support visitInterestRateFuture()");
  }

  @Override
  public T visitSwap(final Swap<?, ?> swap, final S data) {
    throw new UnsupportedOperationException("This visitor does not support visitSwap()");
  }

  @Override
  public T visitFixedCouponSwap(final FixedCouponSwap<?> swap, final S data) {
    throw new UnsupportedOperationException("This visitor does not support visitFixedCouponSwap()");
  }

  @Override
  public T visitTenorSwap(final TenorSwap swap, final S data) {
    throw new UnsupportedOperationException("This visitor does not support visitTenorSwap()");
  }

  @Override
  public T visitFloatingRateNote(final FloatingRateNote frn, final S data) {
    throw new UnsupportedOperationException("This visitor does not support visitFloatingRateNote()");
  }

  @Override
  public T visitBond(final Bond bond, final S data) {
    throw new UnsupportedOperationException("This visitor does not support visitBond()");
  }

  @Override
  public T visitGenericAnnuity(final GenericAnnuity<? extends Payment> annuity, final S data) {
    throw new UnsupportedOperationException("This visitor does not support visitGenericAnnuity()");
  }

  @Override
  public T visitFixedPayment(final FixedPayment payment, final S data) {
    throw new UnsupportedOperationException("This visitor does not support visitFixedPayment()");
  }

  @Override
  public T visitForwardLiborPayment(final ForwardLiborPayment payment, final S data) {
    throw new UnsupportedOperationException("This visitor does not support visitForwardLiborPayment()");
  }

  @Override
  public T visitContinuouslyMonitoredAverageRatePayment(final ContinuouslyMonitoredAverageRatePayment payment, final S data) {
    throw new UnsupportedOperationException("This visitor does not support visitContinuouslyMonitoredAverageRatePayment()");
  }

  @Override
  public T visitCash(final Cash cash) {
    throw new UnsupportedOperationException("This visitor does not support visitCash()");
  }

  @Override
  public T visitForwardRateAgreement(final ForwardRateAgreement fra) {
    throw new UnsupportedOperationException("This visitor does not support visitForwardRateAgreement()");
  }

  @Override
  public T visitInterestRateFuture(final InterestRateFuture future) {
    throw new UnsupportedOperationException("This visitor does not support visitInterestRateFuture()");
  }

  @Override
  public T visitSwap(final Swap<?, ?> swap) {
    throw new UnsupportedOperationException("This visitor does not support visitSwap()");
  }

  @Override
  public T visitFixedCouponSwap(final FixedCouponSwap<?> swap) {
    throw new UnsupportedOperationException("This visitor does not support visitFixedCouponSwap()");
  }

  @Override
  public T visitTenorSwap(final TenorSwap swap) {
    throw new UnsupportedOperationException("This visitor does not support visitTenorSwap()");
  }

  @Override
  public T visitFloatingRateNote(final FloatingRateNote frn) {
    throw new UnsupportedOperationException("This visitor does not support visitFloatingRateNote()");
  }

  @Override
  public T visitBond(final Bond bond) {
    throw new UnsupportedOperationException("This visitor does not support visitBond()");
  }

  @Override
  public T visitGenericAnnuity(final GenericAnnuity<? extends Payment> annuity) {
    throw new UnsupportedOperationException("This visitor does not support visitGenericAnnuity()");
  }

  @Override
  public T visitFixedPayment(final FixedPayment payment) {
    throw new UnsupportedOperationException("This visitor does not support visitFixedPayment()");
  }

  @Override
  public T visitForwardLiborPayment(final ForwardLiborPayment payment) {
    throw new UnsupportedOperationException("This visitor does not support visitForwardLiborPayment()");
  }

  @Override
  public T visitContinuouslyMonitoredAverageRatePayment(final ContinuouslyMonitoredAverageRatePayment payment) {
    throw new UnsupportedOperationException("This visitor does not support visitContinuouslyMonitoredAverageRatePayment()");
  }
}
