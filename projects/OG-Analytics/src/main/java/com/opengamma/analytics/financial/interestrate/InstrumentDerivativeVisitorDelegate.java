/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.commodity.derivative.AgricultureForward;
import com.opengamma.analytics.financial.commodity.derivative.AgricultureFuture;
import com.opengamma.analytics.financial.commodity.derivative.AgricultureFutureOption;
import com.opengamma.analytics.financial.commodity.derivative.EnergyForward;
import com.opengamma.analytics.financial.commodity.derivative.EnergyFuture;
import com.opengamma.analytics.financial.commodity.derivative.EnergyFutureOption;
import com.opengamma.analytics.financial.commodity.derivative.MetalForward;
import com.opengamma.analytics.financial.commodity.derivative.MetalFuture;
import com.opengamma.analytics.financial.commodity.derivative.MetalFutureOption;
import com.opengamma.analytics.financial.credit.cds.ISDACDSDerivative;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableForward;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableOption;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIbor;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborSpread;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborTransaction;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositCounterpart;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositIbor;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositZero;
import com.opengamma.analytics.financial.interestrate.fra.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.DeliverableSwapFuturesSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFuture;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponInterpolationGearing;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthlyGearing;
import com.opengamma.analytics.financial.interestrate.payments.ForexForward;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMSSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompounded;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponOIS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.CrossCurrencySwap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.FixedFloatSwap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.FloatingRateNote;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.TenorSwap;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionBermudaFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 * @param <DATA_TYPE> The type of the data
 * @param <RESULT_TYPE> The return type of the calculation
 */
public class InstrumentDerivativeVisitorDelegate<DATA_TYPE, RESULT_TYPE> implements InstrumentDerivativeVisitor<DATA_TYPE, RESULT_TYPE> {
  private final InstrumentDerivativeVisitor<DATA_TYPE, RESULT_TYPE> _delegate;

  public InstrumentDerivativeVisitorDelegate(final InstrumentDerivativeVisitor<DATA_TYPE, RESULT_TYPE> delegate) {
    ArgumentChecker.notNull(delegate, "delegate");
    _delegate = delegate;
  }

