/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;

/**
 * Similar to {@link ByteArrayRequestReceiver}, except that all messages are
 * extracted to a {@link FudgeMsgEnvelope} before transmission.
 *
 * @author kirk
 */
public interface FudgeMsgRequestReceiver {
  
  FudgeFieldContainer requestReceived(FudgeContext fudgeContext, FudgeMsgEnvelope requestEnvelope);

}
