/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.position.impl;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.core.change.PassthroughChangeManager;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;

/**
 * An abstract PositionSource built on top of an underlying master with possibly a PositionMaster and or PositionSource to
 * resolve the positions in the portfolio.
 * 
 */
@PublicSPI
public abstract class AbstractMasterPositionSource implements PositionSource {
  
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractMasterPositionSource.class);
  
  private final PortfolioMaster _portfolioMaster;
  
  public AbstractMasterPositionSource(PortfolioMaster portfolioMaster) {
    ArgumentChecker.notNull(portfolioMaster, "portfolioMaster");
    
    _portfolioMaster = portfolioMaster;
  }
  
  /**
   * Get collection of Position referenced in the Portfolio.
   * 
   * @param positionSearch the positionSearch populated with positionIds referenced in the portfolio.
   * @return the collection of positions referenced in the portfolio, not-null.
   */
  protected abstract Collection<Position> positions(PositionSearchRequest positionSearch);
  
  protected abstract ChangeProvider[] changeProviders();

  @Override
  public ChangeManager changeManager() {
    ChangeProvider[] changeProviders = changeProviders();
    if (changeProviders != null) {
      return new PassthroughChangeManager(changeProviders());
    } else {
      return DummyChangeManager.INSTANCE;
    }
  }
  
  @Override
  public Portfolio getPortfolio(UniqueId uniqueId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    final ManageablePortfolio manPrt = getPortfolioMaster().get(uniqueId).getPortfolio();
    final SimplePortfolio prt = new SimplePortfolio(manPrt.getUniqueId(), manPrt.getName());
    convertNode(manPrt.getRootNode(), prt.getRootNode(), versionCorrection);
    copyAttributes(manPrt, prt);
    return prt;
  }
  
  /**
   * Converts a manageable node to a source node.
   *
   * @param manNode the manageable node, not null
   * @param sourceNode the source node, not null
   * @param versionCorrection the version/correction time for resolving the constituent positions, not null
   */
  protected void convertNode(final ManageablePortfolioNode manNode, final SimplePortfolioNode sourceNode, final VersionCorrection versionCorrection) {
    final PositionSearchRequest positionSearch = new PositionSearchRequest();
    final Map<ObjectId, Position> positionCache;
    final int positionCount = populatePositionSearchRequest(positionSearch, manNode);
    if (positionCount > 0) {
      positionCache = Maps.newHashMapWithExpectedSize(positionCount);
      positionSearch.setVersionCorrection(versionCorrection);
      Collection<Position> positions = positions(positionSearch);
      if (positions != null) {
        for (Position position : positions) {
          positionCache.put(position.getUniqueId().getObjectId(), position);
        }
      }
    } else {
      positionCache = null;
    }
    convertNode(manNode, sourceNode, positionCache);
  }
  
  
  
  private void copyAttributes(final ManageablePortfolio manPrt, final SimplePortfolio prt) {
    if (manPrt.getAttributes() != null) {
      for (final Entry<String, String> entry : manPrt.getAttributes().entrySet()) {
        if (entry.getKey() != null && entry.getValue() != null) {
          prt.addAttribute(entry.getKey(), entry.getValue());
        }
      }
    }
  }
  
  private static int populatePositionSearchRequest(final PositionSearchRequest positionSearch, final ManageablePortfolioNode node) {
    int count = 0;
    for (final ObjectId positionId : node.getPositionIds()) {
      positionSearch.addPositionObjectId(positionId);
      count++;
    }
    for (final ManageablePortfolioNode child : node.getChildNodes()) {
      count += populatePositionSearchRequest(positionSearch, child);
    }
    return count;
  }
  
  /**
   * Converts a manageable node to a source node.
   *
   * @param manNode the manageable node, not null
   * @param sourceNode the source node, not null
   * @param positionCache the positions, not null
   */
  protected void convertNode(final ManageablePortfolioNode manNode, final SimplePortfolioNode sourceNode, final Map<ObjectId, Position> positionCache) {
    final UniqueId nodeId = manNode.getUniqueId();
    sourceNode.setUniqueId(nodeId);
    sourceNode.setName(manNode.getName());
    sourceNode.setParentNodeId(manNode.getParentNodeId());
    if (manNode.getPositionIds().size() > 0) {
      for (final ObjectId positionId : manNode.getPositionIds()) {
        final Position foundPosition = positionCache.get(positionId);
        if (foundPosition != null) {
          sourceNode.addPosition(foundPosition);
        } else {
          s_logger.warn("Position {} not found for portfolio node {}", positionId, nodeId);
        }
      }
    }
    for (final ManageablePortfolioNode child : manNode.getChildNodes()) {
      final SimplePortfolioNode childNode = new SimplePortfolioNode();
      convertNode(child, childNode, positionCache);
      sourceNode.addChildNode(childNode);
    }
  }

  @Override
  public Portfolio getPortfolio(ObjectId objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final ManageablePortfolio manPrt = getPortfolioMaster().get(objectId, versionCorrection).getPortfolio();
    final SimplePortfolio prt = new SimplePortfolio(manPrt.getUniqueId(), manPrt.getName());
    convertNode(manPrt.getRootNode(), prt.getRootNode(), versionCorrection);
    copyAttributes(manPrt, prt);
    return prt;
  }

  @Override
  public PortfolioNode getPortfolioNode(UniqueId uniqueId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    final ManageablePortfolioNode manNode = getPortfolioMaster().getNode(uniqueId);
    if (manNode == null) {
      throw new DataNotFoundException("Unable to find node: " + uniqueId);
    }
    final SimplePortfolioNode node = new SimplePortfolioNode();
    convertNode(manNode, node, versionCorrection);
    return node;
  }

  /**
   * Gets the portfolioMaster.
   * @return the portfolioMaster
   */
  public PortfolioMaster getPortfolioMaster() {
    return _portfolioMaster;
  }

}
