/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * A view definition repository which represents one or more others. When retrieving a view definition, repositories
 * are searched in the order that they were added and the first matching definition is returned.
 * <p>
 * This is temporary, to be replaced once view definitions have proper identifiers.
 */
public class AggregatingViewDefinitionRepository implements ViewDefinitionRepository {

  private final Set<ViewDefinitionRepository> _repositories = new CopyOnWriteArraySet<ViewDefinitionRepository>();

  public AggregatingViewDefinitionRepository(Collection<ViewDefinitionRepository> repositories) {
    for (ViewDefinitionRepository repository : repositories) {
      addRepository(repository);
    }
  }

  public void addRepository(ViewDefinitionRepository repository) {
    _repositories.add(repository);
  }

  @Override
  public ViewDefinition getDefinition(String definitionName) {
    for (ViewDefinitionRepository repository : _repositories) {
      ViewDefinition definition = repository.getDefinition(definitionName);
      if (definition != null) {
        return definition;
      }
    }
    return null;
  }

  @Override
  public Set<String> getDefinitionNames() {
    Set<String> result = new TreeSet<String>();
    for (ViewDefinitionRepository repository : _repositories) {
      result.addAll(repository.getDefinitionNames());
    }
    return result;
  }

}
