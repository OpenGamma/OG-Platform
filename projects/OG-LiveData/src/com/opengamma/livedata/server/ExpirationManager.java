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

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.client.HeartbeatSender;
import com.opengamma.livedata.server.distribution.MarketDataDistributor;
import com.opengamma.util.ArgumentChecker;

/**
 * Keeps track of all market data currently being published, and controls the
 * expiry by keeping track of heartbeat messages.
 */
public class ExpirationManager implements SubscriptionListener {

  /** 
   * How long market data should live, by default. Milliseconds
   */
  public static final long DEFAULT_TIMEOUT_EXTENSION = 3 * HeartbeatSender.DEFAULT_PERIOD;
  /**
   * How often expiry task should run. Milliseconds
   */
  public static final long DEFAULT_CHECK_PERIOD = HeartbeatSender.DEFAULT_PERIOD / 2;
  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ExpirationManager.class);

  /**
   * The live data server.
   */
  private final AbstractLiveDataServer _dataServer;
  /**
   * The extension to the timeout.
   */
  private final long _timeoutExtension;

  /**
   * Creates the manager with a default period between checks.
   * 
   * @param dataServer  the live data server, not null
   */
  public ExpirationManager(AbstractLiveDataServer dataServer) {
    this(dataServer, DEFAULT_CHECK_PERIOD);
  }

  /**
   * Creates the manager specifying the check period.
   * 
   * @param dataServer  the live data server, not null
   * @param checkPeriod  the check period in milliseconds
   */
  public ExpirationManager(AbstractLiveDataServer dataServer, long checkPeriod) {
    this(dataServer, DEFAULT_TIMEOUT_EXTENSION, new Timer("ExpirationManager Timer"), checkPeriod);
  }

  /**
   * Creates the manager.
   * 
   * @param dataServer  the live data server, not null
   * @param timeoutExtension  the timeout extension in milliseconds
   * @param checkPeriod  the check period in milliseconds
   */
  public ExpirationManager(AbstractLiveDataServer dataServer, long timeoutExtension, long checkPeriod) {
    this(dataServer, timeoutExtension, new Timer("ExpirationManager Timer"), checkPeriod);
  }

  /**
   * Creates the manager.
   * 
   * @param dataServer  the live data server, not null
   * @param timeoutExtension  the timeout extension in milliseconds
   * @param timer  the timer to use, not null
   * @param checkPeriod  the check period in milliseconds
   */
  public ExpirationManager(AbstractLiveDataServer dataServer, long timeoutExtension, Timer timer, long checkPeriod) {
    ArgumentChecker.notNull(dataServer, "dataServer");
    ArgumentChecker.notNull(timer, "timer");
    _dataServer = dataServer;
    _timeoutExtension = timeoutExtension;
    _dataServer.addSubscriptionListener(this);
    timer.schedule(new ExpirationCheckTimerTask(), checkPeriod, checkPeriod);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the live data server.
   * 
   * @return the dataServer  the server, not null
   */
  public AbstractLiveDataServer getDataServer() {
    return _dataServer;
  }

  /**
   * Gets the timeout extension.
   * 
   * @return the timeoutExtension  the extension in milliseconds
   */
  public long getTimeoutExtension() {
    return _timeoutExtension;
  }

  //-------------------------------------------------------------------------
  /**
   * Extends the expiry for the distributors of the subscription.
   * 
   * @param subscription  the subscription, not null
   */
  @Override
  public void subscribed(Subscription subscription) {
    for (MarketDataDistributor distributor : subscription.getDistributors()) {
      distributor.extendExpiry(getTimeoutExtension());
    }
  }

  /**
   * Takes no action.
   * 
   * @param subscription  the subscription, not null
   */
  @Override
  public void unsubscribed(Subscription subscription) {
  }

  //-------------------------------------------------------------------------
  /**
   * Extends the timeout for the live data specification.
   * 
   * @param fullyQualifiedSpec  the specification, not null
   */
  public void extendPublicationTimeout(LiveDataSpecification fullyQualifiedSpec) {
    MarketDataDistributor distributor = _dataServer.getMarketDataDistributor(fullyQualifiedSpec);
    if (distributor != null) {
      distributor.extendExpiry(getTimeoutExtension());
    } else {
      s_logger.warn("Failed to find distributor for heartbeat on {} from {}", fullyQualifiedSpec, _dataServer);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * A periodic task that calls {@link #expirationCheck()}  
   */
  final class ExpirationCheckTimerTask extends TimerTask {
    @Override
    public void run() {
      try {
        expirationCheck();
      } catch (RuntimeException e) {
        s_logger.error("Checking for data specifications to time out failed", e);
      }
    }
  }

  // this is called by the timer task.
  void expirationCheck() {
    s_logger.debug("Checking for data specifications to time out");
    int nExpired = 0;
    for (Subscription subscription : _dataServer.getSubscriptions()) {
      for (MarketDataDistributor distributor : subscription.getDistributors()) {
        if (distributor.hasExpired()) {
          boolean stopped = _dataServer.stopDistributor(distributor);
          if (stopped) {
            nExpired++;
          }
        }
      }
    }
    s_logger.info("Expired {} specifications", nExpired);
  }

}
