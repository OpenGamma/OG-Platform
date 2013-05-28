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
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.opengamma.engine.calcnode.CalculationJob;
import com.opengamma.engine.calcnode.CalculationJobResult;
import com.opengamma.engine.calcnode.CalculationJobSpecification;
import com.opengamma.engine.calcnode.JobResultReceiver;
import com.opengamma.engine.exec.plan.ExecutingGraph;
import com.opengamma.engine.exec.plan.GraphExecutionPlan;
import com.opengamma.engine.view.cycle.SingleComputationCycle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.Cancelable;

/**
 * Executes a {@link GraphExecutionPlan} by forming jobs and submitting them to the available calculation nodes.
 */
public class PlanExecutor implements JobResultReceiver, Cancelable, Future<Object> {

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

  // NOTE: The caller is responsible for passing statistics to the gatherer

  public PlanExecutor(final SingleComputationCycle cycle, final GraphExecutionPlan plan) {
    ArgumentChecker.notNull(cycle, "cycle");
    ArgumentChecker.notNull(plan, "plan");
    _cycle = cycle;
    _graph = plan.createExecution(cycle.getUniqueId(), cycle.getValuationTime(), cycle.getVersionCorrection());
    _state = State.NOT_STARTED;
  }

  protected SingleComputationCycle getCycle() {
    return _cycle;
  }

  protected ExecutingGraph getGraph() {
    return _graph;
  }

  protected synchronized void submit(CalculationJob job) {
    if (_executing != null) {
      // Already complete or cancelled; don't submit anything new
      _executing.put(job.getSpecification(), new ExecutingJob(job, getCycle().getViewProcessContext().getComputationJobDispatcher().dispatchJob(job, this)));
    }
  }

  protected void submitExecutableJobs() {
    CalculationJob nextJob = getGraph().nextExecutableJob();
    while (nextJob != null) {
      submit(nextJob);
      nextJob = getGraph().nextExecutableJob();
    }
  }

  public void start() {
    synchronized (this) {
      if (_state != State.NOT_STARTED) {
        throw new IllegalStateException(_state.toString());
      }
      _state = State.EXECUTING;
    }
    submitExecutableJobs();
  }

  protected synchronized void notifyComplete() {
    if (_executing != null) {
      _state = State.FINISHED;
      _executing = null;
    }
    notifyAll();
  }

  // Cancelable

  @Override
  public boolean cancel(final boolean mayInterruptIfRunning) {
    final Collection<ExecutingJob> jobs;
    synchronized (this) {
      if (_executing == null) {
        // Already complete or cancelled
        return false;
      }
      jobs = _executing.values();
      _executing = null;
      _state = State.CANCELLED;
    }
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
        return;
      }
      job = _executing.remove(result.getSpecification());
    }
    if (job != null) {
      final ExecutingGraph graph = getGraph();
      if (graph.jobCompleted(result.getSpecification())) {
        CalculationJob nextJob = graph.nextExecutableJob();
        while (nextJob != null) {
          submit(nextJob);
          nextJob = graph.nextExecutableJob();
        }
        getCycle().jobCompleted(job.getJob(), result);
        if (graph.isFinished()) {
          notifyComplete();
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
  public synchronized Object get() throws InterruptedException, ExecutionException {
    while (_executing != null) {
      wait();
    }
    if (_state == State.CANCELLED) {
      throw new CancellationException();
    }
    return null;
  }

  @Override
  public synchronized Object get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    if (_executing != null) {
      wait(unit.toMillis(timeout));
    }
    if (_state == State.CANCELLED) {
      throw new CancellationException();
    }
    return null;
  }

}
