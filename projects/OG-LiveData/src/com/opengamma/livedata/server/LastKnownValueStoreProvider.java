/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.Set;
import com.opengamma.id.ExternalId;

/**
 * A Factory for {@link LastKnownValueStore}s.
 */
public interface LastKnownValueStoreProvider {
  
  LastKnownValueStore newInstance(ExternalId security, String normalizationRuleSetId);
  
  /**
   * Optional operation: scan the underlying LKV persistence and return
   * the identifiers presently stored for the given scheme.
   *
   * @param identifierScheme The scheme to check for raw identifier values
   * @return all unique identifiers for that scheme in the store
   * @throws UnsupportedOperationException if this operation is not supported.
   */
  Set<String> getAllIdentifiers(String identifierScheme);
}
