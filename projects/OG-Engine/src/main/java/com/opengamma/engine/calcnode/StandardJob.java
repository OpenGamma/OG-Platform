/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.cache.CacheSelectHint;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Triple;

/**
 * Standard job with an execution tail that can be retried in the event of failure.
 * <p>
 * See {@link DispatchableJob} for a description of standard and "watched" jobs.
 */
/* package */final class StandardJob extends DispatchableJob {

  private static final Logger s_logger = LoggerFactory.getLogger(StandardJob.class);

  private final ConcurrentMap<CalculationJobSpecification, JobResultReceiver> _resultReceivers;
  private Set<String> _usedJobInvoker;
  private int _rescheduled;

  private static List<CalculationJob> getAllJobs(CalculationJob job, List<CalculationJob> jobs) {
    if (jobs == null) {
      jobs = new LinkedList<CalculationJob>();
    }
    jobs.add(job);
    if (job.getTail() != null) {
      for (CalculationJob tail : job.getTail()) {
        getAllJobs(tail, jobs);
      }
    }
    return jobs;
  }

  /**
   * Creates a new job for submission to the invokers.
   * 
   * @param dispatcher the parent dispatcher that manages the invokers
   * @param job the root job to send
   * @param resultReceiver the callback for when the job and it's tail completes
   */
  public StandardJob(final JobDispatcher dispatcher, final CalculationJob job, final JobResultReceiver resultReceiver) {
    super(dispatcher, job);
    _resultReceivers = new ConcurrentHashMap<CalculationJobSpecification, JobResultReceiver>();
    final List<CalculationJob> jobs = getAllJobs(job, null);
    for (CalculationJob jobref : jobs) {
      _resultReceivers.put(jobref.getSpecification(), resultReceiver);
    }
  }

  @Override
  protected JobResultReceiver getResultReceiver(final CalculationJobResult result) {
    return _resultReceivers.remove(result.getSpecification());
  }

  @Override
  protected boolean isLastResult() {
    return _resultReceivers.isEmpty();
  }

  /**
   * Change the cache hints on a job. Tail jobs run on the same node as their parent but if we split them into discreet jobs any values previously produced by their parents into the private cache must
   * now go into the shared cache.
   * 
   * @param job the job to process, not null
   * @param the adjusted job, not null
   */
  /* package */static CalculationJob adjustCacheHints(final CalculationJob job,
      final Map<ValueSpecification, Triple<CalculationJob, ? extends Set<ValueSpecification>, ? extends Set<ValueSpecification>>> outputs) {
    // (job, private, public)
    final Triple<CalculationJob, ? extends Set<ValueSpecification>, ? extends Set<ValueSpecification>> jobValues = Triple
        .of(job, new HashSet<ValueSpecification>(), new HashSet<ValueSpecification>());
    final CacheSelectHint hint = job.getCacheSelectHint();
    for (CalculationJobItem item : job.getJobItems()) {
      for (ValueSpecification input : item.getInputs()) {
        final Triple<CalculationJob, ? extends Set<ValueSpecification>, ? extends Set<ValueSpecification>> producer = outputs.get(input);
        if (producer == null) {
          // Input produced by a previous job, so must be in the shared cache
          assert !hint.isPrivateValue(input);
          jobValues.getThird().add(input);
        } else if (producer.getFirst() != job) {
          // Input produced by a previous job into the private cache -- rewrite to the shared
          assert hint.isPrivateValue(input);
          jobValues.getThird().add(input);
          if (producer.getSecond().remove(input)) {
            producer.getThird().add(input);
          }
        }
      }
      for (ValueSpecification output : item.getOutputs()) {
        if (hint.isPrivateValue(output)) {
          // Private output -- may be subject to a rewrite
          jobValues.getSecond().add(output);
          outputs.put(output, jobValues);
        } else {
          // Shared output
          jobValues.getThird().add(output);
        }
      }
    }
    // Rewriting the tail can further adjust the sets in our data triple from the original private/shared distribution
    final Collection<CalculationJob> oldTail = job.getTail();
    final Collection<CalculationJob> newTail;
    if (oldTail != null) {
      newTail = new ArrayList<CalculationJob>(oldTail.size());
      for (CalculationJob tail : oldTail) {
        newTail.add(adjustCacheHints(tail, outputs));
      }
    } else {
      newTail = null;
    }
    // Recalculate the smallest hint for our rewritten data
    final CacheSelectHint newHint;
    if (jobValues.getSecond().size() > jobValues.getThird().size()) {
      newHint = CacheSelectHint.sharedValues(jobValues.getThird());
    } else {
      newHint = CacheSelectHint.privateValues(jobValues.getSecond());
    }
    s_logger.debug("Rewriting {} to {}", hint, newHint);
    // Construct the rewritten job
    final CalculationJob newJob = new CalculationJob(job.getSpecification(), job.getFunctionInitializationIdentifier(), job.getResolverVersionCorrection(), job.getRequiredJobIds(), job.getJobItems(),
        newHint);
    if (newTail != null) {
      for (CalculationJob tail : newTail) {
        newJob.addTail(tail);
      }
    }
    return newJob;
  }

  /**
   * A watched job instance that corresponds to one of the original jobs. The job may have a tail. When it completes, new watched job instances will be submitted for each tail job.
   */
  /* package */static final class WholeWatchedJob extends WatchedJob implements JobResultReceiver {

    private static final class BlockedJob {

      private final CalculationJob _job;
      private int _count;

      public BlockedJob(final CalculationJob job) {
        _job = job;
      }

    }

    private static final class JobState {

      private boolean _completed;
      private List<BlockedJob> _notify;

    }

    private static final class Context {

      private final ConcurrentMap<CalculationJobSpecification, JobResultReceiver> _resultReceivers;
      private final Long2ObjectMap<JobState> _jobs = new Long2ObjectOpenHashMap<JobState>();

      public Context(final ConcurrentMap<CalculationJobSpecification, JobResultReceiver> resultReceivers) {
        _resultReceivers = resultReceivers;
      }

      public JobResultReceiver getResultReceiver(final CalculationJobResult job) {
        return _resultReceivers.remove(job.getSpecification());
      }

      public synchronized void declareJobPending(final long jobId) {
        _jobs.put(jobId, new JobState());
      }

      public synchronized List<BlockedJob> declareJobCompletion(final long jobId) {
        final JobState job = _jobs.remove(jobId);
        if (job._completed) {
          // Duplicate completion
          return null;
        }
        job._completed = true;
        if (job._notify != null) {
          for (BlockedJob notify : job._notify) {
            notify._count--;
          }
          return job._notify;
        } else {
          return Collections.emptyList();
        }
      }

      public synchronized boolean isRunnable(final CalculationJob job) {
        if (job.getRequiredJobIds() == null) {
          return true;
        }
        BlockedJob blocked = null;
        for (long required : job.getRequiredJobIds()) {
          final JobState state = _jobs.get(required);
          if ((state == null) || state._completed) {
            continue;
          }
          if (blocked == null) {
            blocked = new BlockedJob(job);
          }
          blocked._count++;
          if (state._notify == null) {
            state._notify = new LinkedList<BlockedJob>();
          }
          state._notify.add(blocked);
        }
        return blocked == null;
      }

    }

    private final Context _context;
    private final Collection<CalculationJob> _tail;

    private WholeWatchedJob(final DispatchableJob creator, final CalculationJob job, final Context context) {
      super(creator, new CalculationJob(job.getSpecification(), job.getFunctionInitializationIdentifier(), job.getResolverVersionCorrection(), null, job.getJobItems(), job.getCacheSelectHint()));
      _context = context;
      _tail = job.getTail();
      context.declareJobPending(job.getSpecification().getJobId());
    }

    @Override
    protected JobResultReceiver getResultReceiver(final CalculationJobResult result) {
      return this;
    }

    @Override
    public void resultReceived(final CalculationJobResult result) {
      final List<BlockedJob> blocked = _context.declareJobCompletion(result.getSpecification().getJobId());
      if (blocked != null) {
        // Submit any blocked tail jobs
        if (!blocked.isEmpty()) {
          for (BlockedJob job : blocked) {
            if (job._count == 0) {
              s_logger.debug("Releasing blocked job {} from {}", job._job, this);
              getDispatcher().dispatchJobImpl(new WholeWatchedJob(this, job._job, _context));
            }
          }
        }
        // Submit any new tail jobs
        if (_tail != null) {
          for (CalculationJob job : _tail) {
            if (_context.isRunnable(job)) {
              s_logger.debug("Submitting tail job {} from {}", job, this);
              getDispatcher().dispatchJobImpl(new WholeWatchedJob(this, job, _context));
            }
          }
        }
        // Notify the original receiver of the job that completed
        final JobResultReceiver receiver = _context.getResultReceiver(result);
        if (receiver != null) {
          s_logger.debug("Watched job {} complete", this);
          receiver.resultReceived(result);
        } else {
          s_logger.warn("Result already dispatched for watched job {} completed on node {}", this, result.getComputeNodeId());
        }
      } else {
        s_logger.warn("Watched job {} completed on node {} but is not currently pending", this, result.getComputeNodeId());
      }
    }

  }

  /* package */WholeWatchedJob createWholeWatchedJob(final CalculationJob job) {
    return new WholeWatchedJob(this, job, new WholeWatchedJob.Context(_resultReceivers));
  }

  /* package */WatchedJob createWatchedJob() {
    if (getJob().getTail() == null) {
      final List<CalculationJobItem> items = getJob().getJobItems();
      switch (items.size()) {
        case 0:
          // Daft case, but not prevented
          return null;
        case 1:
          // If this is a single item with no tail then we can report it immediately and abort
          getDispatcher().getFunctionBlacklistMaintainer().failedJobItem(getJob().getJobItems().get(0));
          return null;
        default:
          // Job had no tails, so don't need to rewrite the caching
          final JobResultReceiver receiver = _resultReceivers.remove(getJob().getSpecification());
          if (receiver != null) {
            s_logger.debug("Submitting watched job for {}", this);
            return new WatchedJob.Whole(this, getJob(), receiver);
          } else {
            // No result receiver means we've already completed/aborted or are about to do so
            return null;
          }
      }
    } else {
      // Rewrite the private/shared caching information and submit a watched job for the root. Any tail jobs will be submitted after their
      // parent jobs complete
      final CalculationJob job = adjustCacheHints(getJob(), new HashMap<ValueSpecification, Triple<CalculationJob, ? extends Set<ValueSpecification>, ? extends Set<ValueSpecification>>>());
      s_logger.debug("Submitting adjusted watched job for {}", this);
      return createWholeWatchedJob(job);
    }
  }

  @Override
  protected DispatchableJob prepareRetryJob(final JobInvoker jobInvoker) {
    if ((_usedJobInvoker != null) && _usedJobInvoker.contains(jobInvoker.getInvokerId())) {
      return createWatchedJob();
    } else {
      _rescheduled++;
      if (_rescheduled >= getDispatcher().getMaxJobAttempts()) {
        return createWatchedJob();
      } else {
        s_logger.info("Retrying job {}", this);
        if (_usedJobInvoker == null) {
          _usedJobInvoker = new HashSet<String>();
        }
        _usedJobInvoker.add(jobInvoker.getInvokerId());
        return this;
      }
    }
  }

  @Override
  protected void fail(final CalculationJob job, final CalculationJobResultItem failure) {
    final JobResultReceiver resultReceiver = _resultReceivers.remove(job.getSpecification());
    if (resultReceiver != null) {
      notifyFailure(job, failure, resultReceiver);
    } else {
      s_logger.warn("Job {} already completed at propogation of failure", this);
      // This can happen if the root job timed out but things had started to complete
    }
    if (job.getTail() != null) {
      for (CalculationJob tail : job.getTail()) {
        fail(tail, failure);
      }
    }
  }

  @Override
  protected boolean isAlive(final JobInvoker jobInvoker) {
    return jobInvoker.isAlive(_resultReceivers.keySet());
  }

  @Override
  protected void cancel(final JobInvoker jobInvoker) {
    jobInvoker.cancel(_resultReceivers.keySet());
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder('S').append(getJob().getSpecification().getJobId());
    if (_rescheduled > 0) {
      sb.append('(').append(_rescheduled + 1).append(')');
    }
    return sb.toString();
  }

}
