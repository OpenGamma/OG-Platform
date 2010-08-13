/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.socket;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 */
public abstract class AbstractSocketProcess implements Lifecycle {
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractSocketProcess.class);
  private InetAddress _inetAddress;
  private int _portNumber;
  
  private boolean _started;
  private Socket _socket;

  /**
   * @return the inetAddress
   */
  public InetAddress getInetAddress() {
    return _inetAddress;
  }

  /**
   * @param inetAddress the inetAddress to set
   */
  public void setInetAddress(InetAddress inetAddress) {
    _inetAddress = inetAddress;
  }

  public void setAddress(final String host) throws UnknownHostException {
    setInetAddress(InetAddress.getByName(host));
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
  
  protected Socket getSocket() {
    return _socket;
  }
  
  protected void startIfNecessary() {
    if (!isRunning()) {
      s_logger.debug("Starting implicitly as start() was not called before use.");
      start();
    }
  }

  @Override
  public synchronized boolean isRunning() {
    return _started;
  }

  @Override
  public synchronized void start() {
    ArgumentChecker.notNullInjected(getInetAddress(), "Remote InetAddress");
    ArgumentChecker.isTrue(getPortNumber() > 0, "Must specify valid portNumber property");
    
    openRemoteConnection();
    
    _started = true;
  }
  
  protected synchronized void openRemoteConnection() {
    s_logger.info("Opening remote connection to {}:{}", getInetAddress(), getPortNumber());
    OutputStream os = null;
    InputStream is = null;
    try {
      _socket = new Socket(getInetAddress(), getPortNumber());
      os = _socket.getOutputStream();
      is = _socket.getInputStream();
    } catch (IOException ioe) {
      throw new OpenGammaRuntimeException("Unable to open remote connection to " + getInetAddress() + ":" + getPortNumber(), ioe);
    }
    os = new BufferedOutputStream(os);
    socketOpened(_socket, os, is);
  }

  @Override
  public synchronized void stop() {
    if (_socket.isConnected()) {
      try {
        _socket.close();
      } catch (IOException e) {
        s_logger.warn("Unable to close connected socket to {}", new Object[]{_socket.getRemoteSocketAddress()}, e);
      }
    }
    
    _socket = null;
    _started = false;
    socketClosed();
  }
  
  protected boolean exceptionForcedByClose(final Exception e) {
    return (e instanceof SocketException) && "Socket closed".equals(e.getMessage());
  }

  protected abstract void socketOpened(Socket socket, OutputStream os, InputStream is);
  
  protected void socketClosed() {
  }

}
