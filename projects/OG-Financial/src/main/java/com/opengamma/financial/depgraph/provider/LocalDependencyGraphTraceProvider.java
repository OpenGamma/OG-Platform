/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.depgraph.provider;

import org.threeten.bp.Instant;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.depgraph.rest.DependencyGraphBuildTrace;
import com.opengamma.financial.depgraph.rest.DependencyGraphTraceBuilder;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * Local implementation. Simply delegates to trace builder class.
 */
public class LocalDependencyGraphTraceProvider implements DependencyGraphTraceProvider {
  
  private final DependencyGraphTraceBuilder _traceBuilder;
  
  public LocalDependencyGraphTraceProvider(DependencyGraphTraceBuilder traceBuilder) {
    _traceBuilder = traceBuilder;
  }
  
  /**
   * @return the configured trace builder
   */
  public DependencyGraphTraceBuilder getTraceBuilder() {
    return _traceBuilder;
  }

  @Override
  public DependencyGraphBuildTrace getTraceWithValuationTime(Instant valuationTime) {
    return _traceBuilder.valuationTime(valuationTime).build();
  }

  @Override
  public DependencyGraphBuildTrace getTraceWithResolutionTime(VersionCorrection resolutionTime) {
    return _traceBuilder.resolutionTime(resolutionTime).build();
  }

  @Override
  public DependencyGraphBuildTrace getTraceWithCalculationConfigurationName(String calculationConfigurationName) {
    return _traceBuilder.calculationConfigurationName(calculationConfigurationName).build();
  }

  @Override
  public DependencyGraphBuildTrace getTraceWithDefaultProperties(ValueProperties defaultProperties) {
    return _traceBuilder.defaultProperties(defaultProperties).build();
  }

  @Override
  public DependencyGraphBuildTrace getTraceWithMarketData(MarketDataSpecification marketData) {
    return _traceBuilder.marketData(marketData).build();
  }

  @Override
  public DependencyGraphBuildTrace getTraceWithValueRequirementByUniqueId(String valueName, String targetType, UniqueId uniqueId) {
    ValueRequirement valueRequirement = toValueRequirement(valueName, new ComputationTargetSpecification(ComputationTargetType.parse(targetType), uniqueId));
    return _traceBuilder.addRequirement(valueRequirement).build();
  }

  @Override
  public DependencyGraphBuildTrace getTraceWithValueRequirementByExternalId(String valueName, String targetType, ExternalId externalId) {
    ValueRequirement valueRequirement = toValueRequirement(valueName, new ComputationTargetRequirement(ComputationTargetType.parse(targetType), externalId));
    return _traceBuilder.addRequirement(valueRequirement).build();
  }

  private ValueRequirement toValueRequirement(final String valueName, final ComputationTargetReference target) {
    final String name;
    final ValueProperties constraints;
    final int i = valueName.indexOf('{');
    if ((i > 0) && (valueName.charAt(valueName.length() - 1) == '}')) {
      name = valueName.substring(0, i);
      constraints = ValueProperties.parse(valueName.substring(i));
    } else {
      name = valueName;
      constraints = ValueProperties.none();
    }
    return new ValueRequirement(name, target, constraints);
  }

  
}
