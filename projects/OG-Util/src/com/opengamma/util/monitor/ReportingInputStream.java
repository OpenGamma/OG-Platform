/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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

  private static final long TIME_TO_REPORT = 1000000000; // 1s

  private final Logger _logger;
  private final String _streamName;
  private long _nextReportTime;
  private int _callStack;
  private long _readTime;
  private long _readBytes;
  private long _readOperations;

  /**
   * Creates an instance.
   * 
   * @param logger  the device to report to, not null
   * @param streamName  the identifier to include in the log output
   * @param underlying  the underlying stream, not null
   */
  public ReportingInputStream(final Logger logger, final String streamName, final InputStream underlying) {
    super(underlying);
    _logger = logger;
    _streamName = streamName;
    _nextReportTime = System.nanoTime() + TIME_TO_REPORT;
  }

  private void beginRead() {
    if (_callStack++ == 0) {
      _readOperations++;
      _readTime -= System.nanoTime();
    }
  }

  private void endRead() {
    if (--_callStack == 0) {
      long time = System.nanoTime();
      _readTime += time;
      if (time >= _nextReportTime) {
        _nextReportTime = time + TIME_TO_REPORT;
        _logger.info("Stream {} read {}Kb in {}ms from {} operations ({}M)}", new Object[] {_streamName, (double) _readBytes / 1024d, (double) _readTime / 1000000d, _readOperations,
          (double) _readBytes * 8192d / (double) _readTime});
        
        // scale down influence of older data
        _readOperations >>= 1;
        _readBytes >>= 1;
        _readTime >>= 1;
      }
    }
  }

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
      _readBytes += bytes;
      return bytes;
    } finally {
      endRead();
    }
  }

}
