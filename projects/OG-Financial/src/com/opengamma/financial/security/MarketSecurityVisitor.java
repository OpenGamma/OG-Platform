/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.option.*;

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
  public Boolean visitEquitySecurity(EquitySecurity security) {
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
}
