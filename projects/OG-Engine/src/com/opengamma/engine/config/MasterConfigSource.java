/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import javax.time.Instant;
import javax.time.InstantProvider;

import com.google.common.base.Function;
import com.google.common.collect.ComputationException;
import com.google.common.collect.MapMaker;
import com.opengamma.DataNotFoundException;
import com.opengamma.config.ConfigDocument;
import com.opengamma.config.ConfigMaster;
import com.opengamma.config.ConfigSearchHistoricRequest;
import com.opengamma.config.ConfigSearchHistoricResult;
import com.opengamma.config.ConfigSearchRequest;
import com.opengamma.config.ConfigSearchResult;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A {@code ConfigSource} implemented using an underlying {@code ConfigMaster}.
 * <p>
 * The {@link ConfigSource} interface provides securities to the engine via a narrow API.
 * This class provides the source on top of a standard {@link ConfigMaster}.
 */
public class MasterConfigSource implements ConfigSource {

  /**
   * Map of masters by class.
   */
  private ConcurrentMap<Class<?>, ConfigMaster<?>> _configMasterMap = new MapMaker().makeComputingMap(new Function<Class<?>, ConfigMaster<?>>() {
    public ConfigMaster<?> apply(final Class<?> clazz) {
      return createMaster(clazz);
    }
  });

  /**
   * The instant to search for a version at.
   * Null is treated as the latest version.
   */
  private final Instant _versionAsOfInstant;

  /**
   * Creates an instance.
   */
  public MasterConfigSource() {
    this(null);
  }

  /**
   * Creates an instance viewing the version that existed on the specified instant.
   * @param versionAsOfInstantProvider  the version instant to retrieve, null for latest version
   */
  public MasterConfigSource(final InstantProvider versionAsOfInstantProvider) {
    if (versionAsOfInstantProvider != null) {
      _versionAsOfInstant = Instant.of(versionAsOfInstantProvider);
    } else {
      _versionAsOfInstant = null;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Overridable method to create a master for the type specified.
   * @param <T>  the type of the master to create
   * @param clazz  the class to create a master for, not null
   * @return the created master, not null
   * @throws IllegalArgumentException if a master cannot be created for the specified type
   */
  protected <T> ConfigMaster<T> createMaster(Class<T> clazz) {
    throw new IllegalArgumentException("Unable to create ConfigMaster for " + clazz.getName());
  }

  /**
   * Finds a config master for the type specified.
   * @param <T>  the type
   * @param clazz  the class to create a master for, not null
   * @return the master, not null
   */
  @SuppressWarnings("unchecked")
  public <T> ConfigMaster<T> getMaster(Class<T> clazz) {
    try {
      return (ConfigMaster<T>) _configMasterMap.get(clazz);
    } catch (ComputationException ex) {
      if (ex.getCause() instanceof RuntimeException) {
        throw (RuntimeException) ex.getCause();
      }
      throw ex;
    }
  }

  /**
   * Finds a config master for the type specified.
   * @param <T>  the type
   * @param clazz  the class to create a master for, not null
   * @param master  the master to add, not null
   */
  public <T> void addMaster(Class<T> clazz, ConfigMaster<T> master) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(master, "master");
    ConfigMaster<?> old = _configMasterMap.putIfAbsent(clazz, master);
    if (old != null) {
      throw new IllegalStateException("Master already exists for " + clazz.getName());
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> List<T> search(final Class<T> clazz, final ConfigSearchRequest request) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(request, "request");
    ConfigMaster<T> configMaster = getMaster(clazz);
    request.setVersionAsOfInstant(_versionAsOfInstant);
    ConfigSearchResult<T> searchResult = configMaster.search(request);
    List<ConfigDocument<T>> documents = searchResult.getDocuments();
    List<T> result = new ArrayList<T>();
    for (ConfigDocument<T> configDocument : documents) {
      result.add(configDocument.getValue());
    }
    return result;
  }

  @Override
  public <T> T get(final Class<T> clazz, final UniqueIdentifier uid) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(uid, "uid");
    ConfigMaster<T> configMaster = getMaster(clazz);
    if (_versionAsOfInstant != null) {
      ConfigSearchHistoricRequest request = new ConfigSearchHistoricRequest(uid, _versionAsOfInstant);
      ConfigSearchHistoricResult<T> result = configMaster.searchHistoric(request);
      if (result.getDocuments().isEmpty()) {
        return null;
      }
      if (uid.isVersioned() && uid.getVersion().equals(result.getFirstDocument().getConfigId().getVersion()) == false) {
        return null;  // config found, but not matching the version we asked for
      }
      return result.getFirstValue();
    } else {
      // Just want the latest (or version) asked for, so don't use the more costly historic search operation
      try {
        return configMaster.get(uid).getValue();
      } catch (DataNotFoundException ex) {
        return null;
      }
    }
  }

}
