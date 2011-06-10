/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionSource;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationBuilder;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinitionSource;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.currency.CurrencyMatrixSource;

/**
 * Utility methods to pull standard objects out of a {@link FunctionCompilationContext}.
 */
public final class OpenGammaCompilationContext {

  private static final String CONFIG_SOURCE_NAME = "configSource";
  private static final String REGION_SOURCE_NAME = "regionSource";
  private static final String CONVENTION_BUNDLE_SOURCE_NAME = "conventionBundleSource";
  private static final String INTERPOLATED_YIELD_CURVE_DEFINITION_SOURCE_NAME = "interpolatedYieldCurveDefinitionSource";
  private static final String INTERPOLATED_YIELD_CURVE_SPECIFICATION_BUILDER_NAME = "interpolatedYieldCurveSpecificationBuilder";
  private static final String VOLATILITY_CUBE_DEFINITION_SOURCE_NAME = "volatilityCubeDefinitionSource";
  private static final String CURRENCY_MATRIX_SOURCE_NAME = "currencyMatrixSource";

  /**
   * Restricted constructor.
   */
  private OpenGammaCompilationContext() {
  }

  @SuppressWarnings("unchecked")
  private static <T> T get(final FunctionCompilationContext context, final String key) {
    return (T) context.get(key);
  }

  private static <T> void set(final FunctionCompilationContext context, final String key, final T value) {
    context.put(key, value);
  }

  // -------------------------------------------------------------------------
  /**
   * Gets a {@code ConfigSource} from the context.
   * @param compilationContext  the context to examine, not null
   * @return the config source, null if not found
   */
  public static ConfigSource getConfigSource(FunctionCompilationContext compilationContext) {
    return get(compilationContext, CONFIG_SOURCE_NAME);
  }

  /**
   * Stores a {@code ConfigSource} in the context.
   * @param compilationContext  the context to store in, not null
   * @param configSource  the config source to store, not null
   */
  public static void setConfigSource(FunctionCompilationContext compilationContext, ConfigSource configSource) {
    set(compilationContext, CONFIG_SOURCE_NAME, configSource);
  }

  // -------------------------------------------------------------------------
  /**
   * Gets a {@code RegionSource} from the context.
   * @param compilationContext  the context to examine, not null
   * @return the region source, null if not found
   */
  public static RegionSource getRegionSource(FunctionCompilationContext compilationContext) {
    return get(compilationContext, REGION_SOURCE_NAME);
  }

  /**
   * Stores a {@code RegionSource} in the context.
   * @param compilationContext  the context to store in, not null
   * @param regionSource  the region source to store, not null
   */
  public static void setRegionSource(FunctionCompilationContext compilationContext, RegionSource regionSource) {
    set(compilationContext, REGION_SOURCE_NAME, regionSource);
  }

  // -------------------------------------------------------------------------
  /**
   * Gets a {@code ConventionBundleSource} from the context.
   * @param compilationContext  the context to examine, not null
   * @return the convention bundle source, null if not found
   */
  public static ConventionBundleSource getConventionBundleSource(FunctionCompilationContext compilationContext) {
    return get(compilationContext, CONVENTION_BUNDLE_SOURCE_NAME);
  }

  /**
   * Stores a {@code ConventionBundleSource} in the context.
   * @param compilationContext  the context to store in, not null
   * @param conventionBundleSource  the convention bundle source to store, not null
   */
  public static void setConventionBundleSource(FunctionCompilationContext compilationContext, ConventionBundleSource conventionBundleSource) {
    set(compilationContext, CONVENTION_BUNDLE_SOURCE_NAME, conventionBundleSource);
  }

  public static InterpolatedYieldCurveDefinitionSource getInterpolatedYieldCurveDefinitionSource(final FunctionCompilationContext compilationContext) {
    return get(compilationContext, INTERPOLATED_YIELD_CURVE_DEFINITION_SOURCE_NAME);
  }

  public static void setInterpolatedYieldCurveDefinitionSource(final FunctionCompilationContext compilationContext, final InterpolatedYieldCurveDefinitionSource source) {
    set(compilationContext, INTERPOLATED_YIELD_CURVE_DEFINITION_SOURCE_NAME, source);
  }

  public static InterpolatedYieldCurveSpecificationBuilder getInterpolatedYieldCurveSpecificationBuilder(final FunctionCompilationContext compilationContext) {
    return get(compilationContext, INTERPOLATED_YIELD_CURVE_SPECIFICATION_BUILDER_NAME);
  }

  public static void setInterpolatedYieldCurveSpecificationBuilder(final FunctionCompilationContext compilationContext, final InterpolatedYieldCurveSpecificationBuilder builder) {
    set(compilationContext, INTERPOLATED_YIELD_CURVE_SPECIFICATION_BUILDER_NAME, builder);
  }
  
  public static VolatilityCubeDefinitionSource getVolatilityCubeDefinitionSource(final FunctionCompilationContext compilationContext) {
    return get(compilationContext, VOLATILITY_CUBE_DEFINITION_SOURCE_NAME);
  }

  public static void setVolatilityCubeDefinitionSource(final FunctionCompilationContext compilationContext, final VolatilityCubeDefinitionSource source) {
    set(compilationContext, VOLATILITY_CUBE_DEFINITION_SOURCE_NAME, source);
  }

  public static CurrencyMatrixSource getCurrencyMatrixSource(final FunctionCompilationContext compilationContext) {
    return get(compilationContext, CURRENCY_MATRIX_SOURCE_NAME);
  }

  public static void setCurrencyMatrixSource(final FunctionCompilationContext compilationContext, final CurrencyMatrixSource currencyMatrixSource) {
    set(compilationContext, CURRENCY_MATRIX_SOURCE_NAME, currencyMatrixSource);
  }

}
