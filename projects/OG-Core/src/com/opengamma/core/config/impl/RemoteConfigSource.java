/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.config.impl;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import com.opengamma.core.AbstractRemoteSource;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.sun.jersey.api.client.GenericType;

/**
 * Provides remote access to a {@link ConfigSource}.
 */
public class RemoteConfigSource extends AbstractRemoteSource<ConfigItem<?>> implements ConfigSource {

  /**
   * The change manager.
   */
  private final ChangeManager _changeManager;

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteConfigSource(final URI baseUri) {
    super(baseUri);
    _changeManager = new BasicChangeManager();
  }

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   * @param changeManager  the change manager to use, not null
   */
  public RemoteConfigSource(final URI baseUri, final ChangeManager changeManager) {
    super(baseUri);
    ArgumentChecker.notNull(changeManager, "changeManager");
    _changeManager = changeManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

  @Override
  public ConfigItem<?> get(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    URI uri = DataConfigSourceResource.uriGet(getBaseUri(), uniqueId);
    return accessRemote(uri).get(ConfigItem.class);
  }

  @Override
  public ConfigItem<?> get(ObjectId objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");

    URI uri = DataConfigSourceResource.uriGet(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(ConfigItem.class);
  }

  //-------------------------------------------------------------------------
  @Override
  @SuppressWarnings("unchecked")
  public <R> R getConfig(Class<R> clazz, UniqueId uniqueId) {
    GenericType<R> type = new GenericType<R>() {
    };
    Object value = get(uniqueId).getValue();
    if (type.getRawClass().isAssignableFrom(value.getClass())) {
      return (R) value;
    } else {
      throw new RuntimeException("The requested object type is not " + type.getRawClass());
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <R> R getConfig(Class<R> clazz, ObjectId objectId, VersionCorrection versionCorrection) {
    GenericType<R> type = new GenericType<R>() {
    };
    Object value = get(objectId, versionCorrection).getValue();
    if (type.getRawClass().isAssignableFrom(value.getClass())) {
      return (R) value;
    } else {
      throw new RuntimeException("The requested object type is not " + type.getRawClass());
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public <R> R getConfig(Class<R> clazz, String configName, VersionCorrection versionCorrection) {
    return get(clazz, configName, versionCorrection).getValue();
  }

  @Override
  public <R> ConfigItem<R> get(Class<R> clazz, String configName, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(configName, "configName");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    
    URI uri = DataConfigSourceResource.uriSearchSingle(getBaseUri(), configName, versionCorrection, clazz);
    GenericType<ConfigItem<R>> gt = new GenericType<ConfigItem<R>>() {
    };
    return accessRemote(uri).get(gt.getRawClass());
  }

  @Override
  public <R> R getLatestByName(Class<R> clazz, String name) {
    return getConfig(clazz, name, VersionCorrection.LATEST);
  }

  @Override
  public <R> Collection<ConfigItem<R>> getAll(Class<R> clazz, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    
    URI uri = DataConfigSourceResource.uriSearch(getBaseUri(), clazz, versionCorrection);
    
    GenericType<List<ConfigItem<R>>> gt = new GenericType<List<ConfigItem<R>>>() { };
    return accessRemote(uri).get(gt.getRawClass());
  }

}
