/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.CachingComputationTargetResolver;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * Wraps an existing security source to populate a resolver cache as targets are retrieved.
 */
public class CacheNotifyingSecuritySource implements SecuritySource {

  // TODO: should be package visible

  private final SecuritySource _underlying;
  private final CachingComputationTargetResolver _cache;

  public CacheNotifyingSecuritySource(final SecuritySource underlying, final CachingComputationTargetResolver cache) {
    _underlying = underlying;
    _cache = cache;
  }

  private SecuritySource getUnderlying() {
    return _underlying;
  }

  private CachingComputationTargetResolver getCache() {
    return _cache;
  }

  @Override
  public ChangeManager changeManager() {
    return getUnderlying().changeManager();
  }

  @Override
  public Security get(final UniqueId uniqueId) {
    final Security security = getUnderlying().get(uniqueId);
    if (security != null) {
      getCache().cacheSecurities(Collections.singleton(security));
    }
    return security;
  }

  @Override
  public Map<UniqueId, Security> get(final Collection<UniqueId> uniqueIds) {
    final Map<UniqueId, Security> securities = getUnderlying().get(uniqueIds);
    if (!securities.isEmpty()) {
      getCache().cacheSecurities(securities.values());
    }
    return securities;
  }

  @Override
  public Security get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    final Security security = getUnderlying().get(objectId, versionCorrection);
    if (security != null) {
      getCache().cacheSecurities(Collections.singleton(security));
    }
    return security;
  }

  @Override
  public Collection<Security> get(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    final Collection<Security> securities = getUnderlying().get(bundle, versionCorrection);
    if (!securities.isEmpty()) {
      getCache().cacheSecurities(securities);
    }
    return securities;
  }

  @Override
  public Collection<Security> get(final ExternalIdBundle bundle) {
    final Collection<Security> securities = getUnderlying().get(bundle);
    if (!securities.isEmpty()) {
      getCache().cacheSecurities(securities);
    }
    return securities;
  }

  @Override
  public Security getSingle(final ExternalIdBundle bundle) {
    final Security security = getUnderlying().getSingle(bundle);
    if (security != null) {
      getCache().cacheSecurities(Collections.singleton(security));
    }
    return security;
  }

  @Override
  public Security getSingle(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    final Security security = getUnderlying().getSingle(bundle, versionCorrection);
    if (security != null) {
      getCache().cacheSecurities(Collections.singleton(security));
    }
    return security;
  }

}
