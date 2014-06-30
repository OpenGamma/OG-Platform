/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageFixingDatesCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageFixingDatesDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageFixingDatesCompoundingFlatSpreadDefinition;
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
 *
 * @param <DATA_TYPE> Type of the data
 * @param <RESULT_TYPE> Type of the result
 */
public interface InstrumentDefinitionVisitor<DATA_TYPE, RESULT_TYPE> {

  // -----     Bond and bill     -----

  /**
   * Fixed-coupon bond security method that takes data.
   * @param bond A fixed-coupon bond security
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitBondFixedSecurityDefinition(BondFixedSecurityDefinition bond, DATA_TYPE data);

  /**
   * Fixed-coupon bond security method.
   * @param bond A fixed-coupon bond security
   * @return The result
   */
  RESULT_TYPE visitBondFixedSecurityDefinition(BondFixedSecurityDefinition bond);

  /**
   * Fixed-coupon bond transaction method that takes data.
   * @param bond A fixed-coupon bond transaction
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitBondFixedTransactionDefinition(BondFixedTransactionDefinition bond, DATA_TYPE data);

  /**
   * Fixed-coupon bond transaction method.
   * @param bond A fixed-coupon bond transaction
   * @return The result
   */
  RESULT_TYPE visitBondFixedTransactionDefinition(BondFixedTransactionDefinition bond);

  /**
   * Ibor bond transaction method that takes data.
   * @param bond An ibor bond transaction
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitBondIborTransactionDefinition(BondIborTransactionDefinition bond, DATA_TYPE data);

  /**
   * Ibor bond transaction method.
   * @param bond An ibor bond transaction
   * @return The result
   */
  RESULT_TYPE visitBondIborTransactionDefinition(BondIborTransactionDefinition bond);

  /**
   * Ibor bond security method that takes data.
   * @param bond An ibor bond security
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitBondIborSecurityDefinition(BondIborSecurityDefinition bond, DATA_TYPE data);

  /**
   * Ibor bond security method.
   * @param bond An ibor bond security
   * @return The result
   */
  RESULT_TYPE visitBondIborSecurityDefinition(BondIborSecurityDefinition bond);

  /**
   * Bill security method that takes data.
   * @param bill A bill security
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitBillSecurityDefinition(BillSecurityDefinition bill, DATA_TYPE data);

  /**
   * Bill security method.
   * @param bill A bill security
   * @return The result
   */
  RESULT_TYPE visitBillSecurityDefinition(BillSecurityDefinition bill);

  /**
   * Bill transaction method that takes data.
   * @param bill A bill transaction
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitBillTransactionDefinition(BillTransactionDefinition bill, DATA_TYPE data);

  /**
   * Bill transaction method.
   * @param bill A bill transaction
   * @return The result
   */
  RESULT_TYPE visitBillTransactionDefinition(BillTransactionDefinition bill);

  // -----     Deposit     -----

  /**
   * Cash method that takes data.
   * @param cash The cash
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCashDefinition(CashDefinition cash, DATA_TYPE data);

  /**
   * Cash method.
   * @param cash The cash
   * @return The result
   */
  RESULT_TYPE visitCashDefinition(CashDefinition cash);

  /**
   * Ibor deposit method that takes data.
   * @param deposit The ibor deposit
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitDepositIborDefinition(DepositIborDefinition deposit, DATA_TYPE data);

  /**
   * Ibor deposit method.
   * @param deposit The ibor deposit
   * @return The result
   */
  RESULT_TYPE visitDepositIborDefinition(DepositIborDefinition deposit);

  /**
   * Counterparty deposit method that takes data.
   * @param deposit The counterparty deposit
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitDepositCounterpartDefinition(DepositCounterpartDefinition deposit, DATA_TYPE data);

  /**
   * Counterparty deposit method.
   * @param deposit The counterparty deposit
   * @return The result
   */
  RESULT_TYPE visitDepositCounterpartDefinition(DepositCounterpartDefinition deposit);

  /**
   * Zero deposit method that takes data.
   * @param deposit The zero deposit
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitDepositZeroDefinition(DepositZeroDefinition deposit, DATA_TYPE data);

  /**
   * Zero deposit method.
   * @param deposit The zero deposit
   * @return The result
   */
  RESULT_TYPE visitDepositZeroDefinition(DepositZeroDefinition deposit);

  // -----     Futures     -----

  /**
   * Bond future method that takes data.
   * @param bondFuture A bond future
   * @param data The data
   * @return The result
   * @deprecated {@link BondFutureDefinition} is deprecated
   */
  @Deprecated
  RESULT_TYPE visitBondFutureDefinition(BondFutureDefinition bondFuture, DATA_TYPE data);

  /**
   * Bond future method.
   * @param bondFuture A bond future
   * @return The result
   * @deprecated {@link BondFutureDefinition} is deprecated
   */
  @Deprecated
  RESULT_TYPE visitBondFutureDefinition(BondFutureDefinition bondFuture);

  /**
   * Bond future security method that takes data.
   * @param bondFuture A bond future security
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitBondFuturesSecurityDefinition(BondFuturesSecurityDefinition bondFuture, DATA_TYPE data);

  /**
   * Bond future security method.
   * @param bondFuture A bond future security
   * @return The result
   */
  RESULT_TYPE visitBondFuturesSecurityDefinition(BondFuturesSecurityDefinition bondFuture);

  /**
   * Bond future transaction method that takes data.
   * @param bondFuture A bond future transaction
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitBondFuturesTransactionDefinition(BondFuturesTransactionDefinition bondFuture, DATA_TYPE data);

  /**
   * Bond future transaction method.
   * @param bondFuture A bond future transaction
   * @return The result
   */
  RESULT_TYPE visitBondFuturesTransactionDefinition(BondFuturesTransactionDefinition bondFuture);

  /**
   * Yield average bond future security method that takes data.
   * @param bondFuture A bond future security
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitBondFuturesYieldAverageSecurityDefinition(BondFuturesYieldAverageSecurityDefinition bondFuture, DATA_TYPE data);

  /**
   * Yield average bond future security method.
   * @param bondFuture A bond future security
   * @return The result
   */
  RESULT_TYPE visitBondFuturesYieldAverageSecurityDefinition(BondFuturesYieldAverageSecurityDefinition bondFuture);

  /**
   * Yield average bond future transaction method that takes data.
   * @param bondFuture A bond future transaction
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitYieldAverageBondFuturesTransactionDefinition(BondFuturesYieldAverageTransactionDefinition bondFuture, DATA_TYPE data);

  /**
   * Yield average bond future transaction method.
   * @param bondFuture A bond future transaction
   * @return The result
   */
  RESULT_TYPE visitYieldAverageBondFuturesTransactionDefinition(BondFuturesYieldAverageTransactionDefinition bondFuture);

  /**
   * Forward rate agreement method that takes data.
   * @param fra The forward rate agreement
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitForwardRateAgreementDefinition(ForwardRateAgreementDefinition fra, DATA_TYPE data);

  /**
   * Forward rate agreement method.
   * @param fra The forward rate agreement
   * @return The result
   */
  RESULT_TYPE visitForwardRateAgreementDefinition(ForwardRateAgreementDefinition fra);

