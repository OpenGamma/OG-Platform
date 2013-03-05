/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.conversion;

import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.opengamma.integration.tool.portfolio.xml.PortfolioDocumentConverter;
import com.opengamma.integration.tool.portfolio.xml.VersionedPortfolioHandler;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.Portfolio;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.PortfolioDocumentV1_0;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.Position;

public class PortfolioDocumentConverterV1_0 implements PortfolioDocumentConverter {

  @Override
  public Iterable<VersionedPortfolioHandler> convert(Object content) {

    PortfolioDocumentV1_0 portfolioDocument = (PortfolioDocumentV1_0) content;

    Iterable<Portfolio> portfolios = extractPortfolios(portfolioDocument);

    return Iterables.transform(portfolios, new Function<Portfolio, VersionedPortfolioHandler>() {
      @Override
      public VersionedPortfolioHandler apply(final Portfolio portfolio) {
        return new VersionedPortfolioHandler(portfolio.getName(), new PortfolioConverter(portfolio).getPositions().iterator());
      }
    });
  }

  private Iterable<Portfolio> extractPortfolios(PortfolioDocumentV1_0 portfolioDocument) {

    Set<Portfolio> portfolios = portfolioDocument.getPortfolios();

    return portfolios == null || portfolios.isEmpty() ?
            // File didn't have a portfolio, so create one
            ImmutableList.of(createDummyPortfolio(portfolioDocument)) :
            portfolios;
  }

  private Portfolio createDummyPortfolio(PortfolioDocumentV1_0 portfolioDocument) {

    Portfolio pf = new Portfolio();

    // We leave the portfolio unnamed so a name can be provided from the command line

    // If we have trades with no positions, just add them to the dummy portfolio. If we have positions, just use them
    Set<Position> positions = portfolioDocument.getPositions();

    if (positions == null || positions.isEmpty()) {
      pf.setTrades(portfolioDocument.getTrades());
    } else {
      pf.setPositions(positions);
    }
    return pf;
  }
}
