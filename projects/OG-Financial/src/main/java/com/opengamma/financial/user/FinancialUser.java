/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user;

/**
 * A user that provides access to underlying services which are managed.
 */
public class FinancialUser {

  /**
   * The user manager.
   */
  private final FinancialUserManager _manager;
  /**
   * The user name.
   */
  private final String _userName;
  /**
   * The client manager.
   */
  private final FinancialClientManager _clientManager;

  /**
   * Creates an instance.
   * 
   * @param manager  the user manager, not null
   * @param userName  the user name, not null
   */
  public FinancialUser(FinancialUserManager manager, String userName) {
    _manager = manager;
    _userName = userName;
    _clientManager = new FinancialClientManager(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the user name.
   * 
   * @return the user name, not null
   */
  public String getUserName() {
    return _userName;
  }

  /**
   * Gets the services.
   * 
   * @return the services, not null
   */
  public FinancialUserManager getUserManager() {
    return _manager;
  }

  /**
   * Gets the client manager.
   * 
   * @return the client manager, not null
   */
  public FinancialClientManager getClientManager() {
    return _clientManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getUserName() + "]";
  }

}
