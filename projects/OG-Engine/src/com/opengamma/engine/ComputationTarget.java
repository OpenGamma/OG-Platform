/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.text.StrBuilder;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionOrTrade;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * A fully resolved target, sufficient for computation invocation.
 */
@PublicAPI
public class ComputationTarget implements Serializable {

  private static final long serialVersionUID = 1L;
  
  /**
   * The type of the target.
   */
  private final ComputationTargetType _type;
  /**
   * The actual target.
   */
  private final Object _value;

  /**
   * The cached hash code.
   */
  private transient volatile int _hashCode;
  
  /**
   * Creates a target for computation.
   * @param value  the target itself, may be null
   */
  public ComputationTarget(final Object value) {
    _type = ComputationTargetType.determineFromTarget(value);
    _value = value;
  }

  /**
   * Creates a target for computation.
   * @param type  the type of the target, not null
   * @param value  the target itself, may be null
   * @throws IllegalArgumentException if the value is invalid for the type
   */
  public ComputationTarget(final ComputationTargetType type, final Object value) {
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
   * Gets the unique identifier, if one exists.
   * @return the unique identifier, may be null
   */
  public UniqueId getUniqueId() {
    final Object value = getValue();
    if (value instanceof UniqueIdentifiable) {
      return ((UniqueIdentifiable) value).getUniqueId();
    }
    return null;
  }

  //-------------------------------------------------------------------------
  /**
   * Safely converts the target to a {@code PortfolioNode}.
   * @return the portfolio node, not null
   * @throws IllegalStateException if the type is not PORTFOLIO_NODE
   */
  public PortfolioNode getPortfolioNode() {
    if (getType() != ComputationTargetType.PORTFOLIO_NODE) {
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
   * Safely converts the target to a {@code Trade}.
   * @return the trade, not null
   * @throws IllegalStateException if the type is not TRADE
   */
  public Trade getTrade() {
    if (getType() != ComputationTargetType.TRADE) {
      throw new IllegalStateException("Requested a Trade for a target of type " + getType());
    }
    return (Trade) getValue();
  }
  /**
   * Safely converts the target to a {@code Position} or {@code Trade}.
   * @return the position or trade, not null
   * @throws IllegalStateException if the type is not a POSITION or TRADE
   */
  public PositionOrTrade getPositionOrTrade() {
    if (getType() != ComputationTargetType.POSITION && getType() != ComputationTargetType.TRADE) {
      throw new IllegalStateException("Requested a Position or Trade for a target of type " + getType());
    }
    return (PositionOrTrade) getValue();
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
   * Gets the name of the computation target.
   * <p>
   * This can the portfolio name, the security name,
   * the name of the security underlying a position,
   * or - for primitives - null.
   * 
   * @return the name of the computation target, null if a primitive 
   */
  public String getName() {
    switch (getType()) {
      case PORTFOLIO_NODE:
        return getPortfolioNode().getName();
        
      case SECURITY:
        return getSecurity().getName();
        
      case POSITION:
        return getPosition().getSecurity().getName();
        
      case PRIMITIVE:
        return null;
        
      default:
        throw new RuntimeException("Unexpected type" + getType());
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a specification that is equivalent to this target.
   * @return the specification equivalent to this target, not null
   */
  public ComputationTargetSpecification toSpecification() {
    return new ComputationTargetSpecification(_type, getUniqueId());
  }

  //-------------------------------------------------------------------------
  
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof ComputationTarget) {
      final ComputationTarget other = (ComputationTarget) obj;
      return _type == other._type &&
          ObjectUtils.equals(_value, other._value);
    }
    return false;
  }

  @Override
  public int hashCode() {
    if (_hashCode == 0) {
      final int prime = 31;
      int result = 1;
      result = prime * result + _type.hashCode();
      if (_value != null) {
        result = prime * result + _value.hashCode();
      }
      _hashCode = result;
    }
    return _hashCode;
  }

  @Override
  public String toString() {
    return new StrBuilder()
      .append("CT[")
      .append(getType())
      .append(", ")
      .append(getValue())
      .append(']')
      .toString();
  }

}
