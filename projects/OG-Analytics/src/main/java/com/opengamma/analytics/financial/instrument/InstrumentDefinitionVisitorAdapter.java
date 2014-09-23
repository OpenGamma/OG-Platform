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
import com.opengamma.analytics.financial.commodity.multicurvecommodity.definition.AgricultureFutureSecurityDefinition;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.definition.AgricultureFutureTransactionDefinition;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.definition.CouponCommodityCashSettleDefinition;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.definition.CouponCommodityPhysicalSettleDefinition;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.definition.EnergyFutureSecurityDefinition;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.definition.EnergyFutureTransactionDefinition;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.definition.ForwardCommodityCashSettleDefinition;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.definition.ForwardCommodityPhysicalSettleDefinition;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.definition.MetalFutureSecurityDefinition;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.definition.MetalFutureTransactionDefinition;
import com.opengamma.analytics.financial.equity.EquityDefinition;
import com.opengamma.analytics.financial.equity.future.definition.EquityFutureDefinition;
import com.opengamma.analytics.financial.equity.future.definition.EquityIndexDividendFutureDefinition;
import com.opengamma.analytics.financial.equity.future.definition.EquityIndexFutureDefinition;
import com.opengamma.analytics.financial.equity.future.definition.IndexFutureDefinition;
import com.opengamma.analytics.financial.equity.future.definition.VolatilityIndexFutureDefinition;
import com.opengamma.analytics.financial.equity.option.EquityIndexFutureOptionDefinition;
import com.opengamma.analytics.financial.equity.option.EquityIndexOptionDefinition;
import com.opengamma.analytics.financial.equity.option.EquityOptionDefinition;
import com.opengamma.analytics.financial.equity.trs.definition.EquityTotalReturnSwapDefinition;
import com.opengamma.analytics.financial.equity.variance.EquityVarianceSwapDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexNonDeliverableForwardDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexNonDeliverableOptionDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionDigitalDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionSingleBarrierDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexSwapDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillTotalReturnSwapDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillTransactionDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondCapitalIndexedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondCapitalIndexedTransactionDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedTransactionDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondIborSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondIborTransactionDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondInterestIndexedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondInterestIndexedTransactionDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondTotalReturnSwapDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositCounterpartDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositIborDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositZeroDefinition;
import com.opengamma.analytics.financial.instrument.cds.ISDACDSDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureOptionPremiumSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureOptionPremiumTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesOptionMarginTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesYieldAverageSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesYieldAverageTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionPremiumSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionPremiumTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.SwapFuturesPriceDeliverableSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.SwapFuturesPriceDeliverableTransactionDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CapFloorInflationYearOnYearInterpolationDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CapFloorInflationYearOnYearMonthlyDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CapFloorInflationZeroCouponInterpolationDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CapFloorInflationZeroCouponMonthlyDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationYearOnYearInterpolationDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationYearOnYearInterpolationWithMarginDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationYearOnYearMonthlyDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationYearOnYearMonthlyWithMarginDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationGearingDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CapFloorCMSDefinition;
import com.opengamma.analytics.financial.instrument.payment.CapFloorCMSSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CapFloorIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedAccruedCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedFxResetDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageFixingDatesCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageFixingDatesCompoundingFlatSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageFixingDatesDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageIndexDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingFlatSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingSimpleSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborRatchetDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageSpreadSimplifiedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONCompoundedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONSimplifiedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONSpreadSimplifiedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapIborIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapMultilegDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapXCcyIborIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.TotalReturnSwapDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionBermudaFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedCompoundedONCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.varianceswap.VarianceSwapDefinition;
import com.opengamma.analytics.financial.instrument.volatilityswap.FXVolatilitySwapDefinition;
import com.opengamma.analytics.financial.instrument.volatilityswap.VolatilitySwapDefinition;

/**
 * A convenience class that means that classes implementing InstrumentDefinitionVisitor do not have to implement every method.
 * @param <DATA_TYPE> Type of the data
 * @param <RESULT_TYPE> Type of the result
 */
public abstract class InstrumentDefinitionVisitorAdapter<DATA_TYPE, RESULT_TYPE> implements InstrumentDefinitionVisitor<DATA_TYPE, RESULT_TYPE> {

