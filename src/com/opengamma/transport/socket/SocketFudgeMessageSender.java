/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.socket;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

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

  private Queue<FudgeFieldContainer> _messagesToWrite;
  
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
  public void send(FudgeFieldContainer message) {
    synchronized (this) {
      if (_messagesToWrite != null) {
        s_logger.debug("Deferring message with {} fields to another thread", message.getNumFields());
        _messagesToWrite.add(message);
        return;
      } else {
        _messagesToWrite = new LinkedList<FudgeFieldContainer>();
      }
    }
    boolean clearPointer = true;
    try {
      startIfNecessary();
      do {
        s_logger.info("Sending message with {} fields", message.getNumFields());
        _msgWriter.writeMessage(message);
        synchronized (this) {
          message = _messagesToWrite.poll();
          if (message == null) {
            _messagesToWrite = null;
            clearPointer = false;
          }
        }
      } while (message != null);
    } finally {
      synchronized (this) {
        if (clearPointer) {
          _messagesToWrite = null;
        }
      }
    }
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
