/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.connector;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * When scheduled periodically, will runs a prescribed command if the {@link #stillAlive} hasn't been called since
 * the previous schedule. This can be used, for example, to trigger shutdown of a failed client connection that has
 * lost its heartbeat. 
 */
public class Watchdog implements Runnable {

  private static final Logger s_logger = LoggerFactory.getLogger(Watchdog.class);

  private final AtomicBoolean _alive = new AtomicBoolean(true);
  private final Runnable _action;

  public Watchdog(final Runnable action) {
    _action = action;
  }

  @Override
  public void run() {
    if (!_alive.getAndSet(false)) {
      s_logger.error("Watchdog alarm triggered - running action");
      _action.run();
    } else {
      s_logger.debug("Watchdog still alive");
    }
  }

  public void stillAlive() {
    _alive.set(true);
  }

}
