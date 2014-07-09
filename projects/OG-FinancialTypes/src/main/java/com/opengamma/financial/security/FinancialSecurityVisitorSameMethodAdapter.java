/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

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
import com.opengamma.financial.security.cds.CDSSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexDefinitionSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexSecurity;
import com.opengamma.financial.security.cds.LegacyFixedRecoveryCDSSecurity;
import com.opengamma.financial.security.cds.LegacyRecoveryLockCDSSecurity;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.financial.security.cds.StandardFixedRecoveryCDSSecurity;
import com.opengamma.financial.security.cds.StandardRecoveryLockCDSSecurity;
import com.opengamma.financial.security.cds.StandardVanillaCDSSecurity;
import com.opengamma.financial.security.credit.IndexCDSDefinitionSecurity;
import com.opengamma.financial.security.credit.IndexCDSSecurity;
import com.opengamma.financial.security.credit.LegacyCDSSecurity;
import com.opengamma.financial.security.credit.StandardCDSSecurity;
import com.opengamma.financial.security.deposit.ContinuousZeroDepositSecurity;
import com.opengamma.financial.security.deposit.PeriodicZeroDepositSecurity;
import com.opengamma.financial.security.deposit.SimpleZeroDepositSecurity;
import com.opengamma.financial.security.equity.AmericanDepositaryReceiptSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.equity.ExchangeTradedFundSecurity;
import com.opengamma.financial.security.forward.AgricultureForwardSecurity;
import com.opengamma.financial.security.forward.EnergyForwardSecurity;
import com.opengamma.financial.security.forward.MetalForwardSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.fra.ForwardRateAgreementSecurity;
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
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.EquityWarrantSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.FxFutureOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.BillTotalReturnSwapSecurity;
import com.opengamma.financial.security.swap.BondTotalReturnSwapSecurity;
import com.opengamma.financial.security.swap.EquityTotalReturnSwapSecurity;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.security.swap.YearOnYearInflationSwapSecurity;
import com.opengamma.financial.security.swap.ZeroCouponInflationSwapSecurity;

/**
 * Adapter for visiting all concrete asset classes.
 *
 * @param <T> Return type for visitor.
 */
public class FinancialSecurityVisitorSameMethodAdapter<T> implements FinancialSecurityVisitor<T> {

  /**
   * @param <T> Return type for the visitor.
   */
  public interface Visitor<T> {
    T visit(FinancialSecurity security);
  }

  private final Visitor<T> _value;

  public FinancialSecurityVisitorSameMethodAdapter(final Visitor<T> value) {
    _value = value;
  }

  @Override
  public T visitAgricultureFutureSecurity(final AgricultureFutureSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitBillSecurity(final BillSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitCorporateBondSecurity(final CorporateBondSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitGovernmentBondSecurity(final GovernmentBondSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitMunicipalBondSecurity(final MunicipalBondSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitInflationBondSecurity(final InflationBondSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitCapFloorSecurity(final CapFloorSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitCashBalanceSecurity(final CashBalanceSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitCashSecurity(final CashSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitCashFlowSecurity(final CashFlowSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitContinuousZeroDepositSecurity(final ContinuousZeroDepositSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitEquityIndexFutureOptionSecurity(final EquityIndexFutureOptionSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitEquityOptionSecurity(final EquityOptionSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitEquitySecurity(final EquitySecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitFRASecurity(final FRASecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitForwardRateAgreementSecurity(final ForwardRateAgreementSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitFXForwardSecurity(final FXForwardSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitFXOptionSecurity(final FXOptionSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitPeriodicZeroDepositSecurity(final PeriodicZeroDepositSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitSimpleZeroDepositSecurity(final SimpleZeroDepositSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitForwardSwapSecurity(final ForwardSwapSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitSwapSecurity(final SwapSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitSwaptionSecurity(final SwaptionSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitBondFutureSecurity(final BondFutureSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitCommodityFutureOptionSecurity(final CommodityFutureOptionSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitFxFutureOptionSecurity(final FxFutureOptionSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitBondFutureOptionSecurity(final BondFutureOptionSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitEquityFutureSecurity(final EquityFutureSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitEquityIndexDividendFutureSecurity(final EquityIndexDividendFutureSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitFXFutureSecurity(final FXFutureSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitIndexFutureSecurity(final IndexFutureSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitMetalFutureSecurity(final MetalFutureSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitStockFutureSecurity(final StockFutureSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitAgricultureForwardSecurity(final AgricultureForwardSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitEnergyForwardSecurity(final EnergyForwardSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitMetalForwardSecurity(final MetalForwardSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitCDSSecurity(final CDSSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitStandardVanillaCDSSecurity(final StandardVanillaCDSSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitStandardFixedRecoveryCDSSecurity(final StandardFixedRecoveryCDSSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitStandardRecoveryLockCDSSecurity(final StandardRecoveryLockCDSSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitLegacyVanillaCDSSecurity(final LegacyVanillaCDSSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitLegacyFixedRecoveryCDSSecurity(final LegacyFixedRecoveryCDSSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitLegacyRecoveryLockCDSSecurity(final LegacyRecoveryLockCDSSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitDeliverableSwapFutureSecurity(final DeliverableSwapFutureSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitCreditDefaultSwapIndexDefinitionSecurity(final CreditDefaultSwapIndexDefinitionSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitCreditDefaultSwapIndexSecurity(final CreditDefaultSwapIndexSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitCreditDefaultSwapOptionSecurity(final CreditDefaultSwapOptionSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitZeroCouponInflationSwapSecurity(final ZeroCouponInflationSwapSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitYearOnYearInflationSwapSecurity(final YearOnYearInflationSwapSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitInterestRateSwapSecurity(final InterestRateSwapSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitFXVolatilitySwapSecurity(final FXVolatilitySwapSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitExchangeTradedFundSecurity(final ExchangeTradedFundSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitAmericanDepositaryReceiptSecurity(final AmericanDepositaryReceiptSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitEquityWarrantSecurity(final EquityWarrantSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitFloatingRateNoteSecurity(final FloatingRateNoteSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitEquityTotalReturnSwapSecurity(final EquityTotalReturnSwapSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitBondTotalReturnSwapSecurity(final BondTotalReturnSwapSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitBillTotalReturnSwapSecurity(BillTotalReturnSwapSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitStandardCDSSecurity(final StandardCDSSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitLegacyCDSSecurity(final LegacyCDSSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitIndexCDSSecurity(final IndexCDSSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitIndexCDSDefinitionSecurity(final IndexCDSDefinitionSecurity security) {
    return _value.visit(security);
  }
}
