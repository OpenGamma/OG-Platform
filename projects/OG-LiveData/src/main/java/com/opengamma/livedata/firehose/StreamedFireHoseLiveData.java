/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.livedata.firehose;

import java.util.concurrent.ExecutorService;

import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;

/**
 * Implementation of {@link AbstractFireHoseLiveData} based on the concept of a stream of data that can be tokenised into records.
 * 
 * @param <T> the record type
 */
public abstract class StreamedFireHoseLiveData<T> extends AbstractFireHoseLiveData {

  private static final Logger s_logger = LoggerFactory.getLogger(StreamedFireHoseLiveData.class);

  private FudgeContext _fudgeContext = FudgeContext.GLOBAL_DEFAULT;
  private AbstractConnectorJob<T> _job;
  private RecordStream.Factory<T> _streamFactory;
  private AbstractConnectorJob.Factory<T> _connectorFactory;
  private ExecutorService _executorService;
  private boolean _pipeLineIO;

  private final AbstractConnectorJob.Callback<T> _connectionHandler = new AbstractConnectorJob.Callback<T>() {

    @Override
    public void disconnected() {
      s_logger.info("Live data server disconnected");
      liveDataDisconnected();
      setMarketDataComplete(false);
    }

    @Override
    public void received(final T record) {
      s_logger.debug("Received {}", record);
      recordReceived(record);
    }

    @Override
    public void connected() {
      s_logger.info("Live data server connected");
      liveDataConnected();
    }

  };

  protected abstract void recordReceived(T record);

  protected void liveDataDisconnected() {
    // No-op
  }

  protected void liveDataConnected() {
    // No-op
  }

  public void setFudgeContext(final FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  public void setConnectorFactory(final AbstractConnectorJob.Factory<T> connectorFactory) {
    ArgumentChecker.notNull(connectorFactory, "connectorFactory");
    _connectorFactory = connectorFactory;
  }

  public AbstractConnectorJob.Factory<T> getConnectorFactory() {
    return _connectorFactory;
  }

  public RecordStream.Factory<T> getStreamFactory() {
    return _streamFactory;
  }

  public void setStreamFactory(final RecordStream.Factory<T> streamFactory) {
    ArgumentChecker.notNull(streamFactory, "streamFactory");
    _streamFactory = streamFactory;
  }

  public ExecutorService getExecutorService() {
    return _executorService;
  }

  public void setExecutorService(final ExecutorService executorService) {
    _executorService = executorService;
  }

  public boolean isPipeLineIO() {
    return _pipeLineIO;
  }

  public void setPipeLineIO(final boolean pipeLineIO) {
    _pipeLineIO = pipeLineIO;
  }

  @Override
  public synchronized void start() {
    if (_job != null) {
      throw new IllegalStateException("Connection job already active");
    }
    s_logger.info("Starting connector job");
    _job = getConnectorFactory().newInstance(_connectionHandler, getStreamFactory(), isPipeLineIO() ? getExecutorService() : null);
    getExecutorService().submit(_job);
  }

  @Override
  public synchronized void stop() {
    final AbstractConnectorJob<T> job = _job;
    if (job == null) {
      throw new IllegalStateException("Connection job not active");
    }
    s_logger.info("Poisoning connector job");
    _job = null;
    job.poison();
  }

  @Override
  public synchronized boolean isStarted() {
    return _job != null;
  }

}
