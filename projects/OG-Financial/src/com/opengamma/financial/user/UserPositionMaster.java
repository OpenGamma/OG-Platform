/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.user;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.position.ManageableTrade;
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

  /**
   * Creates an instance.
   * 
   * @param userName  the user name, not null
   * @param clientName  the client name, not null
   * @param tracker  the tracker of user data, not null
   * @param underlying  the underlying master, not null
   */
  public UserPositionMaster(final String userName, final String clientName, final UserDataTracker tracker, final PositionMaster underlying) {
    _userName = userName;
    _clientName = clientName;
    _tracker = tracker;
    _underlying = underlying;
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionSearchResult search(PositionSearchRequest request) {
    return _underlying.search(request);
  }

  @Override
  public PositionDocument get(UniqueIdentifier uid) {
    return _underlying.get(uid);
  }

  @Override
  public PositionDocument add(PositionDocument document) {
    document = _underlying.add(document);
    if (document.getUniqueId() != null) {
      _tracker.created(_userName, _clientName, UserDataType.POSITION, document.getUniqueId());
    }
    return document;
  }

  @Override
  public PositionDocument update(PositionDocument document) {
    return _underlying.update(document);
  }

  @Override
  public void remove(UniqueIdentifier uid) {
    _underlying.remove(uid);
    _tracker.deleted(_userName, _clientName, UserDataType.POSITION, uid);
  }

  @Override
  public PositionHistoryResult history(PositionHistoryRequest request) {
    return _underlying.history(request);
  }

  @Override
  public PositionDocument correct(PositionDocument document) {
    return _underlying.correct(document);
  }

  @Override
  public ManageableTrade getTrade(UniqueIdentifier uid) {
    return _underlying.getTrade(uid);
  }

}