  /**
   * Interest rate future transaction method that takes data.
   * @param future An interest rate future transaction
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitInterestRateFutureTransactionDefinition(InterestRateFutureTransactionDefinition future, DATA_TYPE data);

  /**
   * Interest rate future transaction method.
   * @param future An interest rate future transaction
   * @return The result
   */
  RESULT_TYPE visitInterestRateFutureTransactionDefinition(InterestRateFutureTransactionDefinition future);

  /**
   * Interest rate future security method that takes data.
   * @param future An interest rate future security
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitInterestRateFutureSecurityDefinition(InterestRateFutureSecurityDefinition future, DATA_TYPE data);

  /**
   * Interest rate future security method.
   * @param future An interest rate future security
   * @return The result
   */
  RESULT_TYPE visitInterestRateFutureSecurityDefinition(InterestRateFutureSecurityDefinition future);

  /**
   * Federal funds future security method that takes data.
   * @param future A Federal funds future security
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitFederalFundsFutureSecurityDefinition(FederalFundsFutureSecurityDefinition future, DATA_TYPE data);

  /**
   * Federal funds future security method.
   * @param future A Federal funds future security
   * @return The result
   */
  RESULT_TYPE visitFederalFundsFutureSecurityDefinition(FederalFundsFutureSecurityDefinition future);

  /**
   * Federal funds future transaction method that takes data.
   * @param future A Federal funds future transaction
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitFederalFundsFutureTransactionDefinition(FederalFundsFutureTransactionDefinition future, DATA_TYPE data);

  /**
   * Federal funds future transaction method.
   * @param future A Federal funds future transaction
   * @return The result
   */
  RESULT_TYPE visitFederalFundsFutureTransactionDefinition(FederalFundsFutureTransactionDefinition future);

  /**
   * Deliverable swap future security method that takes data.
   * @param future A deliverable swap future security
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitDeliverableSwapFuturesSecurityDefinition(SwapFuturesPriceDeliverableSecurityDefinition future, DATA_TYPE data);

  /**
   * Deliverable swap future security method.
   * @param future A deliverable swap future security
   * @return The result
   */
  RESULT_TYPE visitDeliverableSwapFuturesSecurityDefinition(SwapFuturesPriceDeliverableSecurityDefinition future);

  /**
   * Deliverable swap future transaction method that takes data.
   * @param future A deliverable swap future transaction
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitDeliverableSwapFuturesTransactionDefinition(SwapFuturesPriceDeliverableTransactionDefinition future, DATA_TYPE data);

  /**
   * Deliverable swap future transaction method.
   * @param future A deliverable swap future transaction
   * @return The result
   */
  RESULT_TYPE visitDeliverableSwapFuturesTransactionDefinition(SwapFuturesPriceDeliverableTransactionDefinition future);

  // -----     Futures options    -----

  /**
   * Interest rate future option with premium security method that takes data.
   * @param futureOption An interest rate future option with premium security
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitInterestRateFutureOptionPremiumSecurityDefinition(InterestRateFutureOptionPremiumSecurityDefinition futureOption, DATA_TYPE data);

  /**
   * Interest rate future option with premium security method.
   * @param futureOption An interest rate future option with premium security
   * @return The result
   */
  RESULT_TYPE visitInterestRateFutureOptionPremiumSecurityDefinition(InterestRateFutureOptionPremiumSecurityDefinition futureOption);

  /**
   * Interest rate future option with premium transaction method that takes data.
   * @param futureOption An interest rate future option with premium transaction
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitInterestRateFutureOptionPremiumTransactionDefinition(InterestRateFutureOptionPremiumTransactionDefinition futureOption, DATA_TYPE data);

  /**
   * Interest rate future option with premium transaction method.
   * @param futureOption An interest rate future option with premium transaction
   * @return The result
   */
  RESULT_TYPE visitInterestRateFutureOptionPremiumTransactionDefinition(InterestRateFutureOptionPremiumTransactionDefinition futureOption);

  /**
   * Interest rate future option with margin security method that takes data.
   * @param futureOption An interest rate future option with margin security
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitInterestRateFutureOptionMarginSecurityDefinition(InterestRateFutureOptionMarginSecurityDefinition futureOption, DATA_TYPE data);

  /**
   * Interest rate future option with margin security method.
   * @param futureOption An interest rate future option with margin security
   * @return The result
   */
  RESULT_TYPE visitInterestRateFutureOptionMarginSecurityDefinition(InterestRateFutureOptionMarginSecurityDefinition futureOption);

  /**
   * Interest rate future option with margin transaction method that takes data.
   * @param futureOption An interest rate future option with margin transaction
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitInterestRateFutureOptionMarginTransactionDefinition(InterestRateFutureOptionMarginTransactionDefinition futureOption, DATA_TYPE data);

  /**
   * Interest rate future option with margin transaction method.
   * @param futureOption An interest rate future option with margin transaction
   * @return The result
   */
  RESULT_TYPE visitInterestRateFutureOptionMarginTransactionDefinition(InterestRateFutureOptionMarginTransactionDefinition futureOption);

  /**
   * Bond future option with premium security method that takes data.
   * @param bondFutureOption A bond future option with premium security
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitBondFutureOptionPremiumSecurityDefinition(BondFutureOptionPremiumSecurityDefinition bondFutureOption, DATA_TYPE data);

  /**
   * Bond future option with premium security method.
   * @param bondFutureOption Bond future future option with premium security
   * @return The result
   */
  RESULT_TYPE visitBondFutureOptionPremiumSecurityDefinition(BondFutureOptionPremiumSecurityDefinition bondFutureOption);

  /**
   * Bond future option with premium transaction method that takes data.
   * @param bondFutureOption A bond future option with premium transaction
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitBondFutureOptionPremiumTransactionDefinition(BondFutureOptionPremiumTransactionDefinition bondFutureOption, DATA_TYPE data);

  /**
   * Bond future option with premium transaction method.
   * @param bondFutureOption A bond future option with premium transaction
   * @return The result
   */
  RESULT_TYPE visitBondFutureOptionPremiumTransactionDefinition(BondFutureOptionPremiumTransactionDefinition bondFutureOption);

  /**
   * Bond future option with margin security method that takes data.
   * @param bondFutureOption A bond future option with margin security
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitBondFuturesOptionMarginSecurityDefinition(BondFuturesOptionMarginSecurityDefinition bondFutureOption, DATA_TYPE data);

  /**
   * Bond future option with margin security method.
   * @param bondFutureOption Bond future future option with margin security
   * @return The result
   */
  RESULT_TYPE visitBondFuturesOptionMarginSecurityDefinition(BondFuturesOptionMarginSecurityDefinition bondFutureOption);

  /**
   * Bond future option with margin transaction method that takes data.
   * @param bondFutureOption A bond future option with margin transaction
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitBondFuturesOptionMarginTransactionDefinition(BondFuturesOptionMarginTransactionDefinition bondFutureOption, DATA_TYPE data);

  /**
   * Bond future option with margin transaction method.
   * @param bondFutureOption A bond future option with margin transaction
   * @return The result
   */
  RESULT_TYPE visitBondFuturesOptionMarginTransactionDefinition(BondFuturesOptionMarginTransactionDefinition bondFutureOption);

  // -----     Payment and coupon     -----

  /**
   * Fixed payment method that takes data.
   * @param payment A fixed payment
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitPaymentFixedDefinition(PaymentFixedDefinition payment, DATA_TYPE data);

  /**
   * Fixed payment method.
   * @param payment A fixed payment
   * @return The result
   */
  RESULT_TYPE visitPaymentFixedDefinition(PaymentFixedDefinition payment);

