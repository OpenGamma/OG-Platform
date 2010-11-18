/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.math.BigDecimal;

import com.opengamma.core.security.Security;
import com.opengamma.id.IdentifierBundle;

/**
 * TradeOrPosition is the super type for a {@link Trade Trade} or {@link Position Position}
 * 
 * <p>
 * The security itself may be unresolved. An unresolved position is where only the reference
 * to the underlying security is held instead of the full data.
 * An unresolved position will return null when {@link #getSecurity()} is called.
 * 
 */
public interface TradeOrPosition {

  /**
   * Gets the amount of the position held in terms of the security.
   * @return the amount of the position
   */
  BigDecimal getQuantity();

  /**
   * Gets a key to the security being held.
   * <p>
   * This allows the security to be referenced without actually loading the security itself.
   * @return the security key
   */
  IdentifierBundle getSecurityKey();

  /**
   * Gets the security being held, returning {@code null} if it has not been loaded.
   * <p>
   * This method is guaranteed to return a security within an analytic function.
   * @return the security
   */
  Security getSecurity();
  
}
