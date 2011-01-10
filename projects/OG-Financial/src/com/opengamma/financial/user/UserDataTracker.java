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
public interface UserDataTracker {

  /**
   * Called after the user has created a data entry.
   * 
   * @param userName the user name
   * @param clientName the user's client name
   * @param type the data type
   * @param identifier the identifier allocated by the underlying
   */
  void created(String userName, String clientName, UserDataType type, UniqueIdentifier identifier);

  /**
   * Called after the user has deleted a data entry.
   * 
   * @param userName the user name
   * @param clientName the user's client name
   * @param type the data type
   * @param identifier the identifier deleted from the underlying
   */
  void deleted(String userName, String clientName, UserDataType type, UniqueIdentifier identifier);

  // TODO: Do we want any form of access control callbacks

}
