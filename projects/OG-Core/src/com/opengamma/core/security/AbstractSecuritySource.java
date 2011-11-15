/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.security;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * Partial implementation of {@link SecuritySource}.
 */
public abstract class AbstractSecuritySource implements SecuritySource {

  @Override
  public Map<UniqueId, Security> getSecurity(Collection<UniqueId> uniqueIds) {
    ArgumentChecker.notNull(uniqueIds, "uniqueIds");
    Map<UniqueId, Security> result = new HashMap<UniqueId, Security>(uniqueIds.size());
    for (UniqueId uniqueId : uniqueIds) {
      try {
        Security security = getSecurity(uniqueId);
        if (security != null) {
          result.put(uniqueId, security);
        }
      } catch (DataNotFoundException ex) {
        // Ignore
      }
    }
    return result;
  }

}
