/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.config.impl;

import java.util.Collection;
import java.util.Map;

import org.threeten.bp.Instant;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A {@link ConfigSource} wrapper which sets a specific version/correction on all requests that would otherwise request "latest".
 * <p>
 * Where possible, code should be written that explicitly passes the necessary version/correction information around - this is an intermediate solution for working with existing code that is not
 * properly version aware.
 * 
 * @deprecated Call code that is properly version aware (whenever possible)
 */
@Deprecated
public class VersionLockedConfigSource implements ConfigSource {

  private final ConfigSource _underlying;
  private final VersionCorrection _versionCorrection;

  public VersionLockedConfigSource(final ConfigSource underlying, final VersionCorrection versionCorrection) {
    _underlying = ArgumentChecker.notNull(underlying, "underlying");
    _versionCorrection = ArgumentChecker.notNull(versionCorrection, "versionCorrection");
  }

  protected ConfigSource getUnderlying() {
    return _underlying;
  }

  protected VersionCorrection getVersionCorrection() {
    return _versionCorrection;
  }

  protected VersionCorrection lockVersionCorrection(final VersionCorrection versionCorrection) {
    if (versionCorrection.containsLatest()) {
      final Instant version = (versionCorrection.getVersionAsOf() == null) ? getVersionCorrection().getVersionAsOf() : versionCorrection.getVersionAsOf();
      final Instant correction = (versionCorrection.getCorrectedTo() == null) ? getVersionCorrection().getCorrectedTo() : versionCorrection.getCorrectedTo();
      return VersionCorrection.of(version, correction);
    } else {
      return versionCorrection;
    }
  }

  @Override
  public Map<UniqueId, ConfigItem<?>> get(Collection<UniqueId> uniqueIds) {
    return getUnderlying().get(uniqueIds);
  }

  @Override
  public Map<ObjectId, ConfigItem<?>> get(Collection<ObjectId> objectIds, VersionCorrection versionCorrection) {
    return getUnderlying().get(objectIds, lockVersionCorrection(versionCorrection));
  }

  @Override
  public ChangeManager changeManager() {
    return getUnderlying().changeManager();
  }

  @Override
  public ConfigItem<?> get(UniqueId uniqueId) {
    return getUnderlying().get(uniqueId);
  }

  @Override
  public ConfigItem<?> get(ObjectId objectId, VersionCorrection versionCorrection) {
    return getUnderlying().get(objectId, lockVersionCorrection(versionCorrection));
  }

  @Override
  public <R> Collection<ConfigItem<R>> get(Class<R> clazz, String configName, VersionCorrection versionCorrection) {
    return getUnderlying().get(clazz, configName, lockVersionCorrection(versionCorrection));
  }

  @Override
  public <R> Collection<ConfigItem<R>> getAll(Class<R> clazz, VersionCorrection versionCorrection) {
    return getUnderlying().getAll(clazz, lockVersionCorrection(versionCorrection));
  }

  @Override
  public <R> R getConfig(Class<R> clazz, UniqueId uniqueId) {
    return getUnderlying().getConfig(clazz, uniqueId);
  }

  @Override
  public <R> R getConfig(Class<R> clazz, ObjectId objectId, VersionCorrection versionCorrection) {
    return getUnderlying().getConfig(clazz, objectId, lockVersionCorrection(versionCorrection));
  }

  @Override
  public <R> R getSingle(Class<R> clazz, String configName, VersionCorrection versionCorrection) {
    return getUnderlying().getSingle(clazz, configName, lockVersionCorrection(versionCorrection));
  }

  @Override
  public <R> R getLatestByName(Class<R> clazz, String name) {
    return getUnderlying().getSingle(clazz, name, getVersionCorrection());
  }

}
