/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.cache;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;

/**
 * Decorates a reference data provider, adding caching of invalid field errors.
 * <p>
 * The cache is implemented using memory.
 */
public class InMemoryInvalidFieldCachingReferenceDataProvider extends AbstractInvalidFieldCachingReferenceDataProvider {

  /**
   * The in memory cache.
   */
  private static final ConcurrentMap<String, Set<String>> s_cache = new ConcurrentHashMap<String, Set<String>>();

  /**
   * Creates an instance.
   * 
   * @param underlying  the underlying provider, not null
   */
  public InMemoryInvalidFieldCachingReferenceDataProvider(ReferenceDataProvider underlying) {
    super(underlying);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void saveInvalidFields(String identifier, Set<String> invalidFields) {
    s_cache.put(identifier, invalidFields);
  }

  @Override
  protected Map<String, Set<String>> loadInvalidFields(Set<String> identifiers) {
    Map<String, Set<String>> result = Maps.newHashMap();
    for (String identifier : identifiers) {
      Set<String> invalidFields = s_cache.get(identifier);
      result.put(identifier, invalidFields);
    }
    return result;
  }

}
