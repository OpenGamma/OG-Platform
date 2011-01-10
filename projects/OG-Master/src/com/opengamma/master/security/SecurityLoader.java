/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security;

import java.util.Collection;
import java.util.Map;

import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * A general purpose security loader for populating a master.
 * <p>
 * SecurityLoader adds or updates the details about a security in the attached master.
 * This will normally be achieved by calling a standard external data source.
 */
public interface SecurityLoader {

  /**
   * Loads the security data for the requested IdentifierBundles
   * 
   * @param identifiers  a collection of identifiers to load, not null
   * @return a map of input bundle to created unique identifier from the master, not null
   */
  Map<IdentifierBundle, UniqueIdentifier> loadSecurity(Collection<IdentifierBundle> identifiers);

  /**
   * Gets the associated master.
   * 
   * @return the master that is being populated, not null
   */
  SecurityMaster getSecurityMaster();

}
