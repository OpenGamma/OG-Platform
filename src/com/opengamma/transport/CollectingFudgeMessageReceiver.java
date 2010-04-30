/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import java.util.LinkedList;
import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;

/**
 * Collects all Fudge message envelopes in an array for later analysis.
 *
 * @author kirk
 */
public class CollectingFudgeMessageReceiver implements FudgeMessageReceiver {
  private final List<FudgeMsgEnvelope> _messages = new LinkedList<FudgeMsgEnvelope>();

  @Override
  public synchronized void messageReceived(FudgeContext fudgeContext,
      FudgeMsgEnvelope msgEnvelope) {
    _messages.add(msgEnvelope);
  }
  
  public List<FudgeMsgEnvelope> getMessages() {
    return _messages;
  }
  
  public synchronized void clear() {
    _messages.clear();
  }

}
