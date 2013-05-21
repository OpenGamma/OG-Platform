/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import java.util.Set;

import com.google.common.collect.Iterables;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.money.Currency;

/**
 * Responsible for identifying yield curves within a dependency graph.
 */
public class YieldCurveStructureExtractor extends AbstractDependencyGraphStructureExtractor<YieldCurveKey> {

  /**
   * Creates an extractor for the specified graph.
   * @param graph graph to examine for yield curves
   */
  public YieldCurveStructureExtractor(DependencyGraph graph) {
    super(ValueRequirementNames.YIELD_CURVE_MARKET_DATA, graph);
  }

  @Override
  public YieldCurveKey getStructuredKey(ValueSpecification spec) {
    Currency currency = Currency.of(spec.getTargetSpecification().getUniqueId().getValue());
    String curve = getSingleProperty(spec, ValuePropertyNames.CURVE);
    return new YieldCurveKey(currency, curve);
  }

  private String getSingleProperty(final ValueSpecification spec, final String propertyName) {
    final ValueProperties properties = spec.getProperties();
    final Set<String> curves = properties.getValues(propertyName);
    return Iterables.getOnlyElement(curves);
  }
}