  /**
   * Fixed coupon method that takes data.
   * @param payment A fixed coupon
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCouponFixedDefinition(CouponFixedDefinition payment, DATA_TYPE data);

  /**
   * Fixed coupon method.
   * @param payment A fixed coupon
   * @return The result
   */
  RESULT_TYPE visitCouponFixedDefinition(CouponFixedDefinition payment);

  /**
   * Fixed coupon with compounding method that takes data.
   * @param payment A fixed coupon with compounding
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCouponFixedCompoundingDefinition(CouponFixedCompoundingDefinition payment, DATA_TYPE data);

  /**
   * Fixed coupon with compounding method.
   * @param payment A fixed coupon with compounding
   * @return The result
   */
  RESULT_TYPE visitCouponFixedCompoundingDefinition(CouponFixedCompoundingDefinition payment);

  /**
   * Fixed coupon with accrued compounding method that takes data.
   * @param payment A fixed coupon with accrued compounding
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCouponFixedAccruedCompoundingDefinition(CouponFixedAccruedCompoundingDefinition payment, DATA_TYPE data);

  /**
   * Fixed coupon with accrued compounding method.
   * @param payment A fixed coupon with accrued compounding
   * @return The result
   */
  RESULT_TYPE visitCouponFixedAccruedCompoundingDefinition(CouponFixedAccruedCompoundingDefinition payment);

  /**
   * Ibor coupon method that takes data.
   * @param payment An ibor coupon
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCouponIborDefinition(CouponIborDefinition payment, DATA_TYPE data);

  /**
   * Ibor coupon method.
   * @param payment An ibor coupon
   * @return The result
   */
  RESULT_TYPE visitCouponIborDefinition(CouponIborDefinition payment);

  /**
   * Averaged ibor coupon method that takes data.
   * @param payment An averaged ibor coupon
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCouponIborAverageDefinition(CouponIborAverageIndexDefinition payment, DATA_TYPE data);

  /**
   * Averaged ibor coupon method.
   * @param payment An averaged ibor coupon
   * @return The result
   */
  RESULT_TYPE visitCouponIborAverageDefinition(CouponIborAverageIndexDefinition payment);

  /**
   * Ibor coupon with spread method that takes data.
   * @param payment An ibor coupon with spread
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCouponIborSpreadDefinition(CouponIborSpreadDefinition payment, DATA_TYPE data);

  /**
   * Ibor coupon with spread method that takes data.
   * @param payment An ibor coupon with spread
   * @return The result
   */
  RESULT_TYPE visitCouponIborSpreadDefinition(CouponIborSpreadDefinition payment);

  /**
   * Ibor coupon with gearing method that takes data.
   * @param payment An ibor coupon with gearing
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCouponIborGearingDefinition(CouponIborGearingDefinition payment, DATA_TYPE data);

  /**
   * Ibor coupon with gearing method.
   * @param payment An ibor coupon with gearing
   * @return The result
   */
  RESULT_TYPE visitCouponIborGearingDefinition(CouponIborGearingDefinition payment);

  /**
   * Ibor coupon with compounding method that takes data.
   * @param payment An ibor coupon with compounding
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCouponIborCompoundingDefinition(CouponIborCompoundingDefinition payment, DATA_TYPE data);

  /**
   * Ibor coupon with compounding method.
   * @param payment An ibor coupon with compounding
   * @return The result
   */
  RESULT_TYPE visitCouponIborCompoundingDefinition(CouponIborCompoundingDefinition payment);

  /**
   * Ibor coupon with compounding and spread method that takes data.
   * @param payment An ibor coupon with compounding and spread
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCouponIborCompoundingSpreadDefinition(CouponIborCompoundingSpreadDefinition payment, DATA_TYPE data);

  /**
   * Ibor coupon with compounding and spread method.
   * @param payment An ibor coupon with compounding and spread
   * @return The result
   */
  RESULT_TYPE visitCouponIborCompoundingSpreadDefinition(CouponIborCompoundingSpreadDefinition payment);

  /**
   * Ibor coupon with compounding of type "Compounding Flat" and spread method that takes data.
   * @param payment An ibor coupon with compounding and spread
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCouponIborCompoundingFlatSpreadDefinition(CouponIborCompoundingFlatSpreadDefinition payment, DATA_TYPE data);

  /**
   * Ibor coupon with compounding of type "Compounding treating spread as simple interest" and spread method.
   * @param payment An ibor coupon with compounding and spread
   * @return The result
   */
  RESULT_TYPE visitCouponIborCompoundingSimpleSpreadDefinition(CouponIborCompoundingSimpleSpreadDefinition payment);

  /**
   * Ibor coupon with compounding of type "Compounding treating spread as simple interest" and spread method that takes data.
   * @param payment An ibor coupon with compounding and spread
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCouponIborCompoundingSimpleSpreadDefinition(CouponIborCompoundingSimpleSpreadDefinition payment, DATA_TYPE data);

  /**
   * Ibor coupon with compounding of type "Compounding Flat" and spread method.
   * @param payment An ibor coupon with compounding and spread
   * @return The result
   */
  RESULT_TYPE visitCouponIborCompoundingFlatSpreadDefinition(CouponIborCompoundingFlatSpreadDefinition payment);

  /**
   * Ratcheted ibor coupon method that takes data.
   * @param payment A ratcheted ibor coupon
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCouponIborRatchetDefinition(CouponIborRatchetDefinition payment, DATA_TYPE data);

  /**
   * Ratcheted ibor coupon method.
   * @param payment A ratcheted ibor coupon
   * @return The result
   */
  RESULT_TYPE visitCouponIborRatchetDefinition(CouponIborRatchetDefinition payment);

  /**
   * Ibor cap/floor method that takes data.
   * @param payment An ibor cap/floor
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCapFloorIborDefinition(CapFloorIborDefinition payment, DATA_TYPE data);

  /**
   * Ibor cap/floor method.
   * @param payment An ibor cap/floor
   * @return The result
   */
  RESULT_TYPE visitCapFloorIborDefinition(CapFloorIborDefinition payment);

  /**
   * OIS coupon method that takes data.
   * @param payment An OIS coupon
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCouponOISDefinition(CouponONDefinition payment, DATA_TYPE data);

  /**
   * OIS coupon method.
   * @param payment An OIS coupon
   * @return The result
   */
  RESULT_TYPE visitCouponOISDefinition(CouponONDefinition payment);

  /**
   * Overnight compounded coupon method that takes data.
   * @param payment An overnight compounded coupon
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCouponONCompoundedDefinition(CouponONCompoundedDefinition payment, DATA_TYPE data);

  /**
   * Overnight compounded coupon method.
   * @param payment An overnight compounded coupon
   * @return The result
   */
  RESULT_TYPE visitCouponONCompoundedDefinition(CouponONCompoundedDefinition payment);

  /**
   * Simplified OIS coupon method that takes data.
   * @param payment A simplified OIS coupon
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCouponOISSimplifiedDefinition(CouponONSimplifiedDefinition payment, DATA_TYPE data);

  /**
   * Simplified OIS coupon method.
   * @param payment A simplified OIS coupon
   * @return The result
   */
  RESULT_TYPE visitCouponOISSimplifiedDefinition(CouponONSimplifiedDefinition payment);

