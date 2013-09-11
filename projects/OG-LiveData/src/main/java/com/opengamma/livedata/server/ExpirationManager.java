/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.client.Heartbeater;
import com.opengamma.livedata.server.distribution.MarketDataDistributor;
import com.opengamma.util.async.AbstractHousekeeper;

/**
 * Keeps track of all market data currently being published, and controls the expiry by keeping track of heartbeat messages.
 */
public class ExpirationManager extends AbstractHousekeeper<StandardLiveDataServer> {

  /**
   * How long market data should live, by default. Milliseconds
   */
  public static final long DEFAULT_TIMEOUT_EXTENSION = 3 * Heartbeater.DEFAULT_PERIOD;
  /**
   * How often expiry task should run. Milliseconds
   */
  public static final long DEFAULT_CHECK_PERIOD = Heartbeater.DEFAULT_PERIOD / 2;

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ExpirationManager.class);

  /**
   * The extension to the timeout.
   */
  private long _timeoutExtension = DEFAULT_TIMEOUT_EXTENSION;
  /**
   * The checking period.
   */
  private long _checkPeriod = DEFAULT_CHECK_PERIOD;

  /**
   * Creates the manager with a default period between checks.
   * 
   * @param dataServer the live data server, not null
   */
  /* package */ExpirationManager(StandardLiveDataServer dataServer) {
    super(dataServer);
  }

  /**
   * Sets the timeout extension.
   * 
   * @param timeoutExtension the extension in milliseconds
   */
  public void setTimeoutExtension(final long timeoutExtension) {
    _timeoutExtension = timeoutExtension;
  }

  /**
   * Sets the check period. This must be set before the manager is started.
   * 
   * @param checkPeriod the checking period in milliseconds
   */
  public void setCheckPeriod(final long checkPeriod) {
    _checkPeriod = checkPeriod;
  }

  /**
   * Gets the check period.
   * 
   * @return the checking period in milliseconds
   */
  public long getCheckPeriod() {
    return _checkPeriod;
  }

  /**
   * Gets the timeout extension.
   * 
   * @return the timeoutExtension the extension in milliseconds
   */
  public long getTimeoutExtension() {
    return _timeoutExtension;
  }

  /**
   * Extends the timeout for the live data specification.
   * 
   * @param fullyQualifiedSpecs the fully-qualified specifications, not null
   */
  public void extendPublicationTimeout(Collection<LiveDataSpecification> fullyQualifiedSpecs) {
    final StandardLiveDataServer server = getTarget();
    if (server == null) {
      s_logger.warn("No live data server set in expiration manager - unable to extend publication timeouts");
      return;
    }
    
    Set<LiveDataSpecification> resubscriptions = new HashSet<LiveDataSpecification>();
    for (LiveDataSpecification fullyQualifiedSpec : fullyQualifiedSpecs) {
      MarketDataDistributor distributor = server.getMarketDataDistributor(fullyQualifiedSpec);
      if (distributor != null) {
        s_logger.debug("Heartbeat on {}", fullyQualifiedSpec);
        distributor.extendExpiry(getTimeoutExtension());
      } else {
        // We have (presumably erroneously) dropped a subscription that a client is
        // expecting. In lieu of determining the underlying cause of dropping the
        // subscription, we automatically create a new subscription
        s_logger.info("Failed to find distributor for heartbeat on {} from {} - will resubscribe", fullyQualifiedSpec, server);
        resubscriptions.add(fullyQualifiedSpec);
      }
    }
    if (!resubscriptions.isEmpty()) {
      s_logger.warn("Received heartbeat for {} live data specifications. Resubscribing to {} of these.", fullyQualifiedSpecs.size(), resubscriptions.size());
      server.subscribe(resubscriptions, false);
    }
  }

  @Override
  protected int getPeriodSeconds() {
    return (int) (_checkPeriod / 1000);
  }

  @Override
  protected boolean housekeep(StandardLiveDataServer server) {
    s_logger.debug("Checking for data specifications to time out");
    int nExpired = server.expireSubscriptions();
    s_logger.info("Expired {} specifications", nExpired);
    return server.isRunning();
  }

}
