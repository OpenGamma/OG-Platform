/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.livedata.firehose;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * Mock implementation of the {@link AbstractConnectorJob} that delivers content from an array.
 * 
 * @param <T> record type
 */
public class ByteArrayConnectorJob<T> extends AbstractConnectorJob<T> {

  private static final String END_CONNECTION_EXCEPTION_MESSAGE = "Terminate the parent job";

  private InputStream _data;

  /**
   * Creates {@link ByteArrayConnectorJob} instances.
   */
  public static class Factory<T> implements AbstractConnectorJob.Factory<T> {

    private final byte[] _data;

    public Factory(final byte[] data) {
      ArgumentChecker.notNull(data, "data");
      _data = data;
    }

    @Override
    public AbstractConnectorJob<T> newInstance(final AbstractConnectorJob.Callback<T> callback, final RecordStream.Factory<T> streamFactory,
        final ExecutorService pipeLineExecutor) {
      return new ByteArrayConnectorJob<T>(callback, streamFactory, pipeLineExecutor, _data);
    }

  }

  protected ByteArrayConnectorJob(final AbstractConnectorJob.Callback<T> callback, final RecordStream.Factory<T> streamFactory,
      final ExecutorService pipeLineExecutor, final byte[] data) {
    super(callback, streamFactory, pipeLineExecutor);
    _data = new ByteArrayInputStream(data);
  }

  @Override
  protected void prepareConnection() {
    if (_data == null) {
      throw new OpenGammaRuntimeException(END_CONNECTION_EXCEPTION_MESSAGE);
    }
  }

  @Override
  protected void establishConnection() throws IOException {
    // No-op
  }

  @Override
  protected void endConnection() {
    final InputStream data = _data;
    if (data != null) {
      _data = null;
      try {
        data.close();
      } catch (IOException e) {
        // Ignore
      }
    }
  }

  @Override
  protected InputStream getInputStream() throws IOException {
    return _data;
  }

}
