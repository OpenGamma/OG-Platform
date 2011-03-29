/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.socket;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.wire.FudgeMsgReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.TerminatableJob;
import com.opengamma.util.TerminatableJobContainer;

// REVIEW kirk 2010-05-12 -- Potential extensions in the future:
// - Use NIO for a large number of seldom broadcasting sockets
// - Allow for single-message receiving sockets that close themselves.

/**
 * Listens on a server socket, receives Fudge-encoded messages, and hands them
 * off to an underlying receiver for processing.
 * An example use case here is a server process that receives messages from
 * other nodes for asynchronous processing (such as a log aggregation server).
 * <p/>
 * This class will create one thread for each open external socket, as well as one
 * thread to accept new sockets from the {@code ServerSocket}.
 * <p>
 * Each message will be handed to the underlying {@link FudgeMessageReceiver} in either
 * the same thread as the messages are consumed (unless an executor service is
 * supplied), so the underlying receiver must be threadsafe, and should not block except
 * where it is fine to block the remote end from publishing during consumption.
 *
 * @author kirk
 */
public class ServerSocketFudgeMessageReceiver extends AbstractServerSocketProcess {
  private static final Logger s_logger = LoggerFactory.getLogger(ServerSocketFudgeMessageReceiver.class);
  private final FudgeMessageReceiver _underlying;
  private final FudgeContext _context;

  private final TerminatableJobContainer _messageReceiveJobs = new TerminatableJobContainer();
  
  public ServerSocketFudgeMessageReceiver(final FudgeMessageReceiver underlying, final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _underlying = underlying;
    _context = fudgeContext;
  }
  
  public ServerSocketFudgeMessageReceiver(final FudgeMessageReceiver underlying, final FudgeContext fudgeContext, final ExecutorService executorService) {
    super(executorService);
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _underlying = underlying;
    _context = fudgeContext;
  }
  
  /**
   * @return the underlying
   */
  public FudgeMessageReceiver getUnderlying() {
    return _underlying;
  }

  /**
   * @return the context
   */
  public FudgeContext getContext() {
    return _context;
  }

  @Override
  protected synchronized void socketOpened(Socket socket) {
    ArgumentChecker.notNull(socket, "socket");
    s_logger.info("Opened socket to remote side {}", socket.getRemoteSocketAddress());
    InputStream is;
    try {
      is = socket.getInputStream();
    } catch (IOException e) {
      s_logger.warn("Unable to open InputStream for socket {}", new Object[]{socket}, e);
      return;
    }

    is = new BufferedInputStream(is);
    MessageReceiveJob job = new MessageReceiveJob(socket, is);
    _messageReceiveJobs.addJobAndStartThread(job, "Message Receive " + socket.getRemoteSocketAddress());
  }
  
  @Override
  protected void cleanupPreAccept() {
    _messageReceiveJobs.cleanupTerminatedInstances();
  }

  private class MessageReceiveJob extends TerminatableJob {
    private final Socket _socket;
    private final FudgeMsgReader _reader;

    // NOTE kirk 2010-05-12 -- Have to pass in the InputStream explicitly so that
    // we can force the IOException catch up above.
    public MessageReceiveJob(Socket socket, InputStream inputStream) {
      ArgumentChecker.notNull(socket, "socket");
      ArgumentChecker.notNull(inputStream, "inputStream");
      _socket = socket;
      _reader = _context.createMessageReader(inputStream);
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
      } catch (Exception e) {
        s_logger.warn("Unable to read message from underlying stream", e);
        return;
      }
      
      if (envelope == null) {
        s_logger.info("Nothing available on the stream. Returning and terminating.");
        terminate();
        return;
      }

      final ExecutorService executorService = getExecutorService();
      if (executorService != null) {
        executorService.execute(new Runnable() {
          @Override
          public void run() {
            dispatch(envelope);
          }
        });
      } else {
        dispatch(envelope);
      }
    }

    private void dispatch(final FudgeMsgEnvelope envelope) {
      try {
        s_logger.debug("Received message with {} fields. Dispatching to underlying.", envelope.getMessage().getNumFields());
        getUnderlying().messageReceived(getContext(), envelope);
      } catch (Exception e) {
        s_logger.warn("Unable to dispatch message to underlying receiver", e);
      }
    }
  }

}
