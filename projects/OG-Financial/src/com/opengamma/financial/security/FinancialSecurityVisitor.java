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
 * General visitor for top level asset classes.
 *
 * @param <T> Return type for visitor.
 */
public interface FinancialSecurityVisitor<T> extends FutureSecurityVisitor<T>, CommodityForwardSecurityVisitor<T> {

  // FUTURES ----------------------------------------------------------------------------

  T visitAgricultureFutureSecurity(AgricultureFutureSecurity security);

  T visitBondFutureSecurity(BondFutureSecurity security);

  T visitEquityIndexDividendFutureSecurity(EquityIndexDividendFutureSecurity security);

  T visitFXFutureSecurity(FXFutureSecurity security);

  T visitStockFutureSecurity(StockFutureSecurity security);

  T visitEquityFutureSecurity(EquityFutureSecurity security);

  T visitEnergyFutureSecurity(EnergyFutureSecurity security);

  T visitIndexFutureSecurity(IndexFutureSecurity security);

  T visitInterestRateFutureSecurity(InterestRateFutureSecurity security);

  T visitMetalFutureSecurity(MetalFutureSecurity security);

  // ------------------------------------------------------------------------------------

  T visitCapFloorCMSSpreadSecurity(CapFloorCMSSpreadSecurity security);

  T visitCapFloorSecurity(CapFloorSecurity security);

  T visitCashSecurity(CashSecurity security);

  T visitCommodityFutureOptionSecurity(CommodityFutureOptionSecurity security);

  T visitContinuousZeroDepositSecurity(ContinuousZeroDepositSecurity security);

  T visitCorporateBondSecurity(CorporateBondSecurity security);

  T visitEquityBarrierOptionSecurity(EquityBarrierOptionSecurity security);

  T visitEquityIndexDividendFutureOptionSecurity(EquityIndexDividendFutureOptionSecurity security);

  T visitEquityIndexOptionSecurity(EquityIndexOptionSecurity security);

  T visitEquityOptionSecurity(EquityOptionSecurity security);

  T visitEquitySecurity(EquitySecurity security);

  T visitEquityVarianceSwapSecurity(EquityVarianceSwapSecurity security);

  T visitFRASecurity(FRASecurity security);

  T visitFXBarrierOptionSecurity(FXBarrierOptionSecurity security);

  T visitFXDigitalOptionSecurity(FXDigitalOptionSecurity security);

  T visitFXForwardSecurity(FXForwardSecurity security);

  T visitFXOptionSecurity(FXOptionSecurity security);

  T visitForwardSwapSecurity(ForwardSwapSecurity security);

  T visitGovernmentBondSecurity(GovernmentBondSecurity security);

  T visitIRFutureOptionSecurity(IRFutureOptionSecurity security);

  T visitMunicipalBondSecurity(MunicipalBondSecurity security);

  T visitNonDeliverableFXDigitalOptionSecurity(NonDeliverableFXDigitalOptionSecurity security);

  T visitNonDeliverableFXForwardSecurity(NonDeliverableFXForwardSecurity security);

  T visitNonDeliverableFXOptionSecurity(NonDeliverableFXOptionSecurity security);

  T visitPeriodicZeroDepositSecurity(PeriodicZeroDepositSecurity security);

  T visitSimpleZeroDepositSecurity(SimpleZeroDepositSecurity security);

  T visitSwapSecurity(SwapSecurity security);

  T visitSwaptionSecurity(SwaptionSecurity security);
}

