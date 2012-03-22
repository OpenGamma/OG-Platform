/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.integration.loadsave.portfolio;

import com.opengamma.integration.tool.IntegrationToolContext;

/**
 * TODO
 * Reads in a sheet where each row specifies a Bloomberg ticker and a quantity and produces a portfolio
 * containing positions in the referenced securities. Related time series (such as PX_LAST, and normally 
 * supplied on the tool's command line) will be pre-loaded from Bloomberg. If supplied, additional columns 
 * for trade date, premium and counterparty will be used to create a trade under each position.
 */
public class ExchangeTradedPortfolioLoader {

  public void run(String portfolioName, String fileName, String dataSource, String dataProvider, 
      String dataField, String observationTime, boolean persist, IntegrationToolContext toolContext) {
  }

}
