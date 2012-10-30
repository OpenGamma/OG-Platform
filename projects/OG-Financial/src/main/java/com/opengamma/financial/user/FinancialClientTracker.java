/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.user;

/**
 * Tracks user/client construction and destruction events.
 */
public interface FinancialClientTracker {

  /**
   * Called when a user representation is created - this will happen before any other notifications for the user. Due to
   * loose synchronization, multiple creation notifications may be received.
   * 
   * @param userName the user name
   */
  void userCreated(String userName);

  /**
   * Called when a user representation is discarded - other notifications may still be received due to loose synchronization.
   * An implementation should maintain a set of valid users (from the create and discard notifications) to detect late
   * notifications and act appropriately on them (e.g. if managing data clean up the objects could be deleted immediately).
   * 
   * @param userName the user name
   */
  void userDiscarded(String userName);

  /**
   * Called when a client representation is created - this will happen before any other notifications for the client. Due to
   * loose synchronization, multiple creation notifications may be received.
   * 
   * @param userName the user name
   * @param clientName the client name
   */
  void clientCreated(String userName, String clientName);

  /**
   * Called when a client representation is discarded - other notifications may still be received due to loose synchronization.
   * An implementation should maintain a set of valid clients (from the create and discard notifications) to detect late
   * notifications and act appropriately on them (e.g. if managing data clean up the objects could be deleted immediately).
   * 
   * @param userName the user name
   * @param clientName the client name
   */
  void clientDiscarded(String userName, String clientName);

}
