/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
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
 * In our context, this means that a MarketDataRequirementNames.MARKET_VALUE is available.
 * When "Security Market Price" is chosen in a View Configuration, (ValueRequirementNames.SECURITY_MARKET_PRICE)
 * SecurityMarketPriceFunction provides the price.
 */
public class MarketSecurityVisitor extends FinancialSecurityVisitorSameValueAdapter<Boolean> {

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
}
