/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.time.Instant;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.VersionedSource;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PagingRequest;
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
   * @param <T>  the type of configuration element
   * @param request  the request object with value for search fields, not null
   * @return all configuration elements matching the request, not null
   */
  public <T> List<T> search(final ConfigSearchRequest<T> request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getType(), "request.type");
    request.setVersionCorrection(getVersionCorrection());
    
    ConfigSearchResult<T> searchResult = getMaster().search(request);
    List<ConfigDocument<T>> documents = searchResult.getDocuments();
    List<T> result = new ArrayList<T>();
    for (ConfigDocument<T> configDocument : documents) {
      result.add(configDocument.getValue());
    }
    return result;
  }

  @Override
  public <T> T getConfig(final Class<T> clazz, final UniqueId uniqueId) {
    return getDocument(clazz, uniqueId).getValue();
  }

  @Override
  public <T> T getConfig(final Class<T> clazz, final ObjectId objectId, VersionCorrection versionCorrection) {
    return getDocument(clazz, objectId, versionCorrection).getValue();
  }

  @Override
  public <T> Collection<? extends T> getConfigs(Class<T> clazz, String configName, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(configName, "configName");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    
    ConfigSearchRequest<T> request = new ConfigSearchRequest<T>();
    request.setVersionCorrection(versionCorrection);
    request.setName(configName);
    request.setType(clazz);
    ConfigSearchResult<T> searchResult = getMaster().search(request);
    return searchResult.getValues();
  }

  @Override
  public <T> T getLatestByName(final Class<T> clazz, final String name) {
    return getByName(clazz, name, null);
  }

  @Override
  public <T> T getByName(final Class<T> clazz, final String name, final Instant versionAsOf) {
    ConfigDocument<T> doc = getDocumentByName(clazz, name, versionAsOf);
    return doc == null ? null : doc.getValue();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a configuration document by unique identifier.
   * 
   * @param <T>  the type of configuration element
   * @param clazz  the configuration element type, not null
   * @param uniqueId  the unique identifier, not null
   * @return the configuration document, null if not found
   */
  public <T> ConfigDocument<T> getDocument(final Class<T> clazz, final UniqueId uniqueId) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    VersionCorrection vc = getVersionCorrection(); // lock against change
    if (vc != null) {
      return getMaster().get(uniqueId, vc, clazz);
    } else {
      return getMaster().get(uniqueId, clazz);
    }
  }

  /**
   * Gets a configuration document by object identifier and version-correction.
   * 
   * @param <T>  the type of configuration element
   * @param clazz  the configuration element type, not null
   * @param objectId  the object identifier, not null
   * @param versionCorrection  the version-correction, not null
   * @return the configuration document, null if not found
   */
  public <T> ConfigDocument<T> getDocument(final Class<T> clazz, final ObjectId objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    return getMaster().get(objectId, versionCorrection, clazz);
  }

  /**
   * Searches for a configuration document matching the specified name.
   * <p>
   * This will always return the latest correction of the version requested, ignoring any other version constraints
   * of the implementation.
   * 
   * @param <T>  the type of configuration element
   * @param clazz  the configuration element type, not null
   * @param name  the element name to search for, wildcards allowed, not null
   * @param versionAsOf  the version to fetch, null means latest
   * @return the versioned configuration document matching the request, null if not found
   */
  public <T> ConfigDocument<T> getDocumentByName(final Class<T> clazz, final String name, final Instant versionAsOf) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(name, "name");
    
    ConfigSearchRequest<T> request = new ConfigSearchRequest<T>();
    request.setPagingRequest(PagingRequest.ONE);
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(versionAsOf));
    request.setName(name);
    request.setType(clazz);
    
    ConfigSearchResult<T> searchResult = getMaster().search(request);
    return searchResult.getFirstDocument();
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
