/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMessageFactory;
import org.fudgemsg.MutableFudgeFieldContainer;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * Encapsulates view-level configuration to describe the types of values required in the calculation results. This
 * configuration could lead to fewer calculations taking place by allowing the dependency graphs to be trimmed,
 * although values will still be calculated if they are required as inputs for other calculations.
 * <p>
 * This configuration acts as a filter on the outputs that have been requested through
 * {@link ViewCalculationConfiguration}. In a sense, it is a view-view.
 */
@PublicAPI
public class ResultModelDefinition implements Serializable {
  
  private ResultOutputMode _aggregatePositionOutputMode;
  private ResultOutputMode _positionOutputMode;
  private ResultOutputMode _securityOutputMode;
  private ResultOutputMode _primitiveOutputMode;

  /**
   * Constructs an instance using the default output mode for each computation target type.
   */
  public ResultModelDefinition() {
    this(ResultOutputMode.TERMINAL_OUTPUTS);
  }
  
  /**
   * Constructs an instance using the specified output mode for every computation target type.
   *  
   * @param defaultMode  the default result output mode
   */
  public ResultModelDefinition(ResultOutputMode defaultMode) {
    this(defaultMode, defaultMode, defaultMode, defaultMode);
  }
  
  /**
   * Constructs an instance using the specified output modes for each computation target type.
   * 
   * @param primitiveOutputMode  the result output mode for primitive targets
   * @param securityOutputMode  the result output mode for security targets
   * @param positionOutputMode  the result output mode for position targets
   * @param aggregatePositionOutputMode  the result output mode for aggregate position targets
   */
  public ResultModelDefinition(ResultOutputMode primitiveOutputMode, ResultOutputMode securityOutputMode, ResultOutputMode positionOutputMode, ResultOutputMode aggregatePositionOutputMode) {
    _primitiveOutputMode = primitiveOutputMode;
    _securityOutputMode = securityOutputMode;
    _positionOutputMode = positionOutputMode;
    _aggregatePositionOutputMode = aggregatePositionOutputMode;
  }
  
  /**
   * Gets the output mode that applies to aggregate position values. This is independent of individual position outputs.
   * 
   * @return  the output mode that applies to aggregate position values
   */
  public ResultOutputMode getAggregatePositionOutputMode() {
    return _aggregatePositionOutputMode;
  }

  /**
   * Sets the output mode that applies to aggregate position outputs. For example, the referenced portfolio could have
   * a deep structure with many nodes at which aggregate portfolio outputs would be calculated. If these are not
   * required then disabling them could speed up the computation cycle significantly.
   * 
   * @param aggregatePositionOutputMode  the output mode to apply to aggregate position values
   */
  public void setAggregatePositionOutputMode(ResultOutputMode aggregatePositionOutputMode) {
    _aggregatePositionOutputMode = aggregatePositionOutputMode;
  }

  /**
   * Gets the output mode that applies to individual position values. This is independent of aggregate position
   * outputs. 
   * 
   * @return  the output mode that applies to position values
   */
  public ResultOutputMode getPositionOutputMode() {
    return _positionOutputMode;
  }

  /**
   * Sets the output mode that applies to individual position outputs. If only aggregate position calculations are
   * required, with respect to the hierarchy of the reference portfolio, then disabling outputs for individual
   * positions through this method could speed up the computation cycle significantly. This is beneficial for
   * calculations, such as VaR, which can be performed at the aggregate level without requiring the complete result of
   * the same calculation on its children. Aggregate calculations where this is not the case will be unaffected,
   * although disabling the individual position outputs will still hide them from the user even though they will be
   * calculated.
   * 
   * @param positionOutputMode  the output mode to apply to position values
   */
  public void setPositionOutputMode(ResultOutputMode positionOutputMode) {
    _positionOutputMode = positionOutputMode;
  }
  
  /**
   * Gets the output mode that applies to security values.
   * 
   * @return  the output mode that applies to security values
   */
  public ResultOutputMode getSecurityOutputMode() {
    return _securityOutputMode;
  }
  
  /**
   * Sets the output mode to apply to security values. These are values which relate generally to a security and apply
   *  to every position in that security. For example, market data on a security would be a security output.
   * 
   * @param securityOutputMode  the output mode to apply to security values
   */
  public void setSecurityOutputMode(ResultOutputMode securityOutputMode) {
    _securityOutputMode = securityOutputMode;
  }
  
  /**
   * Gets the output mode that applies to primitive outputs.
   * 
   * @return  the output mode that applies to primitive values
   */
  public ResultOutputMode getPrimitiveOutputMode() {
    return _primitiveOutputMode;
  }
  
  /**
   * Sets the output mode that applies to primitive outputs. These are values which may be used in calculations for
   * many securities. For example, the USD discount curve would be a primitive.
   * 
   * @param primitiveOutputMode  the output mode to apply to primitive values
   */
  public void setPrimitiveOutputMode(ResultOutputMode primitiveOutputMode) {
    _primitiveOutputMode = primitiveOutputMode;
  }
  
  /**
   * Gets the output mode that applies to values of the given computation target type.
   * 
   * @param computationTargetType  the target type, not null
   * @return  the output mode that applies to values of the give type
   */
  public ResultOutputMode getOutputMode(ComputationTargetType computationTargetType) {
    ArgumentChecker.notNull(computationTargetType, "computationTargetType");
    switch (computationTargetType) {
      case PRIMITIVE:
        return getPrimitiveOutputMode();
      case SECURITY:
        return getSecurityOutputMode();
      case POSITION:
        return getPositionOutputMode();
      case PORTFOLIO_NODE:
        return getAggregatePositionOutputMode();
      default:
        throw new IllegalArgumentException("Unknown target type " + computationTargetType);
    }
  }
  
  /**
   * Indicates whether an output with the given specification should be included in the results.
   * 
   * @param outputSpecification  the specification of the output value, not null
   * @param dependencyGraph  the dependency graph to which the output value belongs, not null
   * @return  <code>true</code> if the output value should be included in the results, <code>false</code> otherwise.
   */
  public boolean shouldOutputResult(ValueSpecification outputSpecification, DependencyGraph dependencyGraph) {
    ArgumentChecker.notNull(outputSpecification, "outputSpecification");
    ArgumentChecker.notNull(dependencyGraph, "dependencyGraph");
    ComputationTargetType targetType = outputSpecification.getTargetSpecification().getType();
    return getOutputMode(targetType).shouldOutputResult(outputSpecification, dependencyGraph);
  }
  
  /**
   * Indicates whether a dependency node produces any outputs that should be included in the results.
   * 
   * @param dependencyNode  the dependency node, not null
   * @return  <code>true</code> if any outputs are produces that should be included in the results, <code>false</code>
   *          otherwise. 
   */
  public boolean shouldOutputFromNode(DependencyNode dependencyNode) {
    ComputationTargetType targetType = dependencyNode.getComputationTarget().getType();
    return getOutputMode(targetType).shouldOutputFromNode(dependencyNode);
  }
  
  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }
  
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

}
