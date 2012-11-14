/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionSource;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationBuilder;
import com.opengamma.financial.analytics.ircurve.calcconfig.CurveCalculationConfigSource;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinitionSource;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.currency.CurrencyMatrixSource;
import com.opengamma.financial.temptarget.TempTargetRepository;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;

/**
 * Utility methods to pull standard objects out of a {@link FunctionCompilationContext}.
 */
public final class OpenGammaCompilationContext {

  /**
   * The name under which an instance of {@link ConfigSource} should be bound.
   * <p>
   * Where possible, components should not be tightly coupled to the configuration database. An intermediate interface, with an implementation that is backed by a ConfigSource, allows the flexibility
   * to source that data from an external system, or a more efficient storage mechanism, in the future.
   */
  public static final String CONFIG_SOURCE_NAME = "configSource";
  /**
   * The name under which an instance of {@link RegionSource} should be bound.
   */
  public static final String REGION_SOURCE_NAME = "regionSource";
  /**
   * The name under which an instance of {@link ConvensionBundleSource} should be bound.
   */
  public static final String CONVENTION_BUNDLE_SOURCE_NAME = "conventionBundleSource";
  /**
   * The name under which an instance of {@link InterpolatedYieldCurveDefinitionSource} should be bound.
   */
  public static final String INTERPOLATED_YIELD_CURVE_DEFINITION_SOURCE_NAME = "interpolatedYieldCurveDefinitionSource";
  /**
   * The name under which an instance of {@link InterpolatedYieldCurveSpecificationBuilder} should be bound.
   */
  public static final String INTERPOLATED_YIELD_CURVE_SPECIFICATION_BUILDER_NAME = "interpolatedYieldCurveSpecificationBuilder";
  /**
   * The name under which an instance of {@link VolatilityCubeDefinitionSource} should be bound.
   */
  public static final String VOLATILITY_CUBE_DEFINITION_SOURCE_NAME = "volatilityCubeDefinitionSource";
  /**
   * The name under which an instance of {@link CurrencyMatrixSource} should be bound.
   */
  public static final String CURRENCY_MATRIX_SOURCE_NAME = "currencyMatrixSource";
  /**
   * The name under which an instance of {@link HolidaySource} should be bound.
   */
  public static final String HOLIDAY_SOURCE_NAME = "holidaySource";
  /**
   * The name under which an instance of {@link ExchangeSource} should be bound.
   */
  public static final String EXCHANGE_SOURCE_NAME = "exchangeSource";
  /**
   * The name under which an instance of {@link SecuritySource} should be bound.
   */
  public static final String SECURITY_SOURCE_NAME = "securitySource";
  /**
   * The name under which an instance of {@link CurveCalculationConfigSource} should be bound.
   */
  public static final String CURVE_CALCULATION_CONFIG_NAME = "curveCalculationConfigurationSource";
  /**
   * The name under which an instance of {@link HistoricalTimeSeriesSource} should be bound.
   */
  public static final String HISTORICAL_TIME_SERIES_SOURCE_NAME = "historicalTimeSeriesSource";
  /**
   * The name under which an instance of {@link HistoricalTimeSeriesResolver} should be bound.
   */
  public static final String HISTORICAL_TIME_SERIES_RESOLVER_NAME = "historicalTimeSeriesResolver";
  /**
   * The name under which an instance of {@link TempTargetRepository} should be bound.
   */
  public static final String TEMPORARY_TARGETS_NAME = "tempTargets";

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
  public static ConfigSource getConfigSource(final FunctionCompilationContext compilationContext) {
    return get(compilationContext, CONFIG_SOURCE_NAME);
  }

  /**
   * Stores a {@code ConfigSource} in the context.
   * @param compilationContext  the context to store in, not null
   * @param configSource  the config source to store, not null
   */
  public static void setConfigSource(final FunctionCompilationContext compilationContext, final ConfigSource configSource) {
    set(compilationContext, CONFIG_SOURCE_NAME, configSource);
  }

  // -------------------------------------------------------------------------
  /**
   * Gets a {@code RegionSource} from the context.
   * @param compilationContext  the context to examine, not null
   * @return the region source, null if not found
   */
  public static RegionSource getRegionSource(final FunctionCompilationContext compilationContext) {
    return get(compilationContext, REGION_SOURCE_NAME);
  }

  /**
   * Stores a {@code RegionSource} in the context.
   * @param compilationContext  the context to store in, not null
   * @param regionSource  the region source to store, not null
   */
  public static void setRegionSource(final FunctionCompilationContext compilationContext, final RegionSource regionSource) {
    set(compilationContext, REGION_SOURCE_NAME, regionSource);
  }

  // -------------------------------------------------------------------------
  /**
   * Gets a {@code ConventionBundleSource} from the context.
   * @param compilationContext  the context to examine, not null
   * @return the convention bundle source, null if not found
   */
  public static ConventionBundleSource getConventionBundleSource(final FunctionCompilationContext compilationContext) {
    return get(compilationContext, CONVENTION_BUNDLE_SOURCE_NAME);
  }

