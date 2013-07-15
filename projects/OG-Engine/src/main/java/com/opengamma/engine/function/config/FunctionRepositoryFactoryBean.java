/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.config;

import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Bean for constructing a {@link FunctionRepository} from a {@link FunctionConfigurationSource} using the {@link FunctionRepositoryFactory}.
 */
public class FunctionRepositoryFactoryBean extends SingletonFactoryBean<FunctionRepository> {

  private FunctionConfigurationSource _functionConfigurationSource;

  public void setFunctionConfigurationSource(final FunctionConfigurationSource functionConfigurationSource) {
    _functionConfigurationSource = functionConfigurationSource;
  }

  public FunctionConfigurationSource getRepositoryConfigurationSource() {
    return _functionConfigurationSource;
  }

  @Override
  protected FunctionRepository createObject() {
    final FunctionConfigurationSource functionConfigurationSource = getRepositoryConfigurationSource();
    ArgumentChecker.notNull(functionConfigurationSource, "functionConfigurationSource");
    final FunctionConfigurationBundle repositoryConfiguration = functionConfigurationSource.getFunctionConfiguration();
    return FunctionRepositoryFactory.constructRepository(repositoryConfiguration);
  }

}
