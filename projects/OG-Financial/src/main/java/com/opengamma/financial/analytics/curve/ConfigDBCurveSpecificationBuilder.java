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
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.ContinuouslyCompoundedRateNode;
import com.opengamma.financial.analytics.ircurve.strips.CreditSpreadNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.DiscountFactorNode;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ConfigDBCurveSpecificationBuilder implements CurveSpecificationBuilder {
  private final ConfigSource _configSource;

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
      final CurveNodeIdMapper builderConfig = getBuilderConfig(valuationTime, cache, curveSpecificationName);
      if (builderConfig == null) {
        throw new OpenGammaRuntimeException("Could not get curve node id mapper for curve named " + curveName);
      }
      //TODO replace with visitor
      if (node instanceof CashNode) {
        final ExternalId identifier = builderConfig.getCashNodeId(curveDate, ((CashNode) node).getMaturityTenor());
        identifiers.add(new CurveNodeWithIdentifier(node, identifier));
      } else if (node instanceof CreditSpreadNode) {
        final ExternalId identifier = builderConfig.getCreditSpreadNodeId(curveDate, ((CreditSpreadNode) node).getTenor());
        identifiers.add(new CurveNodeWithIdentifier(node, identifier));
      } else if (node instanceof SwapNode) {
        final ExternalId identifier = builderConfig.getSwapNodeId(curveDate, ((SwapNode) node).getMaturityTenor());
        identifiers.add(new CurveNodeWithIdentifier(node, identifier));
      } else {
        throw new OpenGammaRuntimeException("Could not handle nodes of type " + node);
      }
    }
    return new CurveSpecification(curveDate, curveName, identifiers);
  }

  private InterpolatedCurveSpecification getInterpolatedCurveSpecification(final Instant valuationTime, final LocalDate curveDate, final InterpolatedCurveDefinition curveDefinition) {
    final Map<String, CurveNodeIdMapper> cache = new HashMap<>();
    final Collection<CurveNodeWithIdentifier> identifiers = new ArrayList<>();
    final String curveName = curveDefinition.getName();
    for (final CurveNode node : curveDefinition.getNodes()) {
      final String curveSpecificationName = node.getCurveNodeIdMapperName();
      final CurveNodeIdMapper builderConfig = getBuilderConfig(valuationTime, cache, curveSpecificationName);
      if (builderConfig == null) {
        throw new OpenGammaRuntimeException("Could not get curve node id mapper for curve named " + curveName);
      }
      if (node instanceof CashNode) {
        final ExternalId identifier = builderConfig.getCashNodeId(curveDate, ((CashNode) node).getMaturityTenor());
        identifiers.add(new CurveNodeWithIdentifier(node, identifier));
      } else if (node instanceof ContinuouslyCompoundedRateNode) {
        final ExternalId identifier = builderConfig.getContinuouslyCompoundedRateNodeId(curveDate, ((ContinuouslyCompoundedRateNode) node).getTenor());
        identifiers.add(new CurveNodeWithIdentifier(node, identifier));
      } else if (node instanceof CreditSpreadNode) {
        final ExternalId identifier = builderConfig.getCreditSpreadNodeId(curveDate, ((CreditSpreadNode) node).getTenor());
        identifiers.add(new CurveNodeWithIdentifier(node, identifier));
      } else if (node instanceof DiscountFactorNode) {
        final ExternalId identifier = builderConfig.getDiscountFactorNodeId(curveDate, ((DiscountFactorNode) node).getTenor());
        identifiers.add(new CurveNodeWithIdentifier(node, identifier));
      } else if (node instanceof SwapNode) {
        final ExternalId identifier = builderConfig.getSwapNodeId(curveDate, ((SwapNode) node).getMaturityTenor());
        identifiers.add(new CurveNodeWithIdentifier(node, identifier));
      } else {
        throw new OpenGammaRuntimeException("Could not handle nodes of type " + node);
      }
    }
    final String interpolatorName = curveDefinition.getInterpolatorName();
    final String rightExtrapolatorName = curveDefinition.getRightExtrapolatorName();
    final String leftExtrapolatorName = curveDefinition.getLeftExtrapolatorName();
    return new InterpolatedCurveSpecification(curveDate, curveName, identifiers, interpolatorName, rightExtrapolatorName, leftExtrapolatorName);
  }

  private CurveNodeIdMapper getBuilderConfig(final Instant valuationTime, final Map<String, CurveNodeIdMapper> cache, final String curveSpecificationName) {
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
