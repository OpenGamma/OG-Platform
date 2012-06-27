/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * A source of {@link FunctionBlacklistPolicy} definitions backed by a config source.
 */
public class ConfigDBFunctionBlacklistPolicySource implements FunctionBlacklistPolicySource {

  private static final String SUFFIX = "_BLACKLIST";

  private final ConfigSource _configSource;

  public ConfigDBFunctionBlacklistPolicySource(final ConfigSource configSource) {
    ArgumentChecker.notNull(configSource, "configSource");
    _configSource = configSource;
  }

  protected ConfigSource getConfigSource() {
    return _configSource;
  }

  @Override
  public FunctionBlacklistPolicy getPolicy(final UniqueId uniqueId) {
    return getConfigSource().getConfig(FunctionBlacklistPolicy.class, uniqueId);
  }

  @Override
  public FunctionBlacklistPolicy getPolicy(final String name) {
    return getConfigSource().getLatestByName(FunctionBlacklistPolicy.class, name + SUFFIX);
  }

}