  /**
   * Overnight coupon with spread method that takes data.
   * @param payment An overnight coupon with spread
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCouponONSpreadDefinition(CouponONSpreadDefinition payment, DATA_TYPE data);

  /**
   * Overnight coupon with spread method.
   * @param payment An overnight coupon with spread
   * @return The result
   */
  RESULT_TYPE visitCouponONSpreadDefinition(CouponONSpreadDefinition payment);

  /**
   * Overnight coupon with spread method that takes data.
   * @param payment An overnight coupon with spread
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCouponONSpreadSimplifiedDefinition(CouponONSpreadSimplifiedDefinition payment, DATA_TYPE data);

  /**
   * Overnight coupon with spread method.
   * @param payment An overnight coupon with spread
   * @return The result
   */
  RESULT_TYPE visitCouponONSpreadSimplifiedDefinition(CouponONSpreadSimplifiedDefinition payment);

  /**
   * CMS coupon method that takes data.
   * @param payment A CMS coupon
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCouponCMSDefinition(CouponCMSDefinition payment, DATA_TYPE data);

  /**
   * CMS coupon method.
   * @param payment A CMS coupon
   * @return The result
   */
  RESULT_TYPE visitCouponCMSDefinition(CouponCMSDefinition payment);

  /**
   * CMS cap/floor method that takes data.
   * @param payment A CMS cap/floor
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCapFloorCMSDefinition(CapFloorCMSDefinition payment, DATA_TYPE data);

  /**
   * CMS cap/floor method.
   * @param payment A CMS cap/floor
   * @return The result
   */
  RESULT_TYPE visitCapFloorCMSDefinition(CapFloorCMSDefinition payment);

  /**
   * CMS cap/floor spread method that takes data.
   * @param payment A CMS cap/floor spread
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCapFloorCMSSpreadDefinition(CapFloorCMSSpreadDefinition payment, DATA_TYPE data);

  /**
   * CMS cap/floor spread method.
   * @param payment A CMS cap/floor spread
   * @return The result
   */
  RESULT_TYPE visitCapFloorCMSSpreadDefinition(CapFloorCMSSpreadDefinition payment);

  /**
   * Arithmetic-averaged overnight coupon method that takes data.
   * @param payment An arithmetic-averaged overnight coupon
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCouponArithmeticAverageONDefinition(CouponONArithmeticAverageDefinition payment, DATA_TYPE data);

  /**
   * Arithmetic-averaged overnight coupon method.
   * @param payment An arithmetic-averaged overnight coupon
   * @return The result
   */
  RESULT_TYPE visitCouponArithmeticAverageONDefinition(CouponONArithmeticAverageDefinition payment);

  /**
   * Arithmetic-averaged overnight coupon with spread method that takes data.
   * @param payment An arithmetic-averaged overnight coupon with spread
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCouponArithmeticAverageONSpreadDefinition(CouponONArithmeticAverageSpreadDefinition payment, DATA_TYPE data);

  /**
   * Arithmetic-averaged overnight coupon with spread method.
   * @param payment An arithmetic-averaged overnight coupon with spread
   * @return The result
   */
  RESULT_TYPE visitCouponArithmeticAverageONSpreadDefinition(CouponONArithmeticAverageSpreadDefinition payment);

  /**
   * Simplified arithmetic-averaged overnight coupon method that takes data.
   * @param payment A simplified arithmetic-averaged overnight coupon
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCouponArithmeticAverageONSpreadSimplifiedDefinition(CouponONArithmeticAverageSpreadSimplifiedDefinition payment, DATA_TYPE data);

  /**
   * Simplified arithmetic-averaged overnight coupon method.
   * @param payment A simplified arithmetic-averaged overnight coupon
   * @return The result
   */
  RESULT_TYPE visitCouponArithmeticAverageONSpreadSimplifiedDefinition(CouponONArithmeticAverageSpreadSimplifiedDefinition payment);

  RESULT_TYPE visitCouponIborAverageFixingDatesDefinition(CouponIborAverageFixingDatesDefinition payment, DATA_TYPE data);

  RESULT_TYPE visitCouponIborAverageFixingDatesDefinition(CouponIborAverageFixingDatesDefinition payment);

  RESULT_TYPE visitCouponIborAverageCompoundingDefinition(CouponIborAverageFixingDatesCompoundingDefinition payment, DATA_TYPE data);

  RESULT_TYPE visitCouponIborAverageCompoundingDefinition(CouponIborAverageFixingDatesCompoundingDefinition payment);

  RESULT_TYPE visitCouponIborAverageFlatCompoundingSpreadDefinition(CouponIborAverageFixingDatesCompoundingFlatSpreadDefinition payment, DATA_TYPE data);

  RESULT_TYPE visitCouponIborAverageFlatCompoundingSpreadDefinition(CouponIborAverageFixingDatesCompoundingFlatSpreadDefinition payment);

  // -----     Annuity     -----

  /**
   * Annuity method that takes data.
   * @param annuity An annuity
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitAnnuityDefinition(AnnuityDefinition<? extends PaymentDefinition> annuity, DATA_TYPE data);

  /**
   * Annuity method.
   * @param annuity An annuity
   * @return The result
   */
  RESULT_TYPE visitAnnuityDefinition(AnnuityDefinition<? extends PaymentDefinition> annuity);

  // -----     Swap     -----

  /**
   * Swap with arbitrary pay and receive legs method that takes data.
   * @param swap A swap
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitSwapDefinition(SwapDefinition swap, DATA_TYPE data);

  /**
   * Swap with arbitrary pay and receive legs method.
   * @param swap A swap
   * @return The result
   */
  RESULT_TYPE visitSwapDefinition(SwapDefinition swap);

  /**
   * Swap with arbitrary multiple legs method that takes data.
   * @param swap A swap
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitSwapMultilegDefinition(SwapMultilegDefinition swap, DATA_TYPE data);

  /**
   * Swap with arbitrary multiple legs method.
   * @param swap A swap
   * @return The result
   */
  RESULT_TYPE visitSwapMultilegDefinition(SwapMultilegDefinition swap);

  /**
   * Fixed / ibor swap method that takes data.
   * @param swap A swap
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitSwapFixedIborDefinition(SwapFixedIborDefinition swap, DATA_TYPE data);

  /**
   * Fixed / ibor swap method.
   * @param swap A swap
   * @return The result
   */
  RESULT_TYPE visitSwapFixedIborDefinition(SwapFixedIborDefinition swap);

  /**
   * Fixed / ibor swap with spread method that takes data.
   * @param swap A fixed / ibor swap
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitSwapFixedIborSpreadDefinition(SwapFixedIborSpreadDefinition swap, DATA_TYPE data);

  /**
   * Fixed / ibor swap with spread method.
   * @param swap A fixed / ibor swap
   * @return The result
   */
  RESULT_TYPE visitSwapFixedIborSpreadDefinition(SwapFixedIborSpreadDefinition swap);

  /**
   * Ibor / ibor swap method that takes data.
   * @param swap An ibor / ibor swap
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitSwapIborIborDefinition(SwapIborIborDefinition swap, DATA_TYPE data);

  /**
   * Ibor / ibor swap method.
   * @param swap An ibor / ibor swap
   * @return The result
   */
  RESULT_TYPE visitSwapIborIborDefinition(SwapIborIborDefinition swap);

  /**
   * Cross-currency ibor / ibor swap method that takes data.
   * @param swap A cross-currency ibor / ibor swap
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitSwapXCcyIborIborDefinition(SwapXCcyIborIborDefinition swap, DATA_TYPE data);

  /**
   * Cross-currency ibor / ibor swap method.
   * @param swap A cross-currency ibor / ibor swap
   * @return The result
   */
  RESULT_TYPE visitSwapXCcyIborIborDefinition(SwapXCcyIborIborDefinition swap);

