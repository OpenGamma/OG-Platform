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
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.transport.EndPointDescriptionProvider;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.TerminatableJob;
import com.opengamma.util.ThreadUtil;

/**
 * 
 *
 */
public abstract class AbstractServerSocketProcess implements Lifecycle, InitializingBean, EndPointDescriptionProvider {
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractServerSocketProcess.class);

  private final ExecutorService _executorService;

  private int _portNumber;
  private InetAddress _bindAddress;

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

  // THE FOLLOWING IS A NASTY HACK - the spring context created by Tomcat doesn't get started properly so the lifecycle methods never get called
  public void afterPropertiesSet() {
    if (!System.getProperty("user.name").startsWith("bamboo")) {
      s_logger.error("Hacking a call to start - take this code out when the context starts up properly!");
      start();
    }
  }

  /**
   * Type of connection. Always {@link #TYPE_VALUE}.
   */
  public static final String TYPE_KEY = "type";
  /**
   * Value of the type of connection.
   */
  public static final String TYPE_VALUE = "Socket";
  /**
   * Connection address.
   */
  public static final String ADDRESS_KEY = "address";
  /**
   * Connection port.
   */
  public static final String PORT_KEY = "port";

  private void loadInterfaceAddress(final NetworkInterface iface, final MutableFudgeFieldContainer message) {
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
      message.add(ADDRESS_KEY, hostAddress);
      s_logger.debug("Address {}/{}", iface.getName(), hostAddress);
    }
  }

  @Override
  public FudgeFieldContainer getEndPointDescription(final FudgeContext fudgeContext) {
    final MutableFudgeFieldContainer desc = fudgeContext.newMessage();
    desc.add(TYPE_KEY, TYPE_VALUE);
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
        desc.add(ADDRESS_KEY, addr.getHostAddress());
      }
    }
    desc.add(PORT_KEY, _serverSocket.getLocalPort());
    return desc;
  }

}
