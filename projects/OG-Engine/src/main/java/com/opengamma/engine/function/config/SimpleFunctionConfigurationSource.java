/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.config;

import com.opengamma.util.ArgumentChecker;

/**
 * Simple implementation of the function configuration source that is setup at construction.
 */
public class SimpleFunctionConfigurationSource implements FunctionConfigurationSource {

  /**
   * The underlying config,
   */
  private final FunctionConfigurationBundle _repoConfig;

  /**
   * Creates an instance.
   * 
   * @param repoConfig  the config, not null
   */
  public SimpleFunctionConfigurationSource(final FunctionConfigurationBundle repoConfig) {
    ArgumentChecker.notNull(repoConfig, "repoConfig");
    _repoConfig = repoConfig;
  }

  @Override
  public FunctionConfigurationBundle getFunctionConfiguration() {
    return _repoConfig;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + _repoConfig + "]";
  }

}
