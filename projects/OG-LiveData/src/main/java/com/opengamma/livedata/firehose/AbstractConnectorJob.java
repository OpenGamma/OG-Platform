/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.livedata.firehose;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * Abstraction for the main job for connecting to a market data feed and decoding the results.
 * 
 * @param <Record> the transport record type
 */
public abstract class AbstractConnectorJob<Record> implements Runnable {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractConnectorJob.class);

  /** the capacity of the buffer between the fire-hose receiver thread and the active mq sender thread */
  private int QUEUE_CAPACITY = 5000;

  /**
   * Callback to receive values from the connector as they are produced.
   */
  public interface Callback<Record> {

    void disconnected();

    void received(Record record);

    void connected();

  }

  /**
   * Factory interface for producing job instances.
   */
  public interface Factory<Record> {

    AbstractConnectorJob<Record> newInstance(Callback<Record> callback, RecordStream.Factory<Record> streamFactory, ExecutorService pipeLineExecutor);

  }

  private static final Object s_eof = new Object();

  /**
   * Decodes records read from the input stream and writes them to a queue so that another thread can process them.
   * Once runninng, the decoding of packets by one thread should be concurrent with the handling of previous data by
   * other thread(s).
   */
  private class RecordDecoder implements Runnable {

    private final BlockingQueue<Object> _queue = new LinkedBlockingQueue<Object>(QUEUE_CAPACITY);
    private final RecordStream<Record> _stream;

    public RecordDecoder(final RecordStream<Record> stream) {
      _stream = stream;
    }

    public BlockingQueue<Object> getQueue() {
      return _queue;
    }

    @Override
    public void run() {
      try {
        while (true) {
          Record record = _stream.readRecord();
          try {
            _queue.put(record);
          } catch (InterruptedException e) {
            e.printStackTrace();  // TODO
          }
        }
      } catch (IOException e) {
        s_logger.warn("I/O exception caught - {}", e.toString());
        s_logger.debug("I/O exception", e);
      }
      _queue.add(s_eof);
    }

  }

  private volatile boolean _poisoned;
  private final RecordStream.Factory<Record> _streamFactory;
  private final Callback<Record> _callback;
  private final ExecutorService _pipeLineExecutor;

  protected AbstractConnectorJob(final Callback<Record> callback, final RecordStream.Factory<Record> streamFactory,
                                 final ExecutorService pipeLineExecutor) {
    ArgumentChecker.notNull(callback, "callback");
    ArgumentChecker.notNull(streamFactory, "streamFactory");
    _callback = callback;
    _streamFactory = streamFactory;
    _pipeLineExecutor = pipeLineExecutor;
  }

  protected Callback<Record> getCallback() {
    return _callback;
  }

  protected RecordStream.Factory<Record> getStreamFactory() {
    return _streamFactory;
  }

  protected boolean isPipeLineRead() {
    return getPipeLineExecutor() != null;
  }

  protected ExecutorService getPipeLineExecutor() {
    return _pipeLineExecutor;
  }

  protected abstract void prepareConnection();

  protected abstract void establishConnection() throws IOException;

  protected abstract void endConnection();

  protected abstract InputStream getInputStream() throws IOException;

  @SuppressWarnings("unchecked")
  @Override
  public void run() {
    s_logger.info("Started connection job");

    // Reconnect until quit requested
    while (!_poisoned) {
      prepareConnection();
      if (_poisoned) {
        break;
      }
      try {
        establishConnection();
        s_logger.info("Connected");
        getCallback().connected();
        if (isPipeLineRead()) {

          // Multi-threaded mode
          final RecordStream<Record> stream = getStreamFactory().newInstance(new BufferedInputStream(getInputStream()));
          final RecordDecoder decoder = new RecordDecoder(stream);
          getPipeLineExecutor().submit(decoder);
          final BlockingQueue<Object> queue = decoder.getQueue();
          try {
            Object record = queue.take();
            while (record != s_eof) {
              getCallback().received((Record) record);
              record = queue.take();
            }
          } catch (InterruptedException e) {
            throw new OpenGammaRuntimeException("Interrupted", e);
          }

        } else {

          // Single-threaded mode
          final RecordStream<Record> records = getStreamFactory().newInstance(new BufferedInputStream(getInputStream()));
          do {
            getCallback().received(records.readRecord());
          } while (true);

        }
      } catch (IOException e) {
        ioExceptionInRead(e);
        s_logger.warn("I/O exception caught - {}", e.toString());
        s_logger.debug("I/O exception", e);
      } finally {
        endConnection();
      }
      s_logger.info("Disconnected");
      getCallback().disconnected();
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        throw new OpenGammaRuntimeException("Interrupted", e);
      }
    }
    s_logger.info("Stopped connection job");
  }

  protected void ioExceptionInRead(IOException e) {
  }

  public void poison() {
    _poisoned = true;
    endConnection();
  }

}
