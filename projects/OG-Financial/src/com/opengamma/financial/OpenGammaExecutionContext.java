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
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.marketdata.OverrideOperationCompiler;
import com.opengamma.financial.analytics.ircurve.calcconfig.CurveCalculationConfigSource;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.master.holiday.HolidayMaster;

/**
 * Utility methods to pull standard objects out of a {@link FunctionExecutionContext}.
 */
public final class OpenGammaExecutionContext {

  /**
   * The name under which an instance of {@link HistoricalTimeSeriesSource} should be bound.
   */
  public static final String HISTORICAL_TIME_SERIES_SOURCE_NAME = "historicalTimeSeriesSource";

  /**
   * The name under which an instance of {@link RegionSource} should be bound.
   */
  public static final String REGION_SOURCE_NAME = "regionSource";

  /**
   * The name under which an instance of {@link HolidayMaster} should be bound.
   */
  public static final String HOLIDAY_SOURCE_NAME = "holidaySource";

  /**
   * The name under which an instance of {@link ConventionBundleSource} should be bound.
   */
  private static final String CONVENTION_BUNDLE_SOURCE_NAME = "conventionBundleSource";

  /**
   * The name under which an instance of {@link ConfigSource} should be bound.
   */
  public static final String CONFIG_SOURCE_NAME = "configSource";

  /**
   * The name under which an instance of {@link ExchangeSource} should be bound.
   */
  public static final String EXCHANGE_SOURCE_NAME = "exchangeSource";

  /**
   * The name under which an instance of {@link OverrideOperationCompiler} should be bound.
   */
  public static final String OVERRIDE_OPERATION_COMPILER_NAME = "overrideOperationCompiler";

  /**
   * The name under which an instance of {@link CurveCalculationConfigSource} should be bound.
   */
  public static final String CURVE_CALCULATION_CONFIG_NAME = "curveCalculationConfigurationSource";

