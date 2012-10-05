/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.config.impl;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.AbstractRemoteSource;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.UniformInterfaceException404NotFound;
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
   */
  public RemoteConfigSource(final URI baseUri, final ChangeManager changeManager) {
    super(baseUri);
    _changeManager = changeManager;
  }

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }


  @Override
  public <T> T getConfig(Class<T> clazz, String configName, VersionCorrection versionCorrection) {
    return get(clazz, configName, versionCorrection).getValue();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getConfig(Class<T> clazz, UniqueId uniqueId) {
    GenericType<T> type = new GenericType<T>() {
    };
    Object value = get(uniqueId).getValue();
    if (type.getRawClass().isAssignableFrom(value.getClass())) {
      return (T) value;
    } else {
      throw new RuntimeException("The requested object type is not " + type.getRawClass());
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getConfig(Class<T> clazz, ObjectId objectId, VersionCorrection versionCorrection) {
    GenericType<T> type = new GenericType<T>() {
    };
    Object value = get(objectId, versionCorrection).getValue();
    if (type.getRawClass().isAssignableFrom(value.getClass())) {
      return (T) value;
    } else {
      throw new RuntimeException("The requested object type is not " + type.getRawClass());
    }
  }

  @Override
  public ConfigItem get(ObjectId objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");

    URI uri = DataConfigSourceResource.uriGet(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(ConfigItem.class);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> ConfigItem<T> get(Class<T> clazz, String configName, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(configName, "configName");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");

    URI uri = DataConfigSourceResource.uriSearchSingle(getBaseUri(), configName, versionCorrection, clazz);
    GenericType<ConfigItem<T>> gt = new GenericType<ConfigItem<T>>() {
    };
    return accessRemote(uri).get(gt.getRawClass());
  }

  @Override
  public <T> T getLatestByName(Class<T> clazz, String name) {
    return getConfig(clazz, name, VersionCorrection.LATEST);
  }

  @Override
  public <T> Collection<ConfigItem<T>> getAll(Class<T> clazz, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");

    URI uri = DataConfigSourceResource.uriSearch(getBaseUri(), clazz, versionCorrection);

    GenericType<List<ConfigItem<T>>> gt = new GenericType<List<ConfigItem<T>>>() {
    };
    return accessRemote(uri).get(gt.getRawClass());
  }

  @Override
  public ConfigItem<?> get(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    URI uri = DataConfigSourceResource.uriGet(getBaseUri(), uniqueId);
    return accessRemote(uri).get(ConfigItem.class);
  }
}
