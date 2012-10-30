/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.transport;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;

/**
 * Accepts FudgeConnections with an initial message. Once a receiver has been set
 * on the connection, further messages will go to that rather than the connection
 * receiver.
 */
public interface FudgeConnectionReceiver {

  void connectionReceived(FudgeContext fudgeContext, FudgeMsgEnvelope message, FudgeConnection connection);

}
