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

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * Gets the name(s) of the curve construction configurations to be used in pricing
 * a security.
 */
public class ConfigDBInstrumentExposuresProvider implements InstrumentExposuresProvider {
  /** The configuration source */
  private final ConfigSource _configSource;
  /** The security source */
  private final SecuritySource _securitySource;

  /**
   * @param configSource The config source, not null
   * @param securitySource The security source, not null
   */
  public ConfigDBInstrumentExposuresProvider(final ConfigSource configSource, final SecuritySource securitySource) {
    ArgumentChecker.notNull(configSource, "config source");
    ArgumentChecker.notNull(securitySource, "security source");
    _configSource = configSource;
    _securitySource = securitySource;
  }

  @Override
  public Set<String> getCurveConstructionConfigurationsForConfig(final String instrumentExposureConfigurationName,
      final FinancialSecurity security) {
    ArgumentChecker.notNull(instrumentExposureConfigurationName, "instrument exposure configuration name");
    ArgumentChecker.notNull(security, "security");
    final ExposureFunctions exposures = _configSource.getLatestByName(ExposureFunctions.class, instrumentExposureConfigurationName);
    if (exposures == null) {
      throw new OpenGammaRuntimeException("Could not get instrument exposure configuration called " + instrumentExposureConfigurationName);
    }
    final List<String> exposureFunctionNames = exposures.getExposureFunctions();
    List<ExternalId> ids = null;
    final Set<String> curveConstructionConfigurationNames = new HashSet<>();
    for (final String exposureFunctionName : exposureFunctionNames) {
      final ExposureFunction exposureFunction = ExposureFunctionFactory.getExposureFunction(_securitySource, exposureFunctionName);
      ids = security.accept(exposureFunction);
      if (ids != null) {
        final Map<ExternalId, String> idsToNames = exposures.getIdsToNames();
        for (final ExternalId id : ids) {
          final String name = idsToNames.get(id);
          if (name == null) {
            break;
          }
          curveConstructionConfigurationNames.add(name);
        }
        if (!curveConstructionConfigurationNames.isEmpty()) {
          return curveConstructionConfigurationNames;
        }
      }
    }
    throw new OpenGammaRuntimeException("Could not get ids for " + security + " from " + instrumentExposureConfigurationName);
  }

}
