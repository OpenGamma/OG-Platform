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
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.AgricultureFutureSecurity;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.AgricultureFutureTransaction;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.CouponCommodityCashSettle;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.CouponCommodityPhysicalSettle;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.EnergyFutureSecurity;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.EnergyFutureTransaction;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.ForwardCommodityCashSettle;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.ForwardCommodityPhysicalSettle;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.MetalFutureSecurity;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.MetalFutureTransaction;
import com.opengamma.analytics.financial.credit.cds.ISDACDSDerivative;
import com.opengamma.analytics.financial.equity.Equity;
import com.opengamma.analytics.financial.equity.future.derivative.CashSettledFuture;
import com.opengamma.analytics.financial.equity.future.derivative.EquityFuture;
import com.opengamma.analytics.financial.equity.future.derivative.EquityIndexDividendFuture;
import com.opengamma.analytics.financial.equity.future.derivative.EquityIndexFuture;
import com.opengamma.analytics.financial.equity.future.derivative.IndexFuture;
import com.opengamma.analytics.financial.equity.future.derivative.VolatilityIndexFuture;
import com.opengamma.analytics.financial.equity.option.EquityIndexFutureOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityOption;
import com.opengamma.analytics.financial.equity.trs.EquityTotalReturnSwap;
import com.opengamma.analytics.financial.equity.variance.EquityVarianceSwap;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableForward;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableOption;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.instrument.index.IndexDeposit;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondInterestIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondInterestIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondTotalReturnSwap;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositCounterpart;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositIbor;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositZero;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesYieldAverageSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesYieldAverageTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableTransaction;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationYearOnYearInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationYearOnYearMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationZeroCouponInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationZeroCouponMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearInterpolationWithMargin;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearMonthlyWithMargin;
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
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedAccruedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverage;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingFlatSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingSimpleSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverage;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverageSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverageSpreadSimplified;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONCompounded;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.DepositIndexCoupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.InterpolatedStubCoupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCompoundingCoupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapMultileg;
import com.opengamma.analytics.financial.interestrate.swap.derivative.TotalReturnSwap;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionBermudaFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.varianceswap.VarianceSwap;
import com.opengamma.analytics.financial.volatilityswap.FXVolatilitySwap;
import com.opengamma.analytics.financial.volatilityswap.VolatilitySwap;
import com.opengamma.util.ArgumentChecker;

/**
 *
 * @param <DATA_TYPE> The type of the data
 * @param <RESULT_TYPE> The return type of the calculation
 */
@SuppressWarnings("deprecation")
public class InstrumentDerivativeVisitorDelegate<DATA_TYPE, RESULT_TYPE> implements InstrumentDerivativeVisitor<DATA_TYPE, RESULT_TYPE> {
  /** The delegate visitor */
  private final InstrumentDerivativeVisitor<DATA_TYPE, RESULT_TYPE> _delegate;

