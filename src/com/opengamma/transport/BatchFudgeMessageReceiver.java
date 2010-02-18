/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;

/**
 * An interface through which multiple fudge messages can be provided for
 * batch operation.
 *
 * @author kirk
 */
public interface BatchFudgeMessageReceiver {

  void messagesReceived(FudgeContext fudgeContext, List<FudgeMsgEnvelope> msgEnvelope);
}
