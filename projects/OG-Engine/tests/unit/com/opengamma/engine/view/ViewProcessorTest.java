/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
    assertEquals(Collections.singleton(env.getViewDefinition().getName()), vp.getViewNames());
    vp.stop();
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void testAssertNotStarted() {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    vp.assertNotStarted();
  }

  @Test
  public void testObtainKnownView() {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    vp.getView(env.getViewDefinition().getName(), ViewProcessorTestEnvironment.TEST_USER);
    vp.stop();
  }

  @Test
  public void testObtainUnknownView() {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();

    View view = vp.getView("Something random", ViewProcessorTestEnvironment.TEST_USER);
    assertNull(view);
  }

  @Test
  public void testSuspend_viewExists() throws InterruptedException, ExecutionException {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    final ViewInternal v = vp.getView(env.getViewDefinition().getName(), ViewProcessorTestEnvironment.TEST_USER);
    Runnable resume = vp.suspend(Executors.newCachedThreadPool()).get();
    assertNotNull(resume);
    final CountDownLatch latch = new CountDownLatch(1);
    Thread tryAndInit = new Thread() {
      @Override
      public void run() {
        v.init();
        latch.countDown();
      }
    };
    tryAndInit.start();
    assertFalse (latch.await(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS));
    resume.run ();
    assertTrue (latch.await(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS));
    vp.stop();
  }

  @Test
  public void testSuspend_viewNotExists() throws InterruptedException, ExecutionException {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    Runnable resume = vp.suspend(Executors.newCachedThreadPool()).get();
    assertNotNull(resume);
    // View must start off in a suspended state
    final ViewInternal v = vp.getView(env.getViewDefinition().getName(), ViewProcessorTestEnvironment.TEST_USER);
    final CountDownLatch latch = new CountDownLatch(1);
    Thread tryAndInit = new Thread() {
      @Override
      public void run() {
        v.init();
        latch.countDown();
      }
    };
    tryAndInit.start();
    assertFalse (latch.await(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS));
    resume.run ();
    assertTrue (latch.await(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS));
    vp.stop();
  }

}
