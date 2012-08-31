/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.opengamma.id.UniqueId;

/**
 * A source of {@link FunctionBlacklistPolicy} definitions backed by a map.
 */
public class InMemoryFunctionBlacklistPolicySource implements FunctionBlacklistPolicySource {
  
  private final Map<String, FunctionBlacklistPolicy> _policiesByName = new ConcurrentHashMap<String, FunctionBlacklistPolicy>();
  private final Map<UniqueId, FunctionBlacklistPolicy> _policiesByUniqueId = new ConcurrentHashMap<UniqueId, FunctionBlacklistPolicy>();

  public void addPolicy(final FunctionBlacklistPolicy policy) {
    _policiesByName.put(policy.getName(), policy);
    _policiesByUniqueId.put(policy.getUniqueId(), policy);
  }

  @Override
  public FunctionBlacklistPolicy getPolicy(final UniqueId uniqueId) {
    return _policiesByUniqueId.get(uniqueId);
  }

  @Override
  public FunctionBlacklistPolicy getPolicy(final String name) {
    return _policiesByName.get(name);
  }

}
