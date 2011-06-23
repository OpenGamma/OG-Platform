/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.future;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.instrument.FixedIncomeInstrumentConverter;
import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.financial.instrument.bond.BondDefinition;
import com.opengamma.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.financial.instrument.bond.BondFixedTransactionDefinition;
import com.opengamma.financial.instrument.bond.BondForwardDefinition;
import com.opengamma.financial.instrument.bond.BondIborSecurityDefinition;
import com.opengamma.financial.instrument.bond.BondIborTransactionDefinition;
import com.opengamma.financial.instrument.cash.CashDefinition;
import com.opengamma.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.financial.instrument.payment.CouponFloatingDefinition;
import com.opengamma.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.financial.instrument.payment.PaymentDefinition;
import com.opengamma.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.financial.instrument.swap.SwapDefinition;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.instrument.swap.SwapFixedIborSpreadDefinition;
import com.opengamma.financial.instrument.swap.SwapIborIborDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;

/**
 * Tests the visitor of Future related definitions.
 */
public class FutureDefinitionVisitorTest {

  private static final InterestRateFutureSecurityDefinition IR_FUT_SECURITY_DEFINITION = FutureInstrumentsDescriptionDataSet.createInterestRateFutureSecurityDefinition();
  private static final InterestRateFutureTransactionDefinition IR_FUT_TRANSACTION_DEFINITION = FutureInstrumentsDescriptionDataSet.createInterestRateFutureTransactionDefinition();
  private static final BondFutureSecurityDefinition BNDFUT_SECURITY_DEFINITION = FutureInstrumentsDescriptionDataSet.createBondFutureSecurityDefinition();
  private static final BondFutureTransactionDefinition BNDFUT_TRANSACTION_DEFINITION = FutureInstrumentsDescriptionDataSet.createBondFutureTransactionDefinition();

  @SuppressWarnings("synthetic-access")
  private static final MyVisitor<Object, String> VISITOR = new MyVisitor<Object, String>();

  @Test
  public void testVisitor() {
    final Object o = "G";
    assertEquals(IR_FUT_SECURITY_DEFINITION.accept(VISITOR), "InterestRateFutureSecurity1");
    assertEquals(IR_FUT_SECURITY_DEFINITION.accept(VISITOR, o), "InterestRateFutureSecurity2");
    assertEquals(IR_FUT_TRANSACTION_DEFINITION.accept(VISITOR), "InterestRateFutureTransaction1");
    assertEquals(IR_FUT_TRANSACTION_DEFINITION.accept(VISITOR, o), "InterestRateFutureTransaction2");
    assertEquals(BNDFUT_SECURITY_DEFINITION.accept(VISITOR), "BondFutureSecurity1");
    assertEquals(BNDFUT_SECURITY_DEFINITION.accept(VISITOR, o), "BondFutureSecurity2");
    assertEquals(BNDFUT_TRANSACTION_DEFINITION.accept(VISITOR), "BondFutureTransaction1");
    assertEquals(BNDFUT_TRANSACTION_DEFINITION.accept(VISITOR, o), "BondFutureTransaction2");
  }

  private static class MyVisitor<T, U> implements FixedIncomeInstrumentDefinitionVisitor<T, String> {

    @Override
    public String visit(final FixedIncomeInstrumentConverter<?> definition, final T data) {
      return null;
    }

    @Override
    public String visit(final FixedIncomeInstrumentConverter<?> definition) {
      return null;
    }

    @Override
    public String visitBondDefinition(final BondDefinition bond, final T data) {
      return null;
    }

    @Override
    public String visitBondDefinition(final BondDefinition bond) {
      return null;
    }

    @Override
    public String visitBondForwardDefinition(final BondForwardDefinition bondForward, final T data) {
      return null;
    }

    @Override
    public String visitBondForwardDefinition(final BondForwardDefinition bondForward) {
      return null;
    }

