/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArraySet;

import com.opengamma.core.change.AggregatingChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * A view definition repository which represents one or more others. When retrieving a view definition, repositories
 * are searched in the order that they were added and the first matching definition is returned.
 * <p>
 * This is temporary, to be replaced once view definitions have proper identifiers.
 */
public class AggregatingViewDefinitionRepository implements ViewDefinitionRepository {

  private final Set<ViewDefinitionRepository> _repositories = new CopyOnWriteArraySet<ViewDefinitionRepository>();
  private final AggregatingChangeManager _changeManager = new AggregatingChangeManager();

  public AggregatingViewDefinitionRepository(Collection<ViewDefinitionRepository> repositories) {
    for (ViewDefinitionRepository repository : repositories) {
      addRepository(repository);
    }
  }

  public void addRepository(ViewDefinitionRepository repository) {
    ArgumentChecker.notNull(repository, "repository");
    _repositories.add(repository);
    _changeManager.addChangeManager(repository.changeManager());
  }

  @Override
  public ViewDefinition getDefinition(UniqueId definitionId) {
    for (ViewDefinitionRepository repository : _repositories) {
      ViewDefinition definition = repository.getDefinition(definitionId);
      if (definition != null) {
        return definition;
      }
    }
    return null;
  }

  @Override
  public ViewDefinition getDefinition(String name) {
    for (ViewDefinitionRepository repository : _repositories) {
      ViewDefinition definition = repository.getDefinition(name);
      if (definition != null) {
        return definition;
      }
    }
    return null;    
  }
  
  @Override
  public Set<UniqueId> getDefinitionIds() {
    Set<UniqueId> result = new TreeSet<UniqueId>();
    for (ViewDefinitionRepository repository : _repositories) {
      result.addAll(repository.getDefinitionIds());
    }
    return result;
  }

  @Override
  public Map<UniqueId, String> getDefinitionEntries() {

    Map<UniqueId, String> result = new HashMap<UniqueId, String>();
    
    for (ViewDefinitionRepository repository : _repositories) {
      result.putAll(repository.getDefinitionEntries());
    }
    
    return result;
  }
  
  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

}
