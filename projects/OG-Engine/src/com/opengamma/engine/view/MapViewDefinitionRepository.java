/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author kirk
 */
public class MapViewDefinitionRepository implements ViewDefinitionRepository, Serializable {
  private final ConcurrentMap<String, ViewDefinition> _definitionsByName = new ConcurrentSkipListMap<String, ViewDefinition>();

  @Override
  public ViewDefinition getDefinition(String definitionName) {
    return _definitionsByName.get(definitionName);
  }

  @Override
  public Set<String> getDefinitionNames() {
    return new TreeSet<String>(_definitionsByName.keySet());
  }
  
  public void addDefinition(ViewDefinition definition) {
    ArgumentChecker.notNull(definition, "View definition");
    _definitionsByName.put(definition.getName(), definition);
  }

}
