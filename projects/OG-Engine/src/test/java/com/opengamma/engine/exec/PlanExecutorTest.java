/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.exec;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.cache.CacheSelectHint;
import com.opengamma.engine.calcnode.CalculationJob;
import com.opengamma.engine.calcnode.CalculationJobItem;
import com.opengamma.engine.calcnode.CalculationJobResult;
import com.opengamma.engine.calcnode.CalculationJobResultItem;
import com.opengamma.engine.calcnode.JobDispatcher;
import com.opengamma.engine.calcnode.JobResultReceiver;
import com.opengamma.engine.exec.DependencyGraphExecutionFuture.Listener;
import com.opengamma.engine.exec.plan.GraphExecutionPlan;
import com.opengamma.engine.exec.plan.PlannedJob;
import com.opengamma.engine.exec.stats.GraphExecutionStatistics;
import com.opengamma.engine.exec.stats.GraphExecutorStatisticsGatherer;
import com.opengamma.engine.exec.stats.TotallingGraphStatisticsGathererProvider;
import com.opengamma.engine.exec.stats.TotallingGraphStatisticsGathererProvider.Statistics;
import com.opengamma.engine.function.EmptyFunctionParameters;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ExecutionLog;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.engine.view.cycle.SingleComputationCycle;
import com.opengamma.engine.view.impl.ViewProcessContext;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.async.Cancelable;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Tests the {@link PlanExecutor} class.
 */
@Test(groups = TestGroup.UNIT)
public class PlanExecutorTest {

  private static final Logger s_logger = LoggerFactory.getLogger(PlanExecutorTest.class);

  private ViewProcessContext createViewProcessContext(final JobDispatcher jobDispatcher) {
    final ViewProcessContext context = Mockito.mock(ViewProcessContext.class);
    Mockito.when(context.getComputationJobDispatcher()).thenReturn(jobDispatcher);
    Mockito.when(context.getGraphExecutorStatisticsGathererProvider()).thenReturn(new TotallingGraphStatisticsGathererProvider());
    return context;
  }

  private SingleComputationCycle createCycle(final JobDispatcher jobDispatcher) {
    final Instant now = Instant.now();
    final SingleComputationCycle cycle = Mockito.mock(SingleComputationCycle.class);
    Mockito.when(cycle.getUniqueId()).thenReturn(UniqueId.of("Cycle", "Test"));
    Mockito.when(cycle.getValuationTime()).thenReturn(now);
    Mockito.when(cycle.getVersionCorrection()).thenReturn(VersionCorrection.of(now, now));
    final ViewProcessContext context = createViewProcessContext(jobDispatcher);
    Mockito.when(cycle.getViewProcessContext()).thenReturn(context);
    Mockito.when(cycle.getViewProcessId()).thenReturn(UniqueId.of("View", "Test"));
    Mockito.when(cycle.toString()).thenReturn("TEST-CYCLE");
    return cycle;
  }

  private List<CalculationJobItem> createJobItems(final int count) {
    final List<CalculationJobItem> result = new ArrayList<CalculationJobItem>(count);
    for (int i = 0; i < count; i++) {
      result.add(new CalculationJobItem("Func", new EmptyFunctionParameters(), ComputationTargetSpecification.NULL, Collections.<ValueSpecification>emptySet(), Collections
          .<ValueSpecification>emptySet(),
          ExecutionLogMode.INDICATORS));
    }
    return result;
  }

  private List<CalculationJobResultItem> createResultItems(final List<CalculationJobItem> items) {
    final List<CalculationJobResultItem> result = new ArrayList<CalculationJobResultItem>(items.size());
    for (int i = 0; i < items.size(); i++) {
      result.add(new CalculationJobResultItem(Collections.<ValueSpecification>emptySet(), Collections.<ValueSpecification>emptySet(), ExecutionLog.EMPTY));
    }
    return result;
  }

  private CalculationJobResult createJobResult(final CalculationJob job) {
    return new CalculationJobResult(job.getSpecification(), 10L, createResultItems(job.getJobItems()), "Test");
  }

  private GraphExecutionPlan createPlan() {
    final PlannedJob job1 = new PlannedJob(1, createJobItems(1), CacheSelectHint.allShared(), null, null);
    final PlannedJob job2 = new PlannedJob(1, createJobItems(2), CacheSelectHint.allShared(), null, new PlannedJob[] {job1 });
    final PlannedJob job3 = new PlannedJob(0, createJobItems(3), CacheSelectHint.allShared(), new PlannedJob[] {job2 }, null);
    return new GraphExecutionPlan("Default", 0, Arrays.asList(job3), 3, 2d, 10d, 20d);
  }

