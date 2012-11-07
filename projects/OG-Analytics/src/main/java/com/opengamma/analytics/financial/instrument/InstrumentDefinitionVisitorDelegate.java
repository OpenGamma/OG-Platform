/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

import com.opengamma.analytics.financial.commodity.definition.AgricultureForwardDefinition;
import com.opengamma.analytics.financial.commodity.definition.AgricultureFutureDefinition;
import com.opengamma.analytics.financial.commodity.definition.AgricultureFutureOptionDefinition;
import com.opengamma.analytics.financial.commodity.definition.EnergyForwardDefinition;
import com.opengamma.analytics.financial.commodity.definition.EnergyFutureDefinition;
import com.opengamma.analytics.financial.commodity.definition.EnergyFutureOptionDefinition;
import com.opengamma.analytics.financial.commodity.definition.MetalForwardDefinition;
import com.opengamma.analytics.financial.commodity.definition.MetalFutureDefinition;
import com.opengamma.analytics.financial.commodity.definition.MetalFutureOptionDefinition;
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
import com.opengamma.analytics.financial.instrument.cds.ISDACDSDefinition;
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
 * 
 */
class InstrumentDefinitionVisitorDelegate<DATA_TYPE, RESULT_TYPE> implements InstrumentDefinitionVisitor<DATA_TYPE, RESULT_TYPE> {
  private final InstrumentDefinitionVisitor<DATA_TYPE, RESULT_TYPE> _delegate;
  
  public InstrumentDefinitionVisitorDelegate(final InstrumentDefinitionVisitor<DATA_TYPE, RESULT_TYPE> delegate) {
    ArgumentChecker.notNull(delegate, "delegate");
    _delegate = delegate;
  }
  
  @Override
  public RESULT_TYPE visit(InstrumentDefinition<?> definition, DATA_TYPE data) {
    return _delegate.visit(definition, data);
  }

  @Override
  public RESULT_TYPE visit(InstrumentDefinition<?> definition) {
    return _delegate.visit(definition);
  }

