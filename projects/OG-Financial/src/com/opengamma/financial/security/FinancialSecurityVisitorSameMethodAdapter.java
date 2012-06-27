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
public class FinancialSecurityVisitorSameMethodAdapter<T> implements FinancialSecurityVisitor<T> {

  public interface Visitor<T> {
    T visit(FinancialSecurity security);
  }

  private final Visitor<T> _value;

  public FinancialSecurityVisitorSameMethodAdapter(Visitor<T> value) {
    _value = value;
  }

  @Override
  public T visitAgricultureFutureSecurity(AgricultureFutureSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitCorporateBondSecurity(CorporateBondSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitGovernmentBondSecurity(GovernmentBondSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitMunicipalBondSecurity(MunicipalBondSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitCapFloorCMSSpreadSecurity(CapFloorCMSSpreadSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitCapFloorSecurity(CapFloorSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitCashSecurity(CashSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitContinuousZeroDepositSecurity(ContinuousZeroDepositSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitEquityBarrierOptionSecurity(EquityBarrierOptionSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitEquityIndexDividendFutureOptionSecurity(EquityIndexDividendFutureOptionSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitEquityIndexOptionSecurity(EquityIndexOptionSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitEquityOptionSecurity(EquityOptionSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitEquitySecurity(EquitySecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitEquityVarianceSwapSecurity(EquityVarianceSwapSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitFRASecurity(FRASecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitFXBarrierOptionSecurity(FXBarrierOptionSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitFXDigitalOptionSecurity(FXDigitalOptionSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitFXForwardSecurity(FXForwardSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitFXOptionSecurity(FXOptionSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitIRFutureOptionSecurity(IRFutureOptionSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitInterestRateFutureSecurity(InterestRateFutureSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitNonDeliverableFXDigitalOptionSecurity(NonDeliverableFXDigitalOptionSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitNonDeliverableFXForwardSecurity(NonDeliverableFXForwardSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitNonDeliverableFXOptionSecurity(NonDeliverableFXOptionSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitPeriodicZeroDepositSecurity(PeriodicZeroDepositSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitSimpleZeroDepositSecurity(SimpleZeroDepositSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitForwardSwapSecurity(ForwardSwapSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitSwapSecurity(SwapSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitSwaptionSecurity(SwaptionSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitBondFutureSecurity(BondFutureSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitCommodityFutureOptionSecurity(CommodityFutureOptionSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitEnergyFutureSecurity(EnergyFutureSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitEquityFutureSecurity(EquityFutureSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitEquityIndexDividendFutureSecurity(EquityIndexDividendFutureSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitFXFutureSecurity(FXFutureSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitIndexFutureSecurity(IndexFutureSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitMetalFutureSecurity(MetalFutureSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitStockFutureSecurity(StockFutureSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitAgricultureForwardSecurity(AgricultureForwardSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitEnergyForwardSecurity(EnergyForwardSecurity security) {
    return _value.visit(security);
  }

  @Override
  public T visitMetalForwardSecurity(MetalForwardSecurity security) {
    return _value.visit(security);
  }
}
