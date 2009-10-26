/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataSpecificationImpl;
import com.opengamma.transport.ByteArrayMessageReceiver;
import com.opengamma.util.ArgumentChecker;

/**
 * Receives heartbeat messages and extends the subscription time.
 *
 * @author kirk
 */
public class HeartbeatReceiver implements ByteArrayMessageReceiver {
  private static final Logger s_logger = LoggerFactory.getLogger(HeartbeatReceiver.class);
  private final ActiveSecurityPublicationManager _activeSecurityPublicationManager;
  private final FudgeContext _fudgeContext;
  
  public HeartbeatReceiver(ActiveSecurityPublicationManager activeSecurityPublicationManager) {
    this(activeSecurityPublicationManager, new FudgeContext());
  }
  
  public HeartbeatReceiver(ActiveSecurityPublicationManager activeSecurityPublicationManager, FudgeContext fudgeContext) {
    ArgumentChecker.checkNotNull(activeSecurityPublicationManager, "Active Security Publication Manager");
    ArgumentChecker.checkNotNull(fudgeContext, "Fudge Context");
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
  public ActiveSecurityPublicationManager getActiveSecurityPublicationManager() {
    return _activeSecurityPublicationManager;
  }

  @Override
  public void messageReceived(byte[] message) {
    FudgeMsgEnvelope heartbeatEnvelope = getFudgeContext().deserialize(message);
    FudgeMsg heartbeatMsg = heartbeatEnvelope.getMessage();
    messageReceived(heartbeatMsg);
  }
  
  public void messageReceived(FudgeFieldContainer msg) {
    for(FudgeField field : msg.getAllFields()) {
      if(field.getValue() instanceof FudgeFieldContainer) {
        LiveDataSpecification liveDataSpec = new LiveDataSpecificationImpl((FudgeFieldContainer) field.getValue());
        s_logger.debug("Heartbeat received on live data specification {}", liveDataSpec);
        getActiveSecurityPublicationManager().extendPublicationTimeout(liveDataSpec);
      } else {
        s_logger.warn("Got field {}:{} which had value {}, unexpected for a heartbeat message.",
            new Object[] {field.getName(), field.getOrdinal(), field.getValue()});
      }
    }
  }

}
