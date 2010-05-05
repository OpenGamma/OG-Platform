/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.Security;
import com.opengamma.id.Identifiable;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A fully resolved target, sufficient for computation invocation.
 */
public class ComputationTarget implements Serializable {

  /**
   * The type of the target.
   */
  private final ComputationTargetType _type;
  /**
   * The actual target.
   */
  private final Object _value;

  /**
   * Creates a target for computation.
   * @param value  the target itself, may be null
   */
  public ComputationTarget(Object value) {
    _type = ComputationTargetType.determineFromTarget(value);
    _value = value;
  }

  /**
   * Creates a target for computation.
   * @param type  the type of the target, not null
   * @param value  the target itself, may be null
   * @throws IllegalArgumentException if the value is invalid for the type
   */
  public ComputationTarget(ComputationTargetType type, Object value) {
    ArgumentChecker.notNull(type, "type");
    if (type.isCompatible(value) == false) {
      throw new IllegalArgumentException("Value is invalid for type: " + type);
    }
    _type = type;
    _value = value;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the type of the target.
   * @return the type, not null
   */
  public ComputationTargetType getType() {
    return _type;
  }

  /**
   * Gets the actual target.
   * @return the target, may be null
   */
  public Object getValue() {
    return _value;
  }

  /**
   * Gets the unique identifier (old fashioned "identity key"), if one exists.
   * @return the unique identifier, may be null
   */
  public Identifier getIdentityKey() {
    // TODO: remove once Security is UniqueIdentifiable
    Object value = getValue();
    if (value instanceof Identifiable) {
      return ((Identifiable) value).getIdentityKey();
    }
    if (value instanceof UniqueIdentifiable) {
      UniqueIdentifier key = ((UniqueIdentifiable) value).getUniqueIdentifier();
      return Identifier.of(key.getScheme(), key.getValue());
    }
    return null;
  }

  /**
   * Gets the unique identifier, if one exists.
   * @return the unique identifier, may be null
   */
  public UniqueIdentifier getUniqueIdentifier() {
    Object value = getValue();
    if (value instanceof UniqueIdentifiable) {
      return ((UniqueIdentifiable) value).getUniqueIdentifier();
    }
    if (value instanceof Identifiable) {  // TODO: remove once Security is UniqueIdentifiable
      Identifier key = ((Identifiable) value).getIdentityKey();
      return UniqueIdentifier.of(key.getScheme().getName(), key.getValue());
    }
    return null;
  }

  //-------------------------------------------------------------------------
  /**
   * Safely converts the target to a {@code PortfolioNode}.
   * @return the portfolio node, not null
   * @throws IllegalStateException if the type is not MULTIPLE_POSITIONS
   */
  public PortfolioNode getPortfolioNode() {
    if (getType() != ComputationTargetType.MULTIPLE_POSITIONS) {
      throw new IllegalStateException("Requested a PortfolioNode for a target of type " + getType());
    }
    return (PortfolioNode) getValue();
  }

  /**
   * Safely converts the target to a {@code Position}.
   * @return the position, not null
   * @throws IllegalStateException if the type is not POSITION
   */
  public Position getPosition() {
    if (getType() != ComputationTargetType.POSITION) {
      throw new IllegalStateException("Requested a Position for a target of type " + getType());
    }
    return (Position) getValue();
  }

  /**
   * Safely converts the target to a {@code Security}.
   * @return the security, not null
   * @throws IllegalStateException if the type is not SECURITY
   */
  public Security getSecurity() {
    if (getType() != ComputationTargetType.SECURITY) {
      throw new IllegalStateException("Requested a Security for a target of type " + getType());
    }
    return (Security) getValue();
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a specification that is equivalent to this target.
   * @return the specification equivalent to this target, not null
   */
  public ComputationTargetSpecification toSpecification() {
    return new ComputationTargetSpecification(_type, getIdentityKey());
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof ComputationTarget) {
      ComputationTarget other = (ComputationTarget) obj;
      return _type == other._type &&
          ObjectUtils.equals(_value, other._value);
    }
    return false;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _type.hashCode();
    if (_value != null) {
      result = prime * result + _value.hashCode();
    }
    return result;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
