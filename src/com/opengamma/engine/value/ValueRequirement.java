/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.value;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author kirk
 */
public class ValueRequirement implements Serializable, Cloneable {
  private final String _valueName;
  private final ComputationTargetType _targetType;
  private final String _targetKey;
  
  public ValueRequirement(String valueName, ComputationTargetType targetType, String targetKey) {
    ArgumentChecker.checkNotNull(valueName, "Value name");
    ArgumentChecker.checkNotNull(targetType, "Computation target type");
    // Target key may be null.
    _valueName = valueName.intern();
    _targetType = targetType;
    _targetKey = targetKey;
  }
  
  /**
   * @return the valueName
   */
  public String getValueName() {
    return _valueName;
  }
  
  /**
   * @return the targetType
   */
  public ComputationTargetType getTargetType() {
    return _targetType;
  }

  /**
   * @return the targetKey
   */
  public String getTargetKey() {
    return _targetKey;
  }

  @Override
  protected ValueRequirement clone() {
    try {
      return (ValueRequirement)super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Yes, it is supported", e);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(obj == null) {
      return false;
    }
    if(!(obj instanceof ValueRequirement)) {
      return false;
    }
    ValueRequirement other = (ValueRequirement) obj;
    // Note that we're interning, so we can do this.
    if(_valueName != other._valueName) {
      return false;
    }
    if(_targetType != other._targetType) {
      return false;
    }
    // Always do this one last because it's the slowest comparison.
    if(!ObjectUtils.equals(_targetKey, other._targetKey)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _valueName.hashCode();
    result = prime * result + _targetType.hashCode();
    if(_targetKey != null) {
      result = prime * result + _targetKey.hashCode();
    }
    return result;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
  
}
