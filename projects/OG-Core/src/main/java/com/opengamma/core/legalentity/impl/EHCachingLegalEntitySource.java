/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.legalentity.impl;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.google.common.collect.MapMaker;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.legalentity.LegalEntity;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * A cache decorating a {@code LegalEntitySource}.
 * <p/>
 * The cache is implemented using {@code EHCache}.
 * <p/>
 * Any requests with a "latest" version/correction or unversioned unique identifier are not cached
 * and will always hit the underlying. This should not be an issue in practice as the engine components
 * which use the legal entity source will always specify an exact version/correction and versioned unique identifiers.
 */
public class EHCachingLegalEntitySource implements LegalEntitySource {

  /**
   * Cache key for legal entities.
   */
  private static final String LEGALENTITY_CACHE = "legalentity";

  /**
   * The underlying legal entity source.
   */
  private final LegalEntitySource _underlying;
  /**
   * The cache manager.
   */
  private final CacheManager _cacheManager;
  /**
   * The legal entity cache.
   */
  private final Cache _legalentityCache;
  /**
   * The front cache.
   */
  private final ConcurrentMap<Object, LegalEntity> _frontCache = new MapMaker().weakValues().makeMap();

  /**
   * Creates the cache around an underlying legal entity source.
   *
   * @param underlying   the underlying data, not null
   * @param cacheManager the cache manager, not null
   */
  public EHCachingLegalEntitySource(final LegalEntitySource underlying, final CacheManager cacheManager) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    _underlying = underlying;
    _cacheManager = cacheManager;
    EHCacheUtils.addCache(cacheManager, LEGALENTITY_CACHE);
    _legalentityCache = EHCacheUtils.getCacheFromManager(cacheManager, LEGALENTITY_CACHE);
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the underlying source of legal entities.
   *
   * @return the underlying source of legal entities, not null
   */
  protected LegalEntitySource getUnderlying() {
    return _underlying;
  }

  /**
   * Gets the cache manager.
   *
   * @return the cache manager, not null
   */
  protected CacheManager getCacheManager() {
    return _cacheManager;
  }

  /**
   * For use by test methods only to control the front cache.
   */
  void emptyFrontCache() {
    _frontCache.clear();
  }

  /**
   * For use by test methods only to control the EH cache.
   */
  void emptyEHCache() {
    EHCacheUtils.clear(getCacheManager(), LEGALENTITY_CACHE);
  }

  //-------------------------------------------------------------------------
  protected LegalEntity addToFrontCache(LegalEntity legalentity, VersionCorrection versionCorrection) {
    if (legalentity.getUniqueId().isLatest()) {
      return legalentity;
    }
    final LegalEntity existing = _frontCache.putIfAbsent(legalentity.getUniqueId(), legalentity);
    if (existing != null) {
      return existing;
    }
    if (versionCorrection != null) {
      _frontCache.put(Pairs.of(legalentity.getExternalIdBundle(), versionCorrection), legalentity);
      _frontCache.put(Pairs.of(legalentity.getUniqueId().getObjectId(), versionCorrection), legalentity);
    }
    return legalentity;
  }

  protected LegalEntity addToCache(LegalEntity legalentity, VersionCorrection versionCorrection) {
    final LegalEntity front = addToFrontCache(legalentity, null);
    if (front == legalentity) {
      if (legalentity.getUniqueId().isVersioned()) {
        _legalentityCache.put(new Element(legalentity.getUniqueId(), legalentity));
      }
      if (versionCorrection != null) {
        _legalentityCache.put(new Element(Pairs.of(legalentity.getExternalIdBundle(), versionCorrection), legalentity));
        _legalentityCache.put(new Element(Pairs.of(legalentity.getUniqueId().getObjectId(), versionCorrection), legalentity));
      }
    }
    return front;
  }

  //-------------------------------------------------------------------------
  @Override
  public LegalEntity get(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    // check cache, but not if latest
    if (uniqueId.isVersioned()) {
      LegalEntity cached = _frontCache.get(uniqueId);
      if (cached != null) {
        return cached;
      }
      final Element e = _legalentityCache.get(uniqueId);
      if (e != null) {
        cached = (LegalEntity) e.getObjectValue();
        return addToFrontCache(cached, null);
      }
    }
    // query underlying
    LegalEntity legalEntity = getUnderlying().get(uniqueId);
    return addToCache(legalEntity, null);
  }

