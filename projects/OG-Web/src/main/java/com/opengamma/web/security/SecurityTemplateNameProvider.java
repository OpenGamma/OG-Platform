/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.security;

import com.opengamma.financial.security.FinancialSecurityVisitorSameValueAdapter;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
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
/*package*/ class SecurityTemplateNameProvider extends FinancialSecurityVisitorSameValueAdapter<String> {
  
  SecurityTemplateNameProvider() {
    super("default-security.ftl");
  }

  @Override
  public String visitEquitySecurity(EquitySecurity security) {
    return "equity.ftl";
  }

  @Override
  public String visitFRASecurity(FRASecurity security) {
    return "fra.ftl";
  }

  @Override
  public String visitCashSecurity(CashSecurity security) {
    return "cash.ftl";
  }

  @Override
  public String visitCashFlowSecurity(CashFlowSecurity security) {
    return "cashflow.ftl";
  }

  @Override
  public String visitCorporateBondSecurity(CorporateBondSecurity security) {
    return getBond();
  }

  @Override
  public String visitGovernmentBondSecurity(GovernmentBondSecurity security) {
    return getBond();
  }

  @Override
  public String visitMunicipalBondSecurity(MunicipalBondSecurity security) {
    return getBond();
  }
  
  @Override
  public String visitInterestRateFutureSecurity(InterestRateFutureSecurity security) {
    return getFuture();
  }

  @Override
  public String visitBondFutureSecurity(BondFutureSecurity security) {
    return getFuture();
  }

  @Override
  public String visitEnergyFutureSecurity(EnergyFutureSecurity security) {
    return getFuture();
  }

  @Override
  public String visitEquityFutureSecurity(EquityFutureSecurity security) {
    return getFuture();
  }

  @Override
  public String visitEquityIndexDividendFutureSecurity(EquityIndexDividendFutureSecurity security) {
    return getFuture();
  }

  @Override
  public String visitFXFutureSecurity(FXFutureSecurity security) {
    return getFuture();
  }

  @Override
  public String visitIndexFutureSecurity(IndexFutureSecurity security) {
    return getFuture();
  }

  @Override
  public String visitMetalFutureSecurity(MetalFutureSecurity security) {
    return getFuture();
  }

  @Override
  public String visitStockFutureSecurity(StockFutureSecurity security) {
    return getFuture();
  }

  @Override
  public String visitAgricultureFutureSecurity(AgricultureFutureSecurity security) {
    return getFuture();
  }

  @Override
  public String visitDeliverableSwapFutureSecurity(DeliverableSwapFutureSecurity security) {
    return getFuture();
  }
  
  @Override
  public String visitEquityOptionSecurity(EquityOptionSecurity security) {
    return "equity-option.ftl";
  }
  
  @Override
  public String visitEquityBarrierOptionSecurity(EquityBarrierOptionSecurity security) {
    return "equity-barrier-option.ftl";
  }
  
  @Override
  public String visitFXForwardSecurity(FXForwardSecurity security) {
    return "fxforward.ftl";
  }
  
  @Override
  public String visitNonDeliverableFXForwardSecurity(NonDeliverableFXForwardSecurity security) {
    return "nondeliverable-fxforward.ftl";
  }

  @Override
  public String visitSwapSecurity(SwapSecurity security) {
    return "swap.ftl";
  }
  
  @Override
  public String visitFXBarrierOptionSecurity(FXBarrierOptionSecurity security) {
    return "fxbarrier-option.ftl";
  }
  
  @Override
  public String visitFXOptionSecurity(FXOptionSecurity security) {
    return "fxoption.ftl";
  }

  @Override
  public String visitEquityIndexOptionSecurity(EquityIndexOptionSecurity security) {
    return "equity-index-option.ftl";
  }

  @Override
  public String visitFXDigitalOptionSecurity(FXDigitalOptionSecurity security) {
    return "fxdigital-option.ftl";
  }

  @Override
  public String visitNonDeliverableFXDigitalOptionSecurity(NonDeliverableFXDigitalOptionSecurity security) {
    return "nondeliverable-fxdigital-option.ftl";
  }

  @Override
  public String visitNonDeliverableFXOptionSecurity(NonDeliverableFXOptionSecurity security) {
    return "nondeliverable-fxoption.ftl";
  }

  @Override
  public String visitSwaptionSecurity(SwaptionSecurity security) {
    return "swaption.ftl";
  }

  @Override
  public String visitEquityIndexFutureOptionSecurity(EquityIndexFutureOptionSecurity security) {
    return "equity-index-future-option.ftl";
  }

  @Override
  public String visitIRFutureOptionSecurity(IRFutureOptionSecurity security) {
    return "irfuture-option.ftl";
  }

  @Override
  public String visitCommodityFutureOptionSecurity(CommodityFutureOptionSecurity security) {
    return "commodity-future-option.ftl";
  }

  @Override
  public String visitFxFutureOptionSecurity(FxFutureOptionSecurity security) {
    return "fxfuture-option.ftl";
  }

  @Override
  public String visitBondFutureOptionSecurity(BondFutureOptionSecurity security) {
    return "bond-future-option.ftl";
  }

  @Override
  public String visitCapFloorCMSSpreadSecurity(CapFloorCMSSpreadSecurity security) {
    return "capfloor-cms-spread.ftl";
  }

  @Override
  public String visitCapFloorSecurity(CapFloorSecurity security) {
    return "capfloor.ftl";
  }

  @Override
  public String visitEquityIndexDividendFutureOptionSecurity(EquityIndexDividendFutureOptionSecurity security) {
    return "equity-index-dividend-future-option.ftl";
  }
  
  @Override
  public String visitEquityVarianceSwapSecurity(EquityVarianceSwapSecurity security) {
    return "equity-variance-swap.ftl";
  }
  
  @Override
  public String visitCreditDefaultSwapIndexDefinitionSecurity(CreditDefaultSwapIndexDefinitionSecurity security) {
    return "cds-index-definition.ftl";
  }

  @Override
  public String visitCreditDefaultSwapIndexSecurity(CreditDefaultSwapIndexSecurity security) {
    return "cds-index.ftl";
  }
  
  @Override
  public String visitStandardFixedRecoveryCDSSecurity(StandardFixedRecoveryCDSSecurity security) {
    return "standard-fixed-recovery-cds.ftl";
  }
  
  @Override
  public String visitStandardVanillaCDSSecurity(StandardVanillaCDSSecurity security) {
    return "standard-vanilla-cds.ftl";
  }

  @Override
  public String visitStandardRecoveryLockCDSSecurity(StandardRecoveryLockCDSSecurity security) {
    return "standard-recovery-lock-cds.ftl";
  }
  
  @Override
  public String visitLegacyVanillaCDSSecurity(LegacyVanillaCDSSecurity security) {
    return "legacy-vanilla-cds.ftl";
  }
 
  @Override
  public String visitLegacyFixedRecoveryCDSSecurity(LegacyFixedRecoveryCDSSecurity security) {
    return "legacy-fixed-recovery-cds.ftl";
  }
  
  @Override
  public String visitLegacyRecoveryLockCDSSecurity(LegacyRecoveryLockCDSSecurity security) {
    return "legacy-recovery-lock-cds.ftl";
  }

  @Override
  public String visitCreditDefaultSwapOptionSecurity(CreditDefaultSwapOptionSecurity security) {
    return "cds-option.ftl";
  }
  
  @Override
  public String visitFederalFundsFutureSecurity(FederalFundsFutureSecurity security) {
    return getFuture();
  }
  
  @Override
  public String visitZeroCouponInflationSwapSecurity(ZeroCouponInflationSwapSecurity security) {
    return "swap.ftl";
  }
  
  @Override
  public String visitYearOnYearInflationSwapSecurity(YearOnYearInflationSwapSecurity security) {
    return "swap.ftl";
  }

  private String getBond() {
    return "bond.ftl";
  }
  
  private String getFuture() {
    return "future.ftl";
  }
}
