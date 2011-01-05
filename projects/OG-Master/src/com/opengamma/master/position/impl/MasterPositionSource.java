/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position.impl;

import javax.time.Instant;
import javax.time.InstantProvider;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Objects;
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
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioHistoryRequest;
import com.opengamma.master.portfolio.PortfolioHistoryResult;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionHistoryRequest;
import com.opengamma.master.position.PositionHistoryResult;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.util.ArgumentChecker;

/**
 * A {@code PositionSource} implemented using an underlying {@code PositionMaster}.
 * <p>
 * The {@link PositionSource} interface provides securities to the engine via a narrow API.
 * This class provides the source on top of a standard {@link PortfolioMaster}.
 */
public class MasterPositionSource implements PositionSource {
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
   * The instant to search for a version at.
   * Null is treated as the latest version.
   */
  private final Instant _versionAsOfInstant;
  /**
   * The instant to search for corrections for.
   * Null is treated as the latest correction.
   */
  private final Instant _correctedToInstant;

  /**
   * Creates an instance with underlying masters.
   * 
   * @param portfolioMaster  the portfolio master, not null
   * @param positionMaster  the position master, not null
   */
  public MasterPositionSource(final PortfolioMaster portfolioMaster, final PositionMaster positionMaster) {
    this(portfolioMaster, positionMaster, null, null);
  }

  /**
   * Creates an instance with underlying masters viewing the version
   * that existed on the specified instant.
   * 
   * @param portfolioMaster  the portfolio master, not null
   * @param positionMaster  the position master, not null
   * @param versionAsOfInstantProvider  the version instant to retrieve, null for latest version
   */
  public MasterPositionSource(final PortfolioMaster portfolioMaster, final PositionMaster positionMaster, final InstantProvider versionAsOfInstantProvider) {
    this(portfolioMaster, positionMaster, versionAsOfInstantProvider, null);
  }

