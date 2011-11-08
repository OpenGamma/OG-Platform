/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position.impl;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.VersionedSource;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;

/**
 * A {@code PositionSource} implemented using an underlying {@code PositionMaster}.
 * <p>
 * The {@link PositionSource} interface provides securities to the engine via a narrow API.
 * This class provides the source on top of a standard {@link PortfolioMaster}.
 */
@PublicSPI
public class MasterPositionSource implements PositionSource, VersionedSource {
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
   * The version-correction locator to search at, null to not override versions.
   */
  private volatile VersionCorrection _versionCorrection;

  /**
   * Creates an instance with underlying masters which does not override versions.
   * 
   * @param portfolioMaster  the portfolio master, not null
   * @param positionMaster  the position master, not null
   */
  public MasterPositionSource(final PortfolioMaster portfolioMaster, final PositionMaster positionMaster) {
    this(portfolioMaster, positionMaster, null);
  }

  /**
   * Creates an instance with underlying masters optionally overriding the requested version.
   * 
   * @param portfolioMaster  the portfolio master, not null
   * @param positionMaster  the position master, not null
   * @param versionCorrection  the version-correction locator to search at, null to not override versions
   */
  public MasterPositionSource(final PortfolioMaster portfolioMaster, final PositionMaster positionMaster, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(portfolioMaster, "portfolioMaster");
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    _portfolioMaster = portfolioMaster;
    _positionMaster = positionMaster;
    _versionCorrection = versionCorrection;
  }

  //-------------------------------------------------------------------------
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

  /**
   * Gets the version-correction locator to search at.
   * 
   * @return the version-correction locator to search at, null if not overriding versions
   */
  public VersionCorrection getVersionCorrection() {
    return _versionCorrection;
  }

  /**
   * Sets the version-correction locator to search at.
   * 
   * @param versionCorrection  the version-correction locator to search at, null to not override versions
   */
  @Override
  public void setVersionCorrection(final VersionCorrection versionCorrection) {
    _versionCorrection = versionCorrection;
  }

  //-------------------------------------------------------------------------
  @Override
  public Portfolio getPortfolio(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    final VersionCorrection vc = getVersionCorrection();  // lock against change
    ManageablePortfolio manPrt;
    try {
      if (vc != null) {
        manPrt = getPortfolioMaster().get(uniqueId, vc).getPortfolio();
      } else {
        manPrt = getPortfolioMaster().get(uniqueId).getPortfolio();
      }
    } catch (DataNotFoundException ex) {
      return null;
    }
    SimplePortfolio prt = new SimplePortfolio(manPrt.getUniqueId(), manPrt.getName());
    convertNode(manPrt.getRootNode(), prt.getRootNode());
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
    ManageablePortfolio manPrt;
    try {
      manPrt = getPortfolioMaster().get(objectId, versionCorrection).getPortfolio();
    } catch (DataNotFoundException ex) {
      return null;
    }
    SimplePortfolio prt = new SimplePortfolio(manPrt.getUniqueId(), manPrt.getName());
    convertNode(manPrt.getRootNode(), prt.getRootNode());
    copyAttributes(manPrt, prt);
    return prt;
  }

  @Override
  public PortfolioNode getPortfolioNode(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    final VersionCorrection vc = getVersionCorrection();  // lock against change
    ManageablePortfolioNode manNode;
    if (vc != null) {
      // use defined instants
      PortfolioSearchRequest portfolioSearch = new PortfolioSearchRequest();
      portfolioSearch.addNodeObjectId(uniqueId);
      portfolioSearch.setVersionCorrection(vc);
      PortfolioSearchResult portfolios = getPortfolioMaster().search(portfolioSearch);
      if (portfolios.getDocuments().size() != 1) {
        return null;
      }
      ManageablePortfolio manPrt = portfolios.getFirstPortfolio();
      manNode = manPrt.getRootNode().findNodeByObjectId(uniqueId);
    } else {
      // match by uniqueId
      try {
        manNode = getPortfolioMaster().getNode(uniqueId);
      } catch (DataNotFoundException ex) {
        return null;
      }
    }
    SimplePortfolioNode node = new SimplePortfolioNode();
    convertNode(manNode, node);
    return node;
  }

  @Override
  public Position getPosition(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    String[] schemes = StringUtils.split(uniqueId.getScheme(), '-');
    String[] values = StringUtils.split(uniqueId.getValue(), '-');
    String[] versions = StringUtils.split(uniqueId.getVersion(), '-');
    if (schemes.length != 2 || values.length != 2 || versions.length != 2) {
      throw new IllegalArgumentException("Invalid position identifier for MasterPositionSource: " + uniqueId);
    }
    UniqueId nodeId = UniqueId.of(schemes[0], values[0], versions[0]);
    UniqueId posId = UniqueId.of(schemes[1], values[1], versions[1]);
    final VersionCorrection vc = getVersionCorrection();  // lock against change
    ManageablePosition manPos;
    try {
      if (vc != null) {
        manPos = getPositionMaster().get(posId, vc).getPosition();
      } else {
        manPos = getPositionMaster().get(posId).getPosition();
      }
    } catch (DataNotFoundException ex) {
      return null;
    }
    SimplePosition pos = new SimplePosition();
    convertPosition(nodeId, manPos, pos);
    return pos;
  }

