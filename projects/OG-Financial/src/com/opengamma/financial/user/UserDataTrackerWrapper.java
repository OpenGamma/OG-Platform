/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.user;

import com.opengamma.id.UniqueIdentifier;

/**
 * Tracks events trapped by the user data masters.
 */
/* package */class UserDataTrackerWrapper {

  private final UserDataTracker _tracker;
  private final String _userName;
  private final String _clientName;
  private final UserDataType _type;

  public UserDataTrackerWrapper(final UserDataTracker tracker, final String userName, final String clientName, final UserDataType type) {
    _tracker = tracker;
    _userName = userName;
    _clientName = clientName;
    _type = type;
  }

  public void created(final UniqueIdentifier identifier) {
    _tracker.created(_userName, _clientName, _type, identifier);
  }

  public void deleted(final UniqueIdentifier identifier) {
    _tracker.deleted(_userName, _clientName, _type, identifier);
  }

}
