/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.security;

import com.opengamma.financial.security.FinancialSecurityVisitorSameValueAdapter;
import com.opengamma.financial.security.bond.BillSecurity;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.FloatingRateNoteSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.InflationBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashBalanceSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.cashflow.CashFlowSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexDefinitionSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexSecurity;
import com.opengamma.financial.security.cds.LegacyFixedRecoveryCDSSecurity;
import com.opengamma.financial.security.cds.LegacyRecoveryLockCDSSecurity;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.financial.security.cds.StandardFixedRecoveryCDSSecurity;
import com.opengamma.financial.security.cds.StandardRecoveryLockCDSSecurity;
import com.opengamma.financial.security.cds.StandardVanillaCDSSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.DeliverableSwapFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.FederalFundsFutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXVolatilitySwapSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
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
import com.opengamma.financial.security.option.FxFutureOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.security.swap.YearOnYearInflationSwapSecurity;
import com.opengamma.financial.security.swap.ZeroCouponInflationSwapSecurity;

/**
 * Returns Freemarker template filename for a given security type.
 */
public class SecurityTemplateNameProvider extends FinancialSecurityVisitorSameValueAdapter<String> {

  /**
   * Default constructor.
   */
  SecurityTemplateNameProvider() {
    super("default-security.ftl");
  }

  @Override
  public String visitEquitySecurity(final EquitySecurity security) {
    return "equity.ftl";
  }

  @Override
  public String visitFRASecurity(final FRASecurity security) {
    return "fra.ftl";
  }

  @Override
  public String visitCashSecurity(final CashSecurity security) {
    return "cash.ftl";
  }

  @Override
  public String visitCashFlowSecurity(final CashFlowSecurity security) {
    return "cashflow.ftl";
  }

  @Override
  public String visitCorporateBondSecurity(final CorporateBondSecurity security) {
    return getBond();
  }

  @Override
  public String visitInflationBondSecurity(InflationBondSecurity security) {
    return getBond();
  }

  @Override
  public String visitGovernmentBondSecurity(final GovernmentBondSecurity security) {
    return getBond();
  }

  @Override
  public String visitMunicipalBondSecurity(final MunicipalBondSecurity security) {
    return getBond();
  }