  /**
   * Restricted constructor.
   */
  private OpenGammaExecutionContext() {
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a {@code HistoricalTimeSeriesSource} from the context.
   * 
   * @param context  the context to examine, not null
   * @return the value, null if not found
   */
  public static HistoricalTimeSeriesSource getHistoricalTimeSeriesSource(final FunctionExecutionContext context) {
    return (HistoricalTimeSeriesSource) context.get(HISTORICAL_TIME_SERIES_SOURCE_NAME);
  }

  /**
   * Stores a {@code HistoricalTimeSeriesSource} in the context.
   * 
   * @param context  the context to store in, not null
   * @param source  the value to store, not null
   */
  public static void setHistoricalTimeSeriesSource(final FunctionExecutionContext context, final HistoricalTimeSeriesSource source) {
    context.put(HISTORICAL_TIME_SERIES_SOURCE_NAME, source);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a {@code SecuritySource} from the context.
   * 
   * @param context  the context to examine, not null
   * @return the value, null if not found
   */
  public static SecuritySource getSecuritySource(final FunctionExecutionContext context) {
    return context.getSecuritySource();
  }

  /**
   * Stores a {@code SecuritySource} in the context.
   * 
   * @param context  the context to store in, not null
   * @param securitySource  the value to store, not null
   */
  public static void setSecuritySource(final FunctionExecutionContext context, final SecuritySource securitySource) {
    context.setSecuritySource(securitySource);
  }

  /**
   * Gets a {@code ConventionBundleSource} from the context.
   * 
   * @param context  the context to examine, not null
   * @return the value, null if not found
   */
  public static ConventionBundleSource getConventionBundleSource(final FunctionExecutionContext context) {
    return (ConventionBundleSource) context.get(CONVENTION_BUNDLE_SOURCE_NAME);
  }

  /**
   * Stores a {@code ConventionBundleSource} in the context.
   * 
   * @param context  the context to store in, not null
   * @param conventionBundleSource  the value to store, not null
   */
  public static void setConventionBundleSource(final FunctionExecutionContext context, final ConventionBundleSource conventionBundleSource) {
    context.put(CONVENTION_BUNDLE_SOURCE_NAME, conventionBundleSource);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a {@code RegionSource} from the context.
   * 
   * @param context  the context to examine, not null
   * @return the value, null if not found
   */
  public static RegionSource getRegionSource(final FunctionExecutionContext context) {
    return (RegionSource) context.get(REGION_SOURCE_NAME);
  }

  /**
   * Stores a {@code RegionSource} in the context.
   * 
   * @param context  the context to store in, not null
   * @param regionSource  the value to store, not null
   */
  public static void setRegionSource(final FunctionExecutionContext context, final RegionSource regionSource) {
    context.put(REGION_SOURCE_NAME, regionSource);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a {@code HolidaySource} from the context.
   * 
   * @param context  the context to examine, not null
   * @return the value, null if not found
   */
  public static HolidaySource getHolidaySource(final FunctionExecutionContext context) {
    return (HolidaySource) context.get(HOLIDAY_SOURCE_NAME);
  }

  /**
   * Stores a {@code HolidaySource} in the context.
   * 
   * @param context  the context to store in, not null
   * @param holidaySource  the value to store, not null
   */
  public static void setHolidaySource(final FunctionExecutionContext context, final HolidaySource holidaySource) {
    context.put(HOLIDAY_SOURCE_NAME, holidaySource);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a {@code ExchangeSource} from the context.
   * 
   * @param context  the context to examine, not null
   * @return the value, null if not found
   */
  public static ExchangeSource getExchangeSource(final FunctionExecutionContext context) {
    return (ExchangeSource) context.get(EXCHANGE_SOURCE_NAME);
  }

  /**
   * Stores a {@code ExchangeSource} in the context.
   * 
   * @param context  the context to store in, not null
   * @param exchangeSource  the value to store, not null
   */
  public static void setExchangeSource(final FunctionExecutionContext context, final ExchangeSource exchangeSource) {
    context.put(EXCHANGE_SOURCE_NAME, exchangeSource);
  }

  /**
   * Gets a {@code CurveCalculationConfigSource} from the context.
   * 
   * @param context  the context to examine, not null
   * @return the curve config source, null if not found
   */
  public static CurveCalculationConfigSource getCurveCalculationConfigSource(final FunctionExecutionContext context) {
    return (CurveCalculationConfigSource) context.get(CURVE_CALCULATION_CONFIG_NAME);
  }

  /**
   * Stores a {@code CurveCalculationConfigSource} in the context.
   * 
   * @param context  the context to store in, not null
   * @param curveConfigSource  the curve config source to store, not null
   */
  public static void setCurveCalculationConfigSource(final FunctionExecutionContext context, final CurveCalculationConfigSource curveConfigSource) {
    context.put(CURVE_CALCULATION_CONFIG_NAME, curveConfigSource);
  }
  //-------------------------------------------------------------------------
  /**
   * Gets a {@code ConfigSource} from the context.
   * 
   * @param context  the context to examine, not null
   * @return the value, null if not found
   */
  public static ConfigSource getConfigSource(final FunctionExecutionContext context) {
    return (ConfigSource) context.get(CONFIG_SOURCE_NAME);
  }

  /**
   * Stores a {@code ConfigSource} in the context.
   * 
   * @param context  the context to store in, not null
   * @param configSource  the value to store, not null
   */
  public static void setConfigSource(final FunctionExecutionContext context, final ConfigSource configSource) {
    context.put(CONFIG_SOURCE_NAME, configSource);
  }

  public static OverrideOperationCompiler getOverrideOperationCompiler(final FunctionExecutionContext context) {
    return (OverrideOperationCompiler) context.get(OVERRIDE_OPERATION_COMPILER_NAME);
  }

  public static void setOverrideOperationCompiler(final FunctionExecutionContext context, final OverrideOperationCompiler overrideOperationCompiler) {
    context.put(OVERRIDE_OPERATION_COMPILER_NAME, overrideOperationCompiler);
  }
}
