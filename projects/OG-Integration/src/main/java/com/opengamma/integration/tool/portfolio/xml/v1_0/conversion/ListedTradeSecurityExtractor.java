/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.conversion;

import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.ListedSecurityDefinition;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.ListedSecurityTrade;
import com.opengamma.master.security.ManageableSecurity;

/**
 * Security extractor for listed security trades.
 */
public class ListedTradeSecurityExtractor extends TradeSecurityExtractor<ListedSecurityTrade> {

  /**
   * Create a security extractor for the supplied trade.
   *
   * @param trade the trade to perform extraction on
   */
  public ListedTradeSecurityExtractor(ListedSecurityTrade trade) {
    super(trade);
  }

  //-------------------------------------------------------------------------
  @Override
  public ManageableSecurity[] extractSecurities() {
    // Security extraction is handled by the listed security definition
    ListedSecurityDefinition listedSecurity = getTrade().getListedSecurityDefinition();
    return listedSecurity.getSecurityExtractor().extract();
  }

}
