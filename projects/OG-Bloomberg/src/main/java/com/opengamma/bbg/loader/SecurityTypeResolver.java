/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import java.util.Collection;
import java.util.Map;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.security.ManageableSecurity;

/**
 * Resolves security type based on Bloomberg futureCategory and securityType to
 * subclasses type of {@link ManageableSecurity}.
 */
public interface SecurityTypeResolver {
  
  /**
   * Work out what security type is based on security type and future category 
   * returned by Bloomberg
   * 
   * @param identifiers the identifiers
   * @return the map of requested bundle to Security type, not null.
   */
  Map<ExternalIdBundle, SecurityType> getSecurityType(Collection<ExternalIdBundle> identifiers);

}
