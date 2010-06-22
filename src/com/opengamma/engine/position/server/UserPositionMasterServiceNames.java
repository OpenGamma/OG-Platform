/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position.server;

import com.opengamma.engine.position.UserPositionMaster;

/**
 * Names used in the RESTful {@link UserPositionMaster}
 */
public final class UserPositionMasterServiceNames {
  
  // CSOFF: Just a set of constants
  public static final String DEFAULT_USER_POSITION_MASTER_NAME = "0";
  
  public static final String USER_POSITION_MASTER_FIELD_OWNER = "owner";
  public static final String USER_POSITION_MASTER_FIELD_PORTFOLIO = "portfolio";
  public static final String USER_POSITION_MASTER_FIELD_RESULT = "result";
  
  public static final String USER_POSITION_MASTER_ADD_PORTFOLIO = "addPortfolio";
  
  public static final String USER_POSITION_MASTER_HEARTBEAT = "heartbeat";
  // CSON
  
  /**
   * Private default constructor
   */
  private UserPositionMasterServiceNames() {
  }
  
}
