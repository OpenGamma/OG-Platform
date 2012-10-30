/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.livedata.firehose;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;

/**
 * Replays data from a file.
 * 
 * @param <T> the record type
 */
public class FileReplayConnectorJob<T> extends AbstractConnectorJob<T> {

  private static final Logger s_logger = LoggerFactory.getLogger(FileReplayConnectorJob.class);

  private final String _filename;
  private volatile FileInputStream _file;
  private long _limitSpeed;

  /**
   * Creates {@link FileReplayConnectorJob} instances.
   */
  public static class Factory<T> implements AbstractConnectorJob.Factory<T> {

    private String _filename;

    @Override
    public FileReplayConnectorJob<T> newInstance(final AbstractConnectorJob.Callback<T> callback, final RecordStream.Factory<T> streamFactory, final ExecutorService pipeLineExecutor) {
      return new FileReplayConnectorJob<T>(callback, streamFactory, pipeLineExecutor, getFilename());
    }

    public void setFilename(final String filename) {
      _filename = filename;
    }

    public String getFilename() {
      return _filename;
    }

  }

  protected FileReplayConnectorJob(final AbstractConnectorJob.Callback<T> callback, final RecordStream.Factory<T> streamFactory, final ExecutorService executorService, final String filename) {
    super(callback, streamFactory, executorService);
    ArgumentChecker.notNull(filename, "filename");
    _filename = filename;
  }

  protected String getFilename() {
    return _filename;
  }

  @Override
  protected void prepareConnection() {
    // No-op
  }

  @Override
  protected void establishConnection() throws IOException {
    final long start = System.nanoTime();
    _file = new FileInputStream(getFilename()) {

      private long _bytes;

      @Override
      public int read(final byte[] buffer) throws IOException {
        return read(buffer, 0, buffer.length);
      }

      @Override
      public int read(final byte[] buffer, final int ofs, final int len) throws IOException {
        final int bytesRead = super.read(buffer, ofs, len);
        final long limitSpeed = _limitSpeed;
        if (limitSpeed > 0) {
          _bytes += bytesRead;
          final long expectedTime = (long) (((double) _bytes / (double) limitSpeed) * 1e9);
          final long elapsedTime = System.nanoTime() - start;
          final long delay = (expectedTime - elapsedTime) / 1000000;
          if (delay > 0) {
            try {
              Thread.sleep(delay);
            } catch (InterruptedException e) {
              throw new InterruptedIOException();
            }
          }
        }
        return bytesRead;
      }

    };
  }

  @Override
  protected void endConnection() {
    final FileInputStream file = _file;
    if (file != null) {
      try {
        s_logger.info("Closing file");
        file.close();
      } catch (IOException e) {
        s_logger.debug("I/O exception caught", e);
      }
    } else {
      s_logger.info("No file to close at poison");
    }
  }

  @Override
  protected InputStream getInputStream() throws IOException {
    return _file;
  }

}
