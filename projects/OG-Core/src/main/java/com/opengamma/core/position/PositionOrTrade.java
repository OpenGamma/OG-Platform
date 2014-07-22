/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position;

import java.math.BigDecimal;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.PublicSPI;

/**
 * A position and a trade are related concepts and this interface provides common access.
 * <p>
 * A {@link Trade} stores details of an individual trade and refers to a quantity of a security.
 * A {@link Position} stores the combined set of trades forming a single position of a security.
 * Since both hold a quantity of a security, it can be useful to refer to both in a common way.
 * <p>
 * The reference to a security is held primarily by an identifier bundle.
 * However, this can become resolved, setting the security field with the full data.
 * <p>
 * This interface is read-only.
 * Implementations may be mutable.
 */
@PublicSPI
public interface PositionOrTrade extends UniqueIdentifiable {

  /**
   * Gets the unique identifier of the position/trade.
   * <p>
   * This specifies a single version-correction of the position/trade.
   * 
   * @return the unique identifier for this position/trade, not null within the engine
   */
  @Override
  UniqueId getUniqueId();

  /**
   * Gets the amount of the position held in terms of the security.
   * 
   * @return the amount of the position
   */
  BigDecimal getQuantity();

  /**
   * Gets a link connecting to the security.
   * <p>
   * The link holds a strong or weak reference to the security
   * and can be resolved to the actual security when required.
   * 
   * @return the security link, not null
   */
  SecurityLink getSecurityLink();

  /**
   * Gets the target security from the link.
   * <p>
   * This convenience method gets the target security from the link.
   * This is guaranteed to return a security within an analytic function.
   * 
   * @return the security link, null if target not resolved in the link
   */
  Security getSecurity();

}
