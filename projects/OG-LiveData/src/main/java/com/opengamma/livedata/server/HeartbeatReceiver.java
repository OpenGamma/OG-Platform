/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.livedata.msg.Heartbeat;
import com.opengamma.transport.ByteArrayMessageReceiver;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Receives heartbeat messages and extends the subscription time.
 */
public class HeartbeatReceiver implements ByteArrayMessageReceiver {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(HeartbeatReceiver.class);

  /**
   * The expiration manager.
   */
  private final ExpirationManager _activeSecurityPublicationManager;
  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext;

  /**
   * Creates an instance with an expiration manager.
   * 
   * @param activeSecurityPublicationManager  the manager, not null
   */
  public HeartbeatReceiver(ExpirationManager activeSecurityPublicationManager) {
    this(activeSecurityPublicationManager, OpenGammaFudgeContext.getInstance());
  }

  /**
   * Creates an instance with an expiration manager.
   * 
   * @param activeSecurityPublicationManager  the manager, not null
   * @param fudgeContext  the Fudge context, not null
   */
  public HeartbeatReceiver(ExpirationManager activeSecurityPublicationManager, FudgeContext fudgeContext) {
    ArgumentChecker.notNull(activeSecurityPublicationManager, "activeSecurityPublicationManager");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _activeSecurityPublicationManager = activeSecurityPublicationManager;
    _fudgeContext = fudgeContext;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the Fudge context.
   * 
   * @return the Fudge context, not null
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * Gets the manager.
   * 
   * @return the active security publication manager, not null
   */
  public ExpirationManager getActiveSecurityPublicationManager() {
    return _activeSecurityPublicationManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public void messageReceived(byte[] message) {
    FudgeMsgEnvelope heartbeatEnvelope = getFudgeContext().deserialize(message);
    FudgeMsg heartbeatMsg = heartbeatEnvelope.getMessage();
    messageReceived(heartbeatMsg);
  }

  public void messageReceived(FudgeMsg msg) {
    Heartbeat heartbeat = Heartbeat.fromFudgeMsg(new FudgeDeserializer(_fudgeContext), msg);
    s_logger.debug("Heartbeat received for: {}", heartbeat.getLiveDataSpecifications());
    getActiveSecurityPublicationManager().extendPublicationTimeout(heartbeat.getLiveDataSpecifications());
  }

}
