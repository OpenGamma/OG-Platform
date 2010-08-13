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
import com.opengamma.util.ArgumentChecker;

/**
 * Encapsulates view-level configuration to describe the types of values required in the calculation results. This
 * configuration could lead to fewer calculations taking place by allowing the dependency graphs to be trimmed,
 * although values will still be calculated if they are required as inputs for other calculations.
 * <p>
 * This configuration acts as a filter on the outputs that have been requested through
 * {@link ViewCalculationConfiguration}. In a sense, it is a view-view.
 */
public class ResultModelDefinition implements Serializable {
  
  private static final String AGGREGATE_POSITION_OUTPUTS_ENABLED_FIELD = "aggregatePositionOutputsEnabled";
  private static final String POSITION_OUTPUTS_ENABLED_FIELD = "positionOutputsEnabled";
  private static final String SECURITY_OUTPUTS_ENABLED_FIELD = "securityOutputsEnabled";
  private static final String PRIMITIVE_OUTPUTS_ENABLED_FIELD = "primitiveOutputsEnabled";
  
  private boolean _aggregatePositionOutputsEnabled = true;
  private boolean _positionOutputsEnabled = true;
  private boolean _securityOutputsEnabled = true;
  private boolean _primitiveOutputsEnabled = true;

  /**
   * Gets whether aggregate position outputs are enabled. This is independent of individual position outputs.
   * 
   * @return  whether aggregate position outputs are enabled
   */
  public boolean isAggregatePositionOutputsEnabled() {
    return _aggregatePositionOutputsEnabled;
  }

  /**
   * Sets whether aggregate position outputs are enabled. For example, the referenced portfolio could have a deep
   * structure with many nodes at which aggregate portfolio outputs would be calculated. If these are not required then
   * disabling them could speed up the computation cycle significantly.
   * 
   * @param aggregatePositionOutputsEnabled  whether aggregate position outputs are to be enabled.
   */
  public void setAggregatePositionOutputsEnabled(boolean aggregatePositionOutputsEnabled) {
    _aggregatePositionOutputsEnabled = aggregatePositionOutputsEnabled;
  }

  /**
   * Gets whether individual position outputs are enabled. This is independent of aggregate position outputs. 
   * 
   * @return  whether individual position outputs are enabled
   */
  public boolean isPositionOutputsEnabled() {
    return _positionOutputsEnabled;
  }

  /**
   * Sets whether individual position outputs are enabled. If only aggregate position calculations are required, with
   * respect to the hierarchy of the reference portfolio, then disabling outputs for individual positions through this
   * method could speed up the computation cycle significantly. This is beneficial for calculations, such as VaR, which
   * can be performed at the aggregate level without requiring the complete result of the same calculation on its
   * children. Aggregate calculations where this is not the case will be unaffected, although disabling the individual
   * position outputs will still hide them from the user even though they were calculated.
   * 
   * @param positionOutputsEnabled  whether individual position outputs are to be enabled
   */
  public void setPositionOutputsEnabled(boolean positionOutputsEnabled) {
    _positionOutputsEnabled = positionOutputsEnabled;
  }
  
  /**
   * Gets whether security outputs are enabled.
   * 
   * @return  whether security outputs are enabled
   */
  public boolean isSecurityOutputsEnabled() {
    return _securityOutputsEnabled;
  }
  
  /**
   * Sets whether security outputs are enabled. These are values which relate generally to a security and apply to
   * every position in that security. For example, market data on a security would be a security output.
   * 
   * @param securityOutputsEnabled  whether security outputs are to be enabled
   */
  public void setSecurityOutputsEnabled(boolean securityOutputsEnabled) {
    _securityOutputsEnabled = securityOutputsEnabled;
  }
  
  /**
   * Gets whether primitive outputs are enabled.
   * 
   * @return  whether primitive outputs are enabled
   */
  public boolean isPrimitiveOutputsEnabled() {
    return _primitiveOutputsEnabled;
  }
  
  /**
   * Sets whether primitive outputs are enabled. These are values which may be used in calculations for many
   * securities. For example, the USD discount curve would be a primitive.
   * 
   * @param primitiveOutputsEnabled  whether primitive outputs are to be enabled
   */
  public void setPrimitiveOutputsEnabled(boolean primitiveOutputsEnabled) {
    _primitiveOutputsEnabled = primitiveOutputsEnabled;
  }
  
  /**
   * Gets whether outputs for a particular target type are enabled. This should be used to determine whether or not a
   * value for the target appears in the results.
   * 
   * @param computationTargetType  the target type, not null
   * @return  <code>true</code> if outputs for this target type are enabled, <code>false</code> otherwise.
   */
  public boolean outputsEnabled(ComputationTargetType computationTargetType) {
    ArgumentChecker.notNull(computationTargetType, "computationTargetType");
    switch (computationTargetType) {
      case PRIMITIVE:
        return isPrimitiveOutputsEnabled();
      case SECURITY:
        return isSecurityOutputsEnabled();
      case POSITION:
        return isPositionOutputsEnabled();
      case PORTFOLIO_NODE:
        return isAggregatePositionOutputsEnabled();
      default:
        throw new RuntimeException("Unexpected target type " + computationTargetType);
    }
  }
  
  /**
   * Serializes this object to a Fudge message.
   * 
   * @param factory  the Fudge context, not null
   * @return the Fudge message, not null
   */
  public FudgeFieldContainer toFudgeMsg(FudgeMessageFactory factory) {
    ArgumentChecker.notNull(factory, "Fudge Context");
    MutableFudgeFieldContainer msg = factory.newMessage();
    msg.add(AGGREGATE_POSITION_OUTPUTS_ENABLED_FIELD, _aggregatePositionOutputsEnabled);
    msg.add(POSITION_OUTPUTS_ENABLED_FIELD, _positionOutputsEnabled);
    msg.add(SECURITY_OUTPUTS_ENABLED_FIELD, _securityOutputsEnabled);
    msg.add(PRIMITIVE_OUTPUTS_ENABLED_FIELD, _primitiveOutputsEnabled);
    return msg;
  }
  
  /**
   * Deserializes a Fudge message into a ResultModelDefinition.
   * @param msg  the Fudge message, not null
   * @return the ResultModelDefinition, not null
   */
  public static ResultModelDefinition fromFudgeMsg(FudgeFieldContainer msg) {
    ResultModelDefinition result = new ResultModelDefinition();
    result.setAggregatePositionOutputsEnabled(msg.getBoolean(AGGREGATE_POSITION_OUTPUTS_ENABLED_FIELD));
    result.setPositionOutputsEnabled(msg.getBoolean(POSITION_OUTPUTS_ENABLED_FIELD));
    result.setSecurityOutputsEnabled(msg.getBoolean(SECURITY_OUTPUTS_ENABLED_FIELD));
    result.setPrimitiveOutputsEnabled(msg.getBoolean(PRIMITIVE_OUTPUTS_ENABLED_FIELD));
    return result;
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
