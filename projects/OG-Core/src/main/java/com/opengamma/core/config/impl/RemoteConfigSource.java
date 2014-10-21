/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.config.impl;

import java.net.URI;
import java.util.Collection;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;

import com.google.common.collect.Lists;
import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.AbstractRemoteSource;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.rest.UniformInterfaceException404NotFound;

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

  @SuppressWarnings("unchecked")
  private <R> Collection<ConfigItem<R>> configItemCollectionResult(final FudgeMsg msg) {
    final Collection<ConfigItem<R>> result = Lists.newArrayListWithCapacity(msg.getNumFields());
    final FudgeDeserializer deserializer = new FudgeDeserializer(OpenGammaFudgeContext.getInstance());
    for (final FudgeField field : msg) {
      result.add(deserializer.fieldValueToObject(ConfigItem.class, field));
    }
    return result;
  }

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

  @Override
  public ConfigItem<?> get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    final URI uri = DataConfigSourceResource.uriGet(getBaseUri(), uniqueId);
    return accessRemote(uri).get(ConfigItem.class);
  }

  @Override
  public ConfigItem<?> get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final URI uri = DataConfigSourceResource.uriGet(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(ConfigItem.class);
  }

  //-------------------------------------------------------------------------
  @Override
  @SuppressWarnings("unchecked")
  public <R> R getConfig(final Class<R> clazz, final UniqueId uniqueId) {
    final Object value = get(uniqueId).getValue();
    if (clazz.isAssignableFrom(value.getClass())) {
      return (R) value;
    } else {
      throw new IllegalArgumentException("The requested object is " + value.getClass() + ", not " + clazz);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <R> R getConfig(final Class<R> clazz, final ObjectId objectId, final VersionCorrection versionCorrection) {
    final Object value = get(objectId, versionCorrection).getValue();
    if (clazz.isAssignableFrom(value.getClass())) {
      return (R) value;
    } else {
      throw new IllegalArgumentException("The requested object is " + value.getClass() + ", not " + clazz);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public <R> R getSingle(final Class<R> clazz, final String configName, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(configName, "configName");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    try {
      final URI uri = DataConfigSourceResource.uriSearchSingle(getBaseUri(), configName, versionCorrection, clazz);
      return accessRemote(uri).get(clazz);
    } catch (UniformInterfaceException404NotFound ex) {
      throw new OpenGammaRuntimeException("Unable to access remote source ", ex);
    }
  }

  @Override
  public <R> Collection<ConfigItem<R>> get(final Class<R> clazz, final String configName, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(configName, "configName");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final URI uri = DataConfigSourceResource.uriGet(getBaseUri(), configName, versionCorrection, clazz);
    return configItemCollectionResult(accessRemote(uri).get(FudgeMsg.class));
  }

  @Override
  public <R> R getLatestByName(final Class<R> clazz, final String name) {
    return getSingle(clazz, name, VersionCorrection.LATEST);
  }

  @Override
  public <R> Collection<ConfigItem<R>> getAll(final Class<R> clazz, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final URI uri = DataConfigSourceResource.uriSearch(getBaseUri(), clazz, versionCorrection);
    return configItemCollectionResult(accessRemote(uri).get(FudgeMsg.class));
  }

}
