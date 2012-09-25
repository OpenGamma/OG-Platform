/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexNonDeliverableForwardDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexNonDeliverableOptionDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionDigitalDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionSingleBarrierDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexSwapDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillTransactionDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondCapitalIndexedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondCapitalIndexedTransactionDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedTransactionDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondIborSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondIborTransactionDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositCounterpartDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositIborDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositZeroDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureOptionPremiumSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureOptionPremiumTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.DeliverableSwapFuturesSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionPremiumSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionPremiumTransactionDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationGearingDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CapFloorCMSDefinition;
import com.opengamma.analytics.financial.instrument.payment.CapFloorCMSSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CapFloorIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborRatchetDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponOISDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponOISSimplifiedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapIborIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapXCcyIborIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionBermudaFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborSpreadDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 * A convenience class that means that classes implementing InstrumentDefinitionVisitor do not have to implement every method.
 * @param <DATA_TYPE> Type of the data 
 * @param <RESULT_TYPE> Type of the result
 */
public class AbstractInstrumentDefinitionVisitor<DATA_TYPE, RESULT_TYPE> implements InstrumentDefinitionVisitor<DATA_TYPE, RESULT_TYPE> {

  @Override
  public RESULT_TYPE visit(final InstrumentDefinition<?> definition, final DATA_TYPE data) {
    ArgumentChecker.notNull(definition, "definition");
    ArgumentChecker.notNull(data, "data");
    return definition.accept(this, data);
  }

  @Override
  public RESULT_TYPE visit(final InstrumentDefinition<?> definition) {
    ArgumentChecker.notNull(definition, "definition");
    return definition.accept(this);
  }

  @Override
  public RESULT_TYPE visitBondFixedSecurityDefinition(final BondFixedSecurityDefinition bond, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFixedSecurityDefinition()");
  }

