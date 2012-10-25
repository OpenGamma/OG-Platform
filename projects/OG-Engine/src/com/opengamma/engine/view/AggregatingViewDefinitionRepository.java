/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.AggregatingChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A view definition repository which represents one or more others. When retrieving a view definition, repositories
 * are searched in the order that they were added and the first matching definition is returned.
 * <p>
 * This is temporary, to be replaced once view definitions have proper identifiers.
 */
public class AggregatingViewDefinitionRepository implements ConfigSource {

  private final Set<ConfigSource> _repositories = new CopyOnWriteArraySet<ConfigSource>();
  private final AggregatingChangeManager _changeManager = new AggregatingChangeManager();

  public AggregatingViewDefinitionRepository(Collection<ConfigSource> repositories) {
    for (ConfigSource repository : repositories) {
      addRepository(repository);
    }
  }

  public void addRepository(ConfigSource repository) {
    ArgumentChecker.notNull(repository, "repository");
    _repositories.add(repository);
    _changeManager.addChangeManager(repository.changeManager());
  }

  @Override
  public <R> ConfigItem<R> get(Class<R> clazz, String configName, VersionCorrection versionCorrection) {
    for (ConfigSource repository : _repositories) {
      try {
        ConfigItem<R> result = repository.get(clazz, configName, versionCorrection);
        if (result != null) {
          return result;
        }
      } catch (DataNotFoundException e) {
        // move on
      }
    }
    return null;
  }

  @Override
  public ConfigItem<?> get(UniqueId uniqueId) {
    for (ConfigSource repository : _repositories) {
      try {
        return repository.get(uniqueId);
      } catch (DataNotFoundException e) {
        // move on
      }
    }
    return null;
  }

  @Override
  public ConfigItem<?> get(ObjectId objectId, VersionCorrection versionCorrection) {
    for (ConfigSource repository : _repositories) {
      try {
        ConfigItem<?> result = repository.get(objectId, versionCorrection);
        if (result != null) {
          return result;
        }
      } catch (DataNotFoundException e) {
        // move on
      }
    }
    return null;
  }

  @Override
  public <R> Collection<ConfigItem<R>> getAll(Class<R> clazz, VersionCorrection versionCorrection) {
    for (ConfigSource repository : _repositories) {
      Collection<ConfigItem<R>> result = repository.getAll(clazz, versionCorrection);
      if (result != null && !result.isEmpty()) {
        return result;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R> R getConfig(Class<R> clazz, UniqueId uniqueId) {
    return (R) get(uniqueId);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R> R getConfig(Class<R> clazz, ObjectId objectId, VersionCorrection versionCorrection) {
    return (R) get(objectId, versionCorrection);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R> R getConfig(Class<R> clazz, String configName, VersionCorrection versionCorrection) {
    return (R) get(clazz, configName, versionCorrection);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R> R getLatestByName(Class<R> clazz, String name) {
    return (R) get(clazz, name, VersionCorrection.LATEST);
  }

  @Override
  public Map<UniqueId, ConfigItem<?>> get(Collection<UniqueId> uniqueIds) {
    for (ConfigSource repository : _repositories) {
      Map<UniqueId, ConfigItem<?>> result = repository.get(uniqueIds);
      if (result != null && !result.isEmpty()) {
        return result;
      }
    }
    return null;
  }

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

}
