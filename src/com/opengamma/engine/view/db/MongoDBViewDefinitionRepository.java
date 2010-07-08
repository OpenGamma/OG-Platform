/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.db;

import java.util.List;
import java.util.Set;

import javax.time.Instant;

import com.opengamma.config.ConfigDocument;
import com.opengamma.config.ConfigDocumentRepository;
import com.opengamma.config.db.MongoDBConfigRepository;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDefinitionRepository;
import com.opengamma.util.MongoDBConnectionSettings;

/**
 * ManageableViewDefinitionRepository delegates all the work to  MongoDBConfigurationRepo<ViewDefinition>
 * 
 */
public class MongoDBViewDefinitionRepository implements ConfigDocumentRepository<ViewDefinition>, ViewDefinitionRepository {
  
  private final MongoDBConfigRepository<ViewDefinition> _viewDefinitionRepo;

  /**
   * @param mongoDBsettings mongo connection settings
   */
  public MongoDBViewDefinitionRepository(MongoDBConnectionSettings mongoDBsettings) {
    _viewDefinitionRepo = new MongoDBConfigRepository<ViewDefinition>(ViewDefinition.class, mongoDBsettings);
  }

  @Override
  public ConfigDocument<ViewDefinition> getByName(String name) {
    return _viewDefinitionRepo.getByName(name);
  }

  @Override
  public ConfigDocument<ViewDefinition> getByName(String currentName, Instant effectiveInstant) {
    return _viewDefinitionRepo.getByName(currentName, effectiveInstant);
  }

  @Override
  public Set<String> getNames() {
    return _viewDefinitionRepo.getNames();
  }

  @Override
  public List<ConfigDocument<ViewDefinition>> getSequence(String oid, Instant startDate, Instant endDate) {
    return getSequence(oid, startDate, endDate);
  }

  @Override
  public ConfigDocument<ViewDefinition> insertNewItem(String name, ViewDefinition value) {
    return _viewDefinitionRepo.insertNewItem(name, value);
  }

  @Override
  public ConfigDocument<ViewDefinition> insertNewVersion(String oid, ViewDefinition value) {
    return _viewDefinitionRepo.insertNewVersion(oid, value);
  }

  @Override
  public ConfigDocument<ViewDefinition> insertNewVersion(String oid, String name, ViewDefinition value) {
    return _viewDefinitionRepo.insertNewVersion(oid, name, value);
  }

  @Override
  public ViewDefinition getDefinition(String definitionName) {
    ConfigDocument<ViewDefinition> configurationDocument = getByName(definitionName);
    return configurationDocument.getValue();
  }

  @Override
  public Set<String> getDefinitionNames() {
    return getNames();
  }

  @Override
  public ConfigDocument<ViewDefinition> getByOid(String oid, int version) {
    return _viewDefinitionRepo.getByOid(oid, version);
  }

}
