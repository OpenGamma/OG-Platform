/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.user;

import com.opengamma.id.UniqueId;

/**
 * Base class for wrapping masters to trap calls to record user based information,
 * allowing clean up and hooks for access control logics if needed.
 */
public abstract class AbstractFinancialUserService {

  /**
   * The user name.
   */
  private final String _userName;
  /**
   * The client name.
   */
  private final String _clientName;
  /**
   * The tracker.
   */
  private final FinancialUserDataTracker _tracker;
  /**
   * The data type.
   */
  private final FinancialUserDataType _type;

  /**
   * Creates an instance.
   * 
   * @param userName  the user name, not null
   * @param clientName  the client name, not null
   * @param tracker  the tracker, not null
   * @param type  the data type, not null
   */
  public AbstractFinancialUserService(String userName, String clientName, FinancialUserDataTracker tracker, FinancialUserDataType type) {
    _userName = userName;
    _clientName = clientName;
    _tracker = tracker;
    _type = type;
  }

  /**
   * Creates an instance.
   * 
   * @param client  the client, not null
   * @param type  the data type, not null
   */
  public AbstractFinancialUserService(FinancialClient client, FinancialUserDataType type) {
    _userName = client.getUserName();
    _clientName = client.getClientName();
    _tracker = client.getUserDataTracker();
    _type = type;
  }

  //-------------------------------------------------------------------------
  protected void created(UniqueId uniqueId) {
    _tracker.created(_userName, _clientName, _type, uniqueId);
  }

  protected void deleted(UniqueId uniqueId) {
    _tracker.deleted(_userName, _clientName, _type, uniqueId);
  }

}
