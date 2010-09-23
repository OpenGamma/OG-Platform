/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import com.opengamma.engine.config.ConfigSource;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionSource;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.world.region.RegionSource;

/**
 * Utility methods to pull standard objects out of a {@link FunctionCompilationContext}.
 *
 * @author kirk
 */
public final class OpenGammaCompilationContext {
  /**
   * Name under which an instance of {@link InterpolatedYieldCurveDefinitionSource} will be bound
   * at runtime.
   */
  public static final String YIELD_CURVE_SPECIFICATION_BUILDER_NAME = "yieldCurveSpecificationBuilder";
  private static final String CONFIG_SOURCE_NAME = "configSource";
  private static final String REGION_SOURCE_NAME = "regionSource";
  private static final String CONVENTION_BUNDLE_SOURCE_NAME = "conventionBundleSource";
  
  private OpenGammaCompilationContext() {
  }
  
  public static void setConfigSource(FunctionCompilationContext compilationContext, ConfigSource configSource) {
    compilationContext.put(CONFIG_SOURCE_NAME, configSource);
  }
  
  public static ConfigSource getConfigSource(FunctionCompilationContext compilationContext) {
    return (ConfigSource) compilationContext.get(CONFIG_SOURCE_NAME);
  }
  
  public static void setRegionSource(FunctionCompilationContext compilationContext, RegionSource regionSource) {
    compilationContext.put(REGION_SOURCE_NAME, regionSource);
  }
  
  public static RegionSource getRegionSource(FunctionCompilationContext compilationContext) {
    return (RegionSource) compilationContext.get(REGION_SOURCE_NAME);
  }
  
  public static void setConventionBundleSource(FunctionCompilationContext compilationContext, ConventionBundleSource conventionBundleSource) {
    compilationContext.put(CONVENTION_BUNDLE_SOURCE_NAME, conventionBundleSource);
  }
  
  public static ConventionBundleSource getConventionBundleSource(FunctionCompilationContext compilationContext) {
    return (ConventionBundleSource) compilationContext.get(CONVENTION_BUNDLE_SOURCE_NAME);
  }
}
