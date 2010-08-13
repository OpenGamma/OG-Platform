/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.transport.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
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
import com.opengamma.transport.FudgeConnectionReceiver;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.FudgeMessageSender;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.TerminatableJob;
import com.opengamma.util.TerminatableJobContainer;

/**
 * Listens on a ServerSocket and passes FudgeConnections to an underlying FudgeConnectionReceiver
 */
public class ServerSocketFudgeConnectionReceiver extends AbstractServerSocketProcess {

  private static final Logger s_logger = LoggerFactory.getLogger(ServerSocketFudgeConnectionReceiver.class);

  private final FudgeConnectionReceiver _underlying;
  private final FudgeContext _fudgeContext;

  private final TerminatableJobContainer _connectionJobs = new TerminatableJobContainer();

  public ServerSocketFudgeConnectionReceiver(final FudgeContext fudgeContext, final FudgeConnectionReceiver underlying) {
    _fudgeContext = fudgeContext;
    _underlying = underlying;
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  public FudgeConnectionReceiver getUnderlying() {
    return _underlying;
  }

  @Override
  protected void socketOpened(Socket socket) {
    ArgumentChecker.notNull(socket, "socket");
    s_logger.info("Opened socket to remote side {}", socket.getRemoteSocketAddress());
    InputStream is;
    OutputStream os;
    try {
      is = socket.getInputStream();
      os = socket.getOutputStream();
    } catch (IOException e) {
      s_logger.warn("Unable to open InputStream and OutputStream for socket {}", new Object[] {socket}, e);
      return;
    }
    final ConnectionJob job = new ConnectionJob(socket, is, os);
    _connectionJobs.addJobAndStartThread(job, "Connection dispatch " + socket.getRemoteSocketAddress());
  }

  @Override
  protected void cleanupPreAccept() {
    _connectionJobs.cleanupTerminatedInstances();
  }

  @Override
  public void stop() {
    super.stop();
    _connectionJobs.terminateAll();
  }

  private class ConnectionJob extends TerminatableJob {

    private final Socket _socket;
    private final FudgeMsgReader _reader;
    private final FudgeMsgWriter _writer;
    private final FudgeMessageSender _sender;
    private final FudgeConnection _connection;
    private FudgeMessageReceiver _receiver;

    ConnectionJob(final Socket socket, final InputStream is, final OutputStream os) {
      _socket = socket;
      _reader = getFudgeContext().createMessageReader(new BufferedInputStream(is));
      _writer = getFudgeContext().createMessageWriter(new BufferedOutputStream(os));
      _sender = new FudgeMessageSender() {

        @Override
        public FudgeContext getFudgeContext() {
          return ServerSocketFudgeConnectionReceiver.this.getFudgeContext();
        }

        @Override
        public void send(FudgeFieldContainer message) {
          _writer.writeMessage(message);
        }

      };
      _connection = new FudgeConnection() {

        @Override
        public FudgeMessageSender getFudgeMessageSender() {
          return _sender;
        }

        @Override
        public void setFudgeMessageReceiver(FudgeMessageReceiver receiver) {
          _receiver = receiver;
        }

      };
    }

    @Override
    protected void runOneCycle() {
      if (_socket.isClosed()) {
        terminate();
        return;
      }
      FudgeMsgEnvelope envelope = null;
      try {
        envelope = _reader.nextMessageEnvelope();
      } catch (FudgeRuntimeIOException e) {
        if (exceptionForcedByClose(e.getCause())) {
          s_logger.info("Connection terminated");
        } else {
          s_logger.warn("Unable to read message from underlying stream - terminating connection", e.getCause());
          terminate();
        }
        return;
      }
      if (envelope == null) {
        s_logger.info("Nothing available on stream. Terminating connection");
        terminate();
        return;
      }
      final FudgeMessageReceiver receiver = _receiver;
      if (receiver != null) {
        try {
          receiver.messageReceived(getFudgeContext(), envelope);
        } catch (Exception e) {
          s_logger.warn("Unable to dispatch message to receiver", e);
        }
      } else {
        try {
          getUnderlying().connectionReceived(getFudgeContext(), envelope, _connection);
        } catch (Exception e) {
          s_logger.warn("Unable to dispatch connection to receiver", e);
        }
      }
    }

    @Override
    public void terminate() {
      if (!_socket.isClosed()) {
        try {
          s_logger.debug("Closing socket");
          _socket.close();
        } catch (IOException ex) {
          s_logger.warn("Couldn't close socket to release blocked I/O", ex.getMessage());
        }
      }
      super.terminate();
    }

  }

}
