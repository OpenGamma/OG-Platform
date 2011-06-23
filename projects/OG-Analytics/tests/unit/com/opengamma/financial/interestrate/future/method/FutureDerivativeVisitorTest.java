/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.method;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.instrument.future.FutureInstrumentsDescriptionDataSet;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
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
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.fra.definition.ZZZForwardRateAgreement;
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
 * Tests the visitor of Futures related derivatives.
 */
public class FutureDerivativeVisitorTest {

  private static final InterestRateFutureSecurity IR_FUT_SECURITY = FutureInstrumentsDescriptionDataSet.createInterestRateFutureSecurity();
  private static final InterestRateFutureTransaction IR_FUT_TRANSACTION = FutureInstrumentsDescriptionDataSet.createInterestRateFutureTransaction();
  private static final BondFutureSecurity BNDFUT_SECURITY = FutureInstrumentsDescriptionDataSet.createBondFutureSecurity();
  private static final BondFutureTransaction BNDFUT_TRANSACTION = FutureInstrumentsDescriptionDataSet.createBondFutureTransaction();

  @SuppressWarnings("synthetic-access")
  private static final MyVisitor<Object, String> VISITOR = new MyVisitor<Object, String>();

  @Test
  public void testVisitor() {
    final Object o = "G";
    assertEquals("InterestRateFutureSecurity1", IR_FUT_SECURITY.accept(VISITOR));
    assertEquals("InterestRateFutureSecurity2", IR_FUT_SECURITY.accept(VISITOR, o));
    assertEquals("InterestRateFutureTransaction1", IR_FUT_TRANSACTION.accept(VISITOR));
    assertEquals("InterestRateFutureTransaction2", IR_FUT_TRANSACTION.accept(VISITOR, o));
    assertEquals("BondFutureSecurity1", BNDFUT_SECURITY.accept(VISITOR));
    assertEquals("BondFutureSecurity2", BNDFUT_SECURITY.accept(VISITOR, o));
    assertEquals("BondFutureTransaction1", BNDFUT_TRANSACTION.accept(VISITOR));
    assertEquals("BondFutureTransaction2", BNDFUT_TRANSACTION.accept(VISITOR, o));
  }

  private static class MyVisitor<T, U> implements InterestRateDerivativeVisitor<T, String> {

    @Override
    public String visit(InterestRateDerivative derivative, T data) {
      return null;
    }

    @Override
    public String[] visit(InterestRateDerivative[] derivative, T data) {
      return null;
    }

    @Override
    public String visitBond(Bond bond, T data) {
      return null;
    }

    @Override
    public String visitBondForward(BondForward bondForward, T data) {
      return null;
    }

    @Override
    public String visitBondFuture(BondFuture bondFuture, T data) {
      return null;
    }

    @Override
    public String visitBondFutureSecurity(BondFutureSecurity bondFuture, T data) {
      return "BondFutureSecurity2";
    }

    @Override
    public String visitBondFutureTransaction(BondFutureTransaction bondFuture, T data) {
      return "BondFutureTransaction2";
    }

    @Override
    public String visitBondFixedSecurity(BondFixedSecurity bond, T data) {
      return null;
    }

    @Override
    public String visitBondFixedTransaction(BondFixedTransaction bond, T data) {
      return null;
    }

    @Override
    public String visitBondIborSecurity(BondIborSecurity bond, T data) {
      return null;
    }

    @Override
    public String visitBondIborTransaction(BondIborTransaction bond, T data) {
      return null;
    }

    @Override
    public String visitGenericAnnuity(GenericAnnuity<? extends Payment> genericAnnuity, T data) {
      return null;
    }

    @Override
    public String visitFixedCouponAnnuity(AnnuityCouponFixed fixedCouponAnnuity, T data) {
      return null;
    }

    @Override
    public String visitForwardLiborAnnuity(AnnuityCouponIbor forwardLiborAnnuity, T data) {
      return null;
    }

    @Override
    public String visitSwap(Swap<?, ?> swap, T data) {
      return null;
    }

    @Override
    public String visitFixedCouponSwap(FixedCouponSwap<?> swap, T data) {
      return null;
    }

    @Override
    public String visitFixedFloatSwap(FixedFloatSwap swap, T data) {
      return null;
    }

    @Override
    public String visitSwaptionCashFixedIbor(SwaptionCashFixedIbor swaption, T data) {
      return null;
    }

    @Override
    public String visitSwaptionPhysicalFixedIbor(SwaptionPhysicalFixedIbor swaption, T data) {
      return null;
    }

    @Override
    public String visitTenorSwap(TenorSwap<? extends Payment> tenorSwap, T data) {
      return null;
    }

    @Override
    public String visitCash(Cash cash, T data) {
      return null;
    }

    @Override
    public String visitInterestRateFuture(InterestRateFuture future, T data) {
      return null;
    }

    @Override
    public String visitInterestRateFutureSecurity(InterestRateFutureSecurity future, T data) {
      return "InterestRateFutureSecurity2";
    }

    @Override
    public String visitInterestRateFutureTransaction(InterestRateFutureTransaction future, T data) {
      return "InterestRateFutureTransaction2";
    }

    @Override
    public String visitInterestRateFutureOptionPremiumSecurity(InterestRateFutureOptionPremiumSecurity option, T data) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionPremiumTransaction(InterestRateFutureOptionPremiumTransaction option, T data) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionMarginSecurity(InterestRateFutureOptionMarginSecurity option, T data) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionMarginTransaction(InterestRateFutureOptionMarginTransaction option, T data) {
      return null;
    }

