/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.List;
import java.util.Set;

import javax.time.Instant;

import com.opengamma.config.ConfigurationDocument;
import com.opengamma.config.ConfigurationDocumentRepo;
import com.opengamma.config.db.MongoDBConfigurationRepo;
import com.opengamma.util.MongoDBConnectionSettings;

/**
 * ManageableViewDefinitionRepository delegates all the work to  MongoDBConfigurationRepo<ViewDefinition>
 * 
 */
public class ManageableViewDefinitionRepository implements ConfigurationDocumentRepo<ViewDefinition>, ViewDefinitionRepository {
  
  private final MongoDBConfigurationRepo<ViewDefinition> _viewDefinitionRepo;

  /**
   * @param mongoDBsettings mongo connection settings
   */
  public ManageableViewDefinitionRepository(MongoDBConnectionSettings mongoDBsettings) {
    _viewDefinitionRepo = new MongoDBConfigurationRepo<ViewDefinition>(ViewDefinition.class, mongoDBsettings);
  }

  @Override
  public ConfigurationDocument<ViewDefinition> getByName(String name) {
    return _viewDefinitionRepo.getByName(name);
  }

  @Override
  public ConfigurationDocument<ViewDefinition> getByName(String currentName, Instant effectiveInstant) {
    return _viewDefinitionRepo.getByName(currentName, effectiveInstant);
  }

  @Override
  public Set<String> getNames() {
    return _viewDefinitionRepo.getNames();
  }

  @Override
  public List<ConfigurationDocument<ViewDefinition>> getSequence(String oid, Instant startDate, Instant endDate) {
    return getSequence(oid, startDate, endDate);
  }

  @Override
  public ConfigurationDocument<ViewDefinition> insertNewItem(String name, ViewDefinition value) {
    return _viewDefinitionRepo.insertNewItem(name, value);
  }

  @Override
  public ConfigurationDocument<ViewDefinition> insertNewVersion(String oid, ViewDefinition value) {
    return _viewDefinitionRepo.insertNewVersion(oid, value);
  }

  @Override
  public ConfigurationDocument<ViewDefinition> insertNewVersion(String oid, String name, ViewDefinition value) {
    return _viewDefinitionRepo.insertNewVersion(oid, name, value);
  }

  @Override
  public ViewDefinition getDefinition(String definitionName) {
    ConfigurationDocument<ViewDefinition> configurationDocument = getByName(definitionName);
    return configurationDocument.getValue();
  }

  @Override
  public Set<String> getDefinitionNames() {
    return getNames();
  }

}
