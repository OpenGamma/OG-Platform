/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.core.security.Security;
import com.opengamma.engine.target.digest.SecurityTypeTargetDigests;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.cashflow.CashFlowSecurity;
import com.opengamma.financial.security.cds.CDSSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexDefinitionSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexSecurity;
import com.opengamma.financial.security.cds.LegacyFixedRecoveryCDSSecurity;
import com.opengamma.financial.security.cds.LegacyRecoveryLockCDSSecurity;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.financial.security.cds.StandardFixedRecoveryCDSSecurity;
import com.opengamma.financial.security.cds.StandardRecoveryLockCDSSecurity;
import com.opengamma.financial.security.cds.StandardVanillaCDSSecurity;
import com.opengamma.financial.security.deposit.ContinuousZeroDepositSecurity;
import com.opengamma.financial.security.deposit.PeriodicZeroDepositSecurity;
import com.opengamma.financial.security.deposit.SimpleZeroDepositSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.forward.AgricultureForwardSecurity;
import com.opengamma.financial.security.forward.EnergyForwardSecurity;
import com.opengamma.financial.security.forward.MetalForwardSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.DeliverableSwapFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
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
import com.opengamma.financial.security.swap.CommodityNotional;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.NotionalVisitor;
import com.opengamma.financial.security.swap.SecurityNotional;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.security.swap.VarianceSwapNotional;
import com.opengamma.util.tuple.Pair;

/**
 * Extension to the basic target digests from OG-Engine that supplements the digest with the instrument's currency. This is based on the observation that many of the default constraints are configured
 * with per-currency information.
 * <p>
 * Note that this is not currently appropriate for use alongside some of the default value injecting functions, for example the ones that pick identifiers out of the calculation configuration and
 * inject constraints based on those.
 */
public class FinancialSecurityTargetDigests extends SecurityTypeTargetDigests implements FinancialSecurityVisitor<Object>, NotionalVisitor<Object> {

  // TODO: Should items with an exchange use that in the digest too to capture the per-exchange default constraints?

  // TODO: Fix for the classes of functions that do behave differently (or suppress the logic if items are in the calculation configuration that drive those functions)

  // SecurityTypeTargetDigests

  @Override
  protected Object getSecurityDigest(final Security security) {
    if (security instanceof FinancialSecurity) {
      return Pair.of(security.getClass(), ((FinancialSecurity) security).accept(this));
    } else {
      return super.getSecurityDigest(security);
    }
  }

  // FinancialSecurityVisitor

