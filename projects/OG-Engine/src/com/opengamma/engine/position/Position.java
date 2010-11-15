/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.math.BigDecimal;

import com.opengamma.core.security.Security;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.PublicSPI;

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
@PublicSPI
public interface Position extends UniqueIdentifiable {

  /**
   * Gets the unique identifier of the position.
   * @return the identifier, not null
   */
  UniqueIdentifier getUniqueIdentifier();

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

  /**
   * Gets the unique identifier of the node within the portfolio this position is immediately under.
   * @return the unique identifier
   */
  UniqueIdentifier getPortfolioNode();

}
