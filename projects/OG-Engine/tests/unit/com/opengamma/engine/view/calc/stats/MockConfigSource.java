/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc.stats;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.opengamma.id.IdUtils.isVersionCorrection;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

public class MockConfigSource implements ConfigSource {

  Map<ObjectId, ConfigItem<?>> _store = newHashMap();


  ChangeManager _changeManager = new BasicChangeManager();
  
  @SuppressWarnings("unchecked")
  @Override
  public <T> ConfigItem<T> get(Class<T> clazz, String configName, VersionCorrection versionCorrection) {
    for (ConfigItem<?> configItem : _store.values()) {
      if (clazz.isAssignableFrom(configItem.getType()) &&
        configItem.getName().equals(configName) &&
        isVersionCorrection(versionCorrection, configItem.getVersionFromInstant(), configItem.getVersionToInstant(), configItem.getCorrectionFromInstant(), configItem.getCorrectionToInstant()))
        return (ConfigItem<T>) configItem;
    }
    return null;
  }

  @Override
  public ConfigItem<?> get(UniqueId uniqueId) {
    return _store.get(uniqueId);
  }

  @Override
  public ConfigItem<?> get(ObjectId objectId, VersionCorrection versionCorrection) {
    for (ConfigItem<?> configItem : _store.values()) {
      if (configItem.getObjectId().equals(objectId) &&
        isVersionCorrection(versionCorrection, configItem.getVersionFromInstant(), configItem.getVersionToInstant(), configItem.getCorrectionFromInstant(), configItem.getCorrectionToInstant()))
        return configItem;
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> Collection<ConfigItem<T>> getAll(Class<T> clazz, VersionCorrection versionCorrection) {
    List<ConfigItem<T>> list = newArrayList();
    for (ConfigItem<?> configItem : _store.values()) {
      if (clazz.isAssignableFrom(configItem.getType()) &&
        isVersionCorrection(versionCorrection, configItem.getVersionFromInstant(), configItem.getVersionToInstant(), configItem.getCorrectionFromInstant(), configItem.getCorrectionToInstant()))
        list.add((ConfigItem<T>) configItem);
    }
    return list;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getConfig(Class<T> clazz, UniqueId uniqueId) {
    return (T) get(uniqueId).getValue();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getConfig(Class<T> clazz, ObjectId objectId, VersionCorrection versionCorrection) {
    return (T) get(objectId, versionCorrection).getValue();
  }

  @Override
  public <T> T getConfig(Class<T> clazz, String configName, VersionCorrection versionCorrection) {
    return get(clazz, configName, versionCorrection).getValue();
  }

  @Override
  public <T> T getLatest(Class<T> clazz, String configName) {
    return get(clazz, configName, VersionCorrection.LATEST).getValue();
  }

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

  @Override
  public Map<UniqueId, ConfigItem<?>> get(Collection<UniqueId> uniqueIds) {
    Map<UniqueId, ConfigItem<?>> map = newHashMap();
    for (UniqueId uniqueId : uniqueIds) {
      ConfigItem<?> item = get(uniqueId);
      map.put(uniqueId, item);
    }
    return map;
  }

  public ConfigItem<ViewDefinition> put(ViewDefinition viewDefinition) {
    ConfigItem<ViewDefinition> item = ConfigItem.of(viewDefinition);
    _store.put(viewDefinition.getUniqueId().getObjectId(), item);
    return item;
  }
}
