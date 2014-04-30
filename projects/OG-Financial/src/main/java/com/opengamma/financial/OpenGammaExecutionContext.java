/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.marketdata.OverrideOperationCompiler;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.financial.analytics.ircurve.calcconfig.CurveCalculationConfigSource;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.currency.CurrencyPairsResolver;
import com.opengamma.financial.currency.CurrencyPairsSource;
import com.opengamma.id.ExternalId;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

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
   * The name under which an instance of {@link ConventionSource} should be bound.
   */
  private static final String CONVENTION_SOURCE_NAME = "conventionSource";

  /**
   * The name under which an instance of {@link ConfigMaster} should be bound. The config source must return elements from this master, but may return additional elements other sources/masters too.
   * <p>
   * This might only be a temporary addition; most services should be written to back onto this if necessary rather than data be accessed directly from the config master. This allows the flexibility
   * to have data stored in another system or more efficient storage specific to that type.
   * <p>
   * This is currently required to replace the functionality previously offered by ViewDefinitionRepository which exposed both user maintained views from the persistent config master and
   * temporary/short-lived views created programatically.
   */
  public static final String CONFIG_MASTER_NAME = "configMaster";

  /**
   * The name under which an instance of {@link ConfigSource} should be bound.
   * <p>
   * Where possible, components should not be tightly coupled to the configuration database. An intermediate interface, with an implementation that is backed by a ConfigSource, allows the flexibility
   * to source that data from an external system, or a more efficient storage mechanism, in the future.
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
   * The name under which an instance of {@link ViewProcessor} should be bound. The view processor might not be the same one that an execution is being performed on behalf of, but one which can be
   * used for nested/slave computations. The view processor should use the {@link #CONFIG_MASTER_NAME} from this context so that dynamically created view definitions are visible.
   */
  public static final String VIEW_PROCESSOR_NAME = "viewProcessor";

  private static final String CURRENCY_PAIRS_SOURCE = "currencyPairsSource";

  /**
   * Restricted constructor.
   */
  private OpenGammaExecutionContext() {
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a {@code HistoricalTimeSeriesSource} from the context.
   * 
   * @param context the context to examine, not null
   * @return the value, null if not found
   */
  public static HistoricalTimeSeriesSource getHistoricalTimeSeriesSource(final FunctionExecutionContext context) {
    return (HistoricalTimeSeriesSource) context.get(HISTORICAL_TIME_SERIES_SOURCE_NAME);
  }

  /**
   * Stores a {@code HistoricalTimeSeriesSource} in the context.
   * 
   * @param context the context to store in, not null
   * @param source the value to store, not null
   */
  public static void setHistoricalTimeSeriesSource(final FunctionExecutionContext context, final HistoricalTimeSeriesSource source) {
    context.put(HISTORICAL_TIME_SERIES_SOURCE_NAME, source);
  }

  /**
   * Gets a {@code SecuritySource} from the context.
   * 
   * @param context the context to examine, not null
   * @return the value, null if not found
   */
  public static SecuritySource getSecuritySource(final FunctionExecutionContext context) {
    return context.getSecuritySource();
  }

  /**
   * Stores a {@code SecuritySource} in the context.
   * 
   * @param context the context to store in, not null
   * @param securitySource the value to store, not null
   */
  public static void setSecuritySource(final FunctionExecutionContext context, final SecuritySource securitySource) {
    context.setSecuritySource(securitySource);
  }

  /**
   * Gets a {@code ConventionBundleSource} from the context.
   * 
   * @param context the context to examine, not null
   * @return the value, null if not found
   */
  public static ConventionBundleSource getConventionBundleSource(final FunctionExecutionContext context) {
    return (ConventionBundleSource) context.get(CONVENTION_BUNDLE_SOURCE_NAME);
  }

  /**
   * Stores a {@code ConventionBundleSource} in the context.
   * 
   * @param context the context to store in, not null
   * @param conventionBundleSource the value to store, not null
   */
  public static void setConventionBundleSource(final FunctionExecutionContext context, final ConventionBundleSource conventionBundleSource) {
    context.put(CONVENTION_BUNDLE_SOURCE_NAME, conventionBundleSource);
  }

  /**
   * Gets a {@link ConventionSource} from the context.
   * 
   * @param context the context to examine, not null
   * @return the value, null if not found
   */
  public static ConventionSource getConventionSource(final FunctionExecutionContext context) {
    return (ConventionSource) context.get(CONVENTION_SOURCE_NAME);
  }

  /**
   * Stores a {@code ConventionSource} in the context.
   * 
   * @param context the context to store in, not null
   * @param conventionSource the value to store, not null
   */
  public static void setConventionSource(final FunctionExecutionContext context, final ConventionSource conventionSource) {
    context.put(CONVENTION_SOURCE_NAME, conventionSource);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a {@code RegionSource} from the context.
   * 
   * @param context the context to examine, not null
   * @return the value, null if not found
   */
  public static RegionSource getRegionSource(final FunctionExecutionContext context) {
    return (RegionSource) context.get(REGION_SOURCE_NAME);
  }

  /**
   * Stores a {@code RegionSource} in the context.
   * 
   * @param context the context to store in, not null
   * @param regionSource the value to store, not null
   */
  public static void setRegionSource(final FunctionExecutionContext context, final RegionSource regionSource) {
    context.put(REGION_SOURCE_NAME, regionSource);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a {@code HolidaySource} from the context.
   * 
   * @param context the context to examine, not null
   * @return the value, null if not found
   */
  public static HolidaySource getHolidaySource(final FunctionExecutionContext context) {
    return (HolidaySource) context.get(HOLIDAY_SOURCE_NAME);
  }

  /**
   * Stores a {@code HolidaySource} in the context.
   * 
   * @param context the context to store in, not null
   * @param holidaySource the value to store, not null
   */
  public static void setHolidaySource(final FunctionExecutionContext context, final HolidaySource holidaySource) {
    context.put(HOLIDAY_SOURCE_NAME, holidaySource);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a {@code LegalEntitySource} from the context.
   *
   * @param context the context to examine, not null
   * @return the value, null if not found
   */
  public static LegalEntitySource getLegalEntitySource(final FunctionExecutionContext context) {
    return context.getLegalEntitySource();
  }

  /**
   * Stores a {@code LegalEntitySource} in the context.
   *
   * @param context the context to store in, not null
   * @param legalEntitySource the value to store, not null
   */
  public static void setLegalEntitySource(final FunctionExecutionContext context, final LegalEntitySource legalEntitySource) {
    context.setLegalEntitySource(legalEntitySource);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a {@code ExchangeSource} from the context.
   * 
   * @param context the context to examine, not null
   * @return the value, null if not found
   */
  public static ExchangeSource getExchangeSource(final FunctionExecutionContext context) {
    return (ExchangeSource) context.get(EXCHANGE_SOURCE_NAME);
  }

  /**
   * Stores a {@code ExchangeSource} in the context.
   * 
   * @param context the context to store in, not null
   * @param exchangeSource the value to store, not null
   */
  public static void setExchangeSource(final FunctionExecutionContext context, final ExchangeSource exchangeSource) {
    context.put(EXCHANGE_SOURCE_NAME, exchangeSource);
  }

  /**
   * Gets a {@code CurveCalculationConfigSource} from the context.
   * 
   * @param context the context to examine, not null
   * @return the curve config source, null if not found
   */
  public static CurveCalculationConfigSource getCurveCalculationConfigSource(final FunctionExecutionContext context) {
    return (CurveCalculationConfigSource) context.get(CURVE_CALCULATION_CONFIG_NAME);
  }

  /**
   * Stores a {@code CurveCalculationConfigSource} in the context.
   * 
   * @param context the context to store in, not null
   * @param curveConfigSource the curve config source to store, not null
   */
  public static void setCurveCalculationConfigSource(final FunctionExecutionContext context, final CurveCalculationConfigSource curveConfigSource) {
    context.put(CURVE_CALCULATION_CONFIG_NAME, curveConfigSource);
  }

  /**
   * Gets a {@code ConfigMaster} from the context.
   * 
   * @param context the context to examine, not null
   * @return the value, null if not found
   */
  public static ConfigMaster getConfigMaster(final FunctionExecutionContext context) {
    return (ConfigMaster) context.get(CONFIG_MASTER_NAME);
  }

  /**
   * Stores a {@code ConfigMaster} in the context.
   * 
   * @param context the context to store in, not null
   * @param configMaster the config master instance to store, not null
   */
  public static void setConfigMaster(final FunctionExecutionContext context, final ConfigMaster configMaster) {
    context.put(CONFIG_MASTER_NAME, configMaster);
  }

  /**
   * Gets a {@code ConfigSource} from the context.
   * 
   * @param context the context to examine, not null
   * @return the value, null if not found
   */
  public static ConfigSource getConfigSource(final FunctionExecutionContext context) {
    return (ConfigSource) context.get(CONFIG_SOURCE_NAME);
  }

  /**
   * Stores a {@code ConfigSource} in the context.
   * 
   * @param context the context to store in, not null
   * @param configSource the value to store, not null
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

  public static ViewProcessor getViewProcessor(final FunctionExecutionContext context) {
    return (ViewProcessor) context.get(VIEW_PROCESSOR_NAME);
  }

  public static void setViewProcessor(final FunctionExecutionContext context, final ViewProcessor viewProcessor) {
    context.put(VIEW_PROCESSOR_NAME, viewProcessor);
  }

  /**
   * @deprecated [PLAT-2782] interim measure to move away from direct use of a config source
   */
  @Deprecated
  public static CurrencyPairsSource getCurrencyPairsSource(final FunctionExecutionContext context) {
    final ComputationTargetResolverWrapper resolver = new ComputationTargetResolverWrapper(context.getComputationTargetResolver());
    return new CurrencyPairsSource() {

      @Override
      public CurrencyPairs getCurrencyPairs(String name) {
        if (name == null) {
          name = CurrencyPairs.DEFAULT_CURRENCY_PAIRS;
        }
        return (CurrencyPairs) resolver.get(CurrencyPairs.TYPE, ExternalId.of(CurrencyPairsResolver.IDENTIFIER_SCHEME, name));
      }

      @Override
      public CurrencyPair getCurrencyPair(final String name, final Currency currency1, final Currency currency2) {
        ArgumentChecker.notNull(currency1, "currency1");
        ArgumentChecker.notNull(currency2, "currency2");
        final CurrencyPairs currencyPairs = getCurrencyPairs(name);
        if (currencyPairs == null) {
          return null;
        }
        return currencyPairs.getCurrencyPair(currency1, currency2);
      }
    };
  }

}
