/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.model.equity.SecurityMarketPriceFunction;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FxFutureOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;

/**
 * This visitor returns true if the FinancialSecurity is market traded.
 * In our context, this means that a MarketDataRequirementNames.MARKET_VALUE is available
 * for a security.<p>
 * When {@link ValueRequirementNames#SECURITY_MARKET_PRICE} is chosen in a View Configuration,
 * {@link SecurityMarketPriceFunction} provides the price.
 */
public class MarketSecurityVisitor extends FinancialSecurityVisitorSameValueAdapter<Boolean> {

  /**
   * Sets the default return value to false.
   */
  public MarketSecurityVisitor() {
    super(false);
  }

  @Override
  public Boolean visitMetalFutureSecurity(final MetalFutureSecurity security) {
    return true;
  }


  @Override
  public Boolean visitIndexFutureSecurity(final IndexFutureSecurity security) {
    return true;
  }

  @Override
  public Boolean visitEquityFutureSecurity(final EquityFutureSecurity security) {
    return true;
  }

  @Override
  public Boolean visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
    return true;
  }

  @Override
  public Boolean visitEquitySecurity(final EquitySecurity security) {
    return true;
  }

  @Override
  public Boolean visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
    return true;
  }

  @Override
  public Boolean visitEquityIndexFutureOptionSecurity(final EquityIndexFutureOptionSecurity security) {
    return true;
  }

  @Override
  public Boolean visitEquityOptionSecurity(final EquityOptionSecurity security) {
    return true;
  }

  @Override
  public Boolean visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
    return true;
  }

  @Override
  public Boolean visitCommodityFutureOptionSecurity(final CommodityFutureOptionSecurity commodityFutureOptionSecurity) {
    return true;
  }

  @Override
  public Boolean visitFxFutureOptionSecurity(final FxFutureOptionSecurity security) {
    return true;
  }

  @Override
  public Boolean visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity security) {
    return true;
  }

  @Override
  public Boolean visitBondFutureSecurity(final BondFutureSecurity security) {
    return true;
  }

  @Override
  public Boolean visitCorporateBondSecurity(final CorporateBondSecurity security) {
    return true;
  }

  @Override
  public Boolean visitGovernmentBondSecurity(final GovernmentBondSecurity security) {
    return true;
  }

  @Override
  public Boolean visitMunicipalBondSecurity(final MunicipalBondSecurity security) {
    return true;
  }
  
  @Override
  public Boolean visitEquityIndexDividendFutureSecurity(final EquityIndexDividendFutureSecurity security) {
    return true;
  }
}
