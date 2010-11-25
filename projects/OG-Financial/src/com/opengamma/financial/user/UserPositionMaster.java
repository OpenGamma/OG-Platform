/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.user;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.position.FullPortfolioGetRequest;
import com.opengamma.master.position.FullPortfolioNodeGetRequest;
import com.opengamma.master.position.FullPositionGetRequest;
import com.opengamma.master.position.PortfolioTreeDocument;
import com.opengamma.master.position.PortfolioTreeHistoryRequest;
import com.opengamma.master.position.PortfolioTreeHistoryResult;
import com.opengamma.master.position.PortfolioTreeSearchRequest;
import com.opengamma.master.position.PortfolioTreeSearchResult;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionHistoryRequest;
import com.opengamma.master.position.PositionHistoryResult;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;

/**
 * Wraps a position master to trap calls to record user based information to allow clean up and
 * hooks for access control logics if needed.
 */
public class UserPositionMaster implements PositionMaster {

  private final String _userName;
  private final String _clientName;
  private final UserDataTracker _tracker;
  private final PositionMaster _underlying;

  public UserPositionMaster(final String userName, final String clientName, final UserDataTracker tracker, final PositionMaster underlying) {
    _userName = userName;
    _clientName = clientName;
    _tracker = tracker;
    _underlying = underlying;
  }

  @Override
  public PortfolioTreeDocument addPortfolioTree(PortfolioTreeDocument document) {
    document = _underlying.addPortfolioTree(document);
    if (document.getUniqueId() != null) {
      _tracker.created(_userName, _clientName, UserDataType.PORTFOLIO_TREE, document.getUniqueId());
    }
    return document;
  }

  @Override
  public PositionDocument addPosition(PositionDocument document) {
    document = _underlying.addPosition(document);
    if (document.getUniqueId() != null) {
      _tracker.created(_userName, _clientName, UserDataType.POSITION, document.getUniqueId());
    }
    return document;
  }

  @Override
  public PortfolioTreeDocument correctPortfolioTree(PortfolioTreeDocument document) {
    return _underlying.correctPortfolioTree(document);
  }

  @Override
  public PositionDocument correctPosition(PositionDocument document) {
    return _underlying.correctPosition(document);
  }

  @Override
  public Portfolio getFullPortfolio(FullPortfolioGetRequest request) {
    return _underlying.getFullPortfolio(request);
  }

  @Override
  public PortfolioNode getFullPortfolioNode(FullPortfolioNodeGetRequest request) {
    return _underlying.getFullPortfolioNode(request);
  }

  @Override
  public Position getFullPosition(FullPositionGetRequest request) {
    return _underlying.getFullPosition(request);
  }

  @Override
  public PortfolioTreeDocument getPortfolioTree(UniqueIdentifier uid) {
    return _underlying.getPortfolioTree(uid);
  }

  @Override
  public PositionDocument getPosition(UniqueIdentifier uid) {
    return _underlying.getPosition(uid);
  }

  @Override
  public PortfolioTreeHistoryResult historyPortfolioTree(PortfolioTreeHistoryRequest request) {
    return _underlying.historyPortfolioTree(request);
  }

  @Override
  public PositionHistoryResult historyPosition(PositionHistoryRequest request) {
    return _underlying.historyPosition(request);
  }

  @Override
  public void removePortfolioTree(UniqueIdentifier uid) {
    _underlying.removePortfolioTree(uid);
    _tracker.deleted(_userName, _clientName, UserDataType.PORTFOLIO_TREE, uid);
  }

  @Override
  public void removePosition(UniqueIdentifier uid) {
    _underlying.removePosition(uid);
    _tracker.deleted(_userName, _clientName, UserDataType.POSITION, uid);
  }

  @Override
  public PortfolioTreeSearchResult searchPortfolioTrees(PortfolioTreeSearchRequest request) {
    return _underlying.searchPortfolioTrees(request);
  }

  @Override
  public PositionSearchResult searchPositions(PositionSearchRequest request) {
    return _underlying.searchPositions(request);
  }

  @Override
  public PortfolioTreeDocument updatePortfolioTree(PortfolioTreeDocument document) {
    return _underlying.updatePortfolioTree(document);
  }

  @Override
  public PositionDocument updatePosition(PositionDocument document) {
    return _underlying.updatePosition(document);
  }

}
