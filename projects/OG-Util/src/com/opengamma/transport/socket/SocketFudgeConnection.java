/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.transport.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.wire.FudgeMsgReader;
import org.fudgemsg.wire.FudgeRuntimeIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.transport.FudgeConnection;
import com.opengamma.transport.FudgeConnectionStateListener;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.FudgeMessageSender;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.TerminatableJob;

/**
 * A Socket implementation of FudgeConnection
 */
public class SocketFudgeConnection extends AbstractSocketProcess implements FudgeConnection {

  private static final Logger s_logger = LoggerFactory.getLogger(SocketFudgeConnection.class);

  private final FudgeContext _fudgeContext;
  private final ExecutorService _executorService;
  private final MessageBatchingWriter _writer = new MessageBatchingWriter() {

    /**
     * Prevents re-entrant calls to startIfNecessary if a message is sent as part of a connection
     * reset callback.
     */
    private boolean _isSending;

    @Override
    protected void beforeWrite() {
      if (!_isSending) {
        _isSending = true;
        try {
          startIfNecessary();
        } catch (OpenGammaRuntimeException e) {
          if (e.getCause() instanceof IOException) {
            notifyConnectionFailed((IOException) e.getCause());
            // Should we still carry on and throw the exception if the user's been given it as a callback? Maybe allow the connectionFailed callback specify which to rethrow?
          }
          throw e;
        } finally {
          _isSending = false;
        }
      }
    }

  };

  private FudgeMessageReceiver _receiver;
  private TerminatableJob _receiverJob;
  private volatile FudgeConnectionStateListener _stateListener;

  private final FudgeMessageSender _sender = new FudgeMessageSender() {

    @Override
    public FudgeContext getFudgeContext() {
      return _fudgeContext;
    }

    @Override
    public void send(FudgeMsg message) {
      try {
        _writer.write(message);
      } catch (FudgeRuntimeIOException e) {
        if (exceptionForcedByClose(e.getCause())) {
          s_logger.info("Connection terminated - message not sent");
        } else {
          s_logger.warn("I/O exception during send - {} - stopping socket to flush error", e.getCause().getMessage());
          stop();
          notifyConnectionFailed(e);
        }
        throw e;
      }
    }

  };

  /**
   * Creates a connection where received messages are processed inline with socket read operations.
   * 
   * @param fudgeContext the Fudge context, not null
   */
  public SocketFudgeConnection(final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _fudgeContext = fudgeContext;
    _executorService = null;
  }

  /**
   * Creates a connection where received messages run out of thread to the socket reader using the given
   * {@link ExecutorService}. 
   * 
   * @param fudgeContext the Fudge context, not null
   * @param executorService an executor service to run received messages via, not null
   */
  public SocketFudgeConnection(final FudgeContext fudgeContext, final ExecutorService executorService) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(executorService, "executorService");
    _fudgeContext = fudgeContext;
    _executorService = executorService;
  }

  /**
   * Sets a delay before flushing data messages to allow adjacent messages to be coalesced. Only useful if the
   * message sender is being used concurrently.
   * 
   * @param microseconds the time to wait before flushing, or {@code 0} to flush immediately after a message (or coalesced group)
   */
  public void setFlushDelay(final int microseconds) {
    _writer.setFlushDelay(microseconds);
  }

  /**
   * Note that the message sender may be called concurrently. All messages will be sent from a single thread
   * with others returning immediately. Thus successful completion of a {@link FudgeMessageSender#send} does
   * not guarantee message arrival or that it has even been (or will be) passed to the transport.
   * 
   * @return the Fudge message sender component of the connection
   */
  @Override
  public FudgeMessageSender getFudgeMessageSender() {
    return _sender;
  }

  @Override
  public void setFudgeMessageReceiver(final FudgeMessageReceiver receiver) {
    _receiver = receiver;
  }

  @Override
  protected void socketOpened(Socket socket, BufferedOutputStream os, BufferedInputStream is) {
    final FudgeMsgReader reader = _fudgeContext.createMessageReader(is);
    _writer.setFudgeMsgWriter(_fudgeContext, os);
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
            notifyConnectionFailed(e);
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
          if (_executorService != null) {
            _executorService.execute(new Runnable() {
              @Override
              public void run() {
                dispatch(receiver, envelope);
              }
            });
          } else {
            dispatch(receiver, envelope);
          }
        }
      }

      private void dispatch(final FudgeMessageReceiver receiver, final FudgeMsgEnvelope envelope) {
        try {
          receiver.messageReceived(_fudgeContext, envelope);
        } catch (Exception e) {
          s_logger.warn("Unable to dispatch message to receiver", e);
        }
      }

    };
    final Thread thread = new Thread(_receiverJob, "Incoming " + socket.getRemoteSocketAddress());
    thread.setDaemon(true);
    thread.start();
    // We don't keep hold of the thread as we're never going to join it; terminating the socket will let cause it to stop, finish and be GCd
    final FudgeConnectionStateListener stateListener = _stateListener;
    if (stateListener != null) {
      stateListener.connectionReset(this);
    }
  }

  @Override
  protected void socketClosed() {
    _writer.setFudgeMsgWriter(null);
    _receiverJob.terminate();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("FudgeConnection to ");
    sb.append(getInetAddresses());
    sb.append(':');
    sb.append(getPortNumber());
    if (!isRunning()) {
      sb.append(" (not connected)");
    }
    return sb.toString();
  }

  @Override
  public void setConnectionStateListener(FudgeConnectionStateListener listener) {
    _stateListener = listener;
  }
  
  protected void notifyConnectionFailed(Exception e) {
    final FudgeConnectionStateListener stateListener = _stateListener;
    if (stateListener != null) {
      try {
        stateListener.connectionFailed(SocketFudgeConnection.this, e);
      } catch (Exception e2) {
        s_logger.warn("Error notifying state listener of connection failure", e2);
      }
    }
  }

}
