/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.exec.plan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.threeten.bp.Instant;

import com.opengamma.engine.calcnode.CalculationJob;
import com.opengamma.engine.calcnode.CalculationJobSpecification;
import com.opengamma.engine.exec.JobIdSource;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Holds the state corresponding to a current graph execution. The state is capable of delivering executable jobs by tracking when all dependant jobs have completed.
 */
public class ExecutingGraph {

  /**
   * Temporary information used to construct tail execution chains.
   */
  private static final class TailJobInfo {

    private final long[] _requiredJobIds;
    private int _requiredJobIndex;

    public TailJobInfo(final int blockCount) {
      _requiredJobIds = new long[blockCount];
    }

    public long[] getRequiredJobIds() {
      assert _requiredJobIndex == _requiredJobIds.length;
      return _requiredJobIds;
    }

    public boolean addRequiredJobId(final long id) {
      _requiredJobIds[_requiredJobIndex++] = id;
      return _requiredJobIndex == _requiredJobIds.length;
    }

  }

  /**
   * Information about planned jobs that are currently blocked on one or more executing jobs.
   * <p>
   * Tail jobs are not represented here - the blocking information is held in TailJobInfo - as the calculation node they are dispatched to is responsible for executing them in the correct sequence.
   */
  private static final class BlockedJobInfo {

    private final PlannedJob _job;
    private int _waitingFor;

    public BlockedJobInfo(final PlannedJob job) {
      _job = job;
      _waitingFor = job.getInputJobCount();
    }

    public PlannedJob getJob() {
      assert _waitingFor == 0;
      return _job;
    }

    public boolean unblock() {
      return --_waitingFor == 0;
    }

  }

  private final GraphExecutionPlan _plan;
  private final UniqueId _cycleId;
  private final Instant _valuationTime;
  private final VersionCorrection _resolverVersionCorrection;
  private final List<PlannedJob> _executable;
  private final Map<PlannedJob, BlockedJobInfo> _blocked;
  private final Map<CalculationJobSpecification, BlockedJobInfo[]> _executing;

  /**
   * Creates a new execution state.
   * 
   * @param plan the owning execution plan, not null
   * @param cycleId the cycle identifier for job specifications, not null
   * @param valuationTime the valuation time for job specifications, not null
   * @param resolverVersionCorrection the resolution time stamp, not null
   */
  protected ExecutingGraph(final GraphExecutionPlan plan, final UniqueId cycleId, final Instant valuationTime, final VersionCorrection resolverVersionCorrection) {
    ArgumentChecker.notNull(plan, "plan");
    ArgumentChecker.notNull(cycleId, "cycleId");
    ArgumentChecker.notNull(valuationTime, "valuationTime");
    ArgumentChecker.notNull(resolverVersionCorrection, "resolverVersionCorrection");
    _plan = plan;
    _cycleId = cycleId;
    _valuationTime = valuationTime;
    _resolverVersionCorrection = resolverVersionCorrection;
    _executable = new ArrayList<PlannedJob>(plan.getLeafJobs());
    _blocked = new HashMap<PlannedJob, BlockedJobInfo>();
    _executing = new HashMap<CalculationJobSpecification, BlockedJobInfo[]>();
  }

  protected GraphExecutionPlan getPlan() {
    return _plan;
  }

  public String getCalculationConfiguration() {
    return getPlan().getCalculationConfiguration();
  }

  public long getFunctionInitializationId() {
    return getPlan().getFunctionInitializationId();
  }

  public UniqueId getCycleId() {
    return _cycleId;
  }

  public Instant getValuationTime() {
    return _valuationTime;
  }

  public VersionCorrection getResolverVersionCorrection() {
    return _resolverVersionCorrection;
  }

  /**
   * Allocates a job specification.
   * 
   * @return the allocated job specification
   */
  protected CalculationJobSpecification createJobSpecification() {
    // TODO: Should probably inject a job identifier source - if there are multiple view processor host processes then a static one like this
    // won't give us unique identifiers - we need to partition the identifier space amongst the processes that will use a common calc node pool
    return new CalculationJobSpecification(getCycleId(), getCalculationConfiguration(), getValuationTime(), JobIdSource.getId());
  }

  /**
   * Creates an actual calculation job from a planned job that is destined for "tail" execution.
   * <p>
   * The caller must already hold the synchronisation lock.
   * 
   * @param planned the planned job, not null
   * @param jobInfo the map containing information about the tail job set
   * @return the actual job
   */
  protected CalculationJob createTailCalculationJob(final PlannedJob planned, final Map<PlannedJob, TailJobInfo> jobInfo) {
    final long[] requiredJobIds = jobInfo.get(planned).getRequiredJobIds();
    final CalculationJob actual = planned.createCalculationJob(createJobSpecification(), getFunctionInitializationId(), getResolverVersionCorrection(), requiredJobIds);
    addDependentCalculationJobs(actual, planned);
    addTailCalculationJobs(actual, planned, jobInfo);
    return actual;
  }

