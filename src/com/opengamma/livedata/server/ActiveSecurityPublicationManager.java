/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.client.HeartbeatSender;
import com.opengamma.util.ArgumentChecker;

/**
 * Keeps track of all securities currently being published, and controls the
 * expiry by keeping track of heartbeat messages.
 *
 * @author kirk
 */
public class ActiveSecurityPublicationManager implements SubscriptionListener {
  public static final long DEFAULT_TIMEOUT_EXTENSION = 3 * HeartbeatSender.DEFAULT_PERIOD;
  public static final long DEFAULT_CHECK_PERIOD = HeartbeatSender.DEFAULT_PERIOD / 2;
  private static final Logger s_logger = LoggerFactory.getLogger(ActiveSecurityPublicationManager.class);
  // Injected Inputs:
  private final AbstractLiveDataServer _dataServer;
  private final long _timeoutExtension;
  
  // Running State:
  private final ConcurrentMap<LiveDataSpecification, Long> _activeSpecificationTimeouts =
    new ConcurrentHashMap<LiveDataSpecification, Long>();
  
  public ActiveSecurityPublicationManager(AbstractLiveDataServer dataServer) {
    this(dataServer, DEFAULT_CHECK_PERIOD);
  }
  
  public ActiveSecurityPublicationManager(AbstractLiveDataServer dataServer, long checkPeriod) {
    this(dataServer, DEFAULT_TIMEOUT_EXTENSION, new Timer("ActiveSecurityPublicationManager Timer"), checkPeriod);
  }
  
  public ActiveSecurityPublicationManager(AbstractLiveDataServer dataServer, long timeoutExtension, long checkPeriod) {
    this(dataServer, timeoutExtension, new Timer("ActiveSecurityPublicationManager Timer"), checkPeriod);
  }
  
  public ActiveSecurityPublicationManager(AbstractLiveDataServer dataServer, long timeoutExtension, Timer timer, long checkPeriod) {
    ArgumentChecker.checkNotNull(dataServer, "Data Server");
    ArgumentChecker.checkNotNull(timer, "Expiration Timer");
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

  /**
   * @return the activeSpecificationTimeouts
   */
  public ConcurrentMap<LiveDataSpecification, Long> getActiveSpecificationTimeouts() {
    return _activeSpecificationTimeouts;
  }
  
  public void subscribed(LiveDataSpecification fullyQualifiedSpec) {
    extendPublicationTimeout(fullyQualifiedSpec);        
  }
  
  public void unsubscribed(LiveDataSpecification fullyQualifiedSpec) {
  }

  public boolean isCurrentlyPublished(LiveDataSpecification spec) {
    return getActiveSpecificationTimeouts().containsKey(spec);
  }
  
  public void extendPublicationTimeout(LiveDataSpecification spec) {
    getActiveSpecificationTimeouts().put(spec, (System.currentTimeMillis() + getTimeoutExtension()));
  }
  
  public class ExpirationCheckTimerTask extends TimerTask {
    @Override
    public void run() {
      s_logger.debug("Checking for data specifications to time out");
      int nExpired = 0;
      long startTime = System.currentTimeMillis();
      for(Map.Entry<LiveDataSpecification, Long> entry : getActiveSpecificationTimeouts().entrySet()) {
        if(entry.getValue() < startTime) {
          if(getActiveSpecificationTimeouts().remove(entry.getKey(), entry.getValue())) {
            getDataServer().unsubscribe(entry.getKey());
            nExpired++;
          } else {
            // Someone piped up while we were navigating. Do nothing.
          }
        }
      }
      s_logger.info("Expired {} specifications", nExpired);
    }
  }
  
}
