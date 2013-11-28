/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph.ambiguity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.value.ValueRequirement;

/**
 * Cache of visited requirement, to detect recursion, and of resolved requirements to reduce the workload.
 * <p>
 * This is only for use by a single thread.
 */
/* package */class CheckingCache {

  private static final class VisitedKey {

    private final Object _key;
    private ValueRequirement _requirement;

    public VisitedKey() {
      _key = new Object();
    }

    public VisitedKey(final ValueRequirement requirement) {
      _key = new Object();
      _requirement = requirement;
    }

    private VisitedKey(final VisitedKey copyFrom) {
      _key = copyFrom._key;
      _requirement = copyFrom._requirement;
    }

    public void setRequirement(final ValueRequirement requirement) {
      _requirement = requirement;
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) {
        return false;
      }
      final VisitedKey other = (VisitedKey) o;
      if (other._key != _key) {
        return false;
      }
      return _requirement.equals(other._requirement);
    }

    @Override
    public int hashCode() {
      return _key.hashCode() ^ _requirement.hashCode();
    }

    @Override
    public VisitedKey clone() {
      return new VisitedKey(this);
    }

  }

  private final Set<ValueRequirement> _visited = new HashSet<ValueRequirement>();
  private final Map<Set<ValueRequirement>, VisitedKey> _visitedKey;
  private final ConcurrentMap<VisitedKey, FullRequirementResolution> _cache;
  private final VisitedKey _greedyCacheKey;

  @SuppressWarnings("unchecked")
  public CheckingCache(final boolean greedyCaching, final ConcurrentMap<?, FullRequirementResolution> cache) {
    _visitedKey = greedyCaching ? null : new HashMap<Set<ValueRequirement>, VisitedKey>();
    _cache = (cache != null) ? (ConcurrentMap<VisitedKey, FullRequirementResolution>) cache : new ConcurrentHashMap<VisitedKey, FullRequirementResolution>();
    _greedyCacheKey = greedyCaching ? new VisitedKey() : null;
  }

  public boolean begin(final ValueRequirement requirement) {
    return _visited.add(requirement);
  }

  public void end(final ValueRequirement requirement) {
    _visited.remove(requirement);
  }

  private VisitedKey getVisitedKey(final ValueRequirement requirement) {
    if (_greedyCacheKey != null) {
      _greedyCacheKey.setRequirement(requirement);
      return _greedyCacheKey;
    }
    VisitedKey key = _visitedKey.get(_visited);
    if (key != null) {
      key.setRequirement(requirement);
    } else {
      key = new VisitedKey(requirement);
      _visitedKey.put(ImmutableSet.copyOf(_visited), key);
    }
    return key;
  }

  public FullRequirementResolution get(final ValueRequirement requirement) {
    return _cache.get(getVisitedKey(requirement));
  }

  public FullRequirementResolution put(final FullRequirementResolution resolved) {
    final FullRequirementResolution existing = _cache.putIfAbsent(getVisitedKey(resolved.getRequirement()).clone(), resolved);
    return (existing != null) ? existing : resolved;
  }

}
