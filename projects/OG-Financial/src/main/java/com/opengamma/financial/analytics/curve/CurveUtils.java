/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.financial.analytics.curve.credit.ConfigDBCurveDefinitionSource;
import com.opengamma.financial.analytics.curve.credit.CurveDefinitionSource;
import com.opengamma.financial.analytics.curve.credit.CurveSpecificationBuilder;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeVisitor;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Contains helper methods for curve construction functions.
 */
public class CurveUtils {

  /**
   * Gets a {@link CurveSpecification} from the configuration database that is valid at a particular time.
   * @param valuationTime The valuation time, not null
   * @param configSource The config source, not null
   * @param curveDate The curve date, not null
   * @param curveName The curve name, not null
   * @return The curve specification
   * @throws OpenGammaRuntimeException if the curve definition is not found.
   */
  public static CurveSpecification getCurveSpecification(final Instant valuationTime, final ConfigSource configSource, final LocalDate curveDate, final String curveName) {
    ArgumentChecker.notNull(valuationTime, "valuation time");
    ArgumentChecker.notNull(configSource, "config source");
    ArgumentChecker.notNull(curveDate, "curve date");
    ArgumentChecker.notNull(curveName, "curve name");
    final Instant versionTime = valuationTime.plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS);
    final CurveDefinitionSource curveDefinitionSource = new ConfigDBCurveDefinitionSource(configSource);
    final CurveDefinition curveDefinition = curveDefinitionSource.getCurveDefinition(curveName, VersionCorrection.of(versionTime, versionTime));
    if (curveDefinition == null) {
      throw new OpenGammaRuntimeException("Could not get curve definition called " + curveName);
    }
    final CurveSpecificationBuilder curveSpecificationBuilder = new ConfigDBCurveSpecificationBuilder(configSource);
    return curveSpecificationBuilder.buildCurve(valuationTime, curveDate, curveDefinition);
  }

  /**
   * Gets the names of all the curves that are to be constructed in this configuration.
   * @param configuration The curve construction configuration, not null
   * @return The names of all of the curves to be constructed
   */
  public static String[] getCurveNamesForConstructionConfiguration(final CurveConstructionConfiguration configuration) {
    ArgumentChecker.notNull(configuration, "configuration");
    final List<String> names = new ArrayList<>();
    for (final CurveGroupConfiguration group : configuration.getCurveGroups()) {
      for (final Map.Entry<String, List<CurveTypeConfiguration>> entry : group.getTypesForCurves().entrySet()) {
        names.add(entry.getKey());
      }
    }
    return names.toArray(new String[names.size()]);
  }

  /**
   * Gets the definitions for all of the curves in a configuration, including any in exogenous configurations.
   * @param configuration The curve construction configuration, not null
   * @param configSource The config source that contains information about any exogenous curve configurations, not null
   * @param valuationTime The valuation time, not null
   * @param conventionSource The convention source, not null
   * @param curveNodeCurrencyVisitor The curve node currency visitor, not null
   * @return An ordered set of currencies for these curves
   * @throws OpenGammaRuntimeException if any of the definitions are not found
   */
  public static Set<Currency> getCurrencies(final CurveConstructionConfiguration configuration, final ConfigSource configSource, final Instant valuationTime,
      final ConventionSource conventionSource, final CurveNodeVisitor<Set<Currency>> curveNodeCurrencyVisitor) {
    ArgumentChecker.notNull(configuration, "configuration");
    ArgumentChecker.notNull(configSource, "config source");
    ArgumentChecker.notNull(valuationTime, "valuation time");
    ArgumentChecker.notNull(conventionSource, "convention source");
    ArgumentChecker.notNull(curveNodeCurrencyVisitor, "curve node currency visitor");
    final Instant versionTime = valuationTime.plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS);
    final CurveDefinitionSource curveDefinitionSource = new ConfigDBCurveDefinitionSource(configSource);
    final Set<Currency> currencies = new TreeSet<>();
    for (final CurveGroupConfiguration group : configuration.getCurveGroups()) {
      for (final Map.Entry<String, List<CurveTypeConfiguration>> entry : group.getTypesForCurves().entrySet()) {
        final String curveName = entry.getKey();
        final CurveDefinition curveDefinition = curveDefinitionSource.getCurveDefinition(curveName, VersionCorrection.of(versionTime, versionTime));
        if (curveDefinition == null) {
          throw new OpenGammaRuntimeException("Could not get curve definition called " + curveName);
        }
        for (final CurveNode node : curveDefinition.getNodes()) {
          currencies.addAll(node.accept(curveNodeCurrencyVisitor));
        }
      }
    }
    final List<String> exogenousConfigurations = configuration.getExogenousConfigurations();
    if (exogenousConfigurations != null && !exogenousConfigurations.isEmpty()) {
      final CurveConstructionConfigurationSource source = new ConfigDBCurveConstructionConfigurationSource(configSource);
      for (final String name : exogenousConfigurations) {
        final CurveConstructionConfiguration exogenousConfiguration = source.getCurveConstructionConfiguration(name, VersionCorrection.of(versionTime, versionTime));
        currencies.addAll(getCurrencies(exogenousConfiguration, configSource, valuationTime, conventionSource, curveNodeCurrencyVisitor));
      }
    }
    return currencies;
  }

}
