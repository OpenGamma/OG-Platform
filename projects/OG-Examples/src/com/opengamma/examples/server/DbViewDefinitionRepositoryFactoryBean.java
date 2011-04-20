/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.server;

import com.opengamma.engine.view.MapViewDefinitionRepository;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDefinitionRepository;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Creates a ViewDefinitionRepository (using the MapViewDefinitionRepository implementation) for a set of views.
 * <p>
 * The views are loaded from the database using configmaster
 */
public class DbViewDefinitionRepositoryFactoryBean extends SingletonFactoryBean<ViewDefinitionRepository> {

  private final ConfigMaster _configMaster;
  
  /**
   * Creates an instance 
   * @param configMaster the configmaster not null
   */
  public DbViewDefinitionRepositoryFactoryBean(ConfigMaster configMaster) {
    ArgumentChecker.notNull(configMaster, "configMaster");
    _configMaster = configMaster;
  }

  @Override
  protected ViewDefinitionRepository createObject() {
    MapViewDefinitionRepository viewDefinitionRepository = new MapViewDefinitionRepository();
    
    ConfigSearchRequest<ViewDefinition> request = new ConfigSearchRequest<ViewDefinition>(ViewDefinition.class);
    ConfigSearchResult<ViewDefinition> searchResult = _configMaster.search(request);
    for (ConfigDocument<ViewDefinition> configDocument : searchResult.getDocuments()) {
      viewDefinitionRepository.addDefinition(configDocument.getValue());
    }
    
    return viewDefinitionRepository;
  }

}