  @Override
  public RESULT_TYPE visitBondFixedSecurityDefinition(BondFixedSecurityDefinition bond, DATA_TYPE data) {
    return _delegate.visitBondFixedSecurityDefinition(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFixedSecurityDefinition(BondFixedSecurityDefinition bond) {
    return _delegate.visitBondFixedSecurityDefinition(bond);
  }

  @Override
  public RESULT_TYPE visitBondFixedTransactionDefinition(BondFixedTransactionDefinition bond, DATA_TYPE data) {
    return _delegate.visitBondFixedTransactionDefinition(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFixedTransactionDefinition(BondFixedTransactionDefinition bond) {
    return _delegate.visitBondFixedTransactionDefinition(bond);
  }

  @Override
  public RESULT_TYPE visitBondFutureSecurityDefinition(BondFutureDefinition bond, DATA_TYPE data) {
    return _delegate.visitBondFutureSecurityDefinition(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFutureOptionPremiumSecurityDefinition(BondFutureOptionPremiumSecurityDefinition bond, DATA_TYPE data) {
    return _delegate.visitBondFutureOptionPremiumSecurityDefinition(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFutureOptionPremiumTransactionDefinition(BondFutureOptionPremiumTransactionDefinition bond, DATA_TYPE data) {
    return _delegate.visitBondFutureOptionPremiumTransactionDefinition(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondIborTransactionDefinition(BondIborTransactionDefinition bond, DATA_TYPE data) {
    return _delegate.visitBondIborTransactionDefinition(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondIborTransactionDefinition(BondIborTransactionDefinition bond) {
    return _delegate.visitBondIborTransactionDefinition(bond);
  }

  @Override
  public RESULT_TYPE visitBondIborSecurityDefinition(BondIborSecurityDefinition bond, DATA_TYPE data) {
    return _delegate.visitBondIborSecurityDefinition(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondIborSecurityDefinition(BondIborSecurityDefinition bond) {
    return _delegate.visitBondIborSecurityDefinition(bond);
  }

  @Override
  public RESULT_TYPE visitBillSecurityDefinition(BillSecurityDefinition bill, DATA_TYPE data) {
    return _delegate.visitBillSecurityDefinition(bill, data);
  }

  @Override
  public RESULT_TYPE visitBillSecurityDefinition(BillSecurityDefinition bill) {
    return _delegate.visitBillSecurityDefinition(bill);
  }

  @Override
  public RESULT_TYPE visitBillTransactionDefinition(BillTransactionDefinition bill, DATA_TYPE data) {
    return _delegate.visitBillTransactionDefinition(bill, data);
  }

  @Override
  public RESULT_TYPE visitBillTransactionDefinition(BillTransactionDefinition bill) {
    return _delegate.visitBillTransactionDefinition(bill);
  }

  @Override
  public RESULT_TYPE visitCashDefinition(CashDefinition cash, DATA_TYPE data) {
    return _delegate.visitCashDefinition(cash, data);
  }

  @Override
  public RESULT_TYPE visitCashDefinition(CashDefinition cash) {
    return _delegate.visitCashDefinition(cash);
  }

  @Override
  public RESULT_TYPE visitDepositIborDefinition(DepositIborDefinition deposit, DATA_TYPE data) {
    return _delegate.visitDepositIborDefinition(deposit, data);
  }

  @Override
  public RESULT_TYPE visitDepositIborDefinition(DepositIborDefinition deposit) {
    return _delegate.visitDepositIborDefinition(deposit);
  }

  @Override
  public RESULT_TYPE visitDepositCounterpartDefinition(DepositCounterpartDefinition deposit, DATA_TYPE data) {
    return _delegate.visitDepositCounterpartDefinition(deposit, data);
  }

  @Override
  public RESULT_TYPE visitDepositCounterpartDefinition(DepositCounterpartDefinition deposit) {
    return _delegate.visitDepositCounterpartDefinition(deposit);
  }

  @Override
  public RESULT_TYPE visitDepositZeroDefinition(DepositZeroDefinition deposit, DATA_TYPE data) {
    return _delegate.visitDepositZeroDefinition(deposit, data);
  }

  @Override
  public RESULT_TYPE visitDepositZeroDefinition(DepositZeroDefinition deposit) {
    return _delegate.visitDepositZeroDefinition(deposit);
  }

  @Override
  public RESULT_TYPE visitForwardRateAgreementDefinition(ForwardRateAgreementDefinition fra, DATA_TYPE data) {
    return _delegate.visitForwardRateAgreementDefinition(fra, data);
  }

  @Override
  public RESULT_TYPE visitForwardRateAgreementDefinition(ForwardRateAgreementDefinition fra) {
    return _delegate.visitForwardRateAgreementDefinition(fra);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureSecurityDefinition(InterestRateFutureDefinition future, DATA_TYPE data) {
    return _delegate.visitInterestRateFutureSecurityDefinition(future, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureSecurityDefinition(InterestRateFutureDefinition future) {
    return _delegate.visitInterestRateFutureSecurityDefinition(future);
  }

  @Override
  public RESULT_TYPE visitFederalFundsFutureSecurityDefinition(FederalFundsFutureSecurityDefinition future, DATA_TYPE data) {
    return _delegate.visitFederalFundsFutureSecurityDefinition(future, data);
  }

  @Override
  public RESULT_TYPE visitFederalFundsFutureSecurityDefinition(FederalFundsFutureSecurityDefinition future) {
    return _delegate.visitFederalFundsFutureSecurityDefinition(future);
  }

  @Override
  public RESULT_TYPE visitFederalFundsFutureTransactionDefinition(FederalFundsFutureTransactionDefinition future, DATA_TYPE data) {
    return _delegate.visitFederalFundsFutureTransactionDefinition(future, data);
  }

  @Override
  public RESULT_TYPE visitFederalFundsFutureTransactionDefinition(FederalFundsFutureTransactionDefinition future) {
    return _delegate.visitFederalFundsFutureTransactionDefinition(future);
  }

  @Override
  public RESULT_TYPE visitBondFutureSecurityDefinition(BondFutureDefinition bond) {
    return _delegate.visitBondFutureSecurityDefinition(bond);
  }

  @Override
  public RESULT_TYPE visitDeliverableSwapFuturesSecurityDefinition(DeliverableSwapFuturesSecurityDefinition futures, DATA_TYPE data) {
    return _delegate.visitDeliverableSwapFuturesSecurityDefinition(futures, data);
  }

  @Override
  public RESULT_TYPE visitDeliverableSwapFuturesSecurityDefinition(DeliverableSwapFuturesSecurityDefinition futures) {
    return _delegate.visitDeliverableSwapFuturesSecurityDefinition(futures);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionPremiumSecurityDefinition(InterestRateFutureOptionPremiumSecurityDefinition future, DATA_TYPE data) {
    return _delegate.visitInterestRateFutureOptionPremiumSecurityDefinition(future, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionPremiumSecurityDefinition(InterestRateFutureOptionPremiumSecurityDefinition future) {
    return _delegate.visitInterestRateFutureOptionPremiumSecurityDefinition(future);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionPremiumTransactionDefinition(InterestRateFutureOptionPremiumTransactionDefinition future, DATA_TYPE data) {
    return _delegate.visitInterestRateFutureOptionPremiumTransactionDefinition(future, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionPremiumTransactionDefinition(InterestRateFutureOptionPremiumTransactionDefinition future) {
    return _delegate.visitInterestRateFutureOptionPremiumTransactionDefinition(future);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionMarginSecurityDefinition(InterestRateFutureOptionMarginSecurityDefinition future, DATA_TYPE data) {
    return _delegate.visitInterestRateFutureOptionMarginSecurityDefinition(future, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionMarginSecurityDefinition(InterestRateFutureOptionMarginSecurityDefinition future) {
    return _delegate.visitInterestRateFutureOptionMarginSecurityDefinition(future);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionMarginTransactionDefinition(InterestRateFutureOptionMarginTransactionDefinition future, DATA_TYPE data) {
    return _delegate.visitInterestRateFutureOptionMarginTransactionDefinition(future, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionMarginTransactionDefinition(InterestRateFutureOptionMarginTransactionDefinition future) {
    return _delegate.visitInterestRateFutureOptionMarginTransactionDefinition(future);
  }

  @Override
  public RESULT_TYPE visitBondFutureOptionPremiumSecurityDefinition(BondFutureOptionPremiumSecurityDefinition bond) {
    return _delegate.visitBondFutureOptionPremiumSecurityDefinition(bond);
  }

  @Override
  public RESULT_TYPE visitBondFutureOptionPremiumTransactionDefinition(BondFutureOptionPremiumTransactionDefinition bond) {
    return _delegate.visitBondFutureOptionPremiumTransactionDefinition(bond, null);
  }

  @Override
  public RESULT_TYPE visitPaymentFixedDefinition(PaymentFixedDefinition payment, DATA_TYPE data) {
    return _delegate.visitPaymentFixedDefinition(payment, data);
  }

  @Override
  public RESULT_TYPE visitPaymentFixedDefinition(PaymentFixedDefinition payment) {
    return _delegate.visitPaymentFixedDefinition(payment);
  }

  @Override
  public RESULT_TYPE visitCouponFixedDefinition(CouponFixedDefinition payment, DATA_TYPE data) {
    return _delegate.visitCouponFixedDefinition(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponFixedDefinition(CouponFixedDefinition payment) {
    return _delegate.visitCouponFixedDefinition(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborDefinition(CouponIborDefinition payment, DATA_TYPE data) {
    return _delegate.visitCouponIborDefinition(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborDefinition(CouponIborDefinition payment) {
    return _delegate.visitCouponIborDefinition(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborSpreadDefinition(CouponIborSpreadDefinition payment, DATA_TYPE data) {
    return _delegate.visitCouponIborSpreadDefinition(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborSpreadDefinition(CouponIborSpreadDefinition payment) {
    return _delegate.visitCouponIborSpreadDefinition(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborGearingDefinition(CouponIborGearingDefinition payment, DATA_TYPE data) {
    return _delegate.visitCouponIborGearingDefinition(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborGearingDefinition(CouponIborGearingDefinition payment) {
    return _delegate.visitCouponIborGearingDefinition(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundedDefinition(CouponIborCompoundedDefinition payment, DATA_TYPE data) {
    return _delegate.visitCouponIborCompoundedDefinition(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundedDefinition(CouponIborCompoundedDefinition payment) {
    return _delegate.visitCouponIborCompoundedDefinition(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborRatchetDefinition(CouponIborRatchetDefinition payment, DATA_TYPE data) {
    return _delegate.visitCouponIborRatchetDefinition(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborRatchetDefinition(CouponIborRatchetDefinition payment) {
    return _delegate.visitCouponIborRatchetDefinition(payment);
  }

  @Override
  public RESULT_TYPE visitCapFloorIborDefinition(CapFloorIborDefinition payment, DATA_TYPE data) {
    return _delegate.visitCapFloorIborDefinition(payment, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorIborDefinition(CapFloorIborDefinition payment) {
    return _delegate.visitCapFloorIborDefinition(payment);
  }

  @Override
  public RESULT_TYPE visitCouponOISSimplifiedDefinition(CouponOISSimplifiedDefinition payment, DATA_TYPE data) {
    return _delegate.visitCouponOISSimplifiedDefinition(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponOISSimplifiedDefinition(CouponOISSimplifiedDefinition payment) {
    return _delegate.visitCouponOISSimplifiedDefinition(payment);
  }

  @Override
  public RESULT_TYPE visitCouponOISDefinition(CouponOISDefinition payment, DATA_TYPE data) {
    return _delegate.visitCouponOISDefinition(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponOISDefinition(CouponOISDefinition payment) {
    return _delegate.visitCouponOISDefinition(payment);
  }

  @Override
  public RESULT_TYPE visitCouponCMSDefinition(CouponCMSDefinition payment, DATA_TYPE data) {
    return _delegate.visitCouponCMSDefinition(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponCMSDefinition(CouponCMSDefinition payment) {
    return _delegate.visitCouponCMSDefinition(payment);
  }

  @Override
  public RESULT_TYPE visitCapFloorCMSDefinition(CapFloorCMSDefinition payment, DATA_TYPE data) {
    return _delegate.visitCapFloorCMSDefinition(payment, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorCMSDefinition(CapFloorCMSDefinition payment) {
    return _delegate.visitCapFloorCMSDefinition(payment);
  }

  @Override
  public RESULT_TYPE visitCapFloorCMSSpreadDefinition(CapFloorCMSSpreadDefinition payment, DATA_TYPE data) {
    return _delegate.visitCapFloorCMSSpreadDefinition(payment, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorCMSSpreadDefinition(CapFloorCMSSpreadDefinition payment) {
    return _delegate.visitCapFloorCMSSpreadDefinition(payment);
  }

  @Override
  public RESULT_TYPE visitAnnuityDefinition(AnnuityDefinition<? extends PaymentDefinition> annuity, DATA_TYPE data) {
    return _delegate.visitAnnuityDefinition(annuity, data);
  }

  @Override
  public RESULT_TYPE visitAnnuityDefinition(AnnuityDefinition<? extends PaymentDefinition> annuity) {
    return _delegate.visitAnnuityDefinition(annuity);
  }

  @Override
  public RESULT_TYPE visitSwapDefinition(SwapDefinition swap, DATA_TYPE data) {
    return _delegate.visitSwapDefinition(swap, data);
  }

  @Override
  public RESULT_TYPE visitSwapDefinition(SwapDefinition swap) {
    return _delegate.visitSwapDefinition(swap);
  }

  @Override
  public RESULT_TYPE visitSwapFixedIborDefinition(SwapFixedIborDefinition swap, DATA_TYPE data) {
    return _delegate.visitSwapFixedIborDefinition(swap, data);
  }

  @Override
  public RESULT_TYPE visitSwapFixedIborDefinition(SwapFixedIborDefinition swap) {
    return _delegate.visitSwapFixedIborDefinition(swap);
  }

  @Override
  public RESULT_TYPE visitSwapFixedIborSpreadDefinition(SwapFixedIborSpreadDefinition swap, DATA_TYPE data) {
    return _delegate.visitSwapFixedIborSpreadDefinition(swap, data);
  }

  @Override
  public RESULT_TYPE visitSwapFixedIborSpreadDefinition(SwapFixedIborSpreadDefinition swap) {
    return _delegate.visitSwapFixedIborSpreadDefinition(swap);
  }

  @Override
  public RESULT_TYPE visitSwapIborIborDefinition(SwapIborIborDefinition swap, DATA_TYPE data) {
    return _delegate.visitSwapIborIborDefinition(swap, data);
  }

  @Override
  public RESULT_TYPE visitSwapIborIborDefinition(SwapIborIborDefinition swap) {
    return _delegate.visitSwapIborIborDefinition(swap);
  }

  @Override
  public RESULT_TYPE visitSwapXCcyIborIborDefinition(SwapXCcyIborIborDefinition swap, DATA_TYPE data) {
    return _delegate.visitSwapXCcyIborIborDefinition(swap, data);
  }

  @Override
  public RESULT_TYPE visitSwapXCcyIborIborDefinition(SwapXCcyIborIborDefinition swap) {
    return _delegate.visitSwapXCcyIborIborDefinition(swap);
  }

  @Override
  public RESULT_TYPE visitSwaptionCashFixedIborDefinition(SwaptionCashFixedIborDefinition swaption, DATA_TYPE data) {
    return _delegate.visitSwaptionCashFixedIborDefinition(swaption, data);
  }

  @Override
  public RESULT_TYPE visitSwaptionCashFixedIborDefinition(SwaptionCashFixedIborDefinition swaption) {
    return _delegate.visitSwaptionCashFixedIborDefinition(swaption);
  }

  @Override
  public RESULT_TYPE visitSwaptionPhysicalFixedIborDefinition(SwaptionPhysicalFixedIborDefinition swaption, DATA_TYPE data) {
    return _delegate.visitSwaptionPhysicalFixedIborDefinition(swaption, data);
  }

  @Override
  public RESULT_TYPE visitSwaptionPhysicalFixedIborDefinition(SwaptionPhysicalFixedIborDefinition swaption) {
    return _delegate.visitSwaptionPhysicalFixedIborDefinition(swaption);
  }

  @Override
  public RESULT_TYPE visitSwaptionPhysicalFixedIborSpreadDefinition(SwaptionPhysicalFixedIborSpreadDefinition swaption, DATA_TYPE data) {
    return _delegate.visitSwaptionPhysicalFixedIborSpreadDefinition(swaption, data);
  }

  @Override
  public RESULT_TYPE visitSwaptionPhysicalFixedIborSpreadDefinition(SwaptionPhysicalFixedIborSpreadDefinition swaption) {
    return _delegate.visitSwaptionPhysicalFixedIborSpreadDefinition(swaption);
  }

  @Override
  public RESULT_TYPE visitSwaptionBermudaFixedIborDefinition(SwaptionBermudaFixedIborDefinition swaption, DATA_TYPE data) {
    return _delegate.visitSwaptionBermudaFixedIborDefinition(swaption, data);
  }

  @Override
  public RESULT_TYPE visitSwaptionBermudaFixedIborDefinition(SwaptionBermudaFixedIborDefinition swaption) {
    return _delegate.visitSwaptionBermudaFixedIborDefinition(swaption);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponFirstOfMonth(CouponInflationZeroCouponMonthlyDefinition coupon, DATA_TYPE data) {
    return _delegate.visitCouponInflationZeroCouponFirstOfMonth(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponFirstOfMonth(CouponInflationZeroCouponMonthlyDefinition coupon) {
    return _delegate.visitCouponInflationZeroCouponFirstOfMonth(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponInterpolation(CouponInflationZeroCouponInterpolationDefinition coupon, DATA_TYPE data) {
    return _delegate.visitCouponInflationZeroCouponInterpolation(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponInterpolation(CouponInflationZeroCouponInterpolationDefinition coupon) {
    return _delegate.visitCouponInflationZeroCouponInterpolation(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponMonthlyGearing(CouponInflationZeroCouponMonthlyGearingDefinition coupon, DATA_TYPE data) {
    return _delegate.visitCouponInflationZeroCouponMonthlyGearing(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponMonthlyGearing(CouponInflationZeroCouponMonthlyGearingDefinition coupon) {
    return _delegate.visitCouponInflationZeroCouponMonthlyGearing(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponInterpolationGearing(CouponInflationZeroCouponInterpolationGearingDefinition coupon, DATA_TYPE data) {
    return _delegate.visitCouponInflationZeroCouponInterpolationGearing(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponInterpolationGearing(CouponInflationZeroCouponInterpolationGearingDefinition coupon) {
    return _delegate.visitCouponInflationZeroCouponInterpolationGearing(coupon);
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedSecurity(BondCapitalIndexedSecurityDefinition<?> bond, DATA_TYPE data) {
    return _delegate.visitBondCapitalIndexedSecurity(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedSecurity(BondCapitalIndexedSecurityDefinition<?> bond) {
    return _delegate.visitBondCapitalIndexedSecurity(bond);
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedTransaction(BondCapitalIndexedTransactionDefinition<?> bond, DATA_TYPE data) {
    return _delegate.visitBondCapitalIndexedTransaction(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedTransaction(BondCapitalIndexedTransactionDefinition<?> bond) {
    return _delegate.visitBondCapitalIndexedTransaction(bond);
  }

  @Override
  public RESULT_TYPE visitCDSDefinition(ISDACDSDefinition cds, DATA_TYPE data) {
    return _delegate.visitCDSDefinition(cds, data);
  }

  @Override
  public RESULT_TYPE visitCDSDefinition(ISDACDSDefinition cds) {
    return _delegate.visitCDSDefinition(cds);
  }

  @Override
  public RESULT_TYPE visitForexDefinition(ForexDefinition fx, DATA_TYPE data) {
    return _delegate.visitForexDefinition(fx, data);
  }

  @Override
  public RESULT_TYPE visitForexDefinition(ForexDefinition fx) {
    return _delegate.visitForexDefinition(fx);
  }

  @Override
  public RESULT_TYPE visitForexSwapDefinition(ForexSwapDefinition fx, DATA_TYPE data) {
    return _delegate.visitForexSwapDefinition(fx, data);
  }

  @Override
  public RESULT_TYPE visitForexSwapDefinition(ForexSwapDefinition fx) {
    return _delegate.visitForexSwapDefinition(fx);
  }

  @Override
  public RESULT_TYPE visitForexOptionVanillaDefinition(ForexOptionVanillaDefinition fx, DATA_TYPE data) {
    return _delegate.visitForexOptionVanillaDefinition(fx, data);
  }

  @Override
  public RESULT_TYPE visitForexOptionVanillaDefinition(ForexOptionVanillaDefinition fx) {
    return _delegate.visitForexOptionVanillaDefinition(fx);
  }

  @Override
  public RESULT_TYPE visitForexOptionSingleBarrierDefiniton(ForexOptionSingleBarrierDefinition fx, DATA_TYPE data) {
    return _delegate.visitForexOptionSingleBarrierDefiniton(fx, data);
  }

  @Override
  public RESULT_TYPE visitForexOptionSingleBarrierDefiniton(ForexOptionSingleBarrierDefinition fx) {
    return _delegate.visitForexOptionSingleBarrierDefiniton(fx);
  }

  @Override
  public RESULT_TYPE visitForexNonDeliverableForwardDefinition(ForexNonDeliverableForwardDefinition ndf, DATA_TYPE data) {
    return _delegate.visitForexNonDeliverableForwardDefinition(ndf, data);
  }

  @Override
  public RESULT_TYPE visitForexNonDeliverableForwardDefinition(ForexNonDeliverableForwardDefinition ndf) {
    return _delegate.visitForexNonDeliverableForwardDefinition(ndf);
  }

  @Override
  public RESULT_TYPE visitForexNonDeliverableOptionDefinition(ForexNonDeliverableOptionDefinition ndo, DATA_TYPE data) {
    return _delegate.visitForexNonDeliverableOptionDefinition(ndo, data);
  }

  @Override
  public RESULT_TYPE visitForexNonDeliverableOptionDefinition(ForexNonDeliverableOptionDefinition ndo) {
    return _delegate.visitForexNonDeliverableOptionDefinition(ndo);
  }

  @Override
  public RESULT_TYPE visitForexOptionDigitalDefinition(ForexOptionDigitalDefinition fx, DATA_TYPE data) {
    return _delegate.visitForexOptionDigitalDefinition(fx, data);
  }

  @Override
  public RESULT_TYPE visitForexOptionDigitalDefinition(ForexOptionDigitalDefinition fx) {
    return _delegate.visitForexOptionDigitalDefinition(fx);
  }

  @Override
  public RESULT_TYPE visitMetalForwardDefinition(MetalForwardDefinition future, DATA_TYPE data) {
    return _delegate.visitMetalForwardDefinition(future);
  }

  @Override
  public RESULT_TYPE visitMetalForwardDefinition(MetalForwardDefinition future) {
    return _delegate.visitMetalForwardDefinition(future);
  }

  @Override
  public RESULT_TYPE visitMetalFutureDefinition(MetalFutureDefinition future, DATA_TYPE data) {
    return _delegate.visitMetalFutureDefinition(future);
  }

  @Override
  public RESULT_TYPE visitMetalFutureDefinition(MetalFutureDefinition future) {
    return _delegate.visitMetalFutureDefinition(future);
  }

  @Override
  public RESULT_TYPE visitMetalFutureOptionDefinition(MetalFutureOptionDefinition future, DATA_TYPE data) {
    return _delegate.visitMetalFutureOptionDefinition(future);
  }

  @Override
  public RESULT_TYPE visitMetalFutureOptionDefinition(MetalFutureOptionDefinition future) {
    return _delegate.visitMetalFutureOptionDefinition(future);
  }

  @Override
  public RESULT_TYPE visitAgricultureForwardDefinition(AgricultureForwardDefinition future, DATA_TYPE data) {
    return _delegate.visitAgricultureForwardDefinition(future);
  }

  @Override
  public RESULT_TYPE visitAgricultureForwardDefinition(AgricultureForwardDefinition future) {
    return _delegate.visitAgricultureForwardDefinition(future);
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureDefinition(AgricultureFutureDefinition future, DATA_TYPE data) {
    return _delegate.visitAgricultureFutureDefinition(future);
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureDefinition(AgricultureFutureDefinition future) {
    return _delegate.visitAgricultureFutureDefinition(future);
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureOptionDefinition(AgricultureFutureOptionDefinition future, DATA_TYPE data) {
    return _delegate.visitAgricultureFutureOptionDefinition(future);
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureOptionDefinition(AgricultureFutureOptionDefinition future) {
    return _delegate.visitAgricultureFutureOptionDefinition(future);
  }

  @Override
  public RESULT_TYPE visitEnergyForwardDefinition(EnergyForwardDefinition future, DATA_TYPE data) {
    return _delegate.visitEnergyForwardDefinition(future);
  }

  @Override
  public RESULT_TYPE visitEnergyForwardDefinition(EnergyForwardDefinition future) {
    return _delegate.visitEnergyForwardDefinition(future);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureDefinition(EnergyFutureDefinition future, DATA_TYPE data) {
    return _delegate.visitEnergyFutureDefinition(future);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureDefinition(EnergyFutureDefinition future) {
    return _delegate.visitEnergyFutureDefinition(future);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureOptionDefinition(EnergyFutureOptionDefinition future, DATA_TYPE data) {
    return _delegate.visitEnergyFutureOptionDefinition(future);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureOptionDefinition(EnergyFutureOptionDefinition future) {
    return _delegate.visitEnergyFutureOptionDefinition(future);
  }

}
