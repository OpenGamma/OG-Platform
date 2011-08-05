/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.view.memory;

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.view.AddViewDefinitionRequest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Spring factory bean for {@link InMemoryViewDefinitionRepository}
 */
public class InMemoryViewDefinitionRepositoryFactoryBean extends SingletonFactoryBean<InMemoryViewDefinitionRepository> {

  private ViewDefinition[] _viewDefinitions;
  
  public void setViewDefinition(final ViewDefinition viewDefinition) {
    _viewDefinitions = new ViewDefinition[] {viewDefinition};
  }
  
  public void setViewDefinitions(final ViewDefinition[] viewDefinitions) {
    _viewDefinitions = viewDefinitions;
  }
  
  public ViewDefinition[] getViewDefinitions() {
    return _viewDefinitions;
  }
  
  @Override
  protected InMemoryViewDefinitionRepository createObject() {
    ArgumentChecker.notNullInjected(getViewDefinitions(), "viewDefinitions");
    InMemoryViewDefinitionRepository repository = new InMemoryViewDefinitionRepository();
    for (ViewDefinition definition : getViewDefinitions()) {
      repository.addViewDefinition(new AddViewDefinitionRequest(definition));
    }
    return repository;
  }

}
