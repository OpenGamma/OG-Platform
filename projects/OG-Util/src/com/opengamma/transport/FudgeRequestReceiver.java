/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializationContext;

/**
 * Similar to {@link ByteArrayRequestReceiver}, except that all messages are
 * extracted to a {@link FudgeMsgEnvelope} before transmission.
 *
 * @author kirk
 */
public interface FudgeRequestReceiver {
  
  FudgeMsg requestReceived(FudgeDeserializationContext context, FudgeMsgEnvelope requestEnvelope);

}
