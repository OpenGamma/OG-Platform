/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.*;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.tuple.Triple;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * A cache decorating a {@code FinancialSecuritySource}.
 * <p>
 * The cache is implemented using {@code EHCache}.
 */
public abstract class AbstractEHCachingSourceWithExternalBundle<S extends SourceWithExternalBundle<T>, T extends UniqueIdentifiable & ExternalBundleIdentifiable>
  extends AbstractEHCachingSource<S, T>
  implements SourceWithExternalBundle<T> {


  /** The bundle cache key. */
  static final String BUNDLE_CACHE = "-bundle-cache";
  /** The Bundle hint cache key. */
  static final String BUNDLE_HINT_CACHE = "-bundle-hint-cache";

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractEHCachingSourceWithExternalBundle.class);

  /**
   * The bundle cache.
   */
  protected final Cache _bundleCache;

  /**
   * The bundle hint cache.
   */
  protected final Cache _bundleHintCache;

  /**
   * Creates an instance over an underlying source specifying the cache manager.
   *
   * @param underlying the underlying security source, not null
   * @param cacheManager the cache manager, not null
   */
  public AbstractEHCachingSourceWithExternalBundle(final S underlying, final CacheManager cacheManager) {
    super(underlying, cacheManager);
    EHCacheUtils.addCache(cacheManager, this.getClass().getName() + BUNDLE_CACHE);
    EHCacheUtils.addCache(cacheManager, this.getClass().getName() + BUNDLE_HINT_CACHE);
    _bundleCache = EHCacheUtils.getCacheFromManager(cacheManager, BUNDLE_CACHE);
    _bundleHintCache = EHCacheUtils.getCacheFromManager(cacheManager, BUNDLE_HINT_CACHE);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<T> get(ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    Element e = _bundleCache.get(bundle);
    Collection<T> result = new HashSet<T>();
    if (e != null) {
      if (e.getObjectValue() instanceof Collection<?>) {
        result.addAll((Collection<T>) e.getObjectValue());
      } else {
        s_logger.warn("returned object {} from cache is not a Collection<T>", e.getObjectValue());
      }
    } else {
      result = getUnderlying().get(bundle);
      if (result != null) {
        _bundleCache.put(new Element(bundle, result));
        cacheItems(result);
      }
    }
    return result;
  }

  @Override
  public Collection<T> get(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    Collection<T> result = getUnderlying().get(bundle, versionCorrection);
    if (result == null) {
      return Collections.emptySet();
    }
    cacheItems(result);
    return result;
  }

  @Override
  public T getSingle(ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    Collection<T> matched = get(bundle);
    if (matched.isEmpty()) {
      return null;
    }
    return matched.iterator().next();
  }

  @Override
  public T getSingle(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    T result = _frontCache2.get(bundle, versionCorrection);
    if (result != null) {
      return result;
    }
    final Instant correctedTo = versionCorrection.getCorrectedTo();
    final Instant versionAsOf = versionCorrection.getVersionAsOf();
    final BigInteger correctedToBucket = correctedTo == null ? s_latestBucket : versionCorrection.getCorrectedTo().toEpochNanos().divide(s_versionCorrectionBucketSizeInNanos);
    final BigInteger versionAsOfBucket = versionAsOf == null ? s_latestBucket : versionCorrection.getVersionAsOf().toEpochNanos().divide(s_versionCorrectionBucketSizeInNanos);
    final Triple<ExternalIdBundle, BigInteger, BigInteger> key = Triple.of(bundle, correctedToBucket, versionAsOfBucket);
    final Element hintUid = _bundleHintCache.get(key);
    if (hintUid != null) {
      final ObjectId hint = (ObjectId) hintUid.getValue();
      try {
        //Caching is based on the idea that this query is significantly faster
        result = get(hint, versionCorrection);
        if (result != null) {
          if (result.getExternalIdBundle().containsAny(bundle)) {
            //This is a good enough result with the current resolution logic,
            // h'ver as soon as we have rules about which of multiple matches to use this caching must be rewritten
            final T existing = _frontCache2.putIfAbsent(bundle, versionCorrection, result);
            if (existing != null) {
              return existing;
            } else {
              return result;
            }
          }
        }
      } catch (DataNotFoundException dnfe) {
        s_logger.debug("Hinted security {} has dissapeared", hint);
      }
    }
    final Collection<T> matched = get(bundle, versionCorrection);
    if (matched.isEmpty()) {
      return null;
    }
    result = matched.iterator().next();
    final T existing = _frontCache2.putIfAbsent(bundle, versionCorrection, result);
    if (existing != null) {
      return existing;
    }
    final Element element = new Element(key, result.getUniqueId().getObjectId());
    element.setTimeToLive(s_versionCorrectionBucketSizeInSeconds);
    _bundleHintCache.put(element);
    return result;
  }


  @Override
  public void shutdown() {
    super.shutdown();
    _manager.removeCache(BUNDLE_CACHE);
    _manager.removeCache(BUNDLE_HINT_CACHE);
  }
}
