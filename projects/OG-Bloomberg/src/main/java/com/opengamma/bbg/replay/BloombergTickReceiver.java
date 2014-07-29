/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.replay;

/**
 * Listener for receiving ticks.
 */
public interface BloombergTickReceiver {

  /**
   * Handle a tick.
   * 
   * @param msg  the message, not null
   */
  void tickReceived(BloombergTick msg);

}
