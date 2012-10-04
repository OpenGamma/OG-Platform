/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import static com.opengamma.util.ehcache.EHCacheUtils.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
import com.sun.jersey.api.client.GenericType;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import sun.net.www.content.text.Generic;

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
      putValue(uniqueId.getObjectId(), super.getConfig(clazz, uniqueId), _configCache);
      return putValue(uniqueId, super.getConfig(clazz, uniqueId), _configCache);
    } catch (RuntimeException ex) {
      return EHCacheUtils.<T>putException(uniqueId, ex, _configCache);
    }
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public <T> T getConfig(Class<T> clazz, ObjectId objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");

    final GenericType<ConfigItem<T>> gt = new GenericType<ConfigItem<T>>(){};
      
    final Object searchKey = Arrays.asList(clazz, objectId, versionCorrection);
    final Element element = _configCache.get(searchKey);
    if (element != null) {
      return EHCacheUtils.<T>get(element);
    }
    try {
      ConfigItem<?> item = get(objectId, versionCorrection);
      if(gt.getRawClass().isAssignableFrom(item.getClass())){
        ConfigItem<T> itemT = (ConfigItem<T>) item;
        putValue(itemT.getUniqueId().getObjectId(), itemT.getValue(), _configCache);
        putValue(itemT.getUniqueId(), itemT.getValue(), _configCache);
        return putValue(searchKey, itemT, _configCache).getValue();
      }else{
        throw new RuntimeException("config object is not instance of "+gt.getRawClass());  
      }
    } catch (RuntimeException ex) {
      return EHCacheUtils.<T>putException(searchKey, ex, _configCache);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> T getConfig(Class<T> clazz, String configName, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(configName, "configName");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");

    final Object searchKey = Arrays.asList(clazz, configName, versionCorrection);
    final Element element = _configCache.get(searchKey);
    if (element != null) {
      return EHCacheUtils.get(element);
    }
    try {

      ConfigItem<T> item = get(clazz, configName, versionCorrection);
      putValue(item.getUniqueId().getObjectId(), item.getValue(), _configCache);
      putValue(item.getUniqueId(), item.getValue(), _configCache);

      return putValue(searchKey, item.getValue(), _configCache);
    } catch (RuntimeException ex) {
      return putException(searchKey, ex, _configCache);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> ConfigItem<T> get(Class<T> clazz, String name, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(name, "name");

    final Object searchKey = Arrays.asList(clazz, name, versionCorrection);
    final Element element = _configCache.get(searchKey);
    if (element != null) {
      return EHCacheUtils.get(element);
    }
    try {

      ConfigItem<T> item = get(clazz, name, versionCorrection);
      T config = null;
      if (item != null) {
        putValue(item.getUniqueId().getObjectId(), item.getValue(), _configCache);
        putValue(item.getUniqueId(), item.getValue(), _configCache);
      }
      return putValue(searchKey, item, _configCache);
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
  public <T> T getLatest(Class<T> clazz, String name) {
    return get(clazz, name, VersionCorrection.LATEST).getValue();
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