  @Override
  public LegalEntity get(ObjectId objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    // latest not in cache, can cache only by uniqueId
    if (versionCorrection.containsLatest()) {
      return addToCache(getUnderlying().get(objectId, versionCorrection), null);
    }
    // check cache
    final Pair<ObjectId, VersionCorrection> key = Pairs.of(objectId, versionCorrection);
    LegalEntity cached = _frontCache.get(key);
    if (cached != null) {
      return cached;
    }
    final Element e = _legalentityCache.get(key);
    if (e != null) {
      cached = (LegalEntity) e.getObjectValue();
      return addToFrontCache(cached, versionCorrection);
    }
    // query underlying
    LegalEntity legalEntity = getUnderlying().get(objectId, versionCorrection);
    return addToCache(legalEntity, versionCorrection);
  }

  //-------------------------------------------------------------------------
  @Override
  public LegalEntity getSingle(ExternalId externalId) {
    ArgumentChecker.notNull(externalId, "externalId");
    return getSingle(externalId.toBundle(), VersionCorrection.LATEST);
  }

  @Override
  public LegalEntity getSingle(ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    return getSingle(bundle, VersionCorrection.LATEST);
  }

  @Override
  public LegalEntity getSingle(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    // latest not in cache, can cache only by uniqueId
    if (versionCorrection.containsLatest()) {
      return addToCache(getUnderlying().getSingle(bundle, versionCorrection), null);
    }
    // check cache
    final Pair<ExternalIdBundle, VersionCorrection> key = Pairs.of(bundle, versionCorrection);
    LegalEntity cached = _frontCache.get(key);
    if (cached != null) {
      return cached;
    }
    final Element e = _legalentityCache.get(key);
    if (e != null) {
      cached = (LegalEntity) e.getObjectValue();
      return addToFrontCache(cached, versionCorrection);
    }
    // query underlying
    LegalEntity legalEntity = getUnderlying().getSingle(bundle, versionCorrection);
    return addToCache(legalEntity, versionCorrection);
  }

  //-------------------------------------------------------------------------
  @Override
  @SuppressWarnings("deprecation")
  public Collection<LegalEntity> get(ExternalIdBundle bundle) {
    return getUnderlying().get(bundle);
  }

  @Override
  public Collection<LegalEntity> get(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    return getUnderlying().get(bundle, versionCorrection);
  }

  //-------------------------------------------------------------------------
  @Override
  public Map<UniqueId, LegalEntity> get(Collection<UniqueId> uniqueIds) {
    Map<UniqueId, LegalEntity> map = getUnderlying().get(uniqueIds);
    for (Entry<UniqueId, LegalEntity> entry : map.entrySet()) {
      entry.setValue(addToCache(entry.getValue(), null));
    }
    return map;
  }

  @Override
  public Map<ObjectId, LegalEntity> get(Collection<ObjectId> objectIds, VersionCorrection versionCorrection) {
    Map<ObjectId, LegalEntity> map = getUnderlying().get(objectIds, versionCorrection);
    for (Entry<ObjectId, LegalEntity> entry : map.entrySet()) {
      entry.setValue(addToCache(entry.getValue(), versionCorrection));
    }
    return map;
  }

  @Override
  public Map<ExternalIdBundle, Collection<LegalEntity>> getAll(Collection<ExternalIdBundle> bundles, VersionCorrection versionCorrection) {
    return getUnderlying().getAll(bundles, versionCorrection);
  }

  @Override
  public Map<ExternalIdBundle, LegalEntity> getSingle(Collection<ExternalIdBundle> bundles, VersionCorrection versionCorrection) {
    Map<ExternalIdBundle, LegalEntity> map = getUnderlying().getSingle(bundles, versionCorrection);
    for (Entry<ExternalIdBundle, LegalEntity> entry : map.entrySet()) {
      entry.setValue(addToCache(entry.getValue(), versionCorrection));
    }
    return map;
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return getUnderlying().changeManager();
  }

  /**
   * Call this at the end of a unit test run to clear the state of EHCache.
   * It should not be part of a generic lifecycle method.
   */
  protected void shutdown() {
    _cacheManager.removeCache(LEGALENTITY_CACHE);
    _frontCache.clear();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getUnderlying() + "]";
  }

}
