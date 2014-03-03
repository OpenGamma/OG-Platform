/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.engine.target.ObjectComputationTargetType;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashBalanceSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.cashflow.CashFlowSecurity;
import com.opengamma.financial.security.cds.CDSSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexSecurity;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.financial.security.cds.StandardVanillaCDSSecurity;
import com.opengamma.financial.security.deposit.ContinuousZeroDepositSecurity;
import com.opengamma.financial.security.deposit.PeriodicZeroDepositSecurity;
import com.opengamma.financial.security.deposit.SimpleZeroDepositSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.fra.ForwardRateAgreementSecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.DeliverableSwapFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXVolatilitySwapSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.master.security.RawSecurity;

/**
 * OpenGamma Engine type declarations for the security classes defined in this package.
 */
public class FinancialSecurityTypes {

  /**
   * The base class of all security types in this package.
   */
  public static final ObjectComputationTargetType<FinancialSecurity> FINANCIAL_SECURITY = ObjectComputationTargetType.of(FinancialSecurity.class);

  /**
   * The "raw" security type.
   */
  public static final ObjectComputationTargetType<RawSecurity> RAW_SECURITY = ObjectComputationTargetType.of(RawSecurity.class);

  /**
   * The Agricultural Future security type.
   */
  public static final ObjectComputationTargetType<AgricultureFutureSecurity> AGRICULTURE_FUTURE_SECURITY = ObjectComputationTargetType.of(AgricultureFutureSecurity.class);

  /**
   * The Bond Future Option security type.
   */
  public static final ObjectComputationTargetType<BondFutureOptionSecurity> BOND_FUTURE_OPTION_SECURITY = ObjectComputationTargetType.of(BondFutureOptionSecurity.class);

  /**
   * The Bond Future security type.
   */
  public static final ObjectComputationTargetType<BondFutureSecurity> BOND_FUTURE_SECURITY = ObjectComputationTargetType.of(BondFutureSecurity.class);

  /**
   * The Bond security type.
   */
  public static final ObjectComputationTargetType<BondSecurity> BOND_SECURITY = ObjectComputationTargetType.of(BondSecurity.class);

  /**
   * The CAP/Floor CMS Spread security type.
   */
  public static final ObjectComputationTargetType<CapFloorCMSSpreadSecurity> CAP_FLOOR_CMS_SPREAD_SECURITY = ObjectComputationTargetType.of(CapFloorCMSSpreadSecurity.class);

  /**
   * The CAP/Floor security type.
   */
  public static final ObjectComputationTargetType<CapFloorSecurity> CAP_FLOOR_SECURITY = ObjectComputationTargetType.of(CapFloorSecurity.class);

  /**
   * The cash balance security type.
   */
  public static final ObjectComputationTargetType<CashBalanceSecurity> CASH_BALANCE_SECURITY = ObjectComputationTargetType.of(CashBalanceSecurity.class);

  /**
   * The cash-flow security type.
   */
  public static final ObjectComputationTargetType<CashFlowSecurity> CASH_FLOW_SECURITY = ObjectComputationTargetType.of(CashFlowSecurity.class);

  /**
   * The Cash security type.
   */
  public static final ObjectComputationTargetType<CashSecurity> CASH_SECURITY = ObjectComputationTargetType.of(CashSecurity.class);

  /**
   * The CDS security type.
   */
  public static final ObjectComputationTargetType<CDSSecurity> CDS_SECURITY = ObjectComputationTargetType.of(CDSSecurity.class);

  /**
   * The Commodity Future Option security type.
   */
  public static final ObjectComputationTargetType<CommodityFutureOptionSecurity> COMMODITY_FUTURE_OPTION_SECURITY = ObjectComputationTargetType.of(CommodityFutureOptionSecurity.class);

  /**
   * The Continuous Zero Deposit security type.
   */
  public static final ObjectComputationTargetType<ContinuousZeroDepositSecurity> CONTINUOUS_ZERO_DEPOSIT_SECURITY = ObjectComputationTargetType.of(ContinuousZeroDepositSecurity.class);

  /**
   * The Corporate Bond security type.
   */
  public static final ObjectComputationTargetType<CorporateBondSecurity> CORPORATE_BOND_SECURITY = ObjectComputationTargetType.of(CorporateBondSecurity.class);

  /**
   * The credit default swap index security type
   */
  public static final ObjectComputationTargetType<CreditDefaultSwapIndexSecurity> CREDIT_DEFAULT_SWAP_INDEX_SECURITY = ObjectComputationTargetType.of(CreditDefaultSwapIndexSecurity.class);

  /**
   * The credit default swap option security type.
   */
  public static final ObjectComputationTargetType<CreditDefaultSwapOptionSecurity> CREDIT_DEFAULT_SWAP_OPTION_SECURITY = ObjectComputationTargetType.of(CreditDefaultSwapOptionSecurity.class);

