/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.Security;
import com.opengamma.id.Identifier;

/**
 * The type that computation will be base on.
 */
public enum ComputationTargetType {

  /**
   * A set of positions (a portfolio node, or whole portfolio).
   */
  MULTIPLE_POSITIONS,
  /**
   * A position.
   */
  POSITION,
  /**
   * A security.
   */
  SECURITY,
  /**
   * A simple type, effectively "anything else".
   */
  PRIMITIVE;

  /**
   * Checks if the type is compatible with the target.
   * @param computationTargetType
   * @param target
   * @return true if compatible
   */
  public boolean isCompatible(Object target) {
    switch(this) {
      case MULTIPLE_POSITIONS:
        return (target instanceof PortfolioNode || target instanceof Portfolio);
      case POSITION:
        return (target instanceof Position);
      case SECURITY:
        return (target instanceof Security ||
                (target instanceof Identifier && ((Identifier) target).isScheme(Security.SECURITY_IDENTITY_KEY_DOMAIN)));
      case PRIMITIVE:
        return (target instanceof Portfolio == false &&
                target instanceof PortfolioNode == false &&
                target instanceof Position == false &&
                target instanceof Security == false );
      default:
        throw new OpenGammaRuntimeException("Unhandled computation target type: " + this);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Derives the type for the specified target.
   * @param target  the target to introspect, may be null
   * @return the type, not null
   */
  public static ComputationTargetType determineFromTarget(Object target) {
    if (target instanceof Portfolio) {
      return MULTIPLE_POSITIONS;
    }
    if (target instanceof PortfolioNode) {
      return MULTIPLE_POSITIONS;
    }
    if (target instanceof Position) {
      return POSITION;
    }
    if (target instanceof Security) {
      return SECURITY;
    }
    if (target instanceof Identifier && ((Identifier) target).isScheme(Security.SECURITY_IDENTITY_KEY_DOMAIN)) {
      return SECURITY;
    }
    return PRIMITIVE;  // anything else
  }

}