  /**
   * Updates the state of any dependent fragments of the execution plan so that they may become executable when the job being created here completes.
   * 
   * @param actual the job being created, not null
   * @param planned the execution plan information, not null
   */
  protected void addDependentCalculationJobs(final CalculationJob actual, final PlannedJob planned) {
    final PlannedJob[] dependents = planned.getDependents();
    if (dependents != null) {
      final BlockedJobInfo[] dependentsInfo = new BlockedJobInfo[dependents.length];
      for (int i = 0; i < dependents.length; i++) {
        PlannedJob dependent = dependents[i];
        BlockedJobInfo dependentInfo = _blocked.get(dependent);
        if (dependentInfo == null) {
          dependentInfo = new BlockedJobInfo(dependent);
          _blocked.put(dependent, dependentInfo);
        }
        dependentsInfo[i] = dependentInfo;
      }
      _executing.put(actual.getSpecification(), dependentsInfo);
    } else {
      _executing.put(actual.getSpecification(), null);
    }
  }

  /**
   * Adds the tail executing jobs to the job being created from the execution plan.
   * 
   * @param actual the job being created, not null
   * @param planned the execution plan information, not null
   * @param jobInfo a map to use for temporary storage, not null
   */
  protected void addTailCalculationJobs(final CalculationJob actual, final PlannedJob planned, final Map<PlannedJob, TailJobInfo> jobInfo) {
    if (planned.getTails() != null) {
      final long jobId = actual.getSpecification().getJobId();
      for (PlannedJob tail : planned.getTails()) {
        TailJobInfo tailInfo = jobInfo.get(tail);
        if (tailInfo == null) {
          tailInfo = new TailJobInfo(tail.getInputJobCount());
          jobInfo.put(tail, tailInfo);
        }
        if (tailInfo.addRequiredJobId(jobId)) {
          actual.addTail(createTailCalculationJob(tail, jobInfo));
        }
      }
    }
  }

  /**
   * Creates an actual calculation job from a planned job.
   * <p>
   * The caller must already hold the synchronisation lock.
   * 
   * @param planned the planned job, not null
   * @return the actual calculation job, not null
   */
  protected CalculationJob createCalculationJob(final PlannedJob planned) {
    final CalculationJob actual = planned.createCalculationJob(createJobSpecification(), getFunctionInitializationId(), getResolverVersionCorrection(), null);
    addDependentCalculationJobs(actual, planned);
    if (planned.getTails() != null) {
      addTailCalculationJobs(actual, planned, new HashMap<PlannedJob, TailJobInfo>());
    }
    return actual;
  }

  /**
   * Returns the next job that can be executed, or null if there are none available for execution.
   * <p>
   * A null return may happen if either the graph has completed execution, or there are jobs pending.
   * 
   * @return an executable job, if one is available
   */
  public synchronized CalculationJob nextExecutableJob() {
    final int index = _executable.size() - 1;
    if (index < 0) {
      return null;
    }
    final PlannedJob planned = _executable.remove(index);
    return createCalculationJob(planned);
  }

  /**
   * Tests whether execution of the graph is complete.
   * 
   * @return true if graph execution has finished - there are no more executable jobs and all that were previously returned have been signaled as complete
   */
  public synchronized boolean isFinished() {
    return _executable.isEmpty() && _executing.isEmpty() && _blocked.isEmpty();
  }

  /**
   * Notifies of a job, previously returned by {@link #nextExecutableJob} (either directly or as a job's tail), having completed.
   * <p>
   * Any jobs that were not yet executable because they require one or more results from this job may now become executable.
   * 
   * @param jobSpec the job that has completed, not null
   */
  public synchronized void jobCompleted(CalculationJobSpecification jobSpec) {
    final BlockedJobInfo[] blockedJobs = _executing.remove(jobSpec);
    if (blockedJobs != null) {
      for (BlockedJobInfo blockedJob : blockedJobs) {
        if (blockedJob.unblock()) {
          final PlannedJob job = blockedJob.getJob();
          _executable.add(job);
          _blocked.remove(job);
        }
      }
    }
  }

  @Override
  public String toString() {
    return "ExecutingGraph-" + _plan.getCalculationConfiguration();
  }

}
