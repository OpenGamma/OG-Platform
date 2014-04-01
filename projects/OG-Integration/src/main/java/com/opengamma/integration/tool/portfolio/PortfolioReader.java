/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.integration.copier.portfolio.reader.PositionReader;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * A wrapper around the PositionReader interface that allows
 * a complete portfolio to be build from the data supplied
 * by the reader. This then allows the persistence of the
 * portfolio in a much more straightforward way.
 */
// todo - not the best name, but more appropriate ones are taken
public class PortfolioReader {

  /**
   * The reader for the position data.
   */
  private final PositionReader _positionReader;

  /**
   * The name for the portfolio, if not supplied by the portfolio
   * itself (via the reader).
   */
  private final String _portfolioName;

  /**
   * Create a portfolio builder for the supplied reader.
   * @param positionReader the reader to get the portfolio data from
   * @param name the name for the portfolio, if the portfolio
   * itself does not supply one
   */
  public PortfolioReader(PositionReader positionReader, String name) {
    _positionReader = ArgumentChecker.notNull(positionReader, "positionReader");
    _portfolioName = ArgumentChecker.notNull(name, "name");
  }

  /**
   * Create the portfolio and the set of securities it contains.
   *
   * @return a pair containing the portfolio and the set of securities it contains
   */
  public Pair<Portfolio, Set<ManageableSecurity>> createPortfolio() {

    // Can't create a ManageablePortfolio as that does not hold positions
    // just object ids which we clearly don't have at this point
    SimplePortfolioNode rootNode = new SimplePortfolioNode(_portfolioName);
    Portfolio portfolio = new SimplePortfolio(_portfolioName, rootNode);

    // We don't particularly care which securities are from which node as
    // the positions contain the id bundles so we can easily reconstruct
    Set<ManageableSecurity> securities = new HashSet<>();

    ObjectsPair<ManageablePosition, ManageableSecurity[]> positionData;
    while ((positionData = _positionReader.readNext()) != null) {

      ManageablePosition manageablePosition = positionData.getFirst();

      ManageableSecurity security = positionData.getSecond()[0];
      if (security.getExternalIdBundle().isEmpty()) {
        security.setExternalIdBundle(generateNewId());
      }

      Position position = convertPosition(manageablePosition, security);

      for (ManageableSecurity sec : positionData.getSecond()) {
        securities.add(sec);
      }

      rootNode.addPosition(position);
    }

    _positionReader.close();

    return Pairs.of(portfolio, securities);
  }

  private ExternalIdBundle generateNewId() {

    String id = UUID.randomUUID().toString();
    return ExternalIdBundle.of("OG_GENERATED_ID", id);
  }

  private Position convertPosition(ManageablePosition position, ManageableSecurity security) {

    SimplePosition posn = new SimplePosition();
    posn.setQuantity(position.getQuantity());
    posn.setSecurityLink(new ManageableSecurityLink(security.getExternalIdBundle()));

    for (ManageableTrade trade : position.getTrades()) {
      posn.addTrade(trade);
    }
    posn.setAttributes(position.getAttributes());

    return posn;
  }
}
