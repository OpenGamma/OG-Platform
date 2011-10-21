/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.test;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDefinitionRepository;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * KV: test for behaviour with duplicate view definition names
 */
public class MockViewDefinitionRepository implements ViewDefinitionRepository, Serializable {

  private static final long serialVersionUID = 1L;
  
  private final ConcurrentMap<UniqueId, ViewDefinition> _definitionsById = new ConcurrentSkipListMap<UniqueId, ViewDefinition>();
  private final ConcurrentMap<String, ViewDefinition> _definitionsByName = new ConcurrentSkipListMap<String, ViewDefinition>();
  private final ChangeManager _changeManager = new BasicChangeManager();

  @Override
  public ViewDefinition getDefinition(UniqueId definitionId) {
    return _definitionsById.get(definitionId);
  }
  
  @Override
  public ViewDefinition getDefinition(String definitionName) {
    return _definitionsByName.get(definitionName);
  }
  

  @Override
  public Set<UniqueId> getDefinitionIds() {
    return new TreeSet<UniqueId>(_definitionsById.keySet());
  }
  
  @Override
  public Map<UniqueId, String> getDefinitionEntries() {
    
    Map<UniqueId, String> result = new HashMap<UniqueId, String>();
        
    for (Map.Entry<UniqueId, ViewDefinition> entry : _definitionsById.entrySet()) {
      result.put(entry.getKey(), entry.getValue().getName());
    }
    return result;
  }
  
  public void addDefinition(ViewDefinition definition) {
    ArgumentChecker.notNull(definition, "View definition");
    if (definition.getUniqueId() != null) { 
      _definitionsById.put(definition.getUniqueId(), definition);
    }
    _definitionsByName.put(definition.getName(), definition);
  }

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

}
