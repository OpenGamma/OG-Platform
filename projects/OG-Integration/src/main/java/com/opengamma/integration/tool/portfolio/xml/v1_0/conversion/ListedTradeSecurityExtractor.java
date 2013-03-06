/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma
 group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.conversion;

import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.ListedSecurityDefinition;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.ListedSecurityTrade;
import com.opengamma.master.security.ManageableSecurity;

public class ListedTradeSecurityExtractor extends TradeSecurityExtractor<ListedSecurityTrade> {

  @Override
  public ManageableSecurity[] extractSecurity(ListedSecurityTrade trade) {

    ListedSecurityDefinition listedSecurity = trade.getListedSecurityDefinition();
    return listedSecurity.getSecurityExtractor().extract();
  }
}
