/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.monitor;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;

/**
 * Collects statistics on the number of bytes written and emits info to a log.
 * This is for testing/development and not intended for use in production code.
 */
public class ReportingOutputStream extends FilterOutputStream {

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
   * The write time (system nano time).
   */
  private long _writeTime;
  /**
   * The bytes written.
   */
  private long _writeBytes;
  /**
   * The number of write operations.
   */
  private long _writeOperations;

  /**
   * Creates an instance.
   * 
   * @param logger  the device to report to, not null
   * @param streamName  the name to include in the log output, should not be null
   * @param underlying  the underlying stream, not null
   */
  public ReportingOutputStream(final Logger logger, final String streamName, final OutputStream underlying) {
    super(underlying);
    _logger = logger;
    _streamName = streamName;
    _nextReportTime = System.nanoTime() + TIME_TO_REPORT;
  }

  //-------------------------------------------------------------------------
  @Override
  public void write(final int b) throws IOException {
    beginWrite();
    try {
      out.write(b);
      _writeBytes++;
    } finally {
      endWrite();
    }
  }

  @Override
  public void write(final byte[] b, final int off, final int len) throws IOException {
    beginWrite();
    try {
      out.write(b, off, len);
      _writeBytes += len;
    } finally {
      endWrite();
    }
  }

  @Override
  public void flush() throws IOException {
    beginWrite();
    try {
      super.flush();
    } finally {
      endWrite();
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Called when beginning to write.
   */
  private void beginWrite() {
    if (_callStack++ == 0) {
      _writeOperations++;
      _writeTime -= System.nanoTime();
    }
  }

  /**
   * Called when ending to write.
   */
  private void endWrite() {
    if (--_callStack == 0) {
      long time = System.nanoTime();
      _writeTime += time;
      if (time >= _nextReportTime) {
        _nextReportTime = time + TIME_TO_REPORT;
        _logger.info("Stream {} wrote {}Kb in {}ms from {} operations ({}M)", new Object[] {_streamName, (double) _writeBytes / 1024d, (double) _writeTime / 1000000d, _writeOperations,
          (double) _writeBytes * 8192d / (double) _writeTime});
        
        // scale down influence of older data
        _writeOperations >>= 1;
        _writeBytes >>= 1;
        _writeTime >>= 1;
      }
    }
  }

}
