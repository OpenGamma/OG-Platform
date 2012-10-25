/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;

/**
 * Collects all Fudge message envelopes in an array for later analysis.
 *
 * @author kirk
 */
public class CollectingFudgeMessageReceiver implements FudgeMessageReceiver {
  private final BlockingQueue<FudgeMsgEnvelope> _messages = new LinkedBlockingQueue<FudgeMsgEnvelope>();

  @Override
  public void messageReceived(FudgeContext fudgeContext, FudgeMsgEnvelope msgEnvelope) {
    _messages.add(msgEnvelope);
  }
  
  public List<FudgeMsgEnvelope> getMessages() {
    return new LinkedList<FudgeMsgEnvelope>(_messages);
  }
  
  public void clear() {
    _messages.clear();
  }

  public FudgeMsgEnvelope waitForMessage(final long timeoutMillis) {
    try {
      return _messages.poll(timeoutMillis, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      return null;
    }
  }

}
