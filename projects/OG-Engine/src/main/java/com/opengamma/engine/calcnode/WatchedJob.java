/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.cache.CacheSelectHint;
import com.opengamma.engine.exec.JobIdSource;
import com.opengamma.engine.value.ValueSpecification;

/**
 * A job that is suspected of containing a job item that causes failure and must be reported to a blacklist maintainer.
 * <p>
 * See {@link DispatchableJob} for a description of a "watched" job.
 */
/* package */abstract class WatchedJob extends DispatchableJob {

  private static final Logger s_logger = LoggerFactory.getLogger(WatchedJob.class);

  /**
   * Creates a new watched job for submission to the invokers.
   * 
   * @param creator the origin job that is creating this
   * @param job the job to send
   */
  public WatchedJob(final DispatchableJob creator, final CalculationJob job) {
    super(creator, job);
  }

  @Override
  protected boolean isLastResult() {
    return true;
  }

  /**
   * The basic case of watched job that contains a calculation job in its entirety and reports to a single result receiver.
   */
  protected static class Whole extends WatchedJob {

    private final JobResultReceiver _receiver;

    public Whole(final DispatchableJob creator, final CalculationJob job, final JobResultReceiver receiver) {
      super(creator, job);
      _receiver = receiver;
    }

    @Override
    protected JobResultReceiver getResultReceiver(final CalculationJobResult result) {
      return _receiver;
    }

  }

  /**
   * A job result receiver that will dispatch a second job on the first notification and report the merged results to an underlying receiver on the second notification.
   */
  private static final class HalfJobResultReceiver implements JobResultReceiver {

    private final JobResultReceiver _underlying;
    private DispatchableJob _next;
    private long _duration;
    private Set<String> _nodeIds;
    private List<CalculationJobResultItem> _items;

    public HalfJobResultReceiver(final JobResultReceiver underlying, final WatchedJob creator, final CalculationJob next) {
      _underlying = underlying;
      _next = new Whole(creator, next, this);
    }

    private void resultReceived(final CalculationJobSpecification spec, final long duration, final Set<String> nodeIds, final List<CalculationJobResultItem> items) {
      final DispatchableJob next = _next;
      if (next != null) {
        // First half received -- store results and submit the second half
        _duration = duration;
        _nodeIds = nodeIds;
        _items = new ArrayList<CalculationJobResultItem>(items);
        _next = null;
        next.getDispatcher().dispatchJobImpl(next);
      } else {
        // Second half received -- merge results and notify the underlying
        _duration += duration;
        if (!_nodeIds.containsAll(nodeIds)) {
          _nodeIds = new HashSet<String>(_nodeIds);
          _nodeIds.addAll(nodeIds);
        }
        _items.addAll(items);
        if (_underlying instanceof HalfJobResultReceiver) {
          ((HalfJobResultReceiver) _underlying).resultReceived(spec, _duration, _nodeIds, _items);
        } else {
          final StringBuilder nodeIdString = new StringBuilder();
          final String[] nodeIdSet = _nodeIds.toArray(new String[_nodeIds.size()]);
          Arrays.sort(nodeIdSet);
          for (String nodeId : nodeIdSet) {
            if (nodeIdString.length() != 0) {
              nodeIdString.append(", ");
            }
            nodeIdString.append(nodeId);
          }
          _underlying.resultReceived(new CalculationJobResult(spec, _duration, _items, nodeIdString.toString()));
        }
      }
    }

    @Override
    public void resultReceived(final CalculationJobResult result) {
      resultReceived(result.getSpecification(), result.getDuration(), Collections.singleton(result.getComputeNodeId()), result.getResultItems());
    }

  }

  /**
   * A watched job that is composed of two sequential calculation jobs created by splitting the original. This object manages the first of the jobs. If that fails it will be split. When that job (or
   * it's split form) completes the second job will be submitted using. This behavior is implemented using a new instance of {@link Whole} and the {@HalfJobResultReceiver} to
   * manage the merging of results and submission of the second to allow the original job object to be garbage collected at the first opportunity.
   */
  private static final class Split extends WatchedJob {

    private final CalculationJob _next;
    private final JobResultReceiver _receiver;

    private Split(final DispatchableJob creator, final CalculationJob first, final CalculationJob second, final JobResultReceiver receiver) {
      super(creator, first);
      _next = second;
      _receiver = receiver;
    }

    @Override
    protected JobResultReceiver getResultReceiver(final CalculationJobResult result) {
      return new HalfJobResultReceiver(_receiver, this, _next);
    }

  }

  /**
   * Split a job into two that can be executed sequentially. Any values that are produced in the first but needed in the second and previously considered "private" will be "shared" in the new jobs.
   * The job identifier of the second will be the same as the original job so that any of its tails will recognize its completion as before. The first will receive a new job identifier from
   * JobIdSource.
   * 
   * @param creator the source job, not null
   * @param job the job to split, not null and with at least 2 items
   * @param receiver the receiver to notify when the second job completes, not null
   */
  private static DispatchableJob splitJob(final WatchedJob creator, final CalculationJob job) {
    final List<CalculationJobItem> items = job.getJobItems();
    s_logger.debug("Splitting {} for resubmission ", job);
    final CacheSelectHint hint = job.getCacheSelectHint();
    // Build the head job items
    final int headItemCount = items.size() >> 1;
    final List<CalculationJobItem> headItems = new ArrayList<CalculationJobItem>(headItemCount);
    final Set<ValueSpecification> headShared = new HashSet<ValueSpecification>();
    final Set<ValueSpecification> headPrivate = new HashSet<ValueSpecification>();
    final int tailItemCount = (items.size() + 1) >> 1;
    for (int i = 0; i < headItemCount; i++) {
      final CalculationJobItem item = items.get(i);
      headItems.add(item);
      for (ValueSpecification input : item.getInputs()) {
        if (hint.isPrivateValue(input)) {
          headPrivate.add(input);
        } else {
          headShared.add(input);
        }
      }
      for (ValueSpecification output : item.getOutputs()) {
        if (hint.isPrivateValue(output)) {
          headPrivate.add(output);
        } else {
          headShared.add(output);
        }
      }
    }
    // Build the tail job items and adjust the cache hint for head private values
    final List<CalculationJobItem> tailItems = new ArrayList<CalculationJobItem>(tailItemCount);
    final Set<ValueSpecification> tailShared = new HashSet<ValueSpecification>();
    final Set<ValueSpecification> tailPrivate = new HashSet<ValueSpecification>();
    for (int i = 0; i < tailItemCount; i++) {
      final CalculationJobItem item = items.get(headItemCount + i);
      tailItems.add(item);
      for (ValueSpecification input : item.getInputs()) {
        if (hint.isPrivateValue(input)) {
          // If the head had this as private, make shared
          if (headPrivate.remove(input)) {
            headShared.add(input);
            tailShared.add(input);
          }
        } else {
          tailShared.add(input);
        }
      }
      for (ValueSpecification output : item.getOutputs()) {
        if (hint.isPrivateValue(output)) {
          tailPrivate.add(output);
        } else {
          tailShared.add(output);
        }
      }
    }
    // Construct the head cache hint, job specification (synthetic ID) and job
    final CacheSelectHint headHint;
    if (headPrivate.size() > headShared.size()) {
      headHint = CacheSelectHint.sharedValues(headShared);
    } else {
      headHint = CacheSelectHint.privateValues(headPrivate);
    }
    final CalculationJob head = new CalculationJob(job.getSpecification().withJobId(JobIdSource.getId()), job.getFunctionInitializationIdentifier(), job.getResolverVersionCorrection(), null,
        headItems, headHint);
    // Construct the tail cache hint, job specification (using original ID) and job
    final CacheSelectHint tailHint;
    if (tailPrivate.size() > tailShared.size()) {
      tailHint = CacheSelectHint.sharedValues(tailShared);
    } else {
      tailHint = CacheSelectHint.privateValues(tailPrivate);
    }
    final CalculationJob tail = new CalculationJob(job.getSpecification(), job.getFunctionInitializationIdentifier(), job.getResolverVersionCorrection(), null, tailItems, tailHint);
    // Create the watched job
    return new Split(creator, head, tail, creator.getResultReceiver(null));
  }

  @Override
  protected DispatchableJob prepareRetryJob(final JobInvoker jobInvoker) {
    if (getJob().getJobItems().size() <= 1) {
      // Report the failed job item to the blacklist maintainer
      final CalculationJobItem item = getJob().getJobItems().get(0);
      s_logger.info("Reporting failure of {} from {} to blacklist maintainer", item, this);
      getDispatcher().getFunctionBlacklistMaintainer().failedJobItem(item);
      return null;
    } else {
      return splitJob(this, getJob());
    }
  }

  @Override
  protected void fail(final CalculationJob job, final CalculationJobResultItem failure) {
    notifyFailure(job, failure, getResultReceiver(null));
  }

  @Override
  protected boolean isAlive(final JobInvoker jobInvoker) {
    return jobInvoker.isAlive(getJob().getSpecification());
  }

  @Override
  protected void cancel(final JobInvoker jobInvoker) {
    jobInvoker.cancel(getJob().getSpecification());
  }

  @Override
  public String toString() {
    return "W" + getJob().getSpecification().getJobId();
  }

  public boolean runOn(final JobInvoker jobInvoker) {
    return jobInvoker.invoke(getJob(), this);
  }

}
