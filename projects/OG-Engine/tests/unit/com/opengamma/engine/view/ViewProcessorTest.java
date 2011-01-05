/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.opengamma.util.test.Timeout;

/**
 * Tests ViewProcessor
 */
public class ViewProcessorTest {

  @Test
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

  @Test(expected = IllegalStateException.class)
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
