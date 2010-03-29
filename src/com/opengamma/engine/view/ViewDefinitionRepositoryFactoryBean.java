/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import org.springframework.beans.factory.FactoryBean;

/**
 * A FactoryBean abstract base for constructing ViewDefinitionRepository objects. Using this can make
 * the Spring config cleaner - create a factory in Java for complex initiations and inject that
 * as a parameter instead.
 * 
 * @author Andrew Griffin
 */
public abstract class ViewDefinitionRepositoryFactoryBean implements FactoryBean {

  @Override
  public abstract ViewDefinitionRepository getObject();

  @Override
  public Class<ViewDefinitionRepository> getObjectType() {
    return ViewDefinitionRepository.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
  
}
