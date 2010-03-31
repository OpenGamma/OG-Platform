/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.position.PortfolioNode;
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
  
  public static boolean isCompatible(ComputationTargetType computationTargetType, Object computationTarget) {
    switch(computationTargetType) {
    case PRIMITIVE:
      return computationTarget == null;
    case SECURITY:
      return (computationTarget instanceof Security);
    case POSITION:
      return (computationTarget instanceof Position);
    case MULTIPLE_POSITIONS:
      return (computationTarget instanceof PortfolioNode);
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
    if(computationTarget instanceof PortfolioNode) {
      return MULTIPLE_POSITIONS;
    }
    return null;
  }

}
