/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.config;

import com.opengamma.util.ArgumentChecker;

/**
 * Simple implementation of the repository configuration source that is setup at construction.
 */
public class SimpleRepositoryConfigurationSource implements RepositoryConfigurationSource {

  /**
   * The underlying config,
   */
  private final RepositoryConfiguration _repoConfig;

  /**
   * Creates an instance.
   * 
   * @param repoConfig  the config, not null
   */
  public SimpleRepositoryConfigurationSource(final RepositoryConfiguration repoConfig) {
    ArgumentChecker.notNull(repoConfig, "repoConfig");
    _repoConfig = repoConfig;
  }

  @Override
  public RepositoryConfiguration getRepositoryConfiguration() {
    return _repoConfig;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + _repoConfig + "]";
  }

}
