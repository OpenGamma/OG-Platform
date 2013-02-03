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
 * Connects over a TCP/IP socket, reading records of type T and calling back methods in the supplied callback class
 * on network events.
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

    /**
     * Create a new instance of a network connector job
     * @param callback          the class containing callbacks for events such as receive
     * @param streamFactory     the factory for the stream of records to relay
     * @param pipeLineExecutor  the thread executor (IMPORTANT: if this is null, no additional threads are created
     *                          for pipelining, leading to constipated TCP buffers and potential overflows.

     * @return  the newly created network connector job
     */
    @Override
    public AbstractConnectorJob<T> newInstance(final AbstractConnectorJob.Callback<T> callback,
                                               final RecordStream.Factory<T> streamFactory,
                                               final ExecutorService pipeLineExecutor) {
      return new NetworkConnectorJob<T>(callback, streamFactory,
          getHost(), getPort(), pipeLineExecutor);
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

  /**
   * Creates a new network connector job that is single-threaded. Using this constructor is not recommended as it
   * might cause constipated TCP buffers.
   *
   * @param callback  the class containing callbacks for events such as receive
   * @param streamFactory   the factory for the stream of records to relay
   * @param host  the host to connect to
   * @param port  the TCP port to connect to
   */
  @Deprecated
  public NetworkConnectorJob(final AbstractConnectorJob.Callback<T> callback, final RecordStream.Factory<T> streamFactory,
                             final InetAddress host, final int port) {
    this(callback, streamFactory, host, port, null);
  }

  /**
   * Creates a new network connector job.
   *
   * @param callback  the class containing network event callback methods
   * @param streamFactory   the factory for the stream of records to relay
   * @param host  the host to connect to
   * @param port  the TCP port to connect to
   * @param pipeLineExecutor  the thread executor (Note: if this is null, no additional threads are created for
   *                          pipelining, leading to constipated TCP buffers
   */
  public NetworkConnectorJob(final AbstractConnectorJob.Callback<T> callback, final RecordStream.Factory<T> streamFactory,
                             final InetAddress host, final int port, final ExecutorService pipeLineExecutor) {
    super(callback, streamFactory, pipeLineExecutor);
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

  @Override
  protected void ioExceptionInRead(IOException e) {
    super.ioExceptionInRead(e);
    s_logger.warn("Socket state to {}:{} Bound:{} Closed:{} Connected:{} InputShutdown:{} OutputShutdown:{}",
        new Object[] {
          getHost(),
          getPort(),
          _socket.isBound(),
          _socket.isClosed(),
          _socket.isConnected(),
          _socket.isInputShutdown(),
          _socket.isOutputShutdown()});
  }

}
