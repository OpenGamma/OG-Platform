/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.user;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityHistoryRequest;
import com.opengamma.master.security.SecurityHistoryResult;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;

/**
 * Wraps a security master to trap calls to record user based information to allow clean up and
 * hooks for access control logics if needed.
 */
public class UserSecurityMaster implements SecurityMaster {

  private final UserDataTrackerWrapper _tracker;
  private final SecurityMaster _underlying;

  public UserSecurityMaster(final String userName, final String clientName, final UserDataTracker tracker, final SecurityMaster underlying) {
    _tracker = new UserDataTrackerWrapper(tracker, userName, clientName, UserDataType.SECURITY);
    _underlying = underlying;
  }

  @Override
  public SecurityHistoryResult history(SecurityHistoryRequest request) {
    return _underlying.history(request);
  }

  @Override
  public SecuritySearchResult search(SecuritySearchRequest request) {
    return _underlying.search(request);
  }

  @Override
  public SecurityDocument add(SecurityDocument document) {
    document = _underlying.add(document);
    if (document.getUniqueId() != null) {
      _tracker.created(document.getUniqueId());
    }
    return document;
  }

  @Override
  public SecurityDocument correct(SecurityDocument document) {
    return _underlying.correct(document);
  }

  @Override
  public SecurityDocument get(UniqueIdentifier uid) {
    return _underlying.get(uid);
  }

  @Override
  public void remove(UniqueIdentifier uid) {
    _underlying.remove(uid);
    _tracker.deleted(uid);
  }

  @Override
  public SecurityDocument update(SecurityDocument document) {
    return _underlying.update(document);
  }

}
