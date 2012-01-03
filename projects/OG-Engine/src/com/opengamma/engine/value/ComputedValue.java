/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.value;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.view.calcnode.InvocationResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Set;

/**
 * A value computed by the engine.
 * <p>
 * A computed value consists of the value itself and the specification that defines it.
 * <p>
 * This class is immutable and thread-safe if the object is immutable.
 */
@PublicAPI
public class ComputedValue implements Serializable {
  //NOTE kirk 2009-12-30 -- This is VERY intentionally NOT generified. Having actually
  //worked with the old version of ComputedValue with generics in the engine, the number
  //of warning suppressions necessary just to get it working wasn't matched by any
  //type of benefit in the additional type information at compile time.

  //REVIEW kirk 2009-12-30 -- I've intentionally not made this an interface as we
  //never actually made use of that functionality. We'll split it if and when we have
  //a compelling reason to do so.

  private static final long serialVersionUID = 1L;
  
  /**
   * The specification of the value.
   */
  private final ValueSpecification _specification;
  /**
   * The value itself.
   */
  private final Object _value;

  private InvocationResult _result = null;
  private String _exceptionClass = null;
  private String _exceptionMsg = null;
  private String _stackTrace = null;
  private Set<ValueSpecification> _missingInputs = null;
  private Set<ValueRequirement> _originalRequirements = null;
  private String _nodeId;

  /**
   * Creates a computed value.
   * <p>
   * This combines the value and its specification.
   *
   * @param specification  the specification of the value, not null
   * @param value  the actual value
   */
  public ComputedValue(ValueSpecification specification, Object value) {
    ArgumentChecker.notNull(specification, "value specification");
    if (value instanceof ComputedValue) {
      throw new IllegalArgumentException("Value must not be a ComputedValue instance");
    }
    _specification = specification;
    _value = value;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the value specification.
   *
   * @return the specification, not null
   */
  public ValueSpecification getSpecification() {
    return _specification;
  }

  /**
   * Gets the value.
   *
   * @return the value, not null
   */
  public Object getValue() {
    return _value;
  }

  public InvocationResult getInvocationResult() {
    return _result;
  }

  public void setInvocationResult(InvocationResult result) {
    this._result = result;
  }

  public String getExceptionClass() {
    return _exceptionClass;
  }

  public void setExceptionClass(String exceptionClass) {
    this._exceptionClass = exceptionClass;
  }

  public String getExceptionMsg() {
    return _exceptionMsg;
  }

  public void setExceptionMsg(String exceptionMsg) {
    this._exceptionMsg = exceptionMsg;
  }

  public String getStackTrace() {
    return _stackTrace;
  }

  public void setStackTrace(String stackTrace) {
    this._stackTrace = stackTrace;
  }

  public Set<ValueSpecification> getMissingInputs() {
    return _missingInputs;
  }

  public void setMissingInputs(Set<ValueSpecification> missingInputs) {
    this._missingInputs = missingInputs;
  }

  public Set<ValueRequirement> getRequirements() {
    return _originalRequirements;
  }

  public void setRequirements(Set<ValueRequirement> requirements) {
    _originalRequirements = requirements;
  }

  public String getComputeNodeId() {
    return _nodeId;
  }

  public void setComputeNodeId(String nodeId) {
    _nodeId = nodeId;
  }

  //-------------------------------------------------------------------------

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof ComputedValue) {
      ComputedValue other = (ComputedValue) obj;
      return ObjectUtils.equals(_specification, other._specification) &&
          ObjectUtils.equals(_exceptionClass, other._exceptionClass) &&
          ObjectUtils.equals(_missingInputs, other._missingInputs) &&
          ObjectUtils.equals(_exceptionMsg, other._exceptionMsg) &&
          ObjectUtils.equals(_originalRequirements, other._originalRequirements) &&
          ObjectUtils.equals(_stackTrace, other._stackTrace) &&
          ObjectUtils.equals(_result, other._result) &&
          ObjectUtils.equals(_value, other._value);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = _specification.hashCode();
    result = 31 * result + (_value != null ? _value.hashCode() : 0);
    result = 31 * result + (_result != null ? _result.hashCode() : 0);
    result = 31 * result + (_exceptionClass != null ? _exceptionClass.hashCode() : 0);
    result = 31 * result + (_exceptionMsg != null ? _exceptionMsg.hashCode() : 0);
    result = 31 * result + (_stackTrace != null ? _stackTrace.hashCode() : 0);
    result = 31 * result + (_missingInputs != null ? _missingInputs.hashCode() : 0);
    result = 31 * result + (_originalRequirements != null? _originalRequirements.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    // carefully select useful fields for toString
    ToStringStyle style = ToStringStyle.SHORT_PREFIX_STYLE;
    StringBuffer sb = new StringBuffer();
    style.appendStart(sb, this);
    style.append(sb, "result", getInvocationResult(), null);
    ValueSpecification spec = getSpecification();
    if (spec != null) {
      style.append(sb, "name", spec.getValueName(), null);
      ComputationTargetSpecification targetSpec = spec.getTargetSpecification();
      style.append(sb, "targetId", targetSpec.getIdentifier(), null);
      style.append(sb, "targetType", targetSpec.getType(), null);
      style.append(sb, "properties", spec.getProperties(), null);
    }
    style.append(sb, "value", getValue(), null);
    style.appendEnd(sb, this);
    return sb.toString();
  }

}
