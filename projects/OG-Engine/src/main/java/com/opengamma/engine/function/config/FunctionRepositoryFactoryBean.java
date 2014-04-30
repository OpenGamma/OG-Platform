/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Bean for constructing a {@link FunctionRepository} from a {@link FunctionConfigurationSource} using the {@link FunctionRepositoryFactory}.
 * 
 * @deprecated The {@code FunctionConfigurationSource} requires a version to deliver a reliable repository; configuration using this will not be able to work correctly with dynamically changing
 *             function repositories
 */
@Deprecated
public class FunctionRepositoryFactoryBean extends SingletonFactoryBean<FunctionRepository> {

  private static final Logger s_logger = LoggerFactory.getLogger(FunctionRepositoryFactoryBean.class);

  private FunctionConfigurationSource _functionConfigurationSource;

  private Instant _configurationVersion = Instant.now();

  public void setFunctionConfigurationSource(final FunctionConfigurationSource functionConfigurationSource) {
    _functionConfigurationSource = functionConfigurationSource;
  }

  public FunctionConfigurationSource getRepositoryConfigurationSource() {
    return _functionConfigurationSource;
  }

  public void setConfigurationVersion(final Instant configurationVersion) {
    _configurationVersion = configurationVersion;
  }

  public Instant getConfigurationVersion() {
    return _configurationVersion;
  }

  @Override
  protected FunctionRepository createObject() {
    s_logger.error("Deprecated configuration: pass the FunctionConfigurationSource directly - don't use this factory bean");
    final FunctionConfigurationSource functionConfigurationSource = getRepositoryConfigurationSource();
    final Instant configurationVersion = getConfigurationVersion();
    ArgumentChecker.notNull(functionConfigurationSource, "functionConfigurationSource");
    ArgumentChecker.notNull(configurationVersion, "configurationVersion");
    final FunctionConfigurationBundle repositoryConfiguration = functionConfigurationSource.getFunctionConfiguration(configurationVersion);
    return FunctionRepositoryFactory.constructRepository(repositoryConfiguration);
  }

}
