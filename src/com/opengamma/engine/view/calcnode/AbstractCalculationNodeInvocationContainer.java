/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;

/**
 * Base class for objects that manage a set of AbstractCalculationNodes with the intention of
 * invoking job executions on them.
 */
public abstract class AbstractCalculationNodeInvocationContainer {

  private static final int CLEANUP_PERIOD = 100; // clean up after every 100 jobs
  private static final long RETENTION = 1000000000L; // 10s

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractCalculationNodeInvocationContainer.class);

  /**
   * 
   */
  protected static interface ExecutionReceiver {

    void executionFailed(AbstractCalculationNode node, Exception exception);

    void executionComplete(CalculationJobResult result);

  }

  private static class JobEntry {

    private final CalculationJob _job;
    private ExecutionReceiver _receiver;
    private Collection<JobExecution> _required;

    public JobEntry(final CalculationJob job, final ExecutionReceiver receiver) {
      _job = job;
      _receiver = receiver;
    }

    public CalculationJob getJob() {
      return _job;
    }

    public ExecutionReceiver getReceiver() {
      return _receiver;
    }

    public Collection<JobExecution> getRequired() {
      return _required;
    }

    public void setRequired(final Collection<JobExecution> required) {
      _required = required;
    }

    public void invalidate() {
      _receiver = null;
    }

    @Override
    public int hashCode() {
      return getJob().hashCode();
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof JobEntry)) {
        return false;
      }
      return getJob().equals(((JobEntry) o).getJob());
    }

  }

  private static class JobExecution {

    private enum Status {
      RUNNING, COMPLETED, FAILED;
    }

    private final long _timestamp;
    private Status _status = Status.RUNNING;
    private Set<JobEntry> _blocked;

    public JobExecution() {
      _timestamp = System.nanoTime();
    }

    public long getAge() {
      return System.nanoTime() - _timestamp;
    }

    public Status getStatus() {
      return _status;
    }

    public void setStatus(final Status status) {
      _status = status;
    }

    // Caller must own the monitor
    public Set<JobEntry> getBlocked() {
      Set<JobEntry> blocked = _blocked;
      _blocked = null;
      return blocked;
    }

    // Caller must own the monitor
    public void blockJob(final JobEntry job) {
      if (_blocked == null) {
        _blocked = new HashSet<JobEntry>();
      }
      _blocked.add(job);
    }

  }

  private final Queue<AbstractCalculationNode> _nodes = new ConcurrentLinkedQueue<AbstractCalculationNode>();
  private final ConcurrentMap<Long, JobExecution> _executions = new ConcurrentSkipListMap<Long, JobExecution>();
  private final Queue<JobEntry> _runnableJobs = new ConcurrentLinkedQueue<JobEntry>();
  private final AtomicInteger _jobCount = new AtomicInteger();

  protected Queue<AbstractCalculationNode> getNodes() {
    return _nodes;
  }

  public void addNode(final AbstractCalculationNode node) {
    ArgumentChecker.notNull(node, "node");
    getNodes().add(node);
    onNodeChange();
  }

  public void addNodes(final Collection<AbstractCalculationNode> nodes) {
    ArgumentChecker.notNull(nodes, "nodes");
    getNodes().addAll(nodes);
    onNodeChange();
  }

  public void setNode(final AbstractCalculationNode node) {
    ArgumentChecker.notNull(node, "node");
    getNodes().clear();
    getNodes().add(node);
    onNodeChange();
  }

  public void setNodes(final Collection<AbstractCalculationNode> nodes) {
    ArgumentChecker.notNull(nodes, "nodes");
    getNodes().clear();
    getNodes().addAll(nodes);
    onNodeChange();
  }

  protected abstract void onNodeChange();

  protected void onJobStart(final CalculationJob job) {
  }

  private JobExecution getExecution(final Long jobId) {
    JobExecution execution = _executions.get(jobId);
    if (execution == null) {
      execution = new JobExecution();
      final JobExecution newExecution = _executions.putIfAbsent(jobId, execution);
      if (newExecution != null) {
        return newExecution;
      }
    }
    return execution;
  }

  private void addJobToRunnableQueue(final JobEntry jobexec) {
    final Collection<Long> requiredJobIds = jobexec.getJob().getRequiredJobIds();
    if (requiredJobIds != null) {
      if (jobexec.getRequired() == null) {
        final List<JobExecution> required = new ArrayList<JobExecution>(requiredJobIds.size());
        for (Long requiredId : requiredJobIds) {
          required.add(getExecution(requiredId));
        }
        jobexec.setRequired(required);
      }
      for (JobExecution execution : jobexec.getRequired()) {
        synchronized (execution) {
          switch (execution.getStatus()) {
            case COMPLETED:
              // Job can run
              break;
            case FAILED:
              // Job is not to run - something's already failed
              s_logger.debug("Job {} dropped by failure of earlier job", jobexec.getJob().getSpecification().getJobId());
              return;
            case RUNNING:
              // Job is blocked
              execution.blockJob(jobexec);
              s_logger.debug("Job {} blocked", jobexec.getJob().getSpecification().getJobId());
              return;
          }
        }
      }
    }
    s_logger.debug("Adding job {} to runnable queue", jobexec.getJob().getSpecification().getJobId());
    _runnableJobs.add(jobexec);
  }

  private void failExecution(final JobExecution execution) {
    final Set<JobEntry> blocked;
    synchronized (execution) {
      execution.setStatus(JobExecution.Status.FAILED);
      blocked = execution.getBlocked();
    }
    if (blocked != null) {
      for (JobEntry tail : blocked) {
        tail.invalidate();
        failExecution(getExecution(tail.getJob().getSpecification().getJobId()));
      }
    }
  }

  /**
   * Executes a job, or queues it somewhere to run. This may return immediately if there is another thread that will
   * end up executing the job, or may run more than one job before returning. 
   * 
   * @param node Node to run on, or {@code null} to wait for one to become available. The node will be returned to the container
   * after execution.
   * @param job The job to run.
   * @param receiver The callback object for successful or failed execution.
   */
  protected void executeJob(AbstractCalculationNode node, final CalculationJob job, final ExecutionReceiver receiver) {
    addJobToRunnableQueue(new JobEntry(job, receiver));
    JobEntry jobexec = _runnableJobs.poll();
    while (jobexec != null) {
      if (node == null) {
        s_logger.debug("Requesting calculation node from pool");
        node = getNodes().poll();
        if (node == null) {
          // No nodes available - one of the other threads will take something from the runnable queue
          _runnableJobs.add(jobexec);
          s_logger.debug("No nodes available");
          break;
        }
      }
      s_logger.info("Executing job {}", jobexec.getJob().getSpecification().getJobId());
      onJobStart(jobexec.getJob());
      final JobExecution execution = getExecution(jobexec.getJob().getSpecification().getJobId());
      CalculationJobResult result = null;
      try {
        result = node.executeJob(jobexec.getJob());
      } catch (Exception e) {
        // Any tail jobs will be abandoned
        s_logger.warn("Job {} failed", jobexec.getJob().getSpecification().getJobId());
        failExecution(execution);
        jobexec.getReceiver().executionFailed(node, e);
      }
      if (result != null) {
        final Set<JobEntry> blocked;
        synchronized (execution) {
          execution.setStatus(JobExecution.Status.COMPLETED);
          blocked = execution.getBlocked();
        }
        if (blocked != null) {
          s_logger.info("Job {} completed - releasing blocked jobs", jobexec.getJob().getSpecification().getJobId());
          for (JobEntry tail : blocked) {
            if (tail.getReceiver() != null) {
              addJobToRunnableQueue(tail);
            }
          }
        } else {
          s_logger.info("Job {} completed - no tail jobs", jobexec.getJob().getSpecification().getJobId());
        }
      }
      s_logger.debug("Returning calculation node to pool");
      addNode(node);
      node = null;
      if (result != null) {
        jobexec.getReceiver().executionComplete(result);
      }
      jobexec = _runnableJobs.poll();
    }
    if (_jobCount.incrementAndGet() % CLEANUP_PERIOD == 0) {
      int count = 0;
      final Iterator<Map.Entry<Long, JobExecution>> entryIterator = _executions.entrySet().iterator();
      while (entryIterator.hasNext()) {
        final Map.Entry<Long, JobExecution> entry = entryIterator.next();
        if (entry.getValue().getStatus() != JobExecution.Status.RUNNING) {
          if (entry.getValue().getAge() > RETENTION) {
            s_logger.debug("Removed job {} from execution map", entry.getKey());
            entryIterator.remove();
            count++;
          } else {
            break;
          }
        }
      }
      s_logger.info("Removed {} dead entries from execution map, {} remaining", count, _executions.size());
    }
  }
}
