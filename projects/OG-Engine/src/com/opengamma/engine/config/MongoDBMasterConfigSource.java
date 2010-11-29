/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.time.Instant;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.config.ConfigDocument;
import com.opengamma.config.ConfigSearchRequest;
import com.opengamma.config.ConfigSearchResult;
import com.opengamma.config.ConfigTypeMaster;
import com.opengamma.config.mongo.MongoDBConfigTypeMaster;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * MongoDBMasterConfigSource is setup with all the supported ConfigMaster Types
 * and delegates search and call methods to the appropriate ConfigMaster
 */
public class MongoDBMasterConfigSource implements ConfigSource {
  
  private static final Logger s_logger = LoggerFactory.getLogger(MongoDBMasterConfigSource.class);
  
  private Map<Class<?>, MongoDBConfigTypeMaster<?>> _configMasterMap = new ConcurrentHashMap<Class<?>, MongoDBConfigTypeMaster<?>>();
  
  public MongoDBMasterConfigSource() {
  }
  
  /**
   * Primarily here to make Spring DI easier.
   * @param initialMap a map of class to MongoDBConfigMasters
   */
  public MongoDBMasterConfigSource(Map<Class<?>, MongoDBConfigTypeMaster<?>> initialMap) {
    _configMasterMap.putAll(initialMap);
  }

  @Override
  public <T> T get(Class<T> clazz, UniqueIdentifier identifier) {
    ConfigTypeMaster<T> configMaster = getConfigMasterFor(clazz);
    T result = null;
    try {
      ConfigDocument<T> configDocument = configMaster.get(identifier);
      result = configDocument.getValue();
    } catch (DataNotFoundException ex) {
      s_logger.warn("Cannot find doc with {}", identifier);
    }
    return result;
  }

  /**
   * Search for configuration elements using a request object.
   * 
   * @param <T>  the type of configuration element
   * @param clazz  the configuration element type, not null
   * @param request The request object with value for search fields, not null
   * @return all configuration elements matching the request, not null
   */
  public <T> List<T> search(Class<T> clazz, ConfigSearchRequest request) {
    MongoDBConfigTypeMaster<T> configMaster = getConfigMasterFor(clazz);
    List<T> result = new ArrayList<T>();
    ConfigSearchResult<T> searchResult = configMaster.search(request);
    List<ConfigDocument<T>> documents = searchResult.getDocuments();
    for (ConfigDocument<T> configDocument : documents) {
      s_logger.info("configDocument = " + ToStringBuilder.reflectionToString(configDocument));
      result.add(configDocument.getValue());
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  private <T> MongoDBConfigTypeMaster<T> getConfigMasterFor(Class<T> clazz) {
    MongoDBConfigTypeMaster<T> configMaster = (MongoDBConfigTypeMaster<T>) _configMasterMap.get(clazz);
    if (configMaster == null) {
      s_logger.warn("cannot do lookup on {} document type", clazz);
      throw new OpenGammaRuntimeException("MongoDBMasterConfigSource not set up properly for " + clazz);
    }
    return configMaster;
  }
  
  /**
   * Add a type configMaster
   * @param clazz the class of the config doc, not-null
   * @param configMaster the mongodb configmaster, not-null
   */
  public void addConfigMaster(Class<?> clazz, MongoDBConfigTypeMaster<?> configMaster) {
    ArgumentChecker.notNull(clazz, "config doc class");
    ArgumentChecker.notNull(configMaster, "configMaster");
    _configMasterMap.put(clazz, configMaster);
  }
  
  /**
   * Set the underlying configMasters 
   * @param configMasterMap the configMasterMap, not-null
   */
  public void setConfigMasterMap(Map<Class<?>, MongoDBConfigTypeMaster<?>> configMasterMap) {
    ArgumentChecker.notNull(configMasterMap, "configMasterMap");
    _configMasterMap = configMasterMap;
  }

  @Override
  public <T> T getLatestByName(Class<T> clazz, String name) {
    ConfigSearchRequest searchRequest = new ConfigSearchRequest();
    searchRequest.setName(name);
    List<T> results = search(clazz, searchRequest);
    if (results.size() == 0) {
      return null;
    } else {
      return results.get(0);
    }
  }

  @Override
  public <T> T getByName(Class<T> clazz, String name, Instant versionAsOf) {
    ConfigSearchRequest searchRequest = new ConfigSearchRequest();
    searchRequest.setVersionAsOfInstant(versionAsOf);
    searchRequest.setName(name);
    List<T> results = search(clazz, searchRequest);
    if (results.size() == 0) {
      return null;
    } else {
      return results.get(0);
    }
  }

}
