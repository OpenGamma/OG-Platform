/*
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.financial.security;

import com.opengamma.core.security.Security;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.FederalFundsFutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;

/**
 * Is this security exchange traded?
 */
public class ExchangeTradedVisitor extends FinancialSecurityVisitorSameValueAdapter<Boolean> {

  public ExchangeTradedVisitor() {
    super(false);
  }

  @Override
  public Boolean visitEquitySecurity(EquitySecurity security) {
    return true;
  }

  @Override
  public Boolean visitAgricultureFutureSecurity(final AgricultureFutureSecurity security) {
    return true;
  }

  @Override
  public Boolean visitBondFutureSecurity(final BondFutureSecurity security) {
    return true;
  }

  @Override
  public Boolean visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
    return true;
  }

  @Override
  public Boolean visitEquityFutureSecurity(final EquityFutureSecurity security) {
    return true;
  }

  @Override
  public Boolean visitEquityIndexDividendFutureSecurity(final EquityIndexDividendFutureSecurity security) {
    return true;
  }

  @Override
  public Boolean visitFXFutureSecurity(final FXFutureSecurity security) {
    return true;
  }

  @Override
  public Boolean visitIndexFutureSecurity(final IndexFutureSecurity security) {
    return true;
  }

  @Override
  public Boolean visitMetalFutureSecurity(final MetalFutureSecurity security) {
    return true;
  }

  @Override
  public Boolean visitStockFutureSecurity(final StockFutureSecurity security) {
    return true;
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
    return true;
  }

  @Override
  public Boolean visitBondFutureOptionSecurity(BondFutureOptionSecurity security) {
    return true;
  }

  @Override
  public Boolean visitEquityIndexFutureOptionSecurity(EquityIndexFutureOptionSecurity security) {
    return true;
  }

  @Override
  public Boolean visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
    return true;
  }

  @Override
  public Boolean visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
    return true;
  }

  @Override
  public Boolean visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity security) {
    return true;
  }

}