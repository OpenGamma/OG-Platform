/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.transport.ByteArrayMessageSender;
import com.opengamma.util.ArgumentChecker;

/**
 * All logic relating to sending a subscription heartbeat message.
 *
 * @author kirk
 */
public class HeartbeatSender {
  /**
   * If not specified, send heartbeats every <em>5 minutes</em>.
   */
  public static final long DEFAULT_PERIOD = 5 * 60 * 1000l;
  private static final Logger s_logger = LoggerFactory.getLogger(HeartbeatSender.class);
  private final ByteArrayMessageSender _messageSender;
  private final ValueDistributor _valueDistributor;
  
  public HeartbeatSender(ByteArrayMessageSender messageSender, ValueDistributor valueDistributor) {
    this(messageSender, valueDistributor, new Timer("HeartbeatSender Thread"), DEFAULT_PERIOD);
  }
  
  public HeartbeatSender(ByteArrayMessageSender messageSender, ValueDistributor valueDistributor, Timer timer, long period) {
    ArgumentChecker.checkNotNull(messageSender, "Message Sender");
    ArgumentChecker.checkNotNull(valueDistributor, "Value Distributor");
    ArgumentChecker.checkNotNull(timer, "Timer");
    _messageSender = messageSender;
    _valueDistributor = valueDistributor;
    timer.schedule(new HeartbeatSendingTask(), period, period);
  }

  /**
   * @return the messageSender
   */
  public ByteArrayMessageSender getMessageSender() {
    return _messageSender;
  }
  
  /**
   * @return the valueDistributor
   */
  public ValueDistributor getValueDistributor() {
    return _valueDistributor;
  }

  public class HeartbeatSendingTask extends TimerTask {
    @Override
    public void run() {
      s_logger.debug("Sending heartbeat message.");
      FudgeMsg heartbeatMsg = new FudgeMsg();
      Set<LiveDataSpecification> liveDataSpecs = getValueDistributor().getActiveSpecifications();
      for(LiveDataSpecification liveDataSpecification : liveDataSpecs) {
        FudgeFieldContainer specMsg = liveDataSpecification.toFudgeMsg();
        heartbeatMsg.add(null, null, specMsg);
      }
      byte[] bytes = heartbeatMsg.toByteArray();
      getMessageSender().send(bytes);
    }
  }

}
