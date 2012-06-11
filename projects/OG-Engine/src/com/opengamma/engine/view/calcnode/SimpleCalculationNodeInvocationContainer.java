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
import com.opengamma.util.async.AsynchronousHandle;
import com.opengamma.util.async.AsynchronousHandleExecution;
import com.opengamma.util.async.AsynchronousResult;
import com.opengamma.util.async.ResultListener;
import com.opengamma.util.tuple.Pair;

/**
 * Base class for objects that manage a set of AbstractCalculationNodes with the intention of invoking job executions on them.
 */
public abstract class SimpleCalculationNodeInvocationContainer {

  /**
   * After how many failed jobs should a cleanup be attempted.
   */
  private static final int FAILURE_CLEANUP_PERIOD = 100; // clean up after every 100 failed jobs

  /**
   * Retention period for failed jobs needs to be long enough for any tail jobs to have arrived.
   */
  private static final long FAILURE_RETENTION = 5L * 60L * 100000000L; // 5m

  private static final Logger s_logger = LoggerFactory.getLogger(SimpleCalculationNodeInvocationContainer.class);

  /**
   * 
   */
  protected interface ExecutionReceiver {

    void executionFailed(SimpleCalculationNode node, Exception exception);

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
     * This is only called from a single thread - doing the addJob operation - once it has been called once, another thread will manipulate the block count (e.g. if a job is finishing) and possibly
     * spawn the job. Note that we initialize the count to two so that the job does not get spawned prematurely until the addJob thread has processed the required job list and performs a decrement to
     * clear the additional value.
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
     * 
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

  private static class PartialJobEntry {

    private final JobEntry _entry;
    private final SimpleCalculationNodeState _nodeState;
    private final AsynchronousResult<AsynchronousHandle<CalculationJobResult>> _handle;

    public PartialJobEntry(final JobEntry entry, final SimpleCalculationNodeState nodeState, final AsynchronousResult<AsynchronousHandle<CalculationJobResult>> handle) {
      _entry = entry;
      _nodeState = nodeState;
      _handle = handle;
    }

    public JobEntry getEntry() {
      return _entry;
    }

    public SimpleCalculationNodeState getNodeState() {
      return _nodeState;
    }

