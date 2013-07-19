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

import org.threeten.bp.Instant;
import org.threeten.bp.temporal.ChronoUnit;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Gets the name(s) of the curve construction configurations to be used in pricing
 * a security.
 */
public class ConfigDBInstrumentExposuresProvider implements InstrumentExposuresProvider {
  /** The configuration source */
  private final ConfigSource _configSource;

  /**
   * @param configSource The config source, not null
   */
  public ConfigDBInstrumentExposuresProvider(final ConfigSource configSource) {
    ArgumentChecker.notNull(configSource, "config source");
    _configSource = configSource;
  }

  @Override
  public Set<String> getCurveConstructionConfigurationsForConfig(final String instrumentExposureConfigurationName,
      final FinancialSecurity security, final Instant valuationTime) {
    ArgumentChecker.notNull(instrumentExposureConfigurationName, "instrument exposure configuration name");
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(valuationTime, "valuation time");
    final Instant versionTime = valuationTime.plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS);
    final ExposureFunctions exposures = _configSource.getSingle(ExposureFunctions.class,
        instrumentExposureConfigurationName, VersionCorrection.of(versionTime, versionTime));
    if (exposures == null) {
      throw new OpenGammaRuntimeException("Could not get instrument exposure configuration called " + instrumentExposureConfigurationName);
    }
    final List<ExposureFunction> exposureFunctions = exposures.getExposureFunctions();
    List<ExternalId> ids = null;
    for (final ExposureFunction exposureFunction : exposureFunctions) {
      ids = security.accept(exposureFunction);
      if (ids != null) {
        final Set<String> curveConstructionConfigurationNames = new HashSet<>();
        final Map<ExternalId, String> idsToNames = exposures.getIdsToNames();
        for (final ExternalId id : ids) {
          final String name = idsToNames.get(id);
          if (name == null) {
            throw new OpenGammaRuntimeException("Could not get curve construction configuration name for " + id);
          }
          curveConstructionConfigurationNames.add(name);
        }
        return curveConstructionConfigurationNames;
      }
    }
    throw new OpenGammaRuntimeException("Could not get ids for " + security + " from " + instrumentExposureConfigurationName);
  }

}