    @Override
    public String visitBondFixedSecurityDefinition(final BondFixedSecurityDefinition bond, final T data) {
      return null;
    }

    @Override
    public String visitBondFixedSecurityDefinition(final BondFixedSecurityDefinition bond) {
      return null;
    }

    @Override
    public String visitBondFixedTransactionDefinition(final BondFixedTransactionDefinition bond, final T data) {
      return null;
    }

    @Override
    public String visitBondFixedTransactionDefinition(final BondFixedTransactionDefinition bond) {
      return null;
    }

    @Override
    public String visitBondFutureSecurityDefinition(final BondFutureSecurityDefinition bond, final T data) {
      return "BondFutureSecurity2";
    }

    @Override
    public String visitBondFutureSecurityDefinition(final BondFutureSecurityDefinition bond) {
      return "BondFutureSecurity1";
    }

    @Override
    public String visitBondFutureTransactionDefinition(final BondFutureTransactionDefinition bond, final T data) {
      return "BondFutureTransaction2";
    }

    @Override
    public String visitBondFutureTransactionDefinition(final BondFutureTransactionDefinition bond) {
      return "BondFutureTransaction1";
    }

    @Override
    public String visitBondIborTransactionDefinition(final BondIborTransactionDefinition bond, final T data) {
      return null;
    }

    @Override
    public String visitBondIborTransactionDefinition(final BondIborTransactionDefinition bond) {
      return null;
    }

    @Override
    public String visitBondIborSecurityDefinition(final BondIborSecurityDefinition bond, final T data) {
      return null;
    }

    @Override
    public String visitBondIborSecurityDefinition(final BondIborSecurityDefinition bond) {
      return null;
    }

    @Override
    public String visitCashDefinition(final CashDefinition cash, final T data) {
      return null;
    }

    @Override
    public String visitCashDefinition(final CashDefinition cash) {
      return null;
    }

    @Override
    public String visitInterestRateFutureSecurityDefinition(final InterestRateFutureSecurityDefinition future, final T data) {
      return "InterestRateFutureSecurity2";
    }

    @Override
    public String visitInterestRateFutureSecurityDefinition(final InterestRateFutureSecurityDefinition future) {
      return "InterestRateFutureSecurity1";
    }

    @Override
    public String visitInterestRateFutureTransactionDefinition(final InterestRateFutureTransactionDefinition future, final T data) {
      return "InterestRateFutureTransaction2";
    }

    @Override
    public String visitInterestRateFutureTransactionDefinition(final InterestRateFutureTransactionDefinition future) {
      return "InterestRateFutureTransaction1";
    }

    @Override
    public String visitInterestRateFutureOptionPremiumSecurityDefinition(final InterestRateFutureOptionPremiumSecurityDefinition future, final T data) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionPremiumSecurityDefinition(final InterestRateFutureOptionPremiumSecurityDefinition future) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionPremiumTransactionDefinition(final InterestRateFutureOptionPremiumTransactionDefinition future, final T data) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionPremiumTransactionDefinition(final InterestRateFutureOptionPremiumTransactionDefinition future) {
      return null;
    }

    @Override
    public String visitPaymentFixed(final PaymentFixedDefinition payment, final T data) {
      return null;
    }

    @Override
    public String visitPaymentFixed(final PaymentFixedDefinition payment) {
      return null;
    }

    @Override
    public String visitCouponFixed(final CouponFixedDefinition payment, final T data) {
      return null;
    }

    @Override
    public String visitCouponFixed(final CouponFixedDefinition payment) {
      return null;
    }

    @Override
    public String visitCouponFloating(final CouponFloatingDefinition payment, final T data) {
      return null;
    }

    @Override
    public String visitCouponFloating(final CouponFloatingDefinition payment) {
      return null;
    }

    @Override
    public String visitCouponIbor(final CouponIborDefinition payment, final T data) {
      return null;
    }

