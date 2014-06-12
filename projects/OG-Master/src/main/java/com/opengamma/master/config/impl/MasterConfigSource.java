/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.opengamma.core.AbstractSource;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;

/**
 * A {@code ConfigSource} implemented using an underlying {@code ConfigMaster}.
 * <p>
 * The {@link ConfigSource} interface provides securities to the engine via a narrow API. This class provides the source on top of a standard {@link ConfigMaster}.
 */
@PublicSPI
public class MasterConfigSource extends AbstractSource<ConfigItem<?>> implements ConfigSource {

  /**
   * The config master.
   */
  private final ConfigMaster _configMaster;

  /**
   * Creates an instance with an underlying config master.
   * 
   * @param configMaster the config master, not null
   */
  public MasterConfigSource(final ConfigMaster configMaster) {
    ArgumentChecker.notNull(configMaster, "configMaster");
    _configMaster = configMaster;
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
   * Gets the change manager.
   * 
   * @return the change manager, not null
   */
  @Override
  public ChangeManager changeManager() {
    return getMaster().changeManager();
  }

  //-------------------------------------------------------------------------
  /**
   * Search for configuration elements using a request object.
   * 
   * @param <R> the type of configuration element
   * @param request the request object with value for search fields, not null
   * @return all configuration elements matching the request, not null
   */
  public <R> List<ConfigItem<R>> search(final ConfigSearchRequest<R> request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getType(), "request.type");
    final ConfigSearchResult<R> searchResult = getMaster().search(request);
    return searchResult.getValues();
  }

  //-------------------------------------------------------------------------
  @Override
  @SuppressWarnings("unchecked")
  public <R> R getConfig(final Class<R> clazz, final UniqueId uniqueId) {
    final ConfigItem<?> item = getMaster().get(uniqueId).getConfig();
    if (clazz.isAssignableFrom(item.getType())) {
      return (R) item.getValue();
    } else {
      return null;
    }
  }

  @Override
  public ConfigItem<?> get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    return getMaster().get(objectId, versionCorrection).getConfig();
  }

  @Override
  public ConfigItem<?> get(final UniqueId uniqueId) {
    return getMaster().get(uniqueId).getConfig();
  }

  @Override
  public <R> R getSingle(final Class<R> clazz, final String configName, final VersionCorrection versionCorrection) {
    final Collection<ConfigItem<R>> result = get(clazz, configName, versionCorrection);
    if (!result.isEmpty()) {
      return result.iterator().next().getValue();
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R> R getConfig(final Class<R> clazz, final ObjectId objectId, final VersionCorrection versionCorrection) {
    final ConfigItem<?> item = getMaster().get(objectId, versionCorrection).getConfig();
    if (clazz.isAssignableFrom(item.getType())) {
      return (R) item.getValue();
    } else {
      return null;
    }
  }

  @Override
  public <R> Collection<ConfigItem<R>> get(final Class<R> clazz, final String configName, final VersionCorrection versionCorrection) {
    final ConfigSearchRequest<R> searchRequest = new ConfigSearchRequest<R>(clazz);
    searchRequest.setName(configName);
    searchRequest.setVersionCorrection(versionCorrection);
    return getMaster().search(searchRequest).getValues();
  }

  @Override
  public <R> Collection<ConfigItem<R>> getAll(final Class<R> clazz, final VersionCorrection versionCorrection) {
    final ConfigSearchRequest<R> searchRequest = new ConfigSearchRequest<R>(clazz);
    searchRequest.setType(clazz);
    searchRequest.setVersionCorrection(versionCorrection);
    return getMaster().search(searchRequest).getValues();
  }

  @Override
  public <R> R getLatestByName(final Class<R> clazz, final String name) {
    return getSingle(clazz, name, VersionCorrection.LATEST);
  }

  @Override
  public Map<UniqueId, ConfigItem<?>> get(final Collection<UniqueId> uniqueIds) {
    final Map<UniqueId, ConfigDocument> result = getMaster().get(uniqueIds);
    final Map<UniqueId, ConfigItem<?>> map = newHashMap();
    for (final UniqueId uid : result.keySet()) {
      map.put(uid, result.get(uid).getConfig());
    }
    return map;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "MasterConfigSource[" + getMaster() + "]";
  }

}
