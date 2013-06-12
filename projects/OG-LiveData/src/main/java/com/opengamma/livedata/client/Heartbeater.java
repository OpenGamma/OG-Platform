/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.server.LiveDataHeartbeat;
import com.opengamma.util.ArgumentChecker;

/**
 * All logic relating to sending a subscription heartbeat message.
 * 
 * @author kirk
 */
public class Heartbeater {
  /**
   * If not specified, send heartbeats every <em>5 minutes</em>.
   */
  public static final long DEFAULT_PERIOD = 5 * 60 * 1000L;
  private static final Logger s_logger = LoggerFactory.getLogger(Heartbeater.class);
  private final ValueDistributor _valueDistributor;
  private final LiveDataHeartbeat _heartbeat;

  public Heartbeater(final ValueDistributor valueDistributor, final LiveDataHeartbeat heartbeat, final Timer timer, final long period) {
    ArgumentChecker.notNull(valueDistributor, "Value Distributor");
    ArgumentChecker.notNull(timer, "Timer");
    _valueDistributor = valueDistributor;
    _heartbeat = heartbeat;
    timer.schedule(new HeartbeatSendingTask(), period, period);
  }

  /**
   * @return the valueDistributor
   */
  public ValueDistributor getValueDistributor() {
    return _valueDistributor;
  }

  /**
   * @return the heartbeat interface
   */
  public LiveDataHeartbeat getHeartbeat() {
    return _heartbeat;
  }

  /**
   * The task which actually sends the heartbeat messages.
   */
  public class HeartbeatSendingTask extends TimerTask {
    @Override
    public void run() {
      Collection<LiveDataSpecification> liveDataSpecs = getValueDistributor().getActiveSpecifications();
      if (liveDataSpecs.isEmpty()) {
        return;
      }
      s_logger.debug("Sending heartbeat message with {} specs", liveDataSpecs.size());
      try {
        liveDataSpecs = getHeartbeat().heartbeat(liveDataSpecs);
      } catch (Exception e) {
        s_logger.error("Unable to send heartbeat message", e);
      }
      if ((liveDataSpecs == null) || liveDataSpecs.isEmpty()) {
        return;
      }
      // TODO: Notify something about the failed heartbeats
    }
  }

}
