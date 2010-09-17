/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client;

/**
 * 
 */
public enum ViewClientState {

  /**
   * The client is started. Computation results from the reference view will be published to subscribers.
   */
  STARTED,
  /**
   * The client is stopped. A stopped client does not listen to computation results from the reference view.
   */
  STOPPED,
  /**
   * The client is paused. The client will consume computation results from the reference view but hold onto them until 
   * the client is started, or discard them if the client is stopped.
   */
  PAUSED,
  /**
   * The client is terminated. Any resources associated with the client have been released and the client is no longer
   * usable.
   */
  TERMINATED
  
}
