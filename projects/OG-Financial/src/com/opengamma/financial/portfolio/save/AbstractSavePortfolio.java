/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.portfolio.save;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
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
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;

/**
 * Basic function to save a portfolio into a portfolio master and the positions contained into a position master.
 */
public abstract class AbstractSavePortfolio {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractSavePortfolio.class);

  private final PortfolioMaster _portfolios;
  private final PositionMaster _positions;
  private final Map<UniqueId, ObjectId> _positionMap = new HashMap<UniqueId, ObjectId>();
  private final boolean _rewriteExistingPositions;

  protected AbstractSavePortfolio(final PortfolioMaster portfolios, final PositionMaster positions) {
    this(portfolios, positions, false);
  }
  
  protected AbstractSavePortfolio(final PortfolioMaster portfolios, final PositionMaster positions, final boolean rewriteExistingPositions) {
    _portfolios = portfolios;
    _positions = positions;
    _rewriteExistingPositions = rewriteExistingPositions;
  }

  protected ExternalIdBundle filterSecurityKey(final ExternalIdBundle securityKey) {
    return securityKey;
  }

  protected ExternalIdBundle mapSecurityKey(final ExternalIdBundle securityKey) {
    return null;
  }

  protected ManageablePosition createManageablePosition(final Position position) {
    final ManageablePosition manageablePosition = new ManageablePosition();
    manageablePosition.setQuantity(position.getQuantity());
    final ExternalIdBundle securityKey = filterSecurityKey(position.getSecurityLink().getExternalId());
    manageablePosition.getSecurityLink().setExternalId(securityKey);
    final Set<Trade> trades = position.getTrades();
    final List<ManageableTrade> manageableTrades = new ArrayList<ManageableTrade>(trades.size());
    for (Trade trade : trades) {
      final ManageableTrade mtrade = new ManageableTrade(trade);
      final ExternalIdBundle replacementKey = mapSecurityKey(mtrade.getSecurityLink().getExternalId());
      if (replacementKey != null) {
        mtrade.getSecurityLink().setExternalId(replacementKey);
      }
      manageableTrades.add(mtrade);
    }
    manageablePosition.setTrades(manageableTrades);
    manageablePosition.setProviderId(position.getUniqueId().toExternalId());
    return manageablePosition;
  }

  protected ObjectId mapPositionIdentifier(final Position position) {
    ObjectId id = _positionMap.get(position.getUniqueId());
    if (id == null) {
      if (!_rewriteExistingPositions) {
        final PositionSearchRequest searchRequest = new PositionSearchRequest();
        searchRequest.setPositionProviderId(position.getUniqueId().toExternalId());
        final PositionSearchResult searchResult = _positions.search(searchRequest);
        if (searchResult.getFirstPosition() != null) {
          id = searchResult.getFirstPosition().getUniqueId().getObjectId();
          s_logger.debug("Found position {} in master at {}", position, id);
        }
      }
      if (id == null) {
        s_logger.debug("Adding position {} to master", position);
        id = _positions.add(new PositionDocument(createManageablePosition(position))).getUniqueId().getObjectId();
      }
      _positionMap.put(position.getUniqueId(), id);
    } else {
      s_logger.debug("Position {} already in master at {}", position, id);
    }
    return id;
  }

  private ManageablePortfolioNode createManageablePortfolioNode(final PortfolioNode node) {
    final ManageablePortfolioNode manageableNode = new ManageablePortfolioNode();
    manageableNode.setName(node.getName());
    final List<PortfolioNode> childNodes = node.getChildNodes();
    final List<ManageablePortfolioNode> manageableChildNodes = new ArrayList<ManageablePortfolioNode>(childNodes.size());
    // TODO: put a hook here so a sub-class can choose to flatten the portfolio if it wishes
    for (PortfolioNode childNode : childNodes) {
      manageableChildNodes.add(createManageablePortfolioNode(childNode));
    }
    manageableNode.setChildNodes(manageableChildNodes);
    final List<Position> positions = node.getPositions();
    final List<ObjectId> positionIdentifiers = new ArrayList<ObjectId>(positions.size());
    for (Position position : positions) {
      positionIdentifiers.add(mapPositionIdentifier(position));
    }
    manageableNode.setPositionIds(positionIdentifiers);
    return manageableNode;
  }

  private ManageablePortfolio createManageablePortfolio(final Portfolio portfolio) {
    final ManageablePortfolio manageablePortfolio = new ManageablePortfolio();
    manageablePortfolio.setName(getPortfolioName(portfolio));
    manageablePortfolio.setRootNode(createManageablePortfolioNode(portfolio.getRootNode()));
    return manageablePortfolio;
  }

  protected String getPortfolioName(final Portfolio portfolio) {
    return portfolio.getName();
  }

  private boolean nodesEqual(final ManageablePortfolioNode node1, final ManageablePortfolioNode node2) {
    if (!ObjectUtils.equals(node1.getName(), node2.getName())) {
      return false;
    }
    final List<ManageablePortfolioNode> children1 = node1.getChildNodes(), children2 = node2.getChildNodes();
    if (children1.size() != children2.size()) {
      return false;
    }
    if (!ObjectUtils.equals(node1.getPositionIds(), node2.getPositionIds())) {
      return false;
    }
    final Iterator<ManageablePortfolioNode> itr1 = children1.iterator(), itr2 = children2.iterator();
    while (itr1.hasNext() && itr2.hasNext()) {
      if (!nodesEqual(itr1.next(), itr2.next())) {
        return false;
      }
    }
    return true;
  }

  public UniqueId savePortfolio(final Portfolio portfolio, final boolean updateMatchingName) {
    s_logger.debug("Saving portfolio '{}'", portfolio.getName());
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setName(getPortfolioName(portfolio));
    final PortfolioSearchResult result = _portfolios.search(request);
    final ManageablePortfolio manageablePortfolio = createManageablePortfolio(portfolio);
    PortfolioDocument document;
    if (updateMatchingName) {
      document = result.getFirstDocument();
    } else {
      document = null;
      for (PortfolioDocument resultDocument : result.getDocuments()) {
        final ManageablePortfolio resultPortfolio = resultDocument.getPortfolio();
        if (manageablePortfolio.getName().equals(resultPortfolio.getName()) && nodesEqual(manageablePortfolio.getRootNode(), resultPortfolio.getRootNode())) {
          s_logger.debug("Found existing match at {}", resultDocument.getUniqueId());
          return resultDocument.getUniqueId();
        }
      }
    }
    if (document == null) {
      s_logger.debug("Adding to master");
      document = new PortfolioDocument(manageablePortfolio);
      document = _portfolios.add(document);
    } else {
      s_logger.debug("Updating {} within master", document.getUniqueId());
      document.setPortfolio(manageablePortfolio);
      document = _portfolios.update(document);
    }
    s_logger.info("Portfolio '{}' saved as {}", manageablePortfolio.getName(), document.getUniqueId());
    return document.getUniqueId();
  }

}
