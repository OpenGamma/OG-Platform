/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.firehose;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;

/**
 * Decodes records read from the input stream and writes them to a queue so that another thread can process them.
 * Once running, the decoding of packets by one thread should be concurrent with the handling of previous data by
 * other thread(s).
 * <p/>
 * In the case that the queue fills up, the job will drop new messages to avoid blocking the input
 * queue.
 */
public class RecordConsumptionJob implements Runnable {
  private static final Logger s_logger = LoggerFactory.getLogger(RecordConsumptionJob.class);
  
  private final InputStreamFactory _inputStreamFactory;
  private final RecordStream.Factory<?> _recordStreamFactory;
  private final BlockingQueue<Object> _queue;
  
  private InputStream _inputStream;
  private RecordStream<?> _recordStream;
  private final AtomicBoolean _terminated = new AtomicBoolean(false);

  public RecordConsumptionJob(
      final InputStreamFactory inputStreamFactory,
      final RecordStream.Factory<?> recordStreamFactory,
      final BlockingQueue<Object> queue) {
    ArgumentChecker.notNull(inputStreamFactory, "inputStreamFactory");
    ArgumentChecker.notNull(recordStreamFactory, "recordStreamFactory");
    _inputStreamFactory = inputStreamFactory;
    _recordStreamFactory = recordStreamFactory;
    _queue = queue;
  }

  public BlockingQueue<Object> getQueue() {
    return _queue;
  }
  
  protected void loopWhileConnected() {
    try {
      while (!_terminated.get()) {
        Object record = _recordStream.readRecord();
        s_logger.debug("Received record {}", record);
        try {
          _queue.put(record);
        } catch (InterruptedException e) {
          s_logger.warn("Unable to add a new record to the queue.");
          // TODO kirk 2013-03-19 -- Determine what else to do in this case.
          // In other words, implement the dropping logic in the javadocs.
        }
      }
    } catch (IOException e) {
      s_logger.warn("I/O exception caught - {}", e.toString());
      s_logger.debug("I/O exception", e);
    }
  }
  
  protected void establishConnection() {
    _inputStream = null;
    _recordStream = null;
    
    // TODO -- Attempt reconnect.
    
    InputStream is = null;
    try {
      is = _inputStreamFactory.openConnection();
    } catch (Exception e) {
      s_logger.warn("Unable to open stream using {}", _inputStreamFactory);
      return;
    }
    assert is != null;
    _inputStream = is;
    _recordStream = _recordStreamFactory.newInstance(new BufferedInputStream(_inputStream));
  }
  
  protected void tearDownConnection() {
    try {
      _inputStream.close();
    } catch (Exception e) {
      s_logger.warn("Unable to tear down connection after IOException during read.", e);
    }
    
    _inputStream = null;
    _recordStream = null;
  }

  @Override
  public void run() {
    // Two basic loops:
    // -- Outer loop (implemented in this block) attempts keepalive on the
    //    connection.
    // -- Inner loop processes while the connection is alive.
    
    while (!_terminated.get()) {
      establishConnection();
      if (_recordStream == null) {
        // We failed in establishing the connection. Keep trying in case
        // this is a sporadic issue.
        s_logger.warn("Unable to establish a connection. Looping.");
        continue;
      }
      
      loopWhileConnected();
      
      tearDownConnection();
    }
    _queue.add(FireHoseRecordProcessor.EOF_INDICATOR);
  }
  
  public void terminate() {
    _terminated.set(true);
  }

}