  // -----     Swaption     -----

  /**
   * Cash-settled fixed / ibor swaption method that takes data.
   * @param swaption A cash-settled fixed / ibor swaption
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitSwaptionCashFixedIborDefinition(SwaptionCashFixedIborDefinition swaption, DATA_TYPE data);

  /**
   * Cash-settled fixed / ibor swaption method.
   * @param swaption A cash-settled fixed / ibor swaption
   * @return The result
   */
  RESULT_TYPE visitSwaptionCashFixedIborDefinition(SwaptionCashFixedIborDefinition swaption);

  /**
   * Physically-settled fixed / ibor swaption method that takes data.
   * @param swaption A physically-settled fixed / ibor swaption
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitSwaptionPhysicalFixedIborDefinition(SwaptionPhysicalFixedIborDefinition swaption, DATA_TYPE data);

  /**
   * Physically-settled fixed / ibor swaption method.
   * @param swaption A physically-settled fixed / ibor swaption
   * @return The result
   */
  RESULT_TYPE visitSwaptionPhysicalFixedIborDefinition(SwaptionPhysicalFixedIborDefinition swaption);

  /**
   * Physically-settled fixed / ibor swaption with spread method that takes data.
   * @param swaption A physically-settled fixed / ibor swaption
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitSwaptionPhysicalFixedIborSpreadDefinition(SwaptionPhysicalFixedIborSpreadDefinition swaption, DATA_TYPE data);

  /**
   * Physically-settled fixed / ibor swaption with spread method.
   * @param swaption A physically-settled fixed / ibor swaption
   * @return The result
   */
  RESULT_TYPE visitSwaptionPhysicalFixedIborSpreadDefinition(SwaptionPhysicalFixedIborSpreadDefinition swaption);

  /**
   * Bermudan fixed / ibor swaption method that takes data.
   * @param swaption A Bermudan fixed / ibor swaption
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitSwaptionBermudaFixedIborDefinition(SwaptionBermudaFixedIborDefinition swaption, DATA_TYPE data);

  /**
   * Bermudan fixed / ibor swaption method.
   * @param swaption A Bermudan fixed / ibor swaption
   * @return The result
   */
  RESULT_TYPE visitSwaptionBermudaFixedIborDefinition(SwaptionBermudaFixedIborDefinition swaption);

  /**
   * Cash-settled fixed accrued / overnight compounding swaption (i.e. BRL-like) method that takes data.
   * @param swaption A cash-settled fixed accrued / overnight compounding swaption
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitSwaptionCashFixedONCompoundingDefinition(SwaptionCashFixedCompoundedONCompoundingDefinition swaption, DATA_TYPE data);

  /**
   * Cash-settled fixed accrued / overnight compounding swaption (i.e. BRL-like) method.
   * @param swaption A cash-settled fixed accrued / overnight compounding swaption
   * @return The result
   */
  RESULT_TYPE visitSwaptionCashFixedONCompoundingDefinition(SwaptionCashFixedCompoundedONCompoundingDefinition swaption);

  // -----     Inflation     -----

  /**
   * First-of-month inflation zero coupon method that takes data.
   * @param coupon A first-of-month inflation zero coupon
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCouponInflationZeroCouponFirstOfMonth(CouponInflationZeroCouponMonthlyDefinition coupon, DATA_TYPE data);

  /**
   * First-of-month inflation zero coupon method.
   * @param coupon A first-of-month inflation zero coupon
   * @return The result
   */
  RESULT_TYPE visitCouponInflationZeroCouponFirstOfMonth(CouponInflationZeroCouponMonthlyDefinition coupon);

  /**
   * Interpolated inflation zero coupon method that takes data.
   * @param coupon An interpolated inflation zero coupon
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCouponInflationZeroCouponInterpolation(CouponInflationZeroCouponInterpolationDefinition coupon, DATA_TYPE data);

  /**
   * Interpolated inflation zero coupon method.
   * @param coupon An interpolated inflation zero coupon
   * @return The result
   */
  RESULT_TYPE visitCouponInflationZeroCouponInterpolation(CouponInflationZeroCouponInterpolationDefinition coupon);

  /**
   * Monthly inflation zero coupon with gearing method that takes data.
   * @param coupon A monthly inflation zero coupon with gearing
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCouponInflationZeroCouponMonthlyGearing(CouponInflationZeroCouponMonthlyGearingDefinition coupon, DATA_TYPE data);

  /**
   * Monthly inflation zero coupon with gearing method.
   * @param coupon A monthly inflation zero coupon with gearing
   * @return The result
   */
  RESULT_TYPE visitCouponInflationZeroCouponMonthlyGearing(CouponInflationZeroCouponMonthlyGearingDefinition coupon);

  /**
   * Interpolated inflation zero coupon with gearing method that takes data.
   * @param coupon An interpolated inflation zero coupon with gearing
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCouponInflationZeroCouponInterpolationGearing(CouponInflationZeroCouponInterpolationGearingDefinition coupon, DATA_TYPE data);

  /**
   * Interpolated inflation zero coupon with gearing method.
   * @param coupon An interpolated inflation zero coupon with gearing
   * @return The result
   */
  RESULT_TYPE visitCouponInflationZeroCouponInterpolationGearing(CouponInflationZeroCouponInterpolationGearingDefinition coupon);

  /**
   * Monthly year-on-year inflation zero coupon with margin method that takes data.
   * @param coupon A monthly year-on-year inflation zero coupon with margin
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCouponInflationYearOnYearMonthlyWithMargin(CouponInflationYearOnYearMonthlyWithMarginDefinition coupon, DATA_TYPE data);

  /**
   * Monthly year-on-year inflation zero coupon with margin method.
   * @param coupon A monthly year-on-year inflation zero coupon with margin
   * @return The result
   */
  RESULT_TYPE visitCouponInflationYearOnYearMonthlyWithMargin(CouponInflationYearOnYearMonthlyWithMarginDefinition coupon);

  /**
   * Interpolated year-on-year inflation zero coupon with margin method that takes data.
   * @param coupon An interpolated year-on-year inflation zero coupon with margin
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCouponInflationYearOnYearInterpolationWithMargin(CouponInflationYearOnYearInterpolationWithMarginDefinition coupon, DATA_TYPE data);

  /**
   * Interpolated year-on-year inflation zero coupon with margin method.
   * @param coupon An interpolated year-on-year inflation zero coupon with margin
   * @return The result
   */
  RESULT_TYPE visitCouponInflationYearOnYearInterpolationWithMargin(CouponInflationYearOnYearInterpolationWithMarginDefinition coupon);

  /**
   * First-of-month year-on-year inflation zero coupon method that takes data.
   * @param coupon A first-of-month year-on-year inflation zero coupon with margin
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCouponInflationYearOnYearFirstOfMonth(CouponInflationYearOnYearMonthlyDefinition coupon, DATA_TYPE data);

  /**
   * First-of-month year-on-year inflation zero coupon method.
   * @param coupon A first-of-month year-on-year inflation zero coupon with margin
   * @return The result
   */
  RESULT_TYPE visitCouponInflationYearOnYearFirstOfMonth(CouponInflationYearOnYearMonthlyDefinition coupon);