  @Override
  public Trade getTrade(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    String[] schemes = StringUtils.split(uniqueId.getScheme(), '-');
    String[] values = StringUtils.split(uniqueId.getValue(), '-');
    String[] versions = StringUtils.split(uniqueId.getVersion(), '-');
    if (schemes.length != 2 || values.length != 2 || versions.length != 2) {
      throw new IllegalArgumentException("Invalid trade identifier for MasterPositionSource: " + uniqueId);
    }
    UniqueId nodeId = versions[0].length() == 0 ? UniqueId.of(schemes[0], values[0]) : UniqueId.of(schemes[0], values[0], versions[0]);
    UniqueId tradeId = versions[0].length() == 0 ? UniqueId.of(schemes[1], values[1]) : UniqueId.of(schemes[1], values[1], versions[1]);
    final VersionCorrection vc = getVersionCorrection();  // lock against change
    ManageableTrade manTrade;
    if (vc != null) {
      // use defined instants
      PositionSearchRequest positionSearch = new PositionSearchRequest();
      positionSearch.addTradeObjectId(tradeId);
      positionSearch.setVersionCorrection(vc);
      PositionSearchResult positions = getPositionMaster().search(positionSearch);
      if (positions.getDocuments().size() != 1) {
        return null;
      }
      ManageablePosition manPos = positions.getFirstPosition();
      manTrade = manPos.getTrade(tradeId);
    } else {
      // match by uniqueId
      try {
        manTrade = getPositionMaster().getTrade(tradeId);
      } catch (DataNotFoundException ex) {
        return null;
      }
    }
    convertTrade(nodeId, convertId(manTrade.getParentPositionId(), nodeId), manTrade);
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
   */
  protected void convertNode(final ManageablePortfolioNode manNode, final SimplePortfolioNode sourceNode) {
    PositionSearchRequest positionSearch = new PositionSearchRequest();
    final Map<ObjectId, ManageablePosition> positionCache;
    final int positionCount = populatePositionSearchRequest(positionSearch, manNode);
    if (positionCount > 0) {
      positionCache = Maps.newHashMapWithExpectedSize(positionCount);
      positionSearch.setVersionCorrection(getVersionCorrection());
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
          final SimplePosition position = new SimplePosition();
          convertPosition(nodeId, foundPosition, position);
          sourceNode.addPosition(position);
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

  /**
   * Converts a manageable position to a source position
   * 
   * @param nodeId  the parent node unique identifier, null if root
   * @param manPos  the manageable position, not null
   * @param sourcePosition  the source position, not null
   */
  protected void convertPosition(final UniqueId nodeId, final ManageablePosition manPos, final SimplePosition sourcePosition) {
    UniqueId posId = convertId(manPos.getUniqueId(), nodeId);
    sourcePosition.setUniqueId(posId);
    sourcePosition.setParentNodeId(nodeId);
    sourcePosition.setQuantity(manPos.getQuantity());
    sourcePosition.setSecurityLink(manPos.getSecurityLink());
    for (ManageableTrade manTrade : manPos.getTrades()) {
      convertTrade(nodeId, posId, manTrade);
      sourcePosition.addTrade(manTrade);
    }
    sourcePosition.setAttributes(manPos.getAttributes());
  }

  /**
   * Converts a manageable trade to a source trade.
   * 
   * @param nodeId  the parent node unique identifier, null if root
   * @param posId  the converted position unique identifier, not null
   * @param manTrade  the manageable trade, not null
   */
  protected void convertTrade(final UniqueId nodeId, final UniqueId posId, final ManageableTrade manTrade) {
    manTrade.setUniqueId(convertId(manTrade.getUniqueId(), nodeId));
    manTrade.setParentPositionId(posId);
  }

  /**
   * Converts a position/trade unique identifier to one unique to the node.
   * 
   * @param positionOrTradeId  the unique identifier to convert, not null
   * @param nodeId  the node unique identifier, not null
   * @return the combined unique identifier, not null
   */
  protected UniqueId convertId(final UniqueId positionOrTradeId, final UniqueId nodeId) {
    return UniqueId.of(
        nodeId.getScheme() + '-' + positionOrTradeId.getScheme(),
        nodeId.getValue() + '-' + positionOrTradeId.getValue(),
        StringUtils.defaultString(nodeId.getVersion()) + '-' + StringUtils.defaultString(positionOrTradeId.getVersion()));
  }
  
  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    // NOTE jonathan 2011-08-03 -- PortfolioMaster does not currently implement ChangeProvider so currently return the
    // PositionMaster's ChangeManager.
    return getPositionMaster().changeManager();
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    String str = getClass().getSimpleName() + "[" + getPositionMaster() + "," + getPositionMaster();
    if (getVersionCorrection() != null) {
      str += ",versionCorrection=" + getVersionCorrection();
    }
    return str + "]";
  }

}
