/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import java.util.Date;

import com.opengamma.id.UniqueIdentifier;

/**
 * A security master that can allows update as well as retrieval.
 */
public interface WritableSecuritySource extends SecuritySource {

  /**
   * Persist the given security.
   * 
   * @param instant  the instant at which the security is to be stored, null if no historical storage
   * @param security  the security to store, not null
   * @return The identifier for the newly stored security
   */
  UniqueIdentifier putSecurity(final Date instant, final Security security);

}