  @Override
  public RESULT_TYPE visitBondFixedSecurityDefinition(final BondFixedSecurityDefinition bond) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFixedSecurityDefinition()");
  }

  @Override
  public RESULT_TYPE visitBondFixedTransactionDefinition(final BondFixedTransactionDefinition bond, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFixedTransationSecurityDefinition()");
  }

  @Override
  public RESULT_TYPE visitBondFixedTransactionDefinition(final BondFixedTransactionDefinition bond) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFixedTransationSecurityDefinition()");
  }

  @Override
  public RESULT_TYPE visitBondFutureSecurityDefinition(final BondFutureDefinition bond, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFutureSecurityDefinition()");
  }

  @Override
  public RESULT_TYPE visitBondFutureSecurityDefinition(final BondFutureDefinition bond) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFutureSecurityDefinition()");
  }

  @Override
  public RESULT_TYPE visitBondFutureOptionPremiumSecurityDefinition(final BondFutureOptionPremiumSecurityDefinition bond, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFutureOptionPremiumSecurityDefinition()");
  }

  @Override
  public RESULT_TYPE visitBondFutureOptionPremiumSecurityDefinition(final BondFutureOptionPremiumSecurityDefinition bond) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFutureOptionPremiumSecurityDefinition()");
  }

  @Override
  public RESULT_TYPE visitBondFutureOptionPremiumTransactionDefinition(final BondFutureOptionPremiumTransactionDefinition bond, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFutureOptionPremiumTransationDefinition()");
  }

  @Override
  public RESULT_TYPE visitBondFutureOptionPremiumTransactionDefinition(final BondFutureOptionPremiumTransactionDefinition bond) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFutureOptionPremiumTransactionDefinition()");
  }

  @Override
  public RESULT_TYPE visitBondIborTransactionDefinition(final BondIborTransactionDefinition bond, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondIborTransactionDefinition()");
  }

  @Override
  public RESULT_TYPE visitBondIborTransactionDefinition(final BondIborTransactionDefinition bond) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondIborTransactionDefinition()");
  }

  @Override
  public RESULT_TYPE visitBondIborSecurityDefinition(final BondIborSecurityDefinition bond, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondIborSecurityDefinition()");
  }

  @Override
  public RESULT_TYPE visitBondIborSecurityDefinition(final BondIborSecurityDefinition bond) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondIborSecurityDefinition()");
  }

  @Override
  public RESULT_TYPE visitBillSecurityDefinition(final BillSecurityDefinition bill, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBillSecurityDefinition()");
  }

  @Override
  public RESULT_TYPE visitBillSecurityDefinition(final BillSecurityDefinition bill) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBillSecurityDefinition()");
  }

  @Override
  public RESULT_TYPE visitBillTransactionDefinition(final BillTransactionDefinition bill, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBillTransactionDefinition()");
  }

  @Override
  public RESULT_TYPE visitBillTransactionDefinition(final BillTransactionDefinition bill) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBillTransactionDefinition()");
  }

  @Override
  public RESULT_TYPE visitCashDefinition(final CashDefinition cash, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCashDefinition()");
  }

  @Override
  public RESULT_TYPE visitCashDefinition(final CashDefinition cash) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCashDefinition()");
  }

  @Override
  public RESULT_TYPE visitDepositIborDefinition(final DepositIborDefinition deposit, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitDepositIborDefinition()");
  }

  @Override
  public RESULT_TYPE visitDepositIborDefinition(final DepositIborDefinition deposit) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitDepositIborDefinition()");
  }

  @Override
  public RESULT_TYPE visitDepositCounterpartDefinition(final DepositCounterpartDefinition deposit, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitDepositCounterpartDefinition()");
  }

  @Override
  public RESULT_TYPE visitDepositCounterpartDefinition(final DepositCounterpartDefinition deposit) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitDepositCounterpartDefinition()");
  }

  @Override
  public RESULT_TYPE visitDepositZeroDefinition(final DepositZeroDefinition deposit, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitDepositZeroDefinition()");
  }

  @Override
  public RESULT_TYPE visitDepositZeroDefinition(final DepositZeroDefinition deposit) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitDepositZeroDefinition()");
  }

  @Override
  public RESULT_TYPE visitForwardRateAgreementDefinition(final ForwardRateAgreementDefinition fra, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForwardRateAgreementDefinition()");
  }

  @Override
  public RESULT_TYPE visitForwardRateAgreementDefinition(final ForwardRateAgreementDefinition fra) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForwardRateAgreementDefinition()");
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureSecurityDefinition(final InterestRateFutureDefinition future, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureDefinition()");
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureSecurityDefinition(final InterestRateFutureDefinition future) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureDefinition()");
  }

  @Override
  public RESULT_TYPE visitFederalFundsFutureSecurityDefinition(final FederalFundsFutureSecurityDefinition future, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFederalFundsFutureSecurityDefinition()");
  }

  @Override
  public RESULT_TYPE visitFederalFundsFutureSecurityDefinition(final FederalFundsFutureSecurityDefinition future) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFederalFundsFutureSecurityDefinition()");
  }

  @Override
  public RESULT_TYPE visitFederalFundsFutureTransactionDefinition(final FederalFundsFutureTransactionDefinition future, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFederalFundsFutureTransactionDefinition()");
  }

  @Override
  public RESULT_TYPE visitFederalFundsFutureTransactionDefinition(final FederalFundsFutureTransactionDefinition future) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFederalFundsFutureTransactionDefinition()");
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionPremiumSecurityDefinition(final InterestRateFutureOptionPremiumSecurityDefinition future, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureOptionPremiumSecurityDefinition()");
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionPremiumSecurityDefinition(final InterestRateFutureOptionPremiumSecurityDefinition future) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureOptionPremiumSecurityDefinition()");
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionPremiumTransactionDefinition(final InterestRateFutureOptionPremiumTransactionDefinition future, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureOptionPremiumTransactionDefinition()");
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionPremiumTransactionDefinition(final InterestRateFutureOptionPremiumTransactionDefinition future) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureOptionPremiumTransactionDefinition()");
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionMarginSecurityDefinition(final InterestRateFutureOptionMarginSecurityDefinition future, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureOptionMarginSecurityDefinition()");
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionMarginSecurityDefinition(final InterestRateFutureOptionMarginSecurityDefinition future) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureOptionMarginSecurityDefinition()");
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionMarginTransactionDefinition(final InterestRateFutureOptionMarginTransactionDefinition future, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureOptionMarginTransactionDefinition()");
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionMarginTransactionDefinition(final InterestRateFutureOptionMarginTransactionDefinition future) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureOptionMarginTransactionDefinition()");
  }

  @Override
  public RESULT_TYPE visitPaymentFixedDefinition(final PaymentFixedDefinition payment, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitPaymentFixedDefinition()");
  }

  @Override
  public RESULT_TYPE visitPaymentFixedDefinition(final PaymentFixedDefinition payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitPaymentFixedDefinition()");
  }

  @Override
  public RESULT_TYPE visitCouponFixedDefinition(final CouponFixedDefinition payment, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponFixedDefinition()");
  }

  @Override
  public RESULT_TYPE visitCouponFixedDefinition(final CouponFixedDefinition payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponFixedDefinition()");
  }

  @Override
  public RESULT_TYPE visitCouponIborDefinition(final CouponIborDefinition payment, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponIborDefinition()");
  }

  @Override
  public RESULT_TYPE visitCouponIborDefinition(final CouponIborDefinition payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponIborDefinition()");
  }

  @Override
  public RESULT_TYPE visitCouponIborSpread(final CouponIborSpreadDefinition payment, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponIborSpreadDefinition()");
  }

  @Override
  public RESULT_TYPE visitCouponIborSpread(final CouponIborSpreadDefinition payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponIborSpreadDefinition()");
  }

  @Override
  public RESULT_TYPE visitCouponIborGearing(final CouponIborGearingDefinition payment, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponIborGearingDefinition()");
  }

  @Override
  public RESULT_TYPE visitCouponIborGearing(final CouponIborGearingDefinition payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponIborGearingDefinition()");
  }

  @Override
  public RESULT_TYPE visitCouponIborCompounded(final CouponIborCompoundedDefinition payment, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponIborCompoundedDefinition()");
  }

  @Override
  public RESULT_TYPE visitCouponIborCompounded(final CouponIborCompoundedDefinition payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponIborCompoundedDefinition()");
  }

  @Override
  public RESULT_TYPE visitCouponIborRatchet(final CouponIborRatchetDefinition payment, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponIborRatchetDefinition()");
  }

  @Override
  public RESULT_TYPE visitCouponIborRatchet(final CouponIborRatchetDefinition payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponIborRatchetDefinition()");
  }

  @Override
  public RESULT_TYPE visitCapFloorIbor(final CapFloorIborDefinition payment, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCapFloorIborDefinition()");
  }

  @Override
  public RESULT_TYPE visitCapFloorIbor(final CapFloorIborDefinition payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCapFloorIborDefinition()");
  }

  @Override
  public RESULT_TYPE visitCouponOISSimplified(final CouponOISSimplifiedDefinition payment, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponOISSimplifiedDefinition()");
  }

  @Override
  public RESULT_TYPE visitCouponOISSimplified(final CouponOISSimplifiedDefinition payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponOISSimplifiedDefinition()");
  }

  @Override
  public RESULT_TYPE visitCouponOIS(final CouponOISDefinition payment, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponOISDefinition()");
  }

  @Override
  public RESULT_TYPE visitCouponOIS(final CouponOISDefinition payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponOISDefinition()");
  }

  @Override
  public RESULT_TYPE visitCouponCMS(final CouponCMSDefinition payment, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponCMSDefinition()");
  }

  @Override
  public RESULT_TYPE visitCouponCMS(final CouponCMSDefinition payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponCMSDefinition()");
  }

  @Override
  public RESULT_TYPE visitCapFloorCMS(final CapFloorCMSDefinition payment, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCapFloorCMSDefinition()");
  }

  @Override
  public RESULT_TYPE visitCapFloorCMS(final CapFloorCMSDefinition payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCapFloorCMSDefinition()");
  }

  @Override
  public RESULT_TYPE visitCapFloorCMSSpread(final CapFloorCMSSpreadDefinition payment, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCapFloorCMSSpreadDefinition()");
  }

  @Override
  public RESULT_TYPE visitCapFloorCMSSpread(final CapFloorCMSSpreadDefinition payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCapFloorCMSSpreadDefinition()");
  }

  @Override
  public RESULT_TYPE visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitAnnuityDefinition()");
  }

  @Override
  public RESULT_TYPE visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitAnnuityDefinition()");
  }

  @Override
  public RESULT_TYPE visitSwapDefinition(final SwapDefinition swap, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwapDefinition()");
  }

  @Override
  public RESULT_TYPE visitSwapDefinition(final SwapDefinition swap) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwapDefinition()");
  }

  @Override
  public RESULT_TYPE visitSwapFixedIborDefinition(final SwapFixedIborDefinition swap, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwapFixedIborDefinition()");
  }

  @Override
  public RESULT_TYPE visitSwapFixedIborDefinition(final SwapFixedIborDefinition swap) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwapFixedIborDefinition()");
  }

  @Override
  public RESULT_TYPE visitSwapFixedIborSpreadDefinition(final SwapFixedIborSpreadDefinition swap, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwapFixedIborSpreadDefinition()");
  }

  @Override
  public RESULT_TYPE visitSwapFixedIborSpreadDefinition(final SwapFixedIborSpreadDefinition swap) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwapFixedIborSpreadDefinition()");
  }

  @Override
  public RESULT_TYPE visitSwapIborIborDefinition(final SwapIborIborDefinition swap, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwapIborIborDefinition()");
  }

  @Override
  public RESULT_TYPE visitSwapIborIborDefinition(final SwapIborIborDefinition swap) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwapIborIborDefinition()");
  }

  @Override
  public RESULT_TYPE visitSwapXCcyIborIborDefinition(final SwapXCcyIborIborDefinition swap, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwapXccyIborIborDefinition()");
  }

  @Override
  public RESULT_TYPE visitSwapXCcyIborIborDefinition(final SwapXCcyIborIborDefinition swap) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwapXccyIborIborDefinition()");
  }

  @Override
  public RESULT_TYPE visitSwaptionCashFixedIborDefinition(final SwaptionCashFixedIborDefinition swaption, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwaptionCashFixedIborDefinition()");
  }

  @Override
  public RESULT_TYPE visitSwaptionCashFixedIborDefinition(final SwaptionCashFixedIborDefinition swaption) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwaptionCashFixedIborDefinition()");
  }

  @Override
  public RESULT_TYPE visitSwaptionPhysicalFixedIborDefinition(final SwaptionPhysicalFixedIborDefinition swaption, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwaptionPhysicalFixedIborDefinition()");
  }

  @Override
  public RESULT_TYPE visitSwaptionPhysicalFixedIborDefinition(final SwaptionPhysicalFixedIborDefinition swaption) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwaptionPhysicalFixedIborDefinition()");
  }

  @Override
  public RESULT_TYPE visitSwaptionPhysicalFixedIborSpreadDefinition(final SwaptionPhysicalFixedIborSpreadDefinition swaption, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwaptionPhysicalFixedIborSpreadDefinition()");
  }

  @Override
  public RESULT_TYPE visitSwaptionPhysicalFixedIborSpreadDefinition(final SwaptionPhysicalFixedIborSpreadDefinition swaption) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwaptionPhysicalFixedIborSpreadDefinition()");
  }

  @Override
  public RESULT_TYPE visitSwaptionBermudaFixedIborDefinition(final SwaptionBermudaFixedIborDefinition swaption, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwaptionBermudaFixedIborDefinition()");
  }

  @Override
  public RESULT_TYPE visitSwaptionBermudaFixedIborDefinition(final SwaptionBermudaFixedIborDefinition swaption) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwaptionBermudaFixedIborDefinition()");
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponFirstOfMonth(final CouponInflationZeroCouponMonthlyDefinition coupon, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponInflationZeroCouponFirstOfMonth()");
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponFirstOfMonth(final CouponInflationZeroCouponMonthlyDefinition coupon) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponInflationZeroCouponFirstOfMonth()");
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolationDefinition coupon, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponInflationZeroCouponInterpolation()");
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolationDefinition coupon) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponInflationZeroCouponInterpolation()");
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponMonthlyGearing(final CouponInflationZeroCouponMonthlyGearingDefinition coupon, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponInflationZeroCouponMonthlyGearing()");
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponMonthlyGearing(final CouponInflationZeroCouponMonthlyGearingDefinition coupon) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponInflationZeroCouponMonthlyGearing()");
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponInterpolationGearing(final CouponInflationZeroCouponInterpolationGearingDefinition coupon, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponInflationZeroCouponInterpolationGearing()");
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponInterpolationGearing(final CouponInflationZeroCouponInterpolationGearingDefinition coupon) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponInflationZeroCouponInterpolationGearing()");
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurityDefinition<?> bond, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondCapitalIndexedSecurity()");
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurityDefinition<?> bond) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondCapitalIndexedSecurity()");
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransactionDefinition<?> bond, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondCapitalIndexedTransaction()");
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransactionDefinition<?> bond) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondCapitalIndexedTransaction()");
  }

  @Override
  public RESULT_TYPE visitForexDefinition(final ForexDefinition fx, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexDefinition()");
  }

  @Override
  public RESULT_TYPE visitForexDefinition(final ForexDefinition fx) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexDefinition()");
  }

  @Override
  public RESULT_TYPE visitForexSwapDefinition(final ForexSwapDefinition fx, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexSwapDefinition()");
  }

  @Override
  public RESULT_TYPE visitForexSwapDefinition(final ForexSwapDefinition fx) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexSwapDefinition()");
  }

  @Override
  public RESULT_TYPE visitForexOptionVanillaDefinition(final ForexOptionVanillaDefinition fx, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexOptionVanillaDefinition()");
  }

  @Override
  public RESULT_TYPE visitForexOptionVanillaDefinition(final ForexOptionVanillaDefinition fx) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexOptionVanillaDefinition()");
  }

  @Override
  public RESULT_TYPE visitForexOptionSingleBarrierDefiniton(final ForexOptionSingleBarrierDefinition fx, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexOptionSingleBarrierDefinition()");
  }

  @Override
  public RESULT_TYPE visitForexOptionSingleBarrierDefiniton(final ForexOptionSingleBarrierDefinition fx) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexOptionSingleBarrierDefinition()");
  }

  @Override
  public RESULT_TYPE visitForexNonDeliverableForwardDefinition(final ForexNonDeliverableForwardDefinition ndf, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexNonDeliverableForwardDefinition()");
  }

  @Override
  public RESULT_TYPE visitForexNonDeliverableForwardDefinition(final ForexNonDeliverableForwardDefinition ndf) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexNonDeliverableForwardDefinition()");
  }

  @Override
  public RESULT_TYPE visitForexNonDeliverableOptionDefinition(final ForexNonDeliverableOptionDefinition ndo, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexNonDeliverableOptionDefinition()");
  }

  @Override
  public RESULT_TYPE visitForexNonDeliverableOptionDefinition(final ForexNonDeliverableOptionDefinition ndo) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexNonDeliverableOptionDefinition()");
  }

  @Override
  public RESULT_TYPE visitForexOptionDigitalDefinition(final ForexOptionDigitalDefinition fx, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexOptionDigitalDefinition()");
  }

  @Override
  public RESULT_TYPE visitForexOptionDigitalDefinition(final ForexOptionDigitalDefinition fx) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexOptionDigitalDefinition()");
  }

  @Override
  public RESULT_TYPE visitDeliverableSwapFuturesSecurityDefinition(final DeliverableSwapFuturesSecurityDefinition futures, final DATA_TYPE data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitDeliverableSwpaFuturesSecurityDefinition()");
  }

  @Override
  public RESULT_TYPE visitDeliverableSwapFuturesSecurityDefinition(final DeliverableSwapFuturesSecurityDefinition futures) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitDeliverableSwpaFuturesSecurityDefinition()");
  }

}