  @Override
  public RESULT_TYPE visitBondFixedSecurityDefinition(final BondFixedSecurityDefinition bond, final DATA_TYPE data) {
    return getException(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFixedSecurityDefinition(final BondFixedSecurityDefinition bond) {
    return getException(bond);
  }

  @Override
  public RESULT_TYPE visitBondFixedTransactionDefinition(final BondFixedTransactionDefinition bond, final DATA_TYPE data) {
    return getException(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFixedTransactionDefinition(final BondFixedTransactionDefinition bond) {
    return getException(bond);
  }

  @Override
  public RESULT_TYPE visitBondFutureDefinition(final BondFutureDefinition bond, final DATA_TYPE data) {
    return getException(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFutureDefinition(final BondFutureDefinition bond) {
    return getException(bond);
  }

  @Override
  public RESULT_TYPE visitBondFuturesSecurityDefinition(final BondFuturesSecurityDefinition bond, final DATA_TYPE data) {
    return getException(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFuturesSecurityDefinition(final BondFuturesSecurityDefinition bond) {
    return getException(bond);
  }

  @Override
  public RESULT_TYPE visitBondFuturesTransactionDefinition(final BondFuturesTransactionDefinition bond, final DATA_TYPE data) {
    return getException(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFuturesTransactionDefinition(final BondFuturesTransactionDefinition bond) {
    return getException(bond);
  }

  @Override
  public RESULT_TYPE visitBondFuturesYieldAverageSecurityDefinition(final BondFuturesYieldAverageSecurityDefinition bond, final DATA_TYPE data) {
    return getException(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFuturesYieldAverageSecurityDefinition(final BondFuturesYieldAverageSecurityDefinition bond) {
    return getException(bond);
  }

  @Override
  public RESULT_TYPE visitYieldAverageBondFuturesTransactionDefinition(final BondFuturesYieldAverageTransactionDefinition bond, final DATA_TYPE data) {
    return getException(bond, data);
  }

  @Override
  public RESULT_TYPE visitYieldAverageBondFuturesTransactionDefinition(final BondFuturesYieldAverageTransactionDefinition bond) {
    return getException(bond);
  }

  @Override
  public RESULT_TYPE visitBondFuturesOptionMarginSecurityDefinition(final BondFuturesOptionMarginSecurityDefinition bond, final DATA_TYPE data) {
    return getException(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFuturesOptionMarginSecurityDefinition(final BondFuturesOptionMarginSecurityDefinition bond) {
    return getException(bond);
  }

  @Override
  public RESULT_TYPE visitBondFuturesOptionMarginTransactionDefinition(final BondFuturesOptionMarginTransactionDefinition bond, final DATA_TYPE data) {
    return getException(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFuturesOptionMarginTransactionDefinition(final BondFuturesOptionMarginTransactionDefinition bond) {
    return getException(bond);
  }

  @Override
  public RESULT_TYPE visitBondFutureOptionPremiumSecurityDefinition(final BondFutureOptionPremiumSecurityDefinition bond, final DATA_TYPE data) {
    return getException(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFutureOptionPremiumSecurityDefinition(final BondFutureOptionPremiumSecurityDefinition bond) {
    return getException(bond);
  }

  @Override
  public RESULT_TYPE visitBondFutureOptionPremiumTransactionDefinition(final BondFutureOptionPremiumTransactionDefinition bond, final DATA_TYPE data) {
    return getException(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFutureOptionPremiumTransactionDefinition(final BondFutureOptionPremiumTransactionDefinition bond) {
    return getException(bond);
  }

  @Override
  public RESULT_TYPE visitBondIborTransactionDefinition(final BondIborTransactionDefinition bond, final DATA_TYPE data) {
    return getException(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondIborTransactionDefinition(final BondIborTransactionDefinition bond) {
    return getException(bond);
  }

  @Override
  public RESULT_TYPE visitBondIborSecurityDefinition(final BondIborSecurityDefinition bond, final DATA_TYPE data) {
    return getException(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondIborSecurityDefinition(final BondIborSecurityDefinition bond) {
    return getException(bond);
  }

  @Override
  public RESULT_TYPE visitBillSecurityDefinition(final BillSecurityDefinition bill, final DATA_TYPE data) {
    return getException(bill, data);
  }

  @Override
  public RESULT_TYPE visitBillSecurityDefinition(final BillSecurityDefinition bill) {
    return getException(bill);
  }

  @Override
  public RESULT_TYPE visitBillTransactionDefinition(final BillTransactionDefinition bill, final DATA_TYPE data) {
    return getException(bill, data);
  }

  @Override
  public RESULT_TYPE visitBillTransactionDefinition(final BillTransactionDefinition bill) {
    return getException(bill);
  }

  @Override
  public RESULT_TYPE visitCashDefinition(final CashDefinition cash, final DATA_TYPE data) {
    return getException(cash, data);
  }

  @Override
  public RESULT_TYPE visitCashDefinition(final CashDefinition cash) {
    return getException(cash);
  }

  @Override
  public RESULT_TYPE visitDepositIborDefinition(final DepositIborDefinition deposit, final DATA_TYPE data) {
    return getException(deposit, data);
  }

  @Override
  public RESULT_TYPE visitDepositIborDefinition(final DepositIborDefinition deposit) {
    return getException(deposit);
  }

  @Override
  public RESULT_TYPE visitDepositCounterpartDefinition(final DepositCounterpartDefinition deposit, final DATA_TYPE data) {
    return getException(deposit, data);
  }

  @Override
  public RESULT_TYPE visitDepositCounterpartDefinition(final DepositCounterpartDefinition deposit) {
    return getException(deposit);
  }

  @Override
  public RESULT_TYPE visitDepositZeroDefinition(final DepositZeroDefinition deposit, final DATA_TYPE data) {
    return getException(deposit, data);
  }

  @Override
  public RESULT_TYPE visitDepositZeroDefinition(final DepositZeroDefinition deposit) {
    return getException(deposit);
  }

  @Override
  public RESULT_TYPE visitForwardRateAgreementDefinition(final ForwardRateAgreementDefinition fra, final DATA_TYPE data) {
    return getException(fra, data);
  }

  @Override
  public RESULT_TYPE visitForwardRateAgreementDefinition(final ForwardRateAgreementDefinition fra) {
    return getException(fra);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureSecurityDefinition(final InterestRateFutureSecurityDefinition future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureSecurityDefinition(final InterestRateFutureSecurityDefinition future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureTransactionDefinition(final InterestRateFutureTransactionDefinition future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureTransactionDefinition(final InterestRateFutureTransactionDefinition future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitFederalFundsFutureSecurityDefinition(final FederalFundsFutureSecurityDefinition future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitFederalFundsFutureSecurityDefinition(final FederalFundsFutureSecurityDefinition future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitFederalFundsFutureTransactionDefinition(final FederalFundsFutureTransactionDefinition future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitFederalFundsFutureTransactionDefinition(final FederalFundsFutureTransactionDefinition future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionPremiumSecurityDefinition(final InterestRateFutureOptionPremiumSecurityDefinition future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionPremiumSecurityDefinition(final InterestRateFutureOptionPremiumSecurityDefinition future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionPremiumTransactionDefinition(final InterestRateFutureOptionPremiumTransactionDefinition future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionPremiumTransactionDefinition(final InterestRateFutureOptionPremiumTransactionDefinition future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionMarginSecurityDefinition(final InterestRateFutureOptionMarginSecurityDefinition future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionMarginSecurityDefinition(final InterestRateFutureOptionMarginSecurityDefinition future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionMarginTransactionDefinition(final InterestRateFutureOptionMarginTransactionDefinition future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionMarginTransactionDefinition(final InterestRateFutureOptionMarginTransactionDefinition future) {
    return getException(future);
  }

  // -----     Payment and coupon     -----

  @Override
  public RESULT_TYPE visitPaymentFixedDefinition(final PaymentFixedDefinition payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitPaymentFixedDefinition(final PaymentFixedDefinition payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCouponFixedDefinition(final CouponFixedDefinition payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponFixedDefinition(final CouponFixedDefinition payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCouponFixedCompoundingDefinition(final CouponFixedCompoundingDefinition payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponFixedCompoundingDefinition(final CouponFixedCompoundingDefinition payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCouponFixedAccruedCompoundingDefinition(final CouponFixedAccruedCompoundingDefinition payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponFixedAccruedCompoundingDefinition(final CouponFixedAccruedCompoundingDefinition payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCouponFixedFxResetDefinition(final CouponFixedFxResetDefinition payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponFixedFxResetDefinition(final CouponFixedFxResetDefinition payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborDefinition(final CouponIborDefinition payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborDefinition(final CouponIborDefinition payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborAverageDefinition(final CouponIborAverageIndexDefinition payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborAverageDefinition(final CouponIborAverageIndexDefinition payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborSpreadDefinition(final CouponIborSpreadDefinition payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborSpreadDefinition(final CouponIborSpreadDefinition payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborGearingDefinition(final CouponIborGearingDefinition payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborGearingDefinition(final CouponIborGearingDefinition payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundingDefinition(final CouponIborCompoundingDefinition payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundingDefinition(final CouponIborCompoundingDefinition payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundingSpreadDefinition(final CouponIborCompoundingSpreadDefinition payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundingSpreadDefinition(final CouponIborCompoundingSpreadDefinition payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundingFlatSpreadDefinition(final CouponIborCompoundingFlatSpreadDefinition payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundingFlatSpreadDefinition(final CouponIborCompoundingFlatSpreadDefinition payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundingSimpleSpreadDefinition(final CouponIborCompoundingSimpleSpreadDefinition payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundingSimpleSpreadDefinition(final CouponIborCompoundingSimpleSpreadDefinition payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborRatchetDefinition(final CouponIborRatchetDefinition payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborRatchetDefinition(final CouponIborRatchetDefinition payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCapFloorIborDefinition(final CapFloorIborDefinition capFloor, final DATA_TYPE data) {
    return getException(capFloor, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorIborDefinition(final CapFloorIborDefinition capFloor) {
    return getException(capFloor);
  }

  @Override
  public RESULT_TYPE visitCouponOISSimplifiedDefinition(final CouponONSimplifiedDefinition payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponOISSimplifiedDefinition(final CouponONSimplifiedDefinition payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCouponONSpreadDefinition(final CouponONSpreadDefinition payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponONSpreadDefinition(final CouponONSpreadDefinition payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCouponONSpreadSimplifiedDefinition(final CouponONSpreadSimplifiedDefinition payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponONSpreadSimplifiedDefinition(final CouponONSpreadSimplifiedDefinition payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCouponOISDefinition(final CouponONDefinition payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponOISDefinition(final CouponONDefinition payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCouponONCompoundedDefinition(final CouponONCompoundedDefinition payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponONCompoundedDefinition(final CouponONCompoundedDefinition payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCouponArithmeticAverageONDefinition(final CouponONArithmeticAverageDefinition payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponArithmeticAverageONDefinition(final CouponONArithmeticAverageDefinition payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCouponArithmeticAverageONSpreadDefinition(final CouponONArithmeticAverageSpreadDefinition payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponArithmeticAverageONSpreadDefinition(final CouponONArithmeticAverageSpreadDefinition payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCouponArithmeticAverageONSpreadSimplifiedDefinition(final CouponONArithmeticAverageSpreadSimplifiedDefinition payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponArithmeticAverageONSpreadSimplifiedDefinition(final CouponONArithmeticAverageSpreadSimplifiedDefinition payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCouponCMSDefinition(final CouponCMSDefinition payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponCMSDefinition(final CouponCMSDefinition payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCapFloorCMSDefinition(final CapFloorCMSDefinition capFloor, final DATA_TYPE data) {
    return getException(capFloor, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorCMSDefinition(final CapFloorCMSDefinition capFloor) {
    return getException(capFloor);
  }

  @Override
  public RESULT_TYPE visitCapFloorCMSSpreadDefinition(final CapFloorCMSSpreadDefinition capFloor, final DATA_TYPE data) {
    return getException(capFloor, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorCMSSpreadDefinition(final CapFloorCMSSpreadDefinition capFloorCMSSpread) {
    return getException(capFloorCMSSpread);
  }

  @Override
  public RESULT_TYPE visitCouponIborAverageFixingDatesDefinition(CouponIborAverageFixingDatesDefinition payment, DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborAverageFixingDatesDefinition(CouponIborAverageFixingDatesDefinition payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborAverageCompoundingDefinition(CouponIborAverageFixingDatesCompoundingDefinition payment, DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborAverageCompoundingDefinition(CouponIborAverageFixingDatesCompoundingDefinition payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborAverageFlatCompoundingSpreadDefinition(CouponIborAverageFixingDatesCompoundingFlatSpreadDefinition payment, DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborAverageFlatCompoundingSpreadDefinition(CouponIborAverageFixingDatesCompoundingFlatSpreadDefinition payment) {
    return getException(payment);
  }

  // -----     Annuity     -----

  @Override
  public RESULT_TYPE visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity, final DATA_TYPE data) {
    return getException(annuity, data);
  }

  @Override
  public RESULT_TYPE visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity) {
    return getException(annuity);
  }

  @Override
  public RESULT_TYPE visitSwapDefinition(final SwapDefinition swap, final DATA_TYPE data) {
    return getException(swap, data);
  }

  @Override
  public RESULT_TYPE visitSwapDefinition(final SwapDefinition swap) {
    return getException(swap);
  }

  @Override
  public RESULT_TYPE visitSwapMultilegDefinition(final SwapMultilegDefinition swap, final DATA_TYPE data) {
    return getException(swap, data);
  }

  @Override
  public RESULT_TYPE visitSwapMultilegDefinition(final SwapMultilegDefinition swap) {
    return getException(swap);
  }

  @Override
  public RESULT_TYPE visitSwapFixedIborDefinition(final SwapFixedIborDefinition swap, final DATA_TYPE data) {
    return getException(swap, data);
  }

  @Override
  public RESULT_TYPE visitSwapFixedIborDefinition(final SwapFixedIborDefinition swap) {
    return getException(swap);
  }

  @Override
  public RESULT_TYPE visitSwapFixedIborSpreadDefinition(final SwapFixedIborSpreadDefinition swap, final DATA_TYPE data) {
    return getException(swap, data);
  }

  @Override
  public RESULT_TYPE visitSwapFixedIborSpreadDefinition(final SwapFixedIborSpreadDefinition swap) {
    return getException(swap);
  }

  @Override
  public RESULT_TYPE visitSwapIborIborDefinition(final SwapIborIborDefinition swap, final DATA_TYPE data) {
    return getException(swap, data);
  }

  @Override
  public RESULT_TYPE visitSwapIborIborDefinition(final SwapIborIborDefinition swap) {
    return getException(swap);
  }

  @Override
  public RESULT_TYPE visitSwapXCcyIborIborDefinition(final SwapXCcyIborIborDefinition swap, final DATA_TYPE data) {
    return getException(swap, data);
  }

  @Override
  public RESULT_TYPE visitSwapXCcyIborIborDefinition(final SwapXCcyIborIborDefinition swap) {
    return getException(swap);
  }

  @Override
  public RESULT_TYPE visitSwaptionCashFixedIborDefinition(final SwaptionCashFixedIborDefinition swaption, final DATA_TYPE data) {
    return getException(swaption, data);
  }

  @Override
  public RESULT_TYPE visitSwaptionCashFixedIborDefinition(final SwaptionCashFixedIborDefinition swaption) {
    return getException(swaption);
  }

  @Override
  public RESULT_TYPE visitSwaptionPhysicalFixedIborDefinition(final SwaptionPhysicalFixedIborDefinition swaption, final DATA_TYPE data) {
    return getException(swaption, data);
  }

  @Override
  public RESULT_TYPE visitSwaptionPhysicalFixedIborDefinition(final SwaptionPhysicalFixedIborDefinition swaption) {
    return getException(swaption);
  }

  @Override
  public RESULT_TYPE visitSwaptionPhysicalFixedIborSpreadDefinition(final SwaptionPhysicalFixedIborSpreadDefinition swaption, final DATA_TYPE data) {
    return getException(swaption, data);
  }

  @Override
  public RESULT_TYPE visitSwaptionPhysicalFixedIborSpreadDefinition(final SwaptionPhysicalFixedIborSpreadDefinition swaption) {
    return getException(swaption);
  }

  @Override
  public RESULT_TYPE visitSwaptionBermudaFixedIborDefinition(final SwaptionBermudaFixedIborDefinition swaption, final DATA_TYPE data) {
    return getException(swaption, data);
  }

  @Override
  public RESULT_TYPE visitSwaptionBermudaFixedIborDefinition(final SwaptionBermudaFixedIborDefinition swaption) {
    return getException(swaption);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponFirstOfMonth(final CouponInflationZeroCouponMonthlyDefinition coupon, final DATA_TYPE data) {
    return getException(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponFirstOfMonth(final CouponInflationZeroCouponMonthlyDefinition coupon) {
    return getException(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolationDefinition coupon, final DATA_TYPE data) {
    return getException(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolationDefinition coupon) {
    return getException(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponMonthlyGearing(final CouponInflationZeroCouponMonthlyGearingDefinition coupon, final DATA_TYPE data) {
    return getException(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponMonthlyGearing(final CouponInflationZeroCouponMonthlyGearingDefinition coupon) {
    return getException(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponInterpolationGearing(final CouponInflationZeroCouponInterpolationGearingDefinition coupon, final DATA_TYPE data) {
    return getException(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponInterpolationGearing(final CouponInflationZeroCouponInterpolationGearingDefinition coupon) {
    return getException(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearFirstOfMonth(final CouponInflationYearOnYearMonthlyDefinition coupon, final DATA_TYPE data) {
    return getException(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearFirstOfMonth(final CouponInflationYearOnYearMonthlyDefinition coupon) {
    return getException(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearInterpolationDefinition(final CouponInflationYearOnYearInterpolationDefinition coupon, final DATA_TYPE data) {
    return getException(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearInterpolationDefinition(final CouponInflationYearOnYearInterpolationDefinition coupon) {
    return getException(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearMonthlyWithMargin(final CouponInflationYearOnYearMonthlyWithMarginDefinition coupon, final DATA_TYPE data) {
    return getException(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearMonthlyWithMargin(final CouponInflationYearOnYearMonthlyWithMarginDefinition coupon) {
    return getException(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearInterpolationWithMargin(final CouponInflationYearOnYearInterpolationWithMarginDefinition coupon, final DATA_TYPE data) {
    return getException(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearInterpolationWithMargin(final CouponInflationYearOnYearInterpolationWithMarginDefinition coupon) {
    return getException(coupon);
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationZeroCouponInterpolationDefinition(final CapFloorInflationZeroCouponInterpolationDefinition coupon, final DATA_TYPE data) {
    return getException(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationZeroCouponInterpolationDefinition(final CapFloorInflationZeroCouponInterpolationDefinition coupon) {
    return getException(coupon);
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationZeroCouponMonthlyDefinition(final CapFloorInflationZeroCouponMonthlyDefinition coupon, final DATA_TYPE data) {
    return getException(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationZeroCouponMonthlyDefinition(final CapFloorInflationZeroCouponMonthlyDefinition coupon) {
    return getException(coupon);
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationYearOnYearInterpolationDefinition(final CapFloorInflationYearOnYearInterpolationDefinition coupon, final DATA_TYPE data) {
    return getException(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationYearOnYearInterpolationDefinition(final CapFloorInflationYearOnYearInterpolationDefinition coupon) {
    return getException(coupon);
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationYearOnYearMonthlyDefinition(final CapFloorInflationYearOnYearMonthlyDefinition coupon, final DATA_TYPE data) {
    return getException(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationYearOnYearMonthlyDefinition(final CapFloorInflationYearOnYearMonthlyDefinition coupon) {
    return getException(coupon);
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurityDefinition<?> bond, final DATA_TYPE data) {
    return getException(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurityDefinition<?> bond) {
    return getException(bond);
  }

  @Override
  public RESULT_TYPE visitBondInterestIndexedSecurity(final BondInterestIndexedSecurityDefinition<?, ?> bond, final DATA_TYPE data) {
    return getException(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondInterestIndexedSecurity(final BondInterestIndexedSecurityDefinition<?, ?> bond) {
    return getException(bond);
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransactionDefinition<?> bond, final DATA_TYPE data) {
    return getException(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransactionDefinition<?> bond) {
    return getException(bond);
  }

  @Override
  public RESULT_TYPE visitBondInterestIndexedTransaction(final BondInterestIndexedTransactionDefinition<?, ?> bond, final DATA_TYPE data) {
    return getException(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondInterestIndexedTransaction(final BondInterestIndexedTransactionDefinition<?, ?> bond) {
    return getException(bond);
  }

  @Override
  public RESULT_TYPE visitCDSDefinition(final ISDACDSDefinition cds, final DATA_TYPE data) {
    return getException(cds, data);
  }

  @Override
  public RESULT_TYPE visitCDSDefinition(final ISDACDSDefinition cds) {
    return getException(cds);
  }

  @Override
  public RESULT_TYPE visitForexDefinition(final ForexDefinition fx, final DATA_TYPE data) {
    return getException(fx, data);
  }

  @Override
  public RESULT_TYPE visitForexDefinition(final ForexDefinition fx) {
    return getException(fx);
  }

  @Override
  public RESULT_TYPE visitForexSwapDefinition(final ForexSwapDefinition fx, final DATA_TYPE data) {
    return getException(fx, data);
  }

  @Override
  public RESULT_TYPE visitForexSwapDefinition(final ForexSwapDefinition fx) {
    return getException(fx);
  }

  @Override
  public RESULT_TYPE visitForexOptionVanillaDefinition(final ForexOptionVanillaDefinition fx, final DATA_TYPE data) {
    return getException(fx, data);
  }

  @Override
  public RESULT_TYPE visitForexOptionVanillaDefinition(final ForexOptionVanillaDefinition fx) {
    return getException(fx);
  }

  @Override
  public RESULT_TYPE visitForexOptionSingleBarrierDefiniton(final ForexOptionSingleBarrierDefinition fx, final DATA_TYPE data) {
    return getException(fx, data);
  }

  @Override
  public RESULT_TYPE visitForexOptionSingleBarrierDefiniton(final ForexOptionSingleBarrierDefinition fx) {
    return getException(fx);
  }

  @Override
  public RESULT_TYPE visitForexNonDeliverableForwardDefinition(final ForexNonDeliverableForwardDefinition ndf, final DATA_TYPE data) {
    return getException(ndf, data);
  }

  @Override
  public RESULT_TYPE visitForexNonDeliverableForwardDefinition(final ForexNonDeliverableForwardDefinition ndf) {
    return getException(ndf);
  }

  @Override
  public RESULT_TYPE visitForexNonDeliverableOptionDefinition(final ForexNonDeliverableOptionDefinition ndo, final DATA_TYPE data) {
    return getException(ndo, data);
  }

  @Override
  public RESULT_TYPE visitForexNonDeliverableOptionDefinition(final ForexNonDeliverableOptionDefinition ndo) {
    return getException(ndo);
  }

  @Override
  public RESULT_TYPE visitForexOptionDigitalDefinition(final ForexOptionDigitalDefinition fx, final DATA_TYPE data) {
    return getException(fx, data);
  }

  @Override
  public RESULT_TYPE visitForexOptionDigitalDefinition(final ForexOptionDigitalDefinition fx) {
    return getException(fx);
  }

  @Override
  public RESULT_TYPE visitDeliverableSwapFuturesSecurityDefinition(final SwapFuturesPriceDeliverableSecurityDefinition futures, final DATA_TYPE data) {
    return getException(futures, data);
  }

  @Override
  public RESULT_TYPE visitDeliverableSwapFuturesSecurityDefinition(final SwapFuturesPriceDeliverableSecurityDefinition futures) {
    return getException(futures);
  }

  @Override
  public RESULT_TYPE visitDeliverableSwapFuturesTransactionDefinition(final SwapFuturesPriceDeliverableTransactionDefinition futures, final DATA_TYPE data) {
    return getException(futures, data);
  }

  @Override
  public RESULT_TYPE visitDeliverableSwapFuturesTransactionDefinition(final SwapFuturesPriceDeliverableTransactionDefinition futures) {
    return getException(futures);
  }

  @Override
  public RESULT_TYPE visitMetalForwardDefinition(final MetalForwardDefinition future, final DATA_TYPE data) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitMetalForwardDefinition(final MetalForwardDefinition future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitMetalFutureDefinition(final MetalFutureDefinition future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitMetalFutureDefinition(final MetalFutureDefinition future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitMetalFutureOptionDefinition(final MetalFutureOptionDefinition future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitMetalFutureOptionDefinition(final MetalFutureOptionDefinition future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitAgricultureForwardDefinition(final AgricultureForwardDefinition future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitAgricultureForwardDefinition(final AgricultureForwardDefinition future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureDefinition(final AgricultureFutureDefinition future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureDefinition(final AgricultureFutureDefinition future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureOptionDefinition(final AgricultureFutureOptionDefinition future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureOptionDefinition(final AgricultureFutureOptionDefinition future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitEnergyForwardDefinition(final EnergyForwardDefinition future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitEnergyForwardDefinition(final EnergyForwardDefinition future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureDefinition(final EnergyFutureDefinition future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureDefinition(final EnergyFutureDefinition future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureOptionDefinition(final EnergyFutureOptionDefinition future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureOptionDefinition(final EnergyFutureOptionDefinition future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitEquityFutureDefinition(final EquityFutureDefinition future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitEquityFutureDefinition(final EquityFutureDefinition future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitEquityIndexDividendFutureDefinition(final EquityIndexDividendFutureDefinition future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitEquityIndexDividendFutureDefinition(final EquityIndexDividendFutureDefinition future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitEquityIndexOptionDefinition(final EquityIndexOptionDefinition option, final DATA_TYPE data) {
    return getException(option, data);
  }

  @Override
  public RESULT_TYPE visitEquityIndexOptionDefinition(final EquityIndexOptionDefinition option) {
    return getException(option);
  }

  @Override
  public RESULT_TYPE visitEquityIndexFutureOptionDefinition(final EquityIndexFutureOptionDefinition option, final DATA_TYPE data) {
    return getException(option, data);
  }

  @Override
  public RESULT_TYPE visitEquityIndexFutureOptionDefinition(final EquityIndexFutureOptionDefinition option) {
    return getException(option);
  }

  @Override
  public RESULT_TYPE visitEquityOptionDefinition(final EquityOptionDefinition option, final DATA_TYPE data) {
    return getException(option, data);
  }

  @Override
  public RESULT_TYPE visitEquityOptionDefinition(final EquityOptionDefinition option) {
    return getException(option);
  }

  @Override
  public RESULT_TYPE visitVarianceSwapDefinition(final VarianceSwapDefinition varianceSwap) {
    return getException(varianceSwap);
  }

  @Override
  public RESULT_TYPE visitVarianceSwapDefinition(final VarianceSwapDefinition varianceSwap, final DATA_TYPE data) {
    return getException(varianceSwap, data);
  }

  @Override
  public RESULT_TYPE visitEquityVarianceSwapDefinition(final EquityVarianceSwapDefinition varianceSwap) {
    return getException(varianceSwap);
  }

  @Override
  public RESULT_TYPE visitEquityVarianceSwapDefinition(final EquityVarianceSwapDefinition varianceSwap, final DATA_TYPE data) {
    return getException(varianceSwap, data);
  }

  @Override
  public RESULT_TYPE visitVolatilitySwapDefinition(final VolatilitySwapDefinition volatilitySwap) {
    return getException(volatilitySwap);
  }

  @Override
  public RESULT_TYPE visitVolatilitySwapDefinition(final VolatilitySwapDefinition volatilitySwap, final DATA_TYPE data) {
    return getException(volatilitySwap, data);
  }

  @Override
  public RESULT_TYPE visitFXVolatilitySwapDefinition(final FXVolatilitySwapDefinition volatilitySwap) {
    return getException(volatilitySwap);
  }

  @Override
  public RESULT_TYPE visitFXVolatilitySwapDefinition(final FXVolatilitySwapDefinition volatilitySwap, final DATA_TYPE data) {
    return getException(volatilitySwap, data);
  }

  @Override
  public RESULT_TYPE visitTotalReturnSwapDefinition(final TotalReturnSwapDefinition totalReturnSwap) {
    return getException(totalReturnSwap);
  }

  @Override
  public RESULT_TYPE visitTotalReturnSwapDefinition(final TotalReturnSwapDefinition totalReturnSwap, final DATA_TYPE data) {
    return getException(totalReturnSwap, data);
  }

  @Override
  public RESULT_TYPE visitBondTotalReturnSwapDefinition(final BondTotalReturnSwapDefinition totalReturnSwap) {
    return getException(totalReturnSwap);
  }

  @Override
  public RESULT_TYPE visitBondTotalReturnSwapDefinition(final BondTotalReturnSwapDefinition totalReturnSwap, final DATA_TYPE data) {
    return getException(totalReturnSwap, data);
  }

  @Override
  public RESULT_TYPE visitBillTotalReturnSwapDefinition(final BillTotalReturnSwapDefinition totalReturnSwap) {
    return getException(totalReturnSwap);
  }

  @Override
  public RESULT_TYPE visitBillTotalReturnSwapDefinition(final BillTotalReturnSwapDefinition totalReturnSwap, final DATA_TYPE data) {
    return getException(totalReturnSwap, data);
  }

  @Override
  public RESULT_TYPE visitEquityTotalReturnSwapDefinition(final EquityTotalReturnSwapDefinition totalReturnSwap) {
    return getException(totalReturnSwap);
  }

  @Override
  public RESULT_TYPE visitEquityTotalReturnSwapDefinition(final EquityTotalReturnSwapDefinition totalReturnSwap, final DATA_TYPE data) {
    return getException(totalReturnSwap, data);
  }

  @Override
  public RESULT_TYPE visitEquityDefinition(final EquityDefinition equity) {
    return getException(equity);
  }

  @Override
  public RESULT_TYPE visitEquityDefinition(final EquityDefinition equity, final DATA_TYPE data) {
    return getException(equity, data);
  }

  @Override
  public RESULT_TYPE visitIndexFutureDefinition(final IndexFutureDefinition definition, final DATA_TYPE data) {
    return getException(definition, data);
  }

  @Override
  public RESULT_TYPE visitIndexFutureDefinition(final IndexFutureDefinition definition) {
    return getException(definition);
  }

  @Override
  public RESULT_TYPE visitEquityIndexFutureDefinition(final EquityIndexFutureDefinition definition, final DATA_TYPE data) {
    return getException(definition, data);
  }

  @Override
  public RESULT_TYPE visitEquityIndexFutureDefinition(final EquityIndexFutureDefinition definition) {
    return getException(definition);
  }

  @Override
  public RESULT_TYPE visitVolatilityIndexFutureDefinition(final VolatilityIndexFutureDefinition definition, final DATA_TYPE data) {
    return getException(definition, data);
  }

  @Override
  public RESULT_TYPE visitVolatilityIndexFutureDefinition(final VolatilityIndexFutureDefinition definition) {
    return getException(definition);
  }

  @Override
  public RESULT_TYPE visitSwaptionCashFixedONCompoundingDefinition(final SwaptionCashFixedCompoundedONCompoundingDefinition swaption) {
    return getException(swaption);
  }

  @Override
  public RESULT_TYPE visitSwaptionCashFixedONCompoundingDefinition(final SwaptionCashFixedCompoundedONCompoundingDefinition swaption, final DATA_TYPE data) {
    return getException(swaption, data);
  }

  @Override
  public RESULT_TYPE visitMetalFutureSecurityDefinition(final MetalFutureSecurityDefinition future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitMetalFutureSecurityDefinition(final MetalFutureSecurityDefinition future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitMetalFutureTransactionDefinition(final MetalFutureTransactionDefinition future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitMetalFuturTransactioneDefinition(final MetalFutureTransactionDefinition future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureSecurityDefinition(final AgricultureFutureSecurityDefinition future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureSecurityDefinition(final AgricultureFutureSecurityDefinition future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureTransactionDefinition(final AgricultureFutureTransactionDefinition future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureTransactionDefinition(final AgricultureFutureTransactionDefinition future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureSecurityDefinition(final EnergyFutureSecurityDefinition future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureSecurityDefinition(final EnergyFutureSecurityDefinition future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureTransactionDefinition(final EnergyFutureTransactionDefinition future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureTransactionDefinition(final EnergyFutureTransactionDefinition future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitForwardCommodityCashSettleDefinition(final ForwardCommodityCashSettleDefinition forward, final DATA_TYPE data) {
    return getException(forward, data);
  }

  @Override
  public RESULT_TYPE visitForwardCommodityCashSettleDefinition(final ForwardCommodityCashSettleDefinition forward) {
    return getException(forward);
  }

  @Override
  public RESULT_TYPE visitForwardCommodityPhysicalSettleDefinition(final ForwardCommodityPhysicalSettleDefinition forward, final DATA_TYPE data) {
    return getException(forward, data);
  }

  @Override
  public RESULT_TYPE visitForwardCommodityPhysicalSettleDefinition(final ForwardCommodityPhysicalSettleDefinition forward) {
    return getException(forward);
  }

  @Override
  public RESULT_TYPE visitCouponCommodityCashSettleDefinition(final CouponCommodityCashSettleDefinition coupon, final DATA_TYPE data) {
    return getException(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponCommodityCashSettleDefinition(final CouponCommodityCashSettleDefinition coupon) {
    return getException(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponCommodityPhysicalSettleDefinition(final CouponCommodityPhysicalSettleDefinition coupon, final DATA_TYPE data) {
    return getException(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponCommodityPhysicalSettleDefinition(final CouponCommodityPhysicalSettleDefinition coupon) {
    return getException(coupon);
  }

  /**
   * Default result of calling a visit method, which is to throw an {@link UnsupportedOperationException}
   * @param definition The definition
   * @param data The data
   * @return Throws an exception
   * @throws UnsupportedOperationException
   */
  private RESULT_TYPE getException(final InstrumentDefinition<?> definition, final DATA_TYPE data) {
    if (definition != null && data != null) {
      throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support definitions of type " + definition.getClass().getSimpleName() + " with data of type "
          + data.getClass().getSimpleName());
    }
    throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support this method");
  }

  /**
   * Default result of calling a visit method, which is to throw an {@link UnsupportedOperationException}
   * @param definition The definition
   * @return Throws an exception
   * @throws UnsupportedOperationException
   */
  private RESULT_TYPE getException(final InstrumentDefinition<?> definition) {
    if (definition != null) {
      throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support definitions of type " + definition.getClass().getSimpleName());
    }
    throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support this method");
  }

}
