/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.util.test.Timeout;

/**
 * Tests ViewProcessor
 */
@Test
public class ViewProcessorTest {

  public void testCreateViewProcessor() {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    ViewProcessorImpl vp = env.getViewProcessor();

    assertFalse(vp.isRunning());

    vp.start();

    assertTrue(vp.isRunning());
    assertTrue(vp.getViewProcesses().isEmpty());
    vp.stop();
  }

  @Test
  public void testAttachToKnownView() {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    client.attachToViewProcess(env.getViewDefinition().getName(), ExecutionOptions.getRealTime());
    
    vp.stop();
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testAttachToUnknownView() {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    client.attachToViewProcess("Something random", ExecutionOptions.getRealTime());
  }

  @Test
  public void testSuspend_viewExists() throws InterruptedException, ExecutionException {
    final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();

    Runnable resume = vp.suspend(Executors.newCachedThreadPool()).get();
    assertNotNull(resume);
    
    final CountDownLatch latch = new CountDownLatch(1);
    final ViewClient client2 = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    Thread tryAttach = new Thread() {
      @Override
      public void run() {
        client2.attachToViewProcess(env.getViewDefinition().getName(), ExecutionOptions.getRealTime());
        client2.shutdown();
        latch.countDown();
      }
    };
    tryAttach.start();
    assertFalse(latch.await(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS));
    resume.run();
    assertTrue(latch.await(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS));
    vp.stop();
  }

  @Test
  public void testSuspend_viewNotExists() throws InterruptedException, ExecutionException {
    final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    Runnable resume = vp.suspend(Executors.newCachedThreadPool()).get();
    assertNotNull(resume);
    
    final ViewClient client2 = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    final CountDownLatch latch = new CountDownLatch(1);
    Thread tryAttach = new Thread() {
      @Override
      public void run() {
        client2.attachToViewProcess(env.getViewDefinition().getName(), ExecutionOptions.getRealTime());
        client2.shutdown();
        latch.countDown();
      }
    };
    tryAttach.start();
    assertFalse (latch.await(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS));
    resume.run ();
    assertTrue (latch.await(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS));
    vp.stop();
  }

}
