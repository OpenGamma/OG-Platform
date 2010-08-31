/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.view.cache.CacheSelectFilter;

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

  protected static CalculationJob createTestJob() {
    return new CalculationJob(createTestJobSpec(), createTestJobItems(), CacheSelectFilter.allPrivate());
  }

  protected static CalculationJobResult createTestJobResult(final CalculationJobSpecification jobSpec, final long time, final String nodeId) {
    return new CalculationJobResult(jobSpec, time, new ArrayList<CalculationJobResultItem>(), nodeId);
  }

  private class TestJobInvoker implements JobInvoker {

    private final String _nodeId;
    private JobInvokerRegister _callback;
    private boolean _disabled;

    public TestJobInvoker(final String nodeId) {
      _nodeId = nodeId;
    }

    @Override
    public boolean invoke(final CalculationJob job, final JobInvocationReceiver receiver) {
      if (_disabled) {
        return false;
      }
      _executorService.execute(new Runnable() {
        @Override
        public void run() {
          receiver.jobCompleted(createTestJobResult(job.getSpecification(), 0, _nodeId));
        }
      });
      return true;
    }

    @Override
    public void notifyWhenAvailable(JobInvokerRegister callback) {
      _callback = callback;
    }

    @Override
    public Collection<Capability> getCapabilities() {
      return Collections.emptySet();
    }

  }

  @Test
  public void registerInvokerWithJobPending() {
    s_logger.info("registerInvokerWithJobPending");
    final JobDispatcher jobDispatcher = new JobDispatcher();
    final TestJobResultReceiver result = new TestJobResultReceiver();
    final CalculationJob job = createTestJob();
    jobDispatcher.dispatchJob(job, result);
    assertNull(result.getResult());
    final TestJobInvoker jobInvoker = new TestJobInvoker("Test");
    jobDispatcher.registerJobInvoker(jobInvoker);
    final CalculationJobResult jobResult = result.waitForResult(TIMEOUT);
    assertNotNull(jobResult);
    assertEquals(job.getSpecification(), jobResult.getSpecification());
    assertNull(jobInvoker._callback);
  }

  @Test
  public void registerInvokerWithEmptyQueue() {
    s_logger.info("registerInvokerWithEmptyQueue");
    final JobDispatcher jobDispatcher = new JobDispatcher();
    final TestJobInvoker jobInvoker = new TestJobInvoker("Test");
    jobDispatcher.registerJobInvoker(jobInvoker);
    final TestJobResultReceiver result = new TestJobResultReceiver();
    final CalculationJob job = createTestJob();
    jobDispatcher.dispatchJob(job, result);
    final CalculationJobResult jobResult = result.waitForResult(TIMEOUT);
    assertNotNull(jobResult);
    assertEquals(job.getSpecification(), jobResult.getSpecification());
    assertNull(jobInvoker._callback);
  }

  private void nodeTest(final String expectedNodeId, final JobDispatcher jobDispatcher) {
    final TestJobResultReceiver result = new TestJobResultReceiver();
    final CalculationJob job = createTestJob();
    jobDispatcher.dispatchJob(job, result);
    final CalculationJobResult jobResult = result.waitForResult(TIMEOUT);
    assertNotNull(jobResult);
    assertEquals(job.getSpecification(), jobResult.getSpecification());
    assertEquals(expectedNodeId, jobResult.getComputeNodeId());
  }

  @Test
  public void invokeInRoundRobinOrder() {
    s_logger.info("invokeInRoundRobinOrder");
    final JobDispatcher jobDispatcher = new JobDispatcher();
    final TestJobInvoker node1 = new TestJobInvoker("1");
    final TestJobInvoker node2 = new TestJobInvoker("2");
    final TestJobInvoker node3 = new TestJobInvoker("3");
    jobDispatcher.registerJobInvoker(node1);
    jobDispatcher.registerJobInvoker(node2);
    jobDispatcher.registerJobInvoker(node3);
    nodeTest("1", jobDispatcher);
    assertNull(node1._callback);
    nodeTest("2", jobDispatcher);
    assertNull(node2._callback);
    nodeTest("3", jobDispatcher);
    assertNull(node3._callback);
    node1._disabled = true;
    nodeTest("2", jobDispatcher);
    assertNotNull(node1._callback);
    assertNull(node2._callback);
  }

  @Test
  public void saturateInvokers() {
    s_logger.info("saturateInvokers");
    final JobDispatcher jobDispatcher = new JobDispatcher();
    final JobInvoker[] jobInvokers = new JobInvoker[3];
    for (int i = 0; i < jobInvokers.length; i++) {
      final int iid = i + 1;
      jobDispatcher.registerJobInvoker(new JobInvoker() {

        private final Random rnd = new Random();
        private boolean _busy;
        private JobInvokerRegister _callback;

        @Override
        public boolean invoke(final CalculationJob job, final JobInvocationReceiver receiver) {
          final JobInvoker instance = this;
          synchronized (instance) {
            if (_busy) {
              return false;
            }
            _executorService.execute(new Runnable() {
              @Override
              public void run() {
                try {
                  Thread.sleep(rnd.nextInt(50));
                } catch (InterruptedException e) {
                  s_logger.warn("invoker {} interrupted", iid);
                }
                s_logger.debug("invoker {} completed job {}", iid, job.getSpecification());
                receiver.jobCompleted(createTestJobResult(job.getSpecification(), 0L, instance.toString()));
                synchronized (instance) {
                  _busy = false;
                  if (_callback != null) {
                    s_logger.debug("re-registering invoker {} with dispatcher", iid);
                    final JobInvokerRegister callback = _callback;
                    _callback = null;
                    callback.registerJobInvoker(instance);
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

        @Override
        public Collection<Capability> getCapabilities() {
          return Collections.emptySet();
        }

      });
    }
    final CalculationJob[] jobs = new CalculationJob[100];
    final TestJobResultReceiver[] resultReceivers = new TestJobResultReceiver[jobs.length];
    s_logger.debug("Dispatching {} jobs to {} nodes", jobs.length, jobInvokers.length);
    for (int i = 0; i < jobs.length; i++) {
      jobDispatcher.dispatchJob(jobs[i] = createTestJob(), resultReceivers[i] = new TestJobResultReceiver());
    }
    s_logger.debug("Jobs dispatched");
    for (int i = 0; i < jobs.length; i++) {
      s_logger.debug("Waiting for result {}", i);
      final CalculationJobResult result = resultReceivers[i].waitForResult(TIMEOUT * 2);
      assertNotNull(result);
      assertEquals(jobs[i].getSpecification(), result.getSpecification());
    }
    s_logger.debug("All jobs completed");
  }

  private class FailingJobInvoker implements JobInvoker {

    private int _failureCount;

    @Override
    public boolean invoke(final CalculationJob job, final JobInvocationReceiver receiver) {
      _executorService.execute(new Runnable() {
        @Override
        public void run() {
          s_logger.debug("Failing job {}", job.getSpecification());
          _failureCount++;
          receiver.jobFailed(FailingJobInvoker.this, "Fail", null);
        }
      });
      return true;
    }

    @Override
    public void notifyWhenAvailable(final JobInvokerRegister callback) {
      // shouldn't get called
      fail();
    }

    @Override
    public Collection<Capability> getCapabilities() {
      return Collections.emptySet();
    }

  }

  @Test
  public void testJobRetry() {
    s_logger.info("testJobRetry");
    final JobDispatcher jobDispatcher = new JobDispatcher();
    final TestJobResultReceiver result = new TestJobResultReceiver();
    final CalculationJob job = createTestJob ();
    jobDispatcher.dispatchJob(job, result);
    assertNull(result.getResult());
    final FailingJobInvoker failingInvoker = new FailingJobInvoker();
    final TestJobInvoker workingInvoker = new TestJobInvoker("Test");
    jobDispatcher.registerJobInvoker(failingInvoker);
    // The timeout below must be less than the timeout used to determine no-invokers available on retry
    CalculationJobResult jobResult = result.waitForResult(TIMEOUT);
    assertNull(jobResult);
    jobDispatcher.registerJobInvoker(workingInvoker);
    jobResult = result.waitForResult(TIMEOUT);
    assertNotNull(jobResult);
    assertEquals(1, failingInvoker._failureCount);
    assertEquals("Test", jobResult.getComputeNodeId());
    assertEquals(job.getSpecification(), jobResult.getSpecification());
  }

  private class BlockingJobInvoker implements JobInvoker {

    private final long _waitFor;

    private BlockingJobInvoker(final long waitFor) {
      _waitFor = waitFor;
    }

    @Override
    public Collection<Capability> getCapabilities() {
      return Collections.emptySet();
    }

    @Override
    public boolean invoke(final CalculationJob job, final JobInvocationReceiver receiver) {
      _executorService.execute(new Runnable() {
        @Override
        public void run() {
          try {
            Thread.sleep(_waitFor);
          } catch (InterruptedException e) {
          }
          receiver.jobCompleted(createTestJobResult(job.getSpecification(), 0, "BlockingNode"));
        }
      });
      return true;
    }

    @Override
    public void notifyWhenAvailable(JobInvokerRegister callback) {
    }

  }

  @Test
  public void testJobTimeoutFailure() {
    s_logger.info("testJobTimeoutFailure");
    final JobDispatcher jobDispatcher = new JobDispatcher();
    jobDispatcher.setMaxJobExecutionTime(TIMEOUT);
    jobDispatcher.setMaxJobAttempts(1);
    final TestJobResultReceiver result = new TestJobResultReceiver();
    jobDispatcher.dispatchJob(createTestJob (), result);
    assertNull(result.getResult());
    final BlockingJobInvoker blockingInvoker = new BlockingJobInvoker(2 * TIMEOUT);
    jobDispatcher.registerJobInvoker(blockingInvoker);
    CalculationJobResult jobResult = result.waitForResult(2 * TIMEOUT);
    assertNotNull(jobResult);
    assertEquals(jobDispatcher.getJobFailureNodeId(), jobResult.getComputeNodeId());
  }

  @Test
  public void testJobTimeoutSuccess() {
    s_logger.info("testJobTimeoutSuccess");
    final JobDispatcher jobDispatcher = new JobDispatcher();
    jobDispatcher.setMaxJobExecutionTime(2 * TIMEOUT);
    jobDispatcher.setMaxJobAttempts(1);
    final TestJobResultReceiver result = new TestJobResultReceiver();
    jobDispatcher.dispatchJob(createTestJob (), result);
    assertNull(result.getResult());
    final BlockingJobInvoker blockingInvoker = new BlockingJobInvoker(TIMEOUT);
    jobDispatcher.registerJobInvoker(blockingInvoker);
    CalculationJobResult jobResult = result.waitForResult(2 * TIMEOUT);
    assertNotNull(jobResult);
    assertEquals("BlockingNode", jobResult.getComputeNodeId());
  }

}