    public AsynchronousResult<AsynchronousHandle<CalculationJobResult>> getHandle() {
      return _handle;
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

  private final Queue<SimpleCalculationNode> _nodes = new ConcurrentLinkedQueue<SimpleCalculationNode>();

  /**
   * The set of jobs that are either running, in the runnable queue or blocked by other jobs.
   */
  private final ConcurrentMap<Long, JobExecution> _executions = new ConcurrentHashMap<Long, JobExecution>();

  /**
   * The set of failed jobs. Anything not in this set or {@link #_executions} has completed successfully.
   */
  private final ConcurrentMap<Long, JobExecution> _failures = new ConcurrentSkipListMap<Long, JobExecution>();
  private final AtomicInteger _failureCount = new AtomicInteger();

  /**
   * The queue of runnable jobs. Each are jobs that have been received and are ready to run but have not been started, probably because they are waiting for a node to become available. When nodes are
   * available they will take partial jobs from the {@link _partialJobs} queue in preference to this queue.
   */
  private final Queue<JobEntry> _runnableJobs = new ConcurrentLinkedQueue<JobEntry>();
  /**
   * The queue of partially executed jobs. Each represents a piece of work that was started, became blocked on something external, but is now ready to run again. Note that the resume may not take
   * place on the original node and/or original thread. This is to avoid a potential bottleneck. Nothing should be retaining thread/node specific state (other than the saved node state) so this should
   * not be a problem.
   */
  private final Queue<PartialJobEntry> _partialJobs = new ConcurrentLinkedQueue<PartialJobEntry>();

  private final ExecutorService _executorService = Executors.newCachedThreadPool();

  protected Queue<SimpleCalculationNode> getNodes() {
    return _nodes;
  }

  public void addNode(final SimpleCalculationNode node) {
    ArgumentChecker.notNull(node, "node");
    getNodes().add(node);
    onNodeChange();
  }

  public void addNodes(final Collection<SimpleCalculationNode> nodes) {
    ArgumentChecker.notNull(nodes, "nodes");
    getNodes().addAll(nodes);
    onNodeChange();
  }

  public void setNode(final SimpleCalculationNode node) {
    ArgumentChecker.notNull(node, "node");
    getNodes().clear();
    getNodes().add(node);
    onNodeChange();
  }

  public void setNodes(final Collection<SimpleCalculationNode> nodes) {
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
   * Adds a job to the runnable queue, spawning a worker thread if a node is supplied or one is available.
   */
  private void spawnOrQueueJob(final JobEntry jobexec, SimpleCalculationNode node) {
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
    final SimpleCalculationNode parallelNode = node;
    getExecutorService().execute(new Runnable() {
      @Override
      public void run() {
        executeJobs(parallelNode, jobexec, null);
      }
    });
  }

  private void spawnOrQueueJob(final PartialJobEntry jobexec) {
    SimpleCalculationNode node = getNodes().poll();
    if (node == null) {
      synchronized (this) {
        node = getNodes().poll();
        if (node == null) {
          s_logger.debug("Adding job {} to partially run queue", jobexec.getEntry().getJob().getSpecification().getJobId());
          _partialJobs.add(jobexec);
          return;
        }
      }
    }
    s_logger.debug("Spawning re-execution of job {}", jobexec.getEntry().getJob().getSpecification().getJobId());
    final SimpleCalculationNode parallelNode = node;
    getExecutorService().execute(new Runnable() {
      @Override
      public void run() {
        executeJobs(parallelNode, null, jobexec);
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
   * Adds jobs to the runnable queue, spawning a worker thread if a node is supplied or one is available. Jobs must be added in dependency order - i.e. a job must be submitted before any that require
   * it. This is to simplify retention of job status as we only need to track jobs that are still running or have failed which saves a lot of housekeeping overhead.
   * 
   * @param job job to run, not null
   * @param receiver execution status receiver, not null
   * @param node optional node to start a worker thread with
   */
  protected void addJob(final CalculationJob job, final ExecutionReceiver receiver, final SimpleCalculationNode node) {
    final JobExecution jobExecution = createExecution(job.getSpecification().getJobId());
    final long[] requiredJobIds = job.getRequiredJobIds();
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
   * Executes jobs from the runnable and partially-run queues until both are empty.
   * 
   * @param node Node to run on, not null
   * @param job The first job to run, not null if resumeJob is null
   * @param resumeJob the first job to run, not null if newJob is null
   */
  private void executeJobs(final SimpleCalculationNode node, JobEntry job, PartialJobEntry resumeJob) {
    do {
      if (resumeJob == null) {
        s_logger.info("Executing job {} on {}", job.getExecution().getJobId(), node.getNodeId());
        onJobStart(job.getJob());
      } else {
        job = resumeJob.getEntry();
        s_logger.info("Resuming job {} on {}", job.getExecution().getJobId(), node.getNodeId());
      }
      CalculationJobResult result = null;
      if (job.getExecution().threadBusy(job.getJob())) {
        try {
          if (resumeJob == null) {
            result = node.executeJob(job.getJob());
          } else {
            node.restoreState(resumeJob.getNodeState());
            result = resumeJob.getHandle().getResult().get();
          }
          threadFree(job.getExecution());
        } catch (AsynchronousHandleExecution e) {
          // The job is running asynchronously (e.g. blocked on an external process). Registering a listener with the exception
          // will give us a callable handle back to the job when it is ready to return to this thread. In the meantime we use
          // this thread to perform other work.
          threadFree(job.getExecution());
          s_logger.debug("Job {} running asynchronously", job.getExecution().getJobId());
          final SimpleCalculationNodeState state = node.saveState();
          final JobEntry originalJob = job;
          e.setResultHandleListener(new ResultListener<AsynchronousHandle<CalculationJobResult>>() {
            @Override
            public void operationComplete(final AsynchronousResult<AsynchronousHandle<CalculationJobResult>> result) {
              spawnOrQueueJob(new PartialJobEntry(originalJob, state, result));
            }
          });
        } catch (Exception e) {
          // Any tail jobs will be abandoned
          threadFree(job.getExecution());
          s_logger.warn("Job {} failed", job.getExecution().getJobId());
          failExecution(job.getExecution());
          job.getReceiver().executionFailed(node, e);
        }
      } else {
        s_logger.debug("Job {} cancelled", job.getExecution().getJobId());
      }
      if (result != null) {
        final Set<JobEntry> blocked;
        synchronized (job.getExecution()) {
          job.getExecution().setStatus(JobExecution.Status.COMPLETED);
          blocked = job.getExecution().getBlocked();
          _executions.remove(job.getExecution().getJobId());
        }
        if (blocked != null) {
          s_logger.info("Job {} completed - releasing blocked jobs", job.getExecution().getJobId());
          for (JobEntry tail : blocked) {
            if (tail.getReceiver() != null) {
              if (tail.releaseBlockCount()) {
                spawnOrQueueJob(tail, null);
              }
            }
          }
        } else {
          s_logger.info("Job {} completed - no tail jobs", job.getExecution().getJobId());
        }
      }
      // PLAT-2293 -- we might have an early completion indicator so don't want to let the receiver know just yet
      if (result != null) {
        job.getReceiver().executionComplete(result);
      }
      resumeJob = _partialJobs.poll();
      if (resumeJob == null) {
        job = _runnableJobs.poll();
        if (job == null) {
          synchronized (this) {
            resumeJob = _partialJobs.poll();
            if (resumeJob == null) {
              job = _runnableJobs.poll();
              if (job == null) {
                getNodes().add(node);
                break;
              }
            }
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
