/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.TerminatableJob;
import com.opengamma.util.ThreadUtil;

/**
 * 
 *
 */
public abstract class AbstractServerSocketProcess implements Lifecycle {
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractServerSocketProcess.class);
  private int _portNumber;
  private InetAddress _bindAddress;

  private ServerSocket _serverSocket;
  private Thread _socketAcceptThread;
  private SocketAcceptJob _socketAcceptJob;
  private boolean _started;
  
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

  @Override
  public synchronized boolean isRunning() {
    return _started;
  }

  @Override
  public synchronized void start() {
    s_logger.info("Binding to {}:{}", getBindAddress(), getPortNumber());
    try {
      // NOTE kirk 2010-05-12 -- Backlog of 50 from ServerSocket.
      _serverSocket = new ServerSocket(getPortNumber(), 50, getBindAddress());
      if (getPortNumber() == 0) {
        s_logger.info("Received inbound port {}", _serverSocket.getLocalPort());
      }
      setPortNumber(_serverSocket.getLocalPort());
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

    ThreadUtil.safeJoin(_socketAcceptThread, 60 * 1000L);
    try {
      _serverSocket.close();
    } catch (IOException e) {
      s_logger.warn("Unable to close server socket on lifecycle stop", e);
    }
    
    _started = false;
  }
  
  private class SocketAcceptJob extends TerminatableJob {
    @Override
    protected void runOneCycle() {
      cleanupPreAccept();
      try {
        Socket socket = _serverSocket.accept();
        // Double-check here because we sometimes open sockets just to force
        // termination.
        if (!isTerminated()) {
          socketOpened(socket);
        }
      } catch (IOException e) {
        s_logger.warn("Unable to accept a new connection", e);
      }
    }
  }
  
  protected void cleanupPreAccept() {
  }
  
  protected abstract void socketOpened(Socket socket);
  
}
