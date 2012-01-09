/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.livedata.server.AbstractLiveDataServer.ConnectionStatus;
import com.opengamma.util.ArgumentChecker;

/**
 * Monitors the state of the connection to the underlying market data API
 * and reconnects if the connection has been lost.
 * <p>
 * This beans depends-on the Live Data Server, and any Spring configuration must reflect 
 * this. See <a href="http://jira.springframework.org/browse/SPR-2325">http://jira.springframework.org/browse/SPR-2325</a>.  
 *
 */
public class ReconnectManager implements Lifecycle {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ReconnectManager.class);

  /**
   * How often connection status should be checked. Milliseconds
   */
  public static final long DEFAULT_CHECK_PERIOD = 5000;

  /**
   * The live data server.
   */
  private final AbstractLiveDataServer _server;
  /**
   * The timer.
   */
  private final Timer _timer;
  /**
   * The checking period.
   */
  private final long _checkPeriod;
  /**
   * The checking task.
   */
  private volatile CheckTask _checkTask;

  /**
   * Creates an instance wrapping an underlying server.
   * 
   * @param server  the server, not null
   */
  public ReconnectManager(AbstractLiveDataServer server) {
    this(server, DEFAULT_CHECK_PERIOD);    
  }

  /**
   * Creates an instance wrapping an underlying server.
   * 
   * @param server  the server, not null
   * @param checkIntervalMillis  the checking interval in milliseconds
   */
  public ReconnectManager(AbstractLiveDataServer server, long checkIntervalMillis) {
    this(server, checkIntervalMillis, new Timer("ReconnectManager Timer"));    
  }

  /**
   * Creates an instance wrapping an underlying server.
   * 
   * @param server  the server, not null
   * @param checkIntervalMillis  the checking interval in milliseconds
   * @param timer  the timer, not null
   */
  public ReconnectManager(AbstractLiveDataServer server, long checkIntervalMillis, Timer timer) {
    ArgumentChecker.notNull(server, "server");
    ArgumentChecker.notNull(timer, "timer");
    if (checkIntervalMillis <= 0) {
      throw new IllegalArgumentException("Please give positive check period");
    }
    _server = server;
    _timer = timer;
    _checkPeriod = checkIntervalMillis;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean isRunning() {
    return _checkTask != null;
  }

  @Override
  public void start() {
    _checkTask = new CheckTask();
    _timer.schedule(_checkTask, _checkPeriod, _checkPeriod);
  }

  @Override
  public void stop() {
    _checkTask.cancel();
    _checkTask = null;    
  }

  //-------------------------------------------------------------------------
  private class CheckTask extends TimerTask {
    @Override
    public void run() {
      try {
        check();
      } catch (RuntimeException e) {
        s_logger.error("Checking for reconnection failed", e);
      }
    }
  }

  // called by the timer task
  private void check() {
    if (_server.getConnectionStatus() == ConnectionStatus.NOT_CONNECTED) {
      s_logger.warn("Connection to market data API down. Attemping to reconnect to {}.", _server);
      
      try {
        _server.connect();
      } catch (RuntimeException e) {
        s_logger.warn("Could not reconnect", e);
        return;
      }
      
      s_logger.info("Reconnection successful. Reestablishing subscriptions.");
      _server.reestablishSubscriptions();
      s_logger.info("Reconnect done.");
    
    } else {
      s_logger.debug("Connection up");
    }
  }

}
