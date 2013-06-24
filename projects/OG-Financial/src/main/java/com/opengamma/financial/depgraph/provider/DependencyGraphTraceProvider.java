/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.depgraph.provider;

import org.threeten.bp.Instant;

import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.financial.depgraph.rest.DependencyGraphBuildTrace;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;


/**
 * Interface for retrieving instances of {@link DependencyGraphBuildTrace}.
 */
public interface DependencyGraphTraceProvider {

  /**
   * Gets a trace instance with the specified valuation time.
   * @param valuationTime the valuation time to apply
   * @return a graph trace
   */
  DependencyGraphBuildTrace getTraceWithValuationTime(final Instant valuationTime);

  /**
   * Gets a trace instance with the specified resolution time.
   * @param resolutionTime the resolution time to apply
   * @return a graph trace
   */
  DependencyGraphBuildTrace getTraceWithResolutionTime(final VersionCorrection resolutionTime);

  /**
   * Gets a trace instance with the specified calculation configuration name.
   * @param calculationConfigurationName the calculation configuration name to apply.
   * @return a graph trace
   */
  DependencyGraphBuildTrace getTraceWithCalculationConfigurationName(final String calculationConfigurationName);

  /**
   * Gets a trace instance with the specified default properties.
   * @param defaultProperties the default properties to apply.
   * @return a graph trace
   */
  DependencyGraphBuildTrace getTraceWithDefaultProperties(final ValueProperties defaultProperties);

  /**
   * @param valueName the value name
   * @param targetType the target type
   * @param uniqueId the unique id
   * @return a graph trace
   */
  DependencyGraphBuildTrace getTraceWithValueRequirementByUniqueId(String valueName, String targetType, UniqueId uniqueId);

  /**
   * @param valueName the value name
   * @param targetType the target type
   * @param externalId the external id
   * @return a graph trace
   */
  DependencyGraphBuildTrace getTraceWithValueRequirementByExternalId(String valueName, String targetType, ExternalId externalId);
  
  /**
   * Gets a trace instance with the specified market data.
   * @param marketData the market data to apply
   * @return a graph trace
   */
  DependencyGraphBuildTrace getTraceWithMarketData(final UserMarketDataSpecification marketData);

}