  /**
   * Creates an instance with underlying masters viewing the version
   * that existed on the specified instant as corrected to the correction instant.
   * 
   * @param portfolioMaster  the portfolio master, not null
   * @param positionMaster  the position master, not null
   * @param versionAsOfInstantProvider  the version instant to retrieve, null for latest version
   * @param correctedToInstantProvider  the instant that the data should be corrected to, null for latest correction
   */
  public MasterPositionSource(
      final PortfolioMaster portfolioMaster, final PositionMaster positionMaster,
      final InstantProvider versionAsOfInstantProvider, final InstantProvider correctedToInstantProvider) {
    ArgumentChecker.notNull(portfolioMaster, "portfolioMaster");
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    _portfolioMaster = portfolioMaster;
    _positionMaster = positionMaster;
    if (versionAsOfInstantProvider != null) {
      _versionAsOfInstant = Instant.of(versionAsOfInstantProvider);
    } else {
      _versionAsOfInstant = null;
    }
    if (correctedToInstantProvider != null) {
      _correctedToInstant = Instant.of(correctedToInstantProvider);
    } else {
      _correctedToInstant = null;
    }
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
   * Gets the version instant to retrieve.
   * 
   * @return the version instant to retrieve, null for latest version
   */
  public Instant getVersionAsOfInstant() {
    return _versionAsOfInstant;
  }

  /**
   * Gets the instant that the data should be corrected to.
   * 
   * @return the instant that the data should be corrected to, null for latest correction
   */
  public Instant getCorrectedToInstant() {
    return _correctedToInstant;
  }

  //-------------------------------------------------------------------------
  @Override
  public Portfolio getPortfolio(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    Instant now = Instant.now();
    Instant versionAsOf = Objects.firstNonNull(getVersionAsOfInstant(), now);
    Instant correctedTo = Objects.firstNonNull(getCorrectedToInstant(), now);
    ManageablePortfolio manPrt;
    if (getVersionAsOfInstant() != null || getCorrectedToInstant() != null) {
      // use defined instants
      PortfolioHistoryRequest portfolioSearch = new PortfolioHistoryRequest(uid.toLatest(), getVersionAsOfInstant(), getCorrectedToInstant());
      PortfolioHistoryResult portfolios = getPortfolioMaster().history(portfolioSearch);
      if (portfolios.getDocuments().size() != 1) {
        return null;
      }
      manPrt = portfolios.getFirstPortfolio();
    } else {
      // match by uid
      try {
        PortfolioDocument prtDoc = getPortfolioMaster().get(uid);
        manPrt = prtDoc.getPortfolio();
      } catch (DataNotFoundException ex) {
        return null;
      }
    }
    PortfolioImpl prt = new PortfolioImpl(manPrt.getUniqueId(), manPrt.getName());
    convertNode(versionAsOf, correctedTo, manPrt.getRootNode(), prt.getRootNode());
    return prt;
  }

  @Override
  public PortfolioNode getPortfolioNode(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    Instant now = Instant.now();
    Instant versionAsOf = Objects.firstNonNull(getVersionAsOfInstant(), now);
    Instant correctedTo = Objects.firstNonNull(getCorrectedToInstant(), now);
    ManageablePortfolioNode manNode;
    if (getVersionAsOfInstant() != null || getCorrectedToInstant() != null) {
      // use defined instants
      PortfolioSearchRequest portfolioSearch = new PortfolioSearchRequest();
      portfolioSearch.getNodeIds().add(uid.toLatest());
      portfolioSearch.setVersionAsOfInstant(versionAsOf);
      portfolioSearch.setCorrectedToInstant(correctedTo);
      PortfolioSearchResult portfolios = getPortfolioMaster().search(portfolioSearch);
      if (portfolios.getDocuments().size() != 1) {
        return null;
      }
      ManageablePortfolio manPrt = portfolios.getFirstPortfolio();
      manNode = manPrt.getRootNode().findNodeByObjectIdentifier(uid);
    } else {
      // match by uid
      try {
        manNode = getPortfolioMaster().getNode(uid);
      } catch (DataNotFoundException ex) {
        return null;
      }
    }
    PortfolioNodeImpl node = new PortfolioNodeImpl();
    convertNode(versionAsOf, correctedTo, manNode, node);
    return node;
  }

  @Override
  public Position getPosition(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    String[] schemes = StringUtils.split(uid.getScheme(), '-');
    String[] values = StringUtils.split(uid.getValue(), '-');
    String[] versions = StringUtils.split(uid.getVersion(), '-');
    if (schemes.length != 2 || values.length != 2 || versions.length != 2) {
      throw new IllegalArgumentException("Invalid position identifier for MasterPositionSource: " + uid);
    }
    UniqueIdentifier nodeUid = UniqueIdentifier.of(schemes[0], values[0], versions[0]);
    UniqueIdentifier posUid = UniqueIdentifier.of(schemes[1], values[1], versions[1]);
    ManageablePosition manPos;
    if (getVersionAsOfInstant() != null || getCorrectedToInstant() != null) {
      // use defined instants
      PositionHistoryRequest positionSearch = new PositionHistoryRequest(posUid.toLatest(), getVersionAsOfInstant(), getCorrectedToInstant());
      PositionHistoryResult positions = getPositionMaster().history(positionSearch);
      if (positions.getDocuments().size() != 1) {
        return null;
      }
      manPos = positions.getFirstPosition();
    } else {
      // match by uid
      try {
        PositionDocument posDoc = getPositionMaster().get(posUid);
        manPos = posDoc.getPosition();
      } catch (DataNotFoundException ex) {
        return null;
      }
    }
    PositionImpl pos = new PositionImpl();
    convertPosition(nodeUid, manPos, pos);
    return pos;
  }

  @Override
  public Trade getTrade(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    String[] schemes = StringUtils.split(uid.getScheme(), '-');
    String[] values = StringUtils.split(uid.getValue(), '-');
    String[] versions = StringUtils.split(uid.getVersion(), '-');
    if (schemes.length != 2 || values.length != 2 || versions.length != 2) {
      throw new IllegalArgumentException("Invalid trade identifier for MasterPositionSource: " + uid);
    }
    UniqueIdentifier nodeUid = versions[0].length() == 0 ? UniqueIdentifier.of(schemes[0], values[0]) : UniqueIdentifier.of(schemes[0], values[0], versions[0]);
    UniqueIdentifier tradeUid = versions[0].length() == 0 ? UniqueIdentifier.of(schemes[1], values[1]) : UniqueIdentifier.of(schemes[1], values[1], versions[1]);
    ManageableTrade manTrade;
    if (getVersionAsOfInstant() != null || getCorrectedToInstant() != null) {
      // use defined instants
      PositionSearchRequest positionSearch = new PositionSearchRequest();
      positionSearch.getTradeIds().add(tradeUid.toLatest());
      positionSearch.setVersionAsOfInstant(getVersionAsOfInstant());
      positionSearch.setCorrectedToInstant(getCorrectedToInstant());
      PositionSearchResult positions = getPositionMaster().search(positionSearch);
      if (positions.getDocuments().size() != 1) {
        return null;
      }
      ManageablePosition manPos = positions.getFirstPosition();
      manTrade = manPos.getTrade(tradeUid);
    } else {
      // match by uid
      try {
        manTrade = getPositionMaster().getTrade(tradeUid);
      } catch (DataNotFoundException ex) {
        return null;
      }
    }
    TradeImpl node = new TradeImpl();
    convertTrade(nodeUid, convertUid(manTrade.getPositionId(), nodeUid), manTrade, node);
    return node;
  }

  //-------------------------------------------------------------------------
  /**
   * Converts a manageable node to a source node.
   * 
   * @param versionAsOf  the version as of, not null
   * @param correctedTo  the correction to, not null
   * @param manNode  the manageable node, not null
   * @param sourceNode  the source node, not null
   */
  protected void convertNode(final Instant versionAsOf, final Instant correctedTo, final ManageablePortfolioNode manNode, final PortfolioNodeImpl sourceNode) {
    UniqueIdentifier nodeUid = manNode.getUniqueId();
    sourceNode.setUniqueId(nodeUid);
    sourceNode.setName(manNode.getName());
    sourceNode.setParentNodeId(manNode.getParentNodeId());
    
    if (manNode.getPositionIds().size() > 0) {
      PositionSearchRequest positionSearch = new PositionSearchRequest();
      positionSearch.setPositionIds(manNode.getPositionIds());
      positionSearch.setVersionAsOfInstant(versionAsOf);
      positionSearch.setCorrectedToInstant(correctedTo);
      PositionSearchResult positions = getPositionMaster().search(positionSearch);
      for (PositionDocument posDoc : positions.getDocuments()) {
        ManageablePosition manPos = posDoc.getPosition();
        PositionImpl pos = new PositionImpl();
        convertPosition(nodeUid, manPos, pos);
        sourceNode.addPosition(pos);
      }
    }
    
    for (ManageablePortfolioNode child : manNode.getChildNodes()) {
      PortfolioNodeImpl childNode = new PortfolioNodeImpl();
      convertNode(versionAsOf, correctedTo, child, childNode);
      sourceNode.addChildNode(childNode);
    }
  }

  /**
   * Converts a manageable node to a source node.
   * 
   * @param nodeUid  the parent node unique identifier, null if root
   * @param manPos  the manageable position, not null
   * @param sourcePosition  the source position, not null
   */
  protected void convertPosition(final UniqueIdentifier nodeUid, final ManageablePosition manPos, final PositionImpl sourcePosition) {
    UniqueIdentifier posUid = convertUid(manPos.getUniqueId(), nodeUid);
    sourcePosition.setUniqueId(posUid);
    sourcePosition.setParentNodeId(nodeUid);
    sourcePosition.setQuantity(manPos.getQuantity());
    sourcePosition.setSecurityKey(manPos.getSecurityKey());
    for (ManageableTrade manTrade : manPos.getTrades()) {
      TradeImpl sourceTrade = new TradeImpl();
      convertTrade(nodeUid, posUid, manTrade, sourceTrade);
      sourcePosition.addTrade(sourceTrade);
    }
  }

  /**
   * Converts a manageable trade to a source trade.
   * 
   * @param nodeUid  the parent node unique identifier, null if root
   * @param posUid  the converted position unique identifier, not null
   * @param manTrade  the manageable trade, not null
   * @param sourceTrade  the source trade, not null
   */
  protected void convertTrade(final UniqueIdentifier nodeUid, final UniqueIdentifier posUid, final ManageableTrade manTrade, final TradeImpl sourceTrade) {
    sourceTrade.setUniqueId(convertUid(manTrade.getUniqueId(), nodeUid));
    sourceTrade.setParentPositionId(posUid);
    sourceTrade.setQuantity(manTrade.getQuantity());
    sourceTrade.setSecurityKey(manTrade.getSecurityKey());
    if (manTrade.getCounterpartyId() != null) {
      sourceTrade.setCounterparty(new CounterpartyImpl(manTrade.getCounterpartyId()));
    }
    sourceTrade.setTradeDate(manTrade.getTradeDate());
    sourceTrade.setTradeTime(manTrade.getTradeTime());
  }

  /**
   * Converts a position/trade unique identifier to one unique to the node.
   * 
   * @param positionOrTradeUid  the unique identifier to convert, not null
   * @param nodeUid  the node unique identifier, not null
   * @return the combined unique identifier, not null
   */
  protected UniqueIdentifier convertUid(final UniqueIdentifier positionOrTradeUid, final UniqueIdentifier nodeUid) {
    return UniqueIdentifier.of(
        nodeUid.getScheme() + '-' + positionOrTradeUid.getScheme(),
        nodeUid.getValue() + '-' + positionOrTradeUid.getValue(),
        StringUtils.defaultString(nodeUid.getVersion()) + '-' + StringUtils.defaultString(positionOrTradeUid.getVersion()));
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    String str = "MasterPositionSource[" + getPositionMaster();
    if (_versionAsOfInstant != null) {
      str += ",versionAsOf=" + _versionAsOfInstant;
    }
    if (_versionAsOfInstant != null) {
      str += ",correctedTo=" + _correctedToInstant;
    }
    return str + "]";
  }

}
