/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.socket;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.transport.FudgeMessageSender;
import com.opengamma.util.ArgumentChecker;

// REVIEW kirk 2010-05-12 -- Enhancements here:
// - Keepalive on connection failing
// - Multiple remote endpoints

/**
 * Accepts messages and dispatches them to a remote {@link ServerSocketFudgeMessageReceiver}.
 * On lifecycle startup this class will open a remote connection to the other side,
 * and maintain that until {@link #stop()} is invoked to terminate the socket.
 *
 */
public class SocketFudgeMessageSender extends AbstractSocketProcess implements FudgeMessageSender {
  private static final Logger s_logger = LoggerFactory.getLogger(SocketFudgeMessageSender.class);
  private final FudgeContext _fudgeContext;
  
  private FudgeMsgWriter _msgWriter;
  
  public SocketFudgeMessageSender() {
    this(FudgeContext.GLOBAL_DEFAULT);
  }
  
  public SocketFudgeMessageSender(FudgeContext fudgeContext) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _fudgeContext = fudgeContext;
  }

  @Override
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @Override
  public synchronized void send(FudgeFieldContainer message) {
    startIfNecessary();
    s_logger.info("Sending message with {} fields", message.getNumFields());
    _msgWriter.writeMessage(message);
  }

  @Override
  protected void socketClosed() {
    super.socketClosed();
    _msgWriter = null;
  }

  @Override
  protected void socketOpened(Socket socket, OutputStream os, InputStream is) {
    _msgWriter = getFudgeContext().createMessageWriter(os);
  }

}
