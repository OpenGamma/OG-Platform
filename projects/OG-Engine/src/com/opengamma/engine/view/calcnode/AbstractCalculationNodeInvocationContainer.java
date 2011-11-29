/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Base class for objects that manage a set of AbstractCalculationNodes with the intention of
 * invoking job executions on them.
 */
public abstract class AbstractCalculationNodeInvocationContainer {

  /**
   * After how many failed jobs should a cleanup be attempted.
   */
  private static final int FAILURE_CLEANUP_PERIOD = 100; // clean up after every 100 failed jobs

  /**
   * Retention period for failed jobs needs to be long enough for any tail jobs to have arrived.
   */
  private static final long FAILURE_RETENTION = 5L * 60L * 100000000L; // 5m

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractCalculationNodeInvocationContainer.class);

  /**
   * 
   */
  protected interface ExecutionReceiver {

    void executionFailed(AbstractCalculationNode node, Exception exception);

    void executionComplete(CalculationJobResult result);

  }

  private static class JobEntry {

    private final CalculationJob _job;
    private final JobExecution _execution;
    private ExecutionReceiver _receiver;
    private AtomicInteger _blockCount;

    public JobEntry(final CalculationJob job, final JobExecution execution, final ExecutionReceiver receiver) {
      _job = job;
      _execution = execution;
      _receiver = receiver;
    }

    public CalculationJob getJob() {
      return _job;
    }

    public JobExecution getExecution() {
      return _execution;
    }

    public ExecutionReceiver getReceiver() {
      return _receiver;
    }

    /**
     * This is only called from a single thread - doing the addJob operation - once it has been called once,
     * another thread will manipulate the block count (e.g. if a job is finishing) and possibly spawn the job.
     * Note that we initialize the count to two so that the job does not get spawned prematurely until the
     * addJob thread has processed the required job list and performs a decrement to clear the additional value.
     */
    public void incrementBlockCount() {
      if (_blockCount == null) {
        _blockCount = new AtomicInteger(2);
      } else {
        _blockCount.incrementAndGet();
      }
    }

    /**
     * Decrements the block count.
     * @return true when the count reaches zero
     */
    public boolean releaseBlockCount() {
      return _blockCount.decrementAndGet() == 0;
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

    private final long _jobId;
    private final long _timestamp;
    private Status _status;
    private Set<JobEntry> _blocked;
    private Pair<Thread, CalculationJob> _executor;

    public JobExecution(final long jobId) {
      _jobId = jobId;
      _timestamp = System.nanoTime();
      _status = Status.RUNNING;
    }

    public long getJobId() {
      return _jobId;
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

    public synchronized Pair<Thread, CalculationJob> getAndSetExecutor(final Pair<Thread, CalculationJob> executor) {
      final Pair<Thread, CalculationJob> previous = _executor;
      _executor = executor;
      return previous;
    }

    public synchronized boolean threadBusy(final CalculationJob job) {
      assert _executor == null;
      if (_status == Status.FAILED) {
        return false;
      }
      _executor = Pair.of(Thread.currentThread(), job);
      return true;
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

  /**
   * The set of jobs that are either running, in the runnable queue or blocked by other jobs.
   */
  private final ConcurrentMap<Long, JobExecution> _executions = new ConcurrentHashMap<Long, JobExecution>();

  /**
   * The set of failed jobs. Anything not in this set or {@link #_executions} has completed successfully.
   */
  private final ConcurrentMap<Long, JobExecution> _failures = new ConcurrentSkipListMap<Long, JobExecution>();
  private final AtomicInteger _failureCount = new AtomicInteger();

  private final Queue<JobEntry> _runnableJobs = new ConcurrentLinkedQueue<JobEntry>();
  private final ExecutorService _executorService = Executors.newCachedThreadPool();

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

  protected void onJobExecutionComplete() {
  }

  protected ExecutorService getExecutorService() {
    return _executorService;
  }

  private JobExecution createExecution(final Long jobId) {
    final JobExecution jobexec = new JobExecution(jobId);
    _executions.put(jobId, jobexec);
    return jobexec;
  }

  private JobExecution getExecution(final Long jobId) {
    return _executions.get(jobId);
  }

  private JobExecution getFailure(final Long jobId) {
    return _failures.get(jobId);
  }

  /**
   * Adds a job to the runnable queue, spawning a worker thread if a node is supplied or one is
   * available. 
   */
  private void spawnOrQueueJob(final JobEntry jobexec, AbstractCalculationNode node) {
    if (node == null) {
      node = getNodes().poll();
      if (node == null) {
        synchronized (this) {
          node = getNodes().poll();
          if (node == null) {
            s_logger.debug("Adding job {} to runnable queue", jobexec.getJob().getSpecification().getJobId());
            _runnableJobs.add(jobexec);
            return;
          }
        }
      }
    }
    s_logger.debug("Spawning execution of job {}", jobexec.getJob().getSpecification().getJobId());
    final AbstractCalculationNode parallelNode = node;
    getExecutorService().execute(new Runnable() {

      @Override
      public void run() {
        executeJobs(parallelNode, jobexec);
      }

    });
  }

  private void failExecution(final JobExecution execution) {
    final Set<JobEntry> blocked;
    synchronized (execution) {
      execution.setStatus(JobExecution.Status.FAILED);
      blocked = execution.getBlocked();
      // Add to failure set first, so the job is never missing from both
      _failures.put(execution.getJobId(), execution);
      _executions.remove(execution.getJobId());
      _failureCount.incrementAndGet();
    }
    if (blocked != null) {
      for (JobEntry tail : blocked) {
        tail.invalidate();
        failExecution(tail.getExecution());
      }
    }
  }

  /**
   * Adds jobs to the runnable queue, spawning a worker thread if a node is supplied or one is available. Jobs must be
   * added in dependency order - i.e. a job must be submitted before any that require it. This is to simplify
   * retention of job status as we only need to track jobs that are still running or have failed which saves a lot
   * of housekeeping overhead.
   * 
   * @param job job to run, not null
   * @param receiver execution status receiver, not null
   * @param node optional node to start a worker thread with
   */
  protected void addJob(final CalculationJob job, final ExecutionReceiver receiver, final AbstractCalculationNode node) {
    final JobExecution jobExecution = createExecution(job.getSpecification().getJobId());
    final Collection<Long> requiredJobIds = job.getRequiredJobIds();
    final JobEntry jobEntry = new JobEntry(job, jobExecution, receiver);
    if (requiredJobIds != null) {
      // Shouldn't be passing a node in with a job that might not be runnable
      assert node == null;
      boolean failed = false;
      boolean blocked = false;
      for (Long requiredId : requiredJobIds) {
        JobExecution required = getExecution(requiredId);
        s_logger.debug("Job {} requires {}", jobExecution.getJobId(), requiredId);
        if (required != null) {
          synchronized (required) {
            switch (required.getStatus()) {
              case COMPLETED:
                // No action needed - we can continue
                s_logger.debug("Required job {} completed (from execution cache)", requiredId);
                break;
              case FAILED:
                // We can't run
                failed = true;
                s_logger.debug("Required job {} failed (from execution cache)", requiredId);
                break;
              case RUNNING:
                // We're blocked
                blocked = true;
                // Will increment or initialize to 2
                jobEntry.incrementBlockCount();
                required.blockJob(jobEntry);
                s_logger.debug("Required job {} blocking {}", requiredId, jobExecution.getJobId());
                break;
            }
          }
        } else {
          required = getFailure(requiredId);
          if (required != null) {
            failed = true;
            s_logger.debug("Required job {} failed (from failure cache)", requiredId);
          } else {
            s_logger.debug("Required job {} completion inferred", requiredId);
          }
        }
      }
      if (failed) {
        s_logger.debug("Failing execution of {}", jobExecution.getJobId());
        failExecution(jobExecution);
        return;
      }
      if (blocked) {
        // Decrement the additional count from the initialization
        if (!jobEntry.releaseBlockCount()) {
          s_logger.debug("Blocked execution of {}", jobExecution.getJobId());
          return;
        }
      }
    }
    spawnOrQueueJob(jobEntry, node);
  }

  private void threadFree(final JobExecution exec) {
    // The executor should only go null transiently while the cancel action happens and should then
    // be restored. If, for probably erroneous reasons, the job has been launched twice we can see
    // a continuous null here from the second running thread so have a spin count here to give up
    // after 1s regardless (which may cause further errors, but is better than deadlock).
    int spin = 0;
    do {
      final Pair<Thread, CalculationJob> previous = exec.getAndSetExecutor(null);
      if (previous != null) {
        assert previous.getFirst() == Thread.currentThread();
        return;
      }
      // Executor reference is null while the job is being canceled. An interrupt may be done as part of
      // this so we need to make sure we swallow it rather than leave ourselves in an interrupted state.
      if (Thread.interrupted()) {
        s_logger.debug("Interrupt status cleared");
        return;
      }
      switch (spin) {
        case 0:
          s_logger.debug("Waiting for interrupt");
          break;
        case 1:
          s_logger.info("Waiting for interrupt");
          break;
        case 2:
          s_logger.warn("Waiting for interrupt");
          break;
        default:
          s_logger.error("Waiting for interrupt");
          break;
      }
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        s_logger.debug("Interrupt received");
        return;
      }
    } while (++spin < 1000);
  }

  /**
   * Executes jobs from the runnable queue until it is empty.
   * 
   * @param node Node to run on, not null
   * @param jobexec The first job to run, not null
   */
  private void executeJobs(final AbstractCalculationNode node, JobEntry jobexec) {
    do {
      s_logger.info("Executing job {} on {}", jobexec.getExecution().getJobId(), node.getNodeId());
      onJobStart(jobexec.getJob());
      CalculationJobResult result = null;
      if (jobexec.getExecution().threadBusy(jobexec.getJob())) {
        try {
          result = node.executeJob(jobexec.getJob());
          threadFree(jobexec.getExecution());
        } catch (Exception e) {
          // Any tail jobs will be abandoned
          threadFree(jobexec.getExecution());
          s_logger.warn("Job {} failed", jobexec.getExecution().getJobId());
          failExecution(jobexec.getExecution());
          jobexec.getReceiver().executionFailed(node, e);
        }
      } else {
        s_logger.debug("Job {} cancelled", jobexec.getExecution().getJobId());
      }
      if (result != null) {
        final Set<JobEntry> blocked;
        synchronized (jobexec.getExecution()) {
          jobexec.getExecution().setStatus(JobExecution.Status.COMPLETED);
          blocked = jobexec.getExecution().getBlocked();
          _executions.remove(jobexec.getExecution().getJobId());
        }
        if (blocked != null) {
          s_logger.info("Job {} completed - releasing blocked jobs", jobexec.getExecution().getJobId());
          for (JobEntry tail : blocked) {
            if (tail.getReceiver() != null) {
              if (tail.releaseBlockCount()) {
                spawnOrQueueJob(tail, null);
              }
            }
          }
        } else {
          s_logger.info("Job {} completed - no tail jobs", jobexec.getExecution().getJobId());
        }
      }
      if (result != null) {
        jobexec.getReceiver().executionComplete(result);
      }
      jobexec = _runnableJobs.poll();
      if (jobexec == null) {
        synchronized (this) {
          jobexec = _runnableJobs.poll();
          if (jobexec == null) {
            getNodes().add(node);
            break;
          }
        }
      }
    } while (true);
    s_logger.debug("Finished job execution on {}", node.getNodeId());
    onJobExecutionComplete();
    // Housekeeping
    if (_failureCount.get() > FAILURE_CLEANUP_PERIOD) {
      _failureCount.set(0);
      int count = 0;
      final Iterator<Map.Entry<Long, JobExecution>> entryIterator = _failures.entrySet().iterator();
      while (entryIterator.hasNext()) {
        final Map.Entry<Long, JobExecution> entry = entryIterator.next();
        if (entry.getValue().getAge() > FAILURE_RETENTION) {
          s_logger.debug("Removed job {} from failure set", entry.getKey());
          entryIterator.remove();
          count++;
        } else {
          break;
        }
      }
      s_logger.info("Removed {} dead entries from failure map, {} remaining", count, _failures.size());
    }
    s_logger.debug("Failure map size = {}, execution map size = {}", _failures.size(), _executions.size());
  }

  protected void cancelJob(final CalculationJobSpecification jobSpec) {
    final JobExecution jobExec = getExecution(jobSpec.getJobId());
    if (jobExec == null) {
      s_logger.warn("Request to cancel job {} but already failed or completed", jobSpec.getJobId());
      return;
    }
    s_logger.info("Cancelling job {}", jobSpec.getJobId());
    failExecution(jobExec);
    Pair<Thread, CalculationJob> executor = jobExec.getAndSetExecutor(null);
    if (executor != null) {
      s_logger.debug("Marking job {} cancelled", executor.getSecond().getSpecification().getJobId());
      executor.getSecond().cancel();
      s_logger.info("Interrupting thread {}", executor.getFirst().getName());
      executor.getFirst().interrupt();
      // Need to wait for the execution thread to acknowledge the interrupt, or it may be canceled by us swapping the executor
      // reference back in and the interrupt will affect a subsequent wait causing erroneous behavior
      while (executor.getFirst().isInterrupted()) {
        s_logger.debug("Waiting for thread {} to accept the interrupt", executor.getFirst().getName());
        Thread.yield();
      }
      s_logger.debug("Thread {} interrupted", executor.getFirst().getName());
      executor = jobExec.getAndSetExecutor(executor);
      assert executor == null;
    }
  }

  protected boolean isJobAlive(final CalculationJobSpecification jobSpec) {
    final JobExecution jobExec = getExecution(jobSpec.getJobId());
    if (jobExec == null) {
      // Completed or failed, not alive at any rate!
      return false;
    }
    return true;
  }

}
