/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.msg.Heartbeat;
import com.opengamma.transport.ByteArrayMessageReceiver;
import com.opengamma.util.ArgumentChecker;

/**
 * Receives heartbeat messages and extends the subscription time.
 *
 * @author kirk
 */
public class HeartbeatReceiver implements ByteArrayMessageReceiver {
  private static final Logger s_logger = LoggerFactory.getLogger(HeartbeatReceiver.class);
  private final ExpirationManager _activeSecurityPublicationManager;
  private final FudgeContext _fudgeContext;
  
  public HeartbeatReceiver(ExpirationManager activeSecurityPublicationManager) {
    this(activeSecurityPublicationManager, new FudgeContext());
  }
  
  public HeartbeatReceiver(ExpirationManager activeSecurityPublicationManager, FudgeContext fudgeContext) {
    ArgumentChecker.notNull(activeSecurityPublicationManager, "Active Security Publication Manager");
    _activeSecurityPublicationManager = activeSecurityPublicationManager;
    _fudgeContext = fudgeContext;
  }

  /**
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * @return the activeSecurityPublicationManager
   */
  public ExpirationManager getActiveSecurityPublicationManager() {
    return _activeSecurityPublicationManager;
  }

  @Override
  public void messageReceived(byte[] message) {
    FudgeMsgEnvelope heartbeatEnvelope = getFudgeContext().deserialize(message);
    FudgeFieldContainer heartbeatMsg = heartbeatEnvelope.getMessage();
    messageReceived(heartbeatMsg);
  }
  
  public void messageReceived(FudgeFieldContainer msg) {
    Heartbeat heartbeat = Heartbeat.fromFudgeMsg(new FudgeDeserializationContext(_fudgeContext), msg);
    for (LiveDataSpecification fullyQualifiedLiveDataSpec : heartbeat.getLiveDataSpecifications()) {
      s_logger.debug("Heartbeat received on live data specification {}", fullyQualifiedLiveDataSpec);
      getActiveSecurityPublicationManager().extendPublicationTimeout(fullyQualifiedLiveDataSpec);
    }
  }

}
