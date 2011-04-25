/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client;

import com.opengamma.util.PublicAPI;

/**
 * Enumerates the states of a view client. The state is independent from whether the client is attached to a view
 * process.
 */
@PublicAPI
public enum ViewClientState {

  /**
   * The client is started. If the client is attached to a view process, then computation results will be published to
   * subscribers.
   */
  STARTED,
  /**
   * The client is paused. If the client is attached to a view process, then computation results will be consumed but
   * held until the client is started, and discarded if the client is detached from the process.
   */
  PAUSED,
  /**
   * The client is terminated. Any resources associated with the client have been released and the client cannot be
   * attached to a view process.
   */
  TERMINATED
  
}
