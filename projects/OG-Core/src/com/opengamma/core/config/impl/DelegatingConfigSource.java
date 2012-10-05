/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.config.impl;

import java.util.*;

import com.opengamma.core.change.AggregatingChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSchemeDelegator;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A source of positions that uses the scheme of the unique identifier to determine which
 * underlying source should handle the request.
 * <p>
 * If no scheme-specific handler has been registered, a default is used.
 * <p>
 * Change events are aggregated from the different sources and presented through a single change manager.
 */
public class DelegatingConfigSource extends UniqueIdSchemeDelegator<ConfigSource> implements ConfigSource {

  /**
   * The change manager.
   */
  private final ChangeManager _changeManager;

  /**
   * Creates an instance specifying the default delegate.
   *
   * @param defaultSource  the source to use when no scheme matches, not null
   */
  public DelegatingConfigSource(ConfigSource defaultSource) {
    super(defaultSource);
    _changeManager = defaultSource.changeManager();
  }

  /**
   * Creates an instance specifying the default delegate.
   *
   * @param defaultSource  the source to use when no scheme matches, not null
   * @param schemePrefixToSourceMap  the map of sources by scheme to switch on, not null
   */
  public DelegatingConfigSource(ConfigSource defaultSource, Map<String, ConfigSource> schemePrefixToSourceMap) {
    super(defaultSource, schemePrefixToSourceMap);
    AggregatingChangeManager changeManager = new AggregatingChangeManager();

    // REVIEW jonathan 2011-08-03 -- this assumes that the delegating source lasts for the lifetime of the engine as we
    // never detach from the underlying change managers.
    changeManager.addChangeManager(defaultSource.changeManager());
    for (ConfigSource source : schemePrefixToSourceMap.values()) {
      changeManager.addChangeManager(source.changeManager());
    }
    _changeManager = changeManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigItem<?> get(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    return chooseDelegate(uniqueId.getScheme()).get(uniqueId);
  }

  @Override
  public ConfigItem<?> get(ObjectId objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    return chooseDelegate(objectId.getScheme()).get(objectId, versionCorrection);
  }

  @Override
  public <T> ConfigItem<T> get(Class<T> clazz, String configName, VersionCorrection versionCorrection) {
    Map<String, ConfigSource> delegates = getDelegates();
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(configName, "configName");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    for (ConfigSource configSource : delegates.values()) {
      ConfigItem<T> config = configSource.get(clazz, configName, versionCorrection);
      if (config != null) {
        return config;
      }
    }
    return getDefaultDelegate().get(clazz, configName, versionCorrection);
  }

  @Override
  public <T> Collection<ConfigItem<T>> getAll(Class<T> clazz, VersionCorrection versionCorrection) {
    Map<String, ConfigSource> delegates = getDelegates();
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    for (ConfigSource configSource : delegates.values()) {
      Collection<ConfigItem<T>> config = configSource.getAll(clazz, versionCorrection);
      if (config != null && config.size() > 0) {
        return config;
      }
    }
    return getDefaultDelegate().getAll(clazz, versionCorrection);
  }

  @Override
  public <T> T getConfig(Class<T> clazz, UniqueId uniqueId) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    return chooseDelegate(uniqueId.getScheme()).getConfig(clazz, uniqueId);
  }

  @Override
  public <T> T getConfig(Class<T> clazz, ObjectId objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    return chooseDelegate(objectId.getScheme()).getConfig(clazz, objectId, versionCorrection);
  }

  @Override
  public <T> T getConfig(Class<T> clazz, String configName, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(configName, "configName");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");

    Map<String, ConfigSource> delegates = getDelegates();
    for (ConfigSource configSource : delegates.values()) {
      T config = configSource.getConfig(clazz, configName, versionCorrection);
      if (config != null) {
        return config;
      }
    }
    return getDefaultDelegate().getConfig(clazz, configName, versionCorrection);
  }

  @Override
  public <T> T getLatestByName(Class<T> clazz, String name) {
    return getConfig(clazz, name, VersionCorrection.LATEST);
  }

  @Override
  public ChangeManager changeManager() {
    Map<String, ConfigSource> delegates = getDelegates();
    List<ChangeProvider> changeProviders = new ArrayList<ChangeProvider>();
    for (ConfigSource configSource : delegates.values()) {
      changeProviders.add(configSource);
    }
    return new AggregatingChangeManager(changeProviders);
  }

  @Override
  public Map<UniqueId, ConfigItem<?>> get(Collection<UniqueId> uniqueIds) {
    Map<String, ConfigSource> delegates = getDelegates();
    ArgumentChecker.notNull(uniqueIds, "uniqueIds");
    Map<UniqueId, ConfigItem<?>> map = new HashMap<UniqueId, ConfigItem<?>>();
    for (UniqueId uniqueId : uniqueIds) {
      map.put(uniqueId, get(uniqueId));
    }
    return map;
  }
}
