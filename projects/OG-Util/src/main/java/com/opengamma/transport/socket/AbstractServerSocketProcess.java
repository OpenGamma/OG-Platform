/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.transport.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.transport.EndPointDescriptionProvider;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.TerminatableJob;
import com.opengamma.util.ThreadUtils;

/**
 * 
 *
 */
public abstract class AbstractServerSocketProcess implements Lifecycle, EndPointDescriptionProvider {
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractServerSocketProcess.class);

  private final ExecutorService _executorService;

  private int _portNumber;
  private InetAddress _bindAddress;
  private boolean _isDaemon = true;

  private ServerSocket _serverSocket;
  private Thread _socketAcceptThread;
  private SocketAcceptJob _socketAcceptJob;
  private boolean _started;

  protected AbstractServerSocketProcess() {
    _executorService = null;
  }

  protected AbstractServerSocketProcess(final ExecutorService executorService) {
    ArgumentChecker.notNull(executorService, "executorService");
    _executorService = executorService;
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
   * @param isDaemon true if the socket accept thread should be run as a daemon thread, false otherwise
   */
  public void setDaemon(final boolean isDaemon) {
    _isDaemon = isDaemon;
  }

  /**
   * @return true if the socket accept thread should be run as a daemon thread, false otherwise
   */
  public boolean isDaemon() {
    return _isDaemon;
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
    _socketAcceptThread.setDaemon(_isDaemon);
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

    ThreadUtils.safeJoin(_socketAcceptThread, 60 * 1000L);
    try {
      _serverSocket.close();
    } catch (IOException e) {
      s_logger.warn("Unable to close server socket on lifecycle stop", e);
    }

    _started = false;
  }

  protected boolean exceptionForcedByClose(final Exception e) {
    return (e instanceof SocketException) && "Socket closed".equals(e.getMessage());
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

  protected ExecutorService getExecutorService() {
    return _executorService;
  }

  protected abstract void socketOpened(Socket socket);

  private void loadInterfaceAddress(final NetworkInterface iface, final MutableFudgeMsg message) {
    final Enumeration<NetworkInterface> ni = iface.getSubInterfaces();
    while (ni.hasMoreElements()) {
      loadInterfaceAddress(ni.nextElement(), message);
    }
    final Enumeration<InetAddress> ai = iface.getInetAddresses();
    while (ai.hasMoreElements()) {
      final InetAddress a = ai.nextElement();
      if (a.isLoopbackAddress()) {
        continue;
      }
      final String hostAddress = a.getHostAddress();
      message.add(SocketEndPointDescriptionProvider.ADDRESS_KEY, hostAddress);
      s_logger.debug("Address {}/{}", iface.getName(), hostAddress);
    }
  }

  @Override
  public FudgeMsg getEndPointDescription(final FudgeContext fudgeContext) {
    final MutableFudgeMsg desc = fudgeContext.newMessage();
    desc.add(SocketEndPointDescriptionProvider.TYPE_KEY, SocketEndPointDescriptionProvider.TYPE_VALUE);
    final InetAddress addr = _serverSocket.getInetAddress();
    if (addr != null) {
      if (addr.isAnyLocalAddress()) {
        try {
          Enumeration<NetworkInterface> ni = NetworkInterface.getNetworkInterfaces();
          while (ni.hasMoreElements()) {
            loadInterfaceAddress(ni.nextElement(), desc);
          }
        } catch (IOException e) {
          s_logger.warn("Error resolving local addresses", e);
        }
      } else {
        desc.add(SocketEndPointDescriptionProvider.ADDRESS_KEY, addr.getHostAddress());
      }
    }
    desc.add(SocketEndPointDescriptionProvider.PORT_KEY, _serverSocket.getLocalPort());
    return desc;
  }

}
