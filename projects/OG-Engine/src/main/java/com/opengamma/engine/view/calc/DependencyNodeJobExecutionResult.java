/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.engine.view.AggregatedExecutionLog;
import com.opengamma.engine.view.calcnode.CalculationJobResultItem;
import com.opengamma.util.ArgumentChecker;

/**
 * Encapsulates metadata following the execution of a job on a dependency node.
 * <p>
 * One job corresponds to one engine function invocation; this may produce multiple outputs.
 */
public class DependencyNodeJobExecutionResult {

  private final String _computeNodeId;
  private final CalculationJobResultItem _jobResultItem;
  private final AggregatedExecutionLog _aggregatedExecutionLog;
  
  /**
   * Constructs an instance.
   * 
   * @param computeNodeId  the identifier of the compute node on which the dependency node was executed, not null
   * @param jobResultItem  the calculation job result item, not null
   * @param aggregatedExecutionLog  the aggregated execution log for the job and its inputs, not null
   */
  public DependencyNodeJobExecutionResult(String computeNodeId, CalculationJobResultItem jobResultItem, AggregatedExecutionLog aggregatedExecutionLog) {
    ArgumentChecker.notNull(computeNodeId, "computeNodeId");
    ArgumentChecker.notNull(jobResultItem, "jobResultItem");
    ArgumentChecker.notNull(aggregatedExecutionLog, "aggregatedExecutionLog");
    _computeNodeId = computeNodeId.intern();
    _jobResultItem = jobResultItem;
    _aggregatedExecutionLog = aggregatedExecutionLog;
  }

  /**
   * Gets the identifier of the compute node on which the dependency node was executed.
   * 
   * @return the compute node identifier, not null
   */
  public String getComputeNodeId() {
    return _computeNodeId;
  }

  /**
   * Gets the calculation job result item.
   * 
   * @return the calculation job result item, not null
   */
  public CalculationJobResultItem getJobResultItem() {
    return _jobResultItem;
  }
  
  /**
   * Gets the aggregated execution log for the job and its inputs.
   * 
   * @return the aggregated execution log for the job and its inputs, not null
   */
  public AggregatedExecutionLog getAggregatedExecutionLog() {
    return _aggregatedExecutionLog;
  }

  //-------------------------------------------------------------------------
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_computeNodeId == null) ? 0 : _computeNodeId.hashCode());
    result = prime * result + ((_jobResultItem == null) ? 0 : _jobResultItem.hashCode());
    result = prime * result + ((_aggregatedExecutionLog == null) ? 0 : _jobResultItem.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof DependencyNodeJobExecutionResult)) {
      return false;
    }
    DependencyNodeJobExecutionResult other = (DependencyNodeJobExecutionResult) obj;
    return ObjectUtils.equals(_computeNodeId, other._computeNodeId)
        && ObjectUtils.equals(_jobResultItem, other._jobResultItem)
        && ObjectUtils.equals(_aggregatedExecutionLog, other._aggregatedExecutionLog);
  }
  
}
