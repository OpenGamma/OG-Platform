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

import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.financial.analytics.curve.credit.CurveSpecificationBuilder;
import com.opengamma.financial.analytics.ircurve.strips.CreditSpreadNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.id.ExternalId;
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
  public CurveSpecification buildCurve(final LocalDate curveDate, final CurveDefinition curveDefinition) {
    ArgumentChecker.notNull(curveDate, "curve date");
    ArgumentChecker.notNull(curveDefinition, "curve definition");
    final Map<String, CurveNodeIdMapper> cache = new HashMap<>();
    final Collection<CurveNodeWithIdentifier> securities = new ArrayList<>();
    final String curveName = curveDefinition.getName();
    for (final CurveNode nodes : curveDefinition.getNodes()) {
      final String curveSpecificationName = nodes.getCurveNodeIdMapperName();
      final CurveNodeIdMapper builderConfig = getBuilderConfig(cache, curveSpecificationName);
      if (builderConfig == null) {
        throw new OpenGammaRuntimeException("Could not get curve node id mapper for curve named " + curveName);
      }
      if (nodes instanceof CreditSpreadNode) {
        final ExternalId identifier = builderConfig.getCreditSpreadId(curveDate, ((CreditSpreadNode) nodes).getTenor());
        securities.add(new CurveNodeWithIdentifier(nodes, identifier));
      } else {
        throw new OpenGammaRuntimeException("Can currently only use this code for credit spread strips");
      }
    }
    return new CurveSpecification(curveDate, curveName, curveDefinition, securities);

  }

  private CurveNodeIdMapper getBuilderConfig(final Map<String, CurveNodeIdMapper> cache, final String curveSpecificationName) {
    CurveNodeIdMapper builderSpecDoc = cache.get(curveSpecificationName);
    if (builderSpecDoc != null) {
      return builderSpecDoc;
    }
    builderSpecDoc = _configSource.getLatestByName(CurveNodeIdMapper.class, curveSpecificationName);
    if (builderSpecDoc != null) {
      cache.put(curveSpecificationName, builderSpecDoc);
    }
    return builderSpecDoc;
  }
}
