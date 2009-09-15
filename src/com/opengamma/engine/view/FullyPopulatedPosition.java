/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.Security;

/**
 * Holds a {@link Position} and its corresponding {@link Security}.
 *
 * @author kirk
 */
public class FullyPopulatedPosition implements Cloneable {
  private final Position _position;
  private final Security _security;
  
  public FullyPopulatedPosition(Position position, Security security) {
    if(position == null) {
      throw new NullPointerException("Must specify a position.");
    }
    if(security == null) {
      throw new NullPointerException("Must specify a security.");
    }
    _position = position;
    _security = security;
  }

  /**
   * @return the position
   */
  public Position getPosition() {
    return _position;
  }

  /**
   * @return the security
   */
  public Security getSecurity() {
    return _security;
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(obj == null) {
      return false;
    }
    if(!(obj instanceof FullyPopulatedPosition)) {
      return false;
    }
    FullyPopulatedPosition other = (FullyPopulatedPosition) obj;
    if(!ObjectUtils.equals(getPosition(), other.getPosition())) {
      return false;
    }
    assert ObjectUtils.equals(getSecurity(), other.getSecurity());
    return true;
  }

  @Override
  public int hashCode() {
    return getPosition().hashCode();
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  @Override
  public FullyPopulatedPosition clone(){
    try {
      return (FullyPopulatedPosition)super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("yes, it is supported.");
    }
  }

}