  private class NormalExecutionJobDispatcher extends JobDispatcher {

    private final Queue<Pair<CalculationJob, JobResultReceiver>> _jobs = new LinkedList<Pair<CalculationJob, JobResultReceiver>>();

    private final Queue<Pair<CalculationJob, CalculationJobResult>> _results = new LinkedList<Pair<CalculationJob, CalculationJobResult>>();

    @Override
    public Cancelable dispatchJob(final CalculationJob job, final JobResultReceiver receiver) {
      s_logger.debug("Dispatching {}", job);
      _jobs.add(Pairs.of(job, receiver));
      if (job.getTail() != null) {
        for (CalculationJob tail : job.getTail()) {
          dispatchJob(tail, receiver);
        }
      }
      return Mockito.mock(Cancelable.class);
    }

    protected void notify(final CalculationJob job, final JobResultReceiver receiver) {
      s_logger.debug("Notifying {}", job);
      final CalculationJobResult result = createJobResult(job);
      _results.add(Pairs.of(job, result));
      receiver.resultReceived(result);
    }

    public void completeJobs() {
      do {
        Pair<CalculationJob, JobResultReceiver> job = _jobs.poll();
        if (job == null) {
          s_logger.debug("No more jobs");
          return;
        }
        s_logger.debug("Completing {}", job.getFirst());
        notify(job.getFirst(), job.getSecond());
      } while (true);
    }

    public void execute(final PlanExecutor executor) {
      executor.start();
      completeJobs();
    }

    public Pair<CalculationJob, CalculationJobResult> pollResult() {
      return _results.poll();
    }

  }

  public void testStatisticsReporting() {
    final NormalExecutionJobDispatcher dispatcher = new NormalExecutionJobDispatcher();
    final PlanExecutor executor = new PlanExecutor(createCycle(dispatcher), createPlan());
    final GraphExecutorStatisticsGatherer statsGatherer = executor.getCycle().getViewProcessContext().getGraphExecutorStatisticsGathererProvider().getStatisticsGatherer(UniqueId.of("View", "Test"));
    final GraphExecutionStatistics stats = ((Statistics) statsGatherer).getExecutionStatistics().get(0);
    assertEquals(stats.getCalcConfigName(), "Default");
    assertEquals(stats.getProcessedGraphs(), 1L);
    assertEquals(stats.getAverageJobSize(), 2d);
    assertEquals(stats.getAverageJobCycleCost(), 10d);
    assertEquals(stats.getAverageJobDataCost(), 20d);
    dispatcher.execute(executor);
    assertEquals(stats.getExecutedGraphs(), 1L);
    assertEquals(stats.getExecutedNodes(), 6L);
    assertEquals(stats.getExecutionTime(), 30L);
  }

  // Timeout is set just in case "get" blocks rather than returns immediately
  @Test(timeOut = 5000)
  public void testNormalExecution() throws Throwable {
    final NormalExecutionJobDispatcher dispatcher = new NormalExecutionJobDispatcher();
    final PlanExecutor executor = new PlanExecutor(createCycle(dispatcher), createPlan());
    dispatcher.execute(executor);
    final SingleComputationCycle cycle = executor.getCycle();
    Pair<CalculationJob, CalculationJobResult> result = dispatcher.pollResult();
    Mockito.verify(cycle, Mockito.times(1)).jobCompleted(result.getFirst(), result.getSecond());
    result = dispatcher.pollResult();
    Mockito.verify(cycle, Mockito.times(1)).jobCompleted(result.getFirst(), result.getSecond());
    result = dispatcher.pollResult();
    Mockito.verify(cycle, Mockito.times(1)).jobCompleted(result.getFirst(), result.getSecond());
    assertNull(dispatcher.pollResult());
    assertFalse(executor.isCancelled());
    assertTrue(executor.isDone());
    assertEquals(executor.get(1, TimeUnit.SECONDS), "Default");
    assertEquals(executor.get(), "Default");
  }