  /**
   * Interpolated year-on-year inflation zero coupon method that takes data.
   * @param coupon An interpolated year-on-year inflation zero coupon with margin
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCouponInflationYearOnYearInterpolationDefinition(CouponInflationYearOnYearInterpolationDefinition coupon, DATA_TYPE data);

  /**
   * Interpolated year-on-year inflation zero coupon method.
   * @param coupon An interpolated year-on-year inflation zero coupon with margin
   * @return The result
   */
  RESULT_TYPE visitCouponInflationYearOnYearInterpolationDefinition(CouponInflationYearOnYearInterpolationDefinition coupon);

  /**
   * Inflation cap / floor zero coupon method that takes data.
   * @param coupon An inflation cap / floor zero coupon
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCapFloorInflationZeroCouponInterpolationDefinition(CapFloorInflationZeroCouponInterpolationDefinition coupon, DATA_TYPE data);

  /**
   * Inflation cap / floor zero coupon method.
   * @param coupon An inflation cap / floor zero coupon
   * @return The result
   */
  RESULT_TYPE visitCapFloorInflationZeroCouponInterpolationDefinition(CapFloorInflationZeroCouponInterpolationDefinition coupon);

  /**
   * Monthly cap / floor inflation zero coupon method that takes data.
   * @param coupon A monthly inflation cap / floor zero coupon
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCapFloorInflationZeroCouponMonthlyDefinition(CapFloorInflationZeroCouponMonthlyDefinition coupon, DATA_TYPE data);

  /**
   * Monthly cap / floor inflation zero coupon method.
   * @param coupon A monthly inflation cap / floor zero coupon
   * @return The result
   */
  RESULT_TYPE visitCapFloorInflationZeroCouponMonthlyDefinition(CapFloorInflationZeroCouponMonthlyDefinition coupon);

  /**
   * Interpolated inflation cap / floor zero coupon method that takes data.
   * @param coupon An interpolated inflation cap / floor zero coupon
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCapFloorInflationYearOnYearInterpolationDefinition(CapFloorInflationYearOnYearInterpolationDefinition coupon, DATA_TYPE data);

  /**
   * Interpolated inflation cap / floor zero coupon method.
   * @param coupon An interpolated inflation cap / floor zero coupon
   * @return The result
   */
  RESULT_TYPE visitCapFloorInflationYearOnYearInterpolationDefinition(CapFloorInflationYearOnYearInterpolationDefinition coupon);

  /**
   * Monthly inflation cap / floor zero coupon method that takes data.
   * @param coupon A monthly inflation cap / floor zero coupon
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCapFloorInflationYearOnYearMonthlyDefinition(CapFloorInflationYearOnYearMonthlyDefinition coupon, DATA_TYPE data);

  /**
   * Monthly inflation cap / floor zero coupon method.
   * @param coupon A monthly inflation cap / floor zero coupon
   * @return The result
   */
  RESULT_TYPE visitCapFloorInflationYearOnYearMonthlyDefinition(CapFloorInflationYearOnYearMonthlyDefinition coupon);

  /**
   * Capital-indexed bond security method that takes data.
   * @param bond A capital-indexed bond security
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitBondCapitalIndexedSecurity(BondCapitalIndexedSecurityDefinition<?> bond, DATA_TYPE data);

  /**
   * Capital-indexed bond security method.
   * @param bond A capital-indexed bond security
   * @return The result
   */
  RESULT_TYPE visitBondCapitalIndexedSecurity(BondCapitalIndexedSecurityDefinition<?> bond);

  /**
   * Interest-indexed bond security method that takes data.
   * @param bond An interest-indexed bond security
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitBondInterestIndexedSecurity(BondInterestIndexedSecurityDefinition<?, ?> bond, DATA_TYPE data);

  /**
   * Interest-indexed bond security method.
   * @param bond An interest-indexed bond security
   * @return The result
   */
  RESULT_TYPE visitBondInterestIndexedSecurity(BondInterestIndexedSecurityDefinition<?, ?> bond);

  /**
   * Capital-indexed bond transaction method that takes data.
   * @param bond A capital-indexed bond transaction
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitBondCapitalIndexedTransaction(BondCapitalIndexedTransactionDefinition<?> bond, DATA_TYPE data);

  /**
   * Capital-indexed bond transaction method.
   * @param bond A capital-indexed bond transaction
   * @return The result
   */
  RESULT_TYPE visitBondCapitalIndexedTransaction(BondCapitalIndexedTransactionDefinition<?> bond);

  /**
   * Interest-indexed bond transaction method that takes data.
   * @param bond An interest-indexed bond transaction
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitBondInterestIndexedTransaction(BondInterestIndexedTransactionDefinition<?, ?> bond, DATA_TYPE data);

  /**
   * Interest-indexed bond transaction method.
   * @param bond An interest-indexed bond transaction
   * @return The result
   */
  RESULT_TYPE visitBondInterestIndexedTransaction(BondInterestIndexedTransactionDefinition<?, ?> bond);

  /**
   * ISDA CDS method that takes data.
   * @param cds An ISDA CDS
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCDSDefinition(ISDACDSDefinition cds, DATA_TYPE data);

  /**
   * ISDA CDS method.
   * @param cds An ISDA CDS
   * @return The result
   */
  RESULT_TYPE visitCDSDefinition(ISDACDSDefinition cds);

  // -----     Forex     -----

  /**
   * Forex method that takes data.
   * @param fx A forex
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitForexDefinition(ForexDefinition fx, DATA_TYPE data);

  /**
   * Forex method.
   * @param fx A forex
   * @return The result
   */
  RESULT_TYPE visitForexDefinition(ForexDefinition fx);

  /**
   * Forex swap method that takes data.
   * @param fx A forex swap
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitForexSwapDefinition(ForexSwapDefinition fx, DATA_TYPE data);

  /**
   * Forex swap method.
   * @param fx A forex swap
   * @return The result
   */
  RESULT_TYPE visitForexSwapDefinition(ForexSwapDefinition fx);

  /**
   * Vanilla FX option method that takes data.
   * @param fx A vanilla FX option
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitForexOptionVanillaDefinition(ForexOptionVanillaDefinition fx, DATA_TYPE data);

  /**
   * Vanilla FX option method.
   * @param fx A vanilla FX option
   * @return The result
   */
  RESULT_TYPE visitForexOptionVanillaDefinition(ForexOptionVanillaDefinition fx);

  /**
   * Single-barrier option method that takes data.
   * @param fx A single-barrier FX option
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitForexOptionSingleBarrierDefiniton(ForexOptionSingleBarrierDefinition fx, DATA_TYPE data);

  /**
   * Single-barrier option method.
   * @param fx A single-barrier FX option
   * @return The result
   */
  RESULT_TYPE visitForexOptionSingleBarrierDefiniton(ForexOptionSingleBarrierDefinition fx);

  /**
   * Non-deliverable FX forward method that takes data.
   * @param ndf A non-deliverable FX forward
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitForexNonDeliverableForwardDefinition(ForexNonDeliverableForwardDefinition ndf, DATA_TYPE data);

  /**
   * Non-deliverable FX forward method.
   * @param ndf A non-deliverable FX forward
   * @return The result
   */
  RESULT_TYPE visitForexNonDeliverableForwardDefinition(ForexNonDeliverableForwardDefinition ndf);

