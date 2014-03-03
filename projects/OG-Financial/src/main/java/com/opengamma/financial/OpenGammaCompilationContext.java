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
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionSource;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationBuilder;
import com.opengamma.financial.analytics.model.pnl.PnLRequirementsGatherer;
import com.opengamma.financial.analytics.riskfactors.RiskFactorsGatherer;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinitionSource;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.currency.CurrencyPairsResolver;
import com.opengamma.financial.currency.CurrencyPairsSource;
import com.opengamma.financial.temptarget.TempTargetRepository;
import com.opengamma.id.ExternalId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

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
   * The name under which an instance of {@link ConventionBundleSource} should be bound.
   */
  public static final String CONVENTION_BUNDLE_SOURCE_NAME = "conventionBundleSource";
  /**
   * The name under which an instance of {@link ConventionSource} should be bound.
   */
  public static final String CONVENTION_SOURCE_NAME = "conventionSource";
  /**
   * The name under which an instance of {@link InterpolatedYieldCurveDefinitionSource} should be bound.
   * 
   * @deprecated use a config source to look up the object
   */
  @Deprecated
  public static final String INTERPOLATED_YIELD_CURVE_DEFINITION_SOURCE_NAME = "interpolatedYieldCurveDefinitionSource";
  /**
   * The name under which an instance of {@link InterpolatedYieldCurveSpecificationBuilder} should be bound.
   * 
   * @deprecated use a config source to look up the object
   */
  @Deprecated
  public static final String INTERPOLATED_YIELD_CURVE_SPECIFICATION_BUILDER_NAME = "interpolatedYieldCurveSpecificationBuilder";
  /**
   * The name under which an instance of {@link VolatilityCubeDefinitionSource} should be bound.
   * 
   * @deprecated use a config source to look up the object
   */
  @Deprecated
  public static final String VOLATILITY_CUBE_DEFINITION_SOURCE_NAME = "volatilityCubeDefinitionSource";
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
   * The name under which an instance of {@link RiskFactorsGatherer} should be bound.
   */
  public static final String RISK_FACTORS_GATHERER_NAME = "riskFactorsGatherer";
  /**
   * The name under which a {@link Boolean#TRUE} value should be bound to put functions which support it into a permissive requirement mode. Note that this is a non-default mode of behavior that is
   * not usually required.
   */
  private static final String PERMISSIVE_FLAG_NAME = "permissive";
  /**
   * The name under which an instance of {@link PnLRequirementsGatherer} should be bound.
   */
  public static final String PNL_REQUIREMENTS_GATHERER_NAME = "pnlRequirementsGatherer";

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
   * 
   * @param compilationContext the context to examine, not null
   * @return the config source, null if not found
   */
  public static ConfigSource getConfigSource(final FunctionCompilationContext compilationContext) {
    return get(compilationContext, CONFIG_SOURCE_NAME);
  }

  /**
   * Stores a {@code ConfigSource} in the context.
   * 
   * @param compilationContext the context to store in, not null
   * @param configSource the config source to store, not null
   */
  public static void setConfigSource(final FunctionCompilationContext compilationContext, final ConfigSource configSource) {
    set(compilationContext, CONFIG_SOURCE_NAME, configSource);
  }

  // -------------------------------------------------------------------------
  /**
   * Gets a {@code RegionSource} from the context.
   * 
   * @param compilationContext the context to examine, not null
   * @return the region source, null if not found
   */
  public static RegionSource getRegionSource(final FunctionCompilationContext compilationContext) {
    return get(compilationContext, REGION_SOURCE_NAME);
  }

  /**
   * Stores a {@code RegionSource} in the context.
   * 
   * @param compilationContext the context to store in, not null
   * @param regionSource the region source to store, not null
   */
  public static void setRegionSource(final FunctionCompilationContext compilationContext, final RegionSource regionSource) {
    set(compilationContext, REGION_SOURCE_NAME, regionSource);
  }

  // -------------------------------------------------------------------------
  /**
   * Gets a {@code ConventionSource} from the context.
   * 
   * @param compilationContext the context to examine, not null
   * @return the convention source, null if not found
   */
  public static ConventionSource getConventionSource(final FunctionCompilationContext compilationContext) {
    return get(compilationContext, CONVENTION_SOURCE_NAME);
  }

  /**
   * Stores a {@code ConventionSource} in the context.
   * 
   * @param compilationContext the context to store in, not null
   * @param conventionSource the convention source to store, not null
   */
  public static void setConventionSource(final FunctionCompilationContext compilationContext, final ConventionSource conventionSource) {
    set(compilationContext, CONVENTION_SOURCE_NAME, conventionSource);
  }

  // -------------------------------------------------------------------------
  /**
   * Gets a {@code ConventionBundleSource} from the context.
   * 
   * @param compilationContext the context to examine, not null
   * @return the convention bundle source, null if not found
   */
  public static ConventionBundleSource getConventionBundleSource(final FunctionCompilationContext compilationContext) {
    return get(compilationContext, CONVENTION_BUNDLE_SOURCE_NAME);
  }

  /**
   * Stores a {@code ConventionBundleSource} in the context.
   * 
   * @param compilationContext the context to store in, not null
   * @param conventionBundleSource the convention bundle source to store, not null
   */
  public static void setConventionBundleSource(final FunctionCompilationContext compilationContext, final ConventionBundleSource conventionBundleSource) {
    set(compilationContext, CONVENTION_BUNDLE_SOURCE_NAME, conventionBundleSource);
  }

  /**
   * @deprecated Use config source instead.
   * @param compilationContext the compilation context
   * @return the InterpolatedYieldCurveDefinitionSource
   */
  @Deprecated
  public static InterpolatedYieldCurveDefinitionSource getInterpolatedYieldCurveDefinitionSource(final FunctionCompilationContext compilationContext) {
    return get(compilationContext, INTERPOLATED_YIELD_CURVE_DEFINITION_SOURCE_NAME);
  }

  /**
   * @deprecated Use config source instead.
   * @param compilationContext the compilation context
   * @param source the InterpolatedYieldCurveDefinitionSource
   */
  @Deprecated
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

  public static HolidaySource getHolidaySource(final FunctionCompilationContext compilationContext) {
    return get(compilationContext, HOLIDAY_SOURCE_NAME);
  }

  public static void setHolidaySource(final FunctionCompilationContext compilationContext, final HolidaySource holidaySource) {
    set(compilationContext, HOLIDAY_SOURCE_NAME, holidaySource);
  }

  public static LegalEntitySource getLegalEntitySource(final FunctionCompilationContext compilationContext) {
    return compilationContext.getLegalEntitySource();
  }

  public static void setLegalEntitySource(final FunctionCompilationContext compilationContext, final LegalEntitySource legalEntitySource) {
    compilationContext.setLegalEntitySource(legalEntitySource);
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

  /**
   * Tests whether functions should allow more permissive constraints. Permissive behavior, if implemented by a function, will prefer to satisfy a constraint by assuming (possibly inappropriate)
   * values rather than abandon the production. This increases the chance of a successful graph build for an inaccurately specified view but the graph may not be as the user intended/expected.
   * <p>
   * This flag is off by default.
   * 
   * @param compilationContext the context to test, not null
   * @return true if permissive behavior is enabled, false otherwise.
   */
  public static boolean isPermissive(final FunctionCompilationContext compilationContext) {
    return get(compilationContext, PERMISSIVE_FLAG_NAME) != null;
  }

  /**
   * Sets whether functions should allow more permissive constraints. Permissive behavior, if implemented by a function, will prefer to satisfy a constraint by assuming (possibly inappropriate) values
   * rather than abandon the production. This increases the chance of a successful graph build for an inaccurately specified view but the graph may not be as the user intended/expected.
   * <p>
   * This flag is off by default.
   * 
   * @param compilationContext the context to update, not null
   * @param permissive true to enable permissive behavior, false to disable it
   */
  public static void setPermissive(final FunctionCompilationContext compilationContext, final boolean permissive) {
    if (permissive) {
      set(compilationContext, PERMISSIVE_FLAG_NAME, Boolean.TRUE);
    } else {
      compilationContext.remove(PERMISSIVE_FLAG_NAME);
    }
  }

  public static RiskFactorsGatherer getRiskFactorsGatherer(final FunctionCompilationContext compilationContext) {
    return get(compilationContext, RISK_FACTORS_GATHERER_NAME);
  }

  public static void setRiskFactorsGatherer(final FunctionCompilationContext compilationContext, final RiskFactorsGatherer riskFactorsGatherer) {
    set(compilationContext, RISK_FACTORS_GATHERER_NAME, riskFactorsGatherer);
  }

  public static PnLRequirementsGatherer getPnLRequirementsGatherer(final FunctionCompilationContext compilationContext) {
    return get(compilationContext, PNL_REQUIREMENTS_GATHERER_NAME);
  }

  public static void setPnLRequirementsGatherer(final FunctionCompilationContext compilationContext, final PnLRequirementsGatherer pnlRequirementsGatherer) {
    set(compilationContext, PNL_REQUIREMENTS_GATHERER_NAME, pnlRequirementsGatherer);
  }

  /**
   * @deprecated [PLAT-2782] interim measure to request data via function inputs, or targets
   */
  @Deprecated
  public static CurrencyPairsSource getCurrencyPairsSource(final FunctionCompilationContext compilationContext) {
    final ComputationTargetResolverWrapper resolver = new ComputationTargetResolverWrapper(compilationContext.getComputationTargetResolver());
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
