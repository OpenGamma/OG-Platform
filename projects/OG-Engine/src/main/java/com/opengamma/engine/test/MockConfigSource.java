/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.test;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.AbstractSource;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * A mock config source for testing.
 */
public class MockConfigSource extends AbstractSource<ConfigItem<?>> implements ConfigSource {

  /**
   * The map of data.
   */
  private final Map<ObjectId, ConfigItem<?>> _store = newHashMap();
  /**
   * The change manager.
   */
  private final ChangeManager _changeManager = new BasicChangeManager();

  @SuppressWarnings("unchecked")
  @Override
  public <T> Collection<ConfigItem<T>> get(final Class<T> clazz, final String configName, final VersionCorrection versionCorrection) {
    final Collection<ConfigItem<T>> result = new ArrayList<ConfigItem<T>>();
    for (final ConfigItem configItem : _store.values()) {
      if (clazz.isAssignableFrom(configItem.getType()) && configItem.getName().equals(configName)) {
        result.add(configItem);
      }
    }
    return result;
  }

  @Override
  public ConfigItem<?> get(final UniqueId uniqueId) {
    final ConfigItem<?> item = _store.get(uniqueId.getObjectId());
    if (item != null) {
      return item;
    } else {
      throw new DataNotFoundException(uniqueId.toString());
    }
  }

  @Override
  public ConfigItem<?> get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    for (final ConfigItem<?> configItem : _store.values()) {
      if (configItem.getObjectId().equals(objectId)) {
        return configItem;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R> Collection<ConfigItem<R>> getAll(final Class<R> clazz, final VersionCorrection versionCorrection) {
    final List<ConfigItem<R>> list = newArrayList();
    for (final ConfigItem<?> configItem : _store.values()) {
      if (clazz.isAssignableFrom(configItem.getType())) {
        list.add((ConfigItem<R>) configItem);
      }
    }
    return list;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R> R getConfig(final Class<R> clazz, final UniqueId uniqueId) {
    return (R) get(uniqueId).getValue();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R> R getConfig(final Class<R> clazz, final ObjectId objectId, final VersionCorrection versionCorrection) {
    return (R) get(objectId, versionCorrection).getValue();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R> R getSingle(final Class<R> clazz, final String configName, final VersionCorrection versionCorrection) {
    for (final ConfigItem<?> configItem : _store.values()) {
      if (clazz.isAssignableFrom(configItem.getType()) && configItem.getName().equals(configName)) {
        return (R) configItem.getValue();
      }
    }
    return null;
  }

  @Override
  public <R> R getLatestByName(final Class<R> clazz, final String name) {
    return getSingle(clazz, name, VersionCorrection.LATEST);
  }

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

  public ConfigItem<ViewDefinition> put(final ViewDefinition viewDefinition) {
    // REVIEW 2012-11-28 Andrew -- This shouldn't be specific to view definition
    final ConfigItem<ViewDefinition> item = ConfigItem.of(viewDefinition);
    if (item.getValue().getUniqueId() == null) {
      item.getValue().setUniqueId(UniqueId.of(ViewDefinition.class.getName(), item.getValue().getName()));
    }
    _store.put(viewDefinition.getUniqueId().getObjectId(), item);
    // TODO: should probably notify the change manager
    return item;
  }

}
