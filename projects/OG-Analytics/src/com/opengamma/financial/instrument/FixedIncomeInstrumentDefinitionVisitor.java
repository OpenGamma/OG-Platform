/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument;

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
import com.opengamma.financial.instrument.future.InterestRateFutureOptionPremiumSecurityDefinition;
import com.opengamma.financial.instrument.future.InterestRateFutureOptionPremiumTransactionDefinition;
import com.opengamma.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.financial.instrument.future.InterestRateFutureTransactionDefinition;
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
 * 
 * @param <T> Type of the data 
 * @param <U> Type of the result
 */
public interface FixedIncomeInstrumentDefinitionVisitor<T, U> {

  U visit(FixedIncomeInstrumentConverter<?> definition, T data);

  U visit(FixedIncomeInstrumentConverter<?> definition);

  U visitBondDefinition(BondDefinition bond, T data);

  U visitBondDefinition(BondDefinition bond);

  U visitBondForwardDefinition(BondForwardDefinition bondForward, T data);

  U visitBondForwardDefinition(BondForwardDefinition bondForward);

  U visitBondFixedTransactionDefinition(BondFixedTransactionDefinition bond, T data);

  U visitBondFixedTransactionDefinition(BondFixedTransactionDefinition bond);

  U visitBondFixedSecurityDefinition(BondFixedSecurityDefinition bond, T data);

  U visitBondFixedSecurityDefinition(BondFixedSecurityDefinition bond);

  U visitBondIborTransactionDefinition(BondIborTransactionDefinition bond, T data);

  U visitBondIborTransactionDefinition(BondIborTransactionDefinition bond);

  U visitBondIborSecurityDefinition(BondIborSecurityDefinition bond, T data);

  U visitBondIborSecurityDefinition(BondIborSecurityDefinition bond);

  U visitCashDefinition(CashDefinition cash, T data);

  U visitCashDefinition(CashDefinition cash);

  U visitFRADefinition(FRADefinition fra, T data);

  U visitFRADefinition(FRADefinition fra);

  U visitZZZForwardRateAgreementDefinition(ZZZForwardRateAgreementDefinition fra, T data);

  U visitZZZForwardRateAgreementDefinition(ZZZForwardRateAgreementDefinition fra);

  U visitInterestRateFutureSecurityDefinition(InterestRateFutureSecurityDefinition future, T data);

  U visitInterestRateFutureSecurityDefinition(InterestRateFutureSecurityDefinition future);

  U visitInterestRateFutureTransactionDefinition(InterestRateFutureTransactionDefinition future, T data);

  U visitInterestRateFutureTransactionDefinition(InterestRateFutureTransactionDefinition future);

  U visitInterestRateFutureOptionPremiumSecurityDefinition(InterestRateFutureOptionPremiumSecurityDefinition future, T data);

  U visitInterestRateFutureOptionPremiumSecurityDefinition(InterestRateFutureOptionPremiumSecurityDefinition future);

  U visitInterestRateFutureOptionPremiumTransactionDefinition(InterestRateFutureOptionPremiumTransactionDefinition future, T data);

  U visitInterestRateFutureOptionPremiumTransactionDefinition(InterestRateFutureOptionPremiumTransactionDefinition future);

  U visitPaymentFixed(PaymentFixedDefinition payment, T data);

  U visitPaymentFixed(PaymentFixedDefinition payment);

  U visitCouponFixed(CouponFixedDefinition payment, T data);

  U visitCouponFixed(CouponFixedDefinition payment);

  U visitCouponFloating(CouponFloatingDefinition payment, T data);

  U visitCouponFloating(CouponFloatingDefinition payment);

  U visitCouponIbor(CouponIborDefinition payment, T data);

  U visitCouponIbor(CouponIborDefinition payment);

  U visitCouponIborSpread(CouponIborDefinition payment, T data);

  U visitCouponIborSpread(CouponIborDefinition payment);

  U visitCouponCMS(CouponCMSDefinition payment, T data);

  U visitCouponCMS(CouponCMSDefinition payment);

  U visitAnnuityCouponCMSDefinition(AnnuityDefinition<CouponCMSDefinition> annuity, T data);

  U visitAnnuityCouponCMSDefinition(AnnuityDefinition<CouponCMSDefinition> annuity);

  U visitAnnuityCouponFixedDefinition(AnnuityDefinition<CouponFixedDefinition> annuity, T data);

  U visitAnnuityCouponFixedDefinition(AnnuityDefinition<CouponFixedDefinition> annuity);

  U visitAnnuityCouponIborDefinition(AnnuityDefinition<CouponIborDefinition> annuity, T data);

  U visitAnnuityCouponIborDefinition(AnnuityDefinition<CouponIborDefinition> annuity);

  U visitAnnuityCouponIborSpreadDefinition(AnnuityDefinition<CouponIborSpreadDefinition> annuity, T data);

  U visitAnnuityCouponIborSpreadDefinition(AnnuityDefinition<CouponIborSpreadDefinition> annuity);

  U visitAnnuityDefinition(AnnuityDefinition<? extends PaymentDefinition> annuity, T data);

  U visitAnnuityDefinition(AnnuityDefinition<? extends PaymentDefinition> annuity);

  U visitSwapDefinition(SwapDefinition<? extends PaymentDefinition, ? extends PaymentDefinition> swap, T data);

  U visitSwapDefinition(SwapDefinition<? extends PaymentDefinition, ? extends PaymentDefinition> swap);

  U visitSwapFixedIborDefinition(SwapFixedIborDefinition swap, T data);

  U visitSwapFixedIborDefinition(SwapFixedIborDefinition swap);

  U visitSwapFixedIborSpreadDefinition(SwapFixedIborSpreadDefinition swap, T data);

  U visitSwapFixedIborSpreadDefinition(SwapFixedIborSpreadDefinition swap);

  U visitSwapIborIborDefinition(SwapIborIborDefinition swap, T data);

  U visitSwapIborIborDefinition(SwapIborIborDefinition swap);
}
