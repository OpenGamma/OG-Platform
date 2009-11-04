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
import com.opengamma.engine.position.PositionReference;

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
  private final PositionReference _positionReference;
  private final Collection<PositionReference> _positionReferences;
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
      String functionUniqueIdentifier, PositionReference positionReference, 
      Collection<AnalyticValueDefinition<?>> inputs) {
    this(new CalculationJobSpecification(viewName, iterationTimestamp, jobId),
        functionUniqueIdentifier, null, positionReference, null, inputs, ComputationTargetType.POSITION);
  }
  // aggregate position specific functions
  public CalculationJob(String viewName, long iterationTimestamp, long jobId,
      String functionUniqueIdentifier, Collection<PositionReference> positionReferences, 
      Collection<AnalyticValueDefinition<?>> inputs) {
    this(new CalculationJobSpecification(viewName, iterationTimestamp, jobId),
        functionUniqueIdentifier, null, null, positionReferences, inputs, ComputationTargetType.MULTIPLE_POSITIONS);
  }
  
  protected CalculationJob(
      CalculationJobSpecification specification,
      String functionUniqueIdentifier,
      String securityKey,
      PositionReference positionReference,
      Collection<PositionReference> positionReferences,
      Collection<AnalyticValueDefinition<?>> inputs,
      ComputationTargetType computationTargetType) {
    // TODO kirk 2009-09-29 -- Check Inputs.
    _specification = specification;
    _functionUniqueIdentifier = functionUniqueIdentifier;
    _securityKey = securityKey;
    _positionReference = positionReference;
    _positionReferences = positionReferences;
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
  public PositionReference getPositionReference() {
    if (_positionReference == null) {
      s_logger.warn("getPosition() called when job is "+toString());
    }
    return _positionReference;
  }
  
  /**
   * This should only be called if getPositions() returns AGGREGATE_POSITION
   * @return the positions
   */
  public Collection<PositionReference> getPositionReferences() {
    if (_positionReferences == null) {
      s_logger.warn("getPositions() called when job is "+toString());
    }
    return _positionReferences;
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
