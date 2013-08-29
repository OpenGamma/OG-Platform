/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.exec.plan;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.threeten.bp.Instant;

import com.opengamma.engine.exec.stats.GraphExecutorStatisticsGatherer;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Describes a way of executing a dependency graph - that is, a sequence of jobs that will produce the desired result.
 * <p>
 * An execution plan may be executed on an arbitrary node set, but is typically generated in such a way that it will perform best on a given configuration (for example it might have been created with
 * the intention of being used on {@code X} calculation nodes, each with {@code Y} available execution threads).
 */
public class GraphExecutionPlan implements Serializable {

  private static final long serialVersionUID = 1L;

  private final String _calculationConfiguration;
  private final long _functionInitializationId;
  private final Collection<PlannedJob> _leafJobs;
  private final int _totalJobs;
  private final double _meanJobSize;
  private final double _meanJobCycleCost;
  private final double _meanJobIOCost;

  /**
   * Creates a new execution plan.
   * 
   * @param calculationConfiguration the configuration name, not null - this will be used for constructing job specifications
   * @param functionInitializationId [PLAT-2241] this will go soon
   * @param leafJobs the jobs that will execute first, not null and not containing null - these will refer to other jobs that form part of the full plan
   * @param totalJobs the total number of jobs in the plan
   * @param meanJobSize the mean job size
   * @param meanJobCycleCost the mean of each job's CPU cost
   * @param meanJobIOCost the mean of each job's I/O cost
   */
  public GraphExecutionPlan(final String calculationConfiguration, final long functionInitializationId, final Collection<PlannedJob> leafJobs, final int totalJobs, final double meanJobSize,
      final double meanJobCycleCost, final double meanJobIOCost) {
    ArgumentChecker.notNull(calculationConfiguration, "calculationConfiguration");
    ArgumentChecker.notNull(leafJobs, "leafJobs");
    assert !leafJobs.contains(null);
    _calculationConfiguration = calculationConfiguration;
    _functionInitializationId = functionInitializationId;
    _leafJobs = new ArrayList<PlannedJob>(leafJobs);
    _totalJobs = totalJobs;
    _meanJobSize = meanJobSize;
    _meanJobCycleCost = meanJobCycleCost;
    _meanJobIOCost = meanJobIOCost;
  }

  protected GraphExecutionPlan(final String calculationConfiguration, final GraphExecutionPlan copyFrom) {
    ArgumentChecker.notNull(calculationConfiguration, "calculationConfiguration");
    _calculationConfiguration = calculationConfiguration;
    _functionInitializationId = copyFrom._functionInitializationId;
    _leafJobs = copyFrom._leafJobs;
    _totalJobs = copyFrom._totalJobs;
    _meanJobSize = copyFrom._meanJobSize;
    _meanJobCycleCost = copyFrom._meanJobCycleCost;
    _meanJobIOCost = copyFrom._meanJobIOCost;
  }

  /**
   * Returns a copy with an altered calculation configuration name.
   * 
   * @param calculationConfiguration the new calculation configuration name
   * @return this instance if the name matches, otherwise a new instance
   */
  public GraphExecutionPlan withCalculationConfiguration(final String calculationConfiguration) {
    if (getCalculationConfiguration().equals(calculationConfiguration)) {
      return this;
    }
    return new GraphExecutionPlan(calculationConfiguration, this);
  }

  /**
   * Returns the calculation configuration name.
   * 
   * @return the configuration name, not null
   */
  public String getCalculationConfiguration() {
    return _calculationConfiguration;
  }

  protected long getFunctionInitializationId() {
    return _functionInitializationId;
  }

  protected Collection<PlannedJob> getLeafJobs() {
    return _leafJobs;
  }

  protected int getTotalJobs() {
    return _totalJobs;
  }

  protected double getMeanJobSize() {
    return _meanJobSize;
  }

  protected double getMeanJobCycleCost() {
    return _meanJobCycleCost;
  }

  protected double getMeanJobIOCost() {
    return _meanJobIOCost;
  }

  /**
   * Creates an execution state from the plan. The state may be used to deliver executable jobs that can be used to evaluate the graph that this plan represents.
   * 
   * @param cycleId the view cycle identifier, not null - this will be used to construct job specifications
   * @param valuationTime the cycle valuation time, not null - this will be used to construct job specifications
   * @param resolverVersionCorrection the resolution timestamp, not null - this will be embedded in all jobs
   * @return the executing graph state, not null, with all "leaf" jobs immediately available for execution
   */
  public ExecutingGraph createExecution(final UniqueId cycleId, final Instant valuationTime, final VersionCorrection resolverVersionCorrection) {
    return new ExecutingGraph(this, cycleId, valuationTime, resolverVersionCorrection);
  }

  public void reportStatistics(final GraphExecutorStatisticsGatherer statistics) {
    statistics.graphProcessed(getCalculationConfiguration(), getTotalJobs(), getMeanJobSize(), getMeanJobCycleCost(), getMeanJobIOCost());
  }

  public void print(final PrintStream out) {
    out.println(getCalculationConfiguration() + ", " + getTotalJobs() + " job(s)");
    final Map<PlannedJob, Integer> jobs = new HashMap<PlannedJob, Integer>();
    for (PlannedJob job : getLeafJobs()) {
      job.print(out, "  ", jobs);
    }
  }

  public void print() {
    print(System.out);
  }

}
