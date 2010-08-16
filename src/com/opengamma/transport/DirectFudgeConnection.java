/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.transport;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;

/**
 * Creates two FudgeConnection objects with the receiver and sender of each connected to the other.
 * Intended to help build test cases only.
 */
public class DirectFudgeConnection {

  private final FudgeContext _fudgeContext;

  private FudgeMessageReceiver _end1Receiver;

  private final FudgeMessageSender _end1Sender = new FudgeMessageSender() {

    @Override
    public FudgeContext getFudgeContext() {
      return _fudgeContext;
    }

    @Override
    public void send(FudgeFieldContainer message) {
      if (_end2Receiver != null) {
        _end2Receiver.messageReceived(_fudgeContext, new FudgeMsgEnvelope(message));
      }
    }

  };

  private final FudgeConnection _end1 = new FudgeConnection() {

    @Override
    public FudgeMessageSender getFudgeMessageSender() {
      return _end1Sender;
    }

    @Override
    public void setFudgeMessageReceiver(FudgeMessageReceiver receiver) {
      _end1Receiver = receiver;
    }

  };

  private FudgeMessageReceiver _end2Receiver;

  private final FudgeMessageSender _end2Sender = new FudgeMessageSender() {

    @Override
    public FudgeContext getFudgeContext() {
      return _fudgeContext;
    }

    @Override
    public void send(FudgeFieldContainer message) {
      if (_end1Receiver != null) {
        _end1Receiver.messageReceived(_fudgeContext, new FudgeMsgEnvelope(message));
      }
    }

  };

  private final FudgeConnection _end2 = new FudgeConnection() {

    @Override
    public FudgeMessageSender getFudgeMessageSender() {
      return _end2Sender;
    }

    @Override
    public void setFudgeMessageReceiver(FudgeMessageReceiver receiver) {
      _end2Receiver = receiver;
    }

  };

  public DirectFudgeConnection(final FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
  }

  public FudgeConnection getEnd1() {
    return _end1;
  }

  public FudgeConnection getEnd2() {
    return _end2;
  }

}
