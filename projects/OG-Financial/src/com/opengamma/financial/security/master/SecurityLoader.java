/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.master;

import java.util.Collection;
import java.util.Map;

import com.opengamma.engine.security.ManageableSecurity;
import com.opengamma.id.IdentifierBundle;

/**
 * A general purpose security loader
 * <p>
 * SecurityLoader loads security reference data from sources like BLOOMBERG/REUTERS
 */
public interface SecurityLoader {

  /**
   * Loads the security data for the requested IdentifierBundles
   * 
   * @param identifiers a collection of identifiers to load, not-null
   * @return a map of security with the requested identifierbundle as key
   */
  Map<IdentifierBundle, ManageableSecurity> loadSecurity(Collection<IdentifierBundle> identifiers);
}