  /**
   * A deliverable swap future security type.
   */
  public static final ObjectComputationTargetType<DeliverableSwapFutureSecurity> DELIVERABLE_SWAP_FUTURE_SECURITY = ObjectComputationTargetType.of(DeliverableSwapFutureSecurity.class);

  /**
   * The Energy Future Option security type.
   */
  public static final ObjectComputationTargetType<EnergyFutureSecurity> ENERGY_FUTURE_SECURITY = ObjectComputationTargetType.of(EnergyFutureSecurity.class);

  /**
   * The Equity Barrier Option security type.
   */
  public static final ObjectComputationTargetType<EquityBarrierOptionSecurity> EQUITY_BARRIER_OPTION_SECURITY = ObjectComputationTargetType.of(EquityBarrierOptionSecurity.class);

  /**
   * The Equity Future security type.
   */
  public static final ObjectComputationTargetType<EquityFutureSecurity> EQUITY_FUTURE_SECURITY = ObjectComputationTargetType.of(EquityFutureSecurity.class);

  /**
   * The Equity Index Dividend Future Option security type.
   */
  public static final ObjectComputationTargetType<EquityIndexDividendFutureOptionSecurity> EQUITY_INDEX_DIVIDEND_FUTURE_OPTION_SECURITY = ObjectComputationTargetType
      .of(EquityIndexDividendFutureOptionSecurity.class);

  /**
   * The Equity Index Dividend Future security type.
   */
  public static final ObjectComputationTargetType<EquityIndexDividendFutureSecurity> EQUITY_INDEX_DIVIDEND_FUTURE_SECURITY = ObjectComputationTargetType.of(EquityIndexDividendFutureSecurity.class);

  /**
   * The Equity Index Option security type.
   */
  public static final ObjectComputationTargetType<EquityIndexOptionSecurity> EQUITY_INDEX_OPTION_SECURITY = ObjectComputationTargetType.of(EquityIndexOptionSecurity.class);

  /**
   * The Equity Option security type.
   */
  public static final ObjectComputationTargetType<EquityOptionSecurity> EQUITY_OPTION_SECURITY = ObjectComputationTargetType.of(EquityOptionSecurity.class);

  /**
   * The Equity Index Future Option security type.
   */
  public static final ObjectComputationTargetType<EquityIndexFutureOptionSecurity> EQUITY_INDEX_FUTURE_OPTION_SECURITY = ObjectComputationTargetType.of(EquityIndexFutureOptionSecurity.class);

  /**
   * The Equity security type.
   */
  public static final ObjectComputationTargetType<EquitySecurity> EQUITY_SECURITY = ObjectComputationTargetType.of(EquitySecurity.class);

  /**
   * The Equity Variance Swap security type.
   */
  public static final ObjectComputationTargetType<EquityVarianceSwapSecurity> EQUITY_VARIANCE_SWAP_SECURITY = ObjectComputationTargetType.of(EquityVarianceSwapSecurity.class);

  /**
   * The Forward Swap security type.
   */
  public static final ObjectComputationTargetType<ForwardSwapSecurity> FORWARD_SWAP_SECURITY = ObjectComputationTargetType.of(ForwardSwapSecurity.class);

  /**
   * The FRA security type.
   */
  public static final ObjectComputationTargetType<FRASecurity> FRA_SECURITY = ObjectComputationTargetType.of(FRASecurity.class);

  /**
   * The FRA security type.
   */
  public static final ObjectComputationTargetType<ForwardRateAgreementSecurity> FORWARD_RATE_AGREEMENT_SECURITY = ObjectComputationTargetType.of(ForwardRateAgreementSecurity.class);

  /**
   * The Future security type.
   */
  public static final ObjectComputationTargetType<FutureSecurity> FUTURE_SECURITY = ObjectComputationTargetType.of(FutureSecurity.class);

  /**
   * The FX Barrier Option security type.
   */
  public static final ObjectComputationTargetType<FXBarrierOptionSecurity> FX_BARRIER_OPTION_SECURITY = ObjectComputationTargetType.of(FXBarrierOptionSecurity.class);

  /**
   * The FX Digital Option security type.
   */
  public static final ObjectComputationTargetType<FXDigitalOptionSecurity> FX_DIGITAL_OPTION_SECURITY = ObjectComputationTargetType.of(FXDigitalOptionSecurity.class);

  /**
   * The FX Forward security type.
   */
  public static final ObjectComputationTargetType<FXForwardSecurity> FX_FORWARD_SECURITY = ObjectComputationTargetType.of(FXForwardSecurity.class);

  /**
   * The FX Future security type.
   */
  public static final ObjectComputationTargetType<FXFutureSecurity> FX_FUTURE_SECURITY = ObjectComputationTargetType.of(FXFutureSecurity.class);

