/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class JobDispatcherTest {

  private static final Logger s_logger = LoggerFactory.getLogger(JobDispatcherTest.class);
  private static final long TIMEOUT = 1000L;

  private final ExecutorService _executorService = Executors.newCachedThreadPool();

  private static final AtomicLong s_jobId = new AtomicLong();

  protected static CalculationJobSpecification createTestJobSpec() {
    return new CalculationJobSpecification("Test View", "default", System.currentTimeMillis(), s_jobId.incrementAndGet());
  }

  protected static List<CalculationJobItem> createTestJobItems() {
    return Collections.emptyList();
  }

  private static CalculationJobResult createTestJobResult(final CalculationJobSpecification jobSpec, final long time, final String nodeId) {
    return new CalculationJobResult(jobSpec, time, new ArrayList<CalculationJobResultItem>(), nodeId);
  }

  private class TestJobInvoker implements JobInvoker {

    private int _priority = 10;
    private JobInvokerRegister _callback;
    private boolean _disabled;

    @Override
    public int canInvoke(CalculationJobSpecification jobSpec, List<CalculationJobItem> items) {
      return _priority;
    }

    @Override
    public boolean invoke(final CalculationJobSpecification jobSpec, final List<CalculationJobItem> items, final JobResultReceiver receiver) {
      if (_disabled) {
        return false;
      }
      _executorService.execute(new Runnable() {
        @Override
        public void run() {
          // We'll pass our priority back as the time taken so that the caller can see which invoker received the job
          receiver.resultReceived(createTestJobResult(jobSpec, 0, "" + _priority));
        }
      });
      return true;
    }

    @Override
    public void notifyWhenAvailable(JobInvokerRegister callback) {
      _callback = callback;
    }

  }

  @Test
  public void registerInvokerWithJobPending() {
    final JobDispatcher jobDispatcher = new JobDispatcher();
    final TestJobResultReceiver result = new TestJobResultReceiver();
    final CalculationJobSpecification jobSpec = createTestJobSpec();
    jobDispatcher.dispatchJob(jobSpec, createTestJobItems(), result);
    assertNull(result.getResult());
    final TestJobInvoker jobInvoker = new TestJobInvoker();
    jobDispatcher.registerJobInvoker(jobInvoker);
    final CalculationJobResult jobResult = result.waitForResult(TIMEOUT);
    assertNotNull(jobResult);
    assertEquals(jobSpec, jobResult.getSpecification());
    assertNull(jobInvoker._callback);
  }

  @Test
  public void registerInvokerWithEmptyQueue() {
    final JobDispatcher jobDispatcher = new JobDispatcher();
    final TestJobInvoker jobInvoker = new TestJobInvoker();
    jobDispatcher.registerJobInvoker(jobInvoker);
    final TestJobResultReceiver result = new TestJobResultReceiver();
    final CalculationJobSpecification jobSpec = createTestJobSpec();
    jobDispatcher.dispatchJob(jobSpec, createTestJobItems(), result);
    final CalculationJobResult jobResult = result.waitForResult(TIMEOUT);
    assertNotNull(jobResult);
    assertEquals(jobSpec, jobResult.getSpecification());
    assertNull(jobInvoker._callback);
  }

  private void priorityTest(final String expectedNodeId, final JobDispatcher jobDispatcher) {
    final TestJobResultReceiver result = new TestJobResultReceiver();
    final CalculationJobSpecification jobSpec = createTestJobSpec();
    jobDispatcher.dispatchJob(jobSpec, createTestJobItems(), result);
    final CalculationJobResult jobResult = result.waitForResult(TIMEOUT);
    assertNotNull(jobResult);
    assertEquals(jobSpec, jobResult.getSpecification());
    assertEquals(expectedNodeId, jobResult.getComputeNodeId());
  }

  @Test
  public void invokeInPriorityOrder() {
    final JobDispatcher jobDispatcher = new JobDispatcher();
    final TestJobInvoker nodeLowPriority = new TestJobInvoker();
    nodeLowPriority._priority = 10;
    final TestJobInvoker nodeMediumPriority1 = new TestJobInvoker();
    nodeMediumPriority1._priority = 20;
    final TestJobInvoker nodeMediumPriority2 = new TestJobInvoker();
    nodeMediumPriority2._priority = 20;
    final TestJobInvoker nodeHighPriority = new TestJobInvoker();
    nodeHighPriority._priority = 30;
    jobDispatcher.registerJobInvoker(nodeMediumPriority2);
    jobDispatcher.registerJobInvoker(nodeLowPriority);
    jobDispatcher.registerJobInvoker(nodeHighPriority);
    jobDispatcher.registerJobInvoker(nodeMediumPriority1);
    // Go to high priority node
    priorityTest("30", jobDispatcher);
    assertNull(nodeHighPriority._callback);
    // Go to high priority node
    priorityTest("30", jobDispatcher);
    assertNull(nodeHighPriority._callback);
    // Go to medium priority node, high priority gets a callback notification
    nodeHighPriority._disabled = true;
    priorityTest("20", jobDispatcher);
    assertNotNull(nodeHighPriority._callback);
    // Go to low priority node
    nodeMediumPriority1._disabled = true;
    nodeMediumPriority2._disabled = true;
    priorityTest("10", jobDispatcher);
    assertNotNull(nodeMediumPriority1._callback);
    assertNotNull(nodeMediumPriority2._callback);
  }

  @Test
  public void saturateInvokers() {
    final JobDispatcher jobDispatcher = new JobDispatcher();
    final JobInvoker[] jobInvokers = new JobInvoker[3];
    for (int i = 0; i < jobInvokers.length; i++) {
      final int iid = i + 1;
      jobDispatcher.registerJobInvoker(new JobInvoker() {

        private final Random rnd = new Random ();
        private boolean _busy;
        private JobInvokerRegister _callback;

        @Override
        public int canInvoke(CalculationJobSpecification jobSpec, List<CalculationJobItem> items) {
          return 10;
        }

        @Override
        public boolean invoke(final CalculationJobSpecification jobSpec, final List<CalculationJobItem> items, final JobResultReceiver receiver) {
          final JobInvoker instance = this;
          synchronized (instance) {
            if (_busy) {
              return false;
            }
            _executorService.execute(new Runnable() {
              @Override
              public void run() {
                try {
                  Thread.sleep (rnd.nextInt (50));
                } catch (InterruptedException e) {
                  s_logger.warn ("invoker {} interrupted", iid);
                }
                s_logger.debug("invoker {} completed job {}", iid, jobSpec);
                receiver.resultReceived(createTestJobResult(jobSpec, 0L, instance.toString ()));
                synchronized (instance) {
                  _busy = false;
                  if (_callback != null) {
                    s_logger.debug("re-registering invoker {} with dispatcher", iid);
                    final JobInvokerRegister callback = _callback;
                    _callback = null;
                    callback.registerJobInvoker (instance);
                  } else {
                    s_logger.debug("invoker {} completed job without notify", iid);
                  }
                }
              }
            });
            _busy = true;
            return true;
          }
        }

        @Override
        public void notifyWhenAvailable(final JobInvokerRegister callback) {
          synchronized (this) {
            if (_busy) {
              assertNull(_callback);
              s_logger.debug("invoker {} busy - storing callback", iid);
              _callback = callback;
            } else {
              s_logger.debug("invoker {} ready - immediate callback", iid);
              callback.registerJobInvoker(this);
            }
          }
        }

        @Override
        public String toString() {
          return "invoker " + iid;
        }

      });
    }
    final CalculationJobSpecification[] jobs = new CalculationJobSpecification[100];
    final TestJobResultReceiver[] resultReceivers = new TestJobResultReceiver[jobs.length];
    s_logger.debug("Dispatching {} jobs to {} nodes", jobs.length, jobInvokers.length);
    for (int i = 0; i < jobs.length; i++) {
      jobDispatcher.dispatchJob(jobs[i] = createTestJobSpec(), createTestJobItems(), resultReceivers[i] = new TestJobResultReceiver());
    }
    s_logger.debug("Jobs dispatched");
    for (int i = 0; i < jobs.length; i++) {
      s_logger.debug("Waiting for result {}", i);
      final CalculationJobResult result = resultReceivers[i].waitForResult(TIMEOUT * 2);
      assertNotNull(result);
      assertEquals(jobs[i], result.getSpecification());
    }
    s_logger.debug("All jobs completed");
  }

}
