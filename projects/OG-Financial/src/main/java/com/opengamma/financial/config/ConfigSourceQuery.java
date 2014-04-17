/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.config;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Wrapper for querying a {@link ConfigSource} from a function. This uses a locked version/correction (not LATEST), and triggers function re-initialization when the relevant configuration item(s)
 * change.
 * 
 * @param <T> the type of item returned
 */
public class ConfigSourceQuery<T> {

  private final ConfigSource _configSource;
  private final Class<T> _clazz;
  private final VersionCorrection _lockedVersionCorrection;

  /**
   * Creates a new instance. Most uses from a function should use the {@link #init} method instead.
   * 
   * @param configSource the configuration source to query, not null
   * @param clazz the object type, not null
   * @param versionCorrection the locked version/correction timestamp, not null
   */
  public ConfigSourceQuery(final ConfigSource configSource, final Class<T> clazz, final VersionCorrection versionCorrection) {
    _configSource = ArgumentChecker.notNull(configSource, "configSource");
    _clazz = ArgumentChecker.notNull(clazz, "clazz");
    _lockedVersionCorrection = ArgumentChecker.notNull(versionCorrection, "versionCorrection");
  }

  /**
   * Creates a new instance, registering the calling function for reinitialization.
   * 
   * @param context the function compilation context, not null
   * @param function the function definition making the call, not null
   * @param clazz the object type, not null
   * @param <T> the object type
   * @return the query instance, not null
   */
  public static <T> ConfigSourceQuery<T> init(final FunctionCompilationContext context, final FunctionDefinition function, final Class<T> clazz) {
    AbstractConfigChangeProvider.reinitOnChanges(context, function, clazz);
    return new ConfigSourceQuery<T>(OpenGammaCompilationContext.getConfigSource(context), clazz, context.getFunctionInitializationVersionCorrection());
  }

  public void reinitOnChange(final FunctionCompilationContext context, final FunctionDefinition function) {
    AbstractConfigChangeProvider.reinitOnChanges(context, function, getType());
  }

  public ConfigSource getConfigSource() {
    return _configSource;
  }

  public Class<T> getType() {
    return _clazz;
  }

  public VersionCorrection getVersionCorrection() {
    return _lockedVersionCorrection;
  }

  /**
   * Fetches the "latest" config item. This is the value at the locked version/correction which is typically the time when the system was last in a consistent state (eg at the start of a graph
   * compilation/execution sequence).
   * 
   * @param name the name of the item to query
   * @return the config item, null if not found
   */
  public T get(final String name) {
    return get(name, getVersionCorrection());
  }

  /**
   * Fetches a specific config item.
   * 
   * @param name the name of the item to query
   * @param versionCorrection the version/correction timestamp to query at, not null
   * @return the config item, null if not found
   */
  public T get(final String name, final VersionCorrection versionCorrection) {
    return getConfigSource().getSingle(getType(), name, versionCorrection);
  }

}
