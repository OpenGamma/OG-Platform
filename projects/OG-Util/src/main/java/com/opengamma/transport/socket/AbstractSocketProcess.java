/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.transport.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.transport.EndPointDescriptionProvider;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.monitor.ReportingInputStream;
import com.opengamma.util.monitor.ReportingOutputStream;

/**
 * 
 *
 */
public abstract class AbstractSocketProcess implements Lifecycle, EndPointDescriptionProvider {
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractSocketProcess.class);
  private Collection<InetAddress> _inetAddresses;
  private int _portNumber;

  private boolean _started;
  private Socket _socket;

  /**
   * @return the inetAddress
   */
  public Collection<InetAddress> getInetAddresses() {
    return Collections.unmodifiableCollection(_inetAddresses);
  }

  /**
   * @param inetAddress the inetAddress to set
   */
  public void setInetAddress(InetAddress inetAddress) {
    _inetAddresses = Collections.singleton(inetAddress);
  }

  public void setInetAddresses(Collection<InetAddress> inetAddresses) {
    _inetAddresses = new ArrayList<InetAddress>(inetAddresses);
  }

  public void setAddress(final String host) throws UnknownHostException {
    setInetAddresses(Arrays.asList(InetAddress.getAllByName(host)));
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
   * Set the connection parameters based on the end point description of a server.
   * 
   * @param endPoint An end-point description.
   */
  public void setServer(final FudgeMsg endPoint) {
    ArgumentChecker.notNull(endPoint, "endPoint");
    if (!SocketEndPointDescriptionProvider.TYPE_VALUE.equals(endPoint.getString(SocketEndPointDescriptionProvider.TYPE_KEY))) {
      throw new IllegalArgumentException("End point is not a ServerSocket - " + endPoint);
    }
    final Collection<InetAddress> addresses = new HashSet<InetAddress>();
    for (FudgeField addr : endPoint.getAllByName(SocketEndPointDescriptionProvider.ADDRESS_KEY)) {
      final String host = endPoint.getFieldValue(String.class, addr);
      try {
        addresses.addAll(Arrays.asList(InetAddress.getAllByName(host)));
      } catch (UnknownHostException e) {
        s_logger.warn("Unknown host {}", host);
      }
    }
    setPortNumber(endPoint.getInt(SocketEndPointDescriptionProvider.PORT_KEY));
    setInetAddresses(addresses);
    s_logger.debug("End point {} resolved to {}:{}", new Object[] {endPoint, getInetAddresses(), getPortNumber() });
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
    ArgumentChecker.notNullInjected(getInetAddresses(), "Remote InetAddress");
    ArgumentChecker.isTrue(getPortNumber() > 0, "Must specify valid portNumber property");
    if (_started && (_socket != null)) {
      s_logger.warn("Already connected to {}", _socket.getRemoteSocketAddress());
    } else {
      openRemoteConnection();
      _started = true;
    }
  }

  protected synchronized void openRemoteConnection() {
    s_logger.info("Opening remote connection to {}:{}", getInetAddresses(), getPortNumber());
    OutputStream os = null;
    InputStream is = null;
    for (InetAddress addr : getInetAddresses()) {
      try {
        _socket = new Socket();
        _socket.connect(new InetSocketAddress(addr, getPortNumber()), 3000);
        s_logger.debug("Connected to {}:{}", addr, getPortNumber());
        os = _socket.getOutputStream();
        is = _socket.getInputStream();
        break;
      } catch (IOException ioe) {
        s_logger.debug("Couldn't connect to {}:{}", addr, getPortNumber());
        if (_socket != null) {
          try {
            _socket.close();
          } catch (IOException e) {
            // Ignore
          }
          _socket = null;
        }
      }
    }
    if (_socket == null) {
      throw new OpenGammaRuntimeException("Unable to open remote connection to " + getInetAddresses() + ":" + getPortNumber());
    }
    is = new ReportingInputStream(s_logger, _socket.getRemoteSocketAddress().toString(), is);
    os = new ReportingOutputStream(s_logger, _socket.getRemoteSocketAddress().toString(), os);
    socketOpened(_socket, new BufferedOutputStream(os), new BufferedInputStream(is));
  }

  @Override
  public synchronized void stop() {
    if (_started) {
      if (_socket != null) {
        if (_socket.isConnected()) {
          try {
            _socket.close();
          } catch (IOException e) {
            s_logger.warn("Unable to close connected socket to {}", new Object[] {_socket.getRemoteSocketAddress() }, e);
          }
        }
        _socket = null;
      }
      _started = false;
      socketClosed();
    } else {
      s_logger.warn("Already stopped {}:{}", getInetAddresses(), getPortNumber());
    }
  }

  protected boolean exceptionForcedByClose(final Exception e) {
    return (e instanceof SocketException) && "Socket closed".equals(e.getMessage());
  }

  protected abstract void socketOpened(Socket socket, BufferedOutputStream os, BufferedInputStream is);

  protected void socketClosed() {
  }

  @Override
  public FudgeMsg getEndPointDescription(final FudgeContext fudgeContext) {
    final MutableFudgeMsg desc = fudgeContext.newMessage();
    desc.add(SocketEndPointDescriptionProvider.TYPE_KEY, SocketEndPointDescriptionProvider.TYPE_VALUE);
    if (getInetAddresses() != null) {
      for (InetAddress addr : getInetAddresses()) {
        desc.add(SocketEndPointDescriptionProvider.ADDRESS_KEY, addr.getHostAddress());
      }
    }
    desc.add(SocketEndPointDescriptionProvider.PORT_KEY, getPortNumber());
    return desc;
  }

}
