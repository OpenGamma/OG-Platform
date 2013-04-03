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
 * Bean for constructing a {@link FunctionRepository} from a {@link FunctionConfigurationSource} using the {@link RepositoryFactory}.
 */
public class RepositoryFactoryBean extends SingletonFactoryBean<FunctionRepository> {

  private FunctionConfigurationSource _repositoryConfigurationSource;

  public void setRepositoryConfigurationSource(final FunctionConfigurationSource repositoryConfigurationSource) {
    _repositoryConfigurationSource = repositoryConfigurationSource;
  }

  public FunctionConfigurationSource getRepositoryConfigurationSource() {
    return _repositoryConfigurationSource;
  }

  @Override
  protected FunctionRepository createObject() {
    final FunctionConfigurationSource repositoryConfigurationSource = getRepositoryConfigurationSource();
    ArgumentChecker.notNull(repositoryConfigurationSource, "repositoryConfigurationSource");
    final FunctionConfigurationBundle repositoryConfiguration = repositoryConfigurationSource.getFunctionConfiguration();
    return RepositoryFactory.constructRepository(repositoryConfiguration);
  }

}
