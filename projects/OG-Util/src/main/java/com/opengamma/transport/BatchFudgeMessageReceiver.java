/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;

/**
 * An interface through which multiple Fudge messages can be provided for
 * batch operation.
 */
public interface BatchFudgeMessageReceiver {

  /**
   * Receives and processes a list of byte array messages.
   * Messages are provided in the order originally received.
   * @param fudgeContext  the Fudge context, not null
   * @param messages  the messages received by the underlying transport handler, not null
   */
  void messagesReceived(FudgeContext fudgeContext, List<FudgeMsgEnvelope> messages);

}