  public void testDuplicateJobNotifications() {
    final NormalExecutionJobDispatcher dispatcher = new NormalExecutionJobDispatcher() {
      @Override
      public void notify(final CalculationJob job, final JobResultReceiver receiver) {
        super.notify(job, receiver);
        super.notify(job, receiver);
      }
    };
    final PlanExecutor executor = new PlanExecutor(createCycle(dispatcher), createPlan());
    dispatcher.execute(executor);
    final SingleComputationCycle cycle = executor.getCycle();
    Pair<CalculationJob, CalculationJobResult> result = dispatcher.pollResult();
    Mockito.verify(cycle, Mockito.times(1)).jobCompleted(result.getFirst(), result.getSecond());
    assertNotNull(dispatcher.pollResult()); // Second one will be discarded
    result = dispatcher.pollResult();
    Mockito.verify(cycle, Mockito.times(1)).jobCompleted(result.getFirst(), result.getSecond());
    assertNotNull(dispatcher.pollResult()); // Duplicate will be discarded
    result = dispatcher.pollResult();
    Mockito.verify(cycle, Mockito.times(1)).jobCompleted(result.getFirst(), result.getSecond());
    assertNotNull(dispatcher.pollResult()); // Duplicate will be discarded
    assertNull(dispatcher.pollResult());
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void testDuplicateStart() {
    final PlanExecutor executor = new PlanExecutor(createCycle(new JobDispatcher()), createPlan());
    executor.start();
    executor.start();
  }

  public void testCancelStoppingJobs() {
    final AtomicBoolean canceled = new AtomicBoolean();
    final JobDispatcher dispatcher = new JobDispatcher() {
      @Override
      public Cancelable dispatchJob(final CalculationJob job, final JobResultReceiver receiver) {
        return new Cancelable() {
          @Override
          public boolean cancel(final boolean mayInterruptIfRunning) {
            assertTrue(mayInterruptIfRunning);
            canceled.set(true);
            return true;
          }
        };
      }
    };
    final PlanExecutor executor = new PlanExecutor(createCycle(dispatcher), createPlan());
    executor.start();
    assertFalse(canceled.get());
    assertTrue(executor.cancel(true));
    assertTrue(canceled.get());
    assertTrue(executor.isCancelled());
    assertTrue(executor.isDone());
  }

  public void testCancelNotStoppingJobs() {
    final NormalExecutionJobDispatcher dispatcher = new NormalExecutionJobDispatcher();
    final PlanExecutor executor = new PlanExecutor(createCycle(dispatcher), createPlan());
    executor.start();
    assertTrue(executor.cancel(true));
    dispatcher.completeJobs();
    assertTrue(executor.isCancelled());
    assertTrue(executor.isDone());
    // The first job (and its tail) should have attempted to complete, but the cycle not notified
    final SingleComputationCycle cycle = executor.getCycle();
    Pair<CalculationJob, CalculationJobResult> result = dispatcher.pollResult();
    Mockito.verify(cycle, Mockito.never()).jobCompleted(result.getFirst(), result.getSecond());
    result = dispatcher.pollResult();
    Mockito.verify(cycle, Mockito.never()).jobCompleted(result.getFirst(), result.getSecond());
    // No third job
    assertNull(dispatcher.pollResult());
  }

  public void testCancelDuringDispatch() {
    final Cancelable handle = Mockito.mock(Cancelable.class);
    final AtomicReference<PlanExecutor> executor = new AtomicReference<PlanExecutor>();
    final JobDispatcher dispatcher = new JobDispatcher() {
      @Override
      public Cancelable dispatchJob(final CalculationJob job, final JobResultReceiver receiver) {
        executor.get().cancel(true);
        return handle;
      }
    };
    executor.set(new PlanExecutor(createCycle(dispatcher), createPlan()));
    executor.get().start();
    assertTrue(executor.get().isCancelled());
    assertTrue(executor.get().isDone());
    Mockito.verify(handle).cancel(true);
  }

  public void testCancelAfterCompletion() {
    final NormalExecutionJobDispatcher dispatcher = new NormalExecutionJobDispatcher();
    final PlanExecutor executor = new PlanExecutor(createCycle(dispatcher), createPlan());
    dispatcher.execute(executor);
    assertFalse(executor.isCancelled());
    assertFalse(executor.cancel(true));
    assertFalse(executor.isCancelled());
  }

  public void testCancelBeforeJobSubmission() {
    final JobDispatcher dispatcher = Mockito.mock(JobDispatcher.class);
    final PlanExecutor executor = new PlanExecutor(createCycle(dispatcher), createPlan()) {
      @Override
      protected void submitExecutableJobs() {
        cancel(true);
        super.submitExecutableJobs();
      }
    };
    assertFalse(executor.isCancelled());
    executor.start();
    assertTrue(executor.isCancelled());
    Mockito.verifyZeroInteractions(dispatcher);
  }

  public void testAddListenerBeforeCompletion() throws Throwable {
    final NormalExecutionJobDispatcher dispatcher = new NormalExecutionJobDispatcher();
    final PlanExecutor executor = new PlanExecutor(createCycle(dispatcher), createPlan());
    final AtomicReference<String> result = new AtomicReference<String>();
    executor.setListener(new Listener() {
      @Override
      public void graphCompleted(final String calculationConfiguration) {
        assertEquals(result.getAndSet(calculationConfiguration), null);
      }
    });
    dispatcher.execute(executor);
    assertEquals(result.get(), "Default");
  }

  public void testAddListenerAfterCompletion() throws Throwable {
    final NormalExecutionJobDispatcher dispatcher = new NormalExecutionJobDispatcher();
    final PlanExecutor executor = new PlanExecutor(createCycle(dispatcher), createPlan());
    dispatcher.execute(executor);
    final AtomicReference<String> result = new AtomicReference<String>();
    executor.setListener(new Listener() {
      @Override
      public void graphCompleted(final String calculationConfiguration) {
        assertEquals(result.getAndSet(calculationConfiguration), null);
      }
    });
    assertEquals(result.get(), "Default");
  }

  // Timeout is set just in case "get" decides to block
  @Test(timeOut = 5000, expectedExceptions = CancellationException.class)
  public void testCancelDuringGet() throws Throwable {
    final ExecutorService threads = Executors.newSingleThreadExecutor();
    try {
      final PlanExecutor executor = new PlanExecutor(createCycle(new JobDispatcher()), createPlan());
      executor.start();
      threads.submit(new Runnable() {
        @Override
        public void run() {
          executor.cancel(true);
        }
      });
      executor.get();
    } finally {
      threads.shutdownNow();
      threads.awaitTermination(3, TimeUnit.SECONDS);
    }
  }

  // Timeout is set just in case "get" decides to block
  @Test(timeOut = 5000, expectedExceptions = CancellationException.class)
  public void testCancelDuringGetWithTimeout() throws Throwable {
    final ExecutorService threads = Executors.newSingleThreadExecutor();
    try {
      final PlanExecutor executor = new PlanExecutor(createCycle(new JobDispatcher()), createPlan());
      executor.start();
      threads.submit(new Runnable() {
        @Override
        public void run() {
          executor.cancel(true);
        }
      });
      executor.get(5, TimeUnit.SECONDS);
    } finally {
      threads.shutdownNow();
      threads.awaitTermination(3, TimeUnit.SECONDS);
    }
  }

  // Timeout is set just in case "get" decides to block
  @Test(timeOut = 5000)
  public void testGet() throws Throwable {
    final ExecutorService threads = Executors.newSingleThreadExecutor();
    try {
      final NormalExecutionJobDispatcher dispatcher = new NormalExecutionJobDispatcher();
      final PlanExecutor executor = new PlanExecutor(createCycle(dispatcher), createPlan());
      threads.submit(new Runnable() {
        @Override
        public void run() {
          dispatcher.execute(executor);
        }
      });
      assertEquals(executor.get(), "Default");
    } finally {
      threads.shutdownNow();
      threads.awaitTermination(3, TimeUnit.SECONDS);
    }
  }

  // Timeout is set just in case "get" decides to block
  @Test(timeOut = 5000)
  public void testGetWithTimeout() throws Throwable {
    final ExecutorService threads = Executors.newSingleThreadExecutor();
    try {
      final NormalExecutionJobDispatcher dispatcher = new NormalExecutionJobDispatcher();
      final PlanExecutor executor = new PlanExecutor(createCycle(dispatcher), createPlan());
      threads.submit(new Runnable() {
        @Override
        public void run() {
          dispatcher.execute(executor);
        }
      });
      assertEquals(executor.get(5, TimeUnit.SECONDS), "Default");
    } finally {
      threads.shutdownNow();
      threads.awaitTermination(3, TimeUnit.SECONDS);
    }
  }

  // Timeout is set just in case "get" decides to block
  @Test(timeOut = 5000, expectedExceptions = TimeoutException.class)
  public void testGetWithElapsedTimeout() throws Throwable {
    final ExecutorService threads = Executors.newSingleThreadExecutor();
    try {
      final NormalExecutionJobDispatcher dispatcher = new NormalExecutionJobDispatcher();
      final PlanExecutor executor = new PlanExecutor(createCycle(dispatcher), createPlan());
      executor.get(1, TimeUnit.SECONDS);
    } finally {
      threads.shutdownNow();
      threads.awaitTermination(3, TimeUnit.SECONDS);
    }
  }

  public void testToString() {
    final PlanExecutor executor = new PlanExecutor(createCycle(new JobDispatcher()), createPlan());
    assertEquals(executor.toString(), "ExecutingGraph-Default for TEST-CYCLE");
  }

}
