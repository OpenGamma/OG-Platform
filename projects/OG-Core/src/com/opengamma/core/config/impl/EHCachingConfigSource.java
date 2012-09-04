/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.config.impl;

import static com.opengamma.util.ehcache.EHCacheUtils.get;
import static com.opengamma.util.ehcache.EHCacheUtils.putException;
import static com.opengamma.util.ehcache.EHCacheUtils.putValue;

import java.util.Arrays;
import java.util.Collection;

import javax.time.Instant;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * An EHCache based {@link ConfigSource}. This is better than having no cache but is not very efficient. Also does not listen for changes to the underlying data.
 */
public class EHCachingConfigSource implements ConfigSource {

  /*pacakge*/ static final String CACHE_NAME = "config";
  private final ConfigSource _underlying;
  private final Cache _cache;

  public EHCachingConfigSource(final ConfigSource underlying, final CacheManager cacheManager) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    _underlying = underlying;
    EHCacheUtils.addCache(cacheManager, CACHE_NAME);
    _cache = EHCacheUtils.getCacheFromManager(cacheManager, CACHE_NAME);
  }

  protected ConfigSource getUnderlying() {
    return _underlying;
  }

  protected Cache getCache() {
    return _cache;
  }

  @Override
  public <T> T getConfig(Class<T> clazz, UniqueId uniqueId) {
    final Object key = Arrays.asList(clazz, uniqueId);
    final Element e = getCache().get(key);
    if (e != null) {
      return EHCacheUtils.<T>get(e);
    }
    try {
      return putValue(key, getUnderlying().getConfig(clazz, uniqueId), getCache());
    } catch (RuntimeException ex) {
      return EHCacheUtils.<T>putException(key, ex, getCache());
    }
  }

  @Override
  public <T> T getConfig(Class<T> clazz, ObjectId objectId, VersionCorrection versionCorrection) {
    final Object key = Arrays.asList(clazz, objectId, versionCorrection);
    final Element e = getCache().get(key);
    if (e != null) {
      return EHCacheUtils.<T>get(e);
    }
    try {
      return putValue(key, getUnderlying().getConfig(clazz, objectId, versionCorrection), getCache());
    } catch (RuntimeException ex) {
      return EHCacheUtils.<T>putException(key, ex, getCache());
    }
  }

  @Override
  public <T> Collection<? extends T> getConfigs(Class<T> clazz, String configName, VersionCorrection versionCorrection) {
    final Object key = Arrays.asList(clazz, configName, versionCorrection);
    final Element e = getCache().get(key);
    if (e != null) {
      return get(e);
    }
    try {
      return putValue(key, getUnderlying().getConfigs(clazz, configName, versionCorrection), getCache());
    } catch (RuntimeException ex) {
      return putException(key, ex, getCache());
    }
  }

  @Override
  public <T> T getLatestByName(Class<T> clazz, String name) {
    // this should not really be cached
    return getUnderlying().getLatestByName(clazz, name);
  }

  @Override
  public <T> T getByName(Class<T> clazz, String name, Instant versionAsOf) {
    final Object key = Arrays.asList(clazz, name, versionAsOf);
    final Element e = getCache().get(key);
    if (e != null) {
      return EHCacheUtils.<T>get(e);
    }
    try {
      return putValue(key, getUnderlying().getByName(clazz, name, versionAsOf), getCache());
    } catch (RuntimeException ex) {
      return EHCacheUtils.<T>putException(key, ex, getCache());
    }
  }

}
