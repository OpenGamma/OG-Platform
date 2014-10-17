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
import com.opengamma.analytics.financial.equity.trs.definition.EquityTotalReturnSwap;
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
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTotalReturnSwap;
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
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedFxReset;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverage;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverageFixingDates;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverageFixingDatesCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverageFixingDatesCompoundingFlatSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingFlatSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingSimpleSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborFxReset;
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

/**
 * Adapter that returns the same value regardless of the type of the derivative.
 * @param <DATA_TYPE> The type of the data
 * @param <RESULT_TYPE> The type of the results
 */
@SuppressWarnings("deprecation")
public class InstrumentDerivativeVisitorSameValueAdapter<DATA_TYPE, RESULT_TYPE> implements InstrumentDerivativeVisitor<DATA_TYPE, RESULT_TYPE> {
  /** The value */
  private final RESULT_TYPE _value;

  /**
   * @param value The value
   */
  public InstrumentDerivativeVisitorSameValueAdapter(final RESULT_TYPE value) {
    _value = value;
  }

  @Override
  public RESULT_TYPE visitBondFixedSecurity(final BondFixedSecurity bond, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBondFixedTransaction(final BondFixedTransaction bond, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBondIborSecurity(final BondIborSecurity bond, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBondIborTransaction(final BondIborTransaction bond, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBillSecurity(final BillSecurity bill, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBillTransaction(final BillTransaction bill, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitGenericAnnuity(final Annuity<? extends Payment> genericAnnuity, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitFixedCouponAnnuity(final AnnuityCouponFixed fixedCouponAnnuity, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitAnnuityCouponIborRatchet(final AnnuityCouponIborRatchet annuity, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitFixedCompoundingCouponSwap(final SwapFixedCompoundingCoupon<?> swap, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitSwaptionBermudaFixedIbor(final SwaptionBermudaFixedIbor swaption, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitForexForward(final ForexForward fx, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCash(final Cash cash, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitFixedPayment(final PaymentFixed payment, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponCMS(final CouponCMS payment, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCapFloorIbor(final CapFloorIbor payment, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCapFloorCMS(final CapFloorCMS payment, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCapFloorCMSSpread(final CapFloorCMSSpread payment, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitForwardRateAgreement(final ForwardRateAgreement fra, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurity<?> bond, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBondInterestIndexedSecurity(final BondInterestIndexedSecurity<?, ?> bond, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBondInterestIndexedSecurity(final BondInterestIndexedSecurity<?, ?> bond) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransaction<?> bond, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBondInterestIndexedTransaction(final BondInterestIndexedTransaction<?, ?> bond, final DATA_TYPE data) {
    return _value;
  }



  @Override
  public RESULT_TYPE visitBondFixedSecurity(final BondFixedSecurity bond) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBondFixedTransaction(final BondFixedTransaction bond) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBondIborSecurity(final BondIborSecurity bond) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBondIborTransaction(final BondIborTransaction bond) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBillSecurity(final BillSecurity bill) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBillTransaction(final BillTransaction bill) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitGenericAnnuity(final Annuity<? extends Payment> genericAnnuity) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitFixedCouponAnnuity(final AnnuityCouponFixed fixedCouponAnnuity) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitAnnuityCouponIborRatchet(final AnnuityCouponIborRatchet annuity) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitFixedCouponSwap(final SwapFixedCoupon<?> swap) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitFixedCompoundingCouponSwap(final SwapFixedCompoundingCoupon<?> swap) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitSwaptionBermudaFixedIbor(final SwaptionBermudaFixedIbor swaption) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitSwaptionCashFixedCompoundedONCompounded(final SwaptionCashFixedCompoundedONCompounded swaption, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitSwaptionCashFixedCompoundedONCompounded(final SwaptionCashFixedCompoundedONCompounded swaption) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitSwaptionPhysicalFixedCompoundedONCompounded(final SwaptionPhysicalFixedCompoundedONCompounded swaption, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitSwaptionPhysicalFixedCompoundedONCompounded(final SwaptionPhysicalFixedCompoundedONCompounded swaption) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitForexForward(final ForexForward fx) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCash(final Cash cash) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitFixedPayment(final PaymentFixed payment) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponCMS(final CouponCMS payment) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCapFloorIbor(final CapFloorIbor payment) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCapFloorCMS(final CapFloorCMS payment) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCapFloorCMSSpread(final CapFloorCMSSpread payment) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitForwardRateAgreement(final ForwardRateAgreement fra) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurity<?> bond) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransaction<?> bond) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBondInterestIndexedTransaction(final BondInterestIndexedTransaction<?, ?> bond) {
    return _value;
  }


  @Override
  public RESULT_TYPE visitCouponFixed(final CouponFixed payment, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponFixed(final CouponFixed payment) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponFixedCompounding(final CouponFixedCompounding payment, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponFixedCompounding(final CouponFixedCompounding payment) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponFixedAccruedCompounding(final CouponFixedAccruedCompounding payment, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponFixedAccruedCompounding(final CouponFixedAccruedCompounding payment) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitInterpolatedStubCoupon(final InterpolatedStubCoupon<? extends DepositIndexCoupon<? extends IndexDeposit>, ? extends IndexDeposit> payment, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitInterpolatedStubCoupon(final InterpolatedStubCoupon<? extends DepositIndexCoupon<? extends IndexDeposit>, ? extends IndexDeposit> payment) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponFixedFxReset(final CouponFixedFxReset payment, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponFixedFxReset(final CouponFixedFxReset payment) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponIborFxReset(final CouponIborFxReset payment, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponIborFxReset(final CouponIborFxReset payment) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponIbor(final CouponIbor payment, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponIbor(final CouponIbor payment) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponIborAverage(final CouponIborAverage payment, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponIborAverage(final CouponIborAverage payment) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponIborSpread(final CouponIborSpread payment, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponIborSpread(final CouponIborSpread payment) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponIborGearing(final CouponIborGearing payment) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponIborGearing(final CouponIborGearing payment, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponIborCompounding(final CouponIborCompounding payment) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponIborCompounding(final CouponIborCompounding payment, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundingSpread(final CouponIborCompoundingSpread payment) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundingSpread(final CouponIborCompoundingSpread payment, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundingFlatSpread(final CouponIborCompoundingFlatSpread payment) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundingFlatSpread(final CouponIborCompoundingFlatSpread payment, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundingSimpleSpread(final CouponIborCompoundingSimpleSpread payment) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundingSimpleSpread(final CouponIborCompoundingSimpleSpread payment, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponOIS(final CouponON payment, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponOIS(final CouponON payment) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponONCompounded(final CouponONCompounded payment, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponONCompounded(final CouponONCompounded payment) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponONSpread(final CouponONSpread payment, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponONSpread(final CouponONSpread payment) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponONArithmeticAverage(final CouponONArithmeticAverage payment, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponONArithmeticAverage(final CouponONArithmeticAverage payment) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponONArithmeticAverageSpread(final CouponONArithmeticAverageSpread payment, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponONArithmeticAverageSpread(final CouponONArithmeticAverageSpread payment) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponONArithmeticAverageSpreadSimplified(final CouponONArithmeticAverageSpreadSimplified payment, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponONArithmeticAverageSpreadSimplified(final CouponONArithmeticAverageSpreadSimplified payment) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponIborAverageFixingDates(final CouponIborAverageFixingDates payment, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponIborAverageFixingDates(final CouponIborAverageFixingDates payment) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponIborAverageCompounding(final CouponIborAverageFixingDatesCompounding payment, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponIborAverageCompounding(final CouponIborAverageFixingDatesCompounding payment) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponIborAverageFlatCompoundingSpread(final CouponIborAverageFixingDatesCompoundingFlatSpread payment, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponIborAverageFlatCompoundingSpread(final CouponIborAverageFixingDatesCompoundingFlatSpread payment) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitSwap(final Swap<?, ?> swap, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitSwap(final Swap<?, ?> swap) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitSwapMultileg(final SwapMultileg swap, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitSwapMultileg(final SwapMultileg swap) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponMonthly(final CouponInflationZeroCouponMonthly coupon, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponMonthly(final CouponInflationZeroCouponMonthly coupon) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponMonthlyGearing(final CouponInflationZeroCouponMonthlyGearing coupon, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponMonthlyGearing(final CouponInflationZeroCouponMonthlyGearing coupon) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolation coupon, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolation coupon) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponInterpolationGearing(final CouponInflationZeroCouponInterpolationGearing coupon, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponInterpolationGearing(final CouponInflationZeroCouponInterpolationGearing coupon) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearMonthly(final CouponInflationYearOnYearMonthly coupon, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearMonthly(final CouponInflationYearOnYearMonthly coupon) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearInterpolation(final CouponInflationYearOnYearInterpolation coupon, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearInterpolation(final CouponInflationYearOnYearInterpolation coupon) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearMonthlyWithMargin(final CouponInflationYearOnYearMonthlyWithMargin coupon, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearMonthlyWithMargin(final CouponInflationYearOnYearMonthlyWithMargin coupon) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearInterpolationWithMargin(final CouponInflationYearOnYearInterpolationWithMargin coupon, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearInterpolationWithMargin(final CouponInflationYearOnYearInterpolationWithMargin coupon) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationZeroCouponInterpolation(final CapFloorInflationZeroCouponInterpolation coupon, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationZeroCouponInterpolation(final CapFloorInflationZeroCouponInterpolation coupon) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationZeroCouponMonthly(final CapFloorInflationZeroCouponMonthly coupon, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationZeroCouponMonthly(final CapFloorInflationZeroCouponMonthly coupon) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationYearOnYearInterpolation(final CapFloorInflationYearOnYearInterpolation coupon, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationYearOnYearMonthly(final CapFloorInflationYearOnYearMonthly coupon, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationYearOnYearMonthly(final CapFloorInflationYearOnYearMonthly coupon) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationYearOnYearInterpolation(final CapFloorInflationYearOnYearInterpolation coupon) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBondFuture(final BondFuture bondFuture, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBondFuture(final BondFuture future) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBondFuturesSecurity(final BondFuturesSecurity bondFuture, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBondFuturesSecurity(final BondFuturesSecurity future) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBondFuturesTransaction(final BondFuturesTransaction bondFuture, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBondFuturesTransaction(final BondFuturesTransaction future) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBondFuturesYieldAverageSecurity(final BondFuturesYieldAverageSecurity bondFuture, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBondFuturesYieldAverageSecurity(final BondFuturesYieldAverageSecurity future) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitYieldAverageBondFuturesTransaction(final BondFuturesYieldAverageTransaction bondFuture, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitYieldAverageBondFuturesTransaction(final BondFuturesYieldAverageTransaction future) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureTransaction(final InterestRateFutureTransaction future, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureTransaction(final InterestRateFutureTransaction future) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureSecurity(final InterestRateFutureSecurity future, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureSecurity(final InterestRateFutureSecurity future) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity future, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity future) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitFederalFundsFutureTransaction(final FederalFundsFutureTransaction future, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitFederalFundsFutureTransaction(final FederalFundsFutureTransaction future) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitSwapFuturesPriceDeliverableSecurity(final SwapFuturesPriceDeliverableSecurity futures, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitSwapFuturesPriceDeliverableSecurity(final SwapFuturesPriceDeliverableSecurity futures) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitSwapFuturesPriceDeliverableTransaction(final SwapFuturesPriceDeliverableTransaction futures, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitSwapFuturesPriceDeliverableTransaction(final SwapFuturesPriceDeliverableTransaction futures) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBondFuturesOptionMarginSecurity(final BondFuturesOptionMarginSecurity option, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBondFuturesOptionMarginSecurity(final BondFuturesOptionMarginSecurity option) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBondFuturesOptionMarginTransaction(final BondFuturesOptionMarginTransaction option, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBondFuturesOptionMarginTransaction(final BondFuturesOptionMarginTransaction option) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBondFutureOptionPremiumSecurity(final BondFutureOptionPremiumSecurity option, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBondFutureOptionPremiumSecurity(final BondFutureOptionPremiumSecurity option) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBondFutureOptionPremiumTransaction(final BondFutureOptionPremiumTransaction option, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBondFutureOptionPremiumTransaction(final BondFutureOptionPremiumTransaction option) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity option, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity option) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionPremiumSecurity(final InterestRateFutureOptionPremiumSecurity option, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionPremiumSecurity(final InterestRateFutureOptionPremiumSecurity option) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionPremiumTransaction(final InterestRateFutureOptionPremiumTransaction option, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionPremiumTransaction(final InterestRateFutureOptionPremiumTransaction option) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitDepositIbor(final DepositIbor deposit, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitDepositIbor(final DepositIbor deposit) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitDepositCounterpart(final DepositCounterpart deposit, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitDepositCounterpart(final DepositCounterpart deposit) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitDepositZero(final DepositZero deposit, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitDepositZero(final DepositZero deposit) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitForex(final Forex derivative, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitForex(final Forex derivative) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitForexSwap(final ForexSwap derivative, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitForexSwap(final ForexSwap derivative) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitForexOptionVanilla(final ForexOptionVanilla derivative, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitForexOptionVanilla(final ForexOptionVanilla derivative) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitForexOptionSingleBarrier(final ForexOptionSingleBarrier derivative, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitForexOptionSingleBarrier(final ForexOptionSingleBarrier derivative) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitForexNonDeliverableForward(final ForexNonDeliverableForward derivative, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitForexNonDeliverableForward(final ForexNonDeliverableForward derivative) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitForexNonDeliverableOption(final ForexNonDeliverableOption derivative, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitForexNonDeliverableOption(final ForexNonDeliverableOption derivative) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitForexOptionDigital(final ForexOptionDigital derivative, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitForexOptionDigital(final ForexOptionDigital derivative) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitMetalForward(final MetalForward future, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitMetalForward(final MetalForward future) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitMetalFuture(final MetalFuture future, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitMetalFuture(final MetalFuture future) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitMetalFutureOption(final MetalFutureOption future, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitMetalFutureOption(final MetalFutureOption future) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitAgricultureForward(final AgricultureForward future, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitAgricultureForward(final AgricultureForward future) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitAgricultureFuture(final AgricultureFuture future, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitAgricultureFuture(final AgricultureFuture future) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureOption(final AgricultureFutureOption future, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureOption(final AgricultureFutureOption future) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitEnergyForward(final EnergyForward future, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitEnergyForward(final EnergyForward future) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitEnergyFuture(final EnergyFuture future, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitEnergyFuture(final EnergyFuture future) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitEnergyFutureOption(final EnergyFutureOption future, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitEnergyFutureOption(final EnergyFutureOption future) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitEquityFuture(final EquityFuture future, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitEquityFuture(final EquityFuture future) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitEquityIndexDividendFuture(final EquityIndexDividendFuture future, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitEquityIndexDividendFuture(final EquityIndexDividendFuture future) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitEquityIndexOption(final EquityIndexOption option, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitEquityIndexOption(final EquityIndexOption option) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitEquityIndexFutureOption(final EquityIndexFutureOption option, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitEquityIndexFutureOption(final EquityIndexFutureOption option) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitEquityOption(final EquityOption option, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitEquityOption(final EquityOption option) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitVarianceSwap(final VarianceSwap varianceSwap) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitVarianceSwap(final VarianceSwap varianceSwap, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitEquityVarianceSwap(final EquityVarianceSwap varianceSwap) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitEquityVarianceSwap(final EquityVarianceSwap varianceSwap, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitVolatilitySwap(final VolatilitySwap volatilitySwap) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitVolatilitySwap(final VolatilitySwap volatilitySwap, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitFXVolatilitySwap(final FXVolatilitySwap volatilitySwap) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitFXVolatilitySwap(final FXVolatilitySwap volatilitySwap, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCashSettledFuture(final CashSettledFuture future, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCashSettledFuture(final CashSettledFuture future) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitIndexFuture(final IndexFuture future, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitIndexFuture(final IndexFuture future) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitEquityIndexFuture(final EquityIndexFuture future, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitEquityIndexFuture(final EquityIndexFuture future) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitVolatilityIndexFuture(final VolatilityIndexFuture future, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitVolatilityIndexFuture(final VolatilityIndexFuture future) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitMetalFutureSecurity(final MetalFutureSecurity future, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitMetalFutureSecurity(final MetalFutureSecurity future) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitEnergyFutureSecurity(final EnergyFutureSecurity future, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitEnergyFutureSecurity(final EnergyFutureSecurity future) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureSecurity(final AgricultureFutureSecurity future, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureSecurity(final AgricultureFutureSecurity future) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitMetalFutureTransaction(final MetalFutureTransaction future, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitMetalFutureTransaction(final MetalFutureTransaction future) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitEnergyFutureTransaction(final EnergyFutureTransaction future, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitEnergyFutureTransaction(final EnergyFutureTransaction future) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureTransaction(final AgricultureFutureTransaction future, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureTransaction(final AgricultureFutureTransaction future) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponCommodityCashSettle(final CouponCommodityCashSettle coupon, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponCommodityCashSettle(final CouponCommodityCashSettle coupon) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponCommodityPhysicalSettle(final CouponCommodityPhysicalSettle coupon, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitCouponCommodityPhysicalSettle(final CouponCommodityPhysicalSettle coupon) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitForwardCommodityCashSettle(final ForwardCommodityCashSettle forward, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitForwardCommodityCashSettle(final ForwardCommodityCashSettle forward) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitForwardCommodityPhysicalSettle(final ForwardCommodityPhysicalSettle forward, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitForwardCommodityPhysicalSettle(final ForwardCommodityPhysicalSettle forward) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitTotalReturnSwap(final TotalReturnSwap totalReturnSwap) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitTotalReturnSwap(final TotalReturnSwap totalReturnSwap, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBondTotalReturnSwap(final BondTotalReturnSwap totalReturnSwap) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBondTotalReturnSwap(final BondTotalReturnSwap totalReturnSwap, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBillTotalReturnSwap(final BillTotalReturnSwap totalReturnSwap) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitBillTotalReturnSwap(final BillTotalReturnSwap totalReturnSwap, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitEquityTotalReturnSwap(final EquityTotalReturnSwap totalReturnSwap) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitEquityTotalReturnSwap(final EquityTotalReturnSwap totalReturnSwap, final DATA_TYPE data) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitEquity(final Equity equity) {
    return _value;
  }

  @Override
  public RESULT_TYPE visitEquity(final Equity equity, final DATA_TYPE data) {
    return _value;
  }
}
