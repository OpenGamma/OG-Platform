/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import com.opengamma.financial.interestrate.annuity.definition.FixedCouponAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.ForwardLiborAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.bond.definition.BondForward;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.BondFuture;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.libor.definition.Libor;
import com.opengamma.financial.interestrate.payments.ContinuouslyMonitoredAverageRatePayment;
import com.opengamma.financial.interestrate.payments.FixedCouponPayment;
import com.opengamma.financial.interestrate.payments.FixedPayment;
import com.opengamma.financial.interestrate.payments.ForwardLiborPayment;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.FloatingRateNote;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;

/**
 * 
 * @param <S> The type of the data
 * @param <T> The return type of the calculation
 */
public interface InterestRateDerivativeVisitor<S, T> {

  T visit(InterestRateDerivative derivative, S data);

  T visitBondForward(BondForward bondForward, S data);

  T visitBondFuture(BondFuture bondFuture, S data);

  T visitGenericAnnuity(GenericAnnuity<? extends Payment> genericAnnuity, S data);

  T visitFixedCouponAnnuity(FixedCouponAnnuity fixedCouponAnnuity, S data);

  T visitForwardLiborAnnuity(ForwardLiborAnnuity forwardLiborAnnuity, S data);

  T visitSwap(Swap<?, ?> swap, S data);

  T visitFixedCouponSwap(FixedCouponSwap<?> swap, S data);

  T visitFixedFloatSwap(FixedFloatSwap swap, S data);

  T visitFloatingRateNote(FloatingRateNote frn, S data);

  T visitTenorSwap(TenorSwap<? extends Payment> tenorSwap, S data);

  T visitBond(Bond bond, S data);

  T visitCash(Cash cash, S data);

  T visitLibor(Libor libor, S data);

  T visitInterestRateFuture(InterestRateFuture future, S data);

  T visitContinuouslyMonitoredAverageRatePayment(ContinuouslyMonitoredAverageRatePayment payment, S data);

  T visitFixedPayment(FixedPayment payment, S data);

  T visitFixedCouponPayment(FixedCouponPayment payment, S data);

  T visitForwardLiborPayment(ForwardLiborPayment payment, S data);

  T visitForwardRateAgreement(ForwardRateAgreement fra, S data);

  T visit(InterestRateDerivative derivative);

  T visitBondForward(BondForward bondForward);

  T visitBondFuture(BondFuture bondFuture);

  T visitGenericAnnuity(GenericAnnuity<? extends Payment> genericAnnuity);

  T visitFixedCouponAnnuity(FixedCouponAnnuity fixedCouponAnnuity);

  T visitForwardLiborAnnuity(ForwardLiborAnnuity forwardLiborAnnuity);

  T visitSwap(Swap<?, ?> swap);

  T visitFixedCouponSwap(FixedCouponSwap<?> swap);

  T visitFixedFloatSwap(FixedFloatSwap swap);

  T visitFloatingRateNote(FloatingRateNote frn);

  T visitTenorSwap(TenorSwap<? extends Payment> tenorSwap);

  T visitBond(Bond bond);

  T visitCash(Cash cash);

  T visitLibor(Libor libor);

  T visitInterestRateFuture(InterestRateFuture future);

  T visitContinuouslyMonitoredAverageRatePayment(ContinuouslyMonitoredAverageRatePayment payment);

  T visitFixedPayment(FixedPayment payment);

  T visitFixedCouponPayment(FixedCouponPayment payment);

  T visitForwardLiborPayment(ForwardLiborPayment payment);

  T visitForwardRateAgreement(ForwardRateAgreement fra);

}
