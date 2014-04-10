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
import java.util.Map;
import java.util.Set;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.id.ExternalBundleIdentifiable;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * A cache decorating a {@link SourceWithExternalBundle}.
 * <p>
 * The cache is implemented using {@code EHCache}.
 * 
 * @param <V> the type returned by the source
 * @param <S> the source
 */
public abstract class AbstractEHCachingSourceWithExternalBundle<V extends UniqueIdentifiable & ExternalBundleIdentifiable, S extends SourceWithExternalBundle<V>>
    extends AbstractEHCachingSource<V, S>
    implements SourceWithExternalBundle<V> {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractEHCachingSourceWithExternalBundle.class);

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

    // this is crude but it allows caching of lookups that use VersionCorrection.LATEST.
    // VersionCorrection.LATEST can refer to different versions of the same object, but by clearing the caches
    // when anything in the underlying source changes we ensure we never see stale data.
    // this won't work well if the data changes frequently.
    getUnderlying().changeManager().addChangeListener(new ChangeListener() {
      @Override
      public void entityChanged(ChangeEvent event) {
        _eidToUidCache.flush();
      }
    });
  }

  @Override
  public Collection<V> get(final ExternalIdBundle bundle) {
    final Collection<V> result = getUnderlying().get(bundle);
    cacheItems(result);
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<V> get(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    if (versionCorrection.containsLatest()) {
      Collection<V> results = getUnderlying().get(bundle, versionCorrection);
      cacheItems(results);
      return results;
    }
    final Pair<ExternalIdBundle, VersionCorrection> key = Pairs.of(bundle, versionCorrection);
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
    final Collection<V> result = getUnderlying().get(bundle, versionCorrection);
    if (result.isEmpty()) {
      cacheIdentifiers(Collections.<UniqueId>emptyList(), key);
    } else {
      final List<UniqueId> uids = new ArrayList<UniqueId>(result.size());
      for (final V item : result) {
        uids.add(item.getUniqueId());
      }
      Collections.sort(uids);
      cacheIdentifiers(uids, key);
      cacheItems(result);
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<ExternalIdBundle, Collection<V>> getAll(final Collection<ExternalIdBundle> bundles, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundles, "bundles");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    if (versionCorrection.containsLatest()) {
      return getUnderlying().getAll(bundles, versionCorrection);
    }
    final Map<ExternalIdBundle, Collection<V>> results = Maps.newHashMapWithExpectedSize(bundles.size());
    final Collection<ExternalIdBundle> misses = new ArrayList<ExternalIdBundle>(bundles.size());
    final Map<ExternalIdBundle, Collection<UniqueId>> lookupBundles = Maps.newHashMapWithExpectedSize(bundles.size());
    final Set<UniqueId> lookupIds = Sets.newHashSetWithExpectedSize(bundles.size());
    for (ExternalIdBundle bundle : bundles) {
      final Pair<ExternalIdBundle, VersionCorrection> key = Pairs.of(bundle, versionCorrection);
      final Element e = _eidToUidCache.get(key);
      if (e != null) {
        if (e.getObjectValue() instanceof Collection) {
          final Collection<UniqueId> identifiers = (Collection<UniqueId>) e.getObjectValue();
          if (identifiers.isEmpty()) {
            results.put(bundle, Collections.<V>emptySet());
          } else {
            lookupBundles.put(bundle, identifiers);
            lookupIds.addAll(identifiers);
          }
          continue;
        }
      }
      misses.add(bundle);
    }
    if (!lookupIds.isEmpty()) {
      final Map<UniqueId, V> underlying = get(lookupIds);
      for (Map.Entry<ExternalIdBundle, Collection<UniqueId>> lookupBundle : lookupBundles.entrySet()) {
        final ArrayList<V> resultCollection = new ArrayList<V>(lookupBundle.getValue().size());
        for (UniqueId uid : lookupBundle.getValue()) {
          final V resultValue = underlying.get(uid);
          if (resultValue != null) {
            resultCollection.add(resultValue);
          }
        }
        resultCollection.trimToSize();
        results.put(lookupBundle.getKey(), resultCollection);
      }
    }
    if (!misses.isEmpty()) {
      final Map<ExternalIdBundle, Collection<V>> underlying = getUnderlying().getAll(misses, versionCorrection);
      for (ExternalIdBundle miss : misses) {
        final Pair<ExternalIdBundle, VersionCorrection> key = Pairs.of(miss, versionCorrection);
        final Collection<V> result = underlying.get(miss);
        if ((result == null) || result.isEmpty()) {
          cacheIdentifiers(Collections.<UniqueId>emptyList(), key);
        } else {
          final List<UniqueId> uids = new ArrayList<>(result.size());
          for (final V item : result) {
            uids.add(item.getUniqueId());
          }
          Collections.sort(uids);
          cacheIdentifiers(uids, key);
          cacheItems(result);
          results.put(miss, result);
        }
      }
    }
    return results;
  }

  @Override
  public V getSingle(final ExternalIdBundle bundle) {
    return getSingle(bundle, VersionCorrection.LATEST);
  }

  @SuppressWarnings("unchecked")
  @Override
  public V getSingle(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final Pair<ExternalIdBundle, VersionCorrection> key = Pairs.of(bundle, versionCorrection);
    final Element e = _eidToUidCache.get(key);
    if (e != null) {
      if (e.getObjectValue() instanceof List) {
        final List<UniqueId> identifiers = (List<UniqueId>) e.getObjectValue();
        for (final UniqueId uid : identifiers) {
          V result;
          try {
            result = get(uid);
          } catch (DataNotFoundException dnfe) {
            s_logger.warn("Cached {} for {} no longer available", uid, key);
            result = null;
          }
          if (result != null) {
            return result;
          }
        }
        return null;
      } else if (e.getObjectValue() instanceof UniqueId) {
        // REVIEW 2013-11-06 Andrew -- Get will probably throw a DNFE instead of returning NULL
        final UniqueId uid = (UniqueId) e.getObjectValue();
        try {
          return get(uid);
        } catch (DataNotFoundException dnfe) {
          s_logger.warn("Cached {} for {} no longer available", uid, key);
          return null;
        }
      }
    }
    final V result = getUnderlying().getSingle(bundle, versionCorrection);
    if (result == null) {
      cacheIdentifiers(Collections.<UniqueId>emptyList(), key);
    } else {
      cacheIdentifiers(result.getUniqueId(), key);
      cacheItem(result);
    }
    return result;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<ExternalIdBundle, V> getSingle(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundles, "bundles");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    if (versionCorrection.containsLatest()) {
      return getUnderlying().getSingle(bundles, versionCorrection);
    }
    final Map<ExternalIdBundle, V> results = Maps.newHashMapWithExpectedSize(bundles.size());
    final Collection<ExternalIdBundle> misses = new ArrayList<ExternalIdBundle>(bundles.size());
    final Map<ExternalIdBundle, Collection<UniqueId>> hits = Maps.newHashMapWithExpectedSize(bundles.size());
    final Set<UniqueId> lookup = Sets.newHashSetWithExpectedSize(bundles.size());
    for (ExternalIdBundle bundle : bundles) {
      final Pair<ExternalIdBundle, VersionCorrection> key = Pairs.of(bundle, versionCorrection);
      final Element e = _eidToUidCache.get(key);
      if (e != null) {
        if (e.getObjectValue() instanceof List) {
          final List<UniqueId> identifiers = (List<UniqueId>) e.getObjectValue();
          lookup.addAll(identifiers);
          hits.put(bundle, identifiers);
          continue;
        } else if (e.getObjectValue() instanceof UniqueId) {
          final UniqueId identifier = (UniqueId) e.getObjectValue();
          lookup.add(identifier);
          hits.put(bundle, Collections.singleton(identifier));
          continue;
        }
      }
      misses.add(bundle);
    }
    if (!lookup.isEmpty()) {
      final Map<UniqueId, V> underlying = getUnderlying().get(lookup);
      for (Map.Entry<ExternalIdBundle, Collection<UniqueId>> hit : hits.entrySet()) {
        final ExternalIdBundle bundle = hit.getKey();
        for (UniqueId uid : hit.getValue()) {
          final V result = underlying.get(uid);
          if (result != null) {
            results.put(bundle, result);
            break;
          }
        }
      }
    }
    if (!misses.isEmpty()) {
      final Map<ExternalIdBundle, ? extends V> underlying = getUnderlying().getSingle(misses, versionCorrection);
      for (ExternalIdBundle miss : misses) {
        final Pair<ExternalIdBundle, VersionCorrection> key = Pairs.of(miss, versionCorrection);
        final V result = underlying.get(miss);
        if (result == null) {
          cacheIdentifiers(Collections.<UniqueId>emptyList(), key);
        } else {
          cacheIdentifiers(result.getUniqueId(), key);
          cacheItem(result);
          results.put(miss, result);
        }
      }
    }
    return results;
  }

  protected void cacheIdentifiers(final UniqueId uniqueId, final Pair<ExternalIdBundle, VersionCorrection> key) {
    synchronized (_eidToUidCache) {
      final Element e = _eidToUidCache.get(key);
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
