/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.math.BigDecimal;

import com.opengamma.engine.security.Security;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.Identifiable;
import com.opengamma.id.IdentificationScheme;

/**
 * This class represents either a resolved or unresolved position.  An unresolved position is where we only have the reference to the underlying
 * security and not the fully resolved security data itself.  An unresolved position will return null when the getSecurity() method is called.
 * @author kirk
 */
public interface Position extends Identifiable {
  
  public static final IdentificationScheme POSITION_IDENTITY_KEY_DOMAIN = new IdentificationScheme("PositionIdentityKey");
  
  /**
   * Returns the size of the position
   * @return the size of the position
   */
  BigDecimal getQuantity();
  
  /**
   * Returns a key which can be used to uniquely identify a security to the security master.
   * @return the security key
   */
  IdentifierBundle getSecurityKey();
  
  /**
   * <em>May</em> return a security, if the position has been resolved.  If it hasn't been resolved in will return null.
   * This method <em>is</em> guaranteed to return a security if it's passed to an analytic function.
   * @return the security
   */
  Security getSecurity(); 
  
}
