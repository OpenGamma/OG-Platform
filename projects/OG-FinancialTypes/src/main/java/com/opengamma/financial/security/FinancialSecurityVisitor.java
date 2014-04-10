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
import com.opengamma.financial.security.credit.StandardCDSSecurity;
import com.opengamma.financial.security.credit.IndexCDSDefinitionSecurity;
import com.opengamma.financial.security.credit.IndexCDSSecurity;
import com.opengamma.financial.security.credit.LegacyCDSSecurity;
import com.opengamma.financial.security.deposit.ContinuousZeroDepositSecurity;
import com.opengamma.financial.security.deposit.PeriodicZeroDepositSecurity;
import com.opengamma.financial.security.deposit.SimpleZeroDepositSecurity;
import com.opengamma.financial.security.equity.AmericanDepositaryReceiptSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.equity.ExchangeTradedFundSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.fra.ForwardRateAgreementSecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
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
import com.opengamma.financial.security.swap.BondTotalReturnSwapSecurity;
import com.opengamma.financial.security.swap.EquityTotalReturnSwapSecurity;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.security.swap.YearOnYearInflationSwapSecurity;
import com.opengamma.financial.security.swap.ZeroCouponInflationSwapSecurity;

/**
 * General visitor for top level asset classes.
 *
 * @param <T> Return type for visitor.
 */
public interface FinancialSecurityVisitor<T> extends FutureSecurityVisitor<T>, CommodityForwardSecurityVisitor<T>, CreditSecurityVisitor<T> {

  // FUTURES ----------------------------------------------------------------------------

  @Override
  T visitAgricultureFutureSecurity(AgricultureFutureSecurity security);

  @Override
  T visitBondFutureSecurity(BondFutureSecurity security);

  @Override
  T visitEquityIndexDividendFutureSecurity(EquityIndexDividendFutureSecurity security);

  @Override
  T visitFXFutureSecurity(FXFutureSecurity security);

  @Override
  T visitStockFutureSecurity(StockFutureSecurity security);

  @Override
  T visitEquityFutureSecurity(EquityFutureSecurity security);

  @Override
  T visitEnergyFutureSecurity(EnergyFutureSecurity security);

  @Override
  T visitIndexFutureSecurity(IndexFutureSecurity security);

  @Override
  T visitInterestRateFutureSecurity(InterestRateFutureSecurity security);

  @Override
  T visitMetalFutureSecurity(MetalFutureSecurity security);

  // ------------------------------------------------------------------------------------

  T visitBillSecurity(BillSecurity security);

  T visitCapFloorCMSSpreadSecurity(CapFloorCMSSpreadSecurity security);

  T visitCapFloorSecurity(CapFloorSecurity security);

  T visitCashBalanceSecurity(CashBalanceSecurity security);

  T visitCashSecurity(CashSecurity security);

  T visitCashFlowSecurity(CashFlowSecurity security);

  T visitCommodityFutureOptionSecurity(CommodityFutureOptionSecurity security);

  T visitFxFutureOptionSecurity(FxFutureOptionSecurity security);

  T visitBondFutureOptionSecurity(BondFutureOptionSecurity security);

  T visitContinuousZeroDepositSecurity(ContinuousZeroDepositSecurity security);

  T visitCorporateBondSecurity(CorporateBondSecurity security);

  T visitEquityBarrierOptionSecurity(EquityBarrierOptionSecurity security);

  T visitEquityIndexDividendFutureOptionSecurity(EquityIndexDividendFutureOptionSecurity security);

  T visitEquityIndexFutureOptionSecurity(EquityIndexFutureOptionSecurity security);

  T visitEquityIndexOptionSecurity(EquityIndexOptionSecurity security);

  T visitEquityOptionSecurity(EquityOptionSecurity security);

  T visitEquitySecurity(EquitySecurity security);

  T visitEquityVarianceSwapSecurity(EquityVarianceSwapSecurity security);

  T visitFloatingRateNoteSecurity(FloatingRateNoteSecurity security);

  T visitFRASecurity(FRASecurity security);

  T visitForwardRateAgreementSecurity(ForwardRateAgreementSecurity security);

  T visitFXBarrierOptionSecurity(FXBarrierOptionSecurity security);

  T visitFXDigitalOptionSecurity(FXDigitalOptionSecurity security);

  T visitFXForwardSecurity(FXForwardSecurity security);

  T visitFXOptionSecurity(FXOptionSecurity security);

  T visitForwardSwapSecurity(ForwardSwapSecurity security);

  T visitGovernmentBondSecurity(GovernmentBondSecurity security);

  T visitIRFutureOptionSecurity(IRFutureOptionSecurity security);

  T visitMunicipalBondSecurity(MunicipalBondSecurity security);

  T visitInflationBondSecurity(InflationBondSecurity security);

  T visitNonDeliverableFXDigitalOptionSecurity(NonDeliverableFXDigitalOptionSecurity security);

  T visitNonDeliverableFXForwardSecurity(NonDeliverableFXForwardSecurity security);

  T visitNonDeliverableFXOptionSecurity(NonDeliverableFXOptionSecurity security);

  T visitPeriodicZeroDepositSecurity(PeriodicZeroDepositSecurity security);

  T visitSimpleZeroDepositSecurity(SimpleZeroDepositSecurity security);

  T visitSwapSecurity(SwapSecurity security);

  T visitSwaptionSecurity(SwaptionSecurity security);

  T visitZeroCouponInflationSwapSecurity(ZeroCouponInflationSwapSecurity security);

  T visitYearOnYearInflationSwapSecurity(YearOnYearInflationSwapSecurity security);

  T visitInterestRateSwapSecurity(InterestRateSwapSecurity security);

  T visitFXVolatilitySwapSecurity(FXVolatilitySwapSecurity security);

  T visitExchangeTradedFundSecurity(ExchangeTradedFundSecurity security);

  T visitAmericanDepositaryReceiptSecurity(AmericanDepositaryReceiptSecurity security);

  T visitEquityWarrantSecurity(EquityWarrantSecurity security);

  T visitEquityTotalReturnSwapSecurity(EquityTotalReturnSwapSecurity security);

  T visitBondTotalReturnSwapSecurity(BondTotalReturnSwapSecurity security);

  // Credit

  T visitStandardCDSSecurity(StandardCDSSecurity security);

  T visitLegacyCDSSecurity(LegacyCDSSecurity security);

  T visitIndexCDSSecurity(IndexCDSSecurity security);

  T visitIndexCDSDefinitionSecurity(IndexCDSDefinitionSecurity security);
}
