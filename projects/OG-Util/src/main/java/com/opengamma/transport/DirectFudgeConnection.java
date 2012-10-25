/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.transport;

import java.util.concurrent.atomic.AtomicInteger;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;

/**
 * Creates two FudgeConnection objects with the receiver and sender of each connected to the other.
 * Intended to help build test cases only.
 */
public class DirectFudgeConnection {

  private final FudgeContext _fudgeContext;

  private final AtomicInteger _messages1To2 = new AtomicInteger();
  private final AtomicInteger _messages2To1 = new AtomicInteger();

  private FudgeMessageReceiver _end1Receiver;
  private FudgeConnectionReceiver _end1Connection;

  private final FudgeMessageSender _end1Sender = new FudgeMessageSender() {

    @Override
    public FudgeContext getFudgeContext() {
      return _fudgeContext;
    }

    @Override
    public void send(FudgeMsg message) {
      _messages1To2.incrementAndGet();
      if (_end2Receiver != null) {
        _end2Receiver.messageReceived(_fudgeContext, new FudgeMsgEnvelope(message));
      } else {
        if (_end2Connection != null) {
          _end2Connection.connectionReceived(_fudgeContext, new FudgeMsgEnvelope(message), _end2);
        }
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

    @Override
    public void setConnectionStateListener(final FudgeConnectionStateListener listener) {
      // no action; the direct connection can't change state
    }

  };

  private FudgeMessageReceiver _end2Receiver;
  private FudgeConnectionReceiver _end2Connection;

  private final FudgeMessageSender _end2Sender = new FudgeMessageSender() {

    @Override
    public FudgeContext getFudgeContext() {
      return _fudgeContext;
    }

    @Override
    public void send(FudgeMsg message) {
      _messages2To1.incrementAndGet();
      if (_end1Receiver != null) {
        _end1Receiver.messageReceived(_fudgeContext, new FudgeMsgEnvelope(message));
      } else {
        if (_end1Connection != null) {
          _end1Connection.connectionReceived(_fudgeContext, new FudgeMsgEnvelope(message), _end1);
        }
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

    @Override
    public void setConnectionStateListener(final FudgeConnectionStateListener listener) {
      // no action; the direct connection can't change state
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

  public void connectEnd1(final FudgeConnectionReceiver receiver) {
    _end1Connection = receiver;
  }

  public void connectEnd2(final FudgeConnectionReceiver receiver) {
    _end2Connection = receiver;
  }

  public int getAndResetMessages1To2() {
    return _messages1To2.getAndSet(0);
  }

  public int getAndResetMessages2To1() {
    return _messages2To1.getAndSet(0);
  }

}
