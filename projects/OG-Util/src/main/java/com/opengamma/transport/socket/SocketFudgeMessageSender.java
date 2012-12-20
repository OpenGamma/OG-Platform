/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
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

  private MessageBatchingWriter _writer;

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

  /**
   * Note that a return from a call to {@link #send} does not guarantee the message has been
   * received, or even sent. There may be buffering on the transport but also we batch
   * messages up to a single thread if this is called concurrently.
   * 
   * @param message message to send
   */
  @Override
  public void send(FudgeMsg message) {
    startIfNecessary();
    s_logger.info("Sending message with {} fields", message.getNumFields());
    _writer.write(message);
  }

  @Override
  protected void socketClosed() {
    super.socketClosed();
    _writer = null;
  }

  @Override
  protected void socketOpened(Socket socket, BufferedOutputStream os, BufferedInputStream is) {
    _writer = new MessageBatchingWriter(getFudgeContext(), os);
  }

}
