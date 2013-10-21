/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.config.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.opengamma.core.AbstractEHCachingSource;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;
import com.opengamma.util.tuple.Triple;

/**
 * EHCache backed {@link ConfigSource}.
 * <p>
 * The current implementation of {@link #getSingle} is incorrect; it will cache values queried with a version/correction containing "latest". This should only be used if the issues that such caching
 * will cause are of less concern than the performance penalty of not caching the calls at all.
 */
public class EHCachingConfigSource extends AbstractEHCachingSource<ConfigItem<?>, ConfigSource> implements ConfigSource {

  private final String _nameCacheName = getClass().getName() + "-name-cache";
  private final String _classCacheName = getClass().getName() + "-class-cache";

  private final Cache _nameCache;
  private final Cache _classCache;

  public EHCachingConfigSource(final ConfigSource underlying, final CacheManager cacheManager) {
    super(underlying, cacheManager);
    EHCacheUtils.addCache(cacheManager, _nameCacheName);
    EHCacheUtils.addCache(cacheManager, _classCacheName);
    _nameCache = EHCacheUtils.getCacheFromManager(cacheManager, _nameCacheName);
    _classCache = EHCacheUtils.getCacheFromManager(cacheManager, _classCacheName);
    // this is not nice, but it's better than a stale cache.
    getUnderlying().changeManager().addChangeListener(new ChangeListener() {
      @Override
      public void entityChanged(ChangeEvent event) {
        switch (event.getType()) {
          case ADDED:
            break;
          case CHANGED:
            _nameCache.flush();
            _classCache.flush();
            flush();
            break;
          case REMOVED:
            _nameCache.flush();
            _classCache.flush();
            flush();
            break;
          default:
            break;
        }
      }
    });
  }

  private synchronized <R> void cacheNameHit(final Triple<Class<R>, String, VersionCorrection> key, final R value) {
    Element element = _nameCache.get(key);
    if (element == null) {
      // Don't cache the single form if a collection (or another single) has already been written
      _nameCache.put(new Element(key, ConfigItem.of(value, key.getSecond(), key.getFirst())));
    }

  }

  private synchronized <R> void cacheNameHit(final Triple<Class<R>, String, VersionCorrection> key, final Collection<ConfigItem<R>> values) {
    _nameCache.put(new Element(key, values));
  }

  private synchronized <R> void cacheNameMiss(final Triple<Class<R>, String, VersionCorrection> key) {
    Element element = _nameCache.get(key);
    if (element == null) {
      _nameCache.put(new Element(key, Collections.<ConfigItem<?>>emptySet()));
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R> Collection<ConfigItem<R>> get(final Class<R> clazz, final String configName, final VersionCorrection versionCorrection) {
    if (versionCorrection.containsLatest()) {
      // Not cacheable
      return getUnderlying().get(clazz, configName, versionCorrection);
    }
    final Triple<Class<R>, String, VersionCorrection> nameKey = Triple.of(clazz, configName, versionCorrection);
    Element element = _nameCache.get(nameKey);
    if (element != null) {
      if (element.getObjectValue() instanceof Collection) {
        return (Collection<ConfigItem<R>>) element.getObjectValue();
      }
    }
    final Collection<ConfigItem<R>> result;
    final Pair<Class<R>, VersionCorrection> classKey = Pairs.of(clazz, versionCorrection);
    element = _classCache.get(classKey);
    if (element != null) {
      result = new ArrayList<ConfigItem<R>>();
      for (ConfigItem<?> item : (Collection<ConfigItem<?>>) element.getObjectValue()) {
        if (configName.equals(item.getName()) && clazz.isAssignableFrom(item.getValue().getClass())) {
          result.add((ConfigItem<R>) item);
        }
      }
    } else {
      result = getUnderlying().get(clazz, configName, versionCorrection);
    }
    cacheNameHit(nameKey, result);
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R> Collection<ConfigItem<R>> getAll(final Class<R> clazz, final VersionCorrection versionCorrection) {
    if ((versionCorrection == null) || versionCorrection.containsLatest()) {
      // Not cacheable
      return getUnderlying().getAll(clazz, versionCorrection);
    }
    final Pair<Class<R>, VersionCorrection> key = Pairs.of(clazz, versionCorrection);
    final Element element = _classCache.get(key);
    if (element != null) {
      return (Collection<ConfigItem<R>>) element.getObjectValue();
    }
    Collection<ConfigItem<R>> result = getUnderlying().getAll(clazz, versionCorrection);
    if (result == null) {
      result = Collections.<ConfigItem<R>>emptySet();
    }
    _classCache.put(new Element(key, result));
    return result.isEmpty() ? null : result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R> R getConfig(final Class<R> clazz, final UniqueId uniqueId) {
    final Object value = get(uniqueId).getValue();
    if (clazz.isAssignableFrom(value.getClass())) {
      return (R) value;
    } else {
      throw new IllegalArgumentException("The requested object is " + value.getClass() + ", not " + clazz);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R> R getConfig(final Class<R> clazz, final ObjectId objectId, final VersionCorrection versionCorrection) {
    final Object value = get(objectId, versionCorrection).getValue();
    if (clazz.isAssignableFrom(value.getClass())) {
      return (R) value;
    } else {
      throw new IllegalArgumentException("The requested object is " + value.getClass() + ", not " + clazz);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R> R getSingle(final Class<R> clazz, final String configName, final VersionCorrection versionCorrection) {
    // TODO: This isn't strictly cacheable, if versionCorrection contains latest, but performance can be terrible if we don't
    final Triple<Class<R>, String, VersionCorrection> nameKey = Triple.of(clazz, configName, versionCorrection);
    Element element = _nameCache.get(nameKey);
    if (element == null) {
      final Pair<Class<R>, VersionCorrection> classKey = Pairs.of(clazz, versionCorrection);
      element = _classCache.get(classKey);
      if (element != null) {
        for (ConfigItem<?> item : (Collection<ConfigItem<?>>) element.getObjectValue()) {
          if (configName.equals(item.getName()) && clazz.isAssignableFrom(item.getValue().getClass())) {
            final R result = (R) item.getValue();
            cacheNameHit(nameKey, result);
            return result;
          }
        }
        cacheNameMiss(nameKey);
        return null;
      }
      final R result = getUnderlying().getSingle(clazz, configName, versionCorrection);
      if (result != null) {
        cacheNameHit(nameKey, result);
      } else {
        cacheNameMiss(nameKey);
      }
      return result;
    }
    Object value = element.getObjectValue();
    if (value instanceof ConfigItem) {
      final Object result = ((ConfigItem<?>) value).getValue();
      if (clazz.isAssignableFrom(result.getClass())) {
        return (R) result;
      }
    }
    final Collection<ConfigItem<?>> results = (Collection<ConfigItem<?>>) value;
    if (results.isEmpty()) {
      return null;
    }
    value = results.iterator().next().getValue();
    if (clazz.isAssignableFrom(value.getClass())) {
      return (R) value;
    } else {
      throw new IllegalArgumentException("The requested object is " + value.getClass() + ", not " + clazz);
    }
  }

  @Override
  public <R> R getLatestByName(final Class<R> clazz, final String name) {
    return getSingle(clazz, name, VersionCorrection.LATEST);
  }

}

