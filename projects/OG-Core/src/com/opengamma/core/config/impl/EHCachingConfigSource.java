/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.config.impl;

import static com.opengamma.id.IdUtils.isVersionCorrection;
import static com.opengamma.util.ehcache.EHCacheUtils.putValues;
import static com.opengamma.util.functional.Functional.functional;

import java.util.*;

import javax.time.Instant;

import com.opengamma.core.AbstractEHCachingSource;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.functional.Function1;
import com.opengamma.util.tuple.Pair;
import com.sun.jersey.api.client.GenericType;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * An EHCache based {@link ConfigSource}. This is better than having no cache but is not very efficient. Also does not listen for changes to the underlying data.
 */
public class EHCachingConfigSource extends AbstractEHCachingSource<ConfigSource, ConfigItem<?>> implements ConfigSource {

  private static final String CACHE_NAME = "configs_by_name";
  private final Cache _cache;

//  /**
//   * EHCache doesn't like being hammered repeatedly for the same objects. Also, if the window of objects being requested is bigger than the in memory window then new objects get created as the on-disk
//   * values get deserialized. The solution is to maintain a soft referenced buffer so that all the while the objects we have previously returned are in use we won't requery EHCache for them.
//   */
//
//  private final HashDeepMap2<ObjectId, UniqueId, Pair<String, Object>> _frontCache = new HashDeepMap2<ObjectId, UniqueId, Pair<String, Object>>() {
//    protected MapMaker mapMaker() {
//      return super.mapMaker().softValues();
//    }
//  };


  public EHCachingConfigSource(final ConfigSource underlying, final CacheManager cacheManager) {
    super(underlying, cacheManager);

    EHCacheUtils.addCache(cacheManager, CACHE_NAME);
    _cache = EHCacheUtils.getCacheFromManager(cacheManager, CACHE_NAME);
  }


  public void shutdown() {
    super.shutdown();
    _manager.removeCache(CACHE_NAME);
  }

  protected void cleanCaches(ObjectId oid, Instant versionFrom, Instant versionTo) {
    Map<VersionCorrection, ConfigItem<?>> items = getObjectIdCacheEntry(oid);
    for (Map.Entry<VersionCorrection, ConfigItem<?>> itemEntry : items.entrySet()) {
      if (isVersionCorrection(itemEntry.getKey(), versionFrom, versionTo, null, null)) {
        ConfigItem item = itemEntry.getValue();
        _cache.remove(Pair.of(item.getType(), item.getName()));
      }
    }
    super.cleanCaches(oid, versionFrom, versionTo);
  }

  protected Cache getCache() {
    return _cache;
  }
  

  @Override
  public <T> ConfigItem<T> get(Class<T> clazz, String configName, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(versionCorrection.getVersionAsOf(), "versionCorrection.getVersionAsOf()");
    ArgumentChecker.notNull(versionCorrection.getCorrectedTo(), "versionCorrection.getCorrectedTo()");
    final Object key = Pair.of(clazz, configName);
    final Element e = getCache().get(key);
    if (e != null) {
      List<ConfigItem<T>> items = EHCacheUtils.get(e);
      Collection<? extends ConfigItem<T>> filtered = functional(items).filter(new Function1<ConfigItem<T>, Boolean>() {
        @Override
        public Boolean execute(ConfigItem<T> configItem) {
          return isVersionCorrection(versionCorrection, configItem.getVersionFromInstant(), configItem.getVersionToInstant(), configItem.getCorrectionFromInstant(), configItem.getCorrectionToInstant());
        }
      }).asCollection();
      if (filtered.size() > 1) {
        throw new RuntimeException("Versions/Corrections should not overlap");
      }
      ConfigItem<T> item = functional(filtered).first();
      if (item != null) {
        return item;
      } else {
        try {
          item = getUnderlying().get(clazz, configName, versionCorrection);
          List<ConfigItem<T>> newItems = new ArrayList<ConfigItem<T>>(items);
          newItems.add(item);
          /* reinsert collection */
          putValues(key, newItems, getCache());
          return item;
        } catch (RuntimeException ex) {
          return EHCacheUtils.putException(key, ex, getCache());
        }
      }
    }
    try {
      ConfigItem<T> item = getUnderlying().get(clazz, configName, versionCorrection);
      List<ConfigItem<T>> items = Collections.singletonList(item);
      putValues(key, items, getCache());
      return item;
    } catch (RuntimeException ex) {
      return EHCacheUtils.putException(key, ex, getCache());
    }
  }

  @Override
  public <T> Collection<ConfigItem<T>> getAll(Class<T> clazz, VersionCorrection versionCorrection) {
    return getUnderlying().getAll(clazz, versionCorrection);
  }

  @Override
  public <T> T getLatest(Class<T> clazz, String name) {
    return getUnderlying().get(clazz, name, VersionCorrection.LATEST).getValue();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getConfig(Class<T> clazz, ObjectId objectId, VersionCorrection versionCorrection) {
    GenericType<T> type = new GenericType<T>() {
    };
    Object value = get(objectId, versionCorrection).getValue();
    if (type.getRawClass().isAssignableFrom(value.getClass())) {
      return (T) value;
    } else {
      throw new RuntimeException("The requested object type is not " + type.getRawClass());
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getConfig(Class<T> clazz, UniqueId uniqueId) {
    GenericType<T> type = new GenericType<T>() {
    };
    Object value = get(uniqueId).getValue();
    if (type.getRawClass().isAssignableFrom(value.getClass())) {
      return (T) value;
    } else {
      throw new RuntimeException("The requested object type is not " + type.getRawClass());
    }
  }

  @Override
  public <T> T getConfig(Class<T> clazz, String configName, VersionCorrection versionCorrection) {
    return get(clazz, configName, versionCorrection).getValue();
  }
}