  /**
   * Non-deliverable FX forward option method that takes data.
   * @param ndo A non-deliverable FX forward option
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitForexNonDeliverableOptionDefinition(ForexNonDeliverableOptionDefinition ndo, DATA_TYPE data);

  /**
   * Non-deliverable FX forward option method.
   * @param ndo A non-deliverable FX forward option
   * @return The result
   */
  RESULT_TYPE visitForexNonDeliverableOptionDefinition(ForexNonDeliverableOptionDefinition ndo);

  /**
   * FX digital option method that takes data.
   * @param fx A FX digital option
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitForexOptionDigitalDefinition(ForexOptionDigitalDefinition fx, DATA_TYPE data);

  /**
   * FX digital option method.
   * @param fx A FX digital option
   * @return The result
   */
  RESULT_TYPE visitForexOptionDigitalDefinition(ForexOptionDigitalDefinition fx);

  // -----     Commodity    -----

  /**
   * Metal forward method that takes data.
   * @param forward A metal forward
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitMetalForwardDefinition(MetalForwardDefinition forward, DATA_TYPE data);

  /**
   * Metal forward method.
   * @param forward A metal forward
   * @return The result
   */
  RESULT_TYPE visitMetalForwardDefinition(MetalForwardDefinition forward);

  /**
   * Metal future method that takes data.
   * @param future A metal future
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitMetalFutureDefinition(MetalFutureDefinition future, DATA_TYPE data);

  /**
   * Metal future method.
   * @param future A metal future
   * @return The result
   */
  RESULT_TYPE visitMetalFutureDefinition(MetalFutureDefinition future);

  /**
   * Metal future option method that takes data.
   * @param option A metal future option
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitMetalFutureOptionDefinition(MetalFutureOptionDefinition option, DATA_TYPE data);

  /**
   * Metal future option method.
   * @param option A metal future option
   * @return The result
   */
  RESULT_TYPE visitMetalFutureOptionDefinition(MetalFutureOptionDefinition option);

  /**
   * Agriculture forward method that takes data.
   * @param forward An agriculture forward
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitAgricultureForwardDefinition(AgricultureForwardDefinition forward, DATA_TYPE data);

  /**
   * Agriculture forward method.
   * @param forward An agriculture forward
   * @return The result
   */
  RESULT_TYPE visitAgricultureForwardDefinition(AgricultureForwardDefinition forward);

  /**
   * Agriculture future method that takes data.
   * @param future An agriculture future
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitAgricultureFutureDefinition(AgricultureFutureDefinition future, DATA_TYPE data);

  /**
   * Agriculture future method.
   * @param future An agriculture future
   * @return The result
   */
  RESULT_TYPE visitAgricultureFutureDefinition(AgricultureFutureDefinition future);

  /**
   * Agriculture future option method that takes data.
   * @param option An agriculture future option
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitAgricultureFutureOptionDefinition(AgricultureFutureOptionDefinition option, DATA_TYPE data);

  /**
   * Agriculture future option method.
   * @param option An agriculture future option
   * @return The result
   */
  RESULT_TYPE visitAgricultureFutureOptionDefinition(AgricultureFutureOptionDefinition option);

  /**
   * Equity forward method that takes data.
   * @param forward An equity forward
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitEnergyForwardDefinition(EnergyForwardDefinition forward, DATA_TYPE data);

  /**
   * Equity forward method.
   * @param forward An equity forward
   * @return The result
   */
  RESULT_TYPE visitEnergyForwardDefinition(EnergyForwardDefinition forward);

  /**
   * Energy future method that takes data.
   * @param future An energy future
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitEnergyFutureDefinition(EnergyFutureDefinition future, DATA_TYPE data);

  /**
   * Energy future method.
   * @param future An energy future
   * @return The result
   */
  RESULT_TYPE visitEnergyFutureDefinition(EnergyFutureDefinition future);

  /**
   * Energy future option method that takes data.
   * @param option An energy future option
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitEnergyFutureOptionDefinition(EnergyFutureOptionDefinition option, DATA_TYPE data);

  /**
   * Energy future option method.
   * @param option An energy future option
   * @return The result
   */
  RESULT_TYPE visitEnergyFutureOptionDefinition(EnergyFutureOptionDefinition option);

  /**
   * Metal future Security method that takes data.
   * @param future A metal future Security
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitMetalFutureSecurityDefinition(MetalFutureSecurityDefinition future, DATA_TYPE data);

  /**
   * Metal future Security method.
   * @param future A metal future Security
   * @return The result
   */
  RESULT_TYPE visitMetalFutureSecurityDefinition(MetalFutureSecurityDefinition future);

  /**
   * Metal future Transaction method that takes data.
   * @param future A metal future Transaction
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitMetalFutureTransactionDefinition(MetalFutureTransactionDefinition future, DATA_TYPE data);

  /**
   * Metal future Transaction method.
   * @param future A metal future Transaction
   * @return The result
   */
  RESULT_TYPE visitMetalFuturTransactioneDefinition(MetalFutureTransactionDefinition future);

  /**
   * Agriculture future Security method that takes data.
   * @param future A metal future Security
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitAgricultureFutureSecurityDefinition(AgricultureFutureSecurityDefinition future, DATA_TYPE data);

  /**
   * Agriculture future Security method.
   * @param future A Agriculture future Security
   * @return The result
   */
  RESULT_TYPE visitAgricultureFutureSecurityDefinition(AgricultureFutureSecurityDefinition future);

  /**
   * Agriculture future Transaction method that takes data.
   * @param future A Agriculture future Transaction
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitAgricultureFutureTransactionDefinition(AgricultureFutureTransactionDefinition future, DATA_TYPE data);

  /**
   * Agriculture future Transaction method.
   * @param future A Agriculture future Transaction
   * @return The result
   */
  RESULT_TYPE visitAgricultureFutureTransactionDefinition(AgricultureFutureTransactionDefinition future);

  /**
   * Energy future Security method that takes data.
   * @param future A Energy future Security
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitEnergyFutureSecurityDefinition(EnergyFutureSecurityDefinition future, DATA_TYPE data);

  /**
   * Energy future Security method.
   * @param future A Energy future Security
   * @return The result
   */
  RESULT_TYPE visitEnergyFutureSecurityDefinition(EnergyFutureSecurityDefinition future);

  /**
   * Energy future Transaction method that takes data.
   * @param future A Energy future Transaction
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitEnergyFutureTransactionDefinition(EnergyFutureTransactionDefinition future, DATA_TYPE data);

  /**
   * Energy future Transaction method.
   * @param future A Energy future Transaction
   * @return The result
   */
  RESULT_TYPE visitEnergyFutureTransactionDefinition(EnergyFutureTransactionDefinition future);

  /**
   * Forward commodity cash settle method that takes data.
   * @param forward A forward
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitForwardCommodityCashSettleDefinition(ForwardCommodityCashSettleDefinition forward, DATA_TYPE data);

  /**
   * Forward commodity cash settle  method.
   * @param forward A forward
   * @return The result
   */
  RESULT_TYPE visitForwardCommodityCashSettleDefinition(ForwardCommodityCashSettleDefinition forward);

  /**
   * Forward commodity Physical settle method that takes data.
   * @param forward A forward
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitForwardCommodityPhysicalSettleDefinition(ForwardCommodityPhysicalSettleDefinition forward, DATA_TYPE data);

  /**
   * Forward commodity Physical settle  method.
   * @param forward A forward
   * @return The result
   */
  RESULT_TYPE visitForwardCommodityPhysicalSettleDefinition(ForwardCommodityPhysicalSettleDefinition forward);