  /**
   * The FX Option security type.
   */
  public static final ObjectComputationTargetType<FXOptionSecurity> FX_OPTION_SECURITY = ObjectComputationTargetType.of(FXOptionSecurity.class);

  /**
   * The Government Bond security type.
   */
  public static final ObjectComputationTargetType<GovernmentBondSecurity> GOVERNMENT_BOND_SECURITY = ObjectComputationTargetType.of(GovernmentBondSecurity.class);

  /**
   * The Index Future security type.
   */
  public static final ObjectComputationTargetType<IndexFutureSecurity> INDEX_FUTURE_SECURITY = ObjectComputationTargetType.of(IndexFutureSecurity.class);

  /**
   * The IR Future security type.
   */
  public static final ObjectComputationTargetType<InterestRateFutureSecurity> INTEREST_RATE_FUTURE_SECURITY = ObjectComputationTargetType.of(InterestRateFutureSecurity.class);

  /**
   * The IR Future Option security type.
   */
  public static final ObjectComputationTargetType<IRFutureOptionSecurity> IR_FUTURE_OPTION_SECURITY = ObjectComputationTargetType.of(IRFutureOptionSecurity.class);

  /**
   * The Legacy Vanilla CDS security type
   */
  public static final ObjectComputationTargetType<LegacyVanillaCDSSecurity> LEGACY_VANILLA_CDS_SECURITY = ObjectComputationTargetType.of(LegacyVanillaCDSSecurity.class);

  /**
   * The Metal Future security type.
   */
  public static final ObjectComputationTargetType<MetalFutureSecurity> METAL_FUTURE_SECURITY = ObjectComputationTargetType.of(MetalFutureSecurity.class);

  /**
   * The Municipal Bond security type.
   */
  public static final ObjectComputationTargetType<MunicipalBondSecurity> MUNICIPAL_BOND_SECURITY = ObjectComputationTargetType.of(MunicipalBondSecurity.class);

  /**
   * The Non-Deliverable FX Digital Option security type.
   */
  public static final ObjectComputationTargetType<NonDeliverableFXDigitalOptionSecurity> NON_DELIVERABLE_FX_DIGITAL_OPTION_SECURITY = ObjectComputationTargetType
      .of(NonDeliverableFXDigitalOptionSecurity.class);

  /**
   * The Non-Deliverable FX Forward security type.
   */
  public static final ObjectComputationTargetType<NonDeliverableFXForwardSecurity> NON_DELIVERABLE_FX_FORWARD_SECURITY = ObjectComputationTargetType.of(NonDeliverableFXForwardSecurity.class);

  /**
   * The Non-Deliverable FX Option security type.
   */
  public static final ObjectComputationTargetType<NonDeliverableFXOptionSecurity> NON_DELIVERABLE_FX_OPTION_SECURITY = ObjectComputationTargetType.of(NonDeliverableFXOptionSecurity.class);

  /**
   * The Periodic Zero Deposit security type.
   */
  public static final ObjectComputationTargetType<PeriodicZeroDepositSecurity> PERIODIC_ZERO_DEPOSIT_SECURITY = ObjectComputationTargetType.of(PeriodicZeroDepositSecurity.class);

  /**
   * The Simple Zero Deposit security type.
   */
  public static final ObjectComputationTargetType<SimpleZeroDepositSecurity> SIMPLE_ZERO_DEPOSIT_SECURITY = ObjectComputationTargetType.of(SimpleZeroDepositSecurity.class);

  /**
   * The Standard Vanilla CDS security type
   */
  public static final ObjectComputationTargetType<StandardVanillaCDSSecurity> STANDARD_VANILLA_CDS_SECURITY = ObjectComputationTargetType.of(StandardVanillaCDSSecurity.class);

  /**
   * The Stock Future security type.
   */
  public static final ObjectComputationTargetType<StockFutureSecurity> STOCK_FUTURE_SECURITY = ObjectComputationTargetType.of(StockFutureSecurity.class);

  /**
   * The Swap security type.
   */
  public static final ObjectComputationTargetType<SwapSecurity> SWAP_SECURITY = ObjectComputationTargetType.of(SwapSecurity.class);

  /**
   * The Interest Rate Swap security type.
   */
  public static final ObjectComputationTargetType<InterestRateSwapSecurity> INTEREST_RATE_SWAP_SECURITY = ObjectComputationTargetType.of(InterestRateSwapSecurity.class);

  /**
   * The Swaption security type.
   */
  public static final ObjectComputationTargetType<SwaptionSecurity> SWAPTION_SECURITY = ObjectComputationTargetType.of(SwaptionSecurity.class);

  /**
   * The FX Volatility Swap security type.
   */
  public static final ObjectComputationTargetType<FXVolatilitySwapSecurity> FX_VOLATILITY_SWAP_SECURITY = ObjectComputationTargetType.of(FXVolatilitySwapSecurity.class);

}
