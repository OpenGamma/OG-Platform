/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.exec;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.calcnode.CalculationJob;
import com.opengamma.engine.calcnode.CalculationJobResult;
import com.opengamma.engine.calcnode.CalculationJobSpecification;
import com.opengamma.engine.calcnode.JobResultReceiver;
import com.opengamma.engine.exec.plan.ExecutingGraph;
import com.opengamma.engine.exec.plan.GraphExecutionPlan;
import com.opengamma.engine.exec.stats.GraphExecutorStatisticsGatherer;
import com.opengamma.engine.view.cycle.SingleComputationCycle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.Cancelable;

/**
 * Executes a {@link GraphExecutionPlan} by forming jobs and submitting them to the available calculation nodes.
 */
public class PlanExecutor implements JobResultReceiver, Cancelable, DependencyGraphExecutionFuture {

  private static final Logger s_logger = LoggerFactory.getLogger(PlanExecutor.class);

  private static final class ExecutingJob implements Cancelable {

    private final CalculationJob _job;
    private volatile Cancelable _cancel;

    public ExecutingJob(final CalculationJob job) {
      _job = job;
    }

    public CalculationJob getJob() {
      return _job;
    }

    public void setCancel(final Cancelable cancel) {
      _cancel = cancel;
    }

    private Cancelable getCancel() {
      return _cancel;
    }

    // Cancelable

    @Override
    public boolean cancel(final boolean mayInterruptedIfRunning) {
      Cancelable cancel = getCancel();
      if (cancel != null) {
        s_logger.debug("Cancelling {} for job {}", _cancel, _job);
        return cancel.cancel(mayInterruptedIfRunning);
      } else {
        return false;
      }
    }

  }

  private enum State {
    NOT_STARTED, EXECUTING, CANCELLED, FINISHED;
  }

  private final SingleComputationCycle _cycle;
  private final ExecutingGraph _graph;
  private final AtomicInteger _notifyLock = new AtomicInteger();
  private Map<CalculationJobSpecification, ExecutingJob> _executing = new HashMap<CalculationJobSpecification, ExecutingJob>();
  private State _state;
  private int _nodeCount;
  private long _executionTime;
  private long _startTime;
  private Listener _listener;

  public PlanExecutor(final SingleComputationCycle cycle, final GraphExecutionPlan plan) {
    ArgumentChecker.notNull(cycle, "cycle");
    ArgumentChecker.notNull(plan, "plan");
    _cycle = cycle;
    _graph = plan.createExecution(cycle.getUniqueId(), cycle.getValuationTime(), cycle.getVersionCorrection());
    _state = State.NOT_STARTED;
    plan.reportStatistics(getStatisticsGatherer());
  }

  protected SingleComputationCycle getCycle() {
    return _cycle;
  }

  protected ExecutingGraph getGraph() {
    return _graph;
  }

  protected GraphExecutorStatisticsGatherer getStatisticsGatherer() {
    return getCycle().getViewProcessContext().getGraphExecutorStatisticsGathererProvider().getStatisticsGatherer(getCycle().getViewProcessId());
  }

  protected void storeTailJobs(final CalculationJob job) {
    for (CalculationJob tail : job.getTail()) {
      _executing.put(tail.getSpecification(), new ExecutingJob(tail));
      if (tail.getTail() != null) {
        storeTailJobs(tail);
      }
    }
  }

  protected void cancelableTailJobs(final CalculationJob job, final Cancelable handle) {
    for (CalculationJob tail : job.getTail()) {
      final ExecutingJob executing = _executing.get(tail.getSpecification());
      if (executing != null) {
        executing.setCancel(handle);
      }
      if (tail.getTail() != null) {
        cancelableTailJobs(tail, handle);
      }
    }
  }

  protected void submit(final CalculationJob job) {
    final ExecutingJob executing;
    synchronized (this) {
      if (_executing == null) {
        // Already complete or cancelled; don't submit anything new
        s_logger.debug("Not submitting {} - already completed or cancelled", job);
        return;
      }
      s_logger.debug("Submitting {}", job);
      executing = new ExecutingJob(job);
      _executing.put(job.getSpecification(), executing);
      if (job.getTail() != null) {
        storeTailJobs(job);
      }
    }
    final Cancelable handle = getCycle().getViewProcessContext().getComputationJobDispatcher().dispatchJob(job, this);
    executing.setCancel(handle);
    synchronized (this) {
      if (_executing == null) {
        // Completed or cancelled during the submission
        handle.cancel(true);
        return;
      }
      if (job.getTail() != null) {
        cancelableTailJobs(job, handle);
      }
    }
  }

