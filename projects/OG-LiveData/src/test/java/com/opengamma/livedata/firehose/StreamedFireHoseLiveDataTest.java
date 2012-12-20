/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.livedata.firehose;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.test.Timeout;

/**
 * Test.
 */
@Test(groups = "unit")
public class StreamedFireHoseLiveDataTest {

  private ExecutorService _executorService;

  private static class Impl extends StreamedFireHoseLiveData<Void> {

    public Impl() {
      setStreamFactory(new RecordStream.Factory<Void>() {
        @Override
        public RecordStream<Void> newInstance(final InputStream input) {
          return null;
        }
      });
    }

    @Override
    protected void recordReceived(final Void record) {
      // No-op
    }
  }

  @BeforeTest
  public void init() {
    _executorService = Executors.newCachedThreadPool();
  }

  @AfterTest
  public void done() {
    _executorService.shutdown();
  }

  //-------------------------------------------------------------------------
  private ExecutorService getExecutorService() {
    return _executorService;
  }

  private void runServer(final StreamedFireHoseLiveData<?> fireHose) {
    fireHose.start();
    assertTrue(fireHose.isStarted());
    try {
      Thread.sleep(Timeout.standardTimeoutMillis() / 8);
    } catch (InterruptedException e) {
      throw new OpenGammaRuntimeException("Interrupted", e);
    } finally {
      fireHose.stop();
    }
  }

  public <T> void testStartStopNoPipeline() {
    final StreamedFireHoseLiveData<Void> fireHose = new Impl();
    fireHose.setConnectorFactory(new ByteArrayConnectorJob.Factory<Void>(new byte[] {}));
    fireHose.setExecutorService(getExecutorService());
    fireHose.setPipeLineIO(false);
    runServer(fireHose);
    assertFalse(fireHose.isStarted());
  }

  public void testStartStopWithPipeline() {
    final StreamedFireHoseLiveData<Void> fireHose = new Impl();
    fireHose.setConnectorFactory(new ByteArrayConnectorJob.Factory<Void>(new byte[] {}));
    fireHose.setExecutorService(getExecutorService());
    fireHose.setPipeLineIO(true);
    runServer(fireHose);
    assertFalse(fireHose.isStarted());
  }

  @Test(expectedExceptions = {IllegalStateException.class })
  public void testStartInvalid() {
    final StreamedFireHoseLiveData<Void> fireHose = new Impl();
    fireHose.setConnectorFactory(new ByteArrayConnectorJob.Factory<Void>(new byte[] {}));
    fireHose.setExecutorService(getExecutorService());
    fireHose.start();
    try {
      fireHose.start();
    } finally {
      fireHose.stop();
    }
  }

  @Test(expectedExceptions = {IllegalStateException.class })
  public void testStopInvalid() {
    final StreamedFireHoseLiveData<Void> fireHose = new Impl();
    fireHose.setConnectorFactory(new ByteArrayConnectorJob.Factory<Void>(new byte[] {}));
    fireHose.setExecutorService(getExecutorService());
    fireHose.start();
    fireHose.stop();
    fireHose.stop();
  }

}
