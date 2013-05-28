/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.exec.plan;

import java.util.ArrayList;
import java.util.List;

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

  private final GraphExecutionPlan _plan;
  private final UniqueId _cycleId;
  private final Instant _valuationTime;
  private final VersionCorrection _resolverVersionCorrection;
  private final List<PlannedJob> _executable;

  /**
   * Creates a new execution state.
   * 
   * @param plan the owning execution plan, not null
   * @param cycleId the cycle identifier for job specifications, not null
   * @param valuationTime the valuation time for job specifications, not null
   * @param resolverVersionCorrection the resolution time stamp, not null
   */
  /* package */ExecutingGraph(final GraphExecutionPlan plan, final UniqueId cycleId, final Instant valuationTime, final VersionCorrection resolverVersionCorrection) {
    ArgumentChecker.notNull(plan, "plan");
    ArgumentChecker.notNull(cycleId, "cycleId");
    ArgumentChecker.notNull(valuationTime, "valuationTime");
    ArgumentChecker.notNull(resolverVersionCorrection, "resolverVersionCorrection");
    _plan = plan;
    _cycleId = cycleId;
    _valuationTime = valuationTime;
    _resolverVersionCorrection = resolverVersionCorrection;
    _executable = new ArrayList<PlannedJob>(plan.getLeafJobs());
    // TODO: Create the "blocked job" set from the possible executions
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
   * Creates an actual calculation job from a planned job.
   * <p>
   * The caller must already hold the synchronisation lock.
   * 
   * @param planned the planned job, not null
   * @return the actual calculation job, not null
   */
  protected CalculationJob createCalculationJob(final PlannedJob planned) {
    // TODO: If this is a tail job then we need to lookup the required job identifiers
    final long[] requiredJobIds = new long[0]; // TODO - do this properly
    final CalculationJob actual = planned.createCalculationJob(createJobSpecification(), getFunctionInitializationId(), getResolverVersionCorrection(), requiredJobIds);
    if (planned.getDependents() != null) {
      for (PlannedJob dependent : planned.getDependents()) {
        // TODO: If this job is not already in the blocked set, create an entry for it
        // TODO: Notify the dependent job of our identifier
      }
    }
    if (planned.getTails() != null) {
      for (PlannedJob tail : planned.getTails()) {
        // TODO: This is wrong; we can only create the tail job when all references to it have been identified
        actual.addTail(createCalculationJob(tail));
      }
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
    if (!_executable.isEmpty()) {
      // At least one executable job that has not been dispatched yet
      return false;
    }
    // TODO: is there anything currently blocked
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * Notifies of a job, previously returned by {@link #nextExecutableJob} (either directly or as a job's tail), having completed.
   * <p>
   * Any jobs that were not yet executable because they require one or more results from this job may now become executable.
   * 
   * @param jobSpec the job that has completed, not null
   */
  public synchronized void jobCompleted(CalculationJobSpecification jobSpec) {
    // TODO: update the blocked job states
    throw new UnsupportedOperationException("TODO");
  }

}
