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
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsg;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author kirk
 */
public class ValueRequirement implements Serializable {
  public static final String VALUE_NAME_FIELD_NAME = "valueName";
  public static final String TARGET_TYPE_FIELD_NAME = "targetType";
  public static final String TARGET_ID_FIELD_NAME = "targetIdentifier";
  
  private final String _valueName;
  private final ComputationTargetSpecification _targetSpecification;
  
  public ValueRequirement(String valueName, ComputationTargetType targetType, String targetKey) {
    this(valueName, new ComputationTargetSpecification(targetType, targetKey));
  }
  
  public ValueRequirement(String valueName, ComputationTargetSpecification targetSpecification) {
    ArgumentChecker.checkNotNull(valueName, "Value name");
    ArgumentChecker.checkNotNull(targetSpecification, "Computation target specification");
    _valueName = valueName.intern();
    _targetSpecification = targetSpecification;
  }
  
  /**
   * @return the valueName
   */
  public String getValueName() {
    return _valueName;
  }
  
  /**
   * @return the targetSpecification
   */
  public ComputationTargetSpecification getTargetSpecification() {
    return _targetSpecification;
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
    if(!ObjectUtils.equals(_targetSpecification, other._targetSpecification)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _valueName.hashCode();
    result = prime * result + _targetSpecification.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
  
  public void writeFields(FudgeMsg msg) {
    msg.add(VALUE_NAME_FIELD_NAME, _valueName);
    msg.add(TARGET_TYPE_FIELD_NAME, _targetSpecification.getType().name());
    msg.add(TARGET_ID_FIELD_NAME, _targetSpecification.getIdentifier());
  }
  
  public static ValueRequirement fromFudge(FudgeFieldContainer msg) {
    String valueName = msg.getString(VALUE_NAME_FIELD_NAME);
    ComputationTargetType targetType = ComputationTargetType.valueOf(msg.getString(TARGET_TYPE_FIELD_NAME));
    String targetIdentifier = msg.getString(TARGET_ID_FIELD_NAME);
    return new ValueRequirement(valueName, targetType, targetIdentifier);
  }
  
}
