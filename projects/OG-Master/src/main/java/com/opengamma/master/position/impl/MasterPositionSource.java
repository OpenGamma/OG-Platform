/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position.impl;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.AggregatingChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;

/**
 * A {@code PositionSource} implemented using an underlying {@code PositionMaster} and {@code PortfolioMaster}.
 * <p>
 * The {@link PositionSource} interface provides portfolio and position to the engine via a narrow API. This class provides the source on top of a standard {@link PortfolioMaster} and
 * {@link PositionMaster}.
 */
@PublicSPI
public class MasterPositionSource implements PositionSource {
  // TODO: This still needs work re versioning, as it crosses the boundary between two masters

  private static final Logger s_logger = LoggerFactory.getLogger(MasterPositionSource.class);

  /**
   * The portfolio master.
   */
  private final PortfolioMaster _portfolioMaster;
  /**
   * The position master.
   */
  private final PositionMaster _positionMaster;
  /**
   * The change manager (aggregate from both underlying masters).
   */
  private final ChangeManager _changeManager;

  /**
   * Creates an instance with underlying masters which does not override versions.
   * 
   * @param portfolioMaster  the portfolio master, not null
   * @param positionMaster  the position master, not null
   */
  public MasterPositionSource(final PortfolioMaster portfolioMaster, final PositionMaster positionMaster) {
    ArgumentChecker.notNull(portfolioMaster, "portfolioMaster");
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    _portfolioMaster = portfolioMaster;
    _positionMaster = positionMaster;
    _changeManager = new AggregatingChangeManager(Arrays.<ChangeProvider>asList(portfolioMaster, positionMaster));
  }

  /**
   * Gets the underlying portfolio master.
   * 
   * @return the portfolio master, not null
   */
  public PortfolioMaster getPortfolioMaster() {
    return _portfolioMaster;
  }

  /**
   * Gets the underlying position master.
   * 
   * @return the position master, not null
   */
  public PositionMaster getPositionMaster() {
    return _positionMaster;
  }

  @Override
  public Portfolio getPortfolio(final UniqueId uniqueId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    final ManageablePortfolio manPrt = getPortfolioMaster().get(uniqueId).getPortfolio();
    SimplePortfolio prt = new SimplePortfolio(manPrt.getUniqueId(), manPrt.getName());
    convertNode(manPrt.getRootNode(), prt.getRootNode(), versionCorrection);
    copyAttributes(manPrt, prt);
    return prt;
  }

  private void copyAttributes(ManageablePortfolio manPrt, SimplePortfolio prt) {
    if (manPrt.getAttributes() != null) {
      for (Entry<String, String> entry : manPrt.getAttributes().entrySet()) {
        if (entry.getKey() != null && entry.getValue() != null) {
          prt.addAttribute(entry.getKey(), entry.getValue());
        }
      }
    }
  }

  @Override
  public Portfolio getPortfolio(ObjectId objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ManageablePortfolio manPrt = getPortfolioMaster().get(objectId, versionCorrection).getPortfolio();
    SimplePortfolio prt = new SimplePortfolio(manPrt.getUniqueId(), manPrt.getName());
    convertNode(manPrt.getRootNode(), prt.getRootNode(), versionCorrection);
    copyAttributes(manPrt, prt);
    return prt;
  }

  @Override
  public PortfolioNode getPortfolioNode(final UniqueId uniqueId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    final ManageablePortfolioNode manNode = getPortfolioMaster().getNode(uniqueId);
    if (manNode == null) {
      throw new DataNotFoundException("Unable to find node: " + uniqueId);
    }
    SimplePortfolioNode node = new SimplePortfolioNode();
    convertNode(manNode, node, versionCorrection);
    return node;
  }

  @Override
  public Position getPosition(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    final ManageablePosition manPos = getPositionMaster().get(uniqueId).getPosition();
    if (manPos == null) {
      throw new DataNotFoundException("Unable to find position: " + uniqueId);
    }
    return manPos.toPosition();
  }

  @Override
  public Position getPosition(final ObjectId objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final ManageablePosition position = getPositionMaster().get(objectId, versionCorrection).getPosition();
    if (position == null) {
      throw new DataNotFoundException("Unable to find position: " + objectId + " at " + versionCorrection);
    }
    return position.toPosition();
  }

  @Override
  public Trade getTrade(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    final ManageableTrade manTrade = getPositionMaster().getTrade(uniqueId);
    if (manTrade == null) {
      throw new DataNotFoundException("Unable to find trade: " + uniqueId);
    }
    return manTrade;
  }

  private static int populatePositionSearchRequest(final PositionSearchRequest positionSearch, final ManageablePortfolioNode node) {
    int count = 0;
    for (ObjectId positionId : node.getPositionIds()) {
      positionSearch.addPositionObjectId(positionId);
      count++;
    }
    for (ManageablePortfolioNode child : node.getChildNodes()) {
      count += populatePositionSearchRequest(positionSearch, child);
    }
    return count;
  }

  /**
   * Converts a manageable node to a source node.
   * 
   * @param manNode the manageable node, not null
   * @param sourceNode the source node, not null
   * @param versionCorrection the version/correction time for resolving the constituent positions, not null
   */
  protected void convertNode(final ManageablePortfolioNode manNode, final SimplePortfolioNode sourceNode, final VersionCorrection versionCorrection) {
    PositionSearchRequest positionSearch = new PositionSearchRequest();
    final Map<ObjectId, ManageablePosition> positionCache;
    final int positionCount = populatePositionSearchRequest(positionSearch, manNode);
    if (positionCount > 0) {
      positionCache = Maps.newHashMapWithExpectedSize(positionCount);
      positionSearch.setVersionCorrection(versionCorrection);
      final PositionSearchResult positions = getPositionMaster().search(positionSearch);
      for (PositionDocument position : positions.getDocuments()) {
        positionCache.put(position.getObjectId(), position.getPosition());
      }
    } else {
      positionCache = null;
    }
    convertNode(manNode, sourceNode, positionCache);
  }

  /**
   * Converts a manageable node to a source node.
   * 
   * @param manNode the manageable node, not null
   * @param sourceNode the source node, not null
   * @param positionCache the positions, not null
   */
  protected void convertNode(final ManageablePortfolioNode manNode, final SimplePortfolioNode sourceNode, final Map<ObjectId, ManageablePosition> positionCache) {
    final UniqueId nodeId = manNode.getUniqueId();
    sourceNode.setUniqueId(nodeId);
    sourceNode.setName(manNode.getName());
    sourceNode.setParentNodeId(manNode.getParentNodeId());
    if (manNode.getPositionIds().size() > 0) {
      for (ObjectId positionId : manNode.getPositionIds()) {
        final ManageablePosition foundPosition = positionCache.get(positionId);
        if (foundPosition != null) {
          sourceNode.addPosition(foundPosition.toPosition());
        } else {
          s_logger.warn("Position {} not found for portfolio node {}", positionId, nodeId);
        }
      }
    }
    for (ManageablePortfolioNode child : manNode.getChildNodes()) {
      SimplePortfolioNode childNode = new SimplePortfolioNode();
      convertNode(child, childNode, positionCache);
      sourceNode.addChildNode(childNode);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getPortfolioMaster() + "," + getPositionMaster() + "]";
  }

}
