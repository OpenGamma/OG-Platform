/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import java.util.Date;

import com.opengamma.id.UniqueIdentifier;

/**
 * A source of securities that can be updated.
 */
public interface WritableSecuritySource extends SecuritySource {
  // TODO: this should disappear with the new SecurityMaster

  /**
   * Persist the given security.
   * 
   * @param instant  the instant at which the security is to be stored, null if no historical storage
   * @param security  the security to store, not null
   * @return the unique identifier for the newly stored security, not null
   */
  UniqueIdentifier putSecurity(final Date instant, final Security security);

}
