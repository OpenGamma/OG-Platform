/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.position.Position;

/**
 * The definition of a particular job that must be performed by
 * a Calculation Node.
 *
 * @author kirk
 */
public class CalculationJob implements Serializable {
  private static final Logger s_logger = LoggerFactory.getLogger(CalculationJob.class); 
  private final CalculationJobSpecification _specification;
  private final ComputationTargetType _computationTargetType;
  private final String _functionUniqueIdentifier;
  private final String _securityKey;
  private final Position _position;
  private final Collection<Position> _positions;
  private final Set<AnalyticValueDefinition<?>> _inputs = new HashSet<AnalyticValueDefinition<?>>();
  
  /**
   * @param viewName
   * @param iterationTimestamp
   * @param jobId
   */
  // primitive functions
  public CalculationJob(String viewName, long iterationTimestamp, long jobId,
      String functionUniqueIdentifier, 
      Collection<AnalyticValueDefinition<?>> inputs) {
    this(new CalculationJobSpecification(viewName, iterationTimestamp, jobId),
        functionUniqueIdentifier, null, null, null, inputs, ComputationTargetType.PRIMITIVE);
  }
  // security specific functions
  public CalculationJob(String viewName, long iterationTimestamp, long jobId,
      String functionUniqueIdentifier, String securityKey, 
      Collection<AnalyticValueDefinition<?>> inputs) {
    this(new CalculationJobSpecification(viewName, iterationTimestamp, jobId),
        functionUniqueIdentifier, securityKey, null, null, inputs, ComputationTargetType.SECURITY);
  }
  // position specific functions
  public CalculationJob(String viewName, long iterationTimestamp, long jobId,
      String functionUniqueIdentifier, Position position, 
      Collection<AnalyticValueDefinition<?>> inputs) {
    this(new CalculationJobSpecification(viewName, iterationTimestamp, jobId),
        functionUniqueIdentifier, null, position, null, inputs, ComputationTargetType.POSITION);
  }
  // aggregate position specific functions
  public CalculationJob(String viewName, long iterationTimestamp, long jobId,
      String functionUniqueIdentifier, Collection<Position> positions, 
      Collection<AnalyticValueDefinition<?>> inputs) {
    this(new CalculationJobSpecification(viewName, iterationTimestamp, jobId),
        functionUniqueIdentifier, null, null, positions, inputs, ComputationTargetType.MULTIPLE_POSITIONS);
  }
  
  protected CalculationJob(
      CalculationJobSpecification specification,
      String functionUniqueIdentifier, String securityKey, Position position, Collection<Position> positions,
      Collection<AnalyticValueDefinition<?>> inputs,
      ComputationTargetType computationTargetType) {
    // TODO kirk 2009-09-29 -- Check Inputs.
    _specification = specification;
    _functionUniqueIdentifier = functionUniqueIdentifier;
    _securityKey = securityKey;
    _position = position;
    _positions = positions;
    _inputs.addAll(inputs);
    _computationTargetType = computationTargetType;
  }

  /**
   * @return the functionUniqueIdentifier
   */
  public String getFunctionUniqueIdentifier() {
    return _functionUniqueIdentifier;
  }

  /**
   * This should only be called if getComputationTargetType() returns SECURITY_KEY
   * @return the securityKey
   */
  public String getSecurityKey() {
    if (_securityKey == null) {
      s_logger.warn("getSecurityKey() called when job is "+toString());
    }
    return _securityKey;
  }
  
  /**
   * This should only be called if getComputationTargetType() returns POSITION
   * @return the position
   */
  public Position getPosition() {
    if (_position == null) {
      s_logger.warn("getPosition() called when job is "+toString());
    }
    return _position;
  }
  
  /**
   * This should only be called if getPositions() returns AGGREGATE_POSITION
   * @return the positions
   */
  public Collection<Position> getPositions() {
    if (_positions == null) {
      s_logger.warn("getPositions() called when job is "+toString());
    }
    return _positions;
  }
  
  /**
   * @return the computationTargetType
   */
  public ComputationTargetType getComputationTargetType() {
    return _computationTargetType;
  }
  
  /**
   * @return the inputs
   */
  public Set<AnalyticValueDefinition<?>> getInputs() {
    return _inputs;
  }

  /**
   * @return the specification
   */
  public CalculationJobSpecification getSpecification() {
    return _specification;
  }
  
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
