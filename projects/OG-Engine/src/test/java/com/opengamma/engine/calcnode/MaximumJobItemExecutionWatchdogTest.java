/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Collections;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.calcnode.CalculationJobItem;
import com.opengamma.engine.calcnode.MaximumJobItemExecutionWatchdog;
import com.opengamma.engine.function.EmptyFunctionParameters;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.Timeout;

/**
 * Tests the {@link MaximumJobItemExecutionWatchdogTest}.
 */
@Test(groups = TestGroup.INTEGRATION)
public class MaximumJobItemExecutionWatchdogTest {

  private final CalculationJobItem JOB = new CalculationJobItem("", new EmptyFunctionParameters(), ComputationTargetSpecification.NULL,
      Collections.<ValueSpecification>emptySet(), Collections.<ValueSpecification>emptySet(), ExecutionLogMode.INDICATORS);

  public void testNoAlert() throws Exception {
    final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    try {
      final MaximumJobItemExecutionWatchdog watchdog = new MaximumJobItemExecutionWatchdog();
      watchdog.setMaxJobItemExecutionTime(Timeout.standardTimeoutMillis() / 2);
      watchdog.setScheduler(scheduler);
      final CyclicBarrier barrier = new CyclicBarrier(2);
      (new Thread() {
        @Override
        public void run() {
          try {
            barrier.await(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
            barrier.reset();
            watchdog.jobExecutionStarted(JOB);
            watchdog.jobExecutionStopped();
            barrier.await(Timeout.standardTimeoutMillis() * 2, TimeUnit.MILLISECONDS);
            watchdog.jobExecutionStarted(JOB);
            watchdog.jobExecutionStopped();
          } catch (final Exception e) {
            throw new OpenGammaRuntimeException("exception", e);
          }
        }
      }).start();
      barrier.await(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
      Thread.sleep(Timeout.standardTimeoutMillis());
      // Watchdog should have run while the thread is alive, but not executing anything
      assertTrue(watchdog.areThreadsAlive());
      barrier.await(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
      Thread.sleep(Timeout.standardTimeoutMillis());
      // Watchdog should have run with the thread dead
      assertFalse(watchdog.areThreadsAlive());
    } finally {
      scheduler.shutdown();
    }
  }

  public void testAlert() throws Exception {
    final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    try {
      final MaximumJobItemExecutionWatchdog watchdog = new MaximumJobItemExecutionWatchdog();
      watchdog.setMaxJobItemExecutionTime(Timeout.standardTimeoutMillis() / 2);
      watchdog.setScheduler(scheduler);
      final AtomicReference<InterruptedException> caught = new AtomicReference<InterruptedException>();
      final CyclicBarrier barrier = new CyclicBarrier(2);
      (new Thread() {
        @Override
        public void run() {
          try {
            watchdog.jobExecutionStarted(JOB);
            barrier.await(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
            Thread.sleep(Timeout.standardTimeoutMillis() * 3);
          } catch (final InterruptedException e) {
            caught.set(e);
          } catch (final Exception e) {
            throw new OpenGammaRuntimeException("exception", e);
          }
        }
      }).start();
      barrier.await(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
      assertTrue(watchdog.areThreadsAlive());
      Thread.sleep(Timeout.standardTimeoutMillis());
      // Watchdog will have fired and the default action will have interrupted the thread
      assertFalse(watchdog.areThreadsAlive());
      assertNotNull(caught.get());
    } finally {
      scheduler.shutdown();
    }
  }

}
