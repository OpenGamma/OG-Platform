/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
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
  public Boolean visitEquitySecurity(final EquitySecurity security) {
    return true;
  }

  @Override
  public Boolean visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
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
  public Boolean visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity security) {
    return true;
  }
}
