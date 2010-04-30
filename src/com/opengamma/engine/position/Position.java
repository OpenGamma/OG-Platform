/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.math.BigDecimal;

import com.opengamma.engine.security.Security;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.Identifiable;
import com.opengamma.id.IdentificationScheme;

/**
 * A position held within a portfolio.
 * <p>
 * A position is a business-level structure representing a quantity of a security.
 * For example, a position might be 50 shares of OpenGamma.
 * <p>
 * The security itself may be unresolved. An unresolved position is where only the reference
 * to the underlying security is held instead of the full data.
 * An unresolved position will return null when {@link #getSecurity()} is called.
 */
public interface Position extends Identifiable {

  /**
   * The key to be used to refer to a position in identifiers.
   */
  public static final IdentificationScheme POSITION_IDENTITY_KEY_SCHEME = new IdentificationScheme("PositionIdentityKey");

  /**
   * Gets the identity key of the position.
   * @return the identity key, null if not uniquely identified
   */
  @Override
  Identifier getIdentityKey();

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