    @Override
    public String visitCouponIbor(final CouponIborDefinition payment) {
      return null;
    }

    @Override
    public String visitCouponIborSpread(final CouponIborDefinition payment, final T data) {
      return null;
    }

    @Override
    public String visitCouponIborSpread(final CouponIborDefinition payment) {
      return null;
    }

    @Override
    public String visitCouponCMS(final CouponCMSDefinition payment, final T data) {
      return null;
    }

    @Override
    public String visitCouponCMS(final CouponCMSDefinition payment) {
      return null;
    }

    @Override
    public String visitAnnuityCouponCMSDefinition(final AnnuityDefinition<CouponCMSDefinition> annuity, final T data) {
      return null;
    }

    @Override
    public String visitAnnuityCouponCMSDefinition(final AnnuityDefinition<CouponCMSDefinition> annuity) {
      return null;
    }

    @Override
    public String visitAnnuityCouponFixedDefinition(final AnnuityDefinition<CouponFixedDefinition> annuity, final T data) {
      return null;
    }

    @Override
    public String visitAnnuityCouponFixedDefinition(final AnnuityDefinition<CouponFixedDefinition> annuity) {
      return null;
    }

    @Override
    public String visitAnnuityCouponIborDefinition(final AnnuityDefinition<CouponIborDefinition> annuity, final T data) {
      return null;
    }

    @Override
    public String visitAnnuityCouponIborDefinition(final AnnuityDefinition<CouponIborDefinition> annuity) {
      return null;
    }

    @Override
    public String visitAnnuityCouponIborSpreadDefinition(final AnnuityDefinition<CouponIborSpreadDefinition> annuity, final T data) {
      return null;
    }

    @Override
    public String visitAnnuityCouponIborSpreadDefinition(final AnnuityDefinition<CouponIborSpreadDefinition> annuity) {
      return null;
    }

    @Override
    public String visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity, final T data) {
      return null;
    }

    @Override
    public String visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity) {
      return null;
    }

    @Override
    public String visitSwapFixedIborDefinition(final SwapFixedIborDefinition swap, final T data) {
      return null;
    }

    @Override
    public String visitSwapFixedIborDefinition(final SwapFixedIborDefinition swap) {
      return null;
    }

    @Override
    public String visitSwapFixedIborSpreadDefinition(final SwapFixedIborSpreadDefinition swap, final T data) {
      return null;
    }

    @Override
    public String visitSwapFixedIborSpreadDefinition(final SwapFixedIborSpreadDefinition swap) {
      return null;
    }

    @Override
    public String visitSwapIborIborDefinition(final SwapIborIborDefinition swap, final T data) {
      return null;
    }

    @Override
    public String visitSwapIborIborDefinition(final SwapIborIborDefinition swap) {
      return null;
    }

    @Override
    public String visitForwardRateAgreementDefinition(final ForwardRateAgreementDefinition fra, final T data) {
      return null;
    }

    @Override
    public String visitForwardRateAgreementDefinition(final ForwardRateAgreementDefinition fra) {
      return null;
    }

    @Override
    public String visitSwapDefinition(final SwapDefinition swap, final T data) {
      return null;
    }

    @Override
    public String visitSwapDefinition(final SwapDefinition swap) {
      return null;
    }

    @Override
    public String visitSwaptionCashFixedIborDefinition(final SwaptionCashFixedIborDefinition swaption) {
      return null;
    }

    @Override
    public String visitSwaptionCashFixedIborDefinition(final SwaptionCashFixedIborDefinition swaption, final T data) {
      return null;
    }

    @Override
    public String visitSwaptionPhysicalFixedIborDefinition(final SwaptionPhysicalFixedIborDefinition swaption) {
      return null;
    }

    @Override
    public String visitSwaptionPhysicalFixedIborDefinition(final SwaptionPhysicalFixedIborDefinition swaption, final T data) {
      return null;
    }

  }

}
