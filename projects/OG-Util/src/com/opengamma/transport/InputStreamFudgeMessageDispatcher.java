/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.transport;

import java.io.InputStream;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.wire.FudgeMsgReader;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Listens to an {@link InputStream}, splits the stream into individual
 * {@link FudgeMsg}s, and then dispatches them to a {@link FudgeMessageReceiver}.
 * Must be run in its own thread.
 */
public class InputStreamFudgeMessageDispatcher implements Runnable {
  private final InputStream _inputStream;
  private final FudgeMessageReceiver _messageReceiver;
  private final FudgeContext _fudgeContext;
  private final FudgeMsgReader _fudgeMsgReader;
  
  public InputStreamFudgeMessageDispatcher(InputStream inputStream, FudgeMessageReceiver messageReceiver) {
    this(inputStream, messageReceiver, OpenGammaFudgeContext.getInstance());
  }

  public InputStreamFudgeMessageDispatcher(InputStream inputStream, FudgeMessageReceiver messageReceiver, FudgeContext fudgeContext) {
    ArgumentChecker.notNull(inputStream, "inputStream");
    ArgumentChecker.notNull(messageReceiver, "messageReceiver");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _inputStream = inputStream;
    _messageReceiver = messageReceiver;
    _fudgeContext = fudgeContext;
    _fudgeMsgReader = fudgeContext.createMessageReader(inputStream);
  }

  /**
   * Gets the inputStream.
   * @return the inputStream
   */
  public InputStream getInputStream() {
    return _inputStream;
  }

  /**
   * Gets the messageReceiver.
   * @return the messageReceiver
   */
  public FudgeMessageReceiver getMessageReceiver() {
    return _messageReceiver;
  }

  /**
   * Gets the fudgeContext.
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * Gets the fudgeMsgReader.
   * @return the fudgeMsgReader
   */
  public FudgeMsgReader getFudgeMsgReader() {
    return _fudgeMsgReader;
  }

  @Override
  public void run() {
    while (true) {
      FudgeMsgEnvelope msgEnvelope = getFudgeMsgReader().nextMessageEnvelope();
      if (msgEnvelope == null) {
        // End of stream reached.
        break;
      }
      getMessageReceiver().messageReceived(getFudgeContext(), msgEnvelope);
    }
  }

}
