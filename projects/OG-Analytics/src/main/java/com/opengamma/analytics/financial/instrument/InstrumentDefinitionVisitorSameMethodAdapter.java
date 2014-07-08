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
import com.opengamma.analytics.financial.instrument.future.BondFuturesYieldAverageSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesYieldAverageTransactionDefinition;
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
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingFlatSpreadDefinition;
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
 * Adapter that uses the same method regardless of the type of the instrument definition.
 * @param <DATA_TYPE> The type of the data
 * @param <RESULT_TYPE> The type of the results
 */
public abstract class InstrumentDefinitionVisitorSameMethodAdapter<DATA_TYPE, RESULT_TYPE> implements InstrumentDefinitionVisitor<DATA_TYPE, RESULT_TYPE> {

  /**
   * Calculates the result
   * @param instrument The instrument
   * @return The result
   */
  public abstract RESULT_TYPE visit(InstrumentDefinition<?> instrument);

  /**
   * Calculates the result
   * @param instrument The instrument
   * @param data The data
   * @return The result
   */
  public abstract RESULT_TYPE visit(InstrumentDefinition<?> instrument, DATA_TYPE data);

  @Override
  public RESULT_TYPE visitBondFixedSecurityDefinition(final BondFixedSecurityDefinition bond, final DATA_TYPE data) {
    return visit(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFixedSecurityDefinition(final BondFixedSecurityDefinition bond) {
    return visit(bond);
  }

  @Override
  public RESULT_TYPE visitBondFixedTransactionDefinition(final BondFixedTransactionDefinition bond, final DATA_TYPE data) {
    return visit(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFixedTransactionDefinition(final BondFixedTransactionDefinition bond) {
    return visit(bond);
  }

  @Override
  public RESULT_TYPE visitBondFutureDefinition(final BondFutureDefinition bond, final DATA_TYPE data) {
    return visit(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFutureDefinition(final BondFutureDefinition bond) {
    return visit(bond);
  }

  @Override
  public RESULT_TYPE visitBondFuturesSecurityDefinition(final BondFuturesSecurityDefinition bond, final DATA_TYPE data) {
    return visit(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFuturesSecurityDefinition(final BondFuturesSecurityDefinition bond) {
    return visit(bond);
  }

  @Override
  public RESULT_TYPE visitBondFuturesTransactionDefinition(final BondFuturesTransactionDefinition bond, final DATA_TYPE data) {
    return visit(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFuturesTransactionDefinition(final BondFuturesTransactionDefinition bond) {
    return visit(bond);
  }

  @Override
  public RESULT_TYPE visitBondFuturesYieldAverageSecurityDefinition(final BondFuturesYieldAverageSecurityDefinition bond, final DATA_TYPE data) {
    return visit(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFuturesYieldAverageSecurityDefinition(final BondFuturesYieldAverageSecurityDefinition bond) {
    return visit(bond);
  }

  @Override
  public RESULT_TYPE visitYieldAverageBondFuturesTransactionDefinition(final BondFuturesYieldAverageTransactionDefinition bond, final DATA_TYPE data) {
    return visit(bond, data);
  }

  @Override
  public RESULT_TYPE visitYieldAverageBondFuturesTransactionDefinition(final BondFuturesYieldAverageTransactionDefinition bond) {
    return visit(bond);
  }

  @Override
  public RESULT_TYPE visitBondFuturesOptionMarginSecurityDefinition(final BondFuturesOptionMarginSecurityDefinition bond, final DATA_TYPE data) {
    return visit(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFuturesOptionMarginSecurityDefinition(final BondFuturesOptionMarginSecurityDefinition bond) {
    return visit(bond);
  }

  @Override
  public RESULT_TYPE visitBondFuturesOptionMarginTransactionDefinition(final BondFuturesOptionMarginTransactionDefinition bond, final DATA_TYPE data) {
    return visit(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFuturesOptionMarginTransactionDefinition(final BondFuturesOptionMarginTransactionDefinition bond) {
    return visit(bond);
  }

  @Override
  public RESULT_TYPE visitBondFutureOptionPremiumSecurityDefinition(final BondFutureOptionPremiumSecurityDefinition bond, final DATA_TYPE data) {
    return visit(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFutureOptionPremiumSecurityDefinition(final BondFutureOptionPremiumSecurityDefinition bond) {
    return visit(bond);
  }

  @Override
  public RESULT_TYPE visitBondFutureOptionPremiumTransactionDefinition(final BondFutureOptionPremiumTransactionDefinition bond, final DATA_TYPE data) {
    return visit(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFutureOptionPremiumTransactionDefinition(final BondFutureOptionPremiumTransactionDefinition bond) {
    return visit(bond);
  }

  @Override
  public RESULT_TYPE visitBondIborTransactionDefinition(final BondIborTransactionDefinition bond, final DATA_TYPE data) {
    return visit(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondIborTransactionDefinition(final BondIborTransactionDefinition bond) {
    return visit(bond);
  }

  @Override
  public RESULT_TYPE visitBondIborSecurityDefinition(final BondIborSecurityDefinition bond, final DATA_TYPE data) {
    return visit(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondIborSecurityDefinition(final BondIborSecurityDefinition bond) {
    return visit(bond);
  }

  @Override
  public RESULT_TYPE visitBillSecurityDefinition(final BillSecurityDefinition bill, final DATA_TYPE data) {
    return visit(bill, data);
  }

  @Override
  public RESULT_TYPE visitBillSecurityDefinition(final BillSecurityDefinition bill) {
    return visit(bill);
  }

  @Override
  public RESULT_TYPE visitBillTransactionDefinition(final BillTransactionDefinition bill, final DATA_TYPE data) {
    return visit(bill, data);
  }

  @Override
  public RESULT_TYPE visitBillTransactionDefinition(final BillTransactionDefinition bill) {
    return visit(bill);
  }

  @Override
  public RESULT_TYPE visitCashDefinition(final CashDefinition cash, final DATA_TYPE data) {
    return visit(cash, data);
  }

  @Override
  public RESULT_TYPE visitCashDefinition(final CashDefinition cash) {
    return visit(cash);
  }

  @Override
  public RESULT_TYPE visitDepositIborDefinition(final DepositIborDefinition deposit, final DATA_TYPE data) {
    return visit(deposit, data);
  }

  @Override
  public RESULT_TYPE visitDepositIborDefinition(final DepositIborDefinition deposit) {
    return visit(deposit);
  }

  @Override
  public RESULT_TYPE visitDepositCounterpartDefinition(final DepositCounterpartDefinition deposit, final DATA_TYPE data) {
    return visit(deposit, data);
  }

  @Override
  public RESULT_TYPE visitDepositCounterpartDefinition(final DepositCounterpartDefinition deposit) {
    return visit(deposit);
  }

  @Override
  public RESULT_TYPE visitDepositZeroDefinition(final DepositZeroDefinition deposit, final DATA_TYPE data) {
    return visit(deposit, data);
  }

  @Override
  public RESULT_TYPE visitDepositZeroDefinition(final DepositZeroDefinition deposit) {
    return visit(deposit);
  }

  @Override
  public RESULT_TYPE visitForwardRateAgreementDefinition(final ForwardRateAgreementDefinition fra, final DATA_TYPE data) {
    return visit(fra, data);
  }

  @Override
  public RESULT_TYPE visitForwardRateAgreementDefinition(final ForwardRateAgreementDefinition fra) {
    return visit(fra);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureSecurityDefinition(final InterestRateFutureSecurityDefinition future, final DATA_TYPE data) {
    return visit(future, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureSecurityDefinition(final InterestRateFutureSecurityDefinition future) {
    return visit(future);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureTransactionDefinition(final InterestRateFutureTransactionDefinition future, final DATA_TYPE data) {
    return visit(future, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureTransactionDefinition(final InterestRateFutureTransactionDefinition future) {
    return visit(future);
  }

  @Override
  public RESULT_TYPE visitFederalFundsFutureSecurityDefinition(final FederalFundsFutureSecurityDefinition future, final DATA_TYPE data) {
    return visit(future, data);
  }

  @Override
  public RESULT_TYPE visitFederalFundsFutureSecurityDefinition(final FederalFundsFutureSecurityDefinition future) {
    return visit(future);
  }

  @Override
  public RESULT_TYPE visitFederalFundsFutureTransactionDefinition(final FederalFundsFutureTransactionDefinition future, final DATA_TYPE data) {
    return visit(future, data);
  }

  @Override
  public RESULT_TYPE visitFederalFundsFutureTransactionDefinition(final FederalFundsFutureTransactionDefinition future) {
    return visit(future);
  }

  @Override
  public RESULT_TYPE visitDeliverableSwapFuturesSecurityDefinition(final SwapFuturesPriceDeliverableSecurityDefinition futures, final DATA_TYPE data) {
    return visit(futures, data);
  }

  @Override
  public RESULT_TYPE visitDeliverableSwapFuturesSecurityDefinition(final SwapFuturesPriceDeliverableSecurityDefinition futures) {
    return visit(futures);
  }

  @Override
  public RESULT_TYPE visitDeliverableSwapFuturesTransactionDefinition(final SwapFuturesPriceDeliverableTransactionDefinition futures, final DATA_TYPE data) {
    return visit(futures, data);
  }

  @Override
  public RESULT_TYPE visitDeliverableSwapFuturesTransactionDefinition(final SwapFuturesPriceDeliverableTransactionDefinition futures) {
    return visit(futures);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionPremiumSecurityDefinition(final InterestRateFutureOptionPremiumSecurityDefinition future, final DATA_TYPE data) {
    return visit(future, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionPremiumSecurityDefinition(final InterestRateFutureOptionPremiumSecurityDefinition future) {
    return visit(future);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionPremiumTransactionDefinition(final InterestRateFutureOptionPremiumTransactionDefinition future, final DATA_TYPE data) {
    return visit(future, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionPremiumTransactionDefinition(final InterestRateFutureOptionPremiumTransactionDefinition future) {
    return visit(future);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionMarginSecurityDefinition(final InterestRateFutureOptionMarginSecurityDefinition future, final DATA_TYPE data) {
    return visit(future, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionMarginSecurityDefinition(final InterestRateFutureOptionMarginSecurityDefinition future) {
    return visit(future);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionMarginTransactionDefinition(final InterestRateFutureOptionMarginTransactionDefinition future, final DATA_TYPE data) {
    return visit(future, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionMarginTransactionDefinition(final InterestRateFutureOptionMarginTransactionDefinition future) {
    return visit(future);
  }

  // -----     Payment and coupon     -----

  @Override
  public RESULT_TYPE visitPaymentFixedDefinition(final PaymentFixedDefinition payment, final DATA_TYPE data) {
    return visit(payment, data);
  }

  @Override
  public RESULT_TYPE visitPaymentFixedDefinition(final PaymentFixedDefinition payment) {
    return visit(payment);
  }

  @Override
  public RESULT_TYPE visitCouponFixedDefinition(final CouponFixedDefinition payment, final DATA_TYPE data) {
    return visit(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponFixedDefinition(final CouponFixedDefinition payment) {
    return visit(payment);
  }

  @Override
  public RESULT_TYPE visitCouponFixedCompoundingDefinition(final CouponFixedCompoundingDefinition payment, final DATA_TYPE data) {
    return visit(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponFixedCompoundingDefinition(final CouponFixedCompoundingDefinition payment) {
    return visit(payment);
  }

  @Override
  public RESULT_TYPE visitCouponFixedAccruedCompoundingDefinition(final CouponFixedAccruedCompoundingDefinition payment, final DATA_TYPE data) {
    return visit(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponFixedAccruedCompoundingDefinition(final CouponFixedAccruedCompoundingDefinition payment) {
    return visit(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborDefinition(final CouponIborDefinition payment, final DATA_TYPE data) {
    return visit(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborDefinition(final CouponIborDefinition payment) {
    return visit(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborAverageDefinition(final CouponIborAverageDefinition payment, final DATA_TYPE data) {
    return visit(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborAverageDefinition(final CouponIborAverageDefinition payment) {
    return visit(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborSpreadDefinition(final CouponIborSpreadDefinition payment, final DATA_TYPE data) {
    return visit(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborSpreadDefinition(final CouponIborSpreadDefinition payment) {
    return visit(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborGearingDefinition(final CouponIborGearingDefinition payment, final DATA_TYPE data) {
    return visit(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborGearingDefinition(final CouponIborGearingDefinition payment) {
    return visit(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundingDefinition(final CouponIborCompoundingDefinition payment, final DATA_TYPE data) {
    return visit(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundingDefinition(final CouponIborCompoundingDefinition payment) {
    return visit(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundingSpreadDefinition(final CouponIborCompoundingSpreadDefinition payment, final DATA_TYPE data) {
    return visit(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundingSpreadDefinition(final CouponIborCompoundingSpreadDefinition payment) {
    return visit(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundingFlatSpreadDefinition(final CouponIborCompoundingFlatSpreadDefinition payment, final DATA_TYPE data) {
    return visit(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundingFlatSpreadDefinition(final CouponIborCompoundingFlatSpreadDefinition payment) {
    return visit(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborRatchetDefinition(final CouponIborRatchetDefinition payment, final DATA_TYPE data) {
    return visit(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborRatchetDefinition(final CouponIborRatchetDefinition payment) {
    return visit(payment);
  }

  @Override
  public RESULT_TYPE visitCapFloorIborDefinition(final CapFloorIborDefinition payment, final DATA_TYPE data) {
    return visit(payment, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorIborDefinition(final CapFloorIborDefinition payment) {
    return visit(payment);
  }

  @Override
  public RESULT_TYPE visitCouponOISSimplifiedDefinition(final CouponONSimplifiedDefinition payment, final DATA_TYPE data) {
    return visit(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponOISSimplifiedDefinition(final CouponONSimplifiedDefinition payment) {
    return visit(payment);
  }

  @Override
  public RESULT_TYPE visitCouponONSpreadSimplifiedDefinition(final CouponONSpreadSimplifiedDefinition payment, final DATA_TYPE data) {
    return visit(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponONSpreadSimplifiedDefinition(final CouponONSpreadSimplifiedDefinition payment) {
    return visit(payment);
  }

  @Override
  public RESULT_TYPE visitCouponONSpreadDefinition(final CouponONSpreadDefinition payment, final DATA_TYPE data) {
    return visit(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponONSpreadDefinition(final CouponONSpreadDefinition payment) {
    return visit(payment);
  }
  
  @Override
  public RESULT_TYPE visitCouponOISDefinition(final CouponONDefinition payment, final DATA_TYPE data) {
    return visit(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponOISDefinition(final CouponONDefinition payment) {
    return visit(payment);
  }

  @Override
  public RESULT_TYPE visitCouponONCompoundedDefinition(final CouponONCompoundedDefinition payment, final DATA_TYPE data) {
    return visit(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponONCompoundedDefinition(final CouponONCompoundedDefinition payment) {
    return visit(payment);
  }

  @Override
  public RESULT_TYPE visitCouponArithmeticAverageONDefinition(final CouponONArithmeticAverageDefinition payment, final DATA_TYPE data) {
    return visit(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponArithmeticAverageONDefinition(final CouponONArithmeticAverageDefinition payment) {
    return visit(payment);
  }

  @Override
  public RESULT_TYPE visitCouponArithmeticAverageONSpreadDefinition(final CouponONArithmeticAverageSpreadDefinition payment, final DATA_TYPE data) {
    return visit(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponArithmeticAverageONSpreadDefinition(final CouponONArithmeticAverageSpreadDefinition payment) {
    return visit(payment);
  }

  @Override
  public RESULT_TYPE visitCouponArithmeticAverageONSpreadSimplifiedDefinition(final CouponONArithmeticAverageSpreadSimplifiedDefinition payment, final DATA_TYPE data) {
    return visit(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponArithmeticAverageONSpreadSimplifiedDefinition(final CouponONArithmeticAverageSpreadSimplifiedDefinition payment) {
    return visit(payment);
  }

  @Override
  public RESULT_TYPE visitCouponCMSDefinition(final CouponCMSDefinition payment, final DATA_TYPE data) {
    return visit(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponCMSDefinition(final CouponCMSDefinition payment) {
    return visit(payment);
  }

  @Override
  public RESULT_TYPE visitCapFloorCMSDefinition(final CapFloorCMSDefinition payment, final DATA_TYPE data) {
    return visit(payment, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorCMSDefinition(final CapFloorCMSDefinition payment) {
    return visit(payment);
  }

  @Override
  public RESULT_TYPE visitCapFloorCMSSpreadDefinition(final CapFloorCMSSpreadDefinition payment, final DATA_TYPE data) {
    return visit(payment, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorCMSSpreadDefinition(final CapFloorCMSSpreadDefinition payment) {
    return visit(payment);
  }

  // -----     Annuity     -----

  @Override
  public RESULT_TYPE visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity, final DATA_TYPE data) {
    return visit(annuity, data);
  }

  @Override
  public RESULT_TYPE visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity) {
    return visit(annuity);
  }

  @Override
  public RESULT_TYPE visitSwapDefinition(final SwapDefinition swap, final DATA_TYPE data) {
    return visit(swap, data);
  }

  @Override
  public RESULT_TYPE visitSwapDefinition(final SwapDefinition swap) {
    return visit(swap);
  }

  @Override
  public RESULT_TYPE visitSwapMultilegDefinition(final SwapMultilegDefinition swap, final DATA_TYPE data) {
    return visit(swap, data);
  }

  @Override
  public RESULT_TYPE visitSwapMultilegDefinition(final SwapMultilegDefinition swap) {
    return visit(swap);
  }

  @Override
  public RESULT_TYPE visitSwapFixedIborDefinition(final SwapFixedIborDefinition swap, final DATA_TYPE data) {
    return visit(swap, data);
  }

  @Override
  public RESULT_TYPE visitSwapFixedIborDefinition(final SwapFixedIborDefinition swap) {
    return visit(swap);
  }

  @Override
  public RESULT_TYPE visitSwapFixedIborSpreadDefinition(final SwapFixedIborSpreadDefinition swap, final DATA_TYPE data) {
    return visit(swap, data);
  }

  @Override
  public RESULT_TYPE visitSwapFixedIborSpreadDefinition(final SwapFixedIborSpreadDefinition swap) {
    return visit(swap);
  }

  @Override
  public RESULT_TYPE visitSwapIborIborDefinition(final SwapIborIborDefinition swap, final DATA_TYPE data) {
    return visit(swap, data);
  }

  @Override
  public RESULT_TYPE visitSwapIborIborDefinition(final SwapIborIborDefinition swap) {
    return visit(swap);
  }

  @Override
  public RESULT_TYPE visitSwapXCcyIborIborDefinition(final SwapXCcyIborIborDefinition swap, final DATA_TYPE data) {
    return visit(swap, data);
  }

  @Override
  public RESULT_TYPE visitSwapXCcyIborIborDefinition(final SwapXCcyIborIborDefinition swap) {
    return visit(swap);
  }

  @Override
  public RESULT_TYPE visitSwaptionCashFixedIborDefinition(final SwaptionCashFixedIborDefinition swaption, final DATA_TYPE data) {
    return visit(swaption, data);
  }

  @Override
  public RESULT_TYPE visitSwaptionCashFixedIborDefinition(final SwaptionCashFixedIborDefinition swaption) {
    return visit(swaption);
  }

  @Override
  public RESULT_TYPE visitSwaptionPhysicalFixedIborDefinition(final SwaptionPhysicalFixedIborDefinition swaption, final DATA_TYPE data) {
    return visit(swaption, data);
  }

  @Override
  public RESULT_TYPE visitSwaptionPhysicalFixedIborDefinition(final SwaptionPhysicalFixedIborDefinition swaption) {
    return visit(swaption);
  }

  @Override
  public RESULT_TYPE visitSwaptionPhysicalFixedIborSpreadDefinition(final SwaptionPhysicalFixedIborSpreadDefinition swaption, final DATA_TYPE data) {
    return visit(swaption, data);
  }

  @Override
  public RESULT_TYPE visitSwaptionPhysicalFixedIborSpreadDefinition(final SwaptionPhysicalFixedIborSpreadDefinition swaption) {
    return visit(swaption);
  }

  @Override
  public RESULT_TYPE visitSwaptionBermudaFixedIborDefinition(final SwaptionBermudaFixedIborDefinition swaption, final DATA_TYPE data) {
    return visit(swaption, data);
  }

  @Override
  public RESULT_TYPE visitSwaptionBermudaFixedIborDefinition(final SwaptionBermudaFixedIborDefinition swaption) {
    return visit(swaption);
  }

  @Override
  public RESULT_TYPE visitSwaptionCashFixedONCompoundingDefinition(final SwaptionCashFixedCompoundedONCompoundingDefinition swaption, final DATA_TYPE data) {
    return visit(swaption, data);
  }

  @Override
  public RESULT_TYPE visitSwaptionCashFixedONCompoundingDefinition(final SwaptionCashFixedCompoundedONCompoundingDefinition swaption) {
    return visit(swaption);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponFirstOfMonth(final CouponInflationZeroCouponMonthlyDefinition coupon, final DATA_TYPE data) {
    return visit(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponFirstOfMonth(final CouponInflationZeroCouponMonthlyDefinition coupon) {
    return visit(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolationDefinition coupon, final DATA_TYPE data) {
    return visit(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolationDefinition coupon) {
    return visit(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponMonthlyGearing(final CouponInflationZeroCouponMonthlyGearingDefinition coupon, final DATA_TYPE data) {
    return visit(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponMonthlyGearing(final CouponInflationZeroCouponMonthlyGearingDefinition coupon) {
    return visit(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponInterpolationGearing(final CouponInflationZeroCouponInterpolationGearingDefinition coupon, final DATA_TYPE data) {
    return visit(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponInterpolationGearing(final CouponInflationZeroCouponInterpolationGearingDefinition coupon) {
    return visit(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearFirstOfMonth(final CouponInflationYearOnYearMonthlyDefinition coupon, final DATA_TYPE data) {
    return visit(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearInterpolationDefinition(final CouponInflationYearOnYearInterpolationDefinition coupon) {
    return visit(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearInterpolationDefinition(final CouponInflationYearOnYearInterpolationDefinition coupon, final DATA_TYPE data) {
    return visit(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearFirstOfMonth(final CouponInflationYearOnYearMonthlyDefinition coupon) {
    return visit(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearMonthlyWithMargin(final CouponInflationYearOnYearMonthlyWithMarginDefinition coupon, final DATA_TYPE data) {
    return visit(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearInterpolationWithMargin(final CouponInflationYearOnYearInterpolationWithMarginDefinition coupon) {
    return visit(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearInterpolationWithMargin(final CouponInflationYearOnYearInterpolationWithMarginDefinition coupon, final DATA_TYPE data) {
    return visit(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearMonthlyWithMargin(final CouponInflationYearOnYearMonthlyWithMarginDefinition coupon) {
    return visit(coupon);
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationZeroCouponInterpolationDefinition(final CapFloorInflationZeroCouponInterpolationDefinition coupon, final DATA_TYPE data) {
    return visit(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationZeroCouponInterpolationDefinition(final CapFloorInflationZeroCouponInterpolationDefinition coupon) {
    return visit(coupon);
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationZeroCouponMonthlyDefinition(final CapFloorInflationZeroCouponMonthlyDefinition coupon, final DATA_TYPE data) {
    return visit(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationZeroCouponMonthlyDefinition(final CapFloorInflationZeroCouponMonthlyDefinition coupon) {
    return visit(coupon);
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationYearOnYearInterpolationDefinition(final CapFloorInflationYearOnYearInterpolationDefinition coupon, final DATA_TYPE data) {
    return visit(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationYearOnYearInterpolationDefinition(final CapFloorInflationYearOnYearInterpolationDefinition coupon) {
    return visit(coupon);
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationYearOnYearMonthlyDefinition(final CapFloorInflationYearOnYearMonthlyDefinition coupon, final DATA_TYPE data) {
    return visit(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationYearOnYearMonthlyDefinition(final CapFloorInflationYearOnYearMonthlyDefinition coupon) {
    return visit(coupon);
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurityDefinition<?> bond, final DATA_TYPE data) {
    return visit(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurityDefinition<?> bond) {
    return visit(bond);
  }

  @Override
  public RESULT_TYPE visitBondInterestIndexedSecurity(final BondInterestIndexedSecurityDefinition<?, ?> bond, final DATA_TYPE data) {
    return visit(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondInterestIndexedSecurity(final BondInterestIndexedSecurityDefinition<?, ?> bond) {
    return visit(bond);
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransactionDefinition<?> bond, final DATA_TYPE data) {
    return visit(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransactionDefinition<?> bond) {
    return visit(bond);
  }

  @Override
  public RESULT_TYPE visitBondInterestIndexedTransaction(final BondInterestIndexedTransactionDefinition<?, ?> bond, final DATA_TYPE data) {
    return visit(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondInterestIndexedTransaction(final BondInterestIndexedTransactionDefinition<?, ?> bond) {
    return visit(bond);
  }

  @Override
  public RESULT_TYPE visitCDSDefinition(final ISDACDSDefinition cds, final DATA_TYPE data) {
    return visit(cds, data);
  }

  @Override
  public RESULT_TYPE visitCDSDefinition(final ISDACDSDefinition cds) {
    return visit(cds);
  }

  @Override
  public RESULT_TYPE visitForexDefinition(final ForexDefinition fx, final DATA_TYPE data) {
    return visit(fx, data);
  }

  @Override
  public RESULT_TYPE visitForexDefinition(final ForexDefinition fx) {
    return visit(fx);
  }

  @Override
  public RESULT_TYPE visitForexSwapDefinition(final ForexSwapDefinition fx, final DATA_TYPE data) {
    return visit(fx, data);
  }

  @Override
  public RESULT_TYPE visitForexSwapDefinition(final ForexSwapDefinition fx) {
    return visit(fx);
  }

  @Override
  public RESULT_TYPE visitForexOptionVanillaDefinition(final ForexOptionVanillaDefinition fx, final DATA_TYPE data) {
    return visit(fx, data);
  }

  @Override
  public RESULT_TYPE visitForexOptionVanillaDefinition(final ForexOptionVanillaDefinition fx) {
    return visit(fx);
  }

  @Override
  public RESULT_TYPE visitForexOptionSingleBarrierDefiniton(final ForexOptionSingleBarrierDefinition fx, final DATA_TYPE data) {
    return visit(fx, data);
  }

  @Override
  public RESULT_TYPE visitForexOptionSingleBarrierDefiniton(final ForexOptionSingleBarrierDefinition fx) {
    return visit(fx);
  }

  @Override
  public RESULT_TYPE visitForexNonDeliverableForwardDefinition(final ForexNonDeliverableForwardDefinition ndf, final DATA_TYPE data) {
    return visit(ndf, data);
  }

  @Override
  public RESULT_TYPE visitForexNonDeliverableForwardDefinition(final ForexNonDeliverableForwardDefinition ndf) {
    return visit(ndf);
  }

  @Override
  public RESULT_TYPE visitForexNonDeliverableOptionDefinition(final ForexNonDeliverableOptionDefinition ndo, final DATA_TYPE data) {
    return visit(ndo, data);
  }

  @Override
  public RESULT_TYPE visitForexNonDeliverableOptionDefinition(final ForexNonDeliverableOptionDefinition ndo) {
    return visit(ndo);
  }

  @Override
  public RESULT_TYPE visitForexOptionDigitalDefinition(final ForexOptionDigitalDefinition fx, final DATA_TYPE data) {
    return visit(fx, data);
  }

  @Override
  public RESULT_TYPE visitForexOptionDigitalDefinition(final ForexOptionDigitalDefinition fx) {
    return visit(fx);
  }

  @Override
  public RESULT_TYPE visitMetalForwardDefinition(final MetalForwardDefinition forward, final DATA_TYPE data) {
    return visit(forward, data);
  }

  @Override
  public RESULT_TYPE visitMetalForwardDefinition(final MetalForwardDefinition forward) {
    return visit(forward);
  }

  @Override
  public RESULT_TYPE visitMetalFutureDefinition(final MetalFutureDefinition future, final DATA_TYPE data) {
    return visit(future, data);
  }

  @Override
  public RESULT_TYPE visitMetalFutureDefinition(final MetalFutureDefinition future) {
    return visit(future);
  }

  @Override
  public RESULT_TYPE visitMetalFutureOptionDefinition(final MetalFutureOptionDefinition option, final DATA_TYPE data) {
    return visit(option, data);
  }

  @Override
  public RESULT_TYPE visitMetalFutureOptionDefinition(final MetalFutureOptionDefinition option) {
    return visit(option);
  }

  @Override
  public RESULT_TYPE visitAgricultureForwardDefinition(final AgricultureForwardDefinition forward, final DATA_TYPE data) {
    return visit(forward, data);
  }

  @Override
  public RESULT_TYPE visitAgricultureForwardDefinition(final AgricultureForwardDefinition forward) {
    return visit(forward);
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureDefinition(final AgricultureFutureDefinition future, final DATA_TYPE data) {
    return visit(future, data);
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureDefinition(final AgricultureFutureDefinition future) {
    return visit(future);
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureOptionDefinition(final AgricultureFutureOptionDefinition option, final DATA_TYPE data) {
    return visit(option, data);
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureOptionDefinition(final AgricultureFutureOptionDefinition option) {
    return visit(option);
  }

  @Override
  public RESULT_TYPE visitEnergyForwardDefinition(final EnergyForwardDefinition forward, final DATA_TYPE data) {
    return visit(forward, data);
  }

  @Override
  public RESULT_TYPE visitEnergyForwardDefinition(final EnergyForwardDefinition forward) {
    return visit(forward);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureDefinition(final EnergyFutureDefinition future, final DATA_TYPE data) {
    return visit(future, data);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureDefinition(final EnergyFutureDefinition future) {
    return visit(future);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureOptionDefinition(final EnergyFutureOptionDefinition option, final DATA_TYPE data) {
    return visit(option, data);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureOptionDefinition(final EnergyFutureOptionDefinition option) {
    return visit(option);
  }

  @Override
  public RESULT_TYPE visitEquityFutureDefinition(final EquityFutureDefinition future, final DATA_TYPE data) {
    return visit(future, data);
  }

  @Override
  public RESULT_TYPE visitEquityFutureDefinition(final EquityFutureDefinition future) {
    return visit(future);
  }

  @Override
  public RESULT_TYPE visitIndexFutureDefinition(final IndexFutureDefinition future, final DATA_TYPE data) {
    return visit(future, data);
  }

  @Override
  public RESULT_TYPE visitIndexFutureDefinition(final IndexFutureDefinition future) {
    return visit(future);
  }

  @Override
  public RESULT_TYPE visitEquityIndexDividendFutureDefinition(final EquityIndexDividendFutureDefinition future, final DATA_TYPE data) {
    return visit(future, data);
  }

  @Override
  public RESULT_TYPE visitEquityIndexDividendFutureDefinition(final EquityIndexDividendFutureDefinition future) {
    return visit(future);
  }

  @Override
  public RESULT_TYPE visitEquityIndexOptionDefinition(final EquityIndexOptionDefinition option, final DATA_TYPE data) {
    return visit(option, data);
  }

  @Override
  public RESULT_TYPE visitEquityIndexOptionDefinition(final EquityIndexOptionDefinition option) {
    return visit(option);
  }

  @Override
  public RESULT_TYPE visitEquityIndexFutureOptionDefinition(final EquityIndexFutureOptionDefinition option, final DATA_TYPE data) {
    return visit(option, data);
  }

  @Override
  public RESULT_TYPE visitEquityIndexFutureOptionDefinition(final EquityIndexFutureOptionDefinition option) {
    return visit(option);
  }

  @Override
  public RESULT_TYPE visitEquityOptionDefinition(final EquityOptionDefinition option, final DATA_TYPE data) {
    return visit(option, data);
  }

  @Override
  public RESULT_TYPE visitEquityOptionDefinition(final EquityOptionDefinition option) {
    return visit(option);
  }

  @Override
  public RESULT_TYPE visitVarianceSwapDefinition(final VarianceSwapDefinition varianceSwap) {
    return visit(varianceSwap);
  }

  @Override
  public RESULT_TYPE visitVarianceSwapDefinition(final VarianceSwapDefinition varianceSwap, final DATA_TYPE data) {
    return visit(varianceSwap, data);
  }

  @Override
  public RESULT_TYPE visitEquityVarianceSwapDefinition(final EquityVarianceSwapDefinition varianceSwap) {
    return visit(varianceSwap);
  }

  @Override
  public RESULT_TYPE visitEquityVarianceSwapDefinition(final EquityVarianceSwapDefinition varianceSwap, final DATA_TYPE data) {
    return visit(varianceSwap, data);
  }

  @Override
  public RESULT_TYPE visitVolatilitySwapDefinition(final VolatilitySwapDefinition volatilitySwap) {
    return visit(volatilitySwap);
  }

  @Override
  public RESULT_TYPE visitVolatilitySwapDefinition(final VolatilitySwapDefinition volatilitySwap, final DATA_TYPE data) {
    return visit(volatilitySwap, data);
  }

  @Override
  public RESULT_TYPE visitFXVolatilitySwapDefinition(final FXVolatilitySwapDefinition volatilitySwap) {
    return visit(volatilitySwap);
  }

  @Override
  public RESULT_TYPE visitFXVolatilitySwapDefinition(final FXVolatilitySwapDefinition volatilitySwap, final DATA_TYPE data) {
    return visit(volatilitySwap, data);
  }

  @Override
  public RESULT_TYPE visitTotalReturnSwapDefinition(final TotalReturnSwapDefinition totalReturnSwap) {
    return visit(totalReturnSwap);
  }

  @Override
  public RESULT_TYPE visitTotalReturnSwapDefinition(final TotalReturnSwapDefinition totalReturnSwap, final DATA_TYPE data) {
    return visit(totalReturnSwap, data);
  }

  @Override
  public RESULT_TYPE visitBondTotalReturnSwapDefinition(final BondTotalReturnSwapDefinition totalReturnSwap) {
    return visit(totalReturnSwap);
  }

  @Override
  public RESULT_TYPE visitBondTotalReturnSwapDefinition(final BondTotalReturnSwapDefinition totalReturnSwap, final DATA_TYPE data) {
    return visit(totalReturnSwap, data);
  }

  @Override
  public RESULT_TYPE visitBillTotalReturnSwapDefinition(final BillTotalReturnSwapDefinition totalReturnSwap) {
    return visit(totalReturnSwap);
  }

  @Override
  public RESULT_TYPE visitBillTotalReturnSwapDefinition(final BillTotalReturnSwapDefinition totalReturnSwap, final DATA_TYPE data) {
    return visit(totalReturnSwap, data);
  }

  @Override
  public RESULT_TYPE visitEquityTotalReturnSwapDefinition(final EquityTotalReturnSwapDefinition totalReturnSwap) {
    return visit(totalReturnSwap);
  }

  @Override
  public RESULT_TYPE visitEquityTotalReturnSwapDefinition(final EquityTotalReturnSwapDefinition totalReturnSwap, final DATA_TYPE data) {
    return visit(totalReturnSwap, data);
  }

  @Override
  public RESULT_TYPE visitEquityDefinition(final EquityDefinition equity) {
    return visit(equity);
  }

  @Override
  public RESULT_TYPE visitEquityDefinition(final EquityDefinition equity, final DATA_TYPE data) {
    return visit(equity, data);
  }

  @Override
  public RESULT_TYPE visitEquityIndexFutureDefinition(final EquityIndexFutureDefinition definition, final DATA_TYPE data) {
    return visit(definition, data);
  }

  @Override
  public RESULT_TYPE visitEquityIndexFutureDefinition(final EquityIndexFutureDefinition definition) {
    return visit(definition);
  }

  @Override
  public RESULT_TYPE visitVolatilityIndexFutureDefinition(final VolatilityIndexFutureDefinition definition, final DATA_TYPE data) {
    return visit(definition, data);
  }

  @Override
  public RESULT_TYPE visitVolatilityIndexFutureDefinition(final VolatilityIndexFutureDefinition definition) {
    return visit(definition);
  }

  @Override
  public RESULT_TYPE visitMetalFutureSecurityDefinition(final MetalFutureSecurityDefinition future, final DATA_TYPE data) {
    return visit(future, data);
  }

  @Override
  public RESULT_TYPE visitMetalFutureSecurityDefinition(final MetalFutureSecurityDefinition future) {
    return visit(future);
  }

  @Override
  public RESULT_TYPE visitMetalFutureTransactionDefinition(final MetalFutureTransactionDefinition future, final DATA_TYPE data) {
    return visit(future, data);
  }

  @Override
  public RESULT_TYPE visitMetalFuturTransactioneDefinition(final MetalFutureTransactionDefinition future) {
    return visit(future);
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureSecurityDefinition(final AgricultureFutureSecurityDefinition future, final DATA_TYPE data) {
    return visit(future, data);
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureSecurityDefinition(final AgricultureFutureSecurityDefinition future) {
    return visit(future);
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureTransactionDefinition(final AgricultureFutureTransactionDefinition future, final DATA_TYPE data) {
    return visit(future, data);
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureTransactionDefinition(final AgricultureFutureTransactionDefinition future) {
    return visit(future);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureSecurityDefinition(final EnergyFutureSecurityDefinition future, final DATA_TYPE data) {
    return visit(future, data);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureSecurityDefinition(final EnergyFutureSecurityDefinition future) {
    return visit(future);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureTransactionDefinition(final EnergyFutureTransactionDefinition future, final DATA_TYPE data) {
    return visit(future, data);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureTransactionDefinition(final EnergyFutureTransactionDefinition future) {
    return visit(future);
  }

  @Override
  public RESULT_TYPE visitForwardCommodityCashSettleDefinition(final ForwardCommodityCashSettleDefinition forward, final DATA_TYPE data) {
    return visit(forward, data);
  }

  @Override
  public RESULT_TYPE visitForwardCommodityCashSettleDefinition(final ForwardCommodityCashSettleDefinition forward) {
    return visit(forward);
  }

  @Override
  public RESULT_TYPE visitForwardCommodityPhysicalSettleDefinition(final ForwardCommodityPhysicalSettleDefinition forward, final DATA_TYPE data) {
    return visit(forward, data);
  }

  @Override
  public RESULT_TYPE visitForwardCommodityPhysicalSettleDefinition(final ForwardCommodityPhysicalSettleDefinition forward) {
    return visit(forward);
  }

  @Override
  public RESULT_TYPE visitCouponCommodityCashSettleDefinition(final CouponCommodityCashSettleDefinition coupon, final DATA_TYPE data) {
    return visit(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponCommodityCashSettleDefinition(final CouponCommodityCashSettleDefinition coupon) {
    return visit(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponCommodityPhysicalSettleDefinition(final CouponCommodityPhysicalSettleDefinition coupon, final DATA_TYPE data) {
    return visit(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponCommodityPhysicalSettleDefinition(final CouponCommodityPhysicalSettleDefinition coupon) {
    return visit(coupon);
  }

}