  @Override
  public String visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
    return getFuture();
  }

  @Override
  public String visitBondFutureSecurity(final BondFutureSecurity security) {
    return getFuture();
  }

  @Override
  public String visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
    return getFuture();
  }

  @Override
  public String visitEquityFutureSecurity(final EquityFutureSecurity security) {
    return getFuture();
  }

  @Override
  public String visitEquityIndexDividendFutureSecurity(final EquityIndexDividendFutureSecurity security) {
    return getFuture();
  }

  @Override
  public String visitFXFutureSecurity(final FXFutureSecurity security) {
    return getFuture();
  }

  @Override
  public String visitIndexFutureSecurity(final IndexFutureSecurity security) {
    return getFuture();
  }

  @Override
  public String visitMetalFutureSecurity(final MetalFutureSecurity security) {
    return getFuture();
  }

  @Override
  public String visitStockFutureSecurity(final StockFutureSecurity security) {
    return getFuture();
  }

  @Override
  public String visitAgricultureFutureSecurity(final AgricultureFutureSecurity security) {
    return getFuture();
  }

  @Override
  public String visitDeliverableSwapFutureSecurity(final DeliverableSwapFutureSecurity security) {
    return getFuture();
  }

  @Override
  public String visitEquityOptionSecurity(final EquityOptionSecurity security) {
    return "equity-option.ftl";
  }

  @Override
  public String visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
    return "equity-barrier-option.ftl";
  }

  @Override
  public String visitFXForwardSecurity(final FXForwardSecurity security) {
    return "fxforward.ftl";
  }

  @Override
  public String visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity security) {
    return "nondeliverable-fxforward.ftl";
  }

  @Override
  public String visitSwapSecurity(final SwapSecurity security) {
    return "swap.ftl";
  }

  @Override
  public String visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
    return "fxbarrier-option.ftl";
  }

  @Override
  public String visitFXOptionSecurity(final FXOptionSecurity security) {
    return "fxoption.ftl";
  }

  @Override
  public String visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
    return "equity-index-option.ftl";
  }

  @Override
  public String visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity security) {
    return "fxdigital-option.ftl";
  }

  @Override
  public String visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity security) {
    return "nondeliverable-fxdigital-option.ftl";
  }

  @Override
  public String visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
    return "nondeliverable-fxoption.ftl";
  }

  @Override
  public String visitSwaptionSecurity(final SwaptionSecurity security) {
    return "swaption.ftl";
  }

  @Override
  public String visitEquityIndexFutureOptionSecurity(final EquityIndexFutureOptionSecurity security) {
    return "equity-index-future-option.ftl";
  }

  @Override
  public String visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
    return "irfuture-option.ftl";
  }

  @Override
  public String visitCommodityFutureOptionSecurity(final CommodityFutureOptionSecurity security) {
    return "commodity-future-option.ftl";
  }

  @Override
  public String visitFxFutureOptionSecurity(final FxFutureOptionSecurity security) {
    return "fxfuture-option.ftl";
  }

  @Override
  public String visitBondFutureOptionSecurity(final BondFutureOptionSecurity security) {
    return "bond-future-option.ftl";
  }

  @Override
  public String visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
    return "capfloor-cms-spread.ftl";
  }

  @Override
  public String visitCapFloorSecurity(final CapFloorSecurity security) {
    return "capfloor.ftl";
  }

  @Override
  public String visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity security) {
    return "equity-index-dividend-future-option.ftl";
  }

  @Override
  public String visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
    return "equity-variance-swap.ftl";
  }

  @Override
  public String visitCreditDefaultSwapIndexDefinitionSecurity(final CreditDefaultSwapIndexDefinitionSecurity security) {
    return "cds-index-definition.ftl";
  }

  @Override
  public String visitCreditDefaultSwapIndexSecurity(final CreditDefaultSwapIndexSecurity security) {
    return "cds-index.ftl";
  }

  @Override
  public String visitStandardFixedRecoveryCDSSecurity(final StandardFixedRecoveryCDSSecurity security) {
    return "standard-fixed-recovery-cds.ftl";
  }

  @Override
  public String visitStandardVanillaCDSSecurity(final StandardVanillaCDSSecurity security) {
    return "standard-vanilla-cds.ftl";
  }

  @Override
  public String visitStandardRecoveryLockCDSSecurity(final StandardRecoveryLockCDSSecurity security) {
    return "standard-recovery-lock-cds.ftl";
  }

  @Override
  public String visitLegacyVanillaCDSSecurity(final LegacyVanillaCDSSecurity security) {
    return "legacy-vanilla-cds.ftl";
  }

  @Override
  public String visitLegacyFixedRecoveryCDSSecurity(final LegacyFixedRecoveryCDSSecurity security) {
    return "legacy-fixed-recovery-cds.ftl";
  }

  @Override
  public String visitLegacyRecoveryLockCDSSecurity(final LegacyRecoveryLockCDSSecurity security) {
    return "legacy-recovery-lock-cds.ftl";
  }

  @Override
  public String visitCreditDefaultSwapOptionSecurity(final CreditDefaultSwapOptionSecurity security) {
    return "cds-option.ftl";
  }

  @Override
  public String visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity security) {
    return getFuture();
  }

  @Override
  public String visitZeroCouponInflationSwapSecurity(final ZeroCouponInflationSwapSecurity security) {
    return "swap.ftl";
  }

  @Override
  public String visitYearOnYearInflationSwapSecurity(final YearOnYearInflationSwapSecurity security) {
    return "swap.ftl";
  }

  @Override
  public String visitBillSecurity(final BillSecurity security) {
    return "bill.ftl";
  }

  @Override
  public String visitFloatingRateNoteSecurity(final FloatingRateNoteSecurity security) {
    return "floating-rate-note.ftl";
  }

  @Override
  public String visitFXVolatilitySwapSecurity(final FXVolatilitySwapSecurity security) {
    return "fx-volatility-swap.ftl";
  }

  @Override
  public String visitCashBalanceSecurity(final CashBalanceSecurity security) {
    return "cash-balance.ftl";
  }

  /**
   * Gets the template for all bonds.
   * @return The bond template
   */
  private String getBond() {
    return "bond.ftl";
  }

  /**
   * Gets the template for all futures.
   * @return The future template
   */
  private String getFuture() {
    return "future.ftl";
  }

}
