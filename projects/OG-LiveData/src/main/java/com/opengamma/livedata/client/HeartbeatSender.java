/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import java.util.Collection;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.msg.Heartbeat;
import com.opengamma.livedata.server.LiveDataHeartbeat;
import com.opengamma.transport.ByteArrayFudgeMessageSender;
import com.opengamma.transport.ByteArrayMessageSender;
import com.opengamma.transport.FudgeMessageSender;
import com.opengamma.util.ArgumentChecker;

/**
 * Basic implementation of a subscription heartbeater that notifies the server(s) but does not receive any information back.
 */
public class HeartbeatSender implements LiveDataHeartbeat {

  private final FudgeMessageSender _messageSender;

  public HeartbeatSender(final FudgeMessageSender messageSender) {
    ArgumentChecker.notNull(messageSender, "messageSender");
    _messageSender = messageSender;
  }

  public HeartbeatSender(final ByteArrayMessageSender messageSender, final FudgeContext fudgeContext) {
    this(new ByteArrayFudgeMessageSender(messageSender, fudgeContext));
  }

  protected FudgeMessageSender getMessageSender() {
    return _messageSender;
  }

  // LiveDataHeartbeat

  @Override
  public Collection<LiveDataSpecification> heartbeat(final Collection<LiveDataSpecification> activeSubscriptions) {
    Heartbeat heartbeat = new Heartbeat(activeSubscriptions);
    FudgeMsg heartbeatMsg = heartbeat.toFudgeMsg(new FudgeSerializer(getMessageSender().getFudgeContext()));
    getMessageSender().send(heartbeatMsg);
    return null;
  }

}
