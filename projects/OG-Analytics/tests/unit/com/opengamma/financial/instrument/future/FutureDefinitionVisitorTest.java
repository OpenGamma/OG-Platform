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
import com.opengamma.financial.instrument.fra.FRADefinition;
import com.opengamma.financial.instrument.fra.ZZZForwardRateAgreementDefinition;
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
    public String visit(FixedIncomeInstrumentConverter<?> definition, T data) {
      return null;
    }

    @Override
    public String visit(FixedIncomeInstrumentConverter<?> definition) {
      return null;
    }

    @Override
    public String visitBondDefinition(BondDefinition bond, T data) {
      return null;
    }

    @Override
    public String visitBondDefinition(BondDefinition bond) {
      return null;
    }

    @Override
    public String visitBondForwardDefinition(BondForwardDefinition bondForward, T data) {
      return null;
    }

    @Override
    public String visitBondForwardDefinition(BondForwardDefinition bondForward) {
      return null;
    }

    @Override
    public String visitBondFixedSecurityDefinition(BondFixedSecurityDefinition bond, T data) {
      return null;
    }

    @Override
    public String visitBondFixedSecurityDefinition(BondFixedSecurityDefinition bond) {
      return null;
    }

    @Override
    public String visitBondFixedTransactionDefinition(BondFixedTransactionDefinition bond, T data) {
      return null;
    }

    @Override
    public String visitBondFixedTransactionDefinition(BondFixedTransactionDefinition bond) {
      return null;
    }

    @Override
    public String visitBondFutureSecurityDefinition(BondFutureSecurityDefinition bond, T data) {
      return "BondFutureSecurity2";
    }

    @Override
    public String visitBondFutureSecurityDefinition(BondFutureSecurityDefinition bond) {
      return "BondFutureSecurity1";
    }

    @Override
    public String visitBondFutureTransactionDefinition(BondFutureTransactionDefinition bond, T data) {
      return "BondFutureTransaction2";
    }

    @Override
    public String visitBondFutureTransactionDefinition(BondFutureTransactionDefinition bond) {
      return "BondFutureTransaction1";
    }

    @Override
    public String visitBondIborTransactionDefinition(BondIborTransactionDefinition bond, T data) {
      return null;
    }

    @Override
    public String visitBondIborTransactionDefinition(BondIborTransactionDefinition bond) {
      return null;
    }

    @Override
    public String visitBondIborSecurityDefinition(BondIborSecurityDefinition bond, T data) {
      return null;
    }

    @Override
    public String visitBondIborSecurityDefinition(BondIborSecurityDefinition bond) {
      return null;
    }

    @Override
    public String visitCashDefinition(CashDefinition cash, T data) {
      return null;
    }

    @Override
    public String visitCashDefinition(CashDefinition cash) {
      return null;
    }

    @Override
    public String visitFRADefinition(FRADefinition fra, T data) {
      return null;
    }

    @Override
    public String visitFRADefinition(FRADefinition fra) {
      return null;
    }

    @Override
    public String visitZZZForwardRateAgreementDefinition(ZZZForwardRateAgreementDefinition fra, T data) {
      return null;
    }

    @Override
    public String visitZZZForwardRateAgreementDefinition(ZZZForwardRateAgreementDefinition fra) {
      return null;
    }

    @Override
    public String visitInterestRateFutureSecurityDefinition(InterestRateFutureSecurityDefinition future, T data) {
      return "InterestRateFutureSecurity2";
    }

    @Override
    public String visitInterestRateFutureSecurityDefinition(InterestRateFutureSecurityDefinition future) {
      return "InterestRateFutureSecurity1";
    }

    @Override
    public String visitInterestRateFutureTransactionDefinition(InterestRateFutureTransactionDefinition future, T data) {
      return "InterestRateFutureTransaction2";
    }

    @Override
    public String visitInterestRateFutureTransactionDefinition(InterestRateFutureTransactionDefinition future) {
      return "InterestRateFutureTransaction1";
    }

    @Override
    public String visitInterestRateFutureOptionPremiumSecurityDefinition(InterestRateFutureOptionPremiumSecurityDefinition future, T data) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionPremiumSecurityDefinition(InterestRateFutureOptionPremiumSecurityDefinition future) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionPremiumTransactionDefinition(InterestRateFutureOptionPremiumTransactionDefinition future, T data) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionPremiumTransactionDefinition(InterestRateFutureOptionPremiumTransactionDefinition future) {
      return null;
    }

    @Override
    public String visitPaymentFixed(PaymentFixedDefinition payment, T data) {
      return null;
    }

    @Override
    public String visitPaymentFixed(PaymentFixedDefinition payment) {
      return null;
    }

    @Override
    public String visitCouponFixed(CouponFixedDefinition payment, T data) {
      return null;
    }

    @Override
    public String visitCouponFixed(CouponFixedDefinition payment) {
      return null;
    }

    @Override
    public String visitCouponFloating(CouponFloatingDefinition payment, T data) {
      return null;
    }

    @Override
    public String visitCouponFloating(CouponFloatingDefinition payment) {
      return null;
    }

    @Override
    public String visitCouponIbor(CouponIborDefinition payment, T data) {
      return null;
    }

    @Override
    public String visitCouponIbor(CouponIborDefinition payment) {
      return null;
    }

    @Override
    public String visitCouponIborSpread(CouponIborDefinition payment, T data) {
      return null;
    }

    @Override
    public String visitCouponIborSpread(CouponIborDefinition payment) {
      return null;
    }

    @Override
    public String visitCouponCMS(CouponCMSDefinition payment, T data) {
      return null;
    }

    @Override
    public String visitCouponCMS(CouponCMSDefinition payment) {
      return null;
    }

    @Override
    public String visitAnnuityCouponCMSDefinition(AnnuityDefinition<CouponCMSDefinition> annuity, T data) {
      return null;
    }

    @Override
    public String visitAnnuityCouponCMSDefinition(AnnuityDefinition<CouponCMSDefinition> annuity) {
      return null;
    }

    @Override
    public String visitAnnuityCouponFixedDefinition(AnnuityDefinition<CouponFixedDefinition> annuity, T data) {
      return null;
    }

    @Override
    public String visitAnnuityCouponFixedDefinition(AnnuityDefinition<CouponFixedDefinition> annuity) {
      return null;
    }

    @Override
    public String visitAnnuityCouponIborDefinition(AnnuityDefinition<CouponIborDefinition> annuity, T data) {
      return null;
    }

    @Override
    public String visitAnnuityCouponIborDefinition(AnnuityDefinition<CouponIborDefinition> annuity) {
      return null;
    }

    @Override
    public String visitAnnuityCouponIborSpreadDefinition(AnnuityDefinition<CouponIborSpreadDefinition> annuity, T data) {
      return null;
    }

    @Override
    public String visitAnnuityCouponIborSpreadDefinition(AnnuityDefinition<CouponIborSpreadDefinition> annuity) {
      return null;
    }

    @Override
    public String visitAnnuityDefinition(AnnuityDefinition<? extends PaymentDefinition> annuity, T data) {
      return null;
    }

    @Override
    public String visitAnnuityDefinition(AnnuityDefinition<? extends PaymentDefinition> annuity) {
      return null;
    }

    @Override
    public String visitSwapDefinition(SwapDefinition<? extends PaymentDefinition, ? extends PaymentDefinition> swap, T data) {
      return null;
    }

    @Override
    public String visitSwapDefinition(SwapDefinition<? extends PaymentDefinition, ? extends PaymentDefinition> swap) {
      return null;
    }

    @Override
    public String visitSwapFixedIborDefinition(SwapFixedIborDefinition swap, T data) {
      return null;
    }

    @Override
    public String visitSwapFixedIborDefinition(SwapFixedIborDefinition swap) {
      return null;
    }

    @Override
    public String visitSwapFixedIborSpreadDefinition(SwapFixedIborSpreadDefinition swap, T data) {
      return null;
    }

    @Override
    public String visitSwapFixedIborSpreadDefinition(SwapFixedIborSpreadDefinition swap) {
      return null;
    }

    @Override
    public String visitSwapIborIborDefinition(SwapIborIborDefinition swap, T data) {
      return null;
    }

    @Override
    public String visitSwapIborIborDefinition(SwapIborIborDefinition swap) {
      return null;
    }

  }

}
