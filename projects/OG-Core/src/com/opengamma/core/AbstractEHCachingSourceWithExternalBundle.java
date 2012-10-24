/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.opengamma.id.ExternalBundleIdentifiable;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.tuple.Pair;

/**
 * A cache decorating a {@code FinancialSecuritySource}.
 * <p>
 * The cache is implemented using {@code EHCache}.
 * 
 * @param <V> the type returned by the source
 * @param <S> the source
 */
public abstract class AbstractEHCachingSourceWithExternalBundle<V extends UniqueIdentifiable & ExternalBundleIdentifiable, S extends SourceWithExternalBundle<V>>
    extends AbstractEHCachingSource<V, S>
    implements SourceWithExternalBundle<V> {

  private static final String EID_TO_UID_CACHE = "-eid-to-uid";

  /**
   * The cache of external identifiers at a version/correction to matching unique identifiers.
   */
  private final Cache _eidToUidCache;

  /**
   * Creates an instance over an underlying source specifying the cache manager.
   * 
   * @param underlying the underlying security source, not null
   * @param cacheManager the cache manager, not null
   */
  public AbstractEHCachingSourceWithExternalBundle(final S underlying, final CacheManager cacheManager) {
    super(underlying, cacheManager);
    EHCacheUtils.addCache(cacheManager, this.getClass().getName() + EID_TO_UID_CACHE);
    _eidToUidCache = EHCacheUtils.getCacheFromManager(cacheManager, this.getClass().getName() + EID_TO_UID_CACHE);
  }

  @Override
  public Collection<V> get(final ExternalIdBundle bundle) {
    return getUnderlying().get(bundle);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<V> get(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    if (versionCorrection.containsLatest()) {
      return getUnderlying().get(bundle, versionCorrection);
    }
    final Pair<ExternalIdBundle, VersionCorrection> key = Pair.of(bundle, versionCorrection);
    final Element e = _eidToUidCache.get(key);
    if (e != null) {
      if (e.getObjectValue() instanceof Collection) {
        final Collection<UniqueId> identifiers = (Collection<UniqueId>) e.getObjectValue();
        if (identifiers.isEmpty()) {
          return Collections.emptySet();
        } else {
          return get(identifiers).values();
        }
      }
    }
    Collection<V> result = getUnderlying().get(bundle, versionCorrection);
    if (result.isEmpty()) {
      cacheIdentifiers(Collections.<UniqueId>emptyList(), key);
    } else {
      List<UniqueId> uids = new ArrayList<UniqueId>(result.size());
      for (V item : result) {
        uids.add(item.getUniqueId());
      }
      Collections.sort(uids);
      cacheIdentifiers(uids, key);
      cacheItems(result);
    }
    return result;
  }

  @Override
  public V getSingle(final ExternalIdBundle bundle) {
    return getUnderlying().getSingle(bundle);
  }

  @SuppressWarnings("unchecked")
  @Override
  public V getSingle(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    if (versionCorrection.containsLatest()) {
      return getUnderlying().getSingle(bundle, versionCorrection);
    }
    final Pair<ExternalIdBundle, VersionCorrection> key = Pair.of(bundle, versionCorrection);
    Element e = _eidToUidCache.get(key);
    if (e != null) {
      if (e.getObjectValue() instanceof List) {
        final List<UniqueId> identifiers = (List<UniqueId>) e.getObjectValue();
        for (UniqueId uid : identifiers) {
          final V result = get(uid);
          if (result != null) {
            return result;
          }
        }
        return null;
      } else if (e.getObjectValue() instanceof UniqueId) {
        return get((UniqueId) e.getObjectValue());
      }
    }
    final V result = getUnderlying().getSingle(bundle, versionCorrection);
    if (result == null) {
      cacheIdentifiers(Collections.<UniqueId>emptyList(), key);
    } else {
      cacheIdentifiers(result.getUniqueId(), key);
    }
    return result;
  }

  protected void cacheIdentifiers(final UniqueId uniqueId, final Pair<ExternalIdBundle, VersionCorrection> key) {
    synchronized (_eidToUidCache) {
      Element e = _eidToUidCache.get(key);
      if (e == null) {
        _eidToUidCache.put(new Element(key, uniqueId));
      }
    }
  }

  protected void cacheIdentifiers(final List<UniqueId> uniqueIds, final Pair<ExternalIdBundle, VersionCorrection> key) {
    synchronized (_eidToUidCache) {
      _eidToUidCache.put(new Element(key, uniqueIds));
    }
  }

  @Override
  public void shutdown() {
    super.shutdown();
    getCacheManager().removeCache(EID_TO_UID_CACHE);
  }
}