  /**
   * @param delegate The delegate, not null
   */
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
  public RESULT_TYPE visitFixedCompoundingCouponSwap(final SwapFixedCompoundingCoupon<?> swap, final DATA_TYPE data) {
    return _delegate.visitFixedCompoundingCouponSwap(swap, data);
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
  public RESULT_TYPE visitBondInterestIndexedSecurity(final BondInterestIndexedSecurity<?, ?> bond) {
    return _delegate.visitBondInterestIndexedSecurity(bond);
  }

  @Override
  public RESULT_TYPE visitBondInterestIndexedSecurity(final BondInterestIndexedSecurity<?, ?> bond, final DATA_TYPE data) {
    return _delegate.visitBondInterestIndexedSecurity(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransaction<?> bond, final DATA_TYPE data) {
    return _delegate.visitBondCapitalIndexedTransaction(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondInterestIndexedTransaction(final BondInterestIndexedTransaction<?, ?> bond, final DATA_TYPE data) {
    return _delegate.visitBondInterestIndexedTransaction(bond, data);
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
  public RESULT_TYPE visitFixedCompoundingCouponSwap(final SwapFixedCompoundingCoupon<?> swap) {
    return _delegate.visitFixedCompoundingCouponSwap(swap);
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
  public RESULT_TYPE visitSwaptionCashFixedCompoundedONCompounded(final SwaptionCashFixedCompoundedONCompounded swaption, final DATA_TYPE data) {
    return _delegate.visitSwaptionCashFixedCompoundedONCompounded(swaption, data);
  }

  @Override
  public RESULT_TYPE visitSwaptionCashFixedCompoundedONCompounded(final SwaptionCashFixedCompoundedONCompounded swaption) {
    return _delegate.visitSwaptionCashFixedCompoundedONCompounded(swaption);
  }

  @Override
  public RESULT_TYPE visitSwaptionPhysicalFixedCompoundedONCompounded(final SwaptionPhysicalFixedCompoundedONCompounded swaption, final DATA_TYPE data) {
    return _delegate.visitSwaptionPhysicalFixedCompoundedONCompounded(swaption, data);
  }

  @Override
  public RESULT_TYPE visitSwaptionPhysicalFixedCompoundedONCompounded(final SwaptionPhysicalFixedCompoundedONCompounded swaption) {
    return _delegate.visitSwaptionPhysicalFixedCompoundedONCompounded(swaption);
  }

  @Override
  public RESULT_TYPE visitSwaptionBermudaFixedIbor(final SwaptionBermudaFixedIbor swaption) {
    return _delegate.visitSwaptionBermudaFixedIbor(swaption);
  }

  @Override
  public RESULT_TYPE visitForexForward(final ForexForward fx) {
    return _delegate.visitForexForward(fx);
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
  public RESULT_TYPE visitBondInterestIndexedTransaction(final BondInterestIndexedTransaction<?, ?> bond) {
    return _delegate.visitBondInterestIndexedTransaction(bond);
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
  public RESULT_TYPE visitCouponFixedCompounding(final CouponFixedCompounding payment, final DATA_TYPE data) {
    return _delegate.visitCouponFixedCompounding(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponFixedCompounding(final CouponFixedCompounding payment) {
    return _delegate.visitCouponFixedCompounding(payment);
  }

  @Override
  public RESULT_TYPE visitCouponFixedAccruedCompounding(final CouponFixedAccruedCompounding payment, final DATA_TYPE data) {
    return _delegate.visitCouponFixedAccruedCompounding(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponFixedAccruedCompounding(final CouponFixedAccruedCompounding payment) {
    return _delegate.visitCouponFixedAccruedCompounding(payment);
  }

  @Override
  public RESULT_TYPE visitInterpolatedStubCoupon(final InterpolatedStubCoupon<? extends DepositIndexCoupon<? extends IndexDeposit>, ? extends IndexDeposit> payment, final DATA_TYPE data) {
    return _delegate.visitInterpolatedStubCoupon(payment, data);
  }

  @Override
  public RESULT_TYPE visitInterpolatedStubCoupon(final InterpolatedStubCoupon<? extends DepositIndexCoupon<? extends IndexDeposit>, ? extends IndexDeposit> payment) {
    return _delegate.visitInterpolatedStubCoupon(payment);
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
  public RESULT_TYPE visitCouponIborAverage(final CouponIborAverage payment, final DATA_TYPE data) {
    return _delegate.visitCouponIborAverage(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborAverage(final CouponIborAverage payment) {
    return _delegate.visitCouponIborAverage(payment);
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
  public RESULT_TYPE visitCouponIborCompounding(final CouponIborCompounding payment) {
    return _delegate.visitCouponIborCompounding(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompounding(final CouponIborCompounding payment, final DATA_TYPE data) {
    return _delegate.visitCouponIborCompounding(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundingSpread(final CouponIborCompoundingSpread payment) {
    return _delegate.visitCouponIborCompoundingSpread(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundingSpread(final CouponIborCompoundingSpread payment, final DATA_TYPE data) {
    return _delegate.visitCouponIborCompoundingSpread(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundingFlatSpread(final CouponIborCompoundingFlatSpread payment) {
    return _delegate.visitCouponIborCompoundingFlatSpread(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundingFlatSpread(final CouponIborCompoundingFlatSpread payment, final DATA_TYPE data) {
    return _delegate.visitCouponIborCompoundingFlatSpread(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundingSimpleSpread(final CouponIborCompoundingSimpleSpread payment) {
    return _delegate.visitCouponIborCompoundingSimpleSpread(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundingSimpleSpread(final CouponIborCompoundingSimpleSpread payment, final DATA_TYPE data) {
    return _delegate.visitCouponIborCompoundingSimpleSpread(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponONCompounded(final CouponONCompounded payment, final DATA_TYPE data) {
    return _delegate.visitCouponONCompounded(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponONCompounded(final CouponONCompounded payment) {
    return _delegate.visitCouponONCompounded(payment);
  }

  @Override
  public RESULT_TYPE visitCouponOIS(final CouponON payment, final DATA_TYPE data) {
    return _delegate.visitCouponOIS(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponOIS(final CouponON payment) {
    return _delegate.visitCouponOIS(payment);
  }

  @Override
  public RESULT_TYPE visitCouponONSpread(final CouponONSpread payment, final DATA_TYPE data) {
    return _delegate.visitCouponONSpread(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponONSpread(final CouponONSpread payment) {
    return _delegate.visitCouponONSpread(payment);
  }

  @Override
  public RESULT_TYPE visitCouponONArithmeticAverage(final CouponONArithmeticAverage payment, final DATA_TYPE data) {
    return _delegate.visitCouponONArithmeticAverage(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponONArithmeticAverage(final CouponONArithmeticAverage payment) {
    return _delegate.visitCouponONArithmeticAverage(payment);
  }

  @Override
  public RESULT_TYPE visitCouponONArithmeticAverageSpread(final CouponONArithmeticAverageSpread payment, final DATA_TYPE data) {
    return _delegate.visitCouponONArithmeticAverageSpread(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponONArithmeticAverageSpread(final CouponONArithmeticAverageSpread payment) {
    return _delegate.visitCouponONArithmeticAverageSpread(payment);
  }

  @Override
  public RESULT_TYPE visitCouponONArithmeticAverageSpreadSimplified(final CouponONArithmeticAverageSpreadSimplified payment, final DATA_TYPE data) {
    return _delegate.visitCouponONArithmeticAverageSpreadSimplified(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponONArithmeticAverageSpreadSimplified(final CouponONArithmeticAverageSpreadSimplified payment) {
    return _delegate.visitCouponONArithmeticAverageSpreadSimplified(payment);
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
  public RESULT_TYPE visitSwapMultileg(final SwapMultileg swap, final DATA_TYPE data) {
    return _delegate.visitSwapMultileg(swap, data);
  }

  @Override
  public RESULT_TYPE visitSwapMultileg(final SwapMultileg swap) {
    return _delegate.visitSwapMultileg(swap);
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
  public RESULT_TYPE visitCouponInflationYearOnYearMonthly(final CouponInflationYearOnYearMonthly coupon, final DATA_TYPE data) {
    return _delegate.visitCouponInflationYearOnYearMonthly(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearMonthly(final CouponInflationYearOnYearMonthly coupon) {
    return _delegate.visitCouponInflationYearOnYearMonthly(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearInterpolation(final CouponInflationYearOnYearInterpolation coupon, final DATA_TYPE data) {
    return _delegate.visitCouponInflationYearOnYearInterpolation(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearInterpolation(final CouponInflationYearOnYearInterpolation coupon) {
    return _delegate.visitCouponInflationYearOnYearInterpolation(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearMonthlyWithMargin(final CouponInflationYearOnYearMonthlyWithMargin coupon, final DATA_TYPE data) {
    return _delegate.visitCouponInflationYearOnYearMonthlyWithMargin(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearMonthlyWithMargin(final CouponInflationYearOnYearMonthlyWithMargin coupon) {
    return _delegate.visitCouponInflationYearOnYearMonthlyWithMargin(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearInterpolationWithMargin(final CouponInflationYearOnYearInterpolationWithMargin coupon, final DATA_TYPE data) {
    return _delegate.visitCouponInflationYearOnYearInterpolationWithMargin(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearInterpolationWithMargin(final CouponInflationYearOnYearInterpolationWithMargin coupon) {
    return _delegate.visitCouponInflationYearOnYearInterpolationWithMargin(coupon);
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationZeroCouponInterpolation(final CapFloorInflationZeroCouponInterpolation coupon, final DATA_TYPE data) {
    return _delegate.visitCapFloorInflationZeroCouponInterpolation(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationZeroCouponInterpolation(final CapFloorInflationZeroCouponInterpolation coupon) {
    return _delegate.visitCapFloorInflationZeroCouponInterpolation(coupon);
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationZeroCouponMonthly(final CapFloorInflationZeroCouponMonthly coupon, final DATA_TYPE data) {
    return _delegate.visitCapFloorInflationZeroCouponMonthly(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationZeroCouponMonthly(final CapFloorInflationZeroCouponMonthly coupon) {
    return _delegate.visitCapFloorInflationZeroCouponMonthly(coupon);
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationYearOnYearInterpolation(final CapFloorInflationYearOnYearInterpolation coupon, final DATA_TYPE data) {
    return _delegate.visitCapFloorInflationYearOnYearInterpolation(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationYearOnYearInterpolation(final CapFloorInflationYearOnYearInterpolation coupon) {
    return _delegate.visitCapFloorInflationYearOnYearInterpolation(coupon);
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationYearOnYearMonthly(final CapFloorInflationYearOnYearMonthly coupon, final DATA_TYPE data) {
    return _delegate.visitCapFloorInflationYearOnYearMonthly(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationYearOnYearMonthly(final CapFloorInflationYearOnYearMonthly coupon) {
    return _delegate.visitCapFloorInflationYearOnYearMonthly(coupon);
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
  public RESULT_TYPE visitBondFuturesSecurity(final BondFuturesSecurity bondFuture, final DATA_TYPE data) {
    return _delegate.visitBondFuturesSecurity(bondFuture, data);
  }

  @Override
  public RESULT_TYPE visitBondFuturesSecurity(final BondFuturesSecurity future) {
    return _delegate.visitBondFuturesSecurity(future);
  }

  @Override
  public RESULT_TYPE visitBondFuturesTransaction(final BondFuturesTransaction bondFuture, final DATA_TYPE data) {
    return _delegate.visitBondFuturesTransaction(bondFuture, data);
  }

  @Override
  public RESULT_TYPE visitBondFuturesTransaction(final BondFuturesTransaction future) {
    return _delegate.visitBondFuturesTransaction(future);
  }

  @Override
  public RESULT_TYPE visitBondFuturesYieldAverageSecurity(final BondFuturesYieldAverageSecurity bondFuture, final DATA_TYPE data) {
    return _delegate.visitBondFuturesYieldAverageSecurity(bondFuture, data);
  }

  @Override
  public RESULT_TYPE visitBondFuturesYieldAverageSecurity(final BondFuturesYieldAverageSecurity future) {
    return _delegate.visitBondFuturesYieldAverageSecurity(future);
  }

  @Override
  public RESULT_TYPE visitYieldAverageBondFuturesTransaction(final BondFuturesYieldAverageTransaction bondFuture, final DATA_TYPE data) {
    return _delegate.visitYieldAverageBondFuturesTransaction(bondFuture, data);
  }

  @Override
  public RESULT_TYPE visitYieldAverageBondFuturesTransaction(final BondFuturesYieldAverageTransaction future) {
    return _delegate.visitYieldAverageBondFuturesTransaction(future);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureTransaction(final InterestRateFutureTransaction future, final DATA_TYPE data) {
    return _delegate.visitInterestRateFutureTransaction(future, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureTransaction(final InterestRateFutureTransaction future) {
    return _delegate.visitInterestRateFutureTransaction(future);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureSecurity(final InterestRateFutureSecurity future, final DATA_TYPE data) {
    return _delegate.visitInterestRateFutureSecurity(future, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureSecurity(final InterestRateFutureSecurity future) {
    return _delegate.visitInterestRateFutureSecurity(future);
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
  public RESULT_TYPE visitSwapFuturesPriceDeliverableSecurity(final SwapFuturesPriceDeliverableSecurity futures, final DATA_TYPE data) {
    return _delegate.visitSwapFuturesPriceDeliverableSecurity(futures, data);
  }

  @Override
  public RESULT_TYPE visitSwapFuturesPriceDeliverableSecurity(final SwapFuturesPriceDeliverableSecurity futures) {
    return _delegate.visitSwapFuturesPriceDeliverableSecurity(futures);
  }

  @Override
  public RESULT_TYPE visitSwapFuturesPriceDeliverableTransaction(final SwapFuturesPriceDeliverableTransaction futures, final DATA_TYPE data) {
    return _delegate.visitSwapFuturesPriceDeliverableTransaction(futures, data);
  }

  @Override
  public RESULT_TYPE visitSwapFuturesPriceDeliverableTransaction(final SwapFuturesPriceDeliverableTransaction futures) {
    return _delegate.visitSwapFuturesPriceDeliverableTransaction(futures);
  }

  @Override
  public RESULT_TYPE visitBondFuturesOptionMarginSecurity(final BondFuturesOptionMarginSecurity option, final DATA_TYPE data) {
    return _delegate.visitBondFuturesOptionMarginSecurity(option, data);
  }

  @Override
  public RESULT_TYPE visitBondFuturesOptionMarginSecurity(final BondFuturesOptionMarginSecurity option) {
    return _delegate.visitBondFuturesOptionMarginSecurity(option);
  }

  @Override
  public RESULT_TYPE visitBondFuturesOptionMarginTransaction(final BondFuturesOptionMarginTransaction option, final DATA_TYPE data) {
    return _delegate.visitBondFuturesOptionMarginTransaction(option, data);
  }

  @Override
  public RESULT_TYPE visitBondFuturesOptionMarginTransaction(final BondFuturesOptionMarginTransaction option) {
    return _delegate.visitBondFuturesOptionMarginTransaction(option);
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
  public RESULT_TYPE visitEquityFuture(final EquityFuture future, final DATA_TYPE data) {
    return _delegate.visitEquityFuture(future, data);
  }

  @Override
  public RESULT_TYPE visitEquityFuture(final EquityFuture future) {
    return _delegate.visitEquityFuture(future);
  }

  @Override
  public RESULT_TYPE visitEquityIndexDividendFuture(final EquityIndexDividendFuture future, final DATA_TYPE data) {
    return _delegate.visitEquityIndexDividendFuture(future, data);
  }

  @Override
  public RESULT_TYPE visitEquityIndexDividendFuture(final EquityIndexDividendFuture future) {
    return _delegate.visitEquityIndexDividendFuture(future);
  }

  @Override
  public RESULT_TYPE visitEquityIndexOption(final EquityIndexOption option, final DATA_TYPE data) {
    return _delegate.visitEquityIndexOption(option, data);
  }

  @Override
  public RESULT_TYPE visitEquityIndexOption(final EquityIndexOption option) {
    return _delegate.visitEquityIndexOption(option);
  }

  @Override
  public RESULT_TYPE visitEquityIndexFutureOption(final EquityIndexFutureOption option, final DATA_TYPE data) {
    return _delegate.visitEquityIndexFutureOption(option, data);
  }

  @Override
  public RESULT_TYPE visitEquityIndexFutureOption(final EquityIndexFutureOption option) {
    return _delegate.visitEquityIndexFutureOption(option);
  }

  @Override
  public RESULT_TYPE visitEquityOption(final EquityOption option, final DATA_TYPE data) {
    return _delegate.visitEquityOption(option, data);
  }

  @Override
  public RESULT_TYPE visitEquityOption(final EquityOption option) {
    return _delegate.visitEquityOption(option);
  }

  @Override
  public RESULT_TYPE visitVarianceSwap(final VarianceSwap varianceSwap) {
    return _delegate.visitVarianceSwap(varianceSwap);
  }

  @Override
  public RESULT_TYPE visitVarianceSwap(final VarianceSwap varianceSwap, final DATA_TYPE data) {
    return _delegate.visitVarianceSwap(varianceSwap, data);
  }

  @Override
  public RESULT_TYPE visitEquityVarianceSwap(final EquityVarianceSwap varianceSwap) {
    return _delegate.visitEquityVarianceSwap(varianceSwap);
  }

  @Override
  public RESULT_TYPE visitEquityVarianceSwap(final EquityVarianceSwap varianceSwap, final DATA_TYPE data) {
    return _delegate.visitEquityVarianceSwap(varianceSwap, data);
  }

  @Override
  public RESULT_TYPE visitVolatilitySwap(final VolatilitySwap volatilitySwap) {
    return _delegate.visitVolatilitySwap(volatilitySwap);
  }

  @Override
  public RESULT_TYPE visitVolatilitySwap(final VolatilitySwap volatilitySwap, final DATA_TYPE data) {
    return _delegate.visitVolatilitySwap(volatilitySwap, data);
  }

  @Override
  public RESULT_TYPE visitFXVolatilitySwap(final FXVolatilitySwap volatilitySwap) {
    return _delegate.visitFXVolatilitySwap(volatilitySwap);
  }

  @Override
  public RESULT_TYPE visitFXVolatilitySwap(final FXVolatilitySwap volatilitySwap, final DATA_TYPE data) {
    return _delegate.visitFXVolatilitySwap(volatilitySwap, data);
  }

  @Override
  public RESULT_TYPE visitCashSettledFuture(final CashSettledFuture future, final DATA_TYPE data) {
    return _delegate.visitCashSettledFuture(future, data);
  }

  @Override
  public RESULT_TYPE visitCashSettledFuture(final CashSettledFuture future) {
    return _delegate.visitCashSettledFuture(future);
  }

  @Override
  public RESULT_TYPE visitIndexFuture(final IndexFuture future, final DATA_TYPE data) {
    return _delegate.visitIndexFuture(future, data);
  }

  @Override
  public RESULT_TYPE visitIndexFuture(final IndexFuture future) {
    return _delegate.visitIndexFuture(future);
  }

  @Override
  public RESULT_TYPE visitEquityIndexFuture(final EquityIndexFuture future, final DATA_TYPE data) {
    return _delegate.visitEquityIndexFuture(future, data);
  }

  @Override
  public RESULT_TYPE visitEquityIndexFuture(final EquityIndexFuture future) {
    return _delegate.visitEquityIndexFuture(future);
  }

  @Override
  public RESULT_TYPE visitVolatilityIndexFuture(final VolatilityIndexFuture future, final DATA_TYPE data) {
    return _delegate.visitVolatilityIndexFuture(future, data);
  }

  @Override
  public RESULT_TYPE visitVolatilityIndexFuture(final VolatilityIndexFuture future) {
    return _delegate.visitVolatilityIndexFuture(future);
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureSecurity(final AgricultureFutureSecurity future, final DATA_TYPE data) {
    return _delegate.visitAgricultureFutureSecurity(future, data);
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureSecurity(final AgricultureFutureSecurity future) {
    return _delegate.visitAgricultureFutureSecurity(future);
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureTransaction(final AgricultureFutureTransaction future, final DATA_TYPE data) {
    return _delegate.visitAgricultureFutureTransaction(future, data);
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureTransaction(final AgricultureFutureTransaction future) {
    return _delegate.visitAgricultureFutureTransaction(future);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureSecurity(final EnergyFutureSecurity future, final DATA_TYPE data) {
    return _delegate.visitEnergyFutureSecurity(future, data);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureSecurity(final EnergyFutureSecurity future) {
    return _delegate.visitEnergyFutureSecurity(future);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureTransaction(final EnergyFutureTransaction future, final DATA_TYPE data) {
    return _delegate.visitEnergyFutureTransaction(future, data);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureTransaction(final EnergyFutureTransaction future) {
    return _delegate.visitEnergyFutureTransaction(future);
  }

  @Override
  public RESULT_TYPE visitMetalFutureSecurity(final MetalFutureSecurity future, final DATA_TYPE data) {
    return _delegate.visitMetalFutureSecurity(future, data);
  }

  @Override
  public RESULT_TYPE visitMetalFutureSecurity(final MetalFutureSecurity future) {
    return _delegate.visitMetalFutureSecurity(future);
  }

  @Override
  public RESULT_TYPE visitMetalFutureTransaction(final MetalFutureTransaction future, final DATA_TYPE data) {
    return _delegate.visitMetalFutureTransaction(future, data);
  }

  @Override
  public RESULT_TYPE visitMetalFutureTransaction(final MetalFutureTransaction future) {
    return _delegate.visitMetalFutureTransaction(future);
  }

  @Override
  public RESULT_TYPE visitCouponCommodityCashSettle(final CouponCommodityCashSettle coupon, final DATA_TYPE data) {
    return _delegate.visitCouponCommodityCashSettle(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponCommodityCashSettle(final CouponCommodityCashSettle coupon) {
    return _delegate.visitCouponCommodityCashSettle(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponCommodityPhysicalSettle(final CouponCommodityPhysicalSettle coupon, final DATA_TYPE data) {
    return _delegate.visitCouponCommodityPhysicalSettle(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponCommodityPhysicalSettle(final CouponCommodityPhysicalSettle coupon) {

    return _delegate.visitCouponCommodityPhysicalSettle(coupon);
  }

  @Override
  public RESULT_TYPE visitForwardCommodityCashSettle(final ForwardCommodityCashSettle forward, final DATA_TYPE data) {
    return _delegate.visitForwardCommodityCashSettle(forward, data);
  }

  @Override
  public RESULT_TYPE visitForwardCommodityCashSettle(final ForwardCommodityCashSettle forward) {
    return _delegate.visitForwardCommodityCashSettle(forward);
  }

  @Override
  public RESULT_TYPE visitForwardCommodityPhysicalSettle(final ForwardCommodityPhysicalSettle forward, final DATA_TYPE data) {
    return _delegate.visitForwardCommodityPhysicalSettle(forward, data);
  }

  @Override
  public RESULT_TYPE visitForwardCommodityPhysicalSettle(final ForwardCommodityPhysicalSettle forward) {
    return _delegate.visitForwardCommodityPhysicalSettle(forward);
  }

  @Override
  public RESULT_TYPE visitTotalReturnSwap(final TotalReturnSwap totalReturnSwap) {
    return _delegate.visitTotalReturnSwap(totalReturnSwap);
  }

  @Override
  public RESULT_TYPE visitTotalReturnSwap(final TotalReturnSwap totalReturnSwap, final DATA_TYPE data) {
    return _delegate.visitTotalReturnSwap(totalReturnSwap, data);
  }

  @Override
  public RESULT_TYPE visitBondTotalReturnSwap(final BondTotalReturnSwap totalReturnSwap) {
    return _delegate.visitBondTotalReturnSwap(totalReturnSwap);
  }

  @Override
  public RESULT_TYPE visitBondTotalReturnSwap(final BondTotalReturnSwap totalReturnSwap, final DATA_TYPE data) {
    return _delegate.visitBondTotalReturnSwap(totalReturnSwap, data);
  }

  @Override
  public RESULT_TYPE visitEquityTotalReturnSwap(final EquityTotalReturnSwap totalReturnSwap) {
    return _delegate.visitEquityTotalReturnSwap(totalReturnSwap);
  }

  @Override
  public RESULT_TYPE visitEquityTotalReturnSwap(final EquityTotalReturnSwap totalReturnSwap, final DATA_TYPE data) {
    return _delegate.visitEquityTotalReturnSwap(totalReturnSwap, data);
  }

  @Override
  public RESULT_TYPE visitEquity(final Equity equity) {
    return _delegate.visitEquity(equity);
  }

  @Override
  public RESULT_TYPE visitEquity(final Equity equity, final DATA_TYPE data) {
    return _delegate.visitEquity(equity, data);
  }
}
