/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.financial.interestrate.bond.definition.BondForward;
import com.opengamma.financial.interestrate.bond.definition.BondIborSecurity;
import com.opengamma.financial.interestrate.bond.definition.BondIborTransaction;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.ZZZForwardRateAgreement;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.BondFuture;
import com.opengamma.financial.interestrate.future.definition.BondFutureSecurity;
import com.opengamma.financial.interestrate.future.definition.BondFutureTransaction;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureOptionMarginSecurity;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureOptionMarginTransaction;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureOptionPremiumSecurity;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureSecurity;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureTransaction;
import com.opengamma.financial.interestrate.payments.CapFloorCMS;
import com.opengamma.financial.interestrate.payments.CapFloorIbor;
import com.opengamma.financial.interestrate.payments.ContinuouslyMonitoredAverageRatePayment;
import com.opengamma.financial.interestrate.payments.CouponCMS;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.payments.CouponIborGearing;
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
public interface InterestRateDerivativeVisitor<S, T> {

  // Two arguments

  T visit(InterestRateDerivative derivative, S data);

  T[] visit(InterestRateDerivative[] derivative, S data);

  T visitBond(Bond bond, S data);

  T visitBondForward(BondForward bondForward, S data);

  T visitBondFuture(BondFuture bondFuture, S data);

  T visitBondFutureSecurity(BondFutureSecurity bondFuture, S data);

  T visitBondFutureTransaction(BondFutureTransaction bondFuture, S data);

  T visitBondFixedSecurity(BondFixedSecurity bond, S data);

  T visitBondFixedTransaction(BondFixedTransaction bond, S data);

  T visitBondIborSecurity(BondIborSecurity bond, S data);

  T visitBondIborTransaction(BondIborTransaction bond, S data);

  T visitGenericAnnuity(GenericAnnuity<? extends Payment> genericAnnuity, S data);

  T visitFixedCouponAnnuity(AnnuityCouponFixed fixedCouponAnnuity, S data);

  T visitForwardLiborAnnuity(AnnuityCouponIbor forwardLiborAnnuity, S data);

  T visitSwap(Swap<?, ?> swap, S data);

  T visitFixedCouponSwap(FixedCouponSwap<?> swap, S data);

  T visitFixedFloatSwap(FixedFloatSwap swap, S data);

  T visitSwaptionCashFixedIbor(SwaptionCashFixedIbor swaption, S data);

  T visitSwaptionPhysicalFixedIbor(SwaptionPhysicalFixedIbor swaption, S data);

  //  T visitFloatingRateNote(FloatingRateNote frn, S data);

  T visitTenorSwap(TenorSwap<? extends Payment> tenorSwap, S data);

  T visitCash(Cash cash, S data);

  T visitInterestRateFuture(InterestRateFuture future, S data);

  T visitInterestRateFutureSecurity(InterestRateFutureSecurity future, S data);

  T visitInterestRateFutureTransaction(InterestRateFutureTransaction future, S data);

  T visitInterestRateFutureOptionPremiumSecurity(InterestRateFutureOptionPremiumSecurity option, S data);

  T visitInterestRateFutureOptionPremiumTransaction(InterestRateFutureOptionPremiumTransaction option, S data);

  T visitInterestRateFutureOptionMarginSecurity(InterestRateFutureOptionMarginSecurity option, S data);

  T visitInterestRateFutureOptionMarginTransaction(InterestRateFutureOptionMarginTransaction option, S data);

  T visitContinuouslyMonitoredAverageRatePayment(ContinuouslyMonitoredAverageRatePayment payment, S data);

  T visitFixedPayment(PaymentFixed payment, S data);

  T visitFixedCouponPayment(CouponFixed payment, S data);

  T visitCouponIbor(CouponIbor payment, S data);

  T visitCouponIborGearing(CouponIborGearing payment, S data);

  T visitCapFloorIbor(CapFloorIbor payment, S data);

  T visitCouponCMS(CouponCMS payment, S data);

  T visitCapFloorCMS(CapFloorCMS payment, S data);

  T visitForwardRateAgreement(ForwardRateAgreement fra, S data);

  T visitZZZForwardRateAgreement(ZZZForwardRateAgreement fra, S data);

  // One argument

  T visit(InterestRateDerivative derivative);

  T[] visit(InterestRateDerivative[] derivative);

  T visitBond(Bond bond);

  T visitBondForward(BondForward bondForward);

  T visitBondFuture(BondFuture bondFuture);

  T visitBondFutureSecurity(BondFutureSecurity bondFuture);

  T visitBondFutureTransaction(BondFutureTransaction bondFuture);

  T visitBondFixedSecurity(BondFixedSecurity bond);

  T visitBondFixedTransaction(BondFixedTransaction bond);

  T visitBondIborSecurity(BondIborSecurity bond);

  T visitBondIborTransaction(BondIborTransaction bond);

  T visitGenericAnnuity(GenericAnnuity<? extends Payment> genericAnnuity);

  T visitFixedCouponAnnuity(AnnuityCouponFixed fixedCouponAnnuity);

  T visitForwardLiborAnnuity(AnnuityCouponIbor forwardLiborAnnuity);

  T visitSwap(Swap<?, ?> swap);

  T visitFixedCouponSwap(FixedCouponSwap<?> swap);

  T visitFixedFloatSwap(FixedFloatSwap swap);

  T visitSwaptionCashFixedIbor(SwaptionCashFixedIbor swaption);

  T visitSwaptionPhysicalFixedIbor(SwaptionPhysicalFixedIbor swaption);

  T visitFloatingRateNote(FloatingRateNote frn);

  T visitTenorSwap(TenorSwap<? extends Payment> tenorSwap);

  T visitCash(Cash cash);

  T visitInterestRateFuture(InterestRateFuture future);

  T visitInterestRateFutureSecurity(InterestRateFutureSecurity future);

  T visitInterestRateFutureTransaction(InterestRateFutureTransaction future);

  T visitInterestRateFutureOptionPremiumSecurity(InterestRateFutureOptionPremiumSecurity option);

  T visitInterestRateFutureOptionPremiumTransaction(InterestRateFutureOptionPremiumTransaction option);

  T visitInterestRateFutureOptionMarginSecurity(InterestRateFutureOptionMarginSecurity option);

  T visitInterestRateFutureOptionMarginTransaction(InterestRateFutureOptionMarginTransaction option);

  T visitContinuouslyMonitoredAverageRatePayment(ContinuouslyMonitoredAverageRatePayment payment);

  T visitFixedPayment(PaymentFixed payment);

  T visitFixedCouponPayment(CouponFixed payment);

  T visitCouponIbor(CouponIbor payment);

  T visitCouponIborGearing(CouponIborGearing payment);

  T visitCapFloorIbor(CapFloorIbor payment);

  T visitCouponCMS(CouponCMS payment);

  T visitCapFloorCMS(CapFloorCMS payment);

  T visitForwardRateAgreement(ForwardRateAgreement fra);

  T visitZZZForwardRateAgreement(ZZZForwardRateAgreement fra);

}
