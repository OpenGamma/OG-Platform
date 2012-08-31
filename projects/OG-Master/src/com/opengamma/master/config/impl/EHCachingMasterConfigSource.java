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
import java.util.List;

import javax.time.Instant;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * A cache to optimize the results of {@code MasterConfigSource}.
 */
public class EHCachingMasterConfigSource extends MasterConfigSource {
  
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
    
    underlying.changeManager().addChangeListener(new ConfigDocumentChangeListener());
    
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
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    final Element element = _configCache.get(uniqueId);
    if (element != null) {
      return EHCacheUtils.<T>get(element);
    }
    try {
      return putValue(uniqueId, super.getConfig(clazz, uniqueId), _configCache);
    } catch (RuntimeException ex) {
      return EHCacheUtils.<T>putException(uniqueId, ex, _configCache);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> T getConfig(Class<T> clazz, ObjectId objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    
    final Object searchKey = Arrays.asList(clazz, objectId, versionCorrection);
    final Element element = _configCache.get(searchKey);
    if (element != null) {
      return EHCacheUtils.<T>get(element);
    }
    try {
      ConfigDocument<T> document = getDocument(clazz, objectId, versionCorrection);
      putValue(document.getUniqueId(), document.getValue(), _configCache);
      return putValue(searchKey, document.getValue(), _configCache);
    } catch (RuntimeException ex) {
      return EHCacheUtils.<T>putException(searchKey, ex, _configCache);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> Collection<? extends T> getConfigs(Class<T> clazz, String configName, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(configName, "configName");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    
    final Object searchKey = Arrays.asList(clazz, configName, versionCorrection);
    final Element element = _configCache.get(searchKey);
    if (element != null) {
      return get(element);
    }
    try {
      
      ConfigSearchResult<T> searchResult = searchDocuments(clazz, configName, versionCorrection);
      List<ConfigDocument<T>> documents = searchResult.getDocuments();
      if (documents != null) {
        for (ConfigDocument<T> doc : documents) {
          putValue(doc.getUniqueId(), doc.getValue(), _configCache);
        }
      }
      
      return putValue(searchKey, searchResult.getValues(), _configCache);
    } catch (RuntimeException ex) {
      return putException(searchKey, ex, _configCache);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> T getByName(Class<T> clazz, String name, Instant versionAsOf) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(name, "name");
        
    final Object searchKey = Arrays.asList(clazz, name, versionAsOf);
    final Element element = _configCache.get(searchKey);
    if (element != null) {
      return EHCacheUtils.<T>get(element);
    }
    try {
      
      ConfigDocument<T> doc = getDocumentByName(clazz, name, versionAsOf);
      T config = null;
      if (doc != null) {
        putValue(doc.getUniqueId(), doc.getValue(), _configCache);
        config = doc.getValue();
      }
      return putValue(searchKey, config, _configCache);
    } catch (RuntimeException ex) {
      return EHCacheUtils.<T>putException(searchKey, ex, _configCache);
    }
  }

  @Override
  public <T> T getLatestByName(Class<T> clazz, String name) {
    return getByName(clazz, name, null);
  }
  
  private class ConfigDocumentChangeListener implements ChangeListener {

    @Override
    public void entityChanged(ChangeEvent event) {
      switch (event.getType()) {
        case CORRECTED:
        case UPDATED:
        case REMOVED:
          cleanCaches(event);
          break;
        default:
          break;
      }
    }

    private void cleanCaches(ChangeEvent event) {
      UniqueId uniqueId = event.getBeforeId();
      if (inCache(uniqueId)) {
        _configCache.removeAll();
      }
    }

    private boolean inCache(UniqueId uniqueId) {
      Element element = _configCache.get(uniqueId);
      return element != null;
    }
  }
  
}
