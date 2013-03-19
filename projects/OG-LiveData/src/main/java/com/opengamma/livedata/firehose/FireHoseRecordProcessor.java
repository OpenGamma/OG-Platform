/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.firehose;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.util.ArgumentChecker;

/**
 * The primary class that holds the state for a live running chunking process.
 * Unlike other classes in this hierarchy, a chunker is defined as a process that
 * continuously runs to process records in a thread compliant fashion, rather than
 * a pull-through fashion (which might be useful in a testing context).
 * 
 * @param <TRecord> The type of the actual record that will be processed.
 */
public class FireHoseRecordProcessor<TRecord> implements Lifecycle {
  private static final Logger s_logger = LoggerFactory.getLogger(FireHoseRecordProcessor.class);

  /**
   * The non-blocking size of the internal queue.
   * The actual size here was chosen without any particular rationale, and
   * may not be correct.
   */
  public static final int QUEUE_CAPACITY = 5000;
  /**
   * Added to the queue when the end of the input stream has been reached.
   */
  public static final Object EOF_INDICATOR = new Object();

  // --------------------------------------------------------------------------
  // INJECTED HELPERS
  // --------------------------------------------------------------------------
  private final InputStreamFactory _inputStreamFactory;
  private final RecordStream.Factory<TRecord> _recordStreamFactory;
  private final RecordProcessor<TRecord> _recordProcessor;
  
  // --------------------------------------------------------------------------
  // RUNNING STATE
  // --------------------------------------------------------------------------
  private final BlockingQueue<Object> _queue = new LinkedBlockingQueue<Object>(QUEUE_CAPACITY);

  private final RecordConsumptionJob _recordConsumptionJob;
  private final RecordProcessingJob _recordProcessingJob;
  private Thread _recordConsumptionThread;
  private Thread _recordDispatchThread;
  
  public FireHoseRecordProcessor(
      InputStreamFactory inputStreamFactory,
      RecordStream.Factory<TRecord> recordStreamFactory,
      RecordProcessor<TRecord> recordProcessor) {
    ArgumentChecker.notNull(inputStreamFactory, "inputStreamFactory");
    ArgumentChecker.notNull(recordStreamFactory, "recordStreamFactory");
    ArgumentChecker.notNull(recordProcessor, "recordProcessor");
    _inputStreamFactory = inputStreamFactory;
    _recordStreamFactory = recordStreamFactory;
    _recordProcessor = recordProcessor;
    
    _recordConsumptionJob = new RecordConsumptionJob(_inputStreamFactory, _recordStreamFactory, _queue);
    _recordProcessingJob = new RecordProcessingJob(_queue, _recordProcessor);
  }
  
  protected RecordStream.Factory<TRecord> getRecordStreamFactory() {
    return _recordStreamFactory;
  }

  // --------------------------------------------------------------------------
  // SPRING LIFECYCLE METHODS
  // --------------------------------------------------------------------------
  @Override
  public synchronized void start() {
    if (isRunning()) {
      return;
    }
    
    _recordConsumptionThread = new Thread(_recordConsumptionJob, "FireHoseRecordProcessor Consumption");
    // Is this the right value?
    _recordConsumptionThread.setDaemon(false);
    _recordConsumptionThread.start();
    
    _recordDispatchThread = new Thread(_recordProcessingJob, "FireHoseRecordProcessor Dispatch");
    // Is this the right value?
    _recordDispatchThread.setDaemon(false);
    _recordDispatchThread.start();
  }

  @Override
  public synchronized void stop() {
    try {
      _recordConsumptionJob.terminate();
      _recordConsumptionThread.join(10000L);
    } catch (InterruptedException e) {
      Thread.interrupted();
      s_logger.warn("Interrupted while killing record consumption thread", e);
    }
    _recordConsumptionThread = null;
    try {
      _recordProcessingJob.terminate();
      _recordDispatchThread.join(10000L);
    } catch (InterruptedException e) {
      Thread.interrupted();
      s_logger.warn("Interrupted while killing record processing/dispatch thread", e);
    }
    _recordDispatchThread = null;
  }

  @Override
  public synchronized boolean isRunning() {
    if ((_recordConsumptionThread != null)
        && _recordConsumptionThread.isAlive()
        && (_recordDispatchThread != null)
        && _recordDispatchThread.isAlive()) {
      return true;
    }
    return false;
  }
}
