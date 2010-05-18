/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.FudgeMsgReader;
import org.fudgemsg.FudgeMsgWriter;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.transport.FudgeRequestReceiver;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.TerminatableJob;
import com.opengamma.util.TerminatableJobContainer;

/**
 * Receives Fudge encoded requests from raw sockets and dispatches them
 * to a {@link FudgeRequestReceiver} and returns results over the same socket.
 *
 */
public class ServerSocketFudgeRequestDispatcher extends AbstractServerSocketProcess {
  private static final Logger s_logger = LoggerFactory.getLogger(ServerSocketFudgeRequestDispatcher.class);
  private final FudgeRequestReceiver _underlying;
  private final FudgeContext _fudgeContext;
  private final FudgeDeserializationContext _fudgeDeserializationContext;

  private final TerminatableJobContainer _messageReceiveJobs = new TerminatableJobContainer();
  
  public ServerSocketFudgeRequestDispatcher(FudgeRequestReceiver underlying) {
    this(underlying, FudgeContext.GLOBAL_DEFAULT);
  }
  
  public ServerSocketFudgeRequestDispatcher(FudgeRequestReceiver underlying, FudgeContext fudgeContext) {
    ArgumentChecker.notNull(underlying, "Underlying request receiver");
    ArgumentChecker.notNull(fudgeContext, "Fudge context");
    _underlying = underlying;
    _fudgeContext = fudgeContext;
    _fudgeDeserializationContext = new FudgeDeserializationContext(_fudgeContext);
  }

  /**
   * @return the underlying
   */
  public FudgeRequestReceiver getUnderlying() {
    return _underlying;
  }

  /**
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * @return the fudgeDeserializationContext
   */
  public FudgeDeserializationContext getFudgeDeserializationContext() {
    return _fudgeDeserializationContext;
  }

  @Override
  protected synchronized void socketOpened(Socket socket) {
    ArgumentChecker.notNull(socket, "socket");
    s_logger.info("Opened socket to remote side {}", socket.getRemoteSocketAddress());
    InputStream is;
    OutputStream os;
    try {
      is = socket.getInputStream();
      os = socket.getOutputStream();
    } catch (IOException e) {
      s_logger.warn("Unable to open InputStream and OutputStream for socket {}", new Object[]{socket}, e);
      return;
    }
    
    RequestDispatchJob job = new RequestDispatchJob(socket, is, os);
    _messageReceiveJobs.addJobAndStartThread(job, "Request Dispatch " + socket.getRemoteSocketAddress());
  }
  
  private class RequestDispatchJob extends TerminatableJob {
    private final Socket _socket;
    private final FudgeMsgReader _reader;
    private final FudgeMsgWriter _writer;

    // NOTE kirk 2010-05-12 -- Have to pass in the InputStream and OutputStream explicitly so that
    // we can force the IOException catch up above.
    public RequestDispatchJob(Socket socket, InputStream inputStream, OutputStream outputStream) {
      ArgumentChecker.notNull(socket, "Socket");
      ArgumentChecker.notNull(inputStream, "Socket input stream");
      ArgumentChecker.notNull(outputStream, "Socket output stream");
      _socket = socket;
      _reader = getFudgeContext().createMessageReader(inputStream);
      _writer = getFudgeContext().createMessageWriter(outputStream);
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
      } catch (Exception e) {
        s_logger.warn("Unable to read message from underlying stream", e);
        return;
      }
      
      if (envelope == null) {
        s_logger.info("Nothing available on the stream. Returning and terminating.");
        terminate();
        return;
      }

      FudgeFieldContainer response = null;
      try {
        s_logger.debug("Received message with {} fields. Dispatching to underlying.", envelope.getMessage().getNumFields());
        response = getUnderlying().requestReceived(getFudgeDeserializationContext(), envelope);
      } catch (Exception e) {
        s_logger.warn("Unable to dispatch message to underlying receiver", e);
        return;
      }
      
      try {
        s_logger.debug("Sending response with {} fields.", envelope.getMessage().getNumFields());
        _writer.writeMessage(response);
      } catch (Exception e) {
        s_logger.warn("Unable to dispatch response to client", e);
        return;
      }
      
    }
  }

}
