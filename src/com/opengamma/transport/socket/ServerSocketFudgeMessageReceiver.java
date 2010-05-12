/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.socket;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.FudgeMsgReader;
import org.fudgemsg.FudgeRuntimeIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.TerminatableJob;
import com.opengamma.util.ThreadUtil;

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
 * Each message will be handed to the underlying {@link FudgeMessageReceiver} in the
 * same thread as the messages are consumed, so the underlying receiver must be threadsafe,
 * and should not block except where it is fine to block the remote end from publishing
 * during consumption.
 *
 * @author kirk
 */
public class ServerSocketFudgeMessageReceiver implements Lifecycle {
  private static final Logger s_logger = LoggerFactory.getLogger(ServerSocketFudgeMessageReceiver.class);
  private final FudgeMessageReceiver _underlying;
  private final FudgeContext _context;
  private int _portNumber = 0;
  private InetAddress _bindAddress;
  
  private ServerSocket _serverSocket;
  private Thread _socketAcceptThread;
  private SocketAcceptJob _socketAcceptJob;
  private boolean _started = false;
  private Set<SingleMessageReceiveJob> _messageReceiveJobs = new HashSet<SingleMessageReceiveJob>();
  
  public ServerSocketFudgeMessageReceiver(FudgeMessageReceiver underlying) {
    this(underlying, FudgeContext.GLOBAL_DEFAULT);
  }
  
  public ServerSocketFudgeMessageReceiver(FudgeMessageReceiver underlying, FudgeContext context) {
    ArgumentChecker.notNull(underlying, "underlying Fudge message receiver");
    ArgumentChecker.notNull(context, "Fudge context");
    _underlying = underlying;
    _context = context;
  }
  
  /**
   * @return the portNumber
   */
  public int getPortNumber() {
    return _portNumber;
  }

  /**
   * @param portNumber the portNumber to set
   */
  public void setPortNumber(int portNumber) {
    _portNumber = portNumber;
  }

  /**
   * @return the bindAddress
   */
  public InetAddress getBindAddress() {
    return _bindAddress;
  }

  /**
   * @param bindAddress the bindAddress to set
   */
  public void setBindAddress(InetAddress bindAddress) {
    _bindAddress = bindAddress;
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
  public synchronized boolean isRunning() {
    return _started;
  }

  @Override
  public synchronized void start() {
    // NOTE kirk 2010-05-12 -- Backlog of 50 from ServerSocket.
    try {
      _serverSocket = new ServerSocket(getPortNumber(), 50, getBindAddress());
      _portNumber = _serverSocket.getLocalPort();
    } catch (IOException ioe) {
      throw new OpenGammaRuntimeException("Unable to bind to " + getBindAddress() + " port " + getPortNumber(), ioe);
    }
    
    _socketAcceptJob = new SocketAcceptJob();
    _socketAcceptThread = new Thread(_socketAcceptJob, "Socket Accept Thread");
    _socketAcceptThread.setDaemon(true);
    _socketAcceptThread.start();
    
    _started = true;
  }

  @Override
  public synchronized void stop() {
    _socketAcceptJob.terminate();
    // Open a socket locally in case we're trapped in .accept().
    try {
      Socket socket = new Socket(InetAddress.getLocalHost(), getPortNumber());
      socket.close();
    } catch (IOException e) {
      // Totally fine.
    }

    ThreadUtil.safeJoin(_socketAcceptThread, 60*1000l);
    try {
      _serverSocket.close();
    } catch (IOException e) {
      s_logger.warn("Unable to close server socket on lifecycle stop", e);
    }
    
    _started = false;
  }
  
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
    
    SingleMessageReceiveJob job = new SingleMessageReceiveJob(socket, is);
    Thread t = new Thread(job, "Message receive and dispatch");
    t.setDaemon(true);
    _messageReceiveJobs.add(job);
    s_logger.info("Starting thread ID {} to handle messages from {}", t.getId(), socket.getRemoteSocketAddress());
    t.start();
  }
  
  protected synchronized void cleanRunningReceiveJobs() {
    Iterator<SingleMessageReceiveJob> jobIter = _messageReceiveJobs.iterator();
    while (jobIter.hasNext()) {
      SingleMessageReceiveJob job = jobIter.next();
      if(job.isTerminated()) {
        s_logger.debug("Removing terminated job");
        jobIter.remove();
      }
    }
  }
  
  private class SocketAcceptJob extends TerminatableJob {
    @Override
    protected void runOneCycle() {
      cleanRunningReceiveJobs();
      try {
        Socket socket = _serverSocket.accept();
        // Double-check here because we sometimes open sockets just to force
        // termination.
        if(!isTerminated()) {
          socketOpened(socket);
        }
      } catch (IOException e) {
        s_logger.warn("Unable to accept a new connection", e);
      }
    }
  }
  
  private class SingleMessageReceiveJob extends TerminatableJob {
    private final Socket _socket;
    private final FudgeMsgReader _reader;

    // NOTE kirk 2010-05-12 -- Have to pass in the InputStream explicitly so that
    // we can force the IOException catch up above.
    public SingleMessageReceiveJob(Socket socket, InputStream inputStream) {
      ArgumentChecker.notNull(socket, "Socket");
      ArgumentChecker.notNull(inputStream, "Socket input stream");
      _socket = socket;
      _reader = _context.createMessageReader(inputStream);
    }
    
    @Override
    protected void runOneCycle() {
      if(_socket.isClosed()) {
        terminate();
        return;
      }
    
      FudgeMsgEnvelope envelope = null;
      try {
        envelope = _reader.nextMessageEnvelope();
      } catch (FudgeRuntimeIOException frio) {
        if(frio.getCause() instanceof EOFException) {
          // Special case here specifically for sockets.
          s_logger.info("Detected that underlying socket is closed but not registering on _socket yet, closing");
          terminate();
          return;
        }
        s_logger.warn("Unable to read message from underlying stream", frio);
      } catch (Exception e) {
        s_logger.warn("Unable to read message from underlying stream", e);
        return;
      }

      try {
        s_logger.debug("Received message with {} fields. Dispatching to underlying.", envelope.getMessage().getNumFields());
        getUnderlying().messageReceived(getContext(), envelope);
      } catch (Exception e) {
        s_logger.warn("Unable to dispatch message to underlying receiver", e);
        return;
      }
    }
  }

}
