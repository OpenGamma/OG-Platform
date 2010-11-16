/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security;

import java.util.Collection;
import java.util.Map;

import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * A general purpose security loader
 * <p>
 * SecurityLoader loads security reference data from sources like BLOOMBERG/REUTERS and populate the security master
 * If the security is missing in {@link SecurityMaster securityMaster} it will be added, otherwise it will be updated
 */
public interface SecurityLoader {

  /**
   * Loads the security data for the requested IdentifierBundles
   * 
   * @param identifiers a collection of identifiers to load, not-null
   * @return a map of identifierbundle to uniqueidentifier from security master
   */
  Map<IdentifierBundle, UniqueIdentifier> loadSecurity(Collection<IdentifierBundle> identifiers);
}
