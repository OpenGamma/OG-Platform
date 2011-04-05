/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.transport.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.wire.FudgeMsgReader;
import org.fudgemsg.wire.FudgeRuntimeIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.transport.FudgeConnection;
import com.opengamma.transport.FudgeConnectionReceiver;
import com.opengamma.transport.FudgeConnectionStateListener;
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

  private boolean _lazyFudgeMsgReads;

  public ServerSocketFudgeConnectionReceiver(final FudgeContext fudgeContext, final FudgeConnectionReceiver underlying) {
    _fudgeContext = fudgeContext;
    _underlying = underlying;
  }

  public ServerSocketFudgeConnectionReceiver(final FudgeContext fudgeContext, final FudgeConnectionReceiver underlying,
      final ExecutorService executorService) {
    super(executorService);
    _fudgeContext = fudgeContext;
    _underlying = underlying;
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  public FudgeConnectionReceiver getUnderlying() {
    return _underlying;
  }

  public void setLazyFudgeMsgReads(final boolean lazyFudgeMsgReads) {
    _lazyFudgeMsgReads = lazyFudgeMsgReads;
  }

  public boolean isLazyFudgeMsgReads() {
    return _lazyFudgeMsgReads;
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

  /**
   * An output stream with buffered behavior that only writes to the underlying stream when
   * the buffer is full or when a flush takes place. This is different to the behavior of
   * {@link BufferedOutputStream} which may make partial writes if given data larger than
   * its internal buffer. This class is best used over network transports that have an optimal
   * message size.
   * <p>
   * Note that it is not thread-safe.
   */
  private static class StrictBufferedOutputStream extends FilterOutputStream {

    // TODO: move this out into Util and use for other network buffered outputs
    // TODO: set the packet size from the MTU details of the network used

    private final byte[] _buffer;
    private int _bytes;

    public StrictBufferedOutputStream(final OutputStream out, final int bytes) {
      super(out);
      ArgumentChecker.isTrue(bytes > 0, "bytes");
      _buffer = new byte[bytes];
    }

    public StrictBufferedOutputStream(final OutputStream out) {
      this(out, 1500);
    }

    @Override
    public void write(final int b) throws IOException {
      _buffer[_bytes++] = (byte) b;
      if (_bytes == _buffer.length) {
        out.write(_buffer);
        _bytes = 0;
      }
    }

    @Override
    public void write(final byte[] b, int ofs, int len) throws IOException {
      if (_bytes > 0) {
        final int room = _buffer.length - _bytes;
        if (room < len) {
          // Start of packet fits in buffer
          System.arraycopy(b, ofs, _buffer, _bytes, room);
          out.write(_buffer);
          _bytes = 0;
          ofs += room;
          len -= room;
        } else {
          System.arraycopy(b, ofs, _buffer, _bytes, len);
          if (room == len) {
            // Packet just fits in buffer
            System.arraycopy(b, ofs, _buffer, _bytes, len);
            out.write(_buffer);
            _bytes = 0;
          } else {
            // Packet fits entirely in buffer
            _bytes += len;
          }
          return;
        }
      }
      while (len >= _buffer.length) {
        // Part of the packet can be sent directly
        out.write(b, ofs, _buffer.length);
        ofs += _buffer.length;
        len -= _buffer.length;
      }
      if (len > 0) {
        // Part of the packet is left
        System.arraycopy(b, ofs, _buffer, _bytes, len);
        _bytes += len;
      }
    }

    @Override
    public void flush() throws IOException {
      if (_bytes > 0) {
        out.write(_buffer, 0, _bytes);
        _bytes = 0;
        out.flush();
      }
    }

  }

  private class ConnectionJob extends TerminatableJob {

    private final Socket _socket;
    private final FudgeMsgReader _reader;
    private final FudgeMessageSender _sender;
    private final FudgeConnection _connection;
    private FudgeMessageReceiver _receiver;
    private volatile FudgeConnectionStateListener _listener;

    ConnectionJob(final Socket socket, final InputStream is, final OutputStream os) {
      _socket = socket;
      _reader = getFudgeContext().createMessageReader(new BufferedInputStream(is));
      _reader.setLazyReads(isLazyFudgeMsgReads());
      _sender = new FudgeMessageSender() {

        private final MessageBatchingWriter _writer = new MessageBatchingWriter(getFudgeContext(),
            new StrictBufferedOutputStream(os));

        @Override
        public FudgeContext getFudgeContext() {
          return ServerSocketFudgeConnectionReceiver.this.getFudgeContext();
        }

        @Override
        public void send(FudgeMsg message) {
          try {
            _writer.write(message);
          } catch (FudgeRuntimeIOException e) {
            terminateWithError("Unable to write message to underlying stream - terminating connection", e.getCause());
            throw e;
          }
        }

        @Override
        public String toString() {
          return _socket.getRemoteSocketAddress().toString();
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

        @Override
        public String toString() {
          final StringBuilder sb = new StringBuilder();
          sb.append("FudgeConnection from ");
          sb.append(_socket.getRemoteSocketAddress().toString());
          return sb.toString();
        }

        @Override
        public void setConnectionStateListener(final FudgeConnectionStateListener listener) {
          _listener = listener;
        }

      };
    }

    @Override
    protected void runOneCycle() {
      if (_socket.isClosed()) {
        terminate();
        return;
      }
      final FudgeMsgEnvelope envelope;
      try {
        envelope = _reader.nextMessageEnvelope();
      } catch (FudgeRuntimeIOException e) {
        terminateWithError("Unable to read message from underlying stream - terminating connection", e.getCause());
        return;
      }
      if (envelope == null) {
        terminateWithError("Nothing available on stream - terminating connection", null);
        return;
      }
      final FudgeMessageReceiver receiver = _receiver;
      if (receiver != null) {
        final ExecutorService executorService = getExecutorService();
        if (executorService != null) {
          executorService.execute(new Runnable() {
            @Override
            public void run() {
              dispatchReceiver(receiver, envelope);
            }
          });
        } else {
          dispatchReceiver(receiver, envelope);
        }
      } else {
        try {
          getUnderlying().connectionReceived(getFudgeContext(), envelope, _connection);
        } catch (Exception e) {
          s_logger.warn("Unable to dispatch connection to receiver", e);
        }
      }
    }

    private void dispatchReceiver(final FudgeMessageReceiver receiver, final FudgeMsgEnvelope envelope) {
      try {
        receiver.messageReceived(getFudgeContext(), envelope);
      } catch (Exception e) {
        s_logger.warn("Unable to dispatch message to receiver", e);
      }
    }

    private void terminateWithError(final String errorMessage, final Exception cause) {
      if (cause != null) {
        if (exceptionForcedByClose(cause)) {
          s_logger.info("Connection terminated");
        } else {
          s_logger.warn(errorMessage, cause);
          terminate();
        }
      } else {
        s_logger.info(errorMessage);
        terminate();
      }
      final FudgeConnectionStateListener listener = _listener;
      if (listener != null) {
        listener.connectionFailed(_connection, cause);
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
