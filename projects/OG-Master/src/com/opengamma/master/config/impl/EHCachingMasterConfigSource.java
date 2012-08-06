/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import static com.opengamma.util.ehcache.EHCacheUtils.get;
import static com.opengamma.util.ehcache.EHCacheUtils.putException;
import static com.opengamma.util.ehcache.EHCacheUtils.putValue;

import java.util.Arrays;
import java.util.Collection;

import javax.time.Instant;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * A cache to optimize the results of {@code MasterConfigSource}.
 */
public class EHCachingMasterConfigSource extends MasterConfigSource {

  private static final Logger s_logger = LoggerFactory.getLogger(EHCachingMasterConfigSource.class);
  
  /**
   * Cache key for configs.
   */
  private static final String CONFIG_CACHE = "config";

  /**
   * The cache manager.
   */
  private final CacheManager _cacheManager;
  /**
   * The result cache.
   */
  private final Cache _configCache;

  /**
   * Creates the cache around an underlying config source.
   * 
   * @param underlying  the underlying data, not null
   * @param cacheManager  the cache manager, not null
   */
  public EHCachingMasterConfigSource(final ConfigMaster underlying, final CacheManager cacheManager) {
    super(underlying);
    
    s_logger.warn("EHCache doesn't perform well here (see PLAT-1015)");
    
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    _cacheManager = cacheManager;
    EHCacheUtils.addCache(cacheManager, CONFIG_CACHE);
    _configCache = EHCacheUtils.getCacheFromManager(cacheManager, CONFIG_CACHE);
  }

  //-------------------------------------------------------------------------
  public CacheManager getCacheManager() {
    return _cacheManager;
  }
  
  //-------------------------------------------------------------------------
  @Override
  public <T> T getConfig(Class<T> clazz, UniqueId uniqueId) {
    final Object key = Arrays.asList(clazz, uniqueId);
    final Element e = _configCache.get(key);
    if (e != null) {
      return get(e);
    }
    try {
      return putValue(key, super.getConfig(clazz, uniqueId), _configCache);
    } catch (RuntimeException ex) {
      return putException(key, ex, _configCache);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> T getConfig(Class<T> clazz, ObjectId objectId, VersionCorrection versionCorrection) {
    final Object key = Arrays.asList(clazz, objectId, versionCorrection);
    final Element e = _configCache.get(key);
    if (e != null) {
      return get(e);
    }
    try {
      return putValue(key, super.getConfig(clazz, objectId, versionCorrection), _configCache);
    } catch (RuntimeException ex) {
      return putException(key, ex, _configCache);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> Collection<? extends T> getConfigs(Class<T> clazz, String configName, VersionCorrection versionCorrection) {
    final Object key = Arrays.asList(clazz, configName, versionCorrection);
    final Element e = _configCache.get(key);
    if (e != null) {
      return get(e);
    }
    try {
      return putValue(key, super.getConfigs(clazz, configName, versionCorrection), _configCache);
    } catch (RuntimeException ex) {
      return putException(key, ex, _configCache);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> T getByName(Class<T> clazz, String name, Instant versionAsOf) {
    if (versionAsOf == null) {
      return super.getByName(clazz, name, versionAsOf);
    }
    
    final Object key = Arrays.asList(clazz, name, versionAsOf);
    final Element e = _configCache.get(key);
    if (e != null) {
      return get(e);
    }
    try {
      return putValue(key, super.getByName(clazz, name, versionAsOf), _configCache);
    } catch (RuntimeException ex) {
      return putException(key, ex, _configCache);
    }
  }
  
  
  

}
