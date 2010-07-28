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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.config.ConfigDocument;
import com.opengamma.config.ConfigMaster;
import com.opengamma.config.ConfigSearchRequest;
import com.opengamma.config.ConfigSearchResult;
import com.opengamma.config.db.MongoDBConfigMaster;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.MongoDBConnectionSettings;

/**
 * 
 */
public class MongoDBMasterConfigSource implements ConfigSource {
  
  private static final Logger s_logger = LoggerFactory.getLogger(MongoDBMasterConfigSource.class);
  
  private final Map<Class<?>, MongoDBConfigMaster<?>> _configMasterMap = new ConcurrentHashMap<Class<?>, MongoDBConfigMaster<?>>();
  
  /**
   * @param mongoSettings the mongosettings, not-null
   */
  public MongoDBMasterConfigSource(MongoDBConnectionSettings mongoSettings) {
    ArgumentChecker.notNull(mongoSettings, "mongoSettings");
    _configMasterMap.put(ViewDefinition.class, new MongoDBConfigMaster<ViewDefinition>(ViewDefinition.class, mongoSettings));
  }

  @Override
  public <T> T get(Class<T> clazz, UniqueIdentifier identifier) {
    ConfigMaster<T> configMaster = getConfigMasterFor(clazz);
    T result = null;
    try {
      ConfigDocument<T> configDocument = configMaster.get(identifier);
      result = configDocument.getValue();
    } catch (DataNotFoundException ex) {
      s_logger.warn("Cannot find doc with {}", identifier);
    }
    return result;
  }

  @Override
  public <T> List<T> search(Class<T> clazz, ConfigSearchRequest request) {
    MongoDBConfigMaster<T> configMaster = getConfigMasterFor(clazz);
    List<T> result = new ArrayList<T>();
    ConfigSearchResult<T> searchResult = configMaster.search(request);
    List<ConfigDocument<T>> documents = searchResult.getDocuments();
    for (ConfigDocument<T> configDocument : documents) {
      result.add(configDocument.getValue());
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  private <T> MongoDBConfigMaster<T> getConfigMasterFor(Class<T> clazz) {
    MongoDBConfigMaster<T> configMaster = (MongoDBConfigMaster<T>) _configMasterMap.get(clazz);
    if (configMaster == null) {
      s_logger.warn("cannot do lookup on {} document type", clazz);
      throw new OpenGammaRuntimeException("MongoDBMasterConfigSource not set up properly for " + clazz);
    }
    return configMaster;
  }

}
