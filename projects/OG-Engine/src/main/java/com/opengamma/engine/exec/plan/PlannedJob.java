/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.exec.plan;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.opengamma.engine.cache.CacheSelectHint;
import com.opengamma.engine.calcnode.CalculationJob;
import com.opengamma.engine.calcnode.CalculationJobItem;
import com.opengamma.engine.calcnode.CalculationJobSpecification;
import com.opengamma.id.VersionCorrection;

/**
 * A component from the execution plan which will result in an executable job.
 */
public class PlannedJob implements Serializable {

  private static final long serialVersionUID = 1L;

  private final int _inputJobs;
  private final List<CalculationJobItem> _items;
  private final CacheSelectHint _cacheSelectHint;
  private final PlannedJob[] _tails;
  private final PlannedJob[] _dependents;

  public PlannedJob(final int inputJobs, final List<CalculationJobItem> items, final CacheSelectHint cacheSelectHint, final PlannedJob[] tails, final PlannedJob[] dependents) {
    _inputJobs = inputJobs;
    _items = items;
    _cacheSelectHint = cacheSelectHint;
    _tails = tails;
    _dependents = dependents;
  }

  /**
   * Returns the number of input jobs that this job is dependent on.
   * <p>
   * Leaf jobs will have a value of 0 as they are available for immediate execution.
   * 
   * @return the total number of immediate input jobs
   */
  protected int getInputJobCount() {
    return _inputJobs;
  }

  /**
   * Returns the items that make up the planned job.
   * 
   * @return the job items, not null
   */
  protected List<CalculationJobItem> getItems() {
    return _items;
  }

  /**
   * Returns the cache select hint for locating values produced or consumed by the job items.
   * 
   * @return the cache select hint, not null
   */
  protected CacheSelectHint getCacheSelectHint() {
    return _cacheSelectHint;
  }

  /**
   * Returns the jobs that can run as tails to this job.
   * 
   * @return the tail jobs, or null for none
   */
  protected PlannedJob[] getTails() {
    return _tails;
  }

  /**
   * Returns the jobs that may become runnable after this job completes.
   * 
   * @return the dependent jobs, or null for none
   */
  protected PlannedJob[] getDependents() {
    return _dependents;
  }

  /**
   * Creates a concrete calculation job that can be executed.
   * <p>
   * The job created does not include any of the tails.
   * 
   * @param jobSpec the specification for the new job, not null
   * @param functionInitializationId the function initialization id
   * @param resolverVersionCorrection the resolver version/correction time stamp, not null
   * @param requiredJobIds the identifiers of any precedent jobs (applicable only if this is a tail job), null if not applicable
   * @return the job that's been created, not null
   */
  public CalculationJob createCalculationJob(final CalculationJobSpecification jobSpec, final long functionInitializationId,
      final VersionCorrection resolverVersionCorrection, final long[] requiredJobIds) {
    assert (requiredJobIds == null) || (getInputJobCount() == requiredJobIds.length);
    return new CalculationJob(jobSpec, functionInitializationId, resolverVersionCorrection, requiredJobIds, getItems(), getCacheSelectHint());
  }

  public void print(final PrintStream out, final String indent, final Map<PlannedJob, Integer> jobs) {
    boolean alloc = false;
    Integer id = jobs.get(this);
    if (id == null) {
      id = jobs.size();
      jobs.put(this, id);
      alloc = true;
    }
    out.println(indent + id + ": " + getItems().size() + " item(s)");
    if (alloc) {
      for (CalculationJobItem item : getItems()) {
        out.println(indent + "  " + item.getFunctionUniqueIdentifier() + "(" + item.getComputationTargetSpecification() + "," + Arrays.asList(item.getInputs()) + ") = " +
            Arrays.asList(item.getOutputs()));
      }
      if (getTails() != null) {
        final String tailIndent = indent + " T ";
        for (PlannedJob tail : getTails()) {
          tail.print(out, tailIndent, jobs);
        }
      }
      if (getDependents() != null) {
        final String dependentIndent = indent + "   ";
        for (PlannedJob dependent : getDependents()) {
          dependent.print(out, dependentIndent, jobs);
        }
      }
    }
  }

}
