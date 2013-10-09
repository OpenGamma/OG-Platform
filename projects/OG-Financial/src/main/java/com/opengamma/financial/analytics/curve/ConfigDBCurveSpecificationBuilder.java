/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.financial.analytics.curve.credit.CurveSpecificationBuilder;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Builds a curve specification from a curve definition and curve node id mapper
 * stored in a configuration source.
 */
public class ConfigDBCurveSpecificationBuilder implements CurveSpecificationBuilder {
  /** The config source */
  private final ConfigSource _configSource;

  /**
   * @param configSource The config source, not null
   */
  public ConfigDBCurveSpecificationBuilder(final ConfigSource configSource) {
    ArgumentChecker.notNull(configSource, "config source");
    _configSource = configSource;
  }

  @Override
  public CurveSpecification buildCurve(final Instant valuationTime, final LocalDate curveDate, final CurveDefinition curveDefinition) {
    ArgumentChecker.notNull(curveDate, "curve date");
    ArgumentChecker.notNull(curveDefinition, "curve definition");
    if (curveDefinition instanceof InterpolatedCurveDefinition) {
      return getInterpolatedCurveSpecification(valuationTime, curveDate, (InterpolatedCurveDefinition) curveDefinition);
    }
    final Map<String, CurveNodeIdMapper> cache = new HashMap<>();
    final Collection<CurveNodeWithIdentifier> identifiers = new ArrayList<>();
    final String curveName = curveDefinition.getName();
    for (final CurveNode node : curveDefinition.getNodes()) {
      final String curveSpecificationName = node.getCurveNodeIdMapperName();
      final CurveNodeIdMapper builderConfig = getCurveNodeIdMapper(valuationTime, cache, curveSpecificationName);
      if (builderConfig == null) {
        throw new OpenGammaRuntimeException("Could not get curve node id mapper " + curveSpecificationName + " for curve named " + curveName);
      }
      final CurveNodeWithIdentifierBuilder identifierBuilder = new CurveNodeWithIdentifierBuilder(curveDate, builderConfig);
      identifiers.add(node.accept(identifierBuilder));
    }
    return new CurveSpecification(curveDate, curveName, identifiers);
  }

  private InterpolatedCurveSpecification getInterpolatedCurveSpecification(final Instant valuationTime, final LocalDate curveDate, final InterpolatedCurveDefinition curveDefinition) {
    final Map<String, CurveNodeIdMapper> cache = new HashMap<>();
    final Collection<CurveNodeWithIdentifier> identifiers = new ArrayList<>();
    final String curveName = curveDefinition.getName();
    for (final CurveNode node : curveDefinition.getNodes()) {
      final String curveSpecificationName = node.getCurveNodeIdMapperName();
      final CurveNodeIdMapper builderConfig = getCurveNodeIdMapper(valuationTime, cache, curveSpecificationName);
      if (builderConfig == null) {
        throw new OpenGammaRuntimeException("Could not get curve node id mapper for curve named " + curveName);
      }
      final CurveNodeWithIdentifierBuilder identifierBuilder = new CurveNodeWithIdentifierBuilder(curveDate, builderConfig);
      identifiers.add(node.accept(identifierBuilder));
    }
    final String interpolatorName = curveDefinition.getInterpolatorName();
    final String rightExtrapolatorName = curveDefinition.getRightExtrapolatorName();
    final String leftExtrapolatorName = curveDefinition.getLeftExtrapolatorName();
    return new InterpolatedCurveSpecification(curveDate, curveName, identifiers, interpolatorName, rightExtrapolatorName, leftExtrapolatorName);
  }

  private CurveNodeIdMapper getCurveNodeIdMapper(final Instant valuationTime, final Map<String, CurveNodeIdMapper> cache, final String curveSpecificationName) {
    final Instant versionTime = valuationTime.plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS);
    CurveNodeIdMapper builderSpecDoc = cache.get(curveSpecificationName);
    if (builderSpecDoc != null) {
      return builderSpecDoc;
    }
    builderSpecDoc = _configSource.getSingle(CurveNodeIdMapper.class, curveSpecificationName, VersionCorrection.of(versionTime, versionTime));
    if (builderSpecDoc != null) {
      cache.put(curveSpecificationName, builderSpecDoc);
    }
    return builderSpecDoc;
  }
}