  @Override
  public Object visitDeliverableSwapFutureSecurity(DeliverableSwapFutureSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitEnergyForwardSecurity(EnergyForwardSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitAgricultureForwardSecurity(AgricultureForwardSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitMetalForwardSecurity(MetalForwardSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitCDSSecurity(CDSSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitStandardVanillaCDSSecurity(StandardVanillaCDSSecurity security) {
    return security.getNotional().getCurrency();
  }

  @Override
  public Object visitStandardFixedRecoveryCDSSecurity(StandardFixedRecoveryCDSSecurity security) {
    return security.getNotional().getCurrency();
  }

  @Override
  public Object visitStandardRecoveryLockCDSSecurity(StandardRecoveryLockCDSSecurity security) {
    return security.getNotional().getCurrency();
  }

  @Override
  public Object visitLegacyVanillaCDSSecurity(LegacyVanillaCDSSecurity security) {
    return security.getNotional().getCurrency();
  }

  @Override
  public Object visitLegacyFixedRecoveryCDSSecurity(LegacyFixedRecoveryCDSSecurity security) {
    return security.getNotional().getCurrency();
  }

  @Override
  public Object visitLegacyRecoveryLockCDSSecurity(LegacyRecoveryLockCDSSecurity security) {
    return security.getNotional().getCurrency();
  }

  @Override
  public Object visitCreditDefaultSwapIndexDefinitionSecurity(CreditDefaultSwapIndexDefinitionSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitCreditDefaultSwapIndexSecurity(CreditDefaultSwapIndexSecurity security) {
    return security.getNotional().getCurrency();
  }

  @Override
  public Object visitCreditDefaultSwapOptionSecurity(CreditDefaultSwapOptionSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitAgricultureFutureSecurity(AgricultureFutureSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitBondFutureSecurity(BondFutureSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitEquityIndexDividendFutureSecurity(EquityIndexDividendFutureSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitFXFutureSecurity(FXFutureSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitStockFutureSecurity(StockFutureSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitEquityFutureSecurity(EquityFutureSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitEnergyFutureSecurity(EnergyFutureSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitIndexFutureSecurity(IndexFutureSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitInterestRateFutureSecurity(InterestRateFutureSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitMetalFutureSecurity(MetalFutureSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitCapFloorCMSSpreadSecurity(CapFloorCMSSpreadSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitCapFloorSecurity(CapFloorSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitCashSecurity(CashSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitCashFlowSecurity(CashFlowSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitCommodityFutureOptionSecurity(CommodityFutureOptionSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitFxFutureOptionSecurity(FxFutureOptionSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitBondFutureOptionSecurity(BondFutureOptionSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitContinuousZeroDepositSecurity(ContinuousZeroDepositSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitCorporateBondSecurity(CorporateBondSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitEquityBarrierOptionSecurity(EquityBarrierOptionSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitEquityIndexDividendFutureOptionSecurity(EquityIndexDividendFutureOptionSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitEquityIndexFutureOptionSecurity(EquityIndexFutureOptionSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitEquityIndexOptionSecurity(EquityIndexOptionSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitEquityOptionSecurity(EquityOptionSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitEquitySecurity(EquitySecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitEquityVarianceSwapSecurity(EquityVarianceSwapSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitFRASecurity(FRASecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitFXBarrierOptionSecurity(FXBarrierOptionSecurity security) {
    return Pair.of(security.getPutCurrency(), security.getCallCurrency());
  }

  @Override
  public Object visitFXDigitalOptionSecurity(FXDigitalOptionSecurity security) {
    return Pair.of(security.getPutCurrency(), security.getCallCurrency());
  }

  @Override
  public Object visitFXForwardSecurity(FXForwardSecurity security) {
    return Pair.of(security.getPayCurrency(), security.getReceiveCurrency());
  }

  @Override
  public Object visitFXOptionSecurity(FXOptionSecurity security) {
    return Pair.of(security.getPutCurrency(), security.getCallCurrency());
  }

  @Override
  public Object visitForwardSwapSecurity(ForwardSwapSecurity security) {
    return Pair.of(security.getPayLeg().getNotional().accept(this), security.getReceiveLeg().getNotional().accept(this));
  }

  @Override
  public Object visitGovernmentBondSecurity(GovernmentBondSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitIRFutureOptionSecurity(IRFutureOptionSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitMunicipalBondSecurity(MunicipalBondSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitNonDeliverableFXDigitalOptionSecurity(NonDeliverableFXDigitalOptionSecurity security) {
    return Pair.of(security.getPutCurrency(), security.getCallCurrency());
  }

  @Override
  public Object visitNonDeliverableFXForwardSecurity(NonDeliverableFXForwardSecurity security) {
    return Pair.of(security.getPayCurrency(), security.getReceiveCurrency());
  }

  @Override
  public Object visitNonDeliverableFXOptionSecurity(NonDeliverableFXOptionSecurity security) {
    return Pair.of(security.getPutCurrency(), security.getCallCurrency());
  }

  @Override
  public Object visitPeriodicZeroDepositSecurity(PeriodicZeroDepositSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitSimpleZeroDepositSecurity(SimpleZeroDepositSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Object visitSwapSecurity(SwapSecurity security) {
    return Pair.of(security.getPayLeg().getNotional().accept(this), security.getReceiveLeg().getNotional().accept(this));
  }

  @Override
  public Object visitSwaptionSecurity(SwaptionSecurity security) {
    return security.getCurrency();
  }

  // NotionalVisitor

  @Override
  public Object visitCommodityNotional(CommodityNotional notional) {
    return null;
  }

  @Override
  public Object visitInterestRateNotional(InterestRateNotional notional) {
    return notional.getCurrency();
  }

  @Override
  public Object visitSecurityNotional(SecurityNotional notional) {
    return null;
  }

  @Override
  public Object visitVarianceSwapNotional(VarianceSwapNotional notional) {
    return notional.getCurrency();
  }

}
