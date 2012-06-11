/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.deposit.ContinuousZeroDepositSecurity;
import com.opengamma.financial.security.deposit.PeriodicZeroDepositSecurity;
import com.opengamma.financial.security.deposit.SimpleZeroDepositSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.*;
import com.opengamma.financial.security.swap.SwapSecurity;

/**
 * This visitor returns true if the FinancialSecurity is market traded. 
 * In our context, this means that a MarketDataRequirementNames.MARKET_VALUE is available.
 * When "Security Market Price" is chosen in a View Configuration, (ValueRequirementNames.SECURITY_MARKET_PRICE)
 * SecurityMarketPriceFunction provides the price.
 */
public class MarketSecurityVisitor implements FinancialSecurityVisitor<Boolean> {

  @Override
  public Boolean visitBondSecurity(BondSecurity security) {
    return false;
  }

  @Override
  public Boolean visitCashSecurity(CashSecurity security) {
    return false;
  }

  @Override
  public Boolean visitEquitySecurity(EquitySecurity security) {
    return true;
  }

  @Override
  public Boolean visitFRASecurity(FRASecurity security) {
    return false;
  }

  @Override
  public Boolean visitFutureSecurity(FutureSecurity security) {
    return true;
  }

  @Override
  public Boolean visitSwapSecurity(SwapSecurity security) {
    return false;
  }

  @Override
  public Boolean visitEquityIndexOptionSecurity(EquityIndexOptionSecurity security) {
    return true;
  }

  @Override
  public Boolean visitEquityOptionSecurity(EquityOptionSecurity security) {
    return true;
  }

  @Override
  public Boolean visitEquityBarrierOptionSecurity(EquityBarrierOptionSecurity security) {
    return false;
  }

  @Override
  public Boolean visitFXOptionSecurity(FXOptionSecurity security) {
    return false;
  }

  @Override
  public Boolean visitNonDeliverableFXOptionSecurity(NonDeliverableFXOptionSecurity security) {
    return false;
  }

  @Override
  public Boolean visitSwaptionSecurity(SwaptionSecurity security) {
    return false;
  }

  @Override
  public Boolean visitIRFutureOptionSecurity(IRFutureOptionSecurity security) {
    return true;
  }

  @Override
  public Boolean visitCommodityFutureOptionSecurity(CommodityFutureOptionSecurity commodityFutureOptionSecurity) {
    return true;
  }

  @Override
  public Boolean visitEquityIndexDividendFutureOptionSecurity(EquityIndexDividendFutureOptionSecurity security) {
    return true;
  }

  @Override
  public Boolean visitFXBarrierOptionSecurity(FXBarrierOptionSecurity security) {
    return false;
  }

  @Override
  public Boolean visitFXDigitalOptionSecurity(FXDigitalOptionSecurity security) {
    return false;
  }

  @Override
  public Boolean visitNonDeliverableFXDigitalOptionSecurity(NonDeliverableFXDigitalOptionSecurity security) {
    return false;
  }

  @Override
  public Boolean visitFXForwardSecurity(FXForwardSecurity security) {
    return false;
  }

  @Override
  public Boolean visitNonDeliverableFXForwardSecurity(NonDeliverableFXForwardSecurity security) {
    return false;
  }

  @Override
  public Boolean visitCapFloorSecurity(CapFloorSecurity security) {
    return false;
  }

  @Override
  public Boolean visitCapFloorCMSSpreadSecurity(CapFloorCMSSpreadSecurity security) {
    return false;
  }

  @Override
  public Boolean visitEquityVarianceSwapSecurity(EquityVarianceSwapSecurity security) {
    return false;
  }

  @Override
  public Boolean visitSimpleZeroDepositSecurity(SimpleZeroDepositSecurity security) {
    return false;
  }

  @Override
  public Boolean visitPeriodicZeroDepositSecurity(PeriodicZeroDepositSecurity security) {
    return false;
  }

  @Override
  public Boolean visitContinuousZeroDepositSecurity(ContinuousZeroDepositSecurity security) {
    return false;
  }

}