  @Override
  public RESULT_TYPE visitBondFixedSecurity(final BondFixedSecurity bond, final DATA_TYPE data) {
    return _delegate.visitBondFixedSecurity(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFixedTransaction(final BondFixedTransaction bond, final DATA_TYPE data) {
    return _delegate.visitBondFixedTransaction(bond);
  }

  @Override
  public RESULT_TYPE visitBondIborSecurity(final BondIborSecurity bond, final DATA_TYPE data) {
    return _delegate.visitBondIborSecurity(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondIborTransaction(final BondIborTransaction bond, final DATA_TYPE data) {
    return _delegate.visitBondIborTransaction(bond, data);
  }

  @Override
  public RESULT_TYPE visitBillSecurity(final BillSecurity bill, final DATA_TYPE data) {
    return _delegate.visitBillSecurity(bill, data);
  }

  @Override
  public RESULT_TYPE visitBillTransaction(final BillTransaction bill, final DATA_TYPE data) {
    return _delegate.visitBillTransaction(bill, data);
  }

  @Override
  public RESULT_TYPE visitGenericAnnuity(final Annuity<? extends Payment> genericAnnuity, final DATA_TYPE data) {
    return _delegate.visitGenericAnnuity(genericAnnuity, data);
  }

  @Override
  public RESULT_TYPE visitFixedCouponAnnuity(final AnnuityCouponFixed fixedCouponAnnuity, final DATA_TYPE data) {
    return _delegate.visitFixedCouponAnnuity(fixedCouponAnnuity, data);
  }

  @Override
  public RESULT_TYPE visitAnnuityCouponIborRatchet(final AnnuityCouponIborRatchet annuity, final DATA_TYPE data) {
    return _delegate.visitAnnuityCouponIborRatchet(annuity, data);
  }

  @Override
  public RESULT_TYPE visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final DATA_TYPE data) {
    return _delegate.visitFixedCouponSwap(swap, data);
  }

  @Override
  public RESULT_TYPE visitFixedFloatSwap(final FixedFloatSwap swap, final DATA_TYPE data) {
    return _delegate.visitFixedFloatSwap(swap, data);
  }

  @Override
  public RESULT_TYPE visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final DATA_TYPE data) {
    return _delegate.visitSwaptionCashFixedIbor(swaption, data);
  }

  @Override
  public RESULT_TYPE visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final DATA_TYPE data) {
    return _delegate.visitSwaptionPhysicalFixedIbor(swaption, data);
  }

  @Override
  public RESULT_TYPE visitSwaptionBermudaFixedIbor(final SwaptionBermudaFixedIbor swaption, final DATA_TYPE data) {
    return _delegate.visitSwaptionBermudaFixedIbor(swaption, data);
  }

  @Override
  public RESULT_TYPE visitTenorSwap(final TenorSwap<? extends Payment> tenorSwap, final DATA_TYPE data) {
    return _delegate.visitTenorSwap(tenorSwap, data);
  }

  @Override
  public RESULT_TYPE visitCrossCurrencySwap(final CrossCurrencySwap ccs, final DATA_TYPE data) {
    return _delegate.visitCrossCurrencySwap(ccs, data);
  }

  @Override
  public RESULT_TYPE visitForexForward(final ForexForward fx, final DATA_TYPE data) {
    return _delegate.visitForexForward(fx, data);
  }

  @Override
  public RESULT_TYPE visitCash(final Cash cash, final DATA_TYPE data) {
    return _delegate.visitCash(cash, data);
  }

  @Override
  public RESULT_TYPE visitFixedPayment(final PaymentFixed payment, final DATA_TYPE data) {
    return _delegate.visitFixedPayment(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponCMS(final CouponCMS payment, final DATA_TYPE data) {
    return _delegate.visitCouponCMS(payment, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorIbor(final CapFloorIbor payment, final DATA_TYPE data) {
    return _delegate.visitCapFloorIbor(payment, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorCMS(final CapFloorCMS payment, final DATA_TYPE data) {
    return _delegate.visitCapFloorCMS(payment, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorCMSSpread(final CapFloorCMSSpread payment, final DATA_TYPE data) {
    return _delegate.visitCapFloorCMSSpread(payment, data);
  }

  @Override
  public RESULT_TYPE visitForwardRateAgreement(final ForwardRateAgreement fra, final DATA_TYPE data) {
    return _delegate.visitForwardRateAgreement(fra, data);
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurity<?> bond, final DATA_TYPE data) {
    return _delegate.visitBondCapitalIndexedSecurity(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransaction<?> bond, final DATA_TYPE data) {
    return _delegate.visitBondCapitalIndexedTransaction(bond, data);
  }

  @Override
  public RESULT_TYPE visitCDSDerivative(final ISDACDSDerivative cds, final DATA_TYPE data) {
    return _delegate.visitCDSDerivative(cds, data);
  }

  @Override
  public RESULT_TYPE visitBondFixedSecurity(final BondFixedSecurity bond) {
    return _delegate.visitBondFixedSecurity(bond);
  }

  @Override
  public RESULT_TYPE visitBondFixedTransaction(final BondFixedTransaction bond) {
    return _delegate.visitBondFixedTransaction(bond);
  }

  @Override
  public RESULT_TYPE visitBondIborSecurity(final BondIborSecurity bond) {
    return _delegate.visitBondIborSecurity(bond);
  }

  @Override
  public RESULT_TYPE visitBondIborTransaction(final BondIborTransaction bond) {
    return _delegate.visitBondIborTransaction(bond);
  }

  @Override
  public RESULT_TYPE visitBillSecurity(final BillSecurity bill) {
    return _delegate.visitBillSecurity(bill);
  }

  @Override
  public RESULT_TYPE visitBillTransaction(final BillTransaction bill) {
    return _delegate.visitBillTransaction(bill);
  }

  @Override
  public RESULT_TYPE visitGenericAnnuity(final Annuity<? extends Payment> genericAnnuity) {
    return _delegate.visitGenericAnnuity(genericAnnuity);
  }

  @Override
  public RESULT_TYPE visitFixedCouponAnnuity(final AnnuityCouponFixed fixedCouponAnnuity) {
    return _delegate.visitFixedCouponAnnuity(fixedCouponAnnuity);
  }

  @Override
  public RESULT_TYPE visitAnnuityCouponIborRatchet(final AnnuityCouponIborRatchet annuity) {
    return _delegate.visitAnnuityCouponIborRatchet(annuity);
  }

  @Override
  public RESULT_TYPE visitFixedCouponSwap(final SwapFixedCoupon<?> swap) {
    return _delegate.visitFixedCouponSwap(swap);
  }

  @Override
  public RESULT_TYPE visitFixedFloatSwap(final FixedFloatSwap swap) {
    return _delegate.visitFixedFloatSwap(swap);
  }

  @Override
  public RESULT_TYPE visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption) {
    return _delegate.visitSwaptionCashFixedIbor(swaption);
  }

  @Override
  public RESULT_TYPE visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption) {
    return _delegate.visitSwaptionPhysicalFixedIbor(swaption);
  }

  @Override
  public RESULT_TYPE visitSwaptionBermudaFixedIbor(final SwaptionBermudaFixedIbor swaption) {
    return _delegate.visitSwaptionBermudaFixedIbor(swaption);
  }

  @Override
  public RESULT_TYPE visitCrossCurrencySwap(final CrossCurrencySwap ccs) {
    return _delegate.visitCrossCurrencySwap(ccs);
  }

  @Override
  public RESULT_TYPE visitForexForward(final ForexForward fx) {
    return _delegate.visitForexForward(fx);
  }

  @Override
  public RESULT_TYPE visitTenorSwap(final TenorSwap<? extends Payment> tenorSwap) {
    return _delegate.visitTenorSwap(tenorSwap);
  }

  @Override
  public RESULT_TYPE visitCash(final Cash cash) {
    return _delegate.visitCash(cash);
  }

  @Override
  public RESULT_TYPE visitFixedPayment(final PaymentFixed payment) {
    return _delegate.visitFixedPayment(payment);
  }

  @Override
  public RESULT_TYPE visitCouponCMS(final CouponCMS payment) {
    return _delegate.visitCouponCMS(payment);
  }

  @Override
  public RESULT_TYPE visitCapFloorIbor(final CapFloorIbor payment) {
    return _delegate.visitCapFloorIbor(payment);
  }

  @Override
  public RESULT_TYPE visitCapFloorCMS(final CapFloorCMS payment) {
    return _delegate.visitCapFloorCMS(payment);
  }

  @Override
  public RESULT_TYPE visitCapFloorCMSSpread(final CapFloorCMSSpread payment) {
    return _delegate.visitCapFloorCMSSpread(payment);
  }

  @Override
  public RESULT_TYPE visitForwardRateAgreement(final ForwardRateAgreement fra) {
    return _delegate.visitForwardRateAgreement(fra);
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurity<?> bond) {
    return _delegate.visitBondCapitalIndexedSecurity(bond);
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransaction<?> bond) {
    return _delegate.visitBondCapitalIndexedTransaction(bond);
  }

  @Override
  public RESULT_TYPE visitCDSDerivative(final ISDACDSDerivative cds) {
    return _delegate.visitCDSDerivative(cds);
  }

  @Override
  public RESULT_TYPE visitCouponFixed(final CouponFixed payment, final DATA_TYPE data) {
    return _delegate.visitCouponFixed(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponFixed(final CouponFixed payment) {
    return _delegate.visitCouponFixed(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIbor(final CouponIbor payment, final DATA_TYPE data) {
    return _delegate.visitCouponIbor(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIbor(final CouponIbor payment) {
    return _delegate.visitCouponIbor(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborSpread(final CouponIborSpread payment, final DATA_TYPE data) {
    return _delegate.visitCouponIborSpread(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborSpread(final CouponIborSpread payment) {
    return _delegate.visitCouponIborSpread(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborGearing(final CouponIborGearing payment) {
    return _delegate.visitCouponIborGearing(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborGearing(final CouponIborGearing payment, final DATA_TYPE data) {
    return _delegate.visitCouponIborGearing(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompounded(final CouponIborCompounded payment) {
    return _delegate.visitCouponIborCompounded(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompounded(final CouponIborCompounded payment, final DATA_TYPE data) {
    return _delegate.visitCouponIborCompounded(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponOIS(final CouponOIS payment, final DATA_TYPE data) {
    return _delegate.visitCouponOIS(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponOIS(final CouponOIS payment) {
    return _delegate.visitCouponOIS(payment);
  }

  @Override
  public RESULT_TYPE visitSwap(final Swap<?, ?> swap, final DATA_TYPE data) {
    return _delegate.visitSwap(swap, data);
  }

  @Override
  public RESULT_TYPE visitSwap(final Swap<?, ?> swap) {
    return _delegate.visitSwap(swap);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponMonthly(final CouponInflationZeroCouponMonthly coupon, final DATA_TYPE data) {
    return _delegate.visitCouponInflationZeroCouponMonthly(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponMonthly(final CouponInflationZeroCouponMonthly coupon) {
    return _delegate.visitCouponInflationZeroCouponMonthly(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponMonthlyGearing(final CouponInflationZeroCouponMonthlyGearing coupon, final DATA_TYPE data) {
    return _delegate.visitCouponInflationZeroCouponMonthlyGearing(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponMonthlyGearing(final CouponInflationZeroCouponMonthlyGearing coupon) {
    return _delegate.visitCouponInflationZeroCouponMonthlyGearing(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolation coupon, final DATA_TYPE data) {
    return _delegate.visitCouponInflationZeroCouponInterpolation(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolation coupon) {
    return _delegate.visitCouponInflationZeroCouponInterpolation(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponInterpolationGearing(final CouponInflationZeroCouponInterpolationGearing coupon, final DATA_TYPE data) {
    return _delegate.visitCouponInflationZeroCouponInterpolationGearing(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponInterpolationGearing(final CouponInflationZeroCouponInterpolationGearing coupon) {
    return _delegate.visitCouponInflationZeroCouponInterpolationGearing(coupon);
  }

  @Override
  public RESULT_TYPE visitBondFuture(final BondFuture bondFuture, final DATA_TYPE data) {
    return _delegate.visitBondFuture(bondFuture, data);
  }

  @Override
  public RESULT_TYPE visitBondFuture(final BondFuture future) {
    return _delegate.visitBondFuture(future);
  }

  @Override
  public RESULT_TYPE visitInterestRateFuture(final InterestRateFuture future, final DATA_TYPE data) {
    return _delegate.visitInterestRateFuture(future, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFuture(final InterestRateFuture future) {
    return _delegate.visitInterestRateFuture(future);
  }

  @Override
  public RESULT_TYPE visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity future, final DATA_TYPE data) {
    return _delegate.visitFederalFundsFutureSecurity(future, data);
  }

  @Override
  public RESULT_TYPE visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity future) {
    return _delegate.visitFederalFundsFutureSecurity(future);
  }

  @Override
  public RESULT_TYPE visitFederalFundsFutureTransaction(final FederalFundsFutureTransaction future, final DATA_TYPE data) {
    return _delegate.visitFederalFundsFutureTransaction(future, data);
  }

  @Override
  public RESULT_TYPE visitFederalFundsFutureTransaction(final FederalFundsFutureTransaction future) {
    return _delegate.visitFederalFundsFutureTransaction(future);
  }

  @Override
  public RESULT_TYPE visitDeliverableSwapFuturesSecurity(final DeliverableSwapFuturesSecurity futures, final DATA_TYPE data) {
    return _delegate.visitDeliverableSwapFuturesSecurity(futures, data);
  }

  @Override
  public RESULT_TYPE visitDeliverableSwapFuturesSecurity(final DeliverableSwapFuturesSecurity futures) {
    return _delegate.visitDeliverableSwapFuturesSecurity(futures);
  }

  @Override
  public RESULT_TYPE visitBondFutureOptionPremiumSecurity(final BondFutureOptionPremiumSecurity option, final DATA_TYPE data) {
    return _delegate.visitBondFutureOptionPremiumSecurity(option, data);
  }

  @Override
  public RESULT_TYPE visitBondFutureOptionPremiumSecurity(final BondFutureOptionPremiumSecurity option) {
    return _delegate.visitBondFutureOptionPremiumSecurity(option);
  }

  @Override
  public RESULT_TYPE visitBondFutureOptionPremiumTransaction(final BondFutureOptionPremiumTransaction option, final DATA_TYPE data) {
    return _delegate.visitBondFutureOptionPremiumTransaction(option, data);
  }

  @Override
  public RESULT_TYPE visitBondFutureOptionPremiumTransaction(final BondFutureOptionPremiumTransaction option) {
    return _delegate.visitBondFutureOptionPremiumTransaction(option);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity option, final DATA_TYPE data) {
    return _delegate.visitInterestRateFutureOptionMarginSecurity(option, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity option) {
    return _delegate.visitInterestRateFutureOptionMarginSecurity(option);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option, final DATA_TYPE data) {
    return _delegate.visitInterestRateFutureOptionMarginTransaction(option, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option) {
    return _delegate.visitInterestRateFutureOptionMarginTransaction(option);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionPremiumSecurity(final InterestRateFutureOptionPremiumSecurity option, final DATA_TYPE data) {
    return _delegate.visitInterestRateFutureOptionPremiumSecurity(option, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionPremiumSecurity(final InterestRateFutureOptionPremiumSecurity option) {
    return _delegate.visitInterestRateFutureOptionPremiumSecurity(option);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionPremiumTransaction(final InterestRateFutureOptionPremiumTransaction option, final DATA_TYPE data) {
    return _delegate.visitInterestRateFutureOptionPremiumTransaction(option, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionPremiumTransaction(final InterestRateFutureOptionPremiumTransaction option) {
    return _delegate.visitInterestRateFutureOptionPremiumTransaction(option);
  }

  @Override
  public RESULT_TYPE visitDepositIbor(final DepositIbor deposit, final DATA_TYPE data) {
    return _delegate.visitDepositIbor(deposit, data);
  }

  @Override
  public RESULT_TYPE visitDepositIbor(final DepositIbor deposit) {
    return _delegate.visitDepositIbor(deposit);
  }

  @Override
  public RESULT_TYPE visitDepositCounterpart(final DepositCounterpart deposit, final DATA_TYPE data) {
    return _delegate.visitDepositCounterpart(deposit, data);
  }

  @Override
  public RESULT_TYPE visitDepositCounterpart(final DepositCounterpart deposit) {
    return _delegate.visitDepositCounterpart(deposit);
  }

  @Override
  public RESULT_TYPE visitDepositZero(final DepositZero deposit, final DATA_TYPE data) {
    return _delegate.visitDepositZero(deposit, data);
  }

  @Override
  public RESULT_TYPE visitDepositZero(final DepositZero deposit) {
    return _delegate.visitDepositZero(deposit);
  }

  @Override
  public RESULT_TYPE visitForex(final Forex derivative, final DATA_TYPE data) {
    return _delegate.visitForex(derivative, data);
  }

  @Override
  public RESULT_TYPE visitForex(final Forex derivative) {
    return _delegate.visitForex(derivative);
  }

  @Override
  public RESULT_TYPE visitForexSwap(final ForexSwap derivative, final DATA_TYPE data) {
    return _delegate.visitForexSwap(derivative, data);
  }

  @Override
  public RESULT_TYPE visitForexSwap(final ForexSwap derivative) {
    return _delegate.visitForexSwap(derivative);
  }

  @Override
  public RESULT_TYPE visitForexOptionVanilla(final ForexOptionVanilla derivative, final DATA_TYPE data) {
    return _delegate.visitForexOptionVanilla(derivative, data);
  }

  @Override
  public RESULT_TYPE visitForexOptionVanilla(final ForexOptionVanilla derivative) {
    return _delegate.visitForexOptionVanilla(derivative);
  }

  @Override
  public RESULT_TYPE visitForexOptionSingleBarrier(final ForexOptionSingleBarrier derivative, final DATA_TYPE data) {
    return _delegate.visitForexOptionSingleBarrier(derivative, data);
  }

  @Override
  public RESULT_TYPE visitForexOptionSingleBarrier(final ForexOptionSingleBarrier derivative) {
    return _delegate.visitForexOptionSingleBarrier(derivative);
  }

  @Override
  public RESULT_TYPE visitForexNonDeliverableForward(final ForexNonDeliverableForward derivative, final DATA_TYPE data) {
    return _delegate.visitForexNonDeliverableForward(derivative, data);
  }

  @Override
  public RESULT_TYPE visitForexNonDeliverableForward(final ForexNonDeliverableForward derivative) {
    return _delegate.visitForexNonDeliverableForward(derivative);
  }

  @Override
  public RESULT_TYPE visitForexNonDeliverableOption(final ForexNonDeliverableOption derivative, final DATA_TYPE data) {
    return _delegate.visitForexNonDeliverableOption(derivative, data);
  }

  @Override
  public RESULT_TYPE visitForexNonDeliverableOption(final ForexNonDeliverableOption derivative) {
    return _delegate.visitForexNonDeliverableOption(derivative);
  }

  @Override
  public RESULT_TYPE visitForexOptionDigital(final ForexOptionDigital derivative, final DATA_TYPE data) {
    return _delegate.visitForexOptionDigital(derivative, data);
  }

  @Override
  public RESULT_TYPE visitForexOptionDigital(final ForexOptionDigital derivative) {
    return _delegate.visitForexOptionDigital(derivative);
  }

  @Override
  public RESULT_TYPE visitMetalForward(final MetalForward future, final DATA_TYPE data) {
    return _delegate.visitMetalForward(future, data);
  }

  @Override
  public RESULT_TYPE visitMetalForward(final MetalForward future) {
    return _delegate.visitMetalForward(future);
  }

  @Override
  public RESULT_TYPE visitMetalFuture(final MetalFuture future, final DATA_TYPE data) {
    return _delegate.visitMetalFuture(future, data);
  }

  @Override
  public RESULT_TYPE visitMetalFuture(final MetalFuture future) {
    return _delegate.visitMetalFuture(future);
  }

  @Override
  public RESULT_TYPE visitMetalFutureOption(final MetalFutureOption future, final DATA_TYPE data) {
    return _delegate.visitMetalFutureOption(future, data);
  }

  @Override
  public RESULT_TYPE visitMetalFutureOption(final MetalFutureOption future) {
    return _delegate.visitMetalFutureOption(future);
  }

  @Override
  public RESULT_TYPE visitAgricultureForward(final AgricultureForward future, final DATA_TYPE data) {
    return _delegate.visitAgricultureForward(future, data);
  }

  @Override
  public RESULT_TYPE visitAgricultureForward(final AgricultureForward future) {
    return _delegate.visitAgricultureForward(future);
  }

  @Override
  public RESULT_TYPE visitAgricultureFuture(final AgricultureFuture future, final DATA_TYPE data) {
    return _delegate.visitAgricultureFuture(future, data);
  }

  @Override
  public RESULT_TYPE visitAgricultureFuture(final AgricultureFuture future) {
    return _delegate.visitAgricultureFuture(future);
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureOption(final AgricultureFutureOption future, final DATA_TYPE data) {
    return _delegate.visitAgricultureFutureOption(future, data);
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureOption(final AgricultureFutureOption future) {
    return _delegate.visitAgricultureFutureOption(future);
  }

  @Override
  public RESULT_TYPE visitEnergyForward(final EnergyForward future, final DATA_TYPE data) {
    return _delegate.visitEnergyForward(future, data);
  }

  @Override
  public RESULT_TYPE visitEnergyForward(final EnergyForward future) {
    return _delegate.visitEnergyForward(future);
  }

  @Override
  public RESULT_TYPE visitEnergyFuture(final EnergyFuture future, final DATA_TYPE data) {
    return _delegate.visitEnergyFuture(future, data);
  }

  @Override
  public RESULT_TYPE visitEnergyFuture(final EnergyFuture future) {
    return _delegate.visitEnergyFuture(future);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureOption(final EnergyFutureOption future, final DATA_TYPE data) {
    return _delegate.visitEnergyFutureOption(future, data);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureOption(final EnergyFutureOption future) {
    return _delegate.visitEnergyFutureOption(future);
  }

  @Override
  public RESULT_TYPE visitForwardLiborAnnuity(final AnnuityCouponIbor forwardLiborAnnuity, final DATA_TYPE data) {
    return _delegate.visitForwardLiborAnnuity(forwardLiborAnnuity, data);
  }

  @Override
  public RESULT_TYPE visitForwardLiborAnnuity(final AnnuityCouponIbor forwardLiborAnnuity) {
    return _delegate.visitForwardLiborAnnuity(forwardLiborAnnuity);
  }

  @Override
  public RESULT_TYPE visitAnnuityCouponIborSpread(final AnnuityCouponIborSpread annuity, final DATA_TYPE data) {
    return _delegate.visitAnnuityCouponIborSpread(annuity, data);
  }

  @Override
  public RESULT_TYPE visitAnnuityCouponIborSpread(final AnnuityCouponIborSpread annuity) {
    return _delegate.visitAnnuityCouponIborSpread(annuity);
  }

  @Override
  public RESULT_TYPE visitFloatingRateNote(final FloatingRateNote derivative, final DATA_TYPE data) {
    return _delegate.visitFloatingRateNote(derivative, data);
  }

  @Override
  public RESULT_TYPE visitFloatingRateNote(final FloatingRateNote derivative) {
    return _delegate.visitFloatingRateNote(derivative);
  }

}
