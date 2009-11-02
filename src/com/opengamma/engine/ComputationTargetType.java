/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import java.util.Collection;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.Security;

/**
 * 
 *
 * @author kirk
 */
public enum ComputationTargetType {
  PRIMITIVE,
  SECURITY,
  POSITION,
  MULTIPLE_POSITIONS;
  
  @SuppressWarnings("unchecked")
  public static boolean isCompatible(ComputationTargetType computationTargetType, Object computationTarget) {
    switch(computationTargetType) {
    case PRIMITIVE:
      return computationTarget == null;
    case SECURITY:
      return (computationTarget instanceof Security);
    case POSITION:
      return (computationTarget instanceof Position);
    case MULTIPLE_POSITIONS:
      if(!(computationTarget instanceof Collection)) {
        return false;
      }
      Collection collection = (Collection) computationTarget;
      for(Object obj : collection) {
        if(!(obj instanceof Position)) {
          return false;
        }
      }
      return true;
    default:
      throw new OpenGammaRuntimeException("Unhandled computation target type");
    }
  }
  
  public static ComputationTargetType determineFromTarget(Object computationTarget) {
    if(computationTarget == null) {
      return PRIMITIVE;
    }
    if(computationTarget instanceof Security) {
      return SECURITY;
    }
    if(computationTarget instanceof Position) {
      return POSITION;
    }
    if(computationTarget instanceof Collection<?>) {
      return MULTIPLE_POSITIONS;
    }
    return null;
  }

}
