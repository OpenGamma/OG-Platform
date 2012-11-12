/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.value;

import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.view.ExecutionLog;
import com.opengamma.engine.view.calcnode.InvocationResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * A value computed by the engine for inclusion in a result model.
 * <p>
 * This class is immutable and thread-safe if the value is immutable.
 */
@PublicAPI
public class ComputedValueResult extends ComputedValue {

  private static final long serialVersionUID = 1L;

  private final ExecutionLog _executionLog;
  
  private final String _computeNodeId;
  private final Set<ValueSpecification> _missingInputs;
  
  private final InvocationResult _invocationResult;
  
  /**
   * Creates a computed value result.
   * <p>
   * This combines the value, its specification, and execution information.
   *
   * @param specification  the specification of the value, not null
   * @param value  the actual value
   * @param executionLog  the execution log, not null
   */
  public ComputedValueResult(ValueSpecification specification, Object value, ExecutionLog executionLog) {
    this(specification, value, executionLog, null, null, null);
  }
  
  /**
   * Creates a computed value result from a {@link ComputedValue}.
   * 
   * @param computedValue  the computed value, not null
   * @param executionLog  the execution log, not null
   */
  public ComputedValueResult(ComputedValue computedValue, ExecutionLog executionLog) {
    this(computedValue.getSpecification(), computedValue.getValue(), executionLog);
  }
  
  /**
   * Creates a computed value result.
   * 
   * @param specification  the specification of the value, not null
   * @param value  the actual value
   * @param executionLog  the execution log, not null
   * @param computeNodeId  the identifier of the compute node on which the engine function executed
   * @param missingInputs  any missing inputs 
   * @param invocationResult  the invocation result
   */
  public ComputedValueResult(ValueSpecification specification, Object value, ExecutionLog executionLog,
      String computeNodeId, Set<ValueSpecification> missingInputs, InvocationResult invocationResult) {
    super(specification, value);
    ArgumentChecker.notNull(executionLog, "executionLog");
    _executionLog = executionLog;
    _computeNodeId = computeNodeId;
    _missingInputs = missingInputs;
    _invocationResult = invocationResult;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Gets the log of events which occurred during execution of the engine function that produced this result.
   * 
   * @return the execution log, not null
   */
  public ExecutionLog getExecutionLog() {
    return _executionLog;
  }
  
  /**
   * Gets the identifier of the compute node which executed the engine function that produced this result.
   * 
   * @return the compute node identifier, null if not available
   */
  public String getComputeNodeId() {
    return _computeNodeId;
  }
  
  /**
   * Gets any missing inputs to the engine function that produced this result.
   * 
   * @return the missing inputs, null if not available
   */
  public Set<ValueSpecification> getMissingInputs() {
    return _missingInputs;
  }
  
  /**
   * Gets the invocation result of the engine function that produced this result.
   * 
   * @return the invocation result, null if not available
   */
  public InvocationResult getInvocationResult() {
    return _invocationResult;
  }

  //-------------------------------------------------------------------------
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _executionLog.hashCode();
    result = prime * result + ((_computeNodeId == null) ? 0 : _computeNodeId.hashCode());
    result = prime * result + ((_invocationResult == null) ? 0 : _invocationResult.hashCode());
    result = prime * result + ((_missingInputs == null) ? 0 : _missingInputs.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof ComputedValueResult)) {
      return false;
    }
    ComputedValueResult other = (ComputedValueResult) obj;
    return ObjectUtils.equals(_computeNodeId, other._computeNodeId)
        && ObjectUtils.equals(_invocationResult, other._invocationResult)
        && ObjectUtils.equals(_missingInputs, other._missingInputs)
        && ObjectUtils.equals(_executionLog, other._executionLog);
  }

  @Override
  protected void appendFieldsToString(StringBuffer sb, ToStringStyle style) {
    super.appendFieldsToString(sb, style);
    style.append(sb, "log", getExecutionLog(), null);
  }
  
}
