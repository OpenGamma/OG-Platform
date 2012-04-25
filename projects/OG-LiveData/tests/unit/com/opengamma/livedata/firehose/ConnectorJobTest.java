/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.livedata.firehose;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.util.test.Timeout;

/**
 * Tests the {@link AbstractConnectorJob} class
 */
@Test
public class ConnectorJobTest {

  private static final Logger s_logger = LoggerFactory.getLogger(ConnectorJobTest.class);

  private static final class JobImpl extends AbstractConnectorJob<Integer> {

    private int _prepareConnection;
    private int _establishConnection;
    private int _endConnection;

    public JobImpl(final AbstractConnectorJob.Callback<Integer> callback, final ExecutorService pipelineExecutor) {
      super(callback, new RecordStream.Factory<Integer>() {
        @Override
        public RecordStream<Integer> newInstance(final InputStream input) {
          return new RecordStream<Integer>() {

            private int _count;

            @Override
            public Integer readRecord() throws IOException {
              if (_count < 50) {
                return ++_count;
              } else {
                throw new EOFException();
              }
            }

          };
        }
      }, pipelineExecutor);
    }

    @Override
    protected void prepareConnection() {
      _prepareConnection++;
    }

    @Override
    protected void establishConnection() throws IOException {
      _establishConnection++;
    }

    @Override
    protected void endConnection() {
      _endConnection++;
    }

    @Override
    protected InputStream getInputStream() throws IOException {
      return new ByteArrayInputStream(new byte[0]);
    }
  }

  private static class CallbackImpl implements AbstractConnectorJob.Callback<Integer> {

    private int _disconnected;
    private int _received;
    private int _connected;

    @Override
    public void disconnected() {
      _disconnected++;
    }

    @Override
    public void received(final Integer record) {
      _received++;
    }

    @Override
    public void connected() {
      _connected++;
    }
  }

  public void testPipelinedRead() throws InterruptedException {
    final ExecutorService executor = Executors.newCachedThreadPool();
    try {
      final CallbackImpl callback = new CallbackImpl();
      final JobImpl job = new JobImpl(callback, executor);
      executor.submit(job);
      Thread.sleep(Timeout.standardTimeoutMillis());
      job.endConnection();
      s_logger.debug("Prepare connection called {} time(s)", job._prepareConnection);
      s_logger.debug("Establish connection called {} time(s)", job._establishConnection);
      s_logger.debug("End connection called {} time(s)", job._endConnection);
      s_logger.debug("Disconnected called {} time(s)", callback._disconnected);
      s_logger.debug("Received called {} time(s)", callback._received);
      s_logger.debug("Connected called {} time(s)", callback._connected);
      assertEquals(job._prepareConnection, callback._connected);
      assertEquals(job._establishConnection, callback._connected);
      assertTrue(job._endConnection >= callback._disconnected);
      assertEquals(callback._received, callback._connected * 50);
    } finally {
      executor.shutdown();
    }
  }

  public void testDirectRead() throws InterruptedException {
    final ExecutorService executor = Executors.newCachedThreadPool();
    try {
      final CallbackImpl callback = new CallbackImpl();
      final JobImpl job = new JobImpl(callback, null);
      executor.submit(job);
      Thread.sleep(Timeout.standardTimeoutMillis());
      job.endConnection();
      s_logger.debug("Prepare connection called {} time(s)", job._prepareConnection);
      s_logger.debug("Establish connection called {} time(s)", job._establishConnection);
      s_logger.debug("End connection called {} time(s)", job._endConnection);
      s_logger.debug("Disconnected called {} time(s)", callback._disconnected);
      s_logger.debug("Received called {} time(s)", callback._received);
      s_logger.debug("Connected called {} time(s)", callback._connected);
      assertEquals(job._prepareConnection, callback._connected);
      assertEquals(job._establishConnection, callback._connected);
      assertTrue(job._endConnection >= callback._disconnected);
      assertEquals(callback._received, callback._connected * 50);
    } finally {
      executor.shutdown();
    }
  }

}
