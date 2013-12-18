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
    ArgumentChecker.notNull(valuationTime, "valuation time");
    ArgumentChecker.notNull(curveDate, "curve date");
    ArgumentChecker.notNull(curveDefinition, "curve definition");
    if (curveDefinition instanceof InterpolatedCurveDefinition) {
      return getInterpolatedCurveSpecification(valuationTime, curveDate, (InterpolatedCurveDefinition) curveDefinition);
    }
    return getCurveSpecification(valuationTime, curveDate, curveDefinition);
  }

  @Override
  public AbstractCurveSpecification buildSpecification(final Instant valuationTime, final LocalDate curveDate, final AbstractCurveDefinition curveDefinition) {
    ArgumentChecker.notNull(valuationTime, "valuation time");
    ArgumentChecker.notNull(curveDate, "curve date");
    ArgumentChecker.notNull(curveDefinition, "curve definition");
    if (curveDefinition instanceof InterpolatedCurveDefinition) {
      return getInterpolatedCurveSpecification(valuationTime, curveDate, (InterpolatedCurveDefinition) curveDefinition);
    } else if (curveDefinition instanceof CurveDefinition) {
      return getCurveSpecification(valuationTime, curveDate, (CurveDefinition) curveDefinition);
    } else if (curveDefinition instanceof ConstantCurveDefinition) {
      return getConstantCurveSpecification(valuationTime, curveDate, (ConstantCurveDefinition) curveDefinition);
    }
    throw new UnsupportedOperationException("Cannot handle curve definitions of type " + curveDefinition.getClass());
  }

  /**
   * Creates a {@link CurveSpecification}.
   * @param valuationTime The valuation time
   * @param curveDate The curve date
   * @param curveDefinition The curve definition
   * @return The curve specification
   */
  private CurveSpecification getCurveSpecification(final Instant valuationTime, final LocalDate curveDate, final CurveDefinition curveDefinition) {
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

  /**
   * Creates a {@link InterpolatedCurveSpecification}.
   * @param valuationTime The valuation time
   * @param curveDate The curve date
   * @param curveDefinition The curve definition
   * @return The interpolated curve specification
   */
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

  /**
   * Creates a {@link ConstantCurveSpecification}.
   * @param valuationTime The valuation time
   * @param curveDate The curve date
   * @param curveDefinition The curve definition
   * @return The curve specification
   */
  private static AbstractCurveSpecification getConstantCurveSpecification(final Instant valuationTime, final LocalDate curveDate, final ConstantCurveDefinition curveDefinition) {
    final String curveName = curveDefinition.getName();
    return new ConstantCurveSpecification(curveDate, curveName, curveDefinition.getExternalId(), curveDefinition.getDataField());
  }

  /**
   * Gets a {@link CurveNodeIdMapper} from the config source.
   * @param valuationTime The valuation time
   * @param cache A cache of names to curve node id mappers
   * @param curveSpecificationName The curve specification name
   * @return The curve node id mapper
   */
  private CurveNodeIdMapper getCurveNodeIdMapper(final Instant valuationTime, final Map<String, CurveNodeIdMapper> cache, final String curveSpecificationName) {
    CurveNodeIdMapper builderSpecDoc = cache.get(curveSpecificationName);
    if (builderSpecDoc != null) {
      return builderSpecDoc;
    }
    builderSpecDoc = _configSource.getSingle(CurveNodeIdMapper.class, curveSpecificationName, VersionCorrection.LATEST);
    if (builderSpecDoc != null) {
      cache.put(curveSpecificationName, builderSpecDoc);
    }
    return builderSpecDoc;
  }
}
