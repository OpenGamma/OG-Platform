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
 * Bean for constructing a {@link FunctionRepository} from a {@link RepositoryConfigurationSource} using the {@link RepositoryFactory}.
 */
public class RepositoryFactoryBean extends SingletonFactoryBean<FunctionRepository> {

  private RepositoryConfigurationSource _repositoryConfigurationSource;

  public void setRepositoryConfigurationSource(final RepositoryConfigurationSource repositoryConfigurationSource) {
    _repositoryConfigurationSource = repositoryConfigurationSource;
  }

  public RepositoryConfigurationSource getRepositoryConfigurationSource() {
    return _repositoryConfigurationSource;
  }

  @Override
  protected FunctionRepository createObject() {
    final RepositoryConfigurationSource repositoryConfigurationSource = getRepositoryConfigurationSource();
    ArgumentChecker.notNull(repositoryConfigurationSource, "repositoryConfigurationSource");
    final RepositoryConfiguration repositoryConfiguration = repositoryConfigurationSource.getRepositoryConfiguration();
    return RepositoryFactory.constructRepository(repositoryConfiguration);
  }

}
