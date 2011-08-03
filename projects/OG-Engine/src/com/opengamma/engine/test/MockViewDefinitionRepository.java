/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.test;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDefinitionRepository;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class MockViewDefinitionRepository implements ViewDefinitionRepository, Serializable {

  private static final long serialVersionUID = 1L;
  
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

  @Override
  public ChangeManager changeManager() {
    return DummyChangeManager.INSTANCE;
  }

}
