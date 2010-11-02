/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import com.opengamma.engine.config.ConfigSource;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.world.region.master.RegionSource;

/**
 * Utility methods to pull standard objects out of a {@link FunctionCompilationContext}.
 */
public final class OpenGammaCompilationContext {

  private static final String CONFIG_SOURCE_NAME = "configSource";
  private static final String REGION_SOURCE_NAME = "regionSource";
  private static final String CONVENTION_BUNDLE_SOURCE_NAME = "conventionBundleSource";

  /**
   * Restricted constructor.
   */
  private OpenGammaCompilationContext() {
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a {@code ConfigSource} from the context.
   * @param compilationContext  the context to examine, not null
   * @return the config source, null if not found
   */
  public static ConfigSource getConfigSource(FunctionCompilationContext compilationContext) {
    return (ConfigSource) compilationContext.get(CONFIG_SOURCE_NAME);
  }

  /**
   * Stores a {@code ConfigSource} in the context.
   * @param compilationContext  the context to store in, not null
   * @param configSource  the config source to store, not null
   */
  public static void setConfigSource(FunctionCompilationContext compilationContext, ConfigSource configSource) {
    compilationContext.put(CONFIG_SOURCE_NAME, configSource);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a {@code RegionSource} from the context.
   * @param compilationContext  the context to examine, not null
   * @return the region source, null if not found
   */
  public static RegionSource getRegionSource(FunctionCompilationContext compilationContext) {
    return (RegionSource) compilationContext.get(REGION_SOURCE_NAME);
  }

  /**
   * Stores a {@code RegionSource} in the context.
   * @param compilationContext  the context to store in, not null
   * @param regionSource  the region source to store, not null
   */
  public static void setRegionSource(FunctionCompilationContext compilationContext, RegionSource regionSource) {
    compilationContext.put(REGION_SOURCE_NAME, regionSource);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a {@code ConventionBundleSource} from the context.
   * @param compilationContext  the context to examine, not null
   * @return the convention bundle source, null if not found
   */
  public static ConventionBundleSource getConventionBundleSource(FunctionCompilationContext compilationContext) {
    return (ConventionBundleSource) compilationContext.get(CONVENTION_BUNDLE_SOURCE_NAME);
  }

  /**
   * Stores a {@code ConventionBundleSource} in the context.
   * @param compilationContext  the context to store in, not null
   * @param conventionBundleSource  the convention bundle source to store, not null
   */
  public static void setConventionBundleSource(FunctionCompilationContext compilationContext, ConventionBundleSource conventionBundleSource) {
    compilationContext.put(CONVENTION_BUNDLE_SOURCE_NAME, conventionBundleSource);
  }

}
