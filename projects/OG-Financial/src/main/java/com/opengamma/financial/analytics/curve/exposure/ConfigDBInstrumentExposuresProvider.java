/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.exposure;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.financial.config.ConfigSourceQuery;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Gets the name(s) of the curve construction configurations to be used in pricing a security
 * from a {@link ConfigSource}.
 */
public class ConfigDBInstrumentExposuresProvider implements InstrumentExposuresProvider {
  /** The configuration source */
  private final ConfigSourceQuery<ExposureFunctions> _query;
  /** The security source */
  private final SecuritySource _securitySource;

  /**
   * @param configSource The config source, not null
   * @param securitySource The security source, not null
   * @deprecated Use the form which takes a {@link VersionCorrection} instance instead
   */
  @Deprecated
  public ConfigDBInstrumentExposuresProvider(final ConfigSource configSource, final SecuritySource securitySource) {
    this(configSource, securitySource, VersionCorrection.LATEST);
  }

  /**
   * @param configSource The config source, not null
   * @param securitySource The security source, not null
   * @param configVersionCorrection The version/correction timestamp to make queries to the configuration with, not null
   */
  public ConfigDBInstrumentExposuresProvider(final ConfigSource configSource, final SecuritySource securitySource, final VersionCorrection configVersionCorrection) {
    this(new ConfigSourceQuery<>(configSource, ExposureFunctions.class, configVersionCorrection), securitySource);
  }

  /**
   * @param query Queries the config source
   * @param securitySource The security source, not null
   */
  private ConfigDBInstrumentExposuresProvider(final ConfigSourceQuery<ExposureFunctions> query, final SecuritySource securitySource) {
    ArgumentChecker.notNull(securitySource, "security source");
    _query = query;
    _securitySource = securitySource;
  }

  /**
   * Gets an instrument exposures provider for a function.
   * @param context The function compilation context, not null
   * @param function The function definition, not null
   * @return The instrument exposures provider
   */
  public static ConfigDBInstrumentExposuresProvider init(final FunctionCompilationContext context, final FunctionDefinition function) {
    ArgumentChecker.notNull(context, "context");
    ArgumentChecker.notNull(function, "function");
    return new ConfigDBInstrumentExposuresProvider(ConfigSourceQuery.init(context, function, ExposureFunctions.class), context.getSecuritySource());
  }

  @Override
  public Set<String> getCurveConstructionConfigurationsForConfig(final String instrumentExposureConfigurationName, final FinancialSecurity security) {
    ArgumentChecker.notNull(instrumentExposureConfigurationName, "instrument exposure configuration name");
    ArgumentChecker.notNull(security, "security");
    final ExposureFunctions exposures = _query.get(instrumentExposureConfigurationName);
    if (exposures == null) {
      throw new OpenGammaRuntimeException("Could not get instrument exposure configuration called " + instrumentExposureConfigurationName);
    }
    final List<String> exposureFunctionNames = exposures.getExposureFunctions();
    List<ExternalId> ids = null;
    final Set<String> curveConstructionConfigurationNames = new HashSet<>();
    Multimap<String, ExternalId> functionToIds = LinkedHashMultimap.create();
    for (final String exposureFunctionName : exposureFunctionNames) {
      final ExposureFunction exposureFunction = ExposureFunctionFactory.getExposureFunction(_securitySource, exposureFunctionName);
      ids = security.accept(exposureFunction);
      if (ids != null) {
        final Map<ExternalId, String> idsToNames = exposures.getIdsToNames();
        functionToIds.putAll(exposureFunctionName, ids);
        for (final ExternalId id : ids) {
          final String name = idsToNames.get(id);
          if (name == null) {
            continue;
          }
          curveConstructionConfigurationNames.add(name);
        }
        if (!curveConstructionConfigurationNames.isEmpty()) {
          return curveConstructionConfigurationNames;
        }
      }
    }
    throw new OpenGammaRuntimeException("Could not find a matching list of ids for " + security.getClass().getSimpleName() + "/" + security.getExternalIdBundle() + " from ExposureFunctions object '"
        + instrumentExposureConfigurationName + "'. Ids attempted for referenced functions: " + functionToIds);
  }

}
