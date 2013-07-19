/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.monitor;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;

/**
 * Collects statistics on the number of bytes written and emits info to a log.
 * This is for testing/development and not intended for use in production code.
 */
public class ReportingInputStream extends FilterInputStream {

  /**
   * The frequency of logging.
   */
  private static final long TIME_TO_REPORT = 1000000000; // 1s

  /**
   * The logger.
   */
  private final Logger _logger;
  /**
   * The name of the stream.
   */
  private final String _streamName;
  /**
   * The next time (system nano time) that reporting will occur.
   */
  private long _nextReportTime;
  /**
   * The call stack.
   */
  private int _callStack;
  /**
   * The read time (system nano time).
   */
  private long _readTime;
  /**
   * The bytes read.
   */
  private long _readBytes;
  /**
   * The number of read operations.
   */
  private long _readOperations;

  /**
   * Creates an instance.
   * 
   * @param logger  the device to report to, not null
   * @param streamName  the name to include in the log output, should not be null
   * @param underlying  the underlying stream, not null
   */
  public ReportingInputStream(final Logger logger, final String streamName, final InputStream underlying) {
    super(underlying);
    _logger = logger;
    _streamName = streamName;
    _nextReportTime = System.nanoTime() + TIME_TO_REPORT;
  }

  //-------------------------------------------------------------------------
  @Override
  public int read() throws IOException {
    beginRead();
    try {
      final int value = in.read();
      _readBytes++;
      return value;
    } finally {
      endRead();
    }
  }

  @Override
  public int read(final byte[] b, final int off, final int len) throws IOException {
    beginRead();
    try {
      final int bytes = in.read(b, off, len);
      if (bytes > 0) {
        _readBytes += bytes;
      }
      return bytes;
    } finally {
      endRead();
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Called when beginning to read.
   */
  private void beginRead() {
    if (_callStack++ == 0) {
      _readOperations++;
      _readTime -= System.nanoTime();
    }
  }

  /**
   * Called when ending the read.
   */
  private void endRead() {
    if (--_callStack == 0) {
      long time = System.nanoTime();
      _readTime += time;
      if (time - _nextReportTime >= 0) {
        _nextReportTime = time + TIME_TO_REPORT;
        _logger.info("Stream {} read {}Kb in {}ms from {} operations ({}M)}", new Object[] {_streamName, (double) _readBytes / 1024d, (double) _readTime / 1000000d, _readOperations,
          (double) _readBytes * 8192d / (double) _readTime });
        
        // scale down influence of older data
        _readOperations >>= 1;
        _readBytes >>= 1;
        _readTime >>= 1;
      }
    }
  }

}
