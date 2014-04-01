/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.id.UniqueId;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecurityMasterUtils;
import com.opengamma.util.ArgumentChecker;

/**
 * Persists a portfolio (and its associated securities, positions and
 * trades) into the database masters.
 */
public class PortfolioWriter {

  /**
   * Logger for the class.
   */
  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioWriter.class);

  /**
   * Indicates if the data should actually be written to the masters
   * or whether this is a dry run.
   */
  private final boolean _write;

  private final PortfolioMaster _portfolioMaster;
  private final PositionMaster _positionMaster;
  private final SecurityMaster _securityMaster;

  /**
   * Create a new persister, initialized so that it is ready to
   * write portfolios as required.
   *
   * @param write should the data actually be written to the masters
   * @param portfolioMaster the portfolio master
   * @param positionMaster the position master
   * @param securityMaster the security master
   */
  public PortfolioWriter(boolean write, PortfolioMaster portfolioMaster,
                         PositionMaster positionMaster, SecurityMaster securityMaster) {
    _write = write;
    _portfolioMaster = ArgumentChecker.notNull(portfolioMaster, "portfolioMaster");
    _positionMaster = ArgumentChecker.notNull(positionMaster, "positionMaster");
    _securityMaster = ArgumentChecker.notNull(securityMaster, "securityMaster");
  }

  /**
   * Write the portfolio and supporting securities to the masters.
   *
   * @param portfolio the portfolio to be written, not null
   * @param securities the securities associated with the portfolio (some
   * of which may already exist in the security master)
   */
  public void write(Portfolio portfolio, Set<ManageableSecurity> securities) {

    persistSecurities(securities);
    persistPortfolio(portfolio);
  }

  // Note that if we are passed an OTC security for which we've auto-generated an
  // external id, then there is no way for it to be updated in case it contained
  // incorrect data. If updates are required then when passed the security needs
  // to have an external id (which could well be the external trade id)
  private void persistSecurities(Set<ManageableSecurity> securities) {

    for (ManageableSecurity security : securities) {

      if (security.getExternalIdBundle().isEmpty()) {
        throw new OpenGammaRuntimeException("Unable to persist security with no external id: " + security);
      }

      if (_write) {
        final ManageableSecurity updated = SecurityMasterUtils.addOrUpdateSecurity(_securityMaster, security);
        if (updated == null) {
          throw new OpenGammaRuntimeException("Error persisting security: " + security);
        }
      }
      s_logger.info("Successfully processed security: {}", security);
    }
  }

  private void persistPortfolio(Portfolio portfolio) {
    ManageablePortfolio currentPortfolio = findCurrentPortfolio(portfolio);
    insertPortfolio(portfolio, currentPortfolio == null ? null : currentPortfolio.getUniqueId());
  }

  private ManageablePortfolio findCurrentPortfolio(Portfolio portfolio) {

    PortfolioSearchRequest searchRequest = new PortfolioSearchRequest();
    String portfolioName = portfolio.getName();
    searchRequest.setName(portfolioName);
    PortfolioSearchResult result = _portfolioMaster.search(searchRequest);

    List<ManageablePortfolio> portfolios = result.getPortfolios();

    int size = portfolios.size();
    if (size > 1) {
      s_logger.warn("More than one portfolio found with name: {} - using first found", portfolioName);
    }

    return size == 0 ? null : portfolios.get(0);
  }

  private void insertPortfolio(Portfolio portfolio, UniqueId previousId) {

    List<ManageablePosition> positions = persistPositions(portfolio);

    String portfolioName = portfolio.getName();

    PortfolioDocument portfolioDocument = new PortfolioDocument();
    portfolioDocument.setPortfolio(createPortfolio(portfolioName, positions));
    if (previousId != null) {
      portfolioDocument.setUniqueId(previousId);
    }

    if (_write) {
      _portfolioMaster.add(portfolioDocument);
    }
    s_logger.info("Created portfolio with name: {}", portfolioName);
  }

  private List<ManageablePosition> persistPositions(Portfolio portfolio) {

    List<ManageablePosition> added = new ArrayList<>();
    for (Position position : portfolio.getRootNode().getPositions()) {


      if (position.getSecurityLink() == null || position.getSecurityLink().getExternalId().isEmpty()) {
        throw new OpenGammaRuntimeException("Unable to persist position with no security external id: " + position);
      }

      ManageablePosition manageablePosition = new ManageablePosition(position.getQuantity(),
                                                                     position.getSecurityLink().getExternalId());
      manageablePosition.setAttributes(position.getAttributes());
      manageablePosition.setTrades(convertTrades(position));

      if (_write) {
        PositionDocument addedDoc = _positionMaster.add(new PositionDocument(manageablePosition));
        added.add(addedDoc.getPosition());
      }
      s_logger.info("Added position {}", position);
    }

    // Return the new position which will have position ids which can
    // be referenced by portfolio nodes
    return added;
  }

  private List<ManageableTrade> convertTrades(Position position) {

    List<ManageableTrade> converted = new ArrayList<>();
    for (Trade trade : position.getTrades()) {
      converted.add(new ManageableTrade(trade));
    }
    return converted;
  }

  private ManageablePortfolio createPortfolio(String portfolioName, List<ManageablePosition> positions) {

    ManageablePortfolioNode rootNode = new ManageablePortfolioNode(portfolioName);

    for (ManageablePosition position : positions) {
      rootNode.addPosition(position.getUniqueId());
    }

    return new ManageablePortfolio(portfolioName, rootNode);
  }
}
