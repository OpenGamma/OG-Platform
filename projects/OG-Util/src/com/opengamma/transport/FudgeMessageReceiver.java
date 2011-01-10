/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;

/**
 * 
 *
 * @author kirk
 */
public interface FudgeMessageReceiver {

  void messageReceived(FudgeContext fudgeContext, FudgeMsgEnvelope msgEnvelope);
}
