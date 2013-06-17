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
    private final Cancelable _cancel;

    public ExecutingJob(final CalculationJob job, final Cancelable cancel) {
      _job = job;
      _cancel = cancel;
    }

    public CalculationJob getJob() {
      return _job;
    }

    // Cancelable

    @Override
    public boolean cancel(final boolean mayInterruptedIfRunning) {
      s_logger.debug("Cancelling {} for job {}", _cancel, _job);
      return _cancel.cancel(mayInterruptedIfRunning);
    }

  }

  private enum State {
    NOT_STARTED,
    EXECUTING,
    CANCELLED,
    FINISHED;
  }

  private final SingleComputationCycle _cycle;
  private final ExecutingGraph _graph;
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

  protected synchronized void submit(CalculationJob job) {
    if (_executing != null) {
      s_logger.debug("Submitting {}", job);
      _executing.put(job.getSpecification(), new ExecutingJob(job, getCycle().getViewProcessContext().getComputationJobDispatcher().dispatchJob(job, this)));
    } else {
      // Already complete or cancelled; don't submit anything new
      s_logger.debug("Not submitting {} - already completed or cancelled", job);
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
    s_logger.info("Starting executing {}", this);
    submitExecutableJobs();
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
    }
    if (listener != null) {
      listener.graphCompleted(_graph.getCalculationConfiguration());
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
    }
    s_logger.info("Cancelling current jobs of {}", this);
    boolean result = true;
    for (ExecutingJob job : jobs) {
      if (!job.cancel(mayInterruptIfRunning)) {
        result = false;
      }
    }
    return result;
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
    graph.jobCompleted(result.getSpecification());
    s_logger.debug("{} completed for {}", result, this);
    submitExecutableJobs();
    getCycle().jobCompleted(job.getJob(), result);
    if (graph.isFinished()) {
      final long duration = notifyComplete();
      getStatisticsGatherer().graphExecuted(graph.getCalculationConfiguration(), _nodeCount, _executionTime, duration);
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
    return _graph.getCalculationConfiguration();
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
    return _graph.getCalculationConfiguration();
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
    return _graph.toString() + " for " + _cycle.toString();
  }

}
