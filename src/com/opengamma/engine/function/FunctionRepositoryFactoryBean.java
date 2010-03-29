/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import org.springframework.beans.factory.FactoryBean;

/**
 * A FactoryBean abstract base for constructing FunctionRepository objects. Using this can make
 * the Spring config cleaner - create a factory in Java for complex initiations and inject that
 * as a parameter instead.
 * 
 * @author Andrew Griffin
 */
public abstract class FunctionRepositoryFactoryBean implements FactoryBean {

  @Override
  public abstract FunctionRepository getObject();

  @Override
  public Class<FunctionRepository> getObjectType() {
    return FunctionRepository.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
  
}
