/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position.impl;

import org.apache.commons.lang.StringUtils;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.CounterpartyImpl;
import com.opengamma.core.position.impl.PortfolioImpl;
import com.opengamma.core.position.impl.PortfolioNodeImpl;
import com.opengamma.core.position.impl.PositionImpl;
import com.opengamma.core.position.impl.TradeImpl;
import com.opengamma.id.UniqueIdentifier;
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
  public Portfolio getPortfolio(final UniqueIdentifier uniqueId) {
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
    PortfolioImpl prt = new PortfolioImpl(manPrt.getUniqueId(), manPrt.getName());
    convertNode(manPrt.getRootNode(), prt.getRootNode());
    return prt;
  }

  @Override
  public PortfolioNode getPortfolioNode(final UniqueIdentifier uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    final VersionCorrection vc = getVersionCorrection();  // lock against change
    ManageablePortfolioNode manNode;
    if (vc != null) {
      // use defined instants
      PortfolioSearchRequest portfolioSearch = new PortfolioSearchRequest();
      portfolioSearch.addNodeId(uniqueId);
      portfolioSearch.setVersionCorrection(vc);
      PortfolioSearchResult portfolios = getPortfolioMaster().search(portfolioSearch);
      if (portfolios.getDocuments().size() != 1) {
        return null;
      }
      ManageablePortfolio manPrt = portfolios.getFirstPortfolio();
      manNode = manPrt.getRootNode().findNodeByObjectIdentifier(uniqueId);
    } else {
      // match by uniqueId
      try {
        manNode = getPortfolioMaster().getNode(uniqueId);
      } catch (DataNotFoundException ex) {
        return null;
      }
    }
    PortfolioNodeImpl node = new PortfolioNodeImpl();
    convertNode(manNode, node);
    return node;
  }

  @Override
  public Position getPosition(final UniqueIdentifier uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    String[] schemes = StringUtils.split(uniqueId.getScheme(), '-');
    String[] values = StringUtils.split(uniqueId.getValue(), '-');
    String[] versions = StringUtils.split(uniqueId.getVersion(), '-');
    if (schemes.length != 2 || values.length != 2 || versions.length != 2) {
      throw new IllegalArgumentException("Invalid position identifier for MasterPositionSource: " + uniqueId);
    }
    UniqueIdentifier nodeId = UniqueIdentifier.of(schemes[0], values[0], versions[0]);
    UniqueIdentifier posId = UniqueIdentifier.of(schemes[1], values[1], versions[1]);
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
    PositionImpl pos = new PositionImpl();
    convertPosition(nodeId, manPos, pos);
    return pos;
  }

  @Override
  public Trade getTrade(UniqueIdentifier uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    String[] schemes = StringUtils.split(uniqueId.getScheme(), '-');
    String[] values = StringUtils.split(uniqueId.getValue(), '-');
    String[] versions = StringUtils.split(uniqueId.getVersion(), '-');
    if (schemes.length != 2 || values.length != 2 || versions.length != 2) {
      throw new IllegalArgumentException("Invalid trade identifier for MasterPositionSource: " + uniqueId);
    }
    UniqueIdentifier nodeId = versions[0].length() == 0 ? UniqueIdentifier.of(schemes[0], values[0]) : UniqueIdentifier.of(schemes[0], values[0], versions[0]);
    UniqueIdentifier tradeId = versions[0].length() == 0 ? UniqueIdentifier.of(schemes[1], values[1]) : UniqueIdentifier.of(schemes[1], values[1], versions[1]);
    final VersionCorrection vc = getVersionCorrection();  // lock against change
    ManageableTrade manTrade;
    if (vc != null) {
      // use defined instants
      PositionSearchRequest positionSearch = new PositionSearchRequest();
      positionSearch.addTradeId(tradeId);
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
    TradeImpl node = new TradeImpl();
    convertTrade(nodeId, convertId(manTrade.getPositionId(), nodeId), manTrade, node);
    return node;
  }

  //-------------------------------------------------------------------------
  /**
   * Converts a manageable node to a source node.
   * 
   * @param manNode  the manageable node, not null
   * @param sourceNode  the source node, not null
   */
  protected void convertNode(final ManageablePortfolioNode manNode, final PortfolioNodeImpl sourceNode) {
    UniqueIdentifier nodeId = manNode.getUniqueId();
    sourceNode.setUniqueId(nodeId);
    sourceNode.setName(manNode.getName());
    sourceNode.setParentNodeId(manNode.getParentNodeId());
    
    if (manNode.getPositionIds().size() > 0) {
      PositionSearchRequest positionSearch = new PositionSearchRequest();
      positionSearch.setPositionIds(manNode.getPositionIds());
      positionSearch.setVersionCorrection(getVersionCorrection());
      PositionSearchResult positions = getPositionMaster().search(positionSearch);
      for (PositionDocument posDoc : positions.getDocuments()) {
        ManageablePosition manPos = posDoc.getPosition();
        PositionImpl pos = new PositionImpl();
        convertPosition(nodeId, manPos, pos);
        sourceNode.addPosition(pos);
      }
    }
    
    for (ManageablePortfolioNode child : manNode.getChildNodes()) {
      PortfolioNodeImpl childNode = new PortfolioNodeImpl();
      convertNode(child, childNode);
      sourceNode.addChildNode(childNode);
    }
  }

  /**
   * Converts a manageable node to a source node.
   * 
   * @param nodeId  the parent node unique identifier, null if root
   * @param manPos  the manageable position, not null
   * @param sourcePosition  the source position, not null
   */
  protected void convertPosition(final UniqueIdentifier nodeId, final ManageablePosition manPos, final PositionImpl sourcePosition) {
    UniqueIdentifier posUid = convertId(manPos.getUniqueId(), nodeId);
    sourcePosition.setUniqueId(posUid);
    sourcePosition.setParentNodeId(nodeId);
    sourcePosition.setQuantity(manPos.getQuantity());
    sourcePosition.setSecurityKey(manPos.getSecurityKey());
    for (ManageableTrade manTrade : manPos.getTrades()) {
      TradeImpl sourceTrade = new TradeImpl();
      convertTrade(nodeId, posUid, manTrade, sourceTrade);
      sourcePosition.addTrade(sourceTrade);
    }
    sourcePosition.setAttributes(manPos.getAttributes());
  }

  /**
   * Converts a manageable trade to a source trade.
   * 
   * @param nodeId  the parent node unique identifier, null if root
   * @param posId  the converted position unique identifier, not null
   * @param manTrade  the manageable trade, not null
   * @param sourceTrade  the source trade, not null
   */
  protected void convertTrade(final UniqueIdentifier nodeId, final UniqueIdentifier posId, final ManageableTrade manTrade, final TradeImpl sourceTrade) {
    sourceTrade.setUniqueId(convertId(manTrade.getUniqueId(), nodeId));
    sourceTrade.setParentPositionId(posId);
    sourceTrade.setQuantity(manTrade.getQuantity());
    sourceTrade.setSecurityKey(manTrade.getSecurityKey());
    if (manTrade.getCounterpartyKey() != null) {
      sourceTrade.setCounterparty(new CounterpartyImpl(manTrade.getCounterpartyKey()));
    }
    sourceTrade.setTradeDate(manTrade.getTradeDate());
    sourceTrade.setTradeTime(manTrade.getTradeTime());
    
    //set premium
    sourceTrade.setPremium(manTrade.getPremium());
    sourceTrade.setPremiumCurrency(manTrade.getPremiumCurrency());
    sourceTrade.setPremiumDate(manTrade.getPremiumDate());
    sourceTrade.setPremiumTime(manTrade.getPremiumTime());
    
    sourceTrade.setAttributes(manTrade.getAttributes());
  }

  /**
   * Converts a position/trade unique identifier to one unique to the node.
   * 
   * @param positionOrTradeId  the unique identifier to convert, not null
   * @param nodeId  the node unique identifier, not null
   * @return the combined unique identifier, not null
   */
  protected UniqueIdentifier convertId(final UniqueIdentifier positionOrTradeId, final UniqueIdentifier nodeId) {
    return UniqueIdentifier.of(
        nodeId.getScheme() + '-' + positionOrTradeId.getScheme(),
        nodeId.getValue() + '-' + positionOrTradeId.getValue(),
        StringUtils.defaultString(nodeId.getVersion()) + '-' + StringUtils.defaultString(positionOrTradeId.getVersion()));
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
