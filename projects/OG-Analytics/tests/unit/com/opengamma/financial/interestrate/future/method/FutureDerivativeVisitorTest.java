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
import com.opengamma.financial.interestrate.fra.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.BondFuture;
import com.opengamma.financial.interestrate.future.definition.BondFutureSecurity;
import com.opengamma.financial.interestrate.future.definition.BondFutureTransaction;
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
import com.opengamma.financial.interestrate.payments.CouponFloating;
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

  @Test
  private static class MyVisitor<T, U> implements InterestRateDerivativeVisitor<T, String> {

    @Override
    public String visit(final InterestRateDerivative derivative, final T data) {
      return null;
    }

    @Override
    public String[] visit(final InterestRateDerivative[] derivative, final T data) {
      return null;
    }

    @Override
    public String visitBond(final Bond bond, final T data) {
      return null;
    }

    @Override
    public String visitBondForward(final BondForward bondForward, final T data) {
      return null;
    }

    @Override
    public String visitBondFuture(final BondFuture bondFuture, final T data) {
      return null;
    }

    @Override
    public String visitBondFutureSecurity(final BondFutureSecurity bondFuture, final T data) {
      return "BondFutureSecurity2";
    }

    @Override
    public String visitBondFutureTransaction(final BondFutureTransaction bondFuture, final T data) {
      return "BondFutureTransaction2";
    }

    @Override
    public String visitBondFixedSecurity(final BondFixedSecurity bond, final T data) {
      return null;
    }

    @Override
    public String visitBondFixedTransaction(final BondFixedTransaction bond, final T data) {
      return null;
    }

    @Override
    public String visitBondIborSecurity(final BondIborSecurity bond, final T data) {
      return null;
    }

    @Override
    public String visitBondIborTransaction(final BondIborTransaction bond, final T data) {
      return null;
    }

    @Override
    public String visitGenericAnnuity(final GenericAnnuity<? extends Payment> genericAnnuity, final T data) {
      return null;
    }

    @Override
    public String visitFixedCouponAnnuity(final AnnuityCouponFixed fixedCouponAnnuity, final T data) {
      return null;
    }

    @Override
    public String visitForwardLiborAnnuity(final AnnuityCouponIbor forwardLiborAnnuity, final T data) {
      return null;
    }

    @Override
    public String visitSwap(final Swap<?, ?> swap, final T data) {
      return null;
    }

    @Override
    public String visitFixedCouponSwap(final FixedCouponSwap<?> swap, final T data) {
      return null;
    }

    @Override
    public String visitFixedFloatSwap(final FixedFloatSwap swap, final T data) {
      return null;
    }

    @Override
    public String visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final T data) {
      return null;
    }

    @Override
    public String visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final T data) {
      return null;
    }

    @Override
    public String visitTenorSwap(final TenorSwap<? extends Payment> tenorSwap, final T data) {
      return null;
    }

    @Override
    public String visitCash(final Cash cash, final T data) {
      return null;
    }

    @Override
    public String visitInterestRateFutureSecurity(final InterestRateFutureSecurity future, final T data) {
      return "InterestRateFutureSecurity2";
    }

    @Override
    public String visitInterestRateFutureTransaction(final InterestRateFutureTransaction future, final T data) {
      return "InterestRateFutureTransaction2";
    }

    @Override
    public String visitInterestRateFutureOptionPremiumSecurity(final InterestRateFutureOptionPremiumSecurity option, final T data) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionPremiumTransaction(final InterestRateFutureOptionPremiumTransaction option, final T data) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity option, final T data) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option, final T data) {
      return null;
    }

    @Override
    public String visitContinuouslyMonitoredAverageRatePayment(final ContinuouslyMonitoredAverageRatePayment payment, final T data) {
      return null;
    }

    @Override
    public String visitFixedPayment(final PaymentFixed payment, final T data) {
      return null;
    }

    @Override
    public String visitFixedCouponPayment(final CouponFixed payment, final T data) {
      return null;
    }

    @Override
    public String visitCouponIbor(final CouponIbor payment, final T data) {
      return null;
    }

    @Override
    public String visitCouponIborGearing(final CouponIborGearing payment, final T data) {
      return null;
    }

    @Override
    public String visitCapFloorIbor(final CapFloorIbor payment, final T data) {
      return null;
    }

    @Override
    public String visitCouponCMS(final CouponCMS payment, final T data) {
      return null;
    }

    @Override
    public String visitCapFloorCMS(final CapFloorCMS payment, final T data) {
      return null;
    }

    @Override
    public String visitForwardRateAgreement(final ForwardRateAgreement fra, final T data) {
      return null;
    }

    @Override
    public String visit(final InterestRateDerivative derivative) {
      return null;
    }

    @Override
    public String[] visit(final InterestRateDerivative[] derivative) {
      return null;
    }

    @Override
    public String visitBond(final Bond bond) {
      return null;
    }

    @Override
    public String visitBondForward(final BondForward bondForward) {
      return null;
    }

    @Override
    public String visitBondFuture(final BondFuture bondFuture) {
      return null;
    }

    @Override
    public String visitBondFutureSecurity(final BondFutureSecurity bondFuture) {
      return "BondFutureSecurity1";
    }

    @Override
    public String visitBondFutureTransaction(final BondFutureTransaction bondFuture) {
      return "BondFutureTransaction1";
    }

    @Override
    public String visitBondFixedSecurity(final BondFixedSecurity bond) {
      return null;
    }

    @Override
    public String visitBondFixedTransaction(final BondFixedTransaction bond) {
      return null;
    }

    @Override
    public String visitBondIborSecurity(final BondIborSecurity bond) {
      return null;
    }

    @Override
    public String visitBondIborTransaction(final BondIborTransaction bond) {
      return null;
    }

    @Override
    public String visitGenericAnnuity(final GenericAnnuity<? extends Payment> genericAnnuity) {
      return null;
    }

    @Override
    public String visitFixedCouponAnnuity(final AnnuityCouponFixed fixedCouponAnnuity) {
      return null;
    }

    @Override
    public String visitForwardLiborAnnuity(final AnnuityCouponIbor forwardLiborAnnuity) {
      return null;
    }

    @Override
    public String visitSwap(final Swap<?, ?> swap) {
      return null;
    }

    @Override
    public String visitFixedCouponSwap(final FixedCouponSwap<?> swap) {
      return null;
    }

    @Override
    public String visitFixedFloatSwap(final FixedFloatSwap swap) {
      return null;
    }

    @Override
    public String visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption) {
      return null;
    }

    @Override
    public String visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption) {
      return null;
    }

    @Override
    public String visitFloatingRateNote(final FloatingRateNote frn) {
      return null;
    }

    @Override
    public String visitTenorSwap(final TenorSwap<? extends Payment> tenorSwap) {
      return null;
    }

    @Override
    public String visitCash(final Cash cash) {
      return null;
    }

    @Override
    public String visitInterestRateFutureSecurity(final InterestRateFutureSecurity future) {
      return "InterestRateFutureSecurity1";
    }

    @Override
    public String visitInterestRateFutureTransaction(final InterestRateFutureTransaction future) {
      return "InterestRateFutureTransaction1";
    }

    @Override
    public String visitInterestRateFutureOptionPremiumSecurity(final InterestRateFutureOptionPremiumSecurity option) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionPremiumTransaction(final InterestRateFutureOptionPremiumTransaction option) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity option) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option) {
      return null;
    }

    @Override
    public String visitContinuouslyMonitoredAverageRatePayment(final ContinuouslyMonitoredAverageRatePayment payment) {
      return null;
    }

    @Override
    public String visitFixedPayment(final PaymentFixed payment) {
      return null;
    }

    @Override
    public String visitFixedCouponPayment(final CouponFixed payment) {
      return null;
    }

    @Override
    public String visitCouponIbor(final CouponIbor payment) {
      return null;
    }

    @Override
    public String visitCouponIborGearing(final CouponIborGearing payment) {
      return null;
    }

    @Override
    public String visitCapFloorIbor(final CapFloorIbor payment) {
      return null;
    }

    @Override
    public String visitCouponCMS(final CouponCMS payment) {
      return null;
    }

    @Override
    public String visitCapFloorCMS(final CapFloorCMS payment) {
      return null;
    }

    @Override
    public String visitForwardRateAgreement(final ForwardRateAgreement fra) {
      return null;
    }

    @Override
    public String visitCouponFloating(final CouponFloating payment, final T data) {
      return null;
    }

    @Override
    public String visitCouponFloating(final CouponFloating payment) {
      return null;
    }

  }
}
