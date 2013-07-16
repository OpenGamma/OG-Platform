/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.conversion;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.opengamma.integration.tool.portfolio.xml.PortfolioDocumentConverter;
import com.opengamma.integration.tool.portfolio.xml.PortfolioPosition;
import com.opengamma.integration.tool.portfolio.xml.VersionedPortfolioHandler;
import com.opengamma.integration.tool.portfolio.xml.XmlExternalIdValidator;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.Portfolio;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.PortfolioDocumentV1_0;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.Position;

/**
 * Converts version 1.0 portfolio data to a generic form that can be used
 * to load data into the rest of the system.
 */
public class PortfolioDocumentConverterV1_0  // CSIGNORE underscore in class name
    implements PortfolioDocumentConverter<PortfolioDocumentV1_0> {

  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioDocumentConverterV1_0.class);

  @Override
  public Iterable<VersionedPortfolioHandler> convert(PortfolioDocumentV1_0 portfolioDocument) {

    Iterable<Portfolio> portfolios = extractPortfolios(portfolioDocument);

    Iterable<VersionedPortfolioHandler> transformed =
        Iterables.transform(portfolios, new PortfolioExtractor());

    // Portfolios with errors will leave nulls in the iterable which we need to remove
    return Iterables.filter(transformed, new Predicate<VersionedPortfolioHandler>() {
      @Override
      public boolean apply(VersionedPortfolioHandler versionedPortfolioHandler) {
        return versionedPortfolioHandler != null;
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

  /**
   * Create a dummy portfolio for the case where no portfolio was specified in the file.
   *
   * @param portfolioDocument the portfolio document to create a dummy portfolio for
   * @return the dummy portfolio
   */
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

  /**
   * Extracts the details from a portfolio in an XML document. If the portfolio
   * fails validation then null is returned.
   */
  private static class PortfolioExtractor implements Function<Portfolio, VersionedPortfolioHandler> {

    private XmlExternalIdValidator _xmlExternalIdValidator = new XmlExternalIdValidator();

    /**
     * Extracts the details from the supplied portfolio.
     *
     * @param portfolio the portfolio to extract details (positions, trades, securities etc)
     * @return the extracted VersionedPortfolioHandler if extraction is possible, null otherwise
     */
    @Override
    public VersionedPortfolioHandler apply(final Portfolio portfolio) {
      try {
        Iterable<PortfolioPosition> positions = new PortfolioConverter(portfolio, _xmlExternalIdValidator).getPositions();
        return new VersionedPortfolioHandler(
            portfolio.getName(),
            positions);

      } catch (PortfolioParsingException e) {
        s_logger.error("Unable to parse portfolio [" + portfolio.getName() + "] - it will be skipped", e);
        return null;
      }
    }
  }
}
