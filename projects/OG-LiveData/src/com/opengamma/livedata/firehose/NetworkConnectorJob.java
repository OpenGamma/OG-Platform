/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.livedata.firehose;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;

/**
 * Connects over a TCP/IP socket.
 * 
 * @param <T> the record type
 */
public class NetworkConnectorJob<T> extends AbstractConnectorJob<T> {

  private static final Logger s_logger = LoggerFactory.getLogger(NetworkConnectorJob.class);

  private static final int s_connectionTimeout = 30000;
  private static final int s_heartbeatTimeout = 150000; // 2.5 mins (heartbeat expected every 2mins)

  private final InetAddress _host;
  private final int _port;
  private volatile Socket _socket;

  /**
   * Creates {@link NetworkSurfConnectorJob} instances.
   */
  public static class Factory<T> implements AbstractConnectorJob.Factory<T> {

    // TODO: allow multiple hosts to be specified and do a round-robin thing if they fail

    private InetAddress _host;
    private int _port;

    @Override
    public AbstractConnectorJob<T> newInstance(final AbstractConnectorJob.Callback<T> callback, final RecordStream.Factory<T> streamFactory, final ExecutorService pipeLineExecutor) {
      return new NetworkConnectorJob<T>(callback, streamFactory, getHost(), getPort());
    }

    public void setHost(final InetAddress host) {
      _host = host;
    }

    public void setHostName(final String host) throws UnknownHostException {
      setHost(InetAddress.getByName(host));
    }

    public InetAddress getHost() {
      return _host;
    }

    public String getHostName() {
      if (getHost() == null) {
        return null;
      }
      return getHost().getHostName();
    }

    public void setPort(final int port) {
      _port = port;
    }

    public int getPort() {
      return _port;
    }

  }

  public NetworkConnectorJob(final AbstractConnectorJob.Callback<T> callback, final RecordStream.Factory<T> streamFactory, final InetAddress host, final int port) {
    super(callback, streamFactory, null);
    ArgumentChecker.notNull(host, "host");
    _host = host;
    _port = port;
  }

  protected InetAddress getHost() {
    return _host;
  }

  protected int getPort() {
    return _port;
  }

  @Override
  protected void prepareConnection() {
    _socket = new Socket();
  }

  @Override
  protected void establishConnection() throws IOException {
    s_logger.info("Connecting to {}:{}", getHost(), getPort());
    _socket.connect(new InetSocketAddress(getHost(), getPort()), s_connectionTimeout);
  }

  @Override
  protected void endConnection() {
    final Socket socket = _socket;
    if (socket != null) {
      try {
        s_logger.info("Closing socket");
        socket.close();
      } catch (IOException e) {
        s_logger.debug("I/O exception caught", e);
      }
    } else {
      s_logger.info("No socket to close at poison");
    }
  }

  @Override
  protected InputStream getInputStream() throws IOException {
    _socket.setSoTimeout(s_heartbeatTimeout);
    return _socket.getInputStream();
  }

}
