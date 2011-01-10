/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.user;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioHistoryRequest;
import com.opengamma.master.portfolio.PortfolioHistoryResult;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;

/**
 * Wraps a position master to trap calls to record user based information to allow clean up and
 * hooks for access control logics if needed.
 */
public class UserPortfolioMaster implements PortfolioMaster {

  private final String _userName;
  private final String _clientName;
  private final UserDataTracker _tracker;
  private final PortfolioMaster _underlying;

  /**
   * Creates an instance.
   * 
   * @param userName  the user name, not null
   * @param clientName  the client name, not null
   * @param tracker  the tracker of user data, not null
   * @param underlying  the underlying master, not null
   */
  public UserPortfolioMaster(final String userName, final String clientName, final UserDataTracker tracker, final PortfolioMaster underlying) {
    _userName = userName;
    _clientName = clientName;
    _tracker = tracker;
    _underlying = underlying;
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioSearchResult search(PortfolioSearchRequest request) {
    return _underlying.search(request);
  }

  @Override
  public PortfolioDocument get(UniqueIdentifier uid) {
    return _underlying.get(uid);
  }

  @Override
  public PortfolioDocument add(PortfolioDocument document) {
    document = _underlying.add(document);
    if (document.getUniqueId() != null) {
      _tracker.created(_userName, _clientName, UserDataType.PORTFOLIO, document.getUniqueId());
    }
    return document;
  }

  @Override
  public PortfolioDocument update(PortfolioDocument document) {
    return _underlying.update(document);
  }

  @Override
  public void remove(UniqueIdentifier uid) {
    _underlying.remove(uid);
    _tracker.deleted(_userName, _clientName, UserDataType.PORTFOLIO, uid);
  }

  @Override
  public PortfolioHistoryResult history(PortfolioHistoryRequest request) {
    return _underlying.history(request);
  }

  @Override
  public PortfolioDocument correct(PortfolioDocument document) {
    return _underlying.correct(document);
  }

  @Override
  public ManageablePortfolioNode getNode(UniqueIdentifier uid) {
    return _underlying.getNode(uid);
  }

}
