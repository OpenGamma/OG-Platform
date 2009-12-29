/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.util.ArgumentChecker;

/**
 * A specification of a particular computation target that will be resolved
 * later on in a computation process.
 *
 * @author kirk
 */
public class ComputationTargetSpecification implements Serializable, Cloneable {
  private final ComputationTargetType _type;
  private final String _identifier;

  public ComputationTargetSpecification(ComputationTargetType targetType, String identifier) {
    ArgumentChecker.checkNotNull(targetType, "Computation Target Type");
    switch(targetType) {
    case SECURITY:
    case POSITION:
    case MULTIPLE_POSITIONS:
      ArgumentChecker.checkNotNull(identifier, "Identifier (required for this target type)");
      break;
    default:
      // Not required for Primitive.
      break;
    }
    _type = targetType;
    _identifier = identifier;
  }

  /**
   * @return the type
   */
  public ComputationTargetType getType() {
    return _type;
  }

  /**
   * @return the identifier
   */
  public String getIdentifier() {
    return _identifier;
  }

  @Override
  protected ComputationTargetSpecification clone() {
    try {
      return (ComputationTargetSpecification) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Yes, it is supported.", e);
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
    if(!(obj instanceof ComputationTargetSpecification)) {
      return false;
    }
    ComputationTargetSpecification other = (ComputationTargetSpecification) obj;
    if(_type != other._type) {
      return false;
    }
    if(!ObjectUtils.equals(_identifier, other._identifier)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _type.hashCode();
    if(_identifier != null) {
      result = prime * result + _identifier.hashCode();
    }
    return result;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