  /**
   * Coupon commodity cash settle method that takes data.
   * @param coupon A coupon
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCouponCommodityCashSettleDefinition(CouponCommodityCashSettleDefinition coupon, DATA_TYPE data);

  /**
   * Coupon commodity cash settle  method.
   * @param coupon A coupon
   * @return The result
   */
  RESULT_TYPE visitCouponCommodityCashSettleDefinition(CouponCommodityCashSettleDefinition coupon);

  /**
   * Coupon commodity Physical settle method that takes data.
   * @param coupon A coupon
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitCouponCommodityPhysicalSettleDefinition(CouponCommodityPhysicalSettleDefinition coupon, DATA_TYPE data);

  /**
   * Coupon commodity Physical settle  method.
   * @param coupon A coupon
   * @return The result
   */
  RESULT_TYPE visitCouponCommodityPhysicalSettleDefinition(CouponCommodityPhysicalSettleDefinition coupon);

  // -----     Equity    -----

  /**
   * Equity future method that takes data.
   * @param future An equity future
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitEquityFutureDefinition(EquityFutureDefinition future, DATA_TYPE data);

  /**
   * Equity future method.
   * @param future An equity future
   * @return The result
   */
  RESULT_TYPE visitEquityFutureDefinition(EquityFutureDefinition future);

  /**
   * Index future method that takes data.
   * @param future An index future
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitIndexFutureDefinition(IndexFutureDefinition future, DATA_TYPE data);

  /**
   * Index future method.
   * @param future An index future
   * @return The result
   */
  RESULT_TYPE visitIndexFutureDefinition(IndexFutureDefinition future);

  /**
   * Equity index future method that takes data.
   * @param future An equity index future
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitEquityIndexFutureDefinition(EquityIndexFutureDefinition future, DATA_TYPE data);

  /**
   * Equity index future method.
   * @param future An equity index future
   * @return The result
   */
  RESULT_TYPE visitEquityIndexFutureDefinition(EquityIndexFutureDefinition future);

  /**
   * Equity index dividend future method that takes data.
   * @param future An equity index dividend future
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitEquityIndexDividendFutureDefinition(EquityIndexDividendFutureDefinition future, DATA_TYPE data);

  /**
   * Equity index dividend future method.
   * @param future An equity index dividend future
   * @return The result
   */
  RESULT_TYPE visitEquityIndexDividendFutureDefinition(EquityIndexDividendFutureDefinition future);

  /**
   * Volatility index future method that takes data.
   * @param future A volatility index future
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitVolatilityIndexFutureDefinition(VolatilityIndexFutureDefinition future, DATA_TYPE data);

  /**
   * Volatility index future method.
   * @param future A volatility index future
   * @return The result
   */
  RESULT_TYPE visitVolatilityIndexFutureDefinition(VolatilityIndexFutureDefinition future);

  /**
   * Equity index option method that takes data.
   * @param option An equity index option
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitEquityIndexOptionDefinition(EquityIndexOptionDefinition option, DATA_TYPE data);

  /**
   * Equity index option method.
   * @param option An equity index option
   * @return The result
   */
  RESULT_TYPE visitEquityIndexOptionDefinition(EquityIndexOptionDefinition option);

  /**
   * Equity option method that takes data.
   * @param option An equity option
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitEquityOptionDefinition(EquityOptionDefinition option, DATA_TYPE data);

  /**
   * Equity option method.
   * @param option An equity option
   * @return The result
   */
  RESULT_TYPE visitEquityOptionDefinition(EquityOptionDefinition option);

  /**
   * Equity index future option method that takes data.
   * @param option An equity index future option
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitEquityIndexFutureOptionDefinition(EquityIndexFutureOptionDefinition option, DATA_TYPE data);

  /**
   * Equity index future option method.
   * @param option An equity index future option
   * @return The result
   */
  RESULT_TYPE visitEquityIndexFutureOptionDefinition(EquityIndexFutureOptionDefinition option);

  // -----     Variance and volatility swap      -----

  /**
   * Variance swap method.
   * @param varianceSwap A variance swap
   * @return The result
   */
  RESULT_TYPE visitVarianceSwapDefinition(VarianceSwapDefinition varianceSwap);

  /**
   * Variance swap method that takes data.
   * @param varianceSwap A variance swap
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitVarianceSwapDefinition(VarianceSwapDefinition varianceSwap, DATA_TYPE data);

  /**
   * Variance swap method.
   * @param varianceSwap A variance swap
   * @return The result
   */
  RESULT_TYPE visitEquityVarianceSwapDefinition(EquityVarianceSwapDefinition varianceSwap);

  /**
   * Variance swap method that takes data.
   * @param varianceSwap A variance swap
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitEquityVarianceSwapDefinition(EquityVarianceSwapDefinition varianceSwap, DATA_TYPE data);

  /**
   * Volatility swap method.
   * @param volatilitySwap A volatility swap
   * @return The result
   */
  RESULT_TYPE visitVolatilitySwapDefinition(VolatilitySwapDefinition volatilitySwap);

  /**
   * Volatility swap method that takes data.
   * @param volatilitySwap A volatility swap
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitVolatilitySwapDefinition(VolatilitySwapDefinition volatilitySwap, DATA_TYPE data);

  /**
   * FX volatility swap method.
   * @param volatilitySwap A volatility swap
   * @return The result
   */
  RESULT_TYPE visitFXVolatilitySwapDefinition(FXVolatilitySwapDefinition volatilitySwap);

  /**
   * FX volatility swap method that takes data.
   * @param volatilitySwap A volatility swap
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitFXVolatilitySwapDefinition(FXVolatilitySwapDefinition volatilitySwap, DATA_TYPE data);

  /**
   * The total return swap method.
   * @param totalReturnSwap A total return swap
   * @return The result
   */
  RESULT_TYPE visitTotalReturnSwapDefinition(TotalReturnSwapDefinition totalReturnSwap);

  /**
   * The total return swap method.
   * @param totalReturnSwap A total return swap
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitTotalReturnSwapDefinition(TotalReturnSwapDefinition totalReturnSwap, DATA_TYPE data);

  /**
   * The bond total return swap method.
   * @param totalReturnSwap A bond total return swap
   * @return The result
   */
  RESULT_TYPE visitBondTotalReturnSwapDefinition(BondTotalReturnSwapDefinition totalReturnSwap);

  /**
   * The bond total return swap method.
   * @param totalReturnSwap A bond total return swap
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitBondTotalReturnSwapDefinition(BondTotalReturnSwapDefinition totalReturnSwap, DATA_TYPE data);

  /**
   * The equity total return swap method.
   * @param totalReturnSwap A equity total return swap
   * @return The result
   */
  RESULT_TYPE visitEquityTotalReturnSwapDefinition(EquityTotalReturnSwapDefinition totalReturnSwap);

  /**
   * The equity total return swap method.
   * @param totalReturnSwap A equity total return swap
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitEquityTotalReturnSwapDefinition(EquityTotalReturnSwapDefinition totalReturnSwap, DATA_TYPE data);

  /**
   * The equity method.
   * @param equity A equity
   * @return The result
   */
  RESULT_TYPE visitEquityDefinition(EquityDefinition equity);

  /**
   * The equity method.
   * @param equity An equity
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitEquityDefinition(EquityDefinition equity, DATA_TYPE data);
}
