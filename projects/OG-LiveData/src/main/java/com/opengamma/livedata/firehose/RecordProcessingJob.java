/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.firehose;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;

/**
 * A job, to be run by a {@link Thread}, that hands records off to a
 * {@link RecordProcessor}.
 */
public class RecordProcessingJob implements Runnable {
  private static final Logger s_logger = LoggerFactory.getLogger(RecordProcessingJob.class);
  
  private final BlockingQueue<Object> _queue;
  @SuppressWarnings("rawtypes")
  private final RecordProcessor _recordProcessor;
  private final AtomicBoolean _terminated = new AtomicBoolean(false);
  
  public RecordProcessingJob(
      final BlockingQueue<Object> queue,
      final RecordProcessor<?> recordProcessor) {
    ArgumentChecker.notNull(queue, "queue");
    ArgumentChecker.notNull(recordProcessor, "recordProcessor");
    _queue = queue;
    _recordProcessor = recordProcessor;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void run() {
    while (!_terminated.get()) {
      Object record = null;
      try {
        record = _queue.poll(5L, TimeUnit.SECONDS);
      } catch (InterruptedException ie) {
        Thread.interrupted();
      }
      if (record == null) {
        // Just continue. If we've been interrupted, we need to catch the termination message.
        continue;
      }
      
      try {
        _recordProcessor.process(record);
      } catch (Exception e) {
        s_logger.warn("Unable to process record", e);
        // REVIEW kirk 2013-03-19 -- Is this right to just go back to the loop?
      }
    }
  }
  
  public void terminate() {
    _terminated.set(true);
  }

}
