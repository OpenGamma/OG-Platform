/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.msg.Heartbeat;
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
  private final FudgeContext _fudgeContext;
  
  public HeartbeatSender(ByteArrayMessageSender messageSender, ValueDistributor valueDistributor) {
    this(messageSender, valueDistributor, new FudgeContext(), new Timer("HeartbeatSender Thread"), DEFAULT_PERIOD);
  }
  
  public HeartbeatSender(ByteArrayMessageSender messageSender, ValueDistributor valueDistributor, FudgeContext fudgeContext, Timer timer, long period) {
    ArgumentChecker.notNull(messageSender, "Message Sender");
    ArgumentChecker.notNull(valueDistributor, "Value Distributor");
    ArgumentChecker.notNull(fudgeContext, "Fudge Context");
    ArgumentChecker.notNull(timer, "Timer");
    _messageSender = messageSender;
    _valueDistributor = valueDistributor;
    _fudgeContext = fudgeContext;
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

  /**
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  public class HeartbeatSendingTask extends TimerTask {
    @Override
    public void run() {
      Set<LiveDataSpecification> liveDataSpecs = getValueDistributor().getActiveSpecifications();
      if(liveDataSpecs.isEmpty()) {
        return;
      }
      s_logger.debug("Sending heartbeat message with {} specs", liveDataSpecs.size());
      ArrayList<LiveDataSpecification> liveDataSpecImpls = new ArrayList<LiveDataSpecification>();
      for (LiveDataSpecification spec : liveDataSpecs) {
        liveDataSpecImpls.add(new LiveDataSpecification(spec));               
      }
      Heartbeat heartbeat = new Heartbeat(liveDataSpecImpls);
      FudgeFieldContainer heartbeatMsg = heartbeat.toFudgeMsg(new FudgeSerializationContext(getFudgeContext()));
      byte[] bytes = getFudgeContext ().toByteArray (heartbeatMsg);
      getMessageSender().send(bytes);
    }
  }

}
