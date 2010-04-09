/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import java.util.Date;

/**
 * A {@link SecurityMaster} which can persist {@link Security} objects as
 * well as retrieve.
 *
 * @author Andrew Griffin
 */
public interface WritableSecurityMaster extends SecurityMaster {
  
  /**
   * Persist the given security.
   * 
   * @param now the date at which the security is to be stored, for implementations that can hold multiple versions of a security from different times
   * @param security the security to store
   */
  public void putSecurity (final Date now, final Security security);
  
}