  protected void submitExecutableJobs() {
    CalculationJob nextJob = getGraph().nextExecutableJob();
    int count = 0;
    while (nextJob != null) {
      submit(nextJob);
      nextJob = getGraph().nextExecutableJob();
      count++;
    }
    s_logger.info("Submitted {} executable jobs for {}", count, this);
  }

  public void start() {
    synchronized (this) {
      if (_state != State.NOT_STARTED) {
        s_logger.error("Already started executing {}", this);
        throw new IllegalStateException(_state.toString());
      }
      _state = State.EXECUTING;
      _startTime = System.nanoTime();
    }
    if (getGraph().isFinished()) {
      s_logger.info("Execution plan {} is empty", this);
      notifyComplete();
    } else {
      s_logger.info("Starting executing {}", this);
      submitExecutableJobs();
    }
  }

  protected long notifyComplete() {
    final long startTime;
    final Listener listener;
    synchronized (this) {
      if (_executing != null) {
        s_logger.info("Finished executing {}", this);
        _state = State.FINISHED;
        _executing = null;
      } else {
        s_logger.info("Already completed or cancelled execution of {}", this);
      }
      notifyAll();
      startTime = _startTime;
      listener = _listener;
      _listener = null;
    }
    if (listener != null) {
      listener.graphCompleted(getGraph().getCalculationConfiguration());
    }
    return System.nanoTime() - startTime;
  }

  // Cancelable

  @Override
  public boolean cancel(final boolean mayInterruptIfRunning) {
    final Collection<ExecutingJob> jobs;
    synchronized (this) {
      if (_executing == null) {
        // Already complete or cancelled
        s_logger.warn("Can't cancel - already completed or previously cancelled execution of {}", this);
        return false;
      }
      jobs = _executing.values();
      _executing = null;
      _state = State.CANCELLED;
      notifyAll();
    }
    s_logger.info("Cancelling current jobs of {}", this);
    for (ExecutingJob job : jobs) {
      job.cancel(mayInterruptIfRunning);
    }
    return true;
  }

  // JobResultReceiver

  @Override
  public void resultReceived(final CalculationJobResult result) {
    final ExecutingJob job;
    synchronized (this) {
      if (_executing == null) {
        // Already cancelled (or complete)
        s_logger.debug("Ignoring result for already completed (or cancelled) {}", this);
        return;
      }
      job = _executing.remove(result.getSpecification());
      if (job == null) {
        s_logger.warn("Unexpected (or duplicate completion of) {} for {}", result, this);
        return;
      }
      _nodeCount += result.getResultItems().size();
      _executionTime += result.getDuration();
    }
    final ExecutingGraph graph = getGraph();
    _notifyLock.incrementAndGet();
    graph.jobCompleted(result.getSpecification());
    s_logger.debug("{} completed for {}", result, this);
    submitExecutableJobs();
    getCycle().jobCompleted(job.getJob(), result);
    if (_notifyLock.decrementAndGet() == 0) {
      if (graph.isFinished()) {
        // If the lock count is still 0, then we will notify completion. If another thread caused graph completion, and is still notifying
        // the job completion, the count will be positive. If another thread caused graph completion and got here before us then the count
        // will already be -1.
        if (_notifyLock.compareAndSet(0, -1)) {
          final long duration = notifyComplete();
          getStatisticsGatherer().graphExecuted(graph.getCalculationConfiguration(), _nodeCount, _executionTime, duration);
        }
      }
    }
  }

  // Future

  @Override
  public synchronized boolean isCancelled() {
    return _state == State.CANCELLED;
  }

  @Override
  public boolean isDone() {
    return _executing == null;
  }

  @Override
  public synchronized String get() throws InterruptedException, ExecutionException {
    while (_executing != null) {
      s_logger.debug("Waiting for completion of {}", this);
      wait();
    }
    if (_state == State.CANCELLED) {
      s_logger.info("Cancelled {}", this);
      throw new CancellationException();
    }
    return getGraph().getCalculationConfiguration();
  }

  @Override
  public synchronized String get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    if (_executing != null) {
      s_logger.debug("Waiting for completion of {}", this);
      wait(unit.toMillis(timeout));
      if (_executing != null) {
        s_logger.info("Timeout on {}", this);
        throw new TimeoutException();
      }
    }
    if (_state == State.CANCELLED) {
      s_logger.warn("Cancelled {}", this);
      throw new CancellationException();
    }
    return getGraph().getCalculationConfiguration();
  }

  @Override
  public void setListener(final Listener listener) {
    synchronized (this) {
      _listener = listener;
      if (_executing != null) {
        return;
      }
    }
    listener.graphCompleted(_graph.getCalculationConfiguration());
  }

  // Object

  @Override
  public String toString() {
    return getGraph().toString() + " for " + getCycle().toString();
  }

}
