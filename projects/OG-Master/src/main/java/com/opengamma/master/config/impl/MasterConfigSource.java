/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import static com.google.common.collect.Maps.newHashMap;
import static com.opengamma.util.functional.Functional.functional;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.VersionedSource;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;

/**
 * A {@code ConfigSource} implemented using an underlying {@code ConfigMaster}.
 * <p>
 * The {@link ConfigSource} interface provides securities to the engine via a narrow API.
 * This class provides the source on top of a standard {@link ConfigMaster}.
 * <p>
 * This implementation supports the concept of fixing the version.
 * This allows the version to be set in the constructor, and applied automatically to the methods.
 * Some methods on {@code ConfigSource} specify their own version requirements, which are respected.
 */
@PublicSPI
public class MasterConfigSource implements ConfigSource, VersionedSource {

  /**
   * The config master.
   */
  private final ConfigMaster _configMaster;
  /**
   * The version-correction locator to search at, null to not override versions.
   */
  private volatile VersionCorrection _versionCorrection;

  /**
   * The change manager.
   */
  private ChangeManager _changeManager = new BasicChangeManager();

  /**
   * Creates an instance with an underlying config master which does not override versions.
   *
   * @param configMaster  the config master, not null
   */
  public MasterConfigSource(final ConfigMaster configMaster) {
    this(configMaster, null);
  }

  /**
   * Creates an instance with an underlying config master optionally overriding the requested version.
   *
   * @param configMaster  the config master, not null
   * @param versionCorrection  the version-correction locator to search at, null to not override versions
   */
  public MasterConfigSource(final ConfigMaster configMaster, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(configMaster, "configMaster");
    _configMaster = configMaster;
    _versionCorrection = versionCorrection;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying config master.
   *
   * @return the config master, not null
   */
  public ConfigMaster getMaster() {
    return _configMaster;
  }

  /**
   * Gets the version-correction locator to search at.
   *
   * @return the version-correction locator to search at, null if not overriding versions
   */
  public VersionCorrection getVersionCorrection() {
    return _versionCorrection;
  }

  /**
   * Gets the change manager.
   *
   * @return the change manager, not null
   */
  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

  /**
   * Sets the change manager.
   *
   * @param changeManager  the change manager, not null
   */
  public void setChangeManager(final ChangeManager changeManager) {
    ArgumentChecker.notNull(changeManager, "changeManager");
    _changeManager = changeManager;
  }

  /**
   * Sets the version-correction locator to search at.
   *
   * @param versionCorrection  the version-correction locator to search at, null to not override versions
   */
  @Override
  public void setVersionCorrection(final VersionCorrection versionCorrection) {
    _versionCorrection = versionCorrection;
  }

  //-------------------------------------------------------------------------
  /**
   * Search for configuration elements using a request object.
   *
   * @param <R>  the type of configuration element
   * @param request  the request object with value for search fields, not null
   * @return all configuration elements matching the request, not null
   */
  public <R> List<ConfigItem<R>> search(final ConfigSearchRequest<R> request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getType(), "request.type");
    request.setVersionCorrection(getVersionCorrection());
    ConfigSearchResult<R> searchResult = getMaster().search(request);
    return searchResult.getValues();
  }

  //-------------------------------------------------------------------------
  @Override
  @SuppressWarnings("unchecked")
  public <R> R getConfig(Class<R> clazz, UniqueId uniqueId) {
    ConfigItem<?> item = getMaster().get(uniqueId).getConfig();
    if (clazz.isAssignableFrom(item.getType())) {
      return (R) item.getValue();
    } else {
      return null;
    }
  }

  @Override
  public ConfigItem<?> get(ObjectId objectId, VersionCorrection versionCorrection) {
    return getMaster().get(objectId, versionCorrection).getConfig();
  }

  @Override
  public ConfigItem<?> get(UniqueId uniqueId) {
    return getMaster().get(uniqueId).getConfig();
  }

  @Override
  public <R> R getConfig(Class<R> clazz, String configName, VersionCorrection versionCorrection) {
    ConfigItem<R> result = get(clazz, configName, versionCorrection);
    if (result != null) {
      return result.getValue();
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R> R getConfig(Class<R> clazz, ObjectId objectId, VersionCorrection versionCorrection) {
    ConfigItem<?> item = getMaster().get(objectId, versionCorrection).getConfig();
    if (clazz.isAssignableFrom(item.getType())) {
      return (R) item.getValue();
    } else {
      return null;
    }
  }

  @Override
  public <R> ConfigItem<R> get(Class<R> clazz, String configName, VersionCorrection versionCorrection) {
    ConfigSearchRequest<R> searchRequest = new ConfigSearchRequest<R>(clazz);
    searchRequest.setName(configName);
    searchRequest.setVersionCorrection(versionCorrection);
    return functional(getMaster().search(searchRequest).getValues()).first();
  }

  @Override
  public <R> Collection<ConfigItem<R>> getAll(Class<R> clazz, VersionCorrection versionCorrection) {    
    ConfigSearchRequest<R> searchRequest = new ConfigSearchRequest<R>(clazz);
    searchRequest.setType(clazz);
    searchRequest.setVersionCorrection(versionCorrection);
    return getMaster().search(searchRequest).getValues();
  }

  @Override
  public <R> R getLatestByName(Class<R> clazz, String name) {
    return getConfig(clazz, name, VersionCorrection.LATEST);
  }

  @Override
  public Map<UniqueId, ConfigItem<?>> get(Collection<UniqueId> uniqueIds) {
    Map<UniqueId, ConfigDocument> result = getMaster().get(uniqueIds);
    Map<UniqueId, ConfigItem<?>> map = newHashMap();
    for (UniqueId uid : result.keySet()) {
      map.put(uid, result.get(uid).getConfig());
    }
    return map;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    String str = "MasterConfigSource[" + getMaster();
    if (getVersionCorrection() != null) {
      str += ",versionCorrection=" + getVersionCorrection();
    }
    return str + "]";
  }

}
