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
  
  /**
   * Optional operation: determine whether there is already an LKV store
   * for the given specification.
   * In general, this should return true if a call to
   * {@link #newInstance(ExternalId, String)} will return an instance
   * that is already populated with data, rather than one which is empty.
   * 
   * @param security The security to be checked
   * @param normalizationRuleSetId The normalization rule set checked
   * @return true iff there is an LKV store for this specification
   */
  boolean isAvailable(ExternalId security, String normalizationRuleSetId);
}
