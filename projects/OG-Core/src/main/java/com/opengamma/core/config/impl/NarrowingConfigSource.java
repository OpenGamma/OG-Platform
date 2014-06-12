/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.config.impl;

import java.util.Collection;
import java.util.Map;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A config source that delegates to an another source, but which ensures that
 * it only calls the get methods on the delegate. This is intended to allow
 * the use of proxy classes as the delegates which allows different
 * behaviours e.g. capturing the data returned from sources.
 */
public class NarrowingConfigSource implements ConfigSource {

  private final ConfigSource _delegate;

  /**
   * Create a narrowing source, wrapping the provided source.
   *
   * @param delegate the source to delegate to, not null
   */
  public NarrowingConfigSource(ConfigSource delegate) {
    _delegate = ArgumentChecker.notNull(delegate, "delegate");
  }

  @Override
  public ConfigItem<?> get(UniqueId uniqueId) {
    return _delegate.get(uniqueId);
  }

  @Override
  public ConfigItem<?> get(ObjectId objectId, VersionCorrection versionCorrection) {
    return _delegate.get(objectId, versionCorrection);
  }

  @Override
  public Map<UniqueId, ConfigItem<?>> get(Collection<UniqueId> uniqueIds) {
    return _delegate.get(uniqueIds);
  }

  @Override
  public Map<ObjectId, ConfigItem<?>> get(Collection<ObjectId> objectIds, VersionCorrection versionCorrection) {
    return _delegate.get(objectIds, versionCorrection);
  }

  @Override
  public <R> Collection<ConfigItem<R>> get(Class<R> clazz, String configName, VersionCorrection versionCorrection) {
    return _delegate.get(clazz, configName, versionCorrection);
  }

  @Override
  public <R> Collection<ConfigItem<R>> getAll(Class<R> clazz, VersionCorrection versionCorrection) {
    throw new UnsupportedOperationException("This method should not be used");
  }

  @Override
  @SuppressWarnings("unchecked")
  public <R> R getConfig(Class<R> clazz, UniqueId uniqueId) {
    return checkAndCast(clazz, get(uniqueId));
  }

  @Override
  public <R> R getConfig(Class<R> clazz, ObjectId objectId, VersionCorrection versionCorrection) {
    return checkAndCast(clazz, get(objectId, versionCorrection));
  }

  private <R> R checkAndCast(Class<R> clazz, ConfigItem<?> item) {
    return clazz.isAssignableFrom(item.getType()) ? clazz.cast(item.getValue()) : null;
  }

  @Override
  public <R> R getSingle(Class<R> clazz, String configName, VersionCorrection versionCorrection) {
    Collection<ConfigItem<R>> result = get(clazz, configName, versionCorrection);
    return result.isEmpty() ? null : result.iterator().next().getValue();
  }

  @Override
  public <R> R getLatestByName(Class<R> clazz, String name) {
    return getSingle(clazz, name, VersionCorrection.LATEST);
  }

  @Override
  public ChangeManager changeManager() {
    return _delegate.changeManager();
  }
}
