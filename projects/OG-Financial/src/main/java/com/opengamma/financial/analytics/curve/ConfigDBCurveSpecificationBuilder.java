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
import com.opengamma.financial.analytics.ircurve.strips.CreditSpreadStrip;
import com.opengamma.financial.analytics.ircurve.strips.CurveStrip;
import com.opengamma.financial.analytics.ircurve.strips.CurveStripWithIdentifier;
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
    final Map<String, CurveIdMapper> cache = new HashMap<>();
    final Collection<CurveStripWithIdentifier> securities = new ArrayList<>();
    final String curveName = curveDefinition.getName();
    for (final CurveStrip strip : curveDefinition.getStrips()) {
      final String curveSpecificationName = strip.getCurveSpecificationName();
      final CurveIdMapper builderConfig = getBuilderConfig(cache, curveSpecificationName);
      if (builderConfig == null) {
        throw new OpenGammaRuntimeException("Could not get specification builder configuration for curve named " + curveName);
      }
      if (strip instanceof CreditSpreadStrip) {
        final ExternalId identifier = builderConfig.getCreditSpreadId(curveDate, ((CreditSpreadStrip) strip).getTenor());
        securities.add(new CurveStripWithIdentifier(strip, identifier));
      } else {
        throw new OpenGammaRuntimeException("Can currently only use this code for credit spread strips");
      }
    }
    return new CurveSpecification(curveDate, curveName, curveDefinition, securities);

  }

  private CurveIdMapper getBuilderConfig(final Map<String, CurveIdMapper> cache, final String curveSpecificationName) {
    CurveIdMapper builderSpecDoc = cache.get(curveSpecificationName);
    if (builderSpecDoc != null) {
      return builderSpecDoc;
    }
    builderSpecDoc = _configSource.getLatestByName(CurveIdMapper.class, curveSpecificationName);
    if (builderSpecDoc != null) {
      cache.put(curveSpecificationName, builderSpecDoc);
    }
    return builderSpecDoc;
  }
}