  /**
   * Stores a {@code ConventionBundleSource} in the context.
   * @param compilationContext  the context to store in, not null
   * @param conventionBundleSource  the convention bundle source to store, not null
   */
  public static void setConventionBundleSource(final FunctionCompilationContext compilationContext,
      final ConventionBundleSource conventionBundleSource) {
    set(compilationContext, CONVENTION_BUNDLE_SOURCE_NAME, conventionBundleSource);
  }

  public static InterpolatedYieldCurveDefinitionSource getInterpolatedYieldCurveDefinitionSource(
      final FunctionCompilationContext compilationContext) {
    return get(compilationContext, INTERPOLATED_YIELD_CURVE_DEFINITION_SOURCE_NAME);
  }

  public static void setInterpolatedYieldCurveDefinitionSource(final FunctionCompilationContext compilationContext,
      final InterpolatedYieldCurveDefinitionSource source) {
    set(compilationContext, INTERPOLATED_YIELD_CURVE_DEFINITION_SOURCE_NAME, source);
  }

  public static InterpolatedYieldCurveSpecificationBuilder getInterpolatedYieldCurveSpecificationBuilder(
      final FunctionCompilationContext compilationContext) {
    return get(compilationContext, INTERPOLATED_YIELD_CURVE_SPECIFICATION_BUILDER_NAME);
  }

  public static void setInterpolatedYieldCurveSpecificationBuilder(final FunctionCompilationContext compilationContext,
      final InterpolatedYieldCurveSpecificationBuilder builder) {
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

  public static void setCurrencyMatrixSource(final FunctionCompilationContext compilationContext,
      final CurrencyMatrixSource currencyMatrixSource) {
    set(compilationContext, CURRENCY_MATRIX_SOURCE_NAME, currencyMatrixSource);
  }

  public static HolidaySource getHolidaySource(final FunctionCompilationContext compilationContext) {
    return get(compilationContext, HOLIDAY_SOURCE_NAME);
  }

  public static void setHolidaySource(final FunctionCompilationContext compilationContext, final HolidaySource holidaySource) {
    set(compilationContext, HOLIDAY_SOURCE_NAME, holidaySource);
  }

  public static ExchangeSource getExchangeSource(final FunctionCompilationContext compilationContext) {
    return get(compilationContext, EXCHANGE_SOURCE_NAME);
  }

  public static void setExchangeSource(final FunctionCompilationContext compilationContext, final ExchangeSource exchangeSource) {
    set(compilationContext, EXCHANGE_SOURCE_NAME, exchangeSource);
  }

  public static SecuritySource getSecuritySource(final FunctionCompilationContext compilationContext) {
    return get(compilationContext, SECURITY_SOURCE_NAME);
  }

  public static void setSecuritySource(final FunctionCompilationContext compilationContext, final SecuritySource securitySource) {
    set(compilationContext, SECURITY_SOURCE_NAME, securitySource);
  }

  /**
   * Gets a {@code CurveCalculationConfigSource} from the context.
   * @param compilationContext  the context to examine, not null
   * @return the curve config source, null if not found
   */
  public static CurveCalculationConfigSource getCurveCalculationConfigSource(final FunctionCompilationContext compilationContext) {
    return get(compilationContext, CURVE_CALCULATION_CONFIG_NAME);
  }

  /**
   * Stores a {@code CurveCalculationConfigSource} in the context.
   * @param compilationContext  the context to store in, not null
   * @param curveConfigSource  the curve config source to store, not null
   */
  public static void setCurveCalculationConfigSource(final FunctionCompilationContext compilationContext, final CurveCalculationConfigSource curveConfigSource) {
    set(compilationContext, CURVE_CALCULATION_CONFIG_NAME, curveConfigSource);
  }

  public static HistoricalTimeSeriesSource getHistoricalTimeSeriesSource(final FunctionCompilationContext compilationContext) {
    return get(compilationContext, HISTORICAL_TIME_SERIES_SOURCE_NAME);
  }

  public static void setHistoricalTimeSeriesSource(final FunctionCompilationContext compilationContext, final HistoricalTimeSeriesSource historicalTimeSeriesSource) {
    set(compilationContext, HISTORICAL_TIME_SERIES_SOURCE_NAME, historicalTimeSeriesSource);
  }

  public static HistoricalTimeSeriesResolver getHistoricalTimeSeriesResolver(final FunctionCompilationContext compilationContext) {
    return get(compilationContext, HISTORICAL_TIME_SERIES_RESOLVER_NAME);
  }

  public static void setHistoricalTimeSeriesResolver(final FunctionCompilationContext compilationContext, final HistoricalTimeSeriesResolver historicalTimeSeriesResolver) {
    set(compilationContext, HISTORICAL_TIME_SERIES_RESOLVER_NAME, historicalTimeSeriesResolver);
  }

  public static TempTargetRepository getTempTargets(final FunctionCompilationContext compilationContext) {
    return get(compilationContext, TEMPORARY_TARGETS_NAME);
  }

  public static void setTempTargets(final FunctionCompilationContext compilationContext, final TempTargetRepository tempTargets) {
    set(compilationContext, TEMPORARY_TARGETS_NAME, tempTargets);
  }

}
