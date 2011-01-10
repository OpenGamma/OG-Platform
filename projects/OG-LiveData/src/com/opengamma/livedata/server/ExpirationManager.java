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
  
  private static final Logger s_logger = LoggerFactory.getLogger(ExpirationManager.class);
  // Injected Inputs:
  private final AbstractLiveDataServer _dataServer;
  private final long _timeoutExtension;
  
  public ExpirationManager(AbstractLiveDataServer dataServer) {
    this(dataServer, DEFAULT_CHECK_PERIOD);
  }
  
  public ExpirationManager(AbstractLiveDataServer dataServer, long checkPeriod) {
    this(dataServer, DEFAULT_TIMEOUT_EXTENSION, new Timer("ExpirationManager Timer"), checkPeriod);
  }
  
  public ExpirationManager(AbstractLiveDataServer dataServer, long timeoutExtension, long checkPeriod) {
    this(dataServer, timeoutExtension, new Timer("ExpirationManager Timer"), checkPeriod);
  }
  
  public ExpirationManager(AbstractLiveDataServer dataServer, long timeoutExtension, Timer timer, long checkPeriod) {
    ArgumentChecker.notNull(dataServer, "Data Server");
    ArgumentChecker.notNull(timer, "Expiration Timer");
    _dataServer = dataServer;
    _timeoutExtension = timeoutExtension;
    _dataServer.addSubscriptionListener(this);
    timer.schedule(new ExpirationCheckTimerTask(), checkPeriod, checkPeriod);
  }

  /**
   * @return the dataServer
   */
  public AbstractLiveDataServer getDataServer() {
    return _dataServer;
  }

  /**
   * @return the timeoutExtension
   */
  public long getTimeoutExtension() {
    return _timeoutExtension;
  }
  
  @Override
  public void subscribed(Subscription subscription) {
    for (MarketDataDistributor distributor : subscription.getDistributors()) {
      distributor.extendExpiry(getTimeoutExtension());
    }
  }
  
  @Override
  public void unsubscribed(Subscription subscription) {
  }

  public void extendPublicationTimeout(LiveDataSpecification fullyQualifiedSpec) {
    MarketDataDistributor distributor = _dataServer.getMarketDataDistributor(fullyQualifiedSpec);
    if (distributor != null) {
      distributor.extendExpiry(getTimeoutExtension());
    }
  }

  /**
   * A periodic task that calls {@link #expirationCheck()}  
   */
  public class ExpirationCheckTimerTask extends TimerTask {
    @Override
    public void run() {
      try {
        expirationCheck();
      } catch (RuntimeException e) {
        s_logger.error("Checking for data specifications to time out failed", e);
      }
    }
  }
    
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
