/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.transport.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.FudgeMsgReader;
import org.fudgemsg.FudgeMsgWriter;
import org.fudgemsg.FudgeRuntimeIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.transport.FudgeConnection;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.FudgeMessageSender;
import com.opengamma.util.TerminatableJob;

/**
 * A Socket implementation of FudgeConnection
 */
public class SocketFudgeConnection extends AbstractSocketProcess implements FudgeConnection {

  private static final Logger s_logger = LoggerFactory.getLogger(SocketFudgeConnection.class);

  private final FudgeContext _fudgeContext;

  private FudgeMessageReceiver _receiver;
  private FudgeMsgWriter _msgWriter;
  private TerminatableJob _receiverJob;

  private final FudgeMessageSender _sender = new FudgeMessageSender() {

    @Override
    public FudgeContext getFudgeContext() {
      return _fudgeContext;
    }

    @Override
    public void send(FudgeFieldContainer message) {
      startIfNecessary();
      try {
        _msgWriter.writeMessage(message);
      } catch (FudgeRuntimeIOException e) {
        if (exceptionForcedByClose(e.getCause())) {
          s_logger.info("Connection terminated - message not sent");
        } else {
          s_logger.warn("I/O exception during send - {} - stopping socket to flush error", e.getCause().getMessage());
          stop();
        }
        throw e;
      }
    }

  };

  public SocketFudgeConnection(final FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
  }

  @Override
  public FudgeMessageSender getFudgeMessageSender() {
    return _sender;
  }

  @Override
  public void setFudgeMessageReceiver(final FudgeMessageReceiver receiver) {
    _receiver = receiver;
  }

  @Override
  protected void socketOpened(Socket socket, OutputStream os, InputStream is) {
    final FudgeMsgReader reader = _fudgeContext.createMessageReader(new BufferedInputStream(is));
    _msgWriter = _fudgeContext.createMessageWriter(new BufferedOutputStream(os));
    _receiverJob = new TerminatableJob() {

      @Override
      protected void runOneCycle() {
        final FudgeMsgEnvelope envelope;
        try {
          envelope = reader.nextMessageEnvelope();
        } catch (FudgeRuntimeIOException e) {
          if (exceptionForcedByClose(e.getCause())) {
            s_logger.info("Connection terminated");
          } else {
            s_logger.warn("I/O exception during recv - {} - stopping socket to flush error", e.getCause());
            stop();
          }
          return;
        }
        if (envelope == null) {
          s_logger.info("Nothing available on stream. Terminating connection");
          stop();
          return;
        }
        final FudgeMessageReceiver receiver = _receiver;
        if (receiver != null) {
          try {
            receiver.messageReceived(_fudgeContext, envelope);
          } catch (Exception e) {
            s_logger.warn("Unable to dispatch message to receiver", e);
          }
        }
      }

    };
    final Thread thread = new Thread(_receiverJob, "Incoming " + getInetAddress().toString() + ":" + getPortNumber());
    thread.setDaemon(true);
    thread.start();
    // We don't keep hold of the thread as we're never going to join it; terminating the job will let it finish and be GCd
  }

  @Override
  protected void socketClosed() {
    _msgWriter = null;
    _receiverJob.terminate();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("FudgeConnection to ");
    sb.append(getInetAddress());
    sb.append(':');
    sb.append(getPortNumber());
    if (!isRunning()) {
      sb.append(" (not connected)");
    }
    return sb.toString();
  }

}
