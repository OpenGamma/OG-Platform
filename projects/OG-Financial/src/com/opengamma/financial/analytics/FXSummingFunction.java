/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Collections;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;

/**
 * Temporary hack to allow propagation of aggregates in mixed portfolios until [PLAT-366] is resolved.
 */
public class FXSummingFunction extends FilteringSummingFunction implements FinancialSecurityVisitor<Boolean> {

  public FXSummingFunction(String valueName) {
    super(valueName, Collections.<String>emptySet());
  }
  
  @Override
  protected boolean isIncluded(FinancialSecurity security, ValueProperties filterProperties) {
    return security.accept(this);
  }
  
  //-------------------------------------------------------------------------
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
    return false;
  }

  @Override
  public Boolean visitFRASecurity(FRASecurity security) {
    return false;
  }

  @Override
  public Boolean visitFutureSecurity(FutureSecurity security) {
    return false;
  }

  @Override
  public Boolean visitSwapSecurity(SwapSecurity security) {
    return false;
  }

  @Override
  public Boolean visitEquityIndexOptionSecurity(EquityIndexOptionSecurity security) {
    return false;
  }

  @Override
  public Boolean visitEquityOptionSecurity(EquityOptionSecurity security) {
    return false;
  }

  @Override
  public Boolean visitFXOptionSecurity(FXOptionSecurity security) {
    return true;
  }

  @Override
  public Boolean visitSwaptionSecurity(SwaptionSecurity security) {
    return false;
  }

  @Override
  public Boolean visitIRFutureOptionSecurity(IRFutureOptionSecurity security) {
    return false;
  }

  @Override
  public Boolean visitFXBarrierOptionSecurity(FXBarrierOptionSecurity security) {
    return true;
  }

  @Override
  public Boolean visitFXSecurity(FXSecurity security) {
    return false;
  }

  @Override
  public Boolean visitFXForwardSecurity(FXForwardSecurity security) {
    return true;
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

}
