/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.deposit.ContinuousZeroDepositSecurity;
import com.opengamma.financial.security.deposit.PeriodicZeroDepositSecurity;
import com.opengamma.financial.security.deposit.SimpleZeroDepositSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.forward.AgricultureForwardSecurity;
import com.opengamma.financial.security.forward.EnergyForwardSecurity;
import com.opengamma.financial.security.forward.MetalForwardSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.*;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.*;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;

/**
 * Adapter for visiting all concrete asset classes.
 *
 * @param <T> Return type for visitor.
 */
class FinancialSecurityVisitorDelegate<T> implements FinancialSecurityVisitor<T> {

  private final FinancialSecurityVisitor<T> _delegate;

  public FinancialSecurityVisitorDelegate(FinancialSecurityVisitor<T> delegate) {
    _delegate = delegate;
  }

  @Override
  public T visitAgricultureFutureSecurity(AgricultureFutureSecurity security) {
    return _delegate.visitAgricultureFutureSecurity(security);
  }

  @Override
  public T visitCorporateBondSecurity(CorporateBondSecurity security) {
    return _delegate.visitCorporateBondSecurity(security);
  }

  @Override
  public T visitGovernmentBondSecurity(GovernmentBondSecurity security) {
    return _delegate.visitGovernmentBondSecurity(security);
  }

  @Override
  public T visitMunicipalBondSecurity(MunicipalBondSecurity security) {
    return _delegate.visitMunicipalBondSecurity(security);
  }

  @Override
  public T visitCapFloorCMSSpreadSecurity(CapFloorCMSSpreadSecurity security) {
    return _delegate.visitCapFloorCMSSpreadSecurity(security);
  }

  @Override
  public T visitCapFloorSecurity(CapFloorSecurity security) {
    return _delegate.visitCapFloorSecurity(security);
  }

  @Override
  public T visitCashSecurity(CashSecurity security) {
    return _delegate.visitCashSecurity(security);
  }

  @Override
  public T visitContinuousZeroDepositSecurity(ContinuousZeroDepositSecurity security) {
    return _delegate.visitContinuousZeroDepositSecurity(security);
  }

  @Override
  public T visitEquityBarrierOptionSecurity(EquityBarrierOptionSecurity security) {
    return _delegate.visitEquityBarrierOptionSecurity(security);
  }

  @Override
  public T visitEquityIndexDividendFutureOptionSecurity(EquityIndexDividendFutureOptionSecurity security) {
    return _delegate.visitEquityIndexDividendFutureOptionSecurity(security);
  }

  @Override
  public T visitEquityIndexOptionSecurity(EquityIndexOptionSecurity security) {
    return _delegate.visitEquityIndexOptionSecurity(security);
  }

  @Override
  public T visitEquityOptionSecurity(EquityOptionSecurity security) {
    return _delegate.visitEquityOptionSecurity(security);
  }

  @Override
  public T visitEquitySecurity(EquitySecurity security) {
    return _delegate.visitEquitySecurity(security);
  }

  @Override
  public T visitEquityVarianceSwapSecurity(EquityVarianceSwapSecurity security) {
    return _delegate.visitEquityVarianceSwapSecurity(security);
  }

  @Override
  public T visitFRASecurity(FRASecurity security) {
    return _delegate.visitFRASecurity(security);
  }

  @Override
  public T visitFXBarrierOptionSecurity(FXBarrierOptionSecurity security) {
    return _delegate.visitFXBarrierOptionSecurity(security);
  }

  @Override
  public T visitFXDigitalOptionSecurity(FXDigitalOptionSecurity security) {
    return _delegate.visitFXDigitalOptionSecurity(security);
  }

  @Override
  public T visitFXForwardSecurity(FXForwardSecurity security) {
    return _delegate.visitFXForwardSecurity(security);
  }

  @Override
  public T visitFXOptionSecurity(FXOptionSecurity security) {
    return _delegate.visitFXOptionSecurity(security);
  }

  @Override
  public T visitIRFutureOptionSecurity(IRFutureOptionSecurity security) {
    return _delegate.visitIRFutureOptionSecurity(security);
  }

  @Override
  public T visitInterestRateFutureSecurity(InterestRateFutureSecurity security) {
    return _delegate.visitInterestRateFutureSecurity(security);
  }

  @Override
  public T visitNonDeliverableFXDigitalOptionSecurity(NonDeliverableFXDigitalOptionSecurity security) {
    return _delegate.visitNonDeliverableFXDigitalOptionSecurity(security);
  }

  @Override
  public T visitNonDeliverableFXForwardSecurity(NonDeliverableFXForwardSecurity security) {
    return _delegate.visitNonDeliverableFXForwardSecurity(security);
  }

  @Override
  public T visitNonDeliverableFXOptionSecurity(NonDeliverableFXOptionSecurity security) {
    return _delegate.visitNonDeliverableFXOptionSecurity(security);
  }

  @Override
  public T visitPeriodicZeroDepositSecurity(PeriodicZeroDepositSecurity security) {
    return _delegate.visitPeriodicZeroDepositSecurity(security);
  }

  @Override
  public T visitSimpleZeroDepositSecurity(SimpleZeroDepositSecurity security) {
    return _delegate.visitSimpleZeroDepositSecurity(security);
  }

  @Override
  public T visitForwardSwapSecurity(ForwardSwapSecurity security) {
    return _delegate.visitForwardSwapSecurity(security);
  }

  @Override
  public T visitSwapSecurity(SwapSecurity security) {
    return _delegate.visitSwapSecurity(security);
  }

  @Override
  public T visitSwaptionSecurity(SwaptionSecurity security) {
    return _delegate.visitSwaptionSecurity(security);
  }

  @Override
  public T visitBondFutureSecurity(BondFutureSecurity security) {
    return _delegate.visitBondFutureSecurity(security);
  }

  @Override
  public T visitCommodityFutureOptionSecurity(CommodityFutureOptionSecurity security) {
    return _delegate.visitCommodityFutureOptionSecurity(security);
  }

  @Override
  public T visitEnergyFutureSecurity(EnergyFutureSecurity security) {
    return _delegate.visitEnergyFutureSecurity(security);
  }

  @Override
  public T visitEquityFutureSecurity(EquityFutureSecurity security) {
    return _delegate.visitEquityFutureSecurity(security);
  }

  @Override
  public T visitEquityIndexDividendFutureSecurity(EquityIndexDividendFutureSecurity security) {
    return _delegate.visitEquityIndexDividendFutureSecurity(security);
  }

  @Override
  public T visitFXFutureSecurity(FXFutureSecurity security) {
    return _delegate.visitFXFutureSecurity(security);
  }

  @Override
  public T visitIndexFutureSecurity(IndexFutureSecurity security) {
    return _delegate.visitIndexFutureSecurity(security);
  }

  @Override
  public T visitMetalFutureSecurity(MetalFutureSecurity security) {
    return _delegate.visitMetalFutureSecurity(security);
  }

  @Override
  public T visitStockFutureSecurity(StockFutureSecurity security) {
    return _delegate.visitStockFutureSecurity(security);
  }

  @Override
  public T visitAgricultureForwardSecurity(AgricultureForwardSecurity security) {
    return _delegate.visitAgricultureForwardSecurity(security);
  }

  @Override
  public T visitEnergyForwardSecurity(EnergyForwardSecurity security) {
    return _delegate.visitEnergyForwardSecurity(security);
  }

  @Override
  public T visitMetalForwardSecurity(MetalForwardSecurity security) {
    return _delegate.visitMetalForwardSecurity(security);
  }
}
