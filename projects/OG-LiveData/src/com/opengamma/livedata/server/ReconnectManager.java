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
 * this. See {@link http://jira.springframework.org/browse/SPR-2325}.  
 *
 */
public class ReconnectManager implements Lifecycle {
  
  private static final Logger s_logger = LoggerFactory
    .getLogger(ReconnectManager.class);

  /**
   * How often connection status should be checked. Milliseconds
   */
  public static final long DEFAULT_CHECK_PERIOD = 5000;
  
  private final AbstractLiveDataServer _server;
  private final Timer _timer;
  private final long _checkPeriod;
  private volatile CheckTask _checkTask;
  
  public ReconnectManager(AbstractLiveDataServer server) {
    this(server, DEFAULT_CHECK_PERIOD);    
  }
  
  public ReconnectManager(AbstractLiveDataServer server, long checkIntervalMillis) {
    this(server, checkIntervalMillis, new Timer("ReconnectManager Timer"));    
  }
  
  public ReconnectManager(AbstractLiveDataServer server, long checkIntervalMillis, Timer timer) {
    ArgumentChecker.notNull(server, "Live Data Server");
    ArgumentChecker.notNull(timer, "Timer");
    if (checkIntervalMillis <= 0) {
      throw new IllegalArgumentException("Please give positive check period");
    }
    
    _server = server;
    _timer = timer;
    _checkPeriod = checkIntervalMillis;
  }
  
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
  
  private void check() {
    if (_server.getConnectionStatus() == ConnectionStatus.NOT_CONNECTED) {
      s_logger.info("Connection to market data API down. Attemping to reconnect.");
      
      try {
        _server.connect();
      } catch (RuntimeException e) {
        s_logger.info("Could not reconnect", e);
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