    @Override
    public String visitContinuouslyMonitoredAverageRatePayment(ContinuouslyMonitoredAverageRatePayment payment, T data) {
      return null;
    }

    @Override
    public String visitFixedPayment(PaymentFixed payment, T data) {
      return null;
    }

    @Override
    public String visitFixedCouponPayment(CouponFixed payment, T data) {
      return null;
    }

    @Override
    public String visitCouponIbor(CouponIbor payment, T data) {
      return null;
    }

    @Override
    public String visitCouponIborGearing(CouponIborGearing payment, T data) {
      return null;
    }

    @Override
    public String visitCapFloorIbor(CapFloorIbor payment, T data) {
      return null;
    }

    @Override
    public String visitCouponCMS(CouponCMS payment, T data) {
      return null;
    }

    @Override
    public String visitCapFloorCMS(CapFloorCMS payment, T data) {
      return null;
    }

    @Override
    public String visitForwardRateAgreement(ForwardRateAgreement fra, T data) {
      return null;
    }

    @Override
    public String visitZZZForwardRateAgreement(ZZZForwardRateAgreement fra, T data) {
      return null;
    }

    @Override
    public String visit(InterestRateDerivative derivative) {
      return null;
    }

    @Override
    public String[] visit(InterestRateDerivative[] derivative) {
      return null;
    }

    @Override
    public String visitBond(Bond bond) {
      return null;
    }

    @Override
    public String visitBondForward(BondForward bondForward) {
      return null;
    }

    @Override
    public String visitBondFuture(BondFuture bondFuture) {
      return null;
    }

    @Override
    public String visitBondFutureSecurity(BondFutureSecurity bondFuture) {
      return "BondFutureSecurity1";
    }

    @Override
    public String visitBondFutureTransaction(BondFutureTransaction bondFuture) {
      return "BondFutureTransaction1";
    }

    @Override
    public String visitBondFixedSecurity(BondFixedSecurity bond) {
      return null;
    }

    @Override
    public String visitBondFixedTransaction(BondFixedTransaction bond) {
      return null;
    }

    @Override
    public String visitBondIborSecurity(BondIborSecurity bond) {
      return null;
    }

    @Override
    public String visitBondIborTransaction(BondIborTransaction bond) {
      return null;
    }

    @Override
    public String visitGenericAnnuity(GenericAnnuity<? extends Payment> genericAnnuity) {
      return null;
    }

    @Override
    public String visitFixedCouponAnnuity(AnnuityCouponFixed fixedCouponAnnuity) {
      return null;
    }

    @Override
    public String visitForwardLiborAnnuity(AnnuityCouponIbor forwardLiborAnnuity) {
      return null;
    }

    @Override
    public String visitSwap(Swap<?, ?> swap) {
      return null;
    }

    @Override
    public String visitFixedCouponSwap(FixedCouponSwap<?> swap) {
      return null;
    }

    @Override
    public String visitFixedFloatSwap(FixedFloatSwap swap) {
      return null;
    }

    @Override
    public String visitSwaptionCashFixedIbor(SwaptionCashFixedIbor swaption) {
      return null;
    }

    @Override
    public String visitSwaptionPhysicalFixedIbor(SwaptionPhysicalFixedIbor swaption) {
      return null;
    }

    @Override
    public String visitFloatingRateNote(FloatingRateNote frn) {
      return null;
    }

    @Override
    public String visitTenorSwap(TenorSwap<? extends Payment> tenorSwap) {
      return null;
    }

    @Override
    public String visitCash(Cash cash) {
      return null;
    }

    @Override
    public String visitInterestRateFuture(InterestRateFuture future) {
      return null;
    }

    @Override
    public String visitInterestRateFutureSecurity(InterestRateFutureSecurity future) {
      return "InterestRateFutureSecurity1";
    }

    @Override
    public String visitInterestRateFutureTransaction(InterestRateFutureTransaction future) {
      return "InterestRateFutureTransaction1";
    }

    @Override
    public String visitInterestRateFutureOptionPremiumSecurity(InterestRateFutureOptionPremiumSecurity option) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionPremiumTransaction(InterestRateFutureOptionPremiumTransaction option) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionMarginSecurity(InterestRateFutureOptionMarginSecurity option) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionMarginTransaction(InterestRateFutureOptionMarginTransaction option) {
      return null;
    }

    @Override
    public String visitContinuouslyMonitoredAverageRatePayment(ContinuouslyMonitoredAverageRatePayment payment) {
      return null;
    }

    @Override
    public String visitFixedPayment(PaymentFixed payment) {
      return null;
    }

    @Override
    public String visitFixedCouponPayment(CouponFixed payment) {
      return null;
    }

    @Override
    public String visitCouponIbor(CouponIbor payment) {
      return null;
    }

    @Override
    public String visitCouponIborGearing(CouponIborGearing payment) {
      return null;
    }

    @Override
    public String visitCapFloorIbor(CapFloorIbor payment) {
      return null;
    }

    @Override
    public String visitCouponCMS(CouponCMS payment) {
      return null;
    }

    @Override
    public String visitCapFloorCMS(CapFloorCMS payment) {
      return null;
    }

    @Override
    public String visitForwardRateAgreement(ForwardRateAgreement fra) {
      return null;
    }

    @Override
    public String visitZZZForwardRateAgreement(ZZZForwardRateAgreement fra) {
      return null;
    }

  }
}
