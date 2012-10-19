/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import static com.opengamma.util.ehcache.EHCacheUtils.putException;
import static com.opengamma.util.ehcache.EHCacheUtils.putValue;
import static com.opengamma.util.functional.Functional.functional;

import java.util.Arrays;
import java.util.Collection;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.paging.PagingRequest;

/**
 * A cache to optimize the results of {@code MasterConfigSource}.
 */
public class EHCachingMasterConfigSource extends MasterConfigSource {

  /**
   * Cache key for configs.
   */
  /*package*/ static final String CONFIG_CACHE = "config";

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


  public CacheManager getCacheManager() {
    return _cacheManager;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getConfig(Class<T> clazz, UniqueId uniqueId) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    final Element element = _configCache.get(uniqueId);
    if (element != null) {
      return (T) ((ConfigDocument) EHCacheUtils.get(element)).getObject().getValue();
    }
    try {
      ConfigDocument doc = getMaster().get(uniqueId);
      putValue(uniqueId.getObjectId(), doc, _configCache);
      return (T) putValue(uniqueId, doc, _configCache).getObject().getValue();
    } catch (RuntimeException ex) {
      return EHCacheUtils.putException(uniqueId, ex, _configCache);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getConfig(Class<T> clazz, ObjectId objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
      
    final Object searchKey = Arrays.asList(clazz, objectId, versionCorrection);
    final Element element = _configCache.get(searchKey);
    if (element != null) {
      return ((T) ((ConfigDocument) EHCacheUtils.get(element)).getObject().getValue());
    }
    try {
      ConfigDocument doc = getMaster().get(objectId, versionCorrection);
      ConfigItem<T> item = (ConfigItem<T>) doc.getObject();
      putValue(item.getUniqueId().getObjectId(), doc, _configCache);
      putValue(item.getUniqueId(), doc, _configCache);
      return (T) putValue(searchKey, doc, _configCache).getObject().getValue();
    } catch (RuntimeException ex) {
      return EHCacheUtils.putException(searchKey, ex, _configCache);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getConfig(Class<T> clazz, String configName, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(configName, "configName");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");

    final Object searchKey = Arrays.asList(clazz, configName, versionCorrection);
    final Element element = _configCache.get(searchKey);
    if (element != null) {
      return (T) ((ConfigDocument) EHCacheUtils.get(element)).getObject().getValue();
    }
    try {

      ConfigSearchRequest<T> searchRequest = new ConfigSearchRequest<T>(clazz);
      searchRequest.setName(configName);
      searchRequest.setVersionCorrection(versionCorrection);
      ConfigDocument doc = functional(getMaster().search(searchRequest).getDocuments()).first();
      
      putValue(doc.getUniqueId().getObjectId(), doc, _configCache);
      putValue(doc.getUniqueId(), doc, _configCache);

      return (T) putValue(searchKey, doc, _configCache).getObject().getValue();
    } catch (RuntimeException ex) {
      return putException(searchKey, ex, _configCache);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> ConfigItem<T> get(Class<T> clazz, String name, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(name, "name");

    final Object searchKey = Arrays.asList(clazz, name, versionCorrection);
    final Element element = _configCache.get(searchKey);
    if (element != null) {
      return (ConfigItem<T>) ((ConfigDocument) EHCacheUtils.get(element)).getObject();
    }
    try {
      
      ConfigSearchRequest<T> searchRequest = new ConfigSearchRequest<T>(clazz);
      searchRequest.setName(name);
      searchRequest.setVersionCorrection(versionCorrection);
      ConfigDocument doc = functional(getMaster().search(searchRequest).getDocuments()).first();

      if (doc != null) {
        putValue(doc.getUniqueId().getObjectId(), doc, _configCache);
        putValue(doc.getUniqueId(), doc, _configCache);
      }
      return (ConfigItem<T>) putValue(searchKey, doc, _configCache).getObject();
    } catch (RuntimeException ex) {
      return EHCacheUtils.putException(searchKey, ex, _configCache);
    }
  }

  @Override
  public <T> Collection<ConfigItem<T>> getAll(Class<T> clazz, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(clazz, "clazz");

    ConfigSearchRequest<T> request = new ConfigSearchRequest<T>();
    request.setPagingRequest(PagingRequest.ALL);
    request.setVersionCorrection(versionCorrection);
    request.setType(clazz);

    ConfigSearchResult<T> searchResult = getMaster().search(request);
    return searchResult.getValues();
  }

  @Override
  public <T> T getLatestByName(Class<T> clazz, String name) {
    return getConfig(clazz, name, VersionCorrection.LATEST);
  }

  private class ConfigDocumentChangeListener implements ChangeListener {

    @Override
    public void entityChanged(ChangeEvent event) {
      switch (event.getType()) {
        case CHANGED:
        case REMOVED:
          cleanCaches(event);
          break;
        default:
          break;
      }
    }

    private void cleanCaches(ChangeEvent event) {
      ObjectId objectId = event.getObjectId();
      if (inCache(objectId)) {
        _configCache.removeAll();
      }
    }

    private boolean inCache(ObjectId objectId) {
      Element element = _configCache.get(objectId);
      return element != null;
    }
  }

}
